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

package org.netbeans.modules.mobility.licensing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInstall;

//import org.netbeans.util.Util;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.Utilities;
/**
 * Displays LicensePanel to user. User must accept license to continue.
 * if user does not accept license IllegalStateException is thrown.
 * <p>
 * removed from manifest.mf: OpenIDE-Module-Install: org/netbeans/modules/mobility/licensing/AcceptLicense.class
 *
 * @author  Marek Slama, Adam Sotona
 */

public final class AcceptLicense extends ModuleInstall {
    
    private static JDialog d;
    private static String command;
    
    /** If License was not accepted during installation user must accept it here.
     */
    public void validate() throws IllegalStateException {
//        Util.setDefaultLookAndFeel();
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("LICENSE.txt"); //NOI18N
        if (fo == null) return;
        URL url = null;
        try {
            url = fo.getURL();
        } catch (FileStateInvalidException fsie) {
            throw new IllegalStateException(fsie);
        }
        LicensePanel licensePanel = new LicensePanel(url);
        ResourceBundle bundle = NbBundle.getBundle(AcceptLicense.class);
        String yesLabel = bundle.getString("MSG_LicenseYesButton"); // NOI18N
        String noLabel = bundle.getString("MSG_LicenseNoButton"); // NOI18N
        JButton yesButton = new JButton();
        JButton noButton = new JButton();
        setLocalizedText(yesButton,yesLabel);
        setLocalizedText(noButton,noLabel);
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                command = e.getActionCommand();
                d.setVisible(false);
                d = null;
            }
        };
        yesButton.addActionListener(listener);
        noButton.addActionListener(listener);
        
        yesButton.setActionCommand("yes"); // NOI18N
        noButton.setActionCommand("no"); // NOI18N
        
        yesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_AcceptButton")); // NOI18N
        yesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_AcceptButton")); // NOI18N
        
        noButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_RejectButton")); // NOI18N
        noButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_RejectButton")); // NOI18N
        
        Dimension yesPF = yesButton.getPreferredSize();
        Dimension noPF = noButton.getPreferredSize();
        int maxWidth = Math.max(yesButton.getPreferredSize().width, noButton.getPreferredSize().width);
        int maxHeight = Math.max(yesButton.getPreferredSize().height, noButton.getPreferredSize().height);
        yesButton.setPreferredSize(new Dimension(maxWidth, maxHeight));
        noButton.setPreferredSize(new Dimension(maxWidth, maxHeight));
        
        d = new JDialog((Frame) null,bundle.getString("MSG_LicenseDlgTitle"),true); // NOI18N
        
        d.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_LicenseDlg")); // NOI18N
        d.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_LicenseDlg")); // NOI18N
        
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
            try {
                fo.delete();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        } else {
            throw new IllegalStateException(bundle.getString("MSG_LicenseRejected"));//NOI18N
        }
    }
    
    /**
     * Actual setter of the text & mnemonics for the AbstractButton or
     * their subclasses. We must copy necessary code from org.openide.awt.Mnemonics
     * because org.openide.awt module is not available yet when this code is called.
     * @param item AbstractButton
     * @param text new label
     */
    private static void setLocalizedText(AbstractButton button, String text) {
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
            
            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
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
