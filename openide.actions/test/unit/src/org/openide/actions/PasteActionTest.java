/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.actions;


import java.util.Arrays;
import javax.swing.Action;
import javax.swing.ActionMap;

import junit.textui.TestRunner;

import org.netbeans.junit.*;
import org.openide.actions.*;
import org.openide.util.Lookup;


/** Test behaviour of PasteAction intogether with clonning.
 */
public class PasteActionTest extends AbstractCallbackActionTestHidden {
    public PasteActionTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(PasteActionTest.class));
    }
    
    protected Class actionClass () {
        return PasteAction.class;
    }
    
    protected String actionKey () {
        return javax.swing.text.DefaultEditorKit.pasteAction;
    }

    public void testListenersAreUnregisteredBug32073 () throws Exception {
        action.assertListeners ("When we created clone, we added a listener", 1);
        
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (clone);
        clone = null;
        assertGC ("Clone can disappear", ref);
        action.assertListeners ("No listeners, as the clone has been GCed", 0);
    }
    
    public void testPresenterCanBeGCedIssue47314 () throws Exception {
        javax.swing.JMenuItem item = ((org.openide.util.actions.Presenter.Popup)clone).getPopupPresenter ();
        
        java.lang.ref.WeakReference itemref = new java.lang.ref.WeakReference (item);
        item = null;
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (clone);
        clone = null;
        assertGC ("Item can disappear", itemref);
        assertGC ("Clone can disappear", ref);
    }
    
    
    public void testDelegatesAsOneAction() {
        OurAction[] arr = {
            new OurAction ()
        };

        action.setEnabled (true);
        assertTrue ("Now is the action enabled", clone.isEnabled());
        listener.assertCnt ("No change fired as it was enabled by default", 0);
        
        arr[0].setEnabled (false);
        action.putValue ("delegates", arr);
        
        assertTrue ("Clone should be disabled as the only action we delegate is", !clone.isEnabled ());
        listener.assertCnt ("That means one change should be fired", 1);
        
        action.setEnabled (false);
        assertTrue ("No influence on enabled state in delegate mode", !clone.isEnabled ());
        listener.assertCnt ("No changes fired", 0);
        
        arr[0].setEnabled (true);
        assertTrue ("State of clone changed to be enabled", clone.isEnabled ());
        listener.assertCnt ("Change fired", 1);

        arr[0].setEnabled (false);
        assertTrue ("Disabled again", !clone.isEnabled ());
        listener.assertCnt ("Changed delivered", 1);
        
        action.putValue ("delegates", null);
        assertTrue ("Still disabled, because action itself is disabled", !clone.isEnabled ());
        listener.assertCnt ("No changes due to that", 0);
        
        action.setEnabled (true);
        assertTrue ("Now we are listening to just the one action", clone.isEnabled ());
        listener.assertCnt ("And that is why we should be enabled with one event change", 1);
        
        arr[0].setEnabled (false);
        action.putValue ("delegates", arr);
        assertTrue ("Now we have delegates again, thus we are disabled", !clone.isEnabled ());
        listener.assertCnt ("One change delivered", 1);
    }
    
    public void testDelegatesAsMoreActions () throws Exception {
        action.setEnabled (false);
        listener.assertCnt ("One changed now", 1);
        
        OurAction[] arr = {
            new OurAction (),
            new OurAction ()
        };
        
        
        action.putValue ("delegates", arr);
        assertTrue ("Enabled because it has more than one action", clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);
        
        action.putValue ("delegates", new Object[0]);
        assertTrue ("Disabled as no delegates", !clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);

        action.putValue ("delegates", arr);
        assertTrue ("Enabled again", clone.isEnabled ());
        
        
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        arr[0].assertCnt ("First delegate invoked", 1);
    }
    
    public void testDelegatesAsMorePasteTypes () throws Exception {
        action.setEnabled (false);
        listener.assertCnt ("One changed now", 1);
        
        OurPasteType[] arr = {
            new OurPasteType(),
            new OurPasteType()
        };
        
        
        action.putValue ("delegates", arr);
        assertTrue ("Enabled because it has more than one action", clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);
        
        action.putValue ("delegates", new Object[0]);
        assertTrue ("Disabled as no delegates", !clone.isEnabled ());
        listener.assertCnt ("One changes since that", 1);

        action.putValue ("delegates", arr);
        assertTrue ("Enabled again", clone.isEnabled ());
        
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        arr[0].assertCnt ("First delegate invoked", 1);

        arr = new OurPasteType[] { new OurPasteType () };
        
        action.putValue ("delegates", arr);
        assertTrue ("Enabled still", clone.isEnabled ());
        
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        arr[0].assertCnt ("First delegate invoked", 1);
    }
    
    private static final class OurPasteType extends org.openide.util.datatransfer.PasteType {
        private int cnt;
        
        public java.awt.datatransfer.Transferable paste() throws java.io.IOException {
            cnt++;
            return null;
        }
        
        public void assertCnt (String msg, int count) {
            assertEquals (msg, count, this.cnt);
            this.cnt = 0;
        }
    } // end of OurPasteType
    
}
