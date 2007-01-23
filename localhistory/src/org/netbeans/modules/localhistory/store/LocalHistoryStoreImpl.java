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
 * XXX check for last modified
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
                              fileName.endsWith(LABELS_FILE)); // XXX 
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
                20, -1);                                  
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
            writeHistory(parent, new HistoryEntry[] {new HistoryEntry(ts, from, to, TOUCHED)});                        
        }
        fireChanged(null, file);        
    }
    
    public synchronized void fileChange(File file, long ts) { 
        if(lastModified(file) == ts) {
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
        } 
        fireChanged(null, file);        
    }
    
    public synchronized void fileDelete(File file, long ts) {
        // XXX does this make sense?
//        if(lastModified(file) == ts) {
//            return; 
//        }                
        try {
            fileDeleteImpl(file, null, file.getAbsolutePath(), ts);                            
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }      
        fireChanged(null, file);        
    }

    private void fileDeleteImpl(File file, String from, String to, long ts) throws IOException {        
        StoreDataFile data = readStoreData(file); 
        
        if(data == null) {            
            // XXX should not happen?
            return;
            //assert data != null : "no history entry for file: " + file.getAbsolutePath();
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
            writeHistory(parent, new HistoryEntry[] {new HistoryEntry(ts, from, to, DELETED)});                     
        }
    }
    
    // XXX merge with delete
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

    // XXX merge with create
    public synchronized void fileDeleteFromMove(File from, File to, long ts) {
        // XXX does this make sense?
//        if(lastModified(to) > 0) {
//            return; 
//        }           
        try {
            fileDeleteImpl(from, from.getAbsolutePath(), to.getAbsolutePath(), ts);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }        
        fireChanged(null, from);        
    }    

    private long lastModified(File file) {   
        StoreDataFile data = readStoreData(file); 
        return data != null ? data.getLastModified() : -1;
    }    
    
    public synchronized StoreEntry[] getFiles(File file) {
        if(file.isFile()) {
            File storeFolder = getStoreFolder(file);
            File[] storeFiles = storeFolder.listFiles(fileEntriesFilter);
            if(storeFiles != null) {
                List<StoreEntry> ret = new ArrayList<StoreEntry>(storeFiles.length);                
                if(storeFiles.length > 0) {
                    Map<Long, String> labels = getLabels(file);
                    for (int i = 0; i < storeFiles.length; i++) {
                        long ts = Long.parseLong(storeFiles[i].getName());
                        String label = labels.get(ts);
                        ret.add(new StoreEntry(file, storeFiles[i], ts, label));                
                    }
                    return ret.toArray(new StoreEntry[storeFiles.length]);
                }
                return emptyStoreEntryArray;                        
            } else {
                return emptyStoreEntryArray;            
            }           
        } else {
            return emptyStoreEntryArray;            
        }        
    }
    
    public synchronized StoreEntry getFile(File file, long ts) {
        StoreEntry[] entries = getFiles(file);
        StoreEntry entry = null;
        for(StoreEntry se : entries) {
            if(entry == null) {
                entry = se;
            } else {
                if(se.getTimestamp() <= ts && se.getTimestamp() > entry.getTimestamp()) {
                    entry = se;
                }
            }
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

    public synchronized StoreEntry[] getDeletedFiles(File file) {
        if(file.isFile()) {
            return null;
        }
        
        Map<String, StoreEntry> deleted = new HashMap<String, StoreEntry>();
        List<HistoryEntry> entries = readHistory(file);
        for(HistoryEntry he : entries) {
            // XXX why action? why not status?
            if(he.getAction() == DELETED) {
                String filePath = he.getTo();
                if(!deleted.containsKey(filePath)) {
                    StoreDataFile data = readStoreData(new File(he.getTo()));
                    if(data != null && data.getAction() == DELETED) {
                        File storeFile = data.isFile ? 
                                            getStoreFile(new File(data.getAbsolutePath()), Long.toString(data.getLastModified()), false) :
                                            getStoreFolder(file);
                        deleted.put(filePath, new StoreEntry(new File(data.getAbsolutePath()), storeFile, data.getLastModified(), ""));
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
                    Diagnostics.println("Cleanup Start");
                }
                
                cleanUpImpl(ttl);
                
                if(Diagnostics.ON) {
                    Diagnostics.println("Cleanup End");
                }                
            }
        });
    }
    
    private void cleanUpImpl(long ttl) {        
                        
        // XXX fire events         
        
        long now = System.currentTimeMillis();        
        
        File[] topLevelFiles = storage.listFiles();                
                
        for(File topLevelFile : topLevelFiles) {                        
            File[] secondLevelFiles = topLevelFile.listFiles();
            boolean allEmpty = true;
            for(File secondLevelFile : secondLevelFiles) {       
                                                            
//                try {
                    boolean empty = cleanUpFolder(secondLevelFile, ttl, now);    
                    if(empty) {                        
                        if(secondLevelFile.exists()) {
                            FileUtils.deleteRecursively(secondLevelFile);
                        }
                    } else {
                        allEmpty = false;
                    }
//                } catch (FileNotFoundException e) {
//                    // ignore 
//                    // XXX ignore and delete
//                } catch (IOException e) {   
//                    // XXX why? ignore and delete
//                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
//                }                
            }
            if(allEmpty) {
                FileUtils.deleteRecursively(topLevelFile);
            }        
            
            if( System.currentTimeMillis() - now > 200 ) {
                // it took you to long, get out                
                try {
                    if(Diagnostics.ON) {
                        Diagnostics.println("Cleanup Sleep 200");
                    }                
                    java.lang.Thread.sleep(200);
                } catch (InterruptedException ex) { }                    
            }                                
        }                     
    }
    
    private synchronized boolean cleanUpFolder(File folder, long ttl, long now) {
        File dataFile = new File(folder, DATA_FILE);
        
        if(!dataFile.exists()) {
            // it's a folder
            return cleanUpStoredFolder(folder, ttl, now);
        }
        
        StoreDataFile data = StoreDataFile.read(dataFile); 
        if(data == null) {
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
            dataFile.delete();            
            return true;
        }
        
        File[] files = store.listFiles(fileEntriesFilter);            
        boolean skipped = false;
        
        for(File f : files) {                
            long ts = Long.parseLong(f.getName());
            if(ts < now - ttl) {
                // XXX remove labels
                f.delete(); 
            } else {
                skipped = true;
            }
        }    
        
        if(!skipped) {
            // if all entries are gone then remove also the metadata             
            File labelsFile = new File(store, LABELS_FILE);
            labelsFile.delete();            
            writeStoreData(dataFile, null);                                  
        }                        
        return !skipped;
    }
    
    private boolean cleanUpStoredFolder(File store, long ttl, long now) {
        File historyFile = new File(store, HISTORY_FILE);

        if(!historyFile.exists()) {
            return true;
        }
        
        if(historyFile.lastModified() < now - ttl) {
            historyFile.delete();            
            return true;
        }

        List<HistoryEntry> entries = readHistory(historyFile);
        historyFile.delete();            
        List<HistoryEntry> newEntries = new ArrayList<HistoryEntry>();            
        for(HistoryEntry entry : entries) {
            if(entry.getTimestamp() > now - ttl) {
                newEntries.add(entry);
            }                                
        }
        if(newEntries.size() > 0) {
            writeHistory(historyFile, newEntries.toArray(new HistoryEntry[newEntries.size()]));                        
        }
        return newEntries.size() < 1;
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
        // XXX no data file created if original file was already present when LH began to work!!!
        writeStoreData(file, data);
    }    
       
    private void initStorage() {
        String userDir = System.getProperty("netbeans.user");                                       // NOI18N                
        storage = new File(new File (userDir , "cache"), "localstorage");                           // NOI18N        
        if(!storage.exists()) {
            storage.mkdirs();
        }        
        // XXX what if storage == null
    }    

    private File getStoreFolder(File file) {                        
        String filePath = file.getAbsolutePath();
        File storeFolder = getStoreFolderName(filePath);
        int i = 0;
        while(storeFolder.exists()) {
            StoreDataFile data = readStoreData(storeFolder);
            if(data == null || !data.getAbsolutePath().equals(filePath)) {
                break;                
            }
            storeFolder = getStoreFolderName(filePath + "." + i);
        }            
        return storeFolder;                     
    }
    
    private File getStoreFolderName(String filePath) {        
        int fileHash = filePath.hashCode();                                        
        String storeFileName = getMD5(filePath); // XXX colisions could cause trouble                                
        String storeIndex = storage.getAbsolutePath() + "/" + Integer.toString(fileHash % 173 + 172);   // NOI18N                                  
        // storeFileName = file.getAbsolutePath().replaceAll("/", "<=>");   
        // String storeIndex = storage.getAbsolutePath() + "/" + Integer.toString(1);         
        return new File(storeIndex + "/" + storeFileName);
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
        String ret = "";                                                        // NOI18N
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i] & 0x000000FF);
            if(hex.length()==1) {
                hex = "0" + hex;                                                // NOI18N
            }
            ret += hex;                                                         // NOI18N
        }       
        return ret;
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
    
    private Map<Long, String> getLabels(File file) {
        File labelsFile = getLabelsFile(file);

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
            
    // XXX do we need this?
    private void writeHistory(File file, HistoryEntry[] entries) { // XXX int action
        File history = getHistoryFile(file);
        DataOutputStream dos = null;
        try {
            dos = getOutputStream(history, true);            
            for(HistoryEntry entry : entries) {
                dos.writeLong(entry.getTimestamp());                        
                writeString(dos, entry.getFrom());        
                writeString(dos, entry.getTo());            
                dos.writeInt(entry.getAction());
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

    private List<HistoryEntry> readHistory(File file) {
        File history = getHistoryFile(file);
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
        private int action;        
        HistoryEntry(long ts, String from, String to, int action) {
            this.ts = ts;
            this.from = from;
            this.to = to;
            this.action = action;
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
        int getAction() {
            return action;
        }
    }
    
    private StoreDataFile readStoreData(File file) {
//        if(Diagnostics.ON) {
//            Diagnostics.println("readStoreData:" + file);
//        }    
        return (StoreDataFile) turbo.readEntry(file, DataFilesTurboProvider.ATTR_DATA_FILES);
    }

    private void writeStoreData(File file, StoreDataFile data) {        
//        if(Diagnostics.ON) {
//            Diagnostics.println("writeStoreData:" + file);
//        }            
        turbo.writeEntry(file, DataFilesTurboProvider.ATTR_DATA_FILES, data);        
    }    
    
    private static class StoreDataFile {        
        private final int action;
        private final long lastModified;        
        private final String absolutePath;
        private final boolean isFile;
        
        private StoreDataFile(String absolutePath, int action, long lastModified, boolean isFile) {
            this.action = action;
            this.lastModified = lastModified;
            this.absolutePath = absolutePath;
            this.isFile = isFile;            
        }                 
        
        int getAction() {
            return action;
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
                dos.writeInt(data.getAction());
                dos.writeLong(data.getLastModified());
                dos.writeInt(data.getAbsolutePath().length());
                dos.writeChars(data.getAbsolutePath());          
                dos.flush();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);                
            }
            finally {
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
                                
            File storeFile = getDataFile((File) key);                                
            if(!storeFile.exists()) {
                return null;
            }            
            return StoreDataFile.read(storeFile);
        }

        public synchronized boolean writeEntry(Object key, String name, Object value) {
            assert key instanceof File;
            assert value == null || value instanceof StoreDataFile;
            assert name != null;
                    
            File storeFile = getDataFile((File) key);
                        
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
    

