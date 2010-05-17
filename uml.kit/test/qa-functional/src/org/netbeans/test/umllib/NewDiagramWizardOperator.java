/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.test.umllib;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.event.InputEvent;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;

public class NewDiagramWizardOperator extends JDialogOperator{
    public final static String TITLE = "Create New Diagram";
    public final static String FINISH_BTN = "Finish";
    /**
     * @deprecated ok button was replaced with finish button
     */
    public final static String OK_BTN = FINISH_BTN;
    public final static String CANCEL_BTN = "Cancel";
    
    public final static String CLASS_DIAGRAM = "Class Diagram";
    public final static String SEQUENCE_DIAGRAM = "Sequence Diagram";
    public final static String COMPONENT_DIAGRAM = "Component Diagram";
    public final static String COLLABORATION_DIAGRAM = "Collaboration Diagram";
    public final static String ACTIVITY_DIAGRAM = "Activity Diagram";
    public final static String DEPLOYMENT_DIAGRAM = "Deployment Diagram";
    public final static String STATE_DIAGRAM = "State Diagram";
    public final static String USECASE_DIAGRAM = "Use Case Diagram";
    //
    private String DIAGRAM_NAME_LABEL="Diagram Name:";
    private String NAMESPACE_LABEL="Namespace:";
    private String DIAGRAM_TYPE_LABEL="Diagram Type:";
    
    
    public NewDiagramWizardOperator() {
        super(TITLE);
    }
    
    
    /**
     *
     * @param diagramName
     */
    public void setDiagramName(String diagramName){
        JLabelOperator dnLbl=new JLabelOperator(this,DIAGRAM_NAME_LABEL);
        JTextFieldOperator txtName = new JTextFieldOperator((JTextField)(dnLbl.getLabelFor()));
        //
        txtName.waitComponentShowing(true);
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        //txtName.moveMouse(5,5);
        //txtName.clickMouse(5,5,1);
        
        java.awt.Robot rb=null;
        try {
            rb=new java.awt.Robot();
            Point p=txtName.getLocationOnScreen();
            rb.mouseMove(p.x+5,p.y+5);
            rb.mousePress(InputEvent.BUTTON1_MASK);
            rb.mouseRelease(InputEvent.BUTTON1_MASK);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        try {
            txtName.clearText();
        } catch(org.netbeans.jemmy.TimeoutExpiredException ex) {
            org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(null,"textClearing_");
            throw ex;
        }
        txtName.typeText(diagramName);
        txtName.waitText(diagramName);
    }
    
    /**
     * @deprecated ok button was replaced with finish button
     */
    public void clickOK(){
        clickFinish();
    }
    
    public void clickFinish() {
        new JButtonOperator(this, FINISH_BTN).push();
        this.waitClosed();
    }
    
    public void clickCancel(){
        new JButtonOperator(this, CANCEL_BTN).push();
        this.waitClosed();
    }
    
    
    /**
     *
     * @param diagramType
     * @return
     */
    public boolean isDiagramAllowed(String diagramType){
        JLabelOperator dtLbl=new JLabelOperator(this,DIAGRAM_TYPE_LABEL);
        return (new JListOperator((JList)(dtLbl.getLabelFor())).findItemIndex(diagramType)>-1);
    }
    
    
    /**
     *
     * @param diagramType
     */
    public void setDiagramType(String diagramType){
        JLabelOperator dtLbl=new JLabelOperator(this,DIAGRAM_TYPE_LABEL);
        JListOperator typeLst=new JListOperator((JList)(dtLbl.getLabelFor()));
        int index=typeLst.findItemIndex(diagramType);
        if(index<0) {
            String listInfo="Items:\n";
            for(int i=0;i<typeLst.getModel().getSize();i++) {
                listInfo+=typeLst.getModel().getElementAt(i)+"\n";
            }
            throw new NotFoundException("Can't find "+diagramType+" in List, all elemnts: "+listInfo);
        }
        typeLst.selectItem(index);
        typeLst.waitItemSelection(index,true);
    }
    
    /**
     *
     * @param path
     */
    public void setNamespace(String path) {
        JComboBoxOperator namespaceCmb=new JComboBoxOperator(this);
        namespaceCmb.selectItem(path);
        namespaceCmb.waitItemSelected(path);
    }
}
