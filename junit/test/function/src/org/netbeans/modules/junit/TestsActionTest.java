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

package org.netbeans.modules.junit;

import java.awt.event.ActionEvent;
import java.awt.Component;
import javax.swing.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import junit.framework.*;

public class TestsActionTest extends TestCase {

    public TestsActionTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestsActionTest.class);
        
        return suite;
    }
    
    /** Test of actionPerformed method, of class org.netbeans.modules.junit.TestsAction. */
    public void testActionPerformed() {
        System.out.println("testActionPerformed");
        // there is nothing to test, the actionPerformed is never called for TestsAction
    }
    
    /** Test of getName method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetName() {
        System.out.println("testGetName");
        String name = TO.getName();
        assertTrue(null != name);
    }
    
    /** Test of iconResource method, of class org.netbeans.modules.junit.TestsAction. */
    public void testIconResource() {
        System.out.println("testIconResource");
        String icon = TO.iconResource();
        assertTrue(null != icon);
    }
    
    /** Test of getHelpCtx method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetHelpCtx() {
        System.out.println("testGetHelpCtx");
        HelpCtx hc = TO.getHelpCtx();
        assertTrue(null != hc);
    }
    
    /** Test of getMenuPresenter method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetMenuPresenter() {
        System.out.println("testGetMenuPresenter");
        JMenuItem jm = TO.getMenuPresenter();
        assertTrue(null != jm);
    }
    
    /** Test of getPopupPresenter method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetPopupPresenter() {
        System.out.println("testGetPopupPresenter");
        JMenuItem jm = TO.getPopupPresenter();
        assertTrue(null != jm);
    }
    
    /** Test of getToolbarPresenter method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetToolbarPresenter() {
        System.out.println("testGetToolbarPresenter");
        Component c = TO.getToolbarPresenter();
        assertTrue(null != c);
    }
    
    /* protected members */
    protected CreateTestAction TO = null;
    
    protected void setUp() {
        if (null == TO)
            TO = (CreateTestAction)CreateTestAction.findObject(CreateTestAction.class, true);
    }

    protected void tearDown() {
    }
}
