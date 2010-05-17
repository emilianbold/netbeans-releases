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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.implspi.NamedServicesProvider;

/** Use FolderLookup to find out intances of named services.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(
    service=NamedServicesProvider.class,
    position=200,
    supersedes="org.netbeans.modules.openide.filesystems.RecognizeInstanceFiles"
)
public final class RecognizeInstanceObjects extends NamedServicesProvider {
    private static final Logger LOG = Logger.getLogger(RecognizeInstanceObjects.class.getName());
    
    
    public Lookup create(String path) {
        return new OverObjects(path);
    }        
    
    
    private static final class OverObjects extends ProxyLookup 
    implements LookupListener, FileChangeListener {
        private static Lookup.Result<ClassLoader> CL = Lookup.getDefault().lookupResult(ClassLoader.class);
        
        private final String path;
        
        public OverObjects(String path) {
            super(delegates(path));
            this.path = path;
            CL.addLookupListener(WeakListeners.create(LookupListener.class, this, CL));
            try {
                FileSystem sfs = FileUtil.getConfigRoot().getFileSystem();
                sfs.addFileChangeListener(FileUtil.weakFileChangeListener(this, sfs));
            } catch (FileStateInvalidException x) {
                assert false : x;
            }
        }
        
        @SuppressWarnings("deprecation")
        private static Lookup[] delegates(String path) {
            Collection<? extends ClassLoader> allCL = CL.allInstances();
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                allCL = Collections.singleton(ccl);
            } else {
                if (allCL.isEmpty()) {
                    allCL = Collections.singleton(RecognizeInstanceObjects.class.getClassLoader());
                }
            }
            Lookup base = Lookups.metaInfServices(allCL.iterator().next(), "META-INF/namedservices/" + path); // NOI18N
            FileObject fo = FileUtil.getConfigFile(path);
            if (fo == null) {
                return new Lookup[] {base};
            }
            String s;
            if (path.endsWith("/")) { // NOI18N
                s = path.substring(0, path.length() - 1);
            } else {
                s = path;
            }
            return new Lookup[] {new org.openide.loaders.FolderLookup(DataFolder.findFolder(fo), s).getLookup(), base};
        }
    
        public void resultChanged(LookupEvent ev) {
            setLookups(delegates(path));
        }

        public void fileFolderCreated(FileEvent fe) {
            ch(fe);
        }
        public void fileDataCreated(FileEvent fe) {
            ch(fe);
        }
        public void fileChanged(FileEvent fe) {
            ch(fe);
        }
        public void fileDeleted(FileEvent fe) {
            ch(fe);
        }
        public void fileRenamed(FileRenameEvent fe) {
            ch(fe);
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            ch(fe);
        }
        private void ch(FileEvent e) {
            if ((e.getFile().getPath() + "/").startsWith(path)) { // NOI18N
                setLookups(delegates(path));
            }
        }

    } // end of OverObjects
}
