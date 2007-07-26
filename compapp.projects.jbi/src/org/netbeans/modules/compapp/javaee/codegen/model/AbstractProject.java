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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

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
    
    private static final String SU_NAME = "javaee_su.jar";  //NOI18N
    private static final String JBI_XML_ENTRY = "META-INF/jbi.xml"; //NOI18N
    private static final String JBI_DEFAULT_NS = "http://java.sun.com/xml/ns/jbi"; //NOI18N
    private static final String XML_VERSION = "1.0" ; //NOI18N
    private static final String JBI_VERSION = "1.0" ; //NOI18N
    private static final String SU_FILE_EXT = "jar" ;     //NOI18N
    private static final String ATTR_VERSION = "version" ; //NOI18N
    private static final String ELEM_SERVICES = "services" ;   //NOI18N
    private static final String ELEM_JBI = "jbi" ;     //NOI18N
    
    // resources related
    private static final String RES_XML_ENTRY = "META-INF/sun-resources.xml"; //NOI18N

    // 06/04/07, JavaEE SE endpoint mapping
    private static final String MAPPING_PREFIX = "javaee_" ;     //NOI18N
    private static final String MAPPING_EXT = "javaee_ext" ;     //NOI18N
    private static final String MAPPING_NS = "http://javaee.serviceengine.sun.com/endpoint/naming/extension" ;     //NOI18N
    private static final String MAPPING_ELEMS = "ept-mappings" ;     //NOI18N
    private static final String MAPPING_ELEM = "ept-mapping" ;     //NOI18N
    private static final String MAPPING_JAVA_ELEM = "java-ept" ;     //NOI18N
    private static final String MAPPING_WSDL_ELEM = "wsdl-ept" ;     //NOI18N

    private static Logger logger = Logger.getLogger(AbstractProject.class.getName());
    
    public AbstractProject(String nJarPath) {
        this.jarPath = nJarPath;
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
    
    private String renameToSvcUnitExtension(String javaEEName){
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

    private void createFolderIfNotExists(String dir){
        File flDir = new File(dir);     
        flDir.mkdirs();
    }
    
    public String createJar(String dir, String additionalJbiFileDir) throws Exception {
        List<Endpoint> epts = getWebservicesEndpoints();
        
        // sun-resources
        String resourcesStr = null;
        // only scan for resources if deploying through CompApp
        if (this.deployThruCA) {
            try {
                resourcesStr = scanForResources();
            } catch (Exception e) {
                e.printStackTrace();
                // do not throw exception, let build process continue
            }
        }
        
        Iterator<JavaEEProject> itr = null;
        JarOutputStream jos = null;
        FileOutputStream fos = null;
        JarEntry je = null;
        Set<Endpoint> excludeEpts = new HashSet<Endpoint>();
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
            writeJBIXMLUsingDOM(epts, excludeEpts, jos);

            // sun-resources
            if ((this.deployThruCA) && (resourcesStr != null) 
                                    && (!"".equals(resourcesStr))) {
                je = new JarEntry(RES_XML_ENTRY);
                jos.putNextEntry(je);
                writeResourcesFile(resourcesStr, jos);
            } 

            if (additionalJbiFileDir != null){
                FileOutputStream jfos = null;
                try {
                    createFolderIfNotExists(additionalJbiFileDir);
                    File file = new File(additionalJbiFileDir + File.separator + "jbi.xml"); //NOI18N
                    jfos = new FileOutputStream(file); 
                    writeJBIXMLUsingDOM(epts, excludeEpts, jfos);                
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
            JarClassFileLoader cl = new JarClassFileLoader(jf, "");
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
            ret = new URL("jar:file:" + this.jarPath + "!/");
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

    protected void handleAnnotations(JarClassFileLoader cl, JarEntry je){
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
    
    private void writeJBIXMLUsingDOM(List<Endpoint> epts, Set<Endpoint> excludedEpts, OutputStream os)
    throws IOException {
        try {
            Map<String, String> ns = getNamespacePrefixes(epts);
            Iterator<Map.Entry<String, String>> itr = null;
            Map.Entry entry = null;
            Iterator<Endpoint> itrEpts = null;
            Endpoint ept = null;
            String prefix = null;
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element elemJbi = document.createElement(ELEM_JBI);
            Element elemSvcs = null;
            Element elemConsumesProvides = null;

            elemJbi.setAttribute("xmlns", JBI_DEFAULT_NS); //NOI18N
            String nsPrefix = null;
            itr = ns.entrySet().iterator();
            while (itr.hasNext()){
                entry = itr.next();
                nsPrefix = "xmlns:" + (String) entry.getValue(); //NOI18N
                elemJbi.setAttribute(nsPrefix, (String)entry.getKey());
            }
            elemJbi.setAttribute("xmlns:"+MAPPING_EXT, MAPPING_NS);
            elemJbi.setAttribute("version", JBI_VERSION); //NOI18N
            document.appendChild(elemJbi);
            
            elemSvcs = document.createElement(ELEM_SERVICES);
            elemSvcs.setAttribute("binding-component", "false"); //NOI18N
            elemJbi.appendChild(elemSvcs);

            // Element elemMappings = document.createElementNS(MAPPING_NS, MAPPING_ELEMS); //NOI18N
            Element elemMappings = document.createElement(MAPPING_EXT+":"+MAPPING_ELEMS); //NOI18N

            itrEpts = epts.iterator();
            // Order is important for schematically valid jbi.xml file.
            // Provider should come before consumers
            while(itrEpts.hasNext()){
                ept = itrEpts.next();
                if (!excludedEpts.contains(ept)){
                    if (ept.getEndPointType().equals(Endpoint.EndPointType.Provider)){
                        elemConsumesProvides = document.createElement("provides"); //NOI18N
                        elemConsumesProvides.setAttribute("endpoint-name", MAPPING_PREFIX + ept.getEndPointName());  //NOI18N
                        prefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                        elemConsumesProvides.setAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                        Element elemMapping = document.createElement(MAPPING_EXT+":"+ MAPPING_ELEM); //NOI18N
                        Element elemJavaEpt = document.createElement(MAPPING_EXT+":"+ MAPPING_JAVA_ELEM); //NOI18N
                        Element elemWsdlEpt = document.createElement(MAPPING_EXT+":"+ MAPPING_WSDL_ELEM); //NOI18N
                        elemJavaEpt.setAttribute("endpoint-name", MAPPING_PREFIX + ept.getEndPointName());  //NOI18N
                        elemWsdlEpt.setAttribute("endpoint-name", ept.getEndPointName());  //NOI18N
                        elemJavaEpt.setAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                        elemWsdlEpt.setAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                        prefix = ns.get(ept.getServiceName().getNamespaceURI());
                        elemConsumesProvides.setAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                        elemSvcs.appendChild(elemConsumesProvides);

                        elemJavaEpt.setAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                        elemWsdlEpt.setAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                        elemJavaEpt.setAttribute("type", "provider");  //NOI18N
                        elemWsdlEpt.setAttribute("type", "provider");  //NOI18N
                        elemMapping.appendChild(elemJavaEpt);
                        elemMapping.appendChild(elemWsdlEpt);
                        elemMappings.appendChild(elemMapping);
                    }
                }
            }
            
            itrEpts = epts.iterator();
            while(itrEpts.hasNext()){
                ept = itrEpts.next();
                if (!excludedEpts.contains(ept)){                    
                    if (ept.getEndPointType().equals(Endpoint.EndPointType.Consumer)){
                        elemConsumesProvides = document.createElement("consumes"); //NOI18N
                        elemConsumesProvides.setAttribute("endpoint-name", MAPPING_PREFIX + ept.getEndPointName());  //NOI18N
                        prefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                        elemConsumesProvides.setAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                        Element elemMapping = document.createElement(MAPPING_EXT+":"+ MAPPING_ELEM); //NOI18N
                        Element elemJavaEpt = document.createElement(MAPPING_EXT+":"+ MAPPING_JAVA_ELEM); //NOI18N
                        Element elemWsdlEpt = document.createElement(MAPPING_EXT+":"+ MAPPING_WSDL_ELEM); //NOI18N
                        elemJavaEpt.setAttribute("endpoint-name", MAPPING_PREFIX + ept.getEndPointName());  //NOI18N
                        elemWsdlEpt.setAttribute("endpoint-name", ept.getEndPointName());  //NOI18N
                        elemJavaEpt.setAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                        elemWsdlEpt.setAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N

                        prefix = ns.get(ept.getServiceName().getNamespaceURI());
                        elemConsumesProvides.setAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                        elemSvcs.appendChild(elemConsumesProvides);

                        elemJavaEpt.setAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                        elemWsdlEpt.setAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                        elemJavaEpt.setAttribute("type", "consumer");  //NOI18N
                        elemWsdlEpt.setAttribute("type", "consumer");  //NOI18N
                        elemMapping.appendChild(elemJavaEpt);
                        elemMapping.appendChild(elemWsdlEpt);
                        elemMappings.appendChild(elemMapping);
                    }
                }
            }
            elemSvcs.appendChild(elemMappings);

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
    
    private void writeJBIXML(List<Endpoint> epts, Set<Endpoint> excludedEpts, OutputStream os) {
        try {
            Map<String, String> ns = getNamespacePrefixes(epts);
            Map.Entry entry = null;
            Iterator<Map.Entry<String, String>> itr = null;
            Iterator<Endpoint> itrEpts = null;
            Endpoint ept = null;
            String prefix = null;
            
            XMLOutputFactory of = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = of.createXMLStreamWriter(os);
            
            // XML Document
            writer.writeStartDocument("UTF-8", XML_VERSION); //NOI18N
            
            // jbi element
            writer.writeStartElement(ELEM_JBI);
            writer.writeDefaultNamespace(JBI_DEFAULT_NS);
            
            itr = ns.entrySet().iterator();
            while (itr.hasNext()){
                entry = itr.next();
                writer.writeNamespace((String) entry.getValue(), (String)entry.getKey());
            }
            writer.writeAttribute(ATTR_VERSION, JBI_VERSION);
            
            // services element
            writer.writeStartElement(ELEM_SERVICES);
            writer.writeAttribute("binding-component", "false"); //NOI18N
            
            // Order is important for jbi.xml file to be schematically correct.
            // Proverds should come always before Consumers.
            // Providers
            itrEpts = epts.iterator();
            while(itrEpts.hasNext()){
                ept = itrEpts.next();
                if (!excludedEpts.contains(ept)){
                    if (ept.getEndPointType().equals(Endpoint.EndPointType.Provider)){
                        writer.writeEmptyElement("provides"); //NOI18N
                        writer.writeAttribute("endpoint-name", ept.getEndPointName());  //NOI18N
                        prefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                        writer.writeAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                        
                        prefix = ns.get(ept.getServiceName().getNamespaceURI());
                        writer.writeAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    }
                }
            }

            // Consumers
            itrEpts = epts.iterator();
            while(itrEpts.hasNext()){
                ept = itrEpts.next();
                if (!excludedEpts.contains(ept)){
                    if (ept.getEndPointType().equals(Endpoint.EndPointType.Consumer)){
                        writer.writeEmptyElement("consumes"); //NOI18N
                        writer.writeAttribute("endpoint-name", ept.getEndPointName());  //NOI18N
                        prefix = ns.get(ept.getInterfaceName().getNamespaceURI());
                        writer.writeAttribute("interface-name", prefix + ":" + ept.getInterfaceName().getLocalPart()); //NOI18N
                        
                        prefix = ns.get(ept.getServiceName().getNamespaceURI());
                        writer.writeAttribute("service-name", prefix + ":" + ept.getServiceName().getLocalPart()); //NOI18N
                    }
                }
            }

            writer.writeEndElement();  // services
            writer.writeEndElement();  // jbi
            writer.writeEndDocument();  // XML doc
            writer.flush();
            // Do not close as we might need JarOutputStream to write other files.
        } catch (Exception ex){
            ex.printStackTrace();
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
    
    private String scanForResources() throws Exception {
        // return SunResourcesUtil.scanForSunResources(this.p);
        return SunResourcesUtil.scanForSunResources(this.resourceFolder);
    }
    
    private void writeResourcesFile(String xmlContent, OutputStream os) throws IOException {
        PrintWriter out = new PrintWriter(os);
        out.print(xmlContent);
        out.flush();
    }
    
    public void setResourceFolder(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }
}
