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

/*
 * DerivationTypeEditor.java
 *
 * Created on December 22, 2005, 12:58 PM
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.Derivation;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class DerivationTypeEditor  extends PropertyEditorSupport
    implements ExPropertyEditor {
    
    private String property;
    private String typeDisplayName;
    private HashMap<Derivation.Type,? extends Derivation> typeToDerivationMap = null;
    
    /**
     * Creates a new instance of DerivationTypeEditor
     */
    public DerivationTypeEditor(SchemaComponent component, String property, String typeDisplayName) {
        this.property = property;
        this.typeDisplayName = typeDisplayName;
        initialize(component);
    }
    
    private void initialize(SchemaComponent component) {
        if(component instanceof Element) {
            if(component instanceof GlobalElement && GlobalElement.FINAL_PROPERTY.equals(property)) {
                HashMap<Derivation.Type,GlobalElement.Final> tmpMap = new HashMap<Derivation.Type,GlobalElement.Final>();
                tmpMap.put(Derivation.Type.EMPTY,GlobalElement.Final.EMPTY);
                tmpMap.put(Derivation.Type.ALL,GlobalElement.Final.ALL);
                tmpMap.put(Derivation.Type.EXTENSION,GlobalElement.Final.EXTENSION);
                tmpMap.put(Derivation.Type.RESTRICTION,GlobalElement.Final.RESTRICTION);
                typeToDerivationMap = tmpMap;
            } else if(Element.BLOCK_PROPERTY.equals(property)) {
                HashMap<Derivation.Type,Element.Block> tmpMap = new HashMap<Derivation.Type,Element.Block>();
                tmpMap.put(Derivation.Type.EMPTY,Element.Block.EMPTY);
                tmpMap.put(Derivation.Type.ALL,Element.Block.ALL);
                tmpMap.put(Derivation.Type.EXTENSION,Element.Block.EXTENSION);
                tmpMap.put(Derivation.Type.RESTRICTION,Element.Block.RESTRICTION);
                tmpMap.put(Derivation.Type.SUBSTITUTION,Element.Block.SUBSTITUTION);
                typeToDerivationMap = tmpMap;
            }
        } else if(component instanceof GlobalComplexType) {
            if(GlobalComplexType.FINAL_PROPERTY.equals(property)) {
                HashMap<Derivation.Type,GlobalComplexType.Final> tmpMap = new HashMap<Derivation.Type,GlobalComplexType.Final>();
                tmpMap.put(Derivation.Type.EMPTY,GlobalComplexType.Final.EMPTY);
                tmpMap.put(Derivation.Type.ALL,GlobalComplexType.Final.ALL);
                tmpMap.put(Derivation.Type.EXTENSION,GlobalComplexType.Final.EXTENSION);
                tmpMap.put(Derivation.Type.RESTRICTION,GlobalComplexType.Final.RESTRICTION);
                typeToDerivationMap = tmpMap;
            } else if(GlobalComplexType.BLOCK_PROPERTY.equals(property)) {
                HashMap<Derivation.Type,GlobalComplexType.Block> tmpMap = new HashMap<Derivation.Type,GlobalComplexType.Block>();
                tmpMap.put(Derivation.Type.EMPTY,GlobalComplexType.Block.EMPTY);
                tmpMap.put(Derivation.Type.ALL,GlobalComplexType.Block.ALL);
                tmpMap.put(Derivation.Type.EXTENSION,GlobalComplexType.Block.EXTENSION);
                tmpMap.put(Derivation.Type.RESTRICTION,GlobalComplexType.Block.RESTRICTION);
                typeToDerivationMap = tmpMap;
            }
        } else if(component instanceof GlobalSimpleType) {
            if(GlobalSimpleType.FINAL_PROPERTY.equals(property)) {
                HashMap<Derivation.Type,GlobalSimpleType.Final> tmpMap = new HashMap<Derivation.Type,GlobalSimpleType.Final>();
                tmpMap.put(Derivation.Type.EMPTY,GlobalSimpleType.Final.EMPTY);
                tmpMap.put(Derivation.Type.ALL,GlobalSimpleType.Final.ALL);
                tmpMap.put(Derivation.Type.RESTRICTION,GlobalSimpleType.Final.RESTRICTION);
                tmpMap.put(Derivation.Type.LIST,GlobalSimpleType.Final.LIST);
                tmpMap.put(Derivation.Type.UNION,GlobalSimpleType.Final.UNION);
                typeToDerivationMap = tmpMap;
            }
        } else if(component instanceof Schema) {
            if(Schema.FINAL_DEFAULT_PROPERTY.equals(property)) {
                HashMap<Derivation.Type,Schema.Final> tmpMap = new HashMap<Derivation.Type,Schema.Final>();
                tmpMap.put(Derivation.Type.EMPTY,Schema.Final.EMPTY);
                tmpMap.put(Derivation.Type.ALL,Schema.Final.ALL);
                tmpMap.put(Derivation.Type.EXTENSION,Schema.Final.EXTENSION);
                tmpMap.put(Derivation.Type.RESTRICTION,Schema.Final.RESTRICTION);
                tmpMap.put(Derivation.Type.LIST,Schema.Final.LIST);
                tmpMap.put(Derivation.Type.UNION,Schema.Final.UNION);
                typeToDerivationMap = tmpMap;
            } else if(Schema.BLOCK_DEFAULT_PROPERTY.equals(property)) {
                HashMap<Derivation.Type,Schema.Block> tmpMap = new HashMap<Derivation.Type,Schema.Block>();
                tmpMap.put(Derivation.Type.EMPTY,Schema.Block.EMPTY);
                tmpMap.put(Derivation.Type.ALL,Schema.Block.ALL);
                tmpMap.put(Derivation.Type.EXTENSION,Schema.Block.EXTENSION);
                tmpMap.put(Derivation.Type.RESTRICTION,Schema.Block.RESTRICTION);
                typeToDerivationMap = tmpMap;
            }
        }
    }

    public String getAsText() {
        Object val = getValue();
        if (val == null){
            return null;
        }
        if (val instanceof Set){
            return ((Set)val).toString();
        }
        // TODO how to display invalid values?
        return val.toString();
    }
    
    private Set<Derivation.Type> convertToDerivationType(
            Set<Derivation> derivationSet) {
        Set<Derivation.Type> derivationTypeSet = new HashSet<Derivation.Type>();
        if (derivationSet != null && !derivationSet.isEmpty()) {
            Set<Derivation.Type> keys = typeToDerivationMap.keySet();
            int i = 0;
            for(Derivation.Type key:keys) {
                if (derivationSet.contains(typeToDerivationMap.get(key))) {
                    derivationTypeSet.add(key);
                    i++;
                    if (i==derivationSet.size()) break;
                }
            }
        }
        return derivationTypeSet;
    }
    
    private Set<? extends Derivation> convertToDerivation(
            Set<Derivation.Type> derivationTypeSet) {
        Set<Derivation> derivationSet = new HashSet<Derivation>();
        for(Derivation.Type type: derivationTypeSet) {
            derivationSet.add(typeToDerivationMap.get(type));
        }
        return derivationSet;
    }

    @SuppressWarnings("unchecked")
    public Component getCustomEditor() {
        Object obj = getValue();
        Set<Derivation> initialValue = Collections.emptySet();
        if(obj instanceof Set) initialValue = (Set<Derivation>)obj;
        final DerivationTypeForm panel = new DerivationTypeForm(property,
                convertToDerivationType(initialValue),typeToDerivationMap.keySet());
        final DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(DerivationTypeEditor.class, "LBL_Derivation_Type_Editor_Title",
                typeDisplayName, NbBundle.getMessage(DerivationTypeEditor.class, "LBL_"+property)),
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        Set<Derivation.Type> currentSelection =
                                panel.getCurrentSelection();
                        setValue(currentSelection.isEmpty()?null:
                            convertToDerivation(currentSelection));
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                                iae.getMessage(), iae.getLocalizedMessage(), 
                                null, new java.util.Date());
                        throw iae;
                    }
                }
            }
        });
        
        // enable/disable the dlg ok button depending selection
        panel.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("valid")) {
                    descriptor.setValid(((Boolean)evt.getNewValue()).booleanValue());
                }
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        // set size to 400x300
        //dlg.setPreferredSize(panel.getSize());
        dlg.setPreferredSize(new java.awt.Dimension(400,300));
        return dlg;
    }
    
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public void attachEnv(PropertyEnv env ) {
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }

}
