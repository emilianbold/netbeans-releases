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


import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;

import org.openide.awt.Actions;
import org.openide.cookies.SaveCookie;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.ListView;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeMemberEvent;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListener;

import org.netbeans.core.projects.SessionManager;
import org.netbeans.core.execution.ExecutionEngine;
import org.netbeans.core.execution.ExecutionEvent;
import org.netbeans.core.execution.ExecutionListener;

/** Dialog which lets the user select which open files to close.
 *
 * @author  Ian Formanek, Petr Hrebejk
 */

public class ExitDialog extends JPanel implements java.awt.event.ActionListener {


    private static Object[] exitOptions;

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
        scroll.setBorder (new CompoundBorder (new EmptyBorder (12, 12, 11, 0), scroll.getBorder ()));
        add(scroll, java.awt.BorderLayout.CENTER);
        list.setCellRenderer(new ExitDlgListCellRenderer());
        list.getAccessibleContext().setAccessibleName((NbBundle.getBundle(ExitDialog.class)).getString("ACSN_ListOfChangedFiles"));
        list.getAccessibleContext().setAccessibleDescription((NbBundle.getBundle(ExitDialog.class)).getString("ACSD_ListOfChangedFiles"));
        this.getAccessibleContext().setAccessibleDescription((NbBundle.getBundle(ExitDialog.class)).getString("ACSD_ExitDialog"));
    }
    
    private void updateSaveButton () {
        ((JButton)exitOptions [0]).setEnabled (list.getSelectedIndex () != -1);
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
        } else if (NotifyDescriptor.CANCEL_OPTION.equals (evt.getSource ())) {
            exitDialog.setVisible (false);
        }
    }

    /** Save the files from the listbox
    * @param all true- all files, false - just selected
    */
    private void save(boolean all) {
        Object array[] = ((all) ? listModel.toArray() : list.getSelectedValues());
        int i, count = ((array == null) ? 0 : array.length);
        int index = 0;	// index of last removed item

        for (i = 0; i < count; i++) {
            DataObject nextObject = (DataObject)array[i];
            index = listModel.indexOf(nextObject);
            save(nextObject);
        }

        if (listModel.isEmpty())
            theEnd();
        else {	// reset selection to new item at the same index if available
            if (index < 0)
                index = 0;
            else if (index > listModel.size() - 1) {
                index = listModel.size() - 1;
            }
            list.setSelectedIndex(index);
        }
    }

    /** Tries to save given data object using its save cookie.
     * Notifies user if excetions appear.
     */
    private void save (DataObject dataObject) {
        try {
            SaveCookie sc = (SaveCookie)dataObject.getCookie(SaveCookie.class);
            if (sc != null) {
                sc.save();
            }
            listModel.removeElement(dataObject);
        } catch (java.io.IOException exc) {
            ErrorManager em = ErrorManager.getDefault();
            Throwable t = em.annotate(
                exc, NbBundle.getBundle(ExitDialog.class).getString("EXC_Save")
            );
            em.notify(ErrorManager.EXCEPTION, t);
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

    /** Opens the ExitDialog for unsaved files in filesystems marked 
     * for unmount and blocks until it's closed. If dialog doesm't
     * exists it creates new one. Returns true if the IDE should be closed.
     * It also shows dialog with pending tasks afterwards with the same 
     * policy if <code>showPending</code> is set to <code>true</code>.
     */
    static boolean showDialog(Node[] activatedNodes, boolean showPending) {
        return innerShowDialog(activatedNodes, showPending);
    }

    /** Opens the ExitDialog for unsaved files in filesystems marked 
     * for unmount and blocks until it's closed. If dialog doesm't
     * exists it creates new one. Returns true if the IDE should be closed.
     */
    static boolean showDialog(Node[] activatedNodes) {
        return innerShowDialog( activatedNodes, false );        
    }
    
    /** Opens the ExitDialog and blocks until it's closed. If dialog doesm't
     * exists it creates new one. Returns true if the IDE should be closed.
     */
    static boolean showDialog() {
        return innerShowDialog( null, false );        
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


    /** After showing dialog with unclosed objects, shows also 
     * dialog with running tasks if there are some. */
    private static boolean innerShowDialog(Node[] activatedNodes, boolean showPending) {
        boolean shutdown = innerShowDialogImpl(activatedNodes);
        
        if(shutdown && showPending) {
            return showPendingTasks();
        }
        else {
            return shutdown;
        }
    }
    
    /** Opens the ExitDialog for activated nodes or for
     * whole repository.
     */
    private static boolean innerShowDialogImpl(Node[] activatedNodes) {
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
                ResourceBundle bundle = NbBundle.getBundle(ExitDialog.class);
                JButton buttonSave = new JButton();

                // special handling to handle a button title with mnemonic 
                // and to allow enable/disable control of the option
                Actions.setMenuText(buttonSave, bundle.getString("CTL_Save"), true);

                exitOptions = new Object[] {
                                  buttonSave,
                                  bundle.getString("CTL_SaveAll"),
                                  bundle.getString("CTL_DiscardAll"),
                              };
                ExitDialog exitComponent = null;
                if (activatedNodes != null)
                    exitComponent = new ExitDialog (activatedNodes);
                else
                    exitComponent = new ExitDialog ();
                DialogDescriptor exitDlgDescriptor = new DialogDescriptor (
                                                         exitComponent,                                                   // inside component
                                                         bundle.getString("CTL_ExitTitle"), // title
                                                         true,                                                            // modal
                                                         exitOptions,                                                     // options
                                                         NotifyDescriptor.CANCEL_OPTION,                                        // initial value
                                                         DialogDescriptor.RIGHT_ALIGN,                                    // option align
                                                         new org.openide.util.HelpCtx (ExitDialog.class.getName () + ".dialog"), // HelpCtx // NOI18N
                                                         exitComponent                                                    // Action Listener
                                                     );
                exitDlgDescriptor.setAdditionalOptions (new Object[] {NotifyDescriptor.CANCEL_OPTION});
                exitDialog = TopManager.getDefault ().createDialog (exitDlgDescriptor);
            }

            result = false;
            exitDialog.show(); // Show the modal Save dialog
            return result;

        }
        else
            return true;
    }

    /** Creates dialod for showing pending tasks. */
    private static ExplorerPanel createExplorerPanel() {
        ExplorerPanel panel = new ExplorerPanel();
        
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = 0;
        cons.gridy = 0;
        cons.weightx = 1.0D;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.insets = new Insets(11, 11, 0, 12);

        JLabel label = new JLabel(NbBundle.getBundle(ExitDialog.class)
            .getString("LAB_PendingTasks"));
        label.setDisplayedMnemonic(NbBundle.getBundle(ExitDialog.class)
            .getString("LAB_PendingTasksMnem").charAt(0));
        
        panel.add(label, cons);
        
        cons.gridy = 1;
        cons.weighty = 1.0D;
        cons.fill = GridBagConstraints.BOTH;
        cons.insets = new Insets(7, 11, 0, 12);

        ListView view = new ListView();
        label.setLabelFor(view);
        
        panel.add(view, cons);
        
        view.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ExitDialog.class)
            .getString("ACSD_PendingTasks"));
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ExitDialog.class)
            .getString("ACSD_PendingTitle"));

        return panel;
    }
    
    /** Shows dialog which waits for finishing of pending tasks,
     * (currently actions only) and offers to user to leave IDE 
     * immediatelly interrupting those tasks.
     * @return <code>true</code> if to continue with the action
     * <code>false</code> if the action to cancel
     */
    public static boolean showPendingTasks() {
        if(getPendingTasks().isEmpty()) {
            return true;
        }
  
        ExplorerPanel panel = createExplorerPanel();
        
        final Dialog[] dialog = new Dialog[1];
        final Node root = new AbstractNode(new PendingChildren());

        panel.getExplorerManager().setRootContext(root);
        panel.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // Listen on changes of pending tasks and if all has finished
                // close the dialog.
                if(ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                    if(dialog[0] != null && getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                    }
                }
            }
        });

        final JButton exitOption = new JButton(
            NbBundle.getBundle(ExitDialog.class).getString("LAB_EndTasks"));
        exitOption.setMnemonic(NbBundle.getBundle(ExitDialog.class).
            getString("LAB_EndTasksMnem").charAt(0));
        // No default button.
        exitOption.setDefaultCapable(false);
        exitOption.getAccessibleContext().setAccessibleDescription(
            NbBundle.getBundle(ExitDialog.class).getString("ACSD_EndTasks"));
        
        DialogDescriptor dd = new DialogDescriptor(
            panel,
            NbBundle.getBundle(ExitDialog.class).getString("CTL_PendingTitle"),
            true, // modal
            new Object[] {
                exitOption,
                DialogDescriptor.CANCEL_OPTION
            },
            null,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if(evt.getSource() == exitOption) {
                        killPendingTasks();
                        dialog[0].setVisible(false);
                    }
                }
            }
        );

        if(!getPendingTasks().isEmpty()) {
            root.addNodeListener(new NodeAdapter() {
                public void childrenRemoved(NodeMemberEvent evt) {
                    if(dialog[0] != null && getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                    }
                }
            });

            dialog[0] = TopManager.getDefault().createDialog(dd);
            
            dialog[0].addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowOpened(java.awt.event.WindowEvent evt) {
                    // Dialog was opened but pending tasks could disappear
                    // inbetween.
                    if(getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                    }
                }
            });
            
            dialog[0].show();
            dialog[0].dispose();

            if(dd.getValue() == DialogDescriptor.CANCEL_OPTION
            || dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
                return false;
            }
            
        }
        
        return true;
    }
 
    /** Gets pending (running) tasks. Used as keys 
     * for pending dialog root node children. Currently it gets pending
     * actions only. */
    private static Collection getPendingTasks() {
        
        ArrayList pendingTasks = new ArrayList( 10 );
        
        pendingTasks.addAll( ModuleActions.INSTANCE.getRunningActions() );
        
        if ( !Boolean.getBoolean( "netbeans.full.hack" ) ) { // NOI18N
            // Avoid showing the tasks in the dialog when running internal tests
            pendingTasks.addAll( ExecutionEngine.getExecutionEngine().getRunningTasks() );
        }
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // add them here to the list. Then you need to create also a nodes
        // for them in PendingChildren.createNodes.
        
        return pendingTasks;
    }
    
    /** Ends penidng tasks. */
    private static void killPendingTasks() {
        // [PENDING] For actions, here should be tried
        // to stop the running request processor, create
        // ans implement ModuleActions.killRunningActions, but be aware
        // for some specialities, e.g. not to stop task with 
        // unmounting FS action when actually doing the unmounting.
        ModuleActions.killRunningActions();
        killRunningExecutors();
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // kill them here.
   }
    
   /** Tries to kill running executions */
   private static void killRunningExecutors() {
       ArrayList tasks = new ArrayList( ExecutionEngine.getExecutionEngine().getRunningTasks() );
       
       for ( Iterator it = tasks.iterator(); it.hasNext(); ) {
           ExecutorTask et = (ExecutorTask) it.next();
           if ( !et.isFinished() ) {
               et.stop();
           }
       }
       
   }

    /** Children showing pending tasks. */
    private static class PendingChildren extends Children.Keys implements ExecutionListener {

        /** Listens on changes of sources from getting the tasks from.
         * Currently on module actions only. */
        private PropertyChangeListener propertyListener;
        
        
        /** Constructs new children. */
        public PendingChildren() {
            propertyListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ModuleActions.PROP_RUNNING_ACTIONS.equals(evt.getPropertyName())) {
                        setKeys(getPendingTasks());
                    }
                }
            };

            ModuleActions.INSTANCE.addPropertyChangeListener(
                WeakListener.propertyChange(propertyListener, ModuleActions.INSTANCE)
            );
            
            ExecutionEngine.getExecutionEngine().addExecutionListener( this );
        }

        /** Implements superclass abstract method. Creates nodes from key.
         * @return <code>PendingActionNode</code> if key is of 
         * <code>Action</code> type otherwise <code>null</code> */
        protected Node[] createNodes(Object key) {
            Node n = null;
            if(key instanceof Action) {
                n = new PendingActionNode((Action)key);
            }
            else if ( key instanceof ExecutorTask ) {
                AbstractNode an = new AbstractNode( Children.LEAF );
                an.setName( NbBundle.getBundle(ExitDialog.class).getString("CTL_PendingExternalProcess") + 
                            ExecutionEngine.getExecutionEngine().getRunningTaskName( (ExecutorTask) key ) );
                an.setIconBase( "org/netbeans/core/resources/execution" ); //NOI18N
                n = an;
            }
            return n == null ? null : new Node[] { n };
        }

        /** Implements superclass abstract method. */
        protected void addNotify() {
            setKeys(getPendingTasks());
            super.addNotify();            
        }
        
        /** Implements superclass abstract method. */
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
            ExecutionEngine.getExecutionEngine().removeExecutionListener( this );
        }
        
        // ExecutionListener implementation ------------------------------------
        
        public void startedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }
        
        public void finishedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }
        
    } //  End of class PendingChildren.

    
    /** Node representing pending action task. */
    private static class PendingActionNode extends AbstractNode {

        /** Icon retrieved from action if it is 
         * of <code>SystemAction</code> instance. */
        private Icon icon;
        
        /** Creates node for action. */
        public PendingActionNode(Action action) {
            super(Children.LEAF);
            
            String actionName = org.openide.awt.Actions.cutAmpersand((String)action.getValue(Action.NAME));
            setName(actionName);
            setDisplayName(actionName + " " // NOI18N
                + NbBundle.getBundle(ExitDialog.class)
                    .getString("CTL_ActionInProgress"));
            
            if(action instanceof SystemAction) {
                this.icon = ((SystemAction)action).getIcon();
            }
        }

        /** Overrides superclass method. */
        public Image getIcon(int type) {
            if(icon != null) {
                Image im = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB
                );

                icon.paintIcon(null, im.getGraphics(), 0, 0);
                
                return im;
            } else {
                return super.getIcon(type);
            }
        }

        /** Overrides superclass method.
         * @return empty array of actions */
        protected SystemAction[] createActions() {
            return new SystemAction[0];
        }
        
    } // End of class PendingActionNode.
    
    
    /** Renderer used in list box of exit dialog
     */
    private class ExitDlgListCellRenderer extends JLabel implements ListCellRenderer {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 1877692790854373689L;

        protected Border hasFocusBorder;
        protected Border noFocusBorder;

        public ExitDlgListCellRenderer() {
            this.setOpaque(true);
            this.setBorder(noFocusBorder);
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        public java.awt.Component getListCellRendererComponent(JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
        {
            final DataObject obj = (DataObject)value;
            if (!obj.isValid()) {
                // #17059: it might be invalid already.
                // #18886: but if so, remove it later, otherwise BasicListUI gets confused.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        listModel.removeElement(obj);
                    }
                });
                setText("");
                return this;
            }

            Node node = obj.getNodeDelegate();

            ImageIcon icon = new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
            super.setIcon(icon);

            setText(node.getDisplayName());
            if (isSelected){
                this.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                this.setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            this.setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }
    }
}
