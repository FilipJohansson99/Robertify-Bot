package main.utils.locale;

import java.util.HashMap;

public enum RobertifyLocaleMessage {
    ;

    public enum GeneralMessages implements LocaleMessage {
        ON_STATUS,
        OFF_STATUS,
        INVALID_ARGS,
        INVALID_ARGS_USAGES,
        INSUFFICIENT_ARGS,
        INSUFFICIENT_ARGS_USAGES,
        VOICE_CHANNEL_NEEDED,
        USER_VOICE_CHANNEL_NEEDED,
        SAME_VOICE_CHANNEL,
        SAME_VOICE_CHANNEL_LOC,
        SAME_VOICE_CHANNEL_BUTTON,
        GENERAL_TOGGLE_MESSAGE,
        NO_VOICE_CHANNEL,
        MUST_BE_PLAYING,
        UNEXPECTED_ERROR,
        ID_GT_ZERO,
        ID_OUT_OF_BOUNDS,
        CANT_JOIN_CHANNEL,
        RESTRICTED_TO_JOIN,
        RESTRICTED_TOGGLED_NO_CHANNELS,
        INSUFFICIENT_PERMS,
        SELF_INSUFFICIENT_PERMS,
        DJ_ONLY,
        NOTHING_PLAYING,
        NOTHING_IN_QUEUE,
        CANT_BE_USED_IN_CHANNEL,
        NO_MENU_PERMS,
        NO_PERMS_END_INTERACTION,
        NO_PERMS_BUTTON,
        BUTTON_NO_LONGER_VALID,
        NO_SUCH_CHANNEL,
        OK
    }

    public enum RandomMessages implements LocaleMessage {
        NO_RANDOM_MESSAGES,
        TIP_TITLE,
        TIP_FOOTER
    }

    public enum FilterMessages implements LocaleMessage {
        FILTER_TOGGLE_MESSAGE,
        FILTER_TOGGLE_LOG_MESSAGE
    }

    public enum ClearQueueMessages implements LocaleMessage {
        CQ_NOTHING_IN_QUEUE,
        DJ_PERMS_NEEDED,
        QUEUE_CLEARED_USER,
        QUEUE_CLEAR
    }

    public enum DisconnectMessages implements LocaleMessage {
        NOT_IN_CHANNEL,
        DISCONNECTED_USER,
        DISCONNECTED
    }

    public enum FavouriteTracksMessages implements LocaleMessage {
        FT_MISSING_ID,
        FT_INVALID_ID,
        FT_INVALID_SOURCE,
        FAV_TRACK_ADDED,
        FAV_TRACK_REMOVED,
        NO_FAV_TRACKS,
        FAV_TRACKS_CLEARED,
        FT_SELECT_MENU_OPTION,
        FT_ADDING_TO_QUEUE,
        FT_ADDING_TO_QUEUE_2,
        FT_EMBED_TITLE,
        FT_EMBED_FOOTER,
        FT_EMBED_DESCRIPTION
    }

    public enum JoinMessages implements LocaleMessage {
        ALREADY_JOINED,
        CANT_JOIN,
        JOINED
    }

    public enum JumpMessages implements LocaleMessage {
        JUMP_MISSING_AMOUNT,
        JUMP_INVALID_DURATION,
        JUMP_DURATION_NEG_ZERO,
        JUMP_DURATION_GT_TIME_LEFT,
        JUMPED_LOG,
        JUMPED
    }

    public enum LofiMessages implements LocaleMessage {
        LOFI_ENABLING,
        LOFI_DISABLED
    }

    public enum LoopMessages implements LocaleMessage {
        LOOP_NOTHING_PLAYING,
        LOOP_STOP,
        LOOP_START,
        LOOP_LOG,
        QUEUE_LOOP_STOP,
        QUEUE_LOOP_START,
        QUEUE_LOOP_NOTHING,
        QUEUE_LOOP_LOG
    }

    public enum LyricsMessages implements LocaleMessage {
        LYRICS_SOURCE_NOT_SUPPORTED,
        LYRICS_SEARCHING,
        LYRICS_NOW_SEARCHING,
        LYRICS_NOTHING_FOUND,
        LYRICS_EMBED_TITLE
    }

    public enum MoveMessages implements LocaleMessage {
        INVALID_SONG_ID,
        INVALID_POSITION_ID,
        COULDNT_MOVE,
        MOVED_LOG,
        MOVED
    }

    public enum NowPlayingMessages implements LocaleMessage {
        NP_LOFI_TITLE,
        NP_EMBED_TITLE,
        NP_REQUESTER,
        NP_LIVESTREAM,
        NP_TIME_LEFT,
        NP_AUTHOR,
    }

