package main.audiohandlers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import main.audiohandlers.spotify.SpotifyAudioTrack;
import main.commands.commands.audio.LofiCommand;
import main.utils.json.dedicatedchannel.DedicatedChannelConfig;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AudioLoader implements AudioLoadResultHandler {
    private final Logger logger = LoggerFactory.getLogger(AudioLoader.class);

    private final Guild guild;
    private final User sender;
    private final GuildMusicManager musicManager;
    private final boolean announceMsg;
    private final HashMap<AudioTrack, User> trackRequestedByUser;
    private final String trackUrl;
    private final Message botMsg;
    private final boolean loadPlaylistShuffled;

    public AudioLoader(User sender, GuildMusicManager musicManager, HashMap<AudioTrack, User> trackRequestedByUser,
                       String trackUrl, boolean announceMsg, Message botMsg, boolean loadPlaylistShuffled) {
        this.guild = musicManager.scheduler.getGuild();
        this.sender = sender;
        this.musicManager = musicManager;
        this.trackRequestedByUser = trackRequestedByUser;
        this.trackUrl = trackUrl;
        this.announceMsg = announceMsg;
        this.botMsg = botMsg;
        this.loadPlaylistShuffled = loadPlaylistShuffled;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        sendTrackLoadedMessage(audioTrack);

        if (!announceMsg)
            RobertifyAudioManager.getUnannouncedTracks().add(audioTrack);


        trackRequestedByUser.put(audioTrack, sender);
        musicManager.scheduler.queue(audioTrack);

        if (musicManager.scheduler.playlistRepeating)
            musicManager.scheduler.setSavedQueue(guild, musicManager.scheduler.queue);

        if (new DedicatedChannelConfig().isChannelSet(musicManager.scheduler.getGuild().getIdLong()))
            new DedicatedChannelConfig().updateMessage(musicManager.scheduler.getGuild());
    }

    private void sendTrackLoadedMessage(AudioTrack audioTrack) {
        EmbedBuilder eb = EmbedUtils.embedMessage("Added to queue: `" + audioTrack.getInfo().title
                + "` by `" + audioTrack.getInfo().author + "`");

        if (botMsg != null) {
            if (LofiCommand.getLofiEnabledGuilds().contains(guild.getIdLong()) && LofiCommand.getAnnounceLofiMode().contains(guild.getIdLong())) {
                LofiCommand.getAnnounceLofiMode().remove(guild.getIdLong());
                botMsg.editMessageEmbeds(EmbedUtils.embedMessage("You have enabled Lo-Fi mode").build())
                        .queue();
            } else {
                botMsg.editMessageEmbeds(eb.build()).queue();
            }
        } else {
            new DedicatedChannelConfig().getTextChannel(guild.getIdLong())
                    .sendMessageEmbeds(eb.build()).queue();
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        List<AudioTrack> tracks = audioPlaylist.getTracks();

        if (trackUrl.startsWith("ytsearch:")) {
            sendTrackLoadedMessage(tracks.get(0));

            if (!announceMsg)
               RobertifyAudioManager.getUnannouncedTracks().add(tracks.get(0));

            trackRequestedByUser.put(tracks.get(0), sender);
            musicManager.scheduler.queue(tracks.get(0));

            if (musicManager.scheduler.playlistRepeating)
                musicManager.scheduler.setSavedQueue(guild, musicManager.scheduler.queue);

            if (new DedicatedChannelConfig().isChannelSet(musicManager.scheduler.getGuild().getIdLong()))
                new DedicatedChannelConfig().updateMessage(musicManager.scheduler.getGuild());
            return;
        }

        EmbedBuilder eb = EmbedUtils.embedMessage("Added to queue: `" + tracks.size()
                + "` tracks from `" + audioPlaylist.getName() + "`");

        if (botMsg != null)
          botMsg.editMessageEmbeds(eb.build()).queue();
        else {
            new DedicatedChannelConfig().getTextChannel(guild.getIdLong())
                    .sendMessageEmbeds(eb.build()).queue();
        }

        if (!announceMsg)
            for (final AudioTrack track : tracks)
                RobertifyAudioManager.getUnannouncedTracks().add(track);

        if (loadPlaylistShuffled)
            Collections.shuffle(tracks);

        for (final AudioTrack track : tracks) {
            trackRequestedByUser.put(track, sender);
            musicManager.scheduler.queue(track);
        }


        if (musicManager.scheduler.playlistRepeating)
            musicManager.scheduler.setSavedQueue(guild, musicManager.scheduler.queue);

        if (new DedicatedChannelConfig().isChannelSet(musicManager.scheduler.getGuild().getIdLong()))
            new DedicatedChannelConfig().updateMessage(musicManager.scheduler.getGuild());
    }

    @Override
    public void noMatches() {
        EmbedBuilder eb = EmbedUtils.embedMessage("Nothing was found for `" + trackUrl.replace("ytsearch:", "")
                + "`. Try being more specific. *(Adding name of the artiste)*");
        if (botMsg != null)
            botMsg.editMessageEmbeds(eb.build()).queue();
        else {
            new DedicatedChannelConfig().getTextChannel(guild.getIdLong())
                    .sendMessageEmbeds(eb.build()).queue();
        }
    }

    @Override
    public void loadFailed(FriendlyException e) {
        if (musicManager.audioPlayer.getPlayingTrack() == null)
            guild.getAudioManager().closeAudioConnection();

        if (!e.getMessage().contains("available") && !e.getMessage().contains("format"))
            logger.error("[FATAL ERROR] Could not load track!", e);

        EmbedBuilder eb = EmbedUtils.embedMessage(
                        e.getMessage().contains("available") ? e.getMessage() :
                        e.getMessage().contains("format") ? e.getMessage() :
                        "Error loading track"
        );
        if (botMsg != null)
            botMsg.editMessageEmbeds(eb.build()).queue();
        else {
            new DedicatedChannelConfig().getTextChannel(guild.getIdLong())
                    .sendMessageEmbeds(eb.build()).queue();
        }
    }
}
