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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class BinaryPathNotifier {

    private static final BinaryPathNotifier instance = new BinaryPathNotifier();
    private static final Logger LOG = Logger.getLogger(BinaryPathNotifier.class.getName());

    private final List<FileChangeListener> listeners = new CopyOnWriteArrayList<FileChangeListener> ();
    private final Set<File> interestedIn = new HashSet<File>();
    private final FileChangeListener listener = new FL();


    private BinaryPathNotifier() {
    }

    public void addFileChangeListener(final FileChangeListener listener) {
        Parameters.notNull("listener", listener);
        this.listeners.add(listener);
    }

    public void removeFileChangeListener(final FileChangeListener listener) {
        Parameters.notNull("listener", listener);
        this.listeners.remove(listener);
    }

    public void registerRoots(final Iterable<? extends URL> roots) {
        Parameters.notNull("roots", roots); //NOI18N
        for (URL root : roots) {
            final URL archiveURL = FileUtil.getArchiveFile(root);
            try {
                if (archiveURL != null) {
                    final File archiveFile = new File (archiveURL.toURI());
                    if (interestedIn.add(archiveFile)) {
                        FileUtil.addFileChangeListener(listener, archiveFile);
                    }
                }
            } catch (URISyntaxException e) {
                LOG.warning("Cannot register root: " + root);
            }
        }
    }

    public void unregisterRoots(final Iterable<? extends URL> roots) {
        Parameters.notNull("roots", roots); //NOI18N
        for (URL root : roots) {
            final URL archiveURL = FileUtil.getArchiveFile(root);
            try {
                if (archiveURL != null) {
                    final File archiveFile = new File (archiveURL.toURI());
                    if (interestedIn.remove(archiveFile)) {
                        FileUtil.removeFileChangeListener(listener, archiveFile);
                    }
                }
            } catch (URISyntaxException e) {
                LOG.warning("Cannot unregister root: " + root);
            }
        }
    }    

    private class FL extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            for (FileChangeListener listener : listeners) {
                listener.fileDataCreated(fe);
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            for (FileChangeListener listener : listeners) {
                listener.fileChanged(fe);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            for (FileChangeListener listener : listeners) {
                listener.fileDeleted(fe);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            for (FileChangeListener listener : listeners) {
                listener.fileRenamed(fe);
            }
        }

    }

    public static BinaryPathNotifier getDefault() {
        return instance;
    }

}
