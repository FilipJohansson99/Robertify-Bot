package main.commands.commands.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import main.audiohandlers.lavalink.LavaLinkGuildMusicManager;
import main.audiohandlers.lavaplayer.GuildMusicManager;
import main.audiohandlers.RobertifyAudioManager;
import main.commands.CommandContext;
import main.commands.ICommand;
import main.utils.GeneralUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import javax.script.ScriptException;
import java.util.List;

public class ResumeCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        final Message msg = ctx.getMessage();
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        EmbedBuilder eb;

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!selfVoiceState.inVoiceChannel()) {
            eb = EmbedUtils.embedMessage("There is nothing playing!");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        if (!memberVoiceState.inVoiceChannel()) {
            eb = EmbedUtils.embedMessage("You need to be in a voice channel for this to work");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            eb = EmbedUtils.embedMessage("You must be in the same voice channel as me to use this command");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        final var musicManager = RobertifyAudioManager.getInstance().getMusicManager(ctx.getGuild());
        final var audioPlayer = musicManager.getPlayer();

        if (audioPlayer.getPlayingTrack() == null) {
            eb = EmbedUtils.embedMessage("There is nothing playing");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        if (!audioPlayer.isPaused()) {
            eb = EmbedUtils.embedMessage("The player isn't paused!");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        audioPlayer.setPaused(false);
        eb = EmbedUtils.embedMessage("You have resumed the song!");
        msg.replyEmbeds(eb.build()).queue();
    }

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public String getHelp(String prefix) {
        return "Aliases: `"+ GeneralUtils.listToString(getAliases()) +"`\n" +
                "Resumes the currently playing song if paused\n" +
                "\nUsage: `"+ prefix+"resume`";
    }

    @Override
    public List<String> getAliases() {
        return List.of("res");
    }
}
