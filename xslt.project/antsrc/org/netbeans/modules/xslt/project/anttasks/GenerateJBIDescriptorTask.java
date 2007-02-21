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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.lang.reflect.Method;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xslt.core.xsltmap.TransformationDescType;

import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.modules.xslt.project.anttasks.jbi.ServiceEntry;
import org.netbeans.modules.xslt.core.xsltmap.XmlUtil;
import org.netbeans.modules.xslt.core.xsltmap.XsltMapConst;
import org.netbeans.modules.xslt.core.xsltmap.util.Util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.namespace.QName;

/**
 * Ant task wrapper which invokes the JBI Generation task
 * @author Vitaly Bychkov
 * @author Sreenivasan Genipudi
 */
public class GenerateJBIDescriptorTask extends org.apache.tools.ant.Task {
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
    // Member variable representing project classpath
    /**
     * Project classpath
     */
    private String mProjectClassPath= null;
    /**
     * Custom classloader used to invoke the JBI Generation task
     */
    private AntClassLoader m_myClassLoader = null;
    /**
     * Classpath Reference
     */
    private Reference m_ref = null;
    
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(GenerateJBIDescriptorTask.class.getName());    

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

        
    /**
     * Constructor
     */
    public GenerateJBIDescriptorTask() {
    }
    
    /**
     * Set the classpath reference
     * @param ref Classpath Reference
     */
    public void setClasspathRef(Reference ref) {
        this.m_ref = ref;
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
     * Set the project classpath
     * @param projectClassPath Project classpath
     */
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
        
    
    /**
     * Invoke the task that generates the JBI.xml
     */
    public void execute() throws BuildException { 
        File xsltMapFile = getXsltMapFile();
        
        Document document = null;
        if (xsltMapFile != null) {
            document = XmlUtil.getDocument(xsltMapFile);
        }
        
        if (document != null) {
            NodeList inputNodeList = document.getElementsByTagName(TransformationDescType.INPUT.getTagName());
            if (inputNodeList != null && inputNodeList.getLength() > 0) {
                populateProviderServices(xsltMapFile.getParentFile(), inputNodeList);
            }
            NodeList outputNodeList = document.getElementsByTagName(TransformationDescType.OUTPUT.getTagName());
            if (outputNodeList != null && outputNodeList.getLength() > 0) {
                populateConsumerServices(xsltMapFile.getParentFile(), outputNodeList);
            }
        }
        
        try {
            generateJBIDescriptor();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void generateJBIDescriptor() throws IOException {
        FileOutputStream fos = null;
        try
        {
            StringBuffer sb = new StringBuffer();
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
    
    
    private File getXsltMapFile() {
        String srcDir = getSourceDirectory();
        if (srcDir == null || "".equals(srcDir)) {
            throw new BuildException("source directory shouldn't be null or empty");
        }
        
        File xsltMapFile = new File(srcDir+"/"+XsltproConstants.XSLTMAP_XML);
        return xsltMapFile;
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
    
    private void populateServices(File projectSourceRoot, NodeList nodeList, List<ServiceEntry> services) {

        if (services == null) {
            return;
        }
        
        ServiceEntry service = null;
        
        assert nodeList != null;
        for (int i =0; i< nodeList.getLength(); i++ ) {
            Node tmpNode = nodeList.item(i);
            NamedNodeMap namedNodeMap = tmpNode.getAttributes();

            String partnerLink = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.PARTNER_LINK);
            QName partnerLinkQname = getQName(partnerLink);
            String partnerLinkNsURI = partnerLinkQname.getNamespaceURI();
            String partnerLinkNSPrefix = populateNamespace(partnerLinkNsURI);

            
            String portType = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.PORT_TYPE);
            QName portTypeQname = getQName(portType);
            String portTypeNsURI = portTypeQname.getNamespaceURI();
            String portTypeNSPrefix = populateNamespace(portTypeNsURI);

            String roleName = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.ROLE_NAME);
            
            services.add(new ServiceEntry(
                    partnerLink, 
                    portType, 
                    partnerLinkNsURI, 
                    portTypeNsURI, 
                    roleName, 
                    partnerLinkNSPrefix, 
                    portTypeNSPrefix, 
                    partnerLinkQname, 
                    portTypeQname
                    
                    ));
        }
    }

    private void populateProviderServices(File projectSourceRoot, NodeList nodeList) {
        if (mProviders == null) {
            mProviders = new ArrayList<ServiceEntry>();
        }
        populateServices(projectSourceRoot, nodeList, mProviders);
    }
    
    private void populateConsumerServices(File projectSourceRoot, NodeList nodeList) {
        if (mConsumers == null) {
            mConsumers = new ArrayList<ServiceEntry>();
        }
        populateServices(projectSourceRoot, nodeList, mConsumers);
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
        GenerateJBIDescriptorTask ddt = new GenerateJBIDescriptorTask();

    }
}
