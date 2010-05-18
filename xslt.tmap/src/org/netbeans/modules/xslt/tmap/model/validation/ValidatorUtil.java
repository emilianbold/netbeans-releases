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

package org.netbeans.modules.xslt.tmap.model.validation;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xml.internal.utils.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.util.ModelUtil;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.StylesheetChild;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.spi.XslModelFactory.XslModelFactoryAccess;
import org.netbeans.modules.xslt.tmap.TMapConstants;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.OperationReference;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.api.WsdlDataHolder;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionUtil;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Alex Petrov (05.08.2008)
 */
public class ValidatorUtil {
    public static final String 
        WORD_CHARACTER_PATTERN="[a-zA-Z_0-9]+", // "[\\w]+"
        VAR_NAME_BASE_PATTERN  = WORD_CHARACTER_PATTERN, // one or more word characters: [a-zA-Z_0-9]+
        PART_NAME_BASE_PATTERN = WORD_CHARACTER_PATTERN, // one or more word characters: [a-zA-Z_0-9]+
        VAR_NAME_PATTERN_GROUP = // "(\\$[\\w]+)"
            "(\\" + TMapConstants.DOLLAR_SIGN + VAR_NAME_BASE_PATTERN + ")",
        TRANSFORM_PART_NAME_PATTERN_GROUP = // one lexeme or not at all "(\\.[\\w]+)?"
            "(\\" + TMapConstants.DOT + PART_NAME_BASE_PATTERN + ")?",
        PARAM_PART_NAME_PATTERN_GROUP = // one lexeme exactly (\\.[\\w]+)
            "(\\" + TMapConstants.DOT + PART_NAME_BASE_PATTERN + ")",
        TRANSFORM_VAR_NAME_PATTERN =  // $varName[.partName]
            VAR_NAME_PATTERN_GROUP + TRANSFORM_PART_NAME_PATTERN_GROUP,
        PARAM_VAR_NAME_PATTERN =  // $varName.partName
            VAR_NAME_PATTERN_GROUP + PARAM_PART_NAME_PATTERN_GROUP;

    private static final Pattern 
        PATTERN_TRANSFORM_VARIABLE = Pattern.compile(TRANSFORM_VAR_NAME_PATTERN),
        PATTERN_PARAM_VARIABLE = Pattern.compile(PARAM_VAR_NAME_PATTERN);
    
    private static enum INPUT_OUTPUT_KEY {INPUT, OUTPUT};

    public static boolean checkWsdlContainsOperation(String requiredOperationName, 
        WsdlDataHolder wsdlHolder) {
        if ((requiredOperationName == null) || (requiredOperationName.length() < 1) || 
            (wsdlHolder == null))
            return false;
        
        Set<String> setOperationNames = getWsdlOperationNameSet(wsdlHolder);
        if ((setOperationNames == null) || (setOperationNames.isEmpty())) return false;
        
        return (setOperationNames.contains(requiredOperationName));
    }
        
    public static boolean checkWsdlContainsPortType(String requiredPortType,
        WsdlDataHolder wsdlHolder) {
        if ((requiredPortType == null) || (requiredPortType.length() < 1) || 
            (wsdlHolder == null))
            return false;
        Collection<PortType> portTypes = 
            wsdlHolder.getWsdlModel().getDefinitions().getPortTypes();
        Iterator<PortType> iterator = portTypes.iterator();
        while (iterator.hasNext()) {
            String portTypeName = iterator.next().getName();
            if ((portTypeName != null) && (portTypeName.equals(requiredPortType))) 
                return true;   
        }
        return false;
    }
    
    public static boolean checkWsdlContainsVariablePartName(String requiredPartName,
        WsdlDataHolder wsdlHolder) {
        if ((requiredPartName == null) || (requiredPartName.length() < 1) || 
            (wsdlHolder == null))
            return false;
        
        Set<String> partNames = getWsdlPartNameSet(wsdlHolder);
        if ((partNames == null) || (partNames.isEmpty())) return false;
        
        return (partNames.contains(requiredPartName));
    }

    private static Set<String> getWsdlPartNameSet(WsdlDataHolder wsdlHolder) {
        if (wsdlHolder == null) return null;
        
        Collection<Message> messages = wsdlHolder.getWsdlModel().getDefinitions().getMessages();
        if ((messages == null) || (messages.isEmpty())) return null;
        
        Set<String> partNameSet = new HashSet<String>();
        Iterator<Message> msgIterator = messages.iterator();
        while (msgIterator.hasNext()) {
            Message message = msgIterator.next();
            Collection<Part> parts = message.getParts();
            partNameSet.addAll(getPartNameSet(parts));
        }
        return partNameSet;
    }

