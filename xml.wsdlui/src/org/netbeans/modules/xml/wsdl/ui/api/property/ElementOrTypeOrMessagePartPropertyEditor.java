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
 * Created on May 16, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ElementOrTypeOrMessagePartPropertyEditor extends PropertyEditorSupport
        implements ExPropertyEditor {

    /** Environment passed to the ExPropertyEditor*/
    private PropertyEnv mEnv;
    private ElementOrTypeOrMessagePartProvider mElementOrTypeOrMessagePartProvider;

    public ElementOrTypeOrMessagePartPropertyEditor(ElementOrTypeOrMessagePartProvider elementOrTypeProvider) {
        this.mElementOrTypeOrMessagePartProvider = elementOrTypeProvider;
    }

    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        this.mEnv = env;
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }

    @Override
    public String getAsText() {
        if (mElementOrTypeOrMessagePartProvider != null) {
            ElementOrTypeOrMessagePart value = mElementOrTypeOrMessagePartProvider.getValue();
            if (value != null) {
                return value.toString();
            }
        }
        return "";
    }

    /** @return tags */
    @Override
    public String[] getTags() {
        return null;
    }

    /** @return true */
    @Override
    public boolean supportsCustomEditor() {
        return XAMUtils.isWritable(mElementOrTypeOrMessagePartProvider.getModel());
    }

    /** @return editor component */
    @Override
    public Component getCustomEditor() {
        final ElementOrTypeOrMessagePartPropertyPanel editor = new ElementOrTypeOrMessagePartPropertyPanel(mElementOrTypeOrMessagePartProvider, this.mEnv);
        mEnv.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ((PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID)) {
                    org.netbeans.modules.xml.xam.Component component = editor.getSelectedElementOrTypeOrMessagePart();
                    ElementOrTypeOrMessagePart value = null;
                    if (component instanceof Part) {
                        value = new ElementOrTypeOrMessagePart((Part) component, mElementOrTypeOrMessagePartProvider.getModel());
                    } else if (component instanceof GlobalType) {
                        value = new ElementOrTypeOrMessagePart((GlobalType) component, mElementOrTypeOrMessagePartProvider.getModel());
                    } else if (component instanceof GlobalElement) {
                        value = new ElementOrTypeOrMessagePart((GlobalElement) component, mElementOrTypeOrMessagePartProvider.getModel());
                    }
                    if (value != null) {
                        setValue(value);
                    }
                }
            }
        });

        return editor;
    }
}


