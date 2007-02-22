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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.*;
import java.util.*;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.platform.*;

/**
 * @author Vladimir Kvasihn
 */
public class LibProjectImpl extends ProjectBase {
    
    private String includePath;
    
    public LibProjectImpl(ModelImpl model, String includePathName) {
        super(model, new File(includePathName), includePathName);
        this.includePath = includePathName;
    }
    
    protected void ensureFilesCreated() {
    }
    
    protected void createIfNeed(NativeFileItem file, boolean isSourceFile) {
	// NB: for those who decide to implement this: don't forget to check the language
    }
    
    public FileImpl findFile(File srcFile, int fileType, APTPreprocState preprocState, boolean scheduleParseIfNeed) {
        FileImpl impl = (FileImpl) getFile(srcFile);
        if( impl == null ) {
            synchronized( getFilesLock() ) {
                if( impl == null ) {
                    impl = new FileImpl(ModelSupport.instance().getFileBuffer(srcFile), this, fileType, preprocState);
                    putFile(srcFile, impl);
                    //impl.parse();
                    if( scheduleParseIfNeed ) {
                        APTPreprocState.State ppState = preprocState == null ? null : preprocState.getState();
                        ParserQueue.instance().addLast(impl, ppState);
                    }
                }
            }
        }
        return impl;
    }
    
    /** override parent to avoid inifinite recursion */
    public Collection/*<CsmProject>*/ getLibraries() {
        return Collections.EMPTY_SET;
    }
    
    public void onFileRemoved(NativeFileItem file) {}
    public void onFileAdded(NativeFileItem file) {}
    
    /**
     * called to inform that file was #included from another file with specific callback
     * @param file included file path
     * @param callback callback with which the file is including
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public FileImpl onFileIncluded(String file, APTPreprocState preprocState, int mode) {
        if( ONLY_LEX_SYS_INCLUDES ) {
            return super.onFileIncluded(file, preprocState, GATHERING_MACROS);
        } else {
            return super.onFileIncluded(file, preprocState, mode);
        }
    }
    
    
    protected void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocState.State state) {
        // add library file to the tail
        ParserQueue.instance().addLast(csmFile, state);
    }
    
    public ProjectBase resolveFileProject(String absPath, boolean onInclude, Collection paths) {
        // FIXUP: now accept all /usr/ files
        // FIXUP: now accept cygwin files; this is a temporary solution we need to be able to measure performance on Windows
        //if (absPath.startsWith("/usr/") || absPath.startsWith("C:\\cygwin")) {
        //    return this;
        //} else {
        //    return null;
        //}
        
        File file = new File(absPath);
        List dirs = new ArrayList();
        while((file=file.getParentFile())!= null){
            dirs.add(file);
        }
        for (Iterator i = paths.iterator(); i.hasNext();){
            File path = new File((String)i.next());
            for(int j = 0; j < dirs.size(); j++){
                file = (File)dirs.get(j);
                if (file.equals(path)){
                    return this;
                }
            }
        }
        return null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput aStream) throws IOException {
        super.write(aStream);
        aStream.writeUTF(includePath);
    }
    
    public LibProjectImpl (DataInput aStream)  throws IOException {
        super(aStream);
        includePath = FilePathCache.getString(aStream.readUTF());
        setPlatformProject(new File(includePath));
    }
}
