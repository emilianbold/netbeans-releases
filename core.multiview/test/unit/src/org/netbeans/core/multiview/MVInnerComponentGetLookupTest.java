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
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        final MVElemTopComponent elem1 = new MVElemTopComponent();
        final MVElemTopComponent elem2 = new MVElemTopComponent();
        final MVElemTopComponent elem3 = new MVElemTopComponent();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent mvtop = MultiViewFactory.createMultiView(descs, desc1);
        top = (TopComponent)elem1;
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
        assertEquals(ret[0], nodes[0]);
        Node[] activ = TopComponent.getRegistry().getActivatedNodes();
        assertEquals(activ.length, 2);
        assertEquals(activ[0], nodes[0]);
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
    
    private class ProChange implements PropertyChangeListener {
        public int count = 0;
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if ("activatedNodes".equals(evt.getPropertyName())) {
                count = count + 1;  
            }
        }
        
    }
    
}
