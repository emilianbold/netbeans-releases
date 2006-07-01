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

package org.openide.actions;


import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import junit.textui.TestRunner;

import org.netbeans.junit.*;
import org.openide.actions.*;
import org.openide.actions.ActionsInfraHid.UsefulThings;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;


/** Test behaviour of PasteAction intogether with clonning.
 */
public class PasteActionTest extends AbstractCallbackActionTestHidden {
    static {
            ActionsInfraHid.UT.setActivated (null);
    }
    
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
    
    public void testDelegatesAsArrayOfAction () throws Exception {
        OurAction[] arr = {
            new OurAction ()
        };
        action.putValue ("delegates", arr);
        //arr[0].setEnabled (true);
        
        TopComponent tc = new TopComponent();
        tc.getActionMap ().put(javax.swing.text.DefaultEditorKit.pasteAction, action);
        ActionsInfraHid.UT.setActivated (tc);
        global.actionPerformed (new ActionEvent (this, 0, "waitFinished"));
        
        arr[0].assertCnt ("Performed on delegate", 1);
        action.assertCnt ("Not performed on action", 0);
    }
    
    public void testDelegatesAsArrayOfPasteType () throws Exception {
        OurPasteType [] arr = {
            new OurPasteType ()
        };
        action.putValue ("delegates", arr);
        //arr[0].setEnabled (true);
        
        TopComponent tc = new TopComponent();
        tc.getActionMap ().put(javax.swing.text.DefaultEditorKit.pasteAction, action);
        ActionsInfraHid.UT.setActivated (tc);
        global.actionPerformed (new ActionEvent (this, 0, "waitFinished"));
        
        action.assertCnt ("Not performed on action", 0);
        arr[0].assertCnt ("Performed on delegate", 1);
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
    
    public void testDisableIsOk() throws Exception {
        PasteAction p = (PasteAction)PasteAction.get(PasteAction.class);
        
        class A extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
            }
        }
        A action = new A();
        action.setEnabled(false);
//        action.putValue("delegates", new A[0]);
        
        TopComponent td = new TopComponent();
        td.getActionMap().put(javax.swing.text.DefaultEditorKit.pasteAction, action);
        
        ActionsInfraHid.UT.setActivated(td);
        
        assertFalse("Disabled", p.isEnabled());
        JMenuItem item = p.getMenuPresenter();
        assertTrue("Dynamic one: " + item, item instanceof DynamicMenuContent);
        DynamicMenuContent d = (DynamicMenuContent)item;
        JComponent[] items = d.getMenuPresenters();
        items = d.synchMenuPresenters(items);
        assertEquals("One item", 1, items.length);
        assertTrue("One jmenu item", items[0] instanceof JMenuItem);
        JMenuItem one = (JMenuItem)items[0];
        assertFalse("And is disabled", one.getModel().isEnabled());
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
