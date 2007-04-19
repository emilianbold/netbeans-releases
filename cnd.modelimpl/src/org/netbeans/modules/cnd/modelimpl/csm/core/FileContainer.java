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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
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
    
    public void putFile(File file, FileImpl impl, APTPreprocState.State state) {
        String path = getFileKey(file, true);
        MyFile old;
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmFile> uid = RepositoryUtils.put(impl);
            old = myFiles.put(path, new MyFile(uid, state));
        } else {
            old = myFiles.put(path, new MyFile(impl, state));
        }
        if (old != null){
            System.err.println("Replace file "+file.getAbsoluteFile());
        }
    }
    
    public void removeFile(File file) {
        String path = getFileKey(file, false);
        MyFile f = myFiles.remove(path);
        if (f != null && TraceFlags.USE_REPOSITORY) {
            if (f.fileNew != null){
                // clean repository
                if (false) RepositoryUtils.remove(f.fileNew);
            }
        }
    }
    
    public FileImpl getFile(File file) {
        String path = getFileKey(file, false);
        MyFile f = myFiles.get(path);
        if (f == null){
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
    
    public void putPreprocStateState(File file, APTPreprocState.State state) {
        String path = getFileKey(file, true);
        MyFile f = myFiles.get(path);
        if (f == null){
            return;
        }
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
        if (TRACE_PP_STATE_OUT) {
            System.err.println("\nPut state for file" + path + "\n");
            System.err.println(state);
        }
    }
    
    public APTPreprocState.State getPreprocStateState(File file) {
        String path = getFileKey(file, false);
        MyFile f = myFiles.get(path);
        if (f == null){
            return null;
        }
        return f.state;
    }

    public Object getLock(File file) {
        String path = getFileKey(file, false);
        MyFile f = myFiles.get(path);
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
        HashMap<String,APTPreprocState.State> handlers = new HashMap<String,APTPreprocState.State>();
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
        HashMap<String,APTPreprocState.State> handlers = new HashMap<String,APTPreprocState.State>();
        aFactory.readStringToUIDMap(files, aStream, FilePathCache.getManager());
        PersistentUtils.readStringToStateMap(handlers, aStream);
        synchronized( myFiles ) {
            myFiles.clear();
            for(Map.Entry<String, CsmUID<CsmFile>> entry : files.entrySet()){
                APTPreprocState.State state = handlers.get(entry.getValue());
                MyFile file = new MyFile(entry.getValue(),state);
                myFiles.put(entry.getKey(),file);
            }
        }
    }
    
    public static String getFileKey(File file, boolean sharedText) {
        String key = null;
        if (false && TraceFlags.USE_CANONICAL_PATH) {
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
    private Object lock = new Object();
    
    private static class MyFile{
        private final CsmUID<CsmFile> fileNew;
        private final FileImpl fileOld;
        private APTPreprocState.State state;
        
        private MyFile(CsmUID<CsmFile> fileNew, APTPreprocState.State state) {
            this.fileNew = fileNew;
            fileOld = null;
            this.state = state;
        }
        
        private MyFile(FileImpl fileOld, APTPreprocState.State state){
            this.fileOld = fileOld;
            fileNew = null;
            this.state = state;
        }
    }
}
