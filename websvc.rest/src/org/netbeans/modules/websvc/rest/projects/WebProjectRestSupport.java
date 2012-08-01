/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.rest.projects;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.LogUtils;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
@ProjectServiceProvider(service=RestSupport.class, projectType="org-netbeans-modules-web-project")
public class WebProjectRestSupport extends WebRestSupport {

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";   //NOI18N

    public static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N

    String[] classPathTypes = new String[] {
                ClassPath.COMPILE
            };

    /** Creates a new instance of WebProjectRestSupport */
    public WebProjectRestSupport(Project project) {
        super(project);
    }

    @Override
    public void ensureRestDevelopmentReady() throws IOException {
        boolean needsRefresh = false;
        
        WebRestSupport.RestConfig restConfig = null;
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        Profile profile = webModule.getJ2eeProfile();
        boolean isJee6 = Profile.JAVA_EE_6_WEB.equals(profile) || 
                Profile.JAVA_EE_6_FULL.equals(profile); 
        
        /*
         *  do not show config dialog in JEE6 case. Manually created REST service 
         *  should be configured via editor hint  
         */
        if ( !isJee6 && !isRestSupportOn()) {
            needsRefresh = true;
            restConfig = setApplicationConfigProperty(
                    RestUtils.isAnnotationConfigAvailable(project));
        }
        
        extendBuildScripts();

        String restConfigType = getProjectProperty(PROP_REST_CONFIG_TYPE);
        
        if (!RestUtils.isJSR_311OnClasspath(project)) {
            boolean jsr311Added = false;
            if ( restConfig!= null && restConfig.isServerJerseyLibSelected() ){
                JaxRsStackSupport support = getJaxRsStackSupport(); 
                if ( support != null ){
                        jsr311Added = support.addJsr311Api(project);
                    }
            }
            if ( !jsr311Added ){
                JaxRsStackSupport.getDefault().addJsr311Api(project);
            }
        }

        if (!isJee6) {
            if (restConfigType == null || CONFIG_TYPE_DD.equals(restConfigType))
            {

                String resourceUrl = null;
                if (restConfig != null) {
                    resourceUrl = restConfig.getResourcePath();
                }
                else {
                    resourceUrl = getApplicationPathFromDD();
                }
                if (resourceUrl == null) {
                    resourceUrl = REST_SERVLET_ADAPTOR_MAPPING;
                }
                addResourceConfigToWebApp(resourceUrl);
            }
            if (needsRefresh && CONFIG_TYPE_IDE.equals(restConfigType)) {
                FileObject buildFo = Utils.findBuildXml(project);
                if (buildFo != null) {
                    ActionUtils.runTarget(buildFo,
                            new String[] { WebRestSupport.REST_CONFIG_TARGET },
                            null);
                }
            }
        }

        boolean added = false;
        if (restConfig != null) {
            if ( restConfig.isServerJerseyLibSelected()){
                JaxRsStackSupport support = getJaxRsStackSupport();
                if ( support != null ){
                    added = support.extendsJerseyProjectClasspath(project);
                }
            }
            if ( !added && restConfig.isJerseyLibSelected()){
                JaxRsStackSupport.getDefault().extendsJerseyProjectClasspath(project);
            }
        }

        if (hasSpringSupport()) {
            addJerseySpringJar();
        }
        ProjectManager.getDefault().saveProject(getProject());
        if (needsRefresh) {
            refreshRestServicesMetadataModel();
        }
    }

    @Override
    public void removeRestDevelopmentReadiness() throws IOException {
        removeResourceConfigFromWebApp();
        removeSwdpLibrary(new String[]{
                    ClassPath.COMPILE,
                    ClassPath.EXECUTE
                });
        removeProjectProperties(new String[]{PROP_REST_CONFIG_TYPE, PROP_REST_RESOURCES_PATH});
        ProjectManager.getDefault().saveProject(getProject());
    }

    public boolean isReady() {
        return isRestSupportOn() && hasSwdpLibrary() && hasRestServletAdaptor();
    }

