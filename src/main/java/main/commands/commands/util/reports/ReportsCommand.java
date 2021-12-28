package main.commands.commands.util.reports;

import lombok.SneakyThrows;
import main.commands.CommandContext;
import main.commands.ICommand;
import main.main.Robertify;
import main.utils.GeneralUtils;
import main.utils.component.InteractiveCommand;
import main.utils.database.mongodb.cache.BotInfoCache;
import main.utils.database.sqlite3.BotDB;
import main.utils.json.legacy.reports.LegacyReportsConfig;
import main.utils.json.legacy.reports.ReportsConfigField;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class ReportsCommand extends InteractiveCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(ReportsCommand.class);

    static final List<Long> activeReports = new ArrayList<>();

    @Override
    public void handle(CommandContext ctx) throws ScriptException {
        final List<String> args = ctx.getArgs();
        final User user = ctx.getAuthor();
        final Message msg = ctx.getMessage();

        if (args.isEmpty()) {
            sendReport(user, msg);
        } else {
            switch (args.get(0).toLowerCase()) {
                case "setup" -> setup(msg);
                case "address", "addr", "sort", "handle" -> address(msg, args);
                case "ban" -> ban(msg, args);
                case "unban" -> unban(msg, args);
                default -> sendReport(user, msg);
            }
        }
    }

    @SneakyThrows
    private void setup(Message msg) {
        if (!BotInfoCache.getInstance().isDeveloper(msg.getAuthor().getIdLong())) return;

        final var config = BotInfoCache.getInstance();

        if (config.isReportsSetup()) {
            msg.replyEmbeds(EmbedUtils.embedMessage("The reports category has already been setup!").build())
                    .queue();
            return;
        }

        final var guild = msg.getGuild();

        guild.createCategory("Bug Reports").queue(category -> {
            category.upsertPermissionOverride(guild.getPublicRole())
                    .setDeny(Permission.VIEW_CHANNEL)
                    .queue(success -> {
                        guild.createTextChannel("opened-reports", category).queue(openedChannel -> {
                            config.initReportChannels(category.getIdLong(), openedChannel.getIdLong());
                            msg.addReaction("✅").queue();
                        });
                    });
        });
    }

    @SneakyThrows
    public void address(Message msg, List<String> args) {
        if (!BotInfoCache.getInstance().isDeveloper(msg.getAuthor().getIdLong())) return;

        final var config = BotInfoCache.getInstance();

        if (!config.isReportsSetup()) {
            msg.replyEmbeds(EmbedUtils.embedMessage("The reports category has not been setup!").build())
                    .queue();
            return;
        }

        if (args.size() < 3) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You must provide the ID of a report to address" +
                            " and the message to address it with!").build())
                    .queue();
            return;
        }

        String id = args.get(1);
        String developerMsg = String.join(" ", args.subList(2, args.size()));

        if (!GeneralUtils.stringIsID(id)) {
            msg.replyEmbeds(EmbedUtils.embedMessage("Invalid ID!").build())
                    .queue();
            return;
        }

        final var openedRequests = Robertify.api.getTextChannelById(config.getReportsID(ReportsConfigField.CHANNEL));

        openedRequests.retrieveMessageById(id).queue(reportMsg -> {
            final var fields = reportMsg.getEmbeds().get(0).getFields();
            final var reporter = fields.get(0).getValue();
            final var origin = fields.get(1).getValue();
            final var reproduction = fields.get(2).getValue();
            final var comments = fields.get(3).getValue();

            final var user = Robertify.api.getUserById(GeneralUtils.getDigitsOnly(reporter));

            user.openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(EmbedUtils.embedMessageWithTitle(
                        "Bug Reports",
                        "Your bug report has been handled."
                        )
                                .addField("Developer Comments", developerMsg, false)
                                .addBlankField(false)
                                .appendDescription("\n\n**Your Bug Report**\n```\n" +
                                        "Command/Feature Origin\n" + origin + "\n\n" +
                                        "Reproduction of Bug\n" + reproduction + "\n\n" +
                                        "Additional Comments\n" + comments + "```")
                                .build())
                        .queue(success -> {
                            reportMsg.delete().queue();
                            msg.addReaction("✅").queue();
                        }, new ErrorHandler()
                                .handle(ErrorResponse.CANNOT_SEND_TO_USER, ignored -> {}));
            });
        }, new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MESSAGE, e -> {
                    msg.replyEmbeds(EmbedUtils.embedMessage("That ID doesn't belong to any opened report!").build())
                            .queue();
                }));

    }

    private void sendReport(User user, Message msg) {
        final var config = BotInfoCache.getInstance();

        if (config.isUserReportsBanned(user.getIdLong())) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You are banned from making reports!").build())
                    .queue();
            return;
        }

        if (!config.isReportsSetup()) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You can't create a bug report at this time!").build())
                    .queue();
            return;
        }

        if (activeReports.contains(user.getIdLong())) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You must complete your opened report form " +
                    "before submitting another one!").build()).queue();
            return;
        }

        user.openPrivateChannel().queue(channel -> {
            channel.sendMessageEmbeds(
                    EmbedUtils.embedMessageWithTitle(
                                "Bug Reports",
                                    """
                                            Please answer the following questions in order to file a bug report

                                            **NOTE**: If you are providing images, please provide them as **links**. Do **NOT** upload a file, we will not be able to see it at all."""
                    )
                            .build(),
                    new ReportsEvents().getFirstPage(user.getIdLong()).getEmbed()
                    )
                    .queue(success -> {
                        activeReports.add(user.getIdLong());
                        msg.addReaction("✅").queue();
                    }, new ErrorHandler()
                            .handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
                                msg.replyEmbeds(EmbedUtils.embedMessage("You must have your private messages " +
                                        "opened before using this command!").build()).queue();
                            }));
        });
    }

    private void ban(Message msg, List<String> args) {
        if (!isDeveloper(msg.getAuthor())) return;

        if (args.size() < 2) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You must provide the ID of a user to ban").build())
                    .queue();
            return;
        }

        final String id = args.get(1);

        if (!GeneralUtils.stringIsID(id)) {
            msg.replyEmbeds(EmbedUtils.embedMessage("Invalid ID!").build())
                    .queue();
            return;
        }

        final var user = Robertify.api.getUserById(GeneralUtils.getDigitsOnly(id));

        if (user == null) {
            msg.replyEmbeds(EmbedUtils.embedMessage("Invalid user!").build())
                    .queue();
            return;
        }

        try {
            BotInfoCache.getInstance().banReportsUser(user.getIdLong());
            msg.replyEmbeds(EmbedUtils.embedMessage("You have banned "
                            + user.getAsMention() + "from reports").build())
                    .queue();
        } catch (IllegalStateException e) {
            msg.replyEmbeds(EmbedUtils.embedMessage(e.getMessage()).build()).queue();
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            msg.addReaction("❌").queue();
        }
    }

    private void unban(Message msg, List<String> args) {
        if (!isDeveloper(msg.getAuthor())) return;

        if (args.size() < 2) {
            msg.replyEmbeds(EmbedUtils.embedMessage("You must provide the ID of a user to ban").build())
                    .queue();
            return;
        }

        final String id = args.get(1);

        if (!GeneralUtils.stringIsID(id)) {
            msg.replyEmbeds(EmbedUtils.embedMessage("Invalid ID!").build())
                    .queue();
            return;
        }

        final var user = Robertify.api.getUserById(GeneralUtils.getDigitsOnly(id));

        if (user == null) {
            msg.replyEmbeds(EmbedUtils.embedMessage("Invalid user!").build())
                    .queue();
            return;
        }

        try {
            BotInfoCache.getInstance().unbanReportsUser(user.getIdLong());
            msg.replyEmbeds(EmbedUtils.embedMessage("You have unbanned "
                            + user.getAsMention() + "from reports").build())
                    .queue();
        } catch (IllegalStateException e) {
            msg.replyEmbeds(EmbedUtils.embedMessage(e.getMessage()).build()).queue();
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            msg.addReaction("❌").queue();
        }
    }

    @SneakyThrows
    private boolean isDeveloper(User user) {
        return BotInfoCache.getInstance().isDeveloper(user.getIdLong());
    }

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String getHelp(String prefix) {
        return "Found a bug? Report it using this command! Please ensure that your private messages" +
                " are enabled for starting the bug report process and for receiving a response.\n\n" +
                getUsages(prefix);
    }

    @Override
    public String getUsages(String prefix) {
        return "**__Usages__**\n" +
                "`"+prefix+"report`";
    }

    @Override
    public void initCommand() {

    }

    @Override
    public void initCommand(Guild g) {

    }
}