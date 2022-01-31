package main.audiohandlers.lavalink;

import lavalink.client.io.Link;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import lavalink.client.player.track.AudioTrack;
import lavalink.client.player.track.AudioTrackEndReason;
import lombok.Getter;
import main.audiohandlers.AbstractTrackScheduler;
import main.audiohandlers.RobertifyAudioManager;
import main.constants.Toggles;
import main.main.Robertify;
import main.utils.RobertifyEmbedUtils;
import main.utils.json.dedicatedchannel.DedicatedChannelConfig;
import main.utils.json.guildconfig.GuildConfig;
import main.utils.json.toggles.TogglesConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.*;

public class TrackScheduler extends PlayerEventListenerAdapter implements AbstractTrackScheduler {
    private final static HashMap<Guild, ConcurrentLinkedQueue<AudioTrack>> savedQueue = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final static HashMap<Long, ScheduledFuture<?>> disconnectExecutors = new HashMap<>();

    private final Guild guild;
    private final Link audioPlayer;
    @Getter
    private final Stack<AudioTrack> pastQueue;
    public ConcurrentLinkedQueue<AudioTrack> queue;
    public boolean repeating = false;
    public boolean playlistRepeating = false;
    private Message lastSentMsg = null;

    public TrackScheduler(Guild guild, Link audioPlayer) {
        this.guild = guild;
        this.audioPlayer = audioPlayer;
        this.queue = new ConcurrentLinkedQueue<>();
        this.pastQueue = new Stack<>();
    }

    public void queue(AudioTrack track) {
        if (getMusicPlayer().getPlayingTrack() != null) {
            queue.offer(track);
        } else {
            getMusicPlayer().playTrack(track);
        }
    }

    public void stop() {
        queue.clear();

        if (audioPlayer.getPlayer().getPlayingTrack() != null)
            audioPlayer.getPlayer().stopTrack();
    }

    @Override
    public void onTrackStart(IPlayer player, AudioTrack track) {
        if (repeating) return;

        if (!new TogglesConfig().getToggle(guild, Toggles.ANNOUNCE_MESSAGES)) return;

        if (!new GuildConfig().announcementChannelIsSet(guild.getIdLong())) return;

        if (RobertifyAudioManager.getUnannouncedTracks().contains(track.getTrack())) {
            RobertifyAudioManager.getUnannouncedTracks().remove(track.getTrack());
            return;
        }

        final var requester = RobertifyAudioManager.getRequester(guild, track);
        TextChannel announcementChannel = Robertify.api.getTextChannelById(new GuildConfig().getAnnouncementChannelID(this.guild.getIdLong()));
        EmbedBuilder eb = RobertifyEmbedUtils.embedMessage(announcementChannel.getGuild(), "Now Playing: `" + track.getInfo().getTitle() + "` by `"+track.getInfo().getAuthor() +"`"
                + (new TogglesConfig().getToggle(guild, Toggles.SHOW_REQUESTER) ?
                "\n\n~ Requested by " + requester
                :
                ""
        ));

        try {
            announcementChannel.sendMessageEmbeds(eb.build())
                    .queue(msg -> {
                        if (lastSentMsg != null)
                            lastSentMsg.delete().queueAfter(3L, TimeUnit.SECONDS, null, new ErrorHandler()
                                    .handle(ErrorResponse.UNKNOWN_MESSAGE, ignored -> {}));
                        lastSentMsg = msg;
                    }, new ErrorHandler()
                            .handle(ErrorResponse.MISSING_PERMISSIONS, e -> announcementChannel.sendMessage(eb.build().getDescription())
                                    .queue(nonEmbedMsg -> {
                                        if (lastSentMsg != null)
                                            lastSentMsg.delete().queueAfter(3L, TimeUnit.SECONDS, null, new ErrorHandler()
                                                    .handle(ErrorResponse.UNKNOWN_MESSAGE, ignored -> {}));
                                    })
                            ));
        } catch (NullPointerException e) {
            new GuildConfig().setAnnouncementChannelID(guild.getIdLong(), -1L);
        } catch (InsufficientPermissionException ignored) {}
    }

