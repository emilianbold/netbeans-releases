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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.properties;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 * @author jqian
 */
public class PortTypeEditor extends PropertyEditorSupport {
//                   implements ExPropertyEditor, InplaceEditor.Factory {

    private final static String EMPTY = Constants.EMPTY_STRING;

    private String mPropertyName;
    private PortType mPortType;
    private List<PortType> mAllPortTypes;
    private boolean mCanWrite;
    
    public enum Option { None, Declared, Other };
    
    
    public PortTypeEditor(
            CasaWrapperModel model, 
            PortType initialPortType, 
            String propertyName,
            boolean canWrite) {
        mPropertyName = propertyName;
        mPortType = initialPortType;
        mAllPortTypes = model.getPortTypes();
        mCanWrite = canWrite;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public String getAsText() {
        Object value = super.getValue();
        return value == null ? EMPTY : super.getAsText();
    }

    @Override
    public void setAsText(String s) {
        if (EMPTY.equals(s) && getValue() == null) // NOI18N
            return;
    }

    @Override
    public Component getCustomEditor() {
        final PortTypeEditorPanel panel = new PortTypeEditorPanel(mAllPortTypes, mPortType, mCanWrite);
        
        final DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(PortTypeEditorPanel.class, "LBL_INTERFACE_NAME_Editor"), // NOI18N
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    try {
                        setValue(panel.getValue());
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().annotate(
                                iae, 
                                ErrorManager.USER,
                                iae.getMessage(), 
                                iae.getLocalizedMessage(),
                                null, 
                                new java.util.Date());
                        throw iae;
                    }
                }
            }
        }
        );

        if (!mCanWrite) {
            descriptor.setValid(false);
        }
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        //dlg.setPreferredSize(new Dimension(500, 250));
        return dlg;
    }

    
    /*
     
    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }
     
    private InplaceEditor ed = null;

    public InplaceEditor getInplaceEditor() {
        if (ed == null) {
            ed = new Inplace(mAllPortTypes, mPortType);
        }
        return ed;
    }
    private static class Inplace implements InplaceEditor {
        private final JComboBox mPortTypesComboBox = new JComboBox();
        private PropertyEditor editor = null;
        List<PortType> mPortTypes;
        PortType mPortType;
        
        Map<PortType, QName> mapPTtoQName = new HashMap<PortType, QName>();

        private Inplace(List<PortType> portTypes, PortType portType) {
            mPortTypes = portTypes;
            mPortType = portType;

            QName qName;
            for(PortType pt : portTypes) {
                if(CasaWrapperModel.isDummyPortType(pt)) {
                    qName = new QName(Constants.EMPTY_STRING,Constants.EMPTY_STRING); 
                } else {
                    qName = new QName(pt.getModel().getDefinitions().getTargetNamespace(), pt.getName());
                }
                mPortTypesComboBox.addItem(qName);
                mapPTtoQName.put(pt, qName);
            }
            if(portType != null) {
                 mPortTypesComboBox.setSelectedItem(mapPTtoQName.get(portType));
            }
            initAccessibility();
        }

        private void initAccessibility() {
            ResourceBundle bundle = NbBundle.getBundle(PortTypeEditor.class);
            AccessibleContext context = mPortTypesComboBox.getAccessibleContext();
            context.setAccessibleName(bundle.getString("ACSN_qNameSelector"));
            context.setAccessibleDescription(bundle.getString("ACSD_qNameSelector")); 
        }
        public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        public JComponent getComponent() {
            return mPortTypesComboBox;
        }

        public void clear() {
            //avoid memory leaks:
            editor = null;
            model = null;
            mapPTtoQName = null;
        }

        public Object getValue() {
            return mPortTypes.get(mPortTypesComboBox.getSelectedIndex());
        }

        public void setValue(Object object) {
            if(object != null) {
                mPortTypesComboBox.setSelectedItem(mapPTtoQName.get(object));
            } else {
                mPortTypesComboBox.setSelectedIndex(0);
            }
        }

        public boolean supportsTextEntry() {
            return false;
        }

        public void reset() {
            
        }

        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        public PropertyModel getPropertyModel() {
            return model;
        }

        private PropertyModel model;
        public void setPropertyModel(PropertyModel propertyModel) {
            this.model = propertyModel;
        }

        public boolean isKnownComponent(Component component) {
            return component == mPortTypesComboBox || mPortTypesComboBox.isAncestorOf(component);
        }

        public void addActionListener(ActionListener actionListener) {
           //do nothing - not needed for this component
        }

        public void removeActionListener(ActionListener actionListener) {
           //do nothing - not needed for this component
        }
    }
    */
}
