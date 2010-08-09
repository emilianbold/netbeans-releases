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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class PersistentClassIndex extends ClassIndexImpl {
    
    private final Index index;
    private final URL root;
    private final File cacheRoot;
    private final boolean isSource;
    private volatile URL dirty;
    //@GuardedBy("this")
    private Set<String> rootPkgCache;
    private static final Logger LOGGER = Logger.getLogger(PersistentClassIndex.class.getName());
    private static IndexFactory indexFactory = LuceneIndexFactory.getInstance();
    
    /** Creates a new instance of ClassesAndMembersUQ */
    private PersistentClassIndex(final URL root, final File cacheRoot, final boolean source) 
	    throws IOException, IllegalArgumentException {
        assert root != null;
        this.root = root;
        assert indexFactory != null;
        this.cacheRoot = cacheRoot;
        this.index = indexFactory.create(cacheRoot);
        this.isSource = source;
    }
    
    @Override
    public BinaryAnalyser getBinaryAnalyser () {
        return new BinaryAnalyser (new PIWriter(), this.cacheRoot);
    }
    
    @Override
    public SourceAnalyser getSourceAnalyser () {        
        return new SourceAnalyser (new PIWriter());        
    }

    @Override
    public boolean isSource () {
        return this.isSource;
    }

    @Override
    public boolean isEmpty () {
        try {
            return ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException, InterruptedException {
                    return !PersistentClassIndex.this.index.exists();
                }
            }).booleanValue();
        } catch (InterruptedException ie) {
            //Not thrown but declared
            return false;
        } catch (IOException ioe) {
            //Not thrown but declared
            return false;
        }
    }
    
    @Override
    public boolean isValid() {
        try {
            return index.isValid(true);
        } catch (IOException ex) {
            return false;
        }
    }
    
    @Override
    public FileObject[] getSourceRoots () {
        FileObject[] rootFos;
        if (isSource) {
            FileObject rootFo = URLMapper.findFileObject (this.root);
            rootFos = rootFo == null ? new FileObject[0]  : new FileObject[] {rootFo};
        }
        else {
            rootFos = SourceForBinaryQuery.findSourceRoots(this.root).getRoots();
        }
        return rootFos;
    }
    
    @Override
    public String getSourceName (final String binaryName) throws IOException, InterruptedException {
        final Query q = DocumentUtil.binaryNameQuery(binaryName);        
        Set<String> names = new HashSet<String>();
        index.query(new Query[]{q}, DocumentUtil.sourceNameFieldSelector(), DocumentUtil.sourceNameConvertor(), names);
        return names.isEmpty() ? null : names.iterator().next();
    }
    

    // Factory method
    
    public static ClassIndexImpl create(URL root, final File cacheRoot, final boolean indexNow) 
	    throws IOException, IllegalArgumentException {        
        return new PersistentClassIndex(root, cacheRoot, indexNow);
    }
    
    // Implementation of UsagesQueryImpl ---------------------------------------    
    @Override
    public <T> void search (final String binaryName, final Set<UsageType> usageType, final ResultConvertor<? super Document, T> convertor, final Set<? super T> result) throws InterruptedException, IOException {
        updateDirty();
        if (BinaryAnalyser.OBJECT.equals(binaryName)) {
            this.getDeclaredTypes("", ClassIndex.NameKind.PREFIX, convertor, result);
            return;
        }
        
        ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void> () {
            public Void run () throws IOException, InterruptedException {
                usages(binaryName, usageType, convertor, result);
                return null;
            }
        });        
    }
    
                       
    @Override
    public <T> void getDeclaredTypes (final String simpleName, final ClassIndex.NameKind kind, final ResultConvertor<? super Document, T> convertor, final Set<? super T> result) throws InterruptedException, IOException {
        updateDirty();
        ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void> () {
            @Override
            public Void run () throws IOException, InterruptedException {
                final Query[] queries =  QueryUtil.createQueries(
                        Pair.of(DocumentUtil.FIELD_SIMPLE_NAME,DocumentUtil.FIELD_CASE_INSENSITIVE_NAME),
                        simpleName,
                        kind);
                index.query(queries,DocumentUtil.declaredTypesFieldSelector(),convertor, result);
                return null;
            }
        });
    }
    
    @Override
    public <T> void getDeclaredElements (final String ident, final ClassIndex.NameKind kind, final ResultConvertor<? super Document, T> convertor, final Map<T,Set<String>> result) throws InterruptedException, IOException {
        updateDirty();
        ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run () throws IOException, InterruptedException {
                index.getDeclaredElements(ident, kind, convertor, result);
                return null;
            }
        });                            
    }
    
    
    @Override
    public void getPackageNames (final String prefix, final boolean directOnly, final Set<String> result) throws InterruptedException, IOException {
        ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void>() {
            @Override
            public Void run () throws IOException, InterruptedException {
                final boolean cacheOp = directOnly && prefix.length() == 0;
                Set<String> myPkgs = null;
                Collection<String> collectInto;
                if (cacheOp) {
                    synchronized (PersistentClassIndex.this) {
                        if (rootPkgCache != null) {
                            result.addAll(rootPkgCache);
                            return null;
                        }
                    }
                    myPkgs = new HashSet<String>();
                    collectInto = new TeeCollection(myPkgs,result);
                } else {
                    collectInto = result;
                }
                final Pair<ResultConvertor<Term,String>,Term> filter = QueryUtil.createPackageFilter(prefix,directOnly);
                index.queryTerms(filter.second, filter.first, collectInto);
                if (cacheOp) {
                    synchronized (PersistentClassIndex.this) {
                        if (rootPkgCache == null) {
                            assert myPkgs != null;
                            rootPkgCache = myPkgs;
                        }
                    }
                }
                return null;
            }
        });        
    }
        
    @Override
    public void setDirty (final URL url) {
        this.dirty = url;
    }
    
    public @Override String toString () {
        return "PersistentClassIndex["+this.root.toExternalForm()+"]";     // NOI18N
    }
    
    //Unit test methods
    public static void setIndexFactory (final IndexFactory factory) {
        indexFactory = (factory == null ? LuceneIndexFactory.getInstance() : factory);
    }
    
    //Protected methods --------------------------------------------------------
    @Override
    protected final void close () throws IOException {
        this.index.close();
    }
    
        
    // Private methods ---------------------------------------------------------                          
    
    private void updateDirty () {
        final URL url = this.dirty;
        if (url != null) {
            final FileObject file = url != null ? URLMapper.findFileObject(url) : null;
            final JavaSource js = file != null ? JavaSource.forFileObject(file) : null;
            if (js != null) {
                final long startTime = System.currentTimeMillis();
                Iterator<FileObject> files = js.getFileObjects().iterator();
                FileObject fo = files.hasNext() ? files.next() : null;
                if (fo != null && fo.isValid()) {                                        
                    try {
                        js.runUserActionTask(new Task<CompilationController>() {
                            public void run (final CompilationController controller) {
                                try {                            
                                    ClassIndexManager.getDefault().takeWriteLock(
                                        new ClassIndexManager.ExceptionAction<Void>() {
                                            public Void run () throws IOException {
                                                if (controller.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED)<0) {
                                                    return null;
                                                }
                                                try {
                                                    final SourceAnalyser sa = getSourceAnalyser();
                                                    sa.analyseUnitAndStore(controller.getCompilationUnit(), JavaSourceAccessor.getINSTANCE().getJavacTask(controller),
                                                    ClasspathInfoAccessor.getINSTANCE().getFileManager(controller.getClasspathInfo()));
                                                } catch (IllegalArgumentException ia) {
                                                    //Debug info for issue #187344
                                                    //seems that invalid dirty class index is used
                                                    final ClassPath scp = controller.getClasspathInfo().getClassPath(PathKind.SOURCE);
                                                    throw new IllegalArgumentException(
                                                            String.format("Provided source path: %s root: %s cache root: %",    //NOI18N
                                                                scp == null ? "<null>" : scp.toString(),    //NOI18N
                                                                root.toExternalForm(),
                                                                cacheRoot.toURI().toURL())
                                                                ,ia);
                                                }
                                                return null;
                                            }
                                    });
                                } catch (IndexAlreadyClosedException e) {
                                    //A try to  store to closed index, safe to ignore.
                                    //Data will be scanned when project is reopened.
                                   LOGGER.info("Ignoring store into closed index");
                                } catch (IOException ioe) {
                                   Exceptions.printStackTrace(ioe);
                                }
                                catch (InterruptedException e) {
                                   //Should never happen
                                   Exceptions.printStackTrace(e);
                                }
                            }
                        }, true);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
                this.dirty = null;
                final long endTime = System.currentTimeMillis();
                LOGGER.fine("PersistentClassIndex.updateDirty took: " + (endTime-startTime)+ " ms");     //NOI18N
            }
        }
    }
    
    private <T> void usages (final String binaryName, final Set<UsageType> usageType, ResultConvertor<? super Document, T> convertor, Set<? super T> result) throws InterruptedException, IOException {
        final Query usagesQuery = QueryUtil.createUsagesQuery(binaryName, usageType, Occur.SHOULD);
        this.index.query(new Query[]{usagesQuery},DocumentUtil.declaredTypesFieldSelector(),convertor,result);                
    }
    
    private synchronized void resetPkgCache() {        
        rootPkgCache = null;
    }
    
    private class PIWriter implements Writer {
        @Override
        public void clear() throws IOException {
            resetPkgCache();
            index.clear();
        }
        @Override
        public void store(Map<Pair<String, String>, Object[]> refs, List<Pair<String, String>> topLevels) throws IOException {
            resetPkgCache();
            index.store(refs, topLevels);
        }
        @Override
        public void store(Map<Pair<String, String>, Object[]> refs, Set<Pair<String, String>> toDelete) throws IOException {
            resetPkgCache();
            index.store(refs, toDelete);
        }
    }
    
    private class TeeCollection<T> extends AbstractCollection<T> {
        
        private Collection<T> primary;
        private Collection<T> secondary;
              
        
        TeeCollection(final @NonNull Collection<T> primary, @NonNull Collection<T> secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @Override
        public Iterator<T> iterator() {
            throw new UnsupportedOperationException("Not supported operation.");    //NOI18N
        }

        @Override
        public int size() {
            return primary.size();
        }

        @Override
        public boolean add(T e) {
            final boolean result = primary.add(e);
            secondary.add(e);
            return result;
        }
    }
}
