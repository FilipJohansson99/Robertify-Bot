package main.commands.commands.management;

import main.commands.CommandContext;
import main.commands.ICommand;
import main.commands.commands.management.permissions.Permission;
import main.commands.commands.management.toggles.togglesconfig.Toggles;
import main.commands.commands.management.toggles.togglesconfig.TogglesConfig;
import main.utils.GeneralUtils;
import main.utils.database.sqlite3.ServerDB;
import main.utils.json.restrictedchannels.RestrictedChannelsConfig;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.List;

public class RestrictedChannelsCommand implements ICommand {
    private final static Logger logger = LoggerFactory.getLogger(RestrictedChannelsCommand.class);

    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        final Guild guild = ctx.getGuild();

        if (!GeneralUtils.hasPerms(guild, ctx.getAuthor(), Permission.ROBERTIFY_ADMIN)) return;

        final Message msg = ctx.getMessage();
        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            msg.replyEmbeds(EmbedUtils.embedMessage("Insufficient arguments!\n\n**__Usages__**\n" +
                    "`"+ ServerDB.getPrefix(guild.getIdLong()) +"restrictedchannels add <channelID>`\n" +
                    "`"+ ServerDB.getPrefix(guild.getIdLong()) +"restrictedchannels remove <channelID>`\n" +
                    "`"+ ServerDB.getPrefix(guild.getIdLong()) +"restrictedchannels list`").build())
                    .queue();
        } else {
            switch (args.get(0).toLowerCase()) {
                case "add" -> add(msg, args);
                case "remove" -> remove(msg, args);
                case "list" -> list(msg);
                default -> {
                    msg.replyEmbeds(EmbedUtils.embedMessage("Invalid arguments!\n\n**__Usages__**\n" +
                                    "`"+ ServerDB.getPrefix(guild.getIdLong()) +"restrictedchannels add <channelID>`\n" +
                                    "`"+ ServerDB.getPrefix(guild.getIdLong()) +"restrictedchannels remove <channelID>`\n" +
                                    "`"+ ServerDB.getPrefix(guild.getIdLong()) +"restrictedchannels list`").build())
                            .queue();
                }
            }
        }
    }

    public void add(Message msg, List<String> args) {
        if (args.size() < 2) {
            msg.replyEmbeds(EmbedUtils.embedMessage("""
                    You must provide the voice channel you want to restrict
                    **TIP**: You must enable *developer mode* to view the ID of the voice or text channel. (https://bit.ly/32wGtRz)

                    **__Example__**
                    `restrictedchannels add 842795162513965066`
                    `restrictedchannels add #bot-commands`
                    """).build()).queue();
            return;
        }

        final String id = args.get(1);

        if (!GeneralUtils.stringIsID(id)) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You must provide a valid voice channel ID!").build())
                    .queue();
            return;
        }

        final var guild = msg.getGuild();
        final VoiceChannel voiceChannel = guild.getVoiceChannelById(id);
        final TextChannel textChannel = guild.getTextChannelById(id);
        final long channelId;
        final RestrictedChannelsConfig.ChannelType field;

        if (voiceChannel != null) {
            if (!new TogglesConfig().getToggle(guild, Toggles.RESTRICTED_VOICE_CHANNELS)) {
                msg.replyEmbeds(EmbedUtils.embedMessage("""
                                    This feature is toggled **OFF**.

                                    *Looking to toggle this feature on? Do* `toggle restrictedvoice`""")
                                .build())
                        .queue();
                return;
            }

            channelId = voiceChannel.getIdLong();
            field = RestrictedChannelsConfig.ChannelType.VOICE_CHANNEL;
        } else if (textChannel != null) {
            if (!new TogglesConfig().getToggle(guild, Toggles.RESTRICTED_TEXT_CHANNELS)) {
                msg.replyEmbeds(EmbedUtils.embedMessage("""
                                    This feature is toggled **OFF**.

                                    *Looking to toggle this feature on? Do* `toggle restrictedtext`""")
                                .build())
                        .queue();
                return;
            }

            channelId = textChannel.getIdLong();
            field = RestrictedChannelsConfig.ChannelType.TEXT_CHANNEL;
        } else {
            msg.replyEmbeds(EmbedUtils.embedMessage("The ID provided was not of a valid voice or text channel" +
                            " in this server.").build())
                    .queue();
            return;
        }

        try {
            new RestrictedChannelsConfig().addChannel(guild.getId(), channelId, field);
            msg.replyEmbeds(EmbedUtils.embedMessage("You have successfully added <#"
                            + channelId + "> as a restricted "+ field +" channel!").build())
                    .queue();
        } catch (IllegalStateException e) {
            msg.replyEmbeds(EmbedUtils.embedMessage(e.getMessage()).build())
                    .queue();
        } catch (Exception e) {
            logger.error("Unexpected error!", e);
            msg.addReaction("❌").queue();
        }
    }

    public void remove(Message msg, List<String> args) {
        if (args.size() < 2) {
            msg.replyEmbeds(EmbedUtils.embedMessage("""
                    You must provide the voice channel you want to remove from the restriction list
                    **TIP**: You must enable *developer mode* to view the ID of the voice or text channel. (https://bit.ly/32wGtRz)

                    **__Example__**
                    `restrictedchannels remove 842795162513965066`
                    `restrictedchannels remove #bot-commands`
                    """).build()).queue();
            return;
        }

        final String id = args.get(1);

        if (!GeneralUtils.stringIsID(id)) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You must provide a valid voice channel ID!").build())
                    .queue();
            return;
        }

        final var guild = msg.getGuild();
        final VoiceChannel voiceChannel = guild.getVoiceChannelById(id);
        final TextChannel textChannel = guild.getTextChannelById(id);
        final long channelId;
        final RestrictedChannelsConfig.ChannelType field;

        if (voiceChannel != null) {
            if (!new TogglesConfig().getToggle(guild, Toggles.RESTRICTED_VOICE_CHANNELS)) {
                msg.replyEmbeds(EmbedUtils.embedMessage("""
                                    This feature is toggled **OFF**.

                                    *Looking to toggle this feature on? Do* `toggle restrictedvoice`""")
                                .build())
                        .queue();
                return;
            }

            channelId = voiceChannel.getIdLong();
            field = RestrictedChannelsConfig.ChannelType.VOICE_CHANNEL;
        } else if (textChannel != null) {
            if (!new TogglesConfig().getToggle(guild, Toggles.RESTRICTED_TEXT_CHANNELS)) {
                msg.replyEmbeds(EmbedUtils.embedMessage("""
                                    This feature is toggled **OFF**.

                                    *Looking to toggle this feature on? Do* `toggle restrictedtext`""")
                                .build())
                        .queue();
                return;
            }

            channelId = textChannel.getIdLong();
            field = RestrictedChannelsConfig.ChannelType.TEXT_CHANNEL;
        } else {
            msg.replyEmbeds(EmbedUtils.embedMessage("The ID provided was not of a valid voice or text channel" +
                            " in this server.").build())
                    .queue();
            return;
        }

        try {
            new RestrictedChannelsConfig().removeChannel(guild.getId(), channelId, field);
            msg.replyEmbeds(EmbedUtils.embedMessage("You have successfully removed <#"
                            + channelId + "> as a restricted "+ field +" channel!").build())
                    .queue();
        } catch (IllegalStateException e) {
            msg.replyEmbeds(EmbedUtils.embedMessage(e.getMessage()).build())
                    .queue();
        } catch (Exception e) {
            logger.error("Unexpected error!", e);
            msg.addReaction("❌").queue();
        }
    }

    public void list(Message msg) {
        final var config = new RestrictedChannelsConfig();

        try {
            final var tcs = config.restrictedChannelsToString(msg.getGuild().getId(), RestrictedChannelsConfig.ChannelType.TEXT_CHANNEL);
            final var vcs = config.restrictedChannelsToString(msg.getGuild().getId(), RestrictedChannelsConfig.ChannelType.VOICE_CHANNEL);

            EmbedBuilder embedBuilder = EmbedUtils.embedMessage("Listing all restricted channels");
            embedBuilder.addField("Text Channels", tcs == null ? "No channels" : tcs.isEmpty() ? "No channels" : tcs, false);
            embedBuilder.addField("Voice Channels", vcs == null ? "No channels" : vcs.isEmpty() ? "No channels" : vcs, false);

            msg.replyEmbeds(embedBuilder.build()).queue();
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            msg.addReaction("❌").queue();
        }
    }

    @Override
    public String getName() {
        return "restrictedchannels";
    }

    @Override
    public String getHelp(String guildID) {
        return "Aliases: `"+getAliases().toString().replaceAll("[\\[\\]]", "")+"`" +
                "\nRestrict the bot to join voice channels that you set.\n\n" +
                "**__Usages__**\n" +
                "`"+ ServerDB.getPrefix(Long.parseLong(guildID)) +"restrictedchannels add <channelID>`\n" +
                "`"+ ServerDB.getPrefix(Long.parseLong(guildID)) +"restrictedchannels remove <channelID>`\n" +
                "`"+ ServerDB.getPrefix(Long.parseLong(guildID)) +"restrictedchannels list`";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rc", "rchannels");
    }
}
