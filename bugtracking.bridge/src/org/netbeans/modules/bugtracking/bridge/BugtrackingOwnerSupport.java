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
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.security.auth.login.LoginException;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class BugtrackingOwnerSupport {

    private static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.bridge.BugtrackingOwnerSupport");   // NOI18N

    private static final String REPOSITORY_FOR_FILE_PREFIX = "repository for "; //NOI18N

    private static final String KENAI_PROJECT_URI_REGEXP
       = "(https|http)://(testkenai|kenai)\\.com/(svn|hg)/(\\S*)~(.*)"; //NOI18N
    private static Pattern repositoryPattern = Pattern.compile(KENAI_PROJECT_URI_REGEXP);

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
        return getRepository(file, null, true);
    }

    public Repository getRepository(File file, boolean askIfUnknown) {
        return getRepository(file, null, askIfUnknown);
    }

    public Repository getRepository(File file, String issueId) {
        return getRepository(file, issueId, true);
    }

    public Repository getRepository(File file, String issueId, boolean askIfUnknown) {
        //TODO - synchronization/threading
        Repository repo = fileToRepo.get(file);
        if(repo != null) {
            LOG.log(Level.FINER, " found cached repository [{0}] for file {1}", new Object[] {repo, file}); // NOI18N
        } else {
            LOG.log(Level.FINER, " no repository cached for file {1}", new Object[] {file}); // NOI18N
        }

        if(repo == null && askIfUnknown) {
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
            Repository repository;
            try {
                repository = getKenaiBugtrackingRepository((String) attValue);
            } catch (LoginException ex) {
                return null;
            }

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

        repo = getRepositoryFromPrefs(topmostManagedAncestor);
        if (repo != null) {
            LOG.log(Level.FINER, " found stored repository [" + repo    //NOI18N
                                 + "] for directory " + topmostManagedAncestor); //NOI18N
            fileToRepo.put(topmostManagedAncestor, repo);
            return repo;
        }

        repo = askUserToSpecifyRepository(fileObject, issueId);
        if (repo != null) {
            fileToRepo.put(topmostManagedAncestor, repo);
            storeRepositoryMappingToPrefs(topmostManagedAncestor, repo);
            return repo;
        }

        return null;
    }

    /**
     *
     * @throws  javax.security.auth.login.LoginException
     *          if the user failed to log into Kenai
     */
    private Repository getKenaiBugtrackingRepository(String remoteLocation) throws LoginException {

        if (!isKenaiProjectLocation(remoteLocation)) {
            return null;
        }

        boolean loggedToKenai = checkUserIsLoggedToKenai();
        if (!loggedToKenai) {
            throw new LoginException();
        }

        try {
            KenaiProject project = KenaiProject.forRepository(remoteLocation);
            Repository repository = (project != null) ? getKenaiBugtrackingRepository(project)
                                                      : null;
            return repository;
        } catch (KenaiException ex) {
            LOG.throwing(getClass().getName(),                    //class name
                         "getKenaiBugtrackingRepository(String)", //method name //NOI18N
                         ex);
            return null;
        }
    }

    private boolean isKenaiProjectLocation(String remoteLocation) {
        return repositoryPattern.matcher(remoteLocation).matches();
    }

    /**
     * Checks whether the user is logged to Kenai. If he/she is not logged in,
     * displays the login dialog and allows the user is logged in.
     *
     * @return  {@code true} if the user has already been logged in
     *          or if he/she has successfuly logged in with the dialog;
     *          {@code false} otherwise
     */
    private boolean checkUserIsLoggedToKenai() {
        if (Kenai.getDefault().getPasswordAuthentication() != null) {
            return true;
        } else {
            return UIUtils.showLogin();
        }
    }

    private Repository getKenaiBugtrackingRepository(KenaiProject project) {
        return BugtrackingUtil.getKenaiBugtrackingRepository(project);
    }

    private Repository getRepositoryFromPrefs(File file) {
        String repoString = getRepositoryStringFromPrefs(file);
        if ((repoString == null) || (repoString.length() == 0)) {
            return null;
        }

        char firstChar = repoString.charAt(0);
        if (firstChar == '?') {     //information about loose association
            return null;
        }

        String repositoryName;
        if (firstChar == '!') {
            repositoryName = repoString.substring(1);
            if (repositoryName.length() == 0) {
                return null;
            }
        } else {
            repositoryName = repoString;
        }

        return getRepositoryByName(repositoryName);
    }

    private Repository getRepositoryByName(String requestedName) {
        Repository[] repositories = BugtrackingUtil.getKnownRepositories();
        for (Repository repository : repositories) {
            if (repository.getDisplayName().equals(requestedName)) {
                return repository;
            }
        }

        return null;
    }

    private String getRepositoryStringFromPrefs(File file) {
        String filePath;
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException ex) {
            LOG.throwing(getClass().getCanonicalName(),
                         "getRepositoryForFile",                        //NOI18N
                         ex);
            filePath = file.getAbsolutePath();
        }

        String preferencesKey = REPOSITORY_FOR_FILE_PREFIX + filePath;
        return getPreferences().get(preferencesKey, null);
    }

    protected void storeRepositoryMappingToPrefs(File root, Repository repository) {
        storeRepositoryMappingToPrefs(root, repository, true);
    }

    protected void storeRepositoryMappingToPrefs(File root, Repository repository, boolean strong) {
        String key, value;

        try {
            key = root.getCanonicalPath();
        } catch (IOException ex) {
            LOG.throwing(getClass().getCanonicalName(),
                "storeRepositoryMappingToPrefs",               //NOI18N
                ex);
            return;
        }

        String repositoryName = repository.getDisplayName();
        value = new StringBuilder(1 + repositoryName.length())
                .append(strong ? '!' : '?')
                .append(repositoryName)
                .toString();

        getPreferences().put(REPOSITORY_FOR_FILE_PREFIX + key, value);
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

    public Preferences getPreferences() {
        return NbPreferences.forModule(BugtrackingOwnerSupport.class);
    }

    private String getMessage(String msgKey) {
        return NbBundle.getMessage(BugtrackingOwnerSupport.class, msgKey);
    }

}
