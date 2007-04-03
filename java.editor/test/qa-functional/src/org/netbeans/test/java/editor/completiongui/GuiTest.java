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


package org.netbeans.test.java.editor.completiongui;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import junit.textui.TestRunner;
import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;

/**
 *
 * @author Jiri Prox  Jiri.Prox@Sun.COM
 */
public class GuiTest extends EditorTestCase {
    
    public final String defaultSamplePackage = "org.netbeans.test.java.editor.completiongui.GuiTest";
    
    public final String version;
    
    
    /** Creates a new instance of CreateConstructor */
    public GuiTest(String name) {
        super(name);
        version  = getJDKVersionCode();
    }
    
    private String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");
        
        if (specVersion.startsWith("1.4"))
            return "jdk14";
        
        if (specVersion.startsWith("1.5"))
            return "jdk15";
        
        if (specVersion.startsWith("1.6"))
            return "jdk16";
        
        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openProject("java_editor_test");
        //openDefaultProject();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void performCodeCompletion(String testFile, String prefix, int line,int itemNo, String pattern,boolean allsymbols) {
        openSourceFile(defaultSamplePackage, testFile);
        EditorOperator editor = new EditorOperator(testFile);
        try {
            editor.requestFocus();
            editor.setCaretPosition(line, 1);
            if(prefix!=null) {
                for (int i = 0; i < prefix.length(); i++) {
                    char c = prefix.charAt(i);
                    editor.typeKey(c);
                }
            }
            if(allsymbols) {
                editor.pushKey(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
                new EventTool().waitNoEvent(1500);
            } else {
                editor.pushKey(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
                new EventTool().waitNoEvent(500);
            }
            while(itemNo>1) {
                editor.pushKey(KeyEvent.VK_DOWN);
                itemNo--;
            }
            if(itemNo!=0) editor.pushKey(KeyEvent.VK_ENTER); //instant substitution
            
            new EventTool().waitNoEvent(100);
            String text = editor.getText();
            Pattern p = Pattern.compile(pattern,Pattern.DOTALL);
            boolean ok = p.matcher(text).matches();
            if(!ok) {
                System.out.println("Pattern: "+pattern);
                System.out.println("-------");
                System.out.println(text);
                
            }
            assertTrue("Expected text not found in editor",ok);
        } finally {
            editor.close(false);
        }
    }
    
    public void testOverrideMethod() {
        String pattern  = "";
        if(version.equals("1.6")) {
            pattern = ".*@Override.*protected void finalize\\(\\) throws Throwable \\{.*super.finalize\\(\\);.*\\}.*";
        } else {
            pattern = ".*protected void finalize\\(\\) throws Throwable \\{.*super.finalize\\(\\);.*\\}.*";
        }
        performCodeCompletion("TestSimpleCase", null, 12, 3, pattern, false);
    }
    
    public void testKeyWord() {
        String pattern  = ".*protected.*";
        performCodeCompletion("TestSimpleCase","p", 12, 2, pattern, false);
    }
    
    public void testKeyWord2() {
        String pattern  = ".*extends.*";
        performCodeCompletion("ContextAware","ex", 11, 1, pattern, false);
    }
    
    public void testParameter() {
        String pattern  = ".*\\{.*x.*\\}.*";
        performCodeCompletion("TestSimpleCase",null, 10, 1, pattern, false);
    }
    
    public void testFiled() {
        String pattern  = ".*String s; public void neco\\(\\) \\{s.*";
        performCodeCompletion("TestSimpleCase","String s; public void neco() {", 12, 1, pattern, false);
    }
    
    public void testAnonymousClass() {
        String pattern  = ".*new Runnable\\(\\) \\{.*public void run\\(\\) \\{\n.*throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*\\}.*\\}.*";
        performCodeCompletion("TestSimpleCase","new Runnable", 10, 1, pattern, false);
    }
    
    public void testAddClassWithImport() {
        String pattern  = ".*import java.io.IOException;.*IOException.*";
        performCodeCompletion("TestSimpleCase","IOE", 10, 2, pattern, true);
    }
    
    public void testAddSuperClass() {
        String pattern  = ".*extends Byte.*";
        performCodeCompletion("ContextAware","extends B", 11, 2, pattern, false);
    }
    
    public void testAddInterface() {
        String pattern  = ".*implements Comparable.*";
        performCodeCompletion("ContextAware","implements ", 11, 4, pattern, false);
    }
    
    public void testAddInterface2() {
        String pattern  = ".*implements Comparable, Cloneable.*";
        performCodeCompletion("ContextAware","implements Comparable, ", 11, 3, pattern, false);
    }
    
    public void testAddThrows() {
        String pattern  = ".*throws AssertionError.*";
        performCodeCompletion("ContextAware","throws A", 15, 5, pattern, false);
    }
    
    public void testLoop() {
        String pattern  = ".*for\\(Thread t:.*threads.*";
        performCodeCompletion("ContextAware","", 23, 1, pattern, false);
    }
    
    public void testCatch() {
        String pattern  = ".*catch \\(.*MalformedURLException.*";
        performCodeCompletion("ContextAware","", 31, 2, pattern, false);
    }
    
    public void testReturn() {
        String pattern  = ".*return x.*";
        performCodeCompletion("ContextAware","return ", 35, 1, pattern, false);
    }
    
    public static void main(String[] args) {
        new TestRunner().run(GuiTest.class);
        
    }
    
}