    public static String getWsdlTargetNamespace(WsdlDataHolder wsdlHolder) {
        if (wsdlHolder == null) return null;
        
        String targetNamespace = wsdlHolder.getWsdlModel().getDefinitions().getTargetNamespace();
        return targetNamespace;
    }
    
    public static String getOperationInputMessageName(TMapComponent tmapComponent) {
        return getOperationMessageName(tmapComponent, INPUT_OUTPUT_KEY.INPUT);
    }
    public static String getOperationOutputMessageName(TMapComponent tmapComponent) {
        return getOperationMessageName(tmapComponent, INPUT_OUTPUT_KEY.OUTPUT);
    }
    private static String getOperationMessageName(TMapComponent tmapComponent, 
        INPUT_OUTPUT_KEY operationType) {
        if (! ((tmapComponent instanceof org.netbeans.modules.xslt.tmap.model.api.Operation) || 
               (tmapComponent instanceof Invoke)))
            return null;
        
        Operation operation = ((org.netbeans.modules.xslt.tmap.model.api.OperationReference) 
            tmapComponent).getOperation().get();
        try {
            Message message = null;
            if (operationType.equals(INPUT_OUTPUT_KEY.INPUT)) {
                Input input = operation.getInput();
                message = input.getMessage().get();
            }
            if (operationType.equals(INPUT_OUTPUT_KEY.OUTPUT)) {
                Output output = operation.getOutput();
                message = output.getMessage().get();
            }
            String messageName = message.getName();
            return messageName;
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
        }
        return null;
    }
    
    public static Set<String> getOperationInputPartNameSet(OperationReference operationRef) {
        if (operationRef == null) {
            return null;
        }
        
        try {
            Reference<Operation> wsdlOperationRef = operationRef.getOperation();
            Operation wsdlOperation = wsdlOperationRef == null ? null : wsdlOperationRef.get();
            Input input = wsdlOperation == null ? null : wsdlOperation.getInput();
            if (input == null) {
                return null;
            }
            Reference<Message> messageRef = input.getMessage();
            Message message = messageRef == null ? null : messageRef.get();
            return (getPartNameSet(message == null ? null : message.getParts()));
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
        }
        return null;
    }

    public static Set<String> getOperationOutputPartNameSet(
        org.netbeans.modules.xslt.tmap.model.api.OperationReference operationRef) {
        if (operationRef == null) return null;
        try {
            Reference<Operation> wsdlOperationRef = operationRef.getOperation();
            Operation wsdlOperation = wsdlOperationRef == null ? null : wsdlOperationRef.get();
            Output output = wsdlOperation == null ? null : wsdlOperation.getOutput();
            if (output == null) {
                return null;
            }
            Reference<Message> messageRef = output.getMessage();
            Message message = messageRef == null ? null : messageRef.get();
            return (getPartNameSet(message == null ? null : message.getParts()));
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
        }
        return null;
    }

    public static Set<String> getTransformsOutputPartNameSet(List<Transform> transforms) {
        if (transforms == null) return null;
        
        Set<String> setPartNames = new HashSet<String>();
        for (Transform transform : transforms) {
            try {
                String outputVariableName = transform.getResult().getRefString();
                String usedPartName = ValidatorUtil.getVariablePartName(outputVariableName);
                if ((usedPartName != null) && (usedPartName.length() > 0)) {
                    setPartNames.add(usedPartName);
                }
            } catch(Exception e) {
                continue;
            }
        }
        return setPartNames;
    }

    private static Set<String> getPartNameSet(Collection<Part> parts) {
        if ((parts == null) || (parts.isEmpty())) return Collections.emptySet();

        Set<String> partNameSet = new HashSet<String>();
        Iterator<Part> partIterator = parts.iterator();
        while (partIterator.hasNext()) {
            String partName = partIterator.next().getName();
            if ((partName == null) || (partName.length() < 1)) continue;

            partNameSet.add(partName);
        }
        return partNameSet;
    }
    
