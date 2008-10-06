/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.web.client.tools.common.launcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author  Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class Utils {

    public static String getDebuggerLauncherURI(int port, String sessionId) {
        File tmpFolder = FileUtil.normalizeFile(new File(System.getProperty("java.io.tmpdir")));
        FileObject tmpFolderFileObject = FileUtil.toFileObject(tmpFolder);
        FileObject tmpFileObject;
        try {
            String fileName = "modules.ext.debugger---"
                    // Must match with port paramater name in
                    // extensions/firefox/netbeans-firefox-extension/content/netbeans-firefox-extension-debugger-launcher.js
                    + "netbeans-debugger-port="
                    + port
                    + "--"
                    // Must match with sessionId paramater name in
                    // extensions/firefox/netbeans-firefox-extension/content/netbeans-firefox-extension-debugger-launcher.js
                    + "netbeans-debugger-session-id="
                    + sessionId;
            // XXX #131857 If the file is already there, reuse it.
            tmpFileObject = tmpFolderFileObject.getFileObject(fileName);
            if (tmpFileObject == null) {
                tmpFileObject = FileUtil.copyFile(
                        getDebuggerLauncherFileObject(),
                        tmpFolderFileObject,
                        fileName);
            }
            File tmpdebuggerDotHtmlFile = FileUtil.toFile(tmpFileObject);
            tmpdebuggerDotHtmlFile.deleteOnExit();
            
            fileName = "netbeans.jpg";
            tmpFileObject = tmpFolderFileObject.getFileObject(fileName);
            if (tmpFileObject == null) { // NOI18N               
                // Copy the logo
                FileUtil.toFile(FileUtil.copyFile(
                        getNetBeansLogoFileObject(),
                        tmpFolderFileObject,
                        "netbeans")).deleteOnExit(); // NOI18N
            }
                    
            return tmpdebuggerDotHtmlFile.toURI().toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // The relative path to the debugger launcher file
    // This must match the search term in
    // extensions/firefox/netbeans-firefox-extension/content/netbeans-firefox-extension-debugger-launcher.js
    private static final String DEBUGGER_DOT_HTML = "modules/ext/debugger.html"; // NOI18N
    private static FileObject debuggerDotHtmlFileObject;
    private static final String NETBEANS_DOT_JPG = "modules/ext/netbeans.jpg"; // NOI18N
    private static FileObject netbeansDotJpgFileObject;

    private static FileObject getDebuggerLauncherFileObject() {
        if (debuggerDotHtmlFileObject == null) {
            debuggerDotHtmlFileObject = FileUtil.toFileObject(
                    InstalledFileLocator.getDefault().locate(DEBUGGER_DOT_HTML,
                    // This should match the code name base of the module in
                    // which this file lives. The code name base can be found in
                    // nbproject/project.xml
                    "org.netbeans.modules.web.client.tools.common", // NOI18N
                    true));
        }
        return debuggerDotHtmlFileObject;
    }
    
    private static FileObject getNetBeansLogoFileObject() {
        if (netbeansDotJpgFileObject == null) {
            netbeansDotJpgFileObject = FileUtil.toFileObject(
                    InstalledFileLocator.getDefault().locate(NETBEANS_DOT_JPG,
                    // This should match the code name base of the module in
                    // which this file lives. The code name base can be found in
                    // nbproject/project.xml
                    "org.netbeans.modules.web.client.tools.common", // NOI18N
                    false));
        }
        return netbeansDotJpgFileObject;
    }

}
