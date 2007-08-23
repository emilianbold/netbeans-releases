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

package org.netbeans.qa.form.visualDevelopment;

import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;
import java.util.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Menu and popup menu tests from NetBeans 5.5.1 Form Test Specification
 * from  Visual Development Test Specification
 * @see <a href="http://qa.netbeans.org/modules/form/promo-f/testspecs/visualDevelopment.html">Test specification</a>
 *
 * @author Jiri Vagner
 */
public class MenuAndPopupMenuTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public MenuAndPopupMenuTest(String testName) {
        super(testName);
    }
    
    /* Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
        // fails [Issue 103273]  Can not add JMenuBar to JFrame using context menu        
        suite.addTest(new MenuAndPopupMenuTest("testMenuCreation"));

        // fails [Issue 99825] Unable to modify inserted PopupMenu        
        suite.addTest(new MenuAndPopupMenuTest("testPopupMenuCreation"));
        
        return suite;
    }
    
    public void testMenuCreation() {
	String menuPalettePath = "Insert|";
        String frameName = createJFrameFile();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        
        Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        new Action(null, "Add From Palette|Swing Menus|Menu Bar").perform(node); // NOI18N
        findInCode("jMenuBar1 = new javax.swing.JMenuBar();", designer); // NOI18N
        
        ArrayList<String> items = new ArrayList<String>();
        items.add(menuPalettePath + "Menu Item"); // NOI18N
        items.add(menuPalettePath + "Menu Item / CheckBox"); // NOI18N
        items.add(menuPalettePath + "Menu Item / RadioButton"); // NOI18N
        items.add(menuPalettePath + "Separator"); // NOI18N
        items.add(menuPalettePath + "Menu"); // NOI18N
        
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, false);
        
        node = new Node(inspector.treeComponents(), "[JFrame]|jMenuBar1 [JMenuBar]|jMenu1 [JMenu]"); // NOI18N
        runPopupOverNode(items, node, comparator);
        
        node = new Node(inspector.treeComponents(), "[JFrame]|jMenuBar1 [JMenuBar]|jMenu1 [JMenu]|jMenu3 [JMenu]"); // NOI18N
        runPopupOverNode(items, node, comparator);
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("jMenu3.add(jMenu4);"); // NOI18N
        lines.add("jMenu1.add(jMenu3);"); // NOI18N
        lines.add("jMenuBar1.add(jMenu1);"); // NOI18N
        lines.add("setJMenuBar(jMenuBar1);"); // NOI18N
        findInCode(lines, designer);
        
        removeFile(frameName);
    }

    public void testPopupMenuCreation() {
	String menuPalettePath = "Add From Palette|Swing Menus|";
        String frameName = createJFrameFile();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        
        Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        new Action(null, menuPalettePath + "Popup Menu").perform(node); // NOI18N
        findInCode("jPopupMenu1 = new javax.swing.JPopupMenu();", designer); // NOI18N
        
        ArrayList<String> items = new ArrayList<String>();
        items.add(menuPalettePath + "Menu Item"); // NOI18N
        items.add(menuPalettePath + "CheckBox Menu Item"); // NOI18N
        items.add(menuPalettePath + "RadioButton Menu Item"); // NOI18N
        items.add(menuPalettePath + "Separator"); // NOI18N
        items.add(menuPalettePath + "Menu"); // NOI18N
        
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, false);
        node = new Node(inspector.treeComponents(), "Other Components|jPopupMenu1 [JPopupMenu]"); // NOI18N

        // fails [Issue 99825] Unable to modify inserted PopupMenu        
//        runPopupOverNode(items, node, comparator);
        
        // TODO: tady to zkontrolovat, az ten test pojede
//        ArrayList<String> lines = new ArrayList<String>();
//        lines.add("jMenu3.add(jMenu4);"); // NOI18N
//        lines.add("jMenu1.add(jMenu3);"); // NOI18N
//        lines.add("jMenuBar1.add(jMenu1);"); // NOI18N
//        lines.add("setJMenuBar(jMenuBar1);"); // NOI18N
//        findInCode(lines, designer);
        
        removeFile(frameName);
    }
  }
