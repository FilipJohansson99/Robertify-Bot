package main.audiohandlers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import main.main.Listener;
import main.utils.database.BotUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    private final static HashMap<Guild, BlockingQueue<AudioTrack>> savedQueue = new HashMap<>();
    public BlockingQueue<AudioTrack> queue;
    public boolean repeating = false;
    public boolean playlistRepeating = false;
    private boolean announceNowPlaying = true;
    private boolean errorOccurred = false;
    private final Guild guild;
    private final TextChannel announcementChannel;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.guild = guild;
        this.announcementChannel = new BotUtils().getAnnouncementChannelObject(this.guild.getIdLong());
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
            if (announceNowPlaying) {
                EmbedBuilder eb = EmbedUtils.embedMessage("Now Playing: `" + track.getInfo().title + "`");
                announcementChannel.sendMessageEmbeds(eb.build()).queue();
            }
        }
    }

    public void nextTrack() {
        if (this.queue.isEmpty())
            if (playlistRepeating)
                this.queue = new LinkedBlockingQueue<>(savedQueue.get(this.guild));

        AudioTrack nextTrack = this.queue.poll();

        nextTrack.setPosition(0L);

        try {
            this.player.stopTrack();
            this.player.startTrack(nextTrack, false);
        } catch (IllegalStateException e) {
            announceNowPlaying = false;
            this.player.stopTrack();
            this.player.startTrack(nextTrack.makeClone(), false);
            announceNowPlaying = true;
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (!errorOccurred) {
            if (repeating) {
                player.playTrack(track.makeClone());
            } else if (endReason.mayStartNext) {
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
        System.out.println("track stuck");
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Listener.LOGGER.error("There was an exception with playing the track. Handling it.");

        errorOccurred = true;

        try {
            announceNowPlaying = false;
            player.stopTrack();
            player.startTrack(track, false);
            announceNowPlaying = true;
        } catch (IllegalStateException e) {
            announceNowPlaying = false;
            player.stopTrack();
            player.startTrack(track.makeClone(), false);
            announceNowPlaying = true;
        }

        errorOccurred = false;
    }

    public void setSavedQueue(Guild guild, BlockingQueue<AudioTrack> queue) {
        BlockingQueue<AudioTrack> savedBlockingQueue = new LinkedBlockingQueue<>(queue);

        for (AudioTrack track : savedBlockingQueue)
            track.setPosition(0L);

        savedQueue.put(guild, savedBlockingQueue);
    }

    public void removeSavedQueue(Guild guild) {
        savedQueue.remove(guild);
    }
}
