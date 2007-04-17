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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.openide.util.ChangeSupport;

public class MockChangeListenerTest extends TestCase {

    public MockChangeListenerTest(String n) {
        super(n);
    }

    Object source;
    ChangeSupport cs;
    MockChangeListener l;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new Object();
        cs = new ChangeSupport(source);
        l = new MockChangeListener();
        cs.addChangeListener(l);
    }

    // XXX test expect

    public void testBasicUsage() throws Exception {
        l.assertNoEvents();
        l.assertEventCount(0);
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            l.assertEventCount(1);
            assert false;
        } catch (AssertionFailedError e) {}
        cs.fireChange();
        l.assertEvent();
        l.assertNoEvents();
        l.assertEventCount(0);
        cs.fireChange();
        cs.fireChange();
        l.assertEventCount(2);
        cs.fireChange();
        l.assertEvent();
        l.assertNoEvents();
        l.assertNoEvents();
        cs.fireChange();
        l.reset();
        l.assertNoEvents();
        cs.fireChange();
        cs.fireChange();
        assertEquals(2, l.allEvents().size());
    }

    public void testMessages() throws Exception {
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {}
        try {
            l.msg("stuff").assertEvent();
            assert false;
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().contains("stuff"));
        }
        try {
            l.assertEvent();
            assert false;
        } catch (AssertionFailedError e) {
            assertFalse(String.valueOf(e.getMessage()).contains("stuff"));
        }
    }

}
