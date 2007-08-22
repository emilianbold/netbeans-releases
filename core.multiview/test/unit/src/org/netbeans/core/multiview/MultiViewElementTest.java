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


package org.netbeans.core.multiview;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;



/**
 *
 * @author Milos Kleint
 */
public class MultiViewElementTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewElementTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewElementTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testRequestVisible() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        assertEquals("componentOpened-componentShowing-", elem1.getLog());
        assertEquals("",elem2.getLog());

        // initilize the elements..
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        elem1.resetLog();
        elem2.resetLog();
        elem3.resetLog();
        
        elem2.doRequestVisible();
        assertEquals("componentHidden-", elem1.getLog());
        assertEquals("componentShowing-", elem2.getLog());
        assertEquals("", elem3.getLog());
        
        elem3.doRequestVisible();
        assertEquals("componentHidden-", elem1.getLog());
        assertEquals("componentShowing-componentHidden-", elem2.getLog());
        assertEquals("componentShowing-", elem3.getLog());
        
        elem1.doRequestVisible();
        assertEquals("componentShowing-componentHidden-", elem3.getLog());
        assertEquals("componentHidden-componentShowing-", elem1.getLog());
        
    }

    
    public void testRequestActive() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        tc.requestActive();
        assertEquals("",elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // initilize the elements..
        // test related hack, easy establishing a  connection from Desc->perspective
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc2));
        elem1.resetLog();
        elem2.resetLog();
        elem3.resetLog();
//        System.err.println("start Caring.........................");
        elem1.doRequestActive();
//        System.err.println("elem1=" + elem1.getLog());
//        System.err.println("elem2=" + elem2.getLog());

        assertEquals("componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // do request active the same component, nothing should happen.
        elem1.doRequestActive();
        assertEquals("componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
    }
    
    public void testUndoRedo() throws Exception {
        UndoRedoImpl redo1 = new UndoRedoImpl();
        redo1.undo = false;
        UndoRedoImpl redo2 = new UndoRedoImpl();
        redo2.redo = false;
        ChangeListenerImpl changeList = new ChangeListenerImpl();
        MVElem elem1 = new MVElem();
        elem1.setUndoRedo(redo1);
        MVElem elem2 = new MVElem();
        elem2.setUndoRedo(redo2);
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        UndoRedo result = tc.getUndoRedo();
        assertNotNull(result);
        assertFalse(result.canRedo());
        assertTrue(result.canUndo());
        result.addChangeListener(changeList);
        assertEquals(1, redo2.listeners.size());
        tc.open();
        tc.requestActive();
        assertEquals(0, changeList.count);
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        
        assertTrue(result.canRedo());
        assertFalse(result.canUndo());
        assertEquals(1, redo1.listeners.size());
        assertEquals(0, redo2.listeners.size());
        assertEquals(1, changeList.count);
        
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertFalse(result.canRedo());
        assertFalse(result.canUndo());
        assertEquals(0, redo2.listeners.size());
        assertEquals(0, redo1.listeners.size());
        assertEquals(2, changeList.count);
        
    }    
    
    public void testUpdateTitle() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);

        tc.open();
        assertEquals(null, tc.getDisplayName());
        
        
        elem2.observer.updateTitle("test1");
        assertEquals("test1", tc.getDisplayName());
        
        // switch to desc3 to initilize the element..
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);

        // test related hack, easy establishing a  connection from Desc->perspective
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        
        elem3.observer.updateTitle("test2");
        assertEquals("test2", tc.getDisplayName());
        
    }
    
    private class UndoRedoImpl implements UndoRedo {
        public List listeners = new ArrayList();
        public boolean undo = true;
        public boolean redo = true;
        
        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        public boolean canRedo() {
            return redo;
        }
        public boolean canUndo() {
            return undo;
        }
        public String getRedoPresentationName() {
            return "String";
        }
        public String getUndoPresentationName() {
            return "String2";
        }
        public void redo() throws CannotRedoException {
        }
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        public void undo() throws CannotUndoException {
        }
    }
    
    private class ChangeListenerImpl implements ChangeListener {
        public int count = 0;
        public void stateChanged(ChangeEvent e) {
            count++;
        }
        
    }
    
}

