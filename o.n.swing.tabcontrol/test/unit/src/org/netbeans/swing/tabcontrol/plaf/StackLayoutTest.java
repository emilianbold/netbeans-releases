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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.Collections;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Dafe Simonek
 */
public class StackLayoutTest extends NbTestCase {

    /** Creates a new instance of StackLayoutTest */
    public StackLayoutTest() {
        super("");
    }
    
    public StackLayoutTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(StackLayoutTest.class);
        return suite;
    }
    
    public void test_100486 () throws Exception {
        StackLayout layout = new StackLayout();
        JPanel panel = new JPanel(layout);
        JLabel testLabel = new JLabel("test label");
        panel.add(testLabel);
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        layout.showComponent(testLabel, panel);
        frame.setVisible(true);
        
        frame.setVisible(false);
        frame.getContentPane().remove(panel);
        panel = null;
        frame = null;
        WeakReference<Component> weakTestLabel = new WeakReference<Component>(testLabel);
        testLabel = null;
        
        assertGC("visibleComp member of StackLayout still not GCed", weakTestLabel, Collections.singleton(layout));
    }
    
}
