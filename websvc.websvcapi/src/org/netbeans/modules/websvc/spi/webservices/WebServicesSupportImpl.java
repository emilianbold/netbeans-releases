/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.spi.webservices;

import java.util.List;

import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 *
 * @author Peter Williams
 */
public interface WebServicesSupportImpl {
    
    /*
     * Add web service related entries to the project.properties and project.xml files
     */
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL);
    
    /**
     * Add web service entries to the module's deployment descriptor
     */
    public void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String serviceEndpoint);
    
    /**
     * Get the FileObject of the webservices.xml file.
     */
    public FileObject getWebservicesDD();
    
    /**
     *  Returns the directory that contains webservices.xml in the project
     */
    public FileObject getWsDDFolder();
    
    /**
     * Returns the name of the directory that contains the webservices.xml in
     * the archive
     */
    public String getArchiveDDFolderName();
    
    /**
     * Returns the name of the implementation bean class
     * given the servlet-link or ejb-link name
     */
    public String getImplementationBean(String linkName);
    
    /**
     *  Given the servlet-link or ejb-link, remove the servlet or
     *  ejb entry in the module's deployment descriptor.
     */
    public void removeServiceEntry(String linkName);
    
    /**
     * Remove the web service entries from the project properties
     * project.xml files
     */
    public void removeProjectEntries(String serviceName);
    
    /**
     * Get the AntProjectHelper from the project
     */
    public AntProjectHelper getAntProjectHelper();
    
    /**
     * Generate the implementation bean class and return the class name
     */
    public String generateImplementationBean(String wsName, FileObject pkg, Project project, String delegateData)throws java.io.IOException;
    
    /**
     *  Add the servlet link or ejb link in the webservices.xml entry
     */
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName);
    
    /**
     * Get the ReferenceHelper from the project
     */
    public ReferenceHelper getReferenceHelper();
    
    /**
     * Get the list of services and their wscompile settings.
     */
    public List/*WsCompileEditorSupport.ServiceSettings*/ getServices();
    
    /**
     * Add infrastructure methods and fields (if any) that should be present
     * in the implementation bean class
     */
    public void addInfrastructure(String implBeanClass, FileObject pkg);
    
    /**
     * Determine if the web service was created from WSDL
     */
    public boolean isFromWSDL(String serviceName);
}
