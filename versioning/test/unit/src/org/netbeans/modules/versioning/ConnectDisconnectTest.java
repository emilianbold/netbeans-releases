/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning;

import java.awt.event.ActionEvent;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.VersioningSystem;

/**
 *
 * @author ondra
 */
public class ConnectDisconnectTest extends NbTestCase {
    private static final String VERSIONED_COMMON_FOLDER_SUFFIX = "-connectdisconnect-versioned-common";

    public ConnectDisconnectTest (String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        super.setUp();
    }
    
    public void testSingleVCS () throws Exception {
        File root = new File(getWorkDir(), "root-connectdisconnect-versioned1");
        File folder = new File(root, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();
        VersioningManager.getInstance().getOwner(file);
        
        assertEquals(DisconnectableVCS1.instance, VersioningManager.getInstance().getOwner(file));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
        // disconnect
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS1.instance, root);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(null, VersioningManager.getInstance().getOwner(file));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
        // reconnect
        new VersioningMainMenu.ConnectAction(DisconnectableVCS1.instance, root, "").actionPerformed(new ActionEvent(DisconnectableVCS1.instance, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS1.instance, VersioningManager.getInstance().getOwner(file));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
    }

    public void testMultipleVCS () throws Exception {
        File root = new File(getWorkDir(), "root-connectdisconnect-versioned-common");
        File folder = new File(root, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();

        VersioningSystem owner = VersioningManager.getInstance().getOwner(file);
        assertTrue(owner.toString(), owner == DisconnectableVCS1.instance || owner == DisconnectableVCS2.instance);
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
        assertEquals(root, DisconnectableVCS2.instance.getTopmostManagedAncestor(file));
        // disconnect vcs1
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS1.instance, root);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(DisconnectableVCS2.instance, VersioningManager.getInstance().getOwner(file));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
        assertEquals(root, DisconnectableVCS2.instance.getTopmostManagedAncestor(file));
        // disconnect vcs2
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS2.instance, root);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(null, VersioningManager.getInstance().getOwner(file));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
        assertEquals(root, DisconnectableVCS2.instance.getTopmostManagedAncestor(file));
        // reconnect vcs1
        new VersioningMainMenu.ConnectAction(DisconnectableVCS1.instance, root, "").actionPerformed(new ActionEvent(DisconnectableVCS1.instance, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS1.instance, VersioningManager.getInstance().getOwner(file));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
        assertEquals(root, DisconnectableVCS2.instance.getTopmostManagedAncestor(file));
        // reconnect vcs2
        new VersioningMainMenu.ConnectAction(DisconnectableVCS2.instance, root, "").actionPerformed(new ActionEvent(DisconnectableVCS2.instance, ActionEvent.ACTION_PERFORMED, null));
        owner = VersioningManager.getInstance().getOwner(file);
        assertTrue(owner.toString(), owner == DisconnectableVCS1.instance || owner == DisconnectableVCS2.instance);
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(file));
        assertEquals(root, DisconnectableVCS2.instance.getTopmostManagedAncestor(file));
    }

    public void testHierarchicalVCS () throws Exception {
        File root = new File(getWorkDir(), "root-connectdisconnect-versioned1");
        File folder = new File(root, "root-connectdisconnect-versioned2");
        folder.mkdirs();
        VersioningManager.getInstance().getOwner(folder);

        assertEquals(DisconnectableVCS1.instance, VersioningManager.getInstance().getOwner(root));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(folder));
        assertEquals(DisconnectableVCS2.instance, VersioningManager.getInstance().getOwner(folder));
        assertEquals(folder, DisconnectableVCS2.instance.getTopmostManagedAncestor(folder));
        // disconnect vcs2
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS2.instance, folder);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(DisconnectableVCS1.instance, VersioningManager.getInstance().getOwner(folder));
        assertEquals(DisconnectableVCS1.instance, VersioningManager.getInstance().getOwner(root));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(folder));
        assertEquals(folder, DisconnectableVCS2.instance.getTopmostManagedAncestor(folder));
        // disconnect vcs1
        VersioningConfig.getDefault().disconnectRepository(DisconnectableVCS1.instance, root);
        VersioningManager.getInstance().versionedRootsChanged();
        assertEquals(null, VersioningManager.getInstance().getOwner(folder));
        assertEquals(null, VersioningManager.getInstance().getOwner(root));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(folder));
        assertEquals(folder, DisconnectableVCS2.instance.getTopmostManagedAncestor(folder));
        // reconnect vcs2
        new VersioningMainMenu.ConnectAction(DisconnectableVCS2.instance, folder, "").actionPerformed(new ActionEvent(DisconnectableVCS2.instance, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS2.instance, VersioningManager.getInstance().getOwner(folder));
        assertEquals(null, VersioningManager.getInstance().getOwner(root));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(folder));
        assertEquals(folder, DisconnectableVCS2.instance.getTopmostManagedAncestor(folder));
        // reconnect vcs1
        new VersioningMainMenu.ConnectAction(DisconnectableVCS1.instance, root, "").actionPerformed(new ActionEvent(DisconnectableVCS1.instance, ActionEvent.ACTION_PERFORMED, null));
        assertEquals(DisconnectableVCS2.instance, VersioningManager.getInstance().getOwner(folder));
        assertEquals(DisconnectableVCS1.instance, VersioningManager.getInstance().getOwner(root));
        assertEquals(root, DisconnectableVCS1.instance.getTopmostManagedAncestor(folder));
        assertEquals(folder, DisconnectableVCS2.instance.getTopmostManagedAncestor(folder));
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.spi.VersioningSystem.class)
    public static class DisconnectableVCS1 extends VersioningSystem {
        private static DisconnectableVCS1 instance;
        public static final String VERSIONED_FOLDER_SUFFIX = "-connectdisconnect-versioned1";

        public DisconnectableVCS1 () {
            instance = this;
        }
        
        @Override
        public File getTopmostManagedAncestor(File file) {
            File topmost = null;
            for (; file != null; file = file.getParentFile()) {
                if (file.getName().endsWith(VERSIONED_FOLDER_SUFFIX) || file.getName().endsWith(VERSIONED_COMMON_FOLDER_SUFFIX)) {
                    topmost = file;
                }
            }
            return topmost;
        }
    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.spi.VersioningSystem.class)
    public static class DisconnectableVCS2 extends VersioningSystem {
        private static DisconnectableVCS2 instance;
        public static final String VERSIONED_FOLDER_SUFFIX = "-connectdisconnect-versioned2";

        public DisconnectableVCS2 () {
            instance = this;
        }
        
        @Override
        public File getTopmostManagedAncestor(File file) {
            File topmost = null;
            for (; file != null; file = file.getParentFile()) {
                if (file.getName().endsWith(VERSIONED_FOLDER_SUFFIX) || file.getName().endsWith(VERSIONED_COMMON_FOLDER_SUFFIX)) {
                    topmost = file;
                }
            }
            return topmost;
        }
    }
}
