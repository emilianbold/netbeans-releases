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

package org.netbeans.modules.soa.jca.base.inbound;

import org.netbeans.modules.soa.jca.base.generator.api.GeneratorUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.jca.base.inbound.wizard.InboundConfigDataImpl;
import java.util.List;
import java.util.Map;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Generate artifacts for Inbound MDB, will create MDB java class, and
 * update/create ejb-jar.xml and sun-ejb-jar.xml with activation configuration
 * and pool settings
 *
 * @author echou
 */
public class InboundGenerator {

    private String rarName;
    private List<String> libraryNames;
    private String otdType;
    private String listenerName;
    private InboundConfigDataImpl inboundConfigData;
    private String tx;
    private Project project;
    private String className;
    private FileObject folder;
    private FileObject template;
    private String pkgName;

    public InboundGenerator(String rarName,
            List<String> libNames,
            String otdType,
            String listenerName,
            InboundConfigDataImpl inboundConfigData,
            String tx,
            Project project,
            String className,
            FileObject folder,
            FileObject template,
            String pkgName) {
        this.rarName = rarName;
        this.libraryNames = libNames;
        this.otdType = otdType;
        this.listenerName = listenerName;
        this.inboundConfigData = inboundConfigData;
        this.tx = tx;
        this.project = project;
        this.className = className;
        this.folder = folder;
        this.template = template;
        this.pkgName = pkgName;
    }

    public void addLibraryDependency() throws IOException {
        for (String libraryName : libraryNames) {
            GeneratorUtil.addLibrary(libraryName, project);
        }
    }

    public FileObject generate() throws IOException {
        DataFolder targetFolder = DataFolder.findFolder(folder);
        DataObject templateDO = DataObject.find(template);
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("otdtype", otdType); // NOI18N
        params.put("transaction", tx); // NOI18N
        FileObject createdFile = templateDO.createFromTemplate(targetFolder, className, params).getPrimaryFile();

        try {
            // order matters, because file listener is on the ejb-jar.xml
            generateSunEjbJarXml();
            generateEjbJarXml();
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
        }

        return createdFile;
    }

