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
import javax.swing.Action;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author nenik
 */
@ServiceProvider(service=AnnotationProvider.class)
public class Watcher extends AnnotationProvider {

    private final Ext ext;

    public Watcher() {
        Notifier<?> notifier = getNotifierForPlatform();
        ext = notifier == null ? null : new Ext(notifier);

        if (notifier != null) { // turn off the recursive refresh on focus
            // XXX
        }
    }

    public @Override String annotateName(String name, Set<? extends FileObject> files) {
        return name;
    }
    public @Override Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        return icon;
    }
    public @Override String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return name;
    }
    public @Override Action[] actions(Set<? extends FileObject> files) {
        return null;
    }

    public @Override InterceptionListener getInterceptionListener() {
        return ext;
    }

    private static class Ext<KEY> extends ProvidedExtensions implements Runnable {
        private final Notifier<KEY> impl;
        private final Map<FileObject, KEY> map = new WeakHashMap<FileObject, KEY>();


        public Ext(Notifier<KEY> impl) {
            this.impl = impl;
            new Thread(this).start();
        }

        // will be called from WHM implementation on lost key
        private void fileObjectFreed(KEY key) {
            try {
                if (key != null) impl.removeWatch(key);
            } catch (IOException ioe) {
              Exceptions.printStackTrace(ioe);  
            }
        }

        public @Override long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
            FileObject fo = FileUtil.toFileObject(dir);
            String path = dir.getAbsolutePath();

            if (map.containsKey(fo)) return -1;

            try {
                map.put(fo, impl.addWatch(path));
            } catch (IOException ex) {
                // XXX: handle resource overflow gracefully
                Exceptions.printStackTrace(ex);

            }

            return -1;
        }

        @Override public void run() {
            for (;;) {
                try {
                    String path = impl.nextEvent();

                    // XXX: handle the all-dirty message
                    if (path == null) { // all dirty

                    } else {
                        // don't ask for nonexistent FOs
                        File file = new File(path);
                        FileObject fo = FileObjectFactory.getInstance(file).getCachedOnly(file);

                        // may be null
                        if (map.containsKey(fo)) fo.refresh();
                    }
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }
        }
    }

    /** select the best available notifier implementation on given platform/JDK
     * or null if there is no such service available.
     *
     * @return a suitable {@link Notifier} implementation or <code>null</code>.
     */
    private static Notifier getNotifierForPlatform() {
        if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
            return new LinuxNotifier();
        }

        return null;
    }


}
