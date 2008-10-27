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

package org.netbeans.modules.cnd.makeproject.api.actions;

import java.util.ResourceBundle;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.loaders.ExeObject;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.RunDialogPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.loaders.CoreElfObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class RunDialogAction extends NodeAction {
    protected JButton runButton = null;
    private Object options[];

    private void init() {
	if (runButton == null) {
	    runButton = new JButton(getString("RunButtonText")); // NOI18N
	    runButton.getAccessibleContext().setAccessibleDescription(getString("RunButtonAD"));
	    options = new Object[] {
		runButton,
		DialogDescriptor.CANCEL_OPTION,
	    };
	}
    }

    public String getName () {
	return getString("RUN_COMMAND"); // NOI18N
    }

    protected void performAction (final Node[] activatedNodes) {
	String path = null;
	if (activatedNodes != null && activatedNodes.length == 1) {
	    DataObject dataObject = (DataObject)activatedNodes[0].getCookie(DataObject.class);
	    if (dataObject != null && dataObject instanceof ExeObject) {
		Node node = dataObject.getNodeDelegate();
		path = FileUtil.toFile(dataObject.getPrimaryFile()).getPath();
	    }
	}
	perform(path);
    }

    protected boolean enable(Node[] activatedNodes) {
	if (activatedNodes == null || activatedNodes.length != 1) {
	    return false;
        }
	DataObject dataObject = (DataObject)activatedNodes[0].getCookie(DataObject.class);
        // disabled for core files, see issue 136696
	if (!(dataObject instanceof ExeObject) || dataObject instanceof CoreElfObject) {
	    return false;
        }
	return true;
    }

    public void perform(String executablePath) {
	if (runButton == null) {
	    init();
	}
	perform(new RunDialogPanel(executablePath, true, runButton));
    }

    protected void perform(RunDialogPanel runDialogPanel) {
	if (runButton == null) {
	    init();
	}
	DialogDescriptor dialogDescriptor = new DialogDescriptor(
	    runDialogPanel,
	    getString("RunDialogTitle"),
	    true,
	    options,
	    runButton,
	    DialogDescriptor.BOTTOM_ALIGN,
	    null,
	    null);
	Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
	if (ret == runButton) {
	    Project project = runDialogPanel.getSelectedProject();
	    RunProfile profile = ConfigurationSupport.getProjectDescriptor(project).getConfs().getActive().getProfile();
	    String path = runDialogPanel.getExecutablePath();
            path = IpeUtils.toRelativePath(profile.getRunDirectory(), path); // FIXUP: should use rel or abs ...
	    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
			project,
			ProjectActionEvent.RUN,
			IpeUtils.getBaseName(path) + " (run)", // NOI18N
			path,
			ConfigurationSupport.getProjectDescriptor(project).getConfs().getActive(),
			profile,
			false);
	    ProjectActionSupport.fireActionPerformed(new ProjectActionEvent[] {projectActionEvent});
	}
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(RunDialogAction.class); // FIXUP ???
    }

    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(RunDialogAction.class);
	}
	return bundle.getString(s);
    }
}
