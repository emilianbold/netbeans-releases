/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.codegen;

import java.awt.Dialog;
import java.lang.ref.WeakReference;
import javax.lang.model.element.Element;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach
 */
public class EqualsHashCodeGeneratorTest extends NbTestCase {
    FileObject fo;
    
    public EqualsHashCodeGeneratorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fo = SourceUtilsTestUtil.makeScratchDir(this);
        System.setProperty("netbeans.user", getWorkDirPath());
        SourceUtilsTestUtil.setLookup(new Object[] { new DD() }, getClass().getClassLoader());
    }
    
    

    public void testEnabledFieldsWhenHashCodeExists() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        String what1 = "class X {" +
            "  private int x;" +
            "  private int y;" +
            "  public int hashCode() {";
        
        String what2 = 
            "    return y;" +
            "  }" +
            "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);
        
        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);
        
        class Task implements org.netbeans.api.java.source.Task<CompilationController> {
            EqualsHashCodeGenerator generator;
            

            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element el = cc.getElements().getTypeElement("X");
                generator = EqualsHashCodeGenerator.createEqualsHashCodeGenerator(cc, el);
            }
            
            
            public void post() throws Exception {
                assertNotNull("panel", generator);
                
                assertEquals("Two fields", 2, generator.description.getSubs().size());
                assertEquals("y field selected", true, generator.description.getSubs().get(1).isSelected());
                assertEquals("x field not selected", false, generator.description.getSubs().get(0).isSelected());
                assertEquals("generate hashCode", false, generator.generateHashCode);
                assertEquals("generate equals", true, generator.generateEquals);
            }
        }
        Task t = new Task();
        
        js.runUserActionTask(t, false);
        t.post();
        
        
        JTextArea c = new JTextArea();
        c.getDocument().putProperty(JavaSource.class, new WeakReference<Object>(js));
        c.getDocument().insertString(0, what, null);
        c.setCaretPosition(what1.length());
        
        t.generator.invoke(c);
        
        //String text = c.getDocument().getText(0, c.getDocument().getLength());
        String text = GeneratorUtilsTest.readFromFile(java);
        
        int first = text.indexOf("hashCode");
        if (first < 0) {
            fail("There should be one hashCode mehtod:\n" + text);
        }
        int snd = text.indexOf("hashCode", first + 5);
        if (snd >= 0) {
            fail("Only one hashCode:\n" + text);
        }
    }
    public void testEnabledFieldsWhenEqualsExists() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        String what1 = "class X {" +
            "  private int x;" +
            "  private int y;" +
            "  public Object equals(Object snd) {" +
            "    if (snd instanceof X) {" +
            "       X x2 = (X)snd;";
        
        String what2 = 
            "       return this.x == x2.x;" +
            "    }" +
            "    return false;" +
            "  }" +
            "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);
        
        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);
        
        class Task implements CancellableTask<CompilationController> {
            EqualsHashCodeGenerator generator;
            
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element el = cc.getElements().getTypeElement("X");
                generator = EqualsHashCodeGenerator.createEqualsHashCodeGenerator(cc, el);
            }
            
            
            public void post() throws Exception {
                assertNotNull("panel", generator);
                
                assertEquals("Two fields", 2, generator.description.getSubs().size());
                assertEquals("x field selected", true, generator.description.getSubs().get(0).isSelected());
                assertEquals("y field not selected", false, generator.description.getSubs().get(1).isSelected());
                assertEquals("generate hashCode", true, generator.generateHashCode);
                assertEquals("generate equals", false, generator.generateEquals);
            }
        }
        Task t = new Task();
        
        js.runUserActionTask(t, false);
        t.post();
    }
    
    public void test125114() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "class X {" +
            "  private int x;" +
            "  private int y;" +
            "  public void test() {" +
            "    new Object() {" +
            "       private int i;";
        
        String what2 = 
            "    }" +
            "  }" +
            "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);
        
        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);
        
        class TaskImpl implements Task<CompilationController> {
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Element el = cc.getTrees().getElement(cc.getTreeUtilities().pathFor(what1.length()));
                assertNull(EqualsHashCodeGenerator.createEqualsHashCodeGenerator(cc, el));
            }
        }
        TaskImpl t = new TaskImpl();
        
        js.runUserActionTask(t, false);
    }
    
    private static final class DD extends DialogDisplayer {

        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            descriptor.setValue(descriptor.getDefaultValue());
            
            return new JDialog() {
                public void setVisible(boolean b) {
                }
            };
        }
        
    }
}
