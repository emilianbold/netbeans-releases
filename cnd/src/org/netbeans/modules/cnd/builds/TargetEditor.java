/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.builds;

import java.util.Vector;
import java.util.ResourceBundle;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.Timer;
import javax.accessibility.AccessibleContext;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

public class TargetEditor extends javax.swing.JPanel implements PropertyChangeListener {
    private JList targetList = null;
    private Vector listData = new Vector();
    private Timer isVisibleTimer;
    private PropertyEditorSupport editor;
    
    private JDialog dialog = null;
    private JButton okButton;
    private JButton cancelButton;
    private int returnValue = 0;
    public final static int OK_OPTION = 0;
    public final static int CANCEL_OPTION = 1;
    private ResourceBundle bundle = NbBundle.getBundle(TargetEditor.class);

    
    public TargetEditor(String[] targets, PropertyEditorSupport editor, PropertyEnv env) {
        this.editor = editor;
        initComponents();
        addButton.setMnemonic(bundle.getString("TARGET_EDITOR_ADD_BUTTON_MNEMONIC").toCharArray()[0]);
        editButton.setMnemonic(bundle.getString("TARGET_EDITOR_CHANGE_BUTTON_MNEMONIC").toCharArray()[0]);
        removeButton.setMnemonic(bundle.getString("TARGET_EDITOR_REMOVE_BUTTON_MNEMONIC").toCharArray()[0]);
        upButton.setMnemonic(bundle.getString("TARGET_EDITOR_UP_BUTTON_MNEMONIC").toCharArray()[0]);
        downButton.setMnemonic(bundle.getString("TARGET_EDITOR_DOWN_BUTTON_MNEMONIC").toCharArray()[0]);
 
        for (int i = 0; i < targets.length; i++)
            listData.add(targets[i]);
        targetList = new JList();
        targetList.setListData(listData);
        targetList.addListSelectionListener(new TargetSelectionListener());
        targetList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                targetListKeyPressed(evt);
            }
        });
        scrollPane.setViewportView(targetList);
        listLabel.setLabelFor(targetList);
        checkSelection();

	initAccessibility();
        
        isVisibleTimer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (addButton.isVisible()) {
		    if (targetList.getModel().getSize() > 0) {
			targetList.setSelectedIndex(0);
                        targetList.requestFocus();
		    }
		    else {
                        addButton.requestFocus();
		    }
                    isVisibleTimer.stop();
                }
            }
        });
	isVisibleTimer.start();
        
        if (env != null) {
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addPropertyChangeListener(this);
        }
    }

    private void initAccessibility() {
        AccessibleContext context;

	context = getAccessibleContext();
	context.setAccessibleName(bundle.getString("ACSN_TARGET_EDITOR"));
	context.setAccessibleDescription(bundle.getString("ACSD_TARGET_EDITOR"));

	context = targetList.getAccessibleContext();
	context.setAccessibleName(bundle.getString("ACSN_TARGET_LIST"));
	context.setAccessibleDescription(bundle.getString("ACSD_TARGET_LIST"));

	context = scrollPane.getAccessibleContext();
	context.setAccessibleName(bundle.getString("ACSN_TARGET_LIST"));
	context.setAccessibleDescription(bundle.getString("ACSD_TARGET_LIST"));

	context = scrollPane.getHorizontalScrollBar().getAccessibleContext();
	context.setAccessibleName(bundle.getString("ACSN_TARGET_LIST"));
	context.setAccessibleDescription(bundle.getString("ACSD_TARGET_LIST"));

	context = scrollPane.getVerticalScrollBar().getAccessibleContext();
	context.setAccessibleName(bundle.getString("ACSN_TARGET_LIST"));
	context.setAccessibleDescription(bundle.getString("ACSD_TARGET_LIST"));

	addButton.getAccessibleContext().setAccessibleDescription(addButton.getText());
	editButton.getAccessibleContext().setAccessibleDescription(editButton.getText());
	removeButton.getAccessibleContext().setAccessibleDescription(removeButton.getText());
	upButton.getAccessibleContext().setAccessibleDescription(upButton.getText());
	downButton.getAccessibleContext().setAccessibleDescription(downButton.getText());
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        dataPanel = new javax.swing.JPanel();
        listLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        controlsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        setRequestFocusEnabled(false);
        dataPanel.setLayout(new java.awt.GridBagLayout());

        dataPanel.setRequestFocusEnabled(false);
        listLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/builds/Bundle").getString("TARGET_EDITOR_LIST_MNEMONIC").charAt(0));
        listLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/builds/Bundle").getString("TARGET_EDITOR_LIST_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        dataPanel.add(listLabel, gridBagConstraints);

        scrollPane.setBackground(new java.awt.Color(255, 255, 255));
        scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        dataPanel.add(scrollPane, gridBagConstraints);

        controlsPanel.setLayout(new java.awt.GridBagLayout());

        controlsPanel.setRequestFocusEnabled(false);
        addButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/builds/Bundle").getString("TARGET_EDITOR_ADD_BUTTON_LBL"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        controlsPanel.add(addButton, gridBagConstraints);

        editButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/builds/Bundle").getString("TARGET_EDITOR_CHANGE_BUTTON_LBL"));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        controlsPanel.add(editButton, gridBagConstraints);

        removeButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/builds/Bundle").getString("TARGET_EDITOR_REMOVE_BUTTON_LBL"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        controlsPanel.add(removeButton, gridBagConstraints);

        upButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/builds/Bundle").getString("TARGET_EDITOR_UP_BUTTON_LBL"));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        controlsPanel.add(upButton, gridBagConstraints);

        downButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/builds/Bundle").getString("TARGET_EDITOR_DOWN_BUTTON_LBL"));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        controlsPanel.add(downButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 10);
        dataPanel.add(controlsPanel, gridBagConstraints);

        add(dataPanel, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void targetListKeyPressed(java.awt.event.KeyEvent evt) {
        // Add your handling code here:
        processKeyEvent(evt);
    }

    private void handleEscape(java.awt.event.KeyEvent evt) {
        // Add your handling code here:
        if (evt.isConsumed())
            return;
        if (evt.getKeyChar() == KeyEvent.VK_ESCAPE) {
            evt.consume();
            closeAction(dialog, CANCEL_OPTION);
        }
    }

    private void editAction() {
        NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine("", bundle.getString("TARGET_DIALOG_TITLE")); // NOI18N
        int selectedIndex = targetList.getSelectedIndex();
	notifyDescriptor.setInputText((String)listData.elementAt(selectedIndex));
	DialogDisplayer.getDefault().notify(notifyDescriptor);
	if (notifyDescriptor.getValue() == NotifyDescriptor.OK_OPTION && notifyDescriptor.getInputText().length() > 0) {
	    Object tmp = listData.elementAt(selectedIndex);
	    listData.removeElementAt(selectedIndex);
	    listData.add(selectedIndex, notifyDescriptor.getInputText());
	    targetList.setListData(listData);
            targetList.setSelectedIndex(selectedIndex);
	}
        checkSelection();
	if (dialog != null)
	    dialog.setVisible(true); // to retain focus
	editButton.requestFocus();
    }
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // Add your handling code here:
        editAction();
    }//GEN-LAST:event_editButtonActionPerformed

    private void downAction() {
        int selectedIndex = targetList.getSelectedIndex();
        if (selectedIndex < 0)
            return;
        if (selectedIndex >= (listData.size()-1))
            return;
        Object tmp = listData.elementAt(selectedIndex);
        listData.removeElementAt(selectedIndex);
        listData.add(++selectedIndex, tmp);
        targetList.setListData(listData);
        if (selectedIndex >= 0) {
            targetList.ensureIndexIsVisible(selectedIndex);
            targetList.setSelectedIndex(selectedIndex);
        }
        checkSelection();
	if (dialog != null)
	    dialog.setVisible(true); // to retain focus
	if (downButton.isEnabled()) 
	    downButton.requestFocus();
	else
	    upButton.requestFocus();
    }
    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        // Add your handling code here:
        downAction();
    }//GEN-LAST:event_downButtonActionPerformed

    private void upAction() {
        int selectedIndex = targetList.getSelectedIndex();
        if (selectedIndex <= 0)
            return;
        Object tmp = listData.elementAt(selectedIndex);
        listData.removeElementAt(selectedIndex);
        listData.add(--selectedIndex, tmp);
        targetList.setListData(listData);
        if (selectedIndex >= 0) {
            targetList.ensureIndexIsVisible(selectedIndex);
            targetList.setSelectedIndex(selectedIndex);
        }
        checkSelection();
	if (dialog != null)
	    dialog.setVisible(true); // to retain focus
	if (upButton.isEnabled()) 
	    upButton.requestFocus();
	else
	    downButton.requestFocus();
    }
    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        // Add your handling code here:
        upAction();
    }//GEN-LAST:event_upButtonActionPerformed

    private void removeAction() {
        int selectedIndex = targetList.getSelectedIndex();
        if (selectedIndex < 0)
            return;
        listData.removeElementAt(selectedIndex);
        targetList.setListData(listData);
        selectedIndex = (selectedIndex >= listData.size()) ? selectedIndex-1 : selectedIndex;
        if (selectedIndex >= 0) {
            targetList.ensureIndexIsVisible(selectedIndex);
            targetList.setSelectedIndex(selectedIndex);
        }
        checkSelection();
	if (dialog != null)
	    dialog.setVisible(true); // to retain focus
	if (removeButton.isEnabled()) 
	    removeButton.requestFocus();
	else
	    addButton.requestFocus();
    }
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        removeAction();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addAction() {
        NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine("", bundle.getString("TARGET_DIALOG_TITLE")); // NOI18N
	int addAtIndex = 0;
        int selectedIndex = targetList.getSelectedIndex();
	if (selectedIndex >= 0 && selectedIndex <= listData.size()-1)
	    addAtIndex = selectedIndex+1;
	DialogDisplayer.getDefault().notify(notifyDescriptor);
	if (notifyDescriptor.getValue() == NotifyDescriptor.OK_OPTION && notifyDescriptor.getInputText() != null && notifyDescriptor.getInputText().length() > 0) {
	    listData.add(addAtIndex, notifyDescriptor.getInputText());
	    targetList.setListData(listData);
            targetList.setSelectedIndex(addAtIndex);
	}
	addButton.requestFocus();

        checkSelection();
	if (dialog != null)
	    dialog.setVisible(true); // to retain focus
	addButton.requestFocus();
    }
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        addAction();
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JButton downButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel listLabel;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
    
    private void checkSelection() {
        int i = targetList.getSelectedIndex();
        if (i >= 0) {
            addButton.setEnabled(true);
            removeButton.setEnabled(true);
            editButton.setEnabled(true);
            if (i == 0)
                upButton.setEnabled(false);
            else
                upButton.setEnabled(true);
            if (i >= listData.size()-1)
                downButton.setEnabled(false);
            else
                downButton.setEnabled(true);
        }
        else {
            addButton.setEnabled(true);
            removeButton.setEnabled(false);
            editButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }
    
    private class TargetSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            checkSelection();
        }
    }
    
    public String getTargets() {
        String targets = null;
        for (int i = 0; i < targetList.getModel().getSize(); i++) {
            if (i == 0)
                targets = (String)targetList.getModel().getElementAt(0);
            else
                targets = targets + ", " + targetList.getModel().getElementAt(i); // NOI18N
        }
        return targets;
    }

    private Object getPropertyValue () throws IllegalStateException {
        return getTargets();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    private void closeAction(JDialog dialog, int ret) {
	if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            returnValue = ret;
	}
    }
    
    public int showOpenDialog(JFrame parent) {
        //Frame frame = parent instanceof Frame ? (Frame) parent : (Frame)SwingUtilities.getAncestorOfClass(Frame.class, parent);
        dialog = new JDialog(parent, true);
        dialog.getContentPane().add(this);
        dialog.setTitle(bundle.getString("TARGET_EDITOR_TITLE"));
	AccessibleContext context = dialog.getAccessibleContext();
	context.setAccessibleName(bundle.getString("ACSN_TARGET_EDITOR"));
	context.setAccessibleDescription(bundle.getString("ACSD_TARGET_EDITOR"));

	java.awt.event.KeyAdapter keyListener = new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
		handleEscape(evt);
            }
        };
        
        java.awt.GridBagConstraints gridBagConstraints;
        JPanel buttonPanel = new JPanel();
        okButton = new JButton();
        okButton.setText(bundle.getString("TARGET_EDITOR_OK_BUTTON_LBL"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAction(dialog, OK_OPTION);
            }
        });
	okButton.getAccessibleContext().setAccessibleDescription(okButton.getText());
	getRootPane().setDefaultButton(okButton);

        cancelButton = new JButton();
        cancelButton.setText(bundle.getString("TARGET_EDITOR_CANCEL_BUTTON_LBL"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAction(dialog, CANCEL_OPTION);
            }
        });
	cancelButton.getAccessibleContext().setAccessibleDescription(cancelButton.getText());

	// Handle escape key press (is this really necessary to do per button ???? FIXUP
        okButton.addKeyListener(keyListener);
        cancelButton.addKeyListener(keyListener);
        addButton.addKeyListener(keyListener);
        editButton.addKeyListener(keyListener);
        removeButton.addKeyListener(keyListener);
        upButton.addKeyListener(keyListener);
        downButton.addKeyListener(keyListener);
        addKeyListener(keyListener);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(okButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 10, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        dataPanel.add(buttonPanel, gridBagConstraints);
//---
	okButton.setPreferredSize(cancelButton.getPreferredSize());

        dialog.pack();
	dialog.setLocation(findScreenCenter(dialog));
        dialog.setVisible(true);
        return returnValue;
    }

    private Point findScreenCenter(JDialog dialog) {
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	int x = toolkit.getScreenSize().width/2 - dialog.getHeight()/2;
	int y = toolkit.getScreenSize().height/2 - dialog.getWidth()/2;
	return new Point(x, y);
    }
}
