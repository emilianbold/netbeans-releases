/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.openfile;


import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 * Panel offering mounting points to user, when opening .java file.
 */
// PENDING This all class has to be refactored using form.
public class PackagePanel extends javax.swing.JPanel {

    private File f;
    
    private int pkgLevel;
    
    private Vector dirs;
    
    private Vector pkgs;
    
    /** Creates new form PackagePanel */
    public PackagePanel(File f, int pkgLevel, Vector dirs, Vector pkgs) {
        this.f = f;
        this.pkgLevel = pkgLevel;
        this.dirs = dirs;
        this.pkgs = pkgs;
        
        initComponents2();
    }
    
    
    JButton getOKButton() {
        return okButton;
    }
    
    JButton getCancelButton() {
        return cancelButton;
    }
    
    JList getList() {
        return list;
    }

    /** */
    private void initComponents2() {
        okButton = new JButton (SettingsBeanInfo.getString ("LBL_okButton"));
        cancelButton = new JButton (SettingsBeanInfo.getString ("LBL_cancelButton"));
        list = new JList(pkgs);
        
        setLayout (new BorderLayout (0, 5));
        setBorder (new javax.swing.border.EmptyBorder (8, 8, 8, 8));

        JTextArea textArea = new JTextArea ();
        textArea.setBackground (Color.lightGray);
        textArea.setFont (new Font ("SansSerif", Font.PLAIN, 11)); // NOI18N
        textArea.setText (SettingsBeanInfo.getString (pkgLevel == -1 ? "TXT_whereMountNoSuggest" : "TXT_whereMountSuggest", f.getName ()));
        textArea.setEditable (false);
        textArea.setLineWrap (true);
        textArea.setWrapStyleWord (true);
        add (textArea, BorderLayout.NORTH);

        list.setVisibleRowCount (5);
        list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        if (pkgLevel != -1) list.setSelectedIndex (pkgLevel);
        list.setCellRenderer (new ListCellRenderer () {
            private Icon folderIcon = new ImageIcon (OpenFile.class.getResource ("folder.gif")); // NOI18N
            private Icon rootFolderIcon = new ImageIcon (OpenFile.class.getResource ("rootFolder.gif")); // NOI18N
            private final JLabel lab = new JLabel();
            
            public Component getListCellRendererComponent (JList lst, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String pkg2 = (String) value;
//                JLabel lab = new JLabel (); // TEMP
                if (pkg2.equals ("")) { // NOI18N
                    lab.setText (SettingsBeanInfo.getString ("LBL_packageWillBeDefault"));
                    lab.setIcon (rootFolderIcon);
                } else {
                    lab.setText (SettingsBeanInfo.getString ("LBL_packageWillBe", pkg2));
                    lab.setIcon (folderIcon);
                }
                if (isSelected) {
                    lab.setBackground (lst.getSelectionBackground ());
                    lab.setForeground (lst.getSelectionForeground ());
                } else {
                    lab.setBackground (lst.getBackground ());
                    lab.setForeground (lst.getForeground ());
                }
                lab.setEnabled (lst.isEnabled ());
                lab.setFont (lst.getFont ());
                lab.setOpaque (true);
                return lab;
            }
        });
        add (new JScrollPane (list), BorderLayout.CENTER);

        // Name of mount point:
        final JLabel label = new JLabel ();
        label.setFont (new Font ("Monospaced", Font.PLAIN, 12)); // NOI18N
        add (label, BorderLayout.SOUTH);
        setPreferredSize (new Dimension (450, 300));

        list.addListSelectionListener (new ListSelectionListener () {
                                           public void valueChanged (ListSelectionEvent ev) {
                                               updateLabelEtcFromList (label, list, dirs, okButton);
                                           }
                                       });
        updateLabelEtcFromList (label, list, dirs, okButton);
    }

    private static void updateLabelEtcFromList (JLabel label, JList list, Vector dirs, JButton okButton) {
        int idx = list.getSelectedIndex ();
        if (idx == -1) {
            label.setText (" "); // NOI18N
            okButton.setEnabled (false);
        } else {
            File dir = (File) dirs.elementAt (idx);
            label.setText (SettingsBeanInfo.getString ("LBL_dirWillBe", dir.getAbsolutePath ()));
            okButton.setEnabled (true);
        }
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        setLayout(new java.awt.BorderLayout());
    }//GEN-END:initComponents


    private JButton okButton;
    private JButton cancelButton;
    private JList list;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
