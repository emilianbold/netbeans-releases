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
package org.netbeans.modules.localhistory.store;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;  
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;  
import java.util.Map.Entry;
import java.util.Set;       
import org.netbeans.modules.localhistory.Diagnostics;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.turbo.CustomProviders;     
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.TurboProvider;
import org.netbeans.modules.turbo.TurboProvider;
import org.netbeans.modules.turbo.TurboProvider;
import org.netbeans.modules.turbo.TurboProvider.MemoryCache;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
class LocalHistoryStoreImpl implements LocalHistoryStore {           

    private static final int DELETED = 0;
    private static final int TOUCHED = 1;
        
    private static final String DATA_FILE     = "data";                  // NOI18N  
    private static final String HISTORY_FILE  = "history";               // NOI18N       
    private static final String LABELS_FILE   = "labels";                // NOI18N  
    
    private File storage;
    private Turbo turbo;
    private DataFilesTurboProvider cacheProvider;
    private final PropertyChangeSupport propertyChangeSupport;
    
    private static List<HistoryEntry> emptyHistory = new ArrayList<HistoryEntry>(0);
    private static Map<Long, String> emptyLabels = new HashMap<Long, String>();
    private static StoreEntry[] emptyStoreEntryArray = new StoreEntry[0];            
    
    private static FilenameFilter fileEntriesFilter = 
            new FilenameFilter() {
                public boolean accept(File dir, String fileName) {
                    return !( fileName.endsWith(DATA_FILE)    || 
                              fileName.endsWith(HISTORY_FILE) || 
                              fileName.endsWith(LABELS_FILE)); 
                }
            };
    
    LocalHistoryStoreImpl() {        
        initStorage();
        
        propertyChangeSupport = new PropertyChangeSupport(this);
        
        cacheProvider = new DataFilesTurboProvider();                
        turbo = Turbo.createCustom(
                new CustomProviders() {
                    private final Set providers = Collections.singleton(cacheProvider);
                    public Iterator providers() {
                        return providers.iterator();
                    }
                }, 
                20, -1);        // XXX why -1, isn't in such a case a weakhashmap enough?                          
    }    

    public synchronized void fileCreate(File file, long ts) {
        try {
            fileCreateImpl(file, ts, null, file.getAbsolutePath());        
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }
    }

    private void fileCreateImpl(File file, long ts, String from, String to) throws IOException {     
       if(lastModified(file) > 0) {
            return; 
        }        
        String tsString = Long.toString(ts);
        File storeFile = null;
        if(file.isFile()) {
            storeFile = getStoreFile(file, tsString, true);  // XXX let's call it a lack of inspiration            
            FileUtils.copy(file, StoreEntry.createStoreFileOutputSteam(storeFile));                                 
            
            if(Diagnostics.ON) {
                Diagnostics.logCreate(file, storeFile, ts, from, to);
            }
            
        } 
        touch(file, new StoreDataFile(file.getAbsolutePath(), TOUCHED, ts, file.isFile()));
        File parent = file.getParentFile();
        if(parent != null) {
            // XXX consider also touching the parent - yes (collisions, ...)
            writeHistoryForFile(parent, new HistoryEntry[] {new HistoryEntry(ts, from, to, TOUCHED)});                        
        }
        fireChanged(null, file);        
    }
    
    public synchronized void fileChange(File file, long ts) {
        long lastModified = lastModified(file);
        if(lastModified == ts) {
            return; 
        }        
        if(file.isFile()) { 
            try {
                File storeFile = getStoreFile(file, Long.toString(ts), true);
                FileUtils.copy(file, StoreEntry.createStoreFileOutputSteam(storeFile));                    
                
                if(Diagnostics.ON) {
                    Diagnostics.logChange(file, storeFile, ts);
                }                
                
                touch(file, new StoreDataFile(file.getAbsolutePath(), TOUCHED, ts, file.isFile()));
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
            }            
        } else {
            try {
                touch(file, new StoreDataFile(file.getAbsolutePath(), TOUCHED, ts, file.isFile()));                  
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
            }             
        }
        fireChanged(null, file);        
    }
    
    public synchronized void fileDelete(File file, long ts) {
        try {
            fileDeleteImpl(file, null, file.getAbsolutePath(), ts);                            
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }      
        fireChanged(null, file);
    }

