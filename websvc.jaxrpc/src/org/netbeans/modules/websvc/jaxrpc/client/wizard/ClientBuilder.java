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

package org.netbeans.modules.websvc.jaxrpc.client.wizard;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.jaxrpc.PortInformation;
import org.netbeans.modules.websvc.jaxrpc.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileAlreadyLockedException;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.apache.tools.ant.module.api.support.ActionUtils;

import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.netbeans.modules.websvc.wsdl.config.WsCompileConfigDataObject;
import org.netbeans.modules.websvc.wsdl.config.PortInformationHandler;
import org.openide.util.Lookup;


/**
 *
 * @author Peter Williams
 */
public class ClientBuilder {
    
    private static final String TEMPLATE_BASE = "/org/netbeans/modules/websvc/core/client/resources/"; //NOI18N

    // User/project specified inputs
    private Project project;
    private WebServicesClientSupport projectSupport;
    private FileObject wsdlSource;
    private String packageName;
    private String sourceUrl;
    private ClientStubDescriptor stubDescriptor;

    // Intermediate processing
    private FileObject wsdlTarget;
    private FileObject configFile;
    private List /*FileObject*/ importedWsdlList;
    
    public ClientBuilder(Project project, WebServicesClientSupport support, FileObject wsdlSource, String packageName, String sourceUrl, ClientStubDescriptor sd) {
        this.project = project;
        this.projectSupport = support;
        this.wsdlSource = wsdlSource;
        this.packageName = packageName;
        this.sourceUrl = sourceUrl;
        this.stubDescriptor = sd;
        importedWsdlList = new ArrayList();
    }

    /** If the service or port name begins with a lower case letter, the class
     *  name for the corresponding JAXRPC class will still begin with uppercase
     *  so we need to adjust the name we use accordingly.
     *
     *  See also ...websvc.core.client.actions.InvokeOperationAction.
     */
    private static String classFromName(final String name) {
        String result = name;
        
        if(name.length() > 0 && !Character.isUpperCase(name.charAt(0))) {
            StringBuffer buf = new StringBuffer(name);
            buf.setCharAt(0, Character.toUpperCase(name.charAt(0)));
            result = buf.toString();
        }
        
        return result;
    }

    public Set/*FileObject*/ generate(final ProgressHandle handle) {
        Set result = Collections.EMPTY_SET;

        try {
            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            
            ClassPath classPath = ClassPath.getClassPath(sourceGroups[0].getRootFolder(),ClassPath.COMPILE);
            FileObject wscompileFO = classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class"); //NOI18N
            if (wscompileFO==null) return result;
            
            handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizAddToRegistry"));
            
            // !PW Move step 3 to the beginning to avoid having to synchronize the
            // web service registry with the soon-to-be-created client node.

            // 3. Find services in registry (add if necessary) -- DONE
            //WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
            //registryView.registerService(wsdlSource, true);
            
            
            handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizParsingWSDL"),20);
            
            PortInformationHandler handler = new PortInformationHandler();
            try {
                parse (wsdlSource, handler);
            } catch(ParserConfigurationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_WsdlParseFailure", ex.getMessage()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return result;
            } catch(SAXException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_WsdlParseFailure", ex.getMessage()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return result;
            }
            
            handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizCopyingWSDL"),35);
            
            // 1. Copy wsdl file to wsdl folder -- DONE
            final FileObject wsdlFolder = projectSupport.getWsdlFolder(true);

            // First ensure neither the target wsdl or -config.xml files exist.
            FileObject target = wsdlFolder.getFileObject(wsdlSource.getName(), "wsdl"); //NOI18N
            if(target != null) {
                target.delete();
            }
            target = wsdlFolder.getFileObject(wsdlSource.getName() + WsCompileConfigDataObject.WSCOMPILE_CONFIG_FILENAME_SUFFIX, "xml"); // NOI18N
            if(target != null) {
                target.delete();
            }
            
             // Now copy the wsdl file.
            if (handler.isServiceNameConflict()) {
                wsdlTarget = generateWSDL(wsdlFolder, wsdlSource.getName() , new StreamSource(wsdlSource.getInputStream()));
            } else {
                wsdlTarget = wsdlSource.copy(wsdlFolder, wsdlSource.getName(), "wsdl"); //NOI18N
            }
            
            // Also recursively copy the imported wsdl/schema files
            handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizCopyingSchemas"),40);
            copyImportedSchemas(wsdlSource.getParent(),wsdlFolder,wsdlTarget);
            
            handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizProcessingWSDL"),45);
            
            // 2. Generate config file for WSCompile -- DONE
            File wsdlAsFile = FileUtil.toFile(wsdlTarget);

            if(wsdlAsFile != null) {
                final String wsdlConfigEntry = "\t<wsdl location=\"file:@CONFIG_ABSOLUTE_PATH@/" + wsdlAsFile.getName() + "\" packageName=\"" + packageName + "\"/>"; // NOI81N
                FileSystem fs = wsdlFolder.getFileSystem();
                fs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        configFile = wsdlFolder.createData(wsdlTarget.getName() + WsCompileConfigDataObject.WSCOMPILE_CONFIG_FILENAME_SUFFIX, "xml"); // NOI18N
                        FileLock configLock = configFile.lock();

                        // !PW FIXME this should come from a parameterized registered template
                        try {
                            PrintWriter configWriter = new PrintWriter(configFile.getOutputStream(configLock));

                            try {
                                configWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
                                configWriter.println("<configuration xmlns=\"http://java.sun.com/xml/ns/jax-rpc/ri/config\">"); // NOI18N
                                configWriter.println(wsdlConfigEntry);
                                configWriter.println("</configuration>"); // NOI18N
                            } finally {
                                configWriter.close();
                            }
                        } finally {
                            configLock.releaseLock();
                        }
                    }
                });                
            } else {
                // Can't get File object for wsdl file, we're screwed.
                String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_CannotOpenWsdlFile", wsdlTarget.getNameExt()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return result;
            }

            // WSDL/Config file objects are what we return.
            // !PW per Jiri (HIE), we don't want any files opened when a client is added.
            // so don't return anything from the wizard.
