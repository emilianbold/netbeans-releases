/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.explorer;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.text.DefaultEditorKit;

import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;


/**
 * Test whether the old behaviour of ExplorerPanel is correctly simulated
 * by new API. Inherits testing methods from ExplorerPanel tests, just
 * setup is changed.
 *
 * @author Jaroslav Tulach
 */
public class ExplorerActionsImplTest extends ExplorerPanelTest {
    
    public ExplorerActionsImplTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ExplorerActionsImplTest.class);
        return suite;
    }

    /** Creates a manager to operate on.
     */
    protected Object[] createManagerAndContext (boolean confirm) {
        ExplorerManager em = new ExplorerManager ();
        ActionMap map = new ActionMap ();
        map.put (DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put (DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put (DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));
        map.put ("delete", ExplorerUtils.actionDelete(em, confirm));
        
        return new Object[] { em, org.openide.util.lookup.Lookups.singleton(map) };
    }
    
    /** Instructs the actions to stop/
     */
    protected void stopActions(ExplorerManager em) {
        ExplorerUtils.activateActions (em, false);
    }
    /** Instructs the actions to start again.
     */
    protected void startActions (ExplorerManager em) {
        ExplorerUtils.activateActions (em, true);
    }
    
    
    public void testActionDeleteDoesNotAffectStateOfPreviousInstances () throws Exception {
        ExplorerManager em = new ExplorerManager ();
        Action a1 = ExplorerUtils.actionDelete(em, false);
        Action a2 = ExplorerUtils.actionDelete(em, true);
        
        Node node = new AbstractNode (Children.LEAF) {
            public boolean canDestroy () {
                return true;
            }
        };
        em.setRootContext(node);
        em.setSelectedNodes(new Node[] { node });
        
        assertTrue ("A1 enabled", a1.isEnabled());
        assertTrue ("A2 enabled", a2.isEnabled());
        
        // this should not show a dialog
        a1.actionPerformed (new java.awt.event.ActionEvent (this, 0, ""));
    }
}
