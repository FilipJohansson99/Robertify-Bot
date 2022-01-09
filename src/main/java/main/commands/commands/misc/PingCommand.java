package main.commands.commands.misc;

import main.commands.CommandContext;
import main.commands.ICommand;
import main.main.Robertify;
import main.utils.RobertifyEmbedUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import javax.script.ScriptException;

public class PingCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        final Message msg = ctx.getMessage();
        final var guild = ctx.getGuild();

        Robertify.api.getRestPing().queue(
                (ping) -> {
                    EmbedBuilder eb = RobertifyEmbedUtils.embedMessage(guild, "🏓 Pong!\n\n" +
                            "REST Ping: **"+ping+"ms**\n" +
                            "Websocket Ping: **"+Robertify.api.getGatewayPing()+"ms**");
                    msg.replyEmbeds(eb.build()).queue();
                }
        );
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getHelp(String prefix) {
        return "Shows the bot's ping to discord's servers.";
    }
}
