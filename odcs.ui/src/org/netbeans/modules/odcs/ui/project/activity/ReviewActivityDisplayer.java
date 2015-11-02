/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui.project.activity;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.util.MissingResourceException;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import org.netbeans.modules.mylyn.util.WikiPanel;
import org.netbeans.modules.mylyn.util.WikiUtils;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public final class ReviewActivityDisplayer extends ActivityDisplayer {
    private static final String PROP_REVIEW_ID = "reviewId"; // NOI18N
    private static final String PROP_REVIEW_TITLE = "reviewTitle"; // NOI18N
    private static final String PROP_ACTION = "action"; // NOI18N
    private static final String ACTION_CREATED = "CREATED"; // NOI18N
    private static final String ACTION_COMMENTED = "COMMENTED"; // NOI18N
    // TODO not getting right from the server now (yet)  but probably will want to show
    // more actions in future - like merged and closed (completed/cancelled), approved/rejected
    private static final String PROP_COMMENT = "comment"; // NOI18N

    private final Activity activity;
    private final ProjectHandle<ODCSProject> projectHandle;

    public ReviewActivityDisplayer(Activity activity, ProjectHandle<ODCSProject> projectHandle, int maxWidth) {
        super(activity.getTimestamp(), maxWidth);
        this.activity = activity;
        this.projectHandle = projectHandle;
    }

    @Override
    String getUserName() {
        return getUserNameFromActivity(activity);
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        final String reviewId = activity.getProperty(PROP_REVIEW_ID);
        LinkLabel reviewLink = new LinkLabel() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Utils.openBrowser(getReviewUrl(reviewId));
            }
        };
        String title = Utils.computeFitText(reviewLink, maxWidth, activity.getProperty(PROP_REVIEW_TITLE), false);
        reviewLink.setText(title);

        String bundleKey = "FMT_Review_" + activity.getProperty(PROP_ACTION); // NOI18N
        try {
            return createMultipartTextComponent(bundleKey, reviewId, reviewLink);
        } catch (MissingResourceException ex) {
            return createMultipartTextComponent("FMT_ReviewModified", reviewId, reviewLink); // NOI18N
        }
    }

    @Override
    public JComponent getDetailsComponent() {
        String action = activity.getProperty(PROP_ACTION);
        if (ACTION_COMMENTED.equals(action)) {
            String comment = activity.getProperty(PROP_COMMENT);
            if (comment != null && comment.length() > 0) {
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setOpaque(false);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(0, 0, 5, 0);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.gridwidth = GridBagConstraints.REMAINDER;

                JLabel lbl = new JLabel(NbBundle.getMessage(ReviewActivityDisplayer.class, "LBL_Comment")); // NOI18N
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                panel.add(lbl, gbc);

                String wikiLanguage = projectHandle.getTeamProject().getWikiLanguage();
                WikiPanel commentPanel = WikiUtils.getWikiPanel(wikiLanguage, false, false);
                commentPanel.setWikiFormatText(comment);
                panel.add(commentPanel, gbc);
                return panel;
            }
        }
        return null;
    }

    @Override
    public Icon getActivityIcon() {
        // TODO we'd like to have distinct icons for merge requests, for now using icons for tasks
        String action = activity.getProperty(PROP_ACTION);
        if (ACTION_CREATED.equals(action)) {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_task_created.png", true); // NOI18N
        } else if (ACTION_COMMENTED.equals(action)) {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_task_commented.png", true); // NOI18N
        } else {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_task.png", true); // NOI18N
        }
    }

    private String getReviewUrl(String reviewId) {
        return projectHandle.getTeamProject().getWebUrl() + "/review/" + reviewId; // NOI18N
    }

}
