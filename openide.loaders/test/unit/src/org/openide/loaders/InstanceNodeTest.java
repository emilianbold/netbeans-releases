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
package org.openide.loaders;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstanceNodeTest extends NbTestCase {
    Node node;

    public InstanceNodeTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        DataObject dobj = InstanceDataObject.create (DataFolder.findFolder (root), null, A.class);
        node = dobj.getNodeDelegate ();
        assertTrue ("Is InstanceNode", node instanceof InstanceNode);
    }

    /**
     * Test of getDisplayName method, of class org.openide.loaders.InstanceNode.
     */
    public void testGetDisplayName () throws Exception {
        Node instance = node;
        
        String expResult = "Ahoj";
        // node's name is calculated later, let's wait
        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                
            }
            
        });
        String result = instance.getDisplayName();
        assertEquals(expResult, result);
    }
    
    public static class A extends CallableSystemAction {
        public void performAction () {
        }

        public String getName () {
            assertTrue ("Called from AWT", SwingUtilities.isEventDispatchThread ());
            return "Ahoj";
        }

        public HelpCtx getHelpCtx () {
            assertTrue ("Called from AWT", SwingUtilities.isEventDispatchThread ());
            return HelpCtx.DEFAULT_HELP;
        }
        
    }

}
