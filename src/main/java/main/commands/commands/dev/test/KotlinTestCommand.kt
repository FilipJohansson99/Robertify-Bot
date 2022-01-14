package main.commands.commands.dev.test

import main.commands.CommandContext
import main.commands.ITestCommand
import main.utils.RobertifyEmbedUtils

class KotlinTestCommand : ITestCommand {
    override fun handle(ctx: CommandContext) {
        if (!permissionCheck(ctx)) {
            ctx.message.replyEmbeds(
                RobertifyEmbedUtils.embedMessage(ctx.guild, "You do not have permission to run test commands")
                .build()).queue()
            return;
        }

        val message = ctx.message;
        message.reply("Kotlin command 😏").queue()
    }

    override fun getName(): String {
        return "kt";
    }

    override fun getHelp(guildID: String?): String {
        TODO("Not yet implemented")
    }
}