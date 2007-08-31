/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import static org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
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
    private static IndexFactory indexFactory;
    
    /** Creates a new instance of ClassesAndMembersUQ */
    private PersistentClassIndex(final URL root, final File cacheRoot, final boolean source) 
	    throws IOException, IllegalArgumentException {
        assert root != null;
        this.root = root;
        this.index = (indexFactory == null ? LuceneIndex.create (cacheRoot) : indexFactory.create(cacheRoot));
        this.isSource = source;
    }
    
    public BinaryAnalyser getBinaryAnalyser () {
        return new BinaryAnalyser (this.index);
    }
    
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
    
    public String getSourceName (final String binaryName) {
        try {
            return index.getSourceName(binaryName);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return null;
        }
    }
    

    // Factory method
    
    public static ClassIndexImpl create(URL root, final File cacheRoot, final boolean indexNow) 
	    throws IOException, IllegalArgumentException {        
        return new PersistentClassIndex(root, cacheRoot, indexNow);
    }
    
    // Implementation of UsagesQueryImpl ---------------------------------------    
    public <T> void search (final String binaryName, final Set<UsageType> usageType, final ResultConvertor<T> convertor, final Set<? super T> result) throws InterruptedException {
        updateDirty();
        if (BinaryAnalyser.OBJECT.equals(binaryName)) {
            this.getDeclaredTypes("", ClassIndex.NameKind.PREFIX, convertor, result);
            return;
        }
        try {
            ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void> () {
                public Void run () throws IOException, InterruptedException {
                    usages(binaryName, usageType, convertor, result);
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    
               
    
    public <T> void getDeclaredTypes (final String simpleName, final ClassIndex.NameKind kind, final ResultConvertor<T> convertor, final Set<? super T> result) throws InterruptedException {
        updateDirty();
        try {
            ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void> () {
                public Void run () throws IOException, InterruptedException {
                    index.getDeclaredTypes (simpleName,kind, convertor, result);
                    return null;
                }                    
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    
    public void getPackageNames (final String prefix, final boolean directOnly, final Set<String> result) throws InterruptedException {
        try {
            ClassIndexManager.getDefault().readLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run () throws IOException, InterruptedException {
                    index.getPackageNames(prefix, directOnly, result);
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    public synchronized void setDirty (final JavaSource js) {        
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
        indexFactory = factory;
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
                    if (JavaSourceAccessor.INSTANCE.isDispatchThread()) {
                        //Already under javac's lock
                        try {
                            ClassIndexManager.getDefault().writeLock(
                                new ClassIndexManager.ExceptionAction<Void>() {
                                    public Void run () throws IOException {
                                        CompilationInfo compilationInfo = JavaSourceAccessor.INSTANCE.getCurrentCompilationInfo (js, JavaSource.Phase.RESOLVED);                                        
                                        if (compilationInfo != null) {
                                            //Not cancelled
                                            final SourceAnalyser sa = getSourceAnalyser();
                                            long st = System.currentTimeMillis();
                                            sa.analyseUnitAndStore(compilationInfo.getCompilationUnit(), JavaSourceAccessor.INSTANCE.getJavacTask(compilationInfo),
                                                ClasspathInfoAccessor.INSTANCE.getFileManager(compilationInfo.getClasspathInfo()));
                                            long et = System.currentTimeMillis();
                                            }
                                        return null;
                                    }
                            });                                        
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                        catch (InterruptedException e) {
                            //Should never happen
                            Exceptions.printStackTrace(e);
                        }
                    }
                    else {
                        try {
                            js.runUserActionTask(new Task<CompilationController>() {
                                public void run (final CompilationController controller) {
                                    try {                            
                                        ClassIndexManager.getDefault().writeLock(
                                            new ClassIndexManager.ExceptionAction<Void>() {
                                                public Void run () throws IOException {
                                                    controller.toPhase(Phase.RESOLVED);
                                                    final SourceAnalyser sa = getSourceAnalyser();
                                                    long st = System.currentTimeMillis();
                                                    sa.analyseUnitAndStore(controller.getCompilationUnit(), JavaSourceAccessor.INSTANCE.getJavacTask(controller),
                                                    ClasspathInfoAccessor.INSTANCE.getFileManager(controller.getClasspathInfo()));
                                                    long et = System.currentTimeMillis();
                                                    return null;
                                                }
                                        });
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
                }
                synchronized (this) {
                    this.dirty = null;
                }
                final long endTime = System.currentTimeMillis();
                LOGGER.fine("PersistentClassIndex.updateDirty took: " + (endTime-startTime)+ " ms");     //NOI18N
            }
        }
    }
    
    private <T> void usages (final String binaryName, final Set<UsageType> usageType, ResultConvertor<T> convertor, Set<? super T> result) throws InterruptedException {               
        final List<String> classInternalNames = this.getUsagesFQN(binaryName,usageType, Index.BooleanOperator.OR);
        for (String classInternalName : classInternalNames) {
            T value = convertor.convert(ElementKind.OTHER, classInternalName);
            if (value != null) {                
                result.add(value);
            }
        }
    }    
    
    private List<String> getUsagesFQN (final String binaryName, final Set<UsageType> mask, final Index.BooleanOperator operator) throws InterruptedException {
        List<String> result = null;
        try {
            result = this.index.getUsagesFQN(binaryName, mask, operator);          
        }  catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }        
}
