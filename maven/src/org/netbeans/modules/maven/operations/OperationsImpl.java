/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.maven.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Implementation of IDE's idea how to move/delete/copy a project.
 * makes sure the project is removed from the possible module section of the parent..
 * @author mkleint
 */
public class OperationsImpl implements DeleteOperationImplementation, MoveOperationImplementation, CopyOperationImplementation {
    protected final NbMavenProjectImpl project;
    private ProjectState state;
    /** Creates a new instance of AbstractOperation */
    public OperationsImpl(NbMavenProjectImpl proj, ProjectState state) {
        project = proj;
        this.state = state;
    }
    
    
    protected static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        if (file != null) {
            result.add(file);
        }
    }

    public List<FileObject> getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(projectDirectory, "pom.xml", files); // NOI18N
        addFile(projectDirectory, "profiles.xml", files); // NOI18N
        addFile(projectDirectory, "nbactions.xml", files); //NOI18N
        addFile(projectDirectory, "nb-configuration.xml", files); //NOI18N
        
        return files;
    }
    
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<FileObject>();
        addFile(project.getProjectDirectory(), "src", files); //NOI18N
        //TODO is there more?
        return files;
    }
    
    public void notifyDeleting() throws IOException {
        // cannot run ActionProvider.CLEAN because that one doesn't stop thi thread.
        //TODO shall I get hold of the actual mapping for the clean action?
        BeanRunConfig config = new BeanRunConfig();
        config.setExecutionDirectory(FileUtil.toFile(project.getProjectDirectory()));
        //config.setOffline(true);
        config.setGoals(Collections.singletonList("clean")); //NOI18N
        config.setRecursive(false);
        config.setProject(project);
        config.setExecutionName(NbBundle.getMessage(OperationsImpl.class, "NotifyDeleting.execute"));
        config.setUpdateSnapshots(false);
        config.setTaskDisplayName(NbBundle.getMessage(OperationsImpl.class, "NotifyDeleting.execute"));
        ExecutorTask task = RunUtils.executeMaven(config);
        task.result();
        checkParentProject(project.getProjectDirectory(), true, null, null);
        config.setProject(null);
    }
    
    
    
    public void notifyDeleted() throws IOException {
        state.notifyDeleted();
    }
    
    public void notifyMoving() throws IOException {
        notifyDeleting();
    }
    
    public void notifyMoved(Project original, File originalLoc, final String newName) throws IOException {
        if (original == null) {
            //old project call..
            state.notifyDeleted();
            return;
        } else {
            if (original.getProjectDirectory().equals(project.getProjectDirectory())) {
                // oh well, just change the name in the pom when rename is invoked.
                FileObject pomFO = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    public void performOperation(POMModel model) {
                        model.getProject().setName(newName);
                        model.endTransaction();
                    }
                };
                Utilities.performPOMModelOperations(pomFO, Collections.singletonList(operation));
                NbMavenProject.fireMavenProjectReload(project);
            }
            checkParentProject(project.getProjectDirectory(), false, newName, originalLoc.getName());
        }
    }
    
    public void notifyCopying() throws IOException {
        
    }
    
    public void notifyCopied(Project original, File originalLoc, String newName) throws IOException {
        if (original == null) {
            //old project call..
        } else {
            checkParentProject(project.getProjectDirectory(), false, newName, originalLoc.getName());
        }
    }
    
    private void checkParentProject(FileObject projectDir, final boolean delete, final String newName, final String oldName) throws IOException {
        final String prjLoc = projectDir.getNameExt();
        FileObject fo = projectDir.getParent();
        Project possibleParent = ProjectManager.getDefault().findProject(fo);
        if (possibleParent != null) {
            final NbMavenProjectImpl par = possibleParent.getLookup().lookup(NbMavenProjectImpl.class);
            if (par != null) {
                FileObject pomFO = par.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    public void performOperation(POMModel model) {
                        MavenProject prj = par.getOriginalMavenProject();
                        if ((prj.getModules() != null && prj.getModules().contains(prjLoc)) == delete) {
                            //delete/add module from/to parent..
                            if (delete) {
                                model.getProject().removeModule(prjLoc);
                            } else {
                                model.getProject().addModule(prjLoc);
                            }
                        }
                        if (newName != null && oldName != null) {
                            if (oldName.equals(model.getProject().getArtifactId())) {
                                // is this condition necessary.. why not just overwrite the artifactID always..
                                model.getProject().setArtifactId(newName);
                            }
                        }
                    }
                };
                Utilities.performPOMModelOperations(pomFO, Collections.singletonList(operation));
            }
        }
        
    }
    
}
