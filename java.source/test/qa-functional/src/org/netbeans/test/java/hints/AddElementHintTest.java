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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.hints;

import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;

/**
 *
 * @author jp159440
 */
public class AddElementHintTest extends HintsTestCase{
    
    public AddElementHintTest(String name) {
        super(name);
    }
    
    public void testAddElement() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(34,1);
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Parameter a",
        "Create Local Variable a",
        "Create Field a in org.netbeans.test.java.hints.HintsTest.addHint"},
                ".*private int a;.*");
    }
    
    public void testAddElement2() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(28,1);
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Parameter g",
        "Create Local Variable g",
        "Create Field g in org.netbeans.test.java.hints.HintsTest.addHint"},
                ".*private LinkedList<String> g;.*");
    }
    
    public void testAddElement3() {
        String file = "Element2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", "Element1");
        target = new EditorOperator("Element1");
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(7,1);                
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Field field in org.netbeans.test.java.hints.HintsTest.Element1"},
                ".*int field;.*");
    }
    
    public void testAddElement4() {
        String file = "Element2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", "Element1");
        target = new EditorOperator("Element1");
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(8,1);                
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Field statField in org.netbeans.test.java.hints.HintsTest.Element1"},
                ".*static String statField;.*");
    }
    
    
    
    public static void main(String[] args) {
        new TestRunner().run(AddElementHintTest.class);
    }
    
    
}
