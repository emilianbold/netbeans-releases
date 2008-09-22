/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
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

package org.netbeans.modules.cnd.api.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Miscellaneous utility classes useful for the C/C++/Fortran module */
public class CppUtils {
    
    private static String cygwinBase;
    private static String mingwBase;
    private static String msysBase;
    
    public static String reformatWhitespaces(String string)  {
        return reformatWhitespaces(string, ""); // NOI18N
    }
    
    public static String reformatWhitespaces(String string, String prepend)  {
        return reformatWhitespaces(string, prepend, ""); // NOI18N
    }
    
    public static String reformatWhitespaces(String string, String prepend, String delimiter)  {
        if (string == null || string.length() == 0)
            return string;
        
        boolean firstToken = true;
        ArrayList<String> tokens = tokenizeString(string);
        StringBuilder formattedString = new StringBuilder(string.length());
        for (String token : tokens) {
            if (!firstToken) {
                formattedString.append(delimiter);
                formattedString.append(" "); // NOI18N
            }
            formattedString.append(prepend);
            formattedString.append(token);
            firstToken = false;
        }
        
        return formattedString.toString();
    }
    
    public static ArrayList<String> tokenizeString(String string)  {
        ArrayList<String> list = new ArrayList<String>(0);
        
        if (string == null || string.length() == 0)
            return list;
        StringBuilder token = new StringBuilder();
        boolean inToken = false;
        boolean inQuote = false;
        char quoteChar = '\0';
        for (int i = 0; i <= string.length(); i++) {
            boolean eol = (i == string.length());
            if (eol || inToken) {
                if (!eol && inQuote) {
                    token.append(string.charAt(i));
                    if (string.charAt(i) == quoteChar)
                        inQuote = false;
                } else {
                    if (eol || Character.isWhitespace(string.charAt(i))) {
                        if (token.length() > 0) {
                            list.add(token.toString());
                            }
                        inToken = false;
                        token = new StringBuilder();
                    } else {
                        token.append(string.charAt(i));
                        if (string.charAt(i) == '"' || string.charAt(i) == '`' || string.charAt(i) == '\'') {
                            inQuote = true;
                            quoteChar = string.charAt(i);
                        }
                    }
                }
            } else {
                if (!Character.isWhitespace(string.charAt(i))) {
                    token.append(string.charAt(i));
                    inToken = true;
                }
            }
        }
        if (token.length() > 0)
            list.add(token.toString());
        
        return list;
    }
    
    public static String getCygwinBase() {
        if (cygwinBase == null) {
            File file = new File("C:/Windows/System32/reg.exe"); // NOI18N

            if (file.exists()) {
                // On Vista (at least on my new laptop) I get the Cygwin mounts in HKEY_USERS and HKEY_CURRENT_USER.
                // So look here first.
                List<String> list = new ArrayList<String>();
                list.add(file.getAbsolutePath());
                list.add("query"); // NOI18N
                list.add("hkcu\\software\\cygnus solutions\\cygwin\\mounts v2\\/"); // NOI18N
                ProcessBuilder pb = new ProcessBuilder(list);
                pb.redirectErrorStream(true);
                try {
                    Process process = pb.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("native")) { // NOI18N
                            int pos = line.lastIndexOf("REG_SZ"); // NOI18N
                            if (pos != -1 && pos < line.length()) {
                                cygwinBase = line.substring(pos + 6).trim();
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                }
                if (cygwinBase == null) {
                    // If not found in HKU, look for sytem mounts
                    list.clear();
                    list.add(file.getAbsolutePath());
                    list.add("query"); // NOI18N
                    list.add("hklm\\software\\cygnus solutions\\cygwin\\mounts v2\\/"); // NOI18N
                    pb = new ProcessBuilder(list);
                    pb.redirectErrorStream(true);
                    try {
                        Process process = pb.start();
                        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = br.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("native")) { // NOI18N
                                int pos = line.lastIndexOf("REG_SZ"); // NOI18N
                                if (pos != -1 && pos < line.length()) {
                                    cygwinBase = line.substring(pos + 6).trim();
                                    break;
                                }
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            if (cygwinBase == null) {
                for (String dir : Path.getPath()) {
                    if (dir.toLowerCase().contains("cygwin")) { // NOI18N
                        if (dir.toLowerCase().endsWith("\\usr\\bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 8);
                            break;
                        } else if (dir.toLowerCase().endsWith("\\bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 4);
                            break;
                        }
                    }
                }
            }
            if (cygwinBase == null) {
                for (String dir : Path.getPath()) {
                    if (dir.toLowerCase().endsWith("\\cygwin\\bin")) { // NOI18N
                        cygwinBase = dir.substring(0, dir.length() - 4);
                        break;
                    }
                }
            }
        }
        return cygwinBase;
    }
    
    public static String getMinGWBase() {
        if (mingwBase == null) {
            File file = new File("C:/Windows/System32/reg.exe"); // NOI18N

            if (file.exists()) {
                List<String> list = new ArrayList<String>();
                list.add(file.getAbsolutePath());
                list.add("query"); // NOI18N
                list.add("hklm\\software\\microsoft\\windows\\currentversion\\uninstall\\MinGW"); // NOI18N
                ProcessBuilder pb = new ProcessBuilder(list);
                pb.redirectErrorStream(true);
                try {
                    Process process = pb.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("InstallLocation")) { // NOI18N
                            int pos = line.lastIndexOf("REG_SZ"); // NOI18N
                            if (pos != -1 && pos < line.length()) {
                                mingwBase = line.substring(pos + 6).trim();
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            }
            if (mingwBase == null) {
                for (String dir : Path.getPath()) {
                    if (dir.toLowerCase().endsWith("\\mingw\\bin")) { // NOI18N
                        mingwBase = dir.substring(0, dir.length() - 4);
                        break;
                    }
                }
            }
        }
        return mingwBase;
    }
    
    public static String getMSysBase() {
        if (msysBase == null) {
            File file = new File("C:/Windows/System32/reg.exe"); // NOI18N

            if (file.exists()) {
                List<String> list = new ArrayList<String>();
                list.add(file.getAbsolutePath());
                list.add("query"); // NOI18N
                list.add("hklm\\software\\microsoft\\windows\\currentversion\\uninstall\\msys-1.0_is1"); // NOI18N
                ProcessBuilder pb = new ProcessBuilder(list);
                pb.redirectErrorStream(true);
                try {
                    Process process = pb.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("Inno Setup: App Path")) { // NOI18N
                            int pos = line.lastIndexOf(':');
                            if (pos != -1 && pos < line.length()) {
                                msysBase = line.substring(pos - 1);
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            }
            if (msysBase == null) {
                for (String dir : Path.getPath()) {
                    if (dir.toLowerCase().contains("\\msys\\1.0") && dir.toLowerCase().contains("\\bin")) { // NOI18N
                        msysBase = dir.substring(0, dir.length() - 4);
                        break;
                    }
                }
            }
        }
        return msysBase;
    }
}

