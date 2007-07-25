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
 * Portions Copyrighted 2007 Nokia Siemens Networks Oy
 */
package org.netbeans.core.startup.layers;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.actions.SystemAction;

/**
 * Tests whether the system property 
 * <code>org.netbeans.core.systemfilesystem.custom</code> correctly
 * installs a custom filesystem.
 * 
 * @author David Strupl
 */
public class CustomWritableSystemFileSystemTest extends NbTestCase {

    private SystemFileSystem sfs;

    public CustomWritableSystemFileSystemTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", "memory");
        System.setProperty("org.netbeans.core.systemfilesystem.custom", PoohFileSystem.class.getName());
        sfs = (SystemFileSystem) Repository.getDefault().getDefaultFileSystem();
    }

    public void testCustomSFSWritableLayerPresent() throws Exception {
        FileSystem writable = sfs.createWritableOn(null);
        assertTrue(writable instanceof ModuleLayeredFileSystem);
        ModuleLayeredFileSystem mlf = (ModuleLayeredFileSystem) writable;
        assertTrue(mlf.getWritableLayer() instanceof PoohFileSystem);
    }

    public static class PoohFileSystem extends FileSystem {

        public String getDisplayName() {
            return "Pooh";
        }

        public boolean isReadOnly() {
            return false;
        }

        public FileObject getRoot() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public FileObject findResource(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public SystemAction[] getActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
