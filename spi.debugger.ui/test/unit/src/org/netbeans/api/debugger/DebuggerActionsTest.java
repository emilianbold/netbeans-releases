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

package org.netbeans.api.debugger;

import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestActionsManagerListener;
import org.netbeans.api.debugger.test.TestLazyActionsManagerListener;

import java.util.*;

/**
 * Tests invocations of debugger actions.
 *
 * @author Maros Sandor
 */
public class DebuggerActionsTest extends DebuggerApiTestBase {

    public DebuggerActionsTest(String s) {
        super(s);
    }

    public void testLookup() throws Exception {

        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Map args = new HashMap();
        TestDICookie tdi = TestDICookie.create(args);

        Object [] services = new Object[] { tdi, this };
        DebuggerInfo di = DebuggerInfo.create(TestDICookie.ID, services);

        DebuggerEngine engines [] = dm.startDebugging(di);
        assertEquals("Wrong number of engines started", 1, engines.length);
        DebuggerEngine debugger = engines[0];

        ActionsManager am = debugger.getActionsManager();
        TestActionsManagerListener tam = new TestActionsManagerListener();
        am.addActionsManagerListener(tam);

        TestLazyActionsManagerListener laml = (TestLazyActionsManagerListener) debugger.lookupFirst(null, LazyActionsManagerListener.class);
        assertNotNull("Lazy actions manager listener not loaded", laml);

        am.doAction(DebuggerManager.ACTION_CONTINUE);
        am.doAction(DebuggerManager.ACTION_FIX);
        am.doAction(DebuggerManager.ACTION_MAKE_CALLEE_CURRENT);
        am.doAction(DebuggerManager.ACTION_MAKE_CALLER_CURRENT);
        am.doAction(DebuggerManager.ACTION_PAUSE);
        am.doAction(DebuggerManager.ACTION_POP_TOPMOST_CALL);
        am.doAction(DebuggerManager.ACTION_RESTART);
        am.doAction(DebuggerManager.ACTION_RUN_INTO_METHOD);
        am.doAction(DebuggerManager.ACTION_RUN_TO_CURSOR);
        am.doAction(DebuggerManager.ACTION_STEP_INTO);
        am.doAction(DebuggerManager.ACTION_STEP_OUT);
        am.doAction(DebuggerManager.ACTION_STEP_OVER);
        am.doAction(DebuggerManager.ACTION_TOGGLE_BREAKPOINT);
        dm.getCurrentSession().kill();

        am.removeActionsManagerListener(tam);

        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_START));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_CONTINUE));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_FIX));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_MAKE_CALLEE_CURRENT));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_MAKE_CALLER_CURRENT));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_PAUSE));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_POP_TOPMOST_CALL));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_RESTART));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_RUN_INTO_METHOD));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_RUN_TO_CURSOR));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_STEP_INTO));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_STEP_OUT));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_STEP_OVER));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_TOGGLE_BREAKPOINT));
        assertTrue("Action was not performed", tdi.hasInfo(DebuggerManager.ACTION_KILL));

        testReceivedEvents(tam.getPerformedActions(), false);
        testReceivedEvents(laml.getPerformedActions(), true);
    }

    private void testReceivedEvents(List eventActions, boolean expectStartAction) {
        if (expectStartAction) assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_START));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_CONTINUE));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_FIX));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_MAKE_CALLEE_CURRENT));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_MAKE_CALLER_CURRENT));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_PAUSE));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_POP_TOPMOST_CALL));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_RESTART));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_RUN_INTO_METHOD));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_RUN_TO_CURSOR));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_STEP_INTO));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_STEP_OUT));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_STEP_OVER));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_TOGGLE_BREAKPOINT));
        assertTrue("ActionListener was not notified", eventActions.remove(DebuggerManager.ACTION_KILL));
        assertEquals("ActionListener notification failed", eventActions.size(), 0);
    }
}
