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

package org.netbeans.modules.bugtracking.bridge;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugtrackingOwnerSupport {

    private static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.bridge.BugtrackingOwnerSupport");   // NOI18N

    private Map<File, Repository> fileToRepo = new HashMap<File, Repository>(10);
    private static BugtrackingOwnerSupport instance;

    private BugtrackingOwnerSupport() { }

    public static BugtrackingOwnerSupport getInstance() {
        if(instance == null) {
            instance = new BugtrackingOwnerSupport();
        }
        return instance;
    }
    
    public Repository getRepository(File file) {
        return getRepository(file, null);
    }

    public Repository getRepository(File file, String issueId) {
        // check cached values firts
        // XXX - todo
        // 1.) cache repository for a VCS topmostanagedParent
        // 2.) persist cache
        Repository repo = fileToRepo.get(file);
        if(repo != null) {
            LOG.log(Level.FINER, " found cached repository [" + repo + "] for file " + file); // NOI18N
        }

        // XXX todo
        // 1.) check if file belongs to a kenai project and create repository from
        // its metadata eventually
        // 2.) store in cache
        if(repo == null) {
            repo = getBugtrackingOwner(file, issueId);
            LOG.log(Level.FINER, " caching repository [" + repo + "] for file " + file); // NOI18N
            fileToRepo.put(file, repo);
        }

        return repo;
    }

    private Repository getBugtrackingOwner(File file, String issueId) {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null) {
            return null;
        }

        Object attValue = fileObject.getAttribute(
                                   "ProvidedExtensions.RemoteLocation");//NOI18N
        if (attValue instanceof String) {
            Repository repository = getKenaiBugtrackingRepository(fileObject, (String) attValue);
            if (repository != null) {
                return repository;
            }
        }

        VersioningSystem owner = VersioningSupport.getOwner(file);
        if (owner == null) {
            return null;
        }

        //XXX: should be the nearest managed ancestor (rather than topmost)
        File topmostManagedAncestor = owner.getTopmostManagedAncestor(file);
        if (topmostManagedAncestor == null) {
            assert false;       //this should not happen
            return null;
        }

        Repository repo = fileToRepo.get(topmostManagedAncestor);
        if (repo != null) {
            LOG.log(Level.FINER, " found cached repository [" + repo    //NOI18N
                                 + "] for directory " + topmostManagedAncestor); //NOI18N
            return repo;
        }

        repo = askUserToSpecifyRepository(fileObject, issueId);
        if (repo != null) {
            fileToRepo.put(topmostManagedAncestor, repo);
            return repo;
        }

        return null;
    }

    private Repository getKenaiBugtrackingRepository(FileObject fileObj,
                                                     String remoteLocation) {
        try {
            KenaiProject project = KenaiProject.forRepository(remoteLocation);
            Repository repository = (project != null) ? getKenaiBugtrackingRepository(project)
                                                      : null;
            return repository;
        } catch (KenaiException ex) {
            LOG.throwing(getClass().getName(),     //class name
                         "getBugtrackingOwner",    //method name        //NOI18N
                         ex);
            return null;
        }
    }

    private Repository getKenaiBugtrackingRepository(KenaiProject project) {
        return BugtrackingUtil.getKenaiBugtrackingRepository(project);
    }

    private Repository askUserToSpecifyRepository(FileObject file, String issueId) {
        Repository[] repos = BugtrackingUtil.getKnownRepositories();
        BugtrackingConnector[] connectors = BugtrackingUtil.getBugtrackingConnectors();

        final RepositorySelectorPanel selectorPanel = new RepositorySelectorPanel(repos, connectors);

        final String dialogTitle = getMessage("LBL_BugtrackerSelectorTitle"); //NOI18N
        final String selectButtonLabel = getMessage("CTL_Select");            //NOI18N

        class ButtonActionListener implements ActionListener {
            private Dialog dialog;
            void setDialog(Dialog dialog) {
                this.dialog = dialog;
            }
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == selectButtonLabel) {
                    boolean valuesAreValid = selectorPanel.validateValues();
                    if (valuesAreValid) {
                        assert dialog != null;
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                }
            }
        }

        final ButtonActionListener actionListener = new ButtonActionListener();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(selectorPanel,
                             dialogTitle,
                             true,
                             new Object[] {selectButtonLabel, DialogDescriptor.CANCEL_OPTION},
                             selectButtonLabel,
                             DialogDescriptor.DEFAULT_ALIGN,
                             null,      //HelpCtx
                             actionListener);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        actionListener.setDialog(dialog);

        dialog.pack();
        dialog.setVisible(true);

        if (dialogDescriptor.getValue() == selectButtonLabel) {
            Repository repository = selectorPanel.getSelectedRepository();
            try {
                repository.getController().applyChanges();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                repository = null;
            }
            return repository;
        } else {
            return null;
        }
    }

    private String getMessage(String msgKey) {
        return NbBundle.getMessage(BugtrackingOwnerSupport.class, msgKey);
    }

}
