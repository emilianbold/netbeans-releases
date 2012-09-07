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
package org.netbeans.modules.ods.ui;

import org.netbeans.modules.ods.ui.api.CloudUiServer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.ods.api.CloudServer;
import org.netbeans.modules.ods.api.CloudServerManager;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import static org.netbeans.modules.ods.ui.Bundle.*;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Ondrej Vrabec
 */
@ServiceProviders({
    @ServiceProvider(service=CloudServerProviderImpl.class),
    @ServiceProvider(service=TeamServerProvider.class)
})
public class CloudServerProviderImpl implements TeamServerProvider {

    private static CloudServerProviderImpl instance;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean initialized;

    public CloudServerProviderImpl () {
        CloudServerManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent pce) {
                if (CloudServerManager.PROP_INSTANCES.equals(pce.getPropertyName())) {
                    CloudUiServer oldValue = null, newValue = null;
                    if (pce.getOldValue() instanceof CloudServer) {
                        oldValue = CloudUiServer.forServer(((CloudServer) pce.getOldValue()));
                    }
                    if (pce.getNewValue() instanceof CloudServer) {
                        newValue = CloudUiServer.forServer(((CloudServer) pce.getNewValue()));
                    }
                    propertyChangeSupport.firePropertyChange(TeamServerProvider.PROP_INSTANCES, oldValue, newValue);
                }
            }
        });
    }
    
    public static synchronized TeamServerProvider getDefault () {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(CloudServerProviderImpl.class);
        }
        return instance;
    }

    @Override
    public Collection<? extends TeamServer> getTeamServers () {
        Collection<CloudServer> servers = CloudServerManager.getDefault().getServers();
        List<CloudUiServer> uiServers = new ArrayList<CloudUiServer>(servers.size());
        for (CloudServer server : servers) {
            uiServers.add(CloudUiServer.forServer(server));
        }
        return uiServers;
    }

    @Override
    public TeamServer getTeamServer (String url) {
        CloudServer server = CloudServerManager.getDefault().getServer(url);
        return server == null ? null : CloudUiServer.forServer(server);
    }

    @Override
    public void removeTeamServer (TeamServer instance) {
        if (instance instanceof CloudUiServer) {
            CloudServer server = ((CloudUiServer) instance).getServer();
            if (server != null) {
                CloudServerManager.getDefault().removeServer(server);
            }
        }
    }

    @Override
    @Messages("LBL_ProviderName=Oracle Developer Cloud Service")
    public String getDisplayName () {
        return LBL_ProviderName();
    }

    @Override
    @Messages("LBL_ProviderDescription=Supports team servers built on top of the Oracle Developer Cloud Service technology.")
    public String getDescription () {
        return LBL_ProviderDescription();
    }

    @Override
    public TeamServer createTeamServer (String name, String url) throws MalformedURLException {
        CloudServer server = CloudServerManager.getDefault().createServer(name, url);
        if (server != null) {
            //TODO how to check the url is valid??
        }
        return server == null ? null : CloudUiServer.forServer(server);
    }

    @Override
    public void initialize () {
        if (initialized) {
            return;
        }
        initialized = true;
        Preferences prefs = NbPreferences.forModule(CloudServerProviderImpl.class);
        try {
            if (prefs.keys().length > 0) {
                for (CloudServer k : CloudServerManager.getDefault().getServers()) {
                    Utilities.login(k);
                }
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
        "ERR_NotHttp=Only http and https are supported protocols"})
    public String validate (String s) {
        if(Boolean.getBoolean("team.c2c.mockClient")) {
            // for mock accept whatever is provided
            return null;
        }
        if (!(s.startsWith("https://") || s.startsWith("http://"))) { //NOI18N
            return Bundle.ERR_NotHttp();
        }

        if (s.equals("http://") || s.equals("https://")) { //NOI18N
            return Bundle.ERR_UrlNotValid();
        }
        return null;
    }
    
}
