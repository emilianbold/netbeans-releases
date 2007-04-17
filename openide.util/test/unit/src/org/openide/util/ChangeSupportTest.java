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
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.MockChangeListener;

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
        ChangeSupport support = new ChangeSupport(this);
        MockChangeListener listener1 = new MockChangeListener(), listener2 = new MockChangeListener();

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
        List<ChangeEvent> events = listener1.allEvents();
        assertEquals(1, events.size());
        listener2.assertEventCount(1);
        assertSame(this, events.iterator().next().getSource());

        support.removeChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            }
        });
        support.fireChange();
        listener1.assertEventCount(1);
        listener2.assertEventCount(1);

        support.removeChangeListener(listener1);
        support.fireChange();
        listener1.assertEventCount(0);
        listener2.assertEventCount(1);

        support.addChangeListener(listener2);
        support.fireChange();
        listener2.assertEventCount(2);

        support.removeChangeListener(listener2);
        support.fireChange();
        listener2.assertEventCount(1);

        support.removeChangeListener(listener2);
        support.fireChange();
        listener2.assertEventCount(0);
        assertFalse(support.hasListeners());
    }
}
