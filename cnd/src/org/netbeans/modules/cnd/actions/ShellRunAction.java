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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.loaders.ShellDataObject;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;

/**
 * Base class for Make Actions ...
 */
public class ShellRunAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes)  {
	boolean enabled = false;

	if (activatedNodes == null || activatedNodes.length == 0 || activatedNodes.length > 1) {
	    enabled = false;
	}
	else {
	    DataObject dataObject = activatedNodes[0].getCookie(DataObject.class);
	    if (dataObject instanceof ShellDataObject)
		enabled = true;
	    else
		enabled = false;
	}
	return enabled;
    }

    public String getName() {
        return getString("BTN_Run");
    }

    protected void performAction(Node[] activatedNodes) {
        // Save everything first
        LifecycleManager.getDefault().saveAll();
        
        for (int i = 0; i < activatedNodes.length; i++)
            performAction(activatedNodes[i]);
    }

    public HelpCtx getHelpCtx () {
	return HelpCtx.DEFAULT_HELP; // FIXUP ???
    }

    public void performAction(Node node) {
	ShellExecSupport bes = node.getCookie(ShellExecSupport.class);
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        File shellFile = FileUtil.toFile(fileObject);
        // Build directory
        String bdir = bes.getRunDirectory();
        File buildDir;
        if (bdir.length() == 0 || bdir.equals(".")) { // NOI18N
            buildDir = shellFile.getParentFile();
        } else if (IpeUtils.isPathAbsolute(bdir)) {
            buildDir = new File(bdir);
        } else {
            buildDir = new File(shellFile.getParentFile(), bdir);
        }
        try {
            buildDir = buildDir.getCanonicalFile();
        }
        catch (IOException ioe) {
            // FIXUP
        }
        // Tab Name
        String tabName = getString("RUN_LABEL", node.getName());
        
	String[] shellCommandAndArgs = bes.getShellCommandAndArgs(fileObject); // from inside shell file or properties
        String shellCommand = shellCommandAndArgs[0];
        String shellFilePath = IpeUtils.toRelativePath(buildDir.getPath(), shellFile.getPath()); // Absolute path to shell file
	String[] args = bes.getArguments(); // from properties
        
        // Windows: The command is usually of the from "/bin/sh", but this
        // doesn't work here, so extract the 'sh' part and use that instead. 
        // FIXUP: This is not entirely correct though.
        if (Utilities.isWindows() && shellCommand.length() > 0) {
            int i = shellCommand.lastIndexOf("/"); // UNIX PATH // NOI18N
            if (i >= 0) {
                shellCommand = shellCommand.substring(i+1);
            }
        }
        
        StringBuilder argsFlat = new StringBuilder();
        if (shellCommandAndArgs[0].length() > 0) {
            for (int i = 1; i < shellCommandAndArgs.length; i++) {
                argsFlat.append(" "); // NOI18N
                argsFlat.append(shellCommandAndArgs[i]);
            }
        }
        argsFlat.append(shellFilePath);
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }
       
        // Execute the makefile
        String[] envp = { Path.getPathName() + '=' + CppSettings.getDefault().getPath() };
        try {
            new NativeExecutor(
                buildDir.getPath(),
                shellCommand,
                argsFlat.toString(),
                envp,
                tabName,
                "Run", // NOI18N
                true).execute();
        } catch (IOException ioe) {
        }    
    }
        
    protected final static String getString(String key) {
        return NbBundle.getBundle(ShellRunAction.class).getString(key);
    }
    
    protected final static String getString(String key, String a1) {
        return NbBundle.getMessage(ShellRunAction.class, key, a1);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
