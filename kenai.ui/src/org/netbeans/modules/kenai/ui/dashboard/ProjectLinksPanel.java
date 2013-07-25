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
package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.MessagingAccessorImpl;
import org.netbeans.modules.kenai.ui.ProjectAccessorImpl;
import org.netbeans.modules.team.server.ui.common.LinkButton;
import org.netbeans.modules.team.server.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.server.ui.spi.MessagingHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.commons.treelist.AsynchronousNode;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class ProjectLinksPanel extends JPanel implements PropertyChangeListener {

    private final List<JLabel> labels = new ArrayList<JLabel>(5);
    private final List<LinkButton> buttons = new ArrayList<LinkButton>(3);
    private final ProjectHandle<KenaiProject> project;
    private MessagingHandle messaging;
    private final ProjectLinksNode parentNode;
    
    public ProjectLinksPanel(ProjectHandle<KenaiProject> project, ProjectLinksNode parentNode) {
        this.project = project;
        this.parentNode = parentNode;

        messaging = MessagingAccessorImpl.getDefault().getMessaging(project);
        
        initComponent( project);
        
        messaging.addPropertyChangeListener(this);
        project.getTeamProject().getKenai().addPropertyChangeListener(Kenai.PROP_XMPP_LOGIN, this);        
    }
    
    void configure(JComponent component, Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        if( this == component ) {
            for( JLabel lbl : labels ) {
                lbl.setForeground(foreground);
            }
            for( LinkButton lb : buttons ) {
                lb.setForeground(foreground, isSelected);
            }
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Kenai.PROP_XMPP_LOGIN.equals(evt.getPropertyName())) {
            if (evt.getOldValue() == null) {
                refresh();
            } else if (evt.getNewValue() == null) {
                messaging.removePropertyChangeListener(this);
                messaging = MessagingAccessorImpl.getDefault().getMessaging(project);
                messaging.addPropertyChangeListener(this);
                refresh();
            }
        } else {
            refresh();
        }
    }    

    private void refresh() {
        if(parentNode != null) {
            parentNode.refreshNode();
        } else {
            initComponent(project);
        }
    }
    
    void dispose() {
        if( null != messaging ) {
            messaging.removePropertyChangeListener(this);
        }
        project.getTeamProject().getKenai().removePropertyChangeListener(this);
    }

    private void initComponent(ProjectHandle project) throws MissingResourceException {
        MessagingAccessor accessor = MessagingAccessorImpl.getDefault();
        
        removeAll();
        
        setLayout(new GridBagLayout());
        setOpaque(false);

        labels.clear();
        buttons.clear();
        JLabel lbl = null;
        LinkButton btn = null;
        int onlineCount = messaging.getOnlineCount();
        if( onlineCount >= 0 ) {
            btn = new LinkButton(messaging.getMessageCount()+"",ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/newmessage.png", true), accessor.getOpenMessagesAction(project)); //NOI18N
            btn.setHorizontalTextPosition(JLabel.LEFT);
            buttons.add( btn );
            add( btn, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));

            lbl = new TreeLabel("|"); //NOI18N
            labels.add(lbl);
            add( lbl, new GridBagConstraints(4,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
        } else if (onlineCount == -2) {
            btn = new LinkButton(NbBundle.getMessage(ProjectLinksNode.class, "LBL_CreateChat", messaging.getMessageCount()), accessor.getCreateChatAction(project)); //NOI18N
            buttons.add( btn );
            add( btn, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));

            lbl = new TreeLabel("|"); //NOI18N
            labels.add(lbl);
            add( lbl, new GridBagConstraints(4,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
        } else if (onlineCount == -3) {
            lbl = new JLabel(NbBundle.getMessage(ProjectLinksNode.class, "LBL_ConnectionFailed")); //NOI18N
            lbl.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/ui/resources/error.png"))); //NOI18N
            labels.add(lbl);
            add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));

            lbl = new JLabel("("); //NOI18N
            labels.add(lbl);
            add( lbl, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));

            btn = new LinkButton(NbBundle.getMessage(ProjectLinksNode.class, "LBL_Retry"), accessor.getReconnectAction(project)); //NOI18N
            buttons.add( btn );
            add( btn, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));

            lbl = new JLabel(")"); //NOI18N
            labels.add(lbl);
            add( lbl, new GridBagConstraints(3,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));

            lbl = new JLabel("|"); //NOI18N
            labels.add(lbl);
            add( lbl, new GridBagConstraints(4,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));
        }

        btn = new LinkButton(NbBundle.getMessage(ProjectLinksNode.class, "LBL_ProjectDetails"), ProjectAccessorImpl.getDefault().getDetailsAction(project)); //NOI18N
        buttons.add( btn );
        add( btn, new GridBagConstraints(5,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0,0));

        add( new JLabel(), new GridBagConstraints(8,0,1,1,1.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,0));
        
        revalidate();
    }
}
