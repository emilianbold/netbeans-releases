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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class ChangeSupportTest extends NbTestCase {

    public ChangeSupportTest(String testName) {
        super(testName);
    }

    public void testChangeSupport() {
        final int[] changeCount = { 0 };
        class Listener implements ChangeListener {
            private int notified;
            private ChangeEvent lastEvent;
            public void stateChanged(ChangeEvent event) {
                lastEvent = event;
                notified++;
            }
            public int getNotifiedAndReset() {
                try {
                    return notified;
                } finally {
                    notified = 0;
                }
            }
            public ChangeEvent getLastEvent() {
                return lastEvent;
            }
        }
        ChangeSupport support = new ChangeSupport(this);
        Listener listener1 = new Listener(), listener2 = new Listener();

        support.addChangeListener(null);
        assertFalse(support.hasListeners());

        support.removeChangeListener(null);
        assertFalse(support.hasListeners());

        support.addChangeListener(listener1);
        support.addChangeListener(listener2);
        assertTrue(support.hasListeners());
        Set<ChangeListener> listeners = new HashSet<ChangeListener>(support.listeners);
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(listener1));
        assertTrue(listeners.contains(listener2));

        support.fireChange();
        assertEquals(1, listener1.getNotifiedAndReset());
        assertEquals(1, listener2.getNotifiedAndReset());
        assertSame(this, listener1.getLastEvent().getSource());

        support.removeChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            }
        });
        support.fireChange();
        assertEquals(1, listener1.getNotifiedAndReset());
        assertEquals(1, listener2.getNotifiedAndReset());

        support.removeChangeListener(listener1);
        support.fireChange();
        assertEquals(0, listener1.getNotifiedAndReset());
        assertEquals(1, listener2.getNotifiedAndReset());

        support.addChangeListener(listener2);
        support.fireChange();
        assertEquals(2, listener2.getNotifiedAndReset());

        support.removeChangeListener(listener2);
        support.fireChange();
        assertEquals(1, listener2.getNotifiedAndReset());

        support.removeChangeListener(listener2);
        support.fireChange();
        assertEquals(0, listener2.getNotifiedAndReset());
        assertFalse(support.hasListeners());
    }
}
