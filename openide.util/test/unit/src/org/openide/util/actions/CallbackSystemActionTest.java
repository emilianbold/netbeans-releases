/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/** Test CallbackSystemAction: changing performer, focus tracking.
 * @author Jesse Glick
 */
public class CallbackSystemActionTest extends NbTestCase {
    
    public CallbackSystemActionTest(String name) {
        super(name);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        SimpleCallbackAction.waitInstancesZero();
    }
    
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testPropertyChangeListenersDetachedAtFinalizeIssue58100() throws Exception {
        
        class MyAction extends AbstractAction
                implements ActionPerformer {
            public void actionPerformed(ActionEvent ev) {
            }
            public void performAction(SystemAction a) {
            }
        }
        MyAction action = new MyAction();
        ActionMap map = new ActionMap();
        CallbackSystemAction systemaction = (CallbackSystemAction)SystemAction.get(SimpleCallbackAction.class);
        map.put(systemaction.getActionMapKey(), action);
        Lookup context = Lookups.singleton(map);
        Action delegateaction = systemaction.createContextAwareInstance(context);
        
        assertTrue("Action is expected to have a PropertyChangeListener attached", action.getPropertyChangeListeners().length > 0);
        
        Reference actionref = new WeakReference(systemaction);
        systemaction = null;
        delegateaction = null;
        assertGC("CallbackSystemAction is supposed to be GCed", actionref);
        
        assertEquals("Action is expected to have no PropertyChangeListener attached", 0, action.getPropertyChangeListeners().length);
    }
    
    public void testSurviveFocusChangeInTheNewWay() throws Exception {
        doSurviveFocusChangeInTheNewWay(false);
    }
    
    public void testSurviveFocusChangeInTheNewWayEvenActionIsGCed() throws Exception {
        doSurviveFocusChangeInTheNewWay(true);
    }
    
    private void doSurviveFocusChangeInTheNewWay(boolean doGC) throws Exception {
        class MyAction extends AbstractAction {
            public int cntEnabled;
            public int cntPerformed;
            
            public boolean isEnabled() {
                cntEnabled++;
                return true;
            }
            
            public void actionPerformed(ActionEvent ev) {
                cntPerformed++;
            }
        }
        MyAction myAction = new MyAction();
        
        ActionMap other = new ActionMap();
        ActionMap tc = new ActionMap();
        SurviveFocusChgCallbackAction a = (SurviveFocusChgCallbackAction)SurviveFocusChgCallbackAction.get(SurviveFocusChgCallbackAction.class);
        tc.put(a.getActionMapKey(), myAction);
        
        ActionsInfraHid.setActionMap(other);
        try {
            assertFalse("Disabled on other component", a.isEnabled());
            ActionsInfraHid.setActionMap(tc);
            assertTrue("MyAction is enabled", a.isEnabled());
            assertEquals("isEnabled called once", 1, myAction.cntEnabled);
            
            if (doGC) {
                WeakReference ref = new WeakReference(a);
                a = null;
                assertGC("Action can disappear", ref);
                a = (SurviveFocusChgCallbackAction)SurviveFocusChgCallbackAction.get(SurviveFocusChgCallbackAction.class);
            }
            
            ActionsInfraHid.setActionMap(other);
            assertTrue("Still enabled", a.isEnabled());
            assertEquals("isEnabled called still only once (now it is called twice)", 2, myAction.cntEnabled);
        } finally {
            ActionsInfraHid.setActionMap(null);
        }
        
        WeakReference ref = new WeakReference(a);
        WeakReference ref2 = new WeakReference(myAction);
        WeakReference ref3 = new WeakReference(tc);
        a = null;
        myAction = null;
        tc = null;
        assertGC("We are able to clear global action", ref);
        assertGC("Even our action", ref2);
        assertGC("Even our component", ref3);
    }
    
    /** Make sure that the performer system works and controls enablement.
     */
    public void testPerformer() throws Exception {
        CallbackSystemAction a = (CallbackSystemAction)SystemAction.get(SimpleCallbackAction.class);
        assertFalse(a.isEnabled());
        Performer p = new Performer();
        assertEquals(0, p.count);
        a.setActionPerformer(p);
        assertTrue(a.isEnabled());
        a.actionPerformed(null);
        assertEquals(1, p.count);
        a.setActionPerformer(null);
        assertFalse(a.isEnabled());
    }
    
