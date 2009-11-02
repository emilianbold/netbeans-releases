/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */


package org.netbeans.modules.websvc.rest.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class,
                        projectType="org-netbeans-modules-web-project") //NOI18N
public class RestOpenHook extends ProjectOpenedHook {

    public static final RequestProcessor METADATA_MODEL_RP =
            new RequestProcessor("RestOpenHook.REST_REQUEST_PROCESSOR"); //NOI18N


    private final Project prj;
    private PropertyChangeListener pcl;

    public RestOpenHook(Project prj) {
        this.prj = prj;
    }

    @Override
    protected void projectOpened() {
        final RestSupport support = prj.getLookup().lookup(RestSupport.class);
        if (support == null) return;
        try {
            final RestServicesModel model = getModel(support);
            assert model != null : "null model";
            if (model != null) {
                model.runReadActionWhenReady(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        pcl = new RestServicesListener(support, model);
                        if (metadata.getRoot().getRestServiceDescription().length>0) {
                            support.extendBuildScripts();
                        }
                        metadata.getRoot().addPropertyChangeListener(pcl);
                        return null;
                    }
                });
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected void projectClosed() {
        RestSupport support = prj.getLookup().lookup(RestSupport.class);
        if (support == null) return;
        try {
            RestServicesModel model = getModel(support);
            assert model != null : "null model";
            if (model != null) {
                model.runReadActionWhenReady(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        metadata.getRoot().removePropertyChangeListener(pcl);
                        return null;
                    }
                });
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    class RestServicesListener implements PropertyChangeListener {
        private RestServicesModel model;
        private RestSupport support;
        RestServicesListener(RestSupport support, RestServicesModel model) {
            this.model=model;
            this.support=support;
        }
        private RequestProcessor.Task updateRestResourcesTask = METADATA_MODEL_RP.create(new Runnable() {
            public void run() {
                updateRestResources();
            }
        });

        public void propertyChange(PropertyChangeEvent evt) {
            updateRestResourcesTask.schedule(2000);
        }

        private void updateRestResources() {
            String restApplicationPath = support.getProjectProperty(WebRestSupport.PROP_REST_CONFIG_TYPE);
            if (WebRestSupport.CONFIG_TYPE_IDE.equals(restApplicationPath)) {
                try {
                    model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                        public Void run(RestServicesMetadata metadata) throws IOException {
                            RestServices root = metadata.getRoot();
                            StringBuffer buf = new StringBuffer("");
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
                            if (!newClasses.equals(oldClasses)) {
                                support.setProjectProperty(WebRestSupport.PROP_REST_ROOT_RESOURCES, newClasses);
                                FileObject buildFo = Utils.findBuildXml(prj);
                                if (buildFo != null) {
                                    ActionUtils.runTarget(buildFo, new String[]{WebRestSupport.REST_CONFIG_TARGET}, null);
                                }
                            }

                            return null;
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }



    private RestServicesModel getModel(RestSupport support) {
        return support.getRestServicesModel();
    }

}