    private static Set<String> getWsdlPortTypeSet(WsdlDataHolder wsdlHolder) {
        if (wsdlHolder == null) return null;
        
        Collection<PortType> portTypes = 
            wsdlHolder.getWsdlModel().getDefinitions().getPortTypes();
        if ((portTypes == null) || (portTypes.isEmpty())) return null;
        
        Set<String> setPortTypeNames = new HashSet<String>();
        Iterator<PortType> portTypeIterator = portTypes.iterator();
        while (portTypeIterator.hasNext()) {
            PortType portType = portTypeIterator.next();
            if (portType == null) continue;
            
            String portTypeName = portType.getName();
            if ((portTypeName == null) || (portTypeName.length() < 1)) continue;
                
            setPortTypeNames.add(portTypeName);
        }
        return setPortTypeNames;
    }
    
    private static Set<String> getWsdlOperationNameSet(WsdlDataHolder wsdlHolder) {
        if (wsdlHolder == null) return null;
        
        Collection<PortType> portTypes = 
            wsdlHolder.getWsdlModel().getDefinitions().getPortTypes();
        if ((portTypes == null) || (portTypes.isEmpty())) return null;
        
        Set<String> setOperationNames = new HashSet<String>();
        Iterator<PortType> portTypeIterator = portTypes.iterator();
        while (portTypeIterator.hasNext()) {
            PortType portType = portTypeIterator.next();
            Collection<Operation> operations = portType.getOperations();
            if ((operations == null) || (operations.isEmpty())) continue;
            
            Iterator<Operation> operationIterator = operations.iterator();
            while (operationIterator.hasNext()) {
                String operationName = operationIterator.next().getName();
                if ((operationName == null) || (operationName.length() < 1)) continue;
                
                setOperationNames.add(operationName);
            }
        }
        return setOperationNames;
    }

    public static String getQNameLocalPart(String qualifiedName) {
        if ((qualifiedName == null) || (qualifiedName.length() < 1)) return null;
        String localName = QName.getLocalPart(qualifiedName);
        return localName;
    }

    public static String getQNamePrefix(String qualifiedName) {
        if ((qualifiedName == null) || (qualifiedName.length() < 1)) return null;
        String qnamePrefix = QName.getPrefixPart(qualifiedName);
        return qnamePrefix;
    }

    public static Service getParentService(TMapComponent tmapComponent) {
        if (tmapComponent == null) return null;
        TMapComponent parentComponent = tmapComponent.getParent();
        while (parentComponent != null) {
            if (parentComponent instanceof Service) return ((Service) parentComponent);
            parentComponent = parentComponent.getParent();
        }
        return null;
    }

    public static String getPortTypeQName(TMapComponent tmapComponent) {
        Service service = (tmapComponent instanceof Service ? 
            (Service) tmapComponent : null);
        if (!((tmapComponent instanceof Service) || (tmapComponent instanceof Invoke))) {
            service = getParentService(tmapComponent);
            if (service == null) return null;
        }
        WSDLReference<PortType> refPortType = (tmapComponent instanceof Invoke ? 
            ((Invoke) tmapComponent).getPortType() :
            service.getPortType());
        if (refPortType == null) {
            return null;
        }
        String portTypeQName = refPortType.getRefString();
        return portTypeQName;
    }
    
    public static PortType getPortType(TMapComponent tmapComponent) {
        Service service = (tmapComponent instanceof Service ? 
            (Service) tmapComponent : null);
        if (!((tmapComponent instanceof Service) || (tmapComponent instanceof Invoke))) {
            service = getParentService(tmapComponent);
            if (service == null) return null;
        }
        PortType portType = (tmapComponent instanceof Invoke ? 
            ((Invoke) tmapComponent).getPortType().get() :
            service.getPortType().get());
        return portType;
    }
    
    public static String getVariablePartName(String variableName) {
        if ((variableName == null) || (variableName.length() < 1)) return null;
        
        int index = variableName.lastIndexOf(".");
        if ((index < 0) || (variableName.endsWith("."))) return null;
        
        String partName = variableName.substring(index + 1);
        return partName;
    }
 
    public static String getVariableSimpleName(String variableName) {
        if ((variableName == null) || (variableName.length() < 1)) 
            return variableName;
        
        int dollarPos = variableName.indexOf(TMapConstants.DOLLAR_SIGN),
            dotPos =  variableName.indexOf(TMapConstants.DOT);
        
        int firstPos = (dollarPos < 0 ? 0 : dollarPos + 1),
            lastPos = (dotPos < 0 ? variableName.length() : dotPos);
        
        if (firstPos > variableName.length() - 1) {
            firstPos = variableName.length() - 1;
        }
        String simpleName = variableName.substring(firstPos, lastPos).trim();
        return simpleName;
    }

