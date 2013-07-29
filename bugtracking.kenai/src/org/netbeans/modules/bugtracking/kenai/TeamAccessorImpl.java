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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.team.spi.TeamAccessor;
import org.netbeans.modules.bugtracking.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.NBBugzillaUtils;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiProjectMember;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.team.server.ui.common.NbModuleOwnerSupport;
import org.netbeans.modules.team.server.ui.common.NbModuleOwnerSupport.OwnerInfo;
import org.netbeans.modules.kenai.ui.api.KenaiUserUI;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.api.KenaiUIUtils;
import org.netbeans.modules.team.server.ui.common.DashboardSupport;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProviders({@ServiceProvider(service=org.netbeans.modules.bugtracking.team.spi.TeamAccessor.class),
                                           @ServiceProvider(service=org.netbeans.modules.bugtracking.kenai.TeamAccessorImpl.class)})
public class TeamAccessorImpl extends TeamAccessor {

    private final List<PropertyChangeListener> allKenaiListeners = new ArrayList<PropertyChangeListener>(1);
    public TeamAccessorImpl() {
        super();
        KenaiManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(KenaiManager.PROP_INSTANCES.equals(evt.getPropertyName())) {
                    if(evt.getNewValue() != null) {
                        Kenai k = (Kenai) evt.getNewValue();
                        synchronized(allKenaiListeners) {
                            for (PropertyChangeListener l : allKenaiListeners) {
                                addPropertyChangeListener(l, k);
                            }
                        }
                    } else {
                        Kenai k = (Kenai) evt.getOldValue();
                        synchronized(allKenaiListeners) {
                            for (PropertyChangeListener l : allKenaiListeners) {
                                removePropertyChangeListener(l, k);
                            }
                        }
                    }
                }
            }
        });
    }

    
    static TeamAccessorImpl getInstance() {
        return Lookup.getDefault().lookup(TeamAccessorImpl.class);
    }

    @Override
    public void logTeamUsage(Object... parameters) {
        KenaiUIUtils.logKenaiUsage(parameters); 
    }

    @Override
    public boolean isLoggedIn(String url) {
        boolean loggedIn = false;
        if (url == null) {
            // is the user logged into any kenai?
            // we're just interested if the user works with any kenais
            for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
                if (kenai.getPasswordAuthentication() != null) {
                    loggedIn = true;
                    break;
                }
            }
        } else {
            // is the user logged into a concrete kenai instance?
            Kenai kenai = getKenai(url);
            loggedIn = (kenai != null) && (kenai.getPasswordAuthentication() != null);
        }
        return loggedIn;
    }


    @Override
    public boolean showLogin() {
        return showLoginIntern();
    }

    @Override
    public Collection<RepositoryUser> getProjectMembers(org.netbeans.modules.bugtracking.team.spi.TeamProject kp) throws IOException {
        if(kp instanceof TeamProjectImpl) {
            List<RepositoryUser> members;
            KenaiProjectMember[] kenaiMembers = ((TeamProjectImpl)kp).getProject().getMembers();
            members = new ArrayList<RepositoryUser>(kenaiMembers.length);
            for (KenaiProjectMember member : kenaiMembers) {
                KenaiUser user = member.getKenaiUser();
                members.add(new RepositoryUser(user.getUserName(), user.getFirstName()+" "+user.getLastName())); // NOI18N
            }
            if (members == null) {
                members = Collections.emptyList();
            }
            return members;
        }
        return null;
    }


    @Override
    public PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin) {
        Kenai kenai = getKenai(url);
        if (kenai == null) {
            Support.LOG.log(Level.FINEST, "no kenai for url : [{0}]", url);
            return null;
        }
        return getPasswordAuthentication(kenai, forceLogin);
    }

    @Override
    public boolean isNBTeamServerRegistered() {
        Collection<Kenai> kenais = KenaiManager.getDefault().getKenais();
        for (Kenai kenai : kenais) {
            URL url = kenai.getUrl();
            if(NBBugzillaUtils.isNbRepository(url.toString())) {
                return true;
            }        
        }
        return false;
    }

    @Override
    public JLabel createUserWidget(String userName, String host, String chatMessage) {
        KenaiUserUI ku = new KenaiUserUI(userName + "@" + host);
        ku.setMessage(chatMessage);
        return ku.createUserWidget();
    }

    @Override
    public org.netbeans.modules.bugtracking.team.spi.OwnerInfo getOwnerInfo(Node node) {
        OwnerInfo ownerInfo = NbModuleOwnerSupport.getInstance().getOwnerInfo(node);
        return ownerInfo != null ? new OwnerInfoImpl(ownerInfo) : null;
    }

    @Override
    public org.netbeans.modules.bugtracking.team.spi.OwnerInfo getOwnerInfo(File file) {
        OwnerInfo ownerInfo = NbModuleOwnerSupport.getInstance().getOwnerInfo(NbModuleOwnerSupport.NB_BUGZILLA_CONFIG, file);
        return ownerInfo != null ? new OwnerInfoImpl(ownerInfo) : null;
    }

    @Override
    public org.netbeans.modules.bugtracking.team.spi.TeamProject[] getDashboardProjects(boolean onlyOpened) {
        ProjectHandle<KenaiProject>[] handles = KenaiUIUtils.getDashboardProjects(onlyOpened);
        if ((handles == null) || (handles.length == 0)) {
            return new TeamProjectImpl[0];
        }

        List<TeamProjectImpl> kenaiProjects = new LinkedList<TeamProjectImpl>();
        for (ProjectHandle<KenaiProject> handle : handles) {
            KenaiProject project = handle.getTeamProject();
            if (project != null) {
                kenaiProjects.add(TeamProjectImpl.getInstance(project));
            } else {
                Support.LOG.log(
                        Level.WARNING,
                        "No Kenai project is available for ProjectHandle" + " [{0}, {1}]", //NOI18N
                        new Object[]{handle.getId(), handle.getDisplayName()}); 
            }
        }
        return kenaiProjects.toArray(new TeamProjectImpl[kenaiProjects.size()]);
    }

    @Override
    public org.netbeans.modules.bugtracking.team.spi.TeamProject getTeamProjectForRepository(String url) throws IOException {
        KenaiProject kp = KenaiProject.forRepository(url);
        return kp != null ? TeamProjectImpl.getInstance(kp) : null;
    }

    @Override
    public org.netbeans.modules.bugtracking.team.spi.TeamProject getTeamProject(String url, String projectName) throws IOException {
        Kenai kenai = getKenai(url);
        if (kenai == null) {
            Support.LOG.log(Level.FINEST, "no kenai for url : [{0}]", url);
            return null;
        }
        KenaiProject kp = kenai.getProject(projectName);
        return kp != null ? TeamProjectImpl.getInstance(kp) : null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener, String kenaiHostUrl) {
        Kenai kenai = getKenai(kenaiHostUrl);
        if(kenai != null) {
            addPropertyChangeListener(listener, kenai);
        } else {
            Support.LOG.log(Level.WARNING, "trying to unregister on a unknown kenai host {0}", kenaiHostUrl);  //NOI18N
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener, String kenaiHostUrl) {
        Kenai kenai = getKenai(kenaiHostUrl);
        if(kenai != null) {
            removePropertyChangeListener(listener, kenai);
        } else {
            Support.LOG.log(Level.WARNING, "trying to unregister on a unknown kenai host {0}", kenaiHostUrl);  //NOI18N
        }
    }

    @Override
    public boolean isOwner (String url) {
        return getKenai(url) != null;
    }
    
    /**
     * Returns true if logged into kenai, otherwise false.
     *
     * @return
     */
    static boolean isLoggedIn(Kenai kenai) {
        return kenai.getPasswordAuthentication() != null;
    }

    static Kenai getKenai(String url) {
        // 1st - full url match
        Kenai kenaiCandidate = KenaiManager.getDefault().getKenai(url);
        if (kenaiCandidate != null) {
            return kenaiCandidate;
        }
        // 2nd - VCS repository url match
        try {
            KenaiProject kp = KenaiProject.forRepository(url);
            if (kp != null) {
                return kp.getKenai();
            }
        } catch (KenaiException ex) {
            Support.LOG.log(Level.FINE, url, ex);
        }
        // 3rd - bugtracking issue url match
        for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
            String kenaiUrl = kenai.getUrl().toString();
            if (url.startsWith(kenaiUrl)) {
                Support.LOG.log(Level.FINE, "getKenai: url {0} matches kenai url {1}", new String[] {url, kenaiUrl}); //NOI18N
                return kenai;
            }
        }
        return null;
    }

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials.
     *
     * @param url a {@link Kenai} instance url
     * @param forceLogin  forces a login if user not logged in
     * @return PasswordAuthentication
     */
    static PasswordAuthentication getPasswordAuthentication(Kenai kenai, boolean forceLogin) {
        PasswordAuthentication a = kenai.getPasswordAuthentication();
        if(a != null) {
            return a;
        }

        if(!forceLogin) {
            return null;
        }

        if(!showLoginIntern()) {
            return null;
        }

        return kenai.getPasswordAuthentication();
    }

    static boolean showLoginIntern() {
        return KenaiUIUtils.showLogin();
    }

    void addPropertyChangeListener(PropertyChangeListener listener, Kenai kenai) {
        getKenaiListener(kenai).add(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener, Kenai kenai) {
        getKenaiListener(kenai).remove(listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized(allKenaiListeners) {
            allKenaiListeners.add(listener);
        }
        // XXX what if new team server created
        for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
            addPropertyChangeListener(listener, kenai);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        synchronized(allKenaiListeners) {
            allKenaiListeners.remove(listener);
        }
        // XXX what if new team server created        
        for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
            removePropertyChangeListener(listener, kenai);
        }
    }

    private class OwnerInfoImpl extends org.netbeans.modules.bugtracking.team.spi.OwnerInfo {
        private final OwnerInfo delegate;

        public OwnerInfoImpl(OwnerInfo delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getOwner() {
            return delegate.getOwner();
        }

        @Override
        public List<String> getExtraData() {
            return delegate.getExtraData();
        }
    }

    private Map<String, DelegateKenaiListener> kenaiListeners;
    private synchronized DelegateKenaiListener getKenaiListener(Kenai kenai) {
        if(kenaiListeners == null) {
            kenaiListeners = new HashMap<String, DelegateKenaiListener>();
        }
        DelegateKenaiListener l = kenaiListeners.get(kenai.getUrl().toString());
        if(l == null) {
            l = new DelegateKenaiListener(kenai);
            kenaiListeners.put(kenai.getUrl().toString(), l);
        }
        return l;
    }

    private class DelegateKenaiListener implements PropertyChangeListener {
        private final Collection<PropertyChangeListener> delegates = new LinkedList<PropertyChangeListener>();
        private final Kenai kenai;
        public DelegateKenaiListener(Kenai kenai) {
            this.kenai = kenai; // XXX do not leak this
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(TeamServer.PROP_LOGIN) || 
               evt.getPropertyName().equals(DashboardSupport.PROP_OPENED_PROJECTS)) {
                
                PropertyChangeListener[] la;
                synchronized (delegates) {
                   la = delegates.toArray(new PropertyChangeListener[delegates.size()]);
                }
                String propName;
                if(TeamServer.PROP_LOGIN.equals(evt.getPropertyName())) {
                    propName = PROP_LOGIN;
                } else if (DashboardSupport.PROP_OPENED_PROJECTS.equals(evt.getPropertyName())) {
                    propName = PROP_PROJETCS_CHANGED;
                } else {
                    throw new IllegalStateException("Unknown event " + evt.getPropertyName()); // NOI18N
                }
                for (PropertyChangeListener l : la) {
                    l.propertyChange(new PropertyChangeEvent(evt.getSource(), propName, evt.getOldValue(), evt.getNewValue()));
                }
            } 
        }
        private synchronized void add(PropertyChangeListener l) {
            delegates.add(l);
            if(delegates.size() == 1) {
                kenai.addPropertyChangeListener(this);
                KenaiUIUtils.addDashboardListener(kenai, this);
            }
        }
        private synchronized void remove(PropertyChangeListener l) {
            delegates.remove(l);
            if(delegates.isEmpty()) {
                kenai.removePropertyChangeListener(this);
                KenaiUIUtils.removeDashboardListener(kenai, this);
            }
        }
    }
}
