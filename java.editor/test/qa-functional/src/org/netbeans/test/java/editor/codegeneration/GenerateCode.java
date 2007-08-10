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

import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 *
 * @author jp159440
 */
public class GenerateCode extends EditorTestCase {

    public GenerateCode(String testMethodName) {
        super(testMethodName);
    }

    protected boolean contains(String text, String pattern) {
        return text.contains(pattern);
    }

    private boolean isWin() {
        return System.getProperty("os.name").contains("Windows");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openProject("java_editor_test");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected  void waitAndCompare(String expected) {
        if (isWin()) {
            expected = expected.replace("\n", "\r\n");
        }
        waitMaxMilisForValue(1500, new EditorValueResolver(expected), Boolean.TRUE);
        if (!contains(editor.getText(), expected)) {
            System.out.println("Pattern:");
            System.out.println(expected);
            System.out.println("-------------------");
            System.out.println(editor.getText());
        }
        assertTrue("Expected code is not inserted", contains(editor.getText(), expected));
    }
    protected EditorOperator editor;
    protected JEditorPaneOperator txtOper;
    
    protected  class EditorValueResolver implements ValueResolver {
        
        String text;
        
        public EditorValueResolver(String text) {
            this.text = text;
        }
        
        public Object getValue() {
            return editor.getText().contains(text);
        }
        
    }

}
