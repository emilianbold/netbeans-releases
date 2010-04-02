/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.api.java.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.usages.ClassIndexFactory;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexImplEvent;
import org.netbeans.modules.java.source.usages.ClassIndexImplListener;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClassIndexManagerEvent;
import org.netbeans.modules.java.source.usages.ClassIndexManagerListener;
import org.netbeans.modules.java.source.usages.ResultConvertor;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 * The ClassIndex provides access to information stored in the 
 * persistent index. It can be used to obtain list of packages
 * or declared types. It can be also used to obtain a list of
 * source files referencing given type (usages of given type).
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public final class ClassIndex {
    
    private static final Logger LOGGER = Logger.getLogger(ClassIndex.class.getName());
    
    //INV: Never null
    private final ClassPath bootPath;
    //INV: Never null
    private final ClassPath classPath;
    //INV: Never null
    private final ClassPath sourcePath;

    //INV: Never null
    //@GuardedBy (this)
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}/*,justification="URLs have never host part"*/)    //NOI18N
    private final Set<URL> oldSources;
    //INV: Never null
    //@GuardedBy (this)
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}/*,justification="URLs have never host part"*/)    //NOI18N
    private final Set<URL> oldBoot;    
    //INV: Never null
    //@GuardedBy (this)
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}/*,justification="URLs have never host part"*/)    //NOI18N
    private final Set<URL> oldCompile;
    //INV: Never null
    //@GuardedBy (this)
    private final Set<ClassIndexImpl> sourceIndeces;
    //INV: Never null
    //@GuardedBy (this)
    private final Set<ClassIndexImpl> depsIndeces;

    private final Collection<ClassIndexListener> listeners = new ConcurrentLinkedQueue<ClassIndexListener>();
    private final SPIListener spiListener = new SPIListener ();

    /**
     * Encodes a type of the name kind used by 
     * {@link ClassIndex#getDeclaredTypes} method.
     *
     */
    public enum NameKind {
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes}
         * is an exact simple name of the package or declared type.
         */
        SIMPLE_NAME,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} 
         * is an case sensitive prefix of the package or declared type name.
         */
        PREFIX,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an case insensitive prefix of the declared type name.
         */
        CASE_INSENSITIVE_PREFIX,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an camel case of the declared type name.
         */
        CAMEL_CASE,
        
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an regular expression of the declared type name.
         */
        REGEXP,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an case insensitive regular expression of the declared type name.
         */
        CASE_INSENSITIVE_REGEXP,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * a camel case or case insensitive prefix of the declared type name.
         * For example all these names NPE, NulPoEx, NULLPOInter leads to NullPointerException returned.
         * @since 0.28.0
         */
        CAMEL_CASE_INSENSITIVE
    };
    
    
    /**
     * Encodes a reference type,
     * used by {@link ClassIndex#getElements} and {@link ClassIndex#getResources}
     * to restrict the search.
     */
    public enum SearchKind {
        
        /**
         * The returned class has to extend or implement given element
         */
        IMPLEMENTORS,
        
        /**
         * The returned class has to call method on given element
         */
        METHOD_REFERENCES,
        
        /**
         * The returned class has to access a field on given element
         */
        FIELD_REFERENCES,
        
        /**
         * The returned class contains references to the element type
         */
        TYPE_REFERENCES,        
    };
    
    /**
     * Scope used by {@link ClassIndex} to search in
     */
    public enum SearchScope {
        /**
         * Search is done in source path
         */
        SOURCE,
        /**
         * Search is done in compile and boot path
         */
        DEPENDENCIES
    };
    
    static {
	ClassIndexImpl.FACTORY = new ClassIndexFactoryImpl();
    }    
    
    ClassIndex(final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {
        assert bootPath != null;
        assert classPath != null;
        assert sourcePath != null;
        this.bootPath = bootPath;
        this.classPath = classPath;
        this.sourcePath = sourcePath;
        this.oldBoot = new HashSet<URL>();
        this.oldCompile = new  HashSet<URL>();
        this.oldSources = new HashSet<URL>();
        this.depsIndeces = new HashSet<ClassIndexImpl>();
        this.sourceIndeces = new HashSet<ClassIndexImpl>();
        
        final ClassIndexManager manager = ClassIndexManager.getDefault();
        manager.addClassIndexManagerListener(WeakListeners.create(ClassIndexManagerListener.class, (ClassIndexManagerListener) this.spiListener, manager));
        this.bootPath.addPropertyChangeListener(WeakListeners.propertyChange(spiListener, this.bootPath));
        this.classPath.addPropertyChangeListener(WeakListeners.propertyChange(spiListener, this.classPath));
        this.sourcePath.addPropertyChangeListener(WeakListeners.propertyChange(spiListener, this.sourcePath));                
        reset (true, true);	    
    }
    
    
    /**
     * Adds an {@link ClassIndexListener}. The listener is notified about the
     * changes of declared types in this {@link ClassIndex}
     * @param listener to be added
     */
    public void addClassIndexListener (final @NonNull ClassIndexListener listener) {
        assert listener != null;
        listeners.add (listener);
    }
    
    /**
     * Removes an {@link ClassIndexListener}. The listener is notified about the
     * changes of declared types in this {@link ClassIndex}
     * @param listener to be removed
     */
    public void removeClassIndexListener (final @NonNull ClassIndexListener listener) {
        assert listener != null;
        listeners.remove(listener);
    }
    
    
    /**
     * Returns a set of {@link ElementHandle}s containing reference(s) to given element.
     * @param element for which usages should be found
     * @param searchKind type of reference, {@see SearchKind}
     * @param scope to search in {@see SearchScope}
     * @return set of {@link ElementHandle}s containing the reference(s)
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<ElementHandle<TypeElement>> getElements (final @NonNull ElementHandle<TypeElement> element, final @NonNull Set<SearchKind> searchKind, final @NonNull Set<SearchScope> scope) {
        assert element != null;
        assert element.getSignature()[0] != null;
        assert searchKind != null;
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>> ();
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
        final Set<ClassIndexImpl.UsageType> ut =  encodeSearchKind(element.getKind(),searchKind);
        final String binaryName = element.getSignature()[0];
        final ResultConvertor<ElementHandle<TypeElement>> thConvertor = ResultConvertor.elementHandleConvertor();
        try {
            if (!ut.isEmpty()) {
                for (ClassIndexImpl query : queries) {
                    try {
                        query.search(binaryName, ut, thConvertor, result);
                    } catch (ClassIndexImpl.IndexAlreadyClosedException e) {
                        logClosedIndex (query);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
            return Collections.unmodifiableSet(result);
        } catch (InterruptedException e) {
            return null;
        }
    }
    
    /**
     * Returns a set of source files containing reference(s) to given element.
     * @param element for which usages should be found
     * @param searchKind type of reference, {@see SearchKind}
     * @param scope to search in {@see SearchScope}
     * @return set of {@link FileObject}s containing the reference(s)
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<FileObject> getResources (final @NonNull ElementHandle<TypeElement> element, final @NonNull Set<SearchKind> searchKind, final @NonNull Set<SearchScope> scope) {
        assert element != null;
        assert element.getSignature()[0] != null;
        assert searchKind != null;
        final Set<FileObject> result = new HashSet<FileObject> ();
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
        final Set<ClassIndexImpl.UsageType> ut =  encodeSearchKind(element.getKind(),searchKind);
        final String binaryName = element.getSignature()[0];        
        try {
            if (!ut.isEmpty()) {
                for (ClassIndexImpl query : queries) {
                    final ResultConvertor<FileObject> foConvertor = ResultConvertor.fileObjectConvertor (query.getSourceRoots());
                    try {
                        query.search (binaryName, ut, foConvertor, result);
                    } catch (ClassIndexImpl.IndexAlreadyClosedException e) {
                        logClosedIndex (query);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
            return Collections.unmodifiableSet(result);
        } catch (InterruptedException e) {
            return null;
        }
    }        
    
    
    /**
     * Returns {@link ElementHandle}s for all declared types in given classpath corresponding to the name.
     * @param name case sensitive prefix, case insensitive prefix, exact simple name,
     * camel case or regular expression depending on the kind parameter.
     * @param kind of the name {@see NameKind}
     * @param scope to search in {@see SearchScope}
     * @return set of all matched declared types
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<ElementHandle<TypeElement>> getDeclaredTypes (final @NonNull String name, final @NonNull NameKind kind, final @NonNull Set<SearchScope> scope) {
        assert name != null;
        assert kind != null;
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();        
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);        
        final ResultConvertor<ElementHandle<TypeElement>> thConvertor = ResultConvertor.elementHandleConvertor();
        try {
            for (ClassIndexImpl query : queries) {
                try {
                    query.getDeclaredTypes (name, kind, thConvertor, result);
                } catch (ClassIndexImpl.IndexAlreadyClosedException e) {
                    logClosedIndex (query);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            LOGGER.fine(String.format("ClassIndex.getDeclaredTypes returned %d elements\n", result.size()));
            return Collections.unmodifiableSet(result);
        } catch (InterruptedException e) {
            return null;
        }
    }
    
    /**
     * Returns names af all packages in given classpath starting with prefix.
     * @param prefix of the package name
     * @param directOnly if true treats the packages as folders and returns only
     * the nearest component of the package.
     * @param scope to search in {@see SearchScope}
     * @return set of all matched package names
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<String> getPackageNames (final @NonNull String prefix, boolean directOnly, final @NonNull Set<SearchScope> scope) {
        assert prefix != null;
        final Set<String> result = new HashSet<String> ();        
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
        try {
            for (ClassIndexImpl query : queries) {
                try {
                    query.getPackageNames (prefix, directOnly, result);
                } catch (ClassIndexImpl.IndexAlreadyClosedException e) {
                    logClosedIndex (query);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            return Collections.unmodifiableSet(result);
        } catch (InterruptedException e) {
            return null;
        }
    }
    
    // Private innerclasses ----------------------------------------------------
        
    private static class ClassIndexFactoryImpl implements ClassIndexFactory {
        
	public ClassIndex create(final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {            
	    return new ClassIndex(bootPath, classPath, sourcePath);
        }
	
    }
    
    //Private methods
    
    private static void logClosedIndex (final ClassIndexImpl query) {
        assert query != null;
        LOGGER.info("Ignoring closed index: " + query.toString());  //NOI18N
    }
    
    
    private  void reset (final boolean source, final boolean deps) {
        ProjectManager.mutex().readAccess(new Runnable() {

            public void run() {
                synchronized (ClassIndex.this) {
                    if (source) {            
                        for (ClassIndexImpl impl : sourceIndeces) {
                            impl.removeClassIndexImplListener(spiListener);
                        }
                        sourceIndeces.clear();
                        oldSources.clear();
                        createQueriesForRoots (sourcePath, true, sourceIndeces, oldSources);
                    }
                    if (deps) {
                        for (ClassIndexImpl impl : depsIndeces) {
                            impl.removeClassIndexImplListener(spiListener);
                        }
                        depsIndeces.clear();
                        oldBoot.clear();
                        oldCompile.clear();
                        createQueriesForRoots (bootPath, false, depsIndeces,  oldBoot);                
                        createQueriesForRoots (classPath, false, depsIndeces, oldCompile);	    
                    }
                }
            }
        });        
    }
    
    private Iterable<? extends ClassIndexImpl> getQueries (final Set<SearchScope> scope) {
        final Set<ClassIndexImpl> result = new HashSet<ClassIndexImpl> ();
        synchronized (this) {
            if (scope.contains(SearchScope.SOURCE)) {
                result.addAll(this.sourceIndeces);
            }
            if (scope.contains(SearchScope.DEPENDENCIES)) {
                result.addAll(this.depsIndeces);
            }
        }
        LOGGER.fine(String.format("ClassIndex.queries[Scope=%s, sourcePath=%s, bootPath=%s, classPath=%s] => %s\n",scope,sourcePath,bootPath,classPath,result));
        return result;
    }        
    
    private void createQueriesForRoots (final ClassPath cp, final boolean sources, final Set<? super ClassIndexImpl> queries, final Set<? super URL> oldState) {
        final PathRegistry preg = PathRegistry.getDefault();
        List<ClassPath.Entry> entries = cp.entries();
	for (ClassPath.Entry entry : entries) {
            URL[] srcRoots;
            if (!sources) {
                srcRoots = preg.sourceForBinaryQuery(entry.getURL(), cp, true);
                if (srcRoots == null) {
                    srcRoots = new URL[] {entry.getURL()};
                }
            }
            else {
                srcRoots = new URL[] {entry.getURL()};
            }
            for (URL srcRoot : srcRoots) {
                oldState.add (srcRoot);
                ClassIndexImpl ci = ClassIndexManager.getDefault().getUsagesQuery(srcRoot);
                if (ci != null) {
                    ci.addClassIndexImplListener(spiListener);
                    queries.add (ci);
                }
            }
	}
    }
    
    
    private static Set<ClassIndexImpl.UsageType> encodeSearchKind (final ElementKind elementKind, final Set<ClassIndex.SearchKind> kind) {
        assert kind != null;
        final Set<ClassIndexImpl.UsageType> result = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
        for (ClassIndex.SearchKind sk : kind) {
            switch (sk) {
                case METHOD_REFERENCES:                    
                    result.add(ClassIndexImpl.UsageType.METHOD_REFERENCE);                    
                    break;
                case FIELD_REFERENCES:
                    result.add(ClassIndexImpl.UsageType.FIELD_REFERENCE);
                    break;
                case TYPE_REFERENCES:
                    result.add(ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    break;
                case IMPLEMENTORS:
                    switch( elementKind) {
                        case INTERFACE:
                        case ANNOTATION_TYPE:
                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
                            break;
                        case CLASS:
                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
                            break;
                        case ENUM:	//enum is final
                            break;
                        case OTHER:
                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
                            break;
                        default:
                            throw new IllegalArgumentException ();                                        
                    }
                    break;
                default:
                    throw new IllegalArgumentException ();                    
            }
        }
        return result;
    }           
    
    private class SPIListener implements ClassIndexImplListener, ClassIndexManagerListener, PropertyChangeListener {
        
        public void typesAdded (final ClassIndexImplEvent event) {
            assert event != null;
            final Runnable action = new Runnable () {
                public void run() {
                    assertParserEventThread();
                    TypesEvent _event = new TypesEvent (ClassIndex.this,event.getTypes());
                    for (ClassIndexListener l : listeners) {
                        l.typesAdded(_event);
                    }
                }
            };
            fireByWorker(action);
        }
        
        public void typesRemoved (final ClassIndexImplEvent event) {
            assert event != null;
            final Runnable action = new Runnable() {
                public void run() {
                    assertParserEventThread();
                    TypesEvent _event = new TypesEvent (ClassIndex.this,event.getTypes());
                    for (ClassIndexListener l : listeners) {
                        l.typesRemoved(_event);
                    }
                }
            };
            fireByWorker(action);
        }
        
        public void typesChanged (final ClassIndexImplEvent event) {
            assert event != null;
            final Runnable action = new Runnable() {
                public void run() {
                    assertParserEventThread();
                    TypesEvent _event = new TypesEvent (ClassIndex.this,event.getTypes());
                    for (ClassIndexListener l : listeners) {
                        l.typesChanged(_event);
                    }
                }
            };
            fireByWorker(action);
        }        
        
        public void classIndexAdded (final ClassIndexManagerEvent event) {
            assert event != null;
            final Set<? extends URL> roots = event.getRoots();
            assert roots != null;
            final List<URL> ar = new LinkedList<URL>();
            boolean srcF = containsRoot (sourcePath,roots,ar, false);
            boolean depF = containsRoot (bootPath, roots, ar, true);
            depF |= containsRoot (classPath, roots, ar, true);
            if (srcF || depF) {
                reset (srcF, depF);
                final Runnable action = new Runnable() {
                    public void run() {
                        assertParserEventThread();
                        final RootsEvent e = new RootsEvent(ClassIndex.this, ar);
                        for (ClassIndexListener l : listeners) {
                            l.rootsAdded(e);
                        }
                    }
                };
                fireByWorker(action);
            }
        }
        
        public void classIndexRemoved (final ClassIndexManagerEvent event) {
            //Not important handled by propertyChange from ClassPath
        }

        @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}/*,justification="URLs have never host part"*/)    //NOI18N
        private boolean containsRoot (final ClassPath cp, final Set<? extends URL> roots, final List<? super URL> affectedRoots, final boolean translate) {
            final List<ClassPath.Entry> entries = cp.entries();
            final PathRegistry preg = PathRegistry.getDefault();
            boolean result = false;
            for (ClassPath.Entry entry : entries) {
                URL url = entry.getURL();
                URL[] srcRoots = null;
                if (translate) {
                    srcRoots = preg.sourceForBinaryQuery(entry.getURL(), cp, false);
                }                
                if (srcRoots == null) {
                    if (roots.contains(url)) {
                        affectedRoots.add(url);
                        result = true;                    
                    }
                }
                else {
                    for (URL _url : srcRoots) {
                        if (roots.contains(_url)) {
                            affectedRoots.add(url);
                            result = true;                    
                        }
                    }
                }                
            }
            return result;
        }
        
        private boolean containsNewRoot (final ClassPath cp, final Set<? extends URL> roots,
                final List<? super URL> newRoots, final List<? super URL> removedRoots,
                final boolean translate) throws IOException {
            final List<ClassPath.Entry> entries = cp.entries();
            final PathRegistry preg = PathRegistry.getDefault();
            final ClassIndexManager mgr = ClassIndexManager.getDefault();
            boolean result = false;
            for (ClassPath.Entry entry : entries) {
                URL url = entry.getURL();
                URL[] srcRoots = null;
                if (translate) {
                    srcRoots = preg.sourceForBinaryQuery(entry.getURL(), cp, false);
                }                
                if (srcRoots == null) {
                    if (!roots.remove(url) && mgr.getUsagesQuery(url)!=null) {
                        newRoots.add (url);
                        result = true;
                    }
                }
                else {
                    for (URL _url : srcRoots) {
                        if (!roots.remove(_url) && mgr.getUsagesQuery(_url)!=null) {
                            newRoots.add (_url);
                            result = true;
                        }
                    }
                }
            }
            result |= !roots.isEmpty();
            Collection<? super URL> c = removedRoots;
            c.addAll(roots);
            return result;
        }                

        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ENTRIES.equals (evt.getPropertyName())) {
                final List<URL> newRoots = new LinkedList<URL>();
                final List<URL> removedRoots = new  LinkedList<URL> ();
                boolean dirtySource = false;
                boolean dirtyDeps = false;
                try {
                    Object source = evt.getSource();                
                    if (source == ClassIndex.this.sourcePath) {
                        Set<URL> copy;
                        synchronized (ClassIndex.this) {
                            copy = new HashSet<URL>(oldSources);
                        }
                        dirtySource = containsNewRoot(sourcePath, copy, newRoots, removedRoots, false);                        
                    }                
                    else if (source == ClassIndex.this.classPath) {
                        Set<URL> copy;
                        synchronized (ClassIndex.this) {
                            copy = new HashSet<URL>(oldCompile);
                        }
                        dirtyDeps = containsNewRoot(classPath, copy, newRoots, removedRoots, true);                        
                    }
                    else if (source == ClassIndex.this.bootPath) {
                        Set<URL> copy;
                        synchronized (ClassIndex.this) {
                            copy = new HashSet<URL>(oldBoot);
                        }
                        dirtyDeps = containsNewRoot(bootPath, copy, newRoots, removedRoots, true);
                    }
                    
                    if (dirtySource || dirtyDeps) {                        
                        ClassIndex.this.reset(dirtySource, dirtyDeps);
                        final RootsEvent ae = newRoots.isEmpty() ? null : new RootsEvent(ClassIndex.this, newRoots);
                        final RootsEvent re = removedRoots.isEmpty() ? null : new RootsEvent(ClassIndex.this, removedRoots);
                        //Threading warning:
                        //The Javadoc promises that events are fired under javac lock,
                        //reschedule firing to the Java Worker Thread which runs under javac lock,
                        //trying to access javac lock in this thread may cause deadlock with Java Worker Thread
                        //because the classpath events are fired under the project mutex and it's legal to
                        //aquire project mutex in the CancellableTask.run()
                        JavaSourceAccessor.getINSTANCE().runSpecialTask(new Mutex.ExceptionAction<Void>() {
                            
                            public Void run() {
                                assertParserEventThread();
                                if (ae != null) {
                                    for (ClassIndexListener l : listeners) {
                                        l.rootsAdded(ae);
                                    }                        
                                }
                                if (re != null) {
                                    for (ClassIndexListener l : listeners) {
                                        l.rootsRemoved(re);
                                    }
                                }
                                return null;
                            }                            
                        }, JavaSource.Priority.MAX);                        
                    }                    
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }

    private void fireByWorker (final Runnable action) {
        assert action != null;
        if (Utilities.isTaskProcessorThread(Thread.currentThread())) {
            action.run();
        }
        else {
            Utilities.scheduleSpecialTask(
                new ParserResultTask() {
                    @Override
                    public int getPriority() {
                        return 0;
                    }

                    @Override
                    public Class<? extends Scheduler> getSchedulerClass() {
                        return null;
                    }

                    @Override
                    public void cancel() {
                        //Firing not cancallable
                    }

                    @Override
                    public void run(Result _null, SchedulerEvent event) {
                        action.run();
                    }

                });
        }
    }

    private static void assertParserEventThread() {
        assert Utilities.isTaskProcessorThread(Thread.currentThread());
    }
}
