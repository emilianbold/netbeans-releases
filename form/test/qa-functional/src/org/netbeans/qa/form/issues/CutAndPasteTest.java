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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.qa.form.issues;

import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import java.util.*;

/**
 * Issue #103199 test - Undo after Cut&Paste removes componets from designer
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=103199">issue</a>
 *
 * @author Jiri Vagner
 */
public class CutAndPasteTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public CutAndPasteTest(String testName) {
        super(testName);
    }
    
    /* Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CutAndPasteTest("testCutAndPaste")); // NOI18N
        return suite;
    }
    
    /** Cut and paste test. */
    public void testCutAndPaste() {
        String frameName = createJFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        
        Node actNode = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        runPopupOverNode("Add From Palette|Swing Containers|Panel", actNode); // NOI18N
        runPopupOverNode("Add From Palette|Swing Controls|Button", actNode); // NOI18N
        
        actNode = new Node(inspector.treeComponents(), "[JFrame]|jButton1 [JButton]"); // NOI18N
        runPopupOverNode("Cut", actNode); // NOI18N
        
        actNode = new Node(inspector.treeComponents(), "[JFrame]|jPanel1 [JPanel]"); // NOI18N
        runPopupOverNode("Paste", actNode); // NOI18N
        
        findInCode("javax.swing.JButton jButton1", designer); // NOI18N
        
        removeFile(frameName);
    }
}
