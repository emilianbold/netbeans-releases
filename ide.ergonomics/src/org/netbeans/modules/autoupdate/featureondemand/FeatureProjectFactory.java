/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.featureondemand;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.autoupdate.featureondemand.api.FeatureInfo;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
@ServiceProvider(service=ProjectFactory.class)
public class FeatureProjectFactory implements ProjectFactory {

    public boolean isProject(FileObject projectDirectory) {
        Lookup.Result<FeatureInfo> result = Feature2LayerMapping.featureTypesLookup().lookupResult(FeatureInfo.class);
        for (FeatureInfo pt2m : result.allInstances ()) {
            String pfp = FeatureInfoAccessor.DEFAULT.getDelegateFilePath(pt2m);
            if (pfp != null) {
                FileObject file = projectDirectory.getFileObject(pfp);
                if (file != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        Lookup.Result<FeatureInfo> result = Feature2LayerMapping.featureTypesLookup().lookupResult(FeatureInfo.class);
        for (FeatureInfo pt2m : result.allInstances ()) {
            String pfp = FeatureInfoAccessor.DEFAULT.getDelegateFilePath(pt2m);
            if (pfp != null) {
                FileObject file = projectDirectory.getFileObject(pfp);
                if (file != null) {
                    String cnb = FeatureInfoAccessor.DEFAULT.getCodeName(pt2m);
                    for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                        if (!info.getCodeNameBase().equals(cnb)) {
                            continue;
                        }
                        if (info.isEnabled()) {
                            // do not create any proxy
                            return null;
                        } else {
                            break;
                        }
                    }
                    return new FeatureNonProject(projectDirectory, pt2m, state);
                }
            }
        }
        return null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }

    private static final class FeatureNonProject extends ProjectOpenedHook implements Project, Runnable {
        private final FileObject dir;
        private final FeatureInfo info;
        private final Lookup lookup;
        private final ProjectState state;
        private boolean success = false;

        public FeatureNonProject(FileObject dir, FeatureInfo info, ProjectState state) {
            this.dir = dir;
            this.info = info;
            this.lookup = Lookups.singleton(this);
            this.state = state;
        }
        
        public FileObject getProjectDirectory() {
            return dir;
        }

        public Lookup getLookup() {
            return lookup;
        }

        @Override
        protected void projectOpened() {
            RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY).waitFinished ();
            OpenProjects.getDefault().close(new Project[] { this });
            if (success) {
                try {
                    state.notifyDeleted();
                    Project p = ProjectManager.getDefault().findProject(getProjectDirectory());
                    if (p == this) {
                        throw new IllegalStateException("New project shall be found! " + p);
                    }
                    OpenProjects.getDefault().open(new Project[]{p}, false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        protected void projectClosed() {
        }
        
        public void run () {
            String codeName = FeatureInfoAccessor.DEFAULT.getCodeName (info);
            FindComponentModules findModules = new FindComponentModules (codeName);
            findModules.createFindingTask ().waitFinished ();
            Collection<UpdateElement> toInstall = findModules.getModulesForInstall ();
            Collection<UpdateElement> toEnable = findModules.getModulesForEnable ();
            if (toInstall != null && ! toInstall.isEmpty ()) {
                ModulesInstaller installer = new ModulesInstaller (toInstall);
                installer.getInstallTask ().waitFinished ();
                success = true;
            } else if (toEnable != null && ! toEnable.isEmpty ()) {
                ModulesActivator enabler = new ModulesActivator (toEnable);
                enabler.getEnableTask ().waitFinished ();
                success = true;
            }
        }
        
    }
}
