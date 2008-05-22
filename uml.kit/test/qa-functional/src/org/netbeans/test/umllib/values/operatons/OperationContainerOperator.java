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


/*
 * OperationOperator.java
 *
 * Created on December 6, 2005, 4:41 PM
 *
 */

package org.netbeans.test.umllib.values.operatons;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.util.PopupConstants;
import org.netbeans.test.umllib.values.Argument;
import org.netbeans.test.umllib.values.ValueOperator;
/**
 *
 * @author Alexandr Scherbatiy
 */

public class OperationContainerOperator extends ValueOperator {
    
    private DiagramElementOperator elementOperator;
    private CompartmentOperator operationCompartment;
    
    
    /** Creates a new instance of OperationOperator */
    public OperationContainerOperator(DiagramElementOperator elementOperator) {
        this.elementOperator = elementOperator;
        this.operationCompartment = new CompartmentOperator(elementOperator, CompartmentTypes.OPERATION_LIST_COMPARTMENT);
    }
    
    
    
    //  ======================   Getting  Action  ================================
    
    public OperationElement getOperation(String name){
        return getOperation(name, 0);
    }
    
    public OperationElement getOperation(String name, int index){
        OperationElement[] elems = getOperations(name);
        return elems[index];
    }

    public OperationElement getOperation( int index){
        OperationElement[] elems = getOperations();
        return elems[index];
    }
    
    
    public OperationElement[] getOperations(){
        return getOperations(null);
    }
    
    public OperationElement[] getOperations(String name){
        
        ArrayList<CompartmentOperator> list = operationCompartment.getCompartments();
        ArrayList<OperationElement> operationList = new ArrayList<OperationElement>();
        
        for(CompartmentOperator compar : list ){
            String parse = compar.getName();
            //System.out.println("--- get operation: \"" +  parse+ "\"");
            OperationElement elem = OperationElement.parseOperationElement(parse);
            if (name == null || elem.getName().equals(name)){
                operationList.add(elem);
            }
            
        }
        
        return operationList.toArray(new OperationElement[] {});
    }
    
    
    
    
    //  ======================= Add / Rename / Remove Actions ===================
    
    
    
    public void addOperation(OperationElement operationElement, String projectTreePath){
        
        System.out.println("*** add operation by tree Path: \"" + projectTreePath + "\"");
        Node treeNode = new Node(new ProjectsTabOperator().tree(), projectTreePath);
        treeNode.performPopupAction(PopupConstants.ADD_OPERATION);

        addOperationByContextMenu(operationElement);
    }
    
    
    public void addOperation(OperationElement operationElement){
        addOperationByContextMenu(operationElement);
    }
    
    private void addOperationByContextMenu(OperationElement operationElement){
        
        System.out.println("***************************************************");
        OperationElement old = new OperationElement();
        System.out.println("***  element  = " + operationElement);
        System.out.println("***  old      = " + old);
        System.out.println("***  text     = \"" + operationElement.getText() + "\"");
        System.out.println("***  old text = \"" + old.getText() + "\"");
        operationCompartment.getPopup().pushMenu(LabelsAndTitles.POPUP_ADD_OPERATION);
        
        EditControlOperator ec = new EditControlOperator();
        JTextFieldOperator textFieldOperator=ec.getTextFieldOperator();
        
        //tf.setText(operationElement.getText());
        textFieldOperator.setCaretPosition(0);
        
        changeText( operationElement.getVisibility().getValue(), old.getVisibility().getValue(), textFieldOperator);
        changeText( operationElement.getType().getValue(), old.getType().getValue(), textFieldOperator);
        changeText( operationElement.getName(), old.getName(), textFieldOperator);
        
        
        List<Argument> argList    = operationElement.getArguments();
        
        // TBD: should add multiple arguments!!!
        
        for(int i=0; i < argList.size(); i++){
            moveRight(textFieldOperator);
            printText(argList.get(i).getType().getValue(),  textFieldOperator);
            printText(argList.get(i).getName(), textFieldOperator);
        }
        
        pressEnter(textFieldOperator);
    }
    
    
}
