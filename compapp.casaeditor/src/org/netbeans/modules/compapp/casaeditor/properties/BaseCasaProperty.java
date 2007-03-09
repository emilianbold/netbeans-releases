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

package org.netbeans.modules.compapp.casaeditor.properties;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.Node;

/**
 *
 * @author Josh
 */
public abstract class BaseCasaProperty extends Node.Property {
    
    private CasaNode mNode;
    private String mPropertyType;
    private CasaComponent mComponent;
    
    
    public BaseCasaProperty(
            CasaNode node,
            CasaComponent component, 
            String propertyType, 
            Class valueType,
            String property,
            String propDispName, 
            String propDesc)
    {
        super(valueType);
        
        mNode = node;
        mComponent = component;
        mPropertyType = propertyType;
        
        super.setName(property);
        super.setDisplayName(propDispName);
        super.setShortDescription(propDesc);
    }

    
    public boolean canRead() {
        return true;
    }
    
    @Override
    public boolean canWrite() {
        try {
            
            CasaDataEditorSupport editorSupport = mNode.getDataObject().getEditorSupport();
            if (editorSupport == null || !editorSupport.isDocumentLoaded()) {
                // Ensure the document is loaded, otherwise writes will surely fail.
                // A document may not be loaded if the user closed the editor.
                return false;
            }
            
            Model model = mComponent.getModel();
            return XAMUtils.isWritable(model) && mNode.isEditable(mPropertyType);
        } catch (Throwable t) {
            // At this point we may be inside property rendering.
            // We cannot throw up an error dialog, instead we exit quietly.
            // Log the error.
            t.printStackTrace(System.err);
            return false;
        }
    }
    
    @Override
    public boolean isDefaultValue () {
        try {
            return getValue() == null;
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        } catch (IllegalAccessException ex) {
        }
        return false;
    }

    @Override
    public boolean supportsDefaultValue () {
        return true;
    }

    @Override
    public void restoreDefaultValue()
    throws IllegalAccessException, InvocationTargetException {
        setValue(null);
    }
    
    protected CasaWrapperModel getModel() {
        return mNode.getModel();
    }
    
    protected CasaComponent getComponent() {
        return mComponent;
    }
}
