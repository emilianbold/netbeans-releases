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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.accessibility.AccessibleContext;

import org.netbeans.spi.viewmodel.Models;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.Catalog;


public final class EvaluationWindow extends TopComponent
    implements ActionListener
{

    /** generated Serialized Version UID */
    static final String preferredID = "EvaluationWindow"; // NOI18N
    static EvaluationWindow DEFAULT;

    private transient JComponent tree = null;
    private String name;
    private String view_name;
    private NativeDebugger debugger = null;
    private JMenuItem menuItemFollowSelectedPointer;
    private JMenuItem menuItemClear;
    private JPopupMenu popup;
    private JRadioButtonMenuItem rbMenuItem;
    private JTextArea ta;
    private JScrollPane ta_sp;
    private JPanel hp;
    private JPanel cp;
    //private ArrayList<String> current_addrs;
    private PopupListener popupListener;
    private String expr;
    private JTextField memory_length_jtf;
    private String[] formats = { 
                                        Catalog.get("Default_format"),    //NOI18N
                                        Catalog.get("l_Hexadecimal"),    //NOI18N
                                        Catalog.get("L_Hexadecimal"),    //NOI18N
                                        Catalog.get("l_Decimal"),        //NOI18N
                                        Catalog.get("L_Decimal"),        //NOI18N
                                        Catalog.get("l_U_Decimal"),        //NOI18N
                                        Catalog.get("L_U_Decimal"),        //NOI18N
                                        Catalog.get("l_Float"),          //NOI18N
                                        Catalog.get("L_Float"),          //NOI18N
					/*
                                        Catalog.get("w_Hexadecimal"),    //NOI18N
                                     // Catalog.get("w_Decimal"),        //NOI18N
                                        Catalog.get("l_Octal"),          //NOI18N
                                     // Catalog.get("w_Octal"),          //NOI18N
                                        Catalog.get("L_Float"),          //NOI18N
                                        Catalog.get("l_Float"),          //NOI18N
                                        Catalog.get("L_Instructions"),   //NOI18N
                                        Catalog.get("L_Characters"),     //NOI18N
                                        Catalog.get("L_WideCharacters"), //NOI18N
					*/
                                      };
    private String[] short_formats = {
                                              " ",       //NOI18N
                                              "-fx ",        //NOI18N
                                              "-flx ",        //NOI18N
                                              "-fd ",        //NOI18N
                                              "-fld ",        //NOI18N
                                              "-fu ",        //NOI18N
                                              "-flu ",        //NOI18N
                                              "(float) ",        //NOI18N
                                              "(double) ",        //NOI18N
                                             };
    private JComboBox format_jcb;
    private FormatListener format_listener;
    private int format;
    private boolean dontShowText=true;
    private JComboBox exprList;
    private String selected_text = null;

    public static EvaluationWindow getDefault() {
        if (DEFAULT == null) {
            EvaluationWindow tc = (EvaluationWindow) WindowManager.getDefault().findTopComponent(preferredID);
            if (tc == null)
                new EvaluationWindow();
        }
        return DEFAULT;
    }
    
    public EvaluationWindow() {
	name = Catalog.get("TITLE_EvaluationWindow");    //NOI18N
	view_name = Catalog.get("TITLE_EvaluationView"); //NOI18N
	super.setName(name);
	DEFAULT = this;
	final String iconDir = "org/netbeans/modules/cnd/debugger/common2/icons/";//NOI18N
	setIcon(org.openide.util.ImageUtilities.loadImage
	    (iconDir + "evaluate_expression.png")); // NOI18N
    }

    protected String preferredID() {
        return this.getClass().getName();
    }

    protected void componentHidden () {
	if (exprList != null)
            exprList.setSelectedIndex(0);
	if (debugger != null) {
	    debugger.registerEvaluationWindow(null);
        }
    }
    
    public void componentShowing () {
        super.componentShowing ();
	connectToDebugger(DebuggerManager.get().currentDebugger());
        updateWindow();
    }

    protected void componentClosed () {
        super.componentClosed();
	if (debugger != null) {
	    debugger.registerEvaluationWindow(null);
	    //tree = null;
	    //current_addrs.clear();
            ta.setText(null);
            ta.setCaretPosition(0);
	    //exprList.removeAllItems();
            exprList.setSelectedIndex(0);
	    //exprList = null;
	    //format_jcb.removeAllItems();
            //format_jcb.removeActionListener(format_listener);
            invalidate();
	}
    }

    protected void connectToDebugger (NativeDebugger debugger) {
	this.debugger = debugger;
	if (debugger == null) 
	    return;
	debugger.registerEvaluationWindow(this);
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
        if ((ac != null) && ac.equals("comboBoxChanged")) { // NOI18N
            JComboBox cb = (JComboBox)actionEvent.getSource();
	    if (cb != null) {
                expr = (String)cb.getSelectedItem();
	        if (expr != null && !expr.equals(""))
                    exprEval();
            }
        }
    }
    
    private void exprEval() {
        format = format_jcb.getSelectedIndex();
	if (expr != null && !expr.equals(""))
            debugger.exprEval(short_formats[format], expr);
    }

    private int exprMap(String expr) {
        for (int i = 0; i < exprList.getItemCount(); i++ ) {
            if (expr.equals((String)exprList.getItemAt(i))) {
                return i;
            }
        }
        return -1; // not found
    }

    public void evalResult(String result) {
        if (result == null) 
	    return; 
        if (result.length() == 0) 
	    result = " "; // NOI18N

	int i = result.indexOf(" ="); // NOI18N
	if (i == -1)
	    // 6574458
	    // non-expr, constant
	    i = result.length()-1;

	// 6708564 expr = result.substring(0, i);
	// Add expr to drop-down list
	int index = exprMap(expr);
	if (index == -1) {
	    // not found
	    // Add expr to drop-down list
	    /*
	    if (expr.length() < 40)
		// 6754292
		*/
		exprList.addItem(expr);
	}

	//exprList.setSelectedIndex(index);
        ta.append(result);
        updateWindow();
    }

    private void updateWindow () {
        int i, k;
        
        if (tree == null) {
            ta = new JTextArea();
            ta_sp = new JScrollPane(ta);
            //current_addrs = new ArrayList<String>();
            setLayout (new BorderLayout ());
            tree = Models.createView (Models.EMPTY_MODEL);
            tree.setName (view_name);
            
            ta.setEditable(false);
            ta.setWrapStyleWord(false);
            Font f = ta.getFont();
            ta.setFont(new Font("Monospaced", f.getStyle(), f.getSize())); //NOI18N
            hp = new JPanel(new BorderLayout());
/*
            JLabel hp_name = new JLabel("        Expression                       "); 
            JLabel hp_value = new JLabel("                                Value"); 
            hp_name.setToolTipText("Expression to be evaluated"); 
            hp_value.setToolTipText("Expression value");             
            hp.add(hp_name, BorderLayout.WEST);
            hp.add(hp_value, BorderLayout.CENTER);
  */          

            // Default settings
            expr = ""; //NOI18N
            format = 0;
            
            //cp = new JPanel(new FlowLayout());
	    cp = new JPanel(new java.awt.GridBagLayout());

            cp.setToolTipText("Control panel to specify Expression. Use pop-up menu to specify output format."); // NOI18N
            JLabel cp_text1 = new JLabel(Catalog.get("LBL_Expression"));
            cp_text1.setToolTipText(Catalog.get("HINT_Expression"));
            exprList = new JComboBox();
	    exprList.setMaximumSize(cp.getPreferredSize());
            exprList.addItem(expr);
            exprList.setEditable(true);
            exprList.addActionListener(this);

            JLabel cp_text3 = new JLabel(Catalog.get("LBL_Format"));
            cp_text3.setToolTipText(Catalog.get("HINT_Output_format"));
	    format_listener = new FormatListener();
            format_jcb = new JComboBox(formats);
            format_jcb.setSelectedIndex(format);
            format_jcb.addActionListener(format_listener);

	    java.awt.GridBagConstraints gridBagConstraints ;
	    int gridx = 0;
	    
	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
            cp.add(cp_text1, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
	    gridBagConstraints.weightx = 1.0;
            cp.add(exprList, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            cp.add(cp_text3, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            cp.add(format_jcb, gridBagConstraints);
            
            tree.add(hp, BorderLayout.NORTH);
            tree.add(ta_sp, BorderLayout.CENTER);
            tree.add(cp, BorderLayout.SOUTH);
            AccessibleContext ac = tree.getAccessibleContext();
            ac.setAccessibleDescription("Window to view  expression"); // NOI18N
            ac.setAccessibleName(Catalog.get("TITLE_EvaluationView")); // NOI18N
            add (tree, "Center");  //NOI18N

            //Create the popup menu.
            popup = new JPopupMenu();

            //Create listener
            popupListener = new PopupListener(popup);

            //Add Clear
            menuItemClear = new JMenuItem(new ClearBufferAction());
            popup.add(menuItemClear);

            //Add FollowSelectedPointer
	    /*
            menuItemFollowSelectedPointer = new JMenuItem(new FollowSelectedPointerAction());
            popup.add(menuItemFollowSelectedPointer);
	    */
            
            //Add listener
            ta.addMouseListener(popupListener);
            ta.setText(null);
            ta.setCaretPosition(0);
        }
/*
       	k = current_addrs.size();
	if (k > 0)
            ta.append(current_addrs.get(k-1));
	    */

        invalidate();
    }

    private class FormatListener implements ActionListener {

        // implement ActionListener
        public void actionPerformed(java.awt.event.ActionEvent ev) {

            String ac = ev.getActionCommand();
            if (ac.equals("comboBoxChanged")) { // NOI18N
                // Changed start address
                JComboBox cb = (JComboBox)ev.getSource();
                String s = (String)cb.getSelectedItem();
                for (int i=0; i < formats.length; i++) {
                    if (formats[i].equals(s)) {
                        format = i;
                        format_jcb.setSelectedIndex(i);
                        exprEval();
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
	    /*
                selected_text = ta.getSelectedText();
                if (selected_text == null) {
                    menuItemFollowSelectedPointer.setEnabled(false);
                } else {
                    menuItemFollowSelectedPointer.setEnabled(true);
                }
	    */
                menuItemClear.setEnabled(true);
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }

        public void actionPerformed(ActionEvent ev) {
            JMenuItem source = (JMenuItem)(ev.getSource());
            String s = source.getText();
            for (int i=0; i < formats.length; i++) {
                if (formats[i].equals(s)) {
                    format = i;
                    format_jcb.setSelectedIndex(i);
                    exprEval();
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

    class ClearBufferAction extends AbstractAction {
        public ClearBufferAction() {
            super("Clear", // NOI18N
                new ImageIcon("org/netbeans/modules/cnd/debugger/common2/icons/Pointers.gif")); // NOI18N
        }

        public void actionPerformed(ActionEvent ev) {
            ta.setText(null);
            ta.setCaretPosition(0);
        }
    }
    
    class FollowSelectedPointerAction extends AbstractAction
    {
        public FollowSelectedPointerAction() {
            super("Follow Selected Pointer", // NOI18N
                new ImageIcon("paste.gif")); // NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            FollowSelectedPointer(selected_text);
        }
    }
/*
    class RefreshEvaluationAction extends AbstractAction
    {
        public RefreshEvaluationAction() {
            super("Refresh", 
                new ImageIcon("paste.gif"));
        }
        public void actionPerformed(ActionEvent ev) {
            String s=(String)((exprList.getEditor()).getItem());
            if (s.length() > 0) {
                expr = s;
            }
            exprEval();
        }
    }

    class HideTextAction extends AbstractAction
    {
        public HideTextAction() {
            super("Hide Text", 
                new ImageIcon("cut.gif"));
        }
        public void actionPerformed(ActionEvent ev) {
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
            expr = s;
        }
        exprEval();
    }

    public HelpCtx getHelpCtx() {
	return new HelpCtx("EvaluationWindow");
    }

}
