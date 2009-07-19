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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.lib.editor.codetemplates.storage.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.awt.Mnemonics;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class CodeTemplatesPanel extends JPanel implements ActionListener, ListSelectionListener, KeyListener {
    
    private static final Logger LOG = Logger.getLogger(CodeTemplatesPanel.class.getName());
    private CodeTemplatesModel  model;
    
    /** 
     * Creates new form CodeTemplatesPanel. 
     */
    public CodeTemplatesPanel () {
        initComponents ();
        
        loc(lLanguage, "Language"); //NOI18N
        loc(lTemplates, "Templates"); //NOI18N
        loc(bNew, "New"); //NOI18N
        loc(bRemove, "Remove"); //NOI18N
        loc(lExplandTemplateOn, "ExpandTemplateOn"); //NOI18N
        loc(tabPane, 0, "Expanded_Text", epExpandedText); //NOI18N
        loc(tabPane, 1, "Description", epDescription); //NOI18N
        tabPane.getAccessibleContext().setAccessibleName(loc("AN_tabPane")); //NOI18N
        tabPane.getAccessibleContext().setAccessibleDescription(loc("AD_tabPane")); //NOI18N
        
        cbExpandTemplateOn.addItem(loc("SPACE")); //NOI18N
        cbExpandTemplateOn.addItem(loc("S-SPACE")); //NOI18N
        cbExpandTemplateOn.addItem(loc("TAB")); //NOI18N
        cbExpandTemplateOn.addItem(loc("ENTER")); //NOI18N
        
        bRemove.setEnabled (false);
        tTemplates.getTableHeader().setReorderingAllowed(false);
        tTemplates.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        epExpandedText.addKeyListener(this);
        epDescription.addKeyListener(this);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (CodeTemplatesPanel.class, key);
    }
    
    private static void loc(Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext().setAccessibleName(loc("AN_" + key)); //NOI18N
            c.getAccessibleContext().setAccessibleDescription(loc("AD_" + key)); //NOI18N
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText((AbstractButton) c, loc("CTL_" + key)); //NOI18N
        } else {
            Mnemonics.setLocalizedText((JLabel) c, loc("CTL_" + key)); //NOI18N
        }
    }
    
    private static void loc(JTabbedPane p, int tabIdx, String key, JEditorPane ep) {
        JLabel label = new JLabel(); // Only for setting tab names

        String tabName = loc("CTL_" + key); //NOI18N
        Mnemonics.setLocalizedText(label, tabName);
        p.setTitleAt(tabIdx, label.getText());

        int idx = Mnemonics.findMnemonicAmpersand(tabName);
        if (idx != -1 && idx + 1 < tabName.length()) {
            char ch = Character.toUpperCase(tabName.charAt(idx + 1));
            p.setMnemonicAt(tabIdx, ch);
            ep.setFocusAccelerator(ch);
        }
    }
    
    // OptionsCategory.Panel ...................................................
    
    void update () {
        model = new CodeTemplatesModel ();

        cbLanguage.removeActionListener (this);
        bNew.removeActionListener (this);
        bRemove.removeActionListener (this);
        cbExpandTemplateOn.removeActionListener (this);
        tTemplates.getSelectionModel ().removeListSelectionListener (this);
        
        cbLanguage.removeAllItems ();
        List<String> languages = new ArrayList<String>(model.getLanguages ());
        Collections.sort (languages);
        for(String l : languages) {
            cbLanguage.addItem(l);
        }
        if (languages.isEmpty ()) {
            cbLanguage.setEnabled (false);
            bNew.setEnabled (false);
            bRemove.setEnabled (false);
            tTemplates.setEnabled (false);
            tabPane.setEnabled (false);
            cbExpandTemplateOn.setEnabled (false);
        }
        KeyStroke expander = model.getExpander ();
        if (KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, KeyEvent.SHIFT_MASK).equals (expander))
            cbExpandTemplateOn.setSelectedIndex (1);
        else
        if (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, 0).equals (expander))
            cbExpandTemplateOn.setSelectedIndex (2);
        else
        if (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0).equals (expander))
            cbExpandTemplateOn.setSelectedIndex (3);
        else
            cbExpandTemplateOn.setSelectedIndex (0);
        
        cbLanguage.addActionListener (this);
        bNew.addActionListener (this);
        bRemove.addActionListener (this);
        cbExpandTemplateOn.addActionListener (this);
        tTemplates.getSelectionModel ().addListSelectionListener (this);
        
        // Pre-select a language
        String defaultSelectedLang = null;
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        if (pane != null) {
            String mimeType = (String)pane.getDocument().getProperty("mimeType"); // NOI18N
            if (mimeType != null) {
                defaultSelectedLang = model.findLanguage(mimeType);
            }
        }
        if (defaultSelectedLang == null) {
            defaultSelectedLang = model.findLanguage("text/x-java"); //NOI18N
        }
        if (defaultSelectedLang == null) {
            defaultSelectedLang = model.findLanguage("text/x-ruby"); //NOI18N
        }
        if (defaultSelectedLang == null) {
            defaultSelectedLang = model.findLanguage("text/x-c++"); //NOI18N
        }
        if (defaultSelectedLang == null && model.getLanguages().size() > 0) {
            defaultSelectedLang = model.getLanguages().get(0);
        }
        if (defaultSelectedLang != null) {
            cbLanguage.setSelectedItem(defaultSelectedLang);
        }
    }
    
    void applyChanges () {
        if (model != null) {
            saveCurrentTemplate ();
            model.saveChanges ();
        }
    }
    
    void cancel () {
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        if (model == null) return false;
        return model.isChanged ();
    }
    
    // ActionListener ..........................................................
    private boolean languagePopupVisible = false;

    public void actionPerformed (ActionEvent e) {
        if (e.getSource () == cbLanguage && !languagePopupVisible) {
            saveCurrentTemplate ();
            final String language = (String) cbLanguage.getSelectedItem ();
            final CodeTemplatesModel.TM tableModel = model.getTableModel (language);
            
            tTemplates.setModel (tableModel);
            TableColumn c1 = tTemplates.getTableHeader().getColumnModel().getColumn(0);
            c1.setMinWidth(80);
            c1.setPreferredWidth(100);
            c1.setResizable(true);
            
            TableColumn c2 = tTemplates.getTableHeader().getColumnModel().getColumn(1);
            c2.setMinWidth(180);
            c2.setPreferredWidth(250);
            c2.setResizable(true);

            TableColumn c3 = tTemplates.getTableHeader().getColumnModel().getColumn(2);
            c3.setMinWidth(180);
            c3.setPreferredWidth(250);
            c3.setResizable(true);

            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    epDescription.setEditorKit(CloneableEditorSupport.getEditorKit("text/html")); //NOI18N
                    epExpandedText.setEditorKit(CloneableEditorSupport.getEditorKit(model.getMimeType (language)));
                    if (tableModel.getRowCount () > 0) {
                        lastIndex = -1;
                        tTemplates.getSelectionModel ().setSelectionInterval (0, 0);
                        lastIndex = 0;
                    } else
                        lastIndex = -1;
                }
            });
        } else
        if (e.getSource () == bNew) {
            saveCurrentTemplate ();
            InputLine descriptor = new InputLine (
                loc ("CTL_Enter_template_name"),
                loc ("CTL_New_template_dialog_title")
            );
            if (DialogDisplayer.getDefault().notify(descriptor) == InputLine.OK_OPTION ) {
                String newAbbrev = descriptor.getInputText().trim();
                
                if (newAbbrev.length() == 0) {
                    DialogDisplayer.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            loc ("CTL_Empty_template_name"),
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                } else {
                    CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
                    int i, rows = tableModel.getRowCount ();
                    for (i = 0; i < rows; i++) {
                        String abbrev = tableModel.getAbbreviation(i);
                        if (newAbbrev.equals (abbrev)) {
                            DialogDisplayer.getDefault ().notify (
                                new NotifyDescriptor.Message (
                                    loc ("CTL_Duplicate_template_name"),
                                    NotifyDescriptor.ERROR_MESSAGE
                                )
                            );
                            break;
                        }
                    }
                    if (i == rows) {
                        lastIndex = -1;
                        int rowIdx = tableModel.addCodeTemplate(newAbbrev);
                        tTemplates.getSelectionModel().setSelectionInterval(rowIdx, rowIdx);
                    }
                }
                
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        // Scroll to the bottom
                        spTemplates.getVerticalScrollBar().setValue(
                            spTemplates.getVerticalScrollBar().getMaximum());

                        // Show the extpanded text and place the focus in it
                        tabPane.setSelectedIndex(0);
                        epExpandedText.requestFocus ();
                    }
                });
            }
        } else if (e.getSource () == bRemove) {
            CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
            int index = tTemplates.getSelectedRow ();
            tableModel.removeCodeTemplate(index);
            lastIndex = -1;
            
            if (index < tTemplates.getModel ().getRowCount ()) {
                tTemplates.getSelectionModel ().setSelectionInterval(index, index);
            } else if (tTemplates.getModel ().getRowCount () > 0) {
                tTemplates.getSelectionModel ().setSelectionInterval (
                    tTemplates.getModel ().getRowCount () - 1,
                    tTemplates.getModel ().getRowCount () - 1
                );
            } else {
                bRemove.setEnabled (false);
            }
        } else if (e.getSource () == cbExpandTemplateOn) {
            switch (cbExpandTemplateOn.getSelectedIndex ()) {
                case 0:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0));
                    break;
                case 1:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, KeyEvent.SHIFT_MASK));
                    break;
                case 2:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, 0));
                    break;
                case 3:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0));
                    break;
            }
        }
    }
    
    public void valueChanged (ListSelectionEvent e) {
        // new line in code templates table has been selected
        int index = tTemplates.getSelectedRow ();
        if (index < 0) {
            epDescription.setText(""); //NOI18N
            epExpandedText.setText(""); //NOI18N
            bRemove.setEnabled (false);
            lastIndex = -1;
            return;
        }

        saveCurrentTemplate ();

        // Show details of the newly selected code tenplate
        CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
        // Don't use JEditorPane.setText(), because it goes through EditorKit.read()
        // and performs conversion as if the text was read from a file (eg. EOL
        // translations). See #130095 for details.
        setDocumentText(epDescription.getDocument(), tableModel.getDescription(index));
        setDocumentText(epExpandedText.getDocument(), tableModel.getText(index));

        bRemove.setEnabled(true);
        lastIndex = index;
    }
    
    private static void setDocumentText(Document doc, String text) {
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);
        } catch (BadLocationException ble) {
            LOG.log(Level.WARNING, null, ble);
        }
    }
    
    private int lastIndex = -1;
    
    private void saveCurrentTemplate() {
        if (lastIndex < 0) {
            return;
        }

        CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
        // Don't use JEditorPane.getText(), because it goes through EditorKit.write()
        // and performs conversion as if the text was written to a file (eg. EOL
        // translations). See #130095 for details.
        try {
            tableModel.setDescription(lastIndex, CharSequenceUtilities.toString(DocumentUtilities.getText(epDescription.getDocument(), 0, epDescription.getDocument().getLength())));
            tableModel.setText(lastIndex, CharSequenceUtilities.toString(DocumentUtilities.getText(epExpandedText.getDocument(), 0, epExpandedText.getDocument().getLength())));
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        firePropertyChange(OptionsPanelController.PROP_CHANGED, null, null);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        // XXX: hack for #113802
        if (e.getKeyCode() == 32) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
    }
    
    // UI form .................................................................
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lLanguage = new javax.swing.JLabel();
        cbLanguage = new javax.swing.JComboBox();
        lTemplates = new javax.swing.JLabel();
        spTemplates = new javax.swing.JScrollPane();
        tTemplates = new javax.swing.JTable();
        bNew = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        lExplandTemplateOn = new javax.swing.JLabel();
        cbExpandTemplateOn = new javax.swing.JComboBox();
        tabPane = new javax.swing.JTabbedPane();
        spExpandedText = new javax.swing.JScrollPane();
        epExpandedText = new javax.swing.JEditorPane();
        spDescription = new javax.swing.JScrollPane();
        epDescription = new javax.swing.JEditorPane();

        lLanguage.setLabelFor(cbLanguage);
        lLanguage.setText("Language:");

        cbLanguage.setNextFocusableComponent(tTemplates);
        cbLanguage.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                cbLanguagePopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                cbLanguagePopupMenuWillBecomeVisible(evt);
            }
        });

        lTemplates.setLabelFor(tTemplates);
        lTemplates.setText("Templates:");

        tTemplates.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Abbreviation", "Expanded Text", "Description"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tTemplates.setFocusCycleRoot(true);
        spTemplates.setViewportView(tTemplates);

        bNew.setText("New");
        bNew.setNextFocusableComponent(bRemove);

        bRemove.setText("Remove");

        lExplandTemplateOn.setLabelFor(cbExpandTemplateOn);
        lExplandTemplateOn.setText("Expand Template on:");

        cbExpandTemplateOn.setNextFocusableComponent(bNew);

        tabPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setFocusCycleRoot(true);
        tabPane.setNextFocusableComponent(cbExpandTemplateOn);

        spExpandedText.setViewportView(epExpandedText);

        tabPane.addTab("tab1", spExpandedText);

        spDescription.setViewportView(epDescription);

        tabPane.addTab("tab2", spDescription);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lLanguage)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lTemplates)
                    .add(layout.createSequentialGroup()
                        .add(lExplandTemplateOn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbExpandTemplateOn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, tabPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                            .add(spTemplates, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(bRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(bNew, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                        .add(10, 10, 10)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lLanguage)
                    .add(cbLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lTemplates)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(bNew)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bRemove))
                    .add(spTemplates, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(tabPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lExplandTemplateOn)
                    .add(cbExpandTemplateOn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbLanguagePopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbLanguagePopupMenuWillBecomeVisible
        // TODO add your handling code here:
        languagePopupVisible = true;
    }//GEN-LAST:event_cbLanguagePopupMenuWillBecomeVisible

    private void cbLanguagePopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbLanguagePopupMenuWillBecomeInvisible
        // TODO add your handling code here:
        languagePopupVisible = false;
    }//GEN-LAST:event_cbLanguagePopupMenuWillBecomeInvisible
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNew;
    private javax.swing.JButton bRemove;
    private javax.swing.JComboBox cbExpandTemplateOn;
    private javax.swing.JComboBox cbLanguage;
    private javax.swing.JEditorPane epDescription;
    private javax.swing.JEditorPane epExpandedText;
    private javax.swing.JLabel lExplandTemplateOn;
    private javax.swing.JLabel lLanguage;
    private javax.swing.JLabel lTemplates;
    private javax.swing.JScrollPane spDescription;
    private javax.swing.JScrollPane spExpandedText;
    private javax.swing.JScrollPane spTemplates;
    private javax.swing.JTable tTemplates;
    private javax.swing.JTabbedPane tabPane;
    // End of variables declaration//GEN-END:variables
}