    private void fileDeleteImpl(File file, String from, String to, long ts) throws IOException {        
        StoreDataFile data = readStoreData(file, true);         
        // XXX what if already deleted?
        
        if(data == null) {                        
            if(Diagnostics.ON) {                
                Diagnostics.println("deleting without data for file : " + file);
            }         
            return;            
        }
        // copy from previous entry
        long lastModified = data.getLastModified();
        boolean isFile = data.isFile();
        
        if(Diagnostics.ON) {
            File storeFile = getDataFile(file);
            Diagnostics.logDelete(file, storeFile, ts);
        } 
        
        touch(file, new StoreDataFile(file.getAbsolutePath(), DELETED, lastModified, isFile));        
        File parent = file.getParentFile();
        if(parent != null) {
            // XXX consider also touching the parent
            writeHistoryForFile(parent, new HistoryEntry[] {new HistoryEntry(ts, from, to, DELETED)});                     
        }
    }
    
    public synchronized void fileCreateFromMove(File from, File to, long ts) {
        if(lastModified(to) > 0) {
            return; 
        }        
        try {
            fileCreateImpl(to, ts, from.getAbsolutePath(), to.getAbsolutePath());
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }
        fireChanged(null, to);        
    }

    public synchronized void fileDeleteFromMove(File from, File to, long ts) {
        try {
            fileDeleteImpl(from, from.getAbsolutePath(), to.getAbsolutePath(), ts);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }        
        fireChanged(null, from);        
    }    

    private long lastModified(File file) {   
        StoreDataFile data = readStoreData(file, true); 
        return data != null && data.getStatus() != DELETED ? data.getLastModified() : -1;
    }    
    
    public synchronized StoreEntry[] getStoreEntries(File file) {
        // XXX file.isFile() won't work for deleted files
        return getStoreEntriesImpl(file);
    }
    
    private StoreEntry[] getStoreEntriesImpl(File file) {
        File storeFolder = getStoreFolder(file);
        File[] storeFiles = storeFolder.listFiles(fileEntriesFilter);
        if(storeFiles != null && storeFiles.length > 0) {
            List<StoreEntry> ret = new ArrayList<StoreEntry>(storeFiles.length);                
            if(storeFiles.length > 0) {
                Map<Long, String> labels = getLabels(getLabelsFile(file));
                for (int i = 0; i < storeFiles.length; i++) {
                    long ts = Long.parseLong(storeFiles[i].getName());
                    String label = labels.get(ts);
                    ret.add(StoreEntry.createStoreEntry(file, storeFiles[i], ts, label));                
                }
                return ret.toArray(new StoreEntry[storeFiles.length]);
            }
            return emptyStoreEntryArray;                        
        } else {
            return emptyStoreEntryArray;            
        }           
    }

