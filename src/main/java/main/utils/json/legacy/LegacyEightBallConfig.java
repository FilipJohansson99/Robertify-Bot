package main.utils.json.legacy;

import main.constants.JSONConfigFile;
import main.main.Robertify;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class LegacyEightBallConfig extends AbstractJSONFile {

    public LegacyEightBallConfig() {
        super(JSONConfigFile.EIGHT_BALL);
    }

    @Override
    public void initConfig() {
        try {
            makeConfigFile();
        } catch (IllegalStateException e) {
            updateConfig();
            return;
        }

        final var jsonObject = new JSONObject();

        for (Guild guild : Robertify.api.getGuilds())
            jsonObject.put(guild.getId(), new JSONArray());

        setJSON(jsonObject);
    }

    public synchronized void updateConfig() {
        var obj = getJSONObject();

        for (Guild g : Robertify.api.getGuilds())
            try {
                obj.getJSONArray(g.getId());
            } catch (JSONException e) {
                obj.put(g.getId(), new JSONArray());
            }

        setJSON(obj);
    }

    public void addGuild(String gid) {
        var obj = getJSONObject();
        obj.put(gid, new JSONArray());
        setJSON(obj);
    }

    public LegacyEightBallConfig addResponse(String gid, String response) {
        var obj = getJSONObject();
        obj.getJSONArray(gid).put(response);
        setJSON(obj);
        return this;
    }

    public LegacyEightBallConfig removeResponse(String gid, int responseIndex) {
        var obj = getJSONObject();
        obj.getJSONArray(gid).remove(responseIndex);
        setJSON(obj);
        return this;
    }

    public LegacyEightBallConfig removeAllResponses(String gid) {
        var obj = getJSONObject();
        obj.getJSONArray(gid).clear();
        setJSON(obj);
        return this;
    }

    public List<String> getResponses(String gid) {
        final var obj = getJSONObject().getJSONArray(gid);
        final List<String> responses = new ArrayList<>();

        for (int i = 0; i < obj.length(); i++)
            responses.add(obj.getString(i));

        return responses;
    }
}
