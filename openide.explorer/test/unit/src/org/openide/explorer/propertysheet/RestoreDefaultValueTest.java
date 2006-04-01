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

package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

// This test class tests the main functionality of the property sheet
public class RestoreDefaultValueTest extends ExtTestCase {
    private PropertySheet ps = null;
    public RestoreDefaultValueTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    static {
        ExtTestCase.installCorePropertyEditors();
    }
    
    private static boolean setup = false;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        // Create new TNode
        tn = new TNode();
        
        
        //Replacing NodeOp w/ JFrame to eliminate depending on full IDE init
        //and long delay while waiting for property sheet thus requested to
        //initialize
        final JFrame jf = new JFrame();
        ps = new PropertySheet();
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(ps, BorderLayout.CENTER);
        jf.setLocation(30,30);
        jf.setSize(500,500);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ps.setNodes(new Node[] {tn});
                //ps.setCurrentNode(tn);
                jf.show();
                jf.toFront();
            }
        });
        
        new ExtTestCase.WaitWindow(jf);
        
        try {
            ensurePainted(ps);
        } catch (Exception e) {
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    private void ensurePainted(final PropertySheet ps) throws Exception {
        //issues 39205 & 39206 - ensure the property sheet really repaints
        //before we get the value, or the value in the editor will not
        //have changed
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Graphics g = ps.getGraphics();
                ps.paintImmediately(0,0,ps.getWidth(), ps.getHeight());
            }
        });
    }
    
    
    public void testRestoreDefaultValueWorks() throws Exception {
        System.err.println("testRestoreDefaultValueWorks");
        if (!super.canSafelyRunFocusTests()) {
            return;
        }
        
        sleep();
        sleep();
        sleep();
        
        assertNotNull("Property not created", tp);
        
        final Action invoke = ps.table.getActionMap().get("invokeCustomEditor");
        assertNotNull("Incompatible change - no action from table for the key" +
                "\"invokeCustomEditor\".", invoke);
        
        pressCell(ps.table, 1, 1);
        
        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        
        
        sleep();
        sleep();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                invoke.actionPerformed(null);
            }
        });
        
        int ct = 0;
        while (!(w instanceof JDialog)) {
            if (ct++ > 10000) {
                fail("No dialog ever shown");
            }
            sleep();
            w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        }
        sleep();
        System.out.println("  Dlg now showing");
        final AbstractButton jb = findResetToDefaultButton((JDialog)w);
        
        if (jb == null) {
            fail("Could not find Reset To Default Button in dialog");
        }
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jb.doClick();
            }
        });
        
        
        
        Thread.currentThread().sleep(10000);
        
    }
    
    private AbstractButton findResetToDefaultButton(JDialog jd) {
        String txt = null;
        try {
            txt = NbBundle.getMessage(PropertyDialogManager.class, "CTL_Default");
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            fail("Bundle key CTL_DEFAULT missing from org.openide.explorer.propertysheet.Bundle.properties");
        }
        return findButton(jd.getContentPane(), txt);
    }
    
    private AbstractButton findButton(Container c, String s) {
        if (c instanceof AbstractButton && s.equals(((AbstractButton) c).getText())) {
            return ((AbstractButton) c);
        } else {
            Component[] cs = c.getComponents();
            for (int i=0; i < cs.length; i++) {
                if (cs[i] instanceof Container) {
                    AbstractButton result = findButton((Container) cs[i], s);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }
    
    //Node definition
    public class TNode extends AbstractNode {
        //create Node
        public TNode() {
            super(Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
        }
        //clone existing Node
        public Node cloneNode() {
            return new TNode();
        }
        
        public void destroy() {
            fireNodeDestroyed();
        }
        
        // Create a property sheet:
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            tp = new TProperty("property", true);
            props.put(tp);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private String myValue = "Value";
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
            super(name, String.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = (String) value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        private boolean defValue = false;
        public boolean isDefaultValue() {
            return defValue;
        }
        
        private boolean rdv = false;
        public void restoreDefaultValue() {
            defValue = true;
            rdv = true;
            System.err.println("RestoreDefaultValue");
            try {
                setValue("default");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        
        public void assertRestoreDefaultValueCalled() {
            assertTrue("Restore default value not called", rdv);
            rdv = false;
        }
        
        public void assertRestoreDefaultValueNotCalled() {
            assertFalse("Restore default value not called", rdv);
        }
    }
    
    private TNode tn;
    private TProperty tp;
}
