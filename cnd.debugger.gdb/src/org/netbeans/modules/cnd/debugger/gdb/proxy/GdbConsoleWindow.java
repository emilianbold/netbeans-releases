/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb.proxy;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.spi.viewmodel.Models;
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
    
    private GdbDebugger debugger;
    private GdbProxy gdbProxy;
    private JMenuItem menuItemHideText;
    private JPopupMenu popup;
    private JScrollBar scrollBar;
    private boolean dontShowText=true;
    private String status;
    private static GdbConsoleWindow instance = null;
    
    /** Creates new GdbConsoleWindow */
    private GdbConsoleWindow(GdbDebugger debugger, GdbProxy gdbProxy) {
        initComponents();
        try {
            final TopComponent tc = this;
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    tc.setDisplayName(NbBundle.getMessage(GdbConsoleWindow.class, "TITLE_GdbConsoleWindow")); // NOI18N
                }
            });
        } catch (Exception ex) {
        }
        scrollBar = debuggerLogPane.getVerticalScrollBar();

        this.debugger = debugger;
        this.gdbProxy = gdbProxy;
        debugger.addPropertyChangeListener(this);
        ProjectActionEvent pae = (ProjectActionEvent)
                        debugger.getLookup().lookupFirst(null, ProjectActionEvent.class);
        programName.setText(pae.getExecutable());
        status = debugger.getState().toString();
    }
    
    public static GdbConsoleWindow getInstance(GdbDebugger debugger, GdbProxy gdbProxy) {
        if (instance == null || instance.debugger != debugger || instance.gdbProxy != gdbProxy) {
            instance = new GdbConsoleWindow(debugger, gdbProxy);
            docConsole(instance);
        }
        return instance;
    }
    
    private static void docConsole(final GdbConsoleWindow gcw) {
        if (SwingUtilities.isEventDispatchThread()) {
            Mode mode = WindowManager.getDefault().findMode("output"); // NOI18N
            if (mode != null) {
                mode.dockInto(instance);
            }
            gcw.open();
            gcw.requestActive();
        } else {
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Mode mode = WindowManager.getDefault().findMode("output"); // NOI18N
                        if (mode != null) {
                            mode.dockInto(gcw);
                        }
                        gcw.open();
                        gcw.requestActive();
                    }
                });
            } catch (Exception ex) {}
        }
    }
    
    public void openConsole() {
        if (SwingUtilities.isEventDispatchThread()) {
            open();
        } else {
            final TopComponent tc = this;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tc.open();
                }
            });
        }
    }
    
    public void closeConsole() {
        instance = null;
        if (SwingUtilities.isEventDispatchThread()) {
            close();
        } else {
            final TopComponent tc = this;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tc.close();
                }
            });
        }
    }
    
    protected String preferredID() {
        return this.getClass().getName();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName() == GdbDebugger.PROP_STATE) {
            Object state = ev.getNewValue();
            if (state == GdbDebugger.STATE_NONE) {
                closeConsole();
            } else {
                updateStatus(state.toString());
            }
        }
    }
    
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
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
        if (command == null) return;
        addCommandToList(command);
        // Reset input field
        debuggerCommand.setSelectedIndex(0);
        if (gdbProxy == null) return;
        gdbProxy.getProxyEngine().sendCommand(command);
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
    public void add(String message) {
        debuggerLog.append(message);
        // Scroll down to show last message
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        scrollBar.setValue(scrollBar.getMaximum());
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
        }
    }
    
    /**
     * Updates status
     *
     * @param status Program status
     */
    public void updateStatus(String status) {
        programStatus.setText(status);
    }
    
    class HideTextAction extends AbstractAction {
        public HideTextAction() {
            super("Hide Text", new ImageIcon("cut.gif")); //FIXUP //NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            //System.out.println("HideTextAction.ActionPerformed(Hide Text)"); //DEBUG
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

        getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JP_Debugger_Console"));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSD_JP_Debugger_Console"));
        programLB.setLabelFor(programName);
        programLB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("L_Program_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        add(programLB, gridBagConstraints);
        programLB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JL_ProgramName"));
        programLB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSD_JL_Program_Name"));

        programName.setEditable(false);
        programName.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("TOOLTIP_Program_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 6);
        add(programName, gridBagConstraints);
        programName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JL_ProgramName"));

        statusLB.setLabelFor(programStatus);
        statusLB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("L_Program_Status"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 2);
        add(statusLB, gridBagConstraints);
        statusLB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JL_ProgramStatus"));
        statusLB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSD_JL_Program_Status"));

        programStatus.setEditable(false);
        programStatus.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("TOOLTIP_Program_Status"));
        programStatus.setMinimumSize(new java.awt.Dimension(100, 20));
        programStatus.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 6);
        add(programStatus, gridBagConstraints);
        programStatus.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JL_ProgramStatus"));
        programStatus.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSD_JTF_Program_Status"));

        debuggerLog.setColumns(20);
        debuggerLog.setEditable(false);
        debuggerLog.setRows(5);
        debuggerLogPane.setViewportView(debuggerLog);
        debuggerLog.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JTA_Debugger_Log"));
        debuggerLog.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSD_JTA_Debugger_Log"));

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
        commandLB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("L_Debugger_Command"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 9, 0);
        add(commandLB, gridBagConstraints);
        commandLB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JL_Debugger_Command"));
        commandLB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSD_JL_Debugger_Command"));

        debuggerCommand.setEditable(true);
        debuggerCommand.setModel(new DefaultComboBoxModel(new String[] { "", "help ", "info", "-break-insert main", "-break-delete 1", "-exec-run ", "-exec-continue", "-exec-next", "-exec-step", "-data-evaluate-expression " })); // NOI18N
        debuggerCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("TOOLTIP_Debugger_Command"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 6, 6);
        add(debuggerCommand, gridBagConstraints);
        debuggerCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSN_JL_Debugger_Command"));
        debuggerCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/proxy/Bundle").getString("ACSD_JCB_Debugger_Command"));

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
