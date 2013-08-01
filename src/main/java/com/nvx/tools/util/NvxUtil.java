package com.nvx.tools.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class NvxUtil {
    
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    public static final RoundingMode DEFAULT_ROUND_MODE = RoundingMode.HALF_UP;
    
    public static final String SIMPLE_DELIM = "|||";
    
    public static final String CURRENCY_FORMAT = "#,##0.00";
    
    public static final Pattern regexLowercase = Pattern.compile(".*?[a-z]+?.*");
    public static final Pattern regexUppercase = Pattern.compile(".*?[A-Z]+?.*");
    public static final Pattern regexNumber = Pattern.compile(".*?[0-9]+.*");
    public static final Pattern regexNonAlphanumeric = Pattern.compile(".*?[^a-zA-Z\\d\\s]+.*");
    
    public static boolean hasLowercase(String text) {
        return regexLowercase.matcher(text).matches();
    }
    
    public static boolean hasUppercase(String text) {
        return regexUppercase.matcher(text).matches();
    }
    
    public static boolean hasNumber(String text) {
        return regexNumber.matcher(text).matches();
    }
    
    public static boolean hasNonAlphanumeric(String text) {
        return regexNonAlphanumeric.matcher(text).matches();
    }
    
    public static boolean validatePassword(String pw, int minLength, int maxLength) {
        int len = pw.length();
        return len >= minLength && len <= maxLength 
                && hasLowercase(pw) && hasUppercase(pw) 
                && hasNumber(pw) && hasNonAlphanumeric(pw);
    }

    public static String stringToJson(List<String> list) {
        String json = JsonUtil.getGson(false).toJson(list);
        if (StringUtils.isEmpty(json)) {
            return "[]";
        }
        
        return json;
    }
    
    public static String arrayCombine(Object[] inputs, String delim) {
        return StringUtils.join(inputs, delim);
    }
    
    public static String[] arrayDecombine(String input, String delim) {
        return StringUtils.split(input, delim);
    }
    
    public static String generateRandomAlphaNumeric(int length, boolean uppercase) {
        String x = RandomStringUtils.randomAlphanumeric(length);
        return uppercase ? x.toUpperCase() : x;
    }
    
    public static boolean isWeekday(Date date, List<Date> publicHolidays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        
        if (day == Calendar.SUNDAY || day == Calendar.SATURDAY) {
            return false;
        }
        
        if (publicHolidays != null) {
            final Date dateToCompare = DateUtils.truncate(date, Calendar.DATE);
            for (Date holiday : publicHolidays) {
                if (DateUtils.truncate(holiday, Calendar.DATE).equals(dateToCompare)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public static boolean inDateRange(Date startDate, Date endDate, Date checkDate, boolean inclusive) {
        Date dt = DateUtils.truncate(checkDate, Calendar.DATE);
        
        if (inclusive) {
            return dt.compareTo(DateUtils.truncate(startDate, Calendar.DATE)) >= 0 &&
                    dt.compareTo(DateUtils.truncate(endDate, Calendar.DATE)) <= 0;
        }
        
        return dt.compareTo(DateUtils.truncate(startDate, Calendar.DATE)) > 0 &&
                 dt.compareTo(DateUtils.truncate(endDate, Calendar.DATE)) < 0;
    }
    
    public static int compareToCurrentDateDayField(Date dateToCompare, int daysOffset) {
        Date trunc1 = DateUtils.truncate(dateToCompare, Calendar.DATE);
        Date offsetCurrentDate = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), daysOffset);
        
        return trunc1.compareTo(offsetCurrentDate);
    }
    
    public static BigDecimal centsToMoney(int priceInCents) {
        return BigDecimal.valueOf(priceInCents, 2);
    }
    
    public static String formatToCurrency(BigDecimal number) {
        return formatToCurrency(number, "", DEFAULT_ROUND_MODE);
    }
    
    public static String formatToCurrency(BigDecimal number, String currencySymbol) {
        return formatToCurrency(number, currencySymbol, DEFAULT_ROUND_MODE);
    }
    
    public static String formatToCurrency(BigDecimal number, String currencySymbol, RoundingMode mode) {
        final DecimalFormat format = new DecimalFormat(CURRENCY_FORMAT);
        format.setRoundingMode(mode);
        
        return (StringUtils.isEmpty(currencySymbol) ? "" : currencySymbol) + format.format(number);
    }
    
    public static boolean datesEqualDayField(Date date1, Date date2) {
        return DateUtils.truncate(date1, Calendar.DATE).equals(DateUtils.truncate(date2, Calendar.DATE));
    }
    
    public static String encodeUrl(String input) {
        try {
            return URLEncoder.encode(input, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return input;
        }
    }
    
    public static String escapeHtml(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
    
    public static final String DEFAULT_DATE_FORMAT_DISPLAY = "dd/MM/yyyy";
    public static final String DEFAULT_DATE_FORMAT_DISPLAY_TIME = "dd/MM/yyyy hh:mm:ss";
    
    public static String formatDateForDisplay(Date date, boolean includeTime) {
        return formatDate(date, includeTime ? DEFAULT_DATE_FORMAT_DISPLAY_TIME : DEFAULT_DATE_FORMAT_DISPLAY);
    }
    
    public static String formatDate(Date date, String pattern) {
        return (new SimpleDateFormat(pattern)).format(date);
    }
    
    public static Date parseDateFromDisplay(String date, boolean includeTime) {
        try {
            return parseDate(date, includeTime ? DEFAULT_DATE_FORMAT_DISPLAY_TIME : DEFAULT_DATE_FORMAT_DISPLAY);
        } catch (ParseException e) {
            return null;
        }
    }
    
    public static Date parseDate(String date, String pattern) throws ParseException {
        return (new SimpleDateFormat(pattern)).parse(date);
    }
    
    public static final int DEFAULT_REDUCE_LENGTH = 50;
    
    public static String reduceText(String text) {
        return reduceText(text, DEFAULT_REDUCE_LENGTH);
    }
    
    public static String reduceText(String text, int maxSize) {
        if (text.length() <= maxSize) {
            return text;
        }
        return text.substring(0, DEFAULT_REDUCE_LENGTH - 3) + "...";
    }
    
    public static String linebreaksToBr(String text) {
        return text.replace("\r\n", "<br>").replace("\n", "<br>");
    }
    
    public static String replaceLineBreaks(String stringToProcess, String replacementText) {
        return stringToProcess.replaceAll("\\r|\\n", replacementText);
    }

    public static final String MOD_DT_PATTERN = "dd/MM/yyyy HH:mm:ss";
    
    public static String formatModDt(Date dt) {
        return new SimpleDateFormat(MOD_DT_PATTERN).format(dt);
    }

    public static Date parseModDt(String dt) {
        try {
            return new SimpleDateFormat(MOD_DT_PATTERN).parse(dt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean compareModDates(String oldModDt, Date actualModDt) {
        final Date toCompare = parseModDt(oldModDt);
        final Seconds secs = Seconds.secondsBetween(new DateTime(toCompare), new DateTime(actualModDt));
        int woah = secs.getSeconds();
        return woah == 0;
    }
}