    public void nextTrack() {
        if (queue.isEmpty())
            if (playlistRepeating)
                this.queue = new ConcurrentLinkedQueue<>(savedQueue.get(guild));

        AudioTrack nextTrack = queue.poll();

        if (getMusicPlayer().getPlayingTrack() != null)
            getMusicPlayer().stopTrack();

        try {
            if (nextTrack != null)
                getMusicPlayer().playTrack(nextTrack);
        } catch (IllegalStateException e) {
            getMusicPlayer().playTrack(nextTrack);
        }

        if (new DedicatedChannelConfig().isChannelSet(guild.getIdLong()))
            new DedicatedChannelConfig().updateMessage(guild);
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (repeating) {
            if (track != null) {
                try {
                    player.playTrack(track);
                } catch (UnsupportedOperationException e) {
                    player.seekTo(0);
                }
            } else nextTrack();
        } else if (endReason.mayStartNext) {
            pastQueue.push(track);
            nextTrack();
        }
    }

    @Override
    public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs) {
        if (!new TogglesConfig().getToggle(guild, Toggles.ANNOUNCE_MESSAGES)) return;

        if (!new GuildConfig().announcementChannelIsSet(guild.getIdLong())) return;

        TextChannel announcementChannel = Robertify.api.getTextChannelById(new GuildConfig().getAnnouncementChannelID(this.guild.getIdLong()));
        try {
            announcementChannel.sendMessageEmbeds(
                            RobertifyEmbedUtils.embedMessage(
                                            guild,
                                            "`" + track.getInfo().getTitle() + "` by `" + track.getInfo().getAuthor() + "` could not be played!\nSkipped to the next song. (If available)")
                                    .build()
                    )
                    .queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
        } catch (NullPointerException e) {
            new GuildConfig().setAnnouncementChannelID(guild.getIdLong(), -1L);
        } catch (InsufficientPermissionException ignored) {}

        nextTrack();
    }

    @Override
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
        logger.error("There was an exception with playing the track.", exception);
    }

    public void setSavedQueue(Guild guild, ConcurrentLinkedQueue<AudioTrack> queue) {
        ConcurrentLinkedQueue<AudioTrack> savedQueue = new ConcurrentLinkedQueue<>(queue);

        TrackScheduler.savedQueue.put(guild, savedQueue);
    }

    public void clearSavedQueue(Guild guild) {
        savedQueue.remove(guild);
    }

    public void addToBeginningOfQueue(AudioTrack track) {
        final ConcurrentLinkedQueue<AudioTrack> newQueue = new ConcurrentLinkedQueue<>();
        newQueue.offer(track);
        newQueue.addAll(queue);
        queue = newQueue;
    }

    public void addToBeginningOfQueue(List<AudioTrack> tracks) {
        final ConcurrentLinkedQueue<AudioTrack> newQueue = new ConcurrentLinkedQueue<>();
        tracks.forEach(newQueue::offer);
        newQueue.addAll(queue);
        queue = newQueue;
    }

    public void scheduleDisconnect(boolean announceMsg) {
        scheduleDisconnect(announceMsg, 5, TimeUnit.MINUTES);
    }

    public void scheduleDisconnect(boolean announceMsg, long delay, TimeUnit timeUnit) {
        if (new GuildConfig().get247(guild.getIdLong()))
            return;

        ScheduledFuture<?> schedule = executor.schedule(() -> {
            final var channel = guild.getSelfMember().getVoiceState().getChannel();

            if (!new GuildConfig().get247(guild.getIdLong())) {
                if (channel != null) {
                    RobertifyAudioManager.getInstance().getMusicManager(guild)
                                    .leave();
                    disconnectExecutors.remove(guild.getIdLong());

                    final var guildConfig = new GuildConfig();

                    if (guildConfig.announcementChannelIsSet(guild.getIdLong()) && announceMsg)
                        Robertify.api.getTextChannelById(guildConfig.getAnnouncementChannelID(guild.getIdLong()))
                                .sendMessageEmbeds(RobertifyEmbedUtils.embedMessage(guild, "I have left " + channel.getAsMention() + " due to inactivity.").build())
                                .queue(msg -> msg.delete().queueAfter(2, TimeUnit.MINUTES));
                }
            }
        }, delay, timeUnit);

        disconnectExecutors.putIfAbsent(guild.getIdLong(), schedule);
    }

    public void removeSavedQueue(Guild guild) {
        savedQueue.remove(guild);
    }

    public IPlayer getMusicPlayer() {
        return audioPlayer.getPlayer();
    }
}