/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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


package org.netbeans.modules.parsing.impl.indexing.errors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.Convertor;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.ErrorKind;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;

/**
 *
 * @author Jan Lahoda, Stanislav Aubrecht
 */
public class TaskCache {
    
    private static final String ERR_EXT = "err";
    private static final String WARN_EXT = "warn";

    private static final int VERSION = 1;
    
    private static final Logger LOG = Logger.getLogger(TaskCache.class.getName());
    
    static {
//        LOG.setLevel(Level.FINEST);
    }
    
    private static TaskCache theInstance;
    
    private TaskCache() {
    }
    
    public static TaskCache getDefault() {
        if( null == theInstance ) {
            theInstance = new TaskCache();
        }
        return theInstance;
    }

    private String getTaskType( ErrorKind k ) {
        switch( k ) {
            case ERROR:
            case ERROR_NO_BADGE:
                return "nb-tasklist-error"; //NOI18N
            case WARNING:
                return "nb-tasklist-warning"; //NOI18N
        }
        return null;
    }
    
    public List<Task> getErrors(FileObject file) {
        List<Task> result = new LinkedList<Task>();
        
        result.addAll(getErrors(file, ERR_EXT));
        result.addAll(getErrors(file, WARN_EXT));

        return result;
    }
    
    private List<Task> getErrors(FileObject file, String ext) {
        LOG.log(Level.FINE, "getErrors, file={0}, ext={1}", new Object[] {FileUtil.getFileDisplayName(file), ext});
        
        try {
            File input = computePersistentFile(file, ext);
            
            LOG.log(Level.FINE, "getErrors, error file={0}", input == null ? "null" : input.getAbsolutePath());
            
            if (input == null || !input.canRead())
                return Collections.<Task>emptyList();
            
            input.getParentFile().mkdirs();
            
            return loadErrors(input, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return Collections.<Task>emptyList();
    }
    
    private <T> boolean dumpErrors(File output, Iterable<? extends T> errors, Convertor<T> convertor, boolean interestedInReturnValue) throws IOException {
        if (errors.iterator().hasNext()) {
            boolean existed = interestedInReturnValue && output.exists();
            output.getParentFile().mkdirs();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
            
            for (T err : errors) {
                pw.print(convertor.getKind(err));
                pw.print(':');
                pw.print(convertor.getLineNumber(err));
                pw.print(':');
                
                String description = convertor.getMessage(err);
                
                description = description.replaceAll("\\\\", "\\\\\\\\");
                description = description.replaceAll("\n", "\\\\n");
                description = description.replaceAll(":", "\\\\d");
                
                pw.println(description);
            }
            
            pw.close();
            
            return !existed;
        } else {
            return output.delete();
        }
    }
    
    private <T> void separate(Iterable<? extends T> input, Convertor<T> convertor, List<T> errors, List<T> notErrors) {
        for (T err : input) {
            if (convertor.getKind(err) == ErrorKind.ERROR) {
                errors.add(err);
            } else {
                notErrors.add(err);
            }
        }
    }

    public <T> void dumpErrors(final URL root, final Indexable i, final Iterable<? extends T> errors, final Convertor<T> convertor) {
        try {
            refreshTransaction(new ExceptionAction<Void>() {
                public Void run() throws Exception {
                    dumpErrors(q.get(), root, i, errors, convertor);
                    return null;
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private <T> void dumpErrors(TransactionContext c, URL root, Indexable i, Iterable<? extends T> errors, Convertor<T> convertor) throws IOException {
        //TODO: check to canRead() on "Indexable" was done here
        File[] output = computePersistentFile(root, i);
        
        List<T> trueErrors = new LinkedList<T>();
        List<T> notErrors = new LinkedList<T>();
        
        separate(errors, convertor, trueErrors, notErrors);
        
        boolean modified = dumpErrors(output[0], trueErrors, convertor, true);
        
        dumpErrors(output[1], notErrors, convertor, false);

        URL currentFile = i.getURL();

        c.toRefresh.add(currentFile);

        if (modified) {
            c.toRefresh.add(currentFile = new URL(currentFile, "."));
            final String relativePath = i.getRelativePath();
            for (int depth = relativePath.split("/").length-1; depth>0; depth--) {  //NOI18N
                currentFile = new URL(currentFile, "..");
                c.toRefresh.add(currentFile);
            }

            FileObject rootFO = URLMapper.findFileObject(root);

            //XXX:
            if (rootFO != null) {
                Project p = FileOwnerQuery.getOwner(rootFO);

                if (p != null) {
                    FileObject currentFO = rootFO;
                    FileObject projectDirectory = p.getProjectDirectory();

                    if (FileUtil.isParentOf(projectDirectory, rootFO)) {
                        while (currentFO != null && currentFO != projectDirectory) {
                            c.toRefresh.add(currentFO.getURL());
                            currentFO = currentFO.getParent();
                        }
                    }

                    c.toRefresh.add(projectDirectory.getURL());
                }
            }
        }

        c.rootsToRefresh.add(root);
    }

    private List<Task> loadErrors(File input, FileObject file) throws IOException {
        List<Task> result = new LinkedList<Task>();
        BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
        String line;

        while ((line = pw.readLine()) != null) {
            String[] parts = line.split(":");

            ErrorKind kind = ErrorKind.valueOf(parts[0]);

            if (kind == null) {
                continue;
            }

            int lineNumber = Integer.parseInt(parts[1]);
            String message = parts[2];

            message = message.replaceAll("\\\\d", ":");
            message = message.replaceAll("\\\\n", " ");
            message = message.replaceAll("\\\\\\\\", "\\\\");

            String severity = getTaskType(kind);

            if (null != severity) {
                Task err = Task.create(file, severity, message, lineNumber);
                result.add(err);
            }
        }

        pw.close();
        
        return result;
    }
    
    public List<URL> getAllFilesWithRecord(URL root) throws IOException {
        return getAllFilesWithRecord(root, false);
    }
    
    private List<URL> getAllFilesWithRecord(URL root, boolean onlyErrors) throws IOException {
        try {
            List<URL> result = new LinkedList<URL>();
            if (FileUtil.getArchiveFile(root) != null) {
                return result;
            }
            URI rootURI = root.toURI();
            File cacheRoot = getCacheRoot(root);
            URI cacheRootURI = cacheRoot.toURI();
            Queue<File> todo = new LinkedList<File>();
            
            todo.add(cacheRoot);
            
            while (!todo.isEmpty()) {
                File f = todo.poll();
                
                assert f != null;
                
                if (f.isFile()) {
                    if (f.getName().endsWith(ERR_EXT)) {
                        String relative = cacheRootURI.relativize(f.toURI()).getRawPath();
                        
                        relative = relative.replaceAll("." + ERR_EXT + "$", "");
                        result.add(rootURI.resolve(relative).toURL());
                    }
                    if (!onlyErrors && f.getName().endsWith(WARN_EXT)) {
                        String relative = cacheRootURI.relativize(f.toURI()).getRawPath();
                        
                        relative = relative.replaceAll("." + WARN_EXT + "$", "");
                        result.add(rootURI.resolve(relative).toURL());
                    }
                } else {
                    File[] files = f.listFiles();
                    
                    if (files != null) {
                        for (File children : files)
                            todo.offer(children);
                    }
                }
            }
            
            return result;
        } catch (URISyntaxException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    
    public List<URL> getAllFilesInError(URL root) throws IOException {
        return getAllFilesWithRecord(root, true);
    }
    
    public boolean isInError(FileObject file, boolean recursive) {
        LOG.log(Level.FINE, "file={0}, recursive={1}", new Object[] {file, Boolean.valueOf(recursive)});
        
        if (file.isData()) {
            return !getErrors(file, ERR_EXT).isEmpty();
        } else {
            try {
                ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                
                if (cp == null) {
                    return false;
                }
                
                FileObject root = cp.findOwnerRoot(file);
                
                if (root == null) {
                    LOG.log(Level.FINE, "file={0} does not have a root on its own source classpath", file);
                    return false;
                }
                
                String resourceName = cp.getResourceName(file, File.separatorChar, false);
                File cacheRoot = getCacheRoot(root.getURL(), true);
                
                if (cacheRoot == null) {
                    //index does not exist:
                    return false;
                }
                
                final File folder = new File(cacheRoot, resourceName);
                
                return folderContainsErrors(folder, recursive);
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
                return false;
            }
        }
    }
    
    private boolean folderContainsErrors(File folder, boolean recursively) throws IOException {
        File[] errors = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".err");
            }
        });
        
        if (errors == null)
            return false;
        
        if (errors.length > 0) {
            return true;
        }
        
        if (!recursively)
            return false;
        
        File[] children = folder.listFiles();
        
        if (children == null)
            return false;
        
        for (File c : children) {
            if (c.isDirectory() && folderContainsErrors(c, recursively)) {
                return true;
            }
        }
        
        return false;
    }
    
    private File[] computePersistentFile(URL root, Indexable i) throws IOException {
        String resourceName = i.getRelativePath();
        File cacheRoot = getCacheRoot(root);
        File errorCacheFile = new File(cacheRoot, resourceName + "." + ERR_EXT);
        File warningCacheFile = new File(cacheRoot, resourceName + "." + WARN_EXT);

        return new File[] {errorCacheFile, warningCacheFile};
    }
    
    private File computePersistentFile(FileObject file, String extension) throws IOException {
        ClassPath cp = Utilities.getSourceClassPathFor(file);
        
        if (cp == null)
            return null;
        
        FileObject root = cp.findOwnerRoot(file);
        
        if (root == null) {
            LOG.log(Level.FINE, "file={0} does not have a root on its own source classpath", file);
            return null;
        }
        
        String resourceName = cp.getResourceName(file, File.separatorChar, true);
        File cacheRoot = getCacheRoot(root.getURL());
        File cacheFile = new File(cacheRoot, resourceName + "." + extension);
        
        return cacheFile;
    }

    private ThreadLocal<TransactionContext> q = new ThreadLocal<TransactionContext>();
    
    public <T> T refreshTransaction(ExceptionAction<T> a) throws IOException {
        TransactionContext c = q.get();

        if (c == null) {
            q.set(c = new TransactionContext());
        }

        c.depth++;
        
        try {
            return a.run();
        } catch (IOException ioe) { //???
            throw ioe;
        } catch (Exception ex) {
            throw (IOException) new IOException(ex.getMessage()).initCause(ex);
        } finally {
            if (--c.depth == 0) {
                doRefresh(c);
                q.set(null);
            }
        }
    }

    private static void doRefresh(TransactionContext c) {
        if (Settings.isBadgesEnabled() && !c.toRefresh.isEmpty()) {
            ErrorAnnotator an = ErrorAnnotator.getAnnotator();

            if (an != null) {
                an.updateInError(c.toRefresh);
            }
        }

        for (URL root : c.rootsToRefresh) {
            FileObject rootFO = URLMapper.findFileObject(root);

            if (rootFO != null) {
                TaskProvider.refresh(rootFO);
            }
        }
    }

    private static File getCacheRoot(URL root) throws IOException {
        return getCacheRoot(root, false);
    }

    private static File getCacheRoot(URL root, boolean onlyIfExists) throws IOException {
        FileObject dataFolder = CacheFolder.getDataFolder(root, onlyIfExists);

        if (dataFolder == null) {
            return null;
        }
        
        File cache = FileUtil.toFile(FileUtil.createFolder(dataFolder, "errors/" + VERSION));

        return cache;
    }

    private static final class TransactionContext {
        private int depth;
        private Set<URL> toRefresh = new HashSet<URL>();
        private Set<URL> rootsToRefresh = new HashSet<URL>();
    }
}
