package main.audiohandlers.sources.applemusic;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import main.audiohandlers.sources.RobertifyAudioSourceManager;
import main.audiohandlers.sources.RobertifyAudioTrack;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class AppleMusicSourceManager extends RobertifyAudioSourceManager implements HttpConfigurable {
    private final Logger log = LoggerFactory.getLogger(AppleMusicSourceManager.class);

    public static final Pattern APPLE_MUSIC_URL_PATTERN = Pattern.compile("(https?://)?(www\\.)?music\\.apple\\.com/(?<countrycode>[a-zA-Z]{2}/)?(?<type>album|playlist|artist)(/[a-zA-Z0-9\\-]+)?/(?<identifier>[a-zA-Z0-9.]+)(\\?i=(?<identifier2>\\d+))?");
    public static final String SEARCH_PREFIX = "amsearch:";
    public static final int MAX_PAGE_ITEMS = 300;

    private final HttpInterfaceManager httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
    private String token;
    private Instant tokenExpire;

    public AppleMusicSourceManager(AudioPlayerManager audioPlayerManager) {
        super(audioPlayerManager);
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {
            if (reference.identifier.startsWith(SEARCH_PREFIX))
                return this.getSearch(reference.identifier.substring(SEARCH_PREFIX.length()).trim());

            var matcher = APPLE_MUSIC_URL_PATTERN.matcher(reference.identifier);
            if (!matcher.find())
                return null;

            var countryCode = matcher.group("countrycode");
            var id = matcher.group("identifier");
            switch (matcher.group("type")) {
                case "album":
                    var id2 = matcher.group("identifier2");
                    if (id2 == null || id2.isEmpty())
                        return this.getAlbum(id, countryCode);
                    return this.getSong(id2, countryCode);
                case "playlist":
                    return this.getPlaylist(id, countryCode);
                case "artist":
                    return this.getArtist(id, countryCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public String requestToken() throws IOException {
        var request = new HttpGet("https://music.apple.com");
        try(var response = this.httpInterfaceManager.getInterface().execute(request)){
            var document = Jsoup.parse(response.getEntity().getContent(), null, "");
            return JsonBrowser.parse(URLDecoder.decode(document.selectFirst("meta[name=desktop-music-app/config/environment]").attr("content"), StandardCharsets.UTF_8)).get("MEDIA_API").get("token").text();
        }
    }


    public String getToken() throws IOException {
        if(this.token == null || this.tokenExpire == null || this.tokenExpire.isBefore(Instant.now())){
            this.token = this.requestToken();
            this.tokenExpire = Instant.ofEpochSecond(JsonBrowser.parse(new String(Base64.getDecoder().decode(this.token.split("\\.")[1]))).get("exp").asLong(0));
        }
        return this.token;
    }

    public JsonBrowser getJson(String uri) throws IOException {
        var request = new HttpGet(uri);
        request.addHeader("Authorization", "Bearer " + this.getToken());
        request.addHeader("Origin", "https://music.apple.com");
        try(var response = this.httpInterfaceManager.getInterface().execute(request)){
            if(response.getStatusLine().getStatusCode() == 404){
                return null;
            }
            if(response.getStatusLine().getStatusCode() != 200){
                throw new IOException("HTTP error " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase());
            }
            return JsonBrowser.parse(response.getEntity().getContent());
        }
    }

    public AudioItem getSearch(String query) throws IOException {
        var json = this.getJson("https://api.music.apple.com/v1/catalog/us/search?term=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&limit=" + 25);
        if (json == null)
            return AudioReference.NO_TRACK;
        return new BasicAudioPlaylist("Apple Music Search: " + query, parseTracks(json.get("results").get("songs")), null, true);
    }

    public AudioItem getAlbum(String id, String countryCode) throws IOException {
        var json = this.getJson("https://api.music.apple.com/v1/catalog/" + countryCode + "/albums/" + id);
        if (json == null)
            return AudioReference.NO_TRACK;

        var tracks = new ArrayList<AudioTrack>();
        JsonBrowser page;
        var offset = 0;
        do {
            page = this.getJson("https://api.music.apple.com/v1/catalog/" + countryCode + "/albums/" + id + "/tracks?limit=" + MAX_PAGE_ITEMS + "&offset=" + offset);
            offset += MAX_PAGE_ITEMS;

            tracks.addAll(parseTracks(page));
        } while (page.get("next").text() != null);

        return new BasicAudioPlaylist(json.get("data").index(0).get("attributes").get("name").text(), tracks, null, false);
    }

    public AudioItem getPlaylist(String id, String countryCode) throws IOException {
        var json = this.getJson("https://api.music.apple.com/v1/catalog/" + countryCode + "/playlists/" + id);
        if (json == null)
            return AudioReference.NO_TRACK;

        var tracks = new ArrayList<AudioTrack>();
        JsonBrowser page;
        var offset = 0;
        do {
            page = this.getJson("https://api.music.apple.com/v1/catalog/" + countryCode + "/playlists/" + id + "/tracks?limit=" + MAX_PAGE_ITEMS + "&offset=" + offset);
            offset += MAX_PAGE_ITEMS;

            tracks.addAll(parseTracks(page));
        } while (page.get("next").text() != null);

        return new BasicAudioPlaylist(json.get("data").index(0).get("attributes").get("name").text(), tracks, null, false);
    }

    public AudioItem getArtist(String id, String countryCode) throws IOException {
        var json = this.getJson("https://api.music.apple.com/v1/catalog/" + countryCode + "/artists/" + id + "/view/top-songs");
        if (json == null)
            return AudioReference.NO_TRACK;
        return new BasicAudioPlaylist(json.get("data").index(0).get("attributes").get("artistName").text() + "'s Top Tracks", parseTracks(json), null, false);
    }

    public AudioItem getSong(String id, String countryCode) throws IOException{
        var json = this.getJson("https://api.music.apple.com/v1/catalog/" + countryCode + "/songs/" + id);
        if(json == null)
            return AudioReference.NO_TRACK;
        return parseTrack(json.get("data").index(0));
    }

    private List<AudioTrack> parseTracks(JsonBrowser json) {
        var tracks = new ArrayList<AudioTrack>();
        for(var value : json.get("data").values()){
            tracks.add(this.parseTrack(value));
        }
        return tracks;
    }

    private AudioTrack parseTrack(JsonBrowser json) {
        var attributes = json.get("attributes");
        var artwork = attributes.get("artwork");
        return new RobertifyAudioTrack(
                new AudioTrackInfo(
                        attributes.get("name").text(),
                        attributes.get("artistName").text(),
                        attributes.get("durationInMillis").asLong(0),
                        json.get("id").text(),
                        false,
                        attributes.get("url").text()
                ),
                attributes.get("isrc").text(),
                artwork.get("url").text().replace("{w}", artwork.get("width").text()).replace("{h}", artwork.get("height").text()),
                this
        );
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        final var appleMusicTrack = (RobertifyAudioTrack) track;
        DataFormatTools.writeNullableText(output, appleMusicTrack.getIsrc());
        DataFormatTools.writeNullableText(output, appleMusicTrack.getArtworkURL());
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new RobertifyAudioTrack(trackInfo, DataFormatTools.readNullableText(input), DataFormatTools.readNullableText(input), this);
    }

    @Override
    public void shutdown() {
        try {
            this.httpInterfaceManager.close();
        } catch(IOException e) {
            log.error("Failed to close HTTP interface manager", e);
        }
    }

    @Override
    public String getSearchPrefix() {
        return SEARCH_PREFIX;
    }

    @Override
    public String getSourceName() {
        return "applemusic";
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        this.httpInterfaceManager.configureRequests(configurator);
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        this.httpInterfaceManager.configureBuilder(configurator);
    }
}
