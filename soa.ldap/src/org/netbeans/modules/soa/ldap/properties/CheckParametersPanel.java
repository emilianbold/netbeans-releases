/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.properties;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.net.ConnectException;
import java.net.UnknownHostException;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.soa.ldap.LDAPUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author anjeleevich
 */
public class CheckParametersPanel extends JPanel {
    private CardLayout cardLayout;

    private JPanel connectingPanel;
    private JLabel connectingLabel;
    private JProgressBar connectingProgressBar;
    private JPanel connectingProgressBarContainer;

    private JPanel connectionOkPanel;
    private JTextArea okMessageTextArea;

    private JPanel connectionFailedPanel;
    private JLabel connectionFailedLabel;
    private JTextArea errorMessageTextArea;
    private JScrollPane errorMessageScrollPane;

    private ConnectionProperties connectionProperties;
    private DialogDescriptor dialogDescriptor;
    private RequestProcessor.Task checkParametersTask = null;

    private final Object sync = new Object();

    private boolean closed = false;

    public CheckParametersPanel() {
        // connecting
        connectingLabel = new JLabel(NbBundle.getMessage(getClass(),
                "CheckParametersPanel.connectingLabel.text")); // NOI18N
        connectingProgressBar = new JProgressBar();
        connectingProgressBar.setIndeterminate(true);
        connectingProgressBarContainer = new JPanel(new BorderLayout());
        connectingProgressBarContainer.add(connectingProgressBar,
                BorderLayout.NORTH);
        connectingPanel = new JPanel(new BorderLayout(6, 6));
        connectingPanel.setBorder(new EmptyBorder(11, 10, 11 * 4, 10));
        connectingPanel.add(connectingLabel, BorderLayout.NORTH);
        connectingPanel.add(connectingProgressBarContainer,
                BorderLayout.CENTER);

        // connection ok
        okMessageTextArea = new JTextArea(8, 40);
        okMessageTextArea.setText(NbBundle.getMessage(getClass(), 
                "CheckParametersPanel.okMessageTextArea.text")); // NOI18N
        okMessageTextArea.setEditable(false);
        okMessageTextArea.setWrapStyleWord(true);
        okMessageTextArea.setFont(connectingLabel.getFont());
        okMessageTextArea.setOpaque(false);
        okMessageTextArea.setBorder(null);
        connectionOkPanel = new JPanel(new BorderLayout());
        connectionOkPanel.setBorder(new EmptyBorder(11, 10, 11, 10));
        connectionOkPanel.add(okMessageTextArea, BorderLayout.CENTER);

        // connection failed
        connectionFailedLabel = new JLabel(NbBundle.getMessage(getClass(),
                "CheckParametersPanel.connectionFailedLabel.text")); // NOI18N
        errorMessageTextArea = new JTextArea();
        errorMessageTextArea.setWrapStyleWord(true);
        errorMessageTextArea.setEditable(false);
        errorMessageTextArea.setFont(connectionFailedLabel.getFont());
        errorMessageTextArea.setText("Text");
        errorMessageScrollPane = new JScrollPane(errorMessageTextArea);
        connectionFailedPanel = new JPanel(new BorderLayout(6, 6));
        connectionFailedPanel.setBorder(new EmptyBorder(11, 10, 11, 10));
        connectionFailedPanel.add(connectionFailedLabel, BorderLayout.NORTH);
        connectionFailedPanel.add(errorMessageScrollPane, BorderLayout.CENTER);

        cardLayout = new CardLayout();

        setLayout(cardLayout);

        add(connectingPanel, CONNECTING_PANEL);
        add(connectionOkPanel, CONNECTION_OK_PANEL);
        add(connectionFailedPanel, CONNECTION_FAILED_PANEL);

        cardLayout.show(this, CONNECTING_PANEL);
    }

    public void check(ConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
        this.dialogDescriptor = new DialogDescriptor(
                this, NbBundle.getMessage(getClass(),
                "CheckParametersDialogTitle"), true, // NOI18N
                new Object[] { DialogDescriptor.CLOSED_OPTION },
                null, DialogDescriptor.DEFAULT_ALIGN, null, null, true);
        
        cardLayout.show(this, CONNECTING_PANEL);

        Dialog dialog = DialogDisplayer.getDefault()
                .createDialog(dialogDescriptor);

        checkParametersTask = RequestProcessor.getDefault().post(
                new CheckParametersRunnable(), 500);

        dialog.setVisible(true);
        closed = true;

        if (checkParametersTask != null) {
            checkParametersTask.cancel();
        }

        dialog.dispose();

        checkParametersTask = null;
        dialogDescriptor = null;
    }

    private static final String CONNECTION_FAILED_PANEL 
            = "ConnectionFailed"; // NOI18N
    private static final String CONNECTION_OK_PANEL
            = "ConnectionOk"; // NOI18N
    private static final String CONNECTING_PANEL
            = "Connecting..."; // NOI18N

    private class CheckParametersRunnable implements Runnable {
        public void run() {
            NamingEnumeration<? extends Attribute> result = null;
            DirContext dirContext = null;
            
            try {
                dirContext = connectionProperties.createDirContext();
                Attributes attributes = dirContext.getAttributes("");
                result = attributes.getAll();

                while (result.hasMore()) {
                    Attribute attribute = result.next();
                }
            } catch (NamingException ex) {
                SwingUtilities.invokeLater(new ConnectionFaildRunnable(
                        LDAPUtils.exceptionToString(ex)));
                return;
            } catch (Exception ex) {
                SwingUtilities.invokeLater(new ConnectionFaildRunnable(
                        LDAPUtils.exceptionToString(ex)));
                return;
            } finally {
                LDAPUtils.close(result);
                LDAPUtils.close(dirContext);
                
                result = null;
                dirContext = null;
            }
            SwingUtilities.invokeLater(new ConnectionOkRunnable());
        }
    }

    private class ConnectionOkRunnable implements Runnable {
        public void run() {
            if (!closed) {
                cardLayout.show(CheckParametersPanel.this,
                        CONNECTION_OK_PANEL);
                checkParametersTask = null;
            }
        }
    }

    private class ConnectionFaildRunnable implements Runnable {
        private String errorMessage;

        ConnectionFaildRunnable(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public void run() {
            if (!closed) {
                if (errorMessage == null || errorMessage.trim().length() == 0) {
                    errorMessage = NbBundle.getMessage(CheckParametersPanel
                            .class, "CheckParametersPanel" // NOI18N
                            + ".UNKNOWN_CONNECTION_PROBLEM"); // NOI18N
                }

                errorMessageTextArea.setText(errorMessage);
                errorMessageTextArea.setCaretPosition(0);

                cardLayout.show(CheckParametersPanel.this,
                        CONNECTION_FAILED_PANEL);

                checkParametersTask = null;
            }
        }
    }
}
