/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.compapp.casaeditor.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A CASA extension property.
 * 
 * @author jqian
 */
abstract class ExtensionProperty<T> extends BaseCasaProperty<T> {

    // parent component of the extensibility element
    protected CasaComponent extensionPointComponent;
    protected CasaExtensibilityElement firstEE;

    @SuppressWarnings("unchecked")
    ExtensionProperty(
             CasaNode node,
             CasaComponent extensionPointComponent,
             CasaExtensibilityElement firstEE,
             CasaExtensibilityElement lastEE,
             String propertyType,
             Class valueType,
             String property,
             String propDispName,
             String propDesc) {
        super(node, lastEE, propertyType, valueType,
                property, propDispName, propDesc);

        this.extensionPointComponent = extensionPointComponent;
        this.firstEE = firstEE;
    }

    @SuppressWarnings("unchecked")
    public T getValue()
            throws IllegalAccessException, InvocationTargetException {
        CasaExtensibilityElement casaEE = (CasaExtensibilityElement) getComponent();
        return (T) casaEE.getAttribute(getName());
    }

    public void setValue(T value)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        CasaExtensibilityElement lastEE = (CasaExtensibilityElement) getComponent();
        if (firstEE.getParent() == null) {
            // The extensibility element does not exist in the CASA model yet.

            // 1. Set the attribute value out of a transaction context.
            lastEE.setAttribute(getName(), value.toString());

            // 2. Add the first extensibility element with the new attribute  
            // value into the CASA model.
            getModel().addExtensibilityElement(extensionPointComponent, firstEE);
        } else {
            getModel().setExtensibilityElementAttribute(lastEE, getName(), value.toString());
        }
    }
}

class StringExtensionProperty extends ExtensionProperty<String> {

    StringExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                String.class, propertyName, displayName, description);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new StringEditor();
    }
}

class IntegerExtensionProperty extends ExtensionProperty<Integer> {

    IntegerExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                Integer.class, propertyName, displayName, description);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new IntegerEditor();
    }
}

class QNameExtensionProperty extends ExtensionProperty<QName> {

    QNameExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                Integer.class, propertyName, displayName, description);
    }

    @Override
    public QName getValue() {
        CasaExtensibilityElement casaEE = (CasaExtensibilityElement) getComponent();
        return getAttributeNSName(casaEE.getPeer(), getName());       
    }
    
    @Override
    public void setValue(QName qName)
            throws IllegalAccessException, IllegalArgumentException, 
            InvocationTargetException {
        
        String value = null;
        
        String existingPrefix = getNamespacePrefix(extensionPointComponent.getPeer(), 
                qName.getNamespaceURI());
        String newPrefix = null;
        if (existingPrefix != null) {
            value = existingPrefix + ":" + qName.getLocalPart();
        } else {
            newPrefix = qName.getPrefix();
            value = newPrefix + ":" + qName.getLocalPart();
        }
        
        CasaExtensibilityElement lastEE = (CasaExtensibilityElement) getComponent();
        if (firstEE.getParent() == null) {
            // The extensibility element does not exist in the CASA model yet.

            // 1. Set the attribute value out of a transaction context.
            lastEE.setAttribute(getName(), value);
            if (newPrefix != null) {
                lastEE.setAttribute("xmlns:" + newPrefix, qName.getNamespaceURI());
            }
            
            // 2. Add the first extensibility element with the new attribute  
            // value into the CASA model.
            getModel().addExtensibilityElement(extensionPointComponent, firstEE);
        } else {
            if (newPrefix != null) {
                getModel().setExtensibilityElementAttribute(lastEE, 
                        "xmlns:" + newPrefix, qName.getNamespaceURI());
            } 
                
            getModel().setExtensibilityElementAttribute(lastEE, getName(), value);
        }
    }
        
    private static QName getAttributeNSName(Element e, String attrName) {
        String attrValue = e.getAttribute(attrName);
        return getNSName(e, attrValue);
    }
    
    private static QName getNSName(Element e, String qname) {
        if (qname == null) {
            return null;
        }
        int i = qname.indexOf(':');
        if (i > 0) {
            String localPart = qname.substring(i + 1);
            String prefix = qname.substring(0, i);
            return new QName(getNamespaceURI(e, prefix), localPart);
        } else {
            return new QName(qname);
        }
    }
        
    private static String getNamespaceURI(Node node, String prefix) {
        if ((prefix == null) || (prefix.length() < 1)) {
            return "";
        }
        prefix = prefix.trim();
        try {
            NamedNodeMap map = node.getAttributes();
            for (int j = 0; j < map.getLength(); j++) {
                Node n = map.item(j);
                String attrName = ((Attr)n).getName();
                if (attrName != null) {
                    if (attrName.trim().equals("xmlns:" + prefix)) {
                        return ((Attr)n).getValue();
                    }
                }
            }
            Node parent = node.getParentNode();
            if (parent != null) {
                return getNamespaceURI(parent, prefix);
            }
        } catch (Exception e) {
        }
        
        return "";
    }
    
     private static String getNamespacePrefix(Node node, String namespaceURI) {
        if ((namespaceURI == null) || (namespaceURI.trim().length() < 1)) {
            return null;
        }
        namespaceURI = namespaceURI.trim();
        try {
            NamedNodeMap map = node.getAttributes();
            for (int j = 0; j < map.getLength(); j++) {
                Node n = map.item(j);
                String attrName = ((Attr)n).getName();
                if (attrName != null) {
                    if (attrName.startsWith("xmlns:")) {
                        if (((Attr)n).getValue().equals(namespaceURI)) {
                            return attrName.substring(6);
                        }
                    }
                }
            }
            Node parent = node.getParentNode();
            if (parent != null) {
                return getNamespacePrefix(parent, namespaceURI);
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new NamespaceEditor(this, getModel(),
                getValue(), getDisplayName());
    }
}
