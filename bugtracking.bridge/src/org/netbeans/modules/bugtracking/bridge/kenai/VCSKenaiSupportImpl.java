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
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.kenai.api.KenaiNotification;
import org.netbeans.modules.kenai.api.KenaiNotification.Modification;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.versioning.util.VCSKenaiSupport;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.VCSKenaiSupport.class)
public class VCSKenaiSupportImpl extends VCSKenaiSupport implements PropertyChangeListener {

    private Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.bridge.kenai.VCSKenaiSupport");  // NOI18N

    private static final String KENAI_WEB_SOURCES_REVISION_PATH = "{0}/sources/{1}/revision/{2}"; //NOI18N

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    
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
        org.netbeans.modules.kenai.ui.spi.KenaiUserUI kenaiUser =
                org.netbeans.modules.kenai.ui.spi.KenaiUserUI.forName(user);
        if(kenaiUser == null) {
            return null;
        } else {
            return new KenaiUserImpl(kenaiUser);
        }
    }

    public void addVCSNoficationListener(PropertyChangeListener l) {
        PropertyChangeListener[] ls = support.getPropertyChangeListeners(PROP_KENAI_VCS_NOTIFICATION);
        if(ls == null || ls.length ==0) {
            Dashboard.getDefault().addPropertyChangeListener(this);
            registerVCSNotificationListener(Dashboard.getDefault().getOpenProjects());
        }
        support.addPropertyChangeListener(PROP_KENAI_VCS_NOTIFICATION, l);
    }

    public void removeVCSNoficationListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(PROP_KENAI_VCS_NOTIFICATION, l);
        PropertyChangeListener[] ls = support.getPropertyChangeListeners(PROP_KENAI_VCS_NOTIFICATION);
        if(ls == null || ls.length == 0) {
            Dashboard.getDefault().removePropertyChangeListener(this);
        }
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Dashboard.PROP_OPENED_PROJECTS)) {
            registerVCSNotificationListener(Dashboard.getDefault().getOpenProjects());
        } else if (evt.getPropertyName().equals(KenaiProject.PROP_PROJECT_NOTIFICATION)) {
            Object newValue = evt.getNewValue();
            if(newValue instanceof KenaiNotification) {
                KenaiNotification kn = ((KenaiNotification) newValue);
                if(kn.getType() != KenaiService.Type.SOURCE) {
                    return;
                }
                support.firePropertyChange(PROP_KENAI_VCS_NOTIFICATION, null, new VCSKenaiNotificationImpl(kn));
            }
        }
    }

    private void registerVCSNotificationListener(ProjectHandle[] phs) {
        for (ProjectHandle projectHandle : phs) {
            KenaiProject kp = KenaiUtil.getKenaiProject(projectHandle);
            kp.removePropertyChangeListener(this);
            kp.addPropertyChangeListener(this);
        }
    }

    @Override
    public boolean isUserOnline(String user) {
        return org.netbeans.modules.kenai.ui.spi.KenaiUserUI.isOnline(user);
    }

    @Override
    public String getRevisionUrl(String sourcesUrl, String revision) {
        if (revision == null || sourcesUrl == null) {
            throw new NullPointerException("Null parameter");           //NOI18N
        }
        String revisionUrl = null;
        String projectUrl = KenaiUtil.getProjectUrl(sourcesUrl);
        String repositoryName = getRepositoryName(sourcesUrl);
        if (projectUrl != null && repositoryName != null) {
            // XXX unofficial API
            revisionUrl = KENAI_WEB_SOURCES_REVISION_PATH;               //NOI18N
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
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            delegate.removePropertyChangeListener(listener);
        }

        @Override
        public boolean isOnline() {
            return delegate.isOnline();
        }

        @Override
        public String getUser() {
            return delegate.getUser();
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
            delegate.addPropertyChangeListener(listener);
        }

    }

    private class VCSKenaiNotificationImpl extends VCSKenaiNotification {
        private final KenaiNotification kn;

        public VCSKenaiNotificationImpl(KenaiNotification kn) {
            assert kn.getType() == KenaiService.Type.SOURCE;
            this.kn = kn;
        }

        public URI getUri() {
            return kn.getUri();
        }

        public Date getStamp() {
            return kn.getStamp();
        }

        public Service getService() {
            if(kn.getServiceName().equals(KenaiService.Names.SUBVERSION)) {
                return Service.VCS_SVN;
            } else if (kn.getServiceName().equals(KenaiService.Names.MERCURIAL)) {
                return Service.VCS_HG;
            }
            return Service.UNKNOWN;
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
}
