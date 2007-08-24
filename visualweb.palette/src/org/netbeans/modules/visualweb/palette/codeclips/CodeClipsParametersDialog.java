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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CodeClipParametersDialog.java
 *
 * Created on July 27, 2006, 10:16 AM
 *
 * The dialog box for codclip parameters
 *
 * @author  jhoff & octav
 * @author Joelle Lam <joelle.lam@sun.com>
 * @version %I%, %G%
 */
package org.netbeans.modules.visualweb.palette.codeclips;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.NotifyDescriptor;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;


public class CodeClipsParametersDialog extends JPanel {

    /** UI properties
     */
    /** ToDo - remove this restriction and make newParam/paramArray Vectors
     */
    //private final int MAX_PARAMS = 3;

    private boolean cancelled = false;
    private java.awt.Dialog dialog;
    private DialogDescriptor dlg;
    private java.awt.GridBagConstraints gbc;
    private Vector<String> newParam = new Vector<String>();
    private Vector<String> paramArr = new Vector<String>();
    private ArrayList<JTextField> list = new ArrayList<JTextField>();
    private Component[] components;

    /** Creates new form CodeClipsParameters */
    public CodeClipsParametersDialog(String clipName, Vector<String> paramArray) {

        //this.paramArray = paramArray;
        this.paramArr = paramArray;
        init();
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object o = evt.getSource();
                if (o == NotifyDescriptor.CANCEL_OPTION) {
                    cancelled = true;

                } else if (o == NotifyDescriptor.OK_OPTION) {
                    for (int i=0; i< list.size(); i++) {
                        String str = ((JTextField)list.get(i)).getText();
                        newParam.add(str);
                    }

                    // warnOnEmptyString(newParam.elementAt(0).toString());
                }
            }
        };

        String title = NbBundle.getMessage(CodeClipsParametersDialog.class, "PARAMETERS") + " <" + clipName + ">"; // NOI18N
        dlg = new DialogDescriptor(this,
                title,
                true, listener);
        // disable "Ok" button until user changes the old string
        dlg.setValid(true);


        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setVisible(true);
        getAccessibleContext().setAccessibleName(title);
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeClipsParametersDialog.class,
                "ACSD_NavigationView_CodeClipsParametersDialogDesc"));

    }


    /**
     *
     */
    private void init() {
        final JPanel panel = new JPanel();
        final JPanel panel2 = new JPanel();
        setLayout(new java.awt.GridBagLayout());
        panel2.setLayout(new java.awt.GridLayout(1,0,5,0));
        gbc = new java.awt.GridBagConstraints();
        panel.setLayout(new java.awt.GridBagLayout());
        gbc = new java.awt.GridBagConstraints();
        gbc.insets = new Insets(2,2,2,2);
        int number = paramArr.size();
        addComponents(number, panel, gbc);
        add(panel,gbc);
        add(panel2,gbc);
        setVisible(true);
        setLocation(200,200);
        components = panel.getComponents();
//        setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
    }



    private void addComponents( int n, JPanel panel, GridBagConstraints gbc) {
        for (int i =0; i < n ; i++) {
            JLabel label = new JLabel(paramArr.elementAt(i).replace("_"," "));
           // JTextField tf = new JTextField(paramArr.elementAt(i), 16);
            JTextField tf = new JTextField("", 16);
            tf.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeClipsParametersDialog.class, "ACSD_ParameterName") + paramArr.elementAt(i));
            tf.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeClipsParametersDialog.class, "ACSD_ParameterDesc") + paramArr.elementAt(i));
            gbc.gridwidth = gbc.RELATIVE;
            gbc.anchor = gbc.EAST;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            panel.add(label, gbc);
            gbc.gridwidth = gbc.REMAINDER;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gbc.anchor = gbc.WEST;
            gbc.weightx = 1.0;
            //gbc.insets = new java.awt.Insets(10,0,0,10);
            panel.add(tf, gbc);
            list.add(tf);
            panel.revalidate();
            panel.repaint();
        }
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * @return The array of params edited, some of the entries can be null or ""
     */
    public Vector getNewParam(){
        return newParam;
    }
    
    private void warnOnEmptyString(String name) {
        if ("".equals(name)) {
            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(CodeClipsParametersDialog.class, "NO_EMPTY_STRING"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    private class ParamNameChangeListener implements DocumentListener {
        
        public void insertUpdate(DocumentEvent e) {
            enableOkButton();
        }
        
        public void removeUpdate(DocumentEvent e) {
            enableOkButton();
        }
        
        public void changedUpdate(DocumentEvent e) {
            enableOkButton();
        }
        
        public void enableOkButton() {
            dlg.setValid(true);
        }
    }
}
