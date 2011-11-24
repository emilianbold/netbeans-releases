/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import org.netbeans.modules.maven.j2ee.utils.LoggingUtils;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


public class ProjectHookImpl extends ProjectOpenedHook {
    
    private final Project project;
    private PropertyChangeListener refreshListener;
    private J2eeModuleProvider lastJ2eeProvider;
    

    public ProjectHookImpl(Project project) {
        this.project = project;
    }
    
    @Override
    protected void projectOpened() {
        MavenProjectSupport.changeServer(project, false);
        if (refreshListener == null) {
            //#121148 when the user edits the file we need to reset the server instance
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            refreshListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        MavenProjectSupport.changeServer(project, false);
                    }
                }
            };
            watcher.addPropertyChangeListener(refreshListener);
        }
        
        LoggingUtils.logUsage(ExecutionChecker.class, "USG_PROJECT_OPEN_MAVEN_EE", new Object[] { getServerName(), getEEversion() }, "maven"); //NOI18N
    }
    
    @Override
    protected void projectClosed() {
        //is null check necessary?
        if (refreshListener != null) {
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            watcher.removePropertyChangeListener(refreshListener);
            refreshListener = null;
        }
        if (lastJ2eeProvider != null) {
            Deployment.getDefault().disableCompileOnSaveSupport(lastJ2eeProvider);
            lastJ2eeProvider = null;
        }
        CopyOnSave copyOnSave = project.getLookup().lookup(CopyOnSave.class);
        if (copyOnSave != null) {
            try {
                copyOnSave.cleanup();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private String getServerName() {
        String serverName = MavenProjectSupport.obtainServerName(project);
        if (serverName == null) {
            serverName = NbBundle.getMessage(ProjectHookImpl.class, "MSG_No_Server");  //NOI18N
        }
        return serverName;
    }
    
    private String getEEversion() {
        String eeVersion = null;
        NbMavenProject mavProj = project.getLookup().lookup(NbMavenProject.class);
        if (mavProj != null) {
            String pkgType = mavProj.getPackagingType();
            if ("ear".equals(pkgType)) { //NOI18N
                Ear earProj = Ear.getEar(project.getProjectDirectory());
                if (earProj != null) {
                    eeVersion = earProj.getJ2eePlatformVersion();
                }
            } else if ("war".equals(pkgType)) { //NOI18N
                WebModule webM = WebModule.getWebModule(project.getProjectDirectory());
                if (webM != null) {
                    eeVersion = webM.getJ2eePlatformVersion();
                }
            } else if ("ejb".equals(pkgType)) { //NOI18N
                EjbJar ejbProj = EjbJar.getEjbJar(project.getProjectDirectory());
                if (ejbProj != null) {
                    eeVersion = ejbProj.getJ2eePlatformVersion();
                }
            }
        }
        if (eeVersion == null) {
            eeVersion = NbBundle.getMessage(ProjectHookImpl.class, "TXT_UnknownEEVersion"); //NOI18N
        }
        return eeVersion;
    }
}
