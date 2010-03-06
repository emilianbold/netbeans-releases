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

package org.netbeans.core.osgi;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.xml.sax.SAXException;

/**
 * Default repository to which layers can be added or removed.
 */
class OSGiRepository extends Repository {

    private static final Logger LOG = Logger.getLogger(OSGiRepository.class.getName());
    public static final OSGiRepository DEFAULT = new OSGiRepository();

    private final SFS fs;

    private OSGiRepository() {
        this(new SFS());
    }

    private OSGiRepository(SFS fs) {
        super(fs);
        this.fs = fs;
    }

    public void addLayers(URL... resources) {
        if (resources.length > 0) {
//            System.err.println("adding layers: " + Arrays.toString(resources));
            fs.addLayers(resources);
        }
    }

    public void removeLayers(URL... resources) {
        if (resources.length > 0) {
//            System.err.println("removing layers: " + Arrays.toString(resources));
            fs.removeLayers(resources);
        }
    }

    private static final class SFS extends MultiFileSystem implements LookupListener {

        static {
            @SuppressWarnings("deprecation") Object _1 = FileSystem.Environment.class; // FELIX-2128
            @SuppressWarnings("deprecation") Object _2 = org.openide.filesystems.FileSystemCapability.class;
            Object _3 = FileStatusListener.class;
        }

        private static final class Layers extends MultiFileSystem {
            Layers() {
                setPropagateMasks(true);
            }
            void _setDelegates(Collection<FileSystem> delegates) {
                setDelegates(delegates.toArray(new FileSystem[delegates.size()]));
            }
        }

        private final Map<String,FileSystem> fss = new HashMap<String,FileSystem>();
        private final FileSystem userdir;
        private final Layers layers;
        private final Lookup.Result<FileSystem> dynamic = Lookup.getDefault().lookupResult(FileSystem.class);

        @SuppressWarnings("LeakingThisInConstructor")
        SFS() {
            dynamic.addLookupListener(this);
            layers = new Layers();
            File config = null;
            String netbeansUser = System.getProperty("netbeans.user");
            if (netbeansUser != null) {
                config = new File(netbeansUser, "config");
            }
            if (config != null && (config.isDirectory() || config.mkdirs())) {
                LocalFileSystem lfs = new LocalFileSystem();
                try {
                    lfs.setRootDirectory(config);
                } catch (Exception x) {
                    LOG.log(Level.WARNING, "Could not set userdir: " + config, x);
                }
                userdir = lfs;
            } else {
                // no persisted configuration
                userdir = FileUtil.createMemoryFileSystem();
            }
            resetAll();
        }

        private synchronized void addLayers(URL... resources) {
            LOG.log(Level.FINE, "addLayers: {0}", Arrays.asList(resources));
            for (URL resource : resources) {
                try {
                    fss.put(resource.toString(), new XMLFileSystem(resource));
                } catch (SAXException x) {
                    LOG.log(Level.WARNING, "Could not parse layer: " + resource, x);
                }
            }
            resetLayers();
        }

        private synchronized void removeLayers(URL... resources) {
            for (URL resource : resources) {
                fss.remove(resource.toString());
            }
            resetLayers();
        }

        private void resetLayers() {
            layers._setDelegates(fss.values());
        }

        private void resetAll() {
            List<FileSystem> delegates = new ArrayList<FileSystem>();
            delegates.add(userdir);
            Collection<? extends FileSystem> dyn = dynamic.allInstances();
            for (FileSystem fs : dyn) {
                if (Boolean.TRUE.equals(fs.getRoot().getAttribute("fallback"))) { // NOI18N
                    continue;
                }
                delegates.add(fs);
            }
            delegates.add(layers);
            for (FileSystem fs : dyn) {
                if (Boolean.TRUE.equals(fs.getRoot().getAttribute("fallback"))) { // NOI18N
                    delegates.add(fs);
                }
            }
            setDelegates(delegates.toArray(new FileSystem[delegates.size()]));
            assert getRoot().isValid();
            assert !isReadOnly() : delegates;
        }

        public @Override void resultChanged(LookupEvent ev) {
            resetAll();
        }

    }

}