    public enum PauseMessages implements LocaleMessage {
        PAUSED,
        PAUSED_LOG,
        RESUMED,
        RESUMED_LOG,
        PLAYER_NOT_PAUSED
    }

    public enum PlayMessages implements LocaleMessage {
        MISSING_ARGS,
        MISSING_FILE,
        LOCAL_DIR_ERR,
        FILE_DOWNLOAD_ERR,
        INVALID_FILE
    }

    public enum PreviousTrackMessages implements LocaleMessage {
        NO_PREV_TRACKS,
        PLAYING_PREV_TRACK,
        PREV_TRACK_LOG
    }

    public enum QueueMessages implements LocaleMessage {
        QUEUE_ENTRY
    }

    public enum RemoveMessages implements LocaleMessage {
        REMOVE_INVALID_ID,
        REMOVED,
        REMOVED_LOG,
        COULDNT_REMOVE
    }

    public enum RewindMessages implements LocaleMessage {
        INVALID_DURATION,
        CANT_REWIND_STREAM,
        REWIND_TO_BEGINNING,
        REWIND_TO_BEGINNING_LOG,
        DURATION_GT_CURRENT_TIME,
        REWOUND_BY_DURATION,
        REWOUND_BY_DURATION_LOG
    }

    public enum SearchMessages implements LocaleMessage {
        MUST_PROVIDE_QUERY,
        LOOKING_FOR
    }

    public enum SeekMessages implements LocaleMessage {
        INVALID_MINUTES,
        INVALID_SECONDS,
        POS_GT_DURATION,
        SOUGHT,
        SOUGHT_LOG
    }

    public enum ShufflePlayMessages implements LocaleMessage {
        MISSING_LINK,
        INVALID_LINK,
        NOT_PLAYLIST,
        MUST_PROVIDE_VALID_PLAYLIST,

    }

    public enum ShuffleMessages implements LocaleMessage {
        SHUFFLED,
        SHUFFLED_LOG
    }

    public enum SkipMessages implements LocaleMessage {
        VOTE_SKIP_STARTED,
        VOTE_SKIP_STARTED_LOG,
        VOTE_SKIP_STARTED_EMBED,
        NOTHING_TO_SKIP,
        SKIPPED,
        SKIPPED_LOG,
        VOTE_SKIPPED,
        VOTE_SKIPPED_LOG,
        SKIP_VOTE_ADDED,
        SKIP_VOTE_REMOVED,
        DJ_SKIPPED
    }

    public enum StopMessages implements LocaleMessage {
        STOPPED,
        STOPPED_LOG
    }

    public enum VolumeMessages implements LocaleMessage {
        INVALID_VOLUME,
        VOLUME_CHANGED,
        VOLUME_CHANGED_LOG
    }

    public enum BanMessages implements LocaleMessage {
        BAN_INVALID_USER,
        BAN_INVALID_USER_DETAILED,
        INVALID_BAN_DURATION,
        CANNOT_BAN_ADMIN,
        CANNOT_BAN_DEVELOPER,
        USER_ALREADY_BANNED,
        USER_PERM_BANNED,
        USER_PERM_BANNED_RESPONSE,
        USER_TEMP_BANNED,
        USER_TEMP_BANNED_RESPONSE
    }

    public enum UnbanMessages implements LocaleMessage {
        MISSING_UNBAN_USER,
        INVALID_UNBAN_USER,
        USER_NOT_BANNED,
        USER_UNBANNED,
        USER_UNBANNED_RESPONSE
    }

    public enum LogChannelMessages implements LocaleMessage {
        LOG_CHANNEL_ALREADY_SETUP,
        LOG_CHANNEL_SUCCESSFUL_SETUP,
        LOG_CHANNEL_MISSING,
        INVALID_LOG_CHANNEL,
        CANNOT_SET_LOG_CHANNEL,
        LOG_CHANNEL_SET,
        LOG_CHANNEL_INVALID_TYPE
    }

    public enum RestrictedChannelMessages implements LocaleMessage {
        MISSING_RESTRICTED_CHANNEL,
        INVALID_RESTRICTED_CHANNEL_ID,
        RESTRICTED_CHANNELS_TOGGLED_OFF,
        RESTRICTED_CHANNEL_ID_INVALID_SERVER,
        RESTRICTED_CHANNEL_ADDED,
        LISTING_RESTRICTED_CHANNELS,
        RESTRICTED_CHANNELS_VC_EMBED_FIELD,
        RESTRICTED_CHANNELS_TC_EMBED_FIELD,
        NO_CHANNELS
    }

