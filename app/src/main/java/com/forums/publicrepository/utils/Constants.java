package com.forums.publicrepository.utils;

public class Constants {

    public static String authError = "`~`", users = "users",
            topics = "topics", threads = "threads", dbTimeZone = "Asia/Singapore",
            timeFormat = "dd-MM-yyyy HH:mm:ss", NO_ERROR = "`~NO~~ERROR~`",
            dbRef = "https://public-repository-bb6d2-default-rtdb.asia-southeast1.firebasedatabase.app",
            mID = "messageID", chats = "chats", threadIDs = "threadIDS", messages = "messages",
            threadNum = "threadNum", Lock = "Lock", nullLock = "NULL", NO_PIC = "nullPic",
            INTENT_TOPIC = "intentsTopic", INTENT_THREAD = "intentThread", NO_TITLE = "`N0~~title`",
            storageRef = "gs://public-repository-bb6d2.appspot.com", INTENT_IMAGE = "`intentImage`",
            INTENT_VIDEO = "`intentVideo`", MEDIA_KEY = "~`Media_keyz`~";

    public static int NOT_POSTED = 0, POSTED = 1, THREAD_ACTIVITY = 0, REPLY_ACTIVITY = 1, FILE_PICK_REQUEST = 29;
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024;

    public enum MediaType {
        IMAGE,
        VIDEO,
        UNKNOWN
    }

    public static MediaType getMediaTypeFromContentType(String contentType) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return MediaType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return MediaType.VIDEO;
            }
        }
        return MediaType.UNKNOWN;
    }
}
