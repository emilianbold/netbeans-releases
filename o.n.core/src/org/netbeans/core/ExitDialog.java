/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.Dimension;
import java.util.Iterator;
import java.beans.BeanInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;

import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFolder;

/** Dialog which lets the user select which open files to close.
 *
 * @author  Ian Formanek, Petr Hrebejk
 */

class ExitDialog extends JPanel implements java.awt.event.ActionListener {


    private static JButton[] exitOptions;

    private static Object[] secondaryExitOptions;

    /** The dialog */
    private static java.awt.Dialog exitDialog;

    /** Result of the dialog */
    private static boolean result = false;

    JList list;
    DefaultListModel listModel;

    static final long serialVersionUID = 6039058107124767512L;

    /** Constructs new dlg */
    public ExitDialog () {
        setLayout (new java.awt.BorderLayout ());

        listModel = new DefaultListModel();
        Iterator iter = DataObject.getRegistry ().getModifiedSet ().iterator();
        while (iter.hasNext()) {
            DataObject obj = (DataObject) iter.next();
            listModel.addElement(obj);
        }
        draw ();
    }
    
    /** Constructs new dlg for unsaved files in filesystems marked 
     * for unmount.
    */
    public ExitDialog (Node[] activatedNodes) {
        setLayout (new java.awt.BorderLayout ());

        listModel = new DefaultListModel();
        Iterator iter = getModifiedActSet (activatedNodes).iterator();
        while (iter.hasNext()) {
            DataObject obj = (DataObject) iter.next();
            listModel.addElement(obj);
        }
        draw ();
    }