    public static List<String> getEqualOperationNames(
        WsdlDataHolder mainWsdlHolder, WsdlDataHolder otherWsdlHolder) {
        Set<String> mainOperationName = getWsdlOperationNameSet(mainWsdlHolder),
                    otherOperationName = getWsdlOperationNameSet(otherWsdlHolder);        
        if ((mainOperationName == null) || (mainOperationName.isEmpty()) ||
            (otherOperationName == null) || (otherOperationName.isEmpty()))
            return  null;
        
        mainOperationName.retainAll(otherOperationName);
        return (new ArrayList<String>(mainOperationName));
    }

    public static List<String> getEqualPortTypes(
        WsdlDataHolder mainWsdlHolder, WsdlDataHolder otherWsdlHolder) {
        Set<String> mainPortType = getWsdlPortTypeSet(mainWsdlHolder),
                    otherPortType = getWsdlPortTypeSet(otherWsdlHolder);        
        if ((mainPortType == null) || (mainPortType.isEmpty()) ||
            (otherPortType == null) || (otherPortType.isEmpty()))
            return  null;
        
        mainPortType.retainAll(otherPortType);
        return (new ArrayList<String>(mainPortType));
    }

    public static List<String> getEqualPartNames(
        WsdlDataHolder mainWsdlHolder, WsdlDataHolder otherWsdlHolder) {
        Set<String> mainPartName = getWsdlPartNameSet(mainWsdlHolder),
                    otherPartName = getWsdlPartNameSet(otherWsdlHolder);        
        if ((mainPartName == null) || (mainPartName.isEmpty()) ||
            (otherPartName == null) || (otherPartName.isEmpty()))
            return  null;
        
        mainPartName.retainAll(otherPartName);
        return (new ArrayList<String>(mainPartName));
    }
    
    public static FileObject getXsltFileObject(Transform transform) {
        try {
            // get FileObject, related to the opened file "transformmap.xml"
            FileObject fileObject = 
                transform.getModel().getModelSource().getLookup().lookup(FileObject.class);
            if ((fileObject == null) || (! fileObject.isValid())) {
                return null;
            }
            Project project = SoaUtil.getProject(fileObject);
            FileObject srcFolder = Util.getSrcFolder(project);
            
            String fileName = transform.getFile();
            if ((fileName == null) || (fileName.length() < 1)) return null;
            
            File xsltFile = new File(fileName);
            FileObject xsltFileObject = (xsltFile.isAbsolute() ? 
                FileUtil.toFileObject(xsltFile) :
                ModelUtil.getRelativeFO(srcFolder, fileName));

            return xsltFileObject;            
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
            return null;
        }
    }
    
    public static XslModel getXslModel(FileObject xsltFileObject) {
        try {
            ModelSource xslModelSource = Utilities.createModelSource(
                xsltFileObject, true);
            XslModel xslModel = XslModelFactoryAccess.getFactory().getModel(xslModelSource);
            xslModel.sync();
            return xslModel;            
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionInfo(e);
            return null;
        }
    }
    
    public static List<String> getXsltGlobalParamNameList(XslModel xslModel) {
        if (xslModel == null) return null;
        
        Stylesheet stylesheet = xslModel.getStylesheet();
        if (stylesheet == null) return null;
        
        List<StylesheetChild> stylesheetChildren = stylesheet.getStylesheetChildren();
        if ((stylesheetChildren == null) || (stylesheetChildren.isEmpty())) return null;

        List<String> globalParamNames = new ArrayList<String>();
        for (StylesheetChild child : stylesheetChildren) {
            if (child instanceof org.netbeans.modules.xslt.model.Param) {
                org.netbeans.modules.xslt.model.Param xsltGlobalParam = 
                    (org.netbeans.modules.xslt.model.Param) child;
                javax.xml.namespace.QName qnameGlobalParamName = xsltGlobalParam.getName();
                if (qnameGlobalParamName == null) continue;

                String globalParamName = qnameGlobalParamName.getLocalPart();
                if ((globalParamName == null) || (globalParamName.length() < 1)) continue;

                globalParamNames.add(globalParamName);
            }
        }
        return globalParamNames;
    }

