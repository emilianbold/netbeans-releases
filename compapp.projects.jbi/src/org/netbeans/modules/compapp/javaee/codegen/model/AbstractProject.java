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

/*
 * AbstractProject.java
 *
 * Created on October 6, 2006, 12:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import javax.xml.namespace.QName;


import org.netbeans.modules.compapp.javaee.annotation.handler.AnnotationHandler;
import org.netbeans.modules.compapp.javaee.annotation.handler.JarClassFileLoader;
import org.netbeans.modules.compapp.javaee.annotation.handler.WebServiceClientHanlder;
import org.netbeans.modules.compapp.javaee.annotation.handler.WebServiceHandler;
import org.netbeans.modules.compapp.javaee.annotation.handler.WebserviceRefHandler;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.compapp.javaee.annotation.handler.ClassFileLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author gpatil
 */
public class AbstractProject implements JavaEEProject{

    protected String projectDir;
    protected String jarPath;
    protected JavaEEProject.ProjectType projType;
    protected List<URL> classpathURLs;
    protected List<AnnotationHandler> hanlders = new Vector<AnnotationHandler>();
    protected Set<String> annotations = new HashSet<String>();
    protected boolean deployThruCA = true;
    protected String resourceFolder = null;
    protected List<Endpoint> epts = null;
    protected List<EndpointCfg> epCfgs = null;

    protected static final String SU_NAME = "javaee_su.jar";  //NOI18N
    protected static final String JBI_XML_ENTRY = "META-INF/jbi.xml"; //NOI18N
    protected static final String JBI_XML = "jbi.xml"; //NOI18N

    private static final String JBI_DEFAULT_NS = "http://java.sun.com/xml/ns/jbi"; //NOI18N
    private static final String JBI_VERSION = "1.0" ; //NOI18N
    private static final String SU_FILE_EXT = "jar" ;     //NOI18N
    private static final String ATTR_VERSION = "version" ; //NOI18N
    private static final String ELEM_SERVICES = "services" ;   //NOI18N
    private static final String ELEM_JBI = "jbi" ;     //NOI18N

    private static final String CONSUMER = "consumer"; //NOI18N
    private static final String CONSUMES = "consumes"; //NOI18N
    private static final String EP_NAME = "endpoint-name"; //NOI18N
    private static final String INTERFACE_NAME = "interface-name"; //NOI18N
    private static final String MAPPING_ELEM = "ept-mapping" ;     //NOI18N
    public  static final String MAPPING_ELEMS = "ept-mappings" ;     //NOI18N
    private static final String MAPPING_EXT = "javaee_ext" ;     //NOI18N
    public  static final String MAPPING_JAVA_ELEM = "java-ept" ;     //NOI18N
    public  static final String MAPPING_NS = "http://javaee.serviceengine.sun.com/endpoint/naming/extension" ; //NOI18N
    private static final String MAPPING_PREFIX = "javaee_" ;     //NOI18N
    private static final String MAPPING_WSDL_ELEM = "wsdl-ept" ;     //NOI18N
    private static final String PROVIDER = "provider"; //NOI18N
    private static final String PROVIDES = "provides"; //NOI18N
    private static final String PVT_EPS_ELEM = "private-endpoints" ; //NOI18N
    private static final String PVT_EP_ELEM = "private-endpoint" ; //NOI18N
    private static final String SERVICE_NAME = "service-name"; //NOI18N
    private static final String TYPE = "type"; //NOI18N
    private static final String XMLNS = "xmlns"; //NOI18N
    private static final String XMLNS_COLON = "xmlns:"; //NOI18N

    // resources related
    protected static final String RES_XML_ENTRY = "META-INF/sun-resources.xml"; //NOI18N
    
    private static Logger logger = Logger.getLogger(AbstractProject.class.getName());

    public AbstractProject(String nJarPath) {
        this.jarPath = nJarPath;
        init();
    }

    protected AbstractProject() {
        init();
    }

    public void setProjectDir(String dir){
        this.projectDir = dir;
    }

    public void setJarPath(String nPath){
        this.jarPath = nPath;
    }

    public void setProjectType(ProjectType type){
        this.projType = type;
    }
    public ProjectType getProjectType(){
        return this.projType;
    }

    public void addSubproject(JavaEEProject subProj){
        throw new UnsupportedOperationException("addSubproject() is not supported for this type project.");
    }

    public void isDeployThruCA(boolean depThruCA){
        this.deployThruCA = depThruCA;
    }

    public boolean isDeployThruCA(){
        return this.deployThruCA;
    }

