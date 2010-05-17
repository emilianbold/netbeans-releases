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
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ElementOrTypePropertyEditor extends PropertyEditorSupport
        implements ExPropertyEditor {

    /** Environment passed to the ExPropertyEditor */
    private PropertyEnv mEnv;

    private ElementOrTypeProvider mElementOrTypeProvider;

    public ElementOrTypePropertyEditor(
            ElementOrTypeProvider elementOrTypeProvider) {
        this.mElementOrTypeProvider = elementOrTypeProvider;
        
    }

    @Override
    public String getAsText() {
        if (mElementOrTypeProvider != null) {
            ElementOrType eot = mElementOrTypeProvider.getElementOrType();
            if (eot != null) {
                return eot.toString();
            }
        }
        return "";
    }
    
    /**
     * This method is called by the IDE to pass the environment to the property
     * editor.
     * 
     * @param env
     *            Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        this.mEnv = env;
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // add help id
        desc
                .setValue(ExPropertyEditor.PROPERTY_HELP_ID,
                        "org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypePropertyEditor");
    }

    /** @return tags */
    @Override
    public String[] getTags() {
        return null;
    }

    /** @return true */
    @Override
    public boolean supportsCustomEditor() {
        return XAMUtils.isWritable(mElementOrTypeProvider.getModel());
    }

    /** @return editor component */
    @Override
    public Component getCustomEditor() {
        WSDLModel model = mElementOrTypeProvider.getModel();
        ModelSource modelSource = model.getModelSource();
        FileObject wsdlFile = modelSource.getLookup().lookup(FileObject.class);
        if (wsdlFile != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFile);
            if (project != null) {

                Map<String, String> namespaceToPrefixMap = new HashMap<String, String>();
                Map<String, String> map = ((AbstractDocumentComponent) model
                        .getDefinitions()).getPrefixes();
                for (String prefix : map.keySet()) {
                    namespaceToPrefixMap.put(map.get(prefix), prefix);
                }
                ElementOrType eot = mElementOrTypeProvider.getElementOrType();
                SchemaComponent comp = null;
                if (eot != null) {
                    comp = eot.getElement();
                    if (comp == null) {
                        comp = eot.getType();
                    }
                }

                final ElementOrTypeChooserPanel panel = new ElementOrTypeChooserPanel(
                        project, namespaceToPrefixMap, model, comp);

                mEnv.setState(PropertyEnv.STATE_INVALID);

                final PropertyChangeListener pcl = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getSource() == panel && evt.getPropertyName().equals(ElementOrTypeChooserPanel.PROP_ACTION_APPLY)) {
                            Boolean b = (Boolean) evt.getNewValue();
                            mEnv.setState(b.booleanValue() ? PropertyEnv.STATE_NEEDS_VALIDATION : PropertyEnv.STATE_INVALID);
                            
                        }
                    }
                };
                panel.addPropertyChangeListener(ElementOrTypeChooserPanel.PROP_ACTION_APPLY, pcl);
                mEnv.addPropertyChangeListener(new PropertyChangeListener() {
                
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ((PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID)) {
                            SchemaComponent comp = panel.getSelectedSchemaComponent();
                            if (comp instanceof GlobalType) {
                                setValue(new ElementOrType((GlobalType) comp, mElementOrTypeProvider.getModel())); 
                            } else if (comp instanceof GlobalElement) {
                                setValue(new ElementOrType((GlobalElement) comp, mElementOrTypeProvider.getModel()));
                            }
                        }
                    }
                
                });
                return panel;
            }
        }
        return null;
    }
}
