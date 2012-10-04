/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 * 
 * @author vv159170
 */
public final class NativeProjectProvider {
    
    /** Creates a new instance of NativeProjectProvider */
    private NativeProjectProvider() {
    }
    
    public static NativeProject createProject(String projectRoot, List<File> files,
            List<String> libProjectsPaths,
	    List<String> sysIncludes, List<String> usrIncludes,
	    List<String> sysMacros, List<String> usrMacros, List<String> undefinedMacros, boolean pathsRelCurFile) throws IOException {
	
        NativeProjectImpl project = new NativeProjectImpl(projectRoot, libProjectsPaths,
		sysIncludes, usrIncludes, sysMacros, usrMacros, undefinedMacros, pathsRelCurFile);
	
	project.addFiles(files);
	
        return project;
    }
    
    public static void fireAllFilesChanged(NativeProject nativeProject) {
	if( nativeProject instanceof NativeProjectImpl) {
	    ((NativeProjectImpl) nativeProject).fireAllFilesChanged();
	}
    }
    
    public static void setUserMacros(NativeProject nativeProject, List<String> usrMacros) {
	if( nativeProject instanceof NativeProjectImpl) {
	    ((NativeProjectImpl) nativeProject).usrMacros.clear();
            ((NativeProjectImpl) nativeProject).usrMacros.addAll(usrMacros);
	}
    }

    // XXX:FileObject conversion: remove
    public static NativeFileItem.Language getLanguage(File file, DataObject dobj) {
        CndUtils.assertNotNull(file, "null file"); //NOI18N
        FileObject fo = null;
        if (dobj != null) {
            fo = dobj.getPrimaryFile();
        }
        String mimeType;
        if (fo != null) {
            mimeType = MIMESupport.getSourceFileMIMEType(fo);
        } else {
            mimeType = MIMESupport.getSourceFileMIMEType(file);
        }
        return getLanguage(mimeType);
    }

    public static NativeFileItem.Language getLanguage(FileObject fo, DataObject dobj) {
        CndUtils.assertNotNull(fo, "null file object"); //NOI18N
        String mimeType = MIMESupport.getSourceFileMIMEType(fo);
        return getLanguage(mimeType);
    }

