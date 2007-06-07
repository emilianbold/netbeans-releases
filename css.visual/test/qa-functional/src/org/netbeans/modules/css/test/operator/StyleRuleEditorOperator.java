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
package org.netbeans.modules.css.test.operator;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
/**
 *
 * @author Jindrich Sedek
 */
public class StyleRuleEditorOperator extends NbDialogOperator{
    private static final String radioClass = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "CLASS_NAME_LBL");
    private static final String radioHtmlElement = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "HTML_ELELEMT");
    private static final String radioElementID = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "ELEMENT_ID_LBL");
    private static final String addRule = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "ADD_RULE_LBL");
    private static final String moveUp = Bundle.getString("org.netbeans.modules.css.editor.Bundle", "MOVE_RULE_UP_LBL");
    private static final String moveDown = Bundle.getString("org.netbeans.modules.css.editor.Bundle", "MOVE_RULE_DOWN_LBL");

    public StyleRuleEditorOperator() {
        super(Bundle.getString("org.netbeans.modules.css.editor.Bundle", "STYLE_RULE_EDITOR_TITLE"));
    }
    
    public void up(String item){
        new JListOperator(this).selectItem(item);
        new JButtonOperator(this,moveUp).push();
    }

    public void down(String item){
        new JListOperator(this).selectItem(item);
        new JButtonOperator(this, moveDown).push();
    }
    
    public void selectClass(){
        new JRadioButtonOperator(this, radioClass).push();
    }
    
    public void selectClass(String elementName, String className){
        selectClass();
        setComboBox(elementName, 1);
        new JTextFieldOperator(this, 1).setText(className);
    }
    
    public void selectHtmlElement(){
        new JRadioButtonOperator(this, radioHtmlElement).push();
    }
    
    public void selectHtmlElement(String elementName){
        selectHtmlElement();
        setComboBox(elementName, 0);
    }
    
    private void setComboBox(String elementName, int comboBox){
        JComboBoxOperator operator = new JComboBoxOperator(this, comboBox);
        int i = 0;
        while (!operator.isEnabled()&&(i<10)){
            try{
                Thread.sleep(1000);
            }catch(InterruptedException interupt){
                throw new AssertionError(interupt);
            }
            i++;
        }
        if (!operator.isEnabled()) throw new AssertionError("COMBO BOX IS NOT ENABLED IN 10s");
        operator.selectItem(elementName);
    }
    
    public void selectElementID(){
        new JRadioButtonOperator(this, radioElementID).push();
    }
    
    public void selectElementID(String str){
        selectElementID();
        new JTextFieldOperator(this, 2).setText(str);
    }
    
    public void addRule(){
        new JButtonOperator(this, addRule).push();
    }
    
    public String getPreview(){
        return new JTextFieldOperator(this, 0).getText();
    }
    
}
