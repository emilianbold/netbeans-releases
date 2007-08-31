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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.apache.tools.ant.BuildException;
import org.netbeans.modules.xslt.tmap.model.xsltmap.TransformationDescType;
import org.netbeans.modules.xslt.tmap.model.xsltmap.XmlUtil;
import org.netbeans.modules.xslt.tmap.model.xsltmap.XsltMapConst;
import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.modules.xslt.project.anttasks.jbi.TMapServiceEntry;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class OldProjectTransformer {
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

    private List<TMapServiceEntry> mProviders = new ArrayList<TMapServiceEntry>();
    
    private List<TMapServiceEntry> mConsumers = new ArrayList<TMapServiceEntry>();
    
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(OldProjectTransformer.class.getName());    

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


    public OldProjectTransformer(String srcDir, String buildDir) {
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
    
    public void execute() throws BuildException { 
        File xsltMapFile = getXsltMapFile();
        
        Document document = null;
        if (xsltMapFile != null && xsltMapFile.exists()) {
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
            
        generateTransformMap();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void generateTransformMap() throws IOException {
        FileOutputStream fos = null;
        try
        {
           
            StringBuffer sb = new StringBuffer();
            sb.append("<transformmap \n");
            sb.append("       xmlns=\"http://xml.netbeans.org/schema/transformmap\"\n");

            
            int nss = mNameSpacePrefix.size();
            int i = 0;
            Set<String> nsUris = mNameSpacePrefix.keySet();
            for (String nsUri : nsUris) {
                sb.append("        xmlns:" + mNameSpacePrefix.get(nsUri) + "=\"" + nsUri + "\"");
                i++;
                if(i < nss - 1) {
                    sb.append("\n");
                }
            }
            
            sb.append(">\n");


            if (mProviders != null) {

                List<TMapServiceEntry> uniqueServices = getUniqueServices(mProviders);
                for (TMapServiceEntry uniqueService : uniqueServices) {
                        sb.append("    <service partnerLinkType=\"").
                                append(getColonedQName(uniqueService.getPartnerLinkNameQname(), mNameSpacePrefix)).
                                append("\"");
                        sb.append(" roleName=\"").
                                append(uniqueService.getRoleName()).
                                append("\" >\n");
                        
                        sb.append(getServiceOperations(mProviders, uniqueService));
                        
                        sb.append("    </service>\n");
                        
                }
            }
            
            sb.append("</transformmap>\n");
            String content = sb.toString();
            fos = new FileOutputStream(getTransformMapFile());
            store(content.getBytes("UTF-8"), fos);
        } finally {
            if (fos != null) {
                fos.close();
            } 
        }
    }
    
    private List<TMapServiceEntry> getUniqueServices(List<TMapServiceEntry> allServices) {
        List<TMapServiceEntry> uniqueServices = new ArrayList<TMapServiceEntry>();
        if (allServices == null) {
            return uniqueServices;
        }
        
        for (int j = 0; j < allServices.size(); j++) {
            TMapServiceEntry tmpService = allServices.get(j);
            boolean isUnique = true;
            for (TMapServiceEntry uniqueServiceEntry : uniqueServices) {
                QName servicePltQname = tmpService.getPartnerLinkNameQname();
                String serviceRoleName = tmpService.getRoleName();
                if ( servicePltQname != null
                        && servicePltQname.equals(uniqueServiceEntry.getPartnerLinkNameQname())
                        && serviceRoleName != null
                        && serviceRoleName.equals(uniqueServiceEntry.getRoleName())) {
                    isUnique = false;
                }
            }

            if (isUnique) {
                uniqueServices.add(tmpService);
            }
        }
        
        return uniqueServices;
    }
    
    private String getServiceOperations(List<TMapServiceEntry> allServices, 
            TMapServiceEntry uniqueService) 
    {
        assert uniqueService != null && allServices != null;
        assert uniqueService.getPartnerLinkNameQname() != null && uniqueService.getRoleName() != null;
        StringBuffer serviceOperations = new StringBuffer("");
        
        for (TMapServiceEntry service : allServices) {
            
            if (uniqueService.getRoleName().equals(service.getRoleName())
                    && uniqueService.getPartnerLinkNameQname().equals(service.getPartnerLinkNameQname())) 
            {
                    TMapServiceEntry invoke = getInvoke(service);
                    
                    StringBuffer invokeSb = new StringBuffer();
                    if (invoke != null) {
                        invokeSb.append("            <invoke partnerLinkType=\"").
                                append(getColonedQName(invoke.getPartnerLinkNameQname(), mNameSpacePrefix));
                        invokeSb.append(" opName=\"").
                                append(invoke.getOperation()).
                                append("\" ");
                        invokeSb.append(" roleName=\"").
                                append(invoke.getRoleName()).
                                append("\" ");
                        invokeSb.append(" file=\"").
                                append(invoke.getFile()).
                                append("\" ");
                        invokeSb.append(" transformJBI=\"").
                                append(invoke.getTransformJBI()).
                                append("\" ");
                    } else {
                        invokeSb.append("/>\n");
                    }
                    
                    StringBuffer sbOperation = new StringBuffer();
                    sbOperation.append("        <operation");
                    sbOperation.append(" opName=\"").
                            append(service.getOperation()).
                                append("\" ");
//                    sbOperation.append(" file=\"").
//                            append(service.getFile()).
//                                append("\" ");
//                    sbOperation.append(" transformJBI=\"").
//                            append(service.getTransformJBI()).
//                                append("\" ");
                    sbOperation.append(invokeSb);         
                    
                    serviceOperations.append(sbOperation);
            }
        }

        return serviceOperations.toString();
    }
    
    private TMapServiceEntry getInvoke(TMapServiceEntry input) {
        if (mConsumers == null || mConsumers.size() == 0 
                || input == null || input.getNode() == null) 
        {
            return null;
        }
        
        TMapServiceEntry invoke = null;
        
        for (TMapServiceEntry consumer : mConsumers) {
            Node tmpNode = consumer.getNode();
            tmpNode = tmpNode == null ? null : tmpNode.getParentNode();
            if (tmpNode == null 
                    || ! TransformationDescType.INPUT.getTagName().equals(tmpNode.getLocalName())) 
            {
                continue;
            }

            if (tmpNode.equals(input.getNode().getParentNode())) {
                invoke = consumer;
                break;
            }
        }

        return invoke;
    }
    
    private File getXsltMapFile() throws BuildException {
        String srcDir = getSourceDirectory();
        if (srcDir == null || "".equals(srcDir)) {
            throw new BuildException("source directory shouldn't be null or empty");
        }
        
        File xsltMapFile = new File(srcDir+"/"+XsltproConstants.XSLTMAP_XML);
        return xsltMapFile;
    }
    
    private File getTransformMapFile() throws BuildException {
        String srcDir = getSourceDirectory();
        if (srcDir == null || "".equals(srcDir)) {
            throw new BuildException("source directory shouldn't be null or empty");
        }
        
        File transformMapFile = new File(srcDir+"/"+XsltproConstants.TRANSFORMMAP_XML);
        return transformMapFile;
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
    
    private void populateServices(File projectSourceRoot, NodeList nodeList, List<TMapServiceEntry> services) {

        if (services == null) {
            return;
        }
        
        TMapServiceEntry service = null;
        
        assert nodeList != null;

        for (int i =0; i< nodeList.getLength(); i++ ) {
            Node tmpNode = nodeList.item(i);
            NamedNodeMap namedNodeMap = tmpNode.getAttributes();

            String partnerLink = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.PARTNER_LINK);
            QName partnerLinkQname = getQName(partnerLink);
            if (partnerLinkQname == null) {
                continue;
            }
            populateNamespace(partnerLinkQname.getNamespaceURI());
            
            
            String roleName = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.ROLE_NAME);
            String operation = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.OPERATION);
            String file = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.FILE);
            String transformJBI = XmlUtil.getAttrValue(namedNodeMap, XsltMapConst.TRANSFORM_JBI);
            
            services.add(new TMapServiceEntry(
                    partnerLinkQname, 
                    roleName, 
                    operation,
                    file,
                    transformJBI,
                    tmpNode
                    ));
            
        }
    }

    private void populateProviderServices(File projectSourceRoot, NodeList nodeList) {
        if (mProviders == null) {
            mProviders = new ArrayList<TMapServiceEntry>();
        }
        populateServices(projectSourceRoot, nodeList, mProviders);
    }
    
    private void populateConsumerServices(File projectSourceRoot, NodeList nodeList) {
        if (mConsumers == null) {
            mConsumers = new ArrayList<TMapServiceEntry>();
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
}
