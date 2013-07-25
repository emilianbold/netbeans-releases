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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Becicka
 */
class TeamServerComboModel extends AbstractListModel implements ComboBoxModel {

    private Object selected = (getElementAt(0) instanceof TeamServer)?getElementAt(0):null;
    private List<TeamServer.Status> statuses;
    private int addNew = 1;

    private PropertyChangeListener listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (selected == evt.getOldValue()) {
                selected = Utilities.getPreferredServer();
            }
            fireContentsChanged(evt.getSource(), 0, getSize());
        }
    };
    private TeamServerProvider provider;

    public TeamServerComboModel(TeamServer.Status... statuses) {
        this(true, statuses);
    }

    public TeamServerComboModel(boolean addNew, TeamServer.Status... statuses) {
        this();
        this.statuses = Arrays.asList(statuses);
        this.addNew = addNew?1:0;
    }

    public TeamServerComboModel (TeamServerProvider provider) {
        this();
        this.provider = provider;
        selected = (getElementAt(0) instanceof TeamServer)?getElementAt(0):null;
    }

    public TeamServerComboModel() {
        TeamServerManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(listener, TeamServerManager.getDefault()));
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public Object getElementAt(int index) {
        int i = 0;
        for (TeamServer k: provider == null ? TeamServerManager.getDefault().getTeamServers() : provider.getTeamServers()) {
            if (statuses==null || statuses.contains(k.getStatus())) {
                i++;
            }
            if (i -1 == index) {
                return k;
            }
        }
        return NbBundle.getMessage(TeamServerComboModel.class, "CTL_AddNew");
    }

    @Override
    public int getSize() {
        Collection<? extends TeamServer> teamServers = provider == null ? TeamServerManager.getDefault().getTeamServers() : provider.getTeamServers();
        if (statuses==null) {
            return teamServers.size() + addNew;
        }

        int i=0;
        for (TeamServer k: teamServers) {
            if (statuses.contains(k.getStatus())) {
                i++;
            }
        }
        return i + addNew;
    }
}
