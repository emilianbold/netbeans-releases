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
public class MultiViewsTest extends NbTestCase {
    
    /** Creates a new instance of SFSTest */
    public MultiViewsTest(String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(MultiViewsTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    
    
    public void testcreateMultiViewHandler () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        
        tc = new TopComponent();
        hand = MultiViews.findMultiViewHandler(tc);
        assertNull(hand);
        
        hand = MultiViews.findMultiViewHandler(null);
        assertNull(hand);

    }

    
}

