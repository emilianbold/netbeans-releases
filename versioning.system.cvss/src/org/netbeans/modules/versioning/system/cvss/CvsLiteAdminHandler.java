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
import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
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
        File parent = file.getParentFile();
        CvsMetadata data = MetadataAttic.getMetadata(parent);
        if (new File(parent, CvsVersioningSystem.FILENAME_CVS).isDirectory() || data == null) {
            return stdHandler.getEntry(file);
        }
        String [] se = data.getEntries();
        for (int i = 0; i < se.length; i++) {
            Entry entry = new Entry(se[i]);
            if (entry.getName() != null && entry.getName().equals(file.getName())) return entry;
        }
        return null;
    }

    public boolean exists(File file) {
        if (file.exists()) return true;
        if (CvsVersioningSystem.FILENAME_CVS.equals(file.getName())) file = file.getParentFile(); // NOI18N
        return MetadataAttic.getMetadata(file) != null;
    }

    public Iterator getEntries(File directory) throws IOException {
        checkForInvalidMetadata(directory);
        if (new File(directory, CvsVersioningSystem.FILENAME_CVS).isDirectory()) {
            return stdHandler.getEntries(directory);
        }
        directory = FileUtil.normalizeFile(directory);
        CvsMetadata data = MetadataAttic.getMetadata(directory);
        if (data == null) {             
            return stdHandler.getEntries(directory);
        }
        return new CvsMetadataIterator(data);
    }
    
    public Entry[] getEntriesAsArray(File directory)
            throws IOException {
        checkForInvalidMetadata(directory);
        List entries = new ArrayList();
        for (Iterator i = getEntries(directory); i.hasNext(); ) {
            entries.add(i.next());
        }
        return (Entry[]) entries.toArray(new Entry[entries.size()]);
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
            CvsMetadata data = MetadataAttic.getMetadata(adminDir.getParentFile());
            if (data != null) {
                data.save(adminDir);
                MetadataAttic.setMetadata(adminDir.getParentFile(), null);
            }
        }
    }

    public String getRepositoryForDirectory(String directory, String repository)
            throws IOException {

        checkForInvalidMetadata(new File(directory));
        // TODO consult MetadataAttic.getScheduledRepository

        File dirFile = new File(directory);
        if (dirFile.exists()) return stdHandler.getRepositoryForDirectory(directory, repository);
        
        CvsMetadata data = MetadataAttic.getMetadata(dirFile);
        if (data == null) return stdHandler.getRepositoryForDirectory(directory, repository);
        
        String fileRepository = data.getRepository();
        if (fileRepository == null) {
            fileRepository = ""; // NOI18N
        }

        if (fileRepository.startsWith("/")) { // NOI18N
            return fileRepository;
        }
        // otherwise the cvs is using relative repository path
        // must be a forward slash, regardless of the local filing system
        return repository + '/' + fileRepository;
    }

    public void removeEntry(File file) throws IOException {
        File parent = file.getParentFile();
        checkForInvalidMetadata(parent);
        CvsMetadata data = MetadataAttic.getMetadata(parent);
        if (data != null) {
            String [] entries = data.getEntries();
            for (int i = 0; i < entries.length; i++) {
                Entry entry = new Entry(entries[i]);
                if (file.getName().equals(entry.getName())) {
                    List newEntries = new ArrayList(Arrays.asList(entries));
                    newEntries.remove(entries[i]);
                    if (newEntries.size() == 0 || newEntries.size() == 1 && "D".equals(newEntries.get(0))) { // NOI18N
                        MetadataAttic.setMetadata(parent, null);
                    } else {
                        CvsMetadata newData = new CvsMetadata(data.getRepository(), data.getRoot(), (String[]) newEntries.toArray(new String[newEntries.size()]));
                        MetadataAttic.setMetadata(parent, newData);
                    }
                    return;
                }
            }
        }
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

    private static class CvsMetadataIterator implements Iterator {
        
        private final Entry [] entries;
        private int index;

        public CvsMetadataIterator(CvsMetadata data) {
            String [] se = data.getEntries();
            List le = new ArrayList();
            for (int i = 0; i < se.length; i++) {
                Entry entry = new Entry(se[i]);
                if (entry.getName() != null) {
                    le.add(entry);
                }
            }
            entries = (Entry[]) le.toArray(new Entry[le.size()]);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return index < entries.length;
        }

        public Object next() {
            return entries[index++];
        }
    }
}
