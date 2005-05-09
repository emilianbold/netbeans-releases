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

package org.netbeans.api.progress.aggregate;

import junit.framework.*;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.module.ProgressUIWorker;
import org.netbeans.progress.module.ProgressEvent;

import org.openide.util.Cancellable;


/**
 *
 * @author mkleint
 */
public class AggregateProgressHandleTest extends TestCase {
    
    public AggregateProgressHandleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Controller.defaultInstance = new Controller(new ProgressUIWorker() {
            public void processProgressEvent(ProgressEvent event) { }
            public void processSelectedProgressEvent(ProgressEvent event) { }
        });
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AggregateProgressHandleTest.class);
        
        return suite;
    }


    public void testContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1, contrib2}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib2.getRemainingParentWorkUnits());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib2.getRemainingParentWorkUnits());
        // the +1 deal is there because of the rounding, the last one gest the remainder
        assertEquals(AggregateProgressHandle.WORKUNITS /3 + 1, contrib3.getRemainingParentWorkUnits());
    }
    
    public void testDynamicContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS, contrib1.getRemainingParentWorkUnits());
    
        handle.start();
        contrib1.start(100);
        contrib1.progress(50);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, handle.getCurrentProgress());
        
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        handle.addContributor(contrib2);
        assertEquals(AggregateProgressHandle.WORKUNITS /4, contrib2.getRemainingParentWorkUnits());
        contrib1.finish();
        assertEquals(AggregateProgressHandle.WORKUNITS /4 * 3, handle.getCurrentProgress());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib2.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib3.getRemainingParentWorkUnits());
        contrib3.start(100);
        contrib3.finish();
        assertEquals((AggregateProgressHandle.WORKUNITS /4 * 3) + (AggregateProgressHandle.WORKUNITS /8), 
                     handle.getCurrentProgress());
        
        
    }
    
}
