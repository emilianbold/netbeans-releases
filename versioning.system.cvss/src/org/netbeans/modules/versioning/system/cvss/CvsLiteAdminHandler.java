/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
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

    private StandardAdminHandler stdHandler;

    public CvsLiteAdminHandler() {
        this.stdHandler = new StandardAdminHandler();
    }

    public void updateAdminData(String localDirectory, String repositoryPath,
                         Entry entry, GlobalOptions globalOptions)
            throws IOException {
        stdHandler.updateAdminData(localDirectory, repositoryPath, entry, globalOptions);
    }

    public Entry getEntry(File file) throws IOException {
        if (file.exists()) return stdHandler.getEntry(file);
        File parent = file.getParentFile();
        if (parent.exists()) return stdHandler.getEntry(file);
        CvsMetadata data = MetadataAttic.getMetadata(parent);
        if (data == null) return stdHandler.getEntry(file);
        String [] se = data.getEntries();
        for (int i = 0; i < se.length; i++) {
            Entry entry = new Entry(se[i]);
            if (entry.getName() != null && entry.getName().equals(file.getName())) return entry;
        }
        return null;
    }

    public boolean exists(File file) {
        if (file.exists()) return true;
        if ("CVS".equals(file.getName())) file = file.getParentFile();
        return MetadataAttic.getMetadata(file) != null;
    }

    public Iterator getEntries(File directory) throws IOException {
        if (directory.exists()) {
            return stdHandler.getEntries(directory);
        } else {
            directory = FileUtil.normalizeFile(directory);
            CvsMetadata data = MetadataAttic.getMetadata(directory);
            if (data == null) return Collections.EMPTY_LIST.iterator();
            return new CvsMetadataIterator(data);
        }
    }
    
    public Entry[] getEntriesAsArray(File directory)
            throws IOException {
        List entries = new ArrayList();
        for (Iterator i = getEntries(directory); i.hasNext(); ) {
            entries.add(i.next());
        }
        return (Entry[]) entries.toArray(new Entry[entries.size()]);
    }    

    public void setEntry(File file, Entry entry) throws IOException {
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

        // TODO consult MetadataAttic.getScheduledRepository

        File dirFile = new File(directory);
        if (dirFile.exists()) return stdHandler.getRepositoryForDirectory(directory, repository);
        
        CvsMetadata data = MetadataAttic.getMetadata(dirFile);
        if (data == null) return stdHandler.getRepositoryForDirectory(directory, repository);
        
        String fileRepository = data.getRepository();
        if (fileRepository == null) {
            fileRepository = ""; //NOI18N
        }

        if (fileRepository.startsWith("/")) { //NOI18N
            return fileRepository;
        }
        // otherwise the cvs is using relative repository path
        // must be a forward slash, regardless of the local filing system
        return repository + '/' + fileRepository;
    }

    public void removeEntry(File file) throws IOException {
        File parent = file.getParentFile();
        CvsMetadata data = MetadataAttic.getMetadata(parent);
        if (data != null) {
            String [] entries = data.getEntries();
            for (int i = 0; i < entries.length; i++) {
                Entry entry = new Entry(entries[i]);
                if (file.getName().equals(entry.getName())) {
                    List newEntries = new ArrayList(Arrays.asList(entries));
                    newEntries.remove(entries[i]);
                    if (newEntries.size() == 0 || newEntries.size() == 1 && "D".equals(newEntries.get(0))) {
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
        // TODO: override
        return stdHandler.getAllFiles(directory);
    }

    public String getStickyTagForDirectory(File directory) {
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