//			result.add(wsdlTarget);
//			result.add(configFile);

            // 3. Find services in registry (add if necessary) -- DONE
            // !PW FIXME do we want to notify the user if registration fails?
            // How does the registry view communicate the difference between "already registered"
            // and "failure during registration" (Since the former is irrelevant.)
          WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
          registryView.registerService(wsdlTarget, false);

            // Invoke SAX parser on the WSDL to extract list of port bindings
            //
            // !PW Redo this so that this information is retrieved from the WSDL node
            // (which is possibly still in the process of being created, so be careful.
            //

            // use all imported wsdl files to get the information about WS (Port Info, Binding Info)
            List wsdlLocationsList = handler.getImportedSchemas();
            if (wsdlLocationsList.size()>0 && handler.getServices().size()>0 && handler.getEntirePortList().size()>0) {
                handler = new PortInformationHandler(handler.getTargetNamespace(),handler.getServices(),handler.getEntirePortList(),handler.getBindings(), wsdlLocationsList);
                for(int i =0;i<wsdlLocationsList.size();i++) {
                    String wsdlLocation = (String)wsdlLocationsList.get(i);
                    try {
                        if (wsdlLocation.indexOf("/")<0) { //local
                            FileObject wsdlFo = wsdlFolder.getFileObject(wsdlLocation);
                            if (wsdlFo!=null && importedWsdlList.contains(wsdlFo))
                                parse (wsdlFo, handler);
                        } else { // remote
                            URL wsdlURL = new URL(wsdlLocation);
                            parse (wsdlURL, handler);
                        }
                        
                    } catch(ParserConfigurationException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_WsdlParseFailure", ex.getMessage()); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                        return result;
                    } catch(SAXException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_WsdlParseFailure", ex.getMessage()); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                        return result;
                    }
                }
            }
            
            handle.progress(50);
            
            // 4. Add service-ref entry to deployment descriptor -- only performed for JSR109 client stubs
            if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubDescriptor.getName())) {
/** JSR-109 J2EE 1.4 deployment descriptor
 *	<service-ref>
 *	    <service-ref-name>service/TemperatureService</service-ref-name>
 *	        "service/" + [name attribute of service field from wsdl]
 *      <service-interface>temperature.TemperatureService</service-interface>
 *          [interface package].[service classname -- name attribute of service field from wsdl]
 *      <wsdl-file>WEB-INF/wsdl/TemperatureService.wsdl</wsdl-file>
 *          [relative path from root of deployed module of wsdl file]
 *      <jaxrpc-mapping-file>WEB-INF/temperature-mapping.xml</jaxrpc-mapping-file>
 *          [relative path from root of deployed module of mapping file]
 *      <port-component-ref>
 *          <service-endpoint-interface>temperature.TemperaturePortType</service-endpoint-interface>
 *              [interface package].[service endpoint classname -- name attribute of porttype field from wsdl]
 *      </port-component-ref>
 *	</service-ref>
 */
                handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizUpdatingDD"));
                
                // Make sure server specific support is available.
                J2eeModuleProvider j2eeMP = (J2eeModuleProvider) project.getLookup ().lookup(J2eeModuleProvider.class);			
                j2eeMP.getConfigSupport().ensureConfigurationReady();			

                //get correct top folder where wsdl and mapping file are stored
                // WEB-INF for webapp, META-INF otherwise (ejb, appclient, connector(?))
                String prefix = J2eeModule.WAR.equals(j2eeMP.getJ2eeModule().getModuleType())
                    ? "WEB-INF/"    //NOI18N
                    : "META-INF/";  //NOI18N
                
                // Get deployment descriptor (web.xml or ejbjar.xml)
                // Create service ref
//                FileObject ddFO = projectSupport.getDeploymentDescriptor();

                // If we get null for the deployment descriptor, ignore this step.
//                if(ddFO != null) {
//                    RootInterface rootDD = DDProvider.getDefault().getDDRoot(ddFO);

                    // Add a service ref for each service in the WSDL file.
                    String [] serviceNames = handler.getServiceNames();
                    for(int si = 0; si < serviceNames.length; si++) {
                        String serviceName = serviceNames[si];

                        PortInformation.ServiceInfo serviceInfo = handler.getServiceInfo(serviceName);
                        List portList = serviceInfo.getPorts();

                        if (handler.isServiceNameConflict()) serviceName+="_Service"; //NOI18N
                        try {
                            serviceName = Utilities.removeSpacesFromServiceName(serviceName);
                            String ddServiceName = "service/" + serviceName; // NOI18N
                            String fullyQualifiedServiceName = packageName + "." + classFromName(serviceName); // NOI18N
                            String relativeWsdlPath = prefix + "wsdl/" + wsdlTarget.getNameExt(); // NOI18N !PW FIXME get relative path to WSDL folder from archive root
                            String relativeMappingPath = prefix + wsdlTarget.getName() + "-mapping.xml"; // NOI18N
                            
                            List seiList = new ArrayList();
                            
                            for (int pi = 0; pi < portList.size(); pi++) {
                                PortInformation.PortInfo portInfo = (PortInformation.PortInfo) portList.get(pi);
                                String portTypeName = portInfo.getPortType();
                                if (portTypeName!=null) seiList.add(packageName + "." + classFromName(portTypeName)); //NOI18N
                            }
                            
                            String[] portInfoSEI = new String[seiList.size()];
                            seiList.toArray(portInfoSEI);
                            
                            projectSupport.addServiceClientReference(ddServiceName, 
                                                                    fullyQualifiedServiceName, 
                                                                    relativeWsdlPath, 
                                                                    relativeMappingPath, 
                                                                    portInfoSEI);
                            
//                            ServiceRef serviceRef = (ServiceRef) rootDD.findBeanByName("ServiceRef", "ServiceRefName", ddServiceName); // NOI18N
//                            if(serviceRef == null) {
//                                serviceRef = (ServiceRef) rootDD.addBean("ServiceRef", // NOI18N
//                                    new String [] { /* property list */ 
//                                        "ServiceRefName", // NOI18N
//                                        "ServiceInterface", // NOI18N
//                                        "WsdlFile", // NOI18N
//                                        "JaxrpcMappingFile" // NOI18N
//                                    },
//                                    new String [] { /* property values */ 
//                                        // service name
//                                        ddServiceName,
//                                        // interface package . service name
//                                        fullyQualifiedServiceName,
//                                        // web doc base / wsdl folder / wsdl file name
//                                        relativeWsdlPath,
//                                        // web doc base / mapping file name
//                                        relativeMappingPath
//                                    },
//                                    "ServiceRefName"); // NOI18N
//                            } else {
//                                serviceRef.setServiceInterface(fullyQualifiedServiceName);
//                                serviceRef.setWsdlFile(new URI(relativeWsdlPath));
//                                serviceRef.setJaxrpcMappingFile(relativeMappingPath);
//                            }
//
//                            PortComponentRef [] portRefArray = new PortComponentRef [portList.size()];
//                            for(int pi = 0; pi < portRefArray.length; pi++) {
//                                PortInformationHandler.PortInfo portInfo = (PortInformationHandler.PortInfo) portList.get(pi);
//                                portRefArray[pi] = (PortComponentRef) serviceRef.createBean("PortComponentRef"); // NOI18N
//                                portRefArray[pi].setServiceEndpointInterface(packageName + "." + classFromName(portInfo.getPortType())); // NOI18N
//                            }
//
//                            serviceRef.setPortComponentRef(portRefArray);
                        } catch(ClassCastException ex) {
                            // Programmer error - mistyped object name.
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }

                    // This also saves server specific configuration, if necessary.
//                    rootDD.write(ddFO);
//                } else {
//                    // !PW FIXME JSR-109 stub type, but no deployment descriptor returned.
//                    // We should issue an error about this. 
//                }
            }
            
            // Final steps are performed by the project support object.
            // 5. Add interface source directory to code completion path
            // 6. Add properties to drive new entry in build script -- DONE
            // 7. Add WS libraries to project build path -- DONE
            // 8. Force build script regeneration -- DONE

            handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizUpdatingBuildScript"),65);
            Set features = handler.getWscompileFeatures();
            String[] wscompileFeatures = new String[features.size()];
            features.toArray(wscompileFeatures);
            projectSupport.addServiceClient(wsdlTarget.getName(), packageName, sourceUrl, configFile, stubDescriptor, wscompileFeatures);
            
            // 9. Execute wscompile script for the new client (mostly to populate for code completion.
            handle.progress(NbBundle.getMessage(ClientBuilder.class, "MSG_WizGenerateClient"),80);
            
            String targetName = wsdlTarget.getName() + "-client-wscompile"; // NOI18N
            FileObject buildFO = findBuildXml();
            if(buildFO != null) {
                ExecutorTask task = ActionUtils.runTarget(buildFO, new String [] { targetName }, null);
                task.waitFinished();
                if(task.result() != 0){
                    String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_WsCompileFailed"); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            } else {
                String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_NoBuildScript"); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }

            project.getProjectDirectory().refresh();
        } catch(FileAlreadyLockedException ex) {
            // !PW This should not happen, but if it does...
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_ClientIOError", wsdlSource.getNameExt(), ex.getMessage()); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        } finally {
            handle.progress(95);
        }

        return result;
    }

    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    
    /** Static method to identify wsdl/schema files to import
    */
    static List /*String*/ getSchemaNames(FileObject fo, boolean fromWsdl) {
            List result = null;
            try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    SAXParser saxParser = factory.newSAXParser();
                    ImportsHandler handler= (fromWsdl?(ImportsHandler)new WsdlImportsHandler():(ImportsHandler)new SchemaImportsHandler());
                    saxParser.parse(new InputSource(fo.getInputStream()), (DefaultHandler)handler);
                    result = handler.getSchemaNames();
            } catch(ParserConfigurationException ex) {
                    // Bogus WSDL, return null.
            } catch(SAXException ex) {
                    // Bogus WSDL, return null.
            } catch(IOException ex) {
                    // Bogus WSDL, return null.
            }

            return result;
    }
    
    private static interface ImportsHandler {
        public List getSchemaNames();
    }
    
    private static class WsdlImportsHandler extends DefaultHandler implements ImportsHandler {
        
        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl"; // NOI18N
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/"; // NOI18N
        
        private List schemaNames;
        
        private boolean insideSchema;
        
        WsdlImportsHandler() {
            schemaNames = new ArrayList();
        }
        
        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("types".equals(localname)) { // NOI18N
                    insideSchema=true;
                }
                if("import".equals(localname)) { // NOI18N
                    String wsdlLocation = attributes.getValue("location"); //NOI18N
                    if (wsdlLocation!=null && wsdlLocation.indexOf("/")<0 && wsdlLocation.endsWith(".wsdl")) { //NOI18N
                        schemaNames.add(wsdlLocation);
                    }
                }
            }
            if(insideSchema && "import".equals(localname)) { // NOI18N
                String schemaLocation = attributes.getValue("schemaLocation"); //NOI18N
                if (schemaLocation!=null && schemaLocation.indexOf("/")<0 && schemaLocation.endsWith(".xsd")) { //NOI18N
                    schemaNames.add(schemaLocation);
                }
            }
        }
        
        public void endElement(String uri, String localname, String qname) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("types".equals(localname)) { // NOI18N
                    insideSchema=false;
                }
            }
        }
        
        public List/*String*/ getSchemaNames() {
            return schemaNames;
        }
    }
    
    private static class SchemaImportsHandler extends DefaultHandler implements ImportsHandler {
        
        private List schemaNames;
     
        SchemaImportsHandler() {
            schemaNames = new ArrayList();
        }
        
        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if("import".equals(localname)) { // NOI18N
                String schemaLocation = attributes.getValue("schemaLocation"); //NOI18N
                if (schemaLocation!=null && schemaLocation.indexOf("/")<0 && schemaLocation.endsWith(".xsd")) { //NOI18N
                    schemaNames.add(schemaLocation);
                }
            }
        }
        
        public List/*String*/ getSchemaNames() {
            return schemaNames;
        }
    }
    
    /* Recursive method that copies all necessary wsdl/schema files imported by FileObject to target folder
     */
    private synchronized void copyImportedSchemas(FileObject resourceFolder, FileObject targetFolder, FileObject fo) throws IOException {
        List schemaNames = getSchemaNames(fo,"wsdl".equals(fo.getExt())); //NOI18N
        Iterator it = schemaNames.iterator();
        while (it.hasNext()) {
            String schemaName = (String)it.next();
            FileObject schemaFile = resourceFolder.getFileObject(schemaName);
            if (schemaFile!=null) {
                FileObject target = targetFolder.getFileObject(schemaFile.getName(),schemaFile.getExt());
                if(target != null) {
                    FileLock lock = target.lock();
                    if (lock!=null)
                        try {
                            target.delete(lock);
                        } finally {
                            lock.releaseLock();
                        }
                }
                //copy the schema file
                FileObject copy = schemaFile.copy(targetFolder,schemaFile.getName(),schemaFile.getExt());
                if ("wsdl".equals(schemaFile.getExt())) { //WSDL imports another WSDL
                    importedWsdlList.add(copy);
                }
                copyImportedSchemas(resourceFolder, targetFolder, copy);
            } else {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(ClientBuilder.class,"ERR_FileNotFound",schemaName,resourceFolder.getPath()),
                                                        NotifyDescriptor.ERROR_MESSAGE));
                break;
            }
        }
    }
    
    private void parse(FileObject fo, PortInformationHandler handler) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(fo.getInputStream(), handler);
    }
    
    private void parse(URL url, PortInformationHandler handler) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        try {
            saxParser.parse(url.openConnection().getInputStream(), handler);
        } catch (java.net.UnknownHostException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            String mes = NbBundle.getMessage(ClientBuilder.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
    }
    
    private FileObject generateWSDL(FileObject folder, String wsdlName, StreamSource source) throws IOException 
    {
        FileObject wsdlFile = folder.createData(wsdlName, "wsdl"); //NOI18N
        FileLock fl = null;
        OutputStream os = null;
        try {
            fl = wsdlFile.lock();
            os = new BufferedOutputStream(wsdlFile.getOutputStream(fl));
            Transformer transformer = getTransformer();
            transformer.transform(source, new StreamResult(os));
            os.close();
        }
        catch(TransformerConfigurationException tce) {
            IOException ioe = new IOException();
            ioe.initCause(tce);
            throw ioe;
        }
        catch(TransformerException te) {
            IOException ioe = new IOException();
            ioe.initCause(te);
            throw ioe;
        }
        finally {
            if(os != null) {
                os.close();
            }
            if(fl != null) {
                fl.releaseLock();
            }
        }
        return wsdlFile;
    }
    
    private Transformer getTransformer() throws TransformerConfigurationException {
        InputStream is = new BufferedInputStream(getClass().getResourceAsStream(TEMPLATE_BASE+"WSDL.xml")); //NOI18N
        TransformerFactory transFactory = TransformerFactory.newInstance();
        transFactory.setURIResolver(new URIResolver() {
            public Source resolve(String href, String base)
            throws TransformerException {
                InputStream is = getClass().getResourceAsStream(
                TEMPLATE_BASE + href.substring(href.lastIndexOf('/')+1));
                if (is == null) {
                    return null;
                }
                
                return new StreamSource(is);
            }
        });
        Templates t = transFactory.newTemplates(new StreamSource(is));
        return t.newTransformer();
    }

}
