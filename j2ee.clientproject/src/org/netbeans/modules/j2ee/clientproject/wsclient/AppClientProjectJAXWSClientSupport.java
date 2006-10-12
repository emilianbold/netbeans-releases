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

package org.netbeans.modules.j2ee.clientproject.wsclient;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class AppClientProjectJAXWSClientSupport extends ProjectJAXWSClientSupport /*implements JAXWSClientSupportImpl*/ {
    AppClientProject project;
    
    /**
     * Creates a new instance of AppClientProjectJAXWSClientSupport
     */
    public AppClientProjectJAXWSClientSupport(AppClientProject project, AntProjectHelper antProjectHelper) {
        super(project);
        this.project=project;
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        Car carModule = Car.getCar(project.getProjectDirectory());
        if (carModule!=null) {
            FileObject webInfFo = carModule.getMetaInf();
            if (webInfFo!=null) {
                FileObject wsdlFo = webInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) {
                    return wsdlFo;
                } else if (create) {
                    return webInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }

    protected void addJaxWs20Library() throws Exception {
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        return project.getCarModule().getMetaInf();
    }

    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {
        // create jax-ws.xml if necessary
        FileObject fo = project.findJaxWsFileObject();
        if (fo==null) {
            try {
                project.createJaxWsFileObject();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        String finalClientName = super.addServiceClient(clientName, wsdlUrl, packageName, isJsr109);
        
        // copy resources to META-INF/wsdl/client/${clientName}
        // this will be done only for local wsdl files
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        Client client = jaxWsModel.findClientByName(finalClientName);
        if (client!=null && client.getWsdlUrl().startsWith("file:")) //NOI18N
            try {
                FileObject wsdlFolder = getWsdlFolderForClient(finalClientName);
                FileObject xmlResorcesFo = getLocalWsdlFolderForClient(finalClientName,false);
                if (xmlResorcesFo!=null) WSUtils.copyFiles(xmlResorcesFo, wsdlFolder);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        return finalClientName;
    }
    
}
