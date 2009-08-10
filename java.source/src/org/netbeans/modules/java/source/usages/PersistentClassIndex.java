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

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClassIndex;
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
    private final boolean isSource;
    private WeakReference<JavaSource> dirty;
    private static final Logger LOGGER = Logger.getLogger(PersistentClassIndex.class.getName());
    private static IndexFactory indexFactory = LuceneIndexFactory.getInstance();
    
    /** Creates a new instance of ClassesAndMembersUQ */
    private PersistentClassIndex(final URL root, final File cacheRoot, final boolean source) 
	    throws IOException, IllegalArgumentException {
        assert root != null;
        this.root = root;
        assert indexFactory != null;
        this.index = indexFactory.create(cacheRoot);
        this.isSource = source;
    }
    
    public BinaryAnalyser getBinaryAnalyser () {
        return new BinaryAnalyser (this.index);
    }
    
    public SourceAnalyser getSourceAnalyser () {        
        return new SourceAnalyser (this.index);        
    }

    public boolean isSource () {
        return this.isSource;
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
    
    public String getSourceName (final String binaryName) throws IOException {
        return index.getSourceName(binaryName);        
    }
    

    // Factory method
    
    public static ClassIndexImpl create(URL root, final File cacheRoot, final boolean indexNow) 
	    throws IOException, IllegalArgumentException {        
        return new PersistentClassIndex(root, cacheRoot, indexNow);
    }
    
    // Implementation of UsagesQueryImpl ---------------------------------------    
    public <T> void search (final String binaryName, final Set<UsageType> usageType, final ResultConvertor<T> convertor, final Set<? super T> result) throws InterruptedException, IOException {
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
    
    
               
    
    public <T> void getDeclaredTypes (final String simpleName, final ClassIndex.NameKind kind, final ResultConvertor<T> convertor, final Set<? super T> result) throws InterruptedException, IOException {
        updateDirty();
        ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void> () {
            public Void run () throws IOException, InterruptedException {
                index.getDeclaredTypes (simpleName,kind, convertor, result);
                return null;
            }                    
        });
    }
    
    public <T> void getDeclaredElements (final String ident, final ClassIndex.NameKind kind, final ResultConvertor<T> convertor, final Map<T,Set<String>> result) throws InterruptedException, IOException {
        updateDirty();
        ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run () throws IOException, InterruptedException {
                index.getDeclaredElements(ident, kind, convertor, result);
                return null;
            }
        });                            
    }
    
    
    public void getPackageNames (final String prefix, final boolean directOnly, final Set<String> result) throws InterruptedException, IOException {
        ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run () throws IOException, InterruptedException {
                index.getPackageNames(prefix, directOnly, result);
                return null;
            }
        });        
    }
    
    public synchronized void setDirty (final URL url) {
        final FileObject fo = url != null ? URLMapper.findFileObject(url) : null;
        final JavaSource js = fo != null ? JavaSource.forFileObject(fo) : null;
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
    
    //Unit test methods
    public static void setIndexFactory (final IndexFactory factory) {
        indexFactory = (factory == null ? LuceneIndexFactory.getInstance() : factory);
    }
    
    //Protected methods --------------------------------------------------------
    protected final void close () throws IOException {
        this.index.close();
    }
    
    
    // Private methods ---------------------------------------------------------                          
    
    private void updateDirty () {
        WeakReference<JavaSource> jsRef;        
        synchronized (this) {
            jsRef = this.dirty;
        }
        if (jsRef != null) {
            final JavaSource js = jsRef.get();
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
                                                controller.toPhase(Phase.RESOLVED);
                                                final SourceAnalyser sa = getSourceAnalyser();
                                                sa.analyseUnitAndStore(controller.getCompilationUnit(), JavaSourceAccessor.getINSTANCE().getJavacTask(controller),
                                                ClasspathInfoAccessor.getINSTANCE().getFileManager(controller.getClasspathInfo()));
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
                synchronized (this) {
                    this.dirty = null;
                }
                final long endTime = System.currentTimeMillis();
                LOGGER.fine("PersistentClassIndex.updateDirty took: " + (endTime-startTime)+ " ms");     //NOI18N
            }
        }
    }
    
    private <T> void usages (final String binaryName, final Set<UsageType> usageType, ResultConvertor<T> convertor, Set<? super T> result) throws InterruptedException, IOException {               
        final List<String> classInternalNames = this.getUsagesFQN(binaryName,usageType, Index.BooleanOperator.OR);
        for (String classInternalName : classInternalNames) {
            T value = convertor.convert(ElementKind.OTHER, classInternalName);
            if (value != null) {                
                result.add(value);
            }
        }
    }    
    
    private List<String> getUsagesFQN (final String binaryName, final Set<UsageType> mask, final Index.BooleanOperator operator) throws InterruptedException, IOException {
        List<String> result = this.index.getUsagesFQN(binaryName, mask, operator);          
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }
}
