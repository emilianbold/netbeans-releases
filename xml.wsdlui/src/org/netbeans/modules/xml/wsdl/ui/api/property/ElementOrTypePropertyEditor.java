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
import java.util.HashMap;
import java.util.Map;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrTypeChooserPanel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ElementOrTypePropertyEditor extends PropertyEditorSupport 
implements ExPropertyEditor, PropertyChangeListener {

    /** property name used by propertyChangeEvent */
    public static final String PROP_NAME = "ElementOrType";//NOI18N

    /** Environment passed to the ExPropertyEditor*/
    private PropertyEnv mEnv;
    
    private ElementOrTypeProvider mElementOrTypeProvider;
    
    
    public ElementOrTypePropertyEditor(ElementOrTypeProvider elementOrTypeProvider) {
        this.mElementOrTypeProvider = elementOrTypeProvider;
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
    
    
    
    
    /** @return tags */
    @Override
    public String[] getTags() {
        return null;
    }
    
    /** @return true */
    @Override
    public boolean supportsCustomEditor () {
        return XAMUtils.isWritable(mElementOrTypeProvider.getModel());
    }
    
    /** @return editor component */
    @Override
    public Component getCustomEditor () {
        WSDLModel model = mElementOrTypeProvider.getModel();
        ModelSource modelSource = model.getModelSource();
        FileObject wsdlFile = (FileObject) modelSource.getLookup().lookup(FileObject.class);
        if(wsdlFile != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFile);
            if(project != null) {
                
                Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
                Map<String, String> map = ((AbstractDocumentComponent)model.getDefinitions()).getPrefixes();
                for (String prefix : map.keySet()) {
                    namespaceToPrefixMap.put(map.get(prefix), prefix);
                }
                ElementOrType eot = mElementOrTypeProvider.getElementOrType();
                SchemaComponent comp = eot.getElement();
                if (comp == null) {
                    comp = eot.getType();
                }
                
                final ElementOrTypeChooserPanel panel = new ElementOrTypeChooserPanel(project, namespaceToPrefixMap, model, comp);
                
                panel.setEnvForPropertyEditor(mEnv);
                mEnv.setState(PropertyEnv.STATE_INVALID);
                final PropertyChangeListener pcl = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(evt.getSource()== panel && evt.getPropertyName().
                                equals(ElementOrTypeChooserPanel.PROP_ACTION_APPLY)) {
                            Boolean b = (Boolean) evt.getNewValue();
                            mEnv.setState(b.booleanValue() ? PropertyEnv.STATE_VALID : PropertyEnv.STATE_INVALID);
                        }
                    }
                };
                panel.addPropertyChangeListener(ElementOrTypeChooserPanel.PROP_ACTION_APPLY, pcl);
                panel.addPropertyChangeListener(PROP_NAME, this);
                return panel;
            }
        }
        return null;
    }
    /** handles property change
     *  
     * @param evt propertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent evt) {
        Object comp = evt.getNewValue();
        if (comp instanceof GlobalType) {
            setValue(new ElementOrType((GlobalType) comp, mElementOrTypeProvider.getModel()));
        } else if (comp instanceof GlobalElement){
            setValue(new ElementOrType((GlobalElement) comp, mElementOrTypeProvider.getModel()));
        }
    }
}


