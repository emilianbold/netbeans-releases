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

package org.netbeans.modules.websvc.jaxws.spi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperGenerator;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

/** Abstract class that implements most of JAXWSSupportImpl methods
 * Useful to implement instead of JAXWSSupportImpl
 *
 * @author mkuchtiak
 * Created on February 7, 2006, 11:09 AM
 */
public abstract class ProjectJAXWSSupport implements JAXWSSupportImpl {
    private Project project;
    private AntProjectHelper antProjectHelper;
    
    /** Creates a new instance of JAXWSSupport */
    public ProjectJAXWSSupport(Project project, AntProjectHelper antProjectHelper) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
    }
    
    public void removeService(String serviceName) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            if (service!=null) {
                // remove the service element as well as the implementation class
                jaxWsModel.removeService(serviceName);
                writeJaxWsModel(jaxWsModel);
                WSUtils.removeImplClass(project,service.getImplementationClass());
            }
        }
    }
    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed
     * or when impl.class is removed (manually from project)
     * Default implementation does nothing.
     */
    public void serviceFromJavaRemoved(String serviceName) {}
    
    public boolean isFromWSDL(String serviceName) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        Service service = jaxWsModel.findServiceByName(serviceName);
        if (service!=null && service.getWsdlUrl()!=null) return true;
        else return false;
    }
    
    /**
     * Returns the name of the implementation class
     * given the service (ide) name
     */
    public String getServiceImpl(String serviceName) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            return service==null?null:service.getImplementationClass();
        }
        return null;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return antProjectHelper;
    }
    
    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Boolean value = jaxWsModel.getJsr109();
            if((value == null || Boolean.TRUE.equals(value)) && !isJsr109){
                jaxWsModel.setJsr109(Boolean.FALSE);
                writeJaxWsModel(jaxWsModel);
            } else if (Boolean.FALSE.equals(value) && isJsr109) {
                jaxWsModel.setJsr109(Boolean.TRUE);
                writeJaxWsModel(jaxWsModel);
            }
        }
        
        if(!isJsr109 ){
            try{
                addJaxwsArtifacts(project, serviceName, serviceImpl);
            } catch(Exception e){
                ErrorManager.getDefault().notify(e); //TODO handle this
            }
        }
    }
    
    protected abstract void addJaxwsArtifacts(Project project, String wsName,
            String serviceImpl) throws Exception;
    
    /*
     * Add web service to jax-ws.xml
     * intended for web services from wsdl
     * @return returns the unique IDE service name
     */
    
    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName,
            String portName, String packageName, boolean isJsr109) {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            String finalServiceName = WSUtils.findProperServiceName(name, jaxWsModel);
            boolean serviceAdded=false;
            
            // HACK to enable filesystems to fire events when new folder will be created
            // need to ask for children recursively
            List<FileObject> subfolders = null;
            FileObject serviceArtifactsFolder = project.getProjectDirectory().getFileObject("build/generated/wsimport/service"); //NOI18N
            if (serviceArtifactsFolder!=null) {
                Enumeration en = serviceArtifactsFolder.getChildren(true);
                subfolders = new ArrayList<FileObject>();
                while (en.hasMoreElements()) {
                    FileObject ch = (FileObject) en.nextElement();
                    if (ch.isFolder()) subfolders.add(ch);
                }
            }
            
            FileObject localWsdl=null;
            try {
                // download resources to xml-resources
                FileObject xmlResorcesFo = getLocalWsdlFolderForService(finalServiceName,true);
                localWsdl = WSUtils.retrieveResource(
                        xmlResorcesFo,
                        new URI(wsdlUrl));
                if (localWsdl!=null) {
                    
                    WsdlWrapperHandler handler = null;
                    try {
                        handler = WsdlWrapperGenerator.parse(localWsdl.getURL().toExternalForm());
                    } catch (ParserConfigurationException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (SAXException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    }
                    if (!handler.isServiceElement()) {
                        StreamSource source = new StreamSource(localWsdl.getURL().toExternalForm());
                        try {
                            File wrapperWsdlFile = new File(FileUtil.toFile(localWsdl.getParent()), WsdlWrapperGenerator.getWrapperName(localWsdl.getURL())); //NOI18N

                            if(!wrapperWsdlFile.exists()) {
                                try {
                                    wrapperWsdlFile.createNewFile();
                                } catch(IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
                                }
                            }
                            if (wrapperWsdlFile.exists()) {
                                WsdlWrapperGenerator.generateWrapperWSDLContent(wrapperWsdlFile, source, handler.getTargetNsPrefix(),localWsdl.getNameExt());
                                localWsdl=FileUtil.toFileObject(wrapperWsdlFile);
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                        }
                    }

                    Boolean value = jaxWsModel.getJsr109();
                    if((value == null || Boolean.TRUE.equals(value)) && !isJsr109){
                        jaxWsModel.setJsr109(Boolean.FALSE);
                    } else if (Boolean.FALSE.equals(value) && isJsr109) {
                        jaxWsModel.setJsr109(Boolean.TRUE);
                    }
                    Service service=null;
                    try {
                        service = jaxWsModel.addService(finalServiceName, serviceImpl, wsdlUrl, serviceName, portName, packageName);
                    } catch (ServiceAlreadyExistsExeption ex) {
                        //this shouldn't happen
                    }
                    String localWsdlUrl = FileUtil.getRelativePath(xmlResorcesFo, localWsdl);
                    service.setLocalWsdlFile(localWsdlUrl);
                    FileObject catalog = getCatalogFileObject();
                    if (catalog!=null) service.setCatalogFile(CATALOG_FILE);
                    writeJaxWsModel(jaxWsModel);
                    serviceAdded=true;
                }
                
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
            } catch (UnknownHostException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
            }
            
            if (serviceAdded) {
                if(!isJsr109 ){
                    try{
                        addJaxwsArtifacts(project, serviceName, serviceImpl);
                    } catch(Exception e){
                        ErrorManager.getDefault().notify(e); //TODO handle this
                    }
                }
                FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                try {
                    ExecutorTask wsimportTask =
                            ActionUtils.runTarget(buildImplFo,new String[]{"wsimport-service-"+finalServiceName},null); //NOI18N
                    wsimportTask.waitFinished();
                    
                    // refresh service artifacts directory due to code copletion
                    if (serviceArtifactsFolder==null)
                        serviceArtifactsFolder = project.getProjectDirectory().getFileObject("build/generated/wsimport/service/"+packageName.replace('.','/'));
                    if (serviceArtifactsFolder!=null) {
                        serviceArtifactsFolder.refresh();
                        // refresh also all sub-folders
                        if (subfolders!=null) {
                            for (int i=0;i<subfolders.size();i++) {
                                ((FileObject) subfolders.get(i)).refresh();
                            }
                        }
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                } catch (IllegalArgumentException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
                return finalServiceName;
            }
        }
        return null;
    }
    
    /**
     * Returns the list of web services in the project
     */
    public List<Service> getServices() {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service[] services = jaxWsModel.getServices();
            if (services!=null) {
                List<Service> list = new ArrayList<Service>();
                for (int i=0;i<services.length;i++) {
                    list.add(services[i]);
                }
                return list;
            }
        }
        return new ArrayList<Service>();
    }

    private void writeJaxWsModel(final JaxWsModel jaxWsModel) {
        try {
            final FileObject jaxWsFo = project.getProjectDirectory().getFileObject("nbproject/jax-ws.xml"); //NOI18N
            jaxWsFo.getFileSystem().runAtomicAction(new AtomicAction() {
                public void run() {
                    FileLock lock=null;
                    OutputStream os=null;
                    try {
                        lock = jaxWsFo.lock();
                        os = jaxWsFo.getOutputStream(lock);
                        jaxWsModel.write(os);
                        os.close();
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    } finally {
                        if (os!=null) {
                            try {
                                os.close();
                            } catch (IOException ex) {}
                        }
                        if (lock!=null) lock.releaseLock();
                    }
                }
            });
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /**
     *  return folder for local wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForService(String serviceName, boolean createFolder) {
        return getArtifactsFolder(serviceName, createFolder, true);
    }
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForService(String serviceName, boolean createFolder) {
        return getArtifactsFolder(serviceName, createFolder, false);
    }
    
    private FileObject getArtifactsFolder(String serviceName, boolean createFolder, boolean forWsdl) {
        String folderName = forWsdl?"wsdl":"bindings"; //NOI18N
        FileObject root = getXmlArtifactsRoot();
        if (root==null) {
            assert !createFolder : "Cannot create XML artifacts folder"; //NOI18N
            return null;
        }
        FileObject wsdlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER+"/"+SERVICES_LOCAL_FOLDER+"/"+serviceName+"/"+folderName); //NOI18N
        if (wsdlLocalFolder==null && createFolder) {
            try {
                FileObject xmlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER);
                if (xmlLocalFolder==null) xmlLocalFolder = root.createFolder(XML_RESOURCES_FOLDER);
                FileObject servicesLocalFolder = xmlLocalFolder.getFileObject(SERVICES_LOCAL_FOLDER);
                if (servicesLocalFolder==null) servicesLocalFolder = xmlLocalFolder.createFolder(SERVICES_LOCAL_FOLDER);
                FileObject serviceLocalFolder = servicesLocalFolder.getFileObject(serviceName);
                if (serviceLocalFolder==null) serviceLocalFolder = servicesLocalFolder.createFolder(serviceName);
                wsdlLocalFolder=serviceLocalFolder.getFileObject(folderName);
                if (wsdlLocalFolder==null) wsdlLocalFolder = serviceLocalFolder.createFolder(folderName);
            } catch (IOException ex) {
                return null;
            }
        }
        return wsdlLocalFolder;
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        return project.getProjectDirectory();
    }
    
    private FileObject getCatalogFileObject() {
        return project.getProjectDirectory().getFileObject(CATALOG_FILE);
    }
    
    public URL getCatalog() {
        try {
            FileObject catalog = getCatalogFileObject();
            return catalog==null?null:catalog.getURL();
        } catch (FileStateInvalidException ex) {
            return null;
        }
        
    }
    
    private FileObject getWsdlFolderForService(String name) throws IOException {
        FileObject globalWsdlFolder = getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject(name);
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        return globalWsdlFolder.createFolder(name);
    }
    
    public abstract FileObject getWsdlFolder(boolean create) throws java.io.IOException;
    
    /** Get wsdlLocation information
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    public abstract String getWsdlLocation(String serviceName);

    /**
     * Returns a metadata model of a webservices deployment descriptor
     *
     * @return metadata model of a webservices deployment descriptor
     */
    public abstract MetadataModel<WebservicesMetadata> getWebservicesMetadataModel();
}
