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

package org.netbeans.modules.compapp.projects.jbi.jeese.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.io.File;
import javax.swing.JDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.compapp.javaee.sunresources.tool.JavaEETool;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.ArchiveConstants;
import org.netbeans.modules.compapp.javaee.sunresources.ui.ResourcesPanel;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author echou
 */
public class ServerResourcesAction extends NodeAction {
    
    private String name;
    
    /** Creates a new instance of ServerResourcesAction */
    public ServerResourcesAction() {
        name = NbBundle.getBundle(this.getClass()).getString("SERVER_RES_ACTION_NAME");
    }

    protected void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        Lookup lookup = node.getLookup();
        JbiProject jbiProject = (JbiProject) lookup.lookup(JbiProject.class);
        VisualClassPathItem vcpi = (VisualClassPathItem) lookup.lookup(VisualClassPathItem.class);
        String refProjectName = "project." + vcpi.getProjectName(); // NOI18N
        AntProjectHelper aph = (AntProjectHelper) jbiProject.getLookup().lookup(AntProjectHelper.class);
        EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
        String refProjectLocation = pe.evaluate(ep.getProperty(refProjectName));
        FileObject parentDir = jbiProject.getProjectDirectory();
        try {
            File refProjectDirFile = new File(FileUtil.toFile(parentDir).getCanonicalPath() + 
                File.separator + refProjectLocation);
            FileObject refProjectDir = FileUtil.toFileObject(FileUtil.normalizeFile(refProjectDirFile));
            if (refProjectDir == null) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(ServerResourcesAction.class, "EXC_proj_notfound", 
                            vcpi.getProjectName(),
                            refProjectLocation),
                        NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
        
            Project refProject = ProjectManager.getDefault().findProject(refProjectDir);
            if (refProject == null) {
                 DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                         NbBundle.getMessage(ServerResourcesAction.class, "EXC_cannot_open_proj", 
                            vcpi.getProjectName()), 
                         NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            
            // check type of project
            ArchiveConstants.ArchiveType projType = SunResourcesUtil.getJavaEEProjectType(refProject);
            if (projType == ArchiveConstants.ArchiveType.EAR || 
                    projType == ArchiveConstants.ArchiveType.EJB ||
                    projType == ArchiveConstants.ArchiveType.WAR) {
                JavaEETool javaEETool = null;
                try {
                    javaEETool = new JavaEETool(projType, refProject);
                } catch (ProjectNotBuiltException pnbe) {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(ServerResourcesAction.class, "EXC_proj_notbuild",
                                pnbe.getMessage()), 
                            NotifyDescriptor.ERROR_MESSAGE));
                    return;
                }
                
                // display dialog box ask for user input on which resource to generate
                Dialog resourceDialog = createResourcesDialog(javaEETool);
                resourceDialog.setVisible(true);
                
                javaEETool.close();
            } else {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(ServerResourcesAction.class, "EXC_proj_notsupported",
                            vcpi.getProjectName(),
                            projType),
                        NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public String getName() {
        return name;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    /* helper method to create a resources dialog window
     */
    private Dialog createResourcesDialog(JavaEETool javaEETool) {
        JDialog dlg = new JDialog(WindowManager.getDefault().getMainWindow(),
            NbBundle.getMessage(ServerResourcesAction.class, "SERVER_RES_ACTION_NAME"));
        ResourcesPanel innerPane = new ResourcesPanel(dlg, javaEETool);
        dlg.getContentPane().add(innerPane);
        dlg.setModal(true);
        dlg.setPreferredSize(innerPane.getPreferredSize());
        int width = dlg.getPreferredSize().width;
        int height = dlg.getPreferredSize().height;
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        dlg.setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
        return dlg;
    }
    
    public static class ProjectNotBuiltException extends Exception {
        public ProjectNotBuiltException(String msg) {
            super(msg);
        }
    }
}
