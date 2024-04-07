package gg.moonflower.etched.api.sound;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.common.block.AlbumJukeboxBlock;
import gg.moonflower.etched.common.block.RadioBlock;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import gg.moonflower.etched.core.Etched;
import gg.moonflower.etched.core.mixin.client.GuiAccessor;
import gg.moonflower.etched.core.mixin.client.LevelRendererAccessor;
import gg.moonflower.etched.core.registry.EtchedTags;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;

import java.util.*;
import java.util.function.DoubleSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Language;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Tracks entity sounds and all etched playing sounds for the client side.
 *
 * @author Ocelot
 * @since 2.0.0
 */
public class SoundTracker {

    private static final Int2ObjectArrayMap<SoundInstance> ENTITY_PLAYING_SOUNDS = new Int2ObjectArrayMap<>();
    private static final Set<String> FAILED_URLS = new HashSet<>();
    private static final Text RADIO = Text.translatable("sound_source." + Etched.MOD_ID + ".radio");

    static {
        //MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener(event -> FAILED_URLS.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->  FAILED_URLS.clear());
    }

    private static synchronized void setRecordPlayingNearby(World level, BlockPos pos, boolean playing) {
        BlockState state = level.getBlockState(pos);
        if (state.isIn(EtchedTags.AUDIO_PROVIDER) || state.isOf(Blocks.JUKEBOX)) {
            for (LivingEntity livingEntity : level.getNonSpectatingEntities(LivingEntity.class, new Box(pos).expand(3.0D))) {
                livingEntity.setNearbySongPlaying(pos, playing);
            }
        }
    }

    /**
     * Retrieves the sound instance for the specified entity id.
     *
     * @param entity The id of the entity to get a sound for
     * @return The sound for that entity
     */
    @Nullable
    public static SoundInstance getEntitySound(int entity) {
        return ENTITY_PLAYING_SOUNDS.get(entity);
    }

    /**
     * Sets the playing sound for the specified entity.
     *
     * @param entity   The id of the entity to play a sound for
     * @param instance The new sound to play or <code>null</code> to stop
     */
    public static void setEntitySound(int entity, @Nullable SoundInstance instance) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        if (instance == null) {
            SoundInstance old = ENTITY_PLAYING_SOUNDS.remove(entity);
            if (old != null) {
                if (old instanceof StopListeningSound) {
                    ((StopListeningSound) old).stopListening();
                }
                soundManager.stop(old);
            }
        } else {
            ENTITY_PLAYING_SOUNDS.put(entity, instance);
            soundManager.play(instance);
        }
    }

    /**
     * Creates an online sound for the specified entity.
     *
     * @param url                 The url to play
     * @param title               The title of the record
     * @param entity              The entity to play for
     * @param attenuationDistance The attenuation distance of the sound
     * @param stream              Whether to play a stream or regular file
     * @return A new sound instance
     */
    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Text title, Entity entity, int attenuationDistance, boolean stream) {
        return new OnlineRecordSoundInstance(url, entity, attenuationDistance, new MusicDownloadListener(title, entity::getX, entity::getY, entity::getZ) {
            @Override
            public void onSuccess() {
                if (!entity.isAlive() || !ENTITY_PLAYING_SOUNDS.containsKey(entity.getId())) {
                    this.clearComponent();
                } else {
                    if (PlayableRecord.canShowMessage(entity.getX(), entity.getY(), entity.getZ())) {
                        MinecraftClient.getInstance().inGameHud.setRecordPlayingOverlay(title);
                    }
                }
            }

            @Override
            public void onFail() {
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.translatable("record." + Etched.MOD_ID + ".downloadFail", title), true);
                FAILED_URLS.add(url);
            }
        }, stream ? AudioSource.AudioFileType.STREAM : AudioSource.AudioFileType.FILE);
    }

    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Text title, Entity entity, boolean stream) {
        return SoundTracker.getEtchedRecord(url, title, entity, 16, stream);
    }

    /**
     * Creates an online sound for the specified position.
     *
     * @param url                 The url to play
     * @param title               The title of the record
     * @param level               The level to play the record in
     * @param pos                 The position of the record
     * @param attenuationDistance The attenuation distance of the sound
     * @param type                The type of audio to accept
     * @return A new sound instance
     */
    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Text title, ClientWorld level, BlockPos pos, int attenuationDistance, AudioSource.AudioFileType type) {
        BlockState aboveState = level.getBlockState(pos.up());
        boolean muffled = aboveState.isIn(BlockTags.WOOL);
        boolean hidden = !aboveState.isAir();

        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor) MinecraftClient.getInstance().worldRenderer).getPlayingRecords();
        return new OnlineRecordSoundInstance(url, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, muffled ? 2.0F : 4.0F, muffled ? attenuationDistance / 2 : attenuationDistance, new MusicDownloadListener(title, () -> pos.getX() + 0.5, () -> pos.getY() + 0.5, () -> pos.getZ() + 0.5) {
            @Override
            public void onSuccess() {
                if (!playingRecords.containsKey(pos)) {
                    this.clearComponent();
                } else {
                    if (!hidden && PlayableRecord.canShowMessage(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                        MinecraftClient.getInstance().inGameHud.setRecordPlayingOverlay(title);
                    }
                    setRecordPlayingNearby(level, pos, true);
                }
            }

            @Override
            public void onFail() {
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.translatable("record." + Etched.MOD_ID + ".downloadFail", title), true);
                FAILED_URLS.add(url);
            }
        }, type);
    }

    public static AbstractOnlineSoundInstance getEtchedRecord(String url, Text title, ClientWorld level, BlockPos pos, AudioSource.AudioFileType type) {
        return getEtchedRecord(url, title, level, pos, 16, type);
    }

    private static void playRecord(BlockPos pos, SoundInstance sound) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor) MinecraftClient.getInstance().worldRenderer).getPlayingRecords();
        playingRecords.put(pos, sound);
        soundManager.play(sound);
    }

    private static void playNextRecord(ClientWorld level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AlbumJukeboxBlockEntity jukebox)) {
            return;
        }

        jukebox.next();
        playAlbum((AlbumJukeboxBlockEntity) blockEntity, blockEntity.getCachedState(), level, pos, true);
    }

    public static void playBlockRecord(BlockPos pos, TrackData[] tracks, int track) {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (level == null) {
            return;
        }

        if (track >= tracks.length) {
            setRecordPlayingNearby(level, pos, false);
            return;
        }

        TrackData trackData = tracks[track];
        String url = trackData.url();
        if (!TrackData.isValidURL(url) || FAILED_URLS.contains(url)) {
            playBlockRecord(pos, tracks, track + 1);
            return;
        }
        playRecord(pos, StopListeningSound.create(getEtchedRecord(url, trackData.getDisplayName(), level, pos, AudioSource.AudioFileType.FILE), () -> MinecraftClient.getInstance().send(() -> {
            if (!((LevelRendererAccessor) MinecraftClient.getInstance().worldRenderer).getPlayingRecords().containsKey(pos)) {
                return;
            }
            playBlockRecord(pos, tracks, track + 1);
        })));
    }

    /**
     * Plays a record stack for an entity.
     *
     * @param record              The record to play
     * @param entityId            The id of the entity to play the record at
     * @param track               The track to play
     * @param attenuationDistance The attenuation distance of the sound
     * @param loop                Whether to loop
     */
    public static void playEntityRecord(ItemStack record, int entityId, int track, int attenuationDistance, boolean loop) {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (level == null) {
            return;
        }

        Entity entity = level.getEntityById(entityId);
        if (entity == null) {
            return;
        }

        Optional<? extends SoundInstance> sound = ((PlayableRecord) record.getItem()).createEntitySound(record, entity, track, attenuationDistance);
        if (sound.isEmpty()) {
            if (loop && track != 0) {
                playEntityRecord(record, entityId, 0, attenuationDistance, true);
            }
            return;
        }

        SoundInstance entitySound = ENTITY_PLAYING_SOUNDS.remove(entity.getId());
        if (entitySound != null) {
            if (entitySound instanceof StopListeningSound) {
                ((StopListeningSound) entitySound).stopListening();
            }
            MinecraftClient.getInstance().getSoundManager().stop(entitySound);
        }

        entitySound = StopListeningSound.create(sound.get(), () -> MinecraftClient.getInstance().send(() -> {
            ENTITY_PLAYING_SOUNDS.remove(entityId);
            playEntityRecord(record, entityId, track + 1, attenuationDistance, loop);
        }));

        ENTITY_PLAYING_SOUNDS.put(entityId, entitySound);
        MinecraftClient.getInstance().getSoundManager().play(entitySound);
    }

    public static void playEntityRecord(ItemStack record, int entityId, int track, boolean loop) {
        SoundTracker.playEntityRecord(record, entityId, track, 16, loop);
    }

    /**
     * Plays a record stack for an entity with a boombox.
     *
     * @param entityId The id of the entity to play the record at
     * @param record   The record to play
     */
    public static void playBoombox(int entityId, ItemStack record) {
        setEntitySound(entityId, null);
        if (!record.isEmpty()) {
            playEntityRecord(record, entityId, 0, 8, true);
        }
    }

    /**
     * Plays the records on an album jukebox in order.
     *
     * @param url   The URL of the stream
     * @param state The block state of the radio
     * @param level The level to play records in
     * @param pos   The position of the jukebox
     */
    public static void playRadio(@Nullable String url, BlockState state, ClientWorld level, BlockPos pos) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor) MinecraftClient.getInstance().worldRenderer).getPlayingRecords();

        SoundInstance soundInstance = playingRecords.get(pos);
        if (soundInstance != null) {
            if (soundInstance instanceof StopListeningSound) {
                ((StopListeningSound) soundInstance).stopListening();
            }
            soundManager.stop(soundInstance);
            playingRecords.remove(pos);
            setRecordPlayingNearby(level, pos, false);
        }

        if (FAILED_URLS.contains(url)) {
            return;
        }
        if (!state.contains(RadioBlock.POWERED) || state.get(RadioBlock.POWERED)) { // Something must already be playing since it would otherwise be -1 and a change would occur
            return;
        }

        if (TrackData.isValidURL(url)) {
            AbstractOnlineSoundInstance record = getEtchedRecord(url, RADIO, level, pos, 8, AudioSource.AudioFileType.BOTH);
            record.setLoop(true); // If the sound is a file, then just continue looping that specific track
            playRecord(pos, record); // Get the new block state
        }
    }

    /**
     * Plays the records on an album jukebox in order.
     *
     * @param jukebox The jukebox to play records
     * @param level   The level to play records in
     * @param pos     The position of the jukebox
     * @param force   Whether to force the jukebox to play
     */
    public static void playAlbum(AlbumJukeboxBlockEntity jukebox, BlockState state, ClientWorld level, BlockPos pos, boolean force) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor) MinecraftClient.getInstance().worldRenderer).getPlayingRecords();

        if (!state.contains(AlbumJukeboxBlock.POWERED) || !state.get(AlbumJukeboxBlock.POWERED) && !force && !jukebox.recalculatePlayingIndex(false)) {// Something must already be playing since it would otherwise be -1 and a change would occur
            return;
        }

        SoundInstance soundInstance = playingRecords.get(pos);
        if (soundInstance != null) {
            if (soundInstance instanceof StopListeningSound) {
                ((StopListeningSound) soundInstance).stopListening();
            }
            soundManager.stop(soundInstance);
            playingRecords.remove(pos);
            setRecordPlayingNearby(level, pos, false);
        }

        if (state.get(AlbumJukeboxBlock.POWERED)) {
            jukebox.stopPlaying();
        }

        if (jukebox.getPlayingIndex() < 0) {// Nothing can be played inside the jukebox
            return;
        }

        ItemStack disc = jukebox.getStack(jukebox.getPlayingIndex());
        SoundInstance sound = null;
        if (disc.getItem() instanceof MusicDiscItem) {
            sound = StopListeningSound.create(getEtchedRecord(((MusicDiscItem) disc.getItem()).getSound().getId().toString(), ((MusicDiscItem) disc.getItem()).getDescription(), level, pos, AudioSource.AudioFileType.FILE), () -> MinecraftClient.getInstance().send(() -> playNextRecord(level, pos)));
        } else if (disc.getItem() instanceof PlayableRecord) {
            Optional<TrackData[]> optional = PlayableRecord.getStackMusic(disc);
            if (optional.isPresent()) {
                TrackData[] tracks = optional.get();
                TrackData track = jukebox.getTrack() < 0 || jukebox.getTrack() >= tracks.length ? tracks[0] : tracks[jukebox.getTrack()];
                String url = track.url();
                if (TrackData.isValidURL(url) && !FAILED_URLS.contains(url)) {
                    sound = StopListeningSound.create(getEtchedRecord(url, track.getDisplayName(), level, pos, AudioSource.AudioFileType.FILE), () -> MinecraftClient.getInstance().send(() -> playNextRecord(level, pos)));
                }
            }
        }

        if (sound == null) {
            return;
        }

        playRecord(pos, sound);
        setRecordPlayingNearby(level, pos, true);
    }

    private static class DownloadTextComponent implements Text {

        private TextContent contents;
        private OrderedText visualOrderText;
        private Language decomposedWith;

        public DownloadTextComponent() {
            this.contents = TextContent.EMPTY;
            this.visualOrderText = OrderedText.EMPTY;
            this.decomposedWith = null;
        }

        @Override
        public TextContent getContent() {
            return this.contents;
        }

        @Override
        public List<Text> getSiblings() {
            return Collections.emptyList();
        }

        @Override
        public Style getStyle() {
            return Style.EMPTY;
        }

        @Override
        public OrderedText asOrderedText() {
            Language language = Language.getInstance();
            if (this.decomposedWith != language) {
                this.visualOrderText = language.reorder(this);
                this.decomposedWith = language;
            }

            return this.visualOrderText;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DownloadTextComponent that = (DownloadTextComponent) o;
            return this.contents.equals(that.contents);
        }

        @Override
        public int hashCode() {
            return this.contents.hashCode();
        }

        @Override
        public String toString() {
            return this.contents.toString();
        }

        public void setText(String text) {
            this.contents = new LiteralTextContent(text);
            this.decomposedWith = null;
        }
    }

    private static abstract class MusicDownloadListener implements DownloadProgressListener {

        private final Text title;
        private final DoubleSupplier x;
        private final DoubleSupplier y;
        private final DoubleSupplier z;
        private final BlockPos.Mutable pos;
        private float size;
        private Text requesting;
        private DownloadTextComponent component;

        protected MusicDownloadListener(Text title, DoubleSupplier x, DoubleSupplier y, DoubleSupplier z) {
            this.title = title;
            this.x = x;
            this.y = y;
            this.z = z;
            this.pos = new BlockPos.Mutable();
        }

        private BlockPos.Mutable getPos() {
            return this.pos.set(this.x.getAsDouble(), this.y.getAsDouble(), this.z.getAsDouble());
        }

        private void setComponent(Text text) {
            if (this.component == null && (MinecraftClient.getInstance().world == null || !MinecraftClient.getInstance().world.getBlockState(this.getPos().move(Direction.UP)).isAir() || !PlayableRecord.canShowMessage(this.x.getAsDouble(), this.y.getAsDouble(), this.z.getAsDouble()))) {
                return;
            }

            if (this.component == null) {
                this.component = new DownloadTextComponent();
                MinecraftClient.getInstance().inGameHud.setOverlayMessage(this.component, true);
                ((GuiAccessor) MinecraftClient.getInstance().inGameHud).setOverlayMessageTime(Short.MAX_VALUE);
            }
            this.component.setText(text.getString());
        }

        protected void clearComponent() {
            if (((GuiAccessor) MinecraftClient.getInstance().inGameHud).getOverlayMessageString() == this.component) {
                ((GuiAccessor) MinecraftClient.getInstance().inGameHud).setOverlayMessageTime(60);
                this.component = null;
            }
        }

        @Override
        public void progressStartRequest(Text component) {
            this.requesting = component;
            this.setComponent(component);
        }

        @Override
        public void progressStartDownload(float size) {
            this.size = size;
            this.requesting = null;
            this.progressStagePercentage(0);
        }

        @Override
        public void progressStagePercentage(int percentage) {
            if (this.requesting != null) {
                this.setComponent(this.requesting.copy().append(" " + percentage + "%"));
            } else if (this.size != 0) {
                this.setComponent(Text.translatable("record." + Etched.MOD_ID + ".downloadProgress", String.format(Locale.ROOT, "%.2f", percentage / 100.0F * this.size), String.format(Locale.ROOT, "%.2f", this.size), this.title));
            }
        }

        @Override
        public void progressStartLoading() {
            this.requesting = null;
            this.setComponent(Text.translatable("record." + Etched.MOD_ID + ".loading", this.title));
        }

        @Override
        public void onFail() {
            MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.translatable("record." + Etched.MOD_ID + ".downloadFail", this.title), true);
        }
    }
}
