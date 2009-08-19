/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.usages.fcs.FileChangeSupport;
import org.netbeans.modules.java.source.usages.fcs.FileChangeSupportEvent;
import org.netbeans.modules.java.source.usages.fcs.FileChangeSupportListener;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 *
 * @author Jan Lahoda
 */
public class BuildArtifactMapperImpl {
    
    private static final String ASK_BEFORE_RUN_WITH_ERRORS = "askBeforeRunWithErrors"; //NOI18N
    private static final String DIRTY_ROOT = "dirty"; //NOI18N

    private static final Logger LOG = Logger.getLogger(BuildArtifactMapperImpl.class.getName());
    
    private static final String TAG_FILE_NAME = ".netbeans_automatic_build"; //NOI18N
    private static final String SIG = "." + FileObjects.SIG; //NOI18N
//    private static final Map<URL, File> source2Target = new HashMap<URL, File>();
    private static final Map<URL, Set<ArtifactsUpdated>> source2Listener = new HashMap<URL, Set<ArtifactsUpdated>>();

    private static final long MINIMAL_TIMESTAMP = 2000L;

    public static synchronized void addArtifactsUpdatedListener(URL sourceRoot, ArtifactsUpdated listener) {
        Set<ArtifactsUpdated> listeners = source2Listener.get(sourceRoot);
        
        if (listeners == null) {
            source2Listener.put(sourceRoot, listeners = new HashSet<ArtifactsUpdated>());
        }
        
        listeners.add(listener);
    }

    public static synchronized void removeArtifactsUpdatedListener(URL sourceRoot, ArtifactsUpdated listener) {
        Set<ArtifactsUpdated> listeners = source2Listener.get(sourceRoot);
        
        if (listeners == null) {
            return ;
        }
        
        listeners.remove(listener);
        
        if (listeners.isEmpty()) {
            source2Listener.remove(sourceRoot);
        }
    }

