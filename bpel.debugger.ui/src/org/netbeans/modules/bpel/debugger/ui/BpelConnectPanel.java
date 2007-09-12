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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.bpel.debugger.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bpel.debugger.api.AttachingCookie;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;


/**
 * Panel for entering host/port for attaching to a BPEL service engine.
 *
 * @author Sun Microsystems
 */
public class BpelConnectPanel extends JPanel implements Controller {

    private static final String PROPERTIES_KEY = "BpelDebuggerConnection";

    private JTextField mHostField;
    private JTextField mPortField;
    
    
    /**
     * Adds options for a selected connector type to this panel.
     */
    public BpelConnectPanel() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        // other lines
        mHostField = new JTextField(getSavedHost());
        String hostLabel = 
            NbBundle.getMessage(BpelConnectPanel.class, "CTL_Host");
        char hostMnemonic = 
            NbBundle.getMessage(BpelConnectPanel.class, "CTL_Host_mnemonic").charAt(0);
        addSettingUI(hostLabel, mHostField, hostMnemonic);

        mPortField = new JTextField(getSavedPort());
        String portLabel = 
            NbBundle.getMessage(BpelConnectPanel.class, "CTL_Port");
        char portMnemonic = 
            NbBundle.getMessage(BpelConnectPanel.class, "CTL_Port_mnemonic").charAt(0);
        addSettingUI(portLabel, mPortField, portMnemonic);

        // Create an empty panel that resizes vertically so that
        // other elements have fix height:
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1.0;
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(1, 1));
        add(p, c);
    }
    
    public boolean isValid() {
        return true;
    }

    public boolean cancel() {
        return true;
    }

    public boolean ok() {
        final String host = mHostField.getText();
        final String port = mPortField.getText();
        saveArgs(host, port);

        ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                BpelConnectPanel.class, "CTL_connectProgress"));
        try {
            progress.start();
            DebuggerEngine[] es = DebuggerManager.getDebuggerManager().startDebugging(
                    DebuggerInfo.create(AttachingCookie.ID,
                            new Object[] {
                            AttachingCookie.create(host, port) }));
        } finally {
            progress.finish();
        }
        return true;
    }

    private void addSettingUI(String label, JTextField tfParam, char mnemonic) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 0, 3);
        c.anchor = GridBagConstraints.WEST;
        JLabel iLabel = new JLabel(label);
        iLabel.setDisplayedMnemonic(mnemonic);
        iLabel.setToolTipText(label);
        add(iLabel, c);
        iLabel.setLabelFor(tfParam);
        tfParam.setName(label);
        tfParam.getAccessibleContext().setAccessibleDescription(
                new MessageFormat(NbBundle.getMessage(getClass(), "ACSD_CTL_Argument"))
                        .format(new Object[] { label }));
        tfParam.setToolTipText(label);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(6, 3, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        add(tfParam, c);
    }
    
    private static String getSavedHost() {
        Map masterSettings = Properties.getDefault().getProperties("debugger").
            getMap("connection_settings", new HashMap());
        Map mySettings = (Map) masterSettings.get(PROPERTIES_KEY);
        if (mySettings != null) {
            String host = (String) mySettings.get("host");
            if (host != null) {
                return host;
            }
        }
        return DEFAULT_HOST;
    }
    
    private static String getSavedPort() {
        Map masterSettings = Properties.getDefault().getProperties("debugger").
            getMap("connection_settings", new HashMap());
        Map mySettings = (Map) masterSettings.get(PROPERTIES_KEY);
        if (mySettings != null) {
            String port = (String) mySettings.get("port");
            if (port != null) {
                return port;
            }
        }
        return DEFAULT_PORT;
    }

    private static void saveArgs(String newHost, String newPort) {
        Map masterSettings = Properties.getDefault().getProperties("debugger").
            getMap("connection_settings", new HashMap());
        Map mySettings = new HashMap();
        mySettings.put("host", newHost);
        mySettings.put("port", newPort);
        masterSettings.put(PROPERTIES_KEY, mySettings);
        Properties.getDefault().getProperties("debugger").
            setString("last_attaching_connector", PROPERTIES_KEY);
        Properties.getDefault().getProperties("debugger").
            setMap("connection_settings", masterSettings);
    }

    private static final String DEFAULT_PORT = "3343"; // NOI18N
    private static final String DEFAULT_HOST = "localhost"; // NOI18N
}
