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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.accessibility.AccessibleContext;

import org.netbeans.spi.viewmodel.Models;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager;


public final class MemoryWindow extends TopComponent
    implements ActionListener
{

    /** generated Serialized Version UID */
    static final String preferredID = "MemoryWindow";	// NOI18N
    static MemoryWindow DEFAULT;

    private transient JComponent tree = null;
    private String name;
    private String view_name;
    private NativeDebugger debugger = null;
    private JMenuItem menuItemFollowSelectedPointer;
    private JPopupMenu popup;

    private JTextArea ta;
    private JScrollPane ta_sp;
    // OLD private JPanel headerPanel;
    private JPanel controlPanel;
    private ArrayList<String> current_addrs;
    private PopupListener popupListener;
    private String memory_start;
    private String memory_length;
    private JTextField controlLengthText;
    private String[] memory_formats = { 
                                        Catalog.get("L_Hexadecimal"),    //NOI18N
                                        Catalog.get("l_Hexadecimal"),    //NOI18N
                                        Catalog.get("w_Hexadecimal"),    //NOI18N
                                        Catalog.get("l_Decimal"),        //NOI18N
                                        Catalog.get("l_Octal"),          //NOI18N
                                        Catalog.get("L_Float"),          //NOI18N
                                        Catalog.get("l_Float"),          //NOI18N
                                        Catalog.get("L_Instructions"),   //NOI18N
                                        Catalog.get("L_Characters"),     //NOI18N
                                        Catalog.get("L_WideCharacters"), //NOI18N
                                      };
    private String[] short_memory_formats = {
                                              "lX",       //NOI18N
                                              "X",        //NOI18N
                                              "x",        //NOI18N
                                              "D",        //NOI18N
                                              "O",        //NOI18N
                                              "F",        //NOI18N
                                              "f",        //NOI18N
                                              "i",        //NOI18N
                                              "c",        //NOI18N
                                              "w",        //NOI18N
                                             };
    private JComboBox controlFormatCombo;
    private FormatListener format_listener;
    private int memory_format = 1;

    private boolean needInitData=true;
    private JComboBox controlAddressCombo;
    private String selected_text = null;

    public static TopComponent getDefault() {
        if (DEFAULT == null) {
            MemoryWindow tc = (MemoryWindow) WindowManager.getDefault().findTopComponent(preferredID);
            if (tc == null)
                new MemoryWindow();
        }
        return DEFAULT;
    }
    
    public MemoryWindow() {
	name = Catalog.get("TITLE_MemoryWindow");    //NOI18N
	view_name = Catalog.get("TITLE_MemoryView"); //NOI18N
	super.setName(name);
	DEFAULT = this;
	final String iconDir = "org/netbeans/modules/cnd/debugger/common2/icons/";//NOI18N
	setIcon(org.openide.util.ImageUtilities.loadImage
	    (iconDir + "memory_browser.png")); // NOI18N
    }

    protected String preferredID() {
        return this.getClass().getName();
    }

    protected void componentShowing () {
        super.componentShowing ();
	// 6661013, always connect to current debugger
	connectToDebugger(DebuggerManager.get().currentDebugger());
        needInitData=true;
        updateWindow();
    }

    // interface TopComponent
    protected void componentHidden() {
	if (debugger != null) {
	    debugger.registerMemoryWindow(null);
	}
        super.componentHidden();
    }
    
    // interface TopComponent
    protected void componentClosed () {
	if (debugger != null) {
	    debugger.registerMemoryWindow(null);
	}
        super.componentClosed();
    }

    public void setControlPanelData (String start, String length, int index) {
	memory_start = start;
	memory_length = length;
	memory_format = index;
        controlAddressCombo.getEditor().setItem(start);
	controlLengthText.setText(length);
        controlFormatCombo.setSelectedIndex(index);
    }
    
    public void setDebugger (NativeDebugger debugger) {
	this.debugger = debugger;
    }

    protected void connectToDebugger (NativeDebugger debugger) {
	this.debugger = debugger;
	if (debugger == null) return;
	debugger.registerMemoryWindow(this);
    }

    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
            
    public String getName () {
        return (name);
    }
    
    public String getToolTipText () {
        return (view_name);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String ac = actionEvent.getActionCommand();
        if (ac.equals("comboBoxChanged")) {	// NOI18N
            JComboBox cb = (JComboBox) actionEvent.getSource();
            memory_start = (String) cb.getSelectedItem();
        }
        updateMems();
    }
    
    boolean validate (String length) {
        if (memory_start == null)
	    return false;
	if (length == null)
	    return false;
	if (memory_start.length() == 0 || length.length() == 0)
	    return false;
	return true;
    }

    private void updateMems() {
        memory_length = controlLengthText.getText();
        memory_format = controlFormatCombo.getSelectedIndex();
	if (validate(memory_length))
	    if (debugger != null) {
		debugger.requestMems(memory_start, memory_length, 
			    short_memory_formats[memory_format], 
			    memory_format);
	    }
    }

    private int addrMap(String addr) {
        for (int i = 0; i < controlAddressCombo.getItemCount(); i++ ) {
            if (addr.equals((String)controlAddressCombo.getItemAt(i))) {
		return i;
            }
        }
	return -1; // not found
    }

    public void updateData(List<String> memLines) {
        if ((memLines == null) || (memLines.isEmpty())) {
	    return;
        }

	// Add new address to addressList
	int index = addrMap(memory_start);
	if (index == -1) {
	    // not found
            // Add address to the controlAddressCombo
            controlAddressCombo.addItem(memory_start);
	}

        current_addrs.clear();
        current_addrs.addAll(memLines);
        
        updateWindow();
    }
    
    public String align_memvalue(String memvalue) {
        int i, j, k, valuelen, maxvaluelen, total_len;
        String value, new_memvalue;
        char c;
        
        if ((memory_format == 3) || (memory_format == 4)) {
            total_len = memvalue.length();
            j = memvalue.indexOf(':', 0);
            new_memvalue = "";		// NOI18N
       	    if (j >= 0) {
       	        // Symbol information
       	        j++;
       	        new_memvalue = memvalue.substring(0, j);
       	    } else {
       	        j = 0;
       	    }
       	    for (i = 0 ; i < 4 ; i++) {
       	        for ( ; j < total_len ; j++) {
       	            c = memvalue.charAt(j);
       	            if ((c == ' ') || (c == '\t')) {
       	                new_memvalue = new_memvalue + " ";	// NOI18N
       	                continue;
       	            }
       	            break;
       	        }
       	        if (total_len <= j) break;
       	        value = "";	// NOI18N
       	        valuelen = 0;
       	        maxvaluelen = 12;
       	        if (memory_format == 3) {
       	            if (memvalue.charAt(j) == '-') {
       	                value = "-"; // NOI18N
       	                j++;
       	            } else {
       	                value = "+"; // NOI18N
       	            }
       	            valuelen = 1;
       	            maxvaluelen = 11;
       	        }
       	        for ( ; j < total_len ; j++) {
       	            c = memvalue.charAt(j);
       	            if ((c >= '0') && (c <= '9')) {
       	                value = value + c;
       	                valuelen++;
       	                continue;
       	            }
       	            break;
       	        }
       	        if (valuelen > maxvaluelen) return memvalue; // something wrong
       	        for ( ; valuelen < maxvaluelen ; valuelen++) {
       	            value = " " + value; // NOI18N
       	        }
       	        new_memvalue = new_memvalue + value;
            }
            return new_memvalue;
        }
        return memvalue;
    }
    
    private void updateWindow () {
        int i, k, carpos;
        
        if (tree == null) {
            ta = new JTextArea();
            ta_sp = new JScrollPane(ta);
            current_addrs = new ArrayList<String>();
            setLayout (new BorderLayout ());
            tree = Models.createView (Models.EMPTY_MODEL);
            tree.setName (view_name);
            
            ta.setEditable(false);
            ta.setWrapStyleWord(false);
            Font f = ta.getFont();
            ta.setFont(new Font("Monospaced", f.getStyle(), f.getSize())); //NOI18N

	    /* OLD
	    //
	    // header
	    //

            headerPanel = new JPanel(new BorderLayout());
            JLabel headerAddressLabel =
		new JLabel(Catalog.get("LBL_AddressCol")); 	// NOI18N
            headerAddressLabel.setToolTipText(Catalog.get("TIP_AddressCol")); 	// NOI18N
            headerPanel.add(headerAddressLabel, BorderLayout.WEST);

            JLabel headerValueLabel =
		new JLabel(Catalog.get("LBL_ValueCol")); 	// NOI18N
            headerValueLabel.setToolTipText(Catalog.get("TIP_ValueCol")); // NOI18N
            headerPanel.add(headerValueLabel, BorderLayout.CENTER);
	    */
            
            // Default settings
            memory_start = "main"; // NOI18N
            memory_length = "80";  // NOI18N
            memory_format = 1;
            
	    //
	    // control
	    //
            controlPanel = new JPanel(new java.awt.GridBagLayout());
            controlPanel.setToolTipText(Catalog.get("TIP_MemControlPanel"));	// NOI18N
	    
            controlAddressCombo = new JComboBox();
            controlAddressCombo.addItem(memory_start);
            controlAddressCombo.setEditable(true);
            controlAddressCombo.addActionListener(this);

            JLabel controlAddressLabel =
		new JLabel(Catalog.get("Mem_LBL_Address"));	// NOI18N
            controlAddressLabel.setToolTipText(Catalog.get("TIP_MemAddress"));	// NOI18N

            JLabel controlLengthLabel =
		new JLabel(Catalog.get("Mem_LBL_Length"));	// NOI18N
            controlLengthLabel.setToolTipText(Catalog.get("TIP_MemLength"));	// NOI18N
            controlLengthText = new JTextField(6);
            controlLengthText.setText(memory_length);
            controlLengthText.addActionListener(this);

            JLabel controlFormatLabel =
		new JLabel(Catalog.get("Mem_LBL_Format"));	// NOI18N
            controlFormatLabel.setToolTipText(Catalog.get("TIP_MemFormat"));	// NOI18N
	    format_listener = new FormatListener();
            controlFormatCombo = new JComboBox(memory_formats);
            controlFormatCombo.setSelectedIndex(memory_format);
            controlFormatCombo.addActionListener(format_listener);

	    // 6754292
            java.awt.GridBagConstraints gridBagConstraints ;
            int gridx = 0;

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = gridx++;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
            controlPanel.add(controlAddressLabel,gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = gridx++;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            gridBagConstraints.weightx = 1.0;
            controlPanel.add(controlAddressCombo, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = gridx++;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            controlPanel.add(controlLengthLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = gridx++;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            controlPanel.add(controlLengthText, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = gridx++;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            controlPanel.add(controlFormatLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = gridx++;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            controlPanel.add(controlFormatCombo, gridBagConstraints);
            
            // OLD tree.add(headerPanel, BorderLayout.NORTH);
            tree.add(ta_sp, BorderLayout.CENTER);
            tree.add(controlPanel, BorderLayout.SOUTH);
            AccessibleContext ac = tree.getAccessibleContext();
            ac.setAccessibleDescription(Catalog.get("ACSD_MemoryView")); // NOI18N
            ac.setAccessibleName(Catalog.get("TITLE_MemoryView")); // NOI18N
            add (tree, "Center");  //NOI18N

            // Create the popup menu.
            popup = new JPopupMenu();
            //Create listener
            popupListener = new PopupListener(popup);

            // Add FollowSelectedPointer
            menuItemFollowSelectedPointer =
		new JMenuItem(new FollowSelectedPointerAction());
            popup.add(menuItemFollowSelectedPointer);

            // Add refresh
            popup.add(new RefreshMemoryAction());
	    
            // Add listener
            ta.addMouseListener(popupListener);
            ta.setText(null);
            ta.setCaretPosition(0);
        }

        if (needInitData) {
            needInitData=false;
	    updateMems();
        }

        carpos = ta.getCaretPosition();
        ta.setText(null);
        ta.setCaretPosition(0);
       	for (String line : current_addrs) {
            ta.append(line);
        }
        try {
            ta.setCaretPosition(carpos);
        } catch (java.lang.IllegalArgumentException e) {
            // bad position carpos
            ta.setCaretPosition(0);
        }
        invalidate();
    }
    
    private class FormatListener implements ActionListener {

	// implement ActionListener
	public void actionPerformed(java.awt.event.ActionEvent ev) {

            String ac = ev.getActionCommand();
	    if (ac.equals("comboBoxChanged")) {		// NOI18N
		// Changed start address
		JComboBox cb = (JComboBox)ev.getSource();
		String s = (String)cb.getSelectedItem();
		for (int i=0; i < memory_formats.length; i++) {
		    if (memory_formats[i].equals(s)) {
			memory_format = i;
			controlFormatCombo.setSelectedIndex(i);
			updateMems();
		    }
		}
	    }
        }
    }

    class PopupListener extends MouseAdapter
                           implements ActionListener, 
                                      PopupMenuListener
    {
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
                selected_text = ta.getSelectedText();
                if (selected_text == null) {
                    menuItemFollowSelectedPointer.setEnabled(false);
                } else {
                    menuItemFollowSelectedPointer.setEnabled(true);
                }
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
        public void actionPerformed(ActionEvent ev) {
            JMenuItem source = (JMenuItem)(ev.getSource());
            String s = source.getText();
            for (int i=0; i < memory_formats.length; i++) {
                if (memory_formats[i].equals(s)) {
                    memory_format = i;
                    controlFormatCombo.setSelectedIndex(i);
                    updateMems();
                }
            }
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    class FollowSelectedPointerAction extends AbstractAction
    {
        public FollowSelectedPointerAction() {
            super(Catalog.get("Mem_ACT_Follow_Selected_Pointer"),// NOI18N
                new ImageIcon("paste.gif"));			// NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            FollowSelectedPointer(selected_text);
        }
    }

    class RefreshMemoryAction extends AbstractAction
    {
        public RefreshMemoryAction() {
            super(Catalog.get("Mem_ACT_Refresh"), 		// NOI18N
                new ImageIcon("paste.gif"));			// NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            String s=(String)((controlAddressCombo.getEditor()).getItem());
            if (s.length() > 0) {
                memory_start=s;
            }
            updateMems();
        }
    }

    /* LATER
    class ShowDynamicHelpPageAction extends AbstractAction
    {
        public ShowDynamicHelpPageAction() {
            super("More Info", 					// NOI18N
                new ImageIcon("help.gif"));			// NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            ShowDynamicHelpPage();
        }
    }
    */

    protected void FollowSelectedPointer(String s) {
        int i;
        // Remove all spaces and tabs at the beginning
        for (i=0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') continue;
            if (s.charAt(i) == '\t') continue;
            break;
        }
        if (i > 0) 
            if (i < s.length())
                s=s.substring(i);
                
        // Remove everything after address
        for (i=0; i < s.length(); i++) {
            if (s.charAt(i) == ':') break;
            if (s.charAt(i) == ';') break;
            if (s.charAt(i) == ' ') break;
            if (s.charAt(i) == '\t') break;
        }
        if (i > 0) 
            if (i < s.length())
                s=s.substring(0, i);

        if (s.length() > 0) {
            memory_start=s;
        }
        updateMems();
    }

    public HelpCtx getHelpCtx() {
	return new HelpCtx("MemoryBrowserWindow");
    }
}
