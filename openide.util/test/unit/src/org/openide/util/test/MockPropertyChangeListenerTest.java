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

package org.openide.util.test;

import java.beans.PropertyChangeSupport;
import java.util.Collections;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class MockPropertyChangeListenerTest extends TestCase {

    public MockPropertyChangeListenerTest(String n) {
        super(n);
    }

    Object source;
    PropertyChangeSupport pcs;
    MockPropertyChangeListener l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new Object();
        pcs = new PropertyChangeSupport(source);
        l = new MockPropertyChangeListener();
        pcs.addPropertyChangeListener(l);
    }

    // XXX test expect

    public void testBasicUsage() throws Exception {
        l.assertEvents();
        try {
            l.assertEvents("whatever");
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        l.assertEvents("foo");
        try {
            l.assertEvents("foo");
            assert false;
        } catch (AssertionFailedError e) {}
        l.assertEventCount(0);
        pcs.firePropertyChange("bar", null, null);
        pcs.firePropertyChange("baz", null, null);
        l.assertEventCount(2);
        try {
            l.assertEventCount(2);
            assert false;
        } catch (AssertionFailedError e) {}
        assertEquals(0, l.allEvents().size());
        pcs.firePropertyChange("bar", null, null);
        pcs.firePropertyChange("baz", null, null);
        assertEquals(2, l.allEvents().size());
        assertEquals(0, l.allEvents().size());
        pcs.firePropertyChange("foo", "old", "new");
        l.assertEventsAndValues(null, Collections.singletonMap("foo", "new"));
        pcs.firePropertyChange("foo", "old2", "new2");
        l.assertEventsAndValues(Collections.singletonMap("foo", "old2"), Collections.singletonMap("foo", "new2"));
        try {
            l.assertEventsAndValues(null, Collections.singletonMap("foo", "new2"));
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        l.reset();
        l.assertEvents();
        pcs.firePropertyChange("x", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {}
        l.assertEvents();
    }

    public void testMessages() throws Exception {
        pcs.firePropertyChange("foo", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.firePropertyChange("foo", null, null);
        try {
            l.msg("stuff").assertEvents();
            assert false;
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().contains("stuff"));
        }
        pcs.firePropertyChange("foo", null, null);
        try {
            l.assertEvents();
            assert false;
        } catch (AssertionFailedError e) {
            assertFalse(e.getMessage().contains("stuff"));
        }
    }

    public void testPropertyNameFiltering() throws Exception {
        l.ignore("irrelevant");
        l.blacklist("bad", "worse");
        pcs.firePropertyChange("relevant", null, null);
        l.assertEvents("relevant");
        pcs.firePropertyChange("irrelevant", null, null);
        l.assertEvents();
        try {
            pcs.firePropertyChange("bad", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            pcs.firePropertyChange("worse", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
        pcs.removePropertyChangeListener(l);
        l = new MockPropertyChangeListener("expected1", "expected2");
        pcs.addPropertyChangeListener(l);
        l.ignore("irrelevant");
        pcs.firePropertyChange("expected1", null, null);
        pcs.firePropertyChange("expected2", null, null);
        l.assertEvents("expected1", "expected2");
        pcs.firePropertyChange("irrelevant", null, null);
        l.assertEvents();
        try {
            pcs.firePropertyChange("other", null, null);
            assert false;
        } catch (AssertionFailedError e) {}
    }

}
