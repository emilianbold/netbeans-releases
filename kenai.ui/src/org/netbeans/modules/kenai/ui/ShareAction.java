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
package org.netbeans.modules.kenai.ui;

import java.text.MessageFormat;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.kenai.ui.NewKenaiProjectWizardIterator.CreatedProjectInfo;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class ShareAction extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        assert activatedNodes.length==1;
        actionPerformed(activatedNodes[0]);
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(ShareAction.class, "CTL_ShareAction");
    }

    protected Class[] cookieClasses() {
        return new Class[]{Project.class, DataFolder.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    static boolean isSupported(FileObject fo) {
        String remoteLocation = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        return remoteLocation==null;
    }

    public static void actionPerformed(Node e) {

        WizardDescriptor wizardDescriptor = new WizardDescriptor(new NewKenaiProjectWizardIterator(e));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(NbBundle.getMessage(NewKenaiProjectAction.class,
                "ShareAction.dialogTitle"));

        DialogDisplayer.getDefault().notify(wizardDescriptor);

        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            Set<CreatedProjectInfo> createdProjects = wizardDescriptor.getInstantiatedObjects();
            showDashboard(createdProjects);
        }

    }

    private static void showDashboard(Set<CreatedProjectInfo> projects) {
        final KenaiTopComponent kenaiTc = KenaiTopComponent.getDefault();
        kenaiTc.open();
        kenaiTc.requestActive();
        DashboardImpl.getInstance().selectAndExpand(projects.iterator().next().project);

    }

}

