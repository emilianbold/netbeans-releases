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

package org.netbeans.modules.web.project.jaxws;

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
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
            Library jaxws21_ext = LibraryManager.getDefault().getLibrary("jaxws21"); //NOI18N
            if (jaxws21_ext != null) {
                try {
                    ProjectClassPathModifier.addLibraries(new Library[]{jaxws21_ext}, sgs[0].getRootFolder(), ClassPath.COMPILE);
                }catch(IOException e){
                    throw new Exception("Unable to add JAXWS 2.1 library", e);
                } 
            } else {
                throw new Exception("Unable to add JAXWS 2.1 Library. " +
                        "ProjectClassPathExtender or library not found");
            }
        }
    }
    
    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        FileObject confDir = project.getWebModule().getConfDir();
        return confDir == null ? super.getXmlArtifactsRoot():confDir;
    }

    @Override
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {

        String finalClientName = super.addServiceClient(clientName, wsdlUrl, packageName, isJsr109);
        
        // copy resources to WEB-INF/wsdl/client/${clientName}
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        Client client = jaxWsModel.findClientByName(finalClientName);
        if (client!=null)
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
