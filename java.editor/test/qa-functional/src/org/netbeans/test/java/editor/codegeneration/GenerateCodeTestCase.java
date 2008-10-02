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
package org.netbeans.test.java.editor.codegeneration;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.test.java.editor.lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 *
 * @author Jiri Prox
 */
public class GenerateCodeTestCase extends EditorTestCase {

    public GenerateCodeTestCase(String testMethodName) {
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
//        if (isWin()) {
//            expected = expected.replace("\n", "\r\n");
//        }
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
