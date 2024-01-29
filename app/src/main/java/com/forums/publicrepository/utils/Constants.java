package com.forums.publicrepository.utils;

public class Constants {

    public static String authError = "`~`", users = "users",
            topics = "topics", threads = "threads", dbTimeZone = "Asia/Singapore",
            timeFormat = "dd-MM-yyyy HH:mm:ss", NO_ERROR = "`~NO~~ERROR~`",
            dbRef = "https://public-repository-bb6d2-default-rtdb.asia-southeast1.firebasedatabase.app",
            mID = "messageID", chats = "chats", threadIDs = "threadIDS", messages = "messages",
            threadNum = "threadNum", Lock = "Lock", nullLock = "NULL", NO_PIC = "nullPic",
            INTENT_TOPIC = "intentsTopic", INTENT_THREAD = "intentThread", NO_TITLE = "`N0~~title`";

    public static int NOT_POSTED = 0, POSTED = 1, THREAD_ACTIVITY = 0, REPLY_ACTIVITY = 1, FILE_PICK_REQUEST = 29;
    private static final long MAX_FILE_SIZE = 3 * 1024 * 1024;
}
