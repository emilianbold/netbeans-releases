/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Storage for files and states. Class was extracted from ProjectBase.
 * @author Alexander Simon
 */
/*package-local*/ class FileContainer {
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false);
    
    /** Creates a new instance of FileContainer */
    public FileContainer() {
    }
    
    public void putFile(File file, FileImpl impl, APTPreprocHandler.State state) {
        String path = getFileKey(file, true);
        MyFile newEntry;
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmFile> uid = RepositoryUtils.put(impl);
            newEntry = new MyFile(uid, state, path);
        } else {
            newEntry = new MyFile(impl, state, path);
        }
        MyFile old;
        synchronized (myFiles) {
            old = myFiles.put(path, newEntry);
            addAlternativeFileKey(path, newEntry.canonical);
        }
        if (old != null){
            System.err.println("Replace file "+file.getAbsoluteFile());
        }
    }
    
    public void removeFile(File file) {
        String path = getFileKey(file, false);
        MyFile f;
        synchronized (myFiles) {
            f = myFiles.remove(path);
            if (f != null) {
                removeAlternativeFileKey(f.canonical, path);
            }
        }
        if (f != null && TraceFlags.USE_REPOSITORY) {
            if (f.fileNew != null){
                // clean repository
                if (false) RepositoryUtils.remove(f.fileNew);
            }
        }
    }
    
    public FileImpl getFile(File file) {
        MyFile f = getMyFile(file, false);
        if (f == null) {
            return null;
        }
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmFile> fileUID = f.fileNew;
            FileImpl impl = (FileImpl) UIDCsmConverter.UIDtoFile(f.fileNew);
            assert (impl != null) : "no file for UID " + fileUID;
            return impl;
        } else {
            return f.fileOld;
        }
    }
    
    public void putPreprocState(File file, APTPreprocHandler.State state) {
        MyFile f = getMyFile(file, true);
        if (f == null){
            return;
        }
        synchronized (getLock(file)) {
            if (f.state == null){
                f.state = state;
            } else {
                if (f.state.isStateCorrect()) {
                    if (state.isStateCorrect()) {
                        f.state = state;
                    } else {
                        if (TRACE_PP_STATE_OUT) {
                            System.err.println("Do not reset correct state to incorrect "+file.getAbsolutePath());
                        }
                    }
                } else {
                    if (state.isStateCorrect()){
                        f.state = state;
                    } else {
                        if (TRACE_PP_STATE_OUT) {
                            System.err.println("Do not reset incorrect state to incorrect state "+file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        if (TRACE_PP_STATE_OUT) {
            String path = getFileKey(file, false);
            System.err.println("\nPut state for file" + path + "\n");
            System.err.println(state);
        }
    }
    
    public void invalidatePreprocState(File file) {
        MyFile f = getMyFile(file, false);
        if (f == null){
            return;
        }
        synchronized (getLock(file)) {
            if (f.state != null){
                f.state = APTHandlersSupport.createInvalidPreprocState(f.state);
            }
        }
        if (TRACE_PP_STATE_OUT) {
            String path = getFileKey(file, false);
            System.err.println("\nInvalidated state for file" + path + "\n");
        }
    }
    
    public APTPreprocHandler.State getPreprocState(File file) {
        MyFile f = getMyFile(file, false);
        if (f == null){
            return null;
        }
        return f.state;
    }

    public Object getLock(File file) {
        MyFile f = getMyFile(file, false);
        if (f == null){
            return lock;
        }
        return f;
    }
    
    public void clearState(){
        List<MyFile> files;
        synchronized( myFiles ) {
            files = new ArrayList<MyFile>(myFiles.values());
        }
        for (MyFile file : files){
            file.state = null;
        }
    }
    
    public List<FileImpl> getFiles() {
        List<MyFile> files;
        synchronized( myFiles ) {
            files = new ArrayList<MyFile>(myFiles.values());
        }
        List<FileImpl> res = new ArrayList<FileImpl>(files.size());
        for(MyFile f : files){
            FileImpl file = null;
            if (TraceFlags.USE_REPOSITORY) {
                file = (FileImpl) UIDCsmConverter.UIDtoFile(f.fileNew);
                assert (file != null);
            } else {
                file = f.fileOld;
            }
            res.add(file);
        }
        return res;
    }
    
    public void clear(){
        myFiles.clear();
    }
    
    public int getSize(){
        return myFiles.size();
    }
    
    public void write(DataOutput aStream) throws IOException {
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        HashMap<String,CsmUID<CsmFile>> files = new HashMap<String,CsmUID<CsmFile>>();
        HashMap<String,APTPreprocHandler.State> handlers = new HashMap<String,APTPreprocHandler.State>();
        System.err.println("NEED TO UPDATE SERIALIZATION");
        synchronized( myFiles ) {
            for(Map.Entry<String, MyFile> entry : myFiles.entrySet()){
                files.put(entry.getKey(),entry.getValue().fileNew);
                if (entry.getValue().state != null){
                    handlers.put(entry.getKey(),entry.getValue().state);
                }
            }
        }
        aFactory.writeStringToUIDMap(files, aStream, true);
        PersistentUtils.writeStringToStateMap(handlers, aStream);
    }
    
    public void read(DataInput aStream) throws IOException {
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        HashMap<String,CsmUID<CsmFile>> files = new HashMap<String,CsmUID<CsmFile>>();
        HashMap<String,APTPreprocHandler.State> handlers = new HashMap<String,APTPreprocHandler.State>();
        aFactory.readStringToUIDMap(files, aStream, FilePathCache.getManager());
        PersistentUtils.readStringToStateMap(handlers, aStream);
        myFiles.clear();
        System.err.println("NEED TO UPDATE DESERIALIZATION");
        for(Map.Entry<String, CsmUID<CsmFile>> entry : files.entrySet()){
            APTPreprocHandler.State state = handlers.get(entry.getValue());
            MyFile file = new MyFile(entry.getValue(),state, entry.getKey());
            myFiles.put(entry.getKey(),file);
        }
    }
    
    public static String getFileKey(File file, boolean sharedText) {
        String key = null;
        if (TraceFlags.USE_CANONICAL_PATH) {
            try {
                key = file.getCanonicalPath();
            } catch (IOException ex) {
                key = file.getAbsolutePath();
            }
        } else {
            key = file.getAbsolutePath();
        }
        return sharedText ? FilePathCache.getString(key) : key;
    }
    
    private Map<String, MyFile> myFiles = Collections.synchronizedMap(new HashMap<String, MyFile>());
    
    private Map<String, Object/*String or String[]*/> canonicFiles = Collections.synchronizedMap(new HashMap<String, Object/*String or String[]*/>());

    private String getAlternativeFileKey(String primaryKey) {
        Object out = canonicFiles.get(primaryKey);
        if (out instanceof String) {
            return (String)out;
        } else if (out != null) {
            assert ((String[])out).length >= 2;
            return ((String[])out)[0];
        }
        return null;
    }
    
    private MyFile getMyFile(File file, boolean sharedText) {
        String path = getFileKey(file, sharedText);
        MyFile f = myFiles.get(path);
        if (f == null) {
            // check alternative expecting that 'path' is canonical path
            String path2 = getAlternativeFileKey(path);
            f = path2 == null ? null : myFiles.get(path2);
            if (f != null) {
                if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                    System.err.println("alternative for " + path + " is " + path2);
                }
            }
        }
        return f;
    }
    
    private void addAlternativeFileKey(String primaryKey, String canonicKey) {
        Object out = canonicFiles.get(canonicKey);
        Object newVal;
        if (out == null) {
            newVal = primaryKey;
        } else {
            if (out instanceof String) {
                if (out.equals(primaryKey)) {
                    return;
                }
                newVal = new String[] {(String)out, primaryKey};
            } else {
                String[] oldAr = (String[])out;
                for(String what:oldAr){
                    if (what.equals(primaryKey)){
                        return;
                    }
                }
                String[] newAr = new String[oldAr.length + 1];
                System.arraycopy(oldAr, 0, newAr, 0, oldAr.length);
                newAr[oldAr.length] = primaryKey;
                newVal = newAr;
            }
        }
        canonicFiles.put(canonicKey, newVal);
        if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
            if (newVal instanceof String[]) {
                System.err.println("entry for " + canonicKey + " while adding " + primaryKey + " is " + Arrays.asList((String[])newVal).toString());
            } else {
                System.err.println("entry for " + canonicKey + " while adding " + primaryKey + " is " + newVal);
            }
        }                
    }
    
    private void removeAlternativeFileKey(String canonicKey, String primaryKey) {
        Object out = canonicFiles.get(canonicKey);
        assert out != null : "no entry for " + canonicKey + " of " + primaryKey;
        Object newVal;
        if (out instanceof String) {
            newVal = null;
        } else {
            String[] oldAr = (String[])out;
            assert oldAr.length >= 2;
            if (oldAr.length == 2) {
                newVal = oldAr[0].equals(primaryKey) ? oldAr[1] : oldAr[0];
            } else {
                String[] newAr = new String[oldAr.length - 1];
                int k = 0;
                for(String cur : oldAr){
                    if (!cur.equals(primaryKey)){
                        newAr[k++]=cur;
                    }
                }
                newVal = newAr;
            }
        }
        if (newVal == null) {
            canonicFiles.remove(primaryKey);
            if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                System.err.println("removed entry for " + canonicKey + " while removing " + primaryKey);
            }
        } else {
            canonicFiles.put(canonicKey, newVal);
            if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                System.err.println("change entry for " + canonicKey + " while removing " + primaryKey + " to " + newVal);
            }
        }
    }
    
    private final Object lock = new Object();
    
    private static class MyFile {
        private final CsmUID<CsmFile> fileNew;
        private final FileImpl fileOld;
        private final String canonical;
        private APTPreprocHandler.State state;
        
        private MyFile(CsmUID<CsmFile> fileNew, APTPreprocHandler.State state, String fileKey) {
            this.fileNew = fileNew;
            fileOld = null;
            this.state = state;
            this.canonical = getCanonicalKey(fileKey);
        }
        
        private MyFile(FileImpl fileOld, APTPreprocHandler.State state, String fileKey){
            this.fileOld = fileOld;
            fileNew = null;
            this.state = state;
            this.canonical = getCanonicalKey(fileKey);
        }
    }
    
    private static final String getCanonicalKey(String fileKey) {
        try {
            return new File(fileKey).getCanonicalPath();
        } catch (IOException e) {
            // skip exception
            return fileKey;
        }
    }
}
