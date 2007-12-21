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

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * This utility class provide conversion parsing and serialization of 
 * date/time duration information according to the BPEL specific format. 
 * 
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.14
 */
public final class TimeEventUtil {

  private TimeEventUtil() {}

  public static int parseInt(String value) {
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      return -1;
    }
  }

  public static String getContent(
    boolean isFor,
    int year,
    int month,
    int day,
    int hour,
    int minute,
    int second)
  {
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
      return getParseText(
        getString(year),
        getString(month),
        getString(day),
        getString(hour),
        getString(minute),
        getString(second));
    }
  }

  public static String getParseText(
    String year,
    String month,
    String day,
    String hour,
    String minute,
    String second)
  {
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

  private static String getString(int value) {
    if (0 <= value && value <= NINE) {
      return TimeEventUtil.ZERO + value;
    }
    return getStr(value);
  }

  private static String getStr(int value) {
    return EMPTY + value;
  }

  public static final int NINE = 9;

  public static final String EMPTY   = "";  // NOI18N
  public static final String ZERO    = "0"; // NOI18N
  public static final String MINUS   = "-"; // NOI18N
  public static final String COLON   = ":"; // NOI18N
  public static final String QUOTE   = "'"; // NOI18N

  public static final String D_DELIM = "D"; // NOI18N
  public static final String H_DELIM = "H"; // NOI18N
  public static final String M_DELIM = "M"; // NOI18N
  public static final String P_DELIM = "P"; // NOI18N
  public static final String S_DELIM = "S"; // NOI18N
  public static final String T_DELIM = "T"; // NOI18N
  public static final String Y_DELIM = "Y"; // NOI18N
}
