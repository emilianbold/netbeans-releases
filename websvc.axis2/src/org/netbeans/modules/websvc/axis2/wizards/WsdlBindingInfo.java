/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.websvc.axis2.wizards;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author mkuchtiak
 */
public class WsdlBindingInfo {
    
    List<WsdlOperationInfo> wsdlOperations;
    String serviceName;
    String portName;
    
    private WsdlBindingInfo() {
    }
    
    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public List<WsdlOperationInfo> getWsdlOperations() {
        return wsdlOperations;
    }

    public void setWsdlOperations(List<WsdlOperationInfo> wsdlOperations) {
        this.wsdlOperations = wsdlOperations;
    }
    
    static WsdlBindingInfo getWsdlBindingInfo(WSDLModel wsdlModel, String serviceName, String portName) {
        WsdlBindingInfo bindingInfo = new WsdlBindingInfo();
        bindingInfo.setServiceName(serviceName);
        bindingInfo.setPortName(portName);
        
        List<WsdlOperationInfo> wsdlOperations = new ArrayList<WsdlOperationInfo>();
        Definitions defs = wsdlModel.getDefinitions();
        assert defs != null;
        String bindingName = getBindingName(defs, serviceName, portName);
        if (bindingName != null) {
            Collection<Binding> bindings = defs.getBindings();
            for (Binding binding:bindings) {
                if (bindingName.equals(binding.getName())) {
                    Collection<BindingOperation> operations = binding.getBindingOperations();
                    for (BindingOperation op:operations) {
                        WsdlOperationInfo wsdlOperation = new WsdlOperationInfo();
                        wsdlOperation.setOperationName(op.getName());
                        wsdlOperation.setInOnly(op.getBindingOutput() == null);
                        wsdlOperations.add(wsdlOperation);
                    }
                }
            }
        }
        bindingInfo.setWsdlOperations(wsdlOperations);
        return bindingInfo;
    }
    
    private static String getBindingName(Definitions defs, String serviceName, String portName) {
        String bindingName = null;
        Collection<Service> services = defs.getServices();
        for (Service service:services) {
            if (serviceName.equals(service.getName())) {
                Collection<Port> ports = service.getPorts();
                for (Port port:ports) {
                    if (portName.equals(port.getName())) {
                        NamedComponentReference<Binding> b = port.getBinding();
                        return b.getQName().getLocalPart();
                    }
                }
            }
        }
        return null;
    }
    
    static class WsdlOperationInfo {
        boolean inOnly;
        String operationName;

        public boolean isInOnly() {
            return inOnly;
        }

        public void setInOnly(boolean inOnly) {
            this.inOnly = inOnly;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }
        
    }
}

