/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.search;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.search.PopupItem.IssueItem;

/**
 * Quick search toolbar component
 * @author Jan Becicka
 */
public class QuickSearchComboBar extends javax.swing.JPanel {

    QuickSearchPopup displayer;
    Color origForeground;
    private JPanel caller;
    private Issue issue;
    PropertyChangeSupport changeSupport;
    private boolean ignoreCommandChanges = false;

    public static final String EVT_ISSUE_CHANGED = "QuickSearchComboBar.issue.changed"; // NOI18N

    public QuickSearchComboBar(JPanel caller) {
        this.caller = caller;
        displayer = new QuickSearchPopup(this);
        initComponents();
        command.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {
                textChanged();
            }
            public void removeUpdate(DocumentEvent arg0) {
                textChanged();
            }
            public void changedUpdate(DocumentEvent arg0) {
                textChanged();
            }
            private void textChanged () {
                if(ignoreCommandChanges) {
                    return;
                }
                if (command.isFocusOwner()) {
                    displayer.maybeEvaluate(command.getText());
                }
                setIssue(null);
            }
        });
    }


    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    public Issue getIssue() {
        return issue;
    }

    public void setRepository(Repository repo) {
        displayer.setRepository(repo);
    }

    void setIssue(Issue issue) {
        Issue oldIssue = this.issue;
        this.issue = issue;
        if(this.issue != null) {
            ignoreCommandChanges = true;
            command.setText(IssueItem.getIssueDescription(this.issue));
            ignoreCommandChanges = false;
            displayer.setVisible(false);
        }
        if(oldIssue != null || this.issue != null) {
            getChangeSupport().firePropertyChange(EVT_ISSUE_CHANGED, oldIssue, this.issue);
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

        command = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setName("Form"); // NOI18N
        setOpaque(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        command.setText(org.openide.util.NbBundle.getMessage(QuickSearchComboBar.class, "QuickSearchComboBar.command.text")); // NOI18N
        command.setName("command"); // NOI18N
        command.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                commandKeyPressed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(command, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(command, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        command.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QuickSearchComboBar.class, "QuickSearchComboBar.command.AccessibleContext.accessibleName")); // NOI18N
        command.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QuickSearchComboBar.class, "QuickSearchComboBar.command.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        displayer.setVisible(false);
    }//GEN-LAST:event_formFocusLost

    private void commandKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_commandKeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
            displayer.selectNext();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            displayer.selectPrev();
            evt.consume();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            invokeSelectedItem();
        } else if ((evt.getKeyCode()) == KeyEvent.VK_ESCAPE) {
            returnFocus();
            displayer.clearModel();
            evt.consume();
        }
    }//GEN-LAST:event_commandKeyPressed

    private void returnFocus () {
        displayer.setVisible(false);
        if (caller != null) {
            caller.requestFocus();
        }
    }

    public void enableFields(boolean enable) {
        command.setEnabled(enable);
    }

    /** Actually invokes action selected in the results list */
    public void invokeSelectedItem () {
        JList list = displayer.getList();
        if (list.getModel().getSize() > 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JList l = displayer.getList();
                    if (l.getSelectedIndex() == -1) { // Issue 161447
                        l.setSelectedIndex(l.getModel().getSize()-1);
                    }
                    displayer.invoke();
                }
            });
        }
    }

    private PropertyChangeSupport getChangeSupport() {
        if(changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        return changeSupport;
    }

    public void setNoResults (boolean areNoResults) {
        // no op when called too soon
        if (command == null || origForeground == null) {
            return;
        }
        // don't alter color if showing hint already
        if (command.getForeground().equals(command.getDisabledTextColor())) {
            return;
        }
        command.setForeground(areNoResults ? Color.RED : origForeground);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField command;
    // End of variables declaration//GEN-END:variables


    @Override
    public void requestFocus() {
        super.requestFocus();
        command.requestFocus();
    }

    public JTextField getCommand() {
        return command;
    }

    static Color getPopupBorderColor () {
        Color shadow = UIManager.getColor("controlShadow"); // NOI18N
        return shadow != null ? shadow : Color.GRAY;
    }

    static Color getTextBackground () {
        Color textB = UIManager.getColor("TextPane.background"); // NOI18N
        return textB != null ? textB : Color.WHITE;
    }

    static Color getResultBackground () {
        return getTextBackground();
    }


}
