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

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

import static org.openide.util.ImageUtilities.assignToolTipToImage;
import static org.openide.util.ImageUtilities.loadImage;
import static org.openide.util.ImageUtilities.mergeImages;
import static org.openide.util.NbBundle.getMessage;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.masterfs.providers.AnnotationProvider.class, position=100)
public class ErrorAnnotator extends AnnotationProvider /*implements FileStatusListener*/ {

    private static final Logger LOG = Logger.getLogger(ErrorAnnotator.class.getName());

    private static final String ERROR_BADGE_URL = "org/netbeans/modules/parsing/impl/resources/error-badge.gif";
    
    public ErrorAnnotator() {
    }

    public String annotateName(String name, Set files) {
        return null;
    }
    
    @Override
    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        if (!Settings.isBadgesEnabled())
            return null;
        
        boolean inError = false;
        boolean singleFile = files.size() == 1;

        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            inError = isInError(folder, false, true);
            singleFile = false;
        } else {
            for (Object o : files) {
                if (o instanceof FileObject) {
                    FileObject f = (FileObject) o;
                    
                    if (f.isFolder()) {
                        singleFile = false;
                        if (isInError(f, true, !inError)) {
                            inError = true;
                            continue;
                        }
                        if (inError)
                            continue;
                    } else {
                        if (f.isData()) {
                            if (isInError(f, true, !inError)) {
                                inError = true;
                            }
                        }
                    }
                }
            }
        }
        
        Logger.getLogger(ErrorAnnotator.class.getName()).log(Level.FINE, "files={0}, in error={1}", new Object[] {files, inError});
        
        if (inError) {
            //badge:
            URL errorBadgeIconURL = ErrorAnnotator.class.getResource("/" + ERROR_BADGE_URL);
            assert errorBadgeIconURL != null;
            String errorBadgeSingleTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + getMessage(ErrorAnnotator.class, "TP_ErrorBadgeSingle");
            Image errorBadge = loadImage(ERROR_BADGE_URL);
            assert errorBadge != null;
            String errorBadgeFolderTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + getMessage(ErrorAnnotator.class, "TP_ErrorBadgeFolder");
            Image i = mergeImages(icon, singleFile ? assignToolTipToImage(errorBadge, errorBadgeSingleTP) : assignToolTipToImage(errorBadge, errorBadgeFolderTP), 0, 8);
            Iterator<? extends AnnotationProvider> it = Lookup.getDefault().lookupAll(AnnotationProvider.class).iterator();
            boolean found = false;
            
            while (it.hasNext()) {
                AnnotationProvider p = it.next();
                
                if (found) {
                    Image res = p.annotateIcon(i, iconType, files);
                    
                    if (res != null) {
                        return res;
                    }
                } else {
                    found = p == this;
                }
            }
            
            return i;
        }
        
        return null;
    }

    public String annotateNameHtml(String name, Set files) {
        return null;
    }

    public Action[] actions(Set files) {
        return null;
    }

    public InterceptionListener getInterceptionListener() {
        return null;
    }
    
    public void updateAllInError() {
        try {
            File[] roots = File.listRoots();
            for (File root : roots) {
                FileObject rootFO = FileUtil.toFileObject(root);
                
                if (rootFO != null) {
                    fireFileStatusChanged(new FileStatusEvent(rootFO.getFileSystem(), true, false));
                }
            }
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
    value="DMI_COLLECTION_OF_URLS"
    /*,justification="URLs have never host part"*/)
    public synchronized void updateInError(Set<URL> urls)  {
        Set<FileObject> toRefresh = new HashSet<FileObject>();
        for (Iterator<FileObject> it = knownFiles2Error.keySet().iterator(); it.hasNext(); ) {
            FileObject f = it.next();
            try {
                final URL furl = f.getURL();
                assert PathRegistry.noHostPart(furl) : furl;
                if (urls.contains(furl)) {
                    toRefresh.add(f);
                    Integer i = knownFiles2Error.get(f);
                    
                    if (i != null) {
                        knownFiles2Error.put(f, i | INVALID);
                        
                        enqueue(f);
                    }
                }
            } catch (IOException e) {
                LOG.log(Level.INFO, e.getMessage(), e);
            }
        }
    }
    
    public void fireFileStatusChanged(Set<FileObject> fos) {
        if (fos.isEmpty())
            return ;
        try {
            fireFileStatusChanged(new FileStatusEvent(fos.iterator().next().getFileSystem(), fos, true, false));
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        }
    }
    
    public static ErrorAnnotator getAnnotator() {
        for (AnnotationProvider ap : Lookup.getDefault().lookupAll(AnnotationProvider.class)) {
            if (ap.getClass() == ErrorAnnotator.class) {
                return (ErrorAnnotator) ap;
            }
        }
        
        return null;
    }
    
    private static final int IN_ERROR_REC = 1;
    private static final int IN_ERROR_NONREC = 2;
    private static final int INVALID = 4;
    
    private Map<FileObject, Integer> knownFiles2Error = new WeakHashMap<FileObject, Integer>();
    
    private void enqueue(FileObject file) {
        if (toProcess == null) {
            toProcess = new LinkedList<FileObject>();
            WORKER.schedule(50);
        }
        
        toProcess.add(file);
    }
    
    private synchronized boolean isInError(FileObject file, boolean recursive, boolean forceValue) {
        boolean result = false;
        Integer i = knownFiles2Error.get(file);

        if (i != null) {
            result = (i & (recursive ? IN_ERROR_REC : IN_ERROR_NONREC)) != 0;
            
            if ((i & INVALID) == 0)
                return result;
        }
        
        if (!forceValue) {
            if (i == null) {
                knownFiles2Error.put(file, null);
            }
            return result;
        }

        enqueue(file);
        return result;
    }
    
    private long cumulativeTime;
    private Collection<FileObject> toProcess = null;

    private final RequestProcessor WORKER_THREAD = new RequestProcessor("ErrorAnnotator worker", 1);
    private final RequestProcessor.Task WORKER = WORKER_THREAD.create(new Runnable() {
        public void run() {
            long startTime = System.currentTimeMillis();
            Collection<FileObject> toProcess;
            
            synchronized (ErrorAnnotator.this) {
                toProcess = ErrorAnnotator.this.toProcess;
                ErrorAnnotator.this.toProcess = null;
            }
            
            for (FileObject f : toProcess) {
                synchronized (ErrorAnnotator.this) {
                    Integer currentState = knownFiles2Error.get(f);

                    if (currentState != null && (currentState & INVALID) == 0) {
                        continue;
                    }
                }

                ensureListensOnFS(f);

                boolean recError = false;
                boolean nonRecError = false;
                if (f.isData()) {
                    recError = nonRecError = TaskCache.getDefault().isInError(f, true);
                } else {
                    ClassPath source = Utilities.getSourceClassPathFor (f);
                    
                    if (source == null) {
                        //presumably not under an indexed root:
                        Project p = FileOwnerQuery.getOwner(f);

                        if (p != null) {
                            for (FileObject root : Utilities.findIndexedRootsUnderDirectory(p, f)) {
                                recError |= TaskCache.getDefault().isInError(root, true);
                            }
                        }
                    } else {
                        recError = TaskCache.getDefault().isInError(f, true);
                        nonRecError = TaskCache.getDefault().isInError(f, false);
                    }
                }

                Integer value = (recError ? IN_ERROR_REC : 0) | (nonRecError ? IN_ERROR_NONREC : 0);
                boolean stateChanged;
                
                synchronized (ErrorAnnotator.this) {
                    Integer origInteger = knownFiles2Error.get(f);
                    int orig;

                    if (origInteger != null) {
                        orig = origInteger & ~INVALID;
                    } else {
                        orig = 0;
                    }

                    stateChanged = orig != value;
                    knownFiles2Error.put(f, value);
                }

                if (stateChanged) {
                    fireFileStatusChanged(Collections.singleton(f));
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger(ErrorAnnotator.class.getName()).log(Level.FINE, "time spent in error annotations computation: {0}, cumulative time: {1}", new Object[] {(endTime - startTime), (cumulativeTime += (endTime - startTime))});
        }
    });

    private Map<FileSystem, FileChangeListener> system2RecursiveListener = new WeakHashMap<FileSystem, FileChangeListener>();

    private void ensureListensOnFS(FileObject f) {
        try {
            FileSystem fs = f.getFileSystem();
            if (!system2RecursiveListener.containsKey(fs)) {
                FileChangeListener l = new RootAddedDeletedListener();
                
                system2RecursiveListener.put(fs, l);
                fs.addFileChangeListener(l);
            }
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.FINE, null, ex);
        }
    }

    private final class RootAddedDeletedListener extends FileChangeAdapter {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            update(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            update(fe);
        }

        private void update(FileEvent fe) {
            final RepositoryUpdater ru = RepositoryUpdater.getDefault();
            final FileObject fo = fe.getFile();
            if (!ru.isCacheFile(fo) && ru.getOwningSourceRoot(fo) == null) {
                try {
                    update(fe.getFile().getURL());
                } catch (FileStateInvalidException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
            }
        }

        private void update(final URL root) {
            assert PathRegistry.noHostPart(root) : root;
            WORKER_THREAD.post(new Runnable() {

                @Override
                @org.netbeans.api.annotations.common.SuppressWarnings(
                value="DMI_COLLECTION_OF_URLS"
                /*,justification="URLs have never host part"*/)
                public void run() {
                    try {
                        Set<URL> toRefresh = new HashSet<URL>();
                        URL current = root;

                        toRefresh.add(current);
                        toRefresh.add(current = new URL(current, ".")); //NOI18N
                        
                        for (int depth = current.getPath().split("/").length - 1; depth > 0; depth--) {  //NOI18N
                            current = new URL(current, ".."); //NOI18N
                            toRefresh.add(current);
                        }

                        updateInError(toRefresh);
                    } catch (MalformedURLException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            });
        }
    }
    
}
