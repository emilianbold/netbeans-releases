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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.projects.jbi.api.Endpoint;
import org.openide.util.Exceptions;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <code>Node.Property</code> for CASA configuration extension.
 * 
 * @author jqian
 */
class ExtensionProperty<T> extends BaseCasaProperty<T> {

    /**
     * A CASA extension point element, that is, a non-extensibility element 
     * that is the parent of extensibility elements, 
     * e.x., casa:connection
     */
    protected CasaComponent extensionPointComponent;
    /**
     * The top-level extensiblity elements under a CASA extension point element,
     * e.x., redelivery:redelivery
     */
    protected CasaExtensibilityElement firstEE;

    @SuppressWarnings("unchecked")
    ExtensionProperty(
             CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            Class valueType,
            String propertyName,
            String displayName,
            String description) {
        super(node, lastEE, propertyType, valueType,
                propertyName, displayName, description);

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

/**
 * Extension poperty of String type.
 *
class StringExtensionProperty extends ExtensionProperty<String> {

StringExtensionProperty(CasaNode node,
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
}*/
/**
 * Extension poperty of Integer type (empty value allowed).
 */
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
        // compared to the built-in IntEditor, this one allows empty value
        return new IntegerEditor();
    }
}

/**
 * Extension poperty of QName type.
 */
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
                String attrName = ((Attr) n).getName();
                if (attrName != null) {
                    if (attrName.trim().equals("xmlns:" + prefix)) {
                        return ((Attr) n).getValue();
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
                String attrName = ((Attr) n).getName();
                if (attrName != null) {
                    if (attrName.startsWith("xmlns:")) {
                        if (((Attr) n).getValue().equals(namespaceURI)) {
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

/**
 * Extension poperty of enumerated strings for <code>JbiChoiceExtensionElement</code>.
 */
class ChoiceExtensionProperty extends ExtensionProperty<String> {

    private List<String> choices;
    // a map of possible child extensibility elements keyed by the element names
    private Map<String, CasaExtensibilityElement> choiceMap;
    private CasaNode node;

    public ChoiceExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description,
            Map<String, CasaExtensibilityElement> choiceMap) {

        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                String.class, propertyName, displayName, description);

        this.node = node;
        this.choiceMap = choiceMap;

        choices = new ArrayList<String>();
        choices.addAll(choiceMap.keySet());
        Collections.sort(choices);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new ComboBoxEditor(choices.toArray(new String[]{}));
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getValue()
            throws IllegalAccessException, InvocationTargetException {
        CasaExtensibilityElement casaEE =
                (CasaExtensibilityElement) getComponent(); // e.x., redelivery:on-failure        
        List<CasaExtensibilityElement> children =
                casaEE.getChildren(CasaExtensibilityElement.class);
        if (children != null && children.size() == 1) {
            return children.get(0).getQName().getLocalPart();
        }
        return "";
    }

    @Override
    public void setValue(String value)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        CasaExtensibilityElement lastEE =
                (CasaExtensibilityElement) getComponent(); // e.x., redelivery:on-failure

        if (firstEE.getParent() == null) { // e.x., firstEE: redelivery:redelivery
            // Purge the non-choice elements from the pre-built 
            // extensibility element tree.
            for (CasaExtensibilityElement ee : lastEE.getExtensibilityElements()) {
                lastEE.removeExtensibilityElement(ee);
            }

            // Add the choice element to the extensibility element tree.
            CasaExtensibilityElement ee = choiceMap.get(value.toString());
            assert ee != null : "Failed to find " + value + " from " + choiceMap.keySet();
            lastEE.addExtensibilityElement(
                    (CasaExtensibilityElement) ee.copy(lastEE));

            // The extensibility element does not exist in the CASA model yet.
            getModel().addExtensibilityElement(extensionPointComponent, firstEE);

        } else {
            // Purge the non-choice elements from the pre-built 
            // extensibility element tree.
            for (CasaExtensibilityElement ee : lastEE.getExtensibilityElements()) {
                getModel().removeExtensibilityElement(lastEE, ee);
            }

            // Add the choice element to the extensibility element tree.
            CasaExtensibilityElement ee = choiceMap.get(value.toString());
            assert ee != null : "Failed to find " + value + " from " + choiceMap.keySet();
            getModel().addExtensibilityElement(lastEE,
                    (CasaExtensibilityElement) ee.copy(lastEE));
        }

        // rebuild property sheet
        node.refresh();
    }
}

/**
 * Extension poperty of Endpoint type..
 */
class EndpointExtensionProperty extends ExtensionProperty<Endpoint> {

    private static final String ENDPOINT_NAME = "endpoint-name";
    private static final String SERVICE_NAME = "service-name";
    private static final QName ENDPOINT_NAME_QNAME = new QName(ENDPOINT_NAME);
    private static final QName SERVICE_NAME_QNAME = new QName(SERVICE_NAME);

    EndpointExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                String.class, //?
                propertyName, displayName, description);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        PropertyEditor endpointEditor = new EndpointEditor();
        try {
            endpointEditor.setValue(getValue());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return endpointEditor;
    }

    @Override
    public Endpoint getValue()
            throws IllegalAccessException, InvocationTargetException {
        CasaComponent component = getComponent();
        Element element = component.getPeer();
        String endpointName = component.getAnyAttribute(ENDPOINT_NAME_QNAME);
        QName serviceQName = XmlUtil.getAttributeNSName(element, SERVICE_NAME);

        return new Endpoint(serviceQName, endpointName);
    }

    @Override
    public void setValue(Endpoint endpoint)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        if (endpoint == null) {
            return;
        }

        String endpointName = endpoint.getEndpointName();
        String prefixedServiceName = endpoint.getPrefixedServiceName();

        CasaComponent component = getComponent();

        CasaWrapperModel model = getModel();
        model.startTransaction();
        try {
            component.setAnyAttribute(ENDPOINT_NAME_QNAME, endpointName);
            component.setAnyAttribute(SERVICE_NAME_QNAME, prefixedServiceName);
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }

    class EndpointEditor extends ComboBoxEditor<Endpoint> {

        EndpointEditor() {
            List<Endpoint> values = new ArrayList<Endpoint>();

            CasaWrapperModel model = getModel();
            List<CasaEndpoint> casaEndpoints =
                    model.getRootComponent().getEndpoints().getEndpoints();

            for (CasaEndpoint casaEndpoint : casaEndpoints) {
                Endpoint endpoint = new Endpoint(
                        casaEndpoint.getServiceQName(),
                        casaEndpoint.getEndpointName());
                values.add(endpoint);
            }

            setValues(values.toArray(new Endpoint[0]));
        }

        /**
         * @param value     string representation of an endpoint in the form of
         *                  {namespaceURI}serviceName:endpointName
         */
        @Override
        public void setAsText(String value) {
            Endpoint endpoint = Endpoint.valueOf(value);

            Endpoint[] endpoints = getValues();
            for (Endpoint ep : endpoints) {
                if (ep.equals(endpoint)) {
                    setValue(ep); // endpoint doesn't have prefix info
                    break;
                }
            }
        }
    }
}

class XmlUtil {

    public static QName getAttributeNSName(Element e, String attrName) {
        String attrValue = e.getAttribute(attrName);
        return getNSName(e, attrValue);
    }

    private static QName getNSName(Element e, String qname) {
        if (qname == null) {
            return null;
        }
        int i = qname.indexOf(':');
        if (i > 0) {
            String name = qname.substring(i + 1);
            String prefix = qname.substring(0, i);
            return new QName(getNamespaceURI(e, prefix), name);
        } else {
            return new QName(qname);
        }
    }

    public static String getNamespaceURI(Element el, String prefix) {
        if ((prefix == null) || (prefix.length() < 1)) {
            return "";
        }
        prefix = prefix.trim();
        try {
            NamedNodeMap map = el.getOwnerDocument().getDocumentElement().getAttributes();
            for (int j = 0; j < map.getLength(); j++) {
                Node n = map.item(j);
                String attrName = ((Attr) n).getName();
                if (attrName != null) {
                    if (attrName.trim().equals("xmlns:" + prefix)) {
                        return ((Attr) n).getValue();
                    }
                }
            }
        } catch (Exception e) {
        }

        return "";
    }
}

