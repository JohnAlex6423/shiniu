package com.olcow.shiniu.until;

import java.util.Calendar;
import java.util.Date;

public class TimeType {

    public static String getMessageTimeText(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        String dayOfWeekText;
        switch (calendar.get(Calendar.DAY_OF_WEEK)-1){
            case 0:dayOfWeekText="周日";
                break;
            case 1:dayOfWeekText="周一";
                break;
            case 2:dayOfWeekText="周二";
                break;
            case 3:dayOfWeekText="周三";
                break;
            case 4:dayOfWeekText="周四";
                break;
            case 5:dayOfWeekText="周五";
                break;
            case 6:dayOfWeekText="周六";
                break;
            default:dayOfWeekText="失败";
        }
        return calendar.get(Calendar.MONTH)+1+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" "+dayOfWeekText+" "+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE);
    }
}
