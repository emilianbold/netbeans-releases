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
package org.netbeans.modules.soa.pojo.anttasks.ide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.xml.namespace.QName;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;

import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.PrimitiveElementValue;
import org.netbeans.modules.classfile.Variable;

import org.netbeans.modules.soa.pojo.anttasks.ConsumerMetadata;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.netbeans.modules.classfile.CPUTF8Info;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.tools.ant.taskdefs.Replace;
import org.glassfish.openesb.pojose.core.anno.processor.Message;
import org.glassfish.openesb.pojose.core.anno.processor.POJOAnnotationProcessor;
import org.glassfish.openesb.pojose.core.anno.processor.visitor.ValidationVisitor;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.soa.pojo.anttasks.POJOMetadata;
import org.netbeans.modules.soa.pojo.anttasks.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.schema.POJOs;
import org.netbeans.modules.soa.pojo.schema.POJOProviders;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.validator.cf.impl.WrapperClassLoader;
import org.netbeans.modules.soa.pojo.util.NBPOJOConstants;
import org.netbeans.modules.soa.pojo.util.Util;

/**
 * Generates JBI Descriptor
 * @author Sreenivasan Genipudi
 */
public class IdePOJOGenerateJbiDescriptorTask extends Task {

    private static final String PROVIDER_CLASS_NAME =
            "org/glassfish/openesb/pojose/api/annotation/Provider";// NOI18N
    private static final String POJO_CLASS_NAME =
            "org/glassfish/openesb/pojose/api/annotation/POJO";// NOI18N
    private static final String POJO_OPN_CLASS_NAME =
            "org/glassfish/openesb/pojose/api/annotation/Operation";// NOI18N
    private static final String POJO_ONDONE_CLASS_NAME =
            "org/glassfish/openesb/pojose/api/annotation/OnDone";// NOI18N
    private static final String CONSUMER_ENDPOINT_CLASS_NAME =
            "org/glassfish/openesb/pojose/api/annotation/ConsumerEndpoint";// NOI18N
    private static final String ENDPOINT_CLASS_NAME =
            "org/glassfish/openesb/pojose/api/annotation/Endpoint";// NOI18N
    private static final String CONST_NAME = "name";// NOI18N
    private static final String CONST_INTERFACE_NAME = "interfaceQN";// NOI18N
    //  private static final String CONST_INTERFACE_NS = "interfaceNS";// NOI18N
    private static final String CONST_SERVICE_NAME = "serviceQN";// NOI18N
    //private static final String CONST_SERVICE_NS = "serviceNS";// NOI18N
    private static final String CONST_OUTMSG_NAME = "outMessageTypeQN";// NOI18N
    //private static final String CONST_OUTMSG_NS = "outMessageTypeNS";// NOI18N
    private static final String BUILD_ERROR = "BUILD_ERROR";// NOI18N
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
    public static final String NS_ATTR_VALUE = "http://java.sun.com/xml/ns/jbi"; // NOI18N
    public static final String NS_XSI_ATTR_NAME = "xmlns:xsi";  // NOI18N
    public static final String NS_XSI_ATTR_VALUE = "http://www.w3.org/2001/XMLSchema-instance"; // NOI18N
    public static final String XSI_ATTR_NAME = "xsi:schemaLocation"; // NOI18N
    public static final String XSI_ATTR_VALUE = "http://java.sun.com/xml/ns/jbi jbi.xsd"; // NOI18N
    public static final String JBI_EXT_NS = "http://www.sun.com/jbi/descriptor/service-unit"; // NOI18N
    public static final String JBI_EXT_DISPLAY_NAME = "display-name";
    public static final String JBI_EXT_PROC_NAME_ATTR = "process-name";
    public static final String JBI_EXT_FILE_PATH_ATTR = "file-path";
    public static final String NAMESPACE_PREFIX = "ns"; // NOI18N
    public static final String POJO_NS_PREFIX = "pojons"; // NOI18N
    private static final String VOID_CONST = "void";    //NOI18N
    private static final String POJO_TEMPLATE_WSDL = "org/netbeans/modules/soa/pojo/resources/POJOProviderWSDL.template"; //NOI18N

