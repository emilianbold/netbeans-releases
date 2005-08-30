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

package org.netbeans.core;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;

/** Test NbKeymap.
 * @author Jesse Glick
 * @see "#30455" */
public class NbKeymapTest extends NbTestCase {
    public NbKeymapTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    public void testBasicFunctionality() throws Exception {
        Keymap km = new NbKeymap();
        Action a1 = new DummyAction("a1");
        Action a2 = new DummyAction("a2");
        Action d = new DummyAction("d");
        KeyStroke k1 = KeyStroke.getKeyStroke("X");
        KeyStroke k2 = KeyStroke.getKeyStroke("Y");
        assertFalse(k1.equals(k2));
        assertNull(km.getAction(k1));
        assertNull(km.getAction(k2));
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(km.getBoundActions()));
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(km.getBoundKeyStrokes()));
        assertNull(km.getDefaultAction());
        km.setDefaultAction(d);
        assertEquals(d, km.getDefaultAction());
        km.addActionForKeyStroke(k1, a1);
        assertEquals(a1, km.getAction(k1));
        assertTrue(km.isLocallyDefined(k1));
        assertEquals(null, km.getAction(k2));
        assertEquals(Collections.singletonList(a1), Arrays.asList(km.getBoundActions()));
        assertEquals(Collections.singletonList(k1), Arrays.asList(km.getBoundKeyStrokes()));
        km.addActionForKeyStroke(k2, a2);
        assertEquals(a1, km.getAction(k1));
        assertEquals(a2, km.getAction(k2));
        assertEquals(2, km.getBoundActions().length);
        assertEquals(2, km.getBoundKeyStrokes().length);
        km.addActionForKeyStroke(k1, d);
        assertEquals(d, km.getAction(k1));
        assertEquals(a2, km.getAction(k2));
        assertEquals(2, km.getBoundActions().length);
        assertEquals(2, km.getBoundKeyStrokes().length);
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(km.getKeyStrokesForAction(a1)));
        assertEquals(Collections.singletonList(k2), Arrays.asList(km.getKeyStrokesForAction(a2)));
        assertEquals(Collections.singletonList(k1), Arrays.asList(km.getKeyStrokesForAction(d)));
        km.removeKeyStrokeBinding(k2);
        assertEquals(d, km.getAction(k1));
        assertNull(km.getAction(k2));
        assertEquals(Collections.singletonList(d), Arrays.asList(km.getBoundActions()));
        assertEquals(Collections.singletonList(k1), Arrays.asList(km.getBoundKeyStrokes()));
        km.removeBindings();
        assertNull(km.getAction(k1));
        assertNull(km.getAction(k2));
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(km.getBoundActions()));
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(km.getBoundKeyStrokes()));
    }
    
    public void testObservability() throws Exception {
        NbKeymap km = new NbKeymap();
        O o = new O();
        km.addObserver(o);
        assertFalse(o.changed);
        Action a1 = new DummyAction("a1");
        Action a2 = new DummyAction("a2");
        KeyStroke k1 = KeyStroke.getKeyStroke("X");
        km.addActionForKeyStroke(k1, a1);
        assertTrue(o.changed);
        o.changed = false;
        km.addActionForKeyStroke(k1, a2);
        assertTrue(o.changed);
        o.changed = false;
        km.removeKeyStrokeBinding(k1);
        assertTrue(o.changed);
    }
    
    public void testAcceleratorMapping() throws Exception {
        Keymap km = new NbKeymap();
        Action a1 = new DummyAction("a1");
        Action a2 = new DummyAction("a2");
        KeyStroke k1 = KeyStroke.getKeyStroke("X");
        KeyStroke k2 = KeyStroke.getKeyStroke("Y");
        assertNull(a1.getValue(Action.ACCELERATOR_KEY));
        assertNull(a2.getValue(Action.ACCELERATOR_KEY));
        AccL l = new AccL();
        a1.addPropertyChangeListener(l);
        assertFalse(l.changed);
        km.addActionForKeyStroke(k1, a1);
        assertEquals(k1, a1.getValue(Action.ACCELERATOR_KEY));
        assertTrue(l.changed);
        l.changed = false;
        km.addActionForKeyStroke(k2, a2);
        assertEquals(k2, a2.getValue(Action.ACCELERATOR_KEY));
        km.addActionForKeyStroke(k2, a1);
        Object acc = a1.getValue(Action.ACCELERATOR_KEY);
        assertTrue(acc == k1 || acc == k2);
        assertNull(a2.getValue(Action.ACCELERATOR_KEY));
        km.removeKeyStrokeBinding(k1);
        assertEquals(k2, a1.getValue(Action.ACCELERATOR_KEY));
        km.removeKeyStrokeBinding(k2);
        assertNull(a1.getValue(Action.ACCELERATOR_KEY));
        assertTrue(l.changed);
    }
    
    public void testAddActionForKeyStrokeMap() throws Exception {
        NbKeymap km = new NbKeymap();
        O o = new O();
        km.addObserver(o);
        Action a1 = new DummyAction("a1");
        Action a2 = new DummyAction("a2");
        Action a3 = new DummyAction("a3");
        KeyStroke k1 = KeyStroke.getKeyStroke("X");
        KeyStroke k2 = KeyStroke.getKeyStroke("Y");
        Map m = new HashMap();
        m.put(k1, a1);
        m.put(k2, a2);
        km.addActionForKeyStrokeMap(m);
        assertTrue(o.changed);
        assertEquals(a1, km.getAction(k1));
        assertEquals(a2, km.getAction(k2));
        assertEquals(k1, a1.getValue(Action.ACCELERATOR_KEY));
        assertEquals(k2, a2.getValue(Action.ACCELERATOR_KEY));
        assertEquals(2, km.getBoundActions().length);
        assertEquals(2, km.getBoundKeyStrokes().length);
        km.removeBindings();
        km.addActionForKeyStroke(k1, a3);
        km.addActionForKeyStrokeMap(m);
        assertEquals(a1, km.getAction(k1));
        assertEquals(a2, km.getAction(k2));
        assertEquals(k1, a1.getValue(Action.ACCELERATOR_KEY));
        assertEquals(k2, a2.getValue(Action.ACCELERATOR_KEY));
        assertNull(a3.getValue(Action.ACCELERATOR_KEY));
        assertEquals(2, km.getBoundActions().length);
        assertEquals(2, km.getBoundKeyStrokes().length);
    }
    
    private static final class DummyAction extends AbstractAction {
        private final String name;
        public DummyAction(String name) {
            this.name = name;
        }
        public void actionPerformed(ActionEvent e) {}
        public String toString() {
            return "DummyAction[" + name + "]";
        }
    }
    
    private static final class O implements Observer {
        public boolean changed = false;
        public void update(Observable o, Object arg) {
            changed = true;
        }
    }
    
    private static final class AccL implements PropertyChangeListener {
        public boolean changed = false;
        public void propertyChange(PropertyChangeEvent evt) {
            if (Action.ACCELERATOR_KEY.equals(evt.getPropertyName())) {
                changed = true;
            }
        }
    }
    
}
