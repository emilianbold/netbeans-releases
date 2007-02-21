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
package org.netbeans.modules.bpel.properties.props.editors;

import java.beans.PropertyEditor;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.choosers.TypeChooserPanel;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class TypePropertyCustomizer extends TreeChooserPropertyCustomizer<TypeChooserPanel> {
    
    public TypePropertyCustomizer() {
        super();
    }
    
    protected TypeChooserPanel createChooserPanel() {
        return new TypeChooserPanel();
    }
    
    protected void applyNewValues() {
        TypeContainer type = getChooserPanel().getSelectedValue();
        myPropertyEditor.setValue(type);
    }
    
    public void init(PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
        assert propertyEnv != null && propertyEditor != null && 
                propertyEditor instanceof TypePropEditor : "Wrong params"; // NOI18N
        //
        super.init(propertyEnv, propertyEditor);
        //
        Object[] beans = myPropertyEnv.getBeans();
        BpelNode node = (BpelNode)beans[0];
        Lookup lookup = node.getLookup();
        //
        TypeChooserPanel chooserPanel = getChooserPanel();
        //
        chooserPanel.init(((TypePropEditor)myPropertyEditor).getSTypeFilter(), lookup);
        //
        // Set current selection
        Object value = propertyEditor.getValue();
        try {
            if (value == null) {
                chooserPanel.setSelectedValue(null);
            } else {
                assert value instanceof TypeContainer;
                chooserPanel.setSelectedValue((TypeContainer)value);
            }
        } catch (Exception e) {};
    }
    
}
