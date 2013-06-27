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
package org.netbeans.api.java.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.tools.JavaFileManager;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.CachingFileManager;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.parsing.ProxyFileManager;
import org.netbeans.modules.java.source.parsing.SourceFileManager;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.classpath.AptSourcePath;
import org.netbeans.modules.java.source.classpath.SourcePath;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.parsing.*;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/** Class which contains info about classpath
 *
 * @author Tomas Zezula, Petr Hrebejk
 */
public final class ClasspathInfo {
    
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    private static final Logger log = Logger.getLogger(ClasspathInfo.class.getName());
    
    static {
        ClasspathInfoAccessor.setINSTANCE(new ClasspathInfoAccessorImpl());
        try {
            Class.forName(ClassIndex.class.getName(), true, CompilationInfo.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }    
    
    private final CachingArchiveProvider archiveProvider;
    
    private final ClassPath srcClassPath;
    private final ClassPath bootClassPath;
    private final ClassPath compileClassPath;
    private final ClassPath cachedAptSrcClassPath;
    private final ClassPath cachedSrcClassPath;
    private final ClassPath cachedBootClassPath;
    private final ClassPath cachedCompileClassPath;
    private final ClassPath outputClassPath;
    
    private final ClassPathListener cpListener;
    private final boolean useModifiedFiles;
    private final boolean ignoreExcludes;
    private final JavaFileFilterImplementation filter;
    private final MemoryFileManager memoryFileManager;
    private final ChangeSupport listenerList;
    private final FileManagerTransaction fmTx;
    private final ProcessorGenerated pgTx;
    
    //@GuardedBy("this")
    private ClassIndex usagesQuery;
    
    /** Creates a new instance of ClasspathInfo (private use the factory methods) */
    private ClasspathInfo(final @NonNull CachingArchiveProvider archiveProvider,
                          final @NonNull ClassPath bootCp,
                          final @NonNull ClassPath compileCp,
                          final ClassPath srcCp,
                          final JavaFileFilterImplementation filter,
                          final boolean backgroundCompilation,
                          final boolean ignoreExcludes,
                          final boolean hasMemoryFileManager,
                          final boolean useModifiedFiles) {
        assert archiveProvider != null;
        assert bootCp != null;
        assert compileCp != null;
        this.cpListener = new ClassPathListener ();
        this.archiveProvider = archiveProvider;
        this.bootClassPath = bootCp;
        this.compileClassPath = compileCp;
        this.listenerList = new ChangeSupport(this);
        this.cachedBootClassPath = CacheClassPath.forBootPath(this.bootClassPath,backgroundCompilation);
        this.cachedCompileClassPath = CacheClassPath.forClassPath(this.compileClassPath,backgroundCompilation);
        if (!backgroundCompilation) {
            this.cachedBootClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedBootClassPath));
            this.cachedCompileClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedCompileClassPath));
        }
        if (srcCp == null) {
            this.cachedSrcClassPath = this.srcClassPath = EMPTY_PATH;
            this.cachedAptSrcClassPath = null;
            this.outputClassPath = EMPTY_PATH;
        } else {
            this.srcClassPath = srcCp;
            final ClassPathImplementation noApt = AptSourcePath.sources(srcCp);
            this.cachedSrcClassPath = ClassPathFactory.createClassPath(SourcePath.filtered(noApt, backgroundCompilation));
            this.cachedAptSrcClassPath = ClassPathFactory.createClassPath(
                    SourcePath.filtered(AptSourcePath.aptCache(srcCp), backgroundCompilation));
            this.outputClassPath = CacheClassPath.forSourcePath (ClassPathFactory.createClassPath(noApt),backgroundCompilation);
            if (!backgroundCompilation) {
                this.cachedSrcClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener,this.cachedSrcClassPath));
            }
        }
        this.ignoreExcludes = ignoreExcludes;
        this.useModifiedFiles = useModifiedFiles;
        this.filter = filter;
        if (hasMemoryFileManager) {
            if (srcCp == null) {
                throw new IllegalStateException ();
            }
            this.memoryFileManager = new MemoryFileManager();
        } else {
            this.memoryFileManager = null;
        }
        if (backgroundCompilation) {
            final TransactionContext txCtx = TransactionContext.get();
            fmTx = txCtx.get(FileManagerTransaction.class);
            pgTx = txCtx.get(ProcessorGenerated.class);
        } else {
            //No real transaction, read-only mode.
            fmTx = FileManagerTransaction.nullWrite();
            pgTx = ProcessorGenerated.nullWrite();
        }
        assert fmTx != null : "No file manager transaction.";   //NOI18N
        assert pgTx != null : "No processor generated transaction.";   //NOI18N
        pgTx.bind(this, this.cachedSrcClassPath, this.cachedAptSrcClassPath);
    }

    @Override
    public String toString() {
        return String.format(
            "ClasspathInfo [boot: %s, compile: %s, src: %s, internal boot: %s, internal compile: %s, internal src: %s, internal out: %s]", //NOI18N
                bootClassPath,
                compileClassPath,
                srcClassPath,
                cachedBootClassPath,
                cachedCompileClassPath,
                cachedSrcClassPath,
                outputClassPath);        
    }

    @Override
    public int hashCode() {        
        return Arrays.hashCode(toURIs(this.srcClassPath));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClasspathInfo)) {
            return false;
        }
        final ClasspathInfo other = (ClasspathInfo) obj;
        return Arrays.equals(toURIs(this.srcClassPath), toURIs(other.srcClassPath)) &&
            Arrays.equals(toURIs(this.compileClassPath), toURIs(other.compileClassPath)) &&
            Arrays.equals(toURIs(this.bootClassPath), toURIs(other.bootClassPath));
    }
    // Factory methods ---------------------------------------------------------


    /** Creates new interface to the compiler
     * @param file for which the CompilerInterface should be created
     * @return ClasspathInfo or null if the file does not exist on the
     * local file system or it has no classpath associated
     */
    public static @NullUnknown ClasspathInfo create (@NonNull final File file) {
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

    /**
     * Creates a new instance of the ClasspathInfo for given Document.
     * <div class="nonnormative">
     * <p>
     * It uses the {@link Document#StreamDescriptionProperty} to obtain the
     * {@link DataObject} for the {@link Document} and creates a {@link ClasspathInfo}
     * for the primary file of the {@link DataObject}
     * </p>
     * </div>
     * @param doc a document for which the {@link ClasspathInfo} should be created
     * @return a {@link ClasspathInfo} or null when the document source cannot be
     * found.
     * @since 0.42
     */
    public static @NullUnknown ClasspathInfo create(@NonNull final Document doc) {
        Parameters.notNull("doc", doc);
        final Object source = doc.getProperty(Document.StreamDescriptionProperty);
        if (source instanceof DataObject) {
            DataObject dObj = (DataObject) source;
            return create(dObj.getPrimaryFile());
        } else {
            final String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
            if ("text/x-dialog-binding".equals(mimeType)) { //NOI18N
                InputAttributes attributes = (InputAttributes) doc.getProperty(InputAttributes.class);
                LanguagePath path = LanguagePath.get(MimeLookup.getLookup(mimeType).lookup(Language.class));
                Document d = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
                if (d != null) {
                    Object obj = d.getProperty(Document.StreamDescriptionProperty);
                    if (obj instanceof DataObject) {
                        DataObject dObj = (DataObject) obj;
                        return create(dObj.getPrimaryFile());
                    }
                }
                FileObject fileObject = (FileObject) attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
                if (fileObject != null)
                    return create(fileObject);
            }
            return null;
        }
    }

    private static ClasspathInfo create (final @NonNull FileObject fo,
            final JavaFileFilterImplementation filter,
            final boolean backgroundCompilation,
            final boolean ignoreExcludes,
            final boolean hasMemoryFileManager,
            final boolean useModifiedFiles) {
        ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
        if (bootPath == null) {
            //javac requires at least java.lang
            bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
        if (compilePath == null) {
            compilePath = EMPTY_PATH;
        }
        ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (srcPath == null) {
            srcPath = EMPTY_PATH;
        }
        return create (bootPath, compilePath, srcPath, filter, backgroundCompilation, ignoreExcludes, hasMemoryFileManager, useModifiedFiles);
    }
        
    /** Creates new interface to the compiler
     * @param fo for which the CompilerInterface should be created
     */
    public static @NonNull ClasspathInfo create(@NonNull FileObject fo) {
        return create (fo, null, false, false, false, true);
    }
    
    private static ClasspathInfo create(final @NonNull ClassPath bootPath,
            final @NonNull ClassPath classPath,
            final ClassPath sourcePath,
            final JavaFileFilterImplementation filter,
            final boolean backgroundCompilation,
            final boolean ignoreExcludes,
            final boolean hasMemoryFileManager,
            final boolean useModifiedFiles) {
        return new ClasspathInfo(CachingArchiveProvider.getDefault(), bootPath, classPath, sourcePath,
                filter, backgroundCompilation, ignoreExcludes, hasMemoryFileManager, useModifiedFiles);
    }
    
    public static @NonNull ClasspathInfo create(@NonNull final ClassPath bootPath, @NonNull final ClassPath classPath, @NullAllowed final ClassPath sourcePath) {
        Parameters.notNull("bootPath", bootPath);       //NOI18N
        Parameters.notNull("classPath", classPath);     //NOI18N
        return create (bootPath, classPath, sourcePath, null, false, false, false, true);
    }
       
    // Public methods ----------------------------------------------------------
       
    /** Registers ChangeListener which will be notified about the changes in the classpath.
     * @param listener The listener to register.
     */
    public void addChangeListener(@NonNull final ChangeListener listener) {
        listenerList.addChangeListener(listener);
    }

    /**Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(@NonNull final ChangeListener listener) {
        listenerList.removeChangeListener(listener);
    }

    public ClassPath getClassPath (@NonNull PathKind pathKind) {
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
		return this.cachedSrcClassPath;
	    case OUTPUT:
		return this.outputClassPath;
	    default:
		assert false : "Unknown path type";     //NOI18N
		return null;
	}
    }
    
    
    public synchronized @NonNull ClassIndex getClassIndex () {
        if ( usagesQuery == null ) {
            usagesQuery = new ClassIndex (
                    this.bootClassPath,
                    this.compileClassPath,
                    this.cachedSrcClassPath);
        }
        return usagesQuery;
    }
    
    // Package private methods -------------------------------------------------

    @NonNull
    private synchronized JavaFileManager createFileManager() {
            final boolean hasSources = this.cachedSrcClassPath != EMPTY_PATH;
            final SiblingSource siblings = SiblingSupport.create();
            final JavaFileManager fileManager = new ProxyFileManager (
                new CachingFileManager (this.archiveProvider, this.cachedBootClassPath, true, true),
                new CachingFileManager (this.archiveProvider, this.cachedCompileClassPath, false, true),
                hasSources ? (!useModifiedFiles ? new CachingFileManager (this.archiveProvider, this.cachedSrcClassPath, filter, false, ignoreExcludes)
                    : new SourceFileManager (this.cachedSrcClassPath, ignoreExcludes)) : null,
                cachedAptSrcClassPath != null ? 
                    new AptSourceFileManager(
                            this.cachedSrcClassPath, 
                            this.cachedAptSrcClassPath, 
                            siblings.getProvider(),
                            fmTx
                    ) : null,
                hasSources ? 
                    new OutputFileManager(
                            this.archiveProvider, 
                            this.outputClassPath, 
                            this.cachedSrcClassPath, 
                            this.cachedAptSrcClassPath, 
                            siblings.getProvider(), 
                            fmTx) : null,
                this.memoryFileManager,
                this.pgTx,
                siblings);
        return fileManager;
    }
    
    
    // Private methods ---------------------------------------------------------
    
    private void fireChangeListenerStateChanged() {
        listenerList.fireChange();
    }

    @CheckForNull
    private static URI[] toURIs(@NullAllowed final ClassPath cp) {
        if (cp == null) {
            return null;
        }
        final List<ClassPath.Entry> entries = cp.entries();
        final List<URI> roots = new ArrayList<>(entries.size());
        for (ClassPath.Entry entry : entries) {
            try {
                roots.add(entry.getURL().toURI());
            } catch (URISyntaxException ex) {
                log.log(
                    Level.INFO,
                    "Cannot convert {0} to URI.",   //NOI18N
                    entry.getURL());
            }
        }
        return roots.toArray(new URI[roots.size()]);
    }


    // Innerclasses ------------------------------------------------------------


    public static enum PathKind {	
	BOOT,	
	COMPILE,	
	SOURCE,	
	OUTPUT,
	
    }
    
    private class ClassPathListener implements PropertyChangeListener {			
        @Override
        public void propertyChange (PropertyChangeEvent event) {
            if (ClassPath.PROP_ROOTS.equals(event.getPropertyName())) {                
                fireChangeListenerStateChanged();
            }
        }
    }
    
    private static class ClasspathInfoAccessorImpl extends ClasspathInfoAccessor {
        
        @Override
        @NonNull
        public JavaFileManager createFileManager(@NonNull final ClasspathInfo cpInfo) {
            return cpInfo.createFileManager();
        }
        
        @Override
        @NonNull
        public FileManagerTransaction getFileManagerTransaction(@NonNull ClasspathInfo cpInfo) {
            return cpInfo.fmTx;
        }

        @Override
        public ClassPath getCachedClassPath(final ClasspathInfo cpInfo, final PathKind kind) {
            return cpInfo.getCachedClassPath(kind);
        }                
        
        @Override
        public ClasspathInfo create (final ClassPath bootPath,
                final ClassPath classPath,
                final ClassPath sourcePath,
                final JavaFileFilterImplementation filter,
                final boolean backgroundCompilation,
                final boolean ignoreExcludes,
                final boolean hasMemoryFileManager,
                final boolean useModifiedFiles) {
            return ClasspathInfo.create(bootPath, classPath, sourcePath, filter, backgroundCompilation, ignoreExcludes, hasMemoryFileManager, useModifiedFiles);
        }

        @Override
        public ClasspathInfo create (final @NonNull FileObject fo,
                final JavaFileFilterImplementation filter,
                final boolean backgroundCompilation,
                final boolean ignoreExcludes,
                final boolean hasMemoryFileManager,
                final boolean useModifiedFiles) {
            return ClasspathInfo.create(fo, filter, backgroundCompilation, ignoreExcludes, hasMemoryFileManager, useModifiedFiles);
        }
                
        @Override
        public boolean registerVirtualSource(final ClasspathInfo cpInfo, final InferableJavaFileObject jfo) throws UnsupportedOperationException {
            if (cpInfo.memoryFileManager == null) {
                throw new UnsupportedOperationException ("The ClassPathInfo doesn't support memory JavacFileManager");  //NOI18N
            }
            return cpInfo.memoryFileManager.register(jfo);
        }

        @Override
        public boolean unregisterVirtualSource(final ClasspathInfo cpInfo, final String fqn) throws UnsupportedOperationException {
            if (cpInfo.memoryFileManager == null) {
                throw new UnsupportedOperationException();
            }
            return cpInfo.memoryFileManager.unregister(fqn);
        }
    }
}