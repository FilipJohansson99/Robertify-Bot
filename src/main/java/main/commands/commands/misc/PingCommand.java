package main.commands.commands.misc;

import main.commands.CommandContext;
import main.commands.ICommand;
import main.main.Robertify;
import main.utils.RobertifyEmbedUtils;
import main.utils.component.interactions.AbstractSlashCommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;

public class PingCommand extends AbstractSlashCommand implements ICommand {
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

    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("Ping")
                        .setDescription("Check the ping of the bot to discord's servers!")
                        .build()
        );
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (!checks(event)) return;

        Robertify.api.getRestPing().queue(
                (ping) -> {
                    EmbedBuilder eb = RobertifyEmbedUtils.embedMessage(event.getGuild(), "🏓 Pong!\n\n" +
                            "REST Ping: **"+ping+"ms**\n" +
                            "Websocket Ping: **"+Robertify.api.getGatewayPing()+"ms**");
                    event.replyEmbeds(eb.build()).queue();
                }
        );
    }
}
