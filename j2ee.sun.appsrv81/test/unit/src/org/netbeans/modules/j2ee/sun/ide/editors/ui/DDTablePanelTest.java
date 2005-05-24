/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * DDTablePanelTest.java
 * JUnit based test
 *
 * Created on May 28, 2004, 11:52 AM
 */

package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.lang.ref.*;
import junit.framework.*;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.*;
import org.openide.util.*;

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
        panel.linkLabel(new javax.swing.JLabel("test label"));
        panel.getSelectedRow();
        panel.getPropertyValue();
        panel.getHeaderColor();
        panel.setSelectedRow(0);
        //panel.editSelectedRow();
    }
    
    public DDTablePanelTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DDTablePanelTest.class);
        return suite;
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
