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

package org.netbeans.modules.gsfret.source.usages;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.modules.gsfpath.api.queries.SourceForBinaryQuery;
import org.netbeans.modules.gsf.Language;
import org.netbeans.napi.gsfret.source.ClassIndex;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.gsfret.source.SourceAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class PersistentClassIndex extends ClassIndexImpl {    
    
    private final Index index;
    private final URL root;
    private final boolean isSource;
    private WeakReference<Source> dirty;
    private static final Logger LOGGER = Logger.getLogger(PersistentClassIndex.class.getName());
    
    /** Creates a new instance of ClassesAndMembersUQ */
    private PersistentClassIndex(final Language language, final URL root, final File cacheRoot, final boolean source) 
        throws IOException, IllegalArgumentException {
        assert root != null;
        this.root = root;
        this.index = LuceneIndex.create (language, cacheRoot, this);
        this.isSource = source;
    // BEGIN TOR MODIFICATIONS
        this.language = language;
        this.cacheRoot = cacheRoot;
    // END TOR MODIFICATIONS
    }
    
//    public BinaryAnalyser getBinaryAnalyser () {
//        return new BinaryAnalyser (this.index);
//    }
    
    public SourceAnalyser getSourceAnalyser () {        
        return new SourceAnalyser (this.index);        
    }
    
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
    

    // Factory method
    
    public static ClassIndexImpl create(Language language, URL root, final File cacheRoot, final boolean indexNow) 
        throws IOException, IllegalArgumentException {        
        return new PersistentClassIndex(language, root, cacheRoot, indexNow);
    }
    
    public synchronized void setDirty (final Source js) {        
        if (js == null) {
            this.dirty = null;
        }
        else if (this.dirty == null || this.dirty.get() != js) {
            this.dirty = new WeakReference (js);
        }
    }
    
    public @Override String toString () {
        return "CompromiseUQ["+this.root.toExternalForm()+"]";     // NOI18N
    }
    
    //Protected methods --------------------------------------------------------
    protected final void close () throws IOException {
        this.index.close();
    }
    
    
    // Private methods ---------------------------------------------------------                          

    private Void runIndexers(final CompilationInfo info) throws IOException {
        Indexer indexer = language.getIndexer();
        if (indexer == null) {
            return null;
        }
        final SourceAnalyser sa = getSourceAnalyser();
        long st = System.currentTimeMillis();
        String mimeType = language.getMimeType();
        
        for (ParserResult result: info.getEmbeddedResults(mimeType)) {
            assert result != null;

            if (result.isValid()) {
                sa.analyseUnitAndStore(indexer, result);
            }
        }
        
        long et = System.currentTimeMillis();
        return null;
    }
    
    private void updateDirty () {
        WeakReference<Source> jsRef;        
        synchronized (this) {
            jsRef = this.dirty;
        }
        if (jsRef != null) {
            final Source js = jsRef.get();
            if (js != null) {
                final long startTime = System.currentTimeMillis();
                if (SourceAccessor.getINSTANCE().isDispatchThread()) {
                    //Already under javac's lock
                    try {
                        ClassIndexManager.writeLock(
                            new ClassIndexManager.ExceptionAction<Void>() {
                                public Void run () throws IOException {
                                    CompilationInfo compilationInfo = SourceAccessor.getINSTANCE().getCurrentCompilationInfo (js, Phase.RESOLVED);
                                    if (compilationInfo != null) {
                                        //Not cancelled
                                        return runIndexers(compilationInfo);
                                    }
                                    return null;
                                }
                        });                                        
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
                else {
                    try {
                        js.runUserActionTask(new CancellableTask<CompilationController>() {
                            public void run (final CompilationController controller) {
                                try {                            
                                    ClassIndexManager.writeLock(
                                        new ClassIndexManager.ExceptionAction<Void>() {
                                            public Void run () throws IOException {
                                                controller.toPhase(Phase.RESOLVED);
                                                return runIndexers(controller);
                                            }
                                    });
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                }
                            }

                            public void cancel () {}
                        }, true);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
                synchronized (this) {
                    this.dirty = null;
                }
                final long endTime = System.currentTimeMillis();
                LOGGER.fine("PersistentClassIndex.updateDirty took: " + (endTime-startTime)+ " ms");     //NOI18N
            }
        }
    }
        
    // BEGIN TOR MODIFICATIONS
    public void search(final String primaryField, final String name, final NameKind kind, 
            final Set<ClassIndex.SearchScope> scope, final Set<SearchResult> result, final Set<String> terms) throws IOException {
        updateDirty();
        ClassIndexManager.readLock(new ClassIndexManager.ExceptionAction<Void> () {
            public Void run () throws IOException {
                index.search(primaryField, name, kind, scope, result, terms);
                return null;
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public Map<String,String> getTimeStamps() throws IOException {
        //return this.index.getTimeStamps();
        final Map[] mapHolder = new Map[1];
        ClassIndexManager.readLock(new ClassIndexManager.ExceptionAction<Void> () {
            public Void run () throws IOException {
                mapHolder[0] = index.getTimeStamps();
                return null;
            }
        });

        return (Map<String,String>)mapHolder[0];
    }
    
    /** For development purposes only */
    public File getSegment() {
        return cacheRoot;
    }
    
    private File cacheRoot;
    private Language language;
    
    public URL getRoot() {
        return root;
    }
    
    // For the symbol dumper only
    public org.apache.lucene.index.IndexReader getDumpIndexReader() throws IOException {
        if (index instanceof LuceneIndex) {
            try {
                return ((LuceneIndex)index).getDumpIndexReader();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        
        return null;
    }

    @Override
    public void storeEmpty() {
        List<IndexDocument> list = Collections.emptyList();
        try {
            this.index.store(null, list);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
// END TOR MODIFICATIONS
    
}