    /** Make sure that focus changes turn on or off actions as appropriate.
     */
    public void testFocusChanges() throws Exception {
        helperTestFocusChanges();
        // CallbackSystemAction keeps a listener separately from the action,
        // so make sure the actions still work after collected and recreated.
        // Note that similar code fails to work in NodeActionTest because the
        // GC will not complete, so if the GC assert here starts to fail, it is
        // OK to comment out the GC, its assert, and the second call to
        // helperTestFocusChanges().
        SimpleCallbackAction.waitInstancesZero();
        helperTestFocusChanges();
    }
    private void helperTestFocusChanges() throws Exception {
        ActionMap t1 = new ActionMap();
        ActionMap t2 = new ActionMap();
        ActionsInfraHid.setActionMap(t1);
        try {
            CallbackSystemAction a1 = (CallbackSystemAction)SystemAction.get(SurviveFocusChgCallbackAction.class);
            assertTrue(a1.getSurviveFocusChange());
            CallbackSystemAction a2 = (CallbackSystemAction)SystemAction.get(SimpleCallbackAction.class);
            assertFalse(a2.getSurviveFocusChange());
            CallbackSystemAction a3 = (CallbackSystemAction)SystemAction.get(DoesNotSurviveFocusChgCallbackAction.class);
            assertFalse(a3.getSurviveFocusChange());
            Performer p = new Performer();
            a1.setActionPerformer(p);
            a2.setActionPerformer(p);
            a3.setActionPerformer(p);
            assertTrue(a1.isEnabled());
            assertTrue(a2.isEnabled());
            assertTrue(a3.isEnabled());
            ActionsInfraHid.setActionMap(t2);
            assertTrue(a1.isEnabled());
            assertEquals(p, a1.getActionPerformer());
            assertFalse(a2.isEnabled());
            assertEquals(null, a2.getActionPerformer());
            assertFalse(a3.isEnabled());
            assertEquals(null, a3.getActionPerformer());
        } finally {
            ActionsInfraHid.setActionMap(null);
        }
    }
    
