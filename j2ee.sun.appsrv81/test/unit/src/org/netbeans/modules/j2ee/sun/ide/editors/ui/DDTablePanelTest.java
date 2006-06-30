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

package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import javax.swing.JLabel;
import junit.framework.TestCase;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

/**
 *
 * @author vkraemer
 */
public class DDTablePanelTest extends TestCase {

    public void testCoverage() {
        NameValuePair vals[] = new NameValuePair[1];
        NameValuePair nvp;
        nvp = vals[0] = new NameValuePair();
        vals[0].setParamName("foo");
        vals[0].setParamValue("bar");
        nvp.setParamDescription("this is my description");
        nvp.getParamDescription();
        nvp.getParamName();
        nvp.getParamValue();
        NameValuePairsPropertyEditor nvppe = 
            new NameValuePairsPropertyEditor(vals);
        nvppe.getAsText();
        nvppe.getValue();
        nvppe.isPaintable();
        nvppe.setAsText("abc 123");
        nvppe.setValue(vals);
        nvppe.supportsCustomEditor();
        DDTablePanel panel = (DDTablePanel) nvppe.getCustomEditor();
        panel.setVerticalScrollBarValue(59);
        panel.setSelectedRow(0);
//        panel.setSelectedRow(1);
//        panel.setSelectedRow(-1);
        panel.linkLabel(new JLabel("test label"));
        panel.getSelectedRow();
        panel.getPropertyValue();
        panel.getHeaderColor();
        panel.setSelectedRow(0);
        //panel.editSelectedRow();
    }
    
    public DDTablePanelTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of addListSelectionListener method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testAddListSelectionListener() {
        System.out.println("testAddListSelectionListener");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of addVerticalScrollBarAdjustmentListener method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testAddVerticalScrollBarAdjustmentListener() {
        System.out.println("testAddVerticalScrollBarAdjustmentListener");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setVerticalScrollBarValue method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testSetVerticalScrollBarValue() {
        System.out.println("testSetVerticalScrollBarValue");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of linkLabel method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testLinkLabel() {
        System.out.println("testLinkLabel");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getHeaderColor method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testGetHeaderColor() {
        System.out.println("testGetHeaderColor");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getSelectedRow method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testGetSelectedRow() {
        System.out.println("testGetSelectedRow");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setSelectedRow method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testSetSelectedRow() {
        System.out.println("testSetSelectedRow");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setCellEditor method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testSetCellEditor() {
        System.out.println("testSetCellEditor");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of editSelectedRow method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testEditSelectedRow() {
        System.out.println("testEditSelectedRow");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getPropertyValue method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTablePanel.
     *
    public void testGetPropertyValue() {
        System.out.println("testGetPropertyValue");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    */
    
}
