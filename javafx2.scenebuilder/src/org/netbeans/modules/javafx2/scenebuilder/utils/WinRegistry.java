/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.scenebuilder.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
public class WinRegistry {

    private static final String REGQUERY_UTIL = "reg query "; // NOI18N
    private static final String REGSTR_TOKEN = "REG_SZ"; // NOI18N
    private static final String REGDWORD_TOKEN = "REG_DWORD"; // NOI18N

    public static boolean isAvailable() {
        return Utilities.isWindows();
    }
    
    public static String getString(String path) {
        String result = runQuery(path);
        int p = result.indexOf(REGSTR_TOKEN);

        if (p == -1) {
            return null;
        }

        return result.substring(p + REGSTR_TOKEN.length()).trim();
    }

    public static Integer getDWord(String path) {
        String result = runQuery(path);
        int p = result.indexOf(REGDWORD_TOKEN);

        if (p == -1) {
            return null;
        }

        // CPU speed in Mhz (minus 1) in HEX notation, convert it to DEC
        String temp = result.substring(p + REGDWORD_TOKEN.length()).trim();
        if (temp.startsWith("0x")) {
            return Integer.parseInt(temp.substring("0x".length()), 16);
        } else {
            return Integer.parseInt(temp);
        }
    }

    private static String runQuery(String path) {
        try {
            Process process = Runtime.getRuntime().exec(REGQUERY_UTIL + path);
            StreamReader reader = new StreamReader(process.getInputStream());

            reader.start();
            process.waitFor();
            reader.join();

            return reader.getResult();
        } catch (Exception e) {
            return null;
        }
    }

    static class StreamReader extends Thread {

        private InputStream is;
        private StringWriter sw;

        StreamReader(InputStream is) {
            this.is = is;
            sw = new StringWriter();
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1) {
                    sw.write(c);
                }
            } catch (IOException e) {;
            }
        }

        String getResult() {
            return sw.toString();
        }
    }
}