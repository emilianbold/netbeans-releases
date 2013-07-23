/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.server.ui.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.modules.team.server.Utilities;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.commons.treelist.SelectionList;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Stupka
 * @param <P> the team project type
 */
@NbBundle.Messages("A11Y_TeamProjects=Team Projects")
public final class DashboardSupport<P> {
    
    /**
     * Name of the property that will be fired when some change in opened projects
     * in Dashboard occurs. Firing this property doesn't necessary mean that number
     * of opened project has changed.
     */
    public static final String PROP_OPENED_PROJECTS = "openedProjects"; // NOI18N

    /**
     * fired when user clicks refresh. Source will be this DashboardSupport instance.
     */
    public static final String PROP_REFRESH_REQUEST = "refreshRequest"; // NOI18N
    
    public static final String PROP_BTN_NOT_CLOSING_MEGA_MENU = "MM.NotClosing"; // NOI18N
    
    public static final String PREF_ALL_PROJECTS = "allProjects"; //NOI18N
    public static final String PREF_COUNT = "count"; //NOI18N
    public static final String PREF_ID = "id"; //NOI18N
    
    private final DashboardImpl<P> impl;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PropertyChangeListener implListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            support.firePropertyChange(evt.getPropertyName(), evt.getNewValue(), evt.getOldValue());
        }
    };
    
    public DashboardSupport(TeamServer server, DashboardProvider<P> dashboardProvider) {
         this.impl = Utilities.isMoreProjectsDashboard() ? 
                 new DefaultDashboard<>(server, dashboardProvider) :
                 OneProjectDashboard.create(server, dashboardProvider);
        
         impl.addPropertyChangeListener(WeakListeners.propertyChange(implListener, impl));
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChange) {
        support.addPropertyChangeListener(propertyChange);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }
    
    public void addProjects(ProjectHandle<P>[] pHandle, final boolean isMemberProject, final boolean select) {
        impl.addProjects(pHandle, isMemberProject, select);
    }
    
    public void bookmarkingFinished(ProjectHandle<P> project) {
        impl.bookmarkingFinished(project);
    }

    public void bookmarkingStarted(ProjectHandle<P> project) {
        impl.bookmarkingStarted(project);
    }

    public void deletingFinished() {
        impl.deletingFinished();
    }

    public void deletingStarted() {
        impl.deletingStarted();
    }

    public JComponent getComponent() {
        return impl.getComponent();
    }

    public DashboardProvider<P> getDashboardProvider() {
        return impl.getDashboardProvider();
    }

    public ProjectHandle<P>[] getProjects(boolean onlyOpened) {
        return impl.getProjects(onlyOpened);
    }

    public TeamServer getServer() {
        return impl.getServer();
    }

    public void myProjectsProgressFinished() {
        impl.myProjectsProgressFinished();
    }

    public void myProjectsProgressStarted() {
        impl.myProjectsProgressStarted();
    }

    public void refreshMemberProjects(boolean forceRefresh) {
        impl.refreshMemberProjects(forceRefresh);
    }

    public void removeProject(ProjectHandle<P> project) {
        impl.removeProject(project);
    }

    public void selectAndExpand(ProjectHandle<P> project) {
        impl.selectAndExpand(project);
    }

    public void xmppFinsihed() {
        impl.xmppFinsihed();
    }

    public void xmppStarted() {
        impl.xmppStarted();
    }
    
    public SelectionList getProjectsList( boolean forceRefresh ) {
        return impl.getProjectsList( forceRefresh );
    }
    
    public interface DashboardImpl<P> {

        void addProjects(ProjectHandle<P>[] pHandle, final boolean isMemberProject, final boolean select);

        void addPropertyChangeListener(PropertyChangeListener propertyChange);

        void bookmarkingFinished(ProjectHandle<P> project);

        void bookmarkingStarted(ProjectHandle<P> project);

        void deletingFinished();

        void deletingStarted();

        JComponent getComponent();

        DashboardProvider<P> getDashboardProvider();

        ProjectHandle<P>[] getProjects(boolean onlyOpened);

        TeamServer getServer();

        void myProjectsProgressFinished();

        void myProjectsProgressStarted();

        void refreshMemberProjects(boolean force);

        void removeProject(ProjectHandle<P> project);

        void removePropertyChangeListener(PropertyChangeListener propertyChangeListener);

        void selectAndExpand(ProjectHandle<P> project);

        void xmppFinsihed();

        void xmppStarted();

        SelectionList getProjectsList( boolean forceRefresh );
        
    }
}
