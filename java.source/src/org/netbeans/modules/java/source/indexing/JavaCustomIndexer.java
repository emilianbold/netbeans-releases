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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.FileObjects.InferableJavaFileObject;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.tasklist.TasklistSettings;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.java.source.usages.SourceAnalyser;
import org.netbeans.modules.java.source.usages.VirtualSourceProviderQuery;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda, Dusan Balek, Tomas Zezula
 */
public class JavaCustomIndexer extends CustomIndexer {

    private static final String SOURCE_LEVEL_ROOT = "sourceLevel"; //NOI18N
    private static final String DIRTY_ROOT = "dirty"; //NOI18N
    private static final Pattern ANONYMOUS = Pattern.compile("\\$[0-9]"); //NOI18N
    private static final ClassPath EMPTY = ClassPathSupport.createClassPath(new URL[0]);

    private static final CompileWorker[] WORKERS = {
        new OnePassCompileWorker(),
        new MultiPassCompileWorker()
    };
        
    @Override
    protected void index(final Iterable<? extends Indexable> files, final Context context) {
        JavaIndex.LOG.log(Level.FINE, context.isSupplementaryFilesIndexing() ? "index suplementary({0})" :"index({0})", context.isAllFilesIndexing() ? context.getRootURI() : files); //NOI18N
        try {
            final FileObject root = context.getRoot();
            if (root == null) {
                JavaIndex.LOG.fine("Ignoring request with no root"); //NOI18N
                return;
            }
            String sourceLevel = SourceLevelQuery.getSourceLevel(root);
            if (JavaIndex.ensureAttributeValue(context.getRootURI(), SOURCE_LEVEL_ROOT, sourceLevel)) {
                JavaIndex.LOG.fine("forcing reindex due to source level change"); //NOI18N
                IndexingManager.getDefault().refreshIndex(context.getRootURI(), null);
                return;
            }
            if (JavaIndex.ensureAttributeValue(context.getRootURI(), DIRTY_ROOT, null) && !context.isAllFilesIndexing()) {
                JavaIndex.LOG.fine("forcing reindex due to dirty root"); //NOI18N
                IndexingManager.getDefault().refreshIndex(context.getRootURI(), null);
                return;
            }
            final ClassPath sourcePath = ClassPath.getClassPath(root, ClassPath.SOURCE);
            final ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(root, ClassPath.COMPILE);
            if (sourcePath == null || bootPath == null || compilePath == null) {
                JavaIndex.LOG.warning("Ignoring root with no ClassPath: " + FileUtil.getFileDisplayName(root)); // NOI18N
                return;
            }
            if (!Arrays.asList(sourcePath.getRoots()).contains(root)) {
                JavaIndex.LOG.warning("Source root: " + FileUtil.getFileDisplayName(root) + " is not on its sourcepath"); // NOI18N
                return;
            }
            final List<Indexable> javaSources = new ArrayList<Indexable>();
            final Collection<? extends CompileTuple> virtualSourceTuples = translateVirtualSources (
                    splitSources(files,javaSources),
                    context.getRootURI());

            ClassIndexManager.getDefault().prepareWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run() throws IOException, InterruptedException {
                    return TaskCache.getDefault().refreshTransaction(new ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            final JavaParsingContext javaContext = new JavaParsingContext(context, bootPath, compilePath, sourcePath, virtualSourceTuples);
                            if (javaContext.uq == null)
                                return null; //IDE is exiting, indeces are already closed.                            
                            final Set<ElementHandle<TypeElement>> removedTypes = new HashSet <ElementHandle<TypeElement>> ();
                            final Set<File> removedFiles = new HashSet<File> ();
                            final List<CompileTuple> toCompile = new ArrayList<CompileTuple>(javaSources.size()+virtualSourceTuples.size());
                            javaContext.uq.setDirty(null);
                            for (Indexable i : javaSources) {
                                final CompileTuple tuple = createTuple(context, javaContext, i);
                                if (tuple != null) {
                                    toCompile.add(tuple);
                                }
                                clear(context, javaContext, i.getRelativePath(), removedTypes, removedFiles);
                            }
                            for (CompileTuple tuple : virtualSourceTuples) {
                                clear(context, javaContext, tuple.indexable.getRelativePath(), removedTypes, removedFiles);
                            }
                            toCompile.addAll(virtualSourceTuples);
                            CompileWorker.ParsingOutput compileResult = null;
                            for (CompileWorker w : WORKERS) {
                                compileResult = w.compile(compileResult, context, javaContext, toCompile);
                                if (compileResult.success) {
                                    break;
                                }
                            }
                            assert compileResult != null;

                            Set<ElementHandle<TypeElement>> _at = new HashSet<ElementHandle<TypeElement>> (compileResult.addedTypes); //Added types
                            Set<ElementHandle<TypeElement>> _rt = new HashSet<ElementHandle<TypeElement>> (removedTypes); //Removed types
                            _at.removeAll(removedTypes);
                            _rt.removeAll(compileResult.addedTypes);
                            compileResult.addedTypes.retainAll(removedTypes); //Changed types

                            if (!context.isSupplementaryFilesIndexing()) {
                                compileResult.modifiedTypes.addAll(_rt);
                                Map<URL, Set<URL>> root2Rebuild = findDependent(context.getRootURI(), compileResult.modifiedTypes, !_at.isEmpty());
                                Set<URL> urls = root2Rebuild.get(context.getRootURI());
                                if (urls != null) {
                                    if (context.isAllFilesIndexing()) {
                                        root2Rebuild.remove(context.getRootURI());
                                    } else {
                                        for (CompileTuple ct : toCompile)
                                            urls.remove(ct.indexable.getURL());
                                        if (urls.isEmpty())
                                            root2Rebuild.remove(context.getRootURI());
                                    }
                                }
                                for (Map.Entry<URL, Set<URL>> entry : root2Rebuild.entrySet()) {
                                    context.addSupplementaryFiles(entry.getKey(), entry.getValue());
                                }
                            }
                            javaContext.checkSums.store();
                            javaContext.sa.store();
                            javaContext.uq.typesEvent(_at, _rt, compileResult.addedTypes);
                            BuildArtifactMapperImpl.classCacheUpdated(context.getRootURI(), JavaIndex.getClassFolder(context.getRootURI()), removedFiles, compileResult.createdFiles);
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

    private static final List<? extends Indexable> splitSources(final Iterable<? extends Indexable> indexables, final List<? super Indexable> javaSources) {
        List<Indexable> virtualSources = new LinkedList<Indexable>();
        for (Indexable indexable : indexables) {
            if (indexable.getURL() == null) {
                /*
                    Issue #168179: This is probably deleted source file. Just skipping. 
                 */
                continue;
            }
            if (VirtualSourceProviderQuery.hasVirtualSource(indexable)) {
                virtualSources.add(indexable);
            }
            else {
                javaSources.add(indexable);
            }
        }
        return virtualSources;
    }

    private static Collection<? extends CompileTuple> translateVirtualSources(final Collection<? extends Indexable> virtualSources, final URL rootURL) throws IOException {
        if (virtualSources.isEmpty()) {
            return Collections.<CompileTuple>emptySet();
        }
        try {
            final File root = new File (URI.create(rootURL.toString()));
            return VirtualSourceProviderQuery.translate(virtualSources, root);
        } catch (IllegalArgumentException e) {
            //Called on non local fs => not supported, log and ignore.
            JavaIndex.LOG.warning("Virtual sources in the root: " + rootURL +" are ignored due to: " + e.getMessage()); //NOI18N
            return Collections.<CompileTuple>emptySet();
        }
    }

    private static CompileTuple createTuple(Context context, JavaParsingContext javaContext, Indexable indexable) {
        File root = null;
        if (!context.checkForEditorModifications() && "file".equals(indexable.getURL().getProtocol()) && (root = FileUtil.toFile(context.getRoot())) != null) { //NOI18N
            try {
                File file = new File(indexable.getURL().toURI().getPath());
                return new CompileTuple(FileObjects.fileFileObject(file, root, null, javaContext.encoding), indexable);
            } catch (Exception ex) {}
        }
        FileObject fo = URLMapper.findFileObject(indexable.getURL());
        return fo != null ? new CompileTuple(SourceFileObject.create(fo, context.getRoot()), indexable) : null;
    }

    private static void clearFiles(final Context context, final Iterable<? extends Indexable> files) {
        try {
            if (context.getRoot() == null) {
                JavaIndex.LOG.fine("Ignoring request with no root"); //NOI18N
                return;
            }
            ClassIndexManager.getDefault().prepareWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run() throws IOException, InterruptedException {
                    return TaskCache.getDefault().refreshTransaction(new ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            final JavaParsingContext javaContext = new JavaParsingContext(context);
                            if (javaContext.uq == null)
                                return null; //IDE is exiting, indeces are already closed.
                            final Set<ElementHandle<TypeElement>> removedTypes = new HashSet <ElementHandle<TypeElement>> ();
                            final Set<File> removedFiles = new HashSet<File> ();
                            for (Indexable i : files) {
                                clear(context, javaContext, i.getRelativePath(), removedTypes, removedFiles);
                                TaskCache.getDefault().dumpErrors(context.getRootURI(), i.getURL(), Collections.<Diagnostic>emptyList());
                                javaContext.checkSums.remove(i.getURL());
                            }
                            for (Map.Entry<URL, Set<URL>> entry : findDependent(context.getRootURI(), removedTypes, false).entrySet()) {
                                context.addSupplementaryFiles(entry.getKey(), entry.getValue());
                            }
                            javaContext.checkSums.store();
                            javaContext.sa.store();
                            BuildArtifactMapperImpl.classCacheUpdated(context.getRootURI(), JavaIndex.getClassFolder(context.getRootURI()), removedFiles, Collections.<File>emptySet());
                            javaContext.uq.typesEvent(null, removedTypes, null);
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
        final String ext = FileObjects.getExtension(sourceRelative);
        final String withoutExt = FileObjects.stripExtension(sourceRelative);
        File file;
        final boolean dieIfNoRefFile = VirtualSourceProviderQuery.hasVirtualSource(ext);
        if (dieIfNoRefFile) {
            file = new File(classFolder, sourceRelative + '.' + FileObjects.RX);
        }
        else {
            file = new File(classFolder, withoutExt + '.' + FileObjects.RS);
        }
        boolean cont = !dieIfNoRefFile;
        if (file.exists()) {
            cont = false;
            try {
                String binaryName = FileObjects.getBinaryName(file, classFolder);
                for (String className : readRSFile(file, classFolder)) {
                    File f = new File (classFolder, FileObjects.convertPackage2Folder(className) + '.' + FileObjects.SIG);
                    if (!binaryName.equals(className)) {
                        toDelete.add(Pair.<String,String>of (className, sourceRelative));
                        removedTypes.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                        removedFiles.add(f);
                        f.delete();
                    } else {
                        cont = !dieIfNoRefFile;
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
        if (JavaIndex.ensureAttributeValue(root, SOURCE_LEVEL_ROOT, sourceLevel)) {
            JavaIndex.LOG.fine("forcing reindex due to source level change"); //NOI18N
            IndexingManager.getDefault().refreshIndex(root, null);
        }
    }

    public static Collection<? extends ElementHandle<TypeElement>> getRelatedTypes (final File source, final File root) throws IOException {
        final List<ElementHandle<TypeElement>> result = new LinkedList<ElementHandle<TypeElement>>();
        final File classFolder = JavaIndex.getClassFolder(root);
        final String path = FileObjects.getRelativePath(root, source);
        final String ext = FileObjects.getExtension(path);
        final String pathNoExt = FileObjects.stripExtension(path);
        final boolean dieIfNoRefFile = VirtualSourceProviderQuery.hasVirtualSource(ext);
        File file;
        if (dieIfNoRefFile) {
            file = new File (classFolder, path + '.' + FileObjects.RX); //NOI18N
        }
        else {
            file = new File (classFolder, pathNoExt + '.' + FileObjects.RS); //NOI18N
        }
        
        boolean cont = !dieIfNoRefFile;
        if (file.exists()) {
            cont = false;
            try {
                String binaryName = FileObjects.getBinaryName(file, classFolder);
                for (String className : readRSFile(file, classFolder)) {
                    if (!binaryName.equals(className)) {
                        result.add(ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, className));
                    } else {
                        cont = !dieIfNoRefFile;
                    }
                }
            } catch (IOException ioe) {
                //The signature file is broken, report it but don't stop scanning
                Exceptions.printStackTrace(ioe);
            }
        }
        if (cont && (file = new File(classFolder, pathNoExt + '.' + FileObjects.SIG)).exists()) {
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
        BufferedReader in = new BufferedReader (new InputStreamReader ( new FileInputStream (file), "UTF-8")); //NOI18N
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

    private static boolean checkDeps(final Context context) {
        if (context.isSupplementaryFilesIndexing())
            return false;
        switch(TasklistSettings.getDependencyTracking()) {
            case DISABLED:
                return false;
            case ENABLED_WITHIN_ROOT:
                if (context.isAllFilesIndexing())
                    return false;
        }
        return true;
    }

    private static Map<URL, Set<URL>> findDependent(final URL root, final Collection<ElementHandle<TypeElement>> classes, boolean includeFilesInError) throws IOException {
        final Map<URL, Set<URL>> ret = new LinkedHashMap<URL, Set<URL>>();

        //performance: filter out anonymous innerclasses:
        for (Iterator<ElementHandle<TypeElement>> i = classes.iterator(); i.hasNext(); ) {
            if (ANONYMOUS.matcher(i.next().getBinaryName()).find()) {
                i.remove();
            }
        }

        if (classes.isEmpty() && !includeFilesInError) {
            return ret;
        }

        //get dependencies
        Map<URL, List<URL>> deps = IndexingController.getDefault().getRootDependencies();

        //create inverse dependencies
        final Map<URL, List<URL>> inverseDeps = new HashMap<URL, List<URL>> ();
        for (Map.Entry<URL,List<URL>> entry : deps.entrySet()) {
            final URL u1 = entry.getKey();
            final List<URL> l1 = entry.getValue();
            for (URL u2 : l1) {
                List<URL> l2 = inverseDeps.get(u2);
                if (l2 == null) {
                    l2 = new ArrayList<URL>();
                    inverseDeps.put (u2,l2);
                }
                l2.add (u1);
            }
        }

        //get sorted list of depenedent roots
        List<URL> depRoots = inverseDeps.get(root);
        try {
            switch (TasklistSettings.getDependencyTracking()) {
                case DISABLED:
                    if (depRoots == null) {
                        JavaIndex.setAttribute(root, DIRTY_ROOT, Boolean.TRUE.toString());
                    } else {
                        for (URL url : depRoots) {
                            JavaIndex.setAttribute(url, DIRTY_ROOT, Boolean.TRUE.toString());
                        }
                    }
                    return ret;
                case ENABLED_WITHIN_ROOT:
                    if (depRoots == null) {
                        depRoots = Collections.singletonList(root);
                    } else {
                        for (URL url : depRoots) {
                            JavaIndex.setAttribute(url, DIRTY_ROOT, Boolean.TRUE.toString());
                        }
                    }
                    break;
                case ENABLED_WITHIN_PROJECT:
                    if (depRoots == null) {
                        depRoots = Collections.singletonList(root);
                    } else {
                        Project rootPrj = FileOwnerQuery.getOwner(root.toURI());
                        if (rootPrj == null) {
                            for (URL url : depRoots) {
                                JavaIndex.setAttribute(url, DIRTY_ROOT, Boolean.TRUE.toString());
                            }
                            depRoots = Collections.singletonList(root);
                        } else {
                            List<URL> l = new ArrayList<URL>(depRoots.size());
                            for (URL url : depRoots) {
                                if (FileOwnerQuery.getOwner(url.toURI()) == rootPrj) {
                                    l.add(url);
                                } else {
                                    JavaIndex.setAttribute(url, DIRTY_ROOT, Boolean.TRUE.toString());
                                }
                            }
                            l.add(root);
                            depRoots = Utilities.topologicalSort(l, inverseDeps);
                        }
                    }
                    break;
                case ENABLED:
                    if (depRoots == null) {
                        depRoots = Collections.singletonList(root);
                    } else {
                        List<URL> l = new ArrayList<URL>(depRoots);
                        l.add(root);
                        depRoots = Utilities.topologicalSort(l, inverseDeps);
                    }
                    break;
            }
        } catch (TopologicalSortException ex) {
            JavaIndex.LOG.warning("Cycle in the source root dependencies detected: " + ex.unsortableSets()); //NOI18N
            List part = ex.partialSort();
            part.retainAll(depRoots);
            depRoots = part;
        } catch (URISyntaxException urise) {
            depRoots = Collections.singletonList(root);
        }


        final Queue<ElementHandle<TypeElement>> queue = new LinkedList<ElementHandle<TypeElement>>(classes);
        final Map<URL, Set<ElementHandle<TypeElement>>> bases = new HashMap<URL, Set<ElementHandle<TypeElement>>>();
        for (URL depRoot : depRoots) {            
            if (!ClassIndexManager.getDefault().createUsagesQuery(depRoot, true).isEmpty()) {
                final ClassIndex index = ClasspathInfo.create(EMPTY, EMPTY, ClassPathSupport.createClassPath(depRoot)).getClassIndex();

                final List<URL> dep = deps != null ? deps.get(depRoot) : null;
                if (dep != null) {
                    for (URL url : dep) {
                        final Set<ElementHandle<TypeElement>> b = bases.get(url);
                        if (b != null)
                            queue.addAll(b);
                    }
                }

                final Set<ElementHandle<TypeElement>> toHandle = new HashSet<ElementHandle<TypeElement>>();
                while (!queue.isEmpty()) {
                    final ElementHandle<TypeElement> e = queue.poll();
                    if (toHandle.add(e))
                        queue.addAll(index.getElements(e, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                }
                bases.put(depRoot, toHandle);

                final Set<FileObject> files = new HashSet<FileObject>();
                for (ElementHandle<TypeElement> e : toHandle)
                    files.addAll(index.getResources(e, EnumSet.complementOf(EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS)), EnumSet.of(ClassIndex.SearchScope.SOURCE)));

                final Set<URL> urls = new HashSet<URL>();
                for (FileObject file : files)
                    urls.add(file.getURL());

                if (includeFilesInError) {
                    final List<URL> errUrls = TaskCache.getDefault().getAllFilesInError(depRoot);
                    if (!errUrls.isEmpty()) {
                        //new type creation may cause/fix some errors
                        //not 100% correct (consider eg. a file that has two .* imports
                        //new file creation may cause new error in this case
                        urls.addAll(errUrls);
                    }
                }

                if (!urls.isEmpty())
                    ret.put(depRoot, urls);
            }
        }
        return ret;
    }

    public static class Factory extends CustomIndexerFactory {


        @Override
        public boolean scanStarted(final Context context) {
            try {
                return ClassIndexManager.getDefault().prepareWriteLock(new ClassIndexManager.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException, InterruptedException {
                        return ClassIndexManager.getDefault().takeWriteLock(new ClassIndexManager.ExceptionAction<Boolean>() {
                            public Boolean run() throws IOException, InterruptedException {
                                final ClassIndexImpl uq = ClassIndexManager.getDefault().createUsagesQuery(context.getRootURI(), true);
                                if (uq == null) {
                                    //Closing...
                                    return true;
                                }
                                if (uq.getState() != ClassIndexImpl.State.NEW) {
                                    //Already checked
                                    return true;
                                }
                                try {
                                    return uq.getSourceAnalyser().isValid();
                                } finally {
                                    uq.setState(ClassIndexImpl.State.INITIALIZED);
                                }
                            }
                        });
                    }
                });                
            } catch (IOException ioe) {
                JavaIndex.LOG.log(Level.WARNING, "Exception while checking cache validity for root: "+context.getRootURI(), ioe); //NOI18N
                return false;
            } catch (InterruptedException ie) {
                JavaIndex.LOG.log(Level.WARNING, "Exception while checking cache validity for root: "+context.getRootURI(), ie); //NOI18N
                return false;
            }

        }

        @Override
        public void scanFinished(final Context context) {
            //Not needed now
        }
        
        @Override
        public CustomIndexer createIndexer() {
            return new JavaCustomIndexer();
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            JavaIndex.LOG.log(Level.FINE, "filesDeleted({0})", deleted); //NOI18N
            clearFiles(context, deleted);
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
            assert removedRoots != null;
            final ClassIndexManager cim = ClassIndexManager.getDefault();
            try {
                cim.prepareWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                    public Void run() throws IOException, InterruptedException {
                        for (URL removedRoot : removedRoots) {
                            cim.removeRoot(removedRoot);
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);

            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            JavaIndex.LOG.log(Level.FINE, "filesDirty({0})", dirty); //NOI18N
            markDirtyFiles(context, dirty);
        }

        @Override
        public String getIndexerName() {
            return JavaIndex.NAME;
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public int getIndexVersion() {
            return JavaIndex.VERSION;
        }
    }

    public static final class CompileTuple {
        public final InferableJavaFileObject jfo;
        public final Indexable indexable;
        public final boolean virtual;
        public final boolean index;        

        public CompileTuple (final InferableJavaFileObject jfo, final Indexable indexable,
                final boolean virtual, final boolean index) {
            this.jfo = jfo;
            this.indexable = indexable;
            this.virtual = virtual;
            this.index = index;
        }

        public CompileTuple (final InferableJavaFileObject jfo, final Indexable indexable) {
            this(jfo,indexable,false, true);
        }
    }
}
