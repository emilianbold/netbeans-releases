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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.ErrorManager;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataFolder;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Action to open the project(s) corresponding to selected folder(s).
 * Enabled only for projects which are not already open, and thus useful
 * for opening projects encountered in the Favorites tab, as well as nested
 * projects found beneath open projects in the Files tab.
 * @see "#54122"
 * @author Jesse Glick
 */
public final class OpenProjectFolderAction extends AbstractAction implements ContextAwareAction {
    
    public OpenProjectFolderAction() {
        super(NbBundle.getMessage(OpenProjectFolderAction.class, "OpenProjectFolderAction.LBL_action"));
    }
    
    public @Override void actionPerformed(ActionEvent e) {
        // Cannot be invoked without any context.
        assert false;
    }
    
    public @Override Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    
    private static final class ContextAction extends AbstractAction {
        
        /** Projects to be opened. */
        private final Set<Project> projects;
        
        public ContextAction(Lookup context) {
            projects = new HashSet<Project>();
            // Collect projects corresponding to selected folders.
	    for (DataFolder d: context.lookupAll(DataFolder.class)) {
                Project p = null;
                try {
                    p = ProjectManager.getDefault().findProject(d.getPrimaryFile());
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                if (p != null) {
                    projects.add(p);
                }
                // Ignore folders not corresponding to projects (will not disable action if some correspond to projects).
            }
            if (projects.isEmpty()) {
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            }
            // Ignore projects which are already open (will not disable action if some can be opened).
            projects.removeAll(Arrays.asList(OpenProjectList.getDefault().getOpenProjects()));
            int size = projects.size();
            if (size == 1) {
                String name = ProjectUtils.getInformation(projects.iterator().next()).getDisplayName();
                putValue(Action.NAME, NbBundle.getMessage(OpenProjectFolderAction.class, "OpenProjectFolderAction.LBL_menu_one", name));
            } else if (size > 1) {
                putValue(Action.NAME, NbBundle.getMessage(OpenProjectFolderAction.class, "OpenProjectFolderAction.LBL_menu_multiple", size));
            } else {
                putValue(Action.NAME, NbBundle.getMessage(OpenProjectFolderAction.class, "OpenProjectFolderAction.LBL_action"));
                setEnabled(false);
            }
        }
        
        public @Override void actionPerformed(ActionEvent e) {
            // Run asynch so that UI is not blocked; might show progress dialog (?).
            RequestProcessor.getDefault().post(new Runnable() {
                public @Override void run() {
                    OpenProjectList.getDefault().open(projects.toArray(new Project[projects.size()]), false, true);
                }
            });
        }
        
    }
    
}
