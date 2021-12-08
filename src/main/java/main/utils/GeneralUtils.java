package main.utils;

import main.commands.commands.management.permissions.Permission;
import main.constants.BotConstants;
import main.constants.ENV;
import main.constants.RobertifyEmoji;
import main.constants.TimeFormat;
import main.main.Config;
import main.main.Robertify;
import main.utils.json.permissions.PermissionsConfig;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class GeneralUtils {
    private static final Logger logger = LoggerFactory.getLogger(GeneralUtils.class);

    private static Color embedColor = parseColor(Config.get(ENV.BOT_COLOR));

    public static boolean stringIsNum(String s) {
        if (s == null) return false;
        else {
            try {
                double d = Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public static boolean stringIsInt(String s) {
        if (s == null) return false;
        else {
            try {
                int d = Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public static int longToInt(long l) {
        if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE)
            throw new IllegalArgumentException("This long exceeds the integer limits!");

        return Integer.parseInt(String.valueOf(l));
    }

    public static boolean stringIsID(String s) {
        String idRegex = "^[0-9]{18}$";
        return Pattern.matches(idRegex, s);
    }

    public static boolean isUrl(String url) {
        if (url == null)
            return false;

        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String getDigitsOnly(String s) {
        return s.replaceAll("\\D", "");
    }

    public static void updateENVField(ENV field, String str) throws IOException {
        switch (field) {
            case BOT_TOKEN -> throw new IllegalAccessError("This env value can't be changed from the bot!");
            default -> {
                int doNothing;
            }
        }
        String fileContent = getFileContent(".env");
        String envFieldTitle = field.name();
        String envFieldValue = Config.get(field);
        setFileContent(
                ".env",
                fileContent.replace(envFieldTitle+"="+envFieldValue, envFieldTitle+"="+str)
        );
        Config.reload();
    }

    public static boolean  hasPerms(Guild guild, Member sender, Permission perm) {
        if (sender.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)
        || sender.isOwner()) return true;

        List<Role> userRoles = sender.getRoles();

        PermissionsConfig permissionsConfig = new PermissionsConfig();

        for (Role r : userRoles)
            if (permissionsConfig.getRolesForPermission(guild.getId(), perm).contains(r.getId()) ||
                    permissionsConfig.getRolesForPermission(guild.getId(), Permission.ROBERTIFY_ADMIN).contains(r.getId()))
                return true;


        return permissionsConfig.getUsersForPermission(guild.getId(), perm.name()).contains(sender.getId());
    }

    public static boolean hasPerms(Guild guild, User sender, Permission perm) {
        return hasPerms(guild, guild.getMember(sender), perm);
    }

    public static boolean hasPerms(Guild guild, Member sender, Permission... perms) {
        if (sender.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) return true;

        List<Role> userRoles = sender.getRoles();
        int pass = 0;

        PermissionsConfig permissionsConfig = new PermissionsConfig();


        for (Role r : userRoles) {
            if (permissionsConfig.getRolesForPermission(guild.getId(), Permission.ROBERTIFY_ADMIN).contains(r.getId()))
                return true;
            for (Permission p : perms) {
                if (permissionsConfig.getRolesForPermission(guild.getId(), p).contains(r.getId()))
                    pass++;
                else if (permissionsConfig.getUsersForPermission(guild.getId(), p.name()).contains(sender.getId()))
                    pass++;
            }
        }
        return pass >= perms.length;
    }

    public static boolean hasPerms(Guild guild, User sender, Permission... perm) {
        return hasPerms(guild, guild.getMember(sender), perm);
    }

    @Deprecated @ForRemoval
    @ReplaceWith("progressBar(String percent, ProgressBar barType)")
    public static String progressBar(double percent) {
        StringBuilder str = new StringBuilder();
        for(int i=0; i<12; i++)
            if(i == (int)(percent*12))
                str.append("\uD83D\uDD18"); // 🔘
            else
                str.append("▬");
        return str.toString();
    }

    public static String progressBar(double percent, ProgressBar barType) {
        switch (barType) {
            case DURATION -> {
                StringBuilder str = new StringBuilder();
                for(int i = 0; i < 12; i++)
                    if(i == (int)(percent*12))
                        str.append("\uD83D\uDD18"); // 🔘
                    else
                        str.append("▬");
                return str.toString();
            }
            case FILL -> {
                StringBuilder str = new StringBuilder();

                if (percent * 12 == 0L) {
                    for (int i = 0; i < 12; i++) {
                        if (i == 0)
                            str.append(RobertifyEmoji.BAR_START_EMPTY);
                        else if (i == 11)
                            str.append(RobertifyEmoji.BAR_END_EMPTY);
                        else
                            str.append(RobertifyEmoji.BAR_MIDDLE_EMPTY);
                    }
                } else {
                    for (int i = 0; i < 12; i++)
                        if (i <= (int) (percent * 12)) {
                            if (i == 0)
                                str.append(RobertifyEmoji.BAR_START_FULL);
                            else if (i == 11)
                                str.append(RobertifyEmoji.BAR_END_FULL);
                            else
                                str.append(RobertifyEmoji.BAR_MIDDLE_FULL);
                        } else {
                            if (i == 0)
                                str.append(RobertifyEmoji.BAR_START_EMPTY);
                            else if (i == 11)
                                str.append(RobertifyEmoji.BAR_END_EMPTY);
                            else
                                str.append(RobertifyEmoji.BAR_MIDDLE_EMPTY);
                        }
                }
                return str.toString();
            }
        }
        throw new NullPointerException("Something went wrong!");
    }

    public enum ProgressBar {
        DURATION,
        FILL
    }

    public static String trimString(String string, String delimiter) {
        if (!string.contains(delimiter)) return string;

        switch (delimiter) {
            case "?", "^", "[", ".", "$", "{", "&", "(", "+", ")", "|", "<", ">", "]", "}"
                -> delimiter = "\\\\" + delimiter;
        }
        System.out.println(delimiter+"[a-zA-Z0-9~!@#$%^&*()\\-_=;:'\"|\\\\,./]*");

        return string.replaceAll(delimiter+"[a-zA-Z0-9~!@#$%^&*()\\-_=;:'\"|\\\\,./]*", "");
    }

    public static String getJoinedString(List<String> args, int startIndex) {
        StringBuilder arg = new StringBuilder();
        for (int i = startIndex; i < args.size(); i++)
            arg.append(args.get(i)).append((i < args.size() - 1) ? " " : "");
        return  arg.toString();
    }

    public static String formatDate(long date, TimeFormat style) {
        return switch (style) {
            case DD_MMMM_YYYY, MM_DD_YYYY, DD_MMMM_YYYY_ZZZZ, DD_M_YYYY_HH_MM_SS,
                    E_DD_MMM_YYYY_HH_MM_SS_Z -> new SimpleDateFormat(style.toString()).format(date);
            default -> throw new IllegalArgumentException("The enum provided isn't a supported enum!");
        };
    }

    public static String formatTime(long duration) {
        return DurationFormatUtils.formatDuration(duration, "HH:mm:ss");
    }

    public static boolean isValidDuration(String timeUnparsed) {
        String durationStrRegex = "^\\d*[sSmMhHdD]$";
        return Pattern.matches(durationStrRegex, timeUnparsed);
    }

    public static long getFutureTime(String timeUnparsed) {
        String timeDigits       = timeUnparsed.substring(0, timeUnparsed.length()-1);
        char duration           = timeUnparsed.charAt(timeUnparsed.length()-1);
        long scheduledDuration  = 0L;

        if (Integer.parseInt(timeDigits) < 0)
            throw new IllegalArgumentException("The time cannot be negative!");

        if (GeneralUtils.stringIsInt(timeDigits))
            switch (duration) {
                case 's' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Integer.parseInt(timeDigits));
                case 'm' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(Integer.parseInt(timeDigits));
                case 'h' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(Integer.parseInt(timeDigits));
                case 'd' -> scheduledDuration = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(Integer.parseInt(timeDigits));
                default -> throw new IllegalArgumentException("The duration specifier \""+duration+"\" is invalid!");
            }
        else throw new IllegalArgumentException("There was no valid integer provided!");

        return scheduledDuration;
    }

    public static long getStaticTime(String timeUnparsed) {
        return getFutureTime(timeUnparsed) - System.currentTimeMillis();
    }

    public static String formatDuration(String timeUnparsed) {
        String ret;
        String timeDigits       = timeUnparsed.substring(0, timeUnparsed.length()-1);
        char duration           = timeUnparsed.charAt(timeUnparsed.length()-1);

        if (GeneralUtils.stringIsInt(timeDigits))
            switch (duration) {
                case 's' -> ret = timeDigits + " seconds";
                case 'm' -> ret = timeDigits + " minutes";
                case 'h' -> ret = timeDigits + " hours";
                case 'd' -> ret = timeDigits + " days";
                default -> throw new IllegalArgumentException("The duration specifier \""+duration+"\" is invalid!");
            }
        else throw new IllegalArgumentException("There was no valid integer provided!");

        return ret;
    }

    public static String getFileContent(String path) {
        String ret = null;
        try {
            ret = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ret == null) throw new NullPointerException();

        return ret.replaceAll("\t\n", "");
    }

    public static String getFileContent(Path path) {
        String ret = null;
        try {
            ret = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ret == null) throw new NullPointerException();

        return ret.replaceAll("\t\n", "");
    }

    public static void setFileContent(String path, String content) throws IOException {
        File file = new File(path);
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter(path, false);
        writer.write(content);
        writer.close();
    }

    public static void setFileContent(Path path, String content) throws IOException {
        File file = new File(String.valueOf(path));
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter(String.valueOf(path), false);
        writer.write(content);
        writer.close();
    }

    public static void setFileContent(File passedFile, String content) throws IOException {
        File file = new File(passedFile.getPath());
        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter(passedFile.getPath(), false);
        writer.write(content);
        writer.close();
    }

    public static void setDefaultEmbed() {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(embedColor)
                        .setAuthor(BotConstants.ROBERTIFY_EMBED_TITLE.toString(), null, BotConstants.ICON_URL.toString())
        );
    }

    public static void setCustomEmbed(Color color) {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
        );
    }

    public static void setCustomEmbed(String author, Color color) {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
                        .setAuthor(author, null, BotConstants.ICON_URL.toString())
        );
    }

    public static void setCustomEmbed(String author) {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(embedColor)
                        .setAuthor(author, null, BotConstants.ICON_URL.toString())
        );
    }

    public static void setCustomEmbed(String author, String footer) {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(embedColor)
                        .setAuthor(author,null, BotConstants.ICON_URL.toString())
                        .setFooter(footer)
        );
    }

    public static void setCustomEmbed(String author, @Nullable String title, String footer) {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setAuthor(author, null, BotConstants.ICON_URL.toString())
                        .setColor(embedColor)
                        .setTitle(title)
                        .setFooter(footer)
        );
    }

    public static void setCustomEmbed(String title, Color color, String footer) {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(color)
                        .setTitle(title)
                        .setFooter(footer)
        );
    }

    public static Color parseColor(String hex) {
        return Color.decode(hex);
    }

    public static int parseNumEmoji(String emoji) {
        switch (emoji) {
            case "0️⃣" -> {
                return 0;
            }
            case "1️⃣" -> {
                return 1;
            }
            case "2️⃣" -> {
                return 2;
            }
            case "3️⃣" -> {
                return 3;
            }
            case "4️⃣" -> {
                return 4;
            }
            case "5️⃣" -> {
                return 5;
            }
            case "6️⃣" -> {
                return 6;
            }
            case "7️⃣" -> {
                return 7;
            }
            case "8️⃣" -> {
                return 8;
            }
            case "9️⃣" -> {
                return 9;
            }
            default -> throw new IllegalArgumentException("Invalid argument \""+emoji+"\"");
        }
    }

    public static String parseNumEmoji(int num) {
        switch (num) {
            case 0 -> {
                return "0️⃣";
            }
            case 1 -> {
                return "1️⃣";
            }
            case 2 -> {
                return "2️⃣";
            }
            case 3 -> {
                return "3️⃣";
            }
            case 4 -> {
                return "4️⃣";
            }
            case 5 -> {
                return "5️⃣";
            }
            case 6 -> {
                return "6️⃣";
            }
            case 7 -> {
                return "7️⃣";
            }
            case 8 -> {
                return "8️⃣";
            }
            case 9 -> {
                return "9️⃣";
            }
            default -> throw new IllegalArgumentException("Invalid argument \""+num+"\"");
        }
    }
}
