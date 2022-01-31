package main.commands.commands.audio.filters;

import lavalink.client.io.filters.Timescale;
import main.audiohandlers.RobertifyAudioManager;
import main.commands.CommandContext;
import main.commands.ICommand;
import main.utils.RobertifyEmbedUtils;

import javax.script.ScriptException;

public class NightcoreFilter implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        // TODO Paywall

        final var guild = ctx.getGuild();
        final var msg = ctx.getMessage();
        final var musicManager = RobertifyAudioManager.getInstance().getMusicManager(guild);
        final var audioPlayer = musicManager.getPlayer();
        final var filters = audioPlayer.getFilters();

        if (filters.getTimescale() != null) {
            filters.setTimescale(null).commit();
            msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "You have turned **off** the **Nightcore** filter").build())
                    .queue();
        } else {
            filters.setTimescale(new Timescale()
                    .setPitch(1.5F)
            ).commit();
            msg.replyEmbeds(RobertifyEmbedUtils.embedMessage(guild, "You have turned **on** the **Nightcore** filter").build())
                    .queue();
        }
    }

    @Override
    public String getName() {
        return "nightcore";
    }

    @Override
    public String getHelp(String prefix) {
        return null;
    }
}
