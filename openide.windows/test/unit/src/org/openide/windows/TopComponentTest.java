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

package org.openide.windows;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ObjectInputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Keymap;
import junit.framework.TestCase;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CookieAction;
import org.openide.util.io.NbMarshalledObject;

/**
 *
 * @author mkleint
 */
public class TopComponentTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public TopComponentTest(String testName) {
        super(testName);
    }

    /**
     * Test of readExternal method, of class org.openide.windows.TopComponent.
     */
    public void testReadExternal() throws Exception {
        // first try tc with displayname
        TopComponent tc = new TopComponent();
        tc.setName("testName");
        tc.setDisplayName("testdisplayName");
        tc.setToolTipText("testTooltip");
        NbMarshalledObject obj = new NbMarshalledObject(tc);
        tc.close();
        
        tc = (TopComponent)obj.get();
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertEquals("testdisplayName", tc.getDisplayName());
        
        // now try tc withOUT displayname
        tc = new TopComponent();
        tc.setName("testName");
        tc.setToolTipText("testTooltip");
        obj = new NbMarshalledObject(tc);
        tc.close();
        
        tc = (TopComponent)obj.get();
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertNull(tc.getDisplayName());
        
    }
    
    /**
     * Test of readExternal method, of class org.openide.windows.TopComponent.
     */
    public void testOldReadExternal() throws Exception {
        TopComponent tc = null;
        try {
            ObjectInputStream stream = new ObjectInputStream(
                    getClass().getResourceAsStream("data/oldTcWithoutDisplayName.ser"));
            tc = (TopComponent)stream.readObject();
            stream.close();
        } catch (Exception exc) {
            exc.printStackTrace();
            fail("Cannot read tc");
        }
        
        
        assertNotNull("One again", tc);
        assertEquals("testName", tc.getName());
        assertEquals("testTooltip", tc.getToolTipText());
        assertEquals("If the old component does not have a display name, then keep it null", null, tc.getDisplayName());
    }

    TopComponent tcOpened = null;
    TopComponent tcClosed = null;
    
    public void testOpenedClosed () throws Exception {
        System.out.println("Testing property firing of TopComponent's registry");
        tcOpened = null;
        tcClosed = null;
                
        TopComponent.getRegistry().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (TopComponent.Registry.PROP_TC_OPENED.equals(evt.getPropertyName())) {
                            tcOpened = (TopComponent) evt.getNewValue();
                        } 
                        if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
                            tcClosed = (TopComponent) evt.getNewValue();
                        } 
                    }
                });
                
        TopComponent tc = new TopComponent();
        
        tc.open();
        assertNotNull("Property change was not fired, tcOpened is null", tcOpened);
        assertEquals("New value in property change is wrong", tc, tcOpened);
                
        tc.close();
        assertNotNull("Property change was not fired, tcClosed is null", tcClosed);
        assertEquals("New value in property change is wrong", tc, tcClosed);
        
    }
    
    
    public void testToStringOfDelegateContainsNameOfOriginalAction() throws Exception {
        SaveAction sa = SaveAction.get(SaveAction.class);
        Action a = sa.createContextAwareInstance(Lookup.EMPTY);
        if (a.toString().indexOf("SaveAction") == -1) {
            fail("We need name of the original action:\n" + a.toString());
        }
        
        CharSequence log = Log.enable("org.netbeans.ui", Level.FINER);
        
        final TopComponent tc = new TopComponent();
        tc.getActionMap().put("A", a);
        final KeyEvent ke = new KeyEvent(tc, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_MASK, 0, 'S');
        final KeyStroke ks = KeyStroke.getKeyStrokeForEvent(ke);
        tc.getInputMap().put(ks, a);
        MockServices.setServices(MyKM.class);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                tc.processKeyBinding(ks, ke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, true);
            }
            
        });
        
        if (log.toString().indexOf("SaveAction") == -1) {
            fail(log.toString());
        }
    }
    
    public void testCanGCTopComponentWhenItsActionMapIsHeld() throws Exception {
        TopComponent tc = new TopComponent();
        
        Object map = tc.getActionMap();
        Reference<Object> ref = new WeakReference<Object>(tc);
        tc = null;
        
        assertGC("TC can disappear", ref, Collections.singleton(map));
    }
    
    
    public static final class SaveAction extends CookieAction {
        static int cnt;
        
        protected void performAction(Node[] activatedNodes) {
            cnt++;
        }

        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        public String getName() {
            return "FakeName";
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected int mode() {
            return 0;
        }

        protected Class<?>[] cookieClasses() {
            return new Class[0];
        }
        
    }
    
    public static final class MyKM implements Keymap {

        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getDefaultAction() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setDefaultAction(Action a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action getAction(KeyStroke key) {
            return SaveAction.get(SaveAction.class);
        }

        public KeyStroke[] getBoundKeyStrokes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Action[] getBoundActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public KeyStroke[] getKeyStrokesForAction(Action a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isLocallyDefined(KeyStroke key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addActionForKeyStroke(KeyStroke key, Action a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeKeyStrokeBinding(KeyStroke keys) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeBindings() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Keymap getResolveParent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setResolveParent(Keymap parent) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
