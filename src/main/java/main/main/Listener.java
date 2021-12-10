package main.main;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.SneakyThrows;
import main.commands.CommandManager;
import main.commands.commands.audio.slashcommands.*;
import main.commands.commands.management.BanCommand;
import main.commands.commands.management.SetChannelCommand;
import main.commands.commands.management.UnbanCommand;
import main.commands.commands.management.permissions.RemoveDJCommand;
import main.commands.commands.management.permissions.SetDJCommand;
import main.commands.commands.management.toggles.togglesconfig.TogglesConfig;
import main.commands.commands.misc.EightBallCommand;
import main.commands.commands.util.HelpCommand;
import main.constants.BotConstants;
import main.utils.database.mongodb.PermissionsDB;
import main.utils.database.sqlite3.AudioDB;
import main.utils.database.sqlite3.BanDB;
import main.utils.database.sqlite3.BotDB;
import main.utils.database.sqlite3.ServerDB;
import main.utils.json.EightBallConfig;
import main.utils.json.AbstractJSONConfig;
import main.utils.json.changelog.ChangeLogConfig;
import main.utils.json.dedicatedchannel.DedicatedChannelConfig;
import main.utils.json.permissions.PermissionsConfig;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Listener extends ListenerAdapter {
    private final CommandManager manager;
    public static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);

    public Listener(EventWaiter waiter) {
        manager = new CommandManager(waiter);
    }

    @SneakyThrows
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        AbstractJSONConfig.initDirectory();
        PermissionsConfig permConfig = new PermissionsConfig();
        TogglesConfig togglesConfig = new TogglesConfig();

        permConfig.initConfig();
        togglesConfig.initConfig();

