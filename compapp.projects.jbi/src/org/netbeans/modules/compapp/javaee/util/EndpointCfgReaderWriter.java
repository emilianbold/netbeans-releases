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
package org.netbeans.modules.compapp.javaee.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.compapp.javaee.codegen.model.Endpoint;
import org.netbeans.modules.compapp.javaee.codegen.model.EndpointCfg;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author gpatil
 */
public class EndpointCfgReaderWriter {
    //private static final String JAVAEE_EP_FILE  = "javaee-ep-cfg.xml";//NOI18N
    private static final String JAVAEE_EP_FILE_PFX  = "java-cfg-";//NOI18N
    private static final String JAVAEE_EP_FILE_SFX  = ".xml";//NOI18N
    private static final String JAVAEE_PROJECTS = "javaee-projects"; //NOI18N
    private static final String JAVAEE_PROJECT = "javaee-project"; //NOI18N
    private static final String PROVIDES = "provides"; //NOI18N
    private static final String CONSUMES = "consumes"; //NOI18N
    private static final String EP_PREFIX = "javaee_"; //NOI18N
    private static final String EP_NAME = "endpoint-name"; //NOI18N
    private static final String INT_NAME = "interface-name"; //NOI18N
    private static final String SVC_NAME = "service-name"; //NOI18N    
    private static final String ENABLE_FOR_NMR = "enable-for-nmr"; //NOI18N    
    private static final String ENABLE_FOR_JAVAEE_HTTP = "enable-for-javaee-http"; //NOI18N    

    private static final String JBI_DEFAULT_NS = "http://java.sun.com/xml/ns/jbi/javaee/config/1/"; //NOI18N
    private static final String ELEM_SERVICES = "services"; //NOI18N
    private static final String MAPPING_PREFIX = "javaee_"; //NOI18N
    
    public static synchronized List<EndpointCfg> readConfigs(File cfgDir, String javaeeProjName) {
        List<EndpointCfg> ret = null;
        if (cfgDir != null){
            FileUtil.refreshFor(cfgDir);
            File epCfgFile = new File(cfgDir, JAVAEE_EP_FILE_PFX + javaeeProjName + JAVAEE_EP_FILE_SFX);
            
            if (epCfgFile.exists() && epCfgFile.isFile() && (epCfgFile.length() > 10)){                
                JbiXmlReader reader = new JbiXmlReader();
                try {
                    reader.read(epCfgFile);
                    ret = reader.getEpCfgs();
                } catch (ParserConfigurationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return ret;
    }

    private static Map<String, String> getNamespacePrefixes(List<EndpointCfg> epts) {
        Map<String, String> ret = new HashMap<String, String>();
        String tns = null;
        int counter = 0;
        Iterator<EndpointCfg> itr = epts.iterator();
        Endpoint ept = null;
        QName qn = null;

        while (itr.hasNext()) {
            ept = itr.next();
            qn = ept.getInterfaceName();
            if (qn != null) {
                tns = qn.getNamespaceURI();
                if (tns != null) {
                    if (ret.get(tns) == null) {
                        ret.put(tns, "ns" + counter++); //NOI18N
                    }
                }
            }

            qn = ept.getServiceName();
            if (qn != null) {
                tns = qn.getNamespaceURI();
                if (tns != null) {
                    if (ret.get(tns) == null) {
                        ret.put(tns, "ns" + counter++); //NOI18N
                    }
                }
            }
        }
        return ret;
    }
    

//    private static void writeJavaEEProjConfigs(OutputStream os, Map<String, List<EndpointCfg>> prjEpsMap) throws IOException {
    private static void writeJavaEEProjConfigs(OutputStream os, String projName, List<EndpointCfg> eptCfgs) throws IOException {    
        try {            
            Map<String, String> ns = getNamespacePrefixes(eptCfgs);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element elemProj = document.createElement(JAVAEE_PROJECT);

            elemProj.setAttribute("xmlns", JBI_DEFAULT_NS); //NOI18N
            String nsPrefix = null;
            Iterator<Map.Entry<String, String>> itr = null;
            Map.Entry entry = null;            
            
            itr = ns.entrySet().iterator();
            while (itr.hasNext()) {
                entry = itr.next();
                nsPrefix = "xmlns:" + (String) entry.getValue(); //NOI18N
                elemProj.setAttribute(nsPrefix, (String) entry.getKey());
            }
            elemProj.setAttribute("name", projName);//NOI18N
            document.appendChild(elemProj);
                
            Iterator<EndpointCfg> itrEpts = null;
            EndpointCfg ept = null;
            String prefix = null;

            Element elemSvcs = null;
            Element elemConsumesProvides = null;

            elemSvcs = document.createElement(ELEM_SERVICES);
            elemProj.appendChild(elemSvcs);

            itrEpts = eptCfgs.iterator();
            // Order is important for schematically valid jbi.xml file.
            // Provider should come before consumers
            while (itrEpts.hasNext()) {
                ept = itrEpts.next();
                if (ept.getEndPointType().equals(Endpoint.EndPointType.Provider)) {
                    elemConsumesProvides = document.createElement(PROVIDES); //NOI18N

                    //elemConsumesProvides.setAttribute(ENABLE_FOR_NMR, ept.isActivateForNMR() ? "true" : "false"); //NOI18N
                    elemConsumesProvides.setAttribute(ENABLE_FOR_JAVAEE_HTTP, ept.isActivateJavaEEHttpPort()? "true" : "false"); //NOI18N

                    elemConsumesProvides.setAttribute(EP_NAME, MAPPING_PREFIX + ept.getEndPointName()); 
                    prefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(INT_NAME, prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                    prefix = ns.get(ept.getServiceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(SVC_NAME, prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    elemSvcs.appendChild(elemConsumesProvides);
                }
            }

            itrEpts = eptCfgs.iterator();
            while (itrEpts.hasNext()) {
                ept = itrEpts.next();
                if (ept.getEndPointType().equals(Endpoint.EndPointType.Consumer)) {
                    elemConsumesProvides = document.createElement(CONSUMES); //NOI18N

                    elemConsumesProvides.setAttribute(ENABLE_FOR_NMR, ept.isActivateForNMR() ? "true" : "false"); //NOI18N
                    //elemConsumesProvides.setAttribute(ENABLE_FOR_JAVAEE_HTTP, ept.isActivateJavaEEHttpPort()? "true" : "false"); //NOI18N                        

                    elemConsumesProvides.setAttribute(EP_NAME, MAPPING_PREFIX + ept.getEndPointName()); //NOI18N
                    prefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(INT_NAME, prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                    prefix = ns.get(ept.getServiceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(SVC_NAME, prefix + ":" + ept.getServiceName().getLocalPart());
                    elemSvcs.appendChild(elemConsumesProvides);
                }
            }
                
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(os);

            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml"); //NOI18N
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes"); //NOI18N
            // indent the output to make it more legible...
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
            transformer.transform(source, result);
            os.flush();
            
        } catch (Exception ex) {
            // IllegalArgumentException DOMException
            // TransformerConfigurationException
            // ParserConfigurationException
            // TransformerFactoryConfigurationError
            // TransformerException
            ex.printStackTrace();
            throw new IOException("Exception while writing jbi.xml:" + ex.getMessage());
        }
    }
    
    public static synchronized void writeConfigs(File cfgDir, String javaEEProjName, List<EndpointCfg> cfgs) {               
        if (cfgDir != null){
            FileUtil.refreshFor(cfgDir);
            File epCfgFile = new File(cfgDir, JAVAEE_EP_FILE_PFX + 
                    javaEEProjName + JAVAEE_EP_FILE_SFX);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(epCfgFile);
                writeJavaEEProjConfigs(fos, javaEEProjName, cfgs);                    
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);                
            } finally {
                if (fos != null){
                    try {
                        fos.close();
                    } catch (Exception ex){
                        //Ignore.
                    }
                }
            }            
        }         
    }

    
    static class JbiXmlReader extends DefaultHandler {
        private List<EndpointCfg> eps = new ArrayList<EndpointCfg>();
        private Map<String, String> prefix2URI = new HashMap<String, String>();        
        private EndpointCfg ep = null;
        
        private String getEPName(String pfxdEP) {
            if (pfxdEP != null) {
                if (pfxdEP.startsWith(EP_PREFIX)) {
                    return pfxdEP.substring(7);
                }
            }
            return pfxdEP;
        }

        private void readNamespaces(Attributes attribs) {
            int i = attribs.getLength();
            String qName = null;
            String value = null;

            for (int j = 0; j < i; j++) {
                qName = attribs.getQName(j);
                if ((qName != null) && (qName.startsWith("xmlns:"))) { //NOI18N
                    value = attribs.getValue(j);
                    prefix2URI.put(qName.substring(6), value);
                }
            }
        }

        private String getURI(String qName) {
            if (qName != null) {
                int idx = qName.indexOf(":"); //NOI18N
                if (idx > 0) {
                    return this.prefix2URI.get(qName.substring(0, idx));
                }
            }

            return null;
        }

        private String getLocalName(String qName) {
            if (qName != null) {
                int idx = qName.indexOf(":"); //NOI18N
                if (idx > 0) {
                    return qName.substring(idx + 1);
                }
            }

            return qName;
        }

        private EndpointCfg getEndpoint(Attributes attribs, Endpoint.EndPointType type) {
            EndpointCfg e = new EndpointCfg();
            e.setEndPointType(type);
            String value = null;

            int i = attribs.getLength();
            for (int j = 0; j < i; j++) {
                if (EP_NAME.equals(attribs.getQName(j))) {
                    e.setEndPointName(getEPName(attribs.getValue(j)));
                }

                if (INT_NAME.equals(attribs.getQName(j))) {
                    value = attribs.getValue(j);
                    e.setInterfaceName(new QName(getURI(value), getLocalName(value)));
                }

                if (SVC_NAME.equals(attribs.getQName(j))) {
                    value = attribs.getValue(j);
                    e.setServiceName(new QName(getURI(value), getLocalName(value)));
                }

                if (ENABLE_FOR_NMR.equals(attribs.getQName(j))) {
                    value = attribs.getValue(j);
                    // True by default. even if not present.
                    if (value != null) {
                        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")){ // NOI18N
                            e.setActivateForNMR(false);
                        }
                    }
                }

                if (ENABLE_FOR_JAVAEE_HTTP.equals(attribs.getQName(j))) {
                    value = attribs.getValue(j);
                    // True by default. even if not present.
                    if (value != null) {
                        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")){ // NOI18N
                            e.setActivateJavaEEHttpPort(false);
                        }
                    }
                }                
            }

            return e;
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (PROVIDES.equals(qName) || CONSUMES.equals(qName)) {
                if (ep != null) {
                    this.eps.add(ep);
                    this.ep = null;
                }                
            }            
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            this.prefix2URI.clear();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            //dispAttribs(attributes);
            if (PROVIDES.equals(qName)) {
                ep = getEndpoint(attributes, Endpoint.EndPointType.Provider);
            } else if (CONSUMES.equals(qName)) {
                ep = getEndpoint(attributes, Endpoint.EndPointType.Consumer);
            } else if (JAVAEE_PROJECT.equals(qName)) {
                readNamespaces(attributes);
                //this.projName = attributes.getValue("", "name");//NOI18N
            } else {
                ep = null;
            }
        }

        public void read(File jbiXmlFile) throws ParserConfigurationException, SAXException, IOException {
            SAXParserFactory fact = SAXParserFactory.newInstance();
            fact.setNamespaceAware(false);
            SAXParser parser = fact.newSAXParser();
            parser.parse(jbiXmlFile, this);

        }

        public void read(InputStream is) throws ParserConfigurationException, SAXException, IOException {
            SAXParserFactory fact = SAXParserFactory.newInstance();
            fact.setNamespaceAware(false);
            SAXParser parser = fact.newSAXParser();
            parser.parse(is, this);
        }

        public List<EndpointCfg> getEpCfgs() {
            return this.eps;
        }
    }
}
