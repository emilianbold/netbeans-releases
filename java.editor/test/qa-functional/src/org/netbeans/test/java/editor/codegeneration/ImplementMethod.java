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


import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.java.editor.jelly.GenerateCodeOperator;
import org.netbeans.test.java.editor.jelly.GenerateConstructorOperator;
import org.netbeans.test.java.editor.jelly.ImplementMethodsOperator;

/**
 *
 * @author Jiri Prox
 */
public class ImplementMethod extends GenerateCode {
        
    /** Creates a new instance of CreateConstructor */
    public ImplementMethod(String name) {
        super(name);
    }
            
    public void testIssue112613() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration.ImplementMethod", "test112613");
        editor = new EditorOperator("test112613");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(18, 5);
            assert(GenerateCodeOperator.containsItems(editor, GenerateCodeOperator.IMPLEMENT_METHOD,GenerateCodeOperator.GENERATE_CONSTRUCTOR, GenerateCodeOperator.OVERRIDE_METHOD));
         } finally {
            editor.close(false);
         }           
         openSourceFile("org.netbeans.test.java.editor.codegeneration.ImplementMethod", "test112613b");
         editor = new EditorOperator("test112613b");
         txtOper = editor.txtEditorPane();
         try {
            editor.requestFocus();
            editor.setCaretPosition(17, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.IMPLEMENT_METHOD,editor);
            ImplementMethodsOperator imo = new ImplementMethodsOperator();
            JTreeOperator jto = imo.treeTreeView$ExplorerTree();
            jto.selectRow(1);
            imo.btGenerate().push();
            String expected = "" +
"    public void m() {\n"+
"        throw new UnsupportedOperationException(\"Not supported yet.\");\n"+
"    }\n";                    
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }
    
    public static void main(String[] args) {
        TestRunner.run(ImplementMethod.class);
    }

}
