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
package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.compilers.ToolchainProject;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Sergey Grinev
 */
public abstract class AbstractExecutorRunAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes == null || activatedNodes.length == 0 || activatedNodes.length > 1) {
            enabled = false;
        } else {
            DataObject dataObject = activatedNodes[0].getCookie(DataObject.class);
            if (accept(dataObject)) {
                enabled = true;
            } else {
                enabled = false;
            }
        }
        return enabled;
    }

    protected abstract boolean accept(DataObject object);

    protected static String getDevelopmentHost(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);

        String developmentHost = CompilerSetManager.getDefaultDevelopmentHost();
        if (project != null) {
            RemoteProject info = project.getLookup().lookup(RemoteProject.class);
            if (info != null) {
                developmentHost = info.getDevelopmentHost();
            }
        }
        return developmentHost;
    }

    protected String getMakeCommand(Node node){
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(fileObject);
        String makeCommand = null;
        if (project != null) {
            ToolchainProject toolchain = project.getLookup().lookup(ToolchainProject.class);
            if (toolchain != null) {
                Tool tool = toolchain.getCompilerSet().findTool(Tool.MakeTool);
                if (tool != null) {
                    makeCommand = tool.getPath();
                }
            }
        }
        if (makeCommand == null || makeCommand.length()==0) {
            MakeExecSupport mes = node.getCookie(MakeExecSupport.class);
            makeCommand = mes.getMakeCommand();
        }
        return makeCommand;
    }

    protected File getBuildDirectory(Node node){
	MakeExecSupport mes = node.getCookie(MakeExecSupport.class);
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
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
            // FIXUP
        }
        return buildDir;
    }

    protected static String[] prepareEnv(String developmentHost) {
        CompilerSet cs = null;
        String csdirs = ""; // NOI18N
        String dcsn = CppSettings.getDefault().getCompilerSetName();
        PlatformInfo pi = PlatformInfo.getDefault(developmentHost);
        if (dcsn != null && dcsn.length() > 0) {
            cs = CompilerSetManager.getDefault(developmentHost).getCompilerSet(dcsn);
            if (cs != null) {
                csdirs = cs.getDirectory();
                // TODO Provide platform info
                String commands = cs.getCompilerFlavor().getCommandFolder(pi.getPlatform());
                if (commands != null && commands.length()>0) {
                    // Also add msys to path. Thet's where sh, mkdir, ... are.
                    csdirs += pi.pathSeparator() + commands;
                }
            }
        }
        String[] envp;
        if (csdirs.length() > 0) {
            envp = new String[] { pi.getPathAsStringWith(csdirs) };
        } else {
            envp = new String[0];
        }
        return envp;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP; // FIXUP ???
    }

    protected final static String getString(String key) {
        return NbBundle.getBundle(AbstractExecutorRunAction.class).getString(key);
    }

    protected final static String getString(String key, String a1) {
        return NbBundle.getMessage(AbstractExecutorRunAction.class, key, a1);
    }
}
