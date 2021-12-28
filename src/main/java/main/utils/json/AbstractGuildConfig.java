package main.utils.json;

import lombok.Getter;
import main.utils.database.mongodb.GuildsDB;
import main.utils.database.mongodb.cache.BotInfoCache;
import main.utils.database.mongodb.cache.GuildsDBCache;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGuildConfig implements AbstractJSON {
    private final static Logger logger = LoggerFactory.getLogger(AbstractGuildConfig.class);
    @Getter
    private static GuildsDBCache cache;

    protected abstract void update();

    public JSONObject getGuildObject(long gid) {
        if (!guildHasInfo(gid))
            throw new NullPointerException("This guild doesn't have any information!");

        return cache.getCache().getJSONObject(getIndexOfObjectInArray(cache.getCache(), GuildsDB.Field.GUILD_ID, gid));
    }

    public static void initCache() {
        logger.debug("Instantiating Abstract Guild cache");
        cache = GuildsDBCache.getInstance();
    }

    public boolean guildHasInfo(long gid) {
        return cache.guildHasInfo(gid);
    }
}
