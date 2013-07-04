/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui.impl;

import java.awt.EventQueue;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.KenaiPopupActionsProvider;
import org.netbeans.modules.kenai.ui.NewKenaiProjectAction;
import org.netbeans.modules.kenai.ui.NewKenaiProjectWizardIterator;
import org.netbeans.modules.kenai.ui.ProjectHandleImpl;
import org.netbeans.modules.kenai.ui.Utilities;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import static org.netbeans.modules.kenai.ui.impl.Bundle.*;
import org.netbeans.modules.kenai.ui.api.KenaiUIUtils;
import org.netbeans.modules.subversion.api.Subversion;
import org.netbeans.modules.team.ui.spi.PopupMenuProvider;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Ondrej Vrabec
 */
@ServiceProviders({
    @ServiceProvider(service=TeamServerProviderImpl.class),
    @ServiceProvider(service=TeamServerProvider.class)
})
public class TeamServerProviderImpl implements TeamServerProvider {
    private static TeamServerProviderImpl instance;
    private boolean initialized;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private static final Pattern urlPatten = Pattern.compile("https://([a-zA-Z0-9\\-\\.])+\\.(([a-zA-Z]{2,3})|(info)|(name)|(aero)|(coop)|(museum)|(jobs)|(mobi)|(travel))/?$"); //NOI18N

    public TeamServerProviderImpl () {
        KenaiManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (TeamServer.PROP_LOGIN.equals(pce.getPropertyName())) {
                    final Preferences preferences = NbPreferences.forModule(TeamServerProviderImpl.class);
                    preferences.put(KenaiUIUtils.getPrefName((Kenai) pce.getSource(), LoginUtils.LOGIN_STATUS_PREF), Boolean.toString(pce.getNewValue() != null));
                } else if (Kenai.PROP_XMPP_LOGIN.equals(pce.getPropertyName())) {
                    final Preferences preferences = NbPreferences.forModule(TeamServerProviderImpl.class);
                    preferences.put(KenaiUIUtils.getPrefName((Kenai) pce.getSource(), LoginUtils.ONLINE_STATUS_PREF), Boolean.toString(pce.getNewValue() != null));
                } else if (KenaiManager.PROP_INSTANCES.equals(pce.getPropertyName())) {
                    KenaiServer oldValue = null, newValue = null;
                    if (pce.getOldValue() instanceof Kenai) {
                        oldValue = KenaiServer.forKenai(((Kenai) pce.getOldValue()));
                    }
                    if (pce.getNewValue() instanceof Kenai) {
                        newValue = KenaiServer.forKenai(((Kenai) pce.getNewValue()));
                    }
                    propertyChangeSupport.firePropertyChange(TeamServerProvider.PROP_INSTANCES, oldValue, newValue);
                }
            }
        });
    }
    
    public static synchronized TeamServerProvider getDefault () {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(TeamServerProviderImpl.class);
        }
        return instance;
    }

    @Override
    public Collection<? extends TeamServer> getTeamServers () {
        Collection<Kenai> kenais = KenaiManager.getDefault().getKenais();
        List<KenaiServer> servers = new ArrayList<KenaiServer>(kenais.size());
        for (Kenai kenai : kenais) {
            servers.add(KenaiServer.forKenai(kenai));
        }
        return servers;
    }

    @Override
    public TeamServer getTeamServer (String url) {
        Kenai kenai = KenaiManager.getDefault().getKenai(url);
        return kenai == null ? null : KenaiServer.forKenai(kenai);
    }

    @Override
    public void removeTeamServer (TeamServer instance) {
        if (instance instanceof KenaiServer) {
            KenaiManager.getDefault().removeKenai(((KenaiServer) instance).getKenai());
        }
    }

    @Override
    @Messages("LBL_KenaiProviderName=Kenai")
    public String getDisplayName () {
        return LBL_KenaiProviderName();
    }

    @Override
    @Messages("LBL_KenaiProviderDescription=Supports team servers built on top of the Kenai infrastructure, such as java.net or netbeans.org")
    public String getDescription () {
        return LBL_KenaiProviderDescription();
    }

    @Override
    public TeamServer createTeamServer (String name, String url) throws MalformedURLException {
        Kenai kenai = KenaiManager.getDefault().createKenai(name, url);
        if (kenai != null) {
            try {
                kenai.getServices();
            } catch (KenaiException ex) {
                Logger.getLogger(TeamServerProviderImpl.class.getName()).log(Level.FINE, null, ex);
                KenaiManager.getDefault().removeKenai(kenai);
                kenai = null;
            }
        }
        return kenai == null ? null : KenaiServer.forKenai(kenai);
    }

    @Override
    public void initialize () {
        if (initialized) {
            return;
        }
        initialized = true;
        Preferences prefs = NbPreferences.forModule(TeamServerProviderImpl.class);
        try {
            if (prefs.keys().length > 0) {
                for (Kenai k : KenaiManager.getDefault().getKenais()) {
                    KenaiUIUtils.tryLogin(k, false);
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            if (prefs.keys().length > 0) {
                Utilities.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run () {
                        for (Kenai k : KenaiManager.getDefault().getKenais()) {
                            Utilities.isChatSupported(k);
                        }
                    }
                });
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void addPropertyListener (PropertyChangeListener list) {
        propertyChangeSupport.addPropertyChangeListener(list);
    }

    @Override
    public void removePropertyListener (PropertyChangeListener list) {
        propertyChangeSupport.removePropertyChangeListener(list);
    }
    
    @Override
    @Messages({"ERR_UrlNotValid=This url does not seem to be valid",
        "ERR_NotHttps=The only supported protocol is https"})
    public String validate (String s) {
        if (!s.startsWith("https://")) { //NOI18N
            return ERR_NotHttps();
        }

        if (s.equals("https://") || !urlPatten.matcher(s).matches()) { //NOI18N
            return ERR_UrlNotValid();
        }
        return null;
    }

    @Override
    public PopupMenuProvider getPopupMenuProvider (String repositoryUrl) {
        assert !EventQueue.isDispatchThread();
        if (KenaiProject.getNameForRepository(repositoryUrl) != null) {
            return KenaiPopupActionsProvider.getDefault();
        } else {
            return null;
        }
    }

    @Override
    public boolean supportNewTeamProjectCreation() {
        return true;
    }

    @Override
    public void createNewTeamProject(File[] projectDirs) {
        if (Subversion.isClientAvailable(true)) {
            TeamServer teamServer = TeamUIUtils.getSelectedServer();
            WizardDescriptor wizardDescriptor = new WizardDescriptor(new NewKenaiProjectWizardIterator(projectDirs,
                    teamServer instanceof KenaiServer ? ((KenaiServer) teamServer).getKenai() : Utilities.getPreferredKenai()));
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
            wizardDescriptor.setTitle(NbBundle.getMessage(NewKenaiProjectAction.class,
                    "NewKenaiProjectAction.dialogTitle")); // NOI18N

            DialogDisplayer.getDefault().notify(wizardDescriptor);

            boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
            if (!cancelled) {
                Set<NewKenaiProjectWizardIterator.CreatedProjectInfo> createdProjects = wizardDescriptor.getInstantiatedObjects();
                TeamUIUtils.activateTeamDashboard();
                ProjectHandleImpl project = new ProjectHandleImpl(createdProjects.iterator().next().project);
                KenaiServer.getDashboard(project).selectAndExpand(project);
            }
        }
    }
}
