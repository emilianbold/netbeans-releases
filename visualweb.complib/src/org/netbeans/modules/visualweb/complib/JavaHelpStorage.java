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
import org.openide.filesystems.Repository;

/**
 * Handle the installation and uninstallation of JavaHelp for a component
 * library.
 * 
 * @author Edwin Goei
 */
class JavaHelpStorage {

    private static final FileObject fsRoot = Repository.getDefault()
            .getDefaultFileSystem().getRoot();

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