    private static boolean protectAgainstErrors(File targetFolder, FileObject[][] sources) throws MalformedURLException {
        Preferences pref = NbPreferences.forModule(BuildArtifactMapperImpl.class).node(BuildArtifactMapperImpl.class.getSimpleName());

        if (!pref.getBoolean(ASK_BEFORE_RUN_WITH_ERRORS, true)) {
            return true;
        }
        
        sources(targetFolder, sources);
        
        for (FileObject file : sources[0]) {
            if (TaskCache.getDefault().isInError(file, true)) {
                JButton btnRunAnyway = new JButton();
                org.openide.awt.Mnemonics.setLocalizedText(btnRunAnyway, org.openide.util.NbBundle.getMessage(BuildArtifactMapperImpl.class, "BTN_RunAnyway"));
                btnRunAnyway.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BuildArtifactMapperImpl.class, "ACSN_BTN_RunAnyway"));
                btnRunAnyway.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BuildArtifactMapperImpl.class, "ACSD_BTN_RunAnyway"));

                JButton btnCancel = new JButton();
                org.openide.awt.Mnemonics.setLocalizedText(btnCancel, org.openide.util.NbBundle.getMessage(BuildArtifactMapperImpl.class, "BTN_Cancel"));
                btnCancel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BuildArtifactMapperImpl.class, "ACSN_BTN_Cancel"));
                btnCancel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BuildArtifactMapperImpl.class, "ACSD_BTN_Cancel"));

                ContainsErrorsWarning panel = new ContainsErrorsWarning();
                DialogDescriptor dd = new DialogDescriptor(panel,
                                                           NbBundle.getMessage(BuildArtifactMapperImpl.class, "TITLE_ContainsErrorsWarning"),
                                                           true,
                                                           new Object[] {btnRunAnyway, btnCancel},
                                                           btnRunAnyway,
                                                           DialogDescriptor.DEFAULT_ALIGN,
                                                           null,
                                                           null);

                dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                
                Object option = DialogDisplayer.getDefault().notify(dd);
                
                if (option == btnRunAnyway) {
                    pref.putBoolean(ASK_BEFORE_RUN_WITH_ERRORS, panel.getAskBeforeRunning());

                    return true;
                }

                return false;
            }
        }

        return true;
    }

    private static void sources(File targetFolder, FileObject[][] sources) throws MalformedURLException {
        if (sources[0] == null) {
            URL targetFolderURL = FileUtil.urlForArchiveOrDir(targetFolder);
            
            sources[0] = SourceForBinaryQuery.findSourceRoots(targetFolderURL).getRoots();
        }
    }
    
    private static File getTarget(URL source) {
        Result binaryRoots = BinaryForSourceQuery.findBinaryRoots(source);
        
        File result = null;
        
        for (URL u : binaryRoots.getRoots()) {
            File f = FileUtil.archiveOrDirForURL(u);

            try {
                if (FileUtil.isArchiveFile(f.toURI().toURL())) {
                    continue;
                }
            
                if (f != null && result != null) {
                    Logger.getLogger(BuildArtifactMapperImpl.class.getName()).log(Level.WARNING, "More than one binary directory for root: {0}", source.toExternalForm());
                    return null;
                }

                result = f;
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return result;
    }
    
    @SuppressWarnings("deprecation")
    public static Boolean ensureBuilt(URL sourceRoot, boolean cleanCompletely) throws IOException {
        File targetFolder = getTarget(sourceRoot);
        
        if (targetFolder == null) {
            return null;
        }
        
        try {
            SourceUtils.waitScanFinished();
        } catch (InterruptedException e) {
            //Not Important
            LOG.log(Level.FINE, null, e);
            return null;
        }

        if (JavaIndex.ensureAttributeValue(sourceRoot, DIRTY_ROOT, null)) {
            IndexingManager.getDefault().refreshIndexAndWait(sourceRoot, null);
        }

        FileObject[][] sources = new FileObject[1][];
        
        if (!protectAgainstErrors(targetFolder, sources)) {
            return false;
        }
        
        File tagFile = new File(targetFolder, TAG_FILE_NAME);

        if (tagFile.exists()) {
            return true;
        }
        
        delete(targetFolder, cleanCompletely);
        
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            throw new IOException("Cannot create destination folder: " + targetFolder.getAbsolutePath());
        }

        sources(targetFolder, sources);

        for (FileObject sr : sources[0]) {
            File index = JavaIndex.getClassFolder(sr.getURL(), true);

            if (index == null) {
                return null;
            }

            copyRecursively(index, targetFolder);
        }
        
        new FileOutputStream(tagFile).close();
        
        return true;
    }
    
    @SuppressWarnings("deprecation")
    public static Boolean clean(URL sourceRoot) throws IOException {
        File targetFolder = getTarget(sourceRoot);

        if (targetFolder == null) {
            return null;
        }

        try {
            SourceUtils.waitScanFinished();
        } catch (InterruptedException e) {
            //Not Important
            LOG.log(Level.FINE, null, e);
            return false;
        }

        File tagFile = new File(targetFolder, TAG_FILE_NAME);

        if (!tagFile.exists()) {
            return null;
        }

        delete(targetFolder, false);
        delete(tagFile, true);

        return null;
    }

    public static void classCacheUpdated(URL sourceRoot, File cacheRoot, Iterable<File> deleted, Iterable<File> updated) {
        File targetFolder = deleted.iterator().hasNext() || updated.iterator().hasNext() ? getTarget(sourceRoot) : null;
        
        if (targetFolder == null) {
            return ;
        }
        
        if (!new File(targetFolder, TAG_FILE_NAME).exists()) {
            return ;
        }
        
        List<File> updatedFiles = new LinkedList<File>();

        assert targetFolder != null;
        
        for (File deletedFile : deleted) {
            File toDelete = resolveFile(targetFolder, relativizeFile(cacheRoot, deletedFile));
            
            toDelete.delete();
            updatedFiles.add(toDelete);
        }
        
        for (File updatedFile : updated) {
            File target = resolveFile(targetFolder, relativizeFile(cacheRoot, updatedFile));

            try {
                copyFile(updatedFile, target);
                updatedFiles.add(target);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (updatedFiles.size() > 0) {
            Set<ArtifactsUpdated> listeners;

            synchronized (BuildArtifactMapperImpl.class) {
                listeners = source2Listener.get(sourceRoot);

                if (listeners != null) {
                    listeners = new HashSet<ArtifactsUpdated>(listeners);
                }
            }

            if (listeners != null) {
                for (ArtifactsUpdated listener : listeners) {
                    listener.artifactsUpdated(updatedFiles);
                }
            }
        }
    }

    private static void copyFile(File updatedFile, File target) throws IOException {
        final File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create folder: " + parent.getAbsolutePath());
            }
        }
            
        InputStream ins = null;
        OutputStream out = null;

        try {
            ins = new FileInputStream(updatedFile);
            out = new FileOutputStream(target);

            FileUtil.copy(ins, out);
            //target.setLastModified(MINIMAL_TIMESTAMP); see 156153
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private static void copyRecursively(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                if (!target.mkdirs()) {
                    throw new IOException("Cannot create folder: " + target.getAbsolutePath());
                }
            } else {
                if (!target.isDirectory()) {
                    throw new IOException("Cannot create folder: " + target.getAbsolutePath() + ", already exists as a file.");
                }
            }
            
            File[] listed = source.listFiles();
            
            if (listed == null) {
                return ;
            }
            
            for (File f : listed) {
                String name = f.getName();
                if (name.endsWith(SIG))
                    name = name.substring(0, name.length() - FileObjects.SIG.length()) + FileObjects.CLASS;
                copyRecursively(f, new File(target, name));
            }
        } else {
            if (target.isDirectory()) {
                throw new IOException("Cannot create file: " + target.getAbsolutePath() + ", already exists as a folder.");
            }

            copyFile(source, target);
        }
    }

    private static void delete(File file, boolean cleanCompletely) throws IOException {
        if (file.isDirectory()) {
            File[] listed = file.listFiles();

            if (listed == null) {
                return;
            }

            for (File f : listed) {
                delete(f, cleanCompletely);
            }

            if (cleanCompletely) {
                file.delete();
            }
        } else {
            if (cleanCompletely || file.getName().endsWith(".class")) {
                file.delete();
            }
        }
    }
    
    //XXX: copied from PropertyUtils:
    private static final Pattern RELATIVE_SLASH_SEPARATED_PATH = Pattern.compile("[^:/\\\\.][^:/\\\\]*(/[^:/\\\\.][^:/\\\\]*)*"); // NOI18N
    
    /**
     * Find an absolute file path from a possibly relative path.
     * @param basedir base file for relative filename resolving; must be an absolute path
     * @param filename a pathname which may be relative or absolute and may
     *                 use / or \ as the path separator
     * @return an absolute file corresponding to it
     * @throws IllegalArgumentException if basedir is not absolute
     */
    public static File resolveFile(File basedir, String filename) throws IllegalArgumentException {
        if (basedir == null) {
            throw new NullPointerException("null basedir passed to resolveFile"); // NOI18N
        }
        if (filename == null) {
            throw new NullPointerException("null filename passed to resolveFile"); // NOI18N
        }
        if (!basedir.isAbsolute()) {
            throw new IllegalArgumentException("nonabsolute basedir passed to resolveFile: " + basedir); // NOI18N
        }
        if (filename.endsWith(SIG))
            filename = filename.substring(0, filename.length() - FileObjects.SIG.length()) + FileObjects.CLASS;
        File f;
        if (RELATIVE_SLASH_SEPARATED_PATH.matcher(filename).matches()) {
            // Shortcut - simple relative path. Potentially faster.
            f = new File(basedir, filename.replace('/', File.separatorChar));
        } else {
            // All other cases.
            String machinePath = filename.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            f = new File(machinePath);
            if (!f.isAbsolute()) {
                f = new File(basedir, machinePath);
            }
            assert f.isAbsolute();
        }
        return FileUtil.normalizeFile(f);
    }
    
    /**
     * Produce a machine-independent relativized version of a filename from a basedir.
     * Unlike {@link URI#relativize} this will produce "../" sequences as needed.
     * @param basedir a directory to resolve relative to (need not exist on disk)
     * @param file a file or directory to find a relative path for
     * @return a relativized path (slash-separated), or null if it is not possible (e.g. different DOS drives);
     *         just <samp>.</samp> in case the paths are the same
     * @throws IllegalArgumentException if the basedir is known to be a file and not a directory
     */
    public static String relativizeFile(File basedir, File file) {
        if (basedir.isFile()) {
            throw new IllegalArgumentException("Cannot relative w.r.t. a data file " + basedir); // NOI18N
        }
        if (basedir.equals(file)) {
            return "."; // NOI18N
        }
        StringBuffer b = new StringBuffer();
        File base = basedir;
        String filepath = file.getAbsolutePath();
        while (!filepath.startsWith(slashify(base.getAbsolutePath()))) {
            base = base.getParentFile();
            if (base == null) {
                return null;
            }
            if (base.equals(file)) {
                // #61687: file is a parent of basedir
                b.append(".."); // NOI18N
                return b.toString();
            }
            b.append("../"); // NOI18N
        }
        URI u = base.toURI().relativize(file.toURI());
        assert !u.isAbsolute() : u + " from " + basedir + " and " + file + " with common root " + base;
        b.append(u.getPath());
        if (b.charAt(b.length() - 1) == '/') {
            // file is an existing directory and file.toURI ends in /
            // we do not want the trailing slash
            b.setLength(b.length() - 1);
        }
        return b.toString();
    }

    private static String slashify(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separatorChar;
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.FileBuiltQueryImplementation.class, position=1000)
    public static final class FileBuildQueryImpl implements FileBuiltQueryImplementation {

        private final ThreadLocal<Boolean> recursive = new ThreadLocal<Boolean>();
        private final Map<FileObject, Reference<Status>> file2Status = new WeakHashMap<FileObject, Reference<Status>>();

        public synchronized Status getStatus(FileObject file) {
            Reference<Status> statusRef = file2Status.get(file);
            Status result = statusRef != null ? statusRef.get() : null;

            if (result != null) {
                return result;
            }
            
            if (recursive.get() != null) {
                return null;
            }

            recursive.set(true);

            try {
                Status delegate = FileBuiltQuery.getStatus(file);

                if (delegate == null) {
                    return null;
                }

                ClassPath source = ClassPath.getClassPath(file, ClassPath.SOURCE);
                FileObject owner = source != null ? source.findOwnerRoot(file) : null;

                if (owner == null) {
                    return delegate;
                }

                File target = getTarget(owner.getURL());
                File tagFile = FileUtil.normalizeFile(new File(target, TAG_FILE_NAME));

                Reference<FileChangeListenerImpl> ref = file2Listener.get(tagFile);
                FileChangeListenerImpl l = ref != null ? ref.get() : null;

                if (l == null) {
                    file2Listener.put(tagFile, new WeakReference<FileChangeListenerImpl>(l = new FileChangeListenerImpl()));
                    listener2File.put(l, tagFile);
                    FileChangeSupport.DEFAULT.addListener(l, tagFile);
                }

                file2Status.put(file, new WeakReference<Status>(result = new FileBuiltQueryStatusImpl(delegate, tagFile, l)));

                return result;
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            } finally {
                recursive.remove();
            }
        }
        
    }

    private static Map<File, Reference<FileChangeListenerImpl>> file2Listener = new WeakHashMap<File, Reference<FileChangeListenerImpl>>();
    private static Map<FileChangeListenerImpl, File> listener2File = new WeakHashMap<FileChangeListenerImpl, File>();
    
    private static final class FileBuiltQueryStatusImpl implements FileBuiltQuery.Status, ChangeListener {

        private final FileBuiltQuery.Status delegate;
        private final File tag;
        private final FileChangeListenerImpl fileListener;
        private final ChangeSupport cs = new ChangeSupport(this);

        public FileBuiltQueryStatusImpl(Status delegate, File tag, FileChangeListenerImpl fileListener) {
            this.delegate = delegate;
            this.tag = tag;
            this.fileListener = fileListener;

            delegate.addChangeListener(this);
            fileListener.addListener(this);
        }

        public boolean isBuilt() {
            return delegate.isBuilt() || tag.canRead();
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }
        
    }

    private static final class FileChangeListenerImpl implements FileChangeSupportListener {

        private RequestProcessor NOTIFY = new RequestProcessor(FileChangeListenerImpl.class.getName());
        
        private Set<ChangeListener> notify = new WeakSet<ChangeListener>();
        
        public void fileCreated(FileChangeSupportEvent event) {
            notifyListeners();
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            notifyListeners();
        }

        public void fileModified(FileChangeSupportEvent event) {
            notifyListeners();
        }

        private synchronized void addListener(ChangeListener l) {
            notify.add(l);
        }

        private synchronized void notifyListeners() {
            final Set<ChangeListener> toNotify = new HashSet<ChangeListener>(notify);

            NOTIFY.post(new Runnable() {
                public void run() {
                    for (ChangeListener l : toNotify) {
                        l.stateChanged(null);
                    }
                }
            });
        }
    }
}
