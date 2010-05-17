/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.cvsclient.connection;

import org.netbeans.lib.cvsclient.file.FileUtils;

import java.io.*;
import java.util.*;

/**
 * Represents .cvspass passwords file.
 *
 * @author Petr Kuzel
 */
public final class PasswordsFile {

    /**
     * Locates scrambled password for given CVS Root.
     *
     * @param cvsRootString identifies repository session [:method:][[user][:password]@][hostname[:[port]]]/path/to/repository
     * @return scrambled password or <code>null</code>
     */
    public static String findPassword(String cvsRootString) {
        File passFile = new File(System.getProperty("cvs.passfile", System.getProperty("user.home") + "/.cvspass"));
        BufferedReader reader = null;
        String password = null;

        try {
            reader = new BufferedReader(new FileReader(passFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = normalize(line);
                if (line.startsWith(cvsRootString+" ")) {
                    password = line.substring(cvsRootString.length() + 1);
                    break;
                }
            }
        } catch (IOException e) {
            return null;
        }
        finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) {}
            }
        }
        return password;

    }

    /**
     * List roots matching given prefix e.g. <tt>":pserver:"</tt>.
     */
    public static Collection listRoots(String prefix) {

        List roots = new ArrayList();

        File passFile = new File(System.getProperty("cvs.passfile", System.getProperty("user.home") + "/.cvspass"));
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(passFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = normalize(line);
                String elements[] = line.split(" ");  // NOI18N
                if (elements[0].startsWith(prefix)) {
                    roots.add(elements[0]);
                }
            }
        } catch (IOException e) {
            return Collections.EMPTY_SET;
        }
        finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) {}
            }
        }
        return roots;
    }

    /**
     * Writes scrambled password for given CVS root.
     * Eliminates all previous values and possible duplicities.
     *
     * @param cvsRootString identifies repository session [:method:][[user][:password]@][hostname[:[port]]]/path/to/repository
     * @param encodedPassword
     * @throws IOException on write failure
     */
    public static void storePassword(String cvsRootString, String encodedPassword) throws IOException {
        File passFile = new File(System.getProperty("cvs.passfile",
                                                    System.getProperty("user.home") + File.separatorChar +
                                                    ".cvspass"));
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            final String LF = System.getProperty("line.separator"); // NOI18N
            if (passFile.createNewFile()) {
                writer = new BufferedWriter(new FileWriter(passFile));
                writer.write(cvsRootString + " " + encodedPassword + LF);
                writer.close();
            }
            else {
                File tempFile = File.createTempFile("cvs", "tmp");
                reader = new BufferedReader(new FileReader(passFile));
                writer = new BufferedWriter(new FileWriter(tempFile));
                String line;
                boolean stored = false;
                while ((line = reader.readLine()) != null) {
                    if (normalize(line).startsWith(cvsRootString + " ")) {
                        if (stored == false) {
                            writer.write(cvsRootString + " " + encodedPassword + LF);
                            stored = true;
                        }
                    }
                    else {
                        writer.write(line + LF);
                    }
                }
                if (stored == false) {
                    writer.write(cvsRootString + " " + encodedPassword + LF);
                }
                reader.close();
                writer.flush();
                writer.close();

                // copyFile needs less permissions than File.renameTo
                FileUtils.copyFile(tempFile, passFile);
                tempFile.delete();
            }
        }
        finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Normalizes several possible line formats into
     * 'normal' one that allows to apply dumb string operations.
     */
    private static String normalize(String line) {
        if (line.startsWith("/1 ")) {  // NOI18N
            line = line.substring("/1 ".length()); // NOI18N
        }
        return line;
    }
}
