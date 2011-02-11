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

/*
 * CommitPanel.java
 *
 * Created on Nov 9, 2010, 5:18:15 PM
 */

package org.netbeans.modules.git.ui.commit;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.netbeans.modules.versioning.util.UndoRedoSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class CommitPanel extends javax.swing.JPanel {
    private final GitCommitParameters parameters;
    private UndoRedoSupport um; 

    /** Creates new form CommitPanel */
    public CommitPanel(GitCommitParameters parameters, String commitMessage, String user) {
        this.parameters = parameters;
        
        initComponents();
        Mnemonics.setLocalizedText(messageLabel, getMessage("CTL_CommitForm_Message")); // NOI18N
        
        messageTextArea.setColumns(60);    //this determines the preferred width of the whole dialog
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(4);
        messageTextArea.setTabSize(4);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setMinimumSize(new Dimension(100, 18));
        
        messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
        messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N
        if(commitMessage != null) {
            messageTextArea.setText(commitMessage);
        }
        
        
        List<String> authors = GitModuleConfig.getDefault().getRecentCommitAuthors(); 
        if(authors != null && !authors.isEmpty()) {
            authorComboBox.setModel(new DefaultComboBoxModel(authors.toArray(new String[authors.size()])));
        } else if(user != null) {
            authorComboBox.setModel(new DefaultComboBoxModel(new String[] {user}));
        }
        setCaretPosition(authorComboBox);
        
        List<String> commiters = GitModuleConfig.getDefault().getRecentCommiters();
        if(commiters != null && !commiters.isEmpty()) {
            commiterComboBox.setModel(new DefaultComboBoxModel(commiters.toArray(new String[commiters.size()])));
        } else if(user != null) {
            commiterComboBox.setModel(new DefaultComboBoxModel(new String[] {user}));            
        }
        setCaretPosition(commiterComboBox);
        
        Spellchecker.register (messageTextArea);  
        
    }

    private void setCaretPosition(JComboBox cbo) {
        Component cmp = cbo.getEditor().getEditorComponent();
        if(cmp instanceof JTextComponent) {
            ((JTextComponent)cmp).setCaretPosition(1);
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();

        // XXX why in notify?
        TemplateSelector ts = new TemplateSelector(parameters.getPreferences());
        if (ts.isAutofill()) {
            messageTextArea.setText(ts.getTemplate());
        } else {
            String lastCommitMessage = parameters.getLastCanceledCommitMessage();
            if (lastCommitMessage.isEmpty() && new StringSelector.RecentMessageSelector(parameters.getPreferences()).isAutoFill()) {
                List<String> messages = parameters.getCommitMessages();
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
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        templatesLabel = parameters.getMessagesTemplateLink(messageTextArea);
        recentLabel = parameters.getRecentMessagesLink(messageTextArea);

        messageLabel.setLabelFor(messageTextArea);
        messageLabel.setText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.messageLabel.text")); // NOI18N

        messageTextArea.setColumns(20);
        messageTextArea.setRows(5);
        jScrollPane1.setViewportView(messageTextArea);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.jLabel2.text")); // NOI18N

        authorComboBox.setEditable(true);

        commiterComboBox.setEditable(true);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.jLabel3.text")); // NOI18N

        templatesLabel.setText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.templatesLabel.text")); // NOI18N

        recentLabel.setText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.recentLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(messageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 465, Short.MAX_VALUE)
                        .addComponent(recentLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(templatesLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(authorComboBox, 0, 219, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(commiterComboBox, 0, 219, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageLabel)
                    .addComponent(templatesLabel)
                    .addComponent(recentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(authorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(commiterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JComboBox authorComboBox = new javax.swing.JComboBox();
    final javax.swing.JComboBox commiterComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel messageLabel;
    final javax.swing.JTextArea messageTextArea = new javax.swing.JTextArea();
    private javax.swing.JLabel recentLabel;
    private javax.swing.JLabel templatesLabel;
    // End of variables declaration//GEN-END:variables

    private String getMessage(String msgKey) {
        return NbBundle.getMessage(CommitPanel.class, msgKey);
    }      
    
}
