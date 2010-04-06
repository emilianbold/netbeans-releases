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

/*
 * Created on May 18, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.wsdleditorapi.generator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.util.NbBundle;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NameGenerator {
    
    private static NameGenerator mInstance;
    
    private static final String MESSAGE_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_MESSAGE_PREFIX") ;
    
    private static final String MESSAGE_PART_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_PART_PREFIX") ;
    
    private static final String PORTTYPE_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_PORTTYPE_PREFIX") ;
    
    private static final String OPERATION_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_OPERATION_PREFIX") ;
    
    private static final String OPERATION_INPUT_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_INPUT") ;
    
    private static final String OPERATION_OUTPUT_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_OUTPUT") ;
    
    private static final String OPERATION_FAULT_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_FAULT_PREFIX") ;
    
    private static final String BINDING_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_BINDING_PREFIX") ;
    
    private static final String BINDING_OPERATION_FAULT_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_BINDING_OPERATION_FAULT_PREFIX") ;
    
    private static final String SERVICE_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_SERVICE_PREFIX") ;
    
    private static final String PORT_NAME_PREFIX = NbBundle.getMessage(NameGenerator.class, "NameGenerator_PORT_PREFIX") ;
    
    private static int counterStart = 1;
    
    private NameGenerator() {
        //singleton constructor
    }
    
    public static NameGenerator getInstance() {
        if(mInstance == null) {
            mInstance = new NameGenerator();
        }
    
        return mInstance;
    }
    
    public String generateUniqueMessageName(WSDLModel document) {
        int counter = counterStart;
        String messageName = MESSAGE_NAME_PREFIX + counter;
        while(isMessageExists(messageName, document)) {
            counter++;
            messageName = MESSAGE_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    public String generateUniqueInputMessageName(String operationName, WSDLModel document) {
        int counter = counterStart;
        String messageNamePrefix = operationName + "Request";
        String messageName = messageNamePrefix;
        while(isMessageExists(messageName, document)) {
            messageName = messageNamePrefix + counter;
            counter++;
        }
        
        return messageName;
    }
    
    public String generateUniqueOutputMessageName(String operationName, WSDLModel document) {
        int counter = counterStart;
        String messageNamePrefix = operationName + "Response";
        String messageName = messageNamePrefix;
        while(isMessageExists(messageName, document)) {
            messageName = messageNamePrefix + counter;
            counter++;
        }
        
        return messageName;
    }
    
    public String generateUniqueFaultMessageName(String operationName, WSDLModel document) {
        int counter = counterStart;
        String messageNamePrefix = operationName + "Fault";
        String messageName = messageNamePrefix;
        while(isMessageExists(messageName, document)) {
            messageName = messageNamePrefix + counter;
            counter++;
        }
        
        return messageName;
    }
    
    public boolean isMessageExists(String name, WSDLModel document) {
        boolean exists = false;
        Collection messages = document.getDefinitions().getMessages();
        
        Iterator it = messages.iterator();
        while(it.hasNext()) {
            Message message = (Message) it.next();
            if(name.equals(message.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    public String generateUniqueMessagePartName(Message message) {
        int counter = counterStart;
        String messagePartName = MESSAGE_PART_NAME_PREFIX + counter;
        
        while(isMessagePartExists(messagePartName, message)) {
            counter++;
            messagePartName = MESSAGE_PART_NAME_PREFIX + counter;
        }
        
        return messagePartName;
    }
    
    public boolean isMessagePartExists(String name, Message message) {
        boolean exists = false;
        Collection parts = message.getParts();
        
        Iterator it = parts.iterator();
        while(it.hasNext()) {
            Part part = (Part) it.next();
            if(name.equals(part.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    public String generateUniquePortTypeName(WSDLModel document) {
        int counter = counterStart;
        String messageName = PORTTYPE_NAME_PREFIX + counter;
        
        while(isPortTypeExists(messageName, document)) {
            counter++;
            messageName = PORTTYPE_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    public String generateUniquePortTypeName(WSDLModel document, String portTypePrefix) {
        int counter = counterStart;
        String portTypeName = portTypePrefix;
        
        while(isPortTypeExists(portTypeName, document)) {
            portTypeName = portTypePrefix + counter;
            counter++;
        }
        
        return portTypeName;
    }
    
    public boolean isPortTypeExists(String name, WSDLModel document) {
        boolean exists = false;
        Collection portTypes = document.getDefinitions().getPortTypes();
        
        Iterator it = portTypes.iterator();
        while(it.hasNext()) {
            PortType portType = (PortType) it.next();
            if(name.equals(portType.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    
    public String generateUniqueOperationName(String operationNamePrefix,
            PortType portType) {
        int counter = counterStart;
        String operationName = operationNamePrefix;
        
        while(isOperationExists(operationName, portType)) {
            operationName = operationNamePrefix + counter;
            counter++;
        }
        
        return operationName;
    }
    
    public String generateUniqueOperationName(PortType portType) {
        int counter = counterStart;
        String messageName = OPERATION_NAME_PREFIX + counter;
        while(isOperationExists(messageName, portType)) {
            counter++;
            messageName = OPERATION_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    public boolean isOperationExists(String name, PortType portType) {
        boolean exists = false;
        Collection operations = portType.getOperations();
        
        Iterator it = operations.iterator();
        while(it.hasNext()) {
            Operation operation = (Operation) it.next();
            if(name.equals(operation.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    public String generateUniqueOperationInputName(Operation operation) {
        Set inputNames = getAllInputs(operation);
        int counter = counterStart;
        String operationInputName = OPERATION_INPUT_NAME_PREFIX + counter;
        while(inputNames.contains(operationInputName)) {
            counter++;
            operationInputName = OPERATION_INPUT_NAME_PREFIX + counter;
        }
        return operationInputName;
    }
    
    public Set<String> getAllInputs(Operation operation) {
        Set<String> hashSet = new HashSet<String>();
        PortType portType = (PortType) operation.getParent();
        Collection operations = portType.getOperations();
        if (operations != null) {
            Iterator operIter = operations.iterator();
            while (operIter.hasNext()) {
                Operation oper = (Operation) operIter.next();
                if (oper.getInput() != null) {
                    hashSet.add(oper.getInput().getName());
                }
            }
        }
        return hashSet;
    }
    
    public boolean isOperationInputExists(Operation operation) {
        boolean exists = false;
        
        
        Input operationInput = operation.getInput();
        
        if(operationInput != null) {
            exists = true;
        }
        return exists;
    }
    
    public String generateUniqueOperationOutputName(Operation operation) {
        Set outputNames = getAllOutputs(operation);
        int counter = counterStart;
        String operationOutputName = OPERATION_OUTPUT_NAME_PREFIX + counter;
        while(outputNames.contains(operationOutputName)) {
            counter++;
            operationOutputName = OPERATION_OUTPUT_NAME_PREFIX + counter;
        }
        return operationOutputName;
    }
    
    public Set<String> getAllOutputs(Operation operation) {
        Set<String> hashSet = new HashSet<String>();
        PortType portType = (PortType) operation.getParent();
        Collection operations = portType.getOperations();
        if (operations != null) {
            Iterator operIter = operations.iterator();
            while (operIter.hasNext()) {
                Operation oper = (Operation) operIter.next();
                if (oper.getOutput() != null) {
                    hashSet.add(oper.getOutput().getName());
                }
            }
        }
        return hashSet;
    }
    
    public boolean isOperationOutputExists(Operation operation) {
        boolean exists = false;
        Output operationOutput = operation.getOutput();
        
        if(operationOutput != null) {
            exists = true;
        }
        return exists;
    }
    
    public String generateUniqueOperationFaultName(Operation operation) {
        int counter = counterStart;
        String messageName = OPERATION_FAULT_NAME_PREFIX + counter;
        while(isOperationFaultExists(messageName, operation)) {
            counter++;
            messageName = OPERATION_FAULT_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    private boolean isOperationFaultExists(String name, Operation operation) {
        boolean exists = false;
        Collection faults = operation.getFaults();
        
        Iterator it = faults.iterator();
        while(it.hasNext()) {
            Fault fault = (Fault) it.next();
            if(name.equals(fault.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    public String generateUniqueBindingName(WSDLModel document) {
        int counter = counterStart;
        String messageName = BINDING_NAME_PREFIX + counter;
        while(isBindingExists(messageName, document)) {
            counter++;
            messageName = BINDING_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    public String generateUniqueBindingName(WSDLModel document, String appendage) {
        int counter = counterStart;
        String prefix = BINDING_NAME_PREFIX + "_" + appendage;
        String messageName =  prefix  + counter;
        while(isBindingExists(messageName, document)) {
            counter++;
            messageName = BINDING_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    public String generateUniqueBindingName(String prefix, WSDLModel document) {
        if (prefix == null) {
            return generateUniqueBindingName(document);
        }
        int counter = counterStart;
        String messageName =  prefix  + counter;
        while(isBindingExists(messageName, document)) {
            counter++;
            messageName = prefix + counter;
        }
        
        return messageName;
    }
    
    public boolean isBindingExists(String name, WSDLModel document) {
        boolean exists = false;
        Collection bindings = document.getDefinitions().getBindings();
        
        Iterator it = bindings.iterator();
        while(it.hasNext()) {
            Binding binding = (Binding) it.next();
            if(name.equals(binding.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    
    public String generateUniqueBindingOperationFaultName(BindingOperation bo) {
        int counter = counterStart;
        String faultName = BINDING_OPERATION_FAULT_NAME_PREFIX + counter;
        while(isBindingOperationFaultExists(faultName, bo)) {
            counter++;
            faultName = BINDING_OPERATION_FAULT_NAME_PREFIX + counter;
        }
        
        return faultName;
    }
    
    public boolean isBindingOperationFaultExists(String name, BindingOperation bo) {
        boolean exists = false;
        Collection faults = bo.getBindingFaults();
        
        Iterator it = faults.iterator();
        while(it.hasNext()) {
            BindingFault bindingFault = (BindingFault) it.next();
            if(name.equals(bindingFault.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    public String generateUniqueServiceName(WSDLModel document) {
        int counter = counterStart;
        String messageName = SERVICE_NAME_PREFIX + counter;
        while(isServiceExists(messageName, document)) {
            counter++;
            messageName = SERVICE_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    public boolean isServiceExists(String name, WSDLModel document) {
        boolean exists = false;
        Collection services = document.getDefinitions().getServices();
        
        Iterator it = services.iterator();
        while(it.hasNext()) {
            Service service = (Service) it.next();
            if(name.equals(service.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }
    
    public String generateUniqueServicePortName(Service service) {
        int counter = counterStart;
        String messageName = PORT_NAME_PREFIX + counter;
        while(isServicePortExists(messageName, service)) {
            counter++;
            messageName = PORT_NAME_PREFIX + counter;
        }
        
        return messageName;
    }
    
    public String generateUniqueServicePortName(String prefix, Service service) {
        int counter = counterStart;
        String messageName = prefix + counter;
        while(isServicePortExists(messageName, service)) {
            counter++;
            messageName = prefix + counter;
        }
        
        return messageName;
    }
    
    public boolean isServicePortExists(String name, Service service) {
        boolean exists = false;
        Collection ports = service.getPorts();
        
        Iterator it = ports.iterator();
        while(it.hasNext()) {
            Port port = (Port) it.next();
            if(name.equals(port.getName())) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }

    public String generateNamespacePrefix(String optionalPrefixNameString, WSDLComponent element) {
        return generateNamespacePrefix(optionalPrefixNameString, element.getModel());
    }

    /**
     * Generate a unique namespace prefix for the given model. This is
     * the same as generateNamespacePrefix(String, WSDLModel, int) with
     * a value of zero for the counter parameter.
     *
     * @param  prefix  the desired prefix for the namespace prefix;
     *                 if null, a default of "ns" will be used.
     * @param  model   model in which to find unique prefix.
     * @return  the unique namespace prefix (e.g. "ns0").
     */
    public String generateNamespacePrefix(String prefix, WSDLModel model) {
        return generateNamespacePrefix(prefix, model, 0);
    }

    /**
     * Generate a unique namespace prefix for the given model.
     *
     * @param  prefix   the desired prefix for the namespace prefix;
     *                  if null, a default of "ns" will be used.
     * @param  model    model in which to find unique prefix.
     * @param  counter  minimum number to use as suffix (results in a
     *                  prefix such as "ns" plus the value of counter).
     * @return  the unique namespace prefix (e.g. "ns0").
     */
    public String generateNamespacePrefix(String prefix, WSDLModel model,
            int counter) {
        if (prefix == null) {
            prefix = NbBundle.getMessage(NameGenerator.class,
                    "NameGenerator_DEFAULT_PREFIX");
        }
        String generated = prefix + counter;
        while (isPrefixExist(generated, model)) {
            counter++;
            generated = prefix + counter;
        }
        return generated;
    }

    /**
     * Does the prefix exist in the given model
     * @param prefix the prefix
     * @param model  the model which needs to be checked
     * @return true if prefix exists
     */
    public boolean isPrefixExist(String prefix, WSDLModel model) {
        return Utility.getNamespaceURI(prefix, model) != null ? true : false;
    }
    
    public boolean isAttributeExists(String attrQName, WSDLComponent element) {
        if(attrQName == null) {
            return false;
        }
        
        boolean result = false;
        
        
        /*XMLAttribute[]*/////.getXmlAttributes();*/
        NamedNodeMap attrs = ((AbstractDocumentComponent) element).getPeer().getAttributes();
        //Collection attrs = element.getChildren(Attribute.class);
        
        if(attrs != null) {
            for (int i = 0; i < attrs.getLength();i++) {
                Attr attr = (Attr) attrs.item(i);
                if(attrQName.equals(attr.getName())) { //TODO:SKINIgetQualifiedName())) {
                    return true;
                }
                
            }
        }
        
/*        TODO:SKINI
         Map otherAttrMap = element.getOtherAttributes();
        if(otherAttrMap != null) {
            if(otherAttrMap.get(attrQName) != null) {
                result = true;
            }
        }*/
        
        
        return result;
        
    }
    
