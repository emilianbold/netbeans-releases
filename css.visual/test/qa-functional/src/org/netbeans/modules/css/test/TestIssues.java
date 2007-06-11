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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.css.test;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator;
import static org.netbeans.modules.css.test.operator.StyleBuilderOperator.Panes.*;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator.FontPaneOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class TestIssues extends CSSTest {
    
    /** Creates new CSS Test */
    public TestIssues(String testName) {
        super(testName);
    }
    
    public void test105562(){/*move end bracket, 105574 end semicolon should be added*/
        System.out.println("running test105562");
        String insertion = "h2{font-size: 10px}\n";
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(1);
        eop.insert(insertion);
        eop.setCaretPositionToLine(1);
        StyleBuilderOperator styleOper= new StyleBuilderOperator().invokeBuilder();
        FontPaneOperator fontPane = (FontPaneOperator) styleOper.setPane(FONT);
        JListOperator fontFamilies = fontPane.fontFamilies();
        fontFamilies.selectItem(3);
        waitUpdate();
        String selected = fontFamilies.getSelectedValue().toString();
        String text = eop.getText();
        assertFalse("END BRACKET IS MOVED",text.contains(insertion));
        String rule = text.substring(0, text.indexOf('}'));
        assertTrue("SEMICOLON ADDED", rule.contains("font-size: 10px;"));
        assertTrue("FONT FAMILY SOULD BE GENERATED INSIDE RULE",rule.contains("font-family: "+selected));
        eop.closeDiscardAll();
    }     
    
    public void test105568(){
        System.out.println("running test105568");
        String insertion = "h1{\ntext-decoration    : overline;\n}";
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(1);
        eop.insert(insertion);
        eop.setCaretPositionToLine(1);
        StyleBuilderOperator styleOper= new StyleBuilderOperator();
        waitUpdate();
        FontPaneOperator fontPane = (FontPaneOperator) styleOper.setPane(FONT);
        assertTrue(fontPane.isOverline());
    }
    
}