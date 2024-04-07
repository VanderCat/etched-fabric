package gg.moonflower.etched.client.render.item;

import com.mojang.blaze3d.systems.RenderSystem;
import gg.moonflower.etched.api.record.AlbumCover;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.common.item.AlbumCoverItem;
import gg.moonflower.etched.core.Etched;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;

/**
 * @author Ocelot
 */
public class AlbumCoverItemRenderer extends BuiltinModelItemRenderer implements ResourceReloader {

    public static final AlbumCoverItemRenderer INSTANCE = new AlbumCoverItemRenderer();
    public static final String FOLDER_NAME = Etched.MOD_ID + "_album_cover";

    private static final ModelIdentifier BLANK_ALBUM_COVER = new ModelIdentifier(new Identifier(Etched.MOD_ID, FOLDER_NAME + "/blank"), "inventory");
    private static final ModelIdentifier DEFAULT_ALBUM_COVER = new ModelIdentifier(new Identifier(Etched.MOD_ID, FOLDER_NAME + "/default"), "inventory");
    private static final Identifier ALBUM_COVER_OVERLAY = new Identifier(Etched.MOD_ID, "textures/item/album_cover_overlay.png");

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final JsonUnbakedModel MODEL = JsonUnbakedModel.deserialize("{\"gui_light\":\"front\",\"textures\":{\"layer0\":\"texture\"},\"display\":{\"ground\":{\"rotation\":[0,0,0],\"translation\":[0,2,0],\"scale\":[0.5,0.5,0.5]},\"head\":{\"rotation\":[0,180,0],\"translation\":[0,13,7],\"scale\":[1,1,1]},\"thirdperson_righthand\":{\"rotation\":[0,0,0],\"translation\":[0,3,1],\"scale\":[0.55,0.55,0.55]},\"firstperson_righthand\":{\"rotation\":[0,-90,25],\"translation\":[1.13,3.2,1.13],\"scale\":[0.68,0.68,0.68]},\"fixed\":{\"rotation\":[0,180,0],\"scale\":[1,1,1]}}}");

    private final Map<NbtCompound, CompletableFuture<ModelData>> covers;
    private CoverData data;

    static {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->  INSTANCE.close());
    }

    private AlbumCoverItemRenderer() {
        super(null, null);
        this.covers = new HashMap<>();
        this.data = null;
    }

    @Deprecated
    public static void init() {
//        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, INSTANCE, new ResourceLocation(Etched.MOD_ID, "builtin_album_cover"));
//        ClientNetworkEvent.DISCONNECT.register((controller, player, connection) -> INSTANCE.close());
    }

    public static NativeImage getOverlayImage() {
        return INSTANCE.data.overlay.getImage();
    }

    private static void renderModelLists(BakedModel model, int combinedLight, int combinedOverlay, MatrixStack matrixStack, VertexConsumer buffer, RenderLayer renderType) {
        Random randomsource = Random.create();

        for (Direction direction : Direction.values()) {
            randomsource.setSeed(42L);
            renderQuadList(matrixStack, buffer, model.getQuads(null, direction, randomsource), combinedLight, combinedOverlay);
        }

        randomsource.setSeed(42L);
        renderQuadList(matrixStack, buffer, model.getQuads(null, null, randomsource), combinedLight, combinedOverlay);
    }

    private static void renderQuadList(MatrixStack matrixStack, VertexConsumer buffer, List<BakedQuad> quads, int combinedLight, int combinedOverlay) {
        MatrixStack.Entry pose = matrixStack.peek();
        for (BakedQuad bakedQuad : quads) {
            buffer.quad(pose, bakedQuad, 1, 1, 1, combinedLight, combinedOverlay);
        }
    }

    private static NativeImage getCoverOverlay(ResourceManager resourceManager) {
        try {
            try (InputStream stream = resourceManager.getResourceOrThrow(AlbumCoverItemRenderer.ALBUM_COVER_OVERLAY).getInputStream()) {
                return NativeImage.read(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();

            NativeImage nativeImage = new NativeImage(16, 16, false);
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (k < 8 ^ l < 8) {
                        nativeImage.setColor(l, k, -524040);
                    } else {
                        nativeImage.setColor(l, k, -16777216);
                    }
                }
            }

            nativeImage.untrack();
            return nativeImage;
        }
    }

    private void close() {
        this.covers.values().forEach(future -> future.thenAcceptAsync(data -> {
            if (!this.data.is(data)) {
                data.close();
            }
        }, task -> RenderSystem.recordRenderCall(task::run)));
        this.covers.clear();
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer preparationBarrier, ResourceManager resourceManager, Profiler preparationsProfiler, Profiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> new CoverData(getCoverOverlay(resourceManager)), backgroundExecutor)
                .thenCompose(preparationBarrier::whenPrepared)
                .thenAcceptAsync(data -> {
                    if (this.data != null) {
                        this.data.close();
                    }
                    this.data = data;
                    this.close();
                }, gameExecutor);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode displayContext, MatrixStack poseStack, VertexConsumerProvider buffer, int packedLight, int packedOverlay) {
        if (stack.isEmpty()) {
            return;
        }
        ModelData model = stack.getSubNbt("CoverRecord") == null ? this.data.blank : this.covers.computeIfAbsent(stack.getSubNbt("CoverRecord"), __ -> {
            ItemStack coverStack = AlbumCoverItem.getCoverStack(stack).orElse(ItemStack.EMPTY);
            if (!coverStack.isEmpty() && coverStack.getItem() instanceof PlayableRecord) {
                return ((PlayableRecord) coverStack.getItem()).getAlbumCover(coverStack, MinecraftClient.getInstance().getNetworkProxy(), MinecraftClient.getInstance().getResourceManager()).thenApply(cover -> ModelData.of(cover).orElse(this.data.defaultCover)).exceptionally(e -> {
                    e.printStackTrace();
                    return this.data.defaultCover;
                });
            }
            return CompletableFuture.completedFuture(this.data.blank);
        }).getNow(this.data.defaultCover);

        poseStack.push();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        model.render(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        poseStack.pop();
    }

    public static class CoverData {

        private final DynamicModelData overlay;
        private final ModelData blank;
        private final ModelData defaultCover;

        private CoverData(NativeImage overlay) {
            this.overlay = new DynamicModelData(overlay);
            this.blank = new BakedModelData(BLANK_ALBUM_COVER);
            this.defaultCover = new BakedModelData(DEFAULT_ALBUM_COVER);
        }

        public void close() {
            this.overlay.close();
            this.blank.close();
            this.defaultCover.close();
        }

        public boolean is(ModelData data) {
            return this.overlay == data || this.blank == data || this.defaultCover == data;
        }
    }

    private static class BakedModelData implements ModelData {

        private final ModelIdentifier model;
        private boolean rendering;

        private BakedModelData(ModelIdentifier model) {
            this.model = model;
        }

        @Override
        public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack matrixStack, VertexConsumerProvider buffer, int packedLight, int combinedOverlay) {
            BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
            BakedModel model = this.rendering ? modelManager.getMissingModel() : modelManager.getModel(this.model);
            this.rendering = true; // Prevent deadlock from repeated rendering
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, transformType, transformType == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || transformType == ModelTransformationMode.THIRD_PERSON_LEFT_HAND, matrixStack, buffer, packedLight, combinedOverlay, model);
            this.rendering = false;
        }

        @Override
        public void close() {
        }
    }

    private static class DynamicModelData extends Sprite implements ModelData {

        private static final Identifier ATLAS = new Identifier(Etched.MOD_ID, DigestUtils.md5Hex(UUID.randomUUID().toString()));
        private BakedModel model;

        private DynamicModelData(NativeImage image) {
            super(ATLAS, new SpriteContents(new Identifier(Etched.MOD_ID, DigestUtils.md5Hex(UUID.randomUUID().toString())), new SpriteDimensions(image.getWidth(), image.getHeight()), image, AnimationResourceMetadata.EMPTY), image.getWidth(), image.getHeight(), 0, 0);
        }

        @Override
        public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack matrixStack, VertexConsumerProvider buffer, int packedLight, int combinedOverlay) {
            BakedModel model = this.getModel();
            if (model.isBuiltin()) {
                return;
            }
            model.getTransformation().getTransformation(transformType).apply(transformType == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || transformType == ModelTransformationMode.THIRD_PERSON_LEFT_HAND, matrixStack);
            matrixStack.translate(-0.5D, -0.5D, -0.5D);
            RenderLayer renderType = RenderLayer.getEntityCutout(this.getContents().getId());
            renderModelLists(model, packedLight, combinedOverlay, matrixStack, ItemRenderer.getDirectItemGlintConsumer(buffer, renderType, false, stack.hasGlint()), renderType);
        }

        @SuppressWarnings({"ConstantValue", "DataFlowIssue"})
        private BakedModel getModel() {
            Identifier name = this.getContents().getId();
            if (this.model == null) {
                Profiler profiler = MinecraftClient.getInstance().getProfiler();
                profiler.push("buildAlbumCoverModel");
                this.model = ITEM_MODEL_GENERATOR.create(material -> this, MODEL).bake(null, MODEL, material -> this, ModelRotation.X0_Y0, name, false);
                profiler.pop();
            }
            TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
            if (textureManager.getOrDefault(name, null) == null) {
                textureManager.registerTexture(name, new NativeImageBackedTexture(this.getImage()));
            }
            return this.model;
        }

        public NativeImage getImage() {
            return this.getContents().image;
        }

        @Override
        public float getAnimationFrameDelta() {
            return 0.0F;
        }

        @Override
        public VertexConsumer getTextureSpecificVertexConsumer(VertexConsumer buffer) {
            return buffer;
        }

        @Override
        public void close() {
            this.getContents().close();
            MinecraftClient.getInstance().getTextureManager().destroyTexture(this.getContents().getId());
        }
    }

    @ApiStatus.Internal
    public interface ModelData {

        static Optional<ModelData> of(AlbumCover cover) {
            if (cover instanceof ModelAlbumCover) {
                return Optional.of(new BakedModelData(((ModelAlbumCover) cover).model()));
            }
            if (cover instanceof ImageAlbumCover) {
                return Optional.of(new DynamicModelData(((ImageAlbumCover) cover).image()));
            }
            return Optional.empty();
        }

        void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack matrixStack, VertexConsumerProvider buffer, int packedLight, int combinedOverlay);

        void close();
    }
}
