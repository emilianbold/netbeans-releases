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

package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.openide.explorer.propertysheet.ExtTestCase.WaitWindow;
import org.openide.nodes.Node;

/** Test finding help IDs in the property sheet.
 * @author Jesse Glick
 * @see "#14701"
 */
public class NonEditabilityTest extends ExtTestCase {

    public NonEditabilityTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }

    private PropertySheet sheet = null;
    private JFrame frame = null;
    protected void setUp() throws Exception {
        //Ensure we don't have a bogus stored value
        PropUtils.putSortOrder(PropertySheet.UNSORTED);

        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new BorderLayout());
        sheet = new PropertySheet();
        jf.getContentPane().add (sheet);

        jf.setBounds (20, 20, 200, 400);
        frame = jf;
        new WaitWindow(jf);
    }

    public void testClickInvokesCustomEditor() throws Exception {
        if( !ExtTestCase.canSafelyRunFocusTests() )
            return;
        
        Node n = new ANode();
        setCurrentNode (n, sheet);

        sleep();

        requestFocus (sheet.table);
        sleep();

        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if (owner == sheet.table) { //sanity check to avoid random failures on some window managers

            System.out.println ("About to click cell");

            Rectangle r = sheet.table.getCellRect(1, 1, false);
            final MouseEvent me = new MouseEvent (sheet.table, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, r.x + 3,
                r.y + 3, 2, false);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    sheet.table.dispatchEvent(me);
                }
            });

            sleep();
            sleep();

            System.out.println ("Now checking focus");

            owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            assertTrue ("Focus owner should be custom editor, not " + owner, owner instanceof JTextArea);

            JComponent jc = (JComponent) owner;
            assertTrue ("Custom editor should have been invoked, but focus owner's top level ancestor is not a dialog", jc.getTopLevelAncestor() instanceof Dialog);

            Dialog d = (Dialog) jc.getTopLevelAncestor();

            d.setVisible(false);
        }

        requestFocus (sheet.table);
        sleep();

        owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if (owner == sheet.table) { //sanity check to avoid random failures on some window managers
            pressKey(sheet.table, KeyEvent.VK_SPACE);
            sleep();

            owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            assertTrue ("After pressing a key, focus owner should still be the table, not " + owner, sheet.table == owner);
        }

    }
    
    private static final class ANode extends AbstractNode {
        public ANode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put (new AProperty());
            ss.put (new AProperty());
            s.put(ss);
            return s;
        }
    }

    private static final String name = "foo";
    private static final class AProperty extends PropertySupport.ReadOnly {
        public AProperty() {
            super(name, String.class, name, name);
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "value-" + getName();
        }

        public PropertyEditor getPropertyEditor() {
            return new APropertyEditor();
        }
    }

    private static final class APropertyEditor implements PropertyEditor {
        public void setValue(Object value) {

        }

        public Object getValue() {
            return null;
        }

        public boolean isPaintable() {
            return true;
        }

        public void paintValue(Graphics gfx, Rectangle box) {
            gfx.setColor (Color.ORANGE);
            gfx.fillRect (box.x, box.y, box.width, box.height);
        }

        public String getJavaInitializationString() {
            return null;
        }

        public String getAsText() {
            return null;
        }

        public void setAsText(String text) throws IllegalArgumentException {

        }

        public String[] getTags() {
            return null;
        }

        public Component getCustomEditor() {
            return new JTextArea();
        }

        public boolean supportsCustomEditor() {
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {

        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {

        }
    }
}
