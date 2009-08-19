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
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.search.PopupItem.IssueItem;

/**
 * Quick search toolbar component
 * @author Jan Becicka
 * @author Tomas Stupka
 */
public class QuickSearchComboBar extends javax.swing.JPanel {

    private QuickSearchPopup displayer;
    private Color origForeground;
    private JPanel caller;    
    private PropertyChangeSupport changeSupport;

    public static final String EVT_ISSUE_CHANGED = "QuickSearchComboBar.issue.changed"; // NOI18N

    public QuickSearchComboBar(JPanel caller) {
        this.caller = caller;
        initComponents();
        command.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value instanceof Issue) {
                    Issue item = (Issue) value;
                    value = IssueItem.getIssueDescription(item);
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        command.setEditor(new ComboEditor(command.getEditor()));
        displayer = new QuickSearchPopup(this);
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
        return (Issue) command.getEditor().getItem();
    }

    public void setRepository(Repository repo) {
        displayer.setRepository(repo);
        Collection<Issue> issues = BugtrackingManager.getInstance().getRecentIssues(repo);
        command.setModel(new DefaultComboBoxModel(issues.toArray(new Issue[issues.size()])));
        command.setSelectedItem(null);
    }

    void setIssue(Issue issue) {
        if(issue != null) {
            command.getEditor().setItem(issue);
            displayer.setVisible(false);
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

        command = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setName("Form"); // NOI18N
        setOpaque(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        command.setEditable(true);
        command.setName("command"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(command, 0, 353, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(command, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        displayer.setVisible(false);
    }//GEN-LAST:event_formFocusLost

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
        if (command.getForeground().equals(((JTextField) command.getEditor().getEditorComponent()).getDisabledTextColor())) {
            return;
        }
        command.setForeground(areNoResults ? Color.RED : origForeground);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox command;
    // End of variables declaration//GEN-END:variables


    @Override
    public void requestFocus() {
        super.requestFocus();
        command.requestFocus();
    }

    public Component getIssueComponent() {
        return command;
    }

    String getText() {
        return ((JTextField)command.getEditor().getEditorComponent()).getText();
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

    boolean isTextFieldFocusOwner() {
        return command.getEditor().getEditorComponent().isFocusOwner();
    }

    private class ComboEditor implements ComboBoxEditor {
        private final JTextField editor;
        private Issue issue;
        private boolean ignoreCommandChanges = false;
        private final ComboBoxEditor delegate;

        public ComboEditor(ComboBoxEditor delegate) {
            this.delegate = delegate;
            editor = (JTextField) delegate.getEditorComponent();
            editor.getDocument().addDocumentListener(new DocumentListener() {
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
                    if (isTextFieldFocusOwner()) {
                        if(!editor.getText().equals("")) {
                            command.hidePopup();
                        }
                        displayer.maybeEvaluate(editor.getText());
                    }
                    setItem(null, true);
                }
            });
            editor.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    commandKeyPressed(evt);
                }
            });
        }

        public Component getEditorComponent() {
            return editor;
        }

        private void setItem(Object anObject, boolean keepText) {
            Issue oldIssue = issue;
            if(anObject == null) {
                issue = null;
                if(!keepText) {
                    editor.setText("");
                }
            } else if(anObject instanceof Issue) {
                issue = (Issue) anObject;
                ignoreCommandChanges = true;
                if(!keepText) {
                    editor.setText(IssueItem.getIssueDescription(issue));
                }
                ignoreCommandChanges = false;
            }
            if(oldIssue != null || issue != null) {
                getChangeSupport().firePropertyChange(EVT_ISSUE_CHANGED, oldIssue, issue);
            }
        }

        public void setItem(Object anObject) {
            setItem(anObject, false);
        }

        public Object getItem() {
            return issue;
        }

        public void selectAll() {
            delegate.selectAll();
        }

        public void addActionListener(ActionListener l) {
            delegate.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            delegate.removeActionListener(l);
        }

        private void commandKeyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
                if(displayer.isVisible()) {
                    displayer.selectNext();
                    evt.consume();
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                if(displayer.isVisible()) {
                    displayer.selectPrev();
                    evt.consume();
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                if(displayer.isVisible()) {
                    evt.consume();
                    invokeSelectedItem();
                }
            } else if ((evt.getKeyCode()) == KeyEvent.VK_ESCAPE) {
                if(displayer.isVisible()) {
                    returnFocus();
                    displayer.clearModel();
                    evt.consume();
                }
            }
        }
    }

}
