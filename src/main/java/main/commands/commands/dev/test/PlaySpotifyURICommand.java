package main.commands.commands.dev.test;

import main.audiohandlers.GuildMusicManager;
import main.audiohandlers.PlayerManager;
import main.commands.CommandContext;
import main.commands.commands.ITestCommand;
import main.exceptions.InvalidSpotifyURIException;
import main.utils.spotify.SpotifyURI;
import net.dv8tion.jda.api.entities.Message;

import javax.script.ScriptException;
import java.util.List;

public class PlaySpotifyURICommand implements ITestCommand {
    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        if (!permissionCheck(ctx)) return;

        final List<String> args = ctx.getArgs();
        final Message msg = ctx.getMessage();

        if (args.isEmpty()) {
            msg.reply("Provide arguments.").queue();
            return;
        }

        try {
            var uri = SpotifyURI.parse(args.get(0));
            PlayerManager.getInstance().joinVoiceChannel(ctx.getSelfMember().getVoiceState(), ctx.getMember().getVoiceState());

            final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
            PlayerManager.getInstance().handleSpotifyURI(
                    uri.toString(),
                    musicManager,
                    ctx.getChannel(),
                    ctx,
                    ctx.getSelfMember().getVoiceState(), ctx.getMember().getVoiceState()
            );
        } catch (InvalidSpotifyURIException e) {
            msg.reply(e.getMessage()).queue();
        }
    }

    @Override
    public String getName() {
        return "playspoturi";
    }

    @Override
    public String getHelp(String guildID) {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return List.of("psu");
    }
}
