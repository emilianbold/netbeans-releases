/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.watcher;

import org.netbeans.modules.masterfs.providers.Notifier;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author nenik
 */
@ServiceProviders({
    @ServiceProvider(service=AnnotationProvider.class),
    @ServiceProvider(service=Watcher.class)
})
public final class Watcher extends AnnotationProvider {
    static final Logger LOG = Logger.getLogger(Watcher.class.getName());
    private static final Map<FileObject,int[]> MODIFIED = new WeakHashMap<FileObject, int[]>();
    private final Ext<?> ext;
    
    public Watcher() {
        // Watcher disabled manually or for some tests
        if (Boolean.getBoolean("org.netbeans.modules.masterfs.watcher.disable")) {
            ext = null;
            return;
        }
        ext = make(getNotifierForPlatform());
    }
    
    private static Ext<?> ext() {
        final Watcher w = Lookup.getDefault().lookup(Watcher.class);
        return w == null ? null : w.ext;
        
    }
    
    public static boolean isEnabled() {
        return ext() != null;
    }
    
    public static boolean isWatched(FileObject fo) {
        Ext<?> ext = ext();
        if (ext == null) {
            return false;
        }
        if (fo.isData()) {
            fo = fo.getParent();
        }
        return ext.isWatched(fo);
    }
    public static void register(FileObject fo) {
        Ext<?> ext = ext();
        if (ext == null) {
            return;
        }
        if (fo.isData()) {
            fo = fo.getParent();
        }
        ext.register(fo);
    }
    
    public static void unregister(FileObject fo) {
        Ext<?> ext = ext();
        if (ext == null) {
            return;
        }
        if (fo.isData()) {
            fo = fo.getParent();
            if (!fo.isFolder()) {
                return;
            }
        }
        ext.unregister(fo);
    }

    public static File wrap(File f, FileObject fo) {
        if (f instanceof FOFile) {
            return f;
        }
        return new FOFile(f, fo);
    }

