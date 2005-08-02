/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