    private static NativeFileItem.Language getLanguage(String mimeType) {
        if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.CPP;
        } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.C;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.FORTRAN;
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.C_HEADER;
        }
        return NativeFileItem.Language.OTHER;
    }

    public static DataObject getDataObject(FileObject fo) {
        DataObject dobj = null;
        if (fo != null) {
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                // skip;
            }
        }
        return dobj;
    }

    // XXX:FileObject conversion: remove
    public static DataObject getDataObject(File file) {
        CndUtils.assertNormalized(file);
        return getDataObject(CndFileUtils.toFileObject(file));
    }
    
    public static final class NativeProjectImpl implements NativeProject {
	
	private final List<String> sysIncludes;
	private final List<String> usrIncludes;
	private final List<String> sysMacros;
	private final List<String> usrMacros;
	    
        private final List<NativeFileItem> files  = new ArrayList<NativeFileItem>();
	
        private final String projectRoot;
	private final boolean pathsRelCurFile;
	private final String name;
	private List<NativeProjectItemsListener> listeners = new ArrayList<NativeProjectItemsListener>();
        private final List<NativeProject> libProjects;

        private static final class Lock {}
        private final Object listenersLock = new Lock();
	
	public NativeProjectImpl(String projectRoot,
                List<String> libProjectsPaths,
		List<String> sysIncludes, List<String> usrIncludes, 
		List<String> sysMacros, List<String> usrMacros, List<String> undefinedMacros,
		boolean pathsRelCurFile) {

	    this.projectRoot = projectRoot;
            List<NativeProject> libs = new ArrayList<NativeProject>();
            Collection<CsmProject> projects = CsmModelAccessor.getModel().projects();
            for (String libPath : libProjectsPaths) {
                boolean found = false;
                for (CsmProject csmProject : projects) {
                    Object platformProject = csmProject.getPlatformProject();
                    if (platformProject instanceof NativeProjectProvider.NativeProjectImpl) {
                        if (((NativeProjectProvider.NativeProjectImpl) platformProject).projectRoot.equals(libPath)) {
                            assert !found : "two projects for the same root " + libPath + libs + csmProject;
                            found = true;
                            libs.add((NativeProjectProvider.NativeProjectImpl) platformProject);
                        }
                    }
                }
                assert found : " not found project for " + libPath + " in " + projects;
            }
            this.libProjects = libs;

	    this.pathsRelCurFile = pathsRelCurFile;
	    
	    this.sysIncludes = createIncludes(sysIncludes);
	    this.usrIncludes = createIncludes(usrIncludes);
	    this.sysMacros = new ArrayList<String>(sysMacros);
	    this.usrMacros = new ArrayList<String>(usrMacros);
            this.name = initName(projectRoot);
	}
	
        private String initName(String projectRoot) {
            String out = System.getProperty("cnd.modelimpl.tracemodel.project.name"); // NOI18N
            if (out == null) { 
                out = PathUtilities.getBaseName(projectRoot);
                String dir = PathUtilities.getDirName(projectRoot);
                if (dir != null) {
                    dir = PathUtilities.getBaseName(dir);
                }
                if (dir != null) {
                    out = dir + "_" + out; // NOI18N
                }
            }
            return out;
        }

        private List<String> createIncludes(List<String> src) {
	    if( pathsRelCurFile ) {
		return new ArrayList<String>(src);
	    }
	    else {
		List<String> result = new ArrayList<String>(src.size());
		for( String path : src ) {
		    File file = new File(path);
		    result.add(file.getAbsolutePath());
		}
		return result;
	    }
	}
	
	private void addFiles(List<File> files) throws IOException {
	    for( File file : files ) {
                final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file.getAbsoluteFile()));
                if (fo == null) {
                    throw new IOException("no file object for " + file); // NOI18N
                } else {
                    addFile(fo);
                }
	    }
	}
	
        @Override
        public Lookup.Provider getProject() {
            return null;
        }

        @Override
        public FileSystem getFileSystem() {
            return CndFileUtils.getLocalFileSystem();
        }
        
        @Override
        public List<String> getSourceRoots() {
            return Collections.<String>emptyList();
        }
                
        @Override
        public String getProjectRoot() {
            return this.projectRoot;
        }

        @Override
        public String getProjectDisplayName() {
            return name;
        }

        @Override
        public List<NativeFileItem> getAllFiles() {
            return Collections.unmodifiableList(files);
        }

        @Override
        public void addProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.add(listener);
	    }
        }

        @Override
        public void removeProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.remove(listener);
	    }
        }

	public void fireFileChanged(FileObject fo) {
            NativeFileItem item = findFileItem(fo);
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<NativeProjectItemsListener>(listeners);
	    }
            List<NativeFileItem> list = Collections.singletonList(item);            
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesPropertiesChanged(list);
	    }
        }

        public void fireFileAdded(FileObject fo) {
            NativeFileItem item = findFileItem(fo);
            if (item == null) {
                item = addFile(fo);
            }
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<NativeProjectItemsListener>(listeners);
	    }
            List<NativeFileItem> list = Collections.singletonList(item);
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesAdded(list);
	    }
        }

	private void fireAllFilesChanged() {
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<NativeProjectItemsListener>(listeners);
	    }
	    List<NativeFileItem> items = Collections.unmodifiableList(files);
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesPropertiesChanged(items);
	    }
	}

        @Override
        public NativeFileItem findFileItem(FileObject fileObject) {
            return findFileItem(CndFileUtils.normalizePath(fileObject));
        }

        private NativeFileItem findFileItem(String path) {
            for (NativeFileItem item : files) {
                if (item.getAbsolutePath().equalsIgnoreCase(path)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public List<FSPath> getSystemIncludePaths() {
            return CndFileUtils.toFSPathList(CndFileUtils.getLocalFileSystem(), this.sysIncludes);
        }

        @Override
        public List<FSPath> getUserIncludePaths() {
            return CndFileUtils.toFSPathList(CndFileUtils.getLocalFileSystem(), this.usrIncludes);
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            return this.sysMacros;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return this.usrMacros;
        }

	private NativeFileItem addFile(FileObject fo) {
            File file = FileUtil.toFile(fo);
            DataObject dobj = getDataObject(fo);
	    NativeFileItem.Language lang = getLanguage(fo, dobj);
	    NativeFileItem item = new NativeFileItemImpl(file, this, lang);
	    //TODO: put item in loockup of DataObject
            // registerItemInDataObject(dobj, item);
	    this.files.add(item);
            return item;
	}
	
        @Override
        public List<NativeProject> getDependences() {
            return libProjects;
        }

        @Override
        public void runOnProjectReadiness(NamedRunnable task) {
            task.run();
        }

        @Override
        public void fireFilesPropertiesChanged() {
        }
    }    
        
    /*package*/ static void registerItemInDataObject(DataObject obj, NativeFileItem item) {
        if (obj != null) {
            NativeFileItemSet set = obj.getLookup().lookup(NativeFileItemSet.class);
            if (set != null) {
                set.add(item);
            }
        }
    }
    
    private static final class NativeFileItemImpl implements NativeFileItem {
	
        private final File file;
        private final NativeProjectImpl project;
        private final NativeFileItem.Language lang;

        public NativeFileItemImpl(File file, NativeProjectImpl project, NativeFileItem.Language language) {
	    
            this.project = project;
            this.file = CndFileUtils.normalizeFile(file);
            this.lang = language;
        }
        
        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public FileObject getFileObject() {
            return CndFileUtils.toFileObject(file); // XXX:FileObject conversion
        }
        
        @Override
        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public List<FSPath> getSystemIncludePaths() {
	    List<FSPath> result = project.getSystemIncludePaths();
	    return project.pathsRelCurFile ? toAbsolute(result) : result;
        }

        @Override
        public List<FSPath> getUserIncludePaths() {
	    List<FSPath> result = project.getUserIncludePaths();
            return project.pathsRelCurFile ? toAbsolute(result) : result;
        }
	
	private List<FSPath> toAbsolute(List<FSPath> orig) {
	    File base = file.getParentFile();
	    List<FSPath> result = new ArrayList<FSPath>(orig.size());
	    for( FSPath path : orig ) {
		File pathFile = new File(path.getPath());
		if( pathFile.isAbsolute() ) {
		    result.add(path);
		}
		else {
		    pathFile = new File(base, path.getPath());
		    result.add(new FSPath(CndFileUtils.getLocalFileSystem(), pathFile.getAbsolutePath()));
		}
	    }
	    return result;
	}

        @Override
        public List<String> getSystemMacroDefinitions() {
            return project.getSystemMacroDefinitions();
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return project.getUserMacroDefinitions();
        }

        @Override
        public NativeFileItem.Language getLanguage() {
            return lang;
        }

        @Override
        public NativeFileItem.LanguageFlavor getLanguageFlavor() {
            if(CndTraceFlags.LANGUAGE_FLAVOR_CPP11) {
                return NativeFileItem.LanguageFlavor.CPP11;
            } else {
                return NativeFileItem.LanguageFlavor.UNKNOWN;
            }
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return file.getAbsolutePath();
        }

    }
}