    public void setEndpointOverrides(List<EndpointCfg> epCfgs){
        this.epCfgs = epCfgs;
    }

    public List<EndpointCfg> getEndpointOverrides(){
        return this.epCfgs;
    }

    public List<Endpoint> getEndpoints(){
        return this.epts;
    }
    
    public List<Endpoint> getWebservicesEndpoints() throws IOException {
        scanForEndpoints();
        Iterator<AnnotationHandler> itr = this.hanlders.iterator();
        AnnotationHandler ah = null;
        Set<Endpoint> set = new HashSet<Endpoint>();
        List<Endpoint> epts = new ArrayList<Endpoint>();
        while (itr.hasNext()){
            ah = itr.next();
            set.addAll(ah.getEndPoints());
        }
        removeLocalConsumers(set);
        epts.addAll(set);
        return epts;
    }

    private void removeLocalConsumers(Set<Endpoint> epts){
        if ((epts != null) && (epts.size() > 0)){
            Set locProdConsumers = new HashSet<Endpoint>();
            // get all the consumers
            Iterator<Endpoint> itr = epts.iterator();
            Endpoint consumer = null;
            Endpoint clone = null;
            while (itr.hasNext()){
                consumer = itr.next();
                clone = null;
                if (consumer.getEndPointType() == Endpoint.EndPointType.Consumer){
                    clone = new Endpoint(consumer);
                    clone.setEndPointType(Endpoint.EndPointType.Provider);
                    if (epts.contains(clone)){
                        clone.setEndPointType(Endpoint.EndPointType.Consumer);
                        locProdConsumers.add(clone);
                    }
                }

            }
            epts.removeAll(locProdConsumers);
        }

    }

    protected String renameToSvcUnitExtension(String javaEEName){
        StringBuffer ret = new StringBuffer();
        int index = -1;
        if (javaEEName != null){
            index = javaEEName.lastIndexOf("."); //NOI18N
            if (index >= 0){
                ret.append(javaEEName.substring(0, index + 1));
                ret.append(SU_FILE_EXT);
            }
        }

        return ret.toString();
    }

    protected void createFolderIfNotExists(String dir){
        File flDir = new File(dir);
        flDir.mkdirs();
    }

    public String createJar(String dir, String additionalJbiFileDir) throws Exception {
        this.epts = getWebservicesEndpoints();

        // sun-resources
// Do not generate any Sun-resource.xml content.        
//        String resourcesStr = null;
//        // only scan for resources if deploying through CompApp
//        if (this.deployThruCA) {
//            try {
//                resourcesStr = scanForResources();
//            } catch (Exception e) {
//                e.printStackTrace();
//                // do not throw exception, let build process continue
//            }
//        }

        Iterator<JavaEEProject> itr = null;
        JarOutputStream jos = null;
        FileOutputStream fos = null;
        JarEntry je = null;
        String suName = SU_NAME;
        File jarFile = new File(this.jarPath);
        File instrumentedJarFile = null;
        suName = renameToSvcUnitExtension(jarFile.getName());
        String ret = null;

        try {
            instrumentedJarFile = new File(dir, suName);
            ret = instrumentedJarFile.getAbsolutePath();
            fos = new FileOutputStream(instrumentedJarFile);
            jos = new JarOutputStream(fos);
            je = new JarEntry(JBI_XML_ENTRY);
            jos.putNextEntry(je);
            // Stax implementation is not found for JBI NB project so use DOM.
            // writeJBIXML(epts, excludeEpts, jos);
            writeJBIXMLUsingDOM(epts, jos);

            // sun-resources
// Do not generate any Sun-resource.xml content.                    
//            if ((this.deployThruCA) && (resourcesStr != null)
//                                    && (!"".equals(resourcesStr))) {
//                je = new JarEntry(RES_XML_ENTRY);
//                jos.putNextEntry(je);
//                writeResourcesFile(resourcesStr, jos);
//            }

            if (additionalJbiFileDir != null){
                FileOutputStream jfos = null;
                try {
                    createFolderIfNotExists(additionalJbiFileDir);
                    File file = new File(additionalJbiFileDir
                        + File.separator + JBI_XML);
                    jfos = new FileOutputStream(file);
                    writeJBIXMLUsingDOM(epts, jfos);
                    jfos.flush();
                    jfos.close();
                    jfos = null;
                } catch (Exception ex){
                    logger.log(Level.SEVERE, "Writing jbi.xml.", ex);
                } finally {
                    try {
                        jfos.close();
                    } catch(Exception ex){
                        // ignore
                    }
                }
            }
            copyJar(jos);
        } finally {
            if (jos != null){
                try {
                    jos.close();
                }catch (Exception ex){
                    // Ignore
                }
            }
            if (fos != null){
                try {
                    fos.close();
                }catch (Exception ex){
                    // Ignore
                }
            }
        }

        return ret;
    }