//        new PermissionsDB().init();
        new ChangeLogConfig().initConfig();
        new DedicatedChannelConfig().initConfig();
        new EightBallConfig().initConfig();

        BanDB.initBannedUserMap();

        for (Guild g : new BotDB().getGuilds()) {
            permConfig.initGuild(g.getId());

            initSlashCommands(g);
            rescheduleUnbans(g);

            LOGGER.info("Watching {}", g.getName());
        }

        ServerDB.initPrefixMap();
        new AudioDB().cacheAllTracks();

        Robertify.api.getPresence().setPresence(Activity.listening("+help"), true);
    }

    @SneakyThrows
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();
        String prefix = ServerDB.getPrefix(event.getGuild().getIdLong());
        String raw = event.getMessage().getContentRaw();

        // Making sure the user isn't a bot or webhook command
        if (user.isBot() || event.isWebhookMessage()) return;

        if (raw.startsWith(prefix) && raw.length() > prefix.length()) {
            if (BanDB.isUserBannedLazy(event.getGuild().getIdLong(), user.getIdLong())) {
                event.getMessage().replyEmbeds(EmbedUtils.embedMessage("You are banned from using commands in this server!").build())
                        .queue();
            } else {
                if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                    event.getMessage().replyEmbeds(EmbedUtils.embedMessage("""
                            I do not have enough permissions to do this!
                            Please give my role the `Manage Messages` permission in order for me to execute this command.

                            *For the recommended permissions please invite the bot using this link: https://bit.ly/3DfaNNl*""").build())
                            .queue();
                    return;
                }

                manager.handle(event);
            }
        }
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (BanDB.isUserBannedLazy(event.getGuild().getIdLong(), event.getUser().getIdLong()))
            event.replyEmbeds(EmbedUtils.embedMessage(BotConstants.BANNED_MESSAGE.toString()).build())
                    .queue();
    }

    @SneakyThrows
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();

        BotDB botUtils = new BotDB();
        BanDB banUtils = new BanDB();

        PermissionsConfig permissionsConfig = new PermissionsConfig();
        TogglesConfig togglesConfig = new TogglesConfig();
        new DedicatedChannelConfig().initConfig();
        new EightBallConfig().initConfig();

        botUtils.addGuild(guild.getIdLong())
                .announceNewTrack(guild.getIdLong(), true)
                .closeConnection();

        permissionsConfig.initGuild(guild.getId());
        togglesConfig.initConfig();

        initSlashCommands(guild);

        LOGGER.info("Joined {}", guild.getName());

        ServerDB.initPrefixMap();
        BanDB.initBannedUserMap();
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        Guild guild = event.getGuild();

        BotDB botUtils = new BotDB();
        botUtils.removeGuild(guild.getIdLong()).closeConnection();

        LOGGER.info("Left {}", guild.getName());
    }

    public void initSlashCommands() {
        new PlaySlashCommand().initCommand();
        new QueueSlashCommand().initCommand();
        new LeaveSlashCommand().initCommand();
        new ClearQueueSlashCommand().initCommand();
        new JumpSlashCommand().initCommand();
        new NowPlayingSlashCommand().initCommand();
        new PauseSlashCommand().initCommand();
        new HelpCommand().initCommand();
    }

    public void initSlashCommands(Guild g) {
        new PlaySlashCommand().initCommand(g);
        new QueueSlashCommand().initCommand(g);
        new LeaveSlashCommand().initCommand(g);
        new ClearQueueSlashCommand().initCommand(g);
        new JumpSlashCommand().initCommand(g);
        new NowPlayingSlashCommand().initCommand(g);
        new PauseSlashCommand().initCommand(g);
        new HelpCommand().initCommand(g);
        new SkipSlashCommand().initCommand(g);
        new RemoveSlashCommand().initCommand(g);
        new LoopSlashCommand().initCommand(g);
        new MoveSlashCommand().initCommand(g);
        new RewindSlashCommand().initCommand(g);
        new SetChannelCommand().initCommand(g);
        new VolumeSlashCommand().initCommand(g);
        new SetDJCommand().initCommand(g);
        new RemoveDJCommand().initCommand(g);
        new SeekSlashCommand().initCommand(g);
        new BanCommand().initCommand(g);
        new UnbanCommand().initCommand(g);
        new ShuffleSlashCommand().initCommand(g);
        new EightBallCommand().initCommand(g);
    }

    private static void rescheduleUnbans(Guild g) {
        final var banUtils = new BanDB();
        final var map = BanDB.getBannedUsers().get(g.getIdLong());

        for (long user : map.keySet()) {
            if (map.get(user) == null) continue;
            if (map.get(user) - System.currentTimeMillis() <= 0) {
                try {
                    banUtils.unbanUser(g.getIdLong(), user);
                    map.remove(user);
                } catch (IllegalArgumentException e) {
                    map.remove(user);
                }
                continue;
            }

            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    banUtils.unbanUser(user, g.getIdLong());
                    map.remove(user);

                    Robertify.api.retrieveUserById(user).queue(user -> {
                        user.openPrivateChannel().queue(channel -> {
                            channel.sendMessageEmbeds(
                                    EmbedUtils.embedMessage("You have been unbanned from Robertify in **"+g.getName()+"**")
                                            .build()
                            ).queue(success -> {}, new ErrorHandler()
                                    .handle(ErrorResponse.CANNOT_SEND_TO_USER, (e) -> {
                                        LOGGER.warn("Was not able to send an unban message to " + user.getAsTag() + "("+user.getIdLong()+")");
                                    }));
                        });
                    });
                }
            };
            scheduler.schedule(task, new Date(banUtils.getUnbanTime(g.getIdLong(), user)).getTime(), TimeUnit.MILLISECONDS);

        }
    }

    public static void scheduleUnban(Guild g, User u) {
        final BanDB banUtils = new BanDB();
        final var map = BanDB.getBannedUsers().get(g.getIdLong());
        final var scheduler = Executors.newScheduledThreadPool(1);

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                if (!BanDB.isUserBannedLazy(g.getIdLong(), u.getIdLong()))
                    return;

                banUtils.unbanUser(g.getIdLong(), u.getIdLong());
                map.remove(u.getIdLong());

                u.openPrivateChannel().queue(channel -> {
                    channel.sendMessageEmbeds(
                            EmbedUtils.embedMessage("You have been unbanned from Robertify in **" + g.getName() + "**")
                                    .build()
                    ).queue(success -> {
                    }, new ErrorHandler()
                            .handle(ErrorResponse.CANNOT_SEND_TO_USER, (e) -> {
                                LOGGER.warn("Was not able to send an unban message to " + u.getAsTag() + "(" + u.getIdLong() + ")");
                            }));
                });
            }
        };
        scheduler.schedule(task, new Date(banUtils.getUnbanTime(g.getIdLong(), u.getIdLong())).getTime(), TimeUnit.MILLISECONDS);
    }


}
