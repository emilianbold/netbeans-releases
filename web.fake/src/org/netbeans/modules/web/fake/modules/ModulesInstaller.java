/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.web.fake.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
public class ModulesInstaller {

    private Collection<UpdateElement> modules4install;
    private RequestProcessor.Task installTask = null;
    private OperationContainer<InstallSupport> installContainer;
    private ProgressHandle downloadHandle;
    private ProgressHandle verifyHandle;
    private ProgressHandle installHandle;
    private final ProgressMonitor progressMonitor;

    public ModulesInstaller (Collection<UpdateElement> modules, ProgressMonitor progressMonitor) {
        if (modules == null || modules.isEmpty ()) {
            throw new IllegalArgumentException ("Cannot construct InstallerMissingModules with null or empty Collection " + modules);
        }
        modules4install = modules;
        if (progressMonitor != null) {
            this.progressMonitor = progressMonitor;
        } else {
            this.progressMonitor = ProgressMonitor.DEV_NULL_PROGRESS_MONITOR;
        }
    }

    public static boolean installModules (String... codeNames) {
        return installModules(null, codeNames);
    }

    public static boolean installModules (ProgressMonitor progressMonitor, String... codeNames) {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ!";
        boolean success = false;

        FindComponentModules findModules = new FindComponentModules(codeNames);
        findModules.createFindingTask().waitFinished();

        Collection<UpdateElement> toInstall = findModules.getModulesForInstall();
        Collection<UpdateElement> toEnable = findModules.getModulesForEnable();
        if (toInstall != null && !toInstall.isEmpty()) {
            ModulesInstaller installer = new ModulesInstaller(toInstall, progressMonitor);
            installer.getInstallTask ().schedule (10);
            installer.getInstallTask ().waitFinished();
            findModules.createFindingTask().waitFinished();
            success = findModules.getModulesForInstall ().isEmpty ();
        } else if (toEnable != null && !toEnable.isEmpty()) {
            ModulesActivator enabler = new ModulesActivator(toEnable, progressMonitor);
            enabler.getEnableTask ().schedule (100);
            enabler.getEnableTask ().waitFinished();
            success = true;
        }

        return success;
    }

    public RequestProcessor.Task getInstallTask () {
        if (installTask == null) {
            installTask = createInstallTask ();
        }
        return installTask;
    }

    private RequestProcessor.Task createInstallTask () {
        assert installTask == null || installTask.isFinished () : "The Install Task cannot be started nor scheduled.";
        installTask = RequestProcessor.getDefault ().create (doInstall);
        return installTask;
    }

    private Runnable doInstall = new Runnable () {
        public void run() {
            installMissingModules ();
        }

    };

    private void installMissingModules () {
        try {
            doInstallMissingModules ();
        } catch (Exception x) {
            JButton tryAgain = new JButton ();
            tryAgain.addActionListener(new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    if (installContainer != null) {
                        try {
                            installContainer.getSupport ().doCancel ();
                        } catch (Exception ex) {
                            Logger.getLogger (ModulesInstaller.class.getName ()).
                                    log (Level.INFO, ex.getLocalizedMessage (), ex);
                        }
                    }
                    RequestProcessor.Task task = getInstallTask ();
                    if (task != null) {
                        task.schedule (10);
                    }
                }
            });
            Logger.getLogger (ModulesInstaller.class.getName ()).log (Level.INFO, x.getLocalizedMessage (), x);
            tryAgain.setEnabled (getInstallTask () != null);
            Mnemonics.setLocalizedText (tryAgain, getBundle ("InstallerMissingModules_TryAgainButton"));
            NotifyDescriptor nd = new NotifyDescriptor (
                    getErrorNotifyPanel (x),
                    getBundle ("InstallerMissingModules_ErrorPanel_Title"),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object [] { tryAgain, NotifyDescriptor.OK_OPTION },
                    NotifyDescriptor.OK_OPTION
                    );
            DialogDisplayer.getDefault ().notifyLater (nd);
        }
    }

    private JComponent getErrorNotifyPanel (Exception x) {
        JTextArea area = new JTextArea ();
        area.setWrapStyleWord (true);
        area.setLineWrap (true);
        area.setEditable (false);
        area.setRows (15);
        area.setColumns (40);
        area.setOpaque (false);
        area.setText (getBundle ("InstallerMissingModules_ErrorPanel", x.getLocalizedMessage (), x));
        return area;
    }

    private void doInstallMissingModules () throws OperationException {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot be called in EQ.";
        installContainer = null;
        for (UpdateElement module : modules4install) {
            if (installContainer == null) {
                boolean isNewOne = module.getUpdateUnit ().getInstalled () == null;
                if (isNewOne) {
                    installContainer = OperationContainer.createForInstall ();
                } else {
                    installContainer = OperationContainer.createForUpdate ();
                }
            }
            if (installContainer.canBeAdded (module.getUpdateUnit (), module)) {
                installContainer.add (module);
            }
        }
        if (installContainer.listAll ().isEmpty ()) {
            return ;
        }
        assert installContainer.listInvalid ().isEmpty () :
            "No invalid Update Elements " + installContainer.listInvalid ();
        if (! installContainer.listInvalid ().isEmpty ()) {
            throw new IllegalArgumentException ("Some are invalid for install: " + installContainer.listInvalid ());
        }
        InstallSupport installSupport = installContainer.getSupport ();
        if (downloadHandle == null) {
            downloadHandle = ProgressHandleFactory.createHandle (
                getBundle ("InstallerMissingModules_Download",
                presentUpdateElements (FindComponentModules.getVisibleUpdateElements (modules4install))));
        }
        progressMonitor.onDownload(downloadHandle);
        Validator v = installSupport.doDownload (downloadHandle, false);
        if (verifyHandle == null) {
            verifyHandle = ProgressHandleFactory.createHandle (
                    getBundle ("InstallerMissingModules_Verify"));
            }
        progressMonitor.onValidate(verifyHandle);
        Installer i = installSupport.doValidate (v, verifyHandle);
        if (installHandle == null) {
            installHandle = ProgressHandleFactory.createHandle (
                    getBundle ("InstallerMissingModules_Install"));
            }
        progressMonitor.onInstall(installHandle);
        Restarter r = installSupport.doInstall (i, installHandle);
        assert r == null : "Restart cannot be needed";
        waitToModuleLoaded ();
        /// XXX FindBrokenModules.clearModulesForRepair ();
    }

    public static String presentUpdateElements (Collection<UpdateElement> elems) {
        String res = "";
        for (UpdateElement el : new LinkedList<UpdateElement> (elems)) {
            res += res.length () == 0 ? el.getDisplayName () : ", " + el.getDisplayName (); // NOI18N
        }
        return res;
    }

    private void waitToModuleLoaded () {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot be called in EQ.";
        RequestProcessor.Task waitTask = RequestProcessor.getDefault ().create (new Runnable () {
            public void run () {
            }
        });
        for (UpdateElement m : modules4install) {
           while (! m.isEnabled ()) {
               waitTask.schedule (100);
               waitTask.waitFinished ();
           }
        }

    }

    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (ModulesInstaller.class, key, params);
    }
}
