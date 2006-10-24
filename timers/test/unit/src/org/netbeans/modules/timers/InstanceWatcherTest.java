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
package org.netbeans.modules.timers;

import java.lang.ref.WeakReference;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hrebejk
 */
public class InstanceWatcherTest extends NbTestCase {

    public InstanceWatcherTest(String testName) {
        super(testName);
    }

    public void testFiring() throws Exception {
        System.out.println("addChangeListener");

        QueueListener listener = new QueueListener();
        InstanceWatcher iw = new InstanceWatcher();
        
        iw.addChangeListener(listener);
        
        Integer ts1 = new Integer( 20 );
        iw.add( ts1 );
               
        WeakReference tmp; // For forcing GC
        
        tmp = new WeakReference( new Object() );
        assertGC( "", tmp );
        
        assertEquals( "There should be no change in the queue", 0, listener.changeCount );
        
        ts1 = null; // Remove hard reference
        
        tmp = new WeakReference( new Object() );        
        assertGC( "", tmp ); // Do garbage collect
        
        assertEquals( "There should be one change in the queue", 1, listener.changeCount );
                
    }

    
    private static class QueueListener implements ChangeListener {
        
        int changeCount;
        
        public void stateChanged( ChangeEvent e ) {
            changeCount ++;
        }
        
    }
    
}