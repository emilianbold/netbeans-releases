/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.compapp.casaeditor.properties.spi;

import org.netbeans.modules.compapp.casaeditor.properties.extension.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.properties.spi.ExtensionProperty;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extension property of tabular data.
 * 
 * @author jqian
 */
public abstract class TabularDataExtensionProperty 
        extends ExtensionProperty<TabularData> {

    public TabularDataExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                TabularData.class, 
                propertyName, displayName, description);
    }
    
    protected QName getElementQName() {
        return new QName(firstEE.getQName().getNamespaceURI(), getName());
    }

    @Override
    public PropertyEditor getPropertyEditor() {

        PropertyEditor editor = new TabularDataEditor();
        try {
            TabularData value = getValue();
            editor.setValue(value);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return editor;
    }
    
    @Override
    public boolean supportsDefaultValue () {
        return false;
    }
    
    @Override
    public TabularData getValue()
            throws IllegalAccessException, InvocationTargetException {
        
        CasaComponent component = getComponent();
        
        TabularType tabularType = getTabularType();
        CompositeType rowType  = tabularType.getRowType();
        TabularData tabularData = new TabularDataSupport(tabularType);
        
        Map<String, String> map = new HashMap<String, String>();
        
        List<CasaComponent> children = getChildren(component, getElementQName());
        
        for (CasaComponent child : children) {
            for (Object k : rowType.keySet()) {
                String key = (String) k;
                map.put(key, child.getAnyAttribute(new QName(key)));
            }
            
            try {
                CompositeData rowData = new CompositeDataSupport(rowType, map);
                tabularData.put(rowData);
            } catch (OpenDataException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return tabularData;
    }

    @Override
    public void setValue(TabularData value)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        
//        CasaExtensibilityElement lastEE = (CasaExtensibilityElement) getComponent();
        if (firstEE.getParent() == null) {
            // The extensibility element does not exist in the CASA model yet.

//            // 1. Set the attribute value out of a transaction context.
//            lastEE.setAttribute(getName(), value.toString());

            // 2. Add the first extensibility element with the new attribute  
            // value into the CASA model.
            getModel().addExtensibilityElement(extensionPointComponent, firstEE);
        } else {
//            getModel().setExtensibilityElementAttribute(lastEE, getName(), value.toString());
        }
        
        CasaComponent component = getComponent();

        CasaWrapperModel model = getModel();
        CasaComponentFactory factory = model.getFactory();
        Document document = getModel().getRootComponent().getPeer().getOwnerDocument();
        
        CompositeType rowType = value.getTabularType().getRowType();
        
        model.startTransaction();
        try {
            QName elementQName = getElementQName();
            
            // remove existing ones
            List<CasaComponent> children = getChildren(component, elementQName);
            for (CasaComponent child : children) {
                component.removeExtensibilityElement((CasaExtensibilityElement)child);
            }
            
            // add new ones
            for (Object rowData : value.values()) {
                Element newElement = document.createElementNS(
                        elementQName.getNamespaceURI(), elementQName.getLocalPart());
                CasaComponent newComponent = factory.create(newElement, component);
                for (Object k : rowType.keySet()) {
                    String key = (String) k;
                    newComponent.setAnyAttribute(
                            new QName(key), (String) ((CompositeData)rowData).get(key));
                }
                component.addExtensibilityElement((CasaExtensibilityElement) newComponent);
            }
            
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
    protected abstract TabularDataDescriptor getTabularDataDescriptor();
    
    
    protected TabularType getTabularType() {
        TabularDataDescriptor descriptor = getTabularDataDescriptor();
        
        int columns = descriptor.getChildNames().size();
        String[] itemNames = new String[columns];
        String[] itemDescriptions = new String[columns];
        OpenType[] itemTypes = new OpenType[columns];

        int i = 0;
        for (String key : descriptor.getChildNames()) {
            itemNames[i] = key;
            itemDescriptions[i] = descriptor.getChild(key).getDescription();
//                QName typeQName = descriptor.getChild(key).getTypeQName();
//                if (typeQName.equals(ConfigurationDescriptor.XSD_STRING)) {
            itemTypes[i] = SimpleType.STRING;
//                } 
            i++;
        }

        String[] indexNames = new String[1]; // TMP: assume the first column in the index column
        indexNames[0] = itemNames[0];

        TabularType tabularType = null;
        try {
            CompositeType rowType = rowType = 
                    new CompositeType("row type", "row description",
                    itemNames, itemDescriptions, itemTypes);
            tabularType = new TabularType(descriptor.getName(),
                    descriptor.getDescription(), rowType, indexNames);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tabularType;
    }
    
    private List<CasaComponent> getChildren(CasaComponent component, QName qName) {
        List<CasaComponent> ret = new ArrayList<CasaComponent>();
        for (CasaComponent child : component.getChildren()) {
            Element childPeer = child.getPeer();
            if (childPeer.getLocalName().equals(qName.getLocalPart())) {
                    //&& childPeer.getNamespaceURI().equals(qName.getNamespaceURI())) { // FIXME
                ret.add(child);
            }
        }
        return ret;
    }

    class TabularDataEditor extends PropertyEditorSupport
            implements ExPropertyEditor {

        protected SimpleTabularDataCustomEditor customEditor;

        @Override
        public String getAsText() {
            TabularData tabularData = (TabularData) getValue();
            Set keySet = tabularData.keySet();
            return keySet.toString();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {                        
            try {
                TabularDataDescriptor descriptor = getTabularDataDescriptor();
                TabularData tabularData = (TabularData) getValue(); 
                customEditor = new SimpleTabularDataCustomEditor(
                        tabularData, "", "", descriptor, true);
                return customEditor;
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return null;
        }
        

        public void attachEnv(PropertyEnv env) {
            // Disable direct inline text editing.
            env.getFeatureDescriptor().setValue("canEditAsText", false); // NOI18N

            // Add validation. 
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(new VetoableChangeListener() {

                public void vetoableChange(PropertyChangeEvent ev)
                        throws PropertyVetoException {
                    if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())) {
//                        customEditor.validateValue();
                    }
                }
            });
        }
    }
}