    public J2eePlatform getPlatform() {
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return null;
        }
        try {
            // Fix for BZ#192058 -  NullPointerException: The serverInstanceId parameter cannot be null
            String id = j2eeModuleProvider.getServerInstanceID();
            if ( id == null ){
                return null;
            }
            return Deployment.getDefault().getServerInstance(id).getJ2eePlatform();
        } catch (InstanceRemovedException ex) {
            return null;
        }
    }

    private J2eePlatform getJ2eePlatform(J2eeModuleProvider j2eeModuleProvider){
        String serverInstanceID = j2eeModuleProvider.getServerInstanceID();
        if(serverInstanceID != null && serverInstanceID.length() > 0) {
            try {
                return Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(WebProjectRestSupport.class.getName()).log(Level.INFO, "Failed to find J2eePlatform");
            }
        }
        return null;
    }

    @Override
    public FileObject getPersistenceXml() {
        PersistenceScope ps = PersistenceScope.getPersistenceScope(getProject().getProjectDirectory());
        if (ps != null) {
            return ps.getPersistenceXml();
        }
        return null;
    }

    public Datasource getDatasource(String jndiName) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);

        try {
            return provider.getConfigSupport().findDatasource(jndiName);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public void setDirectoryDeploymentProperty(Properties p) {
        String instance = getAntProjectHelper().getStandardPropertyEvaluator().getProperty(J2EE_SERVER_INSTANCE);
        if (instance != null) {
            J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
            String sdi = jmp.getServerInstanceID();
            J2eeModule mod = jmp.getJ2eeModule();
            if (sdi != null && mod != null) {
                boolean cFD = Deployment.getDefault().canFileDeploy(instance, mod);
                p.setProperty(DIRECTORY_DEPLOYMENT_SUPPORTED, String.valueOf(cFD)); // NOI18N

            }
        }
    }
    
    @Override
    public File getLocalTargetTestRest(){
        String path = RESTBEANS_TEST_DIR;
        AntProjectHelper helper = getAntProjectHelper();
        EditableProperties projectProps = helper
                .getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String path1 = projectProps
                .getProperty(PROP_RESTBEANS_TEST_DIR);
        if (path1 != null) {
            path = path1;
        }
        return helper.resolveFile(path);
    }
    
    public FileObject generateTestClient(File testdir, String url) 
        throws IOException 
   {
        FileObject fileObject = generateTestClient(testdir);
        Map<String,String> map = new HashMap<String, String>();
        map.put(BASE_URL_TOKEN, url );
        modifyFile( fileObject , map );
        return fileObject;
    }
    
    @Override
    public String getBaseURL() throws IOException {
        String applicationPath = getApplicationPath();
        if (applicationPath != null) {
            if (!applicationPath.startsWith("/")) {
                applicationPath = "/"+applicationPath;
            }
        }
        return getContextRootURL()+"||"+applicationPath;            //NOI18N
    }
    
    @Override
    public void deploy() throws IOException{
        FileObject buildFo = Utils.findBuildXml(getProject());
        if (buildFo != null) {
            ExecutorTask task = ActionUtils.runTarget(buildFo,
                    new String[] { COMMAND_DEPLOY },
                    new Properties());
            task.waitFinished();
        }
    }

    @Override
    protected void logResourceCreation(Project prj) {
        Object[] params = new Object[3];
        params[0] = LogUtils.WS_STACK_JAXRS;
        params[1] = project.getClass().getName();
        params[2] = "REST RESOURCE"; // NOI18N
        LogUtils.logWsDetect(params);
    }

    @Override
    protected String getApplicationPathFromAnnotations(final String applPathFromDD) {
        List<RestApplication> restApplications = getRestApplications();
        if (applPathFromDD == null) {
            if (restApplications.size() == 0) {
                return null;
            } else {
                return getApplicationPathFromDialog(restApplications);
            }
        } else {
            if (restApplications.size() == 0) {
                return applPathFromDD;
            } else {
                boolean found = false;
                for (RestApplication appl: restApplications) {
                    if (applPathFromDD.equals(appl.getApplicationPath())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    restApplications.add(new RestApplication() {
                        public String getApplicationPath() {
                            return applPathFromDD;
                        }

                        public String getApplicationClass() {
                            return "web.xml"; //NOI18N
                        }
                    });
                }
                return getApplicationPathFromDialog(restApplications);
            }
        }
    }

    public static String getApplicationPathFromDialog(List<RestApplication> restApplications) {
        if (restApplications.size() == 1) {
            return restApplications.get(0).getApplicationPath();
        } 
        else {
            RestApplicationsPanel panel = new RestApplicationsPanel(restApplications);
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(WebProjectRestSupport.class,"TTL_RestResourcesPath"));
            DialogDisplayer.getDefault().notify(desc);
            if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
                return panel.getApplicationPath();
            }
        }
        return null;
    }
    
    private void extendBuildScripts() throws IOException {
        new AntFilesHelper(this).initRestBuildExtension();
    }


}
