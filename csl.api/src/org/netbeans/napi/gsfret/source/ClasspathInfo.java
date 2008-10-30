/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.napi.gsfret.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.source.CacheClassPath;
import org.netbeans.modules.csl.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * Class which contains info about classpath
 *
 * @author Tomas Zezula, Petr Hrebejk
 */
public final class ClasspathInfo {
    
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    
    static {
        ClasspathInfoAccessor.setInstance(new ClasspathInfoAccessorImpl());
        try {
            Class.forName(ClassIndex.class.getName(), true, CompilationInfo.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            ErrorManager.getDefault().notify (ex);
        }
    }
    
//    private final CachingArchiveProvider archiveProvider;
    private final ClassPath srcClassPath;
    private final ClassPath bootClassPath;
    private final ClassPath compileClassPath;
    private final ClassPath cachedBootClassPath;
    private final ClassPath cachedCompileClassPath;
    private ClassPath outputClassPath;
    
    private final ClassPathListener cpListener;
    private final boolean backgroundCompilation;
    private final boolean ignoreExcludes;
    private final Object/*JavaFileFilterImplementation*/ filter;
    //private JavaFileManager fileManager;
    private EventListenerList listenerList =  null;
    private List<ClassIndex> usagesQuery;
    
    /** Creates a new instance of ClasspathInfo (private use the factory methods) */
    private ClasspathInfo(/*CachingArchiveProvider archiveProvider,*/ ClassPath bootCp, ClassPath compileCp, ClassPath srcCp,
        Object/*JavaFileFilterImplementation*/ filter, boolean backgroundCompilation, boolean ignoreExcludes) {
        assert /*archiveProvider != null &&*/ bootCp != null && compileCp != null;
        this.cpListener = new ClassPathListener ();
        //this.archiveProvider = archiveProvider;        
        this.bootClassPath = bootCp;
        this.compileClassPath = compileCp;
        this.cachedBootClassPath = CacheClassPath.forBootPath(this.bootClassPath);
        this.cachedCompileClassPath = CacheClassPath.forClassPath(this.compileClassPath);
	this.cachedBootClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedBootClassPath));
	this.cachedCompileClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedCompileClassPath));
	if ( srcCp != null ) {
            this.srcClassPath = srcCp;
            this.outputClassPath = CacheClassPath.forSourcePath (this.srcClassPath);
	    this.srcClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.srcClassPath));
	}
        else {
            this.srcClassPath = ClassPathSupport.createClassPath(new URL[0]);
            this.outputClassPath = ClassPathSupport.createClassPath(new URL[0]);
        }
        this.backgroundCompilation = backgroundCompilation;
        this.ignoreExcludes = ignoreExcludes;
        this.filter = filter;
    }
    
    @Override
    public String toString() {
        return "ClasspathInfo boot:[" + cachedBootClassPath + "],compile:[" + cachedCompileClassPath + "],src:[" + srcClassPath + "]";  //NOI18N
    }
    
    // Factory methods ---------------------------------------------------------
    
    
    /** Creates new interface to the compiler
     * @param file for which the CompilerInterface should be created
     * @return ClasspathInfo or null if the file does not exist on the
     * local file system or it has no classpath associated
     */
    public static ClasspathInfo create (final File file) {
        if (file == null) {
            throw new IllegalArgumentException ("Cannot pass null as parameter of ClasspathInfo.create(java.io.File)");     //NOI18N
        }
        final FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return null;
        }
        else {
            return create (fo);
        }
    }
    
    
    private static ClasspathInfo create (FileObject fo, Object/*JavaFileFilterImplementation*/ filter, boolean backgroundCompilation, boolean ignoreExcludes) {
        ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (bootPath == null) {
            //javac requires at least java.lang
            //bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
            bootPath = EMPTY_PATH;
        }
        ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        if (compilePath == null) {
            compilePath = EMPTY_PATH;
        }
        ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (srcPath == null) {
            srcPath = EMPTY_PATH;
        }
        return create (bootPath, compilePath, srcPath, filter, backgroundCompilation, ignoreExcludes);
    }
    
    /** Creates new interface to the compiler
     * @param fo for which the CompilerInterface should be created
     */
    public static ClasspathInfo create(FileObject fo) {
        return create (fo, null, false, false);
    }            
    
    private static ClasspathInfo create(ClassPath bootPath, ClassPath classPath, ClassPath sourcePath, Object/*JavaFileFilterImplementation*/ filter,
                                        boolean backgroundCompilation, boolean ignoreExcludes) {
        return new ClasspathInfo(/*CachingArchiveProvider.getDefault(),*/ bootPath, classPath, sourcePath, filter, backgroundCompilation, ignoreExcludes);
    }
    
    public static ClasspathInfo create(ClassPath bootPath, ClassPath classPath, ClassPath sourcePath) {        
        return new ClasspathInfo(/*CachingArchiveProvider.getDefault(),*/ bootPath, classPath, sourcePath, null, false, false);
    }
       
    // Public methods ----------------------------------------------------------
       
    /** Registers ChangeListener which will be notified about the changes in the classpath.
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
        if (listenerList == null ) {
            listenerList = new EventListenerList();
        }
        listenerList.add (ChangeListener.class, listener);
    }

    /**Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        listenerList.remove (ChangeListener.class, listener);
    }

    public ClassPath getClassPath (PathKind pathKind) {
	switch( pathKind ) {
	    case BOOT:
		return this.bootClassPath;
	    case COMPILE:
		return this.compileClassPath;
	    case SOURCE:
		return this.srcClassPath;
	    default:
		assert false : "Unknown path type";     //NOI18N
		return null;
	}
    }
    
    ClassPath getCachedClassPath (PathKind pathKind) {
        switch( pathKind ) {
	    case BOOT:
		return this.cachedBootClassPath;
	    case COMPILE:
		return this.cachedCompileClassPath;
	    case SOURCE:
		return this.srcClassPath;
	    case OUTPUT:
		return this.outputClassPath;
	    default:
		assert false : "Unknown path type";     //NOI18N
		return null;
	}
    }
    
    
    public synchronized ClassIndex getClassIndex(String mimeType) {
        if (usagesQuery == null) {
            usagesQuery = new ArrayList<ClassIndex>(8);
        } else {
            for (int i = 0; i < usagesQuery.size(); i++) {
                ClassIndex ci = usagesQuery.get(i);
                if (ci.getLanguage().getMimeType().equals(mimeType)) {
                    return ci;
                }
            }
        }

        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        assert language != null : mimeType;
        
        ClassIndex ci = new ClassIndex (
                language,
                this.bootClassPath,
                this.compileClassPath,
                this.srcClassPath);

        usagesQuery.add(ci);
        
        return ci;
    }
    
    // Package private methods -------------------------------------------------
//    
//    synchronized JavaFileManager getFileManager() {
//        if (this.fileManager == null) {
//            boolean hasSources = this.srcClassPath != null;
//            this.fileManager = new ProxyFileManager (
//                new CachingFileManager (this.archiveProvider, this.bootClassPath, true),
//                new CachingFileManager (this.archiveProvider, this.compileClassPath, false),
//                hasSources ? (backgroundCompilation ? new CachingFileManager (this.archiveProvider, this.srcClassPath, filter, false)
//                    : new SourceFileManager (this.srcClassPath.getRoots())) : null,
//                hasSources ? new OutputFileManager (this.archiveProvider, this.outputClassPath, this.srcClassPath) : null
//            );
//        }
//        return this.fileManager;
//    }
    
    // Private methods ---------------------------------------------------------
    
    private void fireChangeListenerStateChanged() {
        ChangeEvent e = null;
        if (listenerList == null) return;
        Object[] listeners = listenerList.getListenerList ();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i]==ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged (e);
           }
        }
    }


    // Innerclasses ------------------------------------------------------------


    public static enum PathKind {	
	BOOT,	
	COMPILE,	
	SOURCE,	
	OUTPUT,
	
    }
    
    private class ClassPathListener implements PropertyChangeListener {	
		
        public void propertyChange (PropertyChangeEvent event) {
            if (ClassPath.PROP_ROOTS.equals(event.getPropertyName())) {
                synchronized (this) {
                    // Kill FileManager
                    //fileManager = null;
                }
                fireChangeListenerStateChanged();
            }
        }
    }
    
    private static class ClasspathInfoAccessorImpl extends ClasspathInfoAccessor {
        
//        @Override
//        public JavaFileManager getFileManager(ClasspathInfo cpInfo) {
//            return cpInfo.getFileManager();
//        }

        public ClassPath getCachedClassPath(final ClasspathInfo cpInfo, final PathKind kind) {
            return cpInfo.getCachedClassPath(kind);
        }                
        
        @Override
        public ClasspathInfo create (ClassPath bootPath, ClassPath classPath, ClassPath sourcePath, Object/*JavaFileFilterImplementation*/ filter, boolean backgroundCompilation, boolean ignoreExcludes) {
            return ClasspathInfo.create(bootPath, classPath, sourcePath, filter, backgroundCompilation, ignoreExcludes);
        }
        
        @Override
        public ClasspathInfo create (FileObject fo, Object/*JavaFileFilterImplementation*/ filter, boolean backgroundCompilation, boolean ignoreExcludes) {
            return ClasspathInfo.create(fo, filter, backgroundCompilation, ignoreExcludes);
        }                                
    }
}
