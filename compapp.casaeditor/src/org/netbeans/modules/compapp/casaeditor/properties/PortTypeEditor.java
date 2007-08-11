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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.properties;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import javax.xml.namespace.QName;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public class PortTypeEditor extends PropertyEditorSupport 
                   implements ExPropertyEditor, InplaceEditor.Factory {

    private final static String EMPTY = Constants.EMPTY_STRING;
    
    private String mPropertyName;
    private PortType mPortType;
    private List<PortType> mAllPortTypes;

    
    public enum Option { None, Declared, Other };
    
    
    public PortTypeEditor(
            CasaWrapperModel model, 
            PortType initialPortType, 
            String propertyName) {
        mPropertyName = propertyName;
        mPortType = initialPortType;
        mAllPortTypes = model.getPortTypes();
    }

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
}
