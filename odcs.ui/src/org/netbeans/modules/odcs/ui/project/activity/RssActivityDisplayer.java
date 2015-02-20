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
package org.netbeans.modules.odcs.ui.project.activity;

import com.tasktop.c2c.server.profile.domain.activity.RssActivity;
import com.tasktop.c2c.server.tasks.domain.Comment;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class RssActivityDisplayer extends ActivityDisplayer {

    private final RssActivity activity;

    public RssActivityDisplayer(RssActivity activity, int maxWidth) {
        super(activity.getActivityDate(), maxWidth);
        this.activity = activity;
    }

    @Override    
    public JComponent getShortDescriptionComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = GridBagConstraints.REMAINDER;

        gbc.insets = new Insets(0, 5, 0, 0);
        LinkLabel linkPage = new LinkLabel(activity.getActivity().getTitle()) {
            @Override
            public void mouseClicked(MouseEvent e) {
                Utils.openBrowser(activity.getActivity().getUrl());
            }
        };
        panel.add(linkPage, gbc);
        
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(), gbc);
        return panel;
    }

    @Override
    public JComponent getDetailsComponent() {
        String description = activity.getActivity().getDescription();
        if(description != null && !description.trim().isEmpty()) {
            return descriptionPanel(description);
        } else {
            return null;
        }
    }

    @Override
    String getUserName() {
        return activity.getActivity().getAuthor();
    }

    @Override
    public Icon getActivityIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_rss.png", true); //NOI18N
    }
    
    private JComponent descriptionPanel(String desc) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_Description"));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        JLabel lblComment = new JLabel(desc);
        panel.add(lblComment, gbc);
        return panel;
    }
}
