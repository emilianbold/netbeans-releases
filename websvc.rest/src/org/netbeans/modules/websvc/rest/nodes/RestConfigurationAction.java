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
package org.netbeans.modules.websvc.rest.nodes;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.projects.RestApplicationsPanel;
import org.netbeans.modules.websvc.rest.projects.WebProjectRestSupport;
import org.netbeans.modules.websvc.rest.spi.ApplicationConfigPanel;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class RestConfigurationAction extends NodeAction  {

    public String getName() {
        return NbBundle.getMessage(RestConfigurationAction.class, "LBL_RestConfigurationAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Project project = activatedNodes[0].getLookup().lookup(Project.class);
        if ( project== null) {
            return false;
        }
        if (RestUtils.isJavaEE6(project)){
            return false;
        }
            
        return true;
    }
    
    protected void performAction(Node[] activatedNodes) {
        Project project = activatedNodes[0].getLookup().lookup(Project.class);
        WebRestSupport restSupport = project.getLookup().lookup(WebRestSupport.class);
        if (restSupport != null) {
            String oldConfigType = restSupport.getProjectProperty(WebRestSupport.PROP_REST_CONFIG_TYPE);
            if (oldConfigType == null) {
                oldConfigType = WebRestSupport.CONFIG_TYPE_DD;
            }
            String oldApplicationPath = "/webresources"; //NOI18N
            try {
                if (oldConfigType.equals( WebRestSupport.CONFIG_TYPE_DD)) {
                    String oldPathFromDD = restSupport.getApplicationPathFromDD();
                    if (oldPathFromDD != null) {
                        oldApplicationPath = oldPathFromDD;
                    }
                } else if (oldConfigType.equals( WebRestSupport.CONFIG_TYPE_IDE)) {
                    String resourcesPath = WebProjectRestSupport.
                        getApplicationPathFromDialog(restSupport.getRestApplications());//restSupport.getProjectProperty(WebRestSupport.PROP_REST_RESOURCES_PATH);
                    if (resourcesPath != null && resourcesPath.length()>0) {
                        oldApplicationPath = resourcesPath;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (!oldApplicationPath.startsWith(("/"))) { //NOI18N
                oldApplicationPath="/"+oldApplicationPath;
            }
            String oldJerseyConfig = restSupport.getProjectProperty(WebRestSupport.PROP_REST_JERSEY);
            // needs detect if Jersey Lib is present
            boolean isJerseyLib = oldJerseyConfig!= null;/*isOnClasspath(project,
                    "com/sun/jersey/spi/container/servlet/ServletContainer.class")  //NOI18N
                     */
            try {
                ApplicationConfigPanel configPanel = new ApplicationConfigPanel(
                        oldConfigType,
                        oldApplicationPath,
                        isJerseyLib,
                        restSupport.getAntProjectHelper() != null 
                            && RestUtils.isAnnotationConfigAvailable(project),
                        restSupport.hasServerJerseyLibrary(), oldJerseyConfig);

                DialogDescriptor desc = new DialogDescriptor(configPanel,
                    NbBundle.getMessage(RestConfigurationAction.class, 
                            "TTL_ApplicationConfigPanel"));                         // NOI18N
                DialogDisplayer.getDefault().notify(desc);
                if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
                    String newConfigType = configPanel.getConfigType();
                    String newApplicationPath = configPanel.getApplicationPath();
                    boolean addJersey = configPanel.isJerseyLibSelected();
                    if (!oldConfigType.equals(newConfigType) || 
                            !oldApplicationPath.equals(newApplicationPath)) 
                    {
                        if (!oldConfigType.equals(newConfigType)) {
                            // set up rest.config.type property
                            restSupport.setProjectProperty(WebRestSupport.PROP_REST_CONFIG_TYPE, newConfigType);

                            if (!WebRestSupport.CONFIG_TYPE_IDE.equals(newConfigType)) {
                                //remove properties related to rest.config.type=ide
                                restSupport.removeProjectProperties(new String[] {
                                    WebRestSupport.PROP_REST_RESOURCES_PATH,
                                });
                            }
                        }

                        if (WebRestSupport.CONFIG_TYPE_IDE.equals(newConfigType)) {
                            if (newApplicationPath.startsWith("/")) { //NOI18N
                                newApplicationPath = newApplicationPath.substring(1);
                            }
                            restSupport.setProjectProperty(WebRestSupport.PROP_REST_RESOURCES_PATH, newApplicationPath);
                            try {
                                setRootResources(project);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            if (!isOnClasspath(project,"javax/ws/rs/ApplicationPath.class")) {
                                // add jsr311 library
                                Library restApiLibrary = LibraryManager.getDefault().getLibrary(WebRestSupport.RESTAPI_LIBRARY);
                                if (restApiLibrary != null) {
                                    FileObject srcRoot = WebRestSupport.findSourceRoot(project);
                                    if (srcRoot != null) {
                                        try {
                                            ProjectClassPathModifier.addLibraries(new Library[] {restApiLibrary}, srcRoot, ClassPath.COMPILE);
                                        } catch(UnsupportedOperationException ex) {
                                            Logger.getLogger(getClass().getName()).info("Can not add JSR311 Library.");
                                        }
                                    }
                                }
                            }
                        } else if (WebRestSupport.CONFIG_TYPE_DD.equals(newConfigType)) { // Deployment Descriptor
                            // add entries to dd
                            try {
                                restSupport.addResourceConfigToWebApp(newApplicationPath);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    //if (!isOnClasspath(project,"com/sun/jersey/spi/container/servlet/ServletContainer.class")) {
                        // add jersey library
                    boolean added = false;
                    JaxRsStackSupport support = restSupport.getJaxRsStackSupport();
                    if (configPanel.isServerJerseyLibSelected()) {
                        restSupport.setProjectProperty(WebProjectRestSupport.PROP_REST_JERSEY, 
                                WebProjectRestSupport.JERSEY_CONFIG_SERVER );
                        if (support != null) {
                            if ( WebProjectRestSupport.JERSEY_CONFIG_IDE.
                                    equals(oldJerseyConfig))
                            {
                                JaxRsStackSupport.getDefault().
                                    removeJaxRsLibraries(project);
                            }
                            added = support
                                    .extendsJerseyProjectClasspath(project);
                        }
                    }
                    if (!added && addJersey) {
                        restSupport.setProjectProperty(WebProjectRestSupport.PROP_REST_JERSEY, 
                                WebProjectRestSupport.JERSEY_CONFIG_IDE );
                        if ( WebProjectRestSupport.JERSEY_CONFIG_SERVER.
                                equals(oldJerseyConfig) && support!= null )
                        {
                            support.removeJaxRsLibraries(project);
                        }
                        added = JaxRsStackSupport.getDefault()
                                .extendsJerseyProjectClasspath(project);
                    }
                    if  (!added) {
                        if ( WebProjectRestSupport.JERSEY_CONFIG_SERVER.
                                equals(oldJerseyConfig) && support!= null )
                        {
                            support.removeJaxRsLibraries(project);
                        }
                        if ( WebProjectRestSupport.JERSEY_CONFIG_IDE.
                                equals(oldJerseyConfig))
                        {
                            JaxRsStackSupport.getDefault().
                                removeJaxRsLibraries(project);
                        }
                        restSupport.removeProjectProperties(new String[]{
                                WebProjectRestSupport.PROP_REST_JERSEY});
                    }
                    //}
                 }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setRootResources(Project prj) throws IOException {
        FileObject buildFo = Utils.findBuildXml(prj);
        if (buildFo != null) {
            ActionUtils.runTarget(buildFo, new String[] {WebRestSupport.REST_CONFIG_TARGET}, null);
        }
    }

    @Override
    public boolean asynchronous() {
        return true;
    }

    private boolean isOnClasspath(Project project, String classResource) {
        FileObject srcRoot = WebRestSupport.findSourceRoot(project);
        if (srcRoot != null) {
            ClassPath cp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
            if (cp.findResource(classResource) != null) {
                return true;
            }
        }
        return false;
    }
    
}

