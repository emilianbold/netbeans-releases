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

import java.awt.Image;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import junit.framework.*;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.*;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewFactoryTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewFactoryTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewFactoryTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testcreateMultiView () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createMultiView(descs, null);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createMultiView(null, null);
        assertNull(tc);
    }

    
    public void testCreateMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyClose close = new MyClose();
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1, close);
        assertNotNull(tc);
        
        tc.open();
        // just one element as shown..
        tc.close();
        // the close handler is not used, becasue all the elements are in consistent state
        assertFalse(close.wasUsed);
        
    }
    
   public void testCreateCloneableMultiView () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        CloneableTopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createCloneableMultiView(descs, null);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createCloneableMultiView(null, null);
        assertNull(tc);
    }

    
    public void testCreateCloneableMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyClose close = new MyClose();
        TopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1, close);
        assertNotNull(tc);
        
        tc.open();
        // just one element as shown..
        tc.close();
        // the close handler is not used, becasue all the elements are in consistent state
        assertFalse(close.wasUsed);
        
    }    


//    public void testCreateSafeCloseState () throws Exception {
//        CloseOperationState state = MultiViewFactory.createSafeCloseState();
//        assertNotNull(state);
//        assertTrue(state.canClose());
//        assertNotNull(state.getDiscardAction());
//        assertNotNull(state.getProceedAction());
//        assertNotNull(state.getCloseWarningID());
//        
//    }

    
    public void testCreateUnsafeCloseState () throws Exception {
        CloseOperationState state = MultiViewFactory.createUnsafeCloseState("ID_UNSAFE", 
                                            MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        assertNotNull(state);
        assertFalse(state.canClose());
        assertNotNull(state.getDiscardAction());
        assertNotNull(state.getProceedAction());
        assertEquals("ID_UNSAFE", state.getCloseWarningID());
        
        state = MultiViewFactory.createUnsafeCloseState( null, null, null);
        assertNotNull(state);
        assertFalse(state.canClose());
        assertNotNull(state.getDiscardAction());
        assertNotNull(state.getProceedAction());
        assertNotNull(state.getCloseWarningID());
        
    }
    
    
    private class MyClose implements CloseOperationHandler {
        
        public boolean wasUsed = false;
        public int supposed = 0;
        public boolean canClose = true;
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            wasUsed = true;
            return canClose;
        }
        
        
    }
    
}

