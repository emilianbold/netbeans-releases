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
 * GdbMemoryWindow.java
 *
 * Created on June 6, 2006, 3:15 PM
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.List;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

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

import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 *
 * Gdb Memory Window
 *
 */
public final class GdbMemoryWindow extends TopComponent
        implements ActionListener {
    
    /** generated Serialized Version UID */
    //static final long serialVersionUID = 8415779626223L;
    
    private transient JComponent tree = null;
    private String name;
    private String view_name;
    private GdbDebugger debugger = null;
    private JMenuItem menuItemFollowSelectedPointer;
    private JMenuItem menuItemHideText;
    private JPopupMenu popup;
    private JRadioButtonMenuItem rbMenuItem;
    private JTextArea ta;
    private JScrollPane ta_sp;
    private JPanel hp;
    private JPanel cp;
    private List previous_addrs;
    private List current_addrs;
    private PopupListener popupListener;
    private String memory_start;
    private String memory_length;
    private JTextField memory_length_jtf;
    private String[] memory_formats = {
        getString("L_Hexadecimal"),   //NOI18N
        getString("l_Hexadecimal"),   //NOI18N
        getString("w_Hexadecimal"),   //NOI18N
        getString("l_Decimal"),       //NOI18N
        // getString("w_Decimal"),    //NOI18N
        getString("l_Octal"),         //NOI18N
        // getString("w_Octal"),      //NOI18N
        getString("L_Float"),         //NOI18N
        getString("l_Float"),         //NOI18N
        getString("L_Instructions"),  //NOI18N
        getString("L_Characters"),    //NOI18N
        getString("L_WideCharacters"),//NOI18N
    };
    private String[] short_memory_formats = {
        "lX",       //NOI18N
        "X",        //NOI18N
        "x",        //NOI18N
        "D",        //NOI18N
        // "d",        //NOI18N
        "O",        //NOI18N
        // "o",        //NOI18N
        "F",        //NOI18N
        "f",        //NOI18N
        "i",        //NOI18N
        "c",        //NOI18N
        "w",        //NOI18N
    };
    private JComboBox format_jcb;
    private int memory_format = 1;
    private boolean needInitData=true;
    private boolean dontShowText=true;
    private JComboBox cp_addressList;
    private String selected_text = null;
    
    /** 
     * Creates a new instance of GdbMemoryWindow
     */
    public GdbMemoryWindow() {
        name = getString("TITLE_GdbMemoryWindow");    //NOI18N
        view_name = getString("TITLE_GdbMemoryView"); //NOI18N
        super.setName(name);
        //setIcon(org.openide.util.Utilities.loadImage
        //    ("com/sun/tools/dbxgui/icons/access.gif")); // NOI18N
    }

    /** 
     * Creates a new instance of GdbMemoryWindow
     * or returns an existing one.
     */
    public static TopComponent getDefault() {
        Iterator it = WindowManager.getDefault().getModes().iterator();
        
        while (it.hasNext()) {
            Mode m = (Mode) it.next();
            TopComponent[] tcs = m.getTopComponents();
            int i, k = tcs.length;
            for (i = 0; i < k; i++) {
                if (tcs[i].getClass().equals(GdbMemoryWindow.class)) {
                    return tcs[i];
                }
            }
        }
        return new GdbMemoryWindow();
    }
    
    protected String preferredID() {
        return this.getClass().getName();
    }
    
    protected void componentShowing() {
        super.componentShowing();
        //connectToDebugger();
        needInitData=true;
        updateWindow();
    }
    
    protected void componentClosed() {
        if (debugger != null) {
            //debugger.setGdbMemoryWindow(false);
            //debugger.registerGdbMemoryWindow(null);
        }
        super.componentClosed();
    }
    
    protected void connectToDebugger(GdbDebugger debugger) {
        this.debugger = debugger;
        if (debugger == null) return;
        //debugger.registerGdbMemoryWindow(this);
        //debugger.setGdbMemoryWindow(true);
    }
    
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
    
    public String getName() {
        return (name);
    }
    
    public String getToolTipText() {
        return (view_name);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
//System.out.println("GdbMemoryWindow.actionPerformed("+actionEvent+")  getActionCommand()="+actionEvent.getActionCommand());
        String ac = actionEvent.getActionCommand();
        //System.out.println("  GdbMemoryWindow.actionPerformed()  actionEvent.getActionCommand()="+ac);
        if (ac.equals("comboBoxEdited")) { //NOI18N
            // Changed start address
            JComboBox cb = (JComboBox)actionEvent.getSource();
            memory_start=(String)cb.getSelectedItem();
        } else {
            // Changed length or output format
            //memory_length = memory_length_jtf.getText();
            //System.out.println("  GdbMemoryWindow.actionPerformed()  memory_length="+memory_length);
        }
        showMemory();
        // super.actionPerformed(actionEvent);
    }
    
    private void showMemory() {
        String start=memory_start;
        // Search if it was already in cp_addressList
        boolean found = false;
        for (int i = 0; i < cp_addressList.getItemCount(); i++ ) {
            if (start.compareTo((String)cp_addressList.getItemAt(i)) ==
                    0) {
                found = true;
                break;
            }
        }
        if (!found) {
            // Add address to the cp_addressList
            cp_addressList.addItem(start);
        }
        //memory_start=start;
        updateData(readData());
    }
    
    private String readData() {
        // Get data from debugger
        if (debugger == null) {
            return null;
        }
        memory_length = memory_length_jtf.getText();
        memory_format = format_jcb.getSelectedIndex();
        String mem = null; // debugger.getMemoryData(memory_start, memory_length, short_memory_formats[memory_format]);
        return(mem);
    }
    
    private void initData() {
        updateData(readData());
    }
    
    public void updateData(String mem) {
        int i, j, k, l, cp, memaddrlen;
        String s, memaddr, memvalue;
        
        if (mem == null) return;
        current_addrs.removeAll();
        l = 1;
        for (i = 0; i < mem.length(); i++, l++) {
            k = mem.indexOf('\n', i);
            if (k < i) break;
            s = mem.substring(i, k + 1);
            i = k;
            memaddr = null;
            memvalue = null;
            memaddrlen = 0;
            for (j=0, k=0; j < s.length(); j++) {
                if (s.charAt(j) != ' ') {
                    if (k == 0) {
                        k = s.indexOf(' ', j);
                        if (k < j) break;
                        memaddrlen = k - j;
                        memaddr = s.substring(j, k);
                        j = k;
                    } else {
                        k = s.indexOf('\n', j);
                        if (k < j) break;
                        memvalue = s.substring(j, k);
                        memvalue = align_memvalue(memvalue);
                        break;
                    }
                }
            }
            current_addrs.add("   " + memaddr + "  " + memvalue + "\n"); //NOI18N
        }
        updateWindow();
    }
    
    private String align_memvalue(String memvalue) {
        int i, j, k, valuelen, maxvaluelen, total_len;
        String value, new_memvalue;
        char c;
        
        if ((memory_format == 3) || (memory_format == 4)) {
            // if (memory_format == 3) it means 4 words: decimal, signed, 4 bytes - align to 11 characters
            // if (memory_format == 4) it means 4 words: octal, unsigned, 4 bytes - align to 11 characters
            
//System.out.println("GdbMemoryWindow.align_memvalue("+memvalue+")  memory_format="+memory_format);
            total_len = memvalue.length();
            j = memvalue.indexOf(':', 0);
            new_memvalue = ""; // NOI18N
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
                        new_memvalue = new_memvalue + ' ';
                        continue;
                    }
                    break;
                }
                if (total_len <= j) break;
                value = ""; // NOI18N
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
            //System.out.println(" GdbMemoryWindow.align_memvalue()  return new_memvalue = "+new_memvalue);
            return new_memvalue;
        }
        return memvalue;
    }
    
    private void updateWindow() {
        int i, k, carpos;
        
        if (tree == null) {
            ta = new JTextArea();
            ta_sp = new JScrollPane(ta);
            previous_addrs = new List();
            current_addrs = new List();
            setLayout(new BorderLayout());
            tree = Models.createView(Models.EMPTY_MODEL);
            tree.setName(view_name);
            
            ta.setEditable(false);
            ta.setWrapStyleWord(false);
            Font f = ta.getFont();
            ta.setFont(new Font("Monospaced", f.getStyle(), f.getSize())); //NOI18N
            
            hp = new JPanel(new BorderLayout());
            JLabel hp_name = new JLabel("        " + getString("LBL_HP_Name") + "                       "); //NOI18N
            JLabel hp_value = new JLabel("                                " + getString("LBL_HP_Value")); //NOI18N
            hp_name.setToolTipText(getString("TTT_HP_Name")); //NOI18N
            hp_value.setToolTipText(getString("TTT_HP_Value")); //NOI18N
            hp.add(hp_name, BorderLayout.WEST);
            hp.add(hp_value, BorderLayout.CENTER);
            
            // Default settings
            memory_start = "main"; //NOI18N
            memory_length = "80";  //NOI18N
            memory_format = 1;
            
            cp = new JPanel(new FlowLayout());
            cp.setToolTipText(getString("TTT_Control_Panel")); //NOI18N
            cp_addressList = new JComboBox();
            cp_addressList.addItem(memory_start);
            cp_addressList.setEditable(true);
            cp_addressList.addActionListener(this);
            JLabel cp_text1 = new JLabel(getString("LBL_CP_Text1")); //NOI18N
            cp_text1.setToolTipText(getString("TTT_CP_Text1")); //NOI18N
            JLabel cp_text2 = new JLabel(getString("LBL_CP_Text2")); //NOI18N
            cp_text2.setToolTipText(getString("TTT_CP_Text2")); //NOI18N
            memory_length_jtf = new JTextField(6);
            memory_length_jtf.setText(memory_length);
            memory_length_jtf.addActionListener(this);
            JLabel cp_text3 = new JLabel(getString("LBL_CP_Text3")); //NOI18N
            cp_text3.setToolTipText(getString("TTT_CP_Text3")); //NOI18N
            format_jcb = new JComboBox(memory_formats);
            format_jcb.setSelectedIndex(memory_format);
            format_jcb.addActionListener(this);
            cp.add(cp_text1);
            cp.add(cp_addressList);
            cp.add(cp_text2);
            cp.add(memory_length_jtf);
            cp.add(cp_text3);
            cp.add(format_jcb);
            
            tree.add(hp, BorderLayout.NORTH);
            tree.add(ta_sp, BorderLayout.CENTER);
            tree.add(cp, BorderLayout.SOUTH);
            AccessibleContext ac = tree.getAccessibleContext();
            ac.setAccessibleDescription(getString("AD_GdbMemoryView")); //NOI18N
            ac.setAccessibleName(getString("TITLE_GdbMemoryView")); // NOI18N
            add(tree, "Center");  //NOI18N
            
            //Create the popup menu.
            popup = new JPopupMenu();
            //Create listener
            popupListener = new PopupListener(popup);
            //Add FollowSelectedPointer
            menuItemFollowSelectedPointer = new JMenuItem(new FollowSelectedPointerAction());
            popup.add(menuItemFollowSelectedPointer);
            //Add a group of output format items
            popup.addSeparator();
            ButtonGroup group = new ButtonGroup();
            for (i=0; i < memory_formats.length; i++) {
                rbMenuItem = new
                        JRadioButtonMenuItem(memory_formats[i]);
                if (i == memory_format) rbMenuItem.setSelected(true);
                //rbMenuItem.setMnemonic(KeyEvent.VK_R);
                rbMenuItem.addActionListener(popupListener);
                group.add(rbMenuItem);
                popup.add(rbMenuItem);
            }
            //Add HideText
            popup.addSeparator();
            menuItemHideText = new JMenuItem(new HideTextAction());
            popup.add(menuItemHideText);
            //Add refresh
            popup.addSeparator();
            popup.add(new RefreshMemoryAction());
            //Add MoreInfo
            popup.addSeparator();
            popup.add(new ShowDynamicHelpPageAction());
            //Add listener
            ta.addMouseListener(popupListener);
            ta.setText(null);
            ta.setCaretPosition(0);
        }
        if (needInitData) {
            // Init page
            needInitData=false;
            initData();
        }
        carpos = ta.getCaretPosition();
        ta.setText(null);
        ta.setCaretPosition(0);
        k = current_addrs.getItemCount();
        for (i = 0; i < k; i++) {
            // ta.setCaretColor(Color.RED);  // mark updated values
            ta.append(current_addrs.getItem(i));
            // ta.setCaretColor(null);  // restore default color
        }
        try {
            ta.setCaretPosition(carpos);
        } catch (java.lang.IllegalArgumentException e) {
            // bad position carpos
            ta.setCaretPosition(0);
        }
        invalidate();
    }
    
    class PopupListener extends MouseAdapter
            implements ActionListener,
            PopupMenuListener {
        JPopupMenu popup;
        
        PopupListener(JPopupMenu popupMenu) {
//System.out.println("GdbMemoryWindow.PopupListener("+popupMenu+") created");
            popup = popupMenu;
        }
        
        public void mousePressed(MouseEvent e) {
//System.out.println("GdbMemoryWindow.PopupListener.mousePressed("+e+")");
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
//System.out.println("GdbMemoryWindow.PopupListener.mouseReleased("+e+")");
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
//System.out.println("GdbMemoryWindow.PopupListener.maybeShowPopup("+e+")");
            if (e.isPopupTrigger()) {
//System.out.println("GdbMemoryWindow.PopupListener.maybeShowPopup  popup.show X="+e.getX()+" Y="+e.getY());
                if (dontShowText == true) {
                    menuItemHideText.setEnabled(false);
                } else {
                    menuItemHideText.setEnabled(true);
                }
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
//System.out.println("GdbMemoryWindow.PopupListener.actionPerformed("+ev+")");
            JMenuItem source = (JMenuItem)(ev.getSource());
//System.out.println("    Event source: " + source.getText());
            String s = source.getText();
            for (int i=0; i < memory_formats.length; i++) {
                if (memory_formats[i].compareTo(s) == 0) {
                    memory_format = i;
                    format_jcb.setSelectedIndex(i);
                    showMemory();
                }
            }
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//System.out.println("GdbMemoryWindow.PopupListener.popupMenuWillBecomeInvisible("+e+")");
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//System.out.println("GdbMemoryWindow.PopupListener.popupMenuWillBecomeVisible("+e+")");
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
//System.out.println("GdbMemoryWindow.PopupListener.popupMenuCanceled("+e+")");
        }
    }
    
    class FollowSelectedPointerAction extends AbstractAction {
        public FollowSelectedPointerAction() {
            super("Follow Selected Pointer", new ImageIcon("paste.gif")); //NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            FollowSelectedPointer(selected_text);
        }
    }
    
    class RefreshMemoryAction extends AbstractAction {
        public RefreshMemoryAction() {
            super("Refresh", new ImageIcon("paste.gif")); //NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
            String s=(String)((cp_addressList.getEditor()).getItem());
            if (s.length() > 0) {
                memory_start=s;
            }
            showMemory();
        }
    }
    
    class HideTextAction extends AbstractAction {
        public HideTextAction() {
            super("Hide Text", new ImageIcon("cut.gif")); //NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
//System.out.println("HideTextAction.ActionPerformed(Hide Text)");
        }
    }
    
    class ShowDynamicHelpPageAction extends AbstractAction {
        public ShowDynamicHelpPageAction() {
            super("More Info", new ImageIcon("help.gif")); //NOI18N
        }
        public void actionPerformed(ActionEvent ev) {
//System.out.println("ShowDynamicHelpPageAction.ActionPerformed(More Info)");
            ShowDynamicHelpPage();
        }
    }
    
    protected void FollowSelectedPointer(String s) {
        int i;
//System.out.println("FollowSelectedPointer("+s+")");
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
        showMemory();
    }
    
    protected void ShowDynamicHelpPage() {
        //HelpManager.getDefault().showDynaHelp("sparc-regs");
    }
    
    // ------------------ Private support methods --------------------
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(GdbMemoryWindow.class);
        }
        if (bundle == null) return s; // FIXUP
        return bundle.getString(s);
    }
    
} /* End of class GdbMemoryWindow */



