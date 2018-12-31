package com.olcow.shiniu.until;

import java.util.Calendar;
import java.util.Date;

public class TimeType {

    public static String getMessageTimeText(long time){
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTime().getTime();
        if (now - time >=172800000&&now-time<604800000){
            calendar.setTime(new Date(time));
            String dayOfWeekText;
            switch (calendar.get(Calendar.DAY_OF_WEEK)-1){
                case 0:
                    dayOfWeekText = "星期日";
                    break;
                case 1:
                    dayOfWeekText = "星期一";
                    break;
                case 2:
                    dayOfWeekText = "星期二";
                    break;
                case 3:
                    dayOfWeekText = "星期三";
                    break;
                case 4:
                    dayOfWeekText = "星期四";
                    break;
                case 5:
                    dayOfWeekText = "星期五";
                    break;
                case 6:
                    dayOfWeekText = "星期六";
                    break;
                default:
                    dayOfWeekText = "";
            }
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            String hourDayText;
            if (hourOfDay<10){
                hourDayText = "0"+hourOfDay;
            }else {
                hourDayText = String.valueOf(hourOfDay);
            }
            int min = calendar.get(Calendar.MINUTE);
            String minText;
            if (min<10){
                minText = "0"+min;
            }else {
                minText = String.valueOf(min);
            }
            return dayOfWeekText+" "+hourDayText+":"+minText;
        } else if (now - time <172800000){
            int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTime(new Date(time));
            int sendDay = calendar.get(Calendar.DAY_OF_MONTH);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            String hourDayText;
            if (hourOfDay<10){
                hourDayText = "0"+hourOfDay;
            }else {
                hourDayText = String.valueOf(hourOfDay);
            }
            int min = calendar.get(Calendar.MINUTE);
            String minText;
            if (min<10){
                minText = "0"+min;
            }else {
                minText = String.valueOf(min);
            }
            if (nowDay==sendDay){
                return "今天 " + hourDayText+":"+minText;
            }else {
                return "昨天 " + hourDayText+":"+minText;
            }
        } else {
            int nowYear = calendar.get(Calendar.YEAR);
            calendar.setTime(new Date(time));
            int sendYear = calendar.get(Calendar.YEAR);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            String hourDayText;
            if (hourOfDay<10){
                hourDayText = "0"+hourOfDay;
            }else {
                hourDayText = String.valueOf(hourOfDay);
            }
            int min = calendar.get(Calendar.MINUTE);
            String minText;
            if (min<10){
                minText = "0"+min;
            }else {
                minText = String.valueOf(min);
            }
            if (nowYear>sendYear){
                return sendYear+"-"+calendar.get(Calendar.MONTH)+1+"-"+calendar.get(Calendar.DAY_OF_MONTH)+hourDayText+":"+minText;
            }
            return calendar.get(Calendar.MONTH)+1+"-"+calendar.get(Calendar.DAY_OF_MONTH)+hourDayText+":"+minText;
        }
    }

    public static String getLastSendMessageTimeText(long time) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTime().getTime();
        if (now - time < 86400000) {
            int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTime(new Date(time));
            int sendDay = calendar.get(Calendar.DAY_OF_MONTH);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            String hourDayText;
            if (hourOfDay<10){
                hourDayText = "0"+hourOfDay;
            }else {
                hourDayText = String.valueOf(hourOfDay);
            }
            int min = calendar.get(Calendar.MINUTE);
            String minText;
            if (min<10){
                minText = "0"+min;
            }else {
                minText = String.valueOf(min);
            }
            if (nowDay==sendDay){
                return "今天 " + hourDayText+":"+minText;
            }else {
                return "昨天 " + hourDayText+":"+minText;
            }
        } else if (now - time > 86400000 && now - time < 604800000) {
            calendar.setTime(new Date(time));
            String dayOfWeekText;
            switch (calendar.get(Calendar.DAY_OF_WEEK) - 1) {
                case 0:
                    dayOfWeekText = "星期日";
                    break;
                case 1:
                    dayOfWeekText = "星期一";
                    break;
                case 2:
                    dayOfWeekText = "星期二";
                    break;
                case 3:
                    dayOfWeekText = "星期三";
                    break;
                case 4:
                    dayOfWeekText = "星期四";
                    break;
                case 5:
                    dayOfWeekText = "星期五";
                    break;
                case 6:
                    dayOfWeekText = "星期六";
                    break;
                default:
                    dayOfWeekText = "";
            }
            return dayOfWeekText;
        } else {
            int nowYear = calendar.get(Calendar.YEAR);
            calendar.setTime(new Date(time));
            int sendYear = calendar.get(Calendar.YEAR);
            if (nowYear > sendYear) {
                return sendYear + "-" + calendar.get(Calendar.MONTH) + 1 + "-" + calendar.get(Calendar.DAY_OF_MONTH);
            }
            return calendar.get(Calendar.MONTH) + 1 + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        }
    }

    public static String getNowDayOfWeek(){
        Calendar calendar = Calendar.getInstance();
        switch (calendar.get(Calendar.DAY_OF_WEEK) - 1) {
            case 0:
                return "星期日";
            case 1:
                return "星期一";
            case 2:
                return "星期二";
            case 3:
                return "星期三";
            case 4:
                return "星期四";
            case 5:
                return "星期五";
            case 6:
                return "星期六";
            default:
                return "";
        }
    }
    public static String getNowDayOfMonth(){
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }
    public static String getNowYear(){
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
    }
}
