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
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.utils.AllSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class ImportProject {

    private File dirF;
    private String name;
    private String makefileName = "Makefile";  // NOI18N
    private String makefilePath;
    private String configurePath;
    private String configureArguments;
    private boolean runConfigure;
    private String workingDir;
    private String buildCommand = "${MAKE} all";  // NOI18N
    private String cleanCommand = "${MAKE} clean";  // NOI18N
    private String buildResult = "";  // NOI18N

    public Set<FileObject> create() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        dirF = FileUtil.normalizeFile(dirF);
        MakeConfiguration extConf = new MakeConfiguration(dirF.getPath(), "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
        String workingDirRel = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(workingDir));
        workingDirRel = FilePathAdaptor.normalize(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommand().setValue(buildCommand);
        extConf.getMakefileConfiguration().getCleanCommand().setValue(cleanCommand);
        // Build result
        if (buildResult != null && buildResult.length() > 0) {
            buildResult = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(buildResult));
            buildResult = FilePathAdaptor.normalize(buildResult);
            extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
        }
        // Add makefile and configure script to important files
        ArrayList<String> importantItems = new ArrayList<String>();
        File makefileFile = new File(makefilePath);
        if (makefilePath != null && makefilePath.length() > 0) {
            makefilePath = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(makefilePath));
            makefilePath = FilePathAdaptor.normalize(makefilePath);
            importantItems.add(makefilePath);
        }
        if (configurePath != null && configurePath.length() > 0) {
            File configureFile = new File(configurePath);
            configurePath = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(configurePath));
            configurePath = FilePathAdaptor.normalize(configurePath);
            importantItems.add(configurePath);

            try {
                FileObject configureFileObject = FileUtil.toFileObject(configureFile);
                DataObject dObj = DataObject.find(configureFileObject);
                Node node = dObj.getNodeDelegate();

                // Add arguments to configure script?
                if (configureArguments != null) {
                    ShellExecSupport ses = node.getCookie(ShellExecSupport.class);
                    // Keep user arguments as is in args[0]
                    ses.setArguments(new String[]{configureArguments});
                }
                // Possibly run the configure script
                if (runConfigure) {
                    // If no makefile, create empty one so it shows up in Interesting Files
                    if (!makefileFile.exists()) {
                        makefileFile.createNewFile();
                    }
                    ShellRunAction.performAction(node);
                }
            } catch (DataObjectNotFoundException e) {
            }
        }
        Iterator importantItemsIterator = importantItems.iterator();
        if (!importantItemsIterator.hasNext()) {
            importantItemsIterator = null;
        }

        SourceFolderInfo info = new SourceFolderInfo() {
            public File getFile() {
                return dirF;
            }
            public String getFolderName() {
                return dirF.getName();
            }
            public boolean isAddSubfoldersSelected() {
                return true;
            }

            public FileFilter getFileFilter() {
                return AllSourceFileFilter.getInstance();
            }
        };
        List<SourceFolderInfo> sources = new ArrayList<SourceFolderInfo>();
        sources.add(info);
        Project makeProject = ProjectGenerator.createProject(dirF, name, makefileName, new MakeConfiguration[]{extConf},sources.iterator()); // NOI18N
        FileObject dir = FileUtil.toFileObject(dirF);
        resultSet.add(dir);
        //OpenProjects.getDefault().open(new Project[]{p}, false);
        //OpenProjects.getDefault().setMainProject(p);
        return resultSet;
    }
}
