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
package org.netbeans.modules.j2ee.sun.share.customizer;

import junit.framework.*;
// added
import junit.extensions.abbot.*; 
import abbot.tester.*;

import org.netbeans.modules.j2ee.sun.share.configbean.CmpJar;
// dedda
/**
 *
 * @author vkraemer
 */
public class CmpJarPanelBot extends ComponentTestFixture { // TestCase {
    
    public CmpJarPanelBot(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CmpJarPanelBot.class);
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}

    // here are the tests    
    private ComponentTester tester;
    protected void setUp() {
        tester = ComponentTester.getTester(CmpJarPanel.class);
    }
    
    public void testFocusTraversal() {
        CmpJarPanel pane = new CmpJarPanel();
        
        // ComponentTestFixture provides the frame
        CmpJar bean = new CmpJar();
        try {
            bean.setJndiName("tip-");
            bean.setPrincipalName("hip-");
        }
        catch (Throwable t) {
            assertTrue("set did not get vetoed",false);
        }
        pane.setObject(bean);
        showFrame(pane);
        try {
            Thread.sleep(2000);
        }
        catch(Throwable t) {
        }
        
        tester.actionKeyPress("VK_END");
        tester.actionKeyRelease("VK_END");
        tester.actionKeyPress("VK_T");
        tester.actionKeyRelease("VK_T");
        tester.actionKeyPress("VK_O");
        tester.actionKeyRelease("VK_O");
        tester.actionKeyPress("VK_P");
        tester.actionKeyRelease("VK_P");
        assertEquals("keystroke did not propigate", "tip-top", bean.getJndiName());
        tester.actionKeyPress("VK_TAB");
        tester.actionKeyRelease("VK_TAB");
        tester.actionKeyPress("VK_END");
        tester.actionKeyRelease("VK_END");
        tester.actionKeyPress("VK_H");
        tester.actionKeyRelease("VK_H");
        tester.actionKeyPress("VK_O");
        tester.actionKeyRelease("VK_O");
        tester.actionKeyPress("VK_P");
        tester.actionKeyRelease("VK_P");
        //assertEquals("Missed a keystroke", "hip-hop", pane.getField().getText());
        assertEquals("keystroke did not propigate", "hip-hop", bean.getPrincipalName());
        tester.actionKeyPress("VK_TAB");
        tester.actionKeyRelease("VK_TAB");
        tester.actionKeyPress("VK_END");
        tester.actionKeyRelease("VK_END");
        tester.actionKeyPress("VK_H");
        tester.actionKeyRelease("VK_H");
        tester.actionKeyPress("VK_O");
        tester.actionKeyRelease("VK_O");
        tester.actionKeyPress("VK_P");
        tester.actionKeyRelease("VK_P");
        assertEquals("keystroke did not propigate", "hop", bean.getPrincipalPassword());
        tester.actionKeyPress("VK_SHIFT");
        tester.actionKeyPress("VK_TAB");
        tester.actionKeyRelease("VK_TAB");
        tester.actionKeyRelease("VK_SHIFT");
        tester.actionKeyPress("VK_END");
        tester.actionKeyRelease("VK_END");
        System.out.println("about to do the clear test");
        tester.actionKeyPress("VK_BACK_SPACE");
        tester.actionKeyRelease("VK_BACK_SPACE");
/*        tester.actionKeyPress("VK_O");
        tester.actionKeyRelease("VK_O");
        tester.actionKeyPress("VK_P");
        tester.actionKeyRelease("VK_P");
*/
        tester.actionKeyPress("VK_BACK_SPACE");
        tester.actionKeyRelease("VK_BACK_SPACE");
        tester.actionKeyPress("VK_BACK_SPACE");
        tester.actionKeyRelease("VK_BACK_SPACE");
        tester.actionKeyPress("VK_BACK_SPACE");
        tester.actionKeyRelease("VK_BACK_SPACE");
        tester.actionKeyPress("VK_BACK_SPACE");
        tester.actionKeyRelease("VK_BACK_SPACE");
        tester.actionKeyPress("VK_BACK_SPACE");
        tester.actionKeyRelease("VK_BACK_SPACE");
        tester.actionKeyPress("VK_BACK_SPACE");
        tester.actionKeyRelease("VK_BACK_SPACE");
        System.out.println("about to hit tab");
        tester.actionKeyPress("VK_TAB");
        tester.actionKeyRelease("VK_TAB");
        System.out.println("have hit tab");
        
        assertEquals("bug in password clearer", "", bean.getPrincipalPassword());

    }
    
}
