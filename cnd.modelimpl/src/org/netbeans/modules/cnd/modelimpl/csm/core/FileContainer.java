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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.utils.APTStringManager;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.FileContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Storage for files and states. Class was extracted from ProjectBase.
 * @author Alexander Simon
 */
/*package-local*/ class FileContainer extends ProjectComponent implements Persistent, SelfPersistent {
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false);
    private final Object lock = new Object();
    private Map<String, MyFile> myFiles = new ConcurrentHashMap<String, MyFile>();
    private Map<String, Object/*String or String[]*/> canonicFiles = new ConcurrentHashMap<String, Object/*String or String[]*/>();
    
    /** Creates a new instance of FileContainer */
    public FileContainer(ProjectBase project) {
	super(new FileContainerKey(project.getUniqueName()));
	put();
    }
    
    public FileContainer (DataInput input) throws IOException {
	super(input);
        readStringToMyFileMap(input, myFiles);
        readStringToStringsArrMap(input, canonicFiles);
	//trace(canonicFiles, "Read in ctor:");
    }
    
    private void trace(Map<String, Object/*String or String[]*/> map, String title) {
	System.err.printf("%s\n", title);
	for( Map.Entry<String, Object> entry : map.entrySet() ) {
	    System.err.printf("%s ->\n%s\n\n", entry.getKey(), entry.getValue());
	}
    }
    
    public void putFile(File file, FileImpl impl, APTPreprocHandler.State state) {
        String path = getFileKey(file, true);
        MyFile newEntry;
        if (TraceFlags.USE_REPOSITORY) {
            @SuppressWarnings("unchecked")
            CsmUID<CsmFile> uid = RepositoryUtils.put(impl);
            newEntry = new MyFile(uid, state, path);
        } else {
            newEntry = new MyFile(impl, state, path);
        }
        MyFile old;

        old = myFiles.put(path, newEntry);
        addAlternativeFileKey(path, newEntry.canonical);

        if (old != null){
            System.err.println("Replace file "+file.getAbsoluteFile());
        }
	put();
    }
    
    public void removeFile(File file) {
        String path = getFileKey(file, false);
        MyFile f;

        f = myFiles.remove(path);
        if (f != null) {
            removeAlternativeFileKey(f.canonical, path);
        }
        
        if (f != null && TraceFlags.USE_REPOSITORY) {
            if (f.fileNew != null){
                // clean repository
                if (false) RepositoryUtils.remove(f.fileNew);
            }
        }
	put();
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
        files = new ArrayList<MyFile>(myFiles.values());
        for (MyFile file : files){
            file.state = null;
        }
	put();
    }
    
    public List<CsmFile> getFiles() {
	List<CsmFile> res = new ArrayList<CsmFile>(myFiles.values().size());
	getFiles2(res);
	return res;
    }
    
    public List<FileImpl> getFileImpls() {
	List<FileImpl> res = new ArrayList<FileImpl>(myFiles.values().size());
	getFiles2(res);
	return res;
    }
    
    public void getFiles2(List res) {
        List<MyFile> files;
        files = new ArrayList<MyFile>(myFiles.values());
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
    }
    
    public void clear(){
        myFiles.clear();
	put();
    }
    
    public int getSize(){
        return myFiles.size();
    }
    
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
	// maps are concurrent, so we don't need synchronization here
        writeStringToMyFileMap(aStream, myFiles);
        writeStringToStringsArrMap(aStream, canonicFiles);
	//trace(canonicFiles, "Wrote in write()");
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
    
    private static void writeStringToMyFileMap (
            final DataOutput output, Map<String, MyFile> aMap) throws IOException {
        assert output != null;
        assert aMap != null;
        int size = aMap.size();
        
        //write size
        output.writeInt(size);
        
        // write the map
        final Set<Entry<String, MyFile>> entrySet = aMap.entrySet();
        final Iterator <Entry<String, MyFile>> setIterator = entrySet.iterator();
        while (setIterator.hasNext()) {
            final Entry<String, MyFile> anEntry = setIterator.next();

            output.writeUTF(anEntry.getKey());
            assert anEntry.getValue() != null;
            anEntry.getValue().write(output);
        }
    }
    
    private static void  readStringToMyFileMap(
            final DataInput input, Map<String, MyFile> aMap) throws IOException {
        
        assert input != null; 
        assert aMap != null;
        
        final APTStringManager pathManager = FilePathCache.getManager();
        
        aMap.clear();
        final int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            String key = input.readUTF();
            key = pathManager == null? key: pathManager.getString(key);
            MyFile value = new MyFile(input);
            
            assert key != null;
            assert value != null;
            
            aMap.put(key, value);
        }
    }
    
    private static void writeStringToStringsArrMap (
            final DataOutput output, final Map<String, Object/*String or String[]*/> aMap) throws IOException {
        
        assert output != null;
        assert aMap != null;
        
        final int size = aMap.size();
        output.writeInt(size);
        
        final Set<Entry<String, Object>> entrySet = aMap.entrySet();
        final Iterator<Entry<String, Object>> setIterator = entrySet.iterator();
        
        while (setIterator.hasNext()) {
            final Entry<String, Object> anEntry = setIterator.next();
            assert anEntry != null;
            
            final String key = anEntry.getKey();
            final Object value = anEntry.getValue();
            assert key != null;
            assert value != null;
            assert ((value instanceof String) || (value instanceof String[]));
            
            output.writeUTF(key);
            
            if (value instanceof String ) {
                output.writeInt(1);
                output.writeUTF((String)value);
            } else if (value instanceof String[]) {
                
                final String[] array = (String[]) value;
                
                output.writeInt(array.length);
                for (int j = 0; j < array.length; j++) {
                    output.writeUTF(array[j]);
                }
            }
        }
    }
    
    private static void readStringToStringsArrMap (
            final DataInput input, Map<String, Object/*String or String[]*/> aMap) throws IOException {
        assert input != null;
        assert aMap != null;
        
        final APTStringManager pathManager = FilePathCache.getManager();
        
        aMap.clear();
        
        final int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            String key = input.readUTF();
            key = pathManager == null ? key : pathManager.getString(key);
            assert key != null;
            
            final int arraySize = input.readInt();
            assert arraySize != 0;
            
	    if( arraySize == 1 ) {
		aMap.put(key, input.readUTF());
	    }
	    else {
		final String[] value = new String[arraySize];
		for (int j = 0; j < arraySize; j++) {
		    String path = input.readUTF();
		    path = pathManager == null ? path : pathManager.getString(path);
		    assert path != null;

		    value[j] = path;
		}
		aMap.put(key, value);
	    }
        }
    }
    
    private static class MyFile implements Persistent, SelfPersistent {
        private final CsmUID<CsmFile> fileNew;
        private final FileImpl fileOld;
        private final String canonical;
        private APTPreprocHandler.State state;
        
        private MyFile (final DataInput input) throws IOException {
            fileNew = UIDObjectFactory.getDefaultFactory().readUID(input);
            canonical = input.readUTF();
            if (input.readBoolean()){
                state = PersistentUtils.readPreprocState(input);
            }
            fileOld = null;
        }
        
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

        public void write(final DataOutput output) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(fileNew, output);
            output.writeUTF(canonical);
            output.writeBoolean(state != null);
            if (state != null) {
                PersistentUtils.writePreprocState(state, output);
            }
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
