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
package org.netbeans.modules.etl.project.anttasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.java.hulp.i18n.Logger;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.etl.engine.impl.ETLEngineImpl;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.RuntimeAttribute;
import java.util.HashMap;
import javax.wsdl.Message;
import javax.wsdl.Part;
import org.netbeans.modules.etl.project.Localizer;


/**
 * This class generates an ETL WSDL file given an ETL engine file name
 * 
 */
public class WsdlGenerator {

    private static Logger logger = Logger.getLogger(WsdlGenerator.class.getName());
    private static WSDLFactory factory;
    private String engineFileName;
    private String wsdlLocation;
    private Definition def;
    private File engineFile;
    private static transient final Logger mLogger = Logger.getLogger(WsdlGenerator.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
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
        this.engineFileName = f.getName().substring(0, f.getName().indexOf(".xml"));
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
     * generates the etl wsdl file and writes to disk
     * 
     * @return Definition
     * 
     * @throws WsdlGenerateException
     */
    public Definition generateWsdl() throws WsdlGenerateException {
        this.def = getWsdlTemplate();

        if (engineFileName == null) {
            throw new WsdlGenerateException("cannot generate wsdl file as engineFileName is null ");
        }
        if (wsdlLocation == null) {
            throw new WsdlGenerateException("cannot generate wsdl file as wsdlLocation is null ");
        }
        modifyWsdl();
        writeWsdl();
        return def;
    }

    /**
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws BaseException
     */
    private Map getEngineInputParams() {
        ETLEngineImpl engine = null;
        try {
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            Element element = df.newDocumentBuilder().parse(engineFile).getDocumentElement();
            engine = new ETLEngineImpl();
            engine.parseXML(element);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return engine.getInputAttrMap();
    }

    /**
     * persist the wsdl file to disk
     * 
     * @throws WsdlGenerateException
     */
    private void writeWsdl() throws WsdlGenerateException {
        try {

            WSDLWriter writer = factory.newWSDLWriter();
            Writer sink = new FileWriter(wsdlLocation + "/" + engineFileName + ".wsdl");

            writer.writeWSDL(def, sink);
            sink.flush();
            sink.close();
        } catch (Exception e) {
            mLogger.infoNoloc(mLoc.t("PRJS015: Exception{0}", e.getMessage()));
            throw new WsdlGenerateException(e);
        }

    }

    /**
     * modify the wsdl template
     */
    private void modifyWsdl() {
        try {
        modifyName();
        modifyMessages();
        modifyMessageTypes();
        modifyPortTypes();
        modifyBinding();
        modifyServices();
        modifyPartnerLink();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void modifyBinding() {
        //Binding b = def.getBinding(new QName(def.getTargetNamespace(), "Binding"));

        HashMap bMap = (HashMap) def.getBindings();
        java.util.Iterator keysIter = bMap.keySet().iterator();
        while (keysIter.hasNext()) {
            Binding b = (Binding) bMap.get(keysIter.next());
            if (b != null) {
        BindingOperation bo = b.getBindingOperation("execute", null, null);
        if (getEngineInputParams().isEmpty()) {
            bo.setBindingInput(null);
        }
            }
        }
    }

    private void modifyMessages() {
        Map msgs = def.getMessages();
        java.util.Iterator keysIter = msgs.keySet().iterator();
        while (keysIter.hasNext()) {
            Message msg = (Message) msgs.get(keysIter.next());
            if (msg != null) {
				modifyMessageElementName((Message) msg);
            }
        }
    }

    private void modifyMessageElementName(Message message) {
        String msgLocalName = message.getQName().getLocalPart();
        if (message != null) {
            QName qname = new QName("http://com.sun.jbi/etl/etlengine", engineFileName + "_" + msgLocalName);
            message.setQName(qname);
        }
		Part p = message.getPart("part");
		QName pqname = new QName("http://com.sun.jbi/etl/etlengine", engineFileName + "_" + p.getElementName().getLocalPart());
		p.setElementName(pqname);
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
                type = "xsd:datetime";
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
            Port p = s.getPort("etlPort");
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
     * this sets the portType name according to the given etl engine file
     */
    @SuppressWarnings("unchecked")
    private void modifyPortTypes() {

        ;

        Map<QName, PortType> m = def.getPortTypes();
        Iterator<QName> iterator = m.keySet().iterator();
        while (iterator.hasNext()) {
            QName key = iterator.next();
            PortType pt = m.get(key);

            //if engine input params is empty, then operation has no inputMsg
            if (getEngineInputParams().isEmpty()) {
                List ops = pt.getOperations();
                for (int i = 0; i < ops.size(); i++) {
                    Operation op = (Operation) ops.get(i);
                    op.setName("execute");
                    op.setInput(null);
                }
            }


            QName qn = new QName(key.getNamespaceURI(), engineFileName + "_" + key.getLocalPart());

            pt.setQName(qn);
        }

    }

    /**
     * reads an etl wsdl template file and genarates the javax.wsdl.Definition
     * 
     * @return Definition
     * @throws WsdlGenerateException
     */
    private Definition getWsdlTemplate() throws WsdlGenerateException {

        Definition def = null;
        WSDLReader reader = factory.newWSDLReader();

        try {
            URL u = WsdlGenerator.class.getResource("etl.wsdl.template");
            String wsdlURI = u.getFile().indexOf(".jar") > 0 ? "jar:" + u.getFile() : u.getFile();
            def = reader.readWSDL(wsdlURI);
        } catch (WSDLException e) {
            mLogger.infoNoloc(mLoc.t("PRJS016: Exception{0}", e.getMessage()));
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
                mLogger.infoNoloc(mLoc.t("PRJS017: Exception{0}", e.getMessage()));
            }
        }
    }

    public static void main(String[] args) {
        File f = new File("test/xxx_engine.xml");

        WsdlGenerator wg = new WsdlGenerator(f, "xxx_engine", "test");
        try {
            wg.generateWsdl();
        } catch (WsdlGenerateException e) {
            e.printStackTrace();
        }

    }
}
