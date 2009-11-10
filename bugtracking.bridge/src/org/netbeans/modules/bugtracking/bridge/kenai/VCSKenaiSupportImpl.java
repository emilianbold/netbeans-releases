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

package org.netbeans.modules.bugtracking.bridge.kenai;

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
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiNotification;
import org.netbeans.modules.kenai.api.KenaiNotification.Modification;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.versioning.util.VCSKenaiSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.VCSKenaiSupport.class)
public class VCSKenaiSupportImpl extends VCSKenaiSupport implements PropertyChangeListener {

    private static final String KENAI_WEB_SOURCES_REVISION_PATH = "{0}/sources/{1}/revision/{2}"; //NOI18N
    private static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation"; // NOI18N

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    private final Set<KenaiProjectListener> registeredKenaiListenres = new HashSet<KenaiProjectListener>();

    @Override
    public boolean isKenai(String url) {
        return KenaiUtil.isKenai(url);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return getPasswordAuthentication(true);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(boolean forceLogin) {
        return KenaiUtil.getPasswordAuthentication(forceLogin);
    }

    @Override
    public boolean showLogin() {
        return KenaiUtil.showLogin();
    }

    @Override
    public void setFirmAssociations(File[] files, String url) {
        Repository repo = KenaiUtil.getKenaiRepository(url);
        if(repo == null) {
            LOG.warning("No issue tracker available for the given vcs url " + url);         // NOI18N
            return;
        }
        BugtrackingOwnerSupport.getInstance().setFirmAssociations(files, repo);
    }


    @Override
    public boolean isLogged () {
        return KenaiUtil.isLoggedIn();
    }

    @Override
    public KenaiUser forName(String user) {
        return new KenaiUserImpl(new org.netbeans.modules.kenai.ui.spi.KenaiUserUI(user));
    }

    public void addVCSNoficationListener(PropertyChangeListener l) {
        PropertyChangeListener[] ls = support.getPropertyChangeListeners(PROP_KENAI_VCS_NOTIFICATION);
        if(ls == null || ls.length == 0) {
            Kenai.getDefault().addPropertyChangeListener(Kenai.PROP_LOGIN, this);
            attachToDashboard(false);
        }
        support.addPropertyChangeListener(PROP_KENAI_VCS_NOTIFICATION, l);
    }

    public void removeVCSNoficationListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(PROP_KENAI_VCS_NOTIFICATION, l);
        PropertyChangeListener[] ls = support.getPropertyChangeListeners(PROP_KENAI_VCS_NOTIFICATION);
        if(ls == null || ls.length == 0) {
            Kenai.getDefault().removePropertyChangeListener(Kenai.PROP_LOGIN, this);
            detachFromDashboard(false);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Dashboard.PROP_OPENED_PROJECTS)) {
            registerVCSNotificationListener(Dashboard.getDefault().getOpenProjects());
        } else if (evt.getPropertyName().equals(Kenai.PROP_LOGIN)) {
            if (KenaiUtil.isLoggedIn()) {
                attachToDashboard(true);
            } else {
                detachFromDashboard(true);
            }
        }
    }

    /**
     * Attaches a listener to the kenai dashboard if immediate is set to true or user is logged into kenai
     * @param immediate
     */
    private void attachToDashboard (boolean immediate) {
        if (immediate || KenaiUtil.isLoggedIn()) {
            Dashboard.getDefault().addPropertyChangeListener(this);
            registerVCSNotificationListener(Dashboard.getDefault().getOpenProjects());
        }
    }

    /**
     * Dettaches a listener from the kenai dashboard if immediate is set to true or user is logged into kenai
     * @param immediate
     */
    private void detachFromDashboard (boolean immediate) {
        if (immediate || KenaiUtil.isLoggedIn()) {
            Dashboard.getDefault().removePropertyChangeListener(this);
            unregisterVCSNotificationListener(Dashboard.getDefault().getOpenProjects());
        }
    }

    private void registerVCSNotificationListener(ProjectHandle[] phs) {
        synchronized(registeredKenaiListenres) {
            // unregister registered
            unregisterVCSNotificationListener(phs);
            // register on all handlers
            for (ProjectHandle projectHandle : phs) {
                KenaiProject kp = KenaiUtil.getKenaiProject(projectHandle);
                kp.addPropertyChangeListener(new KenaiProjectListener(kp));
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
        String projectUrl = KenaiUtil.getProjectUrl(sourcesUrl);
        String repositoryName = getRepositoryName(sourcesUrl);
        if (projectUrl != null && repositoryName != null) {
            // XXX unofficial API
            revisionUrl = KENAI_WEB_SOURCES_REVISION_PATH;               
            revisionUrl = java.text.MessageFormat.format(revisionUrl, projectUrl, repositoryName, revision);
        }
        return revisionUrl;
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

    @Override
    public void logVcsUsage(String vcs, String repositoryUrl) {
        if (repositoryUrl != null && isKenai(repositoryUrl)) {
            UIUtils.logKenaiUsage("SCM", vcs); // NOI18N
        }
    }

    private class KenaiUserImpl extends KenaiUser {
        org.netbeans.modules.kenai.ui.spi.KenaiUserUI delegate;

        public KenaiUserImpl(org.netbeans.modules.kenai.ui.spi.KenaiUserUI delegate) {
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
        private String serviceName;

        public VCSKenaiNotificationImpl(KenaiNotification kn, KenaiProject kp, File projectDir) {
            assert kp != null;
            assert kn != null;
            assert kn.getType() == KenaiService.Type.SOURCE;
            this.kn = kn;
            this.kp = kp;
            this.projectDir = projectDir;
        }

        public URI getUri() {
            return kn.getUri();
        }

        public Date getStamp() {
            return kn.getStamp();
        }

        public Service getService() {
            if(serviceName == null) {
                KenaiFeature[] features = null;
                try {
                    features = kp.getFeatures(KenaiService.Type.SOURCE);
                    for (KenaiFeature kf : features) {
                        if(kf.getName().equals(kn.getServiceName())) {
                            serviceName = kf.getService();
                            break;
                        }
                    }
                } catch (KenaiException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                if(serviceName == null) {
                    // fallback
                    serviceName = kn.getServiceName();
                }

            }

            if(serviceName.equals(KenaiService.Names.SUBVERSION)) {
                return Service.VCS_SVN;
            } else if (serviceName.equals(KenaiService.Names.MERCURIAL)) {
                return Service.VCS_HG;
            } else {
                LOG.warning("Unknown kenai scm service name " + serviceName);
                return Service.UNKNOWN;
            }
        }

        public List<VCSKenaiModification> getModifications() {
            List<VCSKenaiModification> ret = new ArrayList<VCSKenaiModification>(kn.getModifications().size());
            for(Modification m : kn.getModifications()) {
                ret.add(new VCSKenaiModificationImpl(m));
            }
            return Collections.unmodifiableList(ret);
        }

        public String getAuthor() {
            return kn.getAuthor();
        }

        @Override
        public File getProjectDirectory() {
            return projectDir;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            sb.append(projectDir);
            sb.append(",");
            sb.append(getUri());
            sb.append(",");
            sb.append(getService());
            sb.append(",");
            sb.append(getStamp());
            return sb.toString();
        }
    }

    private class VCSKenaiModificationImpl extends VCSKenaiModification{
        private final Modification m;

        public VCSKenaiModificationImpl(Modification m) {
            this.m = m;
        }

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

        public String getResource() {
            return m.getResource();
        }

        public String getId() {
            return m.getId();
        }
    }

    private class KenaiProjectListener implements PropertyChangeListener {
        private final KenaiProject kp;

        public KenaiProjectListener(KenaiProject kp) {
            this.kp = kp;
        }

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
                String url = (String) projectDir.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
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
                    LOG.log(Level.WARNING, null, ex);
                    return;
                }
                String name = kp.getName();
                if (name.equals(projectName)) {
                    support.firePropertyChange(PROP_KENAI_VCS_NOTIFICATION, null, new VCSKenaiNotificationImpl(kn, kp, FileUtil.toFile(projectDir)));
                }
            }
        }
        return;
    }
    
}
