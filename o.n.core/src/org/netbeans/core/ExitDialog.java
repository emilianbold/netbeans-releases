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
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;

import org.openide.cookies.SaveCookie;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
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
    private void saveExc(Exception exception) {
        ErrorManager em = TopManager.getDefault().getErrorManager();
        
        Throwable t = em.annotate(
            exception,
            NbBundle.getBundle(ExitDialog.class).getString("EXC_Save")
        );
        em.notify(ErrorManager.EXCEPTION, t);
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
        boolean res = innerShowDialogImpl(activatedNodes);
        
        if(showPending) {
            return showPendingTasks();
        }
        
        return res;
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
        
        panel.add(new JLabel(NbBundle.getBundle(ExitDialog.class)
            .getString("LAB_PendingTasks")), cons);
        
        cons.gridy = 1;
        cons.weighty = 1.0D;
        cons.fill = GridBagConstraints.BOTH;
        cons.insets = new Insets(7, 11, 0, 12);
        
        panel.add(new org.openide.explorer.view.ListView(), cons);
        
        cons.gridy = 2;
        cons.weighty = 0.0D;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.insets = new Insets(11, 11, 0, 12);
        
        panel.add(new InfiniteProgress(), cons);

        return panel;
    }
    
    /** Shows dialog which waits for finishing of pending tasks,
     * (currently actions only) and offers to user to leave IDE 
     * immediatelly interrupting those tasks.
     * @return <code>true</code> if to continue with the action
     * <code>false</code> if the action to cancel */
    private static boolean showPendingTasks() {
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
                    if(getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                        dialog[0].dispose();
                    }
                }
            }
        });

        // XXX Ulgy trick to get no default button for dialog.
        JButton dummyButton = new JButton();
        Dimension zeroDim = new Dimension(0, 0);
        dummyButton.setMinimumSize(zeroDim);
        dummyButton.setPreferredSize(zeroDim);
        dummyButton.setMaximumSize(zeroDim);

        final JButton exitOption = new JButton(
            NbBundle.getBundle(ExitDialog.class).getString("LAB_EndTasks"));
        exitOption.setMnemonic(NbBundle.getBundle(ExitDialog.class).
            getString("LAB_EndTasksMnem").charAt(0));
        
        DialogDescriptor dd = new DialogDescriptor(
            panel,
            NbBundle.getBundle(ExitDialog.class).getString("CTL_PendingTitle"),
            true, // modal
            new Object[] {
                dummyButton,
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
                        dialog[0].dispose();
                    }
                }
            }
        );

        if(!getPendingTasks().isEmpty()) {
            root.addNodeListener(new NodeAdapter() {
                public void childrenRemoved(NodeMemberEvent evt) {
                    if(getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                        dialog[0].dispose();
                    }
                }
            });

            dialog[0] = TopManager.getDefault().createDialog(dd);
            dialog[0].show();

            if(dd.getValue() == DialogDescriptor.CANCEL_OPTION) {
                return false;
            }
            
        }
        
        return true;
    }
 
    /** Gets pending (running) tasks. Used as keys 
     * for pending dialog root node children. Currently it gets pending
     * actions only. */
    private static Collection getPendingTasks() {
        return ModuleActions.INSTANCE.getRunningActions();
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // add them here to the list. Then you need to create also a nodes
        // for them in PendingChildren.createNodes.
    }
    
    /** Ends penidng tasks. */
    private static void killPendingTasks() {
        // [PENDING] For actions, here should be tried
        // to stop the running request processor, create
        // ans implement ModuleActions.killRunningActions, but be aware
        // for some specialities, e.g. not to stop task with 
        // unmounting FS action when actually doing the unmounting.
        ModuleActions.killRunningActions();
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // kill them here.
   }

    /** Children showing pending tasks. */
    private static class PendingChildren extends Children.Keys {

        /** Listens on changes of sources from getting the tasks from.
         * Currently on module actions only. */
        private PropertyChangeListener propertyListener;
        
        
        /** Constructs new children. */
        public PendingChildren() {
            ModuleActions.INSTANCE.addPropertyChangeListener(WeakListener.propertyChange(
                propertyListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(ModuleActions.PROP_RUNNING_ACTIONS.equals(evt.getPropertyName())) {
                            setKeys(getPendingTasks());
                        }
                    }
                },
                ModuleActions.INSTANCE
            ));
        }

        /** Implements superclass abstract method. Creates nodes from key.
         * @return <code>PendingActionNode</code> if key is of 
         * <code>Action</code> type otherwise <code>null</code> */
        protected Node[] createNodes(Object key) {
            if(key instanceof Action) {
                return new Node[] {new PendingActionNode((Action)key)};
            }
            
            return null;
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
        }
        
    } //  End of class PendingChildren.

    
    /** Node representing pending action task. */
    private static class PendingActionNode extends AbstractNode {

        /** Icon resource retrieved from action if it is 
         * of <code>SystemAction</code> instance. */
        private String iconResource;
        
        /** Creates node for action. */
        public PendingActionNode(Action action) {
            super(Children.LEAF);
            
            String actionName = org.openide.awt.Actions.cutAmpersand((String)action.getValue(Action.NAME));
            setName(actionName);
            setDisplayName(actionName + " " // NOI18N
                + NbBundle.getBundle(ExitDialog.class)
                    .getString("CTL_ActionInProgress"));

            // XXX Is there a better way how to retrieve the action icon?
            if(action instanceof SystemAction) {
                try {
                    java.lang.reflect.Method method = SystemAction.class
                        .getDeclaredMethod("iconResource", new Class[0]);
                    method.setAccessible(true);
                    
                    String iconResource = (String)method.invoke(action, new Object[0]);
                    
                    if(iconResource != null) {
                        this.iconResource = iconResource;
                    }
                } catch(IllegalAccessException iae) {
                } catch(java.lang.reflect.InvocationTargetException ite) {
                } catch(NoSuchMethodException nme) {
                } catch(SecurityException se) {
                }
            }
        }

        /** Overrides superclass method. */
        public Image getIcon(int type) {
            if(iconResource != null) {
                return Utilities.loadImage(iconResource);
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


    /** Infinite progress bar. */
    private static class InfiniteProgress extends JPanel
    implements ActionListener {

        /** Swing timer used to repaint the component. */
        private Timer timer;

        // XXX There could be a nicer repainting policy created for sure.
        /** Index indicating current position of gap whitin a drawing block. */
        private int index;
        /** Index of gap whitin a block.  */
        private final int gapIndex;
        /** Width of one cell in the block. */
        private int cellWidth;


        /** Constructs infinite progress. */
        public InfiniteProgress() {
            this.gapIndex = 4;
            this.cellWidth = getFontMetrics(getFont()).charWidth('n');
            
            setBorder(BorderFactory.createEtchedBorder());
            setForeground(UIManager.getColor("Label.foreground")); // NOI18N
        }
        
        /** Overrides superclass method. Adds starting of timer. */
        public void addNotify() {
            super.addNotify();
            
            getTimer().start();
        }
        
        /** Overrides superclass method. Adds stopping of timer. */
        public void removeNotify() {
            getTimer().stop();
            
            super.removeNotify();
        }
        
        /** Gets timer. */
        private synchronized Timer getTimer() {
            if(timer == null) {
                timer = new Timer(40, this);
            }
            
            return timer;
        }

        /** Implements <code>ActionPerformed</code> interface. */
        public void actionPerformed(ActionEvent evt) {
            index++;
            if(index >= gapIndex) {
                index = 0;
            }
            
            repaint();
        }

        /** Overrides supeclass method. Adds painting of progress. */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Rectangle rect = g.getClipBounds();
            
            for(int i = 0, pos = 0 ; pos < rect.width; pos += cellWidth, i++) {
                if(i % gapIndex != index) {
                    g.fillRect(pos, 0, cellWidth, rect.height);
                }
            }
        }
    } // End of class InfiniteProgress.
    
    
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
