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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;




import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;


import org.openide.util.LookupListener;

import org.openide.windows.TopComponent;



/** 
 *
 * @author Milos Kleint
 */
public class MultiViewActionMapTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewActionMapTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewActionMapTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testElementIsTopComponent() throws Exception {
        MVElemTopComponent elem1 = new MVElemTopComponent();
        MVElemTopComponent elem2 = new MVElemTopComponent();
        MVElemTopComponent elem3 = new MVElemTopComponent();
        doTestActionMap(elem1, elem2, elem3);
    }
    
    public void testElementIsNotTC() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        doTestActionMap(elem1, elem2, elem3);
    }    
    
    private void doTestActionMap(MultiViewElement elem1, MultiViewElement elem2, MultiViewElement elem3) {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        // WARNING: as anything else the first element's action map is set only after the tc is opened..
        tc.open();
        
        Action act = new TestAction("MultiViewAction");
        // add action to the MVTC map
        tc.getActionMap().put("testkey", act);
        ActionMap obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        assertNotNull(obj);
        assertEquals(obj.getClass(), MultiViewActionMap.class);
        Action res = (Action)obj.get("testkey");
        assertNotNull(res);
        assertEquals("MultiViewAction", res.getValue(Action.NAME));
        // remove action from the MVTC map
        tc.getActionMap().remove("testkey");
        obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        res = (Action)obj.get("testkey");
        assertNull(res);
        
        // make sure the action in MVTC has higher priority..
        JComponent elemtc = elem1.getVisualRepresentation();
        Action innerAct = new TestAction("InnerAction");
        elemtc.getActionMap().put("testkey", innerAct);
        assertNotNull(elemtc.getActionMap().get("testkey"));
        obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        // check if anything there in elemen'ts actionmap
        assertNotNull(obj);
        res = (Action)obj.get("testkey");
        assertNotNull(res);
        // put actin to the mvtc actionmap as well..
        tc.getActionMap().put("testkey", act);
        assertNotNull(obj);
        res = (Action)obj.get("testkey");
        assertNotNull(res);
        assertEquals("MultiViewAction", res.getValue(Action.NAME));
        //remove from mvtc's map..
        tc.getActionMap().remove("testkey");
        res = (Action)obj.get("testkey");
        assertNotNull(res);
        assertEquals("InnerAction", res.getValue(Action.NAME));
        // now switch to the other element...
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        res = (Action)obj.get("testkey");
        assertNull(res); // is not defined in element2
    }
    
    public void testActionMapChanges() throws Exception {
        MVElemTopComponent elem1 = new MVElemTopComponent();
        MVElemTopComponent elem2 = new MVElemTopComponent();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        // WARNING: as anything else the first element's action map is set only after the tc is opened..
        tc.open();
        
        Lookup.Result result = tc.getLookup().lookup(new Lookup.Template(ActionMap.class));
        LookListener list = new LookListener();
        result.addLookupListener(list);
        list.resetCount();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        assertEquals(1, list.getCount());
        Accessor.DEFAULT.createPerspective(desc3);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertEquals(2, list.getCount());
    }

    
    
    private class TestAction extends  AbstractAction {
        public TestAction(String name) {
            super(name);
        }
        
        public void actionPerformed(ActionEvent event) {
            
        }
        
    }
    
    private class LookListener implements LookupListener {
        int count = 0;
        
        public void resetCount() {
            count = 0;
        }
        
        
        public int getCount() {
            return count;
        }
        
        public void resultChanged (LookupEvent ev) {
            count++;
        }
    }
    
 }

