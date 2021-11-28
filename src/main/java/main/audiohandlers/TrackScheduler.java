package main.audiohandlers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import main.commands.commands.management.toggles.togglesconfig.Toggles;
import main.commands.commands.management.toggles.togglesconfig.TogglesConfig;
import main.main.Listener;
import main.utils.database.BotUtils;
import main.utils.json.dedicatedchannel.DedicatedChannelConfig;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    private final static HashMap<Guild, ConcurrentLinkedQueue<AudioTrack>> savedQueue = new HashMap<>();
    @Getter
    private final Stack<AudioTrack> pastQueue;
    public ConcurrentLinkedQueue<AudioTrack> queue;
    public boolean repeating = false;
    public boolean playlistRepeating = false;
    private boolean announceNowPlaying = true;
    private boolean errorOccurred = false;
    @Getter
    private final Guild guild;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.queue = new ConcurrentLinkedQueue<>();
        this.pastQueue = new Stack<>();
        this.guild = guild;
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (!repeating) {
            if (new TogglesConfig().getToggle(guild, Toggles.ANNOUNCE_MESSAGES)) {
                final var requester = PlayerManager.getRequester(track);
                TextChannel announcementChannel = new BotUtils().getAnnouncementChannelObject(this.guild.getIdLong());
                EmbedBuilder eb = EmbedUtils.embedMessage("Now Playing: `" + track.getInfo().title + "` by `"+track.getInfo().author+"`"
                        + (
                        (new TogglesConfig().getToggle(guild, Toggles.SHOW_REQUESTER) && requester != null) ?
                                " [" + requester.getAsMention() + "]"
                                :
                                ""
                ));
                announcementChannel.sendMessageEmbeds(eb.build()).queue();
            }
        }
    }

    public void nextTrack() {
        if (this.queue.isEmpty())
            if (playlistRepeating)
                this.queue = new ConcurrentLinkedQueue<>(savedQueue.get(this.guild));

        AudioTrack nextTrack = this.queue.poll();

        if (nextTrack != null) nextTrack.setPosition(0L);

        try {
            this.player.stopTrack();
            this.player.startTrack(nextTrack, false);
        } catch (IllegalStateException e) {
            announceNowPlaying = false;
            this.player.stopTrack();
            this.player.startTrack(nextTrack.makeClone(), false);
            announceNowPlaying = true;
        }

        if (new DedicatedChannelConfig().isChannelSet(guild.getId()))
            new DedicatedChannelConfig().updateMessage(guild);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (!errorOccurred) {
            if (repeating) {
                if (track != null) {
                    if (PlayerManager.getTrackRequestedByUser().containsKey(track))
                        PlayerManager.removeRequester(track, PlayerManager.getRequester(track));
                    player.playTrack(track.makeClone());
                } else
                    nextTrack();
            } else if (endReason.mayStartNext) {
                pastQueue.push(track.makeClone());
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
        Listener.LOGGER.error("Track stuck. Attemping to replay the song.");
        handleTrackException(player, track);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Listener.LOGGER.error("There was an exception with playing the track. Handling it.");
        exception.printStackTrace();
        handleTrackException(player, track);
    }

    private void handleTrackException(AudioPlayer player, AudioTrack track) {
        errorOccurred = true;

        try {
            announceNowPlaying = false;
            player.stopTrack();
            player.startTrack(track, false);
            announceNowPlaying = true;
            errorOccurred = false;
        } catch (IllegalStateException e) {
            announceNowPlaying = false;
            try {
                player.stopTrack();
                player.startTrack(track.makeClone(), false);
                announceNowPlaying = true;
                errorOccurred = false;
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSavedQueue(Guild guild, ConcurrentLinkedQueue<AudioTrack> queue) {
        ConcurrentLinkedQueue<AudioTrack> savedQueue = new ConcurrentLinkedQueue<>(queue);

        for (AudioTrack track : savedQueue)
            track.setPosition(0L);

        TrackScheduler.savedQueue.put(guild, savedQueue);
    }

    public void addToBeginningOfQueue(AudioTrack track) {
        final ConcurrentLinkedQueue<AudioTrack> newQueue = new ConcurrentLinkedQueue<>();
        newQueue.offer(track);
        newQueue.addAll(queue);
        queue = newQueue;
    }

    public void removeSavedQueue(Guild guild) {
        savedQueue.remove(guild);
    }

    public AudioTrack getLastPlayedTrack() {
        return pastQueue.pop();
    }
}
