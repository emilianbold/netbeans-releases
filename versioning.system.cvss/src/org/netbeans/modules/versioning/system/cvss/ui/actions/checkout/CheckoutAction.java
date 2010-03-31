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

package org.netbeans.modules.versioning.system.cvss.ui.actions.checkout;

import org.netbeans.modules.versioning.system.cvss.ui.wizards.CheckoutWizard;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.executor.CheckoutExecutor;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.api.project.Project;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import org.netbeans.modules.versioning.util.ProjectUtilities;

/**
 * Shows checkout wizard, performs the checkout,
 * detects checked out projects and optionally
 * shows open/create project dialog.
 * 
 * @author Petr Kuzel
 */
public final class CheckoutAction extends SystemAction {

    private static final String SHOW_CHECKOUT_COMPLETED = "checkoutAction.showCheckoutCompleted";
    
    // avoid instance fields, it's singleton
    
    public CheckoutAction() {
        setIcon(null);        
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getBundle(CheckoutAction.class).getString("CTL_MenuItem_Checkout_Label");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CheckoutAction.class);
    }

    /**
     * Shows interactive checkout wizard.
     */
    public void actionPerformed(ActionEvent ev) {
        perform();
    }


    /**
     * Perform asynchronous checkout action with preconfigured values.
     * On succesfull finish shows open project dialog.
     *
     * @param modules comma separated list of modules
     * @param tag branch name of <code>null</code> for trunk
     * @param workingDir target directory
     * @param scanProject if true scan folder for projects and show UI (subject of global setting)
     * @param group if specified checkout is added into tthe group without executing it.
     *
     * @return async executor
     */
    public CheckoutExecutor checkout(String cvsRoot, String modules, String tag, String workingDir, boolean scanProject, ExecutorGroup group) {
        CheckoutCommand cmd = new CheckoutCommand();

        String moduleString = modules;
        if (moduleString == null || moduleString.length() == 0) {
            moduleString = ".";  // NOI18N
        }
        StringTokenizer tokenizer = new StringTokenizer(moduleString, ",;");  // NOI18N
        if (tokenizer.countTokens() == 1) {
            cmd.setModule(moduleString);
        } else {
            List moduleList = new ArrayList();
            while( tokenizer.hasMoreTokens()) {
                String s = tokenizer.nextToken().trim();
                moduleList.add(s);
            }
            String[] modules2 = (String[]) moduleList.toArray(new String[moduleList.size()]);
            cmd.setModules(modules2);
        }

        cmd.setDisplayName(NbBundle.getMessage(CheckoutAction.class, "BK1006"));

        if (tag != null) {
            cmd.setCheckoutByRevision(tag);
        } else {
            cmd.setResetStickyOnes(true);
        }
        cmd.setPruneDirectories(CvsModuleConfig.getDefault().getAutoPruneDirectories());
        cmd.setRecursive(true);

        File workingFolder = new File(workingDir);
        File[] files = new File[] {workingFolder};
        cmd.setFiles(files);

        CvsVersioningSystem cvs = CvsVersioningSystem.getInstance();
        GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
        gtx.setCVSRoot(cvsRoot);

        boolean execute = false;
        if (group == null) {
            execute = true;
            group = new ExecutorGroup(NbBundle.getMessage(CheckoutAction.class, "BK1013"));
        }
        CheckoutExecutor executor = new CheckoutExecutor(cvs, cmd, gtx, workingFolder);
        group.addExecutor(executor);
        if (CvsModuleConfig.getDefault().getPreferences().getBoolean(SHOW_CHECKOUT_COMPLETED, true) && scanProject) {
            group.addBarrier(new CheckoutCompletedController(executor, workingFolder));
        }

        if (execute) {
            group.execute();
        }
        return executor;
    }

    /**
     * Shows interactive checkout wizard.
     */
    public void perform() {
        CheckoutWizard wizard = new CheckoutWizard();
        if (wizard.show() == false) return;

        final String tag = wizard.getTag();
        final String modules = wizard.getModules();
        final String workDir = wizard.getWorkingDir();
        final String cvsRoot = wizard.getCvsRoot();
        CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(new Runnable() {
            public void run() {
                checkout(cvsRoot, modules, tag, workDir, true, null);
            }
        });
    }

    /** On task finish shows next steps UI.*/
    private class CheckoutCompletedController implements Runnable {

        private final CheckoutExecutor executor;
        private final File workingFolder;

        public CheckoutCompletedController(CheckoutExecutor executor, File workingFolder) {
            this.executor = executor;
            this.workingFolder = workingFolder;
        }

        public void run() {

            if (executor.isSuccessful() == false) {
                return;
            }

            Map<Project, Set<Project>> checkedOutProjects = new HashMap<Project, Set<Project>>();
            checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
            File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
            // checkout creates new folders and cache must be aware of them
            refreshRecursively(normalizedWorkingFolder);
            FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
            if (fo != null) {
                String name = NbBundle.getMessage(CheckoutAction.class, "BK1007");
                executor.getGroup().progress(name);
                Iterator it = executor.getExpandedModules().iterator();
                while (it.hasNext()) {
                    String module = (String) it.next();
                    if (".".equals(module)) {                   // NOI18N
                        // root folder is scanned, skip remaining modules
                        ProjectUtilities.scanForProjects(fo, checkedOutProjects);
                        break;
                    } else {
                        FileObject subfolder = fo.getFileObject(module);
                        if (subfolder != null) {
                            executor.getGroup().progress(name);
                            ProjectUtilities.scanForProjects(subfolder, checkedOutProjects);
                        }
                    }
                }
            }
            // open project selection
            ProjectUtilities.openCheckedOutProjects(checkedOutProjects, workingFolder);
        }

        /**
         * Refreshes statuses of this folder and all its parent folders up to filesystem root.
         * 
         * @param folder folder to refresh
         */ 
        private void refreshRecursively(File folder) {
            if (folder == null) return;
            refreshRecursively(folder.getParentFile());
            CvsVersioningSystem.getInstance().getStatusCache().refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
}
