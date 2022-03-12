package main.main;

import api.deezer.DeezerApi;
import com.github.kskelm.baringo.BaringoClient;
import com.github.kskelm.baringo.util.BaringoApiException;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lavalink.client.io.jda.JdaLavalink;
import lombok.Getter;
import main.commands.prefixcommands.audio.*;
import main.commands.prefixcommands.dev.test.MenuPaginationTestCommand;
import main.commands.slashcommands.commands.management.dedicatedchannel.DedicatedChannelEvents;
import main.commands.slashcommands.commands.misc.poll.PollEvents;
import main.commands.prefixcommands.util.reports.ReportsEvents;
import main.commands.slashcommands.SlashCommandManager;
import main.constants.ENV;
import main.events.AnnouncementChannelEvents;
import main.events.LogChannelEvents;
import main.events.SuggestionCategoryDeletionEvents;
import main.events.VoiceChannelEvents;
import main.utils.pagination.PaginationEvents;
import main.utils.spotify.SpotifyAuthorizationUtils;
import main.utils.votes.api.discordbotlist.DBLApi;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.discordbots.api.client.DiscordBotListAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;

import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Robertify {

    private static final Logger logger = LoggerFactory.getLogger(Robertify.class);

    @Getter
    public static JDA api;
    @Getter
    private static JdaLavalink lavalink;
    @Getter
    private static DiscordBotListAPI topGGAPI;
    @Getter
    private static DBLApi discordBotListAPI;
    public static BaringoClient baringo;
    @Getter
    private static DeezerApi deezerApi;
    @Getter
    private static SpotifyApi spotifyApi;
    @Getter
    private static final EventWaiter commandWaiter = new EventWaiter();

    public static void main(String[] args) {
        WebUtils.setUserAgent("Mozilla/Robertify / bombies#4445");

        try {
            lavalink = new JdaLavalink(
                    getIdFromToken(Config.get(ENV.BOT_TOKEN)),
                    1,
                    shardId -> getApi()
            );

            for (var node : Config.getLavaNodes())
                lavalink.addNode(node.getURI(), node.getPassword());

            JDABuilder jdaBuilder = JDABuilder.createDefault(
                            Config.get(ENV.BOT_TOKEN),
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGES
                    )
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .setMemberCachePolicy(MemberCachePolicy.VOICE)

                    // Event Listeners
                    .addEventListeners(
                            lavalink,
                            VoiceChannelEvents.waiter,
                            commandWaiter,
                            new Listener(),
                            new VoiceChannelEvents(),
                            new DedicatedChannelEvents(),
                            new PollEvents(),
                            new SuggestionCategoryDeletionEvents(),
                            new ReportsEvents(),
                            new AnnouncementChannelEvents(),
                            new LogChannelEvents(),
                            new SkipCommand()
                    )
                    .setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor())

                    // Test Listeners
                    .addEventListeners(
                            new MenuPaginationTestCommand()
                    )

                    // Button Listeners
                    .addEventListeners(
                            new PaginationEvents()
                    )

                    .enableCache(
                            CacheFlag.VOICE_STATE
                    )
                    .disableCache(
                            CacheFlag.ACTIVITY,
                            CacheFlag.EMOTE,
                            CacheFlag.CLIENT_STATUS,
                            CacheFlag.ROLE_TAGS,
                            CacheFlag.ONLINE_STATUS
                    )
                    .disableIntents(
                            GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.GUILD_BANS,
                            GatewayIntent.GUILD_INVITES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGE_TYPING,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS
                    )
                    .setGatewayEncoding(GatewayEncoding.ETF)
                    .setActivity(Activity.listening("Starting up..."));

            // Register all slash commands
            SlashCommandManager slashCommandManager = new SlashCommandManager();
            for (var cmd : slashCommandManager.getCommands())
                jdaBuilder.addEventListeners(cmd);
            for (var cmd : slashCommandManager.getDevCommands())
                jdaBuilder.addEventListeners(cmd);

            api = jdaBuilder.build();

            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(Config.get(ENV.SPOTIFY_CLIENT_ID))
                    .setClientSecret(Config.get(ENV.SPOTIFY_CLIENT_SECRET))
                    .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost/callback/"))
                    .build();

            deezerApi = new DeezerApi();

            if (!Config.get(ENV.TOP_GG_TOKEN).isEmpty())
                topGGAPI = new DiscordBotListAPI.Builder()
                        .token(Config.get(ENV.TOP_GG_TOKEN))
                        .botId(getIdFromToken(Config.get(ENV.BOT_TOKEN)))
                        .build();

            if (!Config.get(ENV.DBL_TOKEN).isEmpty())
                discordBotListAPI = new DBLApi.Builder()
                    .setToken(Config.get(ENV.DBL_TOKEN))
                        .setBotID(getIdFromToken(Config.get(ENV.BOT_TOKEN)))
                        .build();

            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(SpotifyAuthorizationUtils.doTokenRefresh(), 0, 1, TimeUnit.HOURS);

            try {
                baringo = new BaringoClient.Builder()
                        .clientAuth(Config.get(ENV.IMGUR_CLIENT), Config.get(ENV.IMGUR_SECRET))
                        .build();
            } catch (BaringoApiException e) {
                logger.error("[ERROR] There was an issue building the Baringo client!", e);
            }
        } catch (Exception e) {
            logger.error("[FATAL ERROR] An unexpected error occurred!", e);
        }
    }

    private static String getIdFromToken(String token) {
        return new String(
                Base64.getDecoder().decode(
                        token.split("\\.")[0]
                )
        );
    }
}


