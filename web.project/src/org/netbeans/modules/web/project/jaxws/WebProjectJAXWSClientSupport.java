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

package org.netbeans.modules.web.project.jaxws;

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author mkuchtiak
 */
public class WebProjectJAXWSClientSupport extends ProjectJAXWSClientSupport /*implements JAXWSClientSupportImpl*/ {
    WebProject project;
    
    /** Creates a new instance of WebProjectJAXWSClientSupport */
    public WebProjectJAXWSClientSupport(WebProject project) {
        super(project);
        this.project=project;    
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule!=null) {
            FileObject webInfFo = webModule.getWebInf();
            if (webInfFo!=null) {
                FileObject wsdlFo = webInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) return wsdlFo;
                else if (create) {
                    return webInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }

    protected void addJaxWs20Library() throws Exception{
        
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N
        
        if (wsimportFO == null) {
            //Add the jaxws21 library to the project to be packed with the archive
            ProjectClassPathExtender pce = (ProjectClassPathExtender)project.getLookup().lookup(ProjectClassPathExtender.class);
            Library jaxws21_ext = LibraryManager.getDefault().getLibrary("jaxws21"); //NOI18N
            if ((pce!=null) && (jaxws21_ext != null)) {
                try{
                pce.addLibrary(jaxws21_ext);
                }catch(IOException e){
                    throw new Exception("Unable to add JAXWS 2.1 library", e.getCause());
                } 
            } else {
                throw new Exception("Unable to add JAXWS 2.1 Library. " +
                        "ProjectClassPathExtender or library not found");
            }
        }
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        FileObject confDir = project.getWebModule().getConfDir();
        return confDir == null ? super.getXmlArtifactsRoot():confDir;
    }

    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {

        String finalClientName = super.addServiceClient(clientName, wsdlUrl, packageName, isJsr109);
        
        // copy resources to WEB-INF/wsdl/client/${clientName}
        // this will be done only for local wsdl files
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        Client client = jaxWsModel.findClientByName(finalClientName);
        if (client!=null && client.getWsdlUrl().startsWith("file:")) //NOI18N
            try {
                FileObject wsdlFolder = getWsdlFolderForClient(finalClientName);
                FileObject xmlResorcesFo = getLocalWsdlFolderForClient(finalClientName,false);
                if (xmlResorcesFo!=null) WSUtils.copyFiles(xmlResorcesFo, wsdlFolder);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        
        return finalClientName;
    }

}
