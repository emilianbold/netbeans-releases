/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.profiler.commandrunner;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.v2.ProfilerPlugin;
import org.netbeans.modules.profiler.v2.SessionStorage;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
// NOTE: cannot use mnemonics in popup menu, conflicts with textfields
@NbBundle.Messages({
    "CommandRunner_name=Command Runner",
    "CommandRunner_enabled=Enabled",
    "CommandRunner_starting=Session Starting:",
    "CommandRunner_started=Session Started:",
    "CommandRunner_stopping=Session Stopping:",
    "CommandRunner_stopped=Session Stopped:",
    "CommandRunner_test=Test"
})
final class CommandRunner extends ProfilerPlugin {
    
    private static final String PREFIX = "CommandRunner."; // NOI18N
    private static final String PROP_ENABLED = PREFIX + "PROP_ENABLED"; // NOI18N
    private static final String PROP_STARTING = PREFIX + "PROP_STARTING"; // NOI18N
    private static final String PROP_STARTED = PREFIX + "PROP_STARTED"; // NOI18N
    private static final String PROP_STOPPING = PREFIX + "PROP_STOPPING"; // NOI18N
    private static final String PROP_STOPPED = PREFIX + "PROP_STOPPED"; // NOI18N
    
    private final SessionStorage storage;
    
    
    CommandRunner(SessionStorage storage) {
        super(Bundle.CommandRunner_name());
        this.storage = storage;
    }
    
    
    public void createMenu(JMenu menu) {
        JCheckBoxMenuItem enabledItem = new JCheckBoxMenuItem() {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                storeEnabled(isSelected());
            }
        };
        Mnemonics.setLocalizedText(enabledItem, Bundle.CommandRunner_enabled());
        enabledItem.setSelected(readEnabled());
        menu.add(enabledItem);
        
        menu.addSeparator();
        
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBorder(BorderFactory.createEmptyBorder(0, 5 + getPopupOffset(), 1, 3));
        menuPanel.setOpaque(false);

        int fieldsWidth = 18;
        int vtab = 2;

        int y = 0;
        GridBagConstraints c;

