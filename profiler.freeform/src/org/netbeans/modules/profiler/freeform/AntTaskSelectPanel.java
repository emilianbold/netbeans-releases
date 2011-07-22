/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.freeform;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.profiler.api.project.AntProjectSupport;
import org.netbeans.modules.profiler.ui.NBHTMLLabel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
final class AntTaskSelectPanel extends JPanel implements HelpCtx.Provider {
    
    private static final String SELECT_TARGET_ITEM_STRING = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_SelectTargetItemString"); // NOI18N
    private static final String SELECT_PROJECT_TASK_LABEL_STRING = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_SelectProjectTaskLabelString"); // NOI18N
    private static final String SELECT_FILE_TASK_LABEL_STRING = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_SelectFileTaskLabelString"); // NOI18N
    private static final String CREATE_NEW_TARGET_MSG = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_CreateNewTargetMsg"); // NOI18N
    private static final String TARGET_BOX_ACCESS_NAME = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_TargetBoxAccessName"); // NOI18N
    private static final String TARGET_BOX_ACCESS_DESCR = NbBundle.getMessage(FreeFormProjectProfilingSupportProvider.class,
            "FreeFormProjectTypeProfiler_TargetBoxAccessDescr"); // NOI18N
    
    private static final String HELP_CTX_KEY = "FreeFormProjectTypeProfiler.AntTaskSelectPanel.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    
    //~ Instance fields ------------------------------------------------------------------------------------------------------
    final JComboBox targetBox;
    final JLabel label;
    final NBHTMLLabel descriptionLabel;

    AntTaskSelectPanel(final List list, final int type, final JButton okButton) {
        list.add(0, SELECT_TARGET_ITEM_STRING);
        if (type == AntProjectSupport.TARGET_PROFILE) {
            label = new JLabel(SELECT_PROJECT_TASK_LABEL_STRING);
        } else {
            label = new JLabel(SELECT_FILE_TASK_LABEL_STRING);
        }
        descriptionLabel = new NBHTMLLabel(CREATE_NEW_TARGET_MSG);
        targetBox = new JComboBox(list.toArray(new Object[list.size()]));
        targetBox.setSelectedIndex(0);
        targetBox.addItemListener(new ItemListener() {

            public void itemStateChanged(final ItemEvent e) {
                okButton.setEnabled(targetBox.getSelectedIndex() != 0);
            }
        });
        setLayout(new GridBagLayout());
        label.setLabelFor(targetBox);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.insets = new Insets(12, 12, 12, 12);
        add(label, gridBagConstraints);
        targetBox.getAccessibleContext().setAccessibleName(TARGET_BOX_ACCESS_NAME);
        targetBox.getAccessibleContext().setAccessibleDescription(TARGET_BOX_ACCESS_DESCR);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.insets = new Insets(12, 0, 12, 12);
        add(targetBox, gridBagConstraints);
        descriptionLabel.setFocusable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(12, 12, 12, 12);
        add(descriptionLabel, gridBagConstraints);
        okButton.setEnabled(false);
    }

    //~ Methods --------------------------------------------------------------------------------------------------------------
    public String getTargetName() {
        if (targetBox.getSelectedIndex() == 0) {
            return null; //nothing selected
        }
        return (String) targetBox.getSelectedItem();
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }
    
}
