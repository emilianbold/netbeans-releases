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

/*
 * JavaAbbreviationsTest.java
 *
 * Created on August 29, 2002, 2:52 PM
 */

package org.netbeans.test.editor.suites.abbrevs;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author  Jan Lahoda
 */
public class JavaAbbreviationsTestPerformer extends AbbreviationsTest {

    /** Creates a new instance of JavaAbbreviationsTest */
    public JavaAbbreviationsTestPerformer(String name) {
        super(name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //new JavaAbbreviationsTestPerformer("testAbbreviationTest").testAbbreviationTest();
/*        new JavaAbbreviationsTestPerformer("testAbbreviationWithoutExpansion").testAbbreviationWithoutExpansion();
        new JavaAbbreviationsTestPerformer("testAbbreviationInsideComment").testAbbreviationInsideComment();
        new JavaAbbreviationsTestPerformer("testAbbreviationAdd").testAbbreviationAdd();
        new JavaAbbreviationsTestPerformer("testAbbreviationRemove").testAbbreviationRemove();
        new JavaAbbreviationsTestPerformer("testAbbreviationChange").testAbbreviationChange();
        new JavaAbbreviationsTestPerformer("testAbbreviationOKCancel").testAbbreviationOKCancel();*/
/*        JavaAbbreviationsTest j = new JavaAbbreviationsTestPerformer("testAbbreviationTest");
 
        j.prepareEditor();
        j.moveCaretIntoCode();*/
        //        printAbbreviations("Java Editor");
    }
    
    public Abbreviation[] getAbbreviationsToAdd() {
        return new Abbreviation[] {
            new Abbreviation("test", "testIk", "", ""),
            new Abbreviation("ttest", "test|test", "", ""),
            new Abbreviation("tryc", "try {\n    |\n} catch (Exception ex) {\n    ex.printStackTrace();\n}", "", "")
        };
    }
    
    public Abbreviation[] getAbbreviationsToModify() {
        return new Abbreviation[] {
            new Abbreviation("cl1", "clazz", "cl", "class"),
        };
    }
    
    public Abbreviation[] getAbbreviationsToRemove() {
        return new Abbreviation[] {
            new Abbreviation("Psfb", "", "", ""),
        };
    }
    
    public Abbreviation[] getDefaultAbbreviations() {
        return new Abbreviation[] {
            new Abbreviation("sh", "short ", "", ""),
            new Abbreviation("Psfb", "public static final boolean ", "", ""),
            new Abbreviation("cl", "class ", "", ""),
            new Abbreviation("Re", "Rectangle", "", ""),
            new Abbreviation("ie", "interface ", "", ""),
            new Abbreviation("tw", "throw ", "", ""),
            new Abbreviation("df", "default:", "", ""),
            new Abbreviation("tds", "Thread.dumpStack();", "", ""),
            new Abbreviation("St", "String", "", ""),
            new Abbreviation("tr", "transient ", "", ""),
            new Abbreviation("pr", "private ", "", ""),
            new Abbreviation("th", "throws ", "", ""),
            new Abbreviation("impb", "import java.beans.", "", ""),
            new Abbreviation("imps", "import javax.swing.", "", ""),
            new Abbreviation("Ve", "Vector", "", ""),
            new Abbreviation("iof", "instanceof ", "", ""),
            new Abbreviation("le", "length", "", ""),
            new Abbreviation("fa", "false", "", ""),
            new Abbreviation("wh", "while (", "", ""),
            new Abbreviation("sw", "switch (", "", ""),
            new Abbreviation("Psf", "public static final ", "", ""),
            new Abbreviation("psfs", "private static final String ", "", ""),
            new Abbreviation("sy", "synchronized ", "", ""),
            new Abbreviation("serr", "System.err.println(\"|\");", "", ""),
            new Abbreviation("Psfs", "public static final String ", "", ""),
            new Abbreviation("pe", "protected ", "", ""),
            new Abbreviation("ab", "abstract ", "", ""),
            new Abbreviation("Psfi", "public static final int ", "", ""),
            new Abbreviation("Ex", "Exception", "", ""),
            new Abbreviation("st", "static ", "", ""),
            new Abbreviation("eq", "equals", "", ""),
            new Abbreviation("Gr", "Graphics", "", ""),
            new Abbreviation("bo", "boolean ", "", ""),
            new Abbreviation("ir", "import ", "", ""),
            new Abbreviation("pu", "public ", "", ""),
            new Abbreviation("twn", "throw new ", "", ""),
            new Abbreviation("fy", "finally ", "", ""),
            new Abbreviation("impS", "import com.sun.java.swing.", "", ""),
            new Abbreviation("psfb", "private static final boolean ", "", ""),
            new Abbreviation("ca", "catch (", "", ""),
            new Abbreviation("impa", "import java.awt.", "", ""),
            new Abbreviation("impd", "import org.netbeans.", "", ""),
            new Abbreviation("pst", "printStackTrace();", "", ""),
            new Abbreviation("twne", "throw new Error();", "", ""),
            new Abbreviation("psf", "private static final ", "", ""),
            new Abbreviation("psfi", "private static final int ", "", ""),
            new Abbreviation("fi", "final ", "", ""),
            new Abbreviation("impj", "import java.", "", ""),
            new Abbreviation("fl", "float ", "", ""),
            new Abbreviation("ex", "extends ", "", ""),
            new Abbreviation("im", "implements ", "", ""),
            new Abbreviation("cn", "continue", "", ""),
            new Abbreviation("Ob", "Object", "", ""),
            new Abbreviation("sout", "System.out.println(\"|\");", "", ""),
            new Abbreviation("En", "Enumeration", "", ""),
            new Abbreviation("re", "return ", "", ""),
            new Abbreviation("br", "break", "", ""),
            new Abbreviation("twni", "throw new InternalError();", "", ""),
            new Abbreviation("impq", "import javax.sql.", "", ""),
        };
    }
    
    public String getEditorName() {
        return "Java Editor";
    }
    
    private EditorOperator editor = null;
    
    public synchronized EditorOperator getTestEditor() {
        if (editor == null) {
            
            FileObject fo = Repository.getDefault().findResource("org/netbeans/test/editor/suites/abbrevs/data/testfiles/JavaAbbreviationsTest/Test.java");
            
            try {
                DataObject   od = DataObject.find(fo);
                EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
                
                ec.open();
                
                editor = new EditorOperator("Test");
            } catch (DataObjectNotFoundException e) {
                assertTrue(false);
            }
        }
        
        return editor;
    }
    
    public void moveCaretIntoCode() {
        getTestEditor().pushKey(KeyEvent.VK_END, KeyEvent.CTRL_MASK);
    }
    
    public void moveCaretIntoComment() {
        moveCaretIntoCode();
        getTestEditor().txtEditorPane().typeText("/* */");
        getTestEditor().pushKey(KeyEvent.VK_LEFT);
        getTestEditor().pushKey(KeyEvent.VK_LEFT);
    }
    
    public void prepareEditor() {
        EditorOperator op = getTestEditor();
        
        op.pushKey(KeyEvent.VK_HOME, KeyEvent.CTRL_MASK);
        op.pushKey(KeyEvent.VK_END, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
        //        op.select(op.getText());
        //        op.select(1, op.getLineNumber());
        op.pushKey(KeyEvent.VK_DELETE);
    }
    
    public String getEditorOptionsClassName() {
        return "org.netbeans.modules.editor.options.JavaOptions";
    }
    
    public void finishEditor() {
        getTestEditor().closeDiscard();
    }
    
}
