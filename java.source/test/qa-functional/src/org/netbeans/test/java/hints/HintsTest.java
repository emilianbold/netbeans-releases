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
public class HintsTest extends HintsTestCase {
    
    public HintsTest(String testMethodName) {
        super(testMethodName);
    }
      
    
    public void testCast() {
        String file = "castHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(9,1);
        new EventTool().waitNoEvent(750);
        useHint("Cast ...new Object(...) to String",new String[]{"Cast ...new Object(...) to String","Change type of s to Object"},".*String s = \\(String\\) new Object\\(\\);.*");
    }
    
    public void testCast2() {
        String file = "castHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(12,1);
        new EventTool().waitNoEvent(750);
        useHint("Cast ...get(...) to File",new String[]{"Cast ...get(...) to File","Change type of i to Object"},
                ".*File i = \\(File\\) l.get\\(1\\);.*");
    }
    
    public void testCast3() {
        String file = "castHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(16,1);
        new EventTool().waitNoEvent(750);
        useHint("Cast ...get(...) to Integer",new String[]{"Cast ...get(...) to Integer","Change type of i to Number"},
                ".*Integer i = \\(Integer\\) nums.get\\(1\\);.*");
    }
    
    public void testAddParam() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(13,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter x",new String[]{"Create parameter x",
                                                  "Create local variable x",
                                                  "Create field x in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public addHint\\(String\\[\\] x\\) \\{.*");
    }
    
    public void testAddParam2() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(22,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter a",
                                                  "Create local variable a",
                                                  "Create field a in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,int a\\) \\{.*");
    }
    
    public void testAddParam3() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(23,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter b",
                                                  "Create local variable b",
                                                  "Create field b in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,long b\\) \\{.*");
    }
    
    public void testAddParam4() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(24,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter c",
                                                  "Create local variable c",
                                                  "Create field c in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,char c\\) \\{.*");
    }
    
    public void testAddParam5() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(25,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter d",
                                                  "Create local variable d",
                                                  "Create field d in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,byte d\\) \\{.*");
    }
    
    public void testAddParam6() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(26,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter e",
                                                  "Create local variable e",
                                                  "Create field e in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,double e\\) \\{.*");
    }
    
    public void testAddParam7() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(27,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter f",
                                                  "Create local variable f",
                                                  "Create field f in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,Integer f\\) \\{.*");
    }
    
     public void testAddParam8() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(28,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter g",
                                                  "Create local variable g",
                                                  "Create field g in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,LinkedList<String> g\\) \\{.*");
    }
     
    public void testAddParam9() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(29,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter h",
                                                  "Create local variable h",
                                                  "Create field h in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,String h\\) \\{.*");
    } 
    
    public void testAddParamA() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(34,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter a",
                                                  "Create local variable a",
                                                  "Create field a in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method2\\(double x,int a,int ... y\\) \\{.*");
    }
    
    public void testAddParamB() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(30,1);
        new EventTool().waitNoEvent(750);
        useHint("Create parameter",new String[]{"Create parameter i",
                                                  "Create local variable i",
                                                  "Create field i in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,Map<String, List<String>> i\\) \\{.*");
    }
    
    public void testAddLocal() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(13,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter x",
                                                  "Create local variable x",
                                                  "Create field x in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*String\\[\\] x = new java.lang.String\\[\\]\\{\"array\"\\};.*");
    }
    
    public void testAddLocal2() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(22,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter a",
                                                  "Create local variable a",
                                                  "Create field a in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*int a = 3;.*");
    }
    
    public void testAddLocal3() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(23,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter b",
                                                  "Create local variable b",
                                                  "Create field b in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*long b = 3L;.*");
    }
    
    public void testAddLocal4() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(24,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter c",
                                                  "Create local variable c",
                                                  "Create field c in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*char c = 'c';.*");
    }
    
    public void testAddLocal5() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(25,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter d",
                                                  "Create local variable d",
                                                  "Create field d in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*byte d = \\(byte\\) 2;.*");
    }
    
    public void testAddLocal6() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(26,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter e",
                                                  "Create local variable e",
                                                  "Create field e in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*double e = 3.4;.*");
    }
    
    public void testAddLocal7() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(27,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter f",
                                                  "Create local variable f",
                                                  "Create field f in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*Integer f = new java.lang.Integer\\(1\\).*");
    }
    
     public void testAddLocal8() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(28,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter g",
                                                  "Create local variable g",
                                                  "Create field g in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*LinkedList<String> g = new java.util.LinkedList<java.lang.String>\\(\\);.*");
    }
     
    public void testAddLocal9() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(29,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter h",
                                                  "Create local variable h",
                                                  "Create field h in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*String h = \"ssss\";.*");
    } 
    
    public void testAddLocalA() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(34,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter a",
                                                  "Create local variable a",
                                                  "Create field a in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*int a = 3;.*");
    }
    
    public void testAddLocalB() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(30,1);
        new EventTool().waitNoEvent(750);
        useHint("Create local variable",new String[]{"Create parameter i",
                                                  "Create local variable i",
                                                  "Create field i in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*Map<String, List<String>> i = getMap\\(\\);.*");
    }
                    
    public static void main(String[] args) {
        new TestRunner().run(HintsTest.class);
    }
    
    
    
}