    private String mSourceDirectory = null;
    private String mBuildDirectory = null;

    private POJOs oldCfg = null;
    private Map<String, POJOProvider> mOldCfgLoadedPOJOs = null;
    private List<POJOMetadata> mNewPOJOs = new ArrayList<POJOMetadata>();
    private List<POJOMetadata> mScannedPOJOs = new ArrayList<POJOMetadata>();
    private List<String> mScannedPOJOsKeys = new ArrayList<String>();
    private ResourceBundle mRb = null;

    //Only modified by getNSPrefix method.
    private AtomicInteger nsCtr = new AtomicInteger(0);
    private Map<String, String> nsPrefix2NSMap = new HashMap<String, String>();

    @Override
    public void execute() throws BuildException {
        boolean valErrrors = false;
        mRb = ResourceBundle.getBundle(
                "org.netbeans.modules.soa.pojo.anttasks.ide.Bundle", //NOI18N
                Locale.getDefault());
        try {
            Project antProj = this.getProject();
            FileSet fs = null;
            fs = new FileSet();
            fs.setProject(antProj);
            fs.setDir(new File(this.mBuildDirectory));
            fs.setIncludes("**/*.class");//NOI18N
            String[] classList = fs.getDirectoryScanner().getIncludedFiles();
            ClassFile classFile = null;
            String endPointName = null;

            this.mScannedPOJOs.clear();
            this.mScannedPOJOsKeys.clear();

            for (String path2class : classList) {

                classFile = new ClassFile(this.mBuildDirectory + File.separator + path2class);
                //Get the provider information
                ClassName cln = ClassName.getClassName(PROVIDER_CLASS_NAME);
                Annotation ann = classFile.getAnnotation(cln);
                if (ann == null) {
                    cln = ClassName.getClassName(POJO_CLASS_NAME);
                    ann = classFile.getAnnotation(cln);
                    if (ann == null) {
                        continue;
                    }
                }

                endPointName = getAnnotationValue(ann, CONST_NAME, classFile.getName().getSimpleName());
                String namespace = GeneratorUtil.getNamespace(classFile.getName().getPackage(), endPointName);

                POJOMetadata pjm = new POJOMetadata();
                this.mScannedPOJOs.add(pjm);
                //Get Endpoint meta data
                getProviderMetadata(path2class, classFile, ann, endPointName, namespace, pjm); // @Provider
                getEndpointMetadata(classFile, cln, namespace, pjm); // @Endpoint
                getConsumerEndpointMetadata(classFile, cln, namespace, pjm); //@ConsumerEndpoint
                //Now add to key list
                this.mScannedPOJOsKeys.add(pjm.getPackageName() + "." + pjm.getClassName());
            }

            loadPOJOConfigFile();
            generateJbiXml(mScannedPOJOs); // Uses previously loaded POJOs and updated missing POJOProviders.
            updateConfigFile();
        } catch (Exception ex) {
            throw new BuildException(mRb.getString(BUILD_ERROR), ex);
        }

        validate(valErrrors);
    }

    // Assmed scanning is done.
    private void loadPOJOConfigFile() {
        if (mOldCfgLoadedPOJOs == null) {
            mOldCfgLoadedPOJOs = new HashMap();
        } else {
            mOldCfgLoadedPOJOs.clear();
        }

        this.mNewPOJOs.clear();

        try {
            File projectDir = new File(new File(this.mBuildDirectory).getParentFile().getParentFile(),
                    NBPOJOConstants.NBPROJECT_DIR);
            File pojoConfigXMLFile = new File(projectDir, NBPOJOConstants.POJOS_CONFIG_FILE_NAME);

            if (pojoConfigXMLFile.exists()) {
                this.oldCfg = POJOs.read(pojoConfigXMLFile);
                POJOProviders ps = this.oldCfg.getPOJOProviders();
                POJOProvider[] pojoArray = ps.getPOJOProvider();
                if (ps.getPOJOProvider().length > 0) {
                    for (POJOProvider pojo : pojoArray) {
                        mOldCfgLoadedPOJOs.put(pojo.getPackage() + "." + pojo.getClassName(), pojo);
                    }
                }
            }
        } catch (Exception ee) {
            if (ee != null) {
                this.log(ee.getMessage());
            }
        }
    }

