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

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.Dimension;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.netbeans.modules.versioning.util.UndoRedoSupport;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 *
 * @author Tomas Stupka
 */
public class GitCommitParameters extends DefaultCommitParameters {

    private String commitMessage;
    public GitCommitParameters(Preferences preferences, String commitMessage) {
        super(preferences, commitMessage);
        this.commitMessage = commitMessage;
    }

    @Override
    protected JPanel createPanel() {
        ParametersPanel p = new ParametersPanel();
//        JLabel mpl = DefaultCommitParameters.createMessagesTemplateLink(p.messageTextArea, getPreferences());
//        JLabel rl = DefaultCommitParameters.createRecentMessagesLink(p.messageTextArea, getPreferences());
//        p.setLinks(mpl, rl);
//        if(commitMessage != null) {
//            p.messageTextArea.setText(commitMessage);
//        }
        return p;
//        return super.createPanel();
    }

    @Override
    public ParametersPanel getPanel() {
        return (ParametersPanel) super.getPanel();
    }

    boolean isHeadVsIndex() {
        return getPanel().tgbHeadVsIndex.isSelected();
    }
    
    boolean isHeadVsWorking() {
        return getPanel().tgbHeadVsWorking.isSelected();
    }
    
    boolean isIndexVsWorking() {
        return getPanel().tgbIndexVsWorking.isSelected();
    }

    private class ParametersPanel extends JPanel {
        private JScrollPane scrollpane = new JScrollPane();
        private final JLabel messageLabel = new JLabel();        
        private final JTextArea messageTextArea = new JTextArea();
        JToggleButton tgbHeadVsIndex = new JToggleButton();
        JToggleButton tgbHeadVsWorking = new JToggleButton();
        JToggleButton tgbIndexVsWorking = new JToggleButton();
        private UndoRedoSupport um;                

        public ParametersPanel() {
            messageLabel.setLabelFor(messageTextArea);
            Mnemonics.setLocalizedText(messageLabel, getMessage("CTL_CommitForm_Message")); // NOI18N

            JLabel templateLink = getMessagesTemplateLink(messageTextArea);
            JLabel recentLink = getRecentMessagesLink(messageTextArea);

            ButtonGroup bg = new ButtonGroup();
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);

            tgbHeadVsWorking.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/head_vs_working.png"))); // NOI18N
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/git/ui/commit/Bundle"); // NOI18N
            tgbHeadVsWorking.setToolTipText(bundle.getString("ParametersPanel.tgbHeadVsWorking.toolTipText")); // NOI18N
            tgbHeadVsWorking.setFocusable(false);
            toolbar.add(tgbHeadVsWorking);
            bg.add(tgbHeadVsWorking);
            
            tgbHeadVsIndex.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/head_vs_index.png"))); // NOI18N
            tgbHeadVsIndex.setToolTipText(bundle.getString("ParametersPanel.tgbHeadVsIndex.toolTipText")); // NOI18N
            tgbHeadVsIndex.setFocusable(false);
            toolbar.add(tgbHeadVsIndex);
            bg.add(tgbHeadVsIndex);

            tgbIndexVsWorking.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/index_vs_working.png"))); // NOI18N
            tgbIndexVsWorking.setToolTipText(bundle.getString("ParametersPanel.tgbIndexVsWorking.toolTipText")); // NOI18N
            tgbIndexVsWorking.setFocusable(false);
            toolbar.add(tgbIndexVsWorking);
            bg.add(tgbIndexVsWorking);
            
            messageTextArea.setColumns(60);    //this determines the preferred width of the whole dialog
            messageTextArea.setLineWrap(true);
            messageTextArea.setRows(4);
            messageTextArea.setTabSize(4);
            messageTextArea.setWrapStyleWord(true);
            messageTextArea.setMinimumSize(new Dimension(100, 18));
            scrollpane.setViewportView(messageTextArea);

            messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
            messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N
            if(commitMessage != null) {
                messageTextArea.setText(commitMessage);
            }
            
            JPanel topPanel = new VerticallyNonResizingPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
            topPanel.add(messageLabel);
            topPanel.add(Box.createHorizontalGlue());
            topPanel.add(recentLink);
            topPanel.add(makeHorizontalStrut(recentLink, templateLink, RELATED, this));
            topPanel.add(templateLink);            
            messageLabel.setAlignmentX(LEFT_ALIGNMENT);
            messageLabel.setAlignmentY(BOTTOM_ALIGNMENT); 
            recentLink.setAlignmentY(BOTTOM_ALIGNMENT);
            templateLink.setAlignmentY(BOTTOM_ALIGNMENT);        
            toolbar.setAlignmentX(LEFT_ALIGNMENT);        
//            toolbar.setAlignmentY(TOP_ALIGNMENT);        
//            topPanel.setAlignmentX(LEFT_ALIGNMENT);        
            topPanel.setAlignmentX(LEFT_ALIGNMENT);        
            scrollpane.setAlignmentX(LEFT_ALIGNMENT);        

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(toolbar);
            add(topPanel);
            add(makeVerticalStrut(messageLabel, scrollpane, RELATED, this));            
            add(scrollpane);

            Spellchecker.register (messageTextArea);    
        }

        @Override
        public void addNotify() {
            super.addNotify();

            // XXX why in notify?
            TemplateSelector ts = new TemplateSelector(getPreferences());
            if (ts.isAutofill()) {
                messageTextArea.setText(ts.getTemplate());
            } else {
                String lastCommitMessage = getLastCanceledCommitMessage();
                if (lastCommitMessage.isEmpty() && new StringSelector.RecentMessageSelector(getPreferences()).isAutoFill()) {
                    List<String> messages = getRecentCommitMessages(getPreferences());
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
    }
}
