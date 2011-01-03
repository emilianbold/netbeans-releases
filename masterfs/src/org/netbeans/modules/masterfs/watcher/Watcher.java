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

import java.awt.Image;
import java.io.File;
import java.io.IOException;
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
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
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

    private final Ext<?> ext;

    public Watcher() {
        // Watcher disabled manually or for some tests
        if (Boolean.getBoolean("org.netbeans.modules.masterfs.watcher.disable")) {
            ext = null;
            return;
        }
        
        ext = make(getNotifierForPlatform());
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
    
    public void shutdown() {
        if (ext != null) {
            try {
                ext.shutdown();
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

    private class Ext<KEY> extends ProvidedExtensions implements Runnable {
        private final Notifier<KEY> impl;
        private final Map<FileObject, KEY> map = new WeakHashMap<FileObject, KEY>();
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
            String path = dir.getAbsolutePath();

            if (fo == null && !dir.exists()) {
                return -1;
            }

            assert fo != null : "No fileobject for " + path;

            if (map.containsKey(fo)) {
                return -1;
            }

            try {
                map.put(fo, impl.addWatch(path));
            } catch (IOException ex) {
                // XXX: handle resource overflow gracefully
                LOG.log(Level.WARNING, "Cannot add filesystem watch for {0}", path);
                LOG.log(Level.INFO, "Exception", ex);
            }

            return -1;
        }

        @Override public void run() {
            while (!shutdown) {
                try {
                    String path = impl.nextEvent();
                    LOG.log(Level.FINEST, "nextEvent: {0}", path); 

                    // XXX: handle the all-dirty message
                    if (path == null) { // all dirty
                        enqueueAll(map.keySet());
                    } else {
                        // don't ask for nonexistent FOs
                        File file = new File(path);
                        FileObject fo = FileObjectFactory.getInstance(file).getCachedOnly(file);

                        // may be null
                        if (map.containsKey(fo)) {
                            enqueue(fo);
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
            impl.stop();
            watcher.join(1000);
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
            }
            LOG.log(Level.FINE, "Refreshing {0} directories", toRefresh.size());

            for (FileObject fileObject : toRefresh) {
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
        try {
            if (Utilities.isWindows()) {
                return new WindowsNotifier();
            }
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                return new LinuxNotifier();
            }
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                try {
                    final OSXNotifier notifier = new OSXNotifier();
                    notifier.start();
                    return notifier;
                } catch (IOException ioe) {
                    LOG.log(Level.INFO, null, ioe);
                }
            }
            if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                try {
                    return new FAMNotifier();
                } catch (Exception e) {
                    LOG.log(Level.INFO, null, e);
                } catch (LinkageError x) {
                    //this is normal not to have fam in the system, do not report
                }
            }
        } catch (LinkageError x) {
            LOG.warning(x.toString());
        }
        return null;
    }


}
