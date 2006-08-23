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

import java.io.File;
import java.util.*;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.antlr2.PPCallback;
import org.netbeans.modules.cnd.modelimpl.antlr2.PPCallbackImpl;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;

/**
 * Project implementation
 * @author Vladimir Kvashin
 */
public class ProjectImpl extends ProjectBase {

    public ProjectImpl(ModelImpl model, Object platformProject, String name) {
        super(model, platformProject, name);
    }
    
    protected void createIfNeed(NativeFileItem nativeFile) {
        assert (nativeFile != null && nativeFile.getFile() != null);
        File file = nativeFile.getFile();
        if (TraceFlags.USE_APT) {
            APTPreprocState preprocState = getDefaultPreprocState(nativeFile);
            findFile(file, preprocState, true);
        } else {
            PPCallback callback = getDefaultCallback(nativeFile);
            findFile(file, callback, true);            
        }
    }
    
    protected FileImpl findFile(File file, PPCallback callback, boolean scheduleParseIfNeed) {
        FileImpl impl = (FileImpl) getFiles().get(file);
        if( impl == null ) {
            synchronized( getFiles() ) {
                impl = (FileImpl) getFiles().get(file);
                if( impl == null ) {
                    callback = callback == null ? getCallback(file) : callback;
                    impl = new FileImpl(ModelSupport.instance().getFileBuffer(file), this, callback);
                    getFiles().put(file, impl);
                    // NB: parse only after putting into a map
                    if( scheduleParseIfNeed ) {
                        ParserQueue.instance().addLast(impl);
                    }
                }
            }
        }
        return impl;
    }
    
    // copy
    protected FileImpl findFile(File file, APTPreprocState preprocState, boolean scheduleParseIfNeed) {
        FileImpl impl = (FileImpl) getFiles().get(file);
        if( impl == null ) {
            synchronized( getFiles() ) {
                impl = (FileImpl) getFiles().get(file);
                if( impl == null ) {
                    preprocState = preprocState == null ? getPreprocState(file) : preprocState;
                    impl = new FileImpl(ModelSupport.instance().getFileBuffer(file), this, preprocState);
                    getFiles().put(file, impl);
                    // NB: parse only after putting into a map
                    if( scheduleParseIfNeed ) {
                        ParserQueue.instance().addLast(impl);
                    }
                }
            }
        }
        return impl;
    }
    
    protected void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocState.State state) {
        // add project's file to the head
        ParserQueue.instance().addFirst(csmFile, state);
    }
    
    public void onFileEditStart(FileBuffer buf) {
        if( Diagnostic.DEBUG ) Diagnostic.trace("------------------------- onFileEditSTART " + buf.getFile().getName());
        FileImpl file = (FileImpl) getFiles().get(buf.getFile());
        if( file == null ) {
            file = new FileImpl(buf, this); // don't enqueue here!
            getFiles().put(buf.getFile(), file);
        } else {
            file.setBuffer(buf); // don't enqueue here!
            synchronized( this ) {
                editedFiles.add(file);                
            }
            APTDriver.getInstance().invalidateAPT(buf);            
        }
    }
    
    public void onFileEditEnd(FileBuffer buf) {
        if( Diagnostic.DEBUG ) Diagnostic.trace("------------------------- onFileEditEND " + buf.getFile().getName());
        FileImpl file = (FileImpl) getFiles().get(buf.getFile());
        if( file != null ) {
            synchronized( this ) {
                editedFiles.remove(file);
            }
            file.setBuffer(buf);
            ParserQueue.instance().addFirst(file);
        }
    }
    
    public void onFileRemoved(NativeFileItem nativeFile) {
        try {
            //Notificator.instance().startTransaction();
            File file = nativeFile.getFile();
            FileImpl impl = (FileImpl) getFiles().get(file);
            if( impl != null ) {
                impl.dispose();
                getFiles().remove(file);
                APTDriver.getInstance().invalidateAPT(impl.getBuffer());
                ParserQueue.instance().remove(impl);
            }
        } finally {
            //Notificator.instance().endTransaction();
            Notificator.instance().flush();
        }
    }
    
    protected PPCallback getDefaultCallback(File file) {
        return new PPCallbackImpl(this, file.getAbsolutePath(), false);
    }
    
    public void onFileAdded(NativeFileItem nativeFile) {
        try {
            //Notificator.instance().startTransaction();
            createIfNeed(nativeFile);
        } finally {
            //Notificator.instance().endTransaction();
            Notificator.instance().flush();
        }
    }
    
    protected synchronized void ensureChangedFilesEnqueued() {
        super.ensureChangedFilesEnqueued();
        for( Iterator iter = editedFiles.iterator(); iter.hasNext(); ) {
            FileImpl file = (FileImpl) iter.next();
            if( ! file.isParsed() ) {
                ParserQueue.instance().addLast(file);
            }
        }
        //N.B. don't clear list of editedFiles here.
    }
    
    protected synchronized boolean hasChangedFiles() {
        for( Iterator iter = editedFiles.iterator(); iter.hasNext(); ) {
            FileImpl file = (FileImpl) iter.next();
            if( ! file.isParsed() ) {
                return true;
            }
        }
        return false;
    }
    
    private Set/*<CsmFile>*/ editedFiles = new HashSet/*<CsmFile>*/();
}
