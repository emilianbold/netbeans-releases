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

package org.netbeans.modules.ruby.railsprojects.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.Panel;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.api.project.rake.RakeArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyUtils;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.modules.ruby.railsprojects.UpdateHelper;



/**
 * ProjectNode represents a dependent project under the Libraries Node.
 * It is a leaf node with the following actions: {@link OpenProjectAction},
 * {@link ShowRDocAction} and {@link RemoveClassPathRootAction}
 * 
 * @author Tomas Zezula
 */
class ProjectNode extends AbstractNode {

    private static final String PROJECT_ICON = "org/netbeans/modules/ruby/railsprojects/ui/resources/projectDependencies.gif";    //NOI18N

    private final RakeArtifact antArtifact;
    private final URI artifactLocation;
    private Image cachedIcon;

    ProjectNode (RakeArtifact antArtifact, URI artifactLocation, UpdateHelper helper,ReferenceHelper refHelper, String classPathId, String entryId) {
        super (Children.LEAF, createLookup (antArtifact, artifactLocation, helper, refHelper, classPathId, entryId));
        this.antArtifact = antArtifact;
        this.artifactLocation = artifactLocation;
    }

    public String getDisplayName () {        
        ProjectInformation info = getProjectInformation();        
        if (info != null) {
            return MessageFormat.format(NbBundle.getMessage(ProjectNode.class,"TXT_ProjectArtifactFormat"),
                    new Object[] {info.getDisplayName(), artifactLocation.toString()});
        }
        else {
            return NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName");
        }
    }

    public String getName () {
        return this.getDisplayName();
    }

    public Image getIcon(int type) {
        if (cachedIcon == null) {
            ProjectInformation info = getProjectInformation();
            if (info != null) {
                Icon icon = info.getIcon();
                cachedIcon = Utilities.icon2Image(icon);
            }
            else {
                cachedIcon = Utilities.loadImage(PROJECT_ICON);
            }
        }
        return cachedIcon;
    }

    public Image getOpenedIcon(int type) {
        return this.getIcon(type);
    }

    public boolean canCopy() {
        return false;
    }

    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get (OpenProjectAction.class),
            //SystemAction.get (ShowRDocAction.class),
            //SystemAction.get (RakeMigrateAction.class),
            SystemAction.get (RemoveClassPathRootAction.class),
        };
    }

    public Action getPreferredAction () {
        return getActions(false)[0];
    }
    
    private ProjectInformation getProjectInformation () {
        Project p = this.antArtifact.getProject();
        if (p != null) {
            return ProjectUtils.getInformation(p);
        }
        return null;
    }
    
    private static Lookup createLookup (RakeArtifact antArtifact, URI artifactLocation, 
            UpdateHelper helper, ReferenceHelper refHelper, String classPathId, String entryId) {
        Project p = antArtifact.getProject();
        Object[] content;
//        if (p == null) {
//            content = new Object[1];
//        }
//        else {
//            content = new Object[3];
//            content[1] = new JavadocProvider(antArtifact, artifactLocation);
//            content[2] = p;
//        }
//        content[0] = new Removable(helper,refHelper, classPathId, entryId);        
        if (p == null) {
            content = new Object[0];
        } else {
            content = new Object[1];
            //content[0] = new JavadocProvider(antArtifact, artifactLocation);
            content[0] = p;
        }
        Lookup lkp = Lookups.fixed(content);
        return lkp;
    }

    private static class OpenProjectAction extends NodeAction {

        protected void performAction(Node[] activatedNodes) {
            Project[] projects = new Project[activatedNodes.length];
            for (int i=0; i<projects.length;i++) {
                projects[i] = (Project) activatedNodes[i].getLookup().lookup(Project.class);
            }
            OpenProjects.getDefault().open(projects, false);
        }

        protected boolean enable(Node[] activatedNodes) {
            final Collection/*<Project>*/ openedProjects =Arrays.asList(OpenProjects.getDefault().getOpenProjects());
            for (int i=0; i<activatedNodes.length; i++) {
                Project p;
                if ((p = (Project) activatedNodes[i].getLookup().lookup(Project.class)) == null) {
                    return false;
                }
                if (openedProjects.contains(p)) {
                    return false;
                }
            }
            return true;
        }

        public String getName() {
            return NbBundle.getMessage (ProjectNode.class,"CTL_OpenProject");
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx (OpenProjectAction.class);
        }

        protected boolean asynchronous() {
            return false;
        }
    }
}
