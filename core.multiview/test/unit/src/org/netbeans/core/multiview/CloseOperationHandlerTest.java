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
public class CloseOperationHandlerTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public CloseOperationHandlerTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(CloseOperationHandlerTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testCreateMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new NonClosableElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new NonClosableElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyCloseHandler close = new MyCloseHandler();
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1, close);
        assertNotNull(tc);
        
         tc.open();
        // just one element as shown..
        close.supposed = 1;
        tc.close();
        assertTrue(close.wasUsed);
        
        tc.open();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestActive(handler.getPerspectives()[2]);
        // 1 shall be checked - elem3 can be closed.
        close.supposed = 1;
        // do not allow closing..
        close.canClose = false;
        
        tc.close();  
        assertTrue(tc.isOpened());

        
        handler.requestActive(handler.getPerspectives()[1]);
        // 2 shall be checked.
        close.supposed = 2;
        // allow closing..
        close.canClose = true;
        
        tc.close();  
        assertTrue(!tc.isOpened());
        
    }

    
    private class MyCloseHandler implements CloseOperationHandler {
        
        public boolean wasUsed = false;
        public int supposed = 0;
        public boolean canClose = true;
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            wasUsed = true;
            if (supposed != elements.length) {
                throw new IllegalStateException("A different number of elements returned. Expected=" + supposed + " but was:" + elements.length);
            }
            return canClose;
        }
        
        
    }
    
    private class NonClosableElem extends MVElem {
        
        public CloseOperationState canCloseElement() {
            return MultiViewFactory.createUnsafeCloseState("ID", MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        }
        
    }
    
}

