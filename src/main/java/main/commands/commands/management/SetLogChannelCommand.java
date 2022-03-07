package main.commands.commands.management;

import main.commands.CommandContext;
import main.commands.ICommand;
import main.constants.BotConstants;
import main.constants.Permission;
import main.utils.GeneralUtils;
import main.utils.RobertifyEmbedUtils;
import main.utils.json.dedicatedchannel.DedicatedChannelConfig;
import main.utils.json.logs.LogConfig;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.script.ScriptException;
import java.util.List;

public class SetLogChannelCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        final Guild guild = ctx.getGuild();
        final Message message = ctx.getMessage();

        if (!GeneralUtils.hasPerms(guild, ctx.getMember(), Permission.ROBERTIFY_ADMIN)) {
            message.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, BotConstants.getInsufficientPermsMessage(Permission.ROBERTIFY_ADMIN)).build())
                    .queue();
            return;
        }

        final var args = ctx.getArgs();

        if (args.isEmpty()) {
            message.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "You must provide a channel!").build())
                    .queue();
            return;
        }

        final var channelID = args.get(0);

        if (!GeneralUtils.stringIsID(channelID)) {
            message.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "You must provide a valid channel!").build())
                    .queue();
            return;
        }

        final var channel = guild.getTextChannelById(channelID);

        if (channel == null) {
            message.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "There is no such channel with that ID in this server!").build())
                    .queue();
            return;
        }

        DedicatedChannelConfig dedicatedChannelConfig = new DedicatedChannelConfig();
        if (dedicatedChannelConfig.isChannelSet(guild.getIdLong())) {
            if (dedicatedChannelConfig.getChannelID(guild.getIdLong()) == channel.getIdLong()) {
                message.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "The log channel cannot be set to this channel!").build())
                        .queue();
                return;
            }
        }

        final var config = new LogConfig();

        if (config.channelIsSet(guild.getIdLong())) {
            TextChannel oldChannel = config.getChannel(guild.getIdLong());
            config.removeChannel(guild.getIdLong());
           oldChannel.delete().queue();
        }

        config.setChannel(guild.getIdLong(), channel.getIdLong());
        message.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "The logs channel has been set to: " + channel.getAsMention()).build())
                .queue();
    }

    @Override
    public String getName() {
        return "setlogchannel";
    }

    @Override
    public List<String> getAliases() {
        return List.of("slc");
    }

    @Override
    public String getHelp(String prefix) {
        return "Run this command to set where logs should be sent!";
    }
}