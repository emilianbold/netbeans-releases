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

package org.netbeans.modules.team.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.openide.util.Lookup;

/**
 * Manager of Team server instances
 * @author Jan Becicka
 */
public final class TeamServerManager {

    private static TeamServerManager instance;
    public static final String PROP_INSTANCES = "prop_instances"; // NOI18N
    private final Collection<TeamServerProvider> providers;
    private final PropertyChangeListener list;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * singleton instance
     * @return
     */
    public static synchronized TeamServerManager getDefault() {
        if (instance==null) {
            instance = new TeamServerManager();
        }
        return instance;
    }

    private TeamServerManager() {
        providers = new ArrayList<TeamServerProvider>(Lookup.getDefault().lookupAll(TeamServerProvider.class));
        list = new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (TeamServerProvider.PROP_INSTANCES.equals(evt.getPropertyName())) {
                    propertyChangeSupport.firePropertyChange(PROP_INSTANCES, evt.getOldValue(), evt.getNewValue());
                }
            }
        };
        reattachListeners();
    }
    
    public Collection<TeamServerProvider> getProviders() {
        return new ArrayList(providers);
    }

    /**
     * remove Team server instance from manager
     * @param instance
     */
    public void removeTeamServer (TeamServer instance) {
        // delegate to the actual manager
        for (TeamServerProvider p : providers) {
            p.removeTeamServer(instance);
        }
    }

    /**
     * returns all Team server instances registered in this manager
     * @return
     */
    public Collection<TeamServer> getTeamServers() {
        // delegate to the actual manager
        List<TeamServer> teams = new LinkedList<TeamServer>();
        for (TeamServerProvider p : providers) {
            teams.addAll(p.getTeamServers());
        }
        return new ArrayList(teams);
    }

    /**
     * get Team server instance for specified url
     * @param url
     * @return
     */
    public TeamServer getTeamServer (String url) {
        TeamServer team = null;
        // delegate to the actual manager
        for (TeamServerProvider m : providers) {
            team = m.getTeamServer(url);
            if (team != null) {
                break;
            }
        }
        return team;
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    void firePropertyChange (PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
    }

    private void reattachListeners () {
        for (TeamServerProvider p : providers) {
            p.removePropertyListener(list);
            p.addPropertyListener(list);
        }
    }

}