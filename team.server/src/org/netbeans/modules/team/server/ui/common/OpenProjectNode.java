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

package org.netbeans.modules.team.server.ui.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.team.server.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Project's root node
 *
 * @author S. Aubrecht
 * @param <P>
 */
class OpenProjectNode<P> extends TreeListNode {

    private final ProjectHandle<P> project;
    private final ProjectAccessor<P> accessor;

    private JPanel component = null;
    private JLabel lbl = null;
    private LinkButton btnBookmark = null;
    private JLabel myPrjLabel;
    private LinkButton btnClose = null;

    private boolean isMemberProject = false;

    private final Font regFont;
    private final Font boldFont;

    private final Object LOCK = new Object();

    private final PropertyChangeListener projectListener;
    private final DefaultDashboard<P> dashboard;

    /**
     *
     * @param project
     * @param dashboard
     */
    public OpenProjectNode( final ProjectHandle<P> project, final DefaultDashboard<P> dashboard ) {
        super( true, null );
        if (project==null) {
            throw new IllegalArgumentException("project cannot be null"); // NOI18N
        }
        this.dashboard = dashboard;
        this.projectListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( ProjectHandle.PROP_CONTENT.equals( evt.getPropertyName()) ) {
                    refreshChildren();
                    if (evt.getNewValue() != null || evt.getOldValue() !=null) {
                        Collection<ProjectHandle<P>> myProjects = dashboard.getMyProjects();
                        boolean m = myProjects != null ? myProjects.contains(project) : false;
                        if (m != isMemberProject) {
                            dashboard.refreshMemberProjects(false);
                        }
                        isMemberProject = m;
                    }
                    if( null != lbl ) {
                        lbl.setText(project.getDisplayName());
                        lbl.setFont( isMemberProject ? boldFont : regFont );                    
                    }
                    if (null != btnBookmark) {
                        btnBookmark.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/" + (isMemberProject?"bookmark.png":"unbookmark.png"), true)); // NOI18N
                    }
                }
            }
        };
        this.project = project;
        this.project.addPropertyChangeListener( projectListener );
        this.accessor = dashboard.getDashboardProvider().getProjectAccessor();
        regFont = new TreeLabel().getFont();
        boldFont = regFont.deriveFont(Font.BOLD);
    }

    public ProjectHandle<P> getProject() {
        return project;
    }

    ProjectAccessor<P> getAccessor() {
        return accessor;
    }

    @Override
    protected List<TreeListNode> createChildren() {
        ArrayList<TreeListNode> children = new ArrayList<>();
        DashboardProvider<P> provider = dashboard.getDashboardProvider();
        children.add( provider.createProjectLinksNode(this, project) ); 
        if( null != provider.getMemberAccessor() ) {
            children.add( new MemberListNode(this, project, provider) );
        }
        BuilderAccessor builds = provider.getBuilderAccessor();
        if (builds != null) {
            children.add(new BuildListNode(this, project, builds));
        }
        if( null != provider.getQueryAccessor() ) {
            children.add( new QueryListNode(this, project, provider) );
        }
        if( null != provider.getSourceAccessor() ) {
            children.add( provider.createSourceListNode(this, project) );
        }
        return children;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
        synchronized( LOCK ) {
            if( null == component ) {
                component = new JPanel( new GridBagLayout() );
                component.setOpaque(false);
                lbl = new TreeLabel(project.getDisplayName());
                component.add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,3), 0,0) );

                component.add( new JLabel(), new GridBagConstraints(2,0,1,1,1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0) );
                AbstractAction ba = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        accessor.bookmark(project);
                    }
                };
                btnBookmark = new LinkButton(ImageUtilities.loadImageIcon(
                        "org/netbeans/modules/team/server/resources/" + (isMemberProject?"bookmark.png":"unbookmark.png"), true), ba); //NOI18N
                btnBookmark.setRolloverEnabled(true);
                component.add( btnBookmark, new GridBagConstraints(3,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                myPrjLabel = new JLabel();
                component.add( myPrjLabel, new GridBagConstraints(3,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                btnClose = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/close.png", true), new RemoveProjectAction(project)); //NOI18N
                btnClose.setToolTipText(NbBundle.getMessage(OpenProjectNode.class, "LBL_Close"));
                btnClose.setRolloverEnabled(true);
                btnClose.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/close_over.png", true)); // NOI18N
                component.add( btnClose, new GridBagConstraints(4,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
            }
            lbl.setForeground(foreground);
            lbl.setFont( isMemberProject ? boldFont : regFont );
            btnBookmark.setForeground(foreground, isSelected);
            btnBookmark.setIcon(ImageUtilities.loadImageIcon(
                        "org/netbeans/modules/team/server/resources/" + (isMemberProject?"bookmark.png":"unbookmark.png"), true)); // NOI18N
            btnBookmark.setRolloverIcon(ImageUtilities.loadImageIcon(
                        "org/netbeans/modules/team/server/resources/" + (isMemberProject?"bookmark_over.png":"unbookmark_over.png"), true)); // NOI18N
            btnBookmark.setToolTipText(NbBundle.getMessage(OpenProjectNode.class, isMemberProject?"LBL_LeaveProject":"LBL_Bookmark"));
            if (isMemberProject) {
                myPrjLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/team/server/resources/bookmark.png", true)); // NOI18N
                myPrjLabel.setToolTipText(NbBundle.getMessage(OpenProjectNode.class, "LBL_MyProject_Tooltip")); // NOI18N
            } else {
                myPrjLabel.setIcon(null);
                myPrjLabel.setToolTipText(null);
            }
            btnClose.setForeground(foreground, isSelected);
            return component;
        }
    }

    @Override
    public Action getDefaultAction() {
        return accessor.getDefaultAction(project, true);
    }

    @Override
    public Action[] getPopupActions() {
        return accessor.getPopupActions(project, true);
    }

    public void setMemberProject(boolean isMemberProject) {
        if( isMemberProject == this.isMemberProject )
            return;
        this.isMemberProject = isMemberProject;
        fireContentChanged();
        refreshChildren();
    }

    @Override
    protected void dispose() {
        super.dispose();
        if( null != project )
            project.removePropertyChangeListener( projectListener );
    }
    
    private class RemoveProjectAction extends AbstractAction {

        private final ProjectHandle prj;
        public RemoveProjectAction(ProjectHandle project) {
            super(org.openide.util.NbBundle.getMessage(OpenProjectNode.class, "CTL_RemoveProject"));
            this.prj=project;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            dashboard.removeProject(prj);
        }
    }    
}
