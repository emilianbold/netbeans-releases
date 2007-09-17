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
 * @author Jiri Prox
 */
public class ImplAllAbstractTest extends HintsTestCase{
    
    public ImplAllAbstractTest(String name) {
        super(name);
    }
    
    public void testImplementAbstract() {
        String file = "AllAbs";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(4,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*public void run\\(\\) \\{.*throw new UnsupportedOperationException\\(\\);.*\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public void testImplementAbstract2() {
        String file = "AllAbs2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(6,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*public int getRowCount\\(\\) \\{.*" +
                "throw new UnsupportedOperationException\\(\\);.*" +
                "\\}.*" +
                "public int getColumnCount\\(\\) \\{.*" +
                "throw new UnsupportedOperationException\\(\\);.*" +
                "\\}.*"+
                "public Object getValueAt\\(int rowIndex, int columnIndex\\) \\{.*"+
                "throw new UnsupportedOperationException\\(\\);.*" +
                "\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public void testImplementAbstract3() {
        String file = "AllAbs2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(11,1);
        new EventTool().waitNoEvent(750);
        String pattern = ".*\\{.*public int compareTo\\(T o\\) \\{.*throw new UnsupportedOperationException\\(\\);.*\\}.*\\}.*";
        useHint("Implement",new String[]{"Implement all abstract methods"},pattern);
    }
    
    public static void main(String[] args) {
        new TestRunner().run(ImplAllAbstractTest.class);
    }
    
}
