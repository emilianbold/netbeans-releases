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
package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.maven.problems.ProblemsPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;


/** A node to represent project root.
 *
 * @author Milos Kleint 
 */
public class MavenProjectNode extends AbstractNode {
    private static final String BADGE_ICON = "org/netbeans/modules/maven/brokenProjectBadge.png";//NOI18N
     private static String toolTipBroken = "<img src=\"" + MavenProjectNode.class.getClassLoader().getResource(BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(MavenProjectNode.class, "ICON_BrokenProjectBadge");//NOI18N
     
     private NbMavenProjectImpl project;
     private ProjectInformation info;
     private ProblemReporterImpl reporter;

     public MavenProjectNode(Lookup lookup, NbMavenProjectImpl proj) {
        super(NodeFactorySupport.createCompositeChildren(proj, "Projects/org-netbeans-modules-maven/Nodes"), lookup); //NOI18N
        this.project = proj;
        info = project.getLookup().lookup(ProjectInformation.class);
        NbMavenProject.addPropertyChangeListener(project, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(event.getPropertyName())) {
                    fireDisplayNameChange(null, getDisplayName());
                    fireIconChange();
                }
            }
        });
        reporter = proj.getLookup().lookup(ProblemReporterImpl.class);
        reporter.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        fireIconChange();
                        fireOpenedIconChange();
                        fireDisplayNameChange(null, getDisplayName());
                        fireShortDescriptionChange(null, getShortDescription());
                    }
                });
            }
        });
    }

    
    @Override
    public String getDisplayName() {
        return project.getDisplayName();
    }
    
    @Override
    public Image getIcon(int param) {
        Image img = ImageUtilities.icon2Image(info.getIcon());
        if (reporter.getReports().size() > 0) {
            Image ann = ImageUtilities.loadImage(BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipBroken);
            img = ImageUtilities.mergeImages(img, ann, 8, 0);//NOI18N
        }
        return img;
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        Image img = ImageUtilities.icon2Image(info.getIcon());
        if (reporter.getReports().size() > 0) {
            Image ann = ImageUtilities.loadImage(BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipBroken);
            img = ImageUtilities.mergeImages(img, ann, 8, 0);//NOI18N
        }
        return img;
    }
    
    @Override
    public javax.swing.Action[] getActions(boolean param) {
        ArrayList<Action> lst = new ArrayList<Action>();
        ActionProviderImpl provider = project.getLookup().lookup(ActionProviderImpl.class);
        lst.add(CommonProjectActions.newFileAction());
        lst.add(null);
    
        lst.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, NbBundle.getMessage(MavenProjectNode.class, "ACT_Build"), null));
        lst.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, NbBundle.getMessage(MavenProjectNode.class, "ACT_Clean_Build"), null));
        lst.add(ProjectSensitiveActions.projectCommandAction("build-with-dependencies", NbBundle.getMessage(MavenProjectNode.class, "ACT_Build_Deps"), null));
        lst.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, NbBundle.getMessage(MavenProjectNode.class, "ACT_Clean"), null));
        lst.add(ProjectSensitiveActions.projectCommandAction("javadoc", NbBundle.getMessage(MavenProjectNode.class, "ACT_Javadoc"), null)); //NOI18N
        lst.add(null);
        lst.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, NbBundle.getMessage(MavenProjectNode.class, "ACT_Run"), null));
        lst.addAll(Utilities.actionsForPath("Projects/Debugger_Actions_temporary")); //NOI18N
        lst.addAll(Utilities.actionsForPath("Projects/Profiler_Actions_temporary")); //NOI18N
        lst.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, NbBundle.getMessage(MavenProjectNode.class, "ACT_Test"), null));

        List<? extends Action> acts = Utilities.actionsForPath("Projects/org-netbeans-modules-maven/ProjectActions"); //NOI18N
        for (Action ac : acts) {
            if (ac != null && ac.isEnabled()) {
                lst.add(ac);
            }
        }
        lst.add(null);

        lst.add(provider.createCustomPopupAction()); 
//        if (project.getLookup().lookup(ConfigurationProviderEnabler.class).isConfigurationEnabled()) {
            lst.add(CommonProjectActions.setProjectConfigurationAction());
