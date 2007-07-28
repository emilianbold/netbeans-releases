/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ParameterCustomizer.java
 *
 * Created on August 12, 2005, 4:53 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.propertysupport.customizers;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import java.awt.BorderLayout;
import java.util.Vector;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder.ValidValues;
import org.netbeans.modules.uml.propertysupport.nodes.CustomPropertyEditor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
/**
 *
 * @author thuy
 */
public class ParameterCustomizer extends JPanel 
        implements Customizer, EnhancedCustomPropertyEditor {
    
    private IPropertyElement mElement = null;
    private ArrayList<ElementData> paramPropElements;
    private IPropertyDefinition mDefinition = null;
    private CustomPropertyEditor mEditor = null;
    private ParameterCustomizerPanel paramUIPanel;
    
    /** Creates a new instance of ParameterCustomizer */
    public ParameterCustomizer() {
        super();
        // create customizer panel
        paramUIPanel = new ParameterCustomizerPanel();
        setLayout(new java.awt.BorderLayout());
        add(paramUIPanel, BorderLayout.CENTER);
    }
    
    public void setElement(IPropertyElement element, IPropertyDefinition def){
        mElement = element;
        mDefinition = def;
        initializeTypes();
        initializeKindnDirectionList();
        initializeParamList();
    }
    
    public void setPropertySupport(CustomPropertyEditor editor) {
        mEditor = editor;
    }
    
    protected void notifyChanged() {
        if(mEditor != null) {
            mEditor.firePropertyChange();
        }
    }
    
    protected void initializeParamList() {
        Vector < IPropertyElement > elements = mElement.getSubElements();
        paramPropElements = new ArrayList <ElementData>();
        ElementData elemData = null;
        if (elements != null && elements.size() > 0) {
            for (IPropertyElement paramElement : elements) {
                elemData = new ElementData(paramElement);
                // for each parameter, get its multiplicty ranges
                initializeMultiplicityList(elemData);
                paramPropElements.add(elemData);
            }
        }
        paramUIPanel.setRootProp(mElement, mDefinition);
        paramUIPanel.setParamList(paramPropElements);
    }
    
    // Initialize Type JcomboBox with a list of valid types
    private void initializeTypes() {
        IStrings typeNames;
        typeNames = searchAllTypes();
        
        if (typeNames != null) {
            paramUIPanel.setTypeList(typeNames.toArray());
        }
    }
    
    private IStrings searchAllTypes() {
        IStrings list = new Strings();
        IProduct prod = ProductHelper.getProduct();
        if (prod != null) {
            IProductProjectManager pMan = prod.getProjectManager();
            if (pMan != null) {
                IProject proj = pMan.getCurrentProject();
                if (proj != null) {
                    ITypeManager typeMan = proj.getTypeManager();
                    if (typeMan != null) {
                        IPickListManager pickMan = typeMan.getPickListManager();
                        if (pickMan != null) {
                            String filter = "DataType Class Interface";  // NO I18N
                            list = pickMan.getTypeNamesWithStringFilter(filter);
                            // Fixed IZ=83449. Remove "void" type from the list if found
                            if ( list != null) {
                                if (list.isInList("void", true)) {
                                    list.remove("void");
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
    
    // Initialize Kind and Direction comboBoxes with a list of predefined values
    private void initializeKindnDirectionList() {
        if (mDefinition != null) {
            ValidValues validValues = getValidValues(mDefinition, ElementData.PARAM_DIRECTION);
            paramUIPanel.setDirectionList(validValues);
            validValues = getValidValues(mDefinition, ElementData.PARAM_KIND);
            paramUIPanel.setKindList(validValues);
        }
    }
    
    // get and return the predefined valid values of a given element
    private ValidValues getValidValues(IPropertyDefinition def, String elemName) {
        ValidValues validValues = null;
        if (def != null) {
            IPropertyDefinition subDef  = def.getSubDefinition(elemName);
            if (subDef != null) {
                validValues = DefinitionPropertyBuilder.instance().retreiveValueValue2(subDef);
            }
        }
        return validValues;
    }
    
    private void initializeMultiplicityList(ElementData elemData) {
        Vector multiRangeVec = new Vector();
        if (elemData != null) {
            IPropertyElement multiRanges = elemData.getMultiRangesProp();
            
            if ( multiRanges != null) { 
                Vector < IPropertyElement > elements = multiRanges.getSubElements();
                ElementData multiRangeProp = null;
                if (elements != null) {
                    for (IPropertyElement multiRangeElement : elements) {
                        multiRangeProp = new ElementData(multiRangeElement);
                        multiRangeVec.add(multiRangeProp);
                    }
                }
                elemData.setMultiRanges(multiRangeVec); 
            }
        }
    }
    
////////////////////////////////////////////////////////////////////////////
// EnhancedCustomPropertyEditor Implementation
    
    /**
     * Get the customized property value.  This implementation will
     * return an array of property elements.  Basically when this method
     * gets called the user has pressed the OK button.
     *
     * @return the property value
     * @exception IllegalStateException when the custom property editor does not contain a valid property value
     *            (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        
        Object retVal = null;
        if (paramUIPanel != null) {
            paramUIPanel.saveDataModel();
        }
        
        IPropertyElementManager manager = mElement.getPropertyElementManager();    
        IOperation op = (IOperation) mElement.getElement();
        
        manager.reloadElement(mElement.getElement(), mElement.getPropertyDefinition(), mElement); 
        mElement.getPropertyDefinition().setForceRefersh(true);
        //boolean isDuplicate = checkForCollision(op, op.getFeaturingClassifier()); 
        notifyChanged();
        return retVal;
    }
    
    
    public boolean checkForCollision(IOperation method, IClassifier clazz) {
        boolean duplicate = false;
        ETList < IOperation > operations = clazz.getOperationsByName(method.getName());
        for(IOperation op : operations) {
            // First make sure that we have two different operations
            if(!op.isSame(method)) {
                // check to see if these two operations have the same signature
                if(op.isSignatureSame(method)) {
                    duplicate = true;
                    break;
                }
            }
        }
        return duplicate;     
    } 
}