    public void testGlobalChanges() throws Exception {
        class MyAction extends AbstractAction {
            public int cntEnabled;
            public int cntPerformed;
            
            public boolean isEnabled() {
                cntEnabled++;
                return true;
            }
            
            public void actionPerformed(ActionEvent ev) {
                cntPerformed++;
            }
        }
        MyAction myAction = new MyAction();
        
        ActionMap tc = new ActionMap();
        tc.put(DefaultEditorKit.copyAction, myAction);
        CopyAction a = (CopyAction)CopyAction.get(CopyAction.class);
        
        ActionsInfraHid.setActionMap(tc);
        try {
            assertTrue("MyAction is enabled", a.isEnabled());
            assertEquals("isEnabled called once", 1, myAction.cntEnabled);
            a.setActionPerformer(null);
            assertEquals("An enabled is currentlly called again", 2, myAction.cntEnabled);
        } finally {
            ActionsInfraHid.setActionMap(null);
        }
    }
    private static final class CopyAction extends CallbackSystemAction {
        public Object getActionMapKey() {
            return DefaultEditorKit.copyAction;
        }
        public String getName() {
            return "Copy";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    /** Action performer that counts invocations. */
    public static final class Performer implements ActionPerformer {
        public int count = 0;
        public void performAction(SystemAction action) {
            count++;
        }
    }
    
    /** Simple callback action. */
    public static final class SimpleCallbackAction extends CallbackSystemAction {
        public String getName() {
            return "SimpleCallbackAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        private static ArrayList INSTANCES_WHO = new ArrayList();
        private static Object INSTANCES_LOCK = new Object();
        public static int INSTANCES = 0;
        
        public static void waitInstancesZero() throws InterruptedException {
            synchronized (INSTANCES_LOCK) {
                for (int i = 0; i < 10; i++) {
                    if (INSTANCES == 0) return;
                    
                    ActionsInfraHid.doGC();
                    
                    INSTANCES_LOCK.wait(1000);
                }
                failInstances("Instances are not zero");
            }
        }
        
        private static void failInstances(String msg) {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            pw.println(msg + ": " + INSTANCES);
            for (Iterator it = INSTANCES_WHO.iterator(); it.hasNext();) {
                Exception elem = (Exception) it.next();
                elem.printStackTrace(pw);
            }
            pw.close();
            fail(w.toString());
        }
        
        public SimpleCallbackAction() {
            synchronized (INSTANCES_LOCK) {
                INSTANCES++;
                INSTANCES_LOCK.notifyAll();
                INSTANCES_WHO.add(new Exception("Incremented to " + INSTANCES));
                
                if (INSTANCES == 2) {
                    failInstances("Incremented to two. That is bad");
                }
            }
        }
        protected boolean clearSharedData() {
            synchronized (INSTANCES_LOCK) {
                INSTANCES--;
                INSTANCES_LOCK.notifyAll();
                INSTANCES_WHO.add(new Exception("Decremented to " + INSTANCES));
            }
            return super.clearSharedData();
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    /** Similar but survives focus changes. */
    public static final class SurviveFocusChgCallbackAction extends CallbackSystemAction {
        protected void initialize() {
            super.initialize();
            setSurviveFocusChange(true);
        }
        public String getName() {
            return "SurviveFocusChgCallbackAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    /** Similar but does not; should behave like SimpleCallbackAction (it just sets the flag explicitly). */
    public static final class DoesNotSurviveFocusChgCallbackAction extends CallbackSystemAction {
        protected void initialize() {
            super.initialize();
            setSurviveFocusChange(false);
        }
        public String getName() {
            return "SurviveFocusChgCallbackAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    
    
    
    //
    // Set of tests for ActionMap and context
    //
    
    public void testLookupOfStateInActionMap() throws Exception {
        class MyAction extends AbstractAction
                implements ActionPerformer {
            int actionPerformed;
            int performAction;
            
            public void actionPerformed(ActionEvent ev) {
                actionPerformed++;
            }
            
            public void performAction(SystemAction a) {
                performAction++;
            }
        }
        MyAction action = new MyAction();
        
        ActionMap map = new ActionMap();
        CallbackSystemAction system = (CallbackSystemAction)SystemAction.get(SurviveFocusChgCallbackAction.class);
        system.setActionPerformer(null);
        map.put(system.getActionMapKey(), action);
        
        
        
        Action clone;
        
        
        //
        // Without action map
        //
        
        clone = system.createContextAwareInstance(Lookup.EMPTY);
        
        assertTrue("Action should not be enabled if no callback provided", !clone.isEnabled());
        
        system.setActionPerformer(action);
        assertTrue("Is enabled, because it has a performer", clone.isEnabled());
        system.setActionPerformer(null);
        assertTrue("Is disabled, because the performer has been unregistered", !clone.isEnabled());
        
        //
        // test with actionmap
        //
        action.setEnabled(false);
        
        Lookup context = Lookups.singleton(map);
        clone = system.createContextAwareInstance(context);
        
        CntListener listener = new CntListener();
        clone.addPropertyChangeListener(listener);
        
        assertTrue("Not enabled now", !clone.isEnabled());
        action.setEnabled(true);
        assertTrue("Clone is enabled because the action in ActionMap is", clone.isEnabled());
        listener.assertCnt("One change expected", 1);
        
        system.setActionPerformer(action);
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("MyAction.actionPerformed invoked", 1, action.actionPerformed);
        assertEquals("MyAction.performAction is not invoked", 0, action.performAction);
        
        
        action.setEnabled(false);
        assertTrue("Clone is disabled because the action in ActionMap is", !clone.isEnabled());
        listener.assertCnt("Another change expected", 1);
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("MyAction.actionPerformed invoked again", 2, action.actionPerformed);
        assertEquals("MyAction.performAction is not invoked, remains 0", 0, action.performAction);
        
    }
    
    private static final class CntListener extends Object
            implements PropertyChangeListener {
        private int cnt;
        
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
        
        public void assertCnt(String msg, int count) {
            assertEquals(msg, count, this.cnt);
            this.cnt = 0;
        }
    } // end of CntListener
    
}
