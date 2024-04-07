package gg.moonflower.etched.common.menu;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.download.SoundDownloadSource;
import gg.moonflower.etched.api.sound.download.SoundSourceManager;
import gg.moonflower.etched.common.item.*;
import gg.moonflower.etched.common.network.EtchedMessages;
import gg.moonflower.etched.common.network.play.ClientboundInvalidEtchUrlPacket;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.registry.EtchedBlocks;
import gg.moonflower.etched.core.registry.EtchedItems;
import gg.moonflower.etched.core.registry.EtchedMenus;
import gg.moonflower.etched.core.registry.EtchedSounds;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.inventory.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Ocelot, Jackson
 */
public class EtchingMenu extends ScreenHandler {

    public static final Identifier EMPTY_SLOT_MUSIC_DISC = new Identifier(Etched.MOD_ID, "item/empty_etching_table_slot_music_disc");
    public static final Identifier EMPTY_SLOT_MUSIC_LABEL = new Identifier(Etched.MOD_ID, "item/empty_etching_table_slot_music_label");
    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("\\s*;\\s*");
    private static final Cache<String, CompletableFuture<TrackData[]>> DATA_CACHE = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build();
    private static final boolean IGNORE_CACHE = false;
    private static final Set<String> VALID_FORMATS;

    static {
        ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<>();
        builder.add("audio/wav", "audio/x-wav", "audio/opus", "application/ogg", "audio/ogg", "audio/mpeg", "application/octet-stream", "application/binary");
        VALID_FORMATS = builder.build();
    }

    private final ScreenHandlerContext access;
    private final Property labelIndex;
    private final Slot discSlot;
    private final Slot labelSlot;
    private final Slot resultSlot;
    private final Inventory input;
    private final Inventory result;
    private final PlayerEntity player;
    private String url;
    private int urlId;
    private long lastSoundTime;
    private CompletableFuture<?> currentRequest;
    private int currentRequestId;


    public EtchingMenu(int id, PlayerInventory inventory) {
        this(id, inventory, ScreenHandlerContext.EMPTY);
    }

