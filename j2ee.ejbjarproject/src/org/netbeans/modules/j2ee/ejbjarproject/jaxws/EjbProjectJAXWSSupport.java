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

package org.netbeans.modules.j2ee.ejbjarproject.jaxws;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.spi.ProjectJAXWSSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class EjbProjectJAXWSSupport extends ProjectJAXWSSupport /*implements JAXWSSupportImpl*/ {
    private EjbJarProject project;
    private AntProjectHelper antProjectHelper;
    
    /** Creates a new instance of JAXWSSupport */
    public EjbProjectJAXWSSupport(EjbJarProject project, AntProjectHelper antProjectHelper) {
        super(project,antProjectHelper);
        this.project = project;
        this.antProjectHelper = antProjectHelper;
    }

    public FileObject getWsdlFolder(boolean create) throws java.io.IOException {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule!=null) {
            FileObject metaInfFo = ejbModule.getMetaInf();
            if (metaInfFo!=null) {
                FileObject wsdlFo = metaInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) return wsdlFo;
                else if (create) {
                    return metaInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }
    
    /** Get wsdlLocation information 
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    public String getWsdlLocation(String serviceName) {
        String localWsdl = serviceName+".wsdl"; //NOI18N
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            if (service!=null) {
                String localWsdlFile = service.getLocalWsdlFile();
                if (localWsdlFile!=null) localWsdl=localWsdlFile;
            }
        }
        String prefix = "META-INF/wsdl/"; //NOI18N
        return prefix+serviceName+"/"+localWsdl; //NOI18N
    }

    public FileObject getDeploymentDescriptorFolder() {
        EjbJar ejbModule = EjbJar.getEjbJar(project.getProjectDirectory());
        if (ejbModule!=null) {
            return ejbModule.getMetaInf();
        }   
        return null;
    }

    protected void addJaxwsArtifacts(Project project, String wsName, String serviceImpl) throws Exception {
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        return project.getAPIEjbJar().getMetaInf();
    }

    public void removeNonJsr109Entries(String serviceName) throws IOException {
        //noop since nonJSR 109 web services are not supported in the EJB module
    }

    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName, String portName, String packageName, boolean isJsr109) {
        // create jax-ws.xml if necessary
        FileObject fo = project.findJaxWsFileObject();
        if (fo==null) {
            try {
                project.createJaxWsFileObject();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return super.addService(name, serviceImpl, wsdlUrl, serviceName, portName, packageName, isJsr109);
    }

    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {
        // create jax-ws.xml if necessary
        FileObject fo = project.findJaxWsFileObject();
        if (fo==null) {
            try {
                project.createJaxWsFileObject();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        super.addService(serviceName, serviceImpl, isJsr109);
    }
    
}