    // currently does not validate ejb-jar.xml against schema
    private void generateEjbJarXml() throws Exception {
        FileObject ejbJarXml = getXmlDD("src/conf", "ejb-jar.xml", "ejb-jar", // NOI18N
                "org-netbeans-modules-globalrar/ejb-jar-3.0.xml"); // NOI18N

        InputStream is = ejbJarXml.getInputStream();
        Document doc = null;
        try {
            doc = XMLUtil.parse(new InputSource(is), false, false, null, null);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        Element ejbJar = doc.getDocumentElement();
        NodeList enterpriseBeansList = ejbJar.getElementsByTagName("enterprise-beans"); // NOI18N
        Element enterpriseBeans = null;
        if (enterpriseBeansList.getLength() == 0) {
            enterpriseBeans = doc.createElement("enterprise-beans"); // NOI18N
            ejbJar.appendChild(enterpriseBeans);
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }
        Element messageDriven = doc.createElement("message-driven"); // NOI18N

        // display-name
        Element displayName = doc.createElement("display-name"); // NOI18N
        displayName.setTextContent(pkgName + "." + className + " Message-Driven Bean (mid: " + rarName + ")"); // NOI18N
        messageDriven.appendChild(displayName);

        // ejb-name
        Element ejbName = doc.createElement("ejb-name"); // NOI18N
        ejbName.setTextContent(pkgName + "." + className);
        messageDriven.appendChild(ejbName);

        // ejb-class
        Element ejbClass = doc.createElement("ejb-class"); // NOI18N
        ejbClass.setTextContent(pkgName + "." + className);
        messageDriven.appendChild(ejbClass);

        // messaging-type
        Element messagingType = doc.createElement("messaging-type"); // NOI18N
        messagingType.setTextContent(listenerName);
        messageDriven.appendChild(messagingType);

        // activation-config
        Element activationConfig = doc.createElement("activation-config"); // NOI18N
        for (Map.Entry<String, String> activationEntry : inboundConfigData.getActivationProps()) {
            Element activationConfigProperty = doc.createElement("activation-config-property"); // NOI18N
            Element activationConfigPropertyName = doc.createElement("activation-config-property-name"); // NOI18N
            activationConfigPropertyName.setTextContent(activationEntry.getKey());
            Element activationConfigPropertyValue = doc.createElement("activation-config-property-value"); // NOI18N
            activationConfigPropertyValue.setTextContent(activationEntry.getValue());
            activationConfigProperty.appendChild(activationConfigPropertyName);
            activationConfigProperty.appendChild(activationConfigPropertyValue);
            activationConfig.appendChild(activationConfigProperty);
        }
        messageDriven.appendChild(activationConfig);


        enterpriseBeans.appendChild(messageDriven);

        // write it out
        FileLock lock = ejbJarXml.lock();
        try {
            OutputStream os = ejbJarXml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        } finally {
            lock.releaseLock();
        }
        ejbJarXml.refresh();


    }

    private void generateSunEjbJarXml() throws Exception {
        FileObject sunEjbJarXml = getXmlDD("src/conf", "sun-ejb-jar.xml", "sun-ejb-jar", // NOI18N
                "org-netbeans-modules-globalrar/sun-ejb-jar.xml"); // NOI18N

        XmlErrorHandler errorHandler = new XmlErrorHandler();
        InputStream is = sunEjbJarXml.getInputStream();
        Document doc = null;
        try {
            doc = XMLUtil.parse(new InputSource(is),
                    true, true, errorHandler, new SunEjbJarXmlResolver());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        if (errorHandler.getErrorType() > 1) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(errorHandler.getError());
            DialogDisplayer.getDefault().notifyLater(d);
        }

        Element sunEjbJar = doc.getDocumentElement();
        NodeList enterpriseBeansList = sunEjbJar.getElementsByTagName("enterprise-beans"); // NOI18N
        Element enterpriseBeans = null;
        if (enterpriseBeansList.getLength() == 0) {
            enterpriseBeans = doc.createElement("enterprise-beans"); // NOI18N
            sunEjbJar.appendChild(enterpriseBeans);
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }
        Element ejb = doc.createElement("ejb"); // NOI18N

        // ejb-name
        Element ejbName = doc.createElement("ejb-name"); // NOI18N
        ejbName.setTextContent(pkgName + "." + className);

        // bean-pool
        Element beanPool = doc.createElement("bean-pool"); // NOI18N
        Element steadyPoolSize = doc.createElement("steady-pool-size"); // NOI18N
        steadyPoolSize.setTextContent(Integer.toString(inboundConfigData.getSteadyPoolSize()));
        Element resizeQuantity = doc.createElement("resize-quantity"); // NOI18N
        resizeQuantity.setTextContent(Integer.toString(inboundConfigData.getResizeQuantity()));
        Element maxPoolSize = doc.createElement("max-pool-size"); // NOI18N
        maxPoolSize.setTextContent(Integer.toString(inboundConfigData.getMaxPoolSize()));
        Element poolIdleTimeout = doc.createElement("pool-idle-timeout-in-seconds"); // NOI18N
        poolIdleTimeout.setTextContent(Long.toString(inboundConfigData.getPoolIdleTimeout()));
        Element maxWaitTime = doc.createElement("max-wait-time-in-millis"); // NOI18N
        maxWaitTime.setTextContent(Long.toString(inboundConfigData.getMaxWaitTime()));
        beanPool.appendChild(steadyPoolSize);
        beanPool.appendChild(resizeQuantity);
        beanPool.appendChild(maxPoolSize);
        beanPool.appendChild(poolIdleTimeout);
        beanPool.appendChild(maxWaitTime);

        // mdb-resource-adapter
        Element mdbResourceAdapter = doc.createElement("mdb-resource-adapter"); // NOI18N
        Element resourceAdapterMid = doc.createElement("resource-adapter-mid"); // NOI18N
        resourceAdapterMid.setTextContent(rarName);
        mdbResourceAdapter.appendChild(resourceAdapterMid);

        ejb.appendChild(ejbName);
        ejb.appendChild(beanPool);
        ejb.appendChild(mdbResourceAdapter);

        enterpriseBeans.appendChild(ejb);

        // write it out
        FileLock lock = sunEjbJarXml.lock();
        try {
            OutputStream os = sunEjbJarXml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        } finally {
            lock.releaseLock();
        }
        sunEjbJarXml.refresh();
    }

    private FileObject getXmlDD(String confDirName, String xmlFileName,
            String xmlFileNameNoExt, String srcXmlFilePath) throws IOException {
        FileObject confDir = project.getProjectDirectory().getFileObject(confDirName);
        FileObject ejbJarXml = confDir.getFileObject(xmlFileName);
        if (ejbJarXml == null) {
            FileObject srcXml = Repository.getDefault().getDefaultFileSystem().findResource(srcXmlFilePath);
            if (srcXml == null) {
                throw new IOException(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/Bundle").getString("cannot_find_Xml_descriptor_at_this_location:_") + srcXmlFilePath);
            }
            ejbJarXml = FileUtil.copyFile(srcXml, confDir, xmlFileNameNoExt);
        }

        return ejbJarXml;
    }


    // resolver for sun-ejb-jar.xml
    class SunEjbJarXmlResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) {
            String resource;
            if ("-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 EJB 2.0//EN".equals(publicId)) { // NOI18N
                resource = "/org/netbeans/modules/soa/jca/base/inbound/resources/sun-ejb-jar_2_0-0.dtd"; // NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.1 EJB 2.0//EN".equals(publicId)) { // NOI18N
                resource = "/org/netbeans/modules/soa/jca/base/inbound/resources/sun-ejb-jar_2_0-1.dtd"; // NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 EJB 2.1//EN".equals(publicId)) { // NOI18N
                resource = "/org/netbeans/modules/soa/jca/base/inbound/resources/sun-ejb-jar_2_1-0.dtd"; // NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Application Server 8.0 EJB 2.1//EN".equals(publicId)) { // NOI18N
                resource = "/org/netbeans/modules/soa/jca/base/inbound/resources/sun-ejb-jar_2_1-0.dtd"; // NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Application Server 8.1 EJB 2.1//EN".equals(publicId)) { // NOI18N
                resource = "/org/netbeans/modules/soa/jca/base/inbound/resources/sun-ejb-jar_2_1-1.dtd"; // NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Application Server 9.0 EJB 3.0//EN".equals(publicId)) { // NOI18N
                resource = "/org/netbeans/modules/soa/jca/base/inbound/resources/sun-ejb-jar_3_0-0.dtd"; // NOI18N
            } else {
                return null;
            }
            URL url = this.getClass().getResource(resource);
            return new InputSource(url.toExternalForm());
        }
    }

    class XmlErrorHandler implements ErrorHandler {
        private int errorType = -1;
        private SAXParseException error;

        public void warning(SAXParseException exception) throws SAXException {
            if (errorType  <0) {
                errorType = 0;
                error = exception;
            }
        }
        public void error(SAXParseException exception) throws SAXException {
            if (errorType < 1) {
                errorType = 1;
                error = exception;
            }
        }
        public void fatalError(SAXParseException exception) throws SAXException {
            errorType = 2;
            error = exception;
            throw exception;
        }
        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }
    }

}
