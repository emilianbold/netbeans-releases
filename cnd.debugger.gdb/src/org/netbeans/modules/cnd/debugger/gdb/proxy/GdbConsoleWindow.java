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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * GdbConsoleWindow.java
 *
 * @author Nik Molchanov
 *
 * Originally this class was in org.netbeans.modules.cnd.debugger.gdb package.
 * Later a new "proxy" package was created and this class was moved, that's how
 * it lost its history. To view the history look at the previous location.
 */

package org.netbeans.modules.cnd.debugger.gdb.proxy;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.accessibility.AccessibleContext;

import org.netbeans.spi.viewmodel.Models;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;

/**
 *
 * Gdb Console Window
 *
 */
public final class GdbConsoleWindow  extends TopComponent
        implements ActionListener {
    
    /** generated Serialized Version UID */
    //static final long serialVersionUID = 8415779626223L;
    
    private transient JComponent tree = null;
    private String name;
    private String view_name;
    //private GdbDebugger debugger = null;
    private GdbProxy debugger = null;
    private JMenuItem menuItemHideText;
    private JPopupMenu popup;
    private JTextArea ta;
    private JScrollBar ta_sp_sb;
    private JScrollPane ta_sp;
    private JPanel hp;
    private JPanel cp;
    private PopupListener popupListener;
    private boolean dontShowText=true;
    private JComboBox cp_commandList;
    private JLabel hp_name;
    private String program;
    private final String LC_ProgramName;
    private String status;
    private final String LC_ProgramStatus;
    private String selected_text = null;
    private Vector messagesToProcess;
    
    /**
     * Creates a new instance of GdbConsoleWindow
     */
    public GdbConsoleWindow() {
        name = getString("TITLE_GdbConsoleWindow");    //NOI18N
        view_name = getString("TITLE_GdbConsoleWindow"); //NOI18N
        LC_ProgramName = getString("LABEL_ProgramName"); //NOI18N
        LC_ProgramStatus = getString("LABEL_ProgramStatus"); //NOI18N
        status = LC_ProgramStatus + ' ' + getString("MSG_NotLoaded"); //NOI18N
        program = LC_ProgramName;
        super.setName(name);
        messagesToProcess = new Vector();
        //setIcon(org.openide.util.Utilities.loadImage(getString("ICON_GdbConsoleWindow"))); // NOI18N
    }
    
    protected String preferredID() {
        return this.getClass().getName();
    }
    
    protected void componentShowing() {
        super.componentShowing();
        updateWindow();
    }
    
    /**
     * Connects GdbConsoleWindow to GdbProxy debugger
     * This connection allows to send user's commands to debugger.
     *
     * @param debugger - GdbProxy debugger
     */
    public void connectToDebugger(/* GdbDebugger */ GdbProxy debugger) {
        this.debugger = debugger;
    }
    
    public int getPersistenceType() {
        //return PERSISTENCE_ALWAYS;
        return PERSISTENCE_NEVER;
    }
    
    public String getName() {
        return (name);
    }
    
    public String getToolTipText() {
        return (view_name);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String command;
        String ac = actionEvent.getActionCommand();
        if (ac.equals("comboBoxEdited")) { //NOI18N
            // Get command
            JComboBox cb = (JComboBox)actionEvent.getSource();
            command=(String)cb.getSelectedItem();
        } else {
            //System.out.println("  GdbConsoleWindow.actionPerformed()  actionEvent.getActionCommand()="+ac); //DEBUG
            return;
        }
        if (command == null) return;
        addCommandToList(command);
        // Reset input field
        cp_commandList.setSelectedIndex(0);
        if (debugger == null) return;
        if (debugger.gdbProxyML == null) return;
        debugger.gdbProxyML.sendCommand(command);
    }
    
    private void addCommandToList(String command) {
        // Search if it was already in cp_commandList
        boolean found = false;
        for (int i = 0; i < cp_commandList.getItemCount(); i++ ) {
            if (command.compareTo((String)cp_commandList.getItemAt(i)) == 0) {
                found = true;
                break;
            }
        }
        if (!found) {
            // Add command to the cp_commandList
            cp_commandList.addItem(command);
        }
    }
    
    /**
     * Adds messages to console
     *
     * @param message - a message
     */
    public void add(String message) {
        synchronized (messagesToProcess) {
            messagesToProcess.add(message);
        }
        updateWindow();
    }
    
    /**
     * Updates status
     *
     * @param program - program name
     * @param status - program status
     */
    public void updateStatus(String program, String status) {
        synchronized (messagesToProcess) {
            if (program != null) {
                this.program = LC_ProgramName + ' ' + program;
            }
            if (status != null) {
                this.status = LC_ProgramStatus + ' ' + status;
            }
        }
        updateWindow();
    }
    
    private void updateWindow() {
        int i, k;
        String EMPTY_STRING="";                               //NOI18N
        String dc0="                                       "; //NOI18N
        String dc1="help ";                                   //NOI18N
        String dc2="info";                                    //NOI18N
        String dc3="-break-insert main";                      //NOI18N
        String dc4="-break-delete 1";                         //NOI18N
        String dc5="-exec-run ";                              //NOI18N
        String dc6="-exec-continue";                          //NOI18N
        String dc7="-exec-next";                              //NOI18N
        String dc8="-exec-step";                              //NOI18N
        String dc9="-data-evaluate-expression ";              //NOI18N
        
        if (tree == null) {
            ta = new JTextArea();
            ta_sp = new JScrollPane(ta);
            ta_sp.setViewportView(ta);
            setLayout(new BorderLayout());
            tree = Models.createView(Models.EMPTY_MODEL);
            tree.setName(view_name);
            
            ta.setEditable(false);
            ta.setWrapStyleWord(false);
            Font f = ta.getFont();
            //NM ta.setFont(new Font("Monospaced", f.getStyle(), f.getSize())); //NOI18N
            
            hp = new JPanel(new BorderLayout());
            hp_name = new JLabel(getString("LABEL_GdbConsoleWindow")); //NOI18N
            hp_name.setToolTipText(getString("LABEL_GdbConsoleWindow")); //NOI18N
            hp.add(hp_name, BorderLayout.WEST);
            
            cp = new JPanel(new FlowLayout());
            cp.setToolTipText(getString("TTIP_GdbConsoleWindow")); //NOI18N
            cp_commandList = new JComboBox();
            cp_commandList.setSize(dc0.length() * 2, f.getSize());
            cp_commandList.addItem(EMPTY_STRING);
            cp_commandList.addItem(dc1);
            cp_commandList.addItem(dc2);
            cp_commandList.addItem(dc3);
            cp_commandList.addItem(dc4);
            cp_commandList.addItem(dc5);
            cp_commandList.addItem(dc6);
            cp_commandList.addItem(dc7);
            cp_commandList.addItem(dc8 + dc0 + dc0);
            cp_commandList.addItem(dc9);
            cp_commandList.setEditable(true);
            cp_commandList.addActionListener(this);
            JLabel cp_text1 = new JLabel(getString("LABEL_GdbDebuggerCommand")); //NOI18N
            cp_text1.setToolTipText(getString("AC_DESC_GdbDebuggerCommand")); //NOI18N
            cp.add(cp_text1);
            cp.add(cp_commandList);
            
            tree.add(hp, BorderLayout.NORTH);
            tree.add(ta_sp, BorderLayout.CENTER);
            tree.add(cp, BorderLayout.SOUTH);
            AccessibleContext ac = tree.getAccessibleContext();
            ac.setAccessibleDescription(getString("AC_DESC_GdbConsoleWindow")); // NOI18N
            ac.setAccessibleName(getString("AC_NAME_GdbConsoleWindow")); // NOI18N
            add(tree, "Center");  //NOI18N
            
            //Create the popup menu.
            popup = new JPopupMenu();
            //Create listener
            popupListener = new PopupListener(popup);
            //Add HideText
            popup.addSeparator();
            menuItemHideText = new JMenuItem(new HideTextAction());
            popup.add(menuItemHideText);
            //Add MoreInfo
            popup.addSeparator();
            popup.add(new ShowDynamicHelpPageAction());
            //Add listener
            ta.addMouseListener(popupListener);
            ta.setText(null);
            ta.setCaretPosition(0);
        }
        synchronized (messagesToProcess) {
            String s;
            while (messagesToProcess.size() > 0) {
                s = (String) messagesToProcess.remove(0);
                ta.append(s);
            }
        }
        hp_name.setText(program + "               " + status); //NOI18N
        // Scroll down to show last message
        if (ta_sp_sb == null) {
            ta_sp_sb = ta_sp.getVerticalScrollBar();
        }
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (ta_sp_sb != null) {
                        try {
                            ta_sp_sb.setValue(ta_sp_sb.getMaximum());
                        } catch (java.lang.Exception e) {
                            // Bad value. Ignore it.
                        }
                    }
                }
            });
        } catch (java.lang.Exception e) {
            // Ignore it.
        }
    }
    
    class PopupListener extends MouseAdapter implements ActionListener, PopupMenuListener {
        JPopupMenu popup;
        
        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (dontShowText == true) {
                    menuItemHideText.setEnabled(false);
                } else {
                    menuItemHideText.setEnabled(true);
                }
                selected_text = ta.getSelectedText();
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        public void actionPerformed(ActionEvent ev) {
            JMenuItem source = (JMenuItem)(ev.getSource());
            //System.out.println("    Event source: " + source.getText()); //DEBUG
            String s = source.getText();
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
    
    class HideTextAction extends AbstractAction {
        public HideTextAction() {
            super("Hide Text", new ImageIcon("cut.gif")); //FIXUP //NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            //System.out.println("HideTextAction.ActionPerformed(Hide Text)"); //DEBUG
        }
    }
    
    class ShowDynamicHelpPageAction extends AbstractAction {
        public ShowDynamicHelpPageAction() {
            super("More Info", new ImageIcon("help.gif")); //FIXUP //NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            //System.out.println("ShowDynamicHelpPageAction.ActionPerformed(More Info)");
            ShowDynamicHelpPage();
        }
    }
    
    protected void ShowDynamicHelpPage() {
        //HelpManager.getDefault().showDynaHelp("gdb-help");
    }
    
// ------------------ Private support methods --------------------
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(GdbConsoleWindow.class);
        }
        if (bundle == null) return s; // FIXUP
        return bundle.getString(s);
    }
    
} /* End of class GdbConsoleWindow */
