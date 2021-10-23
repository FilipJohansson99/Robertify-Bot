package main.commands.commands.dev;

import main.commands.CommandContext;
import main.commands.IDevCommand;
import main.utils.GeneralUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.util.List;

public class EvalCommand implements IDevCommand {
    private ScriptEngine engine;

    public EvalCommand() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval("var imports = new JavaImporter(" +
                    "java.io," +
                    "java.lang," +
                    "java.util," +
                    "Packages.net.dv8tion.jda.api," +
                    "Packages.net.dv8tion.jda.api.entities," +
                    "Packages.net.dv8tion.jda.api.entities.impl," +
                    "Packages.net.dv8tion.jda.api.managers," +
                    "Packages.net.dv8tion.jda.api.managers.impl," +
                    "Packages.net.dv8tion.jda.api.utils);");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        if (!ctx.getAuthor().getId().equals("274681651945144321"))
            return;

        final List<String> args = ctx.getArgs();
        final Message msg = ctx.getMessage();
        EmbedBuilder eb = null;
        GeneralUtils.setCustomEmbed(new Color(0, 183, 255));

        if (args.isEmpty()) {
            eb = EmbedUtils.embedMessage("You must provide a snippet to evaluate!");
            msg.replyEmbeds(eb.build()).queue();
            return;
        }

        final String src = String.join(" ", args);

        try {
            engine.put("event", ctx.getEvent());
            engine.put("message", ctx.getMessage());
            engine.put("channel", ctx.getChannel());
            engine.put("args", args);
            engine.put("api", ctx.getJDA());
            engine.put("guild", ctx.getGuild());
            engine.put("member", ctx.getMember());

            ctx.getGuild().createRole().setPermissions();

            Object out = engine.eval(
                    "(function() {\n" +
                            " with (imports) { \n" +
                            src +
                            " \n}" +
                            "\n})();");
            if (out != null) {
                eb = EmbedUtils.embedMessage("```java\n" + src + "```");
                eb.addField("Result", out.toString(), false);
            } else
                eb = EmbedUtils.embedMessage("```java\nExecuted without error.```");

        } catch (Exception e) {
            eb = EmbedUtils.embedMessage("```java\n" + e.getMessage() +"```");
        }

        msg.replyEmbeds(eb.build()).queue();
        msg.delete().queue();

        GeneralUtils.setDefaultEmbed();
    }

    @Override
    public String getName() {
        return "eval";
    }

    @Override
    public String getHelp(String guildID) {
        return null;
    }
}
