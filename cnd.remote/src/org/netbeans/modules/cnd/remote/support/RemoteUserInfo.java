/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.support;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author gordonp
 */
public class RemoteUserInfo implements UserInfo, UIKeyboardInteractive {
    
    private static Map<String, RemoteUserInfo> map;
  
    private String passwd;
    private JTextField passwordField = (JTextField) new JPasswordField(20);
    private final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                         GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0);
    private Container panel;
    
    public static RemoteUserInfo getUserInfo(String host, String user) {
        if (map == null) {
            map = new HashMap<String, RemoteUserInfo>();
        }
        String key = host + ":" + user;
        RemoteUserInfo ui = map.get(key);
        if (ui == null) {
            ui = new RemoteUserInfo();
            map.put(key, ui);
        }
        return ui;
    }

    public String getPassword() { return passwd; }

    public boolean promptYesNo(String str) {
        System.err.println("RPB.MUI.promptYesNo: Warning: " + str);
        Object[] options = { "yes", "no" };
        int foo = JOptionPane.showOptionDialog(null, str, "Warning", JOptionPane.DEFAULT_OPTION, 
             JOptionPane.WARNING_MESSAGE, null, options, options[0]);
       return foo == 0;
    }

    public String getPassphrase() {
        return null; 
    }

    public boolean promptPassphrase(String message) {
        return true; 
    }

    public boolean promptPassword(String message) {
        Object[] ob = { passwordField }; 
        int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            passwd = passwordField.getText();
            return true;
        } else { 
            return false; 
        }
    }

    public void showMessage(String message){
        JOptionPane.showMessageDialog(null, message);
    }

    public String[] promptKeyboardInteractive(String destination, String name,
                          String instruction, String[] prompt, boolean[] echo) {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 0;
        panel.add(new JLabel(instruction), gbc);
        gbc.gridy++;

        gbc.gridwidth = GridBagConstraints.RELATIVE;

        JTextField[] texts = new JTextField[prompt.length];
        for (int i = 0; i < prompt.length; i++) {
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridx = 0;
            gbc.weightx = 1;
            panel.add(new JLabel(prompt[i]), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 1;
            if(echo[i]){
                texts[i]=new JTextField(20);
            } else{
                texts[i]=new JPasswordField(20);
            }
            panel.add(texts[i], gbc);
            gbc.gridy++;
        }

        if (JOptionPane.showConfirmDialog(null, panel, destination + ": " + name, JOptionPane.OK_CANCEL_OPTION,
                                   JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
            String[] response = new String[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                response[i] = texts[i].getText();
            }
            return response;
        } else {
            return null;  // cancel
        }
    }
}
