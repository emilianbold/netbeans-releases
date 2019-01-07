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
package org.netbeans.modules.subversion.remote.ui.checkout;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.wizards.CheckoutWizard;
import org.netbeans.modules.subversion.remote.util.CheckoutCompleted;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * 
 */
@ActionID(id = "org.netbeans.modules.subversion.remote.ui.checkout.CheckoutAction", category = "SubversionRemote")
@ActionRegistration(displayName = "#LBL_CheckoutAction_Name")
@ActionReferences({
   @ActionReference(path="Versioning/SubversionRemote/Actions/Global", position=300 /*, separatorAfter=350*/)
})
public final class CheckoutAction implements ActionListener, HelpCtx.Provider {
           
    private static final String WORKING_COPY_FORMAT_PROP = "svnkit.wc.17"; //NOI18N

    public CheckoutAction() {
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.subversion.ui.checkout.CheckoutAction");
    }
          
    @Override
    public void actionPerformed(ActionEvent ae) {
        org.netbeans.modules.versioning.util.Utils.logVCSActionEvent("SVN"); //NOI18N
        performCheckout(false);
    }
        
    private VCSFileProxy performCheckout (final boolean wait) {
        assert !wait || !EventQueue.isDispatchThread(); // cannot wait in AWT
        final FileSystem defaultFileSystem = VCSFileProxySupport.getDefaultFileSystem();
        if (defaultFileSystem == null || !VCSFileProxySupport.isConnectedFileSystem(defaultFileSystem)) {
            return null;
        }
        CheckoutWizard wizard = new CheckoutWizard(defaultFileSystem);
        if (!wizard.show()) {
            return null;
        
        }
        final SVNUrl repository = wizard.getRepositoryRoot();
        final RepositoryFile[] repositoryFiles = wizard.getRepositoryFiles();
        final VCSFileProxy workDir = wizard.getWorkdir();
        if(!Subversion.getInstance().checkClientAvailable(new Context(workDir))) {
            return null;
        }
        final boolean atWorkingDirLevel = wizard.isAtWorkingDirLevel();
        final boolean doExport = wizard.isExport();
        final boolean showCheckoutCompleted = SvnModuleConfig.getDefault(defaultFileSystem).getShowCheckoutCompleted();
        final boolean old16Format = wizard.isOldFormatPreferred();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                final String oldPreference = System.getProperty(WORKING_COPY_FORMAT_PROP);
                System.setProperty(WORKING_COPY_FORMAT_PROP, Boolean.toString(!old16Format));
                SvnClient client;
                final Context context = new Context(workDir);
                try {
                    // this needs to be done in a background thread, otherwise the password won't be acquired from the keyring
                    client = Subversion.getInstance().getClient(context, repository);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(context, ex, true, true); // should not happen
                    return;
                }
                Task t = performCheckout(defaultFileSystem, repository, client, repositoryFiles, workDir, atWorkingDirLevel, doExport, showCheckoutCompleted);
                t.addTaskListener(new TaskListener() {
                    @Override
                    public void taskFinished (Task task) {
                        if (oldPreference == null) {
                            System.clearProperty(WORKING_COPY_FORMAT_PROP);
                        } else {
                            System.setProperty(WORKING_COPY_FORMAT_PROP, oldPreference);
                        }
                    }
                });
                if (wait) {
                    t.waitFinished();
                }
            }
        };
        if (wait) {
            run.run();
        } else {
            Subversion.getInstance().getRequestProcessor().post(run);
        }
        return wizard.getWorkdir();
    }
    
    private RequestProcessor.Task performCheckout(
        final FileSystem fileSystem,
        final SVNUrl repository,
        final SvnClient client,
        final RepositoryFile[] repositoryFiles,
        final VCSFileProxy workingDir,
        final boolean atWorkingDirLevel,
        final boolean doExport,
        final boolean showCheckoutCompleted)
    {
        SvnProgressSupport support = new SvnProgressSupport(fileSystem) {
            @Override
            public void perform() {
                try {
                    setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/remote/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
                    setCancellableDelegate(client);
                    client.addNotifyListener(this);
                    checkout(client, repository, repositoryFiles, workingDir, atWorkingDirLevel, doExport, this);
                } catch (SVNClientException ex) {
                    annotate(ex);
                    return;
                } finally {
                    Subversion.getInstance().versionedFilesChanged();
                    client.removeNotifyListener(this);
                }
                if(isCanceled()) {
                    return;
                }
                setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/remote/ui/checkout/Bundle").getString("LBL_ScanFolders_Progress"));
                if(showCheckoutCompleted) showCheckoutCompletet(repositoryFiles, workingDir, atWorkingDirLevel, doExport, this);
            }
        };
        return support.start(Subversion.getInstance().getRequestProcessor(repository), repository, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/remote/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
    }

    public static void checkout(final SvnClient client,
                                final SVNUrl repository,
                                final RepositoryFile[] repositoryFiles,
                                final VCSFileProxy workingDir,
                                final boolean atWorkingDirLevel,
                                final boolean doExport,
                                final SvnProgressSupport support)
    throws SVNClientException
    {
        final VCSFileProxy[] destinations = new VCSFileProxy[repositoryFiles.length];
        for (int i = 0; i < repositoryFiles.length; i++) {
            VCSFileProxy destination;
            if(!atWorkingDirLevel) {
                destination = VCSFileProxy.createFileProxy(workingDir, repositoryFiles[i].getName()); // XXX what if the whole repository is seletcted
                destination = destination.normalizeFile();
                VCSFileProxySupport.mkdir(destination);
            } else {
                destination = workingDir;
            }
            destinations[i] = destination;
        }
        SvnUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                for (int i = 0; i < repositoryFiles.length; i++) {
                    VCSFileProxy destination = destinations[i];
                    if(support!=null && support.isCanceled()) { 
                        return null;
                    }
                    if(doExport) {
                        client.doExport(repositoryFiles[i].getFileUrl(), destination, repositoryFiles[i].getRevision(), true);
                    } else {
                        client.checkout(repositoryFiles[i].getFileUrl(), destination, repositoryFiles[i].getRevision(), true);
                    }
                }
                return null;
            }
        }, destinations);
    }

    private static void showCheckoutCompletet(
        final RepositoryFile[] repositoryFiles,
        final VCSFileProxy workingDir,
        final boolean atWorkingDirLevel,
        final boolean doExport,
        final SvnProgressSupport support)
    {
        String[] folders;
        if (atWorkingDirLevel) {
            folders = new String[1];
            folders[0] = "."; // NOI18N
        } else {
            folders = new String[repositoryFiles.length];
            for (int i = 0; i < repositoryFiles.length; i++) {
                if (support != null && support.isCanceled()) {
                    return;
                }
                if (repositoryFiles[i].isRepositoryRoot()) {
                    folders[i] = "."; // NOI18N
                } else {
                    folders[i] = repositoryFiles[i].getFileUrl().getLastPathSegment();
                }
            }
        }
        CheckoutCompleted cc = new CheckoutCompleted(workingDir, folders);
        if (support != null && support.isCanceled()) {
            return;
        }
        cc.scanForProjects(support, doExport ? CheckoutCompleted.Type.EXPORT : CheckoutCompleted.Type.CHECKOUT);
        return;
    }

}
