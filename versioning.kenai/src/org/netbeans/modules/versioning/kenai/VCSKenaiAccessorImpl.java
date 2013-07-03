/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.versioning.kenai;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiActivity;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiNotification;
import org.netbeans.modules.kenai.api.KenaiNotification.Modification;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.api.KenaiUIUtils;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.VCSKenaiAccessor.class)
public class VCSKenaiAccessorImpl extends VCSKenaiAccessor implements PropertyChangeListener {

    private static final String KENAI_WEB_SOURCES_REVISION_PATH = "{0}/sources/{1}/revision/{2}"; //NOI18N

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private final Set<KenaiProjectListener> registeredKenaiListenres = new HashSet<KenaiProjectListener>();

    @Override
    public boolean isKenai(String url) {
        return KenaiProject.getNameForRepository(url) != null;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(String url) {
        return getPasswordAuthentication(url, true);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin) {        
        Kenai kenai = getKenai(url);
        if(kenai == null) {
            // this should not happen
            LOG.log(Level.WARNING, "no kenai instance available for url {0}", url); // NOI18N
            return null;
        }
        
        PasswordAuthentication a = kenai != null ? kenai.getPasswordAuthentication() : null;
        if(a != null) {
            return a;
        }

        if(!forceLogin) {
            return null;
        }

        if(!showLogin()) {
            return null;
        }
        return kenai.getPasswordAuthentication();
    }

    @Override
    public boolean showLogin() {
        return KenaiUIUtils.showLogin();
    }

    @Override
    public boolean isLogged (String url) {
        try {
            KenaiProject kp = KenaiProject.forRepository(url);
            return kp != null ? kp.getKenai().getPasswordAuthentication() != null : false;
        } catch (KenaiException ex) {
            LOG.log(Level.WARNING, "isLogged: Cannot load kenai project for {0}", url); //NOI18N
            LOG.log(Level.FINE, null, ex);
        }
        return false;
    }

    @Override
    public KenaiUser forName(String user) {
        return new KenaiUserImpl(new org.netbeans.modules.kenai.ui.api.KenaiUserUI(user));
    }

    @Override
    public KenaiUser forName(String user, String url) {
        assert url != null;
        String fqUserName;
        if(isKenai(url)){
            Kenai kenai = getKenai(url);
            if (kenai == null) {
                // probably offline, do not continue
                return null;
            }
            fqUserName = user + "@" + kenai.getUrl().getHost(); // NOI18N
        } else {
            fqUserName = user;
        }
        return new KenaiUserImpl(new org.netbeans.modules.kenai.ui.api.KenaiUserUI(fqUserName));
    }

    @Override
    public void addVCSNoficationListener(PropertyChangeListener l) {
        PropertyChangeListener[] ls = support.getPropertyChangeListeners(PROP_KENAI_VCS_NOTIFICATION);
        if(ls == null || ls.length == 0) {
            KenaiManager.getDefault().addPropertyChangeListener(this);
            for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
                if (isLoggedIn(kenai)) {
                    attachToDashboard(kenai);
                }
            }
        }
        support.addPropertyChangeListener(PROP_KENAI_VCS_NOTIFICATION, l);
    }

