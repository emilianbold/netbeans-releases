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

package org.netbeans.modules.versioning.util.common;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextArea;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSCommitParameters {

    private static final String RECENT_COMMIT_MESSAGES  = "recentCommitMessage";
    private static final String LAST_COMMIT_MESSAGE     = "lastCommitMessage";
            
    private JLabel recentLink;
    private JLabel templateLink;
    private Preferences preferences;

    public VCSCommitParameters(Preferences preferences) {
        this.preferences = preferences;
    }        
    
    public abstract JPanel getPanel();   
    
    protected JLabel createRecentMessagesLink(final JTextArea text) {
        if(recentLink == null) {
            recentLink = new JLabel();
            recentLink.setIcon(new ImageIcon(VCSCommitParameters.class.getResource("/org/netbeans/modules/versioning/util/resources/recent_messages.png"))); // NOI18N
            recentLink.setToolTipText(getMessage("CTL_CommitForm_RecentMessages")); // NOI18N            
            
            recentLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            recentLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onBrowseRecentMessages(text);
                }
            });            
        }
        return recentLink;
    }
    
    protected JLabel createMessagesTemplateLink(final JTextArea text) {
        if(templateLink == null) {
            templateLink = new JLabel();
            templateLink.setIcon(new ImageIcon(VCSCommitParameters.class.getResource("/org/netbeans/modules/versioning/util/resources/load_template.png"))); // NOI18N
            templateLink.setToolTipText(getMessage("CTL_CommitForm_LoadTemplate")); // NOI18N            
            
            templateLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            templateLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onTemplate(text);
                }
            });
//          XXX  Spellchecker.register (messageTextArea);            
        }
        return templateLink;
    }
    
    private String getMessage(String msgKey) {
        return NbBundle.getMessage(VCSCommitParameters.class, msgKey);
    } 
    
    private void onBrowseRecentMessages(JTextArea text) {
        StringSelector.RecentMessageSelector selector = new StringSelector.RecentMessageSelector(preferences);    
        String message = selector.getRecentMessage(getMessage("CTL_CommitForm_RecentTitle"),  // NOI18N
                                               getMessage("CTL_CommitForm_RecentPrompt"),  // NOI18N
            getRecentCommitMessages());
        if (message != null) {
            text.replaceSelection(message);
        }
    }

    private void onTemplate(JTextArea text) {
        TemplateSelector ts = new TemplateSelector(preferences);
        if(ts.show()) {
            text.setText(ts.getTemplate());
        }
    }    
    
    protected String getLastCanceledCommitMessage() {
        return preferences.get(LAST_COMMIT_MESSAGE, "");
    }
        
    protected List<String> getRecentCommitMessages() {
        return Utils.getStringList(preferences, RECENT_COMMIT_MESSAGES);
    }    
}