    private void updateConfigFile() {
        // Removed deleted POJOs
        int removedPojos = 0;
        if ((this.oldCfg != null) && (this.oldCfg.getPOJOProviders() != null)){
            if (this.oldCfg.getPOJOProviders().sizePOJOProvider() > 0){
                POJOProviders prs = this.oldCfg.getPOJOProviders();
                Iterator<String> oldCfgPojoKeys = this.mOldCfgLoadedPOJOs.keySet().iterator();
                String oldCfgPojoKey = null;
                while (oldCfgPojoKeys.hasNext()){
                    oldCfgPojoKey = oldCfgPojoKeys.next();
                    if (!this.mScannedPOJOsKeys.contains(oldCfgPojoKey)){
                        prs.removePOJOProvider(this.mOldCfgLoadedPOJOs.get(oldCfgPojoKey));
                        removedPojos++;
                    }
                }
            }
        }

        //Update the config file.
        if ((this.mNewPOJOs.size() > 0) || (removedPojos > 0)) {

            if (this.oldCfg == null) {
                this.oldCfg = new POJOs();
                this.oldCfg.setVersion(NBPOJOConstants.LATEST_CFG_VERSION);
                this.oldCfg.setPOJOProviders(new POJOProviders());
            }

            Iterator<POJOMetadata> newPojos = this.mNewPOJOs.iterator();
            POJOMetadata pm = null;
            POJOProvider prov = null;
            POJOProviders provs = this.oldCfg.getPOJOProviders();
            String wsdlLoc = null;

            while (newPojos.hasNext()) {
                pm = newPojos.next();
                prov = new POJOProvider();
                prov.setClassName(pm.getClassName());
                prov.setPackage(pm.getPackageName());
                prov.setUpdateWsdlDuringBuild(false);
                if (pm.getPackageName() != null) {
                    wsdlLoc = pm.getPackageName().replaceAll("\\.", "/"); //NOI18N
                } else {
                    wsdlLoc = ""; //NOI18N
                }
                wsdlLoc = "src/" + wsdlLoc + "/" + pm.getClassName() + ".wsdl";//NOI18N
                prov.setWsdlLocation(wsdlLoc);
                provs.addPOJOProvider(prov);
            }

            File projectDir = new File(new File(this.mBuildDirectory).getParentFile().getParentFile(),
                    NBPOJOConstants.NBPROJECT_DIR);
            File pojoConfigXMLFile = new File(projectDir, NBPOJOConstants.POJOS_CONFIG_FILE_NAME);
            try {
                this.oldCfg.write(pojoConfigXMLFile);
                Util.fireCfgFileChangedEvent(this.getProject().getBaseDir(), oldCfg);
            } catch (IOException ex) {
                String msg = this.mRb.getString("ExceptionWhileUpdatingCfg");//NOI18N
                this.log(msg, ex, Level.SEVERE.intValue());
            }
        }
    }

    private void resetNSPrefixCtr(){
        this.nsCtr.set(0);
        this.nsPrefix2NSMap.clear();
    }

    private String getNSPrefix(String ns){
        if (ns == null){
            return null;
        }

        String ret = this.nsPrefix2NSMap.get(ns);
        if (ret == null){
            ret = POJO_NS_PREFIX + this.nsCtr.incrementAndGet() ;
            this.nsPrefix2NSMap.put(ns, ret);
        }
        return ret;
    }

