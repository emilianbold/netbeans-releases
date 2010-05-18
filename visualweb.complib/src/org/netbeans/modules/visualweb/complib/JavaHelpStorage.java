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

package org.netbeans.modules.visualweb.complib;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

import javax.help.HelpSet;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Handle the installation and uninstallation of JavaHelp for a component
 * library.
 * 
 * @author Edwin Goei
 */
class JavaHelpStorage {

    private static final FileObject fsRoot = FileUtil.getConfigRoot();

    /**
     * Install complib help into system.
     * 
     * @param complib
     * @throws IOException
     */
    static void installComplibHelp(ExtensionComplib complib) throws IOException {
        String helpSetFile = complib.getHelpSetFile();
        if (helpSetFile == null) {
            // No HelpSet file was specified so do nothing
            return;
        }

        URL hsUrl = HelpSet.findHelpSet(complib.getClassLoader(), helpSetFile);
        if (hsUrl == null) {
            IdeUtil
                    .logWarning("Unable to access HelpSet file for component library: "
                            + helpSetFile);
        }

        String refPath = getHelpSetRefPath(complib);
        FileObject helpSetRefFO = FileUtil.createData(fsRoot, refPath);

        writeHelpSetRefFile(helpSetRefFO, hsUrl);
    }

    /**
     * Uninstall complib help from system if found else do nothing.
     * 
     * @param complib
     * @throws IOException
     */
    static void uninstallComplibHelp(ExtensionComplib complib)
            throws IOException {
        String helpsetrefPath = getHelpSetRefPath(complib);
        FileObject helpSetRefFO = fsRoot.getFileObject(helpsetrefPath);
        if (helpSetRefFO != null) {
            helpSetRefFO.delete();
        }
    }

    private static String getHelpSetRefPath(ExtensionComplib complib) {
        String helpsetrefPath = "Services/JavaHelp/"
                + complib.getDirectoryBaseName() + "-helpset.xml";
        return helpsetrefPath;
    }

    private static void writeHelpSetRefFile(final FileObject helpSetRefFO,
        final URL hsUrl) throws IOException {
        helpSetRefFO.getFileSystem().runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        writeHelpSetRef(helpSetRefFO, hsUrl);
                    }
                });
    }

    private static void writeHelpSetRef(FileObject definitionFile, URL hsUrl)
            throws IOException {
        FileLock lock = null;
        PrintWriter out = null;
        try {
            lock = definitionFile.lock();
            out = new PrintWriter(new OutputStreamWriter(definitionFile
                    .getOutputStream(lock), "UTF-8"));
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
            out
                    .println("<!DOCTYPE helpsetref PUBLIC \"-//NetBeans//DTD JavaHelp Help Set Reference 1.0//EN\" \"http://www.netbeans.org/dtds/helpsetref-1_0.dtd\">"); // NOI18N
            out.println("<helpsetref url=\"" + hsUrl.toExternalForm() + "\"/>"); // NOI18N
        } finally {
            if (out != null)
                out.close();
            if (lock != null)
                lock.releaseLock();
        }
    }
}