    public enum ThemeMessages implements LocaleMessage {
        THEME_SET,
        THEME_EMBED_TITLE,
        THEME_EMBED_DESC,
        THEME_SELECT_MENU_PLACEHOLDER,
        THEME_GREEN,
        THEME_MINT,
        THEME_GOLD,
        THEME_RED,
        THEME_PASTEL_RED,
        THEME_PINK,
        THEME_PURPLE,
        THEME_PASTEL_PURPLE,
        THEME_BLUE,
        THEME_LIGHT_BLUE,
        THEME_BABY_BLUE,
        THEME_ORANGE,
        THEME_YELLOW,
        THEME_PASTEL_YELLOW,
        THEME_DARK,
        THEME_LIGHT
    }

    public enum PremiumMessages implements LocaleMessage {
        LOCKED_COMMAND_EMBED_TITLE,
        LOCKED_COMMAND_EMBED_DESC
    }

    public enum TogglesMessages implements LocaleMessage {
        TOGGLES_EMBED_TITLE,
        TOGGLES_EMBED_TOGGLE_ID_FIELD,
        TOGGLES_MESSAGES_EMBED_FEATURE_FIELD,
        TOGGLES_EMBED_STATUS_FIELD,
        DJ_TOGGLES_EMBED_COMMAND_FIELD,
        DJ_TOGGLES_EMBED_STATUS_FIELD,
        LOG_TOGGLES_EMBED_TYPE_FIELD,
        LOG_TOGGLES_EMBED_STATUS_FIELD,
        TOGGLED,
        INVALID_TOGGLE,
        DJ_TOGGLE_INVALID_COMMAND,
        DJ_TOGGLED,
        LOG_TOGGLE_INVALID_TYPE,
        LOG_TOGGLED,
        SKIP_DJ_TOGGLE_PROMPT
    }

    public enum TwentyFourSevenMessages implements LocaleMessage {
        TWENTY_FOUR_SEVEN_TOGGLED
    }

    public static HashMap<Class<? extends LocaleMessage>, LocaleMessage[]> getMessageTypes() {
        final HashMap<Class<? extends LocaleMessage>, LocaleMessage[]> ret = new HashMap<>();
        ret.put(GeneralMessages.class, GeneralMessages.values());
        ret.put(RandomMessages.class, RandomMessages.values());
        ret.put(FilterMessages.class, FilterMessages.values());
        ret.put(ClearQueueMessages.class, ClearQueueMessages.values());
        ret.put(DisconnectMessages.class, DisconnectMessages.values());
        ret.put(FavouriteTracksMessages.class, FavouriteTracksMessages.values());
        ret.put(JoinMessages.class, JoinMessages.values());
        ret.put(JumpMessages.class, JumpMessages.values());
        ret.put(LofiMessages.class, LofiMessages.values());
        ret.put(LoopMessages.class, LoopMessages.values());
        ret.put(LyricsMessages.class, LyricsMessages.values());
        ret.put(MoveMessages.class, MoveMessages.values());
        ret.put(NowPlayingMessages.class, NowPlayingMessages.values());
        ret.put(PauseMessages.class, PauseMessages.values());
        ret.put(PlayMessages.class, PlayMessages.values());
        ret.put(PreviousTrackMessages.class, PreviousTrackMessages.values());
        ret.put(QueueMessages.class, QueueMessages.values());
        ret.put(RemoveMessages.class, RemoveMessages.values());
        ret.put(RewindMessages.class, RewindMessages.values());
        ret.put(SearchMessages.class, SearchMessages.values());
        ret.put(SeekMessages.class, SeekMessages.values());
        ret.put(ShufflePlayMessages.class, ShufflePlayMessages.values());
        ret.put(ShuffleMessages.class, ShuffleMessages.values());
        ret.put(SkipMessages.class, SkipMessages.values());
        ret.put(StopMessages.class, StopMessages.values());
        ret.put(VolumeMessages.class, VolumeMessages.values());
        ret.put(BanMessages.class, BanMessages.values());
        ret.put(UnbanMessages.class, UnbanMessages.values());
        ret.put(LogChannelMessages.class, LogChannelMessages.values());
        ret.put(RestrictedChannelMessages.class, RestrictedChannelMessages.values());
        ret.put(ThemeMessages.class, ThemeMessages.values());
        ret.put(PremiumMessages.class, PremiumMessages.values());
        ret.put(TogglesMessages.class, TogglesMessages.values());
        ret.put(TwentyFourSevenMessages.class, TwentyFourSevenMessages.values());
        return ret;
    }
}