//        } else {
//            lst.add(provider.createProfilesPopupAction());
//        }

        lst.add(null);
        lst.addAll(Utilities.actionsForPath("Projects/org-netbeans-modules-maven/DependenciesActions")); //NOI18N
        
        // separator
        lst.add(null);
        lst.add(NbMavenProjectImpl.createRefreshAction());
        lst.add(CommonProjectActions.setAsMainProjectAction());
        lst.add(CommonProjectActions.openSubprojectsAction());
        if (NbMavenProject.TYPE_POM.equalsIgnoreCase(project.getProjectWatcher().getPackagingType())) { //NOI18N
            lst.add(new CloseSuprojectsAction());
        }
        lst.add(CommonProjectActions.closeProjectAction());
        lst.add(null);
        lst.add(SystemAction.get(FindAction.class));
        lst.add(null);
        lst.add(CommonProjectActions.renameProjectAction());
        lst.add(CommonProjectActions.moveProjectAction());
        lst.add(CommonProjectActions.copyProjectAction());
        lst.add(CommonProjectActions.deleteProjectAction());
            
        lst.addAll(Utilities.actionsForPath("Projects/Actions")); //NOI18N
        lst.add(null);
        if (reporter.getReports().size() > 0) {
            lst.add(new ShowProblemsAction());
        }
        
        lst.add(CommonProjectActions.customizeProjectAction());
        
        return lst.toArray(new Action[lst.size()]);
    }
    
    @Override
    public String getShortDescription() {
        StringBuffer buf = new StringBuffer();
        String desc;
        if (project.isErrorPom(project.getOriginalMavenProject())) {
            desc = NbBundle.getMessage(MavenProjectNode.class, "TXT_FailedProjectLoadingDesc");
        } else {
            desc = project.getShortDescription();
        }
        buf.append("<html><i>").append(NbBundle.getMessage(MavenProjectNode.class, "DESC_Project1")).append("</i><b> ").append(FileUtil.getFileDisplayName(project.getProjectDirectory())).append("</b><br><i>"); //NOI18N
        buf.append(NbBundle.getMessage(MavenProjectNode.class, "DESC_Project2")).append("</i><b> ").append(project.getOriginalMavenProject().getGroupId()).append("</b><br><i>");//NOI18N
        buf.append(NbBundle.getMessage(MavenProjectNode.class, "DESC_Project3")).append("</i><b> ").append(project.getOriginalMavenProject().getArtifactId()).append("</b><br><i>");//NOI18N
        buf.append(NbBundle.getMessage(MavenProjectNode.class, "DESC_Project4")).append("</i><b> ").append(project.getOriginalMavenProject().getVersion()).append("</b><br><i>");//NOI18N
        //TODO escape the short description
        buf.append(NbBundle.getMessage(MavenProjectNode.class, "DESC_Project5")).append("</i> ").append(breakPerLine(desc, NbBundle.getMessage(MavenProjectNode.class, "DESC_Project5").length()));//NOI18N
        if (reporter.getReports().size() > 0) {
            buf.append("<br><b>").append(NbBundle.getMessage(MavenProjectNode.class, "DESC_Project6")).append("</b><br><ul>");//NOI18N
            Iterator it = reporter.getReports().iterator();
            while (it.hasNext()) {
                ProblemReport elem = (ProblemReport) it.next();
                buf.append("<li>" + elem.getShortDescription() + "</li>");//NOI18N
            }
            buf.append("</ul>");//NOI18N
        }
        // it seems that with ending </html> tag the icon descriptions are not added.
//        buf.append("</html>");//NOI18N
        return buf.toString();
    }

    private String breakPerLine(String string, int start) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(string, " ", true);//NOI18N
        int charCount = start;
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            charCount = charCount + token.length();
            if (charCount > 50) {
                charCount = 0;
                buf.append("<br>");//NOI18N
            }
            buf.append(token);
        }
        return buf.toString();
        
    }
    
    private class CloseSuprojectsAction extends AbstractAction {
        public CloseSuprojectsAction() {
            putValue(Action.NAME, NbBundle.getMessage(MavenProjectNode.class, "ACT_CloseRequired"));
        }

        public void actionPerformed(ActionEvent e) {
            SubprojectProvider subs = project.getLookup().lookup(SubprojectProvider.class);
            Set<? extends Project> lst = subs.getSubprojects();
            Project[] arr = lst.toArray(new Project[lst.size()]);
            OpenProjects.getDefault().close(arr);
        }
    }
    
    private class ShowProblemsAction extends AbstractAction {
        
        public ShowProblemsAction() {
            putValue(Action.NAME, NbBundle.getMessage(MavenProjectNode.class, "ACT_ShowProblems"));
        }
        
        public void actionPerformed(ActionEvent arg0) {
            JButton butt = new JButton();
            
            ProblemsPanel panel = new ProblemsPanel(reporter);
            panel.setActionButton(butt);
            JButton close = new JButton();
            panel.setCloseButton(close);
            close.setText(NbBundle.getMessage(MavenProjectNode.class, "BTN_Close"));
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(MavenProjectNode.class, "TIT_Show_Problems"));
            dd.setOptions(new Object[] { butt,  close});
            dd.setClosingOptions(new Object[] { close });
            dd.setModal(false);
            DialogDisplayer.getDefault().notify(dd);
        }
    }
}