/*
 * http://sourceware.org/gdb/current/onlinedocs/gdb_25.html
 *
 
The -data-read-memory Command
 
Synopsis
 
 
 
 -data-read-memory [ -o byte-offset ]
   address word-format word-size
   nr-rows nr-cols [ aschar ]
 
where:
 
`address'
    An expression specifying the address of the first memory word to be
read. Complex expressions containing embedded white space should be
quoted using the C convention.
 
`word-format'
    The format to be used to print the memory words. The notation is
the same as for GDB's print command (see section Output formats).
 
`word-size'
    The size of each memory word in bytes.
 
`nr-rows'
    The number of rows in the output table.
 
`nr-cols'
    The number of columns in the output table.
 
`aschar'
    If present, indicates that each row should include an ASCII dump.
The value of aschar is used as a padding character when a byte is not a
member of the printable ASCII character set (printable ASCII characters
are those whose code is between 32 and 126, inclusively).
 
`byte-offset'
    An offset to add to the address before fetching memory.
 
This command displays memory contents as a table of nr-rows by nr-cols
words, each word being word-size bytes. In total, nr-rows * nr-cols *
word-size bytes are read (returned as `total-bytes'). Should less than
the requested number of bytes be returned by the target, the missing
words are identified using `N/A'. The number of bytes read from the target
is returned in `nr-bytes' and the starting address used to read memory
in `addr'.
 
The address of the next/previous row or page is available in `next-row'
and `prev-row', `next-page' and `prev-page'.
 
GDB Command
 
The corresponding GDB command is `x'. gdbtk has `gdb_get_mem' memory
read command.
 
Example
 
Read six bytes of memory starting at bytes+6 but then offset by -6
bytes. Format as three rows of two columns. One byte per word. Display each
word in hex.
 
 
 
(gdb)
9-data-read-memory -o -6 -- bytes+6 x 1 3 2
9^done,addr="0x00001390",nr-bytes="6",total-bytes="6",
next-row="0x00001396",prev-row="0x0000138e",next-page="0x00001396",
prev-page="0x0000138a",memory=[
{addr="0x00001390",data=["0x00","0x01"]},
{addr="0x00001392",data=["0x02","0x03"]},
{addr="0x00001394",data=["0x04","0x05"]}]
(gdb)
 
Read two bytes of memory starting at address shorts + 64 and display as
a single word formatted in decimal.
 
 
 
(gdb)
5-data-read-memory shorts+64 d 2 1 1
5^done,addr="0x00001510",nr-bytes="2",total-bytes="2",
next-row="0x00001512",prev-row="0x0000150e",
next-page="0x00001512",prev-page="0x0000150e",memory=[
{addr="0x00001510",data=["128"]}]
(gdb)
 
Read thirty two bytes of memory starting at bytes+16 and format as
eight rows of four columns. Include a string encoding with `x' used as the
non-printable character.
 
 
 
(gdb)
4-data-read-memory bytes+16 x 1 8 4 x
4^done,addr="0x000013a0",nr-bytes="32",total-bytes="32",
next-row="0x000013c0",prev-row="0x0000139c",
next-page="0x000013c0",prev-page="0x00001380",memory=[
{addr="0x000013a0",data=["0x10","0x11","0x12","0x13"],ascii="xxxx"},
{addr="0x000013a4",data=["0x14","0x15","0x16","0x17"],ascii="xxxx"},
{addr="0x000013a8",data=["0x18","0x19","0x1a","0x1b"],ascii="xxxx"},
{addr="0x000013ac",data=["0x1c","0x1d","0x1e","0x1f"],ascii="xxxx"},
{addr="0x000013b0",data=["0x20","0x21","0x22","0x23"],ascii=" !\"#"},
{addr="0x000013b4",data=["0x24","0x25","0x26","0x27"],ascii="$%&'"},
{addr="0x000013b8",data=["0x28","0x29","0x2a","0x2b"],ascii="()*+"},
{addr="0x000013bc",data=["0x2c","0x2d","0x2e","0x2f"],ascii=",-./"}]
(gdb)
 
 */