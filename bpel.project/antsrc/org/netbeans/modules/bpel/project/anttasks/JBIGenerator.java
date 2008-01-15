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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.namespace.QName;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.apache.tools.ant.BuildException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.bpel.project.portmap.DataWriter;

import org.netbeans.modules.bpel.project.anttasks.jbi.Consumer;
import org.netbeans.modules.bpel.project.anttasks.jbi.Provider;

/**
 * Generates JBI.xml
 * @author Sreenivasan Genipudi
 */
public class JBIGenerator {
    private Logger logger = Logger.getLogger(JBIGenerator.class.getName());
    private static final String PARTNER_ROLE = "partnerRole";
    private static final String MY_ROLE = "myRole";
    private List mDepedentProjectDirs;
    private List mSourceDirs;
    
    private File mBuildDir = null;
    private List mProviderList = new ArrayList();
    private List mConsumerList = new ArrayList();
    private Map mNameSpacePrefix = new HashMap();
    
    public static final String JBI_ELEM_NAME = "jbi"; // NOI18N
    public static final String SERVICES_ELEM_NAME = "services"; // NOI18N
    public static final String PROVIDES_ELEM_NAME = "provides"; // NOI18N
    public static final String CONSUMES_ELEM_NAME = "consumes"; // NOI18N
    public static final String BINDING_ATTR_NAME = "binding-component"; // NOI18N
    public static final String INTERFACE_ATTR_NAME = "interface-name"; // NOI18N
    public static final String ENDPOINT_ATTR_NAME = "endpoint-name"; // NOI18N
    public static final String SERVICE_ATTR_NAME = "service-name"; // NOI18N
    
    public static final String VERSION_ATTR_NAME = "version"; // NOI18N
    public static final String VERSION_ATTR_VALUE = "1.0"; // NOI18N
    public static final String NS_ATTR_NAME = "xmlns";  // NOI18N
    public static final String NS_ATTR_VALUE="http://java.sun.com/xml/ns/jbi"; // NOI18N
    public static final String NS_XSI_ATTR_NAME = "xmlns:xsi";  // NOI18N
    public static final String NS_XSI_ATTR_VALUE ="http://www.w3.org/2001/XMLSchema-instance"; // NOI18N
    public static final String XSI_ATTR_NAME = "xsi:schemaLocation"; // NOI18N
    public static final String XSI_ATTR_VALUE ="http://java.sun.com/xml/ns/jbi jbi.xsd"; // NOI18N
    
    public static final String NAMESPACE_PREFIX = "ns"; // NOI18N
    
    public JBIGenerator() {}

    public JBIGenerator(List depedentProjectDirs , List sourceDirs) {
        this.mDepedentProjectDirs = depedentProjectDirs;
        this.mSourceDirs = sourceDirs;
    }

    void process() {
        if (this.mSourceDirs != null
                && this.mSourceDirs.size() != 0) {
            processSourceDirs(this.mSourceDirs);
        }
    }

    public void generate(File buildDir) {
        this.mBuildDir = buildDir;
        process();

        try {
            File cnfFile = new File(buildDir, "META-INF");
            if (!cnfFile.exists()) {
                cnfFile.mkdirs();
            }
            File jbiFile = new File(cnfFile, "jbi.xml");
            
            generateJbiXml(jbiFile);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to create jbi.xml", ex);
        }
    }

    private void addNamespaceToRoot(Element root) {
        Set nameSpaceSet = this.mNameSpacePrefix.entrySet();
        Iterator itr = nameSpaceSet.iterator();
        while(itr.hasNext()) {
            Map.Entry entry = (Map.Entry)  itr.next();
            root.setAttribute(NS_ATTR_NAME+":"+(String)entry.getValue(),(String)entry.getKey() );
        }
    }

    private void generateJbiXml(File jbiFile) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument(); // Create from whole cloth
        
        Element root = (Element) document.createElement(JBI_ELEM_NAME);
        root.setAttribute(VERSION_ATTR_NAME, VERSION_ATTR_VALUE);
        root.setAttribute(NS_ATTR_NAME, NS_ATTR_VALUE);
        // Based on the provider/ consumer elements add their namespaces to the 
        // root element
        addNamespaceToRoot(root);
        
        Map map = new HashMap();
        document.appendChild(root);
        
        // add services
        Element services = (Element) document.createElement(SERVICES_ELEM_NAME);
        services.setAttribute(BINDING_ATTR_NAME, "false"); // NOI18N
        root.appendChild(services);
        
        // add provides
        int psize = this.mProviderList.size();
        
        for (int i=0; i<psize; i++) {
            Provider provider = (Provider) mProviderList.get(i);
            if (provider != null) {
                //  String PLT = portMap.getPartnerLinkType().toString();
                String PT = provider.getPartnerLinkName();
                if (PT != null) {
                    Element portMapNode = (Element) document.createElement(PROVIDES_ELEM_NAME);
                    portMapNode.setAttribute(
                            INTERFACE_ATTR_NAME,
                            provider.getPortNameNamespacePrefix()+":"+provider.getPortName()
                            );
                    portMapNode.setAttribute(
                            SERVICE_ATTR_NAME,
                            provider.getPartnerLinkNamespacePrefix()+":"+provider.getPartnerLinkName()
                            );
                    portMapNode.setAttribute(
                            ENDPOINT_ATTR_NAME, provider.getMyRoleName() + "_" + MY_ROLE
                            );
                    
                    services.appendChild(portMapNode);
                }
            }
        }
        
        // add consumes
        int csize = this.mConsumerList.size();
        for (int i=0; i<csize; i++) {
            Consumer consumer = (Consumer) mConsumerList.get(i);
            if (consumer != null) {
                //  String PLT = portMap.getPartnerLinkType().toString();
                String PT = consumer.getPartnerLinkName();
                if (PT != null) {
                    Element portMapNode = (Element) document.createElement(CONSUMES_ELEM_NAME);
                    portMapNode.setAttribute(
                            INTERFACE_ATTR_NAME,
                            consumer.getPortNameNamespacePrefix()+":"+consumer.getPortName()
                            );
                    portMapNode.setAttribute(
                            SERVICE_ATTR_NAME,
                            consumer.getPartnerLinkNamespacePrefix()+":"+consumer.getPartnerLinkName()
                            );
                    portMapNode.setAttribute(
                            ENDPOINT_ATTR_NAME, consumer.getPartnerRoleName() + "_" + PARTNER_ROLE
                            );
                    
                    services.appendChild(portMapNode);
                }
            }
        }
        
        // add namespaces...
        String key = null;
        String value = null;
        Iterator iterator = map.keySet().iterator();
        
        while ((iterator != null) && (iterator.hasNext() == true)) {
            key = (String) iterator.next();
            
            if (key != null) {
                value = (String) map.get(key);
                root.setAttribute("xmlns:"+key, value);
            }
        }
        
        // File outputFile = new File(jbiFileLocation);
        PrintWriter pw =null;
        try {
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            pw = new PrintWriter(jbiFile, "UTF-8"); 
            StreamResult result = new StreamResult(pw);
            
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");   // NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");  // NOI18N
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");  // NOI18N
            
            transformer.transform(source, result);
        } catch (Exception ex) {
            
        }
        finally {
            if (pw != null) {
                try {
                    pw.flush();
                }catch (Exception ex) {
                    
                }

                try {
                    pw.close();
                }catch (Exception ex) {
                    
                }
            }
        }
    }
    
    private void processFileObject(File file) {
        if (file.isDirectory()) {
            processFolder(file);
        } else {
            processFile(file);
        }
    }

    private void processFolder(File fileDir) {
        File[] children = fileDir.listFiles();
        
        for (int i = 0; i < children.length; i++) {
            processFileObject(children[i]);
        }
    }
    
    protected void processFile(File file) {
        String fileName = file.getName();
        String fileExtension = null;
        int dotIndex = fileName.lastIndexOf('.');

        if(dotIndex != -1) {
            fileExtension = fileName.substring(dotIndex +1);
        }
        
        if (fileExtension != null && fileExtension.equalsIgnoreCase("bpel")) {
            BpelModel bpelModel = null;

            try {
                bpelModel = BPELCatalogModel.getDefault().getBPELModel(file.toURI());
            }
            catch (Exception ex) {
                this.logger.log(java.util.logging.Level.SEVERE, "Error while creating BPEL Model ", ex);
                throw new RuntimeException("Error while creating BPEL Model ",ex);
            }
            try {
                populateProviderConsumer(bpelModel);
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, "Error encountered while processing BPEL file - "+file.getAbsolutePath());
                throw new RuntimeException(ex);
            }
        }
    }
    
    private String populateNamespace(String namespaceURI) {
        String namespacePrefix = null;
        namespacePrefix =(String) mNameSpacePrefix.get(namespaceURI);
        if (namespacePrefix == null){
            namespacePrefix = NAMESPACE_PREFIX+mNameSpacePrefix.size();
            mNameSpacePrefix.put(namespaceURI,namespacePrefix);
        }
        return namespacePrefix;
    }

    void populateProviderConsumer(BpelModel bpelModel) {
        // vlv # 109292
        if (bpelModel == null) {
          return;
        }
        if (bpelModel.getProcess() == null) {
          return;
        }
        if (bpelModel.getProcess().getPartnerLinkContainer() == null) {
          return;
        }
        if (bpelModel.getProcess().getPartnerLinkContainer().getPartnerLinks() == null) {
          return;
        }
        PartnerLink[] pLinks = bpelModel.getProcess().getPartnerLinkContainer().getPartnerLinks();
        Provider provider = null;
        Consumer consumer = null;

        for (int index =0; index < pLinks.length; index++) {
            PartnerLink pLink = pLinks[index];
            String partnerLinkName = pLink.getName();
            WSDLReference partnerLinkTypeWSDLRef = pLinks[index].getPartnerLinkType();
            
            String partnerLinkQNameLocalPart = partnerLinkName;
            String partnerLinkNameSpaceURI = pLinks[index].getBpelModel().getProcess().getTargetNamespace();
            String partnerLinkNSPrefix = populateNamespace(partnerLinkNameSpaceURI);
            
            PartnerLinkType pLTypeForPLinkType = (PartnerLinkType)partnerLinkTypeWSDLRef.get();
            String portName = null;
            String portNameNS = null;
            String portNameNSPrefix = null;
            
            WSDLReference<Role> myRoleWSDLRef = pLinks[index].getMyRole();

            if (pLTypeForPLinkType == null) {
                logger.log(Level.SEVERE, "Problem encountered while processing partnerLinkType of \""+partnerLinkName+"\"");
                throw new RuntimeException("PartnerLink Type is Null!");
            }     

            if ( myRoleWSDLRef != null) {
                String myRoleName = null;
                
                Role myRole = myRoleWSDLRef.get();
                if (myRole != null) {
                    myRoleName = myRole.getName();
                    NamedComponentReference<PortType> portTypeRef = myRole.getPortType();
                 
                    if (portTypeRef != null ) {
                        PortType pt = portTypeRef.get();
                        if (pt != null) {
                            portName = pt.getName();
                            portNameNS = pt.getModel().getDefinitions().getTargetNamespace();
                            portNameNSPrefix = populateNamespace(portNameNS);
                        }
                    }
                }
                if (portName == null) {
                    logger.log(Level.SEVERE, "Problem encountered while processing portType   PartnerLink =  \""+partnerLinkName+"\"");
                    throw new RuntimeException("Problem encountered while processing portType !");
                }
                
                provider = new Provider(partnerLinkName, portName, partnerLinkNameSpaceURI, portNameNS,myRoleName,partnerLinkNSPrefix, portNameNSPrefix  );
                if (!mProviderList.contains(provider)) {
                    mProviderList.add(provider);
                }
            }
            WSDLReference<Role> myPartnerRoleRef = pLinks[index].getPartnerRole();

            if ( myPartnerRoleRef != null) {
                String partnerRoleName = null;
                Role  partnerRole= myPartnerRoleRef.get();

                if (partnerRole != null) {
                    partnerRoleName = partnerRole.getName();
                    
                    NamedComponentReference<PortType> portTypeRef = partnerRole.getPortType();
                    if (portTypeRef != null ) {
                        PortType pt = portTypeRef.get();
                        if (pt != null) {
                            portName = pt.getName();
                            portNameNS = pt.getModel().getDefinitions().getTargetNamespace();
                            portNameNSPrefix = populateNamespace(portNameNS);
                        }
                    }
                }
                if (portName == null) {
                    logger.log(Level.SEVERE, "Problem encountered while processing portType   PartnerLink =  \""+partnerLinkName+"\"");
                    throw new RuntimeException("Problem encountered while processing portType !");
                }
                consumer = new Consumer(partnerLinkName, portName, partnerLinkNameSpaceURI, portNameNS, partnerRoleName,partnerLinkNSPrefix, portNameNSPrefix   );
                if (! mConsumerList.contains(consumer)) {
                    this.mConsumerList.add(consumer);
                }
            }
        }
    }
    
    private void formatXml(File portMapFile) {
        try {
            DataWriter w = new DataWriter(XMLReaderFactory.createXMLReader());
            w.setIndentStep(1);
            StringWriter sWriter = new StringWriter();
            w.setOutput(sWriter);
            w.parse(new InputSource(new FileReader(portMapFile)));
            FileWriter writer = new FileWriter(portMapFile);
            writer.write(sWriter.getBuffer().toString());
            writer.flush();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Failed to format xml  "+ portMapFile.getPath(), ex);
            throw new BuildException("Failed to format xml  "+ portMapFile.getPath() + " \n" +  ex.getMessage());
        }
    }
    
    private void processSourceDirs(List sourceDirs) {
        Iterator it = sourceDirs.iterator();

        while(it.hasNext()) {
            File sourceDir = (File) it.next();
            processSourceDir(sourceDir);
            readAndPackageFromProjectCatalog(sourceDir);
        }
    }
    
    private void processSourceDir(File sourceDir) {
        processFileObject(sourceDir);
    }
    
    private void readAndPackageFromProjectCatalog(File sourceDir) {
        String projectCatalogLocation = new File(CommandlineBpelProjectXmlCatalogProvider.getInstance().getProjectWideCatalogForWizard()).getAbsolutePath();
        PackageCatalogArtifacts pa = new PackageCatalogArtifacts();
        pa.doCopy(projectCatalogLocation, this.mBuildDir );
        
    }
}