    public static String getXmlTagAttributeValue(Model model, String xmlTagName,
        String attributeName) {
        if ((model == null) || (xmlTagName == null) || (xmlTagName.length() < 1) ||
            (attributeName == null) || (attributeName.length() < 1)) return null;
        
        FileObject fileObject = model.getModelSource().getLookup().lookup(FileObject.class);
        if ((fileObject == null) || (! fileObject.isValid())) return null;
        
        String filePath = fileObject.getPath();
        if ((filePath == null) || (filePath.length() < 1)) return null;
        
        Document xmlDocument = null;
        try {
            xmlDocument = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder().parse(
                filePath);
        } catch (Exception e) {
            return null;
        }
        if (xmlDocument == null) return null;
        
        NodeList nodeList = xmlDocument.getElementsByTagName(xmlTagName);
        if ((nodeList == null) || (nodeList.getLength() < 1)) return null;
        
        Node nodeXmlTag = nodeList.item(0);
        if (nodeXmlTag == null) return null;
        
        NamedNodeMap nodeMap = nodeXmlTag.getAttributes();
        if ((nodeMap == null) || (nodeMap.getLength() < 1)) return null;

        Node attribute = nodeMap.getNamedItem(attributeName);
        if (attribute == null) return null;
        
        String attributeValue = attribute.getNodeValue();
        return attributeValue;
    }
    
    public static boolean isVariableNameCorrect(String variableName, 
        TMapComponent tmapComponent) {
        if ((variableName == null) || (variableName.length() < 1)) return false;
        
        if (! ((tmapComponent instanceof Transform) || 
               (tmapComponent instanceof Param))) return false;
        
        Pattern pattern = (tmapComponent instanceof Transform ? 
            PATTERN_TRANSFORM_VARIABLE : PATTERN_PARAM_VARIABLE);
        return (pattern.matcher(variableName).matches());
    }
    
    public static boolean isSourceResultTheSameDataType(
        VariableReference sourceRef, VariableReference resultRef) {
        if ((sourceRef == null) || (resultRef == null)) return true;

        Variable sourceVariable = sourceRef.getReferencedVariable(),
                 resultVariable = resultRef.getReferencedVariable();
        if ((sourceVariable == null) || (resultVariable == null)) return true;
        try {
            Message sourceMessage = sourceVariable.getMessage().get(),
                    resultMessage = resultVariable.getMessage().get();
            
            String sourceVariableName = sourceRef.getRefString(),
                   resultVariableName = resultRef.getRefString(),
                   sourcePartName = getVariablePartName(sourceVariableName),
                   resultPartName = getVariablePartName(resultVariableName);
                
            List<Part> sourceParts = new ArrayList<Part>(sourceMessage.getParts()),
                       resultParts = new ArrayList<Part>(resultMessage.getParts());
            
            Part sourcePart = findPartByName(sourceParts, sourcePartName), 
                 resultPart = findPartByName(resultParts, resultPartName);
            
            NamedComponentReference<GlobalType> 
                sourceTypeRef = sourcePart.getType(),
                resultTypeRef = resultPart.getType();
            if ((sourceTypeRef != null) && (resultTypeRef != null)) { // compare types
                String sourceTypeName = sourceTypeRef.getQName().getLocalPart(),
                       resultTypeName = resultTypeRef.getQName().getLocalPart();
                return (sourceTypeName.equals(resultTypeName));
            } else { // compare type of elements
                NamedComponentReference<GlobalElement>
                    sourceElement = sourcePart.getElement(),
                    resultElement = resultPart.getElement();
                if (! ((sourceElement != null) && (resultElement != null)))
                    return false;

                String sourceTypeName = sourceElement.get().getType().getQName().getLocalPart(),
                       resultTypeName = resultElement.get().getType().getQName().getLocalPart();
                return (sourceTypeName.equals(resultTypeName));
            }
        } catch(Exception e) {
            return false;
        }
    }
    
    private static Part findPartByName(List<Part> parts, String requiredPartName) {
        if ((parts == null) || (parts.isEmpty()) || 
            (requiredPartName == null) || (requiredPartName.length() < 1)) return null;
        for (Part part : parts) {
            String partName = part.getName();
            if ((partName != null) && (partName.equals(requiredPartName)))
                return part;
        }
        return null;
    }
}