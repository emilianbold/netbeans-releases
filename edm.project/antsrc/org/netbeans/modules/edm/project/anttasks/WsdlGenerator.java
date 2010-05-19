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
package org.netbeans.modules.edm.project.anttasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import org.netbeans.modules.edm.editor.utils.RuntimeAttribute;
import org.netbeans.modules.edm.model.impl.MashupDefinitionImpl;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.RuntimeInput;


/**
 * This class generates an EDM WSDL file given an EDM engine file name
 *
 */

public class WsdlGenerator {
    
    private static Logger logger = Logger.getLogger(WsdlGenerator.class.getName());
    private static WSDLFactory factory;
    private String engineFileName;
    private String wsdlLocation;
    private Definition def;
    private File engineFile;
    private static String[][] datatypearray = {
                                                {"12","xsd:string"},
                                              };
    
    public String responseType = null;   
    static {
        initFactory();
    }
    
    /**
     * @param engineFileName
     * @param wsdlLocation
     */
    public WsdlGenerator(String engineFileName, String wsdlLocation) {
        this.engineFileName = engineFileName;
        this.wsdlLocation = wsdlLocation;
        
    }
    
    public WsdlGenerator(File f, String wsdlLocation) {
        engineFile = f;
        this.engineFileName = f.getName().substring(0, f.getName().indexOf(".edm"));
        this.wsdlLocation = wsdlLocation;
        
    }
    
    /**
     *
     * @param f,
     *            represents the engine file
     * @param engineFileName,
     *            the name used for generation wsdl artifacts, should be
     *            combination of project + engine file
     * @param wsdlLocation,
     *            location of the wsdl file generated
     */
    public WsdlGenerator(File f, String engineFileName, String wsdlLocation) {
        engineFile = f;
        this.engineFileName = engineFileName;
        this.wsdlLocation = wsdlLocation;
    }
    
    /**
     * generates the edm wsdl file and writes to disk
     *
     * @return Definition
     *
     * @throws WsdlGenerateException
     */
    public Definition generateWsdl() throws WsdlGenerateException {
        this.def = getWsdlTemplate();
        if (engineFileName == null)
            throw new WsdlGenerateException("cannot generate wsdl file as engineFileName is null ");
        if (wsdlLocation == null)
            throw new WsdlGenerateException("cannot generate wsdl file as wsdlLocation is null ");
        modifyWsdl();
        writeWsdl();
        return def;
    }
    
