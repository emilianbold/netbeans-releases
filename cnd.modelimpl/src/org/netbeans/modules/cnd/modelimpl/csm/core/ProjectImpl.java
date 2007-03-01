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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;

/**
 * Project implementation
 * @author Vladimir Kvashin
 */
public final class ProjectImpl extends ProjectBase {

    public ProjectImpl(ModelImpl model, Object platformProject, String name) {
        super(model, platformProject, name);
        // RepositoryUtils.put(this);
    }
    
    protected void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile) {
	assert (nativeFile != null && nativeFile.getFile() != null);
	if( ! isLanguageSupported(nativeFile.getLanguage() )) {
	    return;
	}
	File file = nativeFile.getFile();
	APTPreprocState preprocState = getDefaultPreprocState(nativeFile);
	if (preprocState != null && getPreprocStateState(file) == null){
	    putPreprocStateState(file,preprocState.getState());
	}
	if (isSourceFile) {
	    findFile(file, FileImpl.SOURCE_FILE, preprocState, true);
	} else {
	    findFile(file, FileImpl.HEADER_FILE, preprocState, true);
	}
    }
    
    protected FileImpl findFile(File file, int fileType, APTPreprocState preprocState, boolean scheduleParseIfNeed) {
        FileImpl impl = (FileImpl) getFile(file);
        if( impl == null ) {
            synchronized( getFilesLock() ) {
                impl = (FileImpl) getFile(file);
                if( impl == null ) {
                    preprocState = preprocState == null ? getPreprocState(file) : preprocState;
                    impl = new FileImpl(ModelSupport.instance().getFileBuffer(file), this, fileType, preprocState);
                    putFile(file, impl);
                    // NB: parse only after putting into a map
                    if( scheduleParseIfNeed ) {
                        APTPreprocState.State ppState = preprocState == null ? null : preprocState.getState();
                        ParserQueue.instance().addLast(impl, ppState);
                    }
                }
            }
        } else {
            if (fileType == FileImpl.SOURCE_FILE && !impl.isSourceFile()){
                impl.setSourceFile();
            } else if (fileType == FileImpl.HEADER_FILE && !impl.isHeaderFile()){
                impl.setHeaderFile();
            }
        }
        return impl;
    }
    
    protected void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocState.State state) {
        // add project's file to the head
        ParserQueue.instance().addFirst(csmFile, state, true);
    }
    
    public void onFileEditStart(FileBuffer buf, NativeFileItem nativeFile) {
	if( ! isLanguageSupported(nativeFile.getLanguage() )) {
	    return;
	}
        if( TraceFlags.DEBUG ) Diagnostic.trace("------------------------- onFileEditSTART " + buf.getFile().getName()); // NOI18N
        FileImpl file = (FileImpl) getFile(buf.getFile());
        if( file == null ) {
            file = new FileImpl(buf, this); // don't enqueue here!
            putFile(buf.getFile(), file);
        } else {
            file.setBuffer(buf); // don't enqueue here!
            synchronized( editedFiles ) {
                editedFiles.add(file);                
            }
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().invalidate(file);
            } else {
                APTDriver.getInstance().invalidateAPT(buf);     
            }
        }
    }
    
    public void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile) {
	if( ! isLanguageSupported(nativeFile.getLanguage() )) {
	    return;
	}
        if( TraceFlags.DEBUG ) Diagnostic.trace("------------------------- onFileEditEND " + buf.getFile().getName()); // NOI18N
        FileImpl file = (FileImpl) getFile(buf.getFile());
        if( file != null ) {
            synchronized( editedFiles ) {
                editedFiles.remove(file);
            }
            file.setBuffer(buf);
            ParserQueue.instance().addFirst(file, getPreprocState(buf.getFile()).getState(), false);
        }
    }
    
    public void onFilePropertyChanged(NativeFileItem nativeFile) {
	if( ! isLanguageSupported(nativeFile.getLanguage() )) {
	    return;
	}
        if( TraceFlags.DEBUG ) Diagnostic.trace("------------------------- onFilePropertyChanged " + nativeFile.getFile().getName()); // NOI18N
        FileImpl file = (FileImpl) getFile(nativeFile.getFile());	
        if( file != null ) {
            file.stateChanged(true);
            ParserQueue.instance().addFirst(file, getPreprocState(nativeFile.getFile()).getState(), false);
        }
    }
    
    public void onFileRemoved(NativeFileItem nativeFile) {
        try {
            //Notificator.instance().startTransaction();
            File file = nativeFile.getFile();
            FileImpl impl = (FileImpl) getFile(file);
            if( impl != null ) {
                synchronized( editedFiles ) {
                    editedFiles.remove(impl);
                }
                impl.dispose();
                removeFile(file);
                if (TraceFlags.USE_AST_CACHE) {
                    CacheManager.getInstance().invalidate(impl);
                } else {
                    APTDriver.getInstance().invalidateAPT(impl.getBuffer());
                }
                ParserQueue.instance().remove(impl);
            }
        } finally {
            //Notificator.instance().endTransaction();
            Notificator.instance().flush();
        }
    }
    
    public void onFileAdded(NativeFileItem nativeFile) {
	if( isLanguageSupported(nativeFile.getLanguage() )) {
	    try {
		//Notificator.instance().startTransaction();
		createIfNeed(nativeFile, isSourceFile(nativeFile));
	    } finally {
		//Notificator.instance().endTransaction();
		Notificator.instance().flush();
	    }
	}
    }
     
    protected void ensureChangedFilesEnqueued() {
	synchronized( editedFiles ) {
	    super.ensureChangedFilesEnqueued();
	    for( Iterator iter = editedFiles.iterator(); iter.hasNext(); ) {
		FileImpl file = (FileImpl) iter.next();
		if( ! file.isParsingOrParsed() ) {
		    ParserQueue.instance().addLast(file, getPreprocState(file.getBuffer().getFile()).getState());
		}
	    }
	}
        //N.B. don't clear list of editedFiles here.
    }
    
    protected boolean hasChangedFiles(CsmFile skipFile) {
        if (skipFile == null) {
            return false;
        }
	synchronized( editedFiles ) {
	    for( Iterator iter = editedFiles.iterator(); iter.hasNext(); ) {
		FileImpl file = (FileImpl) iter.next();
		if( (skipFile != file) && ! file.isParsingOrParsed() ) {
		    return true;
		}
	    }
	}
        return false;
    }
    
    
    private Set/*<CsmFile>*/ editedFiles = new HashSet/*<CsmFile>*/();

    public ProjectBase resolveFileProject(String absPath) {
        ProjectBase retValue = super.resolveFileProject(absPath);
        // trick for tracemodel. We should accept all not registered files as well, till it is not system one.
        if (ParserThreadManager.instance().isStandalone()) {
            retValue = absPath.startsWith("/usr") ? retValue : this; // NOI18N
        }
        return retValue;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
        
    public void write(DataOutput aStream) throws IOException {
        super.write(aStream);
    }
    
    public ProjectImpl (DataInput input) throws IOException {
        super(input);      
    }    
}
