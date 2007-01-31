/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class RunDialogAction extends NodeAction {
    protected JButton runButton;
    private Object options[];

    public RunDialogAction() {
	super();
	runButton = new JButton(getString("RunButtonText")); // NOI18N
        runButton.getAccessibleContext().setAccessibleDescription(getString("RunButtonAD"));
	options = new Object[] {
	    runButton,
	    DialogDescriptor.CANCEL_OPTION,
	};
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
	if (activatedNodes == null || activatedNodes.length != 1)
	    return false;
	DataObject dataObject = (DataObject)activatedNodes[0].getCookie(DataObject.class);
	if (!(dataObject instanceof ExeObject))
	    return false;
	return true;
    }

    public void perform(String executablePath) {
	perform(new RunDialogPanel(executablePath, true, runButton));
    }

    protected void perform(RunDialogPanel runDialogPanel) {
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
			null,
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
