/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.tasklist.RebuildOraculum;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class JavaCustomIndexer extends CustomIndexer {

    private static final String SOURCE_LEVEL_ROOT = "sourceLevel"; //NOI18N

    private static final CompileWorker[] WORKERS = {
        new OnePassCompileWorker(),
        new MultiPassCompileWorker()
    };
    
    @Override
    protected void index(final Iterable<? extends Indexable> files, final Context context) {
        JavaIndex.LOG.log(Level.FINE, context.isSupplementaryFilesIndexing() ? "index suplementary({0})" :"index({0})", files);
        try {
            final FileObject root = context.getRoot();
            String sourceLevel = SourceLevelQuery.getSourceLevel(root);
            if (JavaIndex.ensureAttributeValue(context.getRootURI(), SOURCE_LEVEL_ROOT, sourceLevel, true)) {
                JavaIndex.LOG.fine("forcing reindex due to source level change");
                IndexingManager.getDefault().refreshIndex(context.getRootURI(), null);
                return;
            }
            final ClassPath sourcePath = ClassPath.getClassPath(root, ClassPath.SOURCE);
            final ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(root, ClassPath.COMPILE);
            if (sourcePath == null || bootPath == null || compilePath == null) {
                String rootName = FileUtil.getFileDisplayName(root);
                JavaIndex.LOG.warning("Ignoring root with no ClassPath: " + rootName);    // NOI18N
                return;
            }
            ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run() throws IOException, InterruptedException {
                    return TaskCache.getDefault().refreshTransaction(new ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            final JavaParsingContext javaContext = new JavaParsingContext(root, bootPath, compilePath, sourcePath, context.checkForEditorModifications());
                            if (javaContext.uq == null)
                                return null; //IDE is exiting, indeces are already closed.
                            final List<URL> errUrls = context.isSupplementaryFilesIndexing() ? null : TaskCache.getDefault().getAllFilesInError(context.getRootURI());
                            final Set<ElementHandle<TypeElement>> removedTypes = new HashSet <ElementHandle<TypeElement>> ();
                            final Set<File> removedFiles = new HashSet<File> ();
                            javaContext.uq.setDirty(null);
                            for (Indexable i : files) {
                                clear(context, javaContext, i.getRelativePath(), removedTypes, removedFiles);
                            }

                            CompileWorker.ParsingOutput compileResult = null;
                            for (CompileWorker w : WORKERS) {
                                compileResult = w.compile(compileResult, context, javaContext, files);
                                if (compileResult.success) {
                                    break;
                                }
                            }
                            assert compileResult != null && compileResult.success;

                            Set<ElementHandle<TypeElement>> _at = new HashSet<ElementHandle<TypeElement>> (compileResult.addedTypes); //Added types
                            Set<ElementHandle<TypeElement>> _rt = new HashSet<ElementHandle<TypeElement>> (removedTypes); //Removed types
                            Set<File> _af = new HashSet<File> (compileResult.createdFiles); //Added files
                            _at.removeAll(removedTypes);
                            _rt.removeAll(compileResult.addedTypes);
                            _af.removeAll(removedFiles);
                            compileResult.addedTypes.retainAll(removedTypes); //Changed types

                            if (!context.isSupplementaryFilesIndexing()) {
                                for (Map.Entry<URL, Collection<URL>> entry : RebuildOraculum.findAllDependent(context.getRootURI(), null, javaContext.cpInfo.getClassIndex(), _rt).entrySet()) {
                                    Set<URL> urls = compileResult.root2Rebuild.get(entry.getKey());
                                    if (urls == null) {
                                        compileResult.root2Rebuild.put(entry.getKey(), urls = new HashSet<URL>());
                                    }
                                    urls.addAll(entry.getValue());
                                }
                                if (!errUrls.isEmpty() && !_af.isEmpty()) {
                                    //new type creation may cause/fix some errors
                                    //not 100% correct (consider eg. a file that has two .* imports
                                    //new file creation may cause new error in this case
                                    Set<URL> urls = compileResult.root2Rebuild.get(context.getRootURI());
                                    if (urls == null) {
                                        compileResult.root2Rebuild.put(context.getRootURI(), urls = new HashSet<URL>());
                                    }
                                    urls.addAll(errUrls);
                                }
                            }
                            javaContext.sa.store();
                            javaContext.uq.typesEvent(_at, _rt, compileResult.addedTypes);
                            BuildArtifactMapperImpl.classCacheUpdated(context.getRootURI(), JavaIndex.getClassFolder(context.getRootURI()), removedFiles, compileResult.createdFiles);

                            for (Map.Entry<URL, Set<URL>> entry : compileResult.root2Rebuild.entrySet()) {
                                context.addSupplementaryFiles(entry.getKey(), entry.getValue());
                            }
                            return null;
                        }
                    });
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void clearFiles(final Context context, final Iterable<? extends Indexable> files) {
        try {
            ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run() throws IOException, InterruptedException {
                    return TaskCache.getDefault().refreshTransaction(new ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            final JavaParsingContext javaContext = new JavaParsingContext(context.getRoot());
                            if (javaContext.uq == null)
                                return null; //IDE is exiting, indeces are already closed.
                            final Set<ElementHandle<TypeElement>> removedTypes = new HashSet <ElementHandle<TypeElement>> ();
                            final Set<File> removedFiles = new HashSet<File> ();
                            for (Indexable i : files) {
                                clear(context, javaContext, i.getRelativePath(), removedTypes, removedFiles);
                                TaskCache.getDefault().dumpErrors(context.getRootURI(), i.getURL(), Collections.<Diagnostic>emptyList());
                            }
                            Map<URL, Collection<URL>> root2Rebuild = RebuildOraculum.findAllDependent(context.getRootURI(), null, javaContext.cpInfo.getClassIndex(), removedTypes);
                            javaContext.sa.store();
                            BuildArtifactMapperImpl.classCacheUpdated(context.getRootURI(), JavaIndex.getClassFolder(context.getRootURI()), removedFiles, Collections.<File>emptySet());
                            javaContext.uq.typesEvent(null, removedTypes, null);
                            for (Map.Entry<URL, Collection<URL>> entry : root2Rebuild.entrySet()) {
                                context.addSupplementaryFiles(entry.getKey(), entry.getValue());
                            }
                            return null;
                        }
                    });
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void clear(final Context context, final JavaParsingContext javaContext, final String sourceRelative, final Set<ElementHandle<TypeElement>> removedTypes, final Set<File> removedFiles) throws IOException {
        final List<Pair<String,String>> toDelete = new ArrayList<Pair<String,String>>();
        final File classFolder = JavaIndex.getClassFolder(context);
        String withoutExt = FileObjects.stripExtension(sourceRelative);
        File file = new File(classFolder, withoutExt + '.' + FileObjects.RS);
        boolean cont = true;
        if (file.exists()) {
            cont = false;
            try {
                String binaryName = FileObjects.getBinaryName(file, classFolder);
                for (String className : readRSFile(file, classFolder)) {
                    File f = new File (classFolder, FileObjects.convertPackage2Folder(className) + '.' + FileObjects.CLASS);
                    if (!binaryName.equals(className)) {
                        toDelete.add(Pair.<String,String>of (className, sourceRelative));
                        removedTypes.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                        removedFiles.add(f);
                        f.delete();
                    } else {
                        cont = true;
                    }
                }
            } catch (IOException ioe) {
                //The signature file is broken, report it but don't stop scanning
                Exceptions.printStackTrace(ioe);
            }
            file.delete();
        }
        if (cont && (file = new File(classFolder, withoutExt + '.' + FileObjects.SIG)).exists()) {
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            final String[] patterns = new String[] {fileName + '.', fileName + '$'}; //NOI18N
            File parent = file.getParentFile();
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    for (int i=0; i< patterns.length; i++) {
                        if (name.startsWith(patterns[i])) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            for (File f : parent.listFiles(filter)) {
                String className = FileObjects.getBinaryName (f, classFolder);
                toDelete.add(Pair.<String,String>of (className, null));
                removedTypes.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                removedFiles.add(f);
                f.delete();
            }
        }
        for (Pair<String, String> pair : toDelete) {
            javaContext.sa.delete(pair);
        }
    }

    private static void markDirtyFiles(final Context context, final Iterable<? extends Indexable> files) {
        ClassIndexImpl indexImpl = ClassIndexManager.getDefault().getUsagesQuery(context.getRootURI());
        if (indexImpl != null) {
            for (Indexable i : files) {
                indexImpl.setDirty(i.getURL());
            }
        }
    }

    public static void verifySourceLevel(URL root, String sourceLevel) throws IOException {
        if (JavaIndex.ensureAttributeValue(root, SOURCE_LEVEL_ROOT, sourceLevel, true)) {
            JavaIndex.LOG.fine("forcing reindex due to source level change");
            IndexingManager.getDefault().refreshIndex(root, null);
        }
    }

    public static Collection<? extends ElementHandle<TypeElement>> getRelatedTypes (final File source, final File root) throws IOException {
        final List<ElementHandle<TypeElement>> result = new LinkedList<ElementHandle<TypeElement>>();
        final File classFolder = JavaIndex.getClassFolder(root);
        String path = FileObjects.stripExtension(FileObjects.getRelativePath(root, source));
        File file = new File (classFolder, path + '.' + FileObjects.RS); //NOI18N
        boolean cont = true;
        if (file.exists()) {
            cont = false;
            try {
                String binaryName = FileObjects.getBinaryName(file, classFolder);
                for (String className : readRSFile(file, classFolder)) {
                    if (!binaryName.equals(className)) {
                        result.add(ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, className));
                    } else {
                        cont = true;
                    }
                }
            } catch (IOException ioe) {
                //The signature file is broken, report it but don't stop scanning
                Exceptions.printStackTrace(ioe);
            }
        }
        if (cont && (file = new File(classFolder, path + '.' + FileObjects.SIG)).exists()) {
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            final String[] patterns = new String[] {fileName + '.', fileName + '$'}; //NOI18N
            File parent = file.getParentFile();
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    for (int i=0; i< patterns.length; i++) {
                        if (name.startsWith(patterns[i])) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            for (File f : parent.listFiles(filter)) {
                String className = FileObjects.getBinaryName (f, classFolder);
                result.add(ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, className));
            }
        }
        return result;
    }

    private static List<String> readRSFile (final File file, final File root) throws IOException {
        final List<String> binaryNames = new LinkedList<String>();
        BufferedReader in = new BufferedReader (new InputStreamReader ( new FileInputStream (file), "UTF-8"));
        try {
            String binaryName;
            while ((binaryName=in.readLine())!=null) {
                binaryNames.add(binaryName);
            }
        } finally {
            in.close();
        }
        return binaryNames;
    }

    public static class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new JavaCustomIndexer();
        }

        @Override
        public void filesDeleted(Collection<? extends Indexable> deleted, Context context) {
            JavaIndex.LOG.log(Level.FINE, "filesDeleted({0})", deleted);
            clearFiles(context, deleted);
        }

        @Override
        public void filesDirty(Collection<? extends Indexable> dirty, Context context) {
            JavaIndex.LOG.log(Level.FINE, "filesDirty({0})", dirty);
            markDirtyFiles(context, dirty);
        }

        @Override
        public String getIndexerName() {
            return JavaIndex.NAME;
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public int getIndexVersion() {
            return JavaIndex.VERSION;
        }
    }
}
