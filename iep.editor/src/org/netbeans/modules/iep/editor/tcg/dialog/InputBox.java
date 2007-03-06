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

package org.netbeans.modules.iep.editor.tcg.dialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;

/**
 *  DOCUMENT ME!
 *
 * @author    Bing Lu
 */
public class InputBox
         implements OKCancelDialogInterface, ActionListener {

    
    private String mMsg;

    private String mTitle;

    private JTextField mTextBox;

    private boolean mOk = false;

    private JPanel mPanel; 

    /**
     *  DOCUMENT ME!
     *
     * @param  title  The title
     * @param  msg    The message
     */
    public InputBox(String title, String msg, JTextFieldFilter filer) {
        mMsg = msg;
        mTitle = title;
        mPanel = new JPanel();
        mPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        mTextBox = new JTextField();
        int pH = (int)mTextBox.getPreferredSize().getHeight();
        mTextBox.setPreferredSize(new Dimension(180, pH));
        mTextBox.setDocument(filer);
        mPanel.add(mTextBox, c);
        if (msg != null && !msg.trim().equals("")) {
            c.gridy = 1;
            c.insets.top = 0;
            JLabel label = new JLabel(mMsg);
            mPanel.add(label, c);
        }
    }

    public InputBox(String title, String msg) {
        this(title, msg, JTextFieldFilter.newAlphaNumeric());
    }

    /**
     *  DOCUMENT ME!
     *
     * @return    The actionListener value
     */
    public ActionListener getActionListener() {
        return this;
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The innerPane value
     */
    public Component getInnerPane() {
        return mPanel;
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The ok value
     */
    public boolean isOk() {
        return mOk;
    }


    /**
     *  gets the text
     *
     * @return    the text.
     */
    public String getText() {
        return mTextBox.getText();
    }


    /**
     *  DOCUMENT ME!
     *
     * @return    The title value
     */
    public String getTitle() {
        return mTitle;
    }


    /**
     *  DOCUMENT ME!
     *
     * @param  e  the action event
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equals("OK")) {
            mOk = true;

        } else {
            mOk = false;
        }
    }
}

