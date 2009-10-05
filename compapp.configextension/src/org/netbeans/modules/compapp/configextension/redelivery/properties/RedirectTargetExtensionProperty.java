/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.compapp.configextension.redelivery.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.properties.spi.ExtensionProperty;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Exceptions;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;



/**
 * Extension property of error endpoint and error endpoint operation.
 * 
 * @author jqian
 */
public class RedirectTargetExtensionProperty 
        extends ExtensionProperty<EndpointOperation> {

    private static final String ENDPOINT_NAME = "endpoint-name";
    private static final String SERVICE_NAME = "service-name";
    private static final String OPERATION_NAME = "operation";
    private static final QName ENDPOINT_NAME_QNAME = new QName(ENDPOINT_NAME);
    private static final QName SERVICE_NAME_QNAME = new QName(SERVICE_NAME);
    private static final QName OPERATION_QNAME = new QName(OPERATION_NAME);

    public RedirectTargetExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                EndpointOperation.class, 
                propertyName, displayName, description);
    }

    @Override
    public PropertyEditor getPropertyEditor() {

        PropertyEditor endpointEditor = new ErrorEndpointAndOperationEditor();
        try {
            EndpointOperation value = getValue();
            endpointEditor.setValue(value);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return endpointEditor;
    }
    
    @Override
    public boolean supportsDefaultValue () {
        return false;
    }

    @Override
    public EndpointOperation getValue()
            throws IllegalAccessException, InvocationTargetException {
        
        CasaComponent component = getComponent();
        
        String endpointName = component.getAnyAttribute(ENDPOINT_NAME_QNAME);
        
        Element element = component.getPeer();
        QName serviceQName = XmlUtil.getAttributeNSName(element, SERVICE_NAME);
        
        String operationName = component.getAnyAttribute(OPERATION_QNAME);

        Endpoint endpoint = null;
        if (serviceQName != null && endpointName != null) {
            endpoint = new Endpoint(serviceQName, endpointName);
        }

        return new EndpointOperation(endpoint, operationName);
    }

    @Override
    public void setValue(EndpointOperation value)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        Endpoint endpoint = value.getEndpoint(); 
        String operationName = value.getOperationName();

        String endpointName = endpoint.getEndpointName();
        String prefixedServiceName = endpoint.getPrefixedServiceName();

        CasaComponent component = getComponent();

        CasaWrapperModel model = getModel();
        model.startTransaction();
        try {
            component.setAnyAttribute(ENDPOINT_NAME_QNAME, endpointName);
            component.setAnyAttribute(SERVICE_NAME_QNAME, prefixedServiceName);
            component.setAnyAttribute(OPERATION_QNAME, operationName);
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }

    class ErrorEndpointAndOperationEditor extends PropertyEditorSupport
            implements ExPropertyEditor {

        protected RedirectTargetCustomEditor customEditor;

        @Override
        public String getAsText() {
            EndpointOperation value = (EndpointOperation) getValue();
            Endpoint endpoint = value.getEndpoint();
            String operationName = value.getOperationName();
            if (endpoint == null) {
                return ""; // NOI18N
            } else {
                return endpoint.toString() + " [" + operationName +"]"; // NOI18N
            }
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            
            CasaWrapperModel model = getModel();
            customEditor = new RedirectTargetCustomEditor(model);
            customEditor.setValue((EndpointOperation) getValue());
            return customEditor;
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
                        customEditor.validateValue();
                    }
                }
            });
        }
    }
}

class EndpointOperation {

    private Endpoint endpoint;
    private String operationName;
    
    EndpointOperation(Endpoint endpoint, String operationName) {
        this.endpoint = endpoint;
        this.operationName = operationName;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public String getOperationName() {
        return operationName;
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


