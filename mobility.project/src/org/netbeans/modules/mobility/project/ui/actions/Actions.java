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
package org.netbeans.modules.mobility.project.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.modules.mobility.project.J2MEActionProvider;
import org.netbeans.modules.mobility.project.ui.J2MEPhysicalViewProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * A bunch of indirection to allow layer-based registration of project actions.
 *
 * @author Tim Boudreau
 */
public class Actions {

    public static final String ACTIONS_PATH = "Projects/org-netbeans-modules-mobility-project/Actions/Root"; //NOI18N
    public static final String FOREIGN_ACTIONS_PATH = "Projects/Actions"; //NOI18N


    public static Action[] actions(boolean broken) {
        List<Action> actions = new ArrayList<Action>(Utilities.actionsForPath(ACTIONS_PATH));
        List<? extends Action> foreign = Utilities.actionsForPath(FOREIGN_ACTIONS_PATH);
        actions.addAll(Math.max(0, actions.size() - 2), foreign);
        if (broken) {
            actions.add(Math.max(0, actions.size() - 1), createBrokenLinksAction());
        }
        return actions.toArray(new Action[actions.size()]);
    }

    public static Action createBuildAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_BUILD,
                bundle.getString("LBL_BuildAction_Name"), null); //NOI18N
    }

    public static Action createRebuildAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_REBUILD,
                bundle.getString("LBL_RebuildAction_Name"), null); //NOI18N
    }

    public static Action createCleanAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_CLEAN,
                bundle.getString("LBL_CleanAction_Name"), null); //NOI18N
    }

    public static Action createJavadocAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                J2MEActionProvider.COMMAND_JAVADOC,
                bundle.getString("LBL_JavadocAction_Name"), null); //NOI18N
    }

    public static Action createDeployAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                J2MEActionProvider.COMMAND_DEPLOY,
                bundle.getString("LBL_DeployAction_Name"), null); //NOI18N
    }

    public static Action createBuildAllAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                J2MEActionProvider.COMMAND_BUILD_ALL,
                bundle.getString("LBL_BuildAllAction_Name"), null); //NOI18N
    }

    public static Action createRebuildAllAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                J2MEActionProvider.COMMAND_REBUILD_ALL,
                bundle.getString("LBL_RebuildAllAction_Name"), null); //NOI18N
    }

    public static Action createCleanAllAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                J2MEActionProvider.COMMAND_CLEAN_ALL,
                bundle.getString("LBL_CleanAllAction_Name"), null); //NOI18N
    }

    public static Action createDeployAllAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                J2MEActionProvider.COMMAND_DEPLOY_ALL,
                bundle.getString("LBL_DeployAllAction_Name"), null); //NOI18N
    }

    public static Action createRunAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_RUN,
                bundle.getString("LBL_RunAction_Name"), null); //NOI18N
    }

    public static Action createRunWithAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                J2MEActionProvider.COMMAND_RUN_WITH,
                bundle.getString("LBL_RunWithAction_Name"), null); //NOI18N
    }

    public static Action createDebugAction() {
        ResourceBundle bundle = NbBundle.getBundle(J2MEPhysicalViewProvider.class);
        return ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_DEBUG,
                bundle.getString("LBL_DebugAction_Name"), null); //NOI18N
    }

    public static Action createFindAction() {
        return SystemAction.get(FindAction.class);
    }

    public static Action createRefreshPackagesAction() {
        return new RefreshPackagesAction();
    }

    public static Action createBrokenLinksAction() {
        return new BrokenLinksAction();
    }
}
