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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.java.project.support.ui;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * @author Jan Becicka
 */
public class PackageRenameHandlerTest extends NbTestCase {

    private FileObject fo;
    private Node n;
    private PackageRenameHandlerImpl frh = new PackageRenameHandlerImpl();
    
    
    public PackageRenameHandlerTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        FileObject root = TestUtil.makeScratchDir(this);
        fo = FileUtil.createFolder(root, "test");// NOI18N
        
        SourceGroup group = GenericSources.group(null, root.createFolder("src"), "testGroup", "Test Group", null, null);
        Children ch = PackageView.createPackageView(group).getChildren();
        
        // Create folder
        FileUtil.createFolder(root, "src/foo");
        n = ch.findChild("foo");
        
        assertNotNull(n);
    }
    
    public void testRenameHandlerNotCalled() throws Exception {
        TestUtil.setLookup(new Object[0]);
        frh.called = false;
        
        n.setName("blabla");
        assertFalse(frh.called);
    }
    
    public void testRenameHandlerCalled() throws Exception {
        TestUtil.setLookup(new Object[] {frh});
        frh.called = false;
        
        n.setName("foo");// NOI18N
        assertTrue(frh.called);
    }
    
    private static final class PackageRenameHandlerImpl implements PackageRenameHandler {
        boolean called = false;
        public void handleRename(Node n, String newName) throws IllegalArgumentException {
            called = true;
        }
    }
    
}
