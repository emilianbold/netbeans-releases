/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.project.anttasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.modules.xslt.project.anttasks.jbi.ServiceEntry;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.Invokes;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.PartnerLinkTypeReference;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.project.CommandlineXsltProjectXmlCatalogProvider;

/**
 *
 * @author Vitaly Bychkov
 * @author Sreenivasan Genipudi
 */
public class JBIGenerator {
    /**
     * Map of namespace to its prefix
     */
    private Map<String, String> mNameSpacePrefix = new HashMap<String, String>();

    // Member variable representing source directory
    /**
     * Source directory
     */
    private String mSourceDirectory = null;
    // Member variable representing build directory
    /**
     * Build directory
     */
    private String mBuildDirectory = null;    

    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(JBIGenerator.class.getName());    

    private List<ServiceEntry> mProviders = new ArrayList<ServiceEntry>();
    
    private List<ServiceEntry> mConsumers = new ArrayList<ServiceEntry>();
    
    //Constants used in generating JBI.XML
    /**
     * Constant
     */
    public static final String JBI_ELEM_NAME = "jbi"; // NOI18N
    /**
     * Constant
     */
    public static final String SERVICES_ELEM_NAME = "services"; // NOI18N
    /**
     * Constant
     */
    public static final String PROVIDES_ELEM_NAME = "provides"; // NOI18N
    /**
     * Constant
     */
    public static final String CONSUMES_ELEM_NAME = "consumes"; // NOI18N
    /**
     * Constant
     */
    public static final String BINDING_ATTR_NAME = "binding-component"; // NOI18N
    /**
     * Constant
     */
    public static final String INTERFACE_ATTR_NAME = "interface-name"; // NOI18N
    /**
     * Constant
     */
    public static final String ENDPOINT_ATTR_NAME = "endpoint-name"; // NOI18N
    /**
     * Constant
     */
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
    public static final String COLON_SEPARATOR = ":"; // NOI18N

    private List<File> myDependentProjectDirs;
    private List<File> mySourceDirs;
    
    public JBIGenerator(List<File> depedentProjectDirs , List<File> sourceDirs, String srcDir, String buildDir) {
        myDependentProjectDirs = depedentProjectDirs;
        mySourceDirs = sourceDirs;
        mSourceDirectory = srcDir;
        mBuildDirectory = buildDir;
    }
    
    /**
     * Set the build directory
     * @param buildDir build directory
     */
    public void setBuildDirectory(String buildDir) {
        mBuildDirectory = buildDir;
    }

    /**
     * Get the build directory
     * @return String value of the build directory
     */
    public String getBuildDirectory() {
        return mBuildDirectory;
    }

    /**
     * Set the source directory
     * @param srcDir source directory
     */
    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }
    
    /**
     * Get the source directory
     * @return String value of the source directory
     */
    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    /**
     * Generate JBI.xml
     */
    public void generate() { 
        if (mBuildDirectory == null || mSourceDirectory == null) {
            throw new BuildException("No directory is set for build || source files.");
        }
        
        if (isOldProject()) {
            OldProjectTransformer oldProjectTransformer = 
                    new OldProjectTransformer(getSourceDirectory(), getBuildDirectory());
            oldProjectTransformer.execute();
        }
        
        process();
        try {
            generateJBIDescriptor();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    // TODO m | r
    private boolean isOldProject() {
        File tMapFile = getTransformmapFile();
        return tMapFile == null || !tMapFile.isFile();
    }
    
    protected TMapModel getTMapModel() {
        File transformmapFile = getTransformmapFile();
        if (transformmapFile == null) {
            logger.log(Level.SEVERE, "Error encountered while processing transformmap file - "+transformmapFile.getAbsolutePath());
            throw new RuntimeException("Can't find transformation descriptor");
        }
        TMapModel tMapModel = null;
        try {
            tMapModel = TransformmapCatalogModel.getDefault().getTMapModel(transformmapFile.toURI());
        }catch (Exception ex) {
            this.logger.log(java.util.logging.Level.SEVERE, "Error while creating Tramsformap Model ", ex);
            throw new RuntimeException("Error while creating Transformmap Model ",ex);
        }
        
        if (tMapModel == null 
                || !TMapModel.State.VALID.equals(tMapModel.getState())) 
        {
            this.logger.log(java.util.logging.Level.SEVERE, "Error while creating Transformmap Model - "+(tMapModel == null ? " is null" : " is not valid"));
            throw new RuntimeException("Error while creating Transformmap Model ");
        }
        return tMapModel;
    }
    
    private void process() {
        TMapModel tMapModel = getTMapModel();
        try {
            populateProviderConsumer(tMapModel);
        }catch (Exception ex) {
            logger.log(Level.SEVERE, "Error encountered while processing transformmap model");
            throw new RuntimeException(ex);
        }
////
////        
////        
////        
////        Document document = null;
////        if (transformmapFile != null) {
////            document = XmlUtil.getDocument(transformmapFile);
////        }
////        
////        if (document != null) {
////            NodeList operationNodeList = document.getElementsByTagName(TMapComponents.OPERATION.getTagName());
////            System.out.println("operationNodeList: "+operationNodeList);
////            
////            if (operationNodeList != null && operationNodeList.getLength() > 0) {
////                System.out.println("inside operationNodeList");
////                populateProviderServices(transformmapFile.getParentFile(), operationNodeList);
////            }
////            
////            NodeList invokesNodeList = document.getElementsByTagName(TMapComponents.INVOKES.getTagName());
////            if (invokesNodeList != null && invokesNodeList.getLength() > 0) {
////                populateConsumerServices(transformmapFile.getParentFile(), invokesNodeList);
////            }
////        }
    }
    
    private ServiceEntry createServiceEntry(PartnerLinkTypeReference pltRefComponent) 
    {
        if (pltRefComponent == null) {
            return null;
        }
        
        WSDLReference<PartnerLinkType> pltRef = pltRefComponent.getPartnerLinkType();
        WSDLReference<Role> roleRef = pltRefComponent.getRole();

        if (pltRef == null || roleRef == null) {
            return null;
        }
        ServiceEntry entry = null;
        
        // TODO m
        QName pltQname = pltRef.getQName();
        if (pltQname == null) {
            return null;
        }
        
        String pltNS = pltQname.getNamespaceURI();
        String pltName = pltQname.getLocalPart();
        String pltNSPrefix = populateNamespace(pltNS);

//        PartnerLinkType plt = pltRef.get();
//        if (plt == null) {
//            logger.log(Level.SEVERE, "Problem encountered while processing partnerLinkType of   \""+pltName+"\"");
//            throw new RuntimeException("PartnerLink Type is Null!");
//        }
        
        String portName = null;
        String portNameNS = null;
        String portNameNSPrefix = null;
        QName portNameQname = null;
        
        String roleName = null;
        
        Role role = roleRef == null ? null : roleRef.get();
        if (role == null) {
            return null;
        }
        
        roleName = role.getName();
        NamedComponentReference<PortType> portTypeRef = role.getPortType();
        
        if (portTypeRef != null ) {
            PortType pt = portTypeRef.get();
            if (pt != null) {
                portName = pt.getName();
                portNameNS = pt.getModel().getDefinitions().getTargetNamespace();
                portNameNSPrefix = populateNamespace(portNameNS);
                portNameQname = portTypeRef.getQName();
            }
        }
        
        if (portName == null) {
            logger.log(Level.SEVERE, "Problem encountered while processing portType   PartnerLink =  \""+pltName+"\"");
            throw new RuntimeException("Problem encountered while processing portType !");
        }
        
        entry = new ServiceEntry(
                pltName,
                portName,
                pltNS,
                portNameNS,
                roleName,
                pltNSPrefix,
                portNameNSPrefix,
                pltQname,
                portNameQname
                );
        
        
        return entry;
    }
    
    /**
     * Populate providers/consumers from transformmap model
     */
    private void populateProviderConsumer(TMapModel tMapModel)  {
        ServiceEntry provider = null;
        ServiceEntry consumer = null;
        
        List<Service> services = tMapModel.getTransformMap().getServices();
        if (services == null) {
            return;
        }
        
        for (Service service : services) {
            provider = createServiceEntry(service);
            if (provider != null && !mProviders.contains(provider)) {
                mProviders.add(provider);
            }

            List<Operation> operations = service.getOperations();
            if (operations == null) {
                continue;
            }
            for (Operation operation : operations) {
                List<Invokes> invokess = operation.getInvokess();
                if (invokess == null) {
                    continue;
                }
                
                for (Invokes invokes : invokess) {
                    if (invokes != null) {
                        consumer = createServiceEntry(invokes);
                        if (consumer != null && !mConsumers.contains(consumer)) {
                            mConsumers.add(consumer);
                        }                    
                    }
                }
            }
        }
    }
    
    private void generateJBIDescriptor() throws IOException {

        FileOutputStream fos = null;
        try
        {
            StringBuffer sb = new StringBuffer();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            sb.append("<jbi version=\"1.0\"\n");
            sb.append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            sb.append("        xmlns=\"http://java.sun.com/xml/ns/jbi\"\n");
            sb.append("        xsi:schemaLocation=\"http://java.sun.com/xml/ns/jbi jbi.xsd\"\n");

            
            int nss = mNameSpacePrefix.size();
            int i = 0;
            Set<String> nsUris = mNameSpacePrefix.keySet();
            for (String nsUri : nsUris) {
                sb.append("        xmlns:" + mNameSpacePrefix.get(nsUri) + "=\"" + nsUri + "\"");
                if(i < nss - 1) {
                    sb.append("\n");
                }
                i++;
            }
            
            sb.append(">\n");
            sb.append("    <services binding-component=\"false\">\n");

            if (mProviders != null) {
                for (int j = 0; j < mProviders.size(); j++) {
                    ServiceEntry tmpService = mProviders.get(j); 
                    sb.append("        <provides interface-name=\"" + getColonedQName(tmpService.getPortNameQname(), mNameSpacePrefix));
                    sb.append("\" service-name=\"" + getColonedQName(tmpService.getPartnerLinkNameQname(), mNameSpacePrefix));
                    sb.append("\" endpoint-name=\"" + tmpService.getRoleName());
                    sb.append("\"/>\n");

                }
            }

            if (mConsumers != null) {
                for (int j = 0; j < mConsumers.size(); j++) {
                    ServiceEntry tmpService = mConsumers.get(j); 
                    sb.append("        <consumes interface-name=\"" + getColonedQName(tmpService.getPortNameQname(), mNameSpacePrefix));
                    sb.append("\" service-name=\"" + getColonedQName(tmpService.getPartnerLinkNameQname(), mNameSpacePrefix));
                    sb.append("\" endpoint-name=\"" + tmpService.getRoleName());
                    sb.append("\" link-type=\"standard\"/>\n");
                }
            }

            sb.append("    </services>\n");
            sb.append(" </jbi>\n");
            String content = sb.toString();
            fos = new FileOutputStream(getJbiFile());
            store(content.getBytes("UTF-8"), fos);
        } finally {
            if (fos != null) {
                fos.close();
            } 
        }
    }

// TODO a    
//    private void generateTransformmapFromXsltMap() {
//        File xsltMapFile = getFileInSrc(XsltproConstants.XSLTMAP_XML);
//    
//        if (xsltMapFile != null && xsltMapFile.isFile()) {
//            
//        }
//    }
    
    private File getFileInSrc(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        String srcDir = getSourceDirectory();
        if (srcDir == null || "".equals(srcDir)) {
            throw new BuildException("source directory shouldn't be null or empty");
        }
        
        File file = new File(srcDir+"/"+fileName);
       
        return file;
    }
    
    protected File getTransformmapFile() {        
        File transformmapFile = getFileInSrc(XsltproConstants.TRANSFORMMAP_XML);
        if (transformmapFile == null || !transformmapFile.isFile()) {
//TODO a            transformmapFile = generateTransformmapFromXsltMap();
        } 
        
        
        return transformmapFile;
    }
    
    private File getJbiFile() {
        String buildDir = getBuildDirectory();
        if (buildDir == null || "".equals(buildDir)) {
            throw new BuildException("build directory shouldn't be null or empty");
        }
        
        File jbiFile = new File(buildDir+"/META-INF/jbi.xml");
        return jbiFile;
    }

    /**
     * Collect the namespaces and generate Prefix
     * @param namespaceURI
     * @return namespace prefix
     */
    private String populateNamespace(String namespaceURI) {
        if (namespaceURI == null || "".equals(namespaceURI)) {
            return null;
        }
        
        String namespacePrefix = null;
        namespacePrefix =(String) mNameSpacePrefix.get(namespaceURI);
        
        if (namespacePrefix == null){
            namespacePrefix = NAMESPACE_PREFIX+mNameSpacePrefix.size();
            mNameSpacePrefix.put(namespaceURI,namespacePrefix);
        }
        return namespacePrefix;
    }
    
    private static String getColonedQName(QName qn, Map nsTable)
    {
        String ns = qn.getNamespaceURI();
        String prefix = (String)nsTable.get(ns);
        if(prefix == null)
            return qn.getLocalPart();
        else
            return prefix + COLON_SEPARATOR + qn.getLocalPart();
    }

    private static QName getQName(String qname)
    {
        return QName.valueOf(qname);
    }

    private static void store(byte input[], OutputStream output)
        throws IOException
    {

        ByteArrayInputStream in = new ByteArrayInputStream(input);
        byte buf[] = new byte[4096];
        for(int n = 0; (n = in.read(buf)) != -1;) {
            output.write(buf, 0, n);
        }

        output.flush();
    }

    public static void main(String[] args) {
        GenerateJBIDescriptor dd = new GenerateJBIDescriptor();

    }
}
