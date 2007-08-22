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
import javax.swing.SwingUtilities;
import junit.framework.*;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
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
public class MultiViewHandlerTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewHandlerTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewHandlerTest.class);
        
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
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        assertEquals(hand.getPerspectives().length, 3);
        MultiViewPerspective pers = hand.getSelectedPerspective();

        assertEquals(Accessor.DEFAULT.extractDescription(pers), desc1);
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        assertEquals("componentOpened-componentShowing-", elem1.getLog());
        assertEquals("",elem2.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        
        assertEquals(Accessor.DEFAULT.extractDescription(hand.getSelectedPerspective()), desc2);
        assertEquals("componentOpened-componentShowing-componentHidden-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-", elem2.getLog());
        assertEquals("", elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertEquals("componentOpened-componentShowing-componentHidden-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentHidden-", elem2.getLog());
        assertEquals("componentOpened-componentShowing-", elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        assertEquals("componentOpened-componentShowing-componentHidden-", elem3.getLog());
        assertEquals("componentOpened-componentShowing-componentHidden-componentShowing-", elem1.getLog());
        
    }

    
    public void testRequestActive() throws Exception {
        final MVElem elem1 = new MVElem();
        final MVElem elem2 = new MVElem();
        final MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        assertEquals(hand.getPerspectives().length, 3);
        
        MultiViewPerspective pers = hand.getSelectedPerspective();

        assertEquals(Accessor.DEFAULT.extractDescription(pers), desc2);
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        tc.requestActive();
        assertEquals("",elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
//        System.err.println("start caring..........................");
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
//        System.err.println("elem1=" + elem1.getLog());
//        System.err.println("elem2=" + elem2.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());

        // do request active the same element, nothing should happen.
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        assertEquals("componentOpened-componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
    }
    
    
}

