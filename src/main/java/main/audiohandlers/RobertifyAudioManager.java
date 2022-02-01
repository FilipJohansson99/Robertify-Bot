package main.audiohandlers;

import lavalink.client.player.track.AudioTrack;
import lombok.Getter;
import lombok.SneakyThrows;
import main.commands.CommandContext;
import main.constants.Toggles;
import main.utils.RobertifyEmbedUtils;
import main.utils.json.toggles.TogglesConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobertifyAudioManager {
    private static final Logger logger = LoggerFactory.getLogger(RobertifyAudioManager.class);

    private static RobertifyAudioManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    @Getter
    /*
    Each guild will have a list that consists of tracks formatted "userid:trackstring"
     */
    private static final HashMap<Long, List<String>> tracksRequestedByUsers = new HashMap<>();
    @Getter
    private static final List<String> unannouncedTracks = new ArrayList<>();

    private RobertifyAudioManager() {
        this.musicManagers = new HashMap<>();
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (gid) -> new GuildMusicManager(guild));
    }

    public void removeMusicManager(Guild guild) {
        this.musicManagers.get(guild.getIdLong()).destroy();
        this.musicManagers.remove(guild.getIdLong());
    }

    @SneakyThrows
    public void loadAndPlay(String trackUrl, GuildVoiceState selfVoiceState,
                            GuildVoiceState memberVoiceState, CommandContext ctx,
                            Message botMsg, boolean addToBeginning) {

        final var musicManager = getMusicManager(memberVoiceState.getGuild());
        if (trackUrl.contains("ytsearch:") && !trackUrl.endsWith("audio"))
            trackUrl += " audio";

        try {
//            joinVoiceChannel(ctx.getChannel(), selfVoiceState, memberVoiceState);
            joinVoiceChannel(ctx.getChannel(), memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }

        loadTrack(
                trackUrl,
                musicManager,
                ctx,
                new TogglesConfig().getToggle(selfVoiceState.getGuild(), Toggles.ANNOUNCE_MESSAGES),
                botMsg,
                addToBeginning
        );
    }

    @SneakyThrows
    public void loadAndPlay(String trackUrl, GuildVoiceState selfVoiceState,
                            GuildVoiceState memberVoiceState, TextChannel channel,
                            User user, Message botMsg, boolean addToBeginning) {

        final var musicManager = getMusicManager(memberVoiceState.getGuild());
        if (trackUrl.contains("ytsearch:") && !trackUrl.endsWith("audio"))
            trackUrl += " audio";

        try {
//            joinVoiceChannel(channel, selfVoiceState, memberVoiceState);
            joinVoiceChannel(channel, memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }

        loadTrack(
                trackUrl,
                musicManager,
                user,
                new TogglesConfig().getToggle(selfVoiceState.getGuild(), Toggles.ANNOUNCE_MESSAGES),
                botMsg,
                addToBeginning
        );
    }

    @SneakyThrows
    public void loadAndPlayShuffled(String trackUrl, GuildVoiceState selfVoiceState,
                            GuildVoiceState memberVoiceState, CommandContext ctx,
                                    Message botMsg, boolean addToBeginning) {
        final var musicManager = getMusicManager(memberVoiceState.getGuild());

        if (trackUrl.contains("ytsearch:") && !trackUrl.endsWith("audio"))
            trackUrl += " audio";

        try {
//            joinVoiceChannel(ctx.getChannel(), selfVoiceState, memberVoiceState);
            joinVoiceChannel(ctx.getChannel(), memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }

        loadPlaylistShuffled(
                trackUrl,
                musicManager,
                ctx,
                new TogglesConfig().getToggle(selfVoiceState.getGuild(), Toggles.ANNOUNCE_MESSAGES),
                botMsg,
                addToBeginning
        );
    }

    @SneakyThrows
    public void loadAndPlayFromDedicatedChannel(String trackUrl, GuildVoiceState selfVoiceState,
                                                GuildVoiceState memberVoiceState, CommandContext ctx, Message botMsg,
                                                boolean addToBeginning) {

        final var musicManager = getMusicManager(memberVoiceState.getGuild());

        if (trackUrl.contains("ytsearch:") && !trackUrl.endsWith("audio"))
            trackUrl += " audio";

        try {
//            joinVoiceChannel(ctx.getChannel(), selfVoiceState, memberVoiceState);
            joinVoiceChannel(ctx.getChannel(), memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }

        loadTrack(
                trackUrl,
                musicManager,
                ctx,
                false,
                botMsg,
                addToBeginning
        );
    }

    public void loadAndPlay(String trackUrl, GuildVoiceState selfVoiceState,
                            GuildVoiceState memberVoiceState, Message botMsg,
                            SlashCommandEvent event, boolean addToBeginning) {
        final var musicManager = getMusicManager(memberVoiceState.getGuild());

        if (trackUrl.contains("ytsearch:") && !trackUrl.endsWith("audio"))
            trackUrl += " audio";

        try {
//            joinVoiceChannel(event.getTextChannel(), selfVoiceState, memberVoiceState);
            joinVoiceChannel(event.getTextChannel(), memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }

        loadTrack(
                trackUrl,
                musicManager,
                new TogglesConfig().getToggle(selfVoiceState.getGuild(), Toggles.ANNOUNCE_MESSAGES),
                botMsg,
                event.getUser(),
                addToBeginning
        );
    }

    public void loadAndPlayShuffled(String trackUrl, GuildVoiceState selfVoiceState,
                            GuildVoiceState memberVoiceState, Message botMsg, SlashCommandEvent event,
                                    boolean addToBeginning) {
        final var musicManager = getMusicManager(memberVoiceState.getGuild());

        if (trackUrl.contains("ytsearch:") && !trackUrl.endsWith("audio"))
            trackUrl += " audio";

        try {
//            joinVoiceChannel(event.getTextChannel(), selfVoiceState, memberVoiceState);
            joinVoiceChannel(event.getTextChannel(), memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }

        loadPlaylistShuffled(
                trackUrl,
                musicManager,
                new TogglesConfig().getToggle(selfVoiceState.getGuild(), Toggles.ANNOUNCE_MESSAGES),
                botMsg,
                event.getUser(),
                addToBeginning
        );
    }

    public void loadAndPlayFromDedicatedChannel(String trackUrl, GuildVoiceState selfVoiceState,
                            GuildVoiceState memberVoiceState, Message botMsg, SlashCommandEvent event,
                                                boolean addToBeginning) {
        final var musicManager = getMusicManager(memberVoiceState.getGuild());

        if (trackUrl.contains("ytsearch:") && !trackUrl.endsWith("audio"))
            trackUrl += " audio";

        try {
//            joinVoiceChannel(event.getTextChannel(), selfVoiceState, memberVoiceState);
            joinVoiceChannel(event.getTextChannel(), memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }

        loadTrack(
                trackUrl,
                musicManager,
                false,
                botMsg,
                event.getUser(),
                addToBeginning
        );
    }

    public void loadAndPlayLocal(TextChannel channel, String path, GuildVoiceState selfVoiceState,
                                 GuildVoiceState memberVoiceState, CommandContext ctx, Message botMsg,
                                 boolean addToBeginning) {
        final var musicManager = getMusicManager(channel.getGuild());

        try {
//            joinVoiceChannel(ctx.getChannel(), selfVoiceState, memberVoiceState);
            joinVoiceChannel(ctx.getChannel(), memberVoiceState.getChannel(), musicManager);
        } catch (Exception e) {
            return;
        }
        loadTrack(
                path,
                musicManager,
                ctx,
                new TogglesConfig().getToggle(selfVoiceState.getGuild(), Toggles.ANNOUNCE_MESSAGES),
                botMsg,
                addToBeginning
        );
    }

    private void loadTrack(String trackUrl, AbstractMusicManager musicManager,
                           User user, boolean announceMsg, Message botMsg,
                           boolean addToBeginning) {

        final AudioLoader loader = new AudioLoader(user, musicManager, tracksRequestedByUsers, trackUrl, announceMsg, botMsg, false, addToBeginning);
        ((GuildMusicManager) musicManager).getLink().getRestClient().loadItem(trackUrl, loader);
    }

    private void loadTrack(String trackUrl, AbstractMusicManager musicManager,
                           CommandContext ctx, boolean announceMsg, Message botMsg,
                           boolean addToBeginning) {

        final AudioLoader loader = new AudioLoader(ctx.getAuthor(), musicManager, tracksRequestedByUsers, trackUrl, announceMsg, botMsg, false, addToBeginning);
        ((GuildMusicManager) musicManager).getLink().getRestClient().loadItem(trackUrl, loader);
    }

    private void loadTrack(String trackUrl, AbstractMusicManager musicManager,
                           boolean announceMsg, Message botMsg, User sender,
                           boolean addToBeginning) {

        final AudioLoader loader = new AudioLoader(sender, musicManager, tracksRequestedByUsers, trackUrl, announceMsg, botMsg, false, addToBeginning);
        ((GuildMusicManager) musicManager).getLink().getRestClient().loadItem(trackUrl, loader);
    }

    private void loadPlaylistShuffled(String trackUrl, AbstractMusicManager musicManager,
                                      CommandContext ctx, boolean announceMsg, Message botMsg,
                                      boolean addToBeginning) {

        final AudioLoader loader = new AudioLoader(ctx.getAuthor(), musicManager, tracksRequestedByUsers, trackUrl, announceMsg, botMsg, true, addToBeginning);
        ((GuildMusicManager) musicManager).getLink().getRestClient().loadItem(trackUrl, loader);
    }

    private void loadPlaylistShuffled(String trackUrl, AbstractMusicManager musicManager,
                                      boolean announceMsg, Message botMsg, User sender,
                                      boolean addToBeginning) {

        final AudioLoader loader = new AudioLoader(sender, musicManager, tracksRequestedByUsers, trackUrl, announceMsg, botMsg, true, addToBeginning);
        ((GuildMusicManager) musicManager).getLink().getRestClient().loadItem(trackUrl, loader);
    }

    public void joinVoiceChannel(TextChannel channel, VoiceChannel vc, GuildMusicManager musicManager) {
        try {
            musicManager.getLink().connect(vc);
        } catch (InsufficientPermissionException e) {
            if (channel != null)
                channel.sendMessageEmbeds(RobertifyEmbedUtils.embedMessage(channel.getGuild(), "I do not have enough permissions to join " + vc.getAsMention()).build())
                        .queue();
            throw e;
        }
    }

    public static String getRequester(Guild guild, AudioTrack track) {
        return "<@" +
                tracksRequestedByUsers.get(guild.getIdLong())
                        .stream()
                        .filter(trackInfo -> trackInfo.split(":")[1].equals(track.getTrack()))
                        .findFirst().get()
                        .split(":")[0]
                + ">";
    }

    public static void removeRequester(Guild guild, AudioTrack track, User requester) {
        tracksRequestedByUsers.get(guild.getIdLong()).remove(requester.getId() + ":" + track.getTrack());
    }

    public static void clearRequesters(Guild guild) {
        tracksRequestedByUsers.remove(guild.getIdLong());
    }

    public static RobertifyAudioManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RobertifyAudioManager();
        return INSTANCE;
    }
}
