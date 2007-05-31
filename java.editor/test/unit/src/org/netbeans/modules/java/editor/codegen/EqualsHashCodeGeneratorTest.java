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