//    public static String createNewTargetNamespace (WSDLModel model, WSDLDataObject dObj) {
//        String targetNamespaceStr = null;
//
//        StringBuffer targetNamespace = new StringBuffer("http://localhost/");
//
//        Project project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
//        if(project != null) {
//            ProjectInformation pi = ProjectUtils.getInformation(project);
//            if(pi != null) {
//                targetNamespace.append(pi.getDisplayName());
//                targetNamespace.append("/");
//            }
//        }
//
//        targetNamespace.append(dObj.getName());
//        targetNamespaceStr = targetNamespace.toString();
//
//        
//        return targetNamespaceStr;
//    }
    
    
    public static String generateUniqueValueForKeyAttribute(WSDLComponent component, String attributeName, QName qname, String prefix) {
        WSDLComponent parent = component.getParent();
        HashSet<String> set = new HashSet<String>();
        List<ExtensibilityElement> list = parent.getExtensibilityElements();
        if (list != null) {
            for (ExtensibilityElement element : list) {
                if (element.getQName().equals(qname) && element != component) {
                    if (element.getAttribute(attributeName) != null) {
                        set.add(element.getAttribute(attributeName));
                    }
                }
            }
        }
        
        int counter = counterStart;
        String messageName = prefix + counter;
        while(set.contains(messageName)) {
            counter++;
            messageName = prefix + counter;
        }
        
        return messageName;
    }
    
    public static String generateUniquePartnerLinkType(String partnerLinkTypeNamePrefix, QName partnerLinkTypeElementQName, WSDLModel document) {
        int counter = counterStart;
        String partnerLinkTypeName = null;
        String prefix = "partnerlinktype";
        if (partnerLinkTypeNamePrefix == null) {
            partnerLinkTypeName = prefix + counter;
        } else {
            partnerLinkTypeName = prefix = partnerLinkTypeNamePrefix;
        }
        while(isParterLinkTypeExists(partnerLinkTypeName , partnerLinkTypeElementQName, document)) {
            counter++;
            partnerLinkTypeName = prefix + counter;
        }
        
        return partnerLinkTypeName;
    }
    
    private static boolean isParterLinkTypeExists(String partnerLinkTypeName, QName partnerLinkTypeElementQName, WSDLModel document) {
        return findExtensibilityElementByQName(partnerLinkTypeName, partnerLinkTypeElementQName, document.getDefinitions()) != null;
    }
    
    private static WSDLComponent findExtensibilityElementByQName(String name, QName elementQName, WSDLComponent component) {
        if(name == null || elementQName == null || elementQName.getNamespaceURI() == null || elementQName.getLocalPart() == null || component == null) {
            return null;
        }
    
        List<ExtensibilityElement> elements =  component.getExtensibilityElements();
        Iterator<ExtensibilityElement> it = elements.iterator();
        
        while(it.hasNext()) {
            ExtensibilityElement ee = it.next();
            QName eeQName = ee.getQName();
            String eeNamespace = eeQName.getNamespaceURI();
            String eeLocalPart = eeQName.getLocalPart();
            
            if(elementQName.getNamespaceURI().equals(eeNamespace) && elementQName.getLocalPart().equals(eeLocalPart)) {
                String eeName = ee.getAttribute("name");
                if(eeName != null && eeName.equals(name)) {
                    return ee;
                }
            }
        }
        
        return null;
    }
}
