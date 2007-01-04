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

package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.loaders.MakefileDataObject;
import org.netbeans.modules.cnd.settings.MakeSettings;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Base class for Make Actions ...
 */
public abstract class MakeBaseAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes)  {
	boolean enabled = false;

	if (activatedNodes == null || activatedNodes.length == 0 || activatedNodes.length > 1) {
	    enabled = false;
	}
	else {
	    DataObject dataObject = (DataObject)activatedNodes[0].getCookie(DataObject.class);
	    if (dataObject instanceof MakefileDataObject)
		enabled = true;
	    else
		enabled = false;
	}
	return enabled;
    }

    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++)
            performAction(activatedNodes[i], "");
    }

    public HelpCtx getHelpCtx () {
	return HelpCtx.DEFAULT_HELP; // FIXUP ???
    }

//    public void actionPerformed(ActionEvent evt) {
//	Node[] activeNodes = WindowManager.getDefault().getRegistry ().getActivatedNodes();
//	performAction(activeNodes);
//    }

    protected void performAction(Node node, String target) {
	MakeExecSupport mes = (MakeExecSupport) node.getCookie(MakeExecSupport.class);
        DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        if (MakeSettings.getDefault().getSaveAll()) {
            LifecycleManager.getDefault().saveAll();
        }
        
        File makefile = FileUtil.toFile(fileObject);
        // Build directory
        String bdir = mes.getBuildDirectory();
        File buildDir;
        if (bdir.length() == 0 || bdir.equals(".")) { // NOI18N
            buildDir = makefile.getParentFile();
        } else if (IpeUtils.isPathAbsolute(bdir)) {
            buildDir = new File(bdir);
        } else {
            buildDir = new File(makefile.getParentFile(), bdir);
        }
        try {
            buildDir = buildDir.getCanonicalFile();
        }
        catch (IOException ioe) {
            ;; // FIXUP
        }
        // Executable
        String executable = mes.getMakeCommand();
        // Arguments
        String arguments = "-f " + makefile.getName() + " " + target; // NOI18N
        // Tab Name
        String tabName = "make"; // NOI18N
        if (target != null && target.length() > 0)
            tabName += " " + target; // NOI18N
        
        // Execute the makefile
        try {
        new NativeExecutor(
                buildDir.getPath(),
                executable,
                arguments,
                null,
                tabName,
                "make", // NOI18N
                true).execute();
        }
        catch (IOException ioe) {
            System.err.println("make failed: " + ioe); // FIXUP
        }    
    }
    
    protected final static String getString(String key) {
        return NbBundle.getBundle(MakeBaseAction.class).getString(key);
    }
}
