/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.rest.nodes;

import java.io.IOException;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.ApplicationConfigPanel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
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
        if (activatedNodes.length != 1) return false;
        if (activatedNodes[0].getLookup().lookup(Project.class) == null) return false;
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
            String oldApplicationPath = "/resources"; //NOI18N
            try {
                if (oldConfigType.equals( WebRestSupport.CONFIG_TYPE_DD)) {
                    String oldPathFromDD = restSupport.getApplicationPathFromDD();
                    if (oldPathFromDD != null) {
                        oldApplicationPath = oldPathFromDD;
                    }
                } else if (oldConfigType.equals( WebRestSupport.CONFIG_TYPE_IDE)) {
                    String resourcesPath = restSupport.getProjectProperty(WebRestSupport.PROP_REST_RESOURCES_PATH);
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
            try {
                ApplicationConfigPanel configPanel = new ApplicationConfigPanel(
                        oldConfigType,
                        oldApplicationPath,
                        restSupport.getAntProjectHelper() != null && isAnnotationConfigAvailable(project));

                DialogDescriptor desc = new DialogDescriptor(configPanel,
                    NbBundle.getMessage(RestConfigurationAction.class, "TTL_ApplicationConfigPanel"));
                DialogDisplayer.getDefault().notify(desc);
                if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
                    String newConfigType = configPanel.getConfigType();
                    String newApplicationPath = configPanel.getApplicationPath();
                    boolean applPathChanged = !oldApplicationPath.equals(newApplicationPath);
                    if (!oldConfigType.equals(newConfigType) || applPathChanged) {

                        if (!oldConfigType.equals(newConfigType)) {
                            // set up rest.config.type property
                            restSupport.setProjectProperty(WebRestSupport.PROP_REST_CONFIG_TYPE, newConfigType);

                            if (WebRestSupport.CONFIG_TYPE_IDE.equals(oldConfigType)) {
                                //remove properties related to rest.config.type=ide
                                restSupport.removeProjectProperties(new String[] {
                                    WebRestSupport.PROP_REST_RESOURCES_PATH,
                                    WebRestSupport.PROP_REST_ROOT_RESOURCES
                                });
                            }
                        }

                        if (WebRestSupport.CONFIG_TYPE_IDE.equals(newConfigType)) {
                            if (newApplicationPath.startsWith("/")) { //NOI18N
                                newApplicationPath = newApplicationPath.substring(1);
                            }
                            restSupport.setProjectProperty(WebRestSupport.PROP_REST_RESOURCES_PATH, newApplicationPath);
                            try {
                                setRootResources(project, restSupport, applPathChanged);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else if (!WebRestSupport.CONFIG_TYPE_USER.equals(newConfigType)) { // Deployment Descriptor
                            // add entries to dd
                            try {
                                restSupport.addResourceConfigToWebApp(newApplicationPath);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setRootResources(final Project prj, final RestSupport support, final boolean applPathChanged)
        throws IOException {

        RestServicesModel model = support.getRestServicesModel();
        if (model != null) {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    StringBuffer buf = new StringBuffer(""); //NOI18N
                    int i=0;
                    String oldClasses = support.getProjectProperty(WebRestSupport.PROP_REST_ROOT_RESOURCES);
                    for (RestServiceDescription desc : root.getRestServiceDescription()) {
                        String resourcePath = desc.getUriTemplate();
                        if (resourcePath != null && resourcePath.length()>0) {
                            if (i++ > 0) {
                                buf.append(","); //NOI18N
                            }
                            buf.append(desc.getClassName()+".class"); //NOI18N
                        }
                    }
                    String newClasses = buf.toString();
                    if (applPathChanged || !newClasses.equals(oldClasses)) {
                        support.setProjectProperty(WebRestSupport.PROP_REST_ROOT_RESOURCES, newClasses);
                        FileObject buildFo = Utils.findBuildXml(prj);
                        if (buildFo != null) {
                            ActionUtils.runTarget(buildFo, new String[]{WebRestSupport.REST_CONFIG_TARGET}, null);
                        }
                    }

                    return null;
                }
            });
        } else {
            throw new IOException("Cannot get rest services model from project.");
        }
    }

    @Override
    public boolean asynchronous() {
        return true;
    }

    private boolean isAnnotationConfigAvailable(Project project) throws IOException {
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        if (sourceGroups.length>0) {
            ClassPath cp = ClassPath.getClassPath(sourceGroups[0].getRootFolder(), ClassPath.COMPILE);
            if (cp.findResource("javax/ws/rs/ApplicationPath.class") != null && // NOI18N
                cp.findResource("javax/ws/rs/core/Application.class") != null) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
}

