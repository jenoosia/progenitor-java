package com.nvx.tools.util;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final String EMAIL_PATTERN = 
              "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    private static Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean validateEmail(String email) {
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }
    
    private static final int[] NRICMultiples = { 2, 7, 6, 5, 4, 3, 2 };

    public static boolean validateNRIC(String nricParam) {
        String nric = nricParam.toUpperCase();
        
        if (nric == null || nric.equals("")) {
            return false;
        }

        //  check length
        if (nric.length() != 9) {
            return false;
        }

        int total = 0, count = 0, numericNric;
        char first = Character.toUpperCase(nric.charAt(0)), last = Character.toUpperCase(nric.charAt(nric.length() - 1));

        if (first != 'S' && first != 'T') {
            return false;
        }

        try {
            numericNric = Integer.parseInt(nric.substring(1, nric.length() - 1));
        } catch (NumberFormatException e) {
            return false;
        }

        while (numericNric != 0) {
            total += numericNric % 10 * NRICMultiples[NRICMultiples.length - (1 + count++)];
            numericNric /= 10;
        }

        char[] outputs;
        if (first == 'S') {
            outputs = new char[] { 'J', 'Z', 'I', 'H', 'G', 'F', 'E', 'D', 'C', 'B', 'A' };
        } else {
            outputs = new char[] { 'G', 'F', 'E', 'D', 'C', 'B', 'A', 'J', 'Z', 'I', 'H' };
        }

        return last == outputs[total % 11];
    }

    public static boolean validateFIN(String fin) {
        if (fin == null || fin.isEmpty()) {
            return false;
        }

        //  check length
        if (fin.length() != 9) {
            return false;
        }

        int total = 0, count = 0, numericNric;
        char first = Character.toUpperCase(fin.charAt(0)), last = Character.toUpperCase(fin.charAt(fin.length() - 1));

        if (first != 'F' && first != 'G') {
            return false;
        }

        try {
            numericNric = Integer.parseInt(fin.substring(1, fin.length() - 1));
        } catch (NumberFormatException e) {
            return false;
        }

        while (numericNric != 0) {
            total += numericNric % 10 * NRICMultiples[NRICMultiples.length - (1 + count++)];
            numericNric /= 10;
        }

        char[] outputs;
        if (first == 'F') {
            outputs = new char[] { 'X', 'W', 'U', 'T', 'R', 'Q', 'P', 'N', 'M', 'L', 'K' };
        } else {
            outputs = new char[] { 'R', 'Q', 'P', 'N', 'M', 'L', 'K', 'X', 'W', 'U', 'T' };
        }

        return last == outputs[total % 11];
    }

    public static boolean validateDateInMinRange(Date selectedDate, Date minDate) {
        return selectedDate.compareTo(minDate) >= 0;
    }

    public static boolean validateDateInMaxRange(Date selectedDate, Date maxDate) {
        return selectedDate.compareTo(maxDate) <= 0;
    }

    public static boolean validateDateInRange(Date selectedDate, Date minDate, Date maxDate) {
        if (selectedDate.compareTo(minDate) >= 0 && selectedDate.compareTo(maxDate) <= 0) {
            return true;
        }
        return false;
    }
}