        JLabel startingL = new JLabel();
        Mnemonics.setLocalizedText(startingL, Bundle.CommandRunner_starting());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, vtab, 5);
        menuPanel.add(startingL, c);

        final JTextField startingF = new JTextField();
        startingF.setText(storage.readFlag(PROP_STARTING, "")); // NOI18N
        startingF.setColumns(fieldsWidth);
        startingL.setLabelFor(startingF);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vtab, 5);
        menuPanel.add(startingF, c);

        JButton startingB = new JButton(Bundle.CommandRunner_test()) {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && !startingF.getText().trim().isEmpty());
            }
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                sessionStarting();
            }
        };
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, vtab, 0);
        menuPanel.add(startingB, c);

        JLabel startedL = new JLabel();
        Mnemonics.setLocalizedText(startedL, Bundle.CommandRunner_started());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, vtab, 5);
        menuPanel.add(startedL, c);

        final JTextField startedF = new JTextField();
        startedF.setText(storage.readFlag(PROP_STARTED, "")); // NOI18N
        startedF.setColumns(fieldsWidth);
        startedL.setLabelFor(startedF);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vtab, 5);
        menuPanel.add(startedF, c);

        JButton startedB = new JButton(Bundle.CommandRunner_test()) {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && !startedF.getText().trim().isEmpty());
            }
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                sessionStarted();
            }
        };
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, vtab, 0);
        menuPanel.add(startedB, c);

        JLabel stoppingL = new JLabel();
        Mnemonics.setLocalizedText(stoppingL, Bundle.CommandRunner_stopping());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, vtab, 5);
        menuPanel.add(stoppingL, c);

        final JTextField stoppingF = new JTextField();
        stoppingF.setText(storage.readFlag(PROP_STOPPING, "")); // NOI18N
        stoppingF.setColumns(fieldsWidth);
        stoppingL.setLabelFor(stoppingF);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vtab, 5);
        menuPanel.add(stoppingF, c);

        JButton stoppingB = new JButton(Bundle.CommandRunner_test()) {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && !stoppingF.getText().trim().isEmpty());
            }
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                sessionStopping();
            }
        };
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, vtab, 0);
        menuPanel.add(stoppingB, c);

        JLabel stoppedL = new JLabel();
        Mnemonics.setLocalizedText(stoppedL, Bundle.CommandRunner_stopped());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 5);
        menuPanel.add(stoppedL, c);

        final JTextField stoppedF = new JTextField();
        stoppedF.setText(storage.readFlag(PROP_STOPPED, "")); // NOI18N
        stoppedF.setColumns(fieldsWidth);
        stoppedL.setLabelFor(stoppedF);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 5);
        menuPanel.add(stoppedF, c);

        JButton stoppedB = new JButton(Bundle.CommandRunner_test()) {
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled && !stoppedF.getText().trim().isEmpty());
            }
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                sessionStopped();
            }
        };
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        menuPanel.add(stoppedB, c);

        Font font = menu.getFont();
        boolean enabled = enabledItem.isSelected();
        for (Component comp : menuPanel.getComponents()) {
            comp.setFont(font);
            comp.setEnabled(enabled);
        }
            
        menu.add(menuPanel);
        
        class Listener implements DocumentListener {
            private final JTextField tf;
            private final JButton b;
            private final String prop;
            Listener(JTextField tf, JButton b, String prop) {
                this.tf = tf;
                this.b = b;
                this.prop = prop;
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                String text = tf.getText().trim();
                storage.storeFlag(prop, text.isEmpty() ? null : text);
                b.setEnabled(true);
            }
        }
        
        startingF.getDocument().addDocumentListener(new Listener(startingF, startingB, PROP_STARTING));
        startedF.getDocument().addDocumentListener(new Listener(startedF, startedB, PROP_STARTED));
        stoppingF.getDocument().addDocumentListener(new Listener(stoppingF, stoppingB, PROP_STOPPING));
        stoppedF.getDocument().addDocumentListener(new Listener(stoppedF, stoppedB, PROP_STOPPED));
    }
    
    private static int getPopupOffset() {
        if (UIUtils.isWindowsLookAndFeel()) return 26;
        else if (UIUtils.isNimbusLookAndFeel()) return 21;
        else if (UIUtils.isMetalLookAndFeel()) return 15;
        else if (UIUtils.isGTKLookAndFeel()) return 19;
        else if (UIUtils.isAquaLookAndFeel()) return 20; // ???
        else return 20;
    }
    
    
    protected void sessionStarting() {
        if (!readEnabled()) return;
        runScript(storage.readFlag(PROP_STARTING, "")); // NOI18N
    }
    
    protected void sessionStarted()  {
        if (!readEnabled()) return;
        runScript(storage.readFlag(PROP_STARTED, "")); // NOI18N
    }
    
    protected void sessionStopping() {
        if (!readEnabled()) return;
        runScript(storage.readFlag(PROP_STOPPING, "")); // NOI18N
    }
    
    protected void sessionStopped()  {
        if (!readEnabled()) return;
        runScript(storage.readFlag(PROP_STOPPED, "")); // NOI18N
    }
    
    
    private void runScript(final String script) {
        if (!script.trim().isEmpty()) processor().post(new Runnable() {
            public void run() {
                try {
                    Runtime.getRuntime().exec(script);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                    ProfilerDialogs.displayError(ex.getLocalizedMessage());
                }
            }
        });
    }
    
    
    private void storeEnabled(boolean enabled) {
        storage.storeFlag(PROP_ENABLED, enabled ? null : Boolean.FALSE.toString());
    }
    
    private boolean readEnabled() {
        return Boolean.parseBoolean(storage.readFlag(PROP_ENABLED, Boolean.TRUE.toString()));
    }
    
    
    private static RequestProcessor PROCESSOR;
    private static RequestProcessor processor() {
        if (PROCESSOR == null) PROCESSOR = new RequestProcessor("Command Runner Processor"); // NOI18N
        return PROCESSOR;
    }
    
    
    @ServiceProvider(service=ProfilerPlugin.Provider.class, position=200)
    public static final class Provider extends ProfilerPlugin.Provider {
        public ProfilerPlugin createPlugin(Lookup.Provider project, SessionStorage storage) {
            return new CommandRunner(storage);
        }
    }
    
}
