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
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTIncludeUtils;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Project implementation
 * @author Vladimir Kvashin
 */
public final class ProjectImpl extends ProjectBase {
    
    private ProjectImpl(ModelImpl model, Object platformProject, String name) {
        super(model, platformProject, name);
        // RepositoryUtils.put(this);
    }
    
    public static ProjectImpl createInstance(ModelImpl model, String platformProject, String name) {
        return createInstance(model, (Object) platformProject, name);
    }
    
    public static ProjectImpl createInstance(ModelImpl model, NativeProject platformProject, String name) {
        return createInstance(model, (Object) platformProject, name);
    }
    
    private static ProjectImpl createInstance(ModelImpl model, Object platformProject, String name) {
	ProjectBase instance = null;
	if( TraceFlags.PERSISTENT_REPOSITORY ) {
	    try {
		instance = readInstance(model, platformProject, name);
	    }
	    catch( Exception e ) {
		// just report to console;
		// the code below will create project "from scratch"
		cleanRepository(platformProject, false);
		e.printStackTrace(System.err);
	    }
	}
	if( instance == null ) {
	   instance = new ProjectImpl(model, platformProject, name); 
	}
	return (ProjectImpl) instance;
    }
    
    protected void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocHandler.State state) {
        // add project's file to the head
        ParserQueue.instance().addFirst(csmFile, state, true);
    }
    
    public void onFileEditStart(FileBuffer buf, NativeFileItem nativeFile) {
        if( !acceptNativeItem(nativeFile)) {
            return;
        }
        if( TraceFlags.DEBUG ) Diagnostic.trace("------------------------- onFileEditSTART " + buf.getFile().getName()); // NOI18N
	FileImpl impl = createOrFindFileImpl(buf, nativeFile);
        if (impl != null) {
            impl.setBuffer(buf); // don't enqueue here!
            synchronized( editedFiles ) {
                editedFiles.add(impl);
            }
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().invalidate(impl);
            } else {
                APTDriver.getInstance().invalidateAPT(buf);
            }
        }
    }

    public void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile) {
        if( ! acceptNativeItem(nativeFile)) {
            return;
        }
        if( TraceFlags.DEBUG ) Diagnostic.trace("------------------------- onFileEditEND " + buf.getFile().getName()); // NOI18N
        FileImpl file = getFile(buf.getFile());
        if( file != null ) {
            synchronized( editedFiles ) {
                if (!editedFiles.remove(file)){
                    // FixUp double file edit end on mounted files
                    return;
                }
            }
            file.setBuffer(buf);
            if (TraceFlags.USE_DEEP_REPARSING) {
                DeepReparsingUtils.reparseOnEdit(file,this);
            } else {
                ParserQueue.instance().addFirst(file, getPreprocHandler(buf.getFile()).getState(), false);
            }
        }
    }
    
    public void onFilePropertyChanged(NativeFileItem nativeFile) {
        if( ! acceptNativeItem(nativeFile)) {
            return;
        }
        if( TraceFlags.DEBUG ) Diagnostic.trace("------------------------- onFilePropertyChanged " + nativeFile.getFile().getName()); // NOI18N
        DeepReparsingUtils.reparseOnPropertyChanged(nativeFile, this);
    }
    
    public void onFilePropertyChanged(List<NativeFileItem> items) {
        if (items.size()>0){
            DeepReparsingUtils.reparseOnPropertyChanged(items, this);
        }
    }
    
    public void onFileRemoved(FileImpl impl) {
        try {
            //Notificator.instance().startTransaction();
            onFileRemovedImpl(impl);
            if( impl != null ) {
                DeepReparsingUtils.reparseOnRemoved(impl,this);
            }
        } finally {
            //Notificator.instance().endTransaction();
            Notificator.instance().flush();
        }
    }

    private FileImpl onFileRemovedImpl(FileImpl impl) {
        APTIncludeUtils.clearFileExistenceCache();
        if( impl != null ) {
            synchronized( editedFiles ) {
                editedFiles.remove(impl);
            }
            impl.dispose();
            removeFile(new File(impl.getAbsolutePath()));
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().invalidate(impl);
            } else {
                APTDriver.getInstance().invalidateAPT(impl.getBuffer());
            }
            ParserQueue.instance().remove(impl);
        }
        return impl;
    }

    public void onFileRemoved(List<NativeFileItem> items) {
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
            List<FileImpl> toReparse = new ArrayList<FileImpl>();
            for(NativeFileItem item : items) {
                File file = item.getFile();
                try {
                    //Notificator.instance().startTransaction();
		    FileImpl impl = getFile(file);
                    if( impl != null ) {
			onFileRemovedImpl(impl);
                        toReparse.add(impl);
                    }
                } finally {
                    //Notificator.instance().endTransaction();
                    Notificator.instance().flush();
                }
            }
            DeepReparsingUtils.reparseOnRemoved(toReparse,this);
        } finally{
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    public void onFileAdded(NativeFileItem nativeFile) {
	onFileAddedImpl(nativeFile, true);
    }

    private NativeFileItem onFileAddedImpl(NativeFileItem nativeFile, boolean deepReparse) {
        if( acceptNativeItem(nativeFile)) {
	    APTIncludeUtils.clearFileExistenceCache();
            try {
                //Notificator.instance().startTransaction();
                createIfNeed(nativeFile, isSourceFile(nativeFile), null);
                return nativeFile;
            } finally {
                //Notificator.instance().endTransaction();
                Notificator.instance().flush();
		if( deepReparse ) {
		    DeepReparsingUtils.reparseOnAdded(nativeFile,this);
		}
            }
        }
        return null;
    }

    public void onFileAdded(List<NativeFileItem> items) {
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
            List<NativeFileItem> toReparse = new ArrayList<NativeFileItem>();
            for(NativeFileItem item : items) {
                NativeFileItem done = onFileAddedImpl(item, false);
                if(done != null) {
                    toReparse.add(done);
                }
            }
            DeepReparsingUtils.reparseOnAdded(toReparse,this);
        } finally{
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }
    
    protected void ensureChangedFilesEnqueued() {
        synchronized( editedFiles ) {
            super.ensureChangedFilesEnqueued();
            for( Iterator iter = editedFiles.iterator(); iter.hasNext(); ) {
                FileImpl file = (FileImpl) iter.next();
                if( ! file.isParsingOrParsed() ) {
                    ParserQueue.instance().addLast(file, getPreprocHandler(file.getBuffer().getFile()).getState());
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
    
    public ProjectBase findFileProject(String absPath) {
        ProjectBase retValue = super.findFileProject(absPath);
        // trick for tracemodel. We should accept all not registered files as well, till it is not system one.
        if (ParserThreadManager.instance().isStandalone()) {
            retValue = absPath.startsWith("/usr") ? retValue : this; // NOI18N
        }
        return retValue;
    }

    public boolean isArtificial() {
        return false;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput aStream) throws IOException {
        super.write(aStream);
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
	// we don't need this since ProjectBase persists fqn 
        //aFactory.writeUID(getUID(), aStream);
        LibraryManager.getInsatnce().writeProjectLibraries(getUID(),aStream);
    }

    public ProjectImpl(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
	// we don't need this since ProjectBase persists fqn 
        //CsmUID uid = aFactory.readUID(input);
        //LibraryManager.getInsatnce().read(uid, input);
	LibraryManager.getInsatnce().readProjectLibraries(getUID(), input);
    }
}