    public StoreEntry[] getFolderState(File root, File[] files, long ts) {

        // check if the root wasn't deleted to that time        
        File parentFile = root.getParentFile();
        if(parentFile != null) {
            List<HistoryEntry> parentHistory = readHistoryForFile(parentFile);                
            if(wasDeleted(root, parentHistory, ts)) {                                    
                return emptyStoreEntryArray;
            }        
        }
        
        List<HistoryEntry> history = readHistoryForFile(root);                
        
        // StoreEntries we will return
        List<StoreEntry> ret = new ArrayList<StoreEntry>();                        
        
        Map<File, HistoryEntry> beforeRevert = new HashMap<File, HistoryEntry>();
        Map<File, HistoryEntry> afterRevert = new HashMap<File, HistoryEntry>();
        
        for(HistoryEntry he : history) {
            File file = new File(he.getTo());
            if(he.getTimestamp() < ts) {
                // this is the LAST thing which happened 
                // to a file before the given time
                beforeRevert.put(file, he);                                    
            } else {
                // this is the FIRST thing which happened 
                // to a file before the given time
                if(!afterRevert.containsKey(file)) {
                    afterRevert.put(file, he);                                                       
                }                
            }            
        }  

        for(File file : files) {
            HistoryEntry before = beforeRevert.get(file);            
            HistoryEntry after = afterRevert.get(file);            
            
            // lets see what remains when we are throught all existing files
            beforeRevert.remove(file);
            afterRevert.remove(file);
            
            if(before != null && before.getStatus() == DELETED) {
                // the file was deleted to the given time -> delete it!
                ret.add(StoreEntry.createDeletedStoreEntry(file, ts)); 
                continue;
            }
            
            StoreDataFile data = readStoreData(file, true);
            if(data == null) {
                // XXX ???
                continue;
            }            
            if(data.isFile()) {
                StoreEntry se = getStoreEntry(file, ts);    
                if(se != null) {
                    ret.add(se);
                } else {
                    if(after != null && after.getStatus() == TOUCHED) {                        
                        ret.add(StoreEntry.createDeletedStoreEntry(file, ts));
                    } else {
                        // XXX is this possible?
                    }
                    // the file still exists and there is no entry -> uptodate? 
                }                                                
            } else {
                if(after != null && after.getStatus() == TOUCHED) {                        
                    ret.add(StoreEntry.createDeletedStoreEntry(file, ts));
                } else {
                    // XXX is this possible?
                }                
                // the folder still exists and it wasn't deleted, so do nothing                                                
            }                                    
        } 
        
        
        for(Entry<File, HistoryEntry> entry : beforeRevert.entrySet()) {
            
            File file = entry.getKey();
            
            // lets see what remains
            afterRevert.remove(file);
            
            // the file doesn't exist now, but
            // there was something done to it before the given time
            if(entry.getValue().getStatus() == DELETED) {
                // this is exactly what we have - forget it!
                continue;
            }
                        
            StoreDataFile data = readStoreData(file, true);
            if(data != null) {
                if(data.isFile()) {
                    StoreEntry se = getStoreEntry(file, ts);    
                    if(se != null) {
                        ret.add(se);
                    } else {
                        // XXX what now? this should be covered
                    }                                                
                } else {
                    // it must have existed
                    File storeFile = getStoreFolder(root); // XXX why returning the root
                    StoreEntry folderEntry = StoreEntry.createStoreEntry(new File(data.getAbsolutePath()), storeFile, data.getLastModified(), "");
                    ret.add(folderEntry);
                }                            
            } else {
                // XXX how to cover this?
            }            
        }
        
        // XXX do we even need this                 
//        for(Entry<File, HistoryEntry> entry : afterRevert.entrySet()) {        
//            
//        }
        return ret.toArray(new StoreEntry[ret.size()]);
               
    }
    
    private boolean wasDeleted(File file, List<HistoryEntry> history , long ts) {        
        String path = file.getAbsolutePath();        
        boolean deleted = false;
        
        for(int i = 0; i < history.size(); i++) {
            HistoryEntry he = history.get(i);
            if(he.getTo().equals(path)) {
                if(he.getStatus() == DELETED) {
                    deleted = true;
                } else {
                    deleted = false;
                }                                        
            }
            if(he.ts >= ts) {
                break;
            }
        }        
        return deleted;
    }
    
    public synchronized StoreEntry getStoreEntry(File file, long ts) {
        return getStoreEntryImpl(file, ts, readStoreData(file, true));
    }

    private StoreEntry getStoreEntryImpl(File file, long ts, StoreDataFile data) {
        // XXX what if file deleted?
        StoreEntry entry = null;     
                
        if(data == null) {
            // not in storage?
            return null;
        }
        if(data.isFile()) {
            StoreEntry[] entries = getStoreEntriesImpl(file);                    
            for(StoreEntry se : entries) {
                if(se.getTimestamp() <= ts) {
                    if( entry == null || se.getTimestamp() > entry.getTimestamp() ) {                        
                        entry = se;                        
                    }
                }
            } 
        } else {
            // XXX dont implement this for folders as long there is no need
        }
        
        return entry;
    }
    
    public synchronized void deleteEntry(File file, long ts) {
        File storeFile = getStoreFile(file, Long.toString(ts), false);
        if(storeFile.exists()) { 
            storeFile.delete();    
        }                
        // XXX delete from parent history    
        fireChanged(file, null);
    }

    public synchronized StoreEntry[] getDeletedFiles(File root) {
        if(root.isFile()) {
            return null;
        }
        
        Map<String, StoreEntry> deleted = new HashMap<String, StoreEntry>();
        List<HistoryEntry> entries = readHistoryForFile(root);
                
        for(HistoryEntry he : entries) {
            if(he.getStatus() == DELETED) {
                String filePath = he.getTo();
                if(!deleted.containsKey(filePath)) {
                    StoreDataFile data = readStoreData(new File(he.getTo()), true);
                    if(data != null && data.getStatus() == DELETED) {
                        File storeFile = data.isFile ? 
                                            getStoreFile(new File(data.getAbsolutePath()), Long.toString(data.getLastModified()), false) :
                                            getStoreFolder(root); // XXX why returning the root???
                        deleted.put(filePath, StoreEntry.createStoreEntry(new File(data.getAbsolutePath()), storeFile, data.getLastModified(), ""));
                    }
                }
            }             
        }        
        return deleted.values().toArray(new StoreEntry[deleted.size()]);
    }

