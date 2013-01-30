/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.test.java.editor.completiongui;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.test.java.editor.lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Jiri Prox  Jiri.Prox@Sun.COM
 */
public class GuiTest extends EditorTestCase {
    
    public final String defaultSamplePackage = "org.netbeans.test.java.editor.completiongui.GuiTest";
    
    public final String version;
    
    private static boolean firstRun = true;
    
    
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
        int delay;
        openSourceFile(defaultSamplePackage, testFile);
        if(firstRun) {
            new EventTool().waitNoEvent(10000);
            firstRun = false;
        }
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
            new EventTool().waitNoEvent(1000);
            if(allsymbols) {
                editor.pushKey(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);                
                delay = 3000;
            } else {
                editor.pushKey(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
                
                delay = 1500;
            }
            new EventTool().waitNoEvent(delay);
            while(itemNo>1) {
                editor.pushKey(KeyEvent.VK_DOWN);
                new EventTool().waitNoEvent(200);
                itemNo--;
            }            
            editor.pushKey(KeyEvent.VK_ENTER);
            
            new EventTool().waitNoEvent(300);
            String text = editor.getText();
            Pattern p = Pattern.compile(pattern,Pattern.DOTALL);
            boolean ok = p.matcher(text).matches();
            if(!ok) {
                System.out.println("Pattern: "+pattern);
                System.out.println("-------");
                System.out.println(text);
                        
                log("Pattern: "+pattern);
                log("-------");
                log(text);
                
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
        String pattern  = ".*new Runnable\\(\\) \\{.*public void run\\(\\) \\{.*throw new UnsupportedOperationException\\(\"Not supported yet.\"\\);.*\\}.*\\}.*";
        performCodeCompletion("TestSimpleCase","new Runnable", 10, 1, pattern, false);
    }
    
    public void testAddClassWithImport() {
        String pattern  = ".*import java.io.IOException;.*IOException.*";
        performCodeCompletion("TestSimpleCase","IOE", 10, 2, pattern, true);
    }
    
    public void testAddSuperClass() {
        String pattern  = ".*extends Number.*";
        performCodeCompletion("ContextAware","extends Nu", 11, 2, pattern, false);
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
        TestRunner.run(GuiTest.class);
        
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GuiTest.class).enableModules(".*").clusters(".*"));
    }
    
}
