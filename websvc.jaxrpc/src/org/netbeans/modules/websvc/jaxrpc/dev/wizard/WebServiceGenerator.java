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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.jaxrpc.dev.wizard;

import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import java.net.URI;
import org.openide.filesystems.FileLock;
import java.io.OutputStream;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.openide.nodes.Node;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.Configuration;
import org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.InterfaceType;
import org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.WsdlType;
import org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.wscreation.Bean;
import org.netbeans.modules.websvc.wsdl.config.PortInformationHandler;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class WebServiceGenerator {
    private WSGenerationUtil wsgenUtil = new WSGenerationUtil();
    private static final String WEBSERVICE_TEMPLATE = WSGenerationUtil.TEMPLATE_BASE+"WSImplBean.xml"; //NOI18N
    private static final String WEBSERVICEJAVAEE5_TEMPLATE = WSGenerationUtil.TEMPLATE_BASE+"WSImplBeanJavaEE5.xml"; //NOI18N
    private static final String INTERFACE_TEMPLATE = WSGenerationUtil.TEMPLATE_BASE+"WSInterface.xml"; //NOI18N
    private static final String HANDLER_TEMPLATE = WSGenerationUtil.TEMPLATE_BASE+"MessageHandler.xml"; //NOI18N
    public static final String WSDL_TEMPLATE = WSGenerationUtil.TEMPLATE_BASE+"WSDL.xml"; //NOI18N
    
    public static final String EJB21_EJBCLASS = "Templates/J2EE/EJB21/SessionEjbClass.java"; // NOI18N
    
    private String implBeanClass = "";
    private String intfClass = "";
    private String targetNS = null;
    private String soapBinding = "";
    private String portTypeName = null;
    public static final String WEBSERVICES_DD = "webservices";//NOI18N
    private WebServicesSupport wsSupport;
    private String wsName;
    private FileObject pkg;
    private Project project;
    private List importedSchemaList;
    // flag indicating if service name need to be changed (happens when portName=service name)
    // Issue 58509
    private boolean changeWsName;
    private String[] wscompileFeatures;
    
    public WebServiceGenerator(WebServicesSupport wsSupport, String wsName, FileObject pkg, Project project) {
        this.wsSupport = wsSupport;
        this.wsName = wsName;
        this.pkg = pkg;
        this.project = project;
    }
    
    public WebServiceGenerator(FileObject pkg, Project project){
        this.pkg = pkg;
        this.project = project;
    }
    
    public void generateWebService() throws IOException {
        generateWebService(null);
    }
    
    public FileObject generateWSDL(String template, String wsName, String soapBinding, String portTypeName, FileObject folder, FileObject originalFolder, String wsdlName, StreamSource source) throws IOException {
        return wsgenUtil.generateWSDL(template, wsName, soapBinding, portTypeName, folder, originalFolder, wsdlName, source);
    }
    
    public FileObject generateWSDL(String template, String wsName, String soapBinding, String portTypeName, FileObject folder, String wsdlName, StreamSource source) throws IOException {
        return wsgenUtil.generateWSDL(template, wsName, soapBinding, portTypeName, folder, wsdlName, source);
    }
    
    /**
     * Parse the original wsdl to obtain the portType name, targetNamespace and SOAP
     * binding. Note that if no SOAP binding in obtained, the first portType
     * name that is encountered is used.
     * @return changed service name or null
     */
    public String parseWSDL(InputStream  wsdlInputStream) throws IOException, NoWSPortDefinedException {
        //parse the wsdl to get SEI class name and target namespace
        
        String changedWsName=null;
        
        PortInformationHandler handler = new PortInformationHandler();
        
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(wsdlInputStream, handler);
        } catch(ParserConfigurationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            String mes = NbBundle.getMessage(WebServiceGenerator.class, "ERR_WsdlParseFailure"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return changedWsName;
        } catch(SAXException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            String mes = NbBundle.getMessage(WebServiceGenerator.class, "ERR_WsdlParseFailure"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return changedWsName;
        }
        
        List entirePortList = handler.getEntirePortList();
        if (entirePortList.isEmpty()) {
            throw new NoWSPortDefinedException();
        }
        Iterator iterator = entirePortList.iterator();
        String firstPortType = null;
        while(iterator.hasNext()) {
            PortInformationHandler.PortInfo portInfo = (PortInformationHandler.PortInfo)iterator.next();
            if(firstPortType == null){
                //there should at least be one portType in the WSDL
                firstPortType = portInfo.getPortType();
            }
            //get the first SOAP binding
            if(portInfo.getBindingType() != null &&
                    portInfo.getBindingType().equals("http://schemas.xmlsoap.org/wsdl/soap")) { //NOI18N
                portTypeName = portInfo.getPortType();
                soapBinding = portInfo.getBinding();
                break;
            }
        }
        //if no soap binding was found, use the first portType encountered
        if(portTypeName == null) {
            portTypeName = firstPortType;
        }
        if (portTypeName == null) {
            throw new NoWSPortDefinedException();
        }
        //get name of SEI and Impl class name from portTypeName. In future, we should get this from
        //wscompile spi
        intfClass = WSGenerationUtil.getSelectedPackageName(pkg, project) + "." + normalizePortTypeName(portTypeName);
        implBeanClass = intfClass + "_Impl"; //NOI18N
        targetNS = handler.getTargetNamespace();
        importedSchemaList = handler.getImportedSchemas();
        if (changeWsName) changedWsName=wsName+"_Service"; //NOI18N
        Set features = handler.getWscompileFeatures();
        wscompileFeatures=new String[features.size()];
        features.toArray(wscompileFeatures);
        return changedWsName;
    }
    
    /*
     * if portTypeName does not start with uppercase character, make it
     * uppercase. This is the convention used by wscompile in naming
     * the generated SEI and impl bean classes
     */
    private String normalizePortTypeName(String portTypeName) {
        if (portTypeName == null) {
            return "unknown"; //NOI18N
        }
        String first = portTypeName.substring(0,1);
        String result = first.toUpperCase() + ((portTypeName.length() > 1) ? portTypeName.substring(1) : "");
        if (result.equals(wsName)) {
            changeWsName=true;
        }
        return result;
        
    }
    
    public  void generateMessageHandler(String handlerName) throws IOException{
        String pkgName = wsgenUtil.getSelectedPackageName(pkg, project);
        Bean b = wsgenUtil.getDefaultBean();
        b.setCommentDataWsName(handlerName);
        b.setClassname(true);
        b.setClassnameName(handlerName);
        if(pkgName != null) {
            b.setClassnamePackage(pkgName);
        }
        String handlerClass =  wsgenUtil.getFullClassName(pkgName,
                wsgenUtil.generateClass(HANDLER_TEMPLATE, b, pkg, true));
    }
    
    public void generateWebService( Node[] nodes) throws IOException {
        String pkgName = wsgenUtil.getSelectedPackageName(pkg, project);
        
        Bean b = wsgenUtil.getDefaultBean();
        b.setCommentDataWsName(wsName);
        b.setClassname(true);
        b.setDelegateData("");
        
        b.setClassnameName(wsgenUtil.getBeanClassName(wsName));
        
        if (pkgName != null) {
            b.setClassnamePackage(pkgName);
        }
        
        //FIXE-ME: need to delegate impl bean class to the web module
        if (project.getLookup().lookup(WebModuleImplementation.class) != null) {
            implBeanClass = wsgenUtil.getFullClassName(pkgName, wsgenUtil.generateClass(WEBSERVICE_TEMPLATE, b, pkg, true));
            b.setClassnameName(wsgenUtil.getSEIName(wsName));
            intfClass = wsgenUtil.getFullClassName(pkgName, wsgenUtil.generateClass(INTERFACE_TEMPLATE, b, pkg, false));
            if (implBeanClass!=null) {
                // Retouche
                //                boolean rollback = true;
                //                JMIUtils.beginJmiTransaction(true);
                //                try {
                //                    JavaClass jc = JMIUtils.findClass(implBeanClass, pkg);
                //                    if (jc != null && jc.isValid()) {
                //                        addDelegateMethod(nodes, jc);
                //                    }
                //                    rollback=false;
                //                } catch (Exception ex) {
                //                } finally {
                //                    JMIUtils.endJmiTransaction(rollback);
                //                }
            }
        } else {
            try {
                Map templateParameters = new HashMap<String, String>();
                FileObject ejbClassFO = GenerationUtils.createClass(EJB21_EJBCLASS, pkg, b.getClassnameName(), null, templateParameters);
                implBeanClass = wsgenUtil.getFullClassName(pkgName, ejbClassFO.getName());
                b.setClassnameName(wsgenUtil.getSEIName(wsName));
                intfClass = wsgenUtil.getFullClassName(pkgName, wsgenUtil.generateClass(INTERFACE_TEMPLATE, b, pkg, false));
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            if (implBeanClass!=null) {
                // Retouche
                //                boolean rollback = true;
                //                JMIUtils.beginJmiTransaction(true);
                //                try {
                //                    JavaClass jc = JMIUtils.findClass(implBeanClass, pkg);
                //                    if (jc != null) {
                //                        addDelegateMethod(nodes, jc);
                //                    }
                //                    rollback=false;
                //                } catch (Exception ex) {
                //                } finally {
                //                    JMIUtils.endJmiTransaction(rollback);
                //                }
            }
        }
    }
    
    
    //    public void addReferences(String beanClassName, Node[] nodes) {
    //        for(int i = 0; i < nodes.length; i++) {
    //            Node node = nodes[i];
    //            EjbReference ref = (EjbReference)node.getCookie(EjbReference.class);
    //            if(ref != null) {
    //                EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)project.getLookup()
    //                .lookup(EnterpriseReferenceContainer.class);
    //                if(ref.supportsRemoteInvocation()) {
    //                    EjbRef ejbRef = ref.createRef();
    //                    if(ejbRef.getEjbRefType().equals("Session")) { //NOI18N
    //                        try {
    //// Retouche
    ////                            erc.addEjbReference(ejbRef, beanClassName, ref.getClientJarTarget());
    //                        }
    //                        catch(Exception e) {
    //                            throw new RuntimeException(e.getMessage());
    //                        }
    //                    }
    //                }
    //                if(!ref.supportsRemoteInvocation() &&
    //                ref.supportsLocalInvocation()) {
    //                    EjbLocalRef ejbLocalRef = ref.createLocalRef();
    //                    if(ejbLocalRef.getEjbRefType().equals("Session")) { //NOI18N
    //                        try {
    //// Retouche
    ////                            erc.addEjbLocalReference(ejbLocalRef, beanClassName, ref.getClientJarTarget());
    //                        }
    //                        catch(Exception e) {
    //                            throw new RuntimeException(e.getMessage());
    //                        }
    //                    }
    //                }
    //            }
    //            else  //Java class
    //            {
    //// Retouche
    ////                JavaClass classElement = JMIUtils.getJavaClassFromNode(node);
    ////                assert (classElement != null);
    ////
    ////                //find out if the class is in the same project or not
    ////                FileObject srcFile = JavaMetamodel.getManager().getDataObject(classElement.getResource()).getPrimaryFile();
    ////
    ////                Project p = FileOwnerQuery.getOwner(srcFile);
    ////                if(p != null) //project can be determined. if not, class is
    ////                    //not in any  project and is assumed to already be
    ////                    //in the classpath
    ////                {
    ////                    if(!project.equals(p )) //not in same project
    ////                    {
    ////                        AntArtifact target = AntArtifactQuery.findArtifactsByType(p, getAntArtifactType(p))[0];
    ////                        ReferenceHelper helper = wsSupport.getReferenceHelper();
    ////                        if(helper.addReference(target)) {
    ////                            AntProjectHelper antHelper = wsSupport.getAntProjectHelper();
    ////                            EditableProperties ep =
    ////                            antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
    ////                            String s = ep.getProperty("javac.classpath"); //FIX-ME:get from project
    ////                            s += File.pathSeparatorChar + helper.createForeignFileReference(target);
    ////                            ep.setProperty("javac.classpath", s);
    ////                            antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    ////                        }
    ////                        try {
    ////                            ProjectManager.getDefault().saveProject(project);
    ////                        }
    ////                        catch(java.io.IOException e) {
    ////                            throw new RuntimeException(e.getMessage());
    ////                        }
    ////                    }
    ////                }
    //            }
    //        }
    //    }
    
    // Retouche
    //    public void addDelegateMethod(Node[] nodes, final JavaClass jc) {
    //        if (nodes == null) return;
    //        for (int i = 0; i < nodes.length; i++) {
    //            Node node = nodes[i];
    //            EjbReference ref = (EjbReference)node.getCookie(EjbReference.class);
    //            if (ref != null) {
    //                if (ref.supportsRemoteInvocation()) {
    //                    EjbRef ejbRef = ref.createRef();
    //                    if (ejbRef.getEjbRefType().equals("Session")) { //NOI18N
    //                        try {
    //                            Feature f = ref.generateReferenceCode(jc, ejbRef, false);
    //                            if (f instanceof Method) {
    //                                Method method = (Method) f;
    //                                //if Home is returned, add comment on how to get EJB
    //                                if(method.getType().getName().equals(ejbRef.getHome())) {
    //                                    addMethodJavaDocForHome(method, ejbRef.getHome(), ejbRef.getRemote());
    //                                }
    //                                ((Method) method).setJavadocText(NbBundle.getMessage(WebServiceGenerator.class,"MSG_JAVADOC_LOOKUP_REMOTE"));
    //                            };
    //                        } catch(Exception e) {
    //                            throw new RuntimeException(e.getMessage());
    //                        }
    //                    }
    //                }
    //                if (!ref.supportsRemoteInvocation() && ref.supportsLocalInvocation()) {
    //                    EjbLocalRef ejbLocalRef = ref.createLocalRef();
    //                    if (ejbLocalRef.getEjbRefType().equals("Session")) {
    //                        try {
    //                            Feature f = ref.generateReferenceCode(jc, ejbLocalRef, false);
    //                            if (f instanceof Method) {
    //                                Method method = (Method) f;
    //                                //if LocalHome is returned, add comment on how to get EJB
    //                                if (method.getType().getName().equals(ejbLocalRef.getLocalHome())) {
    //                                    addMethodJavaDocForHome(method, ejbLocalRef.getLocalHome(), ejbLocalRef.getLocal());
    //                                }
    //                                ((Method) method).setJavadocText(NbBundle.getMessage(WebServiceGenerator.class,"MSG_JAVADOC_LOOKUP_LOCAL"));
    //                            }
    //                        } catch (Exception e) {
    //                            throw new RuntimeException(e.getMessage());
    //                        }
    //                    }
    //                }
    //            } else { //Java class
    //                JavaClass classElement = JMIUtils.getJavaClassFromNode(node);
    //                assert (classElement != null);
    //
    //                JavaModelPackage jmp = (JavaModelPackage) classElement.refImmediatePackage();
    //
    //                Field field = jmp.getField().createField();
    //                field.setName(classElement.getName());
    //                String name = varFromName(classElement.getName());
    ////                buffer.append(field.getName() + " " + name + ";");
    //            }
    //        }
    //    }
    //
    //    private Method createClone(JavaClass jc, Method method, String javadoc) {
    //        Method clonnedMethod = JMIUtils.createMethod(jc);
    //        clonnedMethod.setBodyText(method.getBodyText());
    //        clonnedMethod.setModifiers(method.getModifiers());
    //        clonnedMethod.setName(method.getName());
    //        clonnedMethod.setType(method.getType());
    //        clonnedMethod.setJavadocText(javadoc);
    //        return clonnedMethod;
    //    }
    
    private static String varFromName(final String name) {
        if(name.length() > 0) {
            StringBuffer buf = new StringBuffer(name);
            
            // If the first character is uppercase, make it lowercase for the variable name,
            // otherwise, prefix an underscore.
            if(Character.isUpperCase(buf.charAt(0))) {
                buf.setCharAt(0, Character.toLowerCase(buf.charAt(0)));
            } else {
                buf.insert(0, '_');
            }
            
            return buf.toString();
        } else {
            return "unknown"; // NOI18N
        }
    }
    
    //FIX-ME: Is there a better way to find the artifact type of a project?
    private String getAntArtifactType(Project project){
        AntArtifactProvider antArtifactProvider = (AntArtifactProvider)project.getLookup().lookup(AntArtifactProvider.class);
        AntArtifact[] artifacts = antArtifactProvider.getBuildArtifacts();
        return artifacts[0].getType();
    }
    
    // Retouche
    //    private void addMethodJavaDocForHome(Method method, String ejbHome, String ejbLocalOrRemote) throws JmiException {
    //        StringBuffer text = new StringBuffer("Use this method to instantiate the EJB: \n");
    //        String ejbHomeVar = varFromName(ejbHome.substring(ejbHome.lastIndexOf('.') + 1));
    //        text.append(ejbHome+ " " + ejbHomeVar + "  = " + method.getName() +"(); \n");
    //        String ejbLocalOrRemoteVar = varFromName(ejbLocalOrRemote.substring(ejbLocalOrRemote.lastIndexOf('.') + 1));
    //        text.append(ejbLocalOrRemote + " " + ejbLocalOrRemoteVar + " = " + ejbHomeVar + ".create(<args>);");
    //        method.setJavadocText(text.toString());
    //    }
    
    public String getServantClassName() {
        return implBeanClass;
    }
    
    public String getSEIClassName() {
        return intfClass;
    }
    
    public String getSEIBaseName() {
        return wsgenUtil.getBaseName(intfClass);
    }
    
    public String getSoapBinding(){
        return soapBinding;
    }
    
    public String getPortTypeName(){
        return portTypeName;
    }
    
    public List getImportedSchemas() {
        return importedSchemaList;
    }
    
    public String[] getWscompileFeatures() {
        return wscompileFeatures;
    }
    
    public void addWebServiceEntry(String seiClassName, String portTypeName, URI targetNS )
            throws java.io.IOException {
        //Create webservices.xml skeleton file if required
        if(wsSupport.getWebservicesDD() == null) {
            try {
                final FileObject wsxmlTemplate = Repository.getDefault().getDefaultFileSystem().
                        findResource("org-netbeans-modules-websvc-jaxrpc/webservices.xml"); //NOI18N
                System.out.println("wsxmlTemplate = " + wsxmlTemplate);
                final FileObject wsddFolder = wsSupport.getWsDDFolder();
                FileSystem fs = wsddFolder.getFileSystem();
                fs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        FileUtil.copyFile(wsxmlTemplate, wsddFolder, WEBSERVICES_DD);
                    }
                });
            } catch(IOException ioe) {
              ErrorManager.getDefault().notify(ioe);
            }
        }
        //Add web service entry in the webservices.xml DD file
        org.netbeans.modules.j2ee.dd.api.webservices.DDProvider wsDDProvider =
                org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault();
        
        Webservices webServices = wsDDProvider.getDDRoot(wsSupport.getWebservicesDD());
        if(webServices != null){
            try{
                WebserviceDescription wsDescription =
                        (WebserviceDescription)webServices.createBean("WebserviceDescription"); //NOI18N
                wsDescription.setWebserviceDescriptionName(wsName);
                ServiceImplBean serviceImplBean =
                        (ServiceImplBean)webServices.createBean("ServiceImplBean"); //NOI18N
                PortComponent portComponent =
                        (PortComponent)webServices.createBean("PortComponent"); //NOI18N
                portComponent.setPortComponentName(wsName);
                org.netbeans.modules.schema2beans.QName wsdlPortQName =
                        new org.netbeans.modules.schema2beans.QName(targetNS.toString(), //NOI18N
                        ((portTypeName == null) ? wsgenUtil.getBaseName(seiClassName) + "Port" : portTypeName + "Port"), //NOI18N
                        "wsdl-port_ns"); //TO-DO: get this from user(??)
                portComponent.setWsdlPort(wsdlPortQName);
                portComponent.setServiceEndpointInterface
                        (seiClassName);
                //add sevlet-link or ejb-link entry
                wsSupport.addServiceImplLinkEntry(serviceImplBean, wsName);
                String wsDDFolder = wsSupport.getArchiveDDFolderName();
                wsDescription.setWsdlFile( wsDDFolder + "/wsdl/" + wsName + ".wsdl"); //NOI18N
                wsDescription.setJaxrpcMappingFile(wsDDFolder +"/" + wsName + "-mapping.xml"); //NOI18N
                portComponent.setServiceImplBean(serviceImplBean);
                wsDescription.addPortComponent(portComponent);
                webServices.addWebserviceDescription(wsDescription);
                webServices.write(wsSupport.getWebservicesDD());
            }catch(ClassNotFoundException e){
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    public URI getTargetNS() throws java.net.URISyntaxException {
        if(targetNS != null) {
            return new URI(targetNS);
        }
        return  getDefaultTargetNS(wsName);
    }
    
    
    public URI getDefaultTargetNS(String wsName) throws java.net.URISyntaxException {
        return new URI("urn:" + wsName + "/wsdl");
    }
    
    public URI getDefaultTypeNS(String wsName) throws java.net.URISyntaxException {
        return new URI("urn:" + wsName + "/types");
    }
    
    //FIX-ME: Use DD beans in websvc/core
    public FileObject generateConfigFile(URI wsdlLocation)throws java.io.IOException {
        FileObject configFile = pkg.createData(wsName + "-config", "xml"); //NOI18N
        Configuration configuration = new Configuration();
        WsdlType wsdl = configuration.newWsdlType();
        wsdl.setLocation(wsdlLocation);
        wsdl.setPackageName(wsgenUtil.getSelectedPackageName(pkg, project));
        configuration.setWsdl(wsdl);
        
        FileLock lock = null;
        OutputStream out = null;
        try{
            lock = configFile.lock();
            out = configFile.getOutputStream(lock);
            configuration.write(out, "UTF-8"); //NOI18N
        } catch(IOException ioe){
            ErrorManager.getDefault().notify(ioe);
        } finally {
            if(lock != null)
                lock.releaseLock();
            if(out != null)
                out.close();
        }
        return configFile;
    }
    
    
    //FIX-ME: Use DD beans in websvc/core
    public FileObject generateConfigFile(String seiClassName, String servantClassName, URI targetNS, URI typeNS) throws java.io.IOException {
        FileObject configFile = pkg.createData(wsName + "-config", "xml"); //NOI18N
        
        Configuration configuration = new Configuration();
        org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.ServiceType service =
                new org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.ServiceType();
        service.setName(wsName);
        service.setTargetNamespace(targetNS);
        service.setTypeNamespace(typeNS);
        service.setPackageName(wsgenUtil.getSelectedPackageName(pkg, project));
        InterfaceType interf = new InterfaceType();
        interf.setName(seiClassName);
        interf.setServantName(servantClassName);
        service.setInterface(new InterfaceType[] {interf});
        configuration.setService(service);
        
        FileLock lock = null;
        OutputStream out = null;
        try{
            lock = configFile.lock();
            out = configFile.getOutputStream(lock);
            configuration.write(out, "UTF-8"); //NOI18N
        } catch(IOException ioe){
            ErrorManager.getDefault().notify(ioe);
        } finally {
            if(lock != null)
                lock.releaseLock();
            if(out != null)
                out.close();
        }
        return configFile;
    }
    
    public static final String  WSCOMPILE_CLASSPATH = "wscompile.classpath"; //NOI18N
    
}


