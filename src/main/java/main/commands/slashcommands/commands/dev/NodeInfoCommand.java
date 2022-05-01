package main.commands.slashcommands.commands.dev;

import lavalink.client.io.RemoteStats;
import lavalink.client.io.jda.JdaLavalink;
import main.audiohandlers.RobertifyAudioManager;
import main.commands.prefixcommands.CommandContext;
import main.commands.prefixcommands.IDevCommand;
import main.main.Robertify;
import main.utils.GeneralUtils;
import main.utils.RobertifyEmbedUtils;
import main.utils.component.interactions.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;
import java.util.List;

public class NodeInfoCommand extends AbstractSlashCommand implements IDevCommand {
    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        if (!permissionCheck(ctx)) return;

        JdaLavalink lavalink = Robertify.getLavalink();
        StringBuilder descBuilder = new StringBuilder();
        for (final var node : lavalink.getNodes()) {
            RemoteStats stats = node.getStats();
            descBuilder.append("**__" + node.getName() + "__**\n```" +
                    "CPU Cores:" + stats.getCpuCores() + "\n" +
                    "Total Lavalink Load: " + stats.getLavalinkLoad() + "%" +
                    "Total System Load: " + stats.getSystemLoad() + "%" +
                    "Memory Allocated: " + stats.getMemAllocated() + "MB" +
                    "Memory Free: " + stats.getMemFree() + "MB" +
                    "Total Players: " + stats.getPlayers() +
                    "Playing Players: " + stats.getPlayingPlayers() +
                    "Uptime: " + GeneralUtils.getDurationString(stats.getUptime()) +
                    "```\n\n");
        }

        ctx.getMessage().replyEmbeds(RobertifyEmbedUtils.embedMessage(ctx.getGuild(), descBuilder.toString()).build())
                .queue();
    }

    @Override
    public String getName() {
        return "nodeinfo";
    }

    @Override
    public String getHelp(String prefix) {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return List.of("ni");
    }

    @Override
    protected void buildCommand() {
        setCommand(
                getBuilder()
                        .setName("nodeinfo")
                        .setDescription("Count all the voice channels Robertify is currenly playing music in!")
                        .setDevCommand()
                        .build()
        );
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!nameCheck(event)) return;

        JdaLavalink lavalink = Robertify.getLavalink();
        StringBuilder descBuilder = new StringBuilder();
        for (final var node : lavalink.getNodes()) {
            RemoteStats stats = node.getStats();
            descBuilder.append("**__" + node.getName() + "__**\n```" +
                            "CPU Cores:" + stats.getCpuCores() + "\n" +
                            "Total Lavalink Load: " + stats.getLavalinkLoad() + "%" +
                            "Total System Load: " + stats.getSystemLoad() + "%" +
                            "Memory Allocated: " + stats.getMemAllocated() + "MB" +
                            "Memory Free: " + stats.getMemFree() + "MB" +
                            "Total Players: " + stats.getPlayers() +
                            "Playing Players: " + stats.getPlayingPlayers() +
                            "Uptime: " + GeneralUtils.getDurationString(stats.getUptime()) +
                    "```\n\n");
        }

        event.replyEmbeds(RobertifyEmbedUtils.embedMessage(event.getGuild(), descBuilder.toString()).build())
                .queue();
    }
}
