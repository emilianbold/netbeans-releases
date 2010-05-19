/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.utils;

import java.awt.Dimension;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.openide.awt.Mnemonics;
import org.openide.util.NbPreferences;

/**
 * Various utilities.
 * 
 * @author sunsoabi_edwong
 */
public class Utils {
    
    public static final String SCHEDULER_PREFS = "scheduler";           //NOI18N
    
    private static Preferences PREFS = null;
    private static final char DECIMAL_SEP = (new DecimalFormatSymbols())
            .getDecimalSeparator();

    public static boolean equals(String s1, String s2) {
        return (s1 != null) ? s1.equals(s2) : (null == s2);
    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        return (s1 != null) ? s1.equalsIgnoreCase(s2) : (null == s2);
    }
    
    public static String trim(String s) {
        return (s != null) ? s.trim() : null;
    }
    
    public static String trim(double d) {
        return trim(4, d);
    }
    
    public static String trim(int prec, double d) {
        String numStr = String.format("%." + prec + "f", d);            //NOI18N
        StringBuilder sb = new StringBuilder(numStr);
        int dfirst = -1;
        for (int i = sb.length() - 1; i >= 0; i--) {
            if (sb.charAt(i) == '0') {                                  //NOI18N
                dfirst = i;
            } else {
                break;
            }
        }
        if (0 == dfirst) {
            dfirst = (sb.length() > 1) ? 1 : -1;
        }
        if (dfirst != -1) {
            if (dfirst > 0) {
                if (sb.charAt(dfirst - 1) == DECIMAL_SEP) {
                    dfirst--;
                }
            }
            sb.delete(dfirst, sb.length());
        }
        return sb.toString();
    }
    
    public static boolean isEmpty(String s) {
        return (null == s) || (s.length() == 0);
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isEmpty(Collection col) {
        return (null == col) || (col.size() == 0);
    }
    
    public static <T> boolean isEmpty(T[] array) {
        return (null == array) || (array.length == 0);
    }
    
    public static Dimension max(Dimension d1, Dimension d2) {
        return new Dimension(Math.max(d1.width, d2.width),
                Math.max(d1.height, d2.height));
    }
    
    public static void callFromEDT(boolean always, Runnable doRun) {
        if (always || !SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(doRun);
        } else {
            doRun.run();
        }
    }
    
    public static Preferences getPreferences() {
        if (null == PREFS) {
            PREFS = NbPreferences.forModule(Utils.class);
        }
        return PREFS;
    }
    
    public static Preferences getSchedulerPrefs() {
        return getPreferences().node(SCHEDULER_PREFS);
    }
    
    public static boolean isHtml(String s) {
        return (s != null) ? (s.toLowerCase().indexOf("<html>") > -1)   //NOI18N
                : false;
    }
    
    public static String toHtml(String s) {
        return (s != null) ? "<html>" + s + "</html>" : s;              //NOI18N
    }
    
    public static String expungeMnemonicAmpersand(String s) {
        if (isEmpty(s) || (s.indexOf('&') == -1)) {                     //NOI18N
            return s;
        }
        
        int amp = Mnemonics.findMnemonicAmpersand(s);
        if (amp != -1) {
            return (new StringBuilder()).append(s, 0, amp)
                    .append(s, amp + 1, s.length()).toString();
        }
        return s;
    }
    
    public static String firstSecondLastOfList(String[] list) {
        StringBuilder sb = new StringBuilder();
        if (list.length > 0) {
            sb.append(list[0]);
        }
        if (list.length > 1) {
            sb.append(", ").append(list[1]);                            //NOI18N
        }
        if (list.length > 2) {
            sb.append(", ");                                            //NOI18N
            if (list.length > 3) {
                sb.append(" ... ,");                                    //NOI18N
            }
            sb.append(list[list.length - 1]);                           //NOI18N
        }
        return sb.toString();
    }
}