    /** Constructs rest of dialog.
    */
    private void draw () {
        list = new JList(listModel);
        list.setBorder(new EmptyBorder(2, 2, 2, 2));
        list.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
                                           public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
                                               updateSaveButton ();
                                           }
                                       }
                                      );
        updateSaveButton ();
        JScrollPane scroll = new JScrollPane (list);
        scroll.setBorder (new CompoundBorder (new EmptyBorder (5, 5, 5, 5), scroll.getBorder ()));
        add(scroll, java.awt.BorderLayout.CENTER);
        list.setCellRenderer(new ExitDlgListCellRenderer());
    }
    
    private void updateSaveButton () {
        exitOptions [0].setEnabled (list.getSelectedIndex () != -1);
    }

    /** @return preffered size */
    public Dimension getPreferredSize() {
        Dimension prev = super.getPreferredSize();
        return new Dimension(Math.max(300, prev.width), Math.max(150, prev.height));
    }

    /** This method is called when is any of buttons pressed
    */
    public void actionPerformed(final java.awt.event.ActionEvent evt ) {
        if (exitOptions[0].equals (evt.getSource ())) {
            save(false);
        } else if (exitOptions[1].equals (evt.getSource ())) {
            save(true);
        } else if (exitOptions[2].equals (evt.getSource ())) {
            theEnd();
        } else if (secondaryExitOptions[0].equals (evt.getSource ())) {
            exitDialog.setVisible (false);
        }
    }

    /** Save the files from the listbox
    * @param all true- all files, false - just selected
    */
    private void save(boolean all) {
        if (all) {
            SaveCookie sc = null;
            for (int i = listModel.size() - 1; i >= 0; i--) {
                try {
                    DataObject obj = (DataObject) listModel.getElementAt(i);
                    sc = (SaveCookie)obj.getCookie(SaveCookie.class);
                    if (sc != null) sc.save();
                    listModel.removeElement(obj);
                }
                catch (java.io.IOException e) {
                    saveExc(e);
                }
            }
        }
        else {
            Object[] array = list.getSelectedValues();
            SaveCookie sc = null;
            for (int i = 0; i < array.length; i++) {
                try {
                    sc = (SaveCookie)
                         (((DataObject)array[i]).getCookie(SaveCookie.class));
                    if (sc != null) sc.save();
                    listModel.removeElement(array[i]);
                }
                catch (java.io.IOException e) {
                    saveExc(e);
                }
            }
        }
        if (listModel.isEmpty()) {
            theEnd();
        }
    }

    /** Exit the IDE
    */
    private void theEnd() {
        // XXX(-ttran) result must be set before calling setVisible(false)
        // because this will unblock the thread which called Dialog.show()
        
        for (int i = listModel.size() - 1; i >= 0; i--) {            
            DataObject obj = (DataObject) listModel.getElementAt(i);
            obj.setModified(false);
        }

        result = true;
        exitDialog.setVisible (false);
        exitDialog.dispose();
    }

    /** Notification about the save exception
    */
    private void saveExc(Exception e) {
        TopManager.getDefault().notify(
            new NotifyDescriptor.Exception(e,
                                           NbBundle.getBundle(ExitDialog.class).getString("EXC_Save"))
        );
    }


    /** Opens the ExitDialog for unsaved files in filesystems marked 
     * for unmount and blocks until it's closed. If dialog doesm't
     * exists it creates new one. Returns true if the IDE should be closed.
     */
    static boolean showDialog(Node[] activatedNodes) {
        return innerShowDialog( activatedNodes );        
    }
    
    /** Opens the ExitDialog and blocks until it's closed. If dialog doesm't
     * exists it creates new one. Returns true if the IDE should be closed.
     */
    static boolean showDialog() {
        return innerShowDialog( null );        
    }

    /** Returns modified set of DataObjects in filesystems marked 
     * for unmount.
     */
    private static java.util.Set getModifiedActSet (Node[] activatedNodes) {
        Iterator iter = DataObject.getRegistry ().getModifiedSet ().iterator();
        java.util.Set set = new java.util.HashSet();
        while (iter.hasNext()) {
            DataObject obj = (DataObject) iter.next();
            try {
                FileSystem fs = obj.getPrimaryFile().getFileSystem();
                for (int i=0;i<activatedNodes.length;i++) {
                    DataFolder df = (DataFolder)activatedNodes[i].getCookie(DataFolder.class);
                    if (df != null)
                        if (df.getPrimaryFile().getFileSystem().equals(fs)) {
                            set.add(obj);
                            break;
                        }
                }
            } catch (FileStateInvalidException fe) {
            }
        }
        return set;
    }
    
    /** Opens the ExitDialog for activated nodes or for
     * whole repository.
     */
    private static boolean innerShowDialog(Node[] activatedNodes) {
        java.util.Set set = null;
        if (activatedNodes != null)
            set = getModifiedActSet (activatedNodes);
        else
            set = org.openide.loaders.DataObject.getRegistry ().getModifiedSet ();
        if (!set.isEmpty()) {

            // XXX(-ttran) caching this dialog is fatal.  If the user
            // cancels the Exit action, modifies some more files and tries to
            // Exit again the list of modified DataObject's is not updated,
            // changes made by the user after the first aborted Exit will be
            // lost.
            exitDialog = null;
            
            if (exitDialog == null) {
                JButton buttonSave;
                JButton buttonSaveAll;
                JButton buttonDiscardAll;
                exitOptions = new JButton[] {
                                  buttonSave = new JButton (NbBundle.getBundle(ExitDialog.class).getString("CTL_Save")),
                                  buttonSaveAll = new JButton (NbBundle.getBundle(ExitDialog.class).getString("CTL_SaveAll")),
                                  buttonDiscardAll = new JButton (NbBundle.getBundle(ExitDialog.class).getString("CTL_DiscardAll")),
                              };
                buttonSave.setMnemonic(NbBundle.getBundle(ExitDialog.class).getString("CTL_Save_Mnemonic").charAt(0));
                buttonSaveAll.setMnemonic(NbBundle.getBundle(ExitDialog.class).getString("CTL_SaveAll_Mnemonic").charAt(0));
                buttonDiscardAll.setMnemonic(NbBundle.getBundle(ExitDialog.class).getString("CTL_DiscardAll_Mnemonic").charAt(0));
                secondaryExitOptions = new Object[] {
                                           new JButton (NbBundle.getBundle(ExitDialog.class).getString("CTL_Cancel")),
                                       };
                ExitDialog exitComponent = null;
                if (activatedNodes != null)
                    exitComponent = new ExitDialog (activatedNodes);
                else
                    exitComponent = new ExitDialog ();
                DialogDescriptor exitDlgDescriptor = new DialogDescriptor (
                                                         exitComponent,                                                   // inside component
                                                         NbBundle.getBundle(ExitDialog.class).getString("CTL_ExitTitle"), // title
                                                         true,                                                            // modal
                                                         exitOptions,                                                     // options
                                                         secondaryExitOptions [0],                                        // initial value
                                                         DialogDescriptor.RIGHT_ALIGN,                                    // option align
                                                         new org.openide.util.HelpCtx (ExitDialog.class.getName () + ".dialog"), // HelpCtx // NOI18N
                                                         exitComponent                                                    // Action Listener
                                                     );
                exitDlgDescriptor.setAdditionalOptions (secondaryExitOptions);
                exitDialog = TopManager.getDefault ().createDialog (exitDlgDescriptor);
            }

            result = false;
            exitDialog.show(); // Show the modal Save dialog
            return result;

        }
        else
            return true;
    }

    /** Renderer used in list box of exit dialog
     */
    private static class ExitDlgListCellRenderer extends JLabel implements ListCellRenderer {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 1877692790854373689L;

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        public ExitDlgListCellRenderer() {
            setOpaque(true);
            setBorder(noFocusBorder);
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        public java.awt.Component getListCellRendererComponent(JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
        {
            if (!(value instanceof DataObject)) return this;

            Node node = ((DataObject)value).getNodeDelegate();

            ImageIcon icon = new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
            super.setIcon(icon);

            setText(node.getDisplayName());
            if (isSelected){
                setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }
    }
}

/*
* Log
*  2    Gandalf   1.1         1/13/00  Jaroslav Tulach I18N
*  1    Gandalf   1.0         12/1/99  Petr Hrebejk    
* $
*/

