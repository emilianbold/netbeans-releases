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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.java.source.tasklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda, Stanislav Aubrecht
 */
public class TaskCache {
    
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
    
    private String getTaskType( Kind k ) {
        switch( k ) {
            case ERROR:
                return "nb-tasklist-error"; //NOI18N
            case WARNING:
            case MANDATORY_WARNING:
                return "nb-tasklist-warning"; //NOI18N
        }
        return null;
    }
    
    public List<Task> getErrors(FileObject file) {
        return getErrors(file, false);
    }
    
    private List<Task> getErrors(FileObject file, boolean onlyErrors) {
        LOG.log(Level.FINE, "getErrors, file={0}", FileUtil.getFileDisplayName(file));
        
        List<Task> result = new ArrayList<Task>();
        
        try {
            File input = computePersistentFile(file);
            
            LOG.log(Level.FINE, "getErrors, error file={0}", input == null ? "null" : input.getAbsolutePath());
            
            if (input == null || !input.canRead())
                return Collections.<Task>emptyList();
            
            input.getParentFile().mkdirs();
            
            BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
            String line;
            
            while ((line = pw.readLine()) != null) {
                String[] parts = line.split(":");
                
                Kind kind = Kind.valueOf(parts[0]);
                
                if (kind == null) {
                    continue;
                }
                
                int lineNumber = Integer.parseInt(parts[1]);
                String message = parts[2];
                
                message = message.replaceAll("\\\\d", ":");
                message = message.replaceAll("\\\\n", " ");
                message = message.replaceAll("\\\\\\\\", "\\\\");
                
                String severity = getTaskType(kind);
                
                if( null != severity && (!onlyErrors || kind == Kind.ERROR)) {
                    Task err = Task.create(file, severity, message, lineNumber );
                    result.add( err );
                }
            }
            
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public Set<URL> dumpErrors(URL root, URL file, File fileFile, List<? extends Diagnostic> errors) throws IOException {
        File[] output = computePersistentFile(root, file);
        boolean containsErrors = false;
        
        if (!errors.isEmpty()) {
            output[1].getParentFile().mkdirs();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output[1]), "UTF-8"));
            
            for (Diagnostic d : errors) {
                if (d.getKind() == Kind.ERROR) {
                    containsErrors = true;
                }
                pw.print(d.getKind());
                pw.print(':');
                pw.print(d.getLineNumber());
                pw.print(':');
                
                String description = d.getMessage(null);
                
                description = description.replaceAll("\\\\", "\\\\\\\\");
                description = description.replaceAll("\n", "\\\\n");
                description = description.replaceAll(":", "\\\\d");
                
                pw.println(description);
            }
            
            pw.close();
        } else {
            output[1].delete();
        }
        
        Set<URL> toRefresh = new HashSet<URL>();
        
        toRefresh.add(file);
        
        File current = output[1].getParentFile();
        File currentFile = fileFile.getParentFile();
                
        while (!output[0].equals(current)) {
            if (updateInErrorFolder(current, file, containsErrors, true))
                toRefresh.add(currentFile.toURL());
            current = current.getParentFile();
            currentFile = currentFile.getParentFile();
        }
        
        if (updateInErrorFolder(current, file, containsErrors, true))
            toRefresh.add(currentFile.toURL());
        
        updateInErrorFolder(output[1].getParentFile(), file, containsErrors, false);
            
        FileObject rootFO = URLMapper.findFileObject(root);
        
        //XXX:
        if (rootFO != null) {
            Project p = FileOwnerQuery.getOwner(rootFO);
            
            if (p != null) {
                FileObject currentFO = rootFO;
                FileObject projectDirectory = p.getProjectDirectory();
                
                if (FileUtil.isParentOf(projectDirectory, rootFO)) {
                    while (currentFO != null && currentFO != projectDirectory) {
                        toRefresh.add(currentFO.getURL());
                        currentFO = currentFO.getParent();
                    }
                }
                
                toRefresh.add(projectDirectory.getURL());
            }
        }
        
