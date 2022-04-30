package main.commands.slashcommands.commands.audio;

import main.audiohandlers.RobertifyAudioManager;
import main.commands.prefixcommands.CommandContext;
import main.commands.prefixcommands.ICommand;
import main.constants.Toggles;
import main.main.Listener;
import main.utils.GeneralUtils;
import main.utils.RobertifyEmbedUtils;
import main.utils.json.dedicatedchannel.DedicatedChannelConfig;
import main.utils.json.guildconfig.GuildConfig;
import main.utils.json.restrictedchannels.RestrictedChannelsConfig;
import main.utils.json.toggles.TogglesConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import javax.script.ScriptException;
import java.util.List;

public class ShufflePlayCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        final TextChannel channel = ctx.getChannel();
        final List<String> args = ctx.getArgs();
        final Message msg = ctx.getMessage();
        final Member self = ctx.getSelfMember();
        final Guild guild = ctx.getGuild();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        EmbedBuilder eb;

        if (args.isEmpty()) {
            eb = RobertifyEmbedUtils.embedMessage(guild, "You must provide the link of a playlist to play!");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        if (!GeneralUtils.isUrl(args.get(0))) {
            msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "That is an invalid URL! Be sure to provide the URL of a **playlist**").build()).queue();
            return;
        }

        final String url = args.get(0);

        if (!url.contains("deezer.page.link")) {
            if (url.contains("soundcloud.com") && !url.contains("sets")) {
                msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "This SoundCloud URL doesn't contain a playlist!").build()).queue();
                return;
            } else if (url.contains("youtube.com") && !url.contains("playlist") && !url.contains("list")) {
                msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "This YouTube URL doesn't contain a playlist!").build()).queue();
                return;
            } else if (!url.contains("playlist") && !url.contains("album") && !url.contains("soundcloud.com") && !url.contains("youtube.com")) {
                msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "You must provide the link of a valid album/playlist!").build()).queue();
                return;
            }
        }

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inAudioChannel()) {
            eb = RobertifyEmbedUtils.embedMessage(guild, "You need to be in a voice channel for this to work");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        if (selfVoiceState.inAudioChannel() && !memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "You must be in the same voice channel as me to use this command!" + "\n\nI am currently in: " + selfVoiceState.getChannel().getAsMention())
                            .build())
                    .queue();
            return;
        } else if (!selfVoiceState.inAudioChannel()) {
            if (new TogglesConfig().getToggle(ctx.getGuild(), Toggles.RESTRICTED_VOICE_CHANNELS)) {
                final var restrictedChannelsConfig = new RestrictedChannelsConfig();
                if (!restrictedChannelsConfig.isRestrictedChannel(ctx.getGuild().getIdLong(), memberVoiceState.getChannel().getIdLong(), RestrictedChannelsConfig.ChannelType.VOICE_CHANNEL)) {
                    msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "I can't join this channel!" +
                                    (!restrictedChannelsConfig.getRestrictedChannels(
                                            ctx.getGuild().getIdLong(),
                                            RestrictedChannelsConfig.ChannelType.VOICE_CHANNEL
                                    ).isEmpty()
                                            ?
                                            "\n\nI am restricted to only join\n" + restrictedChannelsConfig.restrictedChannelsToString(
                                                    ctx.getGuild().getIdLong(),
                                                    RestrictedChannelsConfig.ChannelType.VOICE_CHANNEL
                                            )
                                            :
                                            "\n\nRestricted voice channels have been toggled **ON**, but there aren't any set!"
                                    )
                            ).build())
                            .queue();
                    return;
                }
            }
        }

        channel.sendMessageEmbeds(RobertifyEmbedUtils.embedMessage(guild, "Adding to queue...").build()).queue(addingMsg -> {
            RobertifyAudioManager.getInstance()
                    .loadAndPlayShuffled(url, selfVoiceState, memberVoiceState, ctx, addingMsg, false);
        });
    }

    @Override
    public String getName() {
        return "shuffleplay";
    }

    @Override
    public String getHelp(String prefix) {
        return "Aliases: `"+ GeneralUtils.listToString(getAliases()) +"`\n\n" +
                "Looking to play a playlist but shuffled right off the bat? This command does that for you.\n\n"
                + getUsages(prefix);
    }

    @Override
    public String getUsages(String prefix) {
        return "**__Usages__**\n" +
                "`"+prefix+"shuffleplay <playlistlink>`";
    }

    @Override
    public List<String> getAliases() {
        return List.of("splay", "spl");
    }
}
