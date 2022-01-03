package main.commands.commands.audio.slashcommands;

import main.audiohandlers.RobertifyAudioManager;
import main.commands.commands.audio.ShufflePlayCommand;
import main.main.Listener;
import main.utils.component.InteractiveCommand;
import main.utils.json.dedicatedchannel.DedicatedChannelConfig;
import main.utils.json.guildconfig.GuildConfig;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShufflePlaySlashCommand extends InteractiveCommand {
    private final String commandName = new ShufflePlayCommand().getName();

    @Override
    public void initCommand() {
        setInteractionCommand(getCommand());
        upsertCommand();
    }

    @Override
    public void initCommand(Guild g) {
        setInteractionCommand(getCommand());
        upsertCommand(g);
    }

    private InteractionCommand getCommand() {
        return InteractionCommand.create()
                .setCommand(Command.of(
                        commandName,
                        "Play a playlist/album shuffled right off the bat!",
                        List.of(CommandOption.of(
                                OptionType.STRING,
                                "playlist",
                                "The playlist/album to play",
                                true
                        )),
                        djPredicate
                ))
                .build();
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (!event.getName().equals(commandName)) return;

        if (!getCommand().getCommand().permissionCheck(event)) {
            event.replyEmbeds(EmbedUtils.embedMessage("You need to be a DJ to run this command!").build())
                    .queue();
            return;
        }

        EmbedBuilder eb;
        final Guild guild = event.getGuild();
        final TextChannel channel = event.getTextChannel();

        if (!new GuildConfig().announcementChannelIsSet(guild.getIdLong())) {
            if (new DedicatedChannelConfig().isChannelSet(guild.getIdLong())) {
                if (channel.getIdLong() == new DedicatedChannelConfig().getChannelID(guild.getIdLong())) {
                    event.replyEmbeds(EmbedUtils.embedMessage("You cannot run this command in this channel " +
                                    "without first having an announcement channel set!").build())
                            .setEphemeral(false)
                            .queue();
                    return;
                }
            }
        }

        Listener.checkIfAnnouncementChannelIsSet(guild, channel);

        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        final GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();

        if (!memberVoiceState.inVoiceChannel()) {
            eb = EmbedUtils.embedMessage("You need to be in a voice channel for this to work");
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState.inVoiceChannel() && !memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.replyEmbeds(EmbedUtils.embedMessage("You must be in the same voice channel as me to use this command!")
                            .build())
                    .queue();
            return;
        }

        String url = event.getOption("playlist").getAsString();

        if (!url.contains("deezer.page.link")) {
            if (url.contains("soundcloud.com") && !url.contains("sets")) {
                event.replyEmbeds(EmbedUtils.embedMessage("This SoundCloud URL doesn't contain a playlist!").build()).queue();
                return;
            } else if (url.contains("youtube.com") && !url.contains("playlist") && !url.contains("list")) {
                event.replyEmbeds(EmbedUtils.embedMessage("This YouTube URL doesn't contain a playlist!").build()).queue();
                return;
            } else if (!url.contains("playlist") && !url.contains("album") && !url.contains("soundcloud.com") && !url.contains("youtube.com")) {
                event.replyEmbeds(EmbedUtils.embedMessage("You must provide the link of a valid album/playlist!").build()).queue();
                return;
            }
        }

        event.deferReply().queue();

        event.getHook().sendMessageEmbeds(EmbedUtils.embedMessage("Adding to queue...").build()).queue(addingMsg -> {
            RobertifyAudioManager.getInstance()
                    .loadAndPlayShuffled(url, selfVoiceState, memberVoiceState, addingMsg, event);
        });
    }
}