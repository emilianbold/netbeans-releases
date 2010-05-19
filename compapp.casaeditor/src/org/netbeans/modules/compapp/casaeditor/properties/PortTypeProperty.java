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

import org.netbeans.modules.compapp.casaeditor.properties.spi.BaseCasaProperty;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.impl.JBIAttributes;
import org.netbeans.modules.compapp.casaeditor.nodes.WSDLEndpointNode;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 * @author jqian
 */
public class PortTypeProperty extends BaseCasaProperty {

    public PortTypeProperty(WSDLEndpointNode node) {
        super(
                node,
                (CasaComponent) node.getData(),
                JBIAttributes.INTERFACE_NAME.getName(),
                String.class,
                "portTypeDefinition", // NOI18N
                NbBundle.getMessage(PortTypeProperty.class, "PROP_PortTypeDefinition"), // NOI18N
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
            if (pt != null) {
                CasaEndpointRef endPointRef = getEndPointRef(casaPort);
                if(endPointRef != null) {
                    if(!CasaWrapperModel.isDummyPortType(pt)) {
                        retValue = endPointRef.getInterfaceQName();
                     }
                } else {
                    retValue = pt.getName();
                }
            }
        } 
        return retValue;
    }

    public void setValue(Object val) throws IllegalAccessException, 
            IllegalArgumentException, InvocationTargetException {

        CasaComponent component = getComponent();
        if (component instanceof CasaPort) {
            CasaEndpointRef endPointRef = getEndPointRef((CasaPort) component); 
            if (endPointRef != null) {
                if (val != null && val instanceof PortType) {
                    PortType pt = (PortType) val;
                    QName qName = new QName(pt.getModel().getDefinitions().getTargetNamespace(), pt.getName());
                    getModel().setEndpointInterfaceQName(endPointRef, qName);
                } else {
                    getModel().setEndpointInterfaceQName(endPointRef, null);
                }

                // #166809: Refresh property sheet for possible property set changes.
                ((WSDLEndpointNode)getNode()).refreshPropertySheet(); 
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

    @Override
    public PropertyEditor getPropertyEditor() {
        PortType pt = null;
        CasaComponent component = getComponent();
        if (component instanceof CasaPort) {
            pt = getModel().getCasaPortType((CasaPort) component);
        }
        return new PortTypeEditor(
                getModel(),
                pt,
                NbBundle.getMessage(getClass(), "PROP_PortTypeDefinition"), // NOI18N
                canWrite());
    }
}
