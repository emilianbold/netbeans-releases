/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.output2;

import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Vector;

class FindDialogPanel extends javax.swing.JPanel implements Runnable {

    static final long serialVersionUID =5048678953767663114L;

    private static Reference panel = null;
    private JButton acceptButton;
    private static Vector history = new Vector();
    
    /** Initializes the Form */
    FindDialogPanel() {
        initComponents ();
        getAccessibleContext().setAccessibleName(NbBundle.getBundle(FindDialogPanel.class).getString("ACSN_Find"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FindDialogPanel.class).getString("ACSD_Find"));
        findWhat.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FindDialogPanel.class).getString("ACSD_Find_What"));
        findWhat.getEditor().getEditorComponent().addKeyListener( new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                if ( evt.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                    findWhat.setPopupVisible( false );
                    ((Dialog)dialogRef.get()).setVisible( false );
                }
            }
        });
        findWhat.getEditor().addActionListener( new java.awt.event.ActionListener() {
               public void actionPerformed( java.awt.event.ActionEvent evt ) {
                   //Give the component a chance to update its text field, or on
                   //first invocation the text will be null
                   SwingUtilities.invokeLater (FindDialogPanel.this);
               }
           });

       acceptButton = new JButton( NbBundle.getBundle(FindDialogPanel.class).getString("BTN_Find") );
       acceptButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FindDialogPanel.class).getString("ACSD_FindBTN"));

       findWhat.setModel( new DefaultComboBoxModel( history ) );
       findWhatLabel.setFocusable(false);

       JComponent[] order = new JComponent[] {
           findWhat, acceptButton
       };

       setFocusCycleRoot(true);
       setFocusTraversalPolicy(new FdFocusTraversalPolicy(order));
    }

    public void run() {
        acceptButton.doClick();
    }

    public static FindDialogPanel getPanel() {
        FindDialogPanel result = null;
        if (panel != null) {
            result = (FindDialogPanel) panel.get();
        }
        if (result == null) {
            result = new FindDialogPanel();
            panel = new SoftReference (result);
        }
        return result;
    }

    private class FdFocusTraversalPolicy extends FocusTraversalPolicy {
        private JComponent[] order;
        FdFocusTraversalPolicy (JComponent[] order) {
            this.order = order;
        }

        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            int idx = Arrays.asList (order).indexOf(aComponent);
            if (idx == -1) {
                for (int i=0; i < order.length; i++) { //Combo box editor
                    if (order[i].isAncestorOf(aComponent)) {
                        idx = i == order.length -1 ? 0 : i+1;
                        break;
                    }
                }
                if (idx == -1) {
                    idx = 0;
                }
            } else {
                if (idx == order.length -1) {
                    idx = 0;
                } else {
                    idx++;
                }
            }
            return order[idx];
        }


        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            int idx = Arrays.asList (order).indexOf(aComponent);
            if (idx == -1) {
                for (int i=0; i < order.length; i++) { //Combo box editor
                    if (order[i].isAncestorOf(aComponent)) {
                        idx = i == 0 ? order.length-1 : i-1;
                        break;
                    }
                }
                if (idx == -1) {
                    idx = 0;
                }
            } else {
                if (idx == 0) {
                    idx = order.length-1;
                } else {
                    idx--;
                }
            }
            return order[idx];
        }

        public Component getFirstComponent(Container focusCycleRoot) {
            return findWhat;
        }

        public Component getLastComponent(Container focusCycleRoot) {
            return acceptButton;
        }

        public Component getDefaultComponent(Container focusCycleRoot) {
            return findWhat;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        findWhatLabel = new javax.swing.JLabel();
        findWhat = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        findWhatLabel.setDisplayedMnemonic(NbBundle.getBundle(FindDialogPanel.class).getString("LBL_Find_What_Mnemonic").charAt(0));
        findWhatLabel.setLabelFor(findWhat);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
        add(findWhatLabel, gridBagConstraints);

        findWhat.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 12);
        add(findWhat, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox findWhat;
    private javax.swing.JLabel findWhatLabel;
    // End of variables declaration//GEN-END:variables



    static void showFindDialog(ActionListener al) {
        java.awt.Dialog dialog = getDialog();
        FindDialogPanel findPanel = getPanel();
        findPanel.acceptButton.putClientProperty ("panel", findPanel);

        if (!Arrays.asList(findPanel.acceptButton.getActionListeners()).contains(al)) {
            findPanel.acceptButton.addActionListener(al);
        }
        dialog.setVisible(true);

        dialog.addWindowListener (new DlgWindowListener(al, findPanel.acceptButton));
    }
    
    private static class DlgWindowListener extends WindowAdapter {
        private ActionListener al;
        private JButton acceptButton;
        DlgWindowListener (ActionListener al, JButton acceptButton) {
            this.al = al;
            this.acceptButton = acceptButton;
        }
        
        public void windowClosed(java.awt.event.WindowEvent windowEvent) {
            acceptButton.removeActionListener (al);
            ((Dialog) windowEvent.getSource()).removeWindowListener(this);
            ((Dialog) windowEvent.getSource()).dispose();
        }
    }

    String getPattern() {
        FindDialogPanel findPanel = (FindDialogPanel) panel.get();
        return findPanel != null ? (String) findPanel.findWhat.getSelectedItem() : null;
    }
    
    private void updateHistory() {
        Object pattern = findWhat.getEditor().getItem();

        history.add( 0, pattern );
        for ( int i = history.size() - 1; i > 0; i-- ) {
            if ( history.get( i ).equals( pattern ) ) {
                history.remove( i );
                break;
            }
        }
    }

    private static Dialog createDialog() {
        final FindDialogPanel findPanel = getPanel();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                               findPanel,
                               NbBundle.getBundle(FindDialogPanel.class).getString("LBL_Find_Title"),
                               true,                                                 // Modal
                               new Object[] { findPanel.acceptButton, DialogDescriptor.CANCEL_OPTION }, // Option lineStartList
                               findPanel.acceptButton,                                         // Default
                               DialogDescriptor.RIGHT_ALIGN,                        // Align
                               null,                                                 // Help
                               new java.awt.event.ActionListener() {
                                   public void actionPerformed( java.awt.event.ActionEvent evt ) {
                                       if ( evt.getSource() == findPanel.acceptButton ) {
                                           getPanel().updateHistory();
                                       }
                                       else

                                       getDialog().setVisible( false );
                                   }
                               });
        Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        dialog.setFocusTraversalPolicy(findPanel.getFocusTraversalPolicy());

        dialog.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(FindDialogPanel.class, "ACSN_Find")); //NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(FindDialogPanel.class, "ACSD_Find")); //NOI18N
        return dialog;
    }

    private static Reference dialogRef = null;
    private static Dialog getDialog() {
        Dialog result = null;
        if (dialogRef != null) {
            result = (Dialog) dialogRef.get();
        }
        if (result == null) {
            result = createDialog();
            dialogRef = new WeakReference(result);
        }
        return result;
    }

}