    @Override
    public void removeVCSNoficationListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(PROP_KENAI_VCS_NOTIFICATION, l);
        PropertyChangeListener[] ls = support.getPropertyChangeListeners(PROP_KENAI_VCS_NOTIFICATION);
        if(ls == null || ls.length == 0) {
            KenaiManager.getDefault().removePropertyChangeListener(this);
            for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
                if (!isLoggedIn(kenai)) {
                    detachFromDashboard(kenai);
                }
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(DashboardSupport.PROP_OPENED_PROJECTS)) {
            registerVCSNotificationListener(KenaiUIUtils.getDashboardProjects(true));
        } else if (evt.getPropertyName().equals(Kenai.PROP_LOGIN)) {
            Kenai kenai = (Kenai) evt.getSource();
            if (isLoggedIn(kenai)) {
                attachToDashboard(kenai);
            } else {
                detachFromDashboard(kenai);
            }
        }
    }

   @Override
    public boolean isUserOnline(String user) {
        return org.netbeans.modules.kenai.api.KenaiUser.isOnline(user);
    }

    @Override
    public String getRevisionUrl(String sourcesUrl, String revision) {
        if (revision == null || sourcesUrl == null) {
            throw new NullPointerException("Null parameter");           // NOI18N
        }
        String revisionUrl = null;
        String projectUrl = getProjectUrl(sourcesUrl);
        String repositoryName = getRepositoryName(sourcesUrl);
        if (projectUrl != null && repositoryName != null) {
            // XXX unofficial API
            revisionUrl = KENAI_WEB_SOURCES_REVISION_PATH;
            revisionUrl = java.text.MessageFormat.format(revisionUrl, projectUrl, repositoryName, revision);
        }
        return revisionUrl;
    }

    @Override
    public void logVcsUsage(String vcs, String repositoryUrl) {
        if (repositoryUrl != null && isKenai(repositoryUrl)) {
            KenaiUIUtils.logKenaiUsage("SCM", vcs); // NOI18N
        }
    }

    @Override
    public boolean isAuthorized (String repositoryURL, RepositoryActivity activity) {
        boolean authorized = true;
        Kenai kenai = getKenai(repositoryURL);
        if (kenai != null) {
            try {
                KenaiProject kp = KenaiProject.forRepository(repositoryURL);
                if (kp != null) {
                    authorized = kenai.isAuthorized(kp, getKenaiActivity(activity));
                }
            } catch (KenaiException ex) {
                Logger.getLogger(VCSKenaiAccessorImpl.class.getName()).log(Level.INFO, null, ex);
                authorized = false;
            }
        }
        return authorized;
    }

    private KenaiActivity getKenaiActivity (RepositoryActivity repositoryActivity) {
        if (RepositoryActivity.WRITE.equals(repositoryActivity)) {
            return KenaiActivity.SOURCE_WRITE;
        } else {
            return KenaiActivity.SOURCE_READ;
        }
    }

    /**
     * Attaches a listener to the kenai dashboard if immediate is set to true or user is logged into kenai
     * @param immediate
     */
    private void attachToDashboard (Kenai kenai) {
        KenaiUIUtils.addDashboardListener(kenai, this);
        registerVCSNotificationListener(KenaiUIUtils.getDashboardProjects(true));
    }

    /**
     * Dettaches a listener from the kenai dashboard if immediate is set to true or user is logged into kenai
     * @param immediate
     */
    private void detachFromDashboard (Kenai kenai) {
        KenaiUIUtils.removeDashboardListener(kenai, this);
        unregisterVCSNotificationListener(KenaiUIUtils.getDashboardProjects(true));
    }

    private static boolean isLoggedIn(Kenai kenai) {
        return kenai.getPasswordAuthentication() != null;
    }
    
    private void registerVCSNotificationListener(ProjectHandle<KenaiProject>[] phs) {
        synchronized(registeredKenaiListenres) {
            // unregister registered
            unregisterVCSNotificationListener(phs);
            // register on all handlers
            for (ProjectHandle<KenaiProject> projectHandle : phs) {
                KenaiProject kp = projectHandle.getTeamProject();
                KenaiProjectListener l = new KenaiProjectListener(kp);
                registeredKenaiListenres.add(l);
                kp.addPropertyChangeListener(l);
            }
        }
    }

    private void unregisterVCSNotificationListener(ProjectHandle[] phs) {
        synchronized(registeredKenaiListenres) {
            for (KenaiProjectListener l : registeredKenaiListenres) {
                l.kp.removePropertyChangeListener(l);
            }
        }
    }

    /**
     * Returns a URL of web location of a kenai project associated with given repository url
     * @param sourcesUrl url of a kenai vcs repository
     * @return web location of associated kenai project or null if no such project exists
     */
    private static String getProjectUrl (String sourcesUrl) {
        try {
            KenaiProject kp = KenaiProject.forRepository(sourcesUrl);
            return kp != null ? kp.getWebLocation().toString() : null;
        } catch (KenaiException ex) {
            LOG.log(Level.WARNING, "getProjectUrl: Cannot load kenai project for {0}", sourcesUrl); //NOI18N
            LOG.log(Level.FINE, null, ex);
            return null;
        }
    }

    private String getRepositoryName (String sourcesUrl) {
        String repositoryName = null;
        // XXX unofficial API
        // repository url looks like https://..../mercurial|svn/PROJECT_NAME~REPOSITORY_NAME
        int pos = sourcesUrl.lastIndexOf('~') + 1;                              // NOI18N
        if (pos > 0 && sourcesUrl.length() > pos) {
            String repositoryNameCandidate = sourcesUrl.substring(pos);
            if (repositoryNameCandidate.indexOf('/') == -1) {                   // NOI18N
                repositoryName = repositoryNameCandidate;
            }
        }
        return repositoryName;
    }

    private Kenai getKenai(String repositoryUr) {
        KenaiProject kp = null;
        try {
            kp = KenaiProject.forRepository(repositoryUr);
        } catch (KenaiException ex) {
            LOG.log(Level.FINE, null, ex);
        }
        if(kp == null) {
            return null;
        }
        return kp.getKenai();
    }

    private static class KenaiUserImpl extends KenaiUser {
        org.netbeans.modules.kenai.ui.api.KenaiUserUI delegate;

        public KenaiUserImpl(org.netbeans.modules.kenai.ui.api.KenaiUserUI delegate) {
            this.delegate = delegate;
        }

        @Override
        public void startChat() {
            delegate.startChat();
        }

        @Override
        public void startChat(String msg) {
            delegate.startChat(msg);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            delegate.getKenaiUser().removePropertyChangeListener(listener);
        }

        @Override
        public boolean isOnline() {
            return delegate.getKenaiUser().isOnline();
        }

        @Override
        public String getUser() {
            return delegate.getUserName();
        }

        @Override
        public Icon getIcon() {
            return delegate.getIcon();
        }

        @Override
        public JLabel createUserWidget() {
            return delegate.createUserWidget();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            delegate.getKenaiUser().addPropertyChangeListener(listener);
        }

    }

    private class VCSKenaiNotificationImpl extends VCSKenaiNotification {
        private final KenaiNotification kn;
        private final File projectDir;
        private final KenaiProject kp;

        public VCSKenaiNotificationImpl(KenaiNotification kn, KenaiProject kp, File projectDir) {
            assert kp != null;
            assert kn != null;
            assert kn.getType() == KenaiService.Type.SOURCE;
            this.kn = kn;
            this.kp = kp;
            this.projectDir = projectDir;
        }

        @Override
        public URI getUri() {
            return kn.getUri();
        }

        @Override
        public Date getStamp() {
            return kn.getStamp();
        }

        @Override
        public Service getService() {
            String serviceName = kn.getServiceName();
            if(serviceName.equals(KenaiService.Names.SUBVERSION)) {
                return Service.VCS_SVN;
            } else if (serviceName.equals(KenaiService.Names.MERCURIAL)) {
                return Service.VCS_HG;
            } else {
                LOG.log(Level.WARNING, "Unknown kenai scm service name {0}", serviceName); // NOI18N
                return Service.UNKNOWN;
            }
        }

        @Override
        public List<VCSKenaiModification> getModifications() {
            List<VCSKenaiModification> ret = new ArrayList<VCSKenaiModification>(kn.getModifications().size());
            for(Modification m : kn.getModifications()) {
                ret.add(new VCSKenaiModificationImpl(m));
            }
            return Collections.unmodifiableList(ret);
        }

        @Override
        public String getAuthor() {
            return kn.getAuthor();
        }

        @Override
        public File getProjectDirectory() {
            return projectDir;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("["); // NOI18N
            sb.append(projectDir);
            sb.append(","); // NOI18N
            sb.append(getUri());
            sb.append(","); // NOI18N
            sb.append(getService());
            sb.append(","); // NOI18N
            sb.append(getStamp());
            return sb.toString();
        }
    }

    private class VCSKenaiModificationImpl extends VCSKenaiModification{
        private final Modification m;

        public VCSKenaiModificationImpl(Modification m) {
            this.m = m;
        }

        @Override
        public Type getType() {
            switch(m.getType()) {
                case NEW:
                    return Type.NEW;
                case CHANGE:
                    return Type.CHANGE;
                case DELETE:
                    return Type.DELETE;
                default:
                    throw new IllegalStateException("unknown modification type" + m.getType());   // NOI18N
            }
        }

        @Override
        public String getResource() {
            return m.getResource();
        }

        @Override
        public String getId() {
            return m.getId();
        }
    }

    private class KenaiProjectListener implements PropertyChangeListener {
        private final KenaiProject kp;

        public KenaiProjectListener(KenaiProject kp) {
            this.kp = kp;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            handleKenaiProjectEvent(evt, kp.getName());
        }

    }

    private void handleKenaiProjectEvent(PropertyChangeEvent evt, String projectName) {
        if (!evt.getPropertyName().equals(KenaiProject.PROP_PROJECT_NOTIFICATION)) {
            return ;
        }
        Object newValue = evt.getNewValue();
        if (newValue instanceof KenaiNotification) {
            KenaiNotification kn = (KenaiNotification) newValue;
            if (kn.getType() != KenaiService.Type.SOURCE) {
                return;
            }
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            for (Project project : projects) {
                FileObject projectDir = project.getProjectDirectory();
                String url = VersioningQuery.getRemoteLocation(projectDir.toURI());
                if (url == null) {
                    continue;
                }
                if (!url.equals(kn.getUri().toString())) {
                    continue;
                }
                KenaiProject kp;
                try {
                    kp = KenaiProject.forRepository(url);
                } catch (KenaiException ex) {
                    LOG.log(Level.WARNING, "handleKenaiProjectEvent: Cannot load kenai project for {0}", url); //NOI18N
                    LOG.log(Level.FINE, null, ex);
                    return;
                }
                String name = kp.getName();
                if (name.equals(projectName)) {
                    support.firePropertyChange(PROP_KENAI_VCS_NOTIFICATION, null, new VCSKenaiNotificationImpl(kn, kp, FileUtil.toFile(projectDir)));
                }
            }
        }
    }
    
}
