/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.soa.validation.util;

import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.14
 */
public final class DurationUtil {

  private DurationUtil() {}

  public static Duration parseDuration(String value, boolean throwException) {
      return parseDuration(value, throwException, true);
  }

  public static Duration parseDuration(String value, boolean throwException, boolean checkQuote) {
//out();
//out("PARSE duration: " + value);
    boolean hasMinus = false;
    int years = 0;
    int months = 0;
    int days = 0;
    int hours = 0;
    int minutes = 0;
    double seconds = 0.0;
//out("1");
    if (value == null || value.length() == 0) {
      return throwException("FIX_Empty_Value", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
//out("2");
    if (checkQuote) {
      if ( !value.startsWith(QUOTE) && !value.endsWith(QUOTE)) {
        if (throwException) {
          return null;
        }
        return new Duration(hasMinus, years, months, days, hours, minutes, seconds);
      }
      if (value.length() == 1 && value.startsWith(QUOTE)) {
        return throwException("FIX_Invalid_Value", value, throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      if (value.startsWith(QUOTE) && !value.endsWith(QUOTE)) {
        return throwException("FIX_Invalid_Value", value, throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      if ( !value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
        return throwException("FIX_Invalid_Value", value, throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
    }
    value = removeQuotes(value);

    if (value.length() == 0) {
      return throwException("FIX_Empty_Value", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
//out("3");
    int k;
    boolean wasDesignator = false;
    boolean wasDesignatorT = false;

    // minus
    if (value.charAt(0) == MINUS.charAt(0)) {
      hasMinus = true;
      value = value.substring(1);
    }
    // P
    if (value.charAt(0) != P_DELIM.charAt(0)) {
      return throwException("FIX_P_symbol", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
    value = value.substring(1);

    // years 
    k = value.indexOf(Y_DELIM);
   
    if (k != -1) {
      wasDesignator = true;
      years = parseInt(value.substring(0, k));
       
      if (years < 0) {
        return throwException("FIX_Year", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    // months 
    k = value.indexOf(M_DELIM);
    int t = value.indexOf(T_DELIM);
//out("4");
    
    if (k != -1) {
      months = parseInt(value.substring(0, k));

      if (months < 0) {
        if (t == -1 || k < t) {
          return throwException("FIX_Months", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
        }
        months = 0;
      }
      else {
        wasDesignator = true;
        value = value.substring(k + 1);
      }
    }
    // days 
    k = value.indexOf(D_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      days = parseInt(value.substring(0, k));
        
      if (days < 0) {
        return throwException("FIX_Days", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    if (value.length() == 0) {
      if ( !wasDesignator) {
        return throwException("FIX_Designator", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      return new Duration(hasMinus, years, months, days, hours, minutes, seconds);
    }
//out("T: " + value);
    // T
    if (value.charAt(0) != T_DELIM.charAt(0)) {
      return throwException("FIX_T_symbol", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
    value = value.substring(1);

    // hours 
    k = value.indexOf(H_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      wasDesignatorT = true;
      hours = parseInt(value.substring(0, k));
        
      if (hours < 0) {
        return throwException("FIX_Hours", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    // minutes 
    k = value.indexOf(M_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      wasDesignatorT = true;
      minutes = parseInt(value.substring(0, k));
        
      if (minutes < 0) {
        return throwException("FIX_Minutes", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    // seconds 
    k = value.indexOf(S_DELIM);
    
    if (k != -1) {
      wasDesignator = true;
      wasDesignatorT = true;
      seconds = parseDouble(value.substring(0, k));
        
      if (seconds < 0) {
        return throwException("FIX_Seconds", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
      }
      value = value.substring(k + 1);
    }
    if ( !wasDesignatorT) {
      return throwException("FIX_Absent_T_symbol", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
    if ( !wasDesignator) {
      return throwException("FIX_Designator", throwException, hasMinus, years, months, days, hours, minutes, seconds); // NOI18N
    }
//out("6: " + years + " " + months + " " + days + " " + hours + " " + minutes + " " + seconds);
    return new Duration(hasMinus, years, months, days, hours, minutes, seconds);
  }

  private static Duration throwException(String key, boolean throwException, boolean hasMinus, int years, int months, int days, int hours, int minutes, double seconds) {
    return throwExceptionMsg(i18n(DurationUtil.class, key), throwException, hasMinus, years, months, days, hours, minutes, seconds);
  }

  private static Duration throwException(String key, String param, boolean throwException, boolean hasMinus, int years, int months, int days, int hours, int minutes, double seconds) {
    return throwExceptionMsg(i18n(DurationUtil.class, key, param), throwException, hasMinus, years, months, days, hours, minutes, seconds);
  }

  private static Duration throwExceptionMsg(String message, boolean throwException, 
    boolean hasMinus,
    int years,
    int months,
    int days,
    int hours,
    int minutes,
    double seconds
  ) {
//out("ERROR: " + message);
    if (throwException) {
      throw new IllegalArgumentException(message);
    }
    return new Duration(hasMinus, years, months, days, hours, minutes, seconds);
  }

  public static String addQuotes(String value) {
    return QUOTE + value + QUOTE;
  }

  public static String removeQuotes(String value) {
    if (value == null) {
      return null;
    }
    if (value.startsWith(QUOTE)) {
      value = value.substring(1);
    }
    if (value.endsWith(QUOTE)) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }

  public static String getContent(
    boolean isFor,
    int year,
    int month,
    int day,
    int hour,
    int minute,
    double second
  ) {
    if (isFor) {
      StringBuffer content = new StringBuffer();
      content.append(P_DELIM);
      content.append(getStr(year));
      content.append(Y_DELIM);
      content.append(getStr(month));
      content.append(M_DELIM);
      content.append(getStr(day));
      content.append(D_DELIM);
      content.append(T_DELIM);
      content.append(getStr(hour));
      content.append(H_DELIM);
      content.append(getStr(minute));
      content.append(M_DELIM);
      content.append(getStr(second));
      content.append(S_DELIM);
      return content.toString();
    }
    else {
      return getParseUntil(
        getString(year),
        getString(month),
        getString(day),
        getString(hour),
        getString(minute),
        getStr(second));
    }
  }

  public static String getParseUntil(
    String year,
    String month,
    String day,
    String hour,
    String minute,
    String second
  ) {
    StringBuffer content = new StringBuffer();
    content.append(year);
    content.append(MINUS);
    content.append(month);
    content.append(MINUS);
    content.append(day);
    content.append(T_DELIM);
    content.append(hour);
    content.append(COLON);
    content.append(minute);
    content.append(COLON);
    content.append(second);
    return content.toString();
  }

  public static int parseInt(String value) {
    return getInt(value);
  }

  public static double parseDouble(String value) {
    return getDouble(value);
  }

  private static String getString(int value) {
    if (0 <= value && value <= NINE) {
      return ZERO + value;
    }
    return getStr(value);
  }

  private static String getStr(int value) {
    return EMPTY + value;
  }
  
  private static String getStr(double value) {
    return EMPTY + value;
  }
  
  private static final int NINE = 9;

  private static final String EMPTY   =  ""; // NOI18N
  private static final String ZERO    = "0"; // NOI18N
  private static final String MINUS   = "-"; // NOI18N
  private static final String COLON   = ":"; // NOI18N
  private static final String QUOTE   = "'"; // NOI18N

  private static final String D_DELIM = "D"; // NOI18N
  private static final String H_DELIM = "H"; // NOI18N
  private static final String M_DELIM = "M"; // NOI18N
  private static final String P_DELIM = "P"; // NOI18N
  private static final String S_DELIM = "S"; // NOI18N
  private static final String Y_DELIM = "Y"; // NOI18N
  public  static final String T_DELIM = "T"; // NOI18N
}
