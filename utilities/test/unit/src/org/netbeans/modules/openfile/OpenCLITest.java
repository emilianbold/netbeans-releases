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

package org.netbeans.modules.openfile;

import java.awt.Component;
import java.io.File;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.UserCancelException;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** 
 *
 * @author Jaroslav Tulach
 */
public class OpenCLITest extends NbTestCase {
    File dir;
    
    public OpenCLITest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    protected void setUp() throws Exception {
        dir = new File(getWorkDir(), "tstdir");
        dir.mkdirs();
        
        MockServices.setServices(MockNodeOperation.class);
        MockNodeOperation.explored = null;
    }

    protected void tearDown() throws Exception {
    }
    
    public void testOpenFolder() throws Exception {
        CommandLine.getDefault().process(new String[] { "--open", dir.getPath() });

        assertNotNull("A node has been explored", MockNodeOperation.explored);
        
        FileObject root = MockNodeOperation.explored.getLookup().lookup(FileObject.class);
        assertNotNull("There is a file object in lookup", root);
        
        assertEquals("It is our dir", dir, FileUtil.toFile(root));
    }
    
    public static final class MockNodeOperation extends NodeOperation {
        public static Node explored;
        
        public boolean customize(Node n) {
            fail("No customize");
            return false;
        }

        public void explore(Node n) {
            assertNull("No explore before", explored);
            explored = n;
        }

        public void showProperties(Node n) {
            fail("no props");
        }

        public void showProperties(Node[] n) {
            fail("no props");
        }

        public Node[] select(String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top) throws UserCancelException {
            fail("no select");
            return null;
        }
        
    }
}
