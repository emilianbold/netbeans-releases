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
package org.netbeans.test.java.editor.codegeneration;

import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.java.editor.jelly.GenerateCodeOperator;
import org.netbeans.test.java.editor.jelly.GenerateGettersAndSettersOperator;

/**
 *
 * @author jp159440
 */
public class CreateGetterSetter extends GenerateCode {

    public CreateGetterSetter(String testMethodName) {
        super(testMethodName);
    }
    
    public void testAvailableGettersSetters() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            int rowCount = jto.getRowCount();
            ggso.cancel();
            assertEquals("Wrong number of rows",5,rowCount);
            
        } finally {
            editor.close(false);
        }        
    }    
    
    public void testAvailableGetters() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            int rowCount = jto.getRowCount();
            ggso.cancel();
            assertEquals("Wrong number of rows",6,rowCount);
        } finally {
            editor.close(false);
        }
    }
    
    public void testAvailableSetters() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.SETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            int rowCount = jto.getRowCount();
            ggso.cancel();
            assertEquals("Wrong number of rows",6,rowCount);
        } finally {
            editor.close(false);
        }        
    }    

        
    public void testPrimitiveType() {        
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(2);            
            ggso.generate();            
            String expected = "" +
                            "    public int getNum() {\n"+
                            "        return num;\n"+
                            "    }\n"+
                            "\n"+
                            "    public void setNum(int num) {\n"+
                            "        this.num = num;\n"+
                            "    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testObjectType() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(4);            
            ggso.generate();            
            String expected = "" +
"    public List<? extends Thread> getThreads() {\n"+
"        return threads;\n"+
"    }\n"+
"\n"+
"    public void setThreads(List<? extends Thread> threads) {\n"+
"        this.threads = threads;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testBooleanType() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(1);            
            ggso.generate();            
            String expected = "" +
"    public boolean isBool() {\n"+
"        return bool;\n"+
"    }\n"+
"\n"+
"    public void setBool(boolean bool) {\n"+
"        this.bool = bool;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testStaticType() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(3);            
            ggso.generate();            
            String expected = "" +
"    public static int getStatField() {\n"+
"        return statField;\n"+
"    }\n"+
"\n"+
"    public static void setStatField(int statField) {\n"+
"        CreateGetterSetter.statField = statField;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testMultipleSetter() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_SETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.SETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(3);
            jto.selectRow(2);
            jto.selectRow(1);
            ggso.generate();
            String expected = "" +
"    public void setBool(boolean bool) {\n"+
"        this.bool = bool;\n"+
"    }\n"+
"\n"+
"    public void setHasGetter(int hasGetter) {\n"+
"        this.hasGetter = hasGetter;\n"+
"    }\n"+
"\n"+
"    public void setNum(int num) {\n"+
"        this.num = num;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }                
    }
    
    public void testMultipleGetter() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(1);
            jto.selectRow(2);
            jto.selectRow(3);
            ggso.generate();
            String expected = "" +
"    public boolean isBool() {\n"+
"        return bool;\n"+
"    }\n"+
"\n"+
"    public int getHasSetter() {\n"+
"        return hasSetter;\n"+
"    }\n"+
"\n"+
"    public int getNum() {\n"+
"        return num;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }                
    }
    
    public void testArray() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.CreateGetterSetter", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            editor.txtEditorPane().typeText("int [] pole;");
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();            
            jto.selectRow(3);
            ggso.generate();
            String expected = "" +
"    public int[] getPole() {\n"+
"        return pole;\n"+
"    }\n"+
"\n"+
"    public void setPole(int[] pole) {\n"+
"        this.pole = pole;\n"+
"    }\n"; 
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }                
    }
    
    public static void main(String[] args) {
        TestRunner.run(CreateGetterSetter.class);
    }

    
    
    
}
