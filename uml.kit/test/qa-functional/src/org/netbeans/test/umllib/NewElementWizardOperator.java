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

import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;

public class NewElementWizardOperator extends JDialogOperator{    
    public final static String TITLE = "New Element Wizard";
    public final static String FINISH_BTN = "Finish";
    public final static String CANCEL_BTN = "Cancel";
    
    //
    private String ELEMENT_NAME_LABEL="Element Name:";
    private String NAMESPACE_LABEL="Namespace:";
    private String ELEMENT_TYPE_LABEL="Element Type:";
    
    
    public NewElementWizardOperator() {
        super(TITLE);
    }
    
    /**
     * 
     * @param elementName 
     */
    public void setElementName(String elementName){
        JLabelOperator pnLbl=new JLabelOperator(this,ELEMENT_NAME_LABEL);
        JTextFieldOperator txtName = new JTextFieldOperator((JTextField)(pnLbl.getLabelFor()));
        txtName.clearText();
        txtName.typeText(elementName);
        txtName.waitText(elementName);
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
     * @param elementType 
     * @return 
     */
    public boolean isElementAllowed(String elementType){
        return (new JListOperator(this).findItemIndex(elementType)>-1);
    }
        
    
    /**
     * 
     * @param elementType 
     */
    public void setElementType(String elementType){
        JLabelOperator dtLbl=new JLabelOperator(this,ELEMENT_TYPE_LABEL);
        JListOperator typeLst=new JListOperator((JList)(dtLbl.getLabelFor()));
        //JListOperator typeLst=new JListOperator(this);
        int index=typeLst.findItemIndex(elementType);
        if(index<0){
            String listInfo="Items:\n";
            for(int i=0;i<typeLst.getModel().getSize();i++)
            {
                listInfo+=typeLst.getModel().getElementAt(i)+"\n";
            }
            throw new NotFoundException("Can't find "+elementType+" in List, all elemnts: "+listInfo);
        }
        typeLst.selectItem(index);
        typeLst.waitItemSelection(index,true);
    }
        
    /**
     * 
     * @param path 
     */
    public void setNamespace(String path)
    {
        JComboBoxOperator namespaceCmb=new JComboBoxOperator(this);
        namespaceCmb.selectItem(path);
        namespaceCmb.waitItemSelected(path);
    }
}
