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

import org.netbeans.modules.soa.jca.base.Util;
import org.netbeans.modules.soa.jca.base.inbound.wizard.InboundConfigDataImpl;
import org.netbeans.modules.soa.jca.base.spi.InboundConfigCustomPanel.InboundConfigData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
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
 *
 * @author echou
 */
public class EjbProjectUtil {

    public static List<JcaMdbModel> getJcaMdbModels(FileObject projDirectory) {
        List<JcaMdbModel> models = new ArrayList<JcaMdbModel> ();

        try {
            parseEjbJarXml(projDirectory, models);
            parseSunEjbJarXml(projDirectory, models);
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
        }

        return models;
    }

    public static void modifyJcaMdbActivation(FileObject projDirectory, JcaMdbModel model) throws Exception {
        modifySunEjbJarXml(projDirectory, model);
        modifyEjbJarXml(projDirectory, model);
    }

    public static void deleteJcaMdb(Project project, JcaMdbModel model) throws Exception {
        deleteJavaClass(project, model);
        deleteSunEjbJarXml(project.getProjectDirectory(), model);
        deleteEjbJarXml(project.getProjectDirectory(), model);
    }

    private static void parseEjbJarXml(FileObject projDirectory, List<JcaMdbModel> models) throws Exception {
        FileObject ejbJarXml = getXmlDD(projDirectory, "src/conf", "ejb-jar.xml"); // NOI18N
        if (ejbJarXml == null) {
            return;
        }

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
            return;
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }

        NodeList messageDrivenList = enterpriseBeans.getElementsByTagName("message-driven");
        for (int i = 0; i < messageDrivenList.getLength(); i++) {
            Element messageDriven = (Element) messageDrivenList.item(i);

            // ejb-name
            NodeList ejbNameList = messageDriven.getElementsByTagName("ejb-name");
            if (ejbNameList.getLength() == 0) {
                continue;
            }
            String ejbName = ejbNameList.item(0).getTextContent();

            // ejb-class
            NodeList ejbClassList = messageDriven.getElementsByTagName("ejb-class");
            if (ejbClassList.getLength() == 0) {
                continue;
            }
            String ejbClass = ejbClassList.item(0).getTextContent();

            JcaMdbModel model = new JcaMdbModel(ejbName, ejbClass);
            models.add(model);
            InboundConfigDataImpl inboundConfigData = new InboundConfigDataImpl();
            model.setInboundConfigData(inboundConfigData);

            // activation-config
            NodeList activationConfigList = messageDriven.getElementsByTagName("activation-config");
            for (int j = 0; j < activationConfigList.getLength(); j++) {
                Element activationConfig = (Element) activationConfigList.item(j);
                NodeList activationConfigPropertyList = activationConfig.getElementsByTagName("activation-config-property");
                for (int k = 0; k < activationConfigPropertyList.getLength(); k++) {
                    Element activationConfigProperty = (Element) activationConfigPropertyList.item(k);
                    String propertyName = activationConfigProperty.getElementsByTagName("activation-config-property-name").item(0).getTextContent();
                    String propertyValue = activationConfigProperty.getElementsByTagName("activation-config-property-value").item(0).getTextContent();
                    inboundConfigData.addActivationProperty(propertyName, propertyValue);
                }
            }
        }

    }

    private static void parseSunEjbJarXml(FileObject projDirectory, List<JcaMdbModel> models) throws Exception {
        FileObject sunEjbJarXml = getXmlDD(projDirectory, "src/conf", "sun-ejb-jar.xml"); // NOI18N
        if (sunEjbJarXml == null) {
            return;
        }

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
            return;
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }

        NodeList ejbList = enterpriseBeans.getElementsByTagName("ejb");
        for (int i = 0; i < ejbList.getLength(); i++) {
            Element ejb = (Element) ejbList.item(i);

            // ejb-name
            NodeList ejbNameList = ejb.getElementsByTagName("ejb-name");
            if (ejbNameList.getLength() == 0) {
                continue;
            }
            String ejbName = ejbNameList.item(0).getTextContent();
            JcaMdbModel jcaMdbModel = null;
            for (JcaMdbModel model : models) {
                if (model.getEjbName().equals(ejbName)) {
                    jcaMdbModel = model;
                    break;
                }
            }
            if (jcaMdbModel == null) {
                continue;
            }

            // bean-pool
            InboundConfigData inboundConfigData = jcaMdbModel.getInboundConfigData();
            NodeList beanPoolList = ejb.getElementsByTagName("bean-pool");
            if (beanPoolList.getLength() > 0) {
                Element beanPool = (Element) beanPoolList.item(0);
                NodeList steadyPoolSizeList = beanPool.getElementsByTagName("steady-pool-size");
                if (steadyPoolSizeList.getLength() > 0) {
                    inboundConfigData.setSteadPoolSize(Integer.parseInt(steadyPoolSizeList.item(0).getTextContent()));
                }
                NodeList resizeQuantityList = beanPool.getElementsByTagName("resize-quantity");
                if (resizeQuantityList.getLength() > 0) {
                    inboundConfigData.setResizeQuantity(Integer.parseInt(resizeQuantityList.item(0).getTextContent()));
                }
                NodeList maxPoolSizeList = beanPool.getElementsByTagName("max-pool-size");
                if (maxPoolSizeList.getLength() > 0) {
                    inboundConfigData.setMaxPoolSize(Integer.parseInt(maxPoolSizeList.item(0).getTextContent()));
                }
                NodeList poolIdleTimeoutList = beanPool.getElementsByTagName("pool-idle-timeout-in-seconds");
                if (poolIdleTimeoutList.getLength() > 0) {
                    inboundConfigData.setPoolIdleTimeout(Long.parseLong(poolIdleTimeoutList.item(0).getTextContent()));
                }
                NodeList maxWaitTimeList = beanPool.getElementsByTagName("max-wait-time-in-millis");
                if (maxWaitTimeList.getLength() > 0) {
                    inboundConfigData.setMaxWaitTime(Long.parseLong(maxWaitTimeList.item(0).getTextContent()));
                }
            }

            // resource-adapter-mid
            NodeList mdbResourceAdapterList = ejb.getElementsByTagName("mdb-resource-adapter");
            if (mdbResourceAdapterList.getLength() > 0) {
                Element mdbResourceAdapter = (Element) mdbResourceAdapterList.item(0);
                NodeList resourceAdapterMidList = mdbResourceAdapter.getElementsByTagName("resource-adapter-mid");
                if (resourceAdapterMidList.getLength() > 0) {
                    jcaMdbModel.setJcaModuleName(resourceAdapterMidList.item(0).getTextContent());
                }
            }

        }

    }

    private static void modifyEjbJarXml(FileObject projDirectory, JcaMdbModel model) throws Exception {
        FileObject ejbJarXml = getXmlDD(projDirectory, "src/conf", "ejb-jar.xml"); // NOI18N
        if (ejbJarXml == null) {
            return;
        }

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
            return;
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }

        NodeList messageDrivenList = enterpriseBeans.getElementsByTagName("message-driven");
        boolean dirtyFlag = false;
        for (int i = 0; i < messageDrivenList.getLength(); i++) {
            Element messageDriven = (Element) messageDrivenList.item(i);

            // ejb-name
            NodeList ejbNameList = messageDriven.getElementsByTagName("ejb-name");
            if (ejbNameList.getLength() == 0) {
                continue;
            }
            String ejbName = ejbNameList.item(0).getTextContent();
            if (!ejbName.equals(model.getEjbName())) {
                continue;
            }


            // remove old activation-config
            NodeList activationConfigList = messageDriven.getElementsByTagName("activation-config");
            for (int j = 0; j < activationConfigList.getLength(); j++) {
                messageDriven.removeChild(activationConfigList.item(j));
            }

            // add new activation-config
            Element activationConfig = doc.createElement("activation-config"); // NOI18N
            for (Map.Entry<String, String> activationEntry : model.getInboundConfigData().getActivationProps()) {
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

            dirtyFlag = true;
            break;
        }

        if (dirtyFlag) {
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

    }

    private static void modifySunEjbJarXml(FileObject projDirectory, JcaMdbModel model) throws Exception {
        FileObject sunEjbJarXml = getXmlDD(projDirectory, "src/conf", "sun-ejb-jar.xml"); // NOI18N
        if (sunEjbJarXml == null) {
            return;
        }

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
            return;
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }

        NodeList ejbList = enterpriseBeans.getElementsByTagName("ejb");
        boolean dirtyFlag = false;
        for (int i = 0; i < ejbList.getLength(); i++) {
            Element ejb = (Element) ejbList.item(i);

            // ejb-name
            NodeList ejbNameList = ejb.getElementsByTagName("ejb-name");
            if (ejbNameList.getLength() == 0) {
                continue;
            }
            String ejbName = ejbNameList.item(0).getTextContent();
            if (!ejbName.equals(model.getEjbName())) {
                continue;
            }

            // remove old bean-pool
            NodeList beanPoolList = ejb.getElementsByTagName("bean-pool");
            for (int j = 0; j < beanPoolList.getLength(); j++) {
                ejb.removeChild(beanPoolList.item(j));
            }

            // add new bean-pool
            InboundConfigData inboundConfigData = model.getInboundConfigData();
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

            ejb.appendChild(beanPool);

            dirtyFlag = true;
            break;
        }

        if (dirtyFlag) {
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

    }

    private static void deleteJavaClass(Project project, JcaMdbModel model) throws Exception {
        String ejbClassName = model.getEjbClass();
        String relativePath = ejbClassName.replaceAll("\\.", "/") + ".java";
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            if (rootFolder == null) {
                continue;
            }

            FileObject javaClassFileObject = rootFolder.getFileObject(relativePath);
            if (javaClassFileObject != null) {
                FileLock lock = javaClassFileObject.lock();
                try {
                    javaClassFileObject.delete(lock);
                } finally {
                    lock.releaseLock();
                }
                break;
            }

        }
    }

    private static void deleteEjbJarXml(FileObject projDirectory, JcaMdbModel model) throws Exception {
        FileObject ejbJarXml = getXmlDD(projDirectory, "src/conf", "ejb-jar.xml"); // NOI18N
        if (ejbJarXml == null) {
            return;
        }

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
            return;
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }

        NodeList messageDrivenList = enterpriseBeans.getElementsByTagName("message-driven");
        boolean dirtyFlag = false;
        for (int i = 0; i < messageDrivenList.getLength(); i++) {
            Element messageDriven = (Element) messageDrivenList.item(i);

            // ejb-name
            NodeList ejbNameList = messageDriven.getElementsByTagName("ejb-name");
            if (ejbNameList.getLength() == 0) {
                continue;
            }
            String ejbName = ejbNameList.item(0).getTextContent();
            if (!ejbName.equals(model.getEjbName())) {
                continue;
            }

            enterpriseBeans.removeChild(messageDriven);

            dirtyFlag = true;
            break;
        }

        if (dirtyFlag) {
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

    }

    private static void deleteSunEjbJarXml(FileObject projDirectory, JcaMdbModel model) throws Exception {
        FileObject sunEjbJarXml = getXmlDD(projDirectory, "src/conf", "sun-ejb-jar.xml"); // NOI18N
        if (sunEjbJarXml == null) {
            return;
        }

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
            return;
        } else {
            enterpriseBeans = (Element) enterpriseBeansList.item(0);
        }

        NodeList ejbList = enterpriseBeans.getElementsByTagName("ejb");
        boolean dirtyFlag = false;
        for (int i = 0; i < ejbList.getLength(); i++) {
            Element ejb = (Element) ejbList.item(i);

            // ejb-name
            NodeList ejbNameList = ejb.getElementsByTagName("ejb-name");
            if (ejbNameList.getLength() == 0) {
                continue;
            }
            String ejbName = ejbNameList.item(0).getTextContent();
            if (!ejbName.equals(model.getEjbName())) {
                continue;
            }

            enterpriseBeans.removeChild(ejb);

            dirtyFlag = true;
            break;
        }

        if (dirtyFlag) {
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

    }

    private static FileObject getXmlDD(FileObject projDirectory, String confDirName, String xmlFileName) throws IOException {
        FileObject confDir = projDirectory.getFileObject(confDirName);
        FileObject ejbJarXml = confDir.getFileObject(xmlFileName);

        return ejbJarXml;
    }

    // resolver for sun-ejb-jar.xml
    static class SunEjbJarXmlResolver implements EntityResolver {
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

    static class XmlErrorHandler implements ErrorHandler {
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

    public static FileObject getEjbJarXmlFileObject(Project project) throws IOException {
        FileObject confDir = project.getProjectDirectory().getFileObject("src/conf");
        FileObject ejbJarXml = confDir.getFileObject("ejb-jar.xml");

        return ejbJarXml;
    }

    public static FileObject getEjbJarParentDirectoryFileObject(Project project) throws IOException {
        FileObject confDir = project.getProjectDirectory().getFileObject("src/conf");

        return confDir;
    }

}
