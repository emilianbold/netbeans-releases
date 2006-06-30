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

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author vkraemer
 */
public class MessageAreaTest extends TestCase {

    public void testCoverage() {
        MessageArea ma = new MessageArea();
        ma = new MessageArea("foolish text");
        ma = new MessageArea("");
        // not null safe
        //ma = new MessageArea(null);
        ma.addNotify();
        //ma.setWidth(100);
        List l = new ArrayList();
        l.add("a");
        l.add("b");
        ma.setBulletItems("");
        // compile error
        // ma.setBulletItems(null);
        ma.setBulletItems("item1 item2 item3");
        ma.setBulletItems(new String[] { "item1", "item2", "item3" });
        ma.setBulletItems(new String[] { null });
        ma.setBulletItems(l);
        l.add(null);
        ma.setBulletItems(l);
        ma.setEndText("end text");
        ma.setEndText("");
        // not safe
        //ma.setEndText(null);
        ma.appendBulletItem("item4 item5 item6");
        ma.appendBulletItems(new String[] { "item7", "item8", "item9" });
        ma.appendBulletItems(new String[] { null });
        ma.appendBulletItems(l);
        ma.appendText("a b c");
        ma.setForeground(Color.CYAN);
        ma.setFont(Font.decode("ARIAL Bold 18"));
        ma.setText("a b c");
        ma.setWidth(100);
        ma.addNotify();
     }
    
    public MessageAreaTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of addNotify method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAddNotify() {
        System.out.println("testAddNotify");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setWidth method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetWidth() {
        System.out.println("testSetWidth");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setFont method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetFont() {
        System.out.println("testSetFont");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setForeground method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetForeground() {
        System.out.println("testSetForeground");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetText() {
        System.out.println("testSetText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setEndText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetEndText() {
        System.out.println("testSetEndText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of appendText method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAppendText() {
        System.out.println("testAppendText");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of setBulletItems method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testSetBulletItems() {
        System.out.println("testSetBulletItems");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of appendBulletItem method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAppendBulletItem() {
        System.out.println("testAppendBulletItem");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of appendBulletItems method, of class org.netbeans.modules.j2ee.sun.ide.editors.ui.MessageArea.
     *
    public void testAppendBulletItems() {
        System.out.println("testAppendBulletItems");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    */
    
}
