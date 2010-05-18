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
package org.netbeans.modules.dlight.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * Util class
 */
public final class ExecUtil {

    static private final Logger log = DLightLogger.getLogger(ExecUtil.class);

    public static void setExecutionPermissions(final List<String> files) {
        if (files.isEmpty()) {
            return;
        }

        List<String> paths = new ArrayList<String>();
        for (String f : files) {
            String fullPath = getFullPath(f);
            if (new File(fullPath).exists()) {
                paths.add(fullPath);
            }
        }

        List<String> commands = new ArrayList<String>();
        commands.add("/bin/chmod"); // NOI18N
        commands.add("755"); // NOI18N
        commands.addAll(paths);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            //Gizmo.err.log("Cannot set execution permissions of files! " + ex.getMessage()); // NOI18N
        }
    }

    public static String getFullPath(String relpath) {
        File file = InstalledFileLocator.getDefault().locate(relpath, null, false);

        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        }

        return relpath;
    }

    private static List<Integer> runScript(String script, String params) {
        log.finest("Run script " + script + " " + params);//NOI18N
        List<Integer> res = new ArrayList<Integer>();

        File scriptFile = InstalledFileLocator.getDefault().locate("bin/" + script, null, false);//NOI18N

        if (scriptFile == null) {
            log.severe("Script " + script + " not found!");//NOI18N
            return Collections.emptyList();
        }

        InputStreamReader reader = null;

        try {
            Process process = Runtime.getRuntime().exec("/bin/sh " + scriptFile.getAbsolutePath() + " " + params);//NOI18N

            InputStream is = process.getInputStream();
            reader = new InputStreamReader(Channels.newInputStream(Channels.newChannel(is)));

            int c;
            StringBuilder sb = new StringBuilder();

            while ((c = reader.read()) != -1) {
                if (c == '\n') {//NOI18N
                    res.add(Integer.parseInt(sb.toString().trim()));
                    sb = new StringBuilder();
                } else {
                    sb.append((char) c);
                }
            }

            process.destroy();
        } catch (ClosedByInterruptException ex) {
            // ignore
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }

        return res;
    }
}
