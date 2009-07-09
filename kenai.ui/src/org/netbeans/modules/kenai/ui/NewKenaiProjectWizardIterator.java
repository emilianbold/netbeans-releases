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
package org.netbeans.modules.kenai.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.mercurial.api.Mercurial;
import org.netbeans.modules.subversion.api.Subversion;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 */
public class NewKenaiProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private transient int index;
    private Node activeNode;
    private boolean isShareExistingFolder;

    public static final String PROP_PRJ_NAME = "projectName"; // NOI18N
    public static final String PROP_PRJ_TITLE = "projectTitle"; // NOI18N
    public static final String PROP_PRJ_DESC = "projectDescription"; // NOI18N
    public static final String PROP_PRJ_LICENSE = "projectLicense"; // NOI18N
    public static final String PROP_SCM_TYPE = "projectSCMType"; // NOI18N
    public static final String PROP_SCM_NAME = "projectSCMName"; // NOI18N
    public static final String PROP_SCM_URL = "projectSCMUrl"; // NOI18N
    public static final String PROP_SCM_LOCAL = "projectSCMLocal"; // NOI18N
    public static final String PROP_ISSUES = "projectIssues"; // NOI18N
    public static final String PROP_ISSUES_URL = "projectIssuesUrl"; // NOI18N
    public static final String PROP_AUTO_COMMIT = "projectIssuesUrl"; // NOI18N
    public static final String PROP_CREATE_CHAT = "projectCreateChat"; // NOI18N

    public static final String PROP_EXC_ERR_MSG = "exceptionErrorMessage"; // NOI18N

    // special values when no features are created
    public static final String NO_REPO = "none"; // NOI18N
    public static final String NO_ISSUES = "none"; // NOI18N

    private Logger logger = Logger.getLogger("org.netbeans.modules.kenai"); // NOI18N

    NewKenaiProjectWizardIterator(Node activatedNode) {
        this.activeNode = activatedNode;
        isShareExistingFolder = true;
    }

    NewKenaiProjectWizardIterator() {
        isShareExistingFolder = false;
    }

    public Set<CreatedProjectInfo> instantiate(ProgressHandle handle) throws IOException {

        handle.start(6);

        String newPrjName = (String) wizard.getProperty(PROP_PRJ_NAME);
        String newPrjTitle = (String) wizard.getProperty(PROP_PRJ_TITLE);
        String newPrjDesc = (String) wizard.getProperty(PROP_PRJ_DESC);
        String newPrjLicense = (String) wizard.getProperty(PROP_PRJ_LICENSE);

        String newPrjScmType = (String) wizard.getProperty(PROP_SCM_TYPE);
        String newPrjScmName = (String) wizard.getProperty(PROP_SCM_NAME);
        String newPrjScmUrl = (String) wizard.getProperty(PROP_SCM_URL);
        String newPrjScmLocal = (String) wizard.getProperty(PROP_SCM_LOCAL);

        String newPrjIssues = (String) wizard.getProperty(PROP_ISSUES);
        String newPrjIssuesUrl = (String) wizard.getProperty(PROP_ISSUES_URL);
        boolean autoCommit = Boolean.valueOf((String) wizard.getProperty(PROP_AUTO_COMMIT));
        Boolean createChat = (Boolean) wizard.getProperty(PROP_CREATE_CHAT);

        if (KenaiService.Names.SUBVERSION.equals(newPrjScmType)) {
            if (!Subversion.isClientAvailable(autoCommit)) {
                ((JComponent) current().getComponent()).putClientProperty(PROP_EXC_ERR_MSG,
                        Subversion.CLIENT_UNAVAILABLE_ERROR_MESSAGE);
                throw new IOException(Subversion.CLIENT_UNAVAILABLE_ERROR_MESSAGE);
            }
        }
        // Create project
        try {
            handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProject.progress.creatingProject"), 1);

            logger.log(Level.FINE, "Creating Kenai Project - Name: " + newPrjName +
                    ", Title: " + newPrjTitle + ", Description: " + newPrjDesc + ", License: " + newPrjLicense); // NOI18N

            Kenai.getDefault().createProject(newPrjName, newPrjTitle,
                    newPrjDesc, new String[] { newPrjLicense }, /*no tags*/ null);

        } catch (KenaiException kex) {
            String errorMsg = getErrorMessage(kex, NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                    "NewKenaiProject.progress.projectCreationFailed"));
            ((JComponent) current().getComponent()).putClientProperty(PROP_EXC_ERR_MSG, errorMsg);
            throw new IOException(errorMsg);
        }

        // Create feature - SCM repository
        boolean repoCreated = false;
        if (!NO_REPO.equals(newPrjScmType)) {
            try {
                handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.creatingRepo"),2);
                String displayName = getScmDisplayName(newPrjScmType);
                String description = getScmDescription(newPrjScmType);
                String extScmUrl = (KenaiService.Names.EXTERNAL_REPOSITORY.equals(newPrjScmType) ? newPrjScmUrl : null);

                logger.log(Level.FINE, "Creating SCM Repository - Name: " + newPrjScmName +
                        ", Type: " + newPrjScmType + ", Ext. URL: " + newPrjScmUrl + ", Local Folder: " + newPrjScmLocal); // NOI18N

                Kenai.getDefault().getProject(newPrjName).createProjectFeature(newPrjScmName,
                        displayName, description, newPrjScmType, /*ext issues URL*/ null, extScmUrl, /*browse repo URL*/ null);

                repoCreated = KenaiService.Names.SUBVERSION.equals(newPrjScmType) || KenaiService.Names.MERCURIAL.equals(newPrjScmType);

            } catch (KenaiException kex) {
                String errorMsg = getErrorMessage(kex, NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.repoCreationFailed")); // NOI18N
                ((JComponent) current().getComponent()).putClientProperty(PROP_EXC_ERR_MSG, errorMsg);
                throw new IOException(errorMsg);
            }
        } else {
            logger.log(Level.FINE, "SCM Repository creation skipped."); // NOI18N
        }

        // Create feature - Issue tracking
        if (!NO_ISSUES.equals(newPrjIssues)) {
            try {
                handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.creatingIssues"),3);
                String displayName = getIssuesDisplayName(newPrjIssues);
                String description = getIssuesDescription(newPrjIssues);
                String extIssuesUrl = (KenaiService.Names.EXTERNAL_ISSUES.equals(newPrjIssues) ? newPrjIssuesUrl : null);

                logger.log(Level.FINE, "Creating Issue Tracking - Name: " + newPrjIssues + ", Ext. URL: " + newPrjIssuesUrl); // NOI18N

                // XXX issue tracking name not clear !!!
                Kenai.getDefault().getProject(newPrjName).createProjectFeature(newPrjName + newPrjIssues,
                    displayName, description, newPrjIssues, extIssuesUrl, /*ext repo URL*/ null, /*browse repo URL*/ null);

            } catch (KenaiException kex) {
                String errorMsg = getErrorMessage(kex, NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.issuesCreationFailed"));
                ((JComponent) current().getComponent()).putClientProperty(PROP_EXC_ERR_MSG, errorMsg);
                throw new IOException(errorMsg);
            }
        } else {
            logger.log(Level.FINE, "Issue Tracking creation skipped."); // NOI18N
        }

        // After the repository is created it must be checked out
        if (repoCreated) {
            try {
                KenaiFeature features[] = Kenai.getDefault().getProject(newPrjName).getFeatures(KenaiService.Type.SOURCE);
                String scmLoc = null;
                String featureService = null;
                for (KenaiFeature feature : features) {
                    if (newPrjScmName.equals(feature.getName())) {
                        scmLoc = feature.getLocation();
                        featureService = feature.getService();
                        continue;
                    }
                }
                if (scmLoc != null) {
                    handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                            "NewKenaiProject.progress.repositoryCheckout"),4);
                    logger.log(Level.FINE, "Checking out repository - Location: " + scmLoc +
                            ", Local Folder: " + newPrjScmLocal + ", Service: " + featureService); // NOI18N
                    PasswordAuthentication passwdAuth = Kenai.getDefault().getPasswordAuthentication();
                    if (passwdAuth != null) {
                        final File localFile = new File(newPrjScmLocal);
                        if (KenaiService.Names.SUBVERSION.equals(featureService)) {
                            try {
                                if (isShareExistingFolder) {
                                    String initialRevision = NbBundle.getMessage(NewKenaiProjectWizardIterator.class, "NewKenaiProject.initialRevision", newPrjTitle);
                                    String dirName = new File(newPrjScmLocal).getName();
                                    final String remoteDir = scmLoc.concat("/" + dirName); // NOI18N
                                    Subversion.mkdir(remoteDir, passwdAuth.getUserName(), new String(passwdAuth.getPassword()), initialRevision);
                                    Subversion.checkoutRepositoryFolder(remoteDir, new String[]{"."}, localFile, // NOI18N
                                            passwdAuth.getUserName(), new String(passwdAuth.getPassword()), true, false);
                                    if (autoCommit) {
                                        handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                                                "NewKenaiProject.progress.repositoryCommit"), 5);
                                        Subversion.commit(new File[]{localFile}, passwdAuth.getUserName(), new String(passwdAuth.getPassword()), initialRevision);
                                    }
                                } else {
                                    Subversion.checkoutRepositoryFolder(scmLoc, new String[]{"."}, localFile, // NOI18N
                                            passwdAuth.getUserName(), new String(passwdAuth.getPassword()), false);
                                }
                            } catch (IOException io) {
                                if (Subversion.CLIENT_UNAVAILABLE_ERROR_MESSAGE.equals(io.getMessage())) {
                                    // DO SOMETHING, svn client is unavailable
                                }
                                throw io;
                            }
                        } else if (KenaiService.Names.MERCURIAL.equals(featureService)) {

                            Mercurial.cloneRepository(scmLoc,localFile, "", "", "", // NOI18N
                                passwdAuth.getUserName(), new String(passwdAuth.getPassword()));

                        }
                    } else {
                        // user not logged in, do nothing
                    }
                }
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (createChat) {
            handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class, "CTL_CreatingChatProgress"), 6);
            KenaiProject project = Kenai.getDefault().getProject(newPrjName);
            if (project.getFeatures(KenaiService.Type.CHAT).length==0) {
                //chat already exist
                final KenaiFeature f = project.createProjectFeature(
                        newPrjName,
                        NbBundle.getMessage(NewKenaiProjectWizardIterator.class, "CTL_ChatRoomName", newPrjName),
                        NbBundle.getMessage(NewKenaiProjectWizardIterator.class, "CTL_ChatRoomDescription", newPrjName),
                        KenaiService.Names.XMPP_CHAT,
                        null,
                        null,
                        null);
            }
        }

        // Open the project in Dashboard
        Set<CreatedProjectInfo> set = new HashSet<CreatedProjectInfo>();
        try {
            KenaiProject project = Kenai.getDefault().getProject(newPrjName);
            Dashboard.getDefault().addProject(new ProjectHandleImpl(project), true);
            set.add(new CreatedProjectInfo(project, newPrjScmLocal));
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }

        handle.finish();

        return set;
        
    }

    public Set<?> instantiate() throws IOException {
        assert false;
        return null;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        this.panels = getPanels();
    }

    public void uninitialize(WizardDescriptor wizard) {
        // XXX set properties to null ???
    }

    public Panel current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage(NewKenaiProjectWizardIterator.class, "NewKenaiProjectWizardIterator.name");
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void addChangeListener(ChangeListener l) { }

    public void removeChangeListener(ChangeListener l) { }

    // ----------

    public static class CreatedProjectInfo {

        public KenaiProject project;
        public String localRepoPath;

        public CreatedProjectInfo(KenaiProject prj, String pth) {
            project = prj;
            localRepoPath = pth;
        }

    }

    // ----------

    private String getErrorMessage(KenaiException kex, String prepend) {
        String errMsg = null;
        if (kex instanceof KenaiException) {
            KenaiException kem = (KenaiException) kex;
            Map<String,String> errMap = kem.getErrors();
            StringBuffer sb = new StringBuffer();
            if (prepend != null) {
                sb.append(prepend + " "); // NOI18N
            }
            boolean sepAdded = false;
            if (errMap != null) {
                for (Iterator<String> it = errMap.keySet().iterator(); it.hasNext(); ) {
                    String fld = it.next();
                    sb.append(errMap.get(fld) + ". "); // NOI18N
                    sepAdded = true;
                }
            }
            if (sepAdded) {
                errMsg = sb.substring(0, sb.length() - 2);
            } else {
                errMsg = sb.toString();
            }
        } else {
            errMsg = kex.getLocalizedMessage();
        }
        return errMsg;
    }

    private String getScmDisplayName(String scmName) {
        String displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.SourceCodeRepository");
        if (KenaiService.Names.SUBVERSION.equals(scmName)) {
            displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                    "NewKenaiProjectWizardIterator.SubversionRepository");
        } else if (KenaiService.Names.MERCURIAL.equals(scmName)) {
            displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                    "NewKenaiProjectWizardIterator.MercurialRepository");
        } else if (KenaiService.Names.EXTERNAL_REPOSITORY.equals(scmName)) {
            displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                    "NewKenaiProjectWizardIterator.ExternalRepository");
        }
        return displayName;
    }

    private String getScmDescription(String scmName) {
        String desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.SourceCodeRepository");
        if (KenaiService.Names.SUBVERSION.equals(scmName)) {
            desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.SubversionSourceCodeRepository");
        } else if (KenaiService.Names.MERCURIAL.equals(scmName)) {
            desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.MercurialSourceCodeRepository");
        } else if (KenaiService.Names.EXTERNAL_REPOSITORY.equals(scmName)) {
            desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.ExternalSourceCodeRepository");
        }
        return desc;
    }

    private String getIssuesDisplayName(String issues) {
        String displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.IssueTracking");
        if (KenaiService.Names.BUGZILLA.equals(issues)) {
            displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.Bugzilla");
        } else if (KenaiService.Names.JIRA.equals(issues)) {
            displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.JIRA");
        } else if (KenaiService.Names.EXTERNAL_ISSUES.equals(issues)) {
            displayName = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.External");
        }
        return displayName;
    }

    private String getIssuesDescription(String issues) {
        String desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.IssueTracking");
        if (KenaiService.Names.BUGZILLA.equals(issues)) {
            desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.BugzillaIssueTracking");
        } else if (KenaiService.Names.JIRA.equals(issues)) {
            desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.JIRAIssueTracking");
        } else if (KenaiService.Names.EXTERNAL_ISSUES.equals(issues)) {
            desc = NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                "NewKenaiProjectWizardIterator.ExternalIssueTracking");
        }
        return desc;
    }

    // ----------

    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                steps[i] = c.getName();
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    private WizardDescriptor.Panel[] createPanels() {
        if (isShareExistingFolder) {
            return new WizardDescriptor.Panel[]{
                        new NameAndLicenseWizardPanel(activeNode)
                    };

        } else {
            return new WizardDescriptor.Panel[]{
                        new NameAndLicenseWizardPanel(),
                        new SourceAndIssuesWizardPanel()
                    };
        }
    }

}
