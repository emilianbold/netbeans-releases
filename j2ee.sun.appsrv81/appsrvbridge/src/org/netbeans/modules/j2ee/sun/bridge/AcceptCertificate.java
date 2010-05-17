/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2ee.sun.bridge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.openide.util.NbBundle;
/**
 * Displays CertificatePanel to user. User must accept Certificate to continue.
 * if user does not accept false is return.
 *
 * @author  Ludo
 */

public final class AcceptCertificate {
    
    private static JDialog d;
    private static String command;
    
    /** If Certificate was not accepted during installation user must accept it here. 
     */
    public static boolean acceptCertificatePanel (String certificate) throws Exception {
        CertificatePanel CertificatePanel = new CertificatePanel(certificate);
        ResourceBundle bundle = NbBundle.getBundle(AcceptCertificate.class);
        String yesLabel = bundle.getString("MSG_CertificateYesButton");
        String noLabel = bundle.getString("MSG_CertificateNoButton");
        JButton yesButton = new JButton(yesLabel);
        JButton noButton = new JButton(noLabel);

        ActionListener listener = new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                command = e.getActionCommand();
                d.setVisible(false);
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
        
        int maxWidth = Math.max(yesButton.getPreferredSize().width, noButton.getPreferredSize().width);
        int maxHeight = Math.max(yesButton.getPreferredSize().height, noButton.getPreferredSize().height);
        yesButton.setPreferredSize(new Dimension(maxWidth, maxHeight));
        noButton.setPreferredSize(new Dimension(maxWidth, maxHeight));
        
        d = new JDialog((Frame) null,bundle.getString("MSG_CertificateDlgTitle"),true);
        
        d.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CertificateDlg"));
        d.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CertificateDlg"));
        
        d.getContentPane().add(CertificatePanel,BorderLayout.CENTER);
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
            return true;
        } else {
            return false;
        }
    }
    

}
