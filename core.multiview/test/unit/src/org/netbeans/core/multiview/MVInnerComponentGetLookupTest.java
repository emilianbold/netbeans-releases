/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.multiview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JTextField;

import junit.framework.*;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import org.netbeans.junit.*;
import org.openide.nodes.Node;

import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Milos Kleint
 */
public class MVInnerComponentGetLookupTest extends org.openide.windows.TopComponentGetLookupTest {
    
    private TopComponent mvtc;
    private TopComponent top2;
    private TopComponent top3;
    MultiViewDescription desc1;
    MultiViewDescription desc2;
    MultiViewDescription desc3;    
    
    public MVInnerComponentGetLookupTest(String testName) {
        super(testName);
    }
    
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new NbTestSuite(MVInnerComponentGetLookupTest.class);
    }
    
    
    protected boolean runInEQ () {
        return true;
    }
    
    protected int checkAtLeastCount() {
        return 0;
    }
    
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        final MVElemTopComponent elem1 = new MVElemTopComponent();
        final MVElemTopComponent elem2 = new MVElemTopComponent();
        final MVElemTopComponent elem3 = new MVElemTopComponent();
        desc1 = new MVDesc("desc1", null, 0, elem1);
        desc2 = new MVDesc("desc2", null, 0, elem2);
        desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent mvtop = MultiViewFactory.createMultiView(descs, desc1);
        top = (TopComponent)elem1;
        top2 = (TopComponent)elem2;
        top3 = (TopComponent)elem3;
        lookup = mvtop.getLookup();
        mvtop.open();
        mvtop.requestActive();
        mvtc = mvtop;
    }
    
    public void testMVTCActivatedNodes() throws Exception {
        ProChange change = new ProChange();
        
        TopComponent.getRegistry().addPropertyChangeListener(change);
        Node[] nodes = new Node[] {new N("one"), new N("two")};
        
        assertEquals(TopComponent.getRegistry().getActivated(), mvtc);
        top.setActivatedNodes(nodes);
        Node[] ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 2);
        
        assertTrue(ret[0] == nodes[1] || ret[0] == nodes[0]);
        assertTrue(ret[1] == nodes[0] || ret[1] == nodes[1]);
        Node[] activ = TopComponent.getRegistry().getActivatedNodes();
        assertEquals(activ.length, 2);
        assertTrue(activ[0] == nodes[1] || activ[0] == nodes[0]);
        assertTrue(activ[1] == nodes[0] || activ[1] == nodes[1]);
//        assertEquals(1, change.count);
        
        
        
        Node[] nodes2 = new Node[] {new N("three")};
        top.setActivatedNodes(nodes2);
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 1);
        assertEquals(ret[0], nodes2[0]);
        activ = TopComponent.getRegistry().getActivatedNodes();
        assertEquals(activ.length, 1);
        assertEquals(activ[0], nodes2[0]);
//        assertEquals(2, change.count);
        
    }
    
    public void testMVTCActivatedNodesOnElementChange() throws Exception {    
        Node[] nodes1 = new Node[] {new N("one"), new N("two")};
        Node[] nodes2 = new Node[] {new N("three"), new N("four"), new N("five")};
        Node[] nodes3 = new Node[] {new N("six")};
        top.setActivatedNodes(nodes1);
        top2.setActivatedNodes(nodes2);
        top3.setActivatedNodes(nodes3);

        assertEquals(TopComponent.getRegistry().getActivated(), mvtc);
        // first element selected now..
        Node[] ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 2);
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvtc);
        // test related hack, easy establishing a  connection from Desc->perspective
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc2));
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 3);
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc3));
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 1);
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 2);
        
    }

    
    private class ProChange implements PropertyChangeListener {
        public int count = 0;
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if ("activatedNodes".equals(evt.getPropertyName())) {
                count = count + 1;  
            }
        }
        
    }
    
}