    public @Override String annotateName(String name, Set<? extends FileObject> files) {
        return null;
    }
    public @Override Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        return null;
    }
    public @Override String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return null;
    }
    public @Override Action[] actions(Set<? extends FileObject> files) {
        return null;
    }

    public @Override InterceptionListener getInterceptionListener() {
        return ext;
    }
    
    public static void shutdown() {
        if (isEnabled()) {
            try {
                ext().shutdown();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Error on shutdown", ex);
            } catch (InterruptedException ex) {
                LOG.log(Level.INFO, "Error on shutdown", ex);
            }
        }
    }
 
    private <KEY> Ext<KEY> make(Notifier<KEY> impl) {
        return impl == null ? null : new Ext<KEY>(impl);
    }

    final void clearQueue() throws IOException {
        ext.clearQueue();
    }

    private class Ext<KEY> extends ProvidedExtensions implements Runnable {
        private final ReferenceQueue<FileObject> REF = new ReferenceQueue<FileObject>();
        private final Notifier<KEY> impl;
        private final Object LOCK = new Object();
        private final Set<NotifierKeyRef> references = new HashSet<NotifierKeyRef>();
        private final Thread watcher;
        private volatile boolean shutdown;

        @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
        public Ext(Notifier<KEY> impl) {
            this.impl = impl;
            (watcher = new Thread(this, "File Watcher")).start(); // NOI18N
        }

        /*
        // will be called from WHM implementation on lost key
        private void fileObjectFreed(KEY key) throws IOException {
            if (key != null) {
                impl.removeWatch(key);
            }
        }
         */

        public @Override long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
            assert dir instanceof FOFile;
            FileObject fo = ((FOFile)dir).fo;
            if (fo == null && !dir.exists()) {
                return -1;
            }
            assert fo != null : "No fileobject for " + dir;
            register(fo);
            return -1;
        }
        private boolean isWatched(FileObject fo) {
            if (!fo.isFolder()) {
                assert fo.isValid() : "Should be a folder: " + fo + " type: " + fo.getClass();
            }
            try {
                clearQueue();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Exception while clearing the queue", ex);
            }
            synchronized (LOCK) {
                NotifierKeyRef<KEY> kr = new NotifierKeyRef<KEY>(fo, null, null, impl);
                return getReferences().contains(kr);
            }
        }
        
        final void register(FileObject fo) {
            if (fo.isValid() && !fo.isFolder()) {
                LOG.log(Level.INFO, "Should be a folder: {0} data: {1} folder: {2} valid: {3}", new Object[]{fo, fo.isData(), fo.isFolder(), fo.isValid()});
            }
            try {
                clearQueue();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Exception while clearing the queue", ex);
            }
            synchronized (LOCK) {
                NotifierKeyRef<KEY> kr = new NotifierKeyRef<KEY>(fo, null, null, impl);
                if (getReferences().contains(kr)) {
                    return;
                }

                try {
                    getReferences().add(new NotifierKeyRef<KEY>(fo, NotifierAccessor.getDefault().addWatch(impl, fo.getPath()), REF, impl));
                } catch (IOException ex) {
                    // XXX: handle resource overflow gracefully
                    LOG.log(Level.WARNING, "Cannot add filesystem watch for {0}: {1}", new Object[] {fo.getPath(), ex});
                    LOG.log(Level.FINE, null, ex);
                }
            }
        }
        
        final void unregister(FileObject fo) {
            assert !fo.isValid() || fo.isFolder() : "If valid, it should be a folder: " + fo + " clazz: " + fo.getClass();
            synchronized (LOCK) {
                final NotifierKeyRef[] equalOne = new NotifierKeyRef[1];
                NotifierKeyRef<KEY> kr = new NotifierKeyRef<KEY>(fo, null, null, impl) {
                    @Override
                    public boolean equals(Object obj) {
                        if (super.equals(obj)) {
                            equalOne[0] = (NotifierKeyRef)obj;
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                };
                if (!references.contains(kr)) {
                    return;
                }
                assert equalOne[0] != null;
                getReferences().remove(equalOne[0]);
                try {
                    equalOne[0].removeWatch();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Cannot remove filesystem watch for {0}", fo.getPath());
                    LOG.log(Level.INFO, "Exception", ex);
                }
            }
        }
        
        final void clearQueue() throws IOException {
            for (;;) {
                NotifierKeyRef kr = (NotifierKeyRef)REF.poll();
                if (kr == null) {
                    break;
                }
                synchronized (LOCK) {
                    getReferences().remove(kr);
                    kr.removeWatch();
                }
            }
        }

        @Override public void run() {
            while (!shutdown) {
                try {
                    clearQueue();
                    String path = NotifierAccessor.getDefault().nextEvent(impl);
                    LOG.log(Level.FINEST, "nextEvent: {0}", path); 
                    if (path == null) { // all dirty
                        Set<FileObject> set = new HashSet<FileObject>();
                        synchronized (LOCK) {
                            for (NotifierKeyRef kr : getReferences()) {
                                final FileObject ref = kr.get();
                                if (ref != null) {
                                    set.add(ref);
                                }
                            }
                        }
                        enqueueAll(set);
                    } else {
                        // don't ask for nonexistent FOs
                        File file = new File(path);
                        final FileObjectFactory factory = FileObjectFactory.getInstance(file);
                        FileObject fo = factory.getCachedOnly(file);
                        if (fo == null) {
                            fo = factory.getCachedOnly(file.getParentFile());
                        }
                        if (fo != null) {
                            synchronized (LOCK) {
                                NotifierKeyRef<KEY> kr = new NotifierKeyRef<KEY>(fo, null, null, impl);
                                if (getReferences().contains(kr)) {
                                    enqueue(fo);
                                }
                            }
                        }
                    }
                } catch (ThreadDeath td) {
                    throw td;
                } catch (InterruptedException ie) {
                    if (!shutdown) {
                        LOG.log(Level.INFO, "Interrupted", ie);
                    }
                } catch (Throwable t) {
                    LOG.log(Level.INFO, "Error dispatching FS changes", t);
                }
            }
        }

        final void shutdown() throws IOException, InterruptedException {
            shutdown = true;
            watcher.interrupt();
            NotifierAccessor.getDefault().stop(impl);
            watcher.join(1000);
        }

        private Set<NotifierKeyRef> getReferences() {
            assert Thread.holdsLock(LOCK);
            return references;
        }
    }

    private final Object lock = new Object();
    private Set<FileObject> pending; // guarded by lock
    private static RequestProcessor RP = new RequestProcessor("Pending refresh", 1);


    private RequestProcessor.Task refreshTask = RP.create(new Runnable() {
        public @Override void run() {
            Set<FileObject> toRefresh;
            synchronized(lock) {
                toRefresh = pending;
                pending = null;
                if (toRefresh == null) {
                    return;
                }
            }
            LOG.log(Level.FINE, "Refreshing {0} directories", toRefresh.size());

            for (FileObject fileObject : toRefresh) {
                if (isLocked(fileObject)) {
                    enqueue(fileObject);
                    continue;
                }
                LOG.log(Level.FINEST, "Refreshing {0}", fileObject);
                fileObject.refresh();
            }
            
            LOG.fine("Refresh finished");
        }
    });


    private void enqueue(FileObject fo) {
        assert fo != null;

        synchronized(lock) {
            if (pending == null) {
                refreshTask.schedule(1500);
                pending = new HashSet<FileObject>();
            }
            pending.add(fo);
        }
    }

    private void enqueueAll(Set<FileObject> fos) {
        assert fos != null;
        assert !fos.contains(null) : "No nulls";

        synchronized(lock) {
            if (pending == null) {
                refreshTask.schedule(1500);
                pending = new HashSet<FileObject>();
            }
            pending.addAll(fos);
        }
    }

    /** select the best available notifier implementation on given platform/JDK
     * or null if there is no such service available.
     *
     * @return a suitable {@link Notifier} implementation or <code>null</code>.
     */
    private static Notifier<?> getNotifierForPlatform() {
        for (Item<Notifier> item : Lookup.getDefault().lookupResult(Notifier.class).allItems()) {
            try {
                final Notifier notifier = item.getInstance();
                if (notifier != null) {
                    NotifierAccessor.getDefault().start(notifier);
                    return notifier;
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Notifier {0} refused to be initialized", item.getType()); // NOI18N
                LOG.log(Level.FINE, null, ex);
            } catch (Exception ex) {
                LOG.log(Level.INFO, "Exception while instantiating " + item, ex);
            } catch (LinkageError ex) {
                LOG.log(Level.INFO, "Linkage error for " + item, ex);
            }
        }
        return null;
    }
    
    public static synchronized void lock(FileObject fileObject) {
        if (fileObject.isData()) {
            fileObject = fileObject.getParent();
        }
        
        int[] arr = MODIFIED.get(fileObject);
        if (arr == null) {
            MODIFIED.put(fileObject, arr = new int[] { 0 });
        }
        arr[0]++;
    }
    
    static synchronized boolean isLocked(FileObject fo) {
        return MODIFIED.get(fo) != null;
    }

    public static synchronized void unlock(FileObject fo) {
        if (fo.isData()) {
            fo = fo.getParent();
        }
        
        int[] arr = MODIFIED.get(fo);
        if (arr == null) {
            return;
        }
        if (--arr[0] == 0) {
            MODIFIED.remove(fo);
        }
    }
}
