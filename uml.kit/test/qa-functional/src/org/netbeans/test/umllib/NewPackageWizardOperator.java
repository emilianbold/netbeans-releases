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

import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.jemmy.operators.CheckboxOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;

public class NewPackageWizardOperator extends JDialogOperator{    
    public final static String TITLE = "New Package Wizard";
    public final static String FINISH_BTN = "Finish";
    public final static String CANCEL_BTN = "Cancel";
    
    public final static String CLASS_DIAGRAM = NewDiagramWizardOperator.CLASS_DIAGRAM;
    public final static String SEQUENCE_DIAGRAM = NewDiagramWizardOperator.SEQUENCE_DIAGRAM;
    public final static String COMPONENT_DIAGRAM = NewDiagramWizardOperator.COMPONENT_DIAGRAM;
    public final static String COLLABORATION_DIAGRAM = NewDiagramWizardOperator.COLLABORATION_DIAGRAM;
    public final static String ACTIVITY_DIAGRAM = NewDiagramWizardOperator.ACTIVITY_DIAGRAM;
    public final static String DEPLOYMENT_DIAGRAM = NewDiagramWizardOperator.DEPLOYMENT_DIAGRAM;
    public final static String STATE_DIAGRAM = NewDiagramWizardOperator.STATE_DIAGRAM;
    public final static String USECASE_DIAGRAM = NewDiagramWizardOperator.USECASE_DIAGRAM;
    //
    private String DIAGRAM_NAME_LABEL="Diagram Name:";
    private String PACKAGE_NAME_LABEL="Name:";
    private String NAMESPACE_LABEL="Namespace:";
    private String DIAGRAM_TYPE_LABEL="Diagram Type:";
    
    
    public NewPackageWizardOperator() {
        super(TITLE);
    }
    
    /**
     * 
     * @param packageName 
     */
    public void setPackageName(String packageName){
        JLabelOperator pnLbl=new JLabelOperator(this,new JLabelOperator.JLabelByLabelFinder(PACKAGE_NAME_LABEL,new Operator.DefaultStringComparator(true,true)));;
        JTextFieldOperator txtName = new JTextFieldOperator((JTextField)(pnLbl.getLabelFor()));
        txtName.clearText();
        txtName.typeText(packageName);
        txtName.waitText(packageName);
    }
    
    
    /**
     * 
     * @param diagramName 
     */
    public void setDiagramName(String diagramName){
        JLabelOperator dnLbl=new JLabelOperator(this,DIAGRAM_NAME_LABEL);
        JTextFieldOperator txtName = new JTextFieldOperator((JTextField)(dnLbl.getLabelFor()));
        txtName.clearText();
        txtName.typeText(diagramName);
        txtName.waitText(diagramName);
    }
    
    /**
     * 
     * @return 
     */
    public boolean isDiagramCreationEnabled()
    {
            JLabelOperator dnmL=new JLabelOperator(this,DIAGRAM_NAME_LABEL);
            JTextFieldOperator dnmT=new JTextFieldOperator((JTextField)(dnmL.getLabelFor()));
            return dnmT.isEnabled();
    }
    
    /**
     * 
     * @param create 
     */
    public void setCreateDiagram(boolean create)
    {
        //
        if((create && !isDiagramCreationEnabled()) || (!create && isDiagramCreationEnabled()))
        {
            new JCheckBoxOperator(this).clickMouse(1);
        }
        JLabelOperator dnLbl=new JLabelOperator(this,DIAGRAM_NAME_LABEL);
        JTextFieldOperator txtName = new JTextFieldOperator((JTextField)(dnLbl.getLabelFor()));
        try {
            txtName.waitComponentEnabled();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void clickFinish()
    {
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
        return (new JListOperator(this).findItemIndex(diagramType)>-1);
    }
        
    
    /**
     * 
     * @param diagramType 
     */
    public void setDiagramType(String diagramType){
        JLabelOperator dtLbl=new JLabelOperator(this,DIAGRAM_TYPE_LABEL);
        JComboBoxOperator typeCmb=new JComboBoxOperator((JComboBox)(dtLbl.getLabelFor()));
        typeCmb.selectItem(diagramType);
        typeCmb.waitItemSelected(diagramType);
    }
        
    /**
     * 
     * @param path 
     */
    public void setNamespace(String path)
    {
        JLabelOperator nsLbl=new JLabelOperator(this,NAMESPACE_LABEL);
        JComboBoxOperator namespaceCmb=new JComboBoxOperator((JComboBox)(nsLbl.getLabelFor()));
        namespaceCmb.selectItem(path);
        namespaceCmb.waitItemSelected(path);
    }
    
    /**
     * 
     * @param packageName 
     * @param diagramName 
     * @param diagramType 
     */
    public void setScopedDiagram(String packageName,String diagramName,String diagramType)
    {
        setPackageName(packageName);
        if(!isDiagramCreationEnabled())
        {
            setCreateDiagram(true);
        }
        setDiagramType(diagramType);
        setDiagramName(diagramName);
    }
}
