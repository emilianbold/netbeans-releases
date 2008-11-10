/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.junit.NbTestCase;

/** Test NbKeymap.
 * @author Jesse Glick
 * @see "#30455" */
public class NbKeymapTest extends NbTestCase {
    public NbKeymapTest(String name) {
        super(name);
    }
    
    @Override
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
        Map<KeyStroke,Action> m = new HashMap<KeyStroke,Action>();
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
        @Override
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