    private Map getEngineInputParams() {
        try {
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            Element element = df.newDocumentBuilder().parse(engineFile).getDocumentElement();
            MashupDefinitionImpl defnImpl = new MashupDefinitionImpl(element, null);
            SQLDefinition sqlDefn = defnImpl.getSQLDefinition();
            RuntimeDatabaseModel rtModel = sqlDefn.getRuntimeDbModel();
            responseType = sqlDefn.getResponseType().toUpperCase();
            if(rtModel != null) {
                RuntimeInput rtInput = rtModel.getRuntimeInput();
                if(rtInput != null) {
                    return rtInput.getRuntimeAttributeMap();
                }
            }
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new HashMap();
    }
    
    /**
     * persist the wsdl file to disk
     *
     * @throws WsdlGenerateException
     */
    private void writeWsdl() throws WsdlGenerateException {
        try {
       
            WSDLWriter writer = factory.newWSDLWriter();
            Writer sink = new FileWriter(wsdlLocation + File.separator + engineFileName + ".wsdl");

            writer.writeWSDL(def, sink);
            sink.flush();
            sink.close();
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new WsdlGenerateException(e);
        }
        
    }
    
    /**
     * modify the wsdl template
     */
    private void modifyWsdl() {
        modifyName();
	modifyMessages();
        modifyMessageTypes();
        modifyMessageParts();
        modifyPortTypes();
        modifyBinding();
        modifyServices();
        modifyPartnerLink();
    }
	
    private void modifyMessages() {
        Map msgs = def.getMessages();
        boolean flag = true;
        int partCount =0;
        Part newPart = null;
        java.util.Iterator keysIter = msgs.keySet().iterator();
        while (keysIter.hasNext()) {
            Message msg = (Message) msgs.get(keysIter.next());
                if (msg != null && msg.getQName().getLocalPart().indexOf("output") != -1) {
                    modifyMessageElementName((Message) msg);
                } else {
                    //modify input type message
                    String msgLocalName = msg.getQName().getLocalPart();
                    if (msg != null) {
                        QName qname = new QName("http://com.sun.jbi/edm/edmengine", engineFileName + "_" + msgLocalName);
                        msg.setQName(qname);
                    }
                    Map inputParams = getEngineInputParams();
                    Iterator iterator = inputParams.keySet().iterator();
                    while (iterator.hasNext()) {
                        Object key = iterator.next();
                        RuntimeAttribute ra = (RuntimeAttribute) inputParams.get(key);
                        Part dynamicPart = def.createPart();
                        QName pqname = new QName(getAttributeType(ra));
                        // dynamicPart.setElementName(pqname);
                        dynamicPart.setName(ra.getAttributeName());
                        dynamicPart.setTypeName(pqname);
                        msg.addPart(dynamicPart);

                    }

            }
        }
    }

    private void modifyMessageElementName(Message message) {
        String msgLocalName = message.getQName().getLocalPart();
        if (message != null) {
            QName qname = new QName("http://com.sun.jbi/edm/edmengine", engineFileName + "_" + msgLocalName);
            message.setQName(qname);
        }
        Part p = message.getPart("part");
        QName pqname = new QName("http://com.sun.jbi/edm/edmengine", engineFileName + "_" + p.getElementName().getLocalPart());
        p.setElementName(pqname);

    }
    
    private void modifyMessageParts() {
        
        Map msgs = def.getMessages();
        Iterator iter = msgs.values().iterator();
        while (iter.hasNext()) {
            Message mesg = (Message) iter.next();
            Map parts = mesg.getParts();
            modifyParts(parts.values());
        } 
    }
    
    private void modifyParts(Collection parts) {
        Iterator iter = parts.iterator(); 
        
        while (iter.hasNext()) {
            Part part = (Part) iter.next();
            
            if (part.getElementName() != null) {
                String namespaceURI = part.getElementName().getNamespaceURI();
                String localpart = part.getElementName().getLocalPart();
                String targetpart = (localpart.indexOf("outputItem") != -1) ? "outputItem" : " ";
                if (targetpart.equalsIgnoreCase("outputItem") && targetpart.length() != 0) {
                    part.setElementName(new QName(namespaceURI, engineFileName + "_" + targetpart));
                }
            } else {
                // mofified the logic for handling input items
                part.setTypeName(new QName(part.getTypeName().getNamespaceURI(), part.getTypeName().getLocalPart()));
                
            }
        }
        
    }
     
    private void addInlineOutputItemSchema(Element root){
        Element outputItem = getElementByName(root, engineFileName + "_" + "outputItem");
        Document doc  = (Document) root.getParentNode().getParentNode().getParentNode(); 
        NodeList outputSeqList = outputItem.getElementsByTagName("xsd:sequence");

        // Build Column Map Here for he Inline schema as dom has been created
        Hashtable outputColumnTable = createListOfColumns();

        // If the List has element, delete and rebuild it
        if (outputSeqList.item(0).getChildNodes().getLength() != 0) {
            for (int i=0; i < outputSeqList.item(0).getChildNodes().getLength(); i++) {
                outputSeqList.item(0).removeChild(outputSeqList.item(0).getChildNodes().item(i));
            }
        }
            
        Node sequence = outputSeqList.item(0);
        if (responseType.equals("RELATIONALMAP")) {
        Enumeration e = outputColumnTable.keys();
            logger.fine("Keys:" + e);
        while (e.hasMoreElements())
        {
            Element child = doc.createElementNS("http://www.w3.org/2001/XMLSchema", "xsd:element");
            String key = e.nextElement().toString();
            child.setAttribute("name", key);
            child.setAttribute("type", outputColumnTable.get(key).toString());
            sequence.appendChild(child);
            }
        }
    }
    
    private Hashtable createListOfColumns() {
        Hashtable<String, String>outputColumnMap = null;
        try {
            outputColumnMap = new Hashtable<String, String>();
            outputColumnMap.put("RECORD", getDataType("12")); //Schema node to represent records from the DB
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            logger.info("engineFile = " + engineFile);
            Element enginefileroot = factory.newDocumentBuilder().parse(engineFile).getDocumentElement();
            
            NodeList dbModelList =  enginefileroot.getElementsByTagName("dbModel");
            for (int i=0; i < dbModelList.getLength(); i++) {
            Element dbmodel = (Element)dbModelList.item(i);
            String attr = dbmodel.getAttribute("type");
            if (attr.equals("source")) {
                // This is the dbModel to work - keep processing
                NodeList tablenames = dbmodel.getElementsByTagName("dbTable");
                for (int j=0; j< tablenames.getLength(); j++) {
                    Element table = (Element)tablenames.item(j);
                    
                    NodeList columnamelist = table.getElementsByTagName("dbColumn");
                    for (int k=0; k < columnamelist.getLength(); k++) {
                        Element column = (Element)columnamelist.item(k);
                        if (!outputColumnMap.containsKey(column.getAttribute("name"))) {
                            outputColumnMap.put(column.getAttribute("name"), getDataType(column.getAttribute("type")));
                        }
                    }
                }
            }
        }
        } 
        catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return outputColumnMap;
    }

    private String getDataType(String dataType)
    {   
        String interpreteddatatype = null;
        for (int i=0; i < this.datatypearray.length;  i++)
        {
            if (this.datatypearray[i][0].equals(dataType))
            {
               interpreteddatatype = this.datatypearray[i][1];
                break;
            }
        }
        if (interpreteddatatype == null) interpreteddatatype="Undefined";
        return interpreteddatatype;
    }
    
    
    private void modifyBinding() {
        Binding b = def.getBinding(new QName(def.getTargetNamespace(), "Binding"));
        
        BindingOperation bo = b.getBindingOperation("execute", null, null);
      //  bo.setBindingInput(bo.getBindingInput());
//        if (getEngineInputParams().isEmpty()) {
//            bo.setBindingInput(null);
//        }
    }
    
    private void modifyMessageTypes() {
        Types types = def.getTypes();
        types.getDocumentationElement();
        List schemaList = types.getExtensibilityElements();

        for (int i = 0; i < schemaList.size(); i++) {
            Object o = schemaList.get(i);
            if (o instanceof Schema) {
                Schema s = (Schema) o;
                modifySchema(s);
            }
        } 
    }
    
    private void modifySchema(Schema s) {
        Element root = s.getElement();
        Document doc = (Document) root.getParentNode().getParentNode().getParentNode();
        Element inputItem = getElementByName(root, "inputItem"); 
        inputItem.setAttribute("name", engineFileName + "_" + "inputItem");
        Element outputItem = getElementByName(root, "outputItem");
        outputItem.setAttribute("name", engineFileName + "_" + "outputItem");
        Node sequence = inputItem.getElementsByTagName("xsd:sequence").item(0);
        
        Map inputParams = getEngineInputParams();
        Iterator iterator = inputParams.keySet().iterator();     
        
        while (iterator.hasNext()) {
            Object key = iterator.next();
            RuntimeAttribute ra = (RuntimeAttribute) inputParams.get(key); 
            Element child = doc.createElementNS("http://www.w3.org/2001/XMLSchema", "xsd:element");
            child.setAttribute("name", ra.getAttributeName());
            child.setAttribute("type", getAttributeType(ra));
            sequence.appendChild(child);
        }
       
        // Generate Inline Schema for the outputItem from the engine file
        //addInlineOutputItemSchema(root);
    }
    
    private String getAttributeType(RuntimeAttribute ra) {
        
        String type = null;        
        switch (ra.getJdbcType()) {
            
        case java.sql.Types.BOOLEAN:
            type = "xsd:boolean";
            break;
        case java.sql.Types.INTEGER:
            type = "xsd:integer";
            break;
        case java.sql.Types.DECIMAL:
            type = "xsd:decimal";
            break;
        case java.sql.Types.DOUBLE:
            type = "xsd:double";
            break;
        case java.sql.Types.FLOAT:
            type = "xsd:float";
            break;
        case java.sql.Types.DATE:
            type = "xsd:date";
            break;
        case java.sql.Types.TIME:
            type = "xsd:time";
            break;
        case java.sql.Types.TIMESTAMP:
            type = "xsd:dateTime";
            break;
        case java.sql.Types.VARCHAR:
        case java.sql.Types.CHAR:
        default:
            type = "xsd:string";
        }
        return type;
    }
    
    private Element getElementByName(Element e, String elementName) {
        if (e.getAttribute("name").equalsIgnoreCase(elementName)) {
            return e;
        }
        NodeList list = e.getChildNodes();
        Element el = null;
        Element e2 = null;
        for (int i = 0; i < list.getLength(); i++) {
            if (e2 == null) {
                Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    el = (Element) n;
                    if (el.getAttribute("name").equalsIgnoreCase(elementName)) {
                        e2 = el;
                        break;
                    } else {
                        e2 = getElementByName(el, elementName);
                        if ((e2 != null) && (e2.getAttribute("name").equalsIgnoreCase(elementName))) {
                            return e2;
                        }
                    }
                }
            } else {
                break;
            }
        }
        return e2;
    }
    
    private void modifyPartnerLink() {
        List<UnknownExtensibilityElement> l = def.getExtensibilityElements();
        UnknownExtensibilityElement plinkType = l.get(0);
        
        // set plinkType name
        plinkType.getElement();
        String plinkName = plinkType.getElement().getAttribute("name");
        plinkType.getElement().setAttribute("name", engineFileName + "_" + plinkName);
        
        // set plink:role name and portType
        NodeList nl = plinkType.getElement().getChildNodes();
        Element plinkRole = (Element) nl.item(1);
        plinkRole.setAttribute("name", engineFileName + "_" + plinkRole.getAttribute("name"));
        
        String temp = plinkRole.getAttribute("portType").substring("tns:".length());
        plinkRole.setAttribute("portType", "tns:" + engineFileName + "_" + temp);
        
    }
    
    private void modifyName() {
        QName q = def.getQName();
        q = new QName(q.getNamespaceURI(), engineFileName);
        def.setQName(q);
    }
    
    /**
     * sets the service name and SOAP address
     */
    @SuppressWarnings("unchecked")
    private void modifyServices() {
        Map<QName, Service> m = def.getServices();
        Iterator<QName> iterator = m.keySet().iterator();
        while (iterator.hasNext()) {
            QName key = iterator.next();
            Service s = m.get(key);
            
            QName qn = new QName(key.getNamespaceURI(), engineFileName + "_" + key.getLocalPart());
            
            s.setQName(qn);
            Port p = s.getPort("edmPort");
            p.setName(engineFileName + "_" + p.getName());
            List<SOAPAddress> l = p.getExtensibilityElements();
            Iterator iterator2 = l.iterator();
            while (iterator2.hasNext()) {
                SOAPAddress element = (SOAPAddress) iterator2.next();
                String loc = element.getLocationURI() + "/" + engineFileName;
                element.setLocationURI(loc);
                
            }
        }
        
    }
    
    /**
     * this sets the portType name according to the given edm engine file
     */
    @SuppressWarnings("unchecked")
    private void modifyPortTypes() {
        
         
        
        Map<QName, PortType> m = def.getPortTypes();
        Iterator<QName> iterator = m.keySet().iterator();
        while (iterator.hasNext()) {
            QName key = iterator.next();
            PortType pt = m.get(key);
            
            //if engine input params is empty, then operation has no inputMsg
//           // if (getEngineInputParams().isEmpty()) {
//                List ops = pt.getOperations();
//                for (int i = 0; i < ops.size(); i++) {
//                    Operation op = (Operation) ops.get(i);
//                    op.setInput(op.getInput());
//                   // op.setInput(null);
//                }
//            //}
            
            
            QName qn = new QName(key.getNamespaceURI(), engineFileName + "_" + key.getLocalPart());
            
            pt.setQName(qn);
        }
        
    }
    
    /**
     * reads an edm wsdl template file and genarates the javax.wsdl.Definition
     *
     * @return Definition
     * @throws WsdlGenerateException
     */
    private Definition getWsdlTemplate() throws WsdlGenerateException {
        
        Definition def = null;
        
        
        WSDLReader reader = factory.newWSDLReader();
        
        try {
            URL u = WsdlGenerator.class.getResource("edm.wsdl.template");
            String wsdlURI = u.getFile().indexOf(".jar") > 0 ? "jar:" + u.getFile() : u.getFile();
            def = reader.readWSDL(wsdlURI);
        } catch (WSDLException e) {
            logger.info(e.getMessage());
            throw new WsdlGenerateException(e);
        }
        return def;
    }
    
    /**
     * initialize the WSDLFactory
     *
     * @throws WsdlGenerateException
     */
    private static void initFactory() {
        if (factory == null) {
            try {
                factory = WSDLFactory.newInstance();
            } catch (WSDLException e) {
                logger.info(e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        //File f = new File("test/xxx_engine.xml");
        File f = new File(args[0]+"_engine.xml");
        
        WsdlGenerator wg = new WsdlGenerator(f, args[0]+"_engine", "./test");
        try {
            wg.generateWsdl();
        } catch (WsdlGenerateException e) {
            e.printStackTrace();
        }
        
    }
    
}
