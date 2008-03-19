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
 */

package org.netbeans.modules.websvc.jaxws.light.spi;

import java.net.URL;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Milan Kuchtiak
 */

/** SPI for JAXWSSupport
 */ 
public interface JAXWSLightSupportImpl {
    
    /**
     * Add web service to jax-ws.xml intended for web services from java
     * @param serviceName service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     */
    public void addService(JaxWsService service);
    
    /**
     * Returns the list of web services in the project
     * @return list of web services
     */
    public List<JaxWsService> getServices();    
    
    /**
     * Remove the web service entries from the project properties
     * @param serviceName service IDE name 
     * project.xml files
     */
    public void removeService(JaxWsService service);

    /** Determine if the web service was created from WSDL
     * @param serviceName service name 
     */
    public boolean isFromWSDL(JaxWsService service);
    
    /** Get WSDL folder for the project (folder containing wsdl files)
     *  The folder is used to save remote or local wsdl files to be available within the jar/war files.
     *  it is usually META-INF/wsdl folder (or WEB-INF/wsdl for web application)
     *  @param createFolder if (createFolder==true) the folder will be created (if not created before)
     *  @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getWsdlFolder(boolean create) throws java.io.IOException;
    
    /** Get folder for local WSDL and XML artifacts for given service
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getLocalWsdlFolder(boolean createFolder);
    
    /** Get folder for local jaxb binding (xml) files for given service
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project 
     */
    public FileObject getBindingsFolder(boolean createFolder);
    
    /** Get EntityCatalog for local copies of wsdl and schema files
     */
    public URL getCatalog();
    
    /** Get wsdlLocation information
     * Useful for web service from wsdl (the @WebService wsdlLocation attribute)
     * @param serviceName service "display" name
     */
    public JaxWsService getService(String implClass);
   
    /**
     * Returns the directory that contains the deployment descriptor in the project
     */
    public FileObject getDeploymentDescriptorFolder();

    /**
     * Returns a metadata model of a webservices deployment descriptor
     *
     * @return metadata model of a webservices deployment descriptor
     */
    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel();
    
}
