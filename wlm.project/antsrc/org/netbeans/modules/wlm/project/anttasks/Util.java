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

package org.netbeans.modules.wlm.project.anttasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSModel;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMModelFactory;
import org.netbeans.modules.wlm.model.api.WSDLReference;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * This class is used to obtain the Task Model from workflow file and form
 * relation between PartnerLink+PartnerLinkType to TaskModel. This class is used
 * by ServiceUnitManager to obtain the taskmodel
 * 
 * @author mbhasin
 */
public class Util {

    private static final Logger mLogger = Logger.getLogger(Util.class.getName());
    
    public static final String  XMLSCHEMA_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * XMLSchema Instance Namespace declaration
     */
    public static final String XMLSCHEMA_INSTANCE_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * XMLNS Namespace declaration.
     */
    public static final String XMLNS_NAMESPACE_URI =
            "http://www.w3.org/2000/xmlns/";

    /**
     * XML Namespace declaration
     */
    public static final String XML_NAMESPACE_URI =
            "http://www.w3.org/XML/1998/namespace";

    /**
     * XForms namespace declaration.
     */
    public static final String XFORMS_NS = "http://www.w3.org/2002/xforms";

    /**
     * Chiba namespace declaration.
     */
    public static final String CHIBA_NS =
            "http://chiba.sourceforge.net/xforms";

    /**
     * XLink namespace declaration.
     */
    public static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    /**
     * XML Events namsepace declaration.
     */
    public static final String XMLEVENTS_NS = "http://www.w3.org/2001/xml-events";

    /**
     * Chiba prefix
     */
    public static final String chibaNSPrefix = "chiba:";

    /**
     * XForms prefix
     */
    public static final String xformsNSPrefix = "xforms:";

    /**
     * Xlink prefix
     */
    public static final String xlinkNSPrefix = "xlink:";

    /**
     * XMLSchema instance prefix *
     */
    public static final String xmlSchemaInstancePrefix = "xsi:";
    

    /**
     * XMLSchema  prefix *
     */
    public static final String xmlSchemaPrefix = "xsd:";    

    /**
     * XML Events prefix
     */
    public static final String xmleventsNSPrefix = "ev:";
    
    /**
     * Obeon xforms namespace declaration
     */
    public static final String XXFORMS = "http://orbeon.org/oxf/xml/xforms";

    private static final String PROP_FILE = "resources/jbi_gen.properties";
    public static final String TASK_SERVICE_NAME = "Task_ServiceName";
    private static Properties mProps;

    public Util() {
    }

    /**
     * Generate *TaskName*xform.xhtml based on the workflowFile in
     * projectSourceDir
     * 
     * @param workFlowFile
     * @param buildDir
     * @param projectSourceDir
     * @throws Exception
     */
    public static boolean generateXForm(File workFlowFile, File buildDir, File projectSourceDir, boolean always, boolean generate) throws Exception {
        WLMModel wlmModel = null;
        TTask task = null;
        try {
            FileObject workFlowFileObj = FileUtil.toFileObject(workFlowFile);
            ModelSource wlmModelSource = Utilities.createModelSource(workFlowFileObj, true);
            wlmModel = WLMModelFactory.getDefault().getModel(wlmModelSource);
            task = wlmModel.getTask();
        } catch (Exception me) {
            mLogger.severe("Error creating WorkFlowModel " + workFlowFile.getAbsolutePath() + " Reason " + me.toString());
            me.printStackTrace();
            throw me;
        }
        Collection<TImport> wsdlDefs = null;
        try {
            wsdlDefs = task.getImports();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error encountered while retrieving the imported WSDL in workfile " + workFlowFile.getAbsolutePath());
        }

        Map<String, WSDLModel> mapOfTNSToWsdlDef = new HashMap<String, WSDLModel>();
        WSDLModel wsdlModel = null;

        for (TImport impWsdls : wsdlDefs) {
            try {
                wsdlModel = impWsdls.getImportedWSDLModel();
            } catch (CatalogModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mapOfTNSToWsdlDef.put(impWsdls.getNamespace(), wsdlModel);
        }

        String taskName = task.getName();
        File xhtml = new File(projectSourceDir, taskName + "Xform.xhtml");
        boolean genXhtml = true;

        if (!generate && !always) {
            genXhtml = false;
        } else if (generate && xhtml.exists() && !always) {
            genXhtml = false;
        } else {
            genXhtml = true;
        }

        WSDLReference<Operation> taskOpn = null;
        try {
            if (task == null) {
                System.out.println(" task is null");
            }
            if (task.getOperation() == null) {
                System.out.println("WSDL opn is null");
            }
             taskOpn = task.getOperation();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error encountered while retreiving WSDL operation  in workfile " + workFlowFile.getAbsolutePath());
        }

        if (taskOpn.get() == null) {
            System.out.println("taskOpn.get() is null");
        }
        org.netbeans.modules.xml.wsdl.model.Input taskOpnInp = taskOpn.get().getInput();

        org.netbeans.modules.xml.wsdl.model.Output taskOpnOut = taskOpn.get().getOutput();

        // Get the input/output message
        NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message> taskOpnInMsg = taskOpnInp.getMessage();
        NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message> taskOpnOutMsg = taskOpnOut.getMessage();

        wsdlModel = (WSDLModel) mapOfTNSToWsdlDef.get(taskOpnInMsg.getQName().getNamespaceURI());

        File wsdlFileFromURI = null;
        Lookup lookup = null;
        try {
            ModelSource wsdlModelSource = wsdlModel.getModelSource();
            lookup = wsdlModelSource.getLookup();
            FileObject fileObj = (FileObject) lookup.lookup(FileObject.class);
            URI wsdlFileURI = FileUtil.toFile(fileObj).toURI();
            wsdlFileFromURI = new File(wsdlFileURI);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new RuntimeException("Invalid  WSDL Location " + wsdlFileFromURI.toString());
        }

        SchemaUnit schemaUnitIn = getSchemaUnit(wsdlModel, taskOpnInMsg, wsdlFileFromURI, buildDir, projectSourceDir, "in", null);
        SchemaUnit schemaUnitOut = getSchemaUnit(wsdlModel, taskOpnOutMsg, wsdlFileFromURI, buildDir, projectSourceDir, "out", schemaUnitIn);

        return writeXform(schemaUnitIn, schemaUnitOut, projectSourceDir, buildDir,  xhtml, taskName, genXhtml);


    }

    private static SchemaUnit getSchemaUnit(
            WSDLModel wsdlModel,
            NamedComponentReference<org.netbeans.modules.xml.wsdl.model.Message> taskOpnInMsg, File wsdlFileFromURI, File buildDir,
            File projectSourceDir, String type, SchemaUnit alreadyCreated) throws Exception {

        // Get the list of parts defined in the message
        SchemaUnit result = null;
        Collection<org.netbeans.modules.xml.wsdl.model.Part> parts = taskOpnInMsg.get().getParts();

        Set<WSDL4JUtil.SchemaLocation> setOfSchemaLoc = null;

        for (org.netbeans.modules.xml.wsdl.model.Part taskPart : parts) {
            NamedComponentReference<GlobalType> taskPartTypeType = taskPart.getType();
            NamedComponentReference<GlobalElement> taskPartElementType = taskPart.getElement();

            setOfSchemaLoc =
                    new HashSet<WSDL4JUtil.SchemaLocation>();

            WSDL4JUtil wsdlUtil = WSDL4JUtil.getInstance();

            NamedComponentReference<? extends ReferenceableSchemaComponent> typeOrElementQName = (taskPartElementType != null) ? taskPartElementType
                    : taskPartTypeType;
            QName  taskPartElQname = taskPartElementType == null ? null:taskPartElementType.getQName();
            QName  taskPartTypeQName = taskPartTypeType == null ? null: taskPartTypeType.getQName();
            XSModel existingModel = alreadyCreated == null ? null : alreadyCreated.getModel();
            if (existingModel != null) {
                if (hasElement(existingModel, taskPartElQname, taskPartTypeQName)) {
                    return  new SchemaUnit(existingModel, alreadyCreated.schemaFile, taskPartElQname, taskPartTypeQName, false);
                }
            }
            
            setOfSchemaLoc =
                    wsdlUtil.getSchemaLocations(wsdlModel, typeOrElementQName.getEffectiveNamespace(), wsdlFileFromURI, buildDir,
                    projectSourceDir, type);

            Iterator<WSDL4JUtil.SchemaLocation> schmLocItr = setOfSchemaLoc.iterator();

            while (schmLocItr.hasNext()) {
                WSDL4JUtil.SchemaLocation wsdlUtilSchLoc = schmLocItr.next();
                if (!wsdlUtilSchLoc.isSimpleType()) {
                    File schemaFile = wsdlUtilSchLoc.getFileLocation();
                    XSModel model = null;
                    try {
                        model = loadSchema(schemaFile.toURL().getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }                    
                    if (setOfSchemaLoc.size() == 1) {
                        result = new SchemaUnit(model, wsdlUtilSchLoc.getFileLocation(), taskPartElQname, taskPartTypeQName, false);
                        return result;
                    } else {
                        if (model != null) {
                            if (hasElement(model, taskPartElQname, taskPartTypeQName)) {
                                result = new SchemaUnit(model, wsdlUtilSchLoc.getFileLocation(), taskPartElQname, taskPartTypeQName, false);
                                return result;
                            }

                        }
                    }
                } else {
                    result = new SchemaUnit(null, wsdlUtilSchLoc.getFileLocation(),  taskPartElQname, taskPartTypeQName, true);
                    return result;
                }
            }
        }
        return result;
    }

    private static boolean writeXform(SchemaUnit inSchemaUnit, SchemaUnit outSchemaUnit, File projectSourceDir, File buildDir, File xhtml, String taskName, boolean writeXhtml) throws Exception {
        // create input, output xml instance
        File inSchemaFile = inSchemaUnit.getSchemaFile();
        QName inElementName = inSchemaUnit.getElementName();
        QName inTypeName = inSchemaUnit.getTypeName();
        File outSchemaFile = outSchemaUnit.getSchemaFile();
        QName outElementName = outSchemaUnit.getElementName();
        QName outTypeName = outSchemaUnit.getTypeName();

        try {

            XmlOptions compileOptions = new XmlOptions();
            compileOptions.setCompileDownloadUrls();
            XmlObject[] schemas = null;
            URL[] urls = null;

            ArrayList<XmlError> errorList = new ArrayList<XmlError>();
            compileOptions.setErrorListener(errorList);
            SchemaTypeSystem sts = null;
            Thread thread = Thread.currentThread();
            ClassLoader oldCl = thread.getContextClassLoader();
            SchemaTypeSystem built_inSTS = null;
            try {
                built_inSTS = XmlBeans.getBuiltinTypeSystem();
                if (inSchemaUnit.isSimpleType() && outSchemaUnit.isSimpleType()) {
                    sts = built_inSTS;
                } else {
                    if (!inSchemaUnit.isSimpleType() && !outSchemaUnit.isSimpleType()) {
                        if (inSchemaFile.getAbsolutePath().equals(outSchemaFile.getAbsolutePath())) {
                            urls = new URL[1];
                            urls[0] = inSchemaFile.getParentFile().toURL();
                        } else {
                            urls = new URL[2];
                            urls[0] = inSchemaFile.getParentFile().toURL();
                            urls[1] = outSchemaFile.getParentFile().toURL();
                        }
                    } else if (!inSchemaUnit.isSimpleType()) {
                        urls = new URL[1];
                        urls[0] = inSchemaFile.getParentFile().toURL();
                    } else if (!outSchemaUnit.isSimpleType()) {
                        urls = new URL[1];
                        urls[0] = outSchemaFile.getParentFile().toURL();
                    }

                    URLClassLoader cl = new URLClassLoader(urls, oldCl);
                    thread.setContextClassLoader(cl);
                    if (inSchemaFile != null && outSchemaFile != null) {
                    if (inSchemaFile.getAbsolutePath().equals(outSchemaFile.getAbsolutePath())) {
                        schemas = new XmlObject[1];
                        schemas[0] = SchemaDocument.Factory.parse(inSchemaFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
                    } else {
                        schemas = new XmlObject[2];
                        schemas[0] = SchemaDocument.Factory.parse(inSchemaFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
                        schemas[1] = SchemaDocument.Factory.parse(outSchemaFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
                    }
                    } else if (inSchemaFile != null) {
                        schemas = new XmlObject[1];
                        schemas[0] = SchemaDocument.Factory.parse(inSchemaFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());
                    } else if (outSchemaFile != null) {
                        schemas = new XmlObject[1];
                        schemas[0] = SchemaDocument.Factory.parse(outSchemaFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest());                       
                    }

                    try {
                        sts = XmlBeans.compileXsd(schemas, XmlBeans.getBuiltinTypeSystem(), compileOptions);
                    } catch (XmlException e) {
                        // TODO Auto-generated catch block
                        for (int i = 0; i <
                                errorList.size(); i++) {
                            XmlError error = errorList.get(i);
                            StringBuffer buffer = new StringBuffer();
                            buffer.append("\n");
                            buffer.append("Message: " + error.getMessage() + "\n");
                            buffer.append("Location of invalid XML: " + error.getCursorLocation().xmlText() + "\n");
                            throw new RuntimeException(buffer.toString(), e);
                        }

                        throw e;
                    }
                }

            } catch (Exception e) {
                throw e;
            } finally {
                thread.setContextClassLoader(oldCl);
            }

            SchemaType[] globalTypes  = sts.globalTypes();
            SchemaGlobalElement[] globalEls = sts.globalElements();
            
            SchemaType inType = null;
            SchemaType outType = null;

            SchemaGlobalElement  inEl = null;
            SchemaGlobalElement  outEl = null;
            
            if (inElementName != null || outElementName != null) {
            for (int i = 0; i < globalEls.length; i++) {
                if (inType == null && inElementName != null && inElementName.equals(globalEls[i].getName())) {
                        inEl = globalEls[i];
                        inType = inEl.getType();
                } 
                
                if (outType == null && outElementName != null && outElementName.equals(globalEls[i].getName())) {
                    outEl = globalEls[i];
                    outType = outEl.getType();
                }

                if (inType != null && outType != null) {
                    break;
                }
                
                }
            } else if (inTypeName != null || outTypeName != null ) {
                for (int i = 0; i < globalTypes.length; i++) {
                    if (inType == null && inTypeName != null && inTypeName.equals(globalTypes[i].getName())) {
                            inType = globalTypes[i];
                    } 
                    
                    if (outType == null && outTypeName != null && outTypeName.equals(globalTypes[i].getName())) {
                        outType = globalTypes[i];
                    }

                    if (inType != null && outType != null) {
                        break;
                    }
                    
                    }                
            }
            if (inType == null && (inTypeName != null)) {
                globalTypes  = built_inSTS.globalTypes();
                for (int i = 0; inType == null && i < globalTypes.length; i++) {
                    if (inTypeName.equals(globalTypes[i].getName())) {
                            inType = globalTypes[i];
                    } 
                }                   
            }
            if (outType == null && (outTypeName != null)) {
                globalTypes  = sts.globalTypes();
                for (int i = 0; outType == null && i < globalTypes.length; i++) {
                    if (outTypeName.equals(globalTypes[i].getName())) {
                        outType = globalTypes[i];
                    } 
                }                   
            }
            String xmlString =  null;
            if (inEl != null) {
                xmlString = InstanceCreater.createSampleForElement(inEl);
            } else if (inType != null) {
                xmlString = SampleXmlUtil.createSampleForType(inType);
            }
            writeFile(new File(projectSourceDir, taskName + "InputInstance.xml").getAbsolutePath(), xmlString, false);


            if (outEl != null) {
                xmlString = InstanceCreater.createSampleForElement(outEl);
            } else if (outType != null) {
                xmlString = InstanceCreater.createSampleForType(outType);
            }
            writeFile(new File(projectSourceDir, taskName + "OutputInstance.xml").getAbsolutePath(), xmlString, false);


            String urlIn = inSchemaFile == null? null: inSchemaFile.toURL().getPath();

            String urlOut = outSchemaFile == null ? null: outSchemaFile.toURL().getPath();

            Map<String, String> defaultValue = new HashMap<String, String>();

            org.xml.sax.XMLReader reader = makeXMLReader();
            reader.setContentHandler(new Sink(defaultValue));


            if (writeXhtml) {
            BaseSchemaFormBuilder builderIn = new BaseSchemaFormBuilder(inElementName, inTypeName);
            BaseSchemaFormBuilder builderOut = new BaseSchemaFormBuilder(outElementName, outTypeName, true, defaultValue);
            Document formIn = builderIn.buildForm(urlIn);
//                       String inString = XmlUtil.toXml(formIn, "UTF-8", true);
//                       System.out.println("in:" + inString);
            Document formOut = builderOut.buildForm(urlOut);
//                       String outString = XmlUtil.toXml(formOut, "UTF-8", true);
//                        System.out.println("out:" + outString);
        

            InputStream urlInXSLT = Util.class.getResourceAsStream("resources/inputtransform.xsl");
            InputStream urlOutXSLT = Util.class.getResourceAsStream("resources/outputtransform.xsl");
            Node intransformed = XmlUtil.transformToDoc(new DOMSource(formIn), new StreamSource(urlInXSLT));
            Node outtransformed = XmlUtil.transformToDoc(new DOMSource(formOut), new StreamSource(urlOutXSLT));
            
            //Get the original output xform header that contains the model
            NodeList nodes = formOut.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "head");
            Element headEl = (Element) nodes.item(0);
            
            //The root of output Xform
            Node outXformNode =((Document) outtransformed).getDocumentElement();
    
            
            Element root = (Element)((Document) intransformed).importNode(formOut.getDocumentElement(), false);
            
            // set required namespace attributes
//            root.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:"
//                +chibaNSPrefix.substring(0,
//                        chibaNSPrefix.length() - 1), CHIBA_NS);
//            root.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:"
//                + xformsNSPrefix.substring(0,
//                        xformsNSPrefix.length() - 1), XFORMS_NS);
//            root.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:"
//                + xlinkNSPrefix.substring(0,
//                        xlinkNSPrefix.length() - 1), XLINK_NS);
        // XMLEvent
//            root.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:"
//                + xmleventsNSPrefix
//                        .substring(0, xmleventsNSPrefix.length() - 1),
//                XMLEVENTS_NS);
        // XML Schema Instance
//            root.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:"
//                + xmlSchemaInstancePrefix.substring(0, xmlSchemaInstancePrefix
//                        .length() - 1), XMLSCHEMA_INSTANCE_NAMESPACE_URI);
//            
//            root.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:"
//                + xmlSchemaPrefix.substring(0, xmlSchemaPrefix
//                        .length() - 1), XMLSCHEMA_NAMESPACE_URI);            
               

            Element head = (Element)((Document) intransformed).importNode(headEl, true);
            root.appendChild(head);

        // style 
            
//            Element style = ((Document) intransformed).createElementNS(XFORMS_NS, "xhtml:style");
//            style.setAttribute("type", "text/css");
//            Node text = ((Document) intransformed).createTextNode("table.input {\n" +
//                    "border-width: 1px 1px 1px 1px;\n" +
//                    "border-spacing: 2px;\n" +
//                    "border-style: outset outset outset outset;\n" +
//                    "border-color: gray gray gray gray;\n" +
//                    "border-collapse: collapse;\n" +
//                    "background-color: white;\n" +
//            "}\n" +
//            "table.input th {\n" +
//                    "border-width: 1px 1px 1px 1px;\n" +
//                    "padding: 3px 3px 3px 3px;\n" +
//                    "border-style: inset inset inset inset;\n" +
//                    "border-color: gray gray gray gray;\n" +
//                    "background-color: white;\n" +
//                    "-moz-border-radius: 0px 0px 0px 0px;\n" +
//            "}\n" +
//            "table.input td {\n" +
//                    "border-width: 1px 1px 1px 1px;\n" +
//                    "padding: 3px 3px 3px 3px;\n" +
//                    "border-style: inset inset inset inset;\n" +
//                    "border-color: gray gray gray gray;\n" +
//                    "background-color: white;\n" +
//                    "-moz-border-radius: 0px 0px 0px 0px;\n" +
//            "}\n"
//            );
//            style.appendChild(text);
//            head.appendChild(style);
            Element title = ((Document) intransformed).createElementNS("http://www.w3.org/1999/xhtml", "xhtml:title");

            title.appendChild(((Document) intransformed).createTextNode("Task Details"));
            head.appendChild(title);

            Element body = ((Document) intransformed).createElementNS("http://www.w3.org/1999/xhtml", "xhtml:body");

            root.appendChild(body);
            
            NodeList inNodeList = ((Document) intransformed).getDocumentElement().getChildNodes();
            for (int i = 0; i< inNodeList.getLength(); i++) {
                body.appendChild(inNodeList.item(i));
            }
            body.appendChild(
                    ((Document) intransformed).importNode(outXformNode, true));

            xmlString = XmlUtil.toXml(root, "UTF-8", true);

            writeFile(xhtml.getAbsolutePath(), xmlString, false);
            }
        } catch (Exception e) {
            throw e;
        }

        return true;
    }

    private static XSModel loadSchema(String inputURI) throws java.lang.ClassNotFoundException, java.lang.InstantiationException,
            java.lang.IllegalAccessException {

        XSModel schema = null;
        // Get DOM Implementation using DOM Registry
        System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        Object o = registry.getDOMImplementation("XS-Loader");
        if (o instanceof XSImplementation) {
            org.apache.xerces.xs.XSImplementation impl = (org.apache.xerces.xs.XSImplementation) o;
            org.apache.xerces.xs.XSLoader schemaLoader = impl.createXSLoader(null);
            schema =
                    schemaLoader.loadURI(inputURI);
        }

        return schema;
    }

    public static boolean hasElement(XSModel schema, QName elementName, QName typeName) {
        if (elementName != null && schema.getElementDeclaration(elementName.getLocalPart(), elementName.getNamespaceURI()) != null) {
            return true;
        }
        if (typeName != null && schema.getTypeDefinition(typeName.getLocalPart(), typeName.getNamespaceURI()) != null) {
         return true;
        }
        return false;        
    }

    public static org.xml.sax.XMLReader makeXMLReader() throws Exception {
        final javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
        final javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
        final org.xml.sax.XMLReader parser = saxParser.getXMLReader();
        return parser;
    }

    public static void writeFile(String filename, String output, boolean append) throws IOException {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, append), "UTF-8"));

        out.write(output);
        out.close();
    }

    public static Properties getCommonProperties() {
        if (mProps == null) {
            mProps = new Properties();
            URL propUrl = Util.class.getResource(PROP_FILE);
            InputStream inputStream = null;
            try {
                inputStream = Util.class.getResourceAsStream(PROP_FILE);
                mProps.load(inputStream);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(NbBundle.getMessage(GenerateAsaArtifacts.class, "EX_FILE_NOT_FOUND", propUrl.toString()), e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(NbBundle.getMessage(GenerateAsaArtifacts.class, "EX_IOException", propUrl.toString()), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        return mProps;
    }

    private static class SchemaUnit {

        private File schemaFile;
        private QName elementName;
        private QName typeName;
        private boolean isSimpleType;
        private XSModel model;

        public SchemaUnit(XSModel model, File schemaFile,
                QName elementName, QName typeName, boolean isSimple) {
            this.schemaFile = schemaFile;
            this.elementName = elementName;
            this.typeName = typeName;
            this.isSimpleType = isSimple;
            this.model = model;
        }

        public QName getElementName() {
            return elementName;
        }
        
        public QName getTypeName () {
            return typeName;
        }

        public void setElementName(QName elementName) {
            this.elementName = elementName;
        }

        public File getSchemaFile() {
            return schemaFile;
        }

        public void setSchemaFile(File schemaFile) {
            this.schemaFile = schemaFile;
        }

        public boolean isSimpleType() {
            return isSimpleType;
        }
        

        public XSModel getModel() {
            return model;
        }
    }

    private static class Sink extends org.xml.sax.helpers.DefaultHandler implements org.xml.sax.ContentHandler {

        private Map<String, String> mMap = null;

        public Sink(Map<String, String> map) {
            super();
            mMap = map;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // TODO Auto-generated method stub
            if (qName.equals("mp:mapping")) {
                String path = "";
                String value = "";
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attName = attributes.getQName(i);
                    if (attName.equals("path")) {
                        path = attributes.getValue(i);
                    } else if (attName.equals("value")) {
                        value = attributes.getValue(i);
                    }
                }
                mMap.put(path, value);
            }
            super.startElement(uri, localName, qName, attributes);
        }
    }


    public static void main(String[] args) {
        File workFlowFile = new File(
                "C:/Alaska/jbicomps/wlmweb/orbeon/sample/purchaseOrderReview/PurchaseOrderWorkflowApp/src/ApprovePurchase.wf");
        File buildDir = new File("C:/Alaska/jbicomps/wlmweb/orbeon/sample/purchaseOrderReview/PurchaseOrderWorkflowApp/build");
        File projectSourceDir = new File("C:/Alaska/jbicomps/wlmweb/orbeon/sample/purchaseOrderReview/PurchaseOrderWorkflowApp/src");
        try {
            generateXForm(workFlowFile, buildDir, projectSourceDir, true, true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