    public synchronized void setLabel(File file, long ts, String label) {
        File labelsFile = getLabelsFile(file);
        File parent = labelsFile.getParentFile();
        if(!parent.exists()) {
            parent.mkdirs();
        }
        
        File labelsNew = null; 
        DataInputStream dis = null;        
        DataOutputStream oos = null; 
        boolean foundLabel = false;
        try {
            if(!labelsFile.exists()) {
                oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(labelsFile)));
                oos.writeLong(ts);
                writeString(oos, label);
            } else {
                labelsNew = new File(labelsFile.getParentFile(), labelsFile.getName() + ".new");            // NOI18N
                oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(labelsNew)));

                dis = getInputStream(labelsFile);
                long readTs = -1;            
                try {
                    while(true) {   
                        readTs = dis.readLong();
                        if(readTs == ts) {
                            oos.writeLong(readTs);
                            writeString(oos, label);
                            int len = dis.readInt();
                            skip(dis, len * 2);
                            copyStreams(oos, dis);
                            break;
                        } else {
                            oos.writeLong(readTs);
                            String l = readString(dis);
                            writeString(oos, l);
                        }
                    }    
                } catch (EOFException e) {
                    if(!foundLabel) {
                        oos.writeLong(ts);
                        writeString(oos, label);
                    }                
                }
            }            
            oos.flush();
        } catch (EOFException e) {
            // ignore
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }                
            }
            if (oos != null) {
                try { oos.close(); } catch (IOException e) { }                
            }            
        }            
        
        try {
            if(labelsNew != null ) {
                FileUtils.renameFile(labelsNew, labelsFile);   
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);            
        }
    
        return;        
    }    
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void cleanUp(final long ttl) {        
        // XXX run only once a day - use the top folder metadata for version and cleanup flag
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {              
                if(Diagnostics.ON) {
                    Diagnostics.println("Cleanup Start");                       // NOI18N                                  
                }
                
                cleanUpImpl(ttl);
                
                if(Diagnostics.ON) {
                    Diagnostics.println("Cleanup End");                         // NOI18N                                      
                }                
            }
        });
    }
    
    private void cleanUpImpl(long ttl) {        
                        
        // XXX fire events         
        
        long now = System.currentTimeMillis();        
        
        File[] topLevelFiles = storage.listFiles();                
        if(topLevelFiles == null || topLevelFiles.length == 0) {
            return;
        }
        
        for(File topLevelFile : topLevelFiles) {                        
            File[] secondLevelFiles = topLevelFile.listFiles();
            if(secondLevelFiles == null || secondLevelFiles.length == 0) {
                FileUtils.deleteRecursively(topLevelFile);
                continue;
            }
            
            boolean allEmpty = true;
            for(File secondLevelFile : secondLevelFiles) {                                                                   
                boolean empty = cleanUpFolder(secondLevelFile, ttl, now);    
                if(empty) {                        
                    if(secondLevelFile.exists()) {
                        FileUtils.deleteRecursively(secondLevelFile);
                    }
                } else {
                    allEmpty = false;
                }             
            }
            if(allEmpty) {
                FileUtils.deleteRecursively(topLevelFile);
            }                    
        }                     
    }
    
    private synchronized boolean cleanUpFolder(File folder, long ttl, long now) {
        File dataFile = new File(folder, DATA_FILE);
        
        if(!dataFile.exists()) {
            // it's a folder
            return cleanUpStoredFolder(folder, ttl, now);
        }
        
        StoreDataFile data = readStoreData(dataFile, false); 
        if(data.getAbsolutePath() == null) {
            // what's this?
            return true;
        }
        if(data.isFile()) {
           return cleanUpStoredFile(folder, ttl, now);
        } else {
           return cleanUpStoredFolder(folder, ttl, now);
        }        
    }
    
    private boolean cleanUpStoredFile(File store, long ttl, long now) {
        File dataFile = new File(store, DATA_FILE);
        
        if(!dataFile.exists()) {
            return true;
        }        
        if(dataFile.lastModified() < now - ttl) {
            purgeDataFile(dataFile);                
            return true;
        }
        
        File[] files = store.listFiles(fileEntriesFilter);            
        boolean skipped = false;
        
        File labelsFile = new File(store, LABELS_FILE);        
        Map<Long, String> labels = emptyLabels;
        if(labelsFile.exists()) {
            labels = getLabels(labelsFile);
        }
        for(File f : files) {      
            // XXX check the timestamp when touched
            long ts = Long.parseLong(f.getName());
            if(ts < now - ttl) {
                if(labels.size() > 0) {
                    labels.remove(ts);
                }                                
                f.delete(); 
            } else {
                skipped = true;
            }
        }                    
        if(!skipped) {
            // all entries are gone -> remove also the metadata             
            labelsFile.delete();            
            writeStoreData(dataFile, null, false);                                  
        } else {
            if(labels.size() > 0) {
                writeLabels(labelsFile, labels);
            } 
        }                       
        return !skipped;
    }
    
    private void writeLabels(File labelsFile, Map<Long, String> labels) {        
        File parent = labelsFile.getParentFile();
        if(!parent.exists()) {
            parent.mkdirs();
        }
        DataInputStream dis = null;
        DataOutputStream oos = null;
        try {
            for(Entry<Long, String> label : labels.entrySet()) {
                oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(labelsFile)));
                oos.writeLong(label.getKey());
                writeString(oos, label.getValue());                
            }            
            oos.flush();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }                
            }
            if (oos != null) {
                try { oos.close(); } catch (IOException e) { }                
            }            
        }            
    }
    
    private boolean cleanUpStoredFolder(File store, long ttl, long now) {
        File historyFile = new File(store, HISTORY_FILE);
        File dataFile = new File(store, DATA_FILE);
        
        boolean dataObsolete = !dataFile.exists() || dataFile.lastModified() < now - ttl;
        boolean historyObsolete = !historyFile.exists() || historyFile.lastModified() < now - ttl;
                               
        if(!historyObsolete) {
            List<HistoryEntry> entries = readHistory(historyFile);
            historyFile.delete();            
            List<HistoryEntry> newEntries = new ArrayList<HistoryEntry>();            
            for(HistoryEntry entry : entries) {
                // XXX check the timestamp when touched - and you also should to write it with the historywhen 
                if(entry.getTimestamp() > now - ttl) {
                    newEntries.add(entry);
                }                                
            }
            if(newEntries.size() > 0) {
                writeHistory(historyFile, newEntries.toArray(new HistoryEntry[newEntries.size()]));                        
            } else {
                historyObsolete = true;                
            }                        
        }         
        if(dataObsolete) {            
            purgeDataFile(dataFile);
        }
        if(historyObsolete) {
            historyFile.delete(); 
        }
        
        return dataObsolete && historyObsolete; 
    }
    
    private void purgeDataFile(File dataFile) {        
        if(dataFile.exists()) {
            writeStoreData(dataFile, null, false);                
        }                
    }
    
    private void fireChanged(File oldValue, File newValue) {
        propertyChangeSupport.firePropertyChange(
            new PropertyChangeEvent(
                    this, 
                    LocalHistoryStore.PROPERTY_CHANGED, 
                    oldValue, 
                    newValue));
    }        
    
    private void touch(File file, StoreDataFile data) throws IOException {      
        writeStoreData(file, data, true);
    }    
       
    private void initStorage() {
        String userDir = System.getProperty("netbeans.user");                   // NOI18N                
        storage = new File(new File (userDir , "var"), "filehistory");       // NOI18N                    
        if(!storage.exists()) {
            storage.mkdirs();
        }        
    }    

    private File getStoreFolder(File file) {                        
        String filePath = file.getAbsolutePath();
        File storeFolder = getStoreFolderName(filePath);
        int i = 0;
        while(storeFolder.exists()) {
            // check for collisions 
            StoreDataFile data = readStoreData(new File(storeFolder, DATA_FILE), false);
            if(data == null || data.getAbsolutePath().equals(filePath)) {
                break;                
            }
            storeFolder = getStoreFolderName(filePath + "." + i++);
        }            
        return storeFolder;                     
    }
    
    private File getStoreFolderName(String filePath) {        
        int fileHash = filePath.hashCode();                                        
        String storeFileName = getMD5(filePath); 
        String storeIndex = storage.getAbsolutePath() + "/" + Integer.toString(fileHash % 173 + 172);   // NOI18N                                  
        return new File(storeIndex + "/" + storeFileName);                                              // NOI18N                                  
    }
    
    private String getMD5(String name) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");                          // NOI18N
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            return null;
        }
        digest.update(name.getBytes());
        byte[] hash = digest.digest();
        StringBuffer ret = new StringBuffer();                                  
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i] & 0x000000FF);
            if(hex.length()==1) {
                hex = "0" + hex;                                                // NOI18N
            }
            ret.append(hex);                                                    
        }       
        return ret.toString();
    }
    
    private File getStoreFile(File file, String name, boolean mkdirs) {
        File storeFolder = getStoreFolder(file);                                    
        if(mkdirs && !storeFolder.exists()) {
            storeFolder.mkdirs();                                                                
        } 
        return new File(storeFolder, name); 
    }

    private File getHistoryFile(File file) {
        File storeFolder = getStoreFolder(file);
        if(!storeFolder.exists()) {
            storeFolder.mkdirs();                                                                
        } 
        return new File(storeFolder, HISTORY_FILE);   
    }    

    private File getDataFile(File file) {       
        File storeFolder = getStoreFolder(file); 
        return new File(storeFolder, DATA_FILE);    
    }  
        
    private File getLabelsFile(File file) {       
        File storeFolder = getStoreFolder(file); 
        return new File(storeFolder, LABELS_FILE);     
    }       
    
    private Map<Long, String> getLabels(File labelsFile) {        

        if(!labelsFile.exists()) {
            return emptyLabels;
        }
        DataInputStream dis = null;        
        Map<Long, String> ret = new HashMap<Long, String>();
        try {
            dis = getInputStream(labelsFile);            
            while(true) {   
                long ts = dis.readLong();
                String label = readString(dis);
                ret.put(ts, label);
            }    
        } catch (EOFException e) {
            return ret;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }
            }
        }    
        return emptyLabels;
    }
                
    private void writeHistoryForFile(File file, HistoryEntry[] entries) { 
        if(Diagnostics.ON) {                
            if(getDataFile(file) == null) {
                Diagnostics.println("writing history for file without data : " + file);    // NOI18N                                  
            }            
        }                 
        File history = getHistoryFile(file);
        writeHistory(history, entries);
    }
    
    private void writeHistory(File history, HistoryEntry[] entries) {                 
        DataOutputStream dos = null;
        try {
            dos = getOutputStream(history, true);            
            for(HistoryEntry entry : entries) {
                dos.writeLong(entry.getTimestamp());                        
                writeString(dos, entry.getFrom());        
                writeString(dos, entry.getTo());            
                dos.writeInt(entry.getStatus());
            }
            dos.flush();            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return;
        }
        finally {
            if (dos != null) {
                try { dos.close(); } catch (IOException e) { }
            }
        }                   
    }       

    private List<HistoryEntry> readHistoryForFile(File file) {
        return readHistory(getHistoryFile(file));
    }
        
    private List<HistoryEntry> readHistory(File history) {        
        if(!history.exists()) {
            return emptyHistory;
        }
        DataInputStream dis = null;
        List<HistoryEntry> entries = new ArrayList<HistoryEntry>();
        try {
            dis = getInputStream(history);
            while(true) {
                long ts = dis.readLong();
                String from = readString(dis);
                String to = readString(dis);
                int action = dis.readInt();
                entries.add(new HistoryEntry(ts, from, to, action));
            }    
        } catch (EOFException e) {
            return entries;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (dis != null) {
                try { dis.close(); } catch (IOException e) { }
            }
        }    
        return emptyHistory;
    }   
         
    private StoreDataFile readStoreData(File file, boolean isOriginalFile) {
        if(isOriginalFile) {
            file = getDataFile(file);
        }
        return (StoreDataFile) turbo.readEntry(file, DataFilesTurboProvider.ATTR_DATA_FILES);
    }

    private void writeStoreData(File file, StoreDataFile data, boolean isOriginalFile) {        
        if(isOriginalFile) {
            file = getDataFile(file);
        }        
        turbo.writeEntry(file, DataFilesTurboProvider.ATTR_DATA_FILES, data);        
    }  
    
    private static void writeString(DataOutputStream dos, String str) throws IOException {
        if(str != null) {
            dos.writeInt(str.length());
            dos.writeChars(str);        
        } else {
            dos.writeInt(0);
        }        
    }
    
    private static String readString(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        if(len == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();                
        while(len-- > 0) {
            char c = dis.readChar();
            sb.append(c);                       
        }        
        return sb.toString();
    }
    
    private static void skip(InputStream is, long len) throws IOException {
        while (len > 0) {
            long n = is.skip(len);
            if (n < 0) throw new EOFException("Missing " + len + " bytes.");                // NOI18N
            len -= n;
        }
    }

    private static void copyStreams(OutputStream out, InputStream in) throws IOException {       
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = in.read(buffer);
            if (n < 0) break; 
            out.write(buffer, 0, n);            
        }        
    }
    
    private static DataOutputStream getOutputStream(File file, boolean append) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, append)));               
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }      
    }
    
    private static DataInputStream getInputStream(File file) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    private class HistoryEntry {                
        private long ts;
        private String from;
        private String to;
        private int status;        
        HistoryEntry(long ts, String from, String to, int action) {
            this.ts = ts;
            this.from = from;
            this.to = to;
            this.status = action;
        }        
        long getTimestamp() {
            return ts;
        }        
        String getFrom() {
            return from;
        }        
        String getTo() {
            return to;
        }        
        int getStatus() {
            return status;
        }
    }  
    
    private static class StoreDataFile {        
        private final int status;
        private final long lastModified;        
        private final String absolutePath;
        private final boolean isFile;
        
        private StoreDataFile(String absolutePath, int action, long lastModified, boolean isFile) {
            this.status = action;
            this.lastModified = lastModified;
            this.absolutePath = absolutePath;
            this.isFile = isFile;            
        }                 
        
        int getStatus() {
            return status;
        }        
        
        long getLastModified() {
            return lastModified;
        }
        
        String getAbsolutePath() {
            return absolutePath;
        }    
        
        boolean isFile() {
            return isFile;
        }
        
        static synchronized StoreDataFile read(File storeFile) {
            DataInputStream dis = null;
            try {
                dis = getInputStream(storeFile);
                boolean isFile = dis.readBoolean();
                int action = dis.readInt();
                long modified  = dis.readLong();                
                String fileName = readString(dis);
                return new StoreDataFile(fileName, action, modified, isFile);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                if (dis != null) {
                    try { dis.close(); } catch (IOException e) { } 
                }
            }    
            return null;
        }
        
        static synchronized void write(File storeFile, StoreDataFile value) {
            DataOutputStream dos = null;
            try {
                dos = getOutputStream(storeFile, false);
                StoreDataFile data = (StoreDataFile) value;
                dos.writeBoolean(data.isFile);                
                dos.writeInt(data.getStatus());
                dos.writeLong(data.getLastModified());
                dos.writeInt(data.getAbsolutePath().length());
                dos.writeChars(data.getAbsolutePath());          
                dos.flush();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);                
            } finally {
                if (dos != null) {
                    try { dos.close(); } catch (IOException e) { }
                }
            }
        }
    }   
            
    private class DataFilesTurboProvider implements TurboProvider {        
                
        static final String ATTR_DATA_FILES = "localhistory.ATTR_DATA_FILES";                 // NOI18N        
        
        public boolean recognizesAttribute(String name) {
            return ATTR_DATA_FILES.equals(name);
        }

        public boolean recognizesEntity(Object key) {
            return key instanceof File;
        }

        public synchronized Object readEntry(Object key, String name, MemoryCache memoryCache) {
            assert key instanceof File;            
            assert name != null;
                                
            File storeFile = (File) key;                                
            if(!storeFile.exists()) {
                return null;
            }            
            return StoreDataFile.read(storeFile);
        }

        public synchronized boolean writeEntry(Object key, String name, Object value) {
            assert key instanceof File;
            assert value == null || value instanceof StoreDataFile;
            assert name != null;
            
            File storeFile = (File) key;   
            if(value == null) {
                if(storeFile.exists()) {
                    storeFile.delete();
                }
                return true;
            }
            
            File parent = storeFile.getParentFile();
            if(!parent.exists()) {
                parent.mkdirs();
            }      
            StoreDataFile.write(storeFile, (StoreDataFile) value);
            return true;
        }                       
    }     
    
}