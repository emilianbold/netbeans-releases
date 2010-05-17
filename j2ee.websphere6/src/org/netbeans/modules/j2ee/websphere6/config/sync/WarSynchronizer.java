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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * $Id$
 */

package org.netbeans.modules.j2ee.websphere6.config.sync;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.websphere6.config.WSWarModuleConfiguration;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.*;

/**
 *
 * @author Dmitry Lipin
 */
public class WarSynchronizer extends Synchronizer {
    
    private static final Logger LOGGER = Logger.getLogger(WarSynchronizer.class.getName());
    
    private final File ibmwebbndFile;
    private final File webxmlFile;
    private XPath xpath = null;
    private boolean saveIbmWebBndNeeded = false;
    private boolean saveWebXmlNeeded = false;
    
    
    /**
     * Synchronizes the web.xml with ibm-web-bnd.xmi
     */
    public WarSynchronizer(File webxmlFilePar, File ibmwebbndFilePar) {
        this.webxmlFile = webxmlFilePar;
        this.ibmwebbndFile = ibmwebbndFilePar;
        try {
            xpath = XPathFactory.newInstance(XPathConstants.DOM_OBJECT_MODEL).newXPath();
        } catch (Exception ex) {
            ex.printStackTrace();
            xpath = null;
        }
    }
    
    
    public synchronized void syncDescriptors() {
        if ((webxmlFile != null) && (ibmwebbndFile != null)) {
            try {
                saveIbmWebBndNeeded = false;
                saveWebXmlNeeded = false;
                
                
                Document webDocument = loadDocument(webxmlFile);
                Document ibmwebbndDocument = loadDocument(ibmwebbndFile);
                
                syncEjbReferences(webDocument,ibmwebbndDocument);
                
                syncRootId(webDocument,ibmwebbndDocument);
                
                if (saveWebXmlNeeded) {
                    saveDocument(webDocument, webxmlFile);
                }
                if (saveIbmWebBndNeeded) {
                    
                    saveDocument(ibmwebbndDocument, ibmwebbndFile);
                }
            } catch (Exception ex)  {
                ex.printStackTrace();
            }
        }
    }
    private void syncRootId(Document webDocument,Document ibmwebbndDocument) {
        if (!WSWarModuleConfiguration.WEB_APP_ID.equals(webDocument.getDocumentElement().getAttribute("id"))) {
            Attr attribute = webDocument.createAttribute("id");
            attribute.setValue(WSWarModuleConfiguration.WEB_APP_ID);
            webDocument.getDocumentElement().getAttributes().setNamedItem(attribute);
            saveWebXmlNeeded = true;
        }
    }
    
    private enum Reference {
        LOCAL, REMOTE;
        public String getTagName() {
            switch(this) {
                case LOCAL: return "ejb-local-ref";
                case REMOTE: return "ejb-ref";
            }
            return null;
        }
        public boolean isLocal() {
            switch(this) {
                case LOCAL: return true;
                case REMOTE: return false;
            }
            return false;
        }
        public boolean isRemote() {
            switch(this) {
                case LOCAL: return false;
                case REMOTE: return true;
            }
            return false;
        }
    };
    
    private void syncEjbReferences(Document webDocument,Document ibmwebbndDocument) {
        try {
            
            
            String [] tags = new String[] {"ejb-ref", "ejb-local-ref"};
            for(Reference reference : new Reference[] {Reference.LOCAL, Reference.REMOTE}) {
                NodeList enterpriseBeansList = (NodeList) xpath.
                        compile("./" + reference.getTagName()).
                        evaluate(webDocument.getDocumentElement(), XPathConstants.NODESET);
                
                
                Node bindingsRoot = ibmwebbndDocument.getDocumentElement();
                if (bindingsRoot == null) {
                    return;
                }
                
                NodeList ejbBindingsList = (NodeList) xpath.
                        compile("./"+ DDXmiConstants.EJB_REF_BINDINGS_ID).
                        evaluate(bindingsRoot, XPathConstants.NODESET);
                
                
                for (int i = 0; i < enterpriseBeansList.getLength(); i++) {
                    Node node = enterpriseBeansList.item(i);
                    
                    String id = getBeanId(node);
                    if ((id == null) || (!bindingExists(bindingsRoot, id))) {
                        String interfaceName = getInterfaceName(node,reference);
                        String refName = getEjbRefNameName(node);
                        if (id == null) {
                            id = createBeanId(node, refName);
                        }
                        
                        Attr attribute = webDocument.createAttribute("id");
                        attribute.setValue(id);

                        node.getAttributes().setNamedItem(attribute);
                        
                        Node binding = constructBinding(ibmwebbndDocument,
                                getBeanId(node),
                                getBindingJNDIName(interfaceName,reference),reference);
                        bindingsRoot.appendChild(ibmwebbndDocument.createTextNode("    "));
                        bindingsRoot.appendChild(binding);
                        bindingsRoot.appendChild(ibmwebbndDocument.createTextNode("\n"));
                        saveIbmWebBndNeeded = true;
                        saveWebXmlNeeded = true;
                    }
                }
                
                for (int i = 0; i < ejbBindingsList.getLength(); i++) {
                    Node node = ejbBindingsList.item(i);
                    
                    String id = getBindingId(node);
                    
                    String type = getBindingType(node);
                    
                    if(((DDXmiConstants.BINDING_EJB_REF_TYPE_LOCAL_STRING).equals(type) &&
                            reference.isLocal()) ||
                            (type==null && reference.isRemote()) ) {
                        if (!ejbReferenceExists(webDocument, id, reference) || 
                                !isEjbReferenceValid(webDocument,id,reference)) {
                            bindingsRoot.removeChild(node);
                            saveIbmWebBndNeeded = true;
                        }
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (XPathFactoryConfigurationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }
    
    private String getReferenceJNDIName(Object [] dirList,String interfaceName,Reference reference) {
        if(dirList == null || dirList.length == 0) {
            return null;
        }
        for (int i = 0; i < dirList.length; i++) {
            File dir = (File) dirList [i];
            File ejbJarFile = new File(dir,   "src" + File.separator+ "conf" + File.separator + "ejb-jar.xml");
            File ejbJarBndFile = new File(dir, "src" + File.separator+"conf"+File.separator+"ibm-ejb-jar-bnd.xmi");
           
            String jndiName = null;
            try {
                Document ejbJarDocument = loadDocument(ejbJarFile);
                Document ibmEjbJarBndDocument = loadDocument(ejbJarBndFile);
                Node session = (Node) xpath.
                        compile("./enterprise-beans/session[" +
                        (reference.isLocal() ?
                            "local" : "remote" ) + "=\"" +
                        interfaceName + "\"]").
                        evaluate(ejbJarDocument.getDocumentElement(), XPathConstants.NODE);
                Node sessionIdNode =  session.getAttributes().getNamedItem("id");
                String sessionId = sessionIdNode.getTextContent();
                
                Node hrefNode = (Node) xpath.
                        compile("./ejbBindings/enterpriseBean[@href=\"META-INF/ejb-jar.xml#" + sessionId + "\"]").
                        evaluate(ibmEjbJarBndDocument.getDocumentElement(), XPathConstants.NODE);
                
                jndiName = hrefNode.getParentNode().getAttributes().getNamedItem(DDXmiConstants.JNDI_NAME_ID).getTextContent();
                
                addSyncFile(ejbJarFile);               
                
            } catch (NullPointerException e) {
                // nodes not found
                continue;
            } catch (XPathExpressionException e) {
                // can`t evaluate XPath
                continue;
            }
            return jndiName;
        }
        return null;
    }
  
    private String getInterfaceName(Node beanNode, Reference reference) throws XPathExpressionException {
        String intName = ((Node) xpath.compile(
                reference.isLocal() ? "child::local" : "child::remote").
                evaluate(beanNode, XPathConstants.NODE)).
                getTextContent();
        return intName;
    }
    private String getEjbRefNameName(Node beanNode) throws XPathExpressionException {
        String refName = ((Node) xpath.compile(
                "child::ejb-ref-name").
                evaluate(beanNode, XPathConstants.NODE)).
                getTextContent();
        return refName;
    }
    
    private String getBeanId(Node beanNode) {
        Node idNode = beanNode.getAttributes().getNamedItem("id");
        
        return (idNode != null)? idNode.getTextContent() : null;
    }
    private Object [] getEjbDirectoryList() {
        ArrayList <File> list = new ArrayList <File> ();
        Project currentProject = FileOwnerQuery.getOwner(FileUtil.toFileObject(webxmlFile));
        File webProjectFolder = FileUtil.toFile(currentProject.getProjectDirectory());
        Properties props = new Properties();        
        Document projectXml = loadDocument(new File(webProjectFolder, "nbproject" + File.separator + "project.xml"));
        try {
            NodeList refProjectNames = (NodeList) xpath.compile("./configuration/references/reference/foreign-project").
                    evaluate(projectXml.getDocumentElement(), XPathConstants.NODESET);
            for(int i=0;i<refProjectNames.getLength();i++) {
                String name = refProjectNames.item(i).getTextContent();
                try {
                    InputStream is = new FileInputStream(new File(webProjectFolder, "nbproject" + File.separator + "project.properties"));
                    props.clear();
                    props.load(is);
                    is.close();
                    String dir = props.getProperty("project." + name);
                    if(dir!=null) {
                        list.add(new File(webProjectFolder, dir));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        }
        return list.toArray();
    }
    
    private String getBindingJNDIName(String interfaceName,Reference referece) throws XPathFactoryConfigurationException, XPathExpressionException {
        return getReferenceJNDIName(getEjbDirectoryList(),interfaceName, referece);
    }
    
    private String createBeanId(Node beanNode, String refName) throws XPathFactoryConfigurationException, XPathExpressionException {
        String name = refName;
        if(name.lastIndexOf("/")!=-1) {
            name = name.substring(name.lastIndexOf("/")+1);
        }
        name = name.replaceAll("\\.","_");        
        return name + BINDING_SEPARATOR + new Date().getTime();
    }
    
    private boolean ejbReferenceExists(Document document, String id, Reference reference) throws XPathExpressionException, XPathFactoryConfigurationException {
        
        String path = "./" + reference.getTagName() + "[@id=\"" + id + "\"]";
        
        Node node = (Node) xpath.compile(path).evaluate(document.getDocumentElement(), XPathConstants.NODE);
        return node != null;
    }
    private boolean isEjbReferenceValid(Document document, String id, Reference reference) throws XPathExpressionException, XPathFactoryConfigurationException {
        String path = "./" + reference.getTagName() + "[@id=\"" + id + "\"]";
        Node ejbRefNode = (Node) xpath.compile(path).evaluate(document.getDocumentElement(), XPathConstants.NODE);
        
        String intName = ((Node) xpath.compile(
                reference.isLocal() ? "child::local" : "child::remote").
                evaluate(ejbRefNode, XPathConstants.NODE)).
                getTextContent();
        
        boolean result = (getReferenceJNDIName(getEjbDirectoryList(),intName,reference)!=null);
        return result;
    }
    private String getBindingId(Node bindingNode) {
        Node idNode = bindingNode.getAttributes().getNamedItem("xmi:id");
        
        if (idNode != null) {
            return idNode.getTextContent();
        } else {
            return null;
        }
    }
    
    private String getBindingType(Node bindingNode)  throws XPathExpressionException, XPathFactoryConfigurationException {
        Node bind = (Node) xpath.compile("./bindingEjbRef").evaluate(bindingNode, XPathConstants.NODE);
        Node typeNode = bind.getAttributes().getNamedItem("xmi:type");
        
        if (typeNode != null) {
            return typeNode.getTextContent();
        } else {
            return null;
        }
    }
    
    private boolean bindingExists(Node root, String id) throws XPathFactoryConfigurationException, XPathExpressionException {
        String path = "./" + DDXmiConstants.EJB_REF_BINDINGS_ID + "[@id=\"" + id + "\"]";
        
        Node node = (Node) xpath.compile(path).evaluate(root, XPathConstants.NODE);
        
        return node != null;
    }
    
    private Node constructBinding(Document document, String id, String jndiName,Reference reference) {
        Element binding = document.createElement(DDXmiConstants.EJB_REF_BINDINGS_ID);
        binding.setAttribute(DDXmiConstants.JNDI_NAME_ID, (jndiName == null) ? "ejb/"+id : jndiName);
        binding.setAttributeNS("http://www.omg.org/XMI", "xmi:id", id);
        
        Element bean = document.createElement(DDXmiConstants.BINDING_EJB_REF_ID);
        
        bean.setAttribute("href", "WEB-INF/web.xml#" + id);
        if(reference.isLocal()) {
            bean.setAttribute("xmi:type", DDXmiConstants.BINDING_EJB_REF_TYPE_LOCAL_STRING);
        }
        
        binding.appendChild(document.createTextNode("\n        "));
        binding.appendChild(bean);
        binding.appendChild(document.createTextNode("\n"));
        return binding;
    }
    
    
}
