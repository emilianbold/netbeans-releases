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

package org.netbeans.license;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.netbeans.util.Util;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
/**
 * Displays LicensePanel to user. User must accept license to continue. 
 * if user does not accept license UserCancelException is thrown.
 *
 * @author  Marek Slama
 */

public final class AcceptLicense {
    
    private static JDialog d;
    private static String command;
    
    /** If License was not accepted during installation user must accept it here. 
     */
    public static void showLicensePanel () throws Exception {
        Util.setDefaultLookAndFeel();
        URL url = AcceptLicense.class.getResource("LICENSE.txt"); // NOI18N
        LicensePanel licensePanel = new LicensePanel(url);
        ResourceBundle bundle = NbBundle.getBundle(AcceptLicense.class);
        String yesLabel = bundle.getString("MSG_LicenseYesButton");
        String noLabel = bundle.getString("MSG_LicenseNoButton");
        JButton yesButton = new JButton();
        JButton noButton = new JButton();
        setLocalizedText(yesButton,yesLabel);
        setLocalizedText(noButton,noLabel);
        ActionListener listener = new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                command = e.getActionCommand();
                d.setVisible(false);
                d = null;
            }            
        };
        yesButton.addActionListener(listener);
        noButton.addActionListener(listener);
        
        yesButton.setActionCommand("yes"); // NOI18N
        noButton.setActionCommand("no"); // NOI18N
        
        yesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_AcceptButton"));
        yesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_AcceptButton"));
        
        noButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_RejectButton"));
        noButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_RejectButton"));
        
        Dimension yesPF = yesButton.getPreferredSize();
        Dimension noPF = noButton.getPreferredSize();
        int maxWidth = Math.max(yesButton.getPreferredSize().width, noButton.getPreferredSize().width);
        int maxHeight = Math.max(yesButton.getPreferredSize().height, noButton.getPreferredSize().height);
        yesButton.setPreferredSize(new Dimension(maxWidth, maxHeight));
        noButton.setPreferredSize(new Dimension(maxWidth, maxHeight));
        
        d = new JDialog((Frame) null,bundle.getString("MSG_LicenseDlgTitle"),true);
        
        d.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_LicenseDlg"));
        d.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_LicenseDlg"));
        
        d.getContentPane().add(licensePanel,BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(17,12,11,11));
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        d.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
        d.setSize(new Dimension(600,600));
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setModal(true);
        d.setResizable(true);
        //Center on screen
        d.setLocationRelativeTo(null);
        d.setVisible(true);
        
        if ("yes".equals(command)) {  // NOI18N
            return;
        } else {
            throw new org.openide.util.UserCancelException();
        }
    }
    
    /**
     * Actual setter of the text & mnemonics for the AbstractButton or
     * their subclasses. We must copy necessary code from org.openide.awt.Mnemonics
     * because org.openide.awt module is not available yet when this code is called.
     * @param item AbstractButton
     * @param text new label
     */
    private static void setLocalizedText (AbstractButton button, String text) {
        if (text == null) {
            button.setText(null);
            return;
        }

        int i = findMnemonicAmpersand(text);

        if (i < 0) {
            // no '&' - don't set the mnemonic
            button.setText(text);
            button.setMnemonic(0);
        } else {
            button.setText(text.substring(0, i) + text.substring(i + 1));
            
            if (Utilities.isMac()) {
                // there shall be no mnemonics on macosx.
                //#55864
                return;
            }

            char ch = text.charAt(i + 1);

            // it's latin character or arabic digit,
            // setting it as mnemonics
            button.setMnemonic(ch);

            // If it's something like "Save &As", we need to set another
            // mnemonic index (at least under 1.4 or later)
            // see #29676
            button.setDisplayedMnemonicIndex(i);
        }
    }
    
    /**
     * Searches for an ampersand in a string which indicates a mnemonic.
     * Recognizes the following cases:
     * <ul>
     * <li>"Drag & Drop", "Ampersand ('&')" - don't have mnemonic ampersand.
     *      "&" is not found before " " (space), or if enclosed in "'"
     *     (single quotation marks).
     * <li>"&File", "Save &As..." - do have mnemonic ampersand.
     * <li>"Rock & Ro&ll", "Underline the '&' &character" - also do have
     *      mnemonic ampersand, but the second one.
     * </ul>
     * @param text text to search
     * @return the position of mnemonic ampersand in text, or -1 if there is none
     */
    public static int findMnemonicAmpersand(String text) {
        int i = -1;

        do {
            // searching for the next ampersand
            i = text.indexOf('&', i + 1);

            if ((i >= 0) && ((i + 1) < text.length())) {
                // before ' '
                if (text.charAt(i + 1) == ' ') {
                    continue;

                    // before ', and after '
                } else if ((text.charAt(i + 1) == '\'') && (i > 0) && (text.charAt(i - 1) == '\'')) {
                    continue;
                }

                // ampersand is marking mnemonics
                return i;
            }
        } while (i >= 0);

        return -1;
    }
}
