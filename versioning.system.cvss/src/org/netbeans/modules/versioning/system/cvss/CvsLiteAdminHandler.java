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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.openide.filesystems.FileUtil;

import java.io.*;
import java.util.*;

/**
 * Behaves as standard admin handler except for deleted files and directories where it acts as if the files
 * were removed with 'cvs remove'.
 * 
 * @author Maros Sandor
 */
class CvsLiteAdminHandler implements AdminHandler {

    static final String INVALID_METADATA_MARKER = "invalid-metadata"; // NOI18N

    private static final String INVALID_METADATA_MARKER_PATH = CvsVersioningSystem.FILENAME_CVS + "/" + INVALID_METADATA_MARKER; // NOI18N

    private StandardAdminHandler stdHandler;

    public CvsLiteAdminHandler() {
        this.stdHandler = new StandardAdminHandler();
    }

    private void checkForInvalidMetadata(File dir) {
        File marker = new File(dir, INVALID_METADATA_MARKER_PATH);
        if (marker.exists()) {
            Utils.deleteRecursively(marker.getParentFile());
        }
    }

    public void updateAdminData(String localDirectory, String repositoryPath,
                         Entry entry, GlobalOptions globalOptions)
            throws IOException {
        checkForInvalidMetadata(new File(localDirectory));
        stdHandler.updateAdminData(localDirectory, repositoryPath, entry, globalOptions);
    }

    public Entry getEntry(File file) throws IOException {
        checkForInvalidMetadata(file.getParentFile());
        return stdHandler.getEntry(file);
    }

    public boolean exists(File file) {
        if (file.exists()) return true;
        if (CvsVersioningSystem.FILENAME_CVS.equals(file.getName())) file = file.getParentFile(); // NOI18N
        return false;
    }

    public Iterator<Entry> getEntries(File directory) throws IOException {
        checkForInvalidMetadata(directory);
        if (new File(directory, CvsVersioningSystem.FILENAME_CVS).isDirectory()) {
            return stdHandler.getEntries(directory);
        }
        directory = FileUtil.normalizeFile(directory);
        return stdHandler.getEntries(directory);
    }
    
    public Entry[] getEntriesAsArray(File directory)
            throws IOException {
        checkForInvalidMetadata(directory);
        List<Entry> entries = new ArrayList<Entry>();
        for (Iterator<Entry> i = getEntries(directory); i.hasNext(); ) {
            entries.add(i.next());
        }
        return entries.toArray(new Entry[entries.size()]);
    }    

    public void setEntry(File file, Entry entry) throws IOException {
        checkForInvalidMetadata(file.getParentFile());
        // create missing directories beforehand
        File adminDir = new File(file.getParentFile(), CvsVersioningSystem.FILENAME_CVS);
        createAdminDirs(adminDir);
        stdHandler.setEntry(file, entry);
    }

    /**
     * Restores all administration files in the given directory and all parent directories recursively.
     *
     * @param adminDir directory to restore
     * @throws IOException
     */
    private void createAdminDirs(File adminDir) throws IOException {
        if (!adminDir.exists()) {
            if (adminDir.getParentFile() != null && adminDir.getParentFile().getParentFile() != null) {
                createAdminDirs(new File(adminDir.getParentFile().getParentFile(), CvsVersioningSystem.FILENAME_CVS));
            }
        }
    }

    public String getRepositoryForDirectory(String directory, String repository)
            throws IOException {

        checkForInvalidMetadata(new File(directory));
        // TODO consult MetadataAttic.getScheduledRepository

        File dirFile = new File(directory);
        if (dirFile.exists()) return stdHandler.getRepositoryForDirectory(directory, repository);
        
        return stdHandler.getRepositoryForDirectory(directory, repository);
    }

    public void removeEntry(File file) throws IOException {
        File parent = file.getParentFile();
        checkForInvalidMetadata(parent);
        stdHandler.removeEntry(file);
    }

    public Set getAllFiles(File directory) throws IOException {
        checkForInvalidMetadata(directory);
        // TODO: override
        return stdHandler.getAllFiles(directory);
    }

    public String getStickyTagForDirectory(File directory) {
        checkForInvalidMetadata(directory);
        return stdHandler.getStickyTagForDirectory(directory);
    }
}
