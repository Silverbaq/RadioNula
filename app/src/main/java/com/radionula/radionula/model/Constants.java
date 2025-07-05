package com.radionula.radionula.model;

public class Constants {


    public interface ACTION {
        String MAIN_ACTION = "com.radionula.foregroundservice.action.main";
        String INIT_ACTION = "com.radionula.foregroundservice.action.init";
        String PAUSE_ACTION = "com.radionula.foregroundservice.action.pause";
        String PLAY_ACTION = "com.radionula.foregroundservice.action.play";
        String NEXT_ACTION = "com.marothiatechs.foregroundservice.action.next";
        String STARTFOREGROUND_ACTION = "com.radionula.foregroundservice.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.radionula.foregroundservice.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}