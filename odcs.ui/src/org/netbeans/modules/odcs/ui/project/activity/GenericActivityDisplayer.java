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

import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import oracle.clouddev.server.profile.activity.client.api.Author;

/**
 * A generic displayer of activity - shows raw activity data as received from
 * the server (the type, ID, and all properties). Can be used to see the activity
 * data when need to implement a new type (or change in data). Normally not shown
 * to the user (there are special displayers for supported activity types), can be
 * configured in ActivityPanel constructor (mapping to activity type) and in
 * RecentActivitiesPanel.isActivityShowable (to enable given type).
 */
public class GenericActivityDisplayer extends ActivityDisplayer {
    private final Activity activity;

    public GenericActivityDisplayer(Activity activity, int maxWidth) {
        super(activity.getTimestamp(), maxWidth);
        this.activity = activity;
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        return new JLabel("Activity " + activity.getType() + " " + activity.getId()); // NOI18N
    }

    @Override
    public JComponent getDetailsComponent() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        panel.setLayout(layout);
        GroupLayout.ParallelGroup hGroup1 = layout.createParallelGroup();
        GroupLayout.ParallelGroup hGroup2 = layout.createParallelGroup();
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        Map<String,String> properties = activity.getProperties();
        if (properties.isEmpty()) {
            JLabel l = new JLabel("no properties"); // NOI18N
            hGroup1.addComponent(l);
            vGroup.addComponent(l);
        } else {
            for (Map.Entry<String,String> e : properties.entrySet()) {
                JLabel l1 = new JLabel(e.getKey());
                JLabel l2 = new JLabel(e.getValue());
                hGroup1.addComponent(l1);
                hGroup2.addComponent(l2);
                vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(l1).addComponent(l2));
            }
        }
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(hGroup1).addGroup(hGroup2));
        layout.setVerticalGroup(vGroup);
        return panel;
    }

    @Override
    String getUserName() {
        Author author = activity.getAuthor();
        return author != null ? author.getFullname() : "Unknown Author"; // NOI18N
    }
    
}