    public String getJarName(){
        String ret = null;
        File jarFile = new File(this.jarPath);
        ret = jarFile.getName();

        int index = -1;
        if (ret != null){
            index = ret.lastIndexOf("."); //NOI18N
            if (index >= 0){
                ret = ret.substring(0, index);
            }
        }

        return ret;
    }

    protected void copyJar(JarOutputStream jos) throws IOException{
        byte[] buffer = new byte[1024 * 5];
        int bytesRead = 0;
        JarFile origJar = null;

        if (this.deployThruCA){
            File jarFile = new File(this.jarPath);
            try {
                origJar = new JarFile(jarFile);
                for (Enumeration entries = origJar.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    InputStream entryStream = origJar.getInputStream(entry);
                    jos.putNextEntry(entry);
                    while ((bytesRead = entryStream.read(buffer)) != -1) {
                        jos.write(buffer, 0, bytesRead);
                    }
                }
            } finally {
                if (origJar != null){
                    origJar.close();
                }
            }
        }
    }

    protected void scanForEndpoints() throws IOException {
        JarFile jf = new JarFile(this.jarPath);
        try {
            JarClassFileLoader cl = new JarClassFileLoader(jf, ""); //NOI18N
            Enumeration<JarEntry> jes = jf.entries();
            while(jes.hasMoreElements()){
                JarEntry je = jes.nextElement();
                if (je.getName().endsWith(".class")){  //NOI18N
                    logger.finest("Checking Annotation in:" + je.getName());
                    // Load the class only if if annotation present.
                    //if (ClassInfo.containsAnnotation(Channels.newChannel(jf.getInputStream(je)), je.getSize(), annotations)) {
                        handleAnnotations(cl, je);
                    //}
                }
            }
        } finally {
            if (jf != null){
                try {
                    jf.close();
                } catch (Exception ex){
                    //Ignore
                }
            }
        }
    }

    protected void addAnnotationHandler(AnnotationHandler handler){
        String annotationClass = handler.getAnnotationClassConstant();
        this.annotations.add(annotationClass);
        this.hanlders.add(handler);
    }


    protected void removeAnnotationHandler(AnnotationHandler handler){
        String annotationClass = handler.getAnnotationClassConstant();
        this.annotations.remove(annotationClass);
        this.hanlders.remove(handler);
    }

    protected URL getClassPathURL(){
        URL ret = null;
        try {
            ret = new URL("jar:file:" + this.jarPath + "!/"); //NOI18N
        } catch (Exception ex){
            logger.warning("Error while getting to to:" + this.jarPath);
        }

        return ret;
    }

    protected void resetHandlers(){
        Iterator<AnnotationHandler> itr = this.hanlders.iterator();
        AnnotationHandler ah = null;
        while (itr.hasNext()){
            ah = itr.next();
            ah.resetEndPoints();
        }
    }

    protected void handleAnnotations(ClassFileLoader cl, JarEntry je){
        try {
            ClassFile classFile = cl.getClassFileUsingJarEntry(je);
            Iterator itr = this.hanlders.iterator();
            while ( itr.hasNext() ){
                AnnotationHandler ah = (AnnotationHandler) itr.next();
                ah.handle(cl, classFile);
            }
        } catch (Throwable ex){
            logger.log(Level.WARNING, "Error while loading class:" + je.getName(), ex);
        }

    }

    public void setClassPathURLs(List<URL> nclasspathURLs) {
        this.classpathURLs = nclasspathURLs;
    }

    protected void init(){
        AnnotationHandler wsh = new WebServiceHandler();
        AnnotationHandler wsrh = new WebserviceRefHandler();
        AnnotationHandler wsc = new WebServiceClientHanlder();
        this.annotations.add(wsh.getAnnotationClassConstant());
        this.annotations.add(wsrh.getAnnotationClassConstant());
        this.annotations.add(wsc.getAnnotationClassConstant());
        this.hanlders.add(wsh);
        this.hanlders.add(wsrh);
        this.hanlders.add(wsc);
    }

    private boolean isActivateJavaEEHttpPort(Endpoint ep){
        boolean ret = true;
        if ((this.epCfgs != null) && (this.epCfgs.contains(ep))){
            int idx = this.epCfgs.indexOf(ep);
            if (idx > -1){
                EndpointCfg cfg = this.epCfgs.get(idx);
                ret = cfg.isActivateJavaEEHttpPort();
            }
        }
        
        return ret;
    }