    public EtchingMenu(int id, PlayerInventory inventory, ScreenHandlerContext containerLevelAccess) {
        super(EtchedMenus.ETCHING_MENU.get(), id);
        this.player = inventory.player;
        this.labelIndex = Property.create();
        this.input = new SimpleInventory(2) {
            @Override
            public void markDirty() {
                super.markDirty();
                EtchingMenu.this.onContentChanged(this);
            }
        };
        this.result = new SimpleInventory(1) {
            @Override
            public void markDirty() {
                super.markDirty();
            }
        };

        this.access = containerLevelAccess;

        this.discSlot = this.addSlot(new Slot(this.input, 0, 44, 43) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == EtchedItems.BLANK_MUSIC_DISC.get() || stack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get();
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_SLOT_MUSIC_DISC);
            }
        });
        this.labelSlot = this.addSlot(new Slot(this.input, 1, 62, 43) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof SimpleMusicLabelItem;
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_SLOT_MUSIC_LABEL);
            }
        });

        this.resultSlot = this.addSlot(new Slot(this.result, 0, 116, 43) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                EtchingMenu.this.discSlot.takeStack(1);
                EtchingMenu.this.labelSlot.takeStack(1);
                if (!EtchingMenu.this.discSlot.hasStack() || !EtchingMenu.this.labelSlot.hasStack()) {
                    EtchingMenu.this.labelIndex.set(0);
                }

                EtchingMenu.this.setupResultSlot();
                EtchingMenu.this.sendContentUpdates();

                containerLevelAccess.run((level, pos) -> {
                    long l = level.getTime();
                    if (EtchingMenu.this.lastSoundTime != l) {
                        level.playSound(null, pos, EtchedSounds.UI_ETCHER_TAKE_RESULT.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                        EtchingMenu.this.lastSoundTime = l;
                    }

                });
                super.onTakeItem(player, stack);
            }
        });

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 98 + y * 18));
            }
        }
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inventory, x, 8 + x * 18, 156));
        }

        this.addProperty(this.labelIndex);
    }

    private static void checkStatus(String url) throws IOException {
        URL uri = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) uri.openConnection(Proxy.NO_PROXY);
        if (!uri.getHost().equals("www.dropbox.com")) { // Hack for dropbox returning the wrong content type for head requests
            httpURLConnection.setRequestMethod("HEAD");
        }
        httpURLConnection.setInstanceFollowRedirects(true);
        Map<String, String> map = SoundDownloadSource.getDownloadHeaders();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(httpURLConnection.getResponseCode() + " " + httpURLConnection.getResponseMessage());
        }

        String contentType = httpURLConnection.getContentType();
        if (!VALID_FORMATS.contains(CONTENT_TYPE_PATTERN.split(contentType.toLowerCase(Locale.ROOT))[0])) {
            throw new IOException("Unsupported Content-Type: " + contentType);
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.access.run((level, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.access, player, EtchedBlocks.ETCHING_TABLE.get());
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int index) {
        if (index >= 0 && index < EtchedMusicDiscItem.LabelPattern.values().length) {
            this.labelIndex.set(index);
            this.setupResultSlot();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < 3) {
                if (!this.insertItem(itemStack2, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, 3, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public void onContentChanged(Inventory container) {
        ItemStack discStack = this.discSlot.getStack();
        ItemStack labelStack = this.labelSlot.getStack();
        ItemStack resultStack = this.resultSlot.getStack();

        if (resultStack.isEmpty() && labelStack.isEmpty()) {
            if (!discStack.isEmpty() && discStack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get()) {
                this.labelIndex.set(EtchedMusicDiscItem.getPattern(discStack).ordinal());
            } else {
                this.labelIndex.set(0);
            }
        }

        this.setupResultSlot();
        super.onContentChanged(container);
    }

    private void setupResultSlot() {
        World level = this.player.method_48926();
        if (level.isClient()) {
            return;
        }
        if (this.currentRequest != null && !this.currentRequest.isDone() && this.urlId == this.currentRequestId) {
            return;
        }

        EtchedMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) this.player), new ClientboundInvalidEtchUrlPacket(""));
        this.resultSlot.setStackNoCallbacks(ItemStack.EMPTY);
        if (this.labelIndex.get() >= 0 && this.labelIndex.get() < EtchedMusicDiscItem.LabelPattern.values().length) {
            ItemStack discStack = this.discSlot.getStack();
            ItemStack labelStack = this.labelSlot.getStack();

            if (discStack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get() || (!discStack.isEmpty() && !labelStack.isEmpty())) {
                if (this.url == null && !discStack.isEmpty()) {
                    this.url = PlayableRecord.getStackAlbum(discStack).map(TrackData::url).orElse(null);
                }
                if (!TrackData.isValidURL(this.url)) {
                    return;
                }

                int currentId = this.currentRequestId = this.urlId;
                this.currentRequest = CompletableFuture.supplyAsync(() -> {
                    ItemStack resultStack = new ItemStack(EtchedItems.ETCHED_MUSIC_DISC.get());
                    resultStack.setCount(1);

                    int discColor = 0x515151;
                    int primaryLabelColor = 0xFFFFFF;
                    int secondaryLabelColor = 0xFFFFFF;
                    TrackData[] data = new TrackData[]{TrackData.EMPTY};
                    if (discStack.getItem() == EtchedItems.ETCHED_MUSIC_DISC.get()) {
                        discColor = EtchedMusicDiscItem.getDiscColor(discStack);
                        primaryLabelColor = EtchedMusicDiscItem.getLabelPrimaryColor(discStack);
                        secondaryLabelColor = EtchedMusicDiscItem.getLabelSecondaryColor(discStack);
                        data = PlayableRecord.getStackMusic(discStack).orElse(data);
                    }
                    if (data.length == 1 && !labelStack.isEmpty()) {
                        data[0] = data[0].withTitle(MusicLabelItem.getTitle(labelStack)).withArtist(MusicLabelItem.getAuthor(labelStack));
                    }
                    if (SoundSourceManager.isValidUrl(this.url)) {
                        try {
                            if (IGNORE_CACHE) {
                                DATA_CACHE.invalidateAll();
                            }
                            data = DATA_CACHE.get(this.url, () -> SoundSourceManager.resolveTracks(this.url, null, Proxy.NO_PROXY)).join();
                        } catch (Exception e) {
                            if (!level.isClient()) {
                                EtchedMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) this.player), new ClientboundInvalidEtchUrlPacket(e instanceof CompletionException ? e.getCause().getMessage() : e.getMessage()));
                            }
                            if (e instanceof CompletionException) {
                                throw (CompletionException) e;
                            }
                            throw new CompletionException(e);
                        }
                    } else if (!TrackData.isLocalSound(this.url)) {
                        try {
                            checkStatus(this.url);
                            data = new TrackData[]{data[0].withUrl(this.url)};
                        } catch (UnknownHostException e) {
                            if (!level.isClient()) {
                                EtchedMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) this.player), new ClientboundInvalidEtchUrlPacket("Unknown host: " + this.url));
                            }
                            throw new CompletionException("Invalid URL", e);
                        } catch (Exception e) {
                            if (!level.isClient()) {
                                EtchedMessages.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) this.player), new ClientboundInvalidEtchUrlPacket(e.getLocalizedMessage()));
                            }
                            throw new CompletionException("Invalid URL", e);
                        }
                    }
                    if (discStack.getItem() instanceof BlankMusicDiscItem) {
                        discColor = ((BlankMusicDiscItem) discStack.getItem()).getColor(discStack);
                    }
                    if (labelStack.getItem() instanceof MusicLabelItem) {
                        primaryLabelColor = MusicLabelItem.getLabelColor(labelStack);
                        secondaryLabelColor = primaryLabelColor;
                    } else if (labelStack.getItem() instanceof ComplexMusicLabelItem) {
                        primaryLabelColor = ComplexMusicLabelItem.getPrimaryColor(labelStack);
                        secondaryLabelColor = ComplexMusicLabelItem.getSecondaryColor(labelStack);
                    }

                    for (int i = 0; i < data.length; i++) {
                        TrackData trackData = data[i];
                        if (trackData.artist().equals(TrackData.EMPTY.artist())) {
                            trackData = trackData.withArtist(MusicLabelItem.getAuthor(labelStack));
                        }
                        if (TrackData.isLocalSound(this.url)) {
                            trackData = trackData.withUrl(new Identifier(this.url).toString());
                        }
                        data[i] = trackData;
                    }

                    EtchedMusicDiscItem.setMusic(resultStack, data);
                    EtchedMusicDiscItem.setColor(resultStack, discColor, primaryLabelColor, secondaryLabelColor);
                    EtchedMusicDiscItem.setPattern(resultStack, EtchedMusicDiscItem.LabelPattern.values()[this.labelIndex.get()]);

                    return resultStack;
                }, NetworkUtils.EXECUTOR).thenAcceptAsync(resultStack -> {
                    if (this.urlId == currentId && !ItemStack.areEqual(resultStack, this.resultSlot.getStack()) && !ItemStack.areEqual(resultStack, this.discSlot.getStack())) {
                        this.resultSlot.setStackNoCallbacks(resultStack);
                        this.urlId++;
                        this.urlId %= 1000;
                        this.sendContentUpdates();
                    }
                }, level.getServer()).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
            }
        }
    }

    public int getLabelIndex() {
        return this.labelIndex.get();
    }

    /**
     * Sets the URL for the resulting stack to the specified value.
     *
     * @param string The new URL
     */
    public void setUrl(String string) {
        if (!Objects.equals(this.url, string)) {
            this.url = string;
            this.urlId++;
            this.urlId %= 1000;
            this.setupResultSlot();
        }
    }
}