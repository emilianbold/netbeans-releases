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
public class AddImportTest extends HintsTestCase{

    public AddImportTest(String name) {
        super(name);
    }
    
    public void testAddImport1() {
        String file = "Imports";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(8,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*import java.util.List;.*";
        useHint("Add import for java.util",new String[]{"Add import for java.util","Add import for java.awt","Create class \"List\""},pattern);
    }
    
    public void testAddImport2() {
        String file = "Imports";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(9,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*";
        useHint("Create",new String[]{"Create class \"NonExisting\""},pattern);
    }
    
    public void testAddImport3() {
        String file = "Imports";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(10,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*import javax.swing.JButton;.*";
        useHint("<html><font color='#808080'><s>Add import",new String[]{"<html><font color='#808080'><s>Add import for javax.swing.JButton"},pattern);
    }
       
    public void testRemoveImport() {
        String file = "RemoveImport";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(7,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*import java\\.net\\.URL;\\simport java\\.util\\.List;.*";
        useHint("Remove Unused Import",new String[]{"Remove Unused Import","Remove All Unused Imports"},pattern);
    }
    
    public void testRemoveAllImport() {
        String file = "RemoveImport";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(7,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*package org.netbeans\\.test\\.java\\.hints\\.HintsTest;\\s*import java\\.io\\.FileReader;\\s*public class RemoveImport \\{.*";
        useHint("Remove All Unused Imports",new String[]{"Remove Unused Import","Remove All Unused Imports"},pattern);
    }
    
    public static void main(String[] args) {
        new TestRunner().run(AddImportTest.class);                
    }

}
