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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.spi.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.worklist.editor.spi.WSDLProvider;
import org.netbeans.modules.worklist.util.Utility;
import org.netbeans.modules.wsdleditorapi.generator.ElementOrType;
import org.netbeans.modules.wsdleditorapi.generator.OperationType;
import org.netbeans.modules.wsdleditorapi.generator.PartAndElementOrType;
import org.netbeans.modules.wsdleditorapi.generator.PortTypeGenerator;
import org.netbeans.modules.wsdleditorapi.generator.WSDLWizardConstants;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mbhasin
 */
public class WLMWSDLProvider implements WSDLProvider {

    static String defaultWSDLFileName = "NewWorflowWSDL"; //NOI18N;
    static String DEFAULT_TARGET_NAMESPACE = "urn:WS/wsdl"; //NOI18N;
    static String encoding = "UTF-8"; //NOI18N;
    static String fileTemplateUrl = "/org/netbeans/modules/worklist/editor/WSDLTemplate.wsdl";
    static String wfDefFileSuffix = ".wsdl"; //NOI18N;

    public WSDLModel generateWSDL(String wsdlName, String location, 
            String namespaceBase, Message inputMessage, Message outputMessage, 
            Message faultMessage) 
    {
        String portTypeName = wsdlName;
        
        if (portTypeName == null) {
            portTypeName = DEFAULT_PORT_TYPE_NAME;
        } else {
            portTypeName = portTypeName.trim();
            if (portTypeName.length() == 0) {
                portTypeName = DEFAULT_PORT_TYPE_NAME;
            }
        }
        
        if (namespaceBase == null) {
            namespaceBase = DEFAULT_NAMESPACE_BASE; 
        } else {
            namespaceBase = namespaceBase.trim();
            if (namespaceBase.length() == 0) {
                namespaceBase = DEFAULT_NAMESPACE_BASE;
            } 
        }
        
        String targetNamespace;
        
        if (namespaceBase.endsWith("/")) { // NOI18N
            targetNamespace = namespaceBase + portTypeName;
        } else {
            targetNamespace = namespaceBase + "/" + portTypeName; // NOI18N
        }
        
        WSDLModel wsdlModel = getWSDLModelForTempleteWSDL(targetNamespace);
        
        createPortType(wsdlModel, portTypeName, 
                inputMessage, outputMessage, faultMessage);
        createWSDLFile(wsdlName, location, wsdlModel);
        
        // now build the model again from the actual wsdl file
        String wsdlFileURI = location + "/" + wsdlName  // NOI18N
                + WSDLProvider.WSDL_SUFFIX + "." + WSDLProvider.WSDL_EXT; // NOI18N

        wsdlModel = Utility.createWSDLModel(wsdlFileURI);
        
        return wsdlModel;
    }

    public static GlobalSimpleType getStringSimpleType(WSDLModel wsdlModel) {
        Map<String, String> namespaceToPrefixMap = buildNamespaceMap(wsdlModel);
        GlobalSimpleType stringSimpleType = null;
        Schema schema = SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema();
        Collection<GlobalSimpleType> simpleTypes = schema.getSimpleTypes();
        for (GlobalSimpleType st : simpleTypes) {
            if (st.getName().equals("string")) {
                stringSimpleType = st;
                break;
            }
        }
        PartAndElementOrType p = new PartAndElementOrType(generateUniquePartName(), new ElementOrType(stringSimpleType, namespaceToPrefixMap));
        return stringSimpleType;
    }

    public static Map buildNamespaceMap(WSDLModel wsdlModel) {
        Map namespaceToPrefixMap = new HashMap<String, String>();
        Map<String, String> prefixes = ((AbstractDocumentComponent) wsdlModel.getDefinitions()).getPrefixes();
        if (prefixes != null) {
            for (String prefix : prefixes.keySet()) {
                namespaceToPrefixMap.put(prefixes.get(prefix), prefix);
            }
        }

        return namespaceToPrefixMap;
    }
    
    public static WSDLModel getWSDLModelForTempleteWSDL() {
        return getWSDLModelForTempleteWSDL(getNS());
    }

    public static WSDLModel getWSDLModelForTempleteWSDL(String targetNameSpace) 
    {
        File templeteWSDLFile = null;
        WSDLModel wsdlModel = null;
        try {
            templeteWSDLFile = getWSDLFromTemplate(templeteWSDLFile);
            templeteWSDLFile.deleteOnExit();
            wsdlModel = prepareModelFromFile(templeteWSDLFile, 
                    defaultWSDLFileName, targetNameSpace);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return wsdlModel;
    }

    public static void createPortType(WSDLModel wsdlModel, ElementOrType input, 
            ElementOrType output, ElementOrType fault) 
    {
        createPortType(wsdlModel, DEFAULT_PORT_TYPE_NAME, input, output, fault);
    }
    
    public static void createPortType(WSDLModel wsdlModel, String portTypeName,
            ElementOrType input, ElementOrType output, ElementOrType fault) 
    {
        Map<String, String> namespaceToPrefixMap = buildNamespaceMap(wsdlModel);
        List<PartAndElementOrType> inputMessageParts = constructMessageParts(input, namespaceToPrefixMap);
        List<PartAndElementOrType> outputMessageParts = constructMessageParts(output, namespaceToPrefixMap);
        // fault message is optional
        List<PartAndElementOrType> faultMessageParts = null;
        if (fault != null) {
            faultMessageParts = constructMessageParts(fault, namespaceToPrefixMap);
        }
        generatePortType(wsdlModel, portTypeName, inputMessageParts, 
                outputMessageParts, faultMessageParts, namespaceToPrefixMap);
    }

    public static void createPortType(WSDLModel wsdlModel, String portTypeName,
            Message input, Message output, Message fault) 
    {
        Map<String, String> namespaceToPrefixMap = buildNamespaceMap(wsdlModel);
        List<PartAndElementOrType> inputMessageParts = constructMessageTypes(input.getParts(), namespaceToPrefixMap);
        List<PartAndElementOrType> outputMessageParts = constructMessageTypes(output.getParts(), namespaceToPrefixMap);
        // fault message is optional
        List<PartAndElementOrType> faultMessageParts = null;
        if (fault != null) {
            faultMessageParts = constructMessageTypes(fault.getParts(), namespaceToPrefixMap);
        }
        generatePortType(wsdlModel, portTypeName, inputMessageParts, 
                outputMessageParts, faultMessageParts, namespaceToPrefixMap);
    }

    public static void createWSDLFile(String taskName, String folderLocation, WSDLModel wsdlModel) {
        File srcFolder = new File(folderLocation);
// BACKUP:
//        FileObject fileObj = FileUtil.toFileObject(srcFolder);
        FileObject fileObj = null;
        
        try {
            fileObj = FileUtil.createFolder(srcFolder);
        } catch (IOException ex) {
            System.out.print(ex.getMessage());
        }
        
        FileObject fo = null;
        try {
            fo = fileObj.createData(taskName + WSDL_SUFFIX, WSDL_EXT);
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        if (wsdlModel != null && fo != null) {
            Document doc = wsdlModel.getBaseDocument();
            Utility.writeOutputFile(doc, fo, encoding);
        }
    }

    private static List<PartAndElementOrType> constructMessageParts(ElementOrType elementOrType, Map<String, String> namespaceToPrefixMap) {
        List<PartAndElementOrType> messageParts = new ArrayList();
        PartAndElementOrType p = new PartAndElementOrType(generateUniquePartName(), elementOrType);
        messageParts.add(p);
        return messageParts;
    }

    private static void generatePortType(WSDLModel wsdlModel,
            String portTypeName,
            List<PartAndElementOrType> inputMessageParts,
            List<PartAndElementOrType> outputMessageParts,
            List<PartAndElementOrType> faultMessageParts,
            Map<String, String> namespaceToPrefixMap) {

        // String portTypeName = "portTypeName";
        String operationName = "executeTask"; // NOI18N

        OperationType ot = new OperationType(OperationType.OPERATION_REQUEST_REPLY, "operationName");
        boolean autoGenPLT = true;

        Map configurationMap = new HashMap();
        //portType
        configurationMap.put(WSDLWizardConstants.PORTTYPE_NAME, portTypeName);
        configurationMap.put(WSDLWizardConstants.OPERATION_NAME, operationName);
        configurationMap.put(WSDLWizardConstants.OPERATION_TYPE, ot);
        configurationMap.put(WSDLWizardConstants.AUTOGEN_PARTNERLINKTYPE, autoGenPLT);

        //opertion type
        configurationMap.put(WSDLWizardConstants.OPERATION_INPUT, inputMessageParts);
        configurationMap.put(WSDLWizardConstants.OPERATION_OUTPUT, outputMessageParts);
        configurationMap.put(WSDLWizardConstants.OPERATION_FAULT, faultMessageParts);
        configurationMap.put(WSDLWizardConstants.NAMESPACE_TO_PREFIX_MAP, namespaceToPrefixMap);
        configurationMap.put(WSDLWizardConstants.IS_FROM_WIZARD, Boolean.FALSE);
        PortTypeGenerator ptGen = new PortTypeGenerator(wsdlModel, configurationMap);

        wsdlModel.startTransaction();
        try {
            ptGen.execute();
        } finally {
            wsdlModel.endTransaction();
        }

    }

    private static List<PartAndElementOrType> constructMessageTypes(Collection<Part> parts, Map<String, String> namespaceToPrefixMap) {
        List<PartAndElementOrType> messageParts = new ArrayList();
        NamedComponentReference<GlobalElement> element = null;
        NamedComponentReference<GlobalType> type = null;
        ElementOrType eot = null;
        Iterator iter = parts.iterator();
        PartAndElementOrType p = null;
        for (Part part : parts) {
            part = (Part) iter.next();
            element = part.getElement();
            if (element != null) {
                GlobalElement e = element.get();
                eot = new ElementOrType(e, namespaceToPrefixMap);
            } else {
                type = part.getType();
                GlobalType t = type.get();
                eot = new ElementOrType(t, namespaceToPrefixMap);
            }

            if (eot != null) {
                p = new PartAndElementOrType(generateUniquePartName(), eot);
                messageParts.add(p);
            }
        }

        return messageParts;
    }

    private static WSDLModel prepareModelFromFile(File file, 
            String definitionName, String targetNameSpace) {
        File f = FileUtil.normalizeFile(file);
//        FileObject fobj = FileUtil.toFileObject(f);
//        ModelSource modelSource = Utilities.getModelSource(fobj, fobj.canWrite());
//        WSDLModel model = WSDLModelFactory.getDefault().getModel(modelSource);

        WSDLModel model = Utility.createWSDLModel(f.getPath());

        if (model.getState() == WSDLModel.State.VALID) {
            model.startTransaction();
            try {
                model.getDefinitions().setName(definitionName);
                String ns = targetNameSpace;
                model.getDefinitions().setTargetNamespace(ns);
                ((AbstractDocumentComponent) model.getDefinitions()).addPrefix("tns", ns);
                if (model.getDefinitions().getTypes() == null) {
                    model.getDefinitions().setTypes(model.getFactory().createTypes());
                }
            } finally {
                model.endTransaction();
            }
        } else {
            assert false : "Model is invalid, correct the template if any";
        }

        return model;
    }

    private static String getNS() {
        String DEFAULT_TARGET_NAMESPACE = "urn:WS/wsdl"; //NOI18N;
        return DEFAULT_TARGET_NAMESPACE;
    }

    private static String generateUniquePartName() {
        String newNamePrefix = "part";
        int counter = 1;
        String generatedName = newNamePrefix + counter++;

        while (isPartNameExists(generatedName)) {
            generatedName = newNamePrefix + counter++;
        }

        return generatedName;
    }

    private static boolean isPartNameExists(String newPartName) {
        // TODO Fix the following. This list should aggregate all the namespaces
        List<PartAndElementOrType> mPartAndElementOrTypeList = new ArrayList();
        Iterator<PartAndElementOrType> it = mPartAndElementOrTypeList.iterator();
        while (it.hasNext()) {
            PartAndElementOrType row = it.next();
            String partName = row.getPartName();
            //if name exists then create another name
            if (partName != null && partName.equals(newPartName)) {
                return true;
            }
        }

        return false;
    }

    private static File getWSDLFromTemplate(File file) 
            throws 
                FileNotFoundException, 
                UnsupportedEncodingException, 
                IOException 
    {
        String content = Utility.readFileContent(fileTemplateUrl);
        content = content.replaceAll("#SERVICE_NAME", defaultWSDLFileName);
        content = content.replaceAll("#TARGET_NAMESPACE", DEFAULT_TARGET_NAMESPACE);
        return Utility.getFile(content, encoding, defaultWSDLFileName, wfDefFileSuffix);
    }
    
    private static final String DEFAULT_NAMESPACE_BASE = "wlmtask";
    private static final String DEFAULT_PORT_TYPE_NAME = "portTypeName"; // NOI18N
}
