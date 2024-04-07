package gg.moonflower.etched.core.mixin;

import com.google.common.base.Suppliers;
import gg.moonflower.etched.api.record.AlbumCover;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.client.render.item.AlbumCoverItemRenderer;
import gg.moonflower.etched.client.sound.EntityRecordSoundInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(MusicDiscItem.class)
public abstract class RecordItemMixin extends Item implements PlayableRecord {

    @Shadow
    public abstract SoundEvent getSound();

    @Unique
    private final Supplier<TrackData[]> etched$track = Suppliers.memoize(() -> {
        Text desc = Text.translatable(this.getTranslationKey() + ".desc");

        String[] parts = desc.getString().split("-", 2);
        if (parts.length < 2) {
            return new TrackData[]{new TrackData(this.getSound().getId().toString(), "Minecraft", desc)};
        }
        return new TrackData[]{new TrackData(this.getSound().getId().toString(), parts[0].trim(), Text.literal(parts[1].trim()).fillStyle(desc.getStyle()))};
    });

    private RecordItemMixin(Settings properties) {
        super(properties);
    }

    @Override
    public boolean canPlay(ItemStack stack) {
        return true;
    }

    @ClientOnly
    @Override
    public Optional<? extends SoundInstance> createEntitySound(ItemStack stack, Entity entity, int track, int attenuationDistance) {
        if (track != 0 || !(stack.getItem() instanceof MusicDiscItem record)) {
            return Optional.empty();
        }

        if (PlayableRecord.canShowMessage(entity.getX(), entity.getY(), entity.getZ())) {
            MinecraftClient.getInstance().inGameHud.setRecordPlayingOverlay(record.getDescription());
        }
        return Optional.of(new EntityRecordSoundInstance(record.getSound(), entity));
    }

    @ClientOnly
    @Override
    public CompletableFuture<AlbumCover> getAlbumCover(ItemStack stack, Proxy proxy, ResourceManager resourceManager) {
        Identifier key = ForgeRegistries.ITEMS.getKey(this);
        return resourceManager.getResource(new Identifier(key.getNamespace(), "models/item/" + AlbumCoverItemRenderer.FOLDER_NAME + "/" + key.getPath() + ".json")).isPresent() ?
                CompletableFuture.completedFuture(AlbumCover.of(new Identifier(key.getNamespace(), AlbumCoverItemRenderer.FOLDER_NAME + "/" + key.getPath()))) :
                CompletableFuture.completedFuture(AlbumCover.EMPTY);
    }

    @Override
    public Optional<TrackData[]> getMusic(ItemStack stack) {
        return Optional.of(this.etched$track.get());
    }

    @Override
    public Optional<TrackData> getAlbum(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public int getTrackCount(ItemStack stack) {
        return 1;
    }
}
