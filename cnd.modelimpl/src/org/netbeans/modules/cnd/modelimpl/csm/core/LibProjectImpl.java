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

import java.io.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 * @author Vladimir Kvasihn
 */
public final class LibProjectImpl extends ProjectBase {
    
    private final String includePath;
    
    private LibProjectImpl(ModelImpl model, String includePathName) {
        super(model, includePathName, includePathName);
        this.includePath = includePathName;
        this.projectRoots.fixFolder(includePathName);
        assert this.includePath != null;
    }
    
    public static LibProjectImpl createInstance(ModelImpl model, String includePathName) {
	ProjectBase instance = null;
        assert includePathName != null;
	if( TraceFlags.PERSISTENT_REPOSITORY ) {
	    try {
		instance = readInstance(model, includePathName, includePathName);
	    }
	    catch( Exception e ) {
		// just report to console;
		// the code below will create project "from scratch"
		cleanRepository(includePathName, true);
		e.printStackTrace(System.err);
	    }
	}
	if( instance == null ) {
	   instance = new LibProjectImpl(model, includePathName);
	}
        if (instance instanceof LibProjectImpl) {
           assert ((LibProjectImpl)instance).includePath != null;
        }
	return (LibProjectImpl) instance;
	
    }
    
    protected String getPath(){
        return includePath;
    }
    
    protected void ensureFilesCreated() {
    }
    
    protected boolean isStableStatus() {
        return true;
    }
    
    protected Collection<Key> getLibrariesKeys() {
        return Collections.EMPTY_SET;
    }
    
    /** override parent to avoid inifinite recursion */
    public Collection<CsmProject> getLibraries() {
        return Collections.EMPTY_SET;
    }
    
    public void onFileRemoved(FileImpl file) {}
    public void onFileRemoved(List<NativeFileItem> file) {}
    public void onFileAdded(NativeFileItem file) {}
    public void onFileAdded(List<NativeFileItem> file) {}
    public void onFilePropertyChanged(NativeFileItem nativeFile) {}
    public void onFilePropertyChanged(List<NativeFileItem> nativeFiles) {}
    
    /**
     * called to inform that file was #included from another file with specific callback
     * @param file included file path
     * @param callback callback with which the file is including
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public FileImpl onFileIncluded(ProjectBase base, String file, APTPreprocHandler preprocHandler, int mode) throws IOException {
        if( ONLY_LEX_SYS_INCLUDES ) {
            return super.onFileIncluded(base, file, preprocHandler, GATHERING_MACROS);
        } else {
            return super.onFileIncluded(base, file, preprocHandler, mode);
        }
    }
    
    
    protected void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocHandler.State state) {
        // add library file to the tail
        ParserQueue.instance().addLast(csmFile, state);
    }

    public boolean isArtificial() {
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput aStream) throws IOException {
        super.write(aStream);
        assert this.includePath != null;
        aStream.writeUTF(this.includePath);
    }
    
    public LibProjectImpl (DataInput aStream)  throws IOException {
        super(aStream);
        this.includePath = FilePathCache.getString(aStream.readUTF());
        assert this.includePath != null;
        setPlatformProject(this.includePath);
    }
}
