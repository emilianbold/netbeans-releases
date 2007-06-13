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

import java.util.Random;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator;
import static org.netbeans.modules.css.test.operator.StyleBuilderOperator.Panes.*;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator.BackgroundPaneOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class TestBackgroundSettings extends CSSTest {
    
    /** Creates new CSS Test */
    public TestBackgroundSettings(String testName) {
        super(testName);
    }
    
    public void testBGColor(){
//        BackgroundPaneOperator backgroundPane = initializeBackgroundChanging();
//        ColorSelectionField colorPanel = backgroundPane.getColor();
//        //--------INSERT ONCE--------//
//        colorPanel.setColorString("red");
//        waitUpdate();
//        assertTrue("INSERTING", getRootRuleText().contains("background-color: red"));
//        //--------  UPDATE   --------//
//        colorPanel.setColorString("green");
//        waitUpdate();
//        assertTrue("UPDATING", getRootRuleText().contains("background-color: green"));
//        //-------- REMOVE -----------//
//        colorPanel.setColorString("");//<NOT SET>
//        waitUpdate();
//        assertFalse("REMOVING", getRootRuleText().contains("background-color"));
    }
    
    public void testTile(){
        BackgroundPaneOperator paneOperator = initializeBackgroundChanging();
        checkAtrribute("background-repeat", paneOperator.getTile());
    }

    public void testScroll(){
        BackgroundPaneOperator paneOperator = initializeBackgroundChanging();
        checkAtrribute("background-attachment", paneOperator.getScroll());
    }

    public void testHPosition(){
        BackgroundPaneOperator paneOperator = initializeBackgroundChanging();
        checkAtrribute("background-position", paneOperator.getHPosition(), true);
    }

    public void testVPosition(){
    }

    private BackgroundPaneOperator initializeBackgroundChanging(){
        EditorOperator eop = openFile(newFileName);
        eop.setVisible(true);
        eop.setCaretPositionToLine(rootRuleLineNumber);
        StyleBuilderOperator styleOper= new StyleBuilderOperator().invokeBuilder();
        return (BackgroundPaneOperator) styleOper.setPane(BACKGROUND);
    }
    
}