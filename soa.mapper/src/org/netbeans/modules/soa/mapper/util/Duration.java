/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mapper.util;

import java.text.DecimalFormat;

import org.openide.util.NbBundle;

/**
 * Implementation of xsd:dateTime and xsd:duration
 * @author $author$
 * @version $Revision$
 */
public class Duration {

    static final DecimalFormat FMT = new DecimalFormat(".###"); // NOI18N

    boolean negative;
    int years;
    int months;
    int days;
    int hours;
    int minutes;
    int seconds;
    int milliseconds;

    //TimeZone tz; fix me !!

    public Duration() {
    }

    public Duration(Duration copy) {
        negative = copy.negative;
        years = copy.years;
        months = copy.months;
        days = copy.days;
        hours = copy.hours;
        minutes = copy.minutes;
        seconds = copy.seconds;
        milliseconds = copy.milliseconds;
    }

    private void check(int value) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    NbBundle.getMessage(Duration.class,
                        "STR_VALUE_CANNOT_BE_NEGATIVE"));   // NOI18N
        }
    }

    public void setYears(int years) {
        check(years);
        this.years = years;
    }

    public void setMonths(int months) {
        check(months);
        this.months = months;
    }

    public void setDays(int days) {
        check(days);
        this.days = days;
    }

    public void setHours(int hours) {
        check(hours);
        this.hours = hours;
    }

    public void setMinutes(int minutes) {
        check(minutes);
        this.minutes = minutes;
    }

    public void setSeconds(int seconds) {
        check(seconds);
        this.seconds = seconds;
    }

    public void setMilliseconds(int millis) {
        check(millis);
        this.milliseconds = millis;
    }

    public int getYears() {
        return years;
    }
    
    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }
    
    public int getMilliseconds() {
        return milliseconds;
    }
    
    public boolean isNegative() {
        return negative;
    }
    
    public void setNegative(boolean value) {
        negative = value;
    }
    
    public String toString() {
        String buffer = "";         // NOI18N
        
        if (negative) {
            buffer += "-";          // NOI18N
        }
        
        buffer += "P";              // NOI18N
        
        if (years > 0) {
            buffer += years;
            buffer += "Y";          // NOI18N
        }
        
        //if (months > 0) {
        buffer += months;
        buffer += "M";              // NOI18N
        
        //}
        if (days > 0) {
            buffer += days;
            buffer += "D";          // NOI18N
        }
        
        if ((hours > 0) || (minutes > 0) || (seconds > 0)
        || (milliseconds > 0)) {
            buffer += "T";          // NOI18N
            
            if (hours > 0) {
                buffer += hours;
                buffer += "H";      // NOI18N
            }
            
            if (minutes > 0) {
                buffer += minutes;
                buffer += "M";      // NOI18N
            }
            
            if ((seconds > 0) || (milliseconds > 0)) {
                buffer += seconds;
                
                if (milliseconds > 0) {
                    buffer += FMT.format((double) milliseconds / 1000);
                }
                
                buffer += "S";      // NOI18N
            }
        }
        
        return buffer;
    }
    
    public static Duration parse(String value) {
        if (value.startsWith(TimeEventUtil.QUOTE)) {
            value = value.substring(1, value.length());
        }
        if (value.endsWith(TimeEventUtil.QUOTE)) {
            value = value.substring(0, value.length() - 1);
        }
        DurationParser parser = new DurationParser(value);
        
        return parser.parseDuration();
    }
    
    //P(nY)? (nM)? (nD)?T(nH)? (nM)? (nS("."n)?)?
    static class DurationParser {
        
        Duration result;
        char[] buf;
        int mark;
        int pos;
        String val = null;
        
        DurationParser(String value) {
            buf = value.toCharArray();
            pos = buf.length - 1;
            val = value;
            result = new Duration();
        }
        
        int peek() {
            if (pos < 0) {
                throw new RuntimeException(
                        NbBundle.getMessage(Duration.class,
                            "STR_MALFORMED_DURATION"));     // NOI18N
            }
            
            return buf[pos];
        }
        
        void lex() {
            pos--;
        }
        
        int parseInt() {
            int tMark = pos;
            
            while ((pos > 0) &&  Character.isDigit(buf[pos - 1])) {
                pos--;
            }
            
            int retResult = Integer.parseInt(new String(buf, pos,
                    (tMark + 1) - pos));
            pos--;
            
            return retResult;
        }
        
        boolean parseSeconds() {
            if (peek() == 'S') {
                lex();
                
                int value = parseInt();
                
                if (peek() == '.') {
                    result.setMilliseconds(
                        (int) (Double.parseDouble("0." + value) * 1000));   // NOI18N
                    lex();
                    value = parseInt();
                }
                
                result.setSeconds(value);
                
                return true;
            }
            
            return false;
        }
        
        boolean parseMinutes() {
            if ((peek() == 'M') && (val.indexOf("T") != -1)) {  // NOI18N
                lex();
                result.setMinutes(parseInt());
                
                return true;
            }
            
            return false;
        }
        
        boolean parseHours() {
            if (peek() == 'H') {
                lex();
                result.setHours(parseInt());
                
                return true;
            }
            
            return false;
        }
        
        void parseT() {
            if (peek() == 'T') {
                lex();
            } else {
                throw new RuntimeException(
                        NbBundle.getMessage(Duration.class,
                            "STR_EXPECTED_T_INSTEAD_OF_X",  // NOI18N
                            new Character((char) peek())));
            }
        }
        
        void parseP() {
            if (peek() == 'P') {
                lex();
            } else {
                throw new RuntimeException(
                        NbBundle.getMessage(Duration.class,
                            "STR_EXPECTED_P_INSTEAD_OF_X",  // NOI18N
                            new Character((char) peek())));
            }
        }
        
        boolean parseDay() {
            if (peek() == 'D') {
                lex();
                result.setDays(parseInt());
                
                return true;
            }
            
            return false;
        }
        
        boolean parseMonth() {
            if (peek() == 'M') {
                lex();
                result.setMonths(parseInt());
                
                return true;
            }
            
            return false;
        }
        
        boolean parseYear() {
            if (peek() == 'Y') {
                lex();
                result.setYears(parseInt());
                
                return true;
            }
            
            return false;
        }

        Duration parseDuration() {
            boolean some = false;
            
            if (parseSeconds()) {
                some = true;
            }
            
            if (parseMinutes()) {
                some = true;
            }
            
            if (parseHours()) {
                some = true;
            }
            
            if (some) {
                parseT();
            }
            
            if (parseDay()) {
                some = true;
            }
            
            if (parseMonth()) {
                some = true;
            }
            
            if (parseYear()) {
                some = true;
            }
            
            parseP();
            parseEnd(some);
            
            return result;
        }
        
        void parseEnd(boolean some) {
            if (!some) {
                throw new RuntimeException(
                        NbBundle.getMessage(Duration.class,
                            "STR_NO_FIELDS_SPECIFIED"));    // NOI18N
            }
            
            if (pos > 0) {
                throw new RuntimeException(
                        NbBundle.getMessage(Duration.class,
                            "STR_UNEXPECTED_TEXT")  // NOI18N
                        + new String(buf, 0, pos));
            }
        }
    }
}
