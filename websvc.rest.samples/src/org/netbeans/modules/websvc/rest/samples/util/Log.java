/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.rest.samples.util;

/**
 * @author Peter Liu
 */
public abstract class Log {

  public static void out () {
    println (isLogEnable || isInfoEnable);
  }

  public static void out (String message) {
    println (isLogEnable || isInfoEnable, message);
  }

  public static void info () {
    println (isInfoEnable);
  }

  public static void info (String message) {
    println (isInfoEnable, message);
  }

  private static void println (boolean isEnable) {
    if (isEnable) {
      System.out.println ();
    }
  }

  public static void println (boolean isEnable, String message) {
    if (isEnable) {
      System.out.println ("**soa sample** " + message); // NOI18N
    }
  }

  private static String value = System.getProperty ("orch2.log"); // NOI18N
  private static boolean isLogEnable = value != null && value.indexOf ("log") != -1; // NOI18N
  private static boolean isInfoEnable = value != null && value.indexOf ("info") != -1; // NOI18N
}
 
