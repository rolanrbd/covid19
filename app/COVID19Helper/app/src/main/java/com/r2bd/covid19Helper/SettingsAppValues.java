package com.r2bd.covid19Helper;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsAppValues {
    public static final String EMERGENCY_NUMBER = "sttEmerg";
    public static final String FOOD_BANK_NUMBER = "sttFoodBank";
    public static final String ALARM_HOT_TEA = "sttHotTea";
    public static final String ALARM_GARGLE = "sttGargle";
    public static final String ALARM_RECORD = "sttRecordDailyContacs";
    public static final String NOTIFICATIONS = "sttNotification";
    public static final String STOP_ALARMS = "sttStopAlarms";

    public static String emergencyNumber;
    public static String foodBankNumber;
    public static int hotTeaFrequency, gargleFrequency;
    public static String dailyRecord;
    public static boolean notifications;
    public static String stopAlarms;

    private static boolean alarmDailyRecordChanged = false;
    private static boolean alarmTeaChanged = false;
    private static boolean alarmGargleChanged = false;
    private static boolean alarmStopChanged = false;
    private static boolean settingInvoked = false;

    public static boolean isAlarmDailyRecordChanged(){return alarmDailyRecordChanged;}
    public static boolean isAlarmTeaChanged(){return alarmTeaChanged;}
    public static boolean isAlarmGargleChanged(){return alarmGargleChanged;}
    public static boolean isAlarmStopChanged(){return alarmStopChanged;}

    public static void getPreferences(SharedPreferences preferences, Context context, boolean b){
        settingInvoked = b;
        String msgError = "OK";

        String strNone = context.getResources().getString(R.string.txtNone);
        String str1Hour = context.getResources().getString(R.string.txt1Hour);
        String str2Hour = context.getResources().getString(R.string.txt2Hour);
        String str3Hour = context.getResources().getString(R.string.txt3Hour);
        String str9_00H = context.getResources().getString(R.string.txt9_00H);
        String str9_30H = context.getResources().getString(R.string.txt9_30H);
        String str10_00H = context.getResources().getString(R.string.txt10_00H);
        String str10_30H = context.getResources().getString(R.string.txt10_30H);
        String str11_00H = context.getResources().getString(R.string.txt11_00H);
        String str11_30H = context.getResources().getString(R.string.txt11_30H);

        emergencyNumber = preferences.getString(EMERGENCY_NUMBER, "911");

        foodBankNumber = preferences.getString(FOOD_BANK_NUMBER, "");

        String hotTea = preferences.getString(ALARM_HOT_TEA, strNone);
        if(hotTea.equals(strNone)){
            alarmTeaChanged = settingInvoked && hotTeaFrequency != 0;
            hotTeaFrequency = 0;
        }
        else if(hotTea.equals(str1Hour)){
            alarmTeaChanged = settingInvoked &&  hotTeaFrequency != 1;
            hotTeaFrequency = 1;
        }
        else if(hotTea.equals(str2Hour)){
            alarmTeaChanged = settingInvoked && hotTeaFrequency != 2;
            hotTeaFrequency = 2;
        }
        else if(hotTea.equals(str3Hour)){
            alarmTeaChanged = settingInvoked && hotTeaFrequency != 3;
            hotTeaFrequency = 3;
        }

        String gargle= preferences.getString(ALARM_GARGLE, strNone);
        if(gargle.equals(strNone)){
            alarmGargleChanged = settingInvoked && gargleFrequency != 0;
            gargleFrequency = 0;
        }
        else if(gargle.equals(str1Hour)){
            alarmGargleChanged = settingInvoked && gargleFrequency != 1;
            gargleFrequency = 1;
        }
        else if(gargle.equals(str2Hour)){
            alarmGargleChanged = settingInvoked && gargleFrequency != 2;
            gargleFrequency = 2;
        }
        else if(gargle.equals(str3Hour)){
            alarmGargleChanged = settingInvoked && gargleFrequency != 3;
            gargleFrequency = 3;
        }

        String dRecord = preferences.getString(ALARM_RECORD,  strNone);
        if(dRecord.equals(strNone)){
            alarmDailyRecordChanged = settingInvoked && !dailyRecord.equals(strNone) ;
            dailyRecord = strNone;
        }
        else if(dRecord.equals(str9_00H)){
            alarmDailyRecordChanged = settingInvoked && !dailyRecord.equals("21:00") ;
            dailyRecord = "21:00";
        }
        else if(dRecord.equals(str9_30H)){
            alarmDailyRecordChanged = settingInvoked && !dailyRecord.equals("21:30") ;
            dailyRecord = "21:30";
        }
        else if(dRecord.equals(str10_00H)){
            alarmDailyRecordChanged = settingInvoked && !dailyRecord.equals("22:00") ;
            dailyRecord = "22:00";
        }
        else if(dRecord.equals(str10_30H)){
            alarmDailyRecordChanged = settingInvoked && !dailyRecord.equals("22:30") ;
            dailyRecord = "22:30";
        }
        else if(dRecord.equals(str11_00H)){
            alarmDailyRecordChanged = settingInvoked && !dailyRecord.equals("23:00") ;
            dailyRecord = "23:00";
        }
        else if(dRecord.equals(str11_30H)){
            alarmDailyRecordChanged = settingInvoked && !dailyRecord.equals("22:30") ;
            dailyRecord = "23:00";
        }

        String strStopAlarm = preferences.getString(STOP_ALARMS,  str9_00H/*strNone*/);
        if(strStopAlarm.equals(str9_00H)){
            alarmStopChanged = settingInvoked && !stopAlarms.equals("21:00");
            stopAlarms = "21:00";
        }
        else if(dRecord.equals(str9_30H)){
            alarmStopChanged = settingInvoked && !stopAlarms.equals("21:30");
            stopAlarms = "21:30";
        }
        else if(dRecord.equals(str10_00H)){
            alarmStopChanged = settingInvoked && !stopAlarms.equals("22:00");
            stopAlarms = "22:00";
        }
        else if(dRecord.equals(str10_30H)){
            alarmStopChanged = settingInvoked && !stopAlarms.equals("22:30");
            stopAlarms = "22:30";
        }
        else if(dRecord.equals(str11_00H)){
            alarmStopChanged = settingInvoked && !stopAlarms.equals("23:00");
            stopAlarms = "23:00";
        }
        else if(dRecord.equals(str11_30H)){
            alarmStopChanged = settingInvoked && !stopAlarms.equals("21:30");
            stopAlarms = "23:30";
        }

        notifications = preferences.getBoolean(NOTIFICATIONS, true);
    }
}
