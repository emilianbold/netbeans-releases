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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.commit;

import java.awt.Color;
import java.util.List;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.netbeans.modules.versioning.util.UndoRedoSupport;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;

/**
 *
 * @author Tomas Stupka
 */
public class CommitParameters extends VCSCommitParameters {
    private ParametersPanel panel;

    public CommitParameters() {
        super(GitModuleConfig.getDefault().getPreferences());
    }
    
    @Override
    public JPanel getPanel() {
        if(panel == null) {
            panel = new ParametersPanel();
        }
        return panel;
    }

    String getCommitMessage() {
        return ((ParametersPanel) getPanel()).messageTextArea.getText();
    }
    
    private class ParametersPanel extends JPanel {
        private JScrollPane scrollpane = new JScrollPane();
        private final JLabel messageLabel = new JLabel();        
        private final JTextArea messageTextArea = new JTextArea();
        private UndoRedoSupport um;                
    
        public ParametersPanel() {
            messageLabel.setLabelFor(messageTextArea);
            Mnemonics.setLocalizedText(messageLabel, getMessage("CTL_CommitForm_Message")); // NOI18N
            
            JLabel templateLink = createMessagesTemplateLink(messageTextArea);
            JLabel recentLink = createRecentMessagesLink(messageTextArea);

            messageTextArea.setColumns(60);    //this determines the preferred width of the whole dialog
            messageTextArea.setLineWrap(true);
            messageTextArea.setRows(4);
            messageTextArea.setTabSize(4);
            messageTextArea.setWrapStyleWord(true);
            messageTextArea.setMinimumSize(new Dimension(100, 18));
            scrollpane.setViewportView(messageTextArea);
            
            messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
            messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N
            
            JPanel topPanel = new VerticallyNonResizingPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
            topPanel.add(messageLabel);
            topPanel.add(Box.createHorizontalGlue());
            topPanel.add(recentLink);
            topPanel.add(makeHorizontalStrut(recentLink, templateLink, RELATED));
            topPanel.add(templateLink);            
            messageLabel.setAlignmentX(LEFT_ALIGNMENT);
            messageLabel.setAlignmentY(BOTTOM_ALIGNMENT); 
            recentLink.setAlignmentY(BOTTOM_ALIGNMENT);
            templateLink.setAlignmentY(BOTTOM_ALIGNMENT);        

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            topPanel.setAlignmentY(BOTTOM_ALIGNMENT);        
            add(topPanel);
            add(makeVerticalStrut(messageLabel, scrollpane, RELATED));            
            add(scrollpane);
            
        }

        @Override
        public void addNotify() {
            super.addNotify();
            
            TemplateSelector ts = new TemplateSelector(GitModuleConfig.getDefault().getPreferences());
            if (ts.isAutofill()) {
                messageTextArea.setText(ts.getTemplate());
            } else {
                String lastCommitMessage = getLastCanceledCommitMessage();
                if (lastCommitMessage.isEmpty() && new StringSelector.RecentMessageSelector(GitModuleConfig.getDefault().getPreferences()).isAutoFill()) {
                    List<String> messages = getRecentCommitMessages();
                    if (messages.size() > 0) {
                        lastCommitMessage = messages.get(0);
                    }
                }
                messageTextArea.setText(lastCommitMessage);
            }
            messageTextArea.selectAll();
            um = UndoRedoSupport.register(messageTextArea);          
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            if (um != null) {
                um.unregister();
                um = null;
            }            
        }
        
        private String getMessage(String msgKey) {
            return NbBundle.getMessage(ParametersPanel.class, msgKey);
        }        
     
        private Component makeVerticalStrut(JComponent compA,
                                            JComponent compB,
                                            ComponentPlacement relatedUnrelated) {
            int height = LayoutStyle.getInstance().getPreferredGap(
                                compA,
                                compB,
                                relatedUnrelated,
                                SOUTH,
                                this);
            return Box.createVerticalStrut(height);
        }    
        
        private Component makeHorizontalStrut(JComponent compA,
                                              JComponent compB,
                                              ComponentPlacement relatedUnrelated) {
            int width = LayoutStyle.getInstance().getPreferredGap(
                                compA,
                                compB,
                                relatedUnrelated,
                                WEST,
                                this);
            return Box.createHorizontalStrut(width);
        }
    }
    
}