        return toRefresh;
    }
    
    private boolean updateInErrorFolder(File current, URL file, boolean inError, boolean recursive) throws IOException {
        File dep = getDep(current, recursive);
        
        if (!dep.canRead() && !inError)
            return false;
        
        Set<URL> read = new HashSet<URL>();
        
//            LOG.log(Level.FINE, "getErrors, error file={0}", input.getAbsolutePath());
            
        if (dep.canRead()) {
            BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(dep), "UTF-8"));
            try {
                String line;
                
                while ((line = pw.readLine()) != null) {
                    try {
                        read.add(new URL(line));
                    } catch (MalformedURLException malformedURL) {
                        LOG.warning("Malformed URL: " + line +" in: " + dep.getAbsolutePath());
                    }
                }
            } finally {
                pw.close();
            }
        }
        
        boolean modified;
        
        if (inError) {
            modified = read.add(file);
        } else {
            modified = read.remove(file);
        }
        
        if (modified) {
            if (read.isEmpty()) {
                dep.delete();
            } else {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dep), "UTF-8"));
                
                try {
                    for (URL u : read) {
                        pw.println(u.toExternalForm());
                    }
                } finally {
                    pw.close();
                }
            }
        }
        
        return modified;
    }
    
    //XXX: slow, should be rewritten:
    public List<URL> getAllFilesWithRecord(URL root) throws IOException {
        try {
            List<URL> result = new LinkedList<URL>();
            URI rootURI = root.toURI();
            File[] cacheRoot = computePersistentFile(root, root);
            URI cacheRootURI = cacheRoot[0].toURI();
            Queue<File> todo = new LinkedList<File>();
            
            todo.add(cacheRoot[0]);
            
            while (!todo.isEmpty()) {
                File f = todo.poll();
                
                assert f != null;
                
                if (f.isFile()) {
                    if (f.getName().endsWith("err")) {
                        String relative = cacheRootURI.relativize(f.toURI()).getPath();
                        
                        relative = relative.replaceAll("err$", "java");
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
        File[] cacheRoot = computePersistentFile(root, root);
        File dep = getDep(cacheRoot[0], true);
        List<URL> result = new LinkedList<URL>();
        
        if (dep.canRead()) {
            BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(dep), "UTF-8"));
            try {
                String line;
                
                while ((line = pw.readLine()) != null) {
                    result.add(new URL(line));
                }
            } finally {
                pw.close();
            }
        }
        
        return result;
    }
    
    private File getDep(File folder, boolean recursive) {
        return new File(folder, recursive ? "recursive.dep" : "nonrecursive.dep");
    }
    
    public boolean isInError(FileObject file, boolean recursive) {
        LOG.log(Level.FINE, "file={0}, recursive={1}", new Object[] {file, Boolean.valueOf(recursive)});
        
        if (file.isData()) {
            return !getErrors(file, true).isEmpty();
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
                File cacheRoot = Index.getClassFolder(root.getURL(), true);
                
                if (cacheRoot == null) {
                    //index does not exist:
                    return false;
                }
                
                File cacheFile = getDep(new File(new File(cacheRoot.getParentFile(), "errors"), resourceName), recursive);
                
                LOG.log(Level.FINE, "cache file={0}, canRead={1}", new Object[] {cacheFile, Boolean.valueOf(cacheFile.canRead())});
                
                return cacheFile.canRead();
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
                return false;
            }
        }
    }
    
    private File[] computePersistentFile(URL root, URL file) throws IOException {
        try {
            URI fileURI = file.toURI();
            URI u = root.toURI();
            String resourceName = u.relativize(fileURI).getPath();
            int lastDot = resourceName.lastIndexOf('.');
            if (lastDot != (-1)) {
                resourceName = resourceName.substring(0, lastDot);
            }
            File cacheRoot = Index.getClassFolder(root);
            File errorsRoot = new File(cacheRoot.getParentFile(), "errors");
            File cacheFile = new File(errorsRoot, resourceName + ".err");
            
            return new File[] {errorsRoot, cacheFile};
        } catch (URISyntaxException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    
    private File computePersistentFile(FileObject file) throws IOException {
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
        
        if (cp == null)
            return null;
        
        FileObject root = cp.findOwnerRoot(file);
        
        if (root == null) {
            LOG.log(Level.FINE, "file={0} does not have a root on its own source classpath", file);
            return null;
        }
        
        String resourceName = cp.getResourceName(file, File.separatorChar, false);
        File cacheRoot = Index.getClassFolder(root.getURL());
        File cacheFile = new File(new File(cacheRoot.getParentFile(), "errors"), resourceName + ".err");
        
        return cacheFile;
    }
    
}