    private boolean isActivateForNMR(Endpoint ep){
        boolean ret = true;
        if ((this.epCfgs != null) && (this.epCfgs.contains(ep))){
            int idx = this.epCfgs.indexOf(ep);
            if (idx > -1){
                EndpointCfg cfg = this.epCfgs.get(idx);
                ret = cfg.isActivateForNMR();
            }
        }
        
        return ret;
    }
    
    /**
     *
     *
     * For selective endpoint exposer.
     *
     * <javaee_ext:private-endpoints>
     *      <javaee_ext:private-endpoint endpoint-name="xxx" service-name="xxx" interface-name="xxx"/>
     *       <javaee_ext:private-endpoint endpoint-name="xxx" service-name="xxx" interface-name="xxx"/>
     *       .....
     *   </javaee_ext:private-endpoints>
     *
     *
     * @param epts
     * @param excludedEpts
     * @param os
     * @throws java.io.IOException
     */
    protected void writeJBIXMLUsingDOM(List<Endpoint> epts, OutputStream os) 
            throws IOException {
        try {
            Map<String, String> ns = getNamespacePrefixes(epts);
            Iterator<Map.Entry<String, String>> itr = null;
            Map.Entry entry = null;
            Iterator<Endpoint> itrEpts = null;
            Endpoint ept = null;
            String intfacePrefix = null;
            String svcPrefix = null;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element elemJbi = document.createElement(ELEM_JBI);
            Element elemSvcs = null;
            Element elemConsumesProvides = null;

            elemJbi.setAttribute(XMLNS, JBI_DEFAULT_NS);
            String nsPrefix = null;
            itr = ns.entrySet().iterator();
            while (itr.hasNext()){
                entry = itr.next();
                nsPrefix = XMLNS_COLON + (String) entry.getValue(); 
                elemJbi.setAttribute(nsPrefix, (String)entry.getKey());
            }
            elemJbi.setAttribute(XMLNS_COLON + MAPPING_EXT, MAPPING_NS);
            elemJbi.setAttribute(ATTR_VERSION, JBI_VERSION);
            document.appendChild(elemJbi);

            elemSvcs = document.createElement(ELEM_SERVICES);
            elemSvcs.setAttribute("binding-component", "false"); //NOI18N
            elemJbi.appendChild(elemSvcs);

            // Element elemMappings = document.createElementNS(MAPPING_NS, MAPPING_ELEMS); //NOI18N
            Element elemMappings = document.createElement(MAPPING_EXT + ":" + MAPPING_ELEMS); //NOI18N
            Element elemPvtEps = document.createElement(MAPPING_EXT + ":" + PVT_EPS_ELEM); //NOI18N

            itrEpts = epts.iterator();
            // Order is important for schematically valid jbi.xml file.
            // Provider should come before consumers
            while(itrEpts.hasNext()){
                ept = itrEpts.next();
                if (ept.getEndPointType().equals(Endpoint.EndPointType.Provider)){
                    elemConsumesProvides = document.createElement(PROVIDES);
                    elemConsumesProvides.setAttribute(EP_NAME, MAPPING_PREFIX + ept.getEndPointName());  
                    intfacePrefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(INTERFACE_NAME, intfacePrefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                    Element elemMapping = document.createElement(MAPPING_EXT + ":" + MAPPING_ELEM); //NOI18N
                    Element elemJavaEpt = document.createElement(MAPPING_EXT + ":" + MAPPING_JAVA_ELEM); //NOI18N
                    Element elemWsdlEpt = document.createElement(MAPPING_EXT+ ":" + MAPPING_WSDL_ELEM); //NOI18N
                    elemJavaEpt.setAttribute(EP_NAME, MAPPING_PREFIX + ept.getEndPointName());  
                    elemWsdlEpt.setAttribute(EP_NAME, ept.getEndPointName());
                    elemJavaEpt.setAttribute(INTERFACE_NAME, intfacePrefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                    elemWsdlEpt.setAttribute(INTERFACE_NAME, intfacePrefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                    svcPrefix = ns.get(ept.getServiceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(SERVICE_NAME, svcPrefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    elemSvcs.appendChild(elemConsumesProvides);

                    elemJavaEpt.setAttribute(SERVICE_NAME, svcPrefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    elemWsdlEpt.setAttribute(SERVICE_NAME, svcPrefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    elemJavaEpt.setAttribute(TYPE, PROVIDER);  
                    elemWsdlEpt.setAttribute(TYPE, PROVIDER);  
                    elemMapping.appendChild(elemJavaEpt);
                    elemMapping.appendChild(elemWsdlEpt);
                    elemMappings.appendChild(elemMapping);

                    if (!isActivateJavaEEHttpPort(ept)) {
                        Element elemPvtEp = document.createElement(MAPPING_EXT + ":" + PVT_EP_ELEM); //NOI18N
                        elemPvtEp.setAttribute(EP_NAME, ept.getEndPointName());
                        elemPvtEp.setAttribute(INTERFACE_NAME, intfacePrefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                        elemPvtEp.setAttribute(SERVICE_NAME, svcPrefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                        elemPvtEps.appendChild(elemPvtEp);
                    }
                }
            }

            itrEpts = epts.iterator();
            while(itrEpts.hasNext()){
                ept = itrEpts.next();
                if (ept.getEndPointType().equals(Endpoint.EndPointType.Consumer) && isActivateForNMR(ept)){
                    elemConsumesProvides = document.createElement(CONSUMES); //NOI18N
                    elemConsumesProvides.setAttribute(EP_NAME, MAPPING_PREFIX + ept.getEndPointName());  //NOI18N
                    intfacePrefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(INTERFACE_NAME, intfacePrefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                    Element elemMapping = document.createElement(MAPPING_EXT + ":" + MAPPING_ELEM); //NOI18N
                    Element elemJavaEpt = document.createElement(MAPPING_EXT+ ":" + MAPPING_JAVA_ELEM); //NOI18N
                    Element elemWsdlEpt = document.createElement(MAPPING_EXT+ ":" + MAPPING_WSDL_ELEM); //NOI18N
                    elemJavaEpt.setAttribute(EP_NAME, MAPPING_PREFIX + ept.getEndPointName());  //NOI18N
                    elemWsdlEpt.setAttribute(EP_NAME, ept.getEndPointName());  
                    elemJavaEpt.setAttribute(INTERFACE_NAME, intfacePrefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                    elemWsdlEpt.setAttribute(INTERFACE_NAME, intfacePrefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                    svcPrefix = ns.get(ept.getServiceName().getNamespaceURI());
                    elemConsumesProvides.setAttribute(SERVICE_NAME, svcPrefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    elemSvcs.appendChild(elemConsumesProvides);

                    elemJavaEpt.setAttribute(SERVICE_NAME, svcPrefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    elemWsdlEpt.setAttribute(SERVICE_NAME, svcPrefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    elemJavaEpt.setAttribute( TYPE,CONSUMER);  
                    elemWsdlEpt.setAttribute(TYPE, CONSUMER);  
                    elemMapping.appendChild(elemJavaEpt);
                    elemMapping.appendChild(elemWsdlEpt);
                    elemMappings.appendChild(elemMapping);
                }
            }
            elemSvcs.appendChild(elemMappings);
            elemSvcs.appendChild(elemPvtEps);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(os);

            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes"); // NOI18N

            // indent the output to make it more legible...
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // NOI18N
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            transformer.transform(source, result);
            os.flush();
        } catch (Exception ex) {
            // IllegalArgumentException DOMException TransformerConfigurationException ParserConfigurationException
            // TransformerFactoryConfigurationError TransformerException
            ex.printStackTrace();
            throw new IOException("Exception while writing jbi.xml:" + ex.getMessage());
        }
    }

    private Map<String, String> getNamespacePrefixes(List<Endpoint> epts){
        Map<String, String> ret = new HashMap<String, String>();
        String tns = null;
        int counter = 0;
        Iterator<Endpoint> itr = epts.iterator();
        Endpoint ept = null;
        QName qn = null;

        while (itr.hasNext()){
            ept = itr.next();
            qn = ept.getInterfaceName();
            if (qn != null){
                tns = qn.getNamespaceURI();
                if (tns != null) {
                    if (ret.get(tns) == null){
                        ret.put(tns, "ns" + counter++);//NOI18N
                    }
                }
            }

            qn = ept.getServiceName();
            if (qn != null){
                tns = qn.getNamespaceURI();
                if (tns != null) {
                    if (ret.get(tns) == null){
                        ret.put(tns, "ns" + counter++);//NOI18N
                    }
                }
            }
        }
        return ret;
    }

    protected String scanForResources() throws Exception {
        // return SunResourcesUtil.scanForSunResources(this.p);
        return SunResourcesUtil.scanForSunResources(this.resourceFolder);
    }

    protected void writeResourcesFile(String xmlContent, OutputStream os) throws IOException {
        PrintWriter out = new PrintWriter(os);
        out.print(xmlContent);
        out.flush();
    }

    public void setResourceFolder(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }
}
