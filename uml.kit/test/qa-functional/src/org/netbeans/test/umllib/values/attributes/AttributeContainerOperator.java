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
 * AttributeContainerOperator.java
 *
 * Created on January 27, 2006, 1:31 PM
 *
 */

package org.netbeans.test.umllib.values.attributes;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.values.ValueOperator;
import org.netbeans.test.umllib.values.operatons.OperationElement;

/**
 *
 * @author Alexandr Scherbatiy
 */

public class AttributeContainerOperator extends ValueOperator {
    
    private DiagramElementOperator elementOperator;
    private CompartmentOperator attributeCompartment;
    
    
    /** Creates a new instance of AttributeContainerOperator */
    public AttributeContainerOperator(DiagramElementOperator elementOperator) {
        this.elementOperator = elementOperator;
        this.attributeCompartment = new CompartmentOperator(elementOperator, CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
    }

    
    //  ======================   Getting  Action  ================================
    
    public AttributeElement getAttribute(String name){
        
        if (name == null ) { return null; }
        
        AttributeElement[] elems = getAttributes();

        for(AttributeElement elem : elems){
            if( name.equals(elem.getName())){
                return elem;
            }
        }
            
        return null;
    }
    

    public AttributeElement getAttribute( int index){
        AttributeElement[] elems = getAttributes();
        return elems[index];
    }
    
    
    
    public AttributeElement[] getAttributes(){
        
        ArrayList<CompartmentOperator> list = attributeCompartment.getCompartments();
        ArrayList<AttributeElement> attributeList = new ArrayList<AttributeElement>();
        
        for(CompartmentOperator compar : list ){
            String parse = compar.getName();
            //System.out.println("--- get operation: \"" +  parse+ "\"");
            AttributeElement elem = AttributeElement.parseOperationElement(parse);
            attributeList.add(elem);
            
        }
        
        return attributeList.toArray(new AttributeElement[] {});
    }
    
    //  ======================= Add / Rename / Remove Actions ===================
    
    
    public void addAttribute(AttributeElement attributeElement){
        addOperationByContextMenu(attributeElement);
    }
    
    private void addOperationByContextMenu(AttributeElement attributeElement){
        
        System.out.println("***************************************************");
        AttributeElement old = new AttributeElement();
        System.out.println("***  element  = " + attributeElement);
        System.out.println("***  old      = " + old);
        System.out.println("***  text     = \"" + attributeElement.getText() + "\"");
        System.out.println("***  old text = \"" + old.getText() + "\"");
        attributeCompartment.getPopup().pushMenu(LabelsAndTitles.POPUP_ADD_ATTRIBUTE);
        
        EditControlOperator ec = new EditControlOperator();
        JTextFieldOperator textFieldOperator=ec.getTextFieldOperator();
        
        //tf.setText(operationElement.getText());
        textFieldOperator.setCaretPosition(0);
        
        changeText( attributeElement.getVisibility().getValue(), old.getVisibility().getValue(), textFieldOperator);
        changeText( attributeElement.getType().getValue(), old.getType().getValue(), textFieldOperator);
        changeText( attributeElement.getName(), old.getName(), textFieldOperator);
        
        textFieldOperator.pushKey(KeyEvent.VK_ENTER);
        
    }
    
    
    
    
}
