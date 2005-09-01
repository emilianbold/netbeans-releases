/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.config.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.actions.PopupAction;
//import org.openide.awt.MouseUtils;

import org.openide.actions.FindAction;
//import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.DataObject;
import org.openide.loaders.OpenSupport;
import org.openide.NotifyDescriptor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;
import org.openide.windows.*;
import org.openide.util.Utilities;
import org.openide.nodes.*;
import org.openide.util.WeakListener;


/**
 * The component that will display a panel corresponding to the node selection of its parent ExplorerManager, usually a
 * ContentPanel. It provides the mapping between selected nodes and displayed panels. A parent ContentPanel will get a node hierarchy
 * by calling getRoot and display that tree in it's structure view. The PanelView will then show an appropriate panel
 * based on the node selection. Subclasses should, at a bare minimum, initialize the root node and override the showSelection method.
 */
public abstract class PanelView extends TopComponent implements PanelFocusCookie {
    
    private Node root;
//    /** not null if popup menu enabled */
//    transient PopupAdapter popupListener;
    /** the most important listener  */
    transient NodeSelectedListener nodeListener = null;
    
    /** Explorer manager, valid when this view is showing */
    transient private ExplorerManager manager;
    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;
    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;
    /** ResourceBundle for ejb package. */
    protected ResourceBundle bundle;
    
    
    /**
     * Creates a new instance of PanelView
     */
    public PanelView() {
        // init listener & attach it
        nodeListener = new NodeSelectedListener();
        bundle = NbBundle.getBundle(PanelView.class);
    }
    
    /**
     * Gets the current root Node.
     * @return the Node.
     */
    public Node getRoot() {
        return root;
    }
    
    public void setRoot(Node r){
        root = r;
    }
    
    /** Called when the view wishes to reuse the context menu from the root node
     * as its own context menu
     * @param value true to set the context menu, false otherwise
     */
//    public void setPopupAllowed(boolean value) {
//        if (popupListener == null && value) {
//            // on
//            popupListener = new PopupAdapter();
//            addMouseListener(popupListener);
//            return;
//        }
//        if (popupListener != null && !value) {
//            // off
//            removeMouseListener(popupListener);
//            popupListener = null;
//            return;
//        }
//    }
    
    /** Popup adapter.
     */
//    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {
//        
//        PopupAdapter() {}
//        
//        protected void showPopup(MouseEvent e) {
//            JPopupMenu popup = getRoot().getContextMenu();
//            popup.show(PanelView.this,e.getX(), e.getY());
//            
//        }
//        
//    }
    
    /** Called when explorer manager has changed the current selection.
     * The view should display the panel corresponding to the selected nodes
     * @param nodes the nodes used to update the view
     */
    abstract public void showSelection(Node[] nodes) ;
    
    /** The view can change the explorer manager's current selection.
     * @param value the new node to explore
     * @param nodes the nodes to select
     * @return false if the explorer manager is not able to change the selection
     */
    
    public boolean setManagerExploredContextAndSelection(Node value, Node[] nodes) {
        try{
            getExplorerManager().setExploredContextAndSelection(value, nodes);
        }
        catch (PropertyVetoException e) {
            return false;
        }
        return true;
    }
    /** The view can change the explorer manager's current selection.
     * @param nodes the nodes to select
     * @return false if the explorer manager is not able to change the selection
     */
    
    public boolean setManagerSelection(Node[] nodes) {
        try{
            getExplorerManager().setSelectedNodes(nodes);
        }
        catch (PropertyVetoException e) {
            return false;
        }
        return true;
    }
    /** Called when explorer manager is about to change the current selection.
     * The view can forbid the change if it is not able to display such
     * selection.
     * @param nodes the nodes to select
     * @return false if the view is not able to change the selection
     */
    protected boolean selectionAccept(Node[] nodes) {
        return true;
    }
    
     /**
     * A parent ComponentPanel uses this method to notify  its PanelView children that it was opened
     * and lets them do any needed initialization as a result. Default implementation does nothing.
     */   
    public void open(){
    }
   /**
     * A parent ComponentPanel uses this method to notify  its PanelView children it is about to close.
     * and lets them determine if they are ready. Default implementation just returns true.
     * 
     * 
     * @return boolean True if the PanelView is ready to close, false otherwise.
     */    
    public boolean canClose(){
        return  true;
    }
     /**
     * This method supports the PanelFocusCookie. It allows an external source set the focus on a ComponentPanel.
     *  See the JavaDoc for PanelFocusCookie for more information.
     *  Default implementation returns true if the panelViewNameHint is the same as the name of the PanelView as determined
     *  by calling get name. It would be the responsibility of any subclass to reset the currently selected node if necessary.
     * 
     * 
     * @param panelViewNameHint String used as a hint for the appropriate PanelView if there is more than one.
     * @param panelNameHint String used as a hint for the appropiate panel in the PanelView
     * @param focusObject Object that can be used to identify the object that should have the focus.
     * @return true if the ComponentPanel was able to focus on the object.
     */   
    public boolean setFocusOn(String panelViewNameHint, String panelNameHint, Object focusObject){
        if (panelViewNameHint !=null && panelViewNameHint.equals(getName()))
         return true;
        return false;
    }
    
    
    /**
     * Computes the localized string for the key.
     * @param key The tag of the string.
     * @return the localized string.
     */
    public String getResourceString(String key) {
        return bundle.getString(key);
    }
    
     /* Initializes the component.
      * We need to register for ExplorerManager events here
      */
    public void addNotify() {
        super.addNotify();
        
        // Enter key in the tree
        
        
        ExplorerManager newManager = ExplorerManager.find(this);
        
        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener(wlvc);
                manager.removePropertyChangeListener(wlpc);
            }
            
            manager = newManager;
            
            manager.addVetoableChangeListener(wlvc = WeakListener.vetoableChange(nodeListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListener.propertyChange(nodeListener, manager));
            
        }
        
    }
    
    /**
     * Gets the current ExplorerManager the view is attached.
     * @return the ExplorerManager.
     */
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    class NodeSelectedListener implements VetoableChangeListener,PropertyChangeListener {
        
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                if (!selectionAccept((Node[])evt.getNewValue())) {
                    throw new PropertyVetoException("", evt); // NOI18N
                }
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;
            Node[] selectedNodes = getExplorerManager().getSelectedNodes();
            showSelection(selectedNodes);
            
        }
    }
}

