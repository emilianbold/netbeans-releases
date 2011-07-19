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

package org.netbeans.modules.cnd.debugger.gdb.proxy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Panel for debugger console. 
 * This panel is a part of "Gdb Debugger Console" window.
 *
 * @author Nik Molchanov and Gordon Prieur
 */
public class GdbConsoleWindow extends TopComponent implements ActionListener, PropertyChangeListener {
    
    private GdbProxy gdbProxy;
    private JScrollBar scrollBar;
    
    /** Creates new GdbConsoleWindow */
    GdbConsoleWindow(GdbDebugger debugger, GdbProxy gdbProxy) {
        initComponents();
        try {
            final TopComponent tc = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tc.setDisplayName(NbBundle.getMessage(GdbConsoleWindow.class, "TITLE_GdbConsoleWindow")); // NOI18N
                }
            });
        } catch (Exception ex) {
        }
        scrollBar = debuggerLogPane.getVerticalScrollBar();

        this.gdbProxy = gdbProxy;
        ProjectActionEvent pae = debugger.getLookup().lookupFirst(null, ProjectActionEvent.class);
        programName.setText(pae.getExecutable());
        debugger.addPropertyChangeListener(this);
    }
    
    private void docConsole() {
        Mode mode = WindowManager.getDefault().findMode("output"); // NOI18N
        if (mode != null) {
            mode.dockInto(this);
        }
        open();
        requestActive();
    }
    
    public void openConsole() {
        if (SwingUtilities.isEventDispatchThread()) {
            docConsole();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    docConsole();
                }
            });
        }
    }
    
    public void closeConsole() {
        if (SwingUtilities.isEventDispatchThread()) {
            close();
        } else {
            final TopComponent tc = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tc.close();
                }
            });
        }
    }
    
    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }
  
    @Override
    public void propertyChange(PropertyChangeEvent ev) { 
        if (GdbDebugger.PROP_STATE.equals(ev.getPropertyName())) {
            Object state = ev.getNewValue();
            if (state == GdbDebugger.State.EXITED) {
                closeConsole();
            } else {
                updateStatus(state.toString());
            }
        }
    }
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command;
        String ac = actionEvent.getActionCommand();
        if (ac.equals("comboBoxEdited")) { //NOI18N
            // Get command
            JComboBox cb = (JComboBox)actionEvent.getSource();
            command=(String)cb.getSelectedItem();
        } else {
            return;
        }
        if (command == null || command.length() == 0) {
            return;
        }
        addCommandToList(command);
        // Reset input field
        debuggerCommand.setSelectedIndex(0);
        if (gdbProxy == null) {
            return;
        }
        gdbProxy.getProxyEngine().sendConsoleCommand(command);
    }
    
    private void addCommandToList(String command) {
        // Search if it was already in debuggerCommand
        boolean found = false;
        for (int i = 0; i < debuggerCommand.getItemCount(); i++ ) {
            if (command.compareTo((String)debuggerCommand.getItemAt(i)) == 0) {
                found = true;
                break;
            }
        }
        if (!found) {
            // Add command to the debuggerCommand
            debuggerCommand.addItem(command);
        }
    }

    /**
     * Adds messages to console
     *
     * @param message - a message
     */
    void add(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    debuggerLog.append(message);
                    if (!message.isEmpty() && message.charAt(message.length()-1) != '\n') {
                        debuggerLog.append("\n"); //NOI18N
                    }
                    // Scroll down to show last message
                    scrollBar.setValue(scrollBar.getMaximum());
                } catch (Exception e) {
                }
            }
        });
    }
    
    /**
     * Updates status
     *
     * @param status Program status
     */
    public void updateStatus(String status) {
        programStatus.setText(status);
    }
    
    static class HideTextAction extends AbstractAction {
        public HideTextAction() {
            super("Hide Text", new ImageIcon("cut.gif")); //FIXUP //NOI18N
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            //System.out.println("HideTextAction.ActionPerformed(Hide Text)"); //DEBUG
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        programLB = new javax.swing.JLabel();
        programName = new javax.swing.JTextField();
        statusLB = new javax.swing.JLabel();
        programStatus = new javax.swing.JTextField();
        debuggerLogPane = new javax.swing.JScrollPane();
        debuggerLog = new javax.swing.JTextArea();
        commandLB = new javax.swing.JLabel();
        debuggerCommand = new javax.swing.JComboBox();
        debuggerCommand.addActionListener(this);

        setLayout(new java.awt.GridBagLayout());

        programLB.setLabelFor(programName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle"); // NOI18N
        programLB.setText(bundle.getString("L_Program_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        add(programLB, gridBagConstraints);
        programLB.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JL_ProgramName")); // NOI18N
        programLB.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JL_Program_Name")); // NOI18N

        programName.setEditable(false);
        programName.setToolTipText(bundle.getString("TOOLTIP_Program_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 6);
        add(programName, gridBagConstraints);
        programName.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JL_ProgramName")); // NOI18N

        statusLB.setLabelFor(programStatus);
        statusLB.setText(bundle.getString("L_Program_Status")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 2);
        add(statusLB, gridBagConstraints);
        statusLB.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JL_ProgramStatus")); // NOI18N
        statusLB.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JL_Program_Status")); // NOI18N

        programStatus.setEditable(false);
        programStatus.setToolTipText(bundle.getString("TOOLTIP_Program_Status")); // NOI18N
        programStatus.setMinimumSize(new java.awt.Dimension(100, 20));
        programStatus.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 6);
        add(programStatus, gridBagConstraints);
        programStatus.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JL_ProgramStatus")); // NOI18N
        programStatus.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JTF_Program_Status")); // NOI18N

        debuggerLog.setColumns(20);
        debuggerLog.setEditable(false);
        debuggerLog.setRows(5);
        debuggerLogPane.setViewportView(debuggerLog);
        debuggerLog.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JTA_Debugger_Log")); // NOI18N
        debuggerLog.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JTA_Debugger_Log")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 70.0;
        gridBagConstraints.weighty = 70.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(debuggerLogPane, gridBagConstraints);

        commandLB.setLabelFor(debuggerCommand);
        commandLB.setText(bundle.getString("L_Debugger_Command")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 9, 0);
        add(commandLB, gridBagConstraints);
        commandLB.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JL_Debugger_Command")); // NOI18N
        commandLB.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JL_Debugger_Command")); // NOI18N

        debuggerCommand.setEditable(true);
        debuggerCommand.setModel(new DefaultComboBoxModel(new String[] { "", "help ", "info", "-break-insert main", "-break-delete 1", "-exec-run ", "-exec-continue", "-exec-next", "-exec-step", "-data-evaluate-expression " })); // NOI18N
        debuggerCommand.setToolTipText(bundle.getString("TOOLTIP_Debugger_Command")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 6, 6);
        add(debuggerCommand, gridBagConstraints);
        debuggerCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JL_Debugger_Command")); // NOI18N
        debuggerCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JCB_Debugger_Command")); // NOI18N

        getAccessibleContext().setAccessibleName(bundle.getString("ACSN_JP_Debugger_Console")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JP_Debugger_Console")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel commandLB;
    protected javax.swing.JComboBox debuggerCommand;
    protected javax.swing.JTextArea debuggerLog;
    protected javax.swing.JScrollPane debuggerLogPane;
    protected javax.swing.JLabel programLB;
    protected javax.swing.JTextField programName;
    protected javax.swing.JTextField programStatus;
    private javax.swing.JLabel statusLB;
    // End of variables declaration//GEN-END:variables
}
