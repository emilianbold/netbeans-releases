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

package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.loaders.MakefileDataObject;
import org.netbeans.modules.cnd.settings.CppSettings;
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
        String tabName = getString("MAKE_LABEL", node.getName());
        if (target != null && target.length() > 0)
            tabName += " " + target; // NOI18N
        
        // Execute the makefile
        String[] envp = { Path.getPathName() + '=' + Path.getPathAsString() };
        try {
            new NativeExecutor(
                    buildDir.getPath(),
                    executable,
                    arguments,
                    envp,
                    tabName,
                    "make", // NOI18N
                    true).execute();
        } catch (IOException ioe) {
        }    
    }
    
    protected final static String getString(String key) {
        return NbBundle.getBundle(MakeBaseAction.class).getString(key);
    }
    protected final static String getString(String key, String a1) {
        return NbBundle.getMessage(MakeBaseAction.class, key, a1);
    }
}
