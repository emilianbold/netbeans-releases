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
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.JFrame;
import org.openide.explorer.propertysheet.ExtTestCase.WaitWindow;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;

/** Test finding help IDs in the property sheet.
 * @author Jesse Glick
 * @see "#14701"
 */
public class FindHelpTest extends ExtTestCase {
    
    public FindHelpTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    private PropertySheet sheet = null;
    private JFrame frame = null;
    protected void setUp() throws Exception {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new BorderLayout());
        sheet = new PropertySheet();
        jf.getContentPane().add(sheet);
        
        jf.setBounds(20, 20, 200, 400);
        frame = jf;
        new WaitWindow(jf);
    }
    
    public void testFindHelpOnProperty() throws Exception {
        Node n = new WithPropertyHelpNode();
        setCurrentNode(n, sheet);
        
        sleep();
        
        PropertySheet.HelpAction act = sheet.helpAction;
        
        assertTrue("No help context found", act.getContext() != null);
        
        assertTrue("Help action should be enabled", act.isEnabled());
        
    }
    
    public void testFindPropertiesHelpOnNode() throws Exception {
        Node n = new WithPropertiesHelpNode();
        setCurrentNode(n, sheet);
        
        sleep();
        
        PropertySheet.HelpAction act = sheet.helpAction;
        
        assertTrue("No help context found", act.getContext() != null);
        
        assertTrue("Help action should be enabled", act.isEnabled());
    }
    
    public void testNoHelpProvided() throws Exception {
        Node n = new HelplessNode();
        setCurrentNode(n, sheet);
        
        sleep();
        
        PropertySheet.HelpAction act = sheet.helpAction;
        
        assertFalse("A help context was found on a node with no properties help", act.getContext() != null);
        
        assertFalse("Help action should be disabled", act.isEnabled());
        
    }
    
    public void testSetHelpProvided() throws Exception {
        Node n = new WithTabsSetHelpNode();
        setCurrentNode(n, sheet);
        
        sleep();
        
        PropertySheet.HelpAction act = sheet.helpAction;
        
        HelpCtx ctx = act.getContext();
        assertTrue("A help context should have been found", ctx != null);
        
        assertTrue("Wrong help context returned: " + ctx.getHelpID(), "set-help-id".equals(ctx.getHelpID()));
    }
    
    // XXX test use of ExPropertyEditor.PROPERTY_HELP_ID
    
    private static Collection findChildren(Component p, Class c) {
        Collection x = new LinkedList();
        findChildren(p, c, x);
        return x;
    }
    
    private static void findChildren(Component p, Class c, Collection x) {
        if (c.isInstance(p)) {
            x.add(p);
        } else if (p instanceof Container) {
            Component[] k = ((Container)p).getComponents();
            for (int i = 0; i < k.length; i++) {
                findChildren(k[i], c, x);
            }
        }
    }
    
    /**
     * A node which provides no help - the help action should always be disabled
     */
    private static final class HelplessNode extends AbstractNode {
        public HelplessNode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put(new WithHelpProperty("prop1", "row-help-1"));
            ss.put(new WithHelpProperty("prop2", "row-help-2"));
            ss.put(new WithHelpProperty("prop3", null));
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithHelpProperty("prop4", "row-help-4"));
            ss.put(new WithHelpProperty("prop5", null));
            s.put(ss);
            return s;
        }
    }
    
    /**
     * A node whose properties provide their own help IDs
     */
    private static final class WithPropertyHelpNode extends AbstractNode {
        public WithPropertyHelpNode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.setValue("helpID", "properties-help");
            ss.put(new WithHelpProperty("prop1", "row-help-1"));
            ss.put(new WithHelpProperty("prop2", "row-help-2"));
            ss.put(new WithHelpProperty("prop3", null));
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithHelpProperty("prop4", "row-help-4"));
            ss.put(new WithHelpProperty("prop5", null));
            s.put(ss);
            return s;
        }
    }
    
    /**
     * A node which uses the per-node key for property sheet specific help -
     * the help action should be enabled for all its properties
     */
    private static final class WithPropertiesHelpNode extends AbstractNode {
        public WithPropertiesHelpNode() {
            super(Children.LEAF);
            setValue("propertiesHelpID", "propertiesHelp");
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put(new WithoutHelpProperty("prop1"));
            ss.put(new WithoutHelpProperty("prop2"));
            ss.put(new WithoutHelpProperty("prop3"));
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithoutHelpProperty("prop4"));
            ss.put(new WithoutHelpProperty("prop5"));
            s.put(ss);
            return s;
        }
    }
    
    private static final class WithTabsSetHelpNode extends AbstractNode {
        public WithTabsSetHelpNode() {
            super(Children.LEAF);
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx("node-help");
        }
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = Sheet.createPropertiesSet();
            ss.put(new WithoutHelpProperty("prop1"));
            ss.put(new WithoutHelpProperty("prop2"));
            ss.put(new WithoutHelpProperty("prop3"));
            ss.setValue("tabName", "Tab 1");
            ss.setValue("helpID", "set-help-id");
            s.put(ss);
            ss = Sheet.createExpertSet();
            ss.put(new WithoutHelpProperty("prop4"));
            ss.put(new WithoutHelpProperty("prop5"));
            ss.setValue("tabName", "Tab 2");
            s.put(ss);
            return s;
        }
    }
    
    
    private static final class WithHelpProperty extends PropertySupport.ReadOnly {
        public WithHelpProperty(String name, String helpID) {
            super(name, String.class, name, name);
            if (helpID != null) {
                setValue("helpID", helpID);
            }
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "value-" + getName();
        }
    }
    
    private static final class WithoutHelpProperty extends PropertySupport.ReadOnly {
        public WithoutHelpProperty(String name) {
            super(name, String.class, name, name);
        }
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "value-" + getName();
        }
    }
    
    
}
