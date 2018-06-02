package com.radionula.radionula.model;

public class Constants {


    public interface ACTION {
        public static String MAIN_ACTION = "com.radionula.foregroundservice.action.main";
        public static String INIT_ACTION = "com.radionula.foregroundservice.action.init";
        public static String PAUSE_ACTION = "com.radionula.foregroundservice.action.pause";
        public static String PLAY_ACTION = "com.radionula.foregroundservice.action.play";
        public static String NEXT_ACTION = "com.marothiatechs.foregroundservice.action.next";
        public static String STARTFOREGROUND_ACTION = "com.radionula.foregroundservice.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.radionula.foregroundservice.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}