package com.olcow.shiniu.until;

import java.util.Calendar;
import java.util.Date;

public class TimeType {

    public static String getMessageTimeText(long time){
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTime().getTime();
        if (now - time >86400000&&now-time<604800000){
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
            return dayOfWeekText+" "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
        } else if (now - time <86400000){
            int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
            calendar.setTime(new Date(time));
            int sendHour = calendar.get(Calendar.HOUR_OF_DAY);
            if (nowHour>sendHour){
                return "今天 " + calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
            }else {
                return "昨天 " + calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
            }
        } else {
            int nowYear = calendar.get(Calendar.YEAR);
            calendar.setTime(new Date(time));
            int sendYear = calendar.get(Calendar.YEAR);
            if (nowYear>sendYear){
                return sendYear+"-"+calendar.get(Calendar.MONTH)+1+"-"+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
            }
            return calendar.get(Calendar.MONTH)+1+"-"+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
        }
    }

    public static String getLastSendMessageTimeText(long time) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTime().getTime();
        if (now - time < 86400000) {
            int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
            calendar.setTime(new Date(time));
            int sendHour = calendar.get(Calendar.HOUR_OF_DAY);
            if (nowHour > sendHour) {
                return "今天" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
            } else {
                return "昨天" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
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
}
