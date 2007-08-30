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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
//import org.netbeans.modules.xml.wsdl.model.impl.PortTypeImpl;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public class PortTypeProperty extends BaseCasaProperty {

    public PortTypeProperty(CasaNode node) {
                super( // <T>?
                node, 
                (CasaComponent) node.getData(), 
                JBIAttributes.INTERFACE_NAME.getName(), 
                String.class, 
                "portTypeDefinition", // NOI18N
                NbBundle.getMessage(PortTypeProperty.class, "PROP_PortTypeDefinition"),  // NOI18N
                NbBundle.getMessage(PortTypeProperty.class, "PROP_PortTypeDefinition")); // NOI18N
    }

    @Override
    public boolean supportsDefaultValue () {
        return false;
    }


    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        Object retValue = Constants.EMPTY_STRING;
        CasaComponent component = getComponent();
        if(component instanceof CasaPort) {
            CasaPort casaPort = (CasaPort) component;
            PortType pt = getModel().getCasaPortType(casaPort);
            CasaEndpointRef endPointRef = getEndPointRef(casaPort);
            if(endPointRef != null) {
                if(!CasaWrapperModel.isDummyPortType(pt)) {
                    retValue = endPointRef.getInterfaceQName();
                 }
            } else {
                retValue = pt.getName();
            }
        } 
        return retValue;
    }

    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        CasaComponent component = getComponent();
        if (component instanceof CasaPort) {
            CasaEndpointRef endPointRef = getEndPointRef((CasaPort) component); 
            if(endPointRef != null) {
                if(val != null && val instanceof PortType) {
                    PortType pt = (PortType) val;
                    QName qName = new QName(pt.getModel().getDefinitions().getTargetNamespace(), pt.getName());
                    getModel().setEndpointInterfaceQName(endPointRef, qName);
                } else {
                    getModel().setEndpointInterfaceQName(endPointRef, null);
                }
            }
        } 
    }

    private CasaEndpointRef getEndPointRef(CasaPort casaPort) {
        CasaEndpointRef endPointRef = casaPort.getConsumes();
        if (endPointRef == null) {
            endPointRef = casaPort.getProvides();
        }
        return endPointRef;
    }
    public PropertyEditor getPropertyEditor() {
        PortType pt = null;
        CasaComponent component = getComponent();
        if (component instanceof CasaPort) {
            pt = getModel().getCasaPortType((CasaPort) component);
        }
        return new PortTypeEditor((CasaWrapperModel)getComponent().getModel(),
                                   pt,
                                   NbBundle.getMessage(getClass(), "PROP_PortTypeDefinition"),  // NOI18N
                                   canWrite()
                                  );
    }
}