    private void generateJbiXml(List<POJOMetadata> listOfPOJOProviders)
            throws ParserConfigurationException {

        resetNSPrefixCtr();

        File cnfFile = new File(mBuildDirectory, "META-INF"); //NOI18N
        if (!cnfFile.exists()) {
            cnfFile.mkdirs();
        }
        File jbiFile = new File(cnfFile, "jbi.xml");    //NOI18N
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument(); // Create from whole cloth

        Element root = (Element) document.createElement(JBI_ELEM_NAME);
        root.setAttribute(VERSION_ATTR_NAME, VERSION_ATTR_VALUE);
        root.setAttribute(NS_ATTR_NAME, NS_ATTR_VALUE);
        document.appendChild(root);
        // add services
        Element services = (Element) document.createElement(SERVICES_ELEM_NAME);
        services.setAttribute(BINDING_ATTR_NAME, "false"); // NOI18N
        root.appendChild(services);

        List<Element> consumes = new ArrayList<Element>();
        String ns = null;
        for (POJOMetadata pjm : listOfPOJOProviders) {
            Element portMapNode = (Element) document.createElement(PROVIDES_ELEM_NAME);

            //pjm.setServiceNSPrefix(POJO_NS_PREFIX + nsIndex++);
            ns = pjm.getServiceName().getNamespaceURI();
            if ((ns != null) && (!"".equals(ns))){
                pjm.setServiceNSPrefix(getNSPrefix(ns));
                root.setAttribute(NS_ATTR_NAME + ":" + pjm.getServiceNSPrefix(),
                    pjm.getServiceName().getNamespaceURI());
                    portMapNode.setAttribute(SERVICE_ATTR_NAME,
                        pjm.getServiceNSPrefix() + ":" + pjm.getServiceName().getLocalPart());
            } else {
                    portMapNode.setAttribute(SERVICE_ATTR_NAME, pjm.getServiceName().getLocalPart());
            }

            ns = pjm.getInterfaceName().getNamespaceURI();
            if ((ns != null) && (!"".equals(ns))){
                pjm.setInterfaceNSPrefix(getNSPrefix(ns));
                root.setAttribute(NS_ATTR_NAME + ":" + pjm.getInterfaceNSPrefix(),
                    pjm.getInterfaceName().getNamespaceURI());
                portMapNode.setAttribute(INTERFACE_ATTR_NAME,
                        pjm.getInterfaceNSPrefix() + ":" + pjm.getInterfaceName().getLocalPart());
            } else {
                portMapNode.setAttribute(INTERFACE_ATTR_NAME, pjm.getInterfaceName().getLocalPart());
            }

            portMapNode.setAttribute(ENDPOINT_ATTR_NAME, pjm.getEndPointName());

            services.appendChild(portMapNode);

            generateWSDL(pjm);

            ConsumerMetadata[] consumers = pjm.getConsumerMetadata();
            if (consumers != null) {
                for (ConsumerMetadata consumer : consumers) {
                    Element consumerNode = (Element) document.createElement(CONSUMES_ELEM_NAME);

                    ns = consumer.getServiceName().getNamespaceURI();
                    if ((ns != null) && (!"".equals(ns))){
                        consumer.setServiceNSPrefix(getNSPrefix(ns));
                        root.setAttribute(NS_ATTR_NAME + ":" + consumer.getServiceNSPrefix(),
                            consumer.getServiceName().getNamespaceURI());
                        consumerNode.setAttribute(SERVICE_ATTR_NAME,
                                consumer.getServiceNSPrefix() + ":" +
                                consumer.getServiceName().getLocalPart());
                    } else {
                        consumerNode.setAttribute(SERVICE_ATTR_NAME,
                                consumer.getServiceName().getLocalPart());
                    }

                    ns = consumer.getInterfaceName().getNamespaceURI();
                    if ((ns != null) && (!"".equals(ns))){
                        consumer.setInterfaceNSPrefix(getNSPrefix(ns));
                        root.setAttribute(NS_ATTR_NAME + ":" + consumer.getInterfaceNSPrefix(),
                            consumer.getInterfaceName().getNamespaceURI());
                        consumerNode.setAttribute(INTERFACE_ATTR_NAME,
                                consumer.getInterfaceNSPrefix() + ":" +
                                consumer.getInterfaceName().getLocalPart());
                    } else {
                        consumerNode.setAttribute(INTERFACE_ATTR_NAME,
                                consumer.getInterfaceName().getLocalPart());
                    }

                    consumerNode.setAttribute(ENDPOINT_ATTR_NAME, consumer.getEndPointName());
                    // Consumes should start only after Provides elements.
                    consumes.add(consumerNode);
                }
            }
        }

        if (consumes.size() > 0) {
            Iterator<Element> itr = consumes.iterator();
            Element ele = null;
            while (itr.hasNext()) {
                ele = itr.next();
                services.appendChild(ele);
            }
        }

        PrintWriter pw = null;
        try {
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();

            tFactory.setAttribute("indent-number", new Integer(4));

            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            pw = new PrintWriter(jbiFile, "UTF-8");
            StreamResult result = new StreamResult(pw);

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");   // NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");  // NOI18N
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");  // NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N

            transformer.transform(source, result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (pw != null) {
                try {
                    pw.flush();
                } catch (Exception ex) {
                }

                try {
                    pw.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    private void generateWSDL(POJOMetadata pjm) {
        File generatedWSDLFile = new File(pjm.getFolder(), pjm.getClassName() + ".wsdl");//NOI18N

        POJOProvider searchedPojo = mOldCfgLoadedPOJOs.get(pjm.getPackageName() + "." + pjm.getClassName()); //NOI18N
        if (searchedPojo == null) {
            this.mNewPOJOs.add(pjm);
            return; //Do not generate WSDL.
        }

        if (!searchedPojo.isUpdateWsdlDuringBuild()) {
            return; //Do not generate WSDL.
        }

        String searchPojoWSDLLoc = searchedPojo.getWsdlLocation();
        if (searchPojoWSDLLoc != null && !searchPojoWSDLLoc.equals("")) {
            File root = this.getProject().getBaseDir();
            generatedWSDLFile = new File(root, searchedPojo.getWsdlLocation());
        }

        InputStream wsdlTemplateIs = this.getClass().getClassLoader().getResourceAsStream(POJO_TEMPLATE_WSDL);
        GeneratorUtil.createFile(wsdlTemplateIs, generatedWSDLFile);

        Project proj1 = this.getProject();

        Replace rep = new Replace();
        rep.setProject(proj1);
        rep.setFile(generatedWSDLFile);
        rep.setToken("${portTypeNamespace}");//NOI18N
        rep.setValue(pjm.getInterfaceName().getNamespaceURI());
        rep.execute();

        rep = new Replace();
        rep.setProject(proj1);
        rep.setFile(generatedWSDLFile);
        rep.setToken("${pojoporttype}");//NOI18N
        rep.setValue(pjm.getInterfaceName().getLocalPart());
        rep.execute();


        rep = new Replace();
        rep.setProject(proj1);
        rep.setFile(generatedWSDLFile);
        rep.setToken("${pojowdloutinput}");//NOI18N

        if (pjm.isInOut()) {
            rep.setValue("<output name=\"output1\" message=\"tns:" + pjm.getOutputMessageType().getLocalPart() + "\"/>"); //NOI18N
        } else {
            rep.setValue("");
        }

        rep.execute();

        rep = new Replace();
        rep.setProject(proj1);
        rep.setFile(generatedWSDLFile);
        rep.setToken("${pojoOutputResponse}");
        rep.setValue(pjm.getOutputMessageType().getLocalPart());
        rep.execute();


        rep = new Replace();
        rep.setProject(proj1);
        rep.setFile(generatedWSDLFile);
        rep.setToken("${pojoendpointname}");
        rep.setValue(pjm.getEndPointName());
        rep.execute();

        Copy cp = new Copy();
        cp.setProject(proj1);
        cp.setFile(generatedWSDLFile);
        cp.setOverwrite(true);
        cp.setTodir(pjm.getFolder());
        cp.execute();

    }

    private String getAnnotationValue(Annotation ann, String name) {
        return getAnnotationValue(ann, name, null);
    }

    private String getAnnotationValue(Annotation ann, String name, String defaultData) {
        AnnotationComponent annComp = ann.getComponent(name);
        String retValue = defaultData;
        if (annComp != null) {
            retValue =
                    ((CPUTF8Info) ((PrimitiveElementValue) annComp.getValue()).getValue()).getValue().toString();
        }
        return retValue;
    }

    public IdePOJOGenerateJbiDescriptorTask() {
    }

    public void setBuildDirectory(String buildDir) {
        mBuildDirectory = buildDir;
    }

    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }

    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }

    private void getProviderMetadata(String path2class, ClassFile classFile,
            Annotation ann, String endPointName, String namespace, POJOMetadata pjm) {

        String intefaceQNameStr = null;
        QName interfaceQN = null;
        String interfaceName = null;
        String interfaceNS = null;
        String serviceQNameStr = null;
        QName serviceQN = null;
        String serviceName = null;
        String serviceNS = null;

        String outMsgTypeQNameStr = null;
        QName outMsgTypeQN = null;
        String outMsgType = null;
        String outMsgTypeNs = null;

        intefaceQNameStr = getAnnotationValue(ann, CONST_INTERFACE_NAME,
                new QName(namespace, endPointName + GeneratorUtil.POJO_INTERFACE_SUFFIX).toString());
        if (intefaceQNameStr != null) {
            interfaceQN = QName.valueOf(intefaceQNameStr);
            interfaceName = interfaceQN.getLocalPart();
            interfaceNS = interfaceQN.getNamespaceURI();
        } else {
            interfaceName = "";
            interfaceNS = "";
        }

        serviceQNameStr = getAnnotationValue(ann, CONST_SERVICE_NAME,
                new QName(namespace, endPointName + GeneratorUtil.POJO_SERVICE_SUFFIX).toString());
        if (serviceQNameStr != null) {
            serviceQN = QName.valueOf(serviceQNameStr);
            serviceName = serviceQN.getLocalPart();
            serviceNS = serviceQN.getNamespaceURI();
        } else {
            serviceName = "";
            serviceNS = "";
        }

        pjm.setEndPointName(endPointName);
        pjm.setInterfaceName(new QName(interfaceNS, interfaceName));
        pjm.setServiceName(new QName(serviceNS, serviceName));

        // Get Operation meta
        Collection<Method> methodList = classFile.getMethods();
        Method operMethod = null;
        Method onDoneMethod = null;

        ClassName opnClsName = ClassName.getClassName(POJO_OPN_CLASS_NAME);
        ClassName onDoneClsName = ClassName.getClassName(POJO_ONDONE_CLASS_NAME);

        for (Method method : methodList) {
            Annotation opnAnn = method.getAnnotation(opnClsName);
            Annotation onDoneAnn = method.getAnnotation(onDoneClsName);
            if (opnAnn != null) {
                outMsgTypeQNameStr = getAnnotationValue(opnAnn,
                        IdePOJOGenerateJbiDescriptorTask.CONST_OUTMSG_NAME,
                        new QName(namespace, endPointName + POJOMetadata.OPN_SUFFIX).toString());

                if (outMsgTypeQNameStr != null) {
                    outMsgTypeQN = QName.valueOf(outMsgTypeQNameStr);
                    outMsgType = outMsgTypeQN.getLocalPart();
                    outMsgTypeNs = outMsgTypeQN.getNamespaceURI();
                } else {
                    outMsgType = "";
                    outMsgTypeNs = "";
                }

                pjm.setOutMessageType(new QName(outMsgTypeNs, outMsgType));

                operMethod = method;
            }

            if (onDoneAnn != null){
                onDoneMethod = method;
            }

            if ((onDoneMethod != null) && (operMethod != null)){
                break;
            }
        }

        if (onDoneMethod != null){
            if (onDoneMethod.getReturnSignature().equals(VOID_CONST)){
                pjm.setInOut(false);
            } else {
                pjm.setInOut(true);
            }
        } else {
            if ((operMethod != null) && (operMethod.getReturnSignature().equals(VOID_CONST))) {
                pjm.setInOut(false);
            } else {
                pjm.setInOut(true);
            }
        }

        File pojoFolder = new File(this.mSourceDirectory + File.separator + path2class);

        pjm.setFolder(pojoFolder.getParentFile());
        String cn = pojoFolder.getName();

        pjm.setClassName(cn.substring(0, cn.lastIndexOf(".")));
        String pkgNameTemp = "";
        path2class = path2class.replace('/', '.');
        path2class = path2class.replace('\\', '.');
        int pkIx = path2class.lastIndexOf(".class");//NOI18N
        path2class = path2class.substring(0, pkIx);
        pkIx = path2class.lastIndexOf(".");
        if (pkIx != -1) {
            pkgNameTemp = path2class.substring(0, pkIx);
        }
        pjm.setPackageName(pkgNameTemp);
        pjm.setSrcFolder(new File(this.mSourceDirectory + File.separator + path2class).getParentFile());

    }

    private void getEndpointMetadata(ClassFile classFile, ClassName cln,
            String namespace, POJOMetadata pjm) {
        String intefaceQNameStr = null;
        String interfaceName = null;
        QName interfaceQN = null;
        String interfaceNS = null;
        String serviceNS = null;
        String serviceName = null;
        String endPointName = null;
        String serviceQNameStr = null;
        QName serviceQN = null;

        //Get Consumer information
        cln = ClassName.getClassName(ENDPOINT_CLASS_NAME);
        Collection<Variable> varColl = classFile.getVariables();
        for (Variable var : varColl) {
            Annotation endpointAnn = var.getAnnotation(cln);
            if (endpointAnn != null) {
                ConsumerMetadata cmd = new ConsumerMetadata();
                endPointName = getAnnotationValue(endpointAnn, CONST_NAME);
                intefaceQNameStr = getAnnotationValue(endpointAnn, CONST_INTERFACE_NAME,
                        new QName(namespace, endPointName + GeneratorUtil.POJO_INTERFACE_SUFFIX).toString());
                if (intefaceQNameStr != null) {
                    interfaceQN = QName.valueOf(intefaceQNameStr);
                    interfaceName = interfaceQN.getLocalPart();
                    interfaceNS = interfaceQN.getNamespaceURI();
                } else {
                    interfaceName = "";
                    interfaceNS = "";
                }
                serviceQNameStr = getAnnotationValue(endpointAnn, CONST_SERVICE_NAME,
                        new QName(namespace, endPointName + GeneratorUtil.POJO_SERVICE_SUFFIX).toString());
                if (serviceQNameStr != null) {
                    serviceQN = QName.valueOf(serviceQNameStr);
                    serviceName = serviceQN.getLocalPart();
                    serviceNS = serviceQN.getNamespaceURI();
                } else {
                    serviceName = "";
                    serviceNS = "";
                }
                if (interfaceName == null) {
                    interfaceName = "";
                }
                if (interfaceNS == null) {
                    interfaceNS = "";
                }
                if (serviceNS == null) {
                    serviceNS = "";
                }
                if (interfaceNS == null) {
                    interfaceNS = "";
                }
                cmd.setInterfaceName(new QName(interfaceNS, interfaceName));
                cmd.setServiceName(new QName(serviceNS, serviceName));
                cmd.setEndPointName(endPointName);
                pjm.addConsumerMetadata(cmd);
            }
        }
    }

    private void getConsumerEndpointMetadata(ClassFile classFile, ClassName cln, String namespace, POJOMetadata pjm) {
        String intefaceQNameStr = null;
        String interfaceName = null;
        QName interfaceQN = null;
        String interfaceNS = null;
        String serviceNS = null;
        String serviceName = null;
        String endPointName = null;
        String serviceQNameStr = null;
        QName serviceQN = null;

        //Get Consumer information
        cln = ClassName.getClassName(CONSUMER_ENDPOINT_CLASS_NAME);
        Collection<Variable> varColl = classFile.getVariables();
        for (Variable var : varColl) {
            Annotation endpointAnn = var.getAnnotation(cln);
            if (endpointAnn != null) {
                ConsumerMetadata cmd = new ConsumerMetadata();
                endPointName = getAnnotationValue(endpointAnn, CONST_NAME);
                intefaceQNameStr = getAnnotationValue(endpointAnn,
                        CONST_INTERFACE_NAME, new QName(namespace,
                        endPointName + GeneratorUtil.POJO_INTERFACE_SUFFIX).toString());
                if (intefaceQNameStr != null) {
                    interfaceQN = QName.valueOf(intefaceQNameStr);
                    interfaceName = interfaceQN.getLocalPart();
                    interfaceNS = interfaceQN.getNamespaceURI();
                } else {
                    interfaceName = "";
                    interfaceNS = "";
                }
                serviceQNameStr = getAnnotationValue(endpointAnn, CONST_SERVICE_NAME,
                        new QName(namespace, endPointName + GeneratorUtil.POJO_SERVICE_SUFFIX).toString());
                if (serviceQNameStr != null) {
                    serviceQN = QName.valueOf(serviceQNameStr);
                    serviceName = serviceQN.getLocalPart();
                    serviceNS = serviceQN.getNamespaceURI();
                } else {
                    serviceName = "";
                    serviceNS = "";
                }
                if (interfaceName == null) {
                    interfaceName = "";
                }
                if (interfaceNS == null) {
                    interfaceNS = "";
                }
                if (serviceNS == null) {
                    serviceNS = "";
                }
                if (interfaceNS == null) {
                    interfaceNS = "";
                }
                cmd.setInterfaceName(new QName(interfaceNS, interfaceName));
                cmd.setServiceName(new QName(serviceNS, serviceName));
                cmd.setEndPointName(endPointName);
                pjm.addConsumerMetadata(cmd);
            }
        }
    }

    private void validate(boolean valErrrors) throws BuildException {
        try {
            File classRoot = new File(this.mBuildDirectory);
            WrapperClassLoader cl = new WrapperClassLoader(classRoot.getAbsolutePath());
            ValidationVisitor vi = new ValidationVisitor();
            POJOAnnotationProcessor.refreshEndpoints(classRoot.getAbsolutePath(), cl, vi);
            List<Message> msgs = vi.getMessages(Message.MessageType.error);
            String msgType = mRb.getString("ErrorMsg"); //NOI18N
            if ((msgs != null) && (msgs.size() > 0)) {
                valErrrors = true;
            }
            for (Message m : msgs) {
                this.handleErrorOutput(msgType + m.getMessage());
                //this.log(msgType + m.getMessage(), Level.SEVERE.intValue());
            }
            msgs = vi.getMessages(Message.MessageType.warn);
            msgType = mRb.getString("WarningMsg"); //NOI18N
            for (Message m : msgs) {
                this.handleErrorFlush(msgType + m.getMessage());
                //this.log(msgType + m.getMessage(), Level.WARNING.intValue());
            }

            msgs = vi.getMessages(Message.MessageType.info);
            msgType = mRb.getString("InfoMsg"); //NOI18N
            for (Message m : msgs) {
                this.log(msgType + m.getMessage());
                //this.log(msgType + m.getMessage(), Level.INFO.intValue());
            }
        } catch (Exception ex) {
            throw new BuildException(mRb.getString("ExceptionWhileValidating") + ex.getLocalizedMessage()); //NOI18N
        }
        if (valErrrors) {
            throw new BuildException(mRb.getString("ValidationErrors")); //NOI18N
        }
    }
}
