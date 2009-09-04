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

package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.spi.MessagingAccessor;
import org.netbeans.modules.kenai.ui.spi.MessagingHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.netbeans.modules.kenai.ui.treelist.LeafNode;
import org.netbeans.modules.kenai.ui.treelist.TreeLabel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * My Project's root node
 *
 * @author Jan Becicka
 */
public class MyProjectNode extends LeafNode {

    private final ProjectHandle project;
    private final ProjectAccessor accessor;
    private final QueryAccessor qaccessor;
    private final MessagingAccessor maccessor;
    private MessagingHandle mh;

    private JPanel component = null;
    private JLabel lbl = null;
    private LinkButton btnBookmark = null;
    private LinkButton btnOpen = null;
    private LinkButton btnMessages = null;
    private LinkButton btnBugs = null;

    private boolean isMemberProject = false;

    private final Object LOCK = new Object();

    private final PropertyChangeListener projectListener;
    private TreeLabel rightPar;
    private TreeLabel leftPar;

    public MyProjectNode( final ProjectHandle project ) {
        super( null );
        if (project==null)
            throw new IllegalArgumentException("project cannot be null"); // NOI18N
        this.projectListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( ProjectHandle.PROP_CONTENT.equals( evt.getPropertyName()) ) {
                    refreshChildren();
                    if( null != lbl )
                        lbl.setText(project.getDisplayName());
                } else if(MessagingHandle.PROP_MESSAGE_COUNT.equals(evt.getPropertyName())) {
                    if (btnMessages!=null) {
                        if (mh.getMessageCount()<0) {
                            setOnline(false);
                        }
                        btnMessages.setText(mh.getMessageCount()+"");
                    }
                } else if (Kenai.PROP_XMPP_LOGIN.equals(evt.getPropertyName())) {
                    if (evt.getOldValue()==null) {
                        if (mh.getMessageCount()>=0)
                            setOnline(true);
                    } else if (evt.getNewValue()==null) {
                        setOnline(false);
                        mh.removePropertyChangeListener(projectListener);
                        mh=maccessor.getMessaging(project);
                        mh.addPropertyChangeListener(projectListener);
                    }
                } else if (QueryHandle.PROP_QUERY_RESULT.equals(evt.getPropertyName())) {
                    List<QueryResultHandle> queryResults = (List<QueryResultHandle>) evt.getNewValue();
                    if (!queryResults.isEmpty()) {
                        setBugsLater(queryResults.get(queryResults.size()>2?2:0));
                        return;
                    }
                }
            }
        };
        this.project = project;
        this.project.addPropertyChangeListener( projectListener );
        this.accessor = ProjectAccessor.getDefault();
        this.maccessor = MessagingAccessor.getDefault();
        this.qaccessor = QueryAccessor.getDefault();
        this.mh = maccessor.getMessaging(project);
        this.mh.addPropertyChangeListener(projectListener);
        Kenai.getDefault().addPropertyChangeListener(projectListener);
    }

    ProjectHandle getProject() {
        return project;
    }

    ProjectAccessor getAccessor() {
        return accessor;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        synchronized( LOCK ) {
            if( null == component ) {
                component = new JPanel( new GridBagLayout() );
                component.setOpaque(false);
                lbl = new TreeLabel(project.getDisplayName());
                component.add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,16,0,3), 0,0) );

                MessagingHandle messaging = maccessor.getMessaging(project);
                int count = messaging.getMessageCount();

                leftPar = new TreeLabel("(");
                rightPar = new TreeLabel(")");
                component.add(leftPar, new GridBagConstraints(1, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                btnMessages = new LinkButton(count + "", ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/newmessage.png", true), maccessor.getOpenMessagesAction(project));
                btnMessages.setHorizontalTextPosition(JLabel.LEFT);
                component.add(btnMessages, new GridBagConstraints(2, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                component.add(rightPar, new GridBagConstraints(4, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                setOnline(messaging.getOnlineCount() >= 0);
                
                post(new Runnable() {

                    public void run() {
                        List<QueryHandle> h = qaccessor.getQueries(project);
                        if (!h.isEmpty()) {
                            QueryHandle handle = h.get(h.size()>1?1:0);
                            handle.addPropertyChangeListener(projectListener);
                            List<QueryResultHandle> queryResults = qaccessor.getQueryResults(handle);
                            if (!queryResults.isEmpty()) {
                                setBugsLater(queryResults.get(queryResults.size()>2?2:0));
                                return;
                            }
                        }
                    }
                });


                component.add( new JLabel(), new GridBagConstraints(5,0,1,1,1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0) );
                btnBookmark = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bookmark.png", true), accessor.getBookmarkAction(project)); //NOI18N
                btnBookmark.setToolTipText(NbBundle.getMessage(MyProjectNode.class, "LBL_LeaveProject"));
                btnBookmark.setRolloverEnabled(true);
                btnBookmark.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bookmark_over.png", true));
                component.add( btnBookmark, new GridBagConstraints(6,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                btnOpen = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/open.png", true), getOpenAction()); //NOI18N
                btnOpen.setToolTipText(NbBundle.getMessage(MyProjectNode.class, "LBL_Open"));
                btnOpen.setRolloverEnabled(true);
                btnOpen.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/open_over.png", true));
                component.add( btnOpen, new GridBagConstraints(7,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
            }
            lbl.setForeground(foreground);
            return component;
        }
    }

    @Override
    public Action getDefaultAction() {
        return accessor.getDefaultAction(project);
    }

    private void setOnline(final boolean b) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (btnBugs == null) {
                    if (leftPar != null) {
                        leftPar.setVisible(b);
                    }
                    if (rightPar != null) {
                        rightPar.setVisible(b);
                    }
                }
                if (btnMessages != null) {
                    btnMessages.setVisible(b);
                }
                DashboardImpl.getInstance().dashboardComponent.repaint();
            }
        });
    }

    private Action getOpenAction() {
        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                DashboardImpl.getInstance().addProject(project, isMemberProject);
            }
        };
    }

    @Override
    public Action[] getPopupActions() {
        return accessor.getPopupActions(project);
    }

    void setMemberProject(boolean isMemberProject) {
        if( isMemberProject == this.isMemberProject )
            return;
        this.isMemberProject = isMemberProject;
        fireContentChanged();
        refreshChildren();
    }

    private static RequestProcessor rp;;

    private static synchronized void post(Runnable run) {
        if (rp == null) {
            rp = new RequestProcessor();
        }
        rp.post(run);
    }

    @Override
    protected void dispose() {
        super.dispose();
        if( null != project )
            project.removePropertyChangeListener( projectListener );
        if (null != mh) {
            mh.removePropertyChangeListener(projectListener);
        }
        Kenai.getDefault().removePropertyChangeListener(projectListener);
        synchronized(MyProjectNode.class) {
            rp=null;
        }
    }

    private void setBugsLater(final QueryResultHandle bug) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (btnBugs!=null) {
                    component.remove(btnBugs);
                }
                btnBugs = new LinkButton(bug.getText(), ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bug.png", true), qaccessor.getOpenQueryResultAction(bug));
                btnBugs.setHorizontalTextPosition(JLabel.LEFT);
                component.add( btnBugs, new GridBagConstraints(3,0,1,1,0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,5,0,0), 0,0) );
                leftPar.setVisible(true);
                rightPar.setVisible(true);
                component.validate();
            }
        });
    }
}
