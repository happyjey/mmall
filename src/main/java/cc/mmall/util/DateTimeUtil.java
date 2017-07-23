package cc.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by Administrator on 2017/6/13.
 */
public class DateTimeUtil {

    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 字符串转时间
     * @param dateTimeStr
     * @param formatStr
     * @return
     */
    public static Date strToDate(String dateTimeStr, String formatStr){
        DateTimeFormatter dtf = DateTimeFormat.forPattern(formatStr);
        DateTime dt = dtf.parseDateTime(dateTimeStr);
        return dt.toDate();
    }
    public static Date strToDate(String dateTimeStr){
        DateTimeFormatter dtf = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dt = dtf.parseDateTime(dateTimeStr);
        return dt.toDate();
    }

    public static String dateToStr(Date date, String formatStr){
        if (date == null){
            return StringUtils.EMPTY;
        }
        DateTime dt = new DateTime(date);
        return dt.toString(formatStr);
    }
    public static String dateToStr(Date date){
        if (date == null){
            return StringUtils.EMPTY;
        }
        DateTime dt = new DateTime(date);
        return dt.toString(STANDARD_FORMAT);
    }

}
