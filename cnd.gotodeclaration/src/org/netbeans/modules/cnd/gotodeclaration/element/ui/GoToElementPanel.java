/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.gotodeclaration.element.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/* package */ class GoToElementPanel extends javax.swing.JPanel {
            
    private static final int BRIGHTER_COLOR_COMPONENT = 10;
    private ContentProvider contentProvider;
    private boolean containsScrollPane;
    private JLabel messageLabel;
    
    // Time when the serach stared (for debugging purposes)
    long time = -1;
    
    
    /** Creates new form GoToPanel */
    public GoToElementPanel( ContentProvider contentProvider ) throws IOException {
        this.contentProvider = contentProvider;
        initComponents();
        containsScrollPane = true;
                
        matchesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        // matchesList.addListSelectionListener(null);
        
        Color bgColorBrighter = new Color(
                                    Math.min(getBackground().getRed() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getGreen() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getBlue() + BRIGHTER_COLOR_COMPONENT, 255)
                            );
        
        messageLabel = new JLabel();
        messageLabel.setBackground(bgColorBrighter);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(true);
        messageLabel.setText(NbBundle.getMessage(GoToElementPanel.class, "TXT_NoElementsFound")); // NOI18N
        messageLabel.setFont(matchesList.getFont());
        
        matchesList.setCellRenderer( contentProvider.getListCellRenderer( matchesList ) );
        contentProvider.setListModel( this, null );
        
        PatternListener pl = new PatternListener( this );
        nameField.getDocument().addDocumentListener(pl);
        caseSensitive.setSelected(GoToElementOptions.GoToElementDialog.getCaseSensitive());
        caseSensitive.addItemListener(pl);
        matchesList.addListSelectionListener(pl);                       
                
    }
    
    /** Sets the model from different therad
     */
    public void setModel( final ListModel model ) { 
        // XXX measure time here
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               if (model.getSize() > 0 || getText() == null || getText().trim().length() == 0 ) {
                   matchesList.setModel(model);
                   matchesList.setSelectedIndex(0);
                   setListPanelContent(null);
                   if ( time != -1 ) {
                       if( GoToElementAction.TRACE ) System.err.printf("Real search time: %d ms", (System.currentTimeMillis() - time));
                       time = -1;
                   }
               }
               else {
                   setListPanelContent( NbBundle.getMessage(GoToElementPanel.class, "TXT_NoElementsFound")  ); // NOI18N
               }
           }
       });
    }
    
    /** Sets the initial text to find in case the user did not start typing yet. */
    public void setInitialText( final String text ) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                String textInField = nameField.getText();
                if ( textInField == null || textInField.trim().length() == 0 ) {
                    nameField.setText(text);
                    nameField.setCaretPosition(text.length());
                    nameField.setSelectionStart(0);
                    nameField.setSelectionEnd(text.length());
                }
            }
        });
    }
    
    public void openSelectedItem() {
        ElementDescriptor selectedValue = ((ElementDescriptor) matchesList.getSelectedValue());
        if ( selectedValue != null ) {
            ((ElementDescriptor) matchesList.getSelectedValue()).open();
        }
    }
            
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("deprecation")
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelText = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabelList = new javax.swing.JLabel();
        listPanel = new javax.swing.JPanel();
        matchesScrollPane1 = new javax.swing.JScrollPane();
        matchesList = new javax.swing.JList();
        caseSensitive = new javax.swing.JCheckBox();
        jLabelLocation = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setFocusable(false);
        setNextFocusableComponent(nameField);
        setLayout(new java.awt.GridBagLayout());

        jLabelText.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelText, org.openide.util.NbBundle.getMessage(GoToElementPanel.class, "TXT_GoToElement_ElementName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelText, gridBagConstraints);

        nameField.setFont(new java.awt.Font("Monospaced", 0, 12));
        nameField.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nameFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nameFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(nameField, gridBagConstraints);

        jLabelList.setLabelFor(matchesScrollPane1);
        jLabelList.setText(org.openide.util.NbBundle.getMessage(GoToElementPanel.class, "TXT_GoToElement_MatchesList_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelList, gridBagConstraints);

        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        listPanel.setName("dataPanel"); // NOI18N
        listPanel.setLayout(new java.awt.BorderLayout());

        matchesScrollPane1.setBorder(null);
        matchesScrollPane1.setFocusable(false);

        matchesList.setFont(new java.awt.Font("Monospaced", 0, 12));
        matchesList.setFocusable(false);
        matchesList.setVisibleRowCount(15);
        matchesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                matchesListMouseReleased(evt);
            }
        });
        matchesScrollPane1.setViewportView(matchesList);

        listPanel.add(matchesScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(listPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitive, org.openide.util.NbBundle.getMessage(GoToElementPanel.class, "TXT_GoToElement_CaseSensitive")); // NOI18N
        caseSensitive.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(caseSensitive, gridBagConstraints);

        jLabelLocation.setText(org.openide.util.NbBundle.getMessage(GoToElementPanel.class, "LBL_GoToElement_LocationJLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelLocation, gridBagConstraints);

        jTextFieldLocation.setEditable(false);
        jTextFieldLocation.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jTextFieldLocation, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void matchesListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchesListMouseReleased
        if ( evt.getClickCount() == 2 ) {
            nameFieldActionPerformed( null );
        }
    }//GEN-LAST:event_matchesListMouseReleased

    private void nameFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyTyped
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyTyped

    private void nameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyReleased
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyReleased

    private void nameFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyPressed
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyPressed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        if (contentProvider.hasValidContent()) {
            contentProvider.closeDialog();
            openSelectedItem();        
        }
    }//GEN-LAST:event_nameFieldActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox caseSensitive;
    private javax.swing.JLabel jLabelList;
    private javax.swing.JLabel jLabelLocation;
    private javax.swing.JLabel jLabelText;
    private javax.swing.JTextField jTextFieldLocation;
    private javax.swing.JPanel listPanel;
    private javax.swing.JList matchesList;
    private javax.swing.JScrollPane matchesScrollPane1;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables
        
    
    private String getText() {
        try {
            String text = nameField.getDocument().getText(0, nameField.getDocument().getLength());
            return text;
        }
        catch( BadLocationException ex ) {
            return null;
        }
    }
    
    public boolean isCaseSensitive () {
        return this.caseSensitive.isSelected();
    }
    
    void setListPanelContent( String message ) {
        
        if ( message == null && !containsScrollPane ) {
           listPanel.remove( messageLabel );
           listPanel.add( matchesScrollPane1 );
           containsScrollPane = true;
           revalidate();
           repaint();
        }        
        else if ( message != null ) { 
           jTextFieldLocation.setText(""); 
           messageLabel.setText(message);
           if ( containsScrollPane ) {
               listPanel.remove( matchesScrollPane1 );
               listPanel.add( messageLabel );
               containsScrollPane = false;
           }
           revalidate();
           repaint();
       }                
    }
    
    private String listActionFor(KeyEvent ev) {
        InputMap map = matchesList.getInputMap();
        Object o = map.get(KeyStroke.getKeyStrokeForEvent(ev));
        if (o instanceof String) {
            return (String)o;
        } else {
            return null;
        }
    }

    private boolean boundScrollingKey(KeyEvent ev) {
        String action = listActionFor(ev);
        // See BasicListUI, MetalLookAndFeel:
        return "selectPreviousRow".equals(action) || // NOI18N
        "selectNextRow".equals(action) || // NOI18N
        // "selectFirstRow".equals(action) || // NOI18N
        // "selectLastRow".equals(action) || // NOI18N
        "scrollUp".equals(action) || // NOI18N
        "scrollDown".equals(action); // NOI18N
    }

    private void delegateScrollingKey(KeyEvent ev) {
        String action = listActionFor(ev);
        
        // Wrap around
        if ( "selectNextRow".equals(action) && 
             matchesList.getSelectedIndex() == matchesList.getModel().getSize() -1 ) {
            matchesList.setSelectedIndex(0);
            matchesList.ensureIndexIsVisible(0);
            return;
        }
        else if ( "selectPreviousRow".equals(action) &&
                  matchesList.getSelectedIndex() == 0 ) {
            int last = matchesList.getModel().getSize() - 1;
            matchesList.setSelectedIndex(last);
            matchesList.ensureIndexIsVisible(last);
            return;
        }
        
        // Plain delegation        
        Action a = matchesList.getActionMap().get(action);
        if (a != null) {
            a.actionPerformed(new ActionEvent(matchesList, 0, action));
        }
    }
    
    private static class PatternListener implements DocumentListener, ItemListener, ListSelectionListener {
               
        private final GoToElementPanel dialog;
        
        
        PatternListener( GoToElementPanel dialog    ) {
            this.dialog = dialog;
        }
        
        PatternListener( DocumentEvent e, GoToElementPanel dialog    ) {
            this.dialog = dialog;
        }
        
        // DocumentListener ----------------------------------------------------
        
        public void changedUpdate( DocumentEvent e ) {
            update();
        }

        public void removeUpdate( DocumentEvent e ) {
            update();
        }

        public void insertUpdate( DocumentEvent e ) {
            update();
        }
        
        // Item Listener -------------------------------------------------------
        
        public void itemStateChanged (final ItemEvent e) {
            GoToElementOptions.GoToElementDialog.setCaseSensitive(dialog.isCaseSensitive());
            update();
        }
        
        // ListSelectionListener -----------------------------------------------
        
        public void valueChanged(ListSelectionEvent ev) {
            ElementDescriptor selectedValue = ((ElementDescriptor) dialog.matchesList.getSelectedValue());
            if ( selectedValue != null ) {
                String fileName = selectedValue.getAbsoluteFileName();
                dialog.jTextFieldLocation.setText((fileName == null) ? "" : fileName);
            }
            else {
                dialog.jTextFieldLocation.setText("");
            }
        }
        
        private void update() {
            dialog.time = System.currentTimeMillis();
            dialog.setListPanelContent(NbBundle.getMessage(GoToElementPanel.class, "TXT_Searching")); // NOI18N
            dialog.contentProvider.setListModel(dialog, dialog.getText());            
        }
                                         
    }
             
    
    public static interface ContentProvider {
        
        public ListCellRenderer getListCellRenderer( JList list );
        
        public void setListModel( GoToElementPanel panel, String text    );
        
        public void closeDialog();
        
        public boolean hasValidContent ();
                
    }
    
}
