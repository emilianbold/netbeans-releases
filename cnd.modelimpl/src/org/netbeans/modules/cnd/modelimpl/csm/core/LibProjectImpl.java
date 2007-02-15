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
import org.netbeans.modules.cnd.modelimpl.platform.*;

/**
 * @author Vladimir Kvasihn
 */
public class LibProjectImpl extends ProjectBase {
    
    private File includePath;
//    private ProjectCache projectCache;
    private boolean filled;
//    private File zipFile;
    
//    private class LibCache extends ProjectZipCache {
//
//        public LibCache(CsmProject project, String pathBase) throws IOException {
//            super(project,  pathBase);
//        }
//
//        protected File findProjectPathFile() throws IOException {
//            return zipFile;
//        }
//
//    }
    
    public LibProjectImpl(ModelImpl model, String includePathName) {
        super(model, new File(includePathName), includePathName);
        this.includePath = (File) super.getPlatformProject();
//        String zipName = "cache-" + CacheUtil.mangleFileName(includePathName, '-') + ".zip";
//        zipFile = ModelSupport.instance().locateFile("modules/" + zipName);
//        if( zipFile != null && zipFile.exists() ) {
//            try {
//                projectCache = new LibCache(this, includePathName);
//            } catch( IOException ex ) {
//                ex.printStackTrace(System.err);
//            }
//        }
//        if( projectCache == null ) {
//            projectCache = new ProejctDummyCache();
//        }
        filled = false;
    }
    
    /* package */ LibProjectImpl () {
        
    }
    
    
    //protected void parseAllIfNeed() {
    protected void ensureFilesCreated() {
        if( ! filled ) {
            synchronized( this ) {
                if( ! filled ) {
                    filled = true;
                    // TODO-threading: this makes it render restored AST in the current thread,
                    // which is incorrect. It's ok now since cache isn't used, but
                    // should be definitely changed prior than switching the cache on!
                    fill();
                }
            }
        }
    }
    
    private void fill() {
//        for( Iterator iter = projectCache.getEntries(); iter.hasNext(); ) {
//            ProjectCache.Entry entry = (ProjectCache.Entry) iter.next();
//            if( entry != null && entry.cache != null ) {
//                    createFile(entry.sourceFile, entry.cache, (APTPreprocState)null);
//            }
//        }
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
    
    public void write(DataOutput aStream) throws IOException, IllegalArgumentException {
        super.write(aStream);
        aStream.writeBoolean(filled);
        aStream.writeUTF(includePath.getCanonicalPath());
    }
    
    void read(DataInput aStream) throws IOException, IllegalArgumentException {
        super.read(aStream);
        filled = aStream.readBoolean();
        includePath = new File (aStream.readUTF());
        
    }
}
