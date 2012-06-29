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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.ui.Utilities;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import static org.netbeans.modules.kenai.ui.impl.Bundle.*;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
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

    public TeamServerProviderImpl () {
        KenaiManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (Kenai.PROP_LOGIN.equals(pce.getPropertyName())) {
                    final Preferences preferences = NbPreferences.forModule(TeamServerProviderImpl.class);
                    preferences.put(UIUtils.getPrefName((Kenai) pce.getSource(), LoginUtils.LOGIN_STATUS_PREF), Boolean.toString(pce.getNewValue() != null));
                } else if (Kenai.PROP_XMPP_LOGIN.equals(pce.getPropertyName())) {
                    final Preferences preferences = NbPreferences.forModule(TeamServerProviderImpl.class);
                    preferences.put(UIUtils.getPrefName((Kenai) pce.getSource(), LoginUtils.ONLINE_STATUS_PREF), Boolean.toString(pce.getNewValue() != null));
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
                    UIUtils.tryLogin(k, false);
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            if (prefs.keys().length > 0) {
                RequestProcessor.getDefault().post(new Runnable() {
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
    
}
