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

import java.io.*;
import java.util.*;
import java.awt.*;
import java.beans.*;
import java.text.MessageFormat;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.*;
import org.openide.util.HelpCtx;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;


/**
 * The ComponentPanel three pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane and the nodes properties in the properties pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public abstract class AbstractDesignEditor extends ExplorerPanel implements PanelFocusCookie {
    
    
    /** The default width of the AbstractComponentEditor */
    public static final int DEFAULT_WIDTH = 400;
    /** The default height of the AbstractComponentEditor */
    public static final int DEFAULT_HEIGHT = 400;
    
    protected static EmptyInspectorNode emptyInspectorNode;
    
    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
    "/org/netbeans/modules/form/resources/emptyInspector"; // NOI18N
    
    
    
    protected JComponent structureView;
    protected JComponent propertiesView;
    protected JComponent contentView;
    
    
    /** The icon for ComponentInspector */
    protected static String iconURL = "/org/netbeans/modules/form/resources/inspector.gif"; // NOI18N
    
    protected static final long serialVersionUID =1L;
    
    
    protected AbstractDesignEditor(){
    }
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public AbstractDesignEditor(PanelView panel){
        contentView = panel;
        contentView.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
        initComponents();
        setRootContext(panel.getRoot());
    }
    
    
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     * @param structure The JComponent that will be used in the structure pane. Should follow the
     *                  ExplorerManager protocol. Will usually be some subclass of BeanTreeView.
     */
    
    public AbstractDesignEditor(PanelView panel, JComponent structure){
        contentView = panel;
        contentView.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
        structureView = structure;
        initComponents();
        setRootContext(panel.getRoot());
    }
    
    
    /**
     * Sets the root context for the ExplorerManager
     * @param node The new root context.
     */
    public void setRootContext(Node node) {
        getExplorerManager().setRootContext(node);
    }
    
    protected void initComponents() {
        ExplorerManager manager = getExplorerManager();
        emptyInspectorNode = new EmptyInspectorNode();
        manager.setRootContext(emptyInspectorNode);
        
        setIcon(Utilities.loadImage(iconURL));
        setName("CTL_ComponentPanelTitle");
        
        // Force winsys to not show tab when this comp is alone
        putClientProperty("TabPolicy", "HideWhenAlone");
        setToolTipText("HINT_ComponentPanel");
        manager.addPropertyChangeListener(new NodeSelectedListener());
        setLayout(new BorderLayout());
    }
    
    
 
    /**
     * Used to get the JComponent used for the content pane. Usually a subclass of PanelView.
     * @return the JComponent
     */
    public JComponent getContentView(){
        return contentView;
    }
    
    /**
     * Used to get the JComponent used for the structure pane. Usually a container for the structure component or the structure component itself.
     * @return the JComponent
     */
    public JComponent getStructureView(){
        if (structureView ==null){
            structureView = createStructureComponent();
        }
        return structureView;
    }
    /**
     * Used to create an instance of the JComponent used for the structure component. Usually a subclass of BeanTreeView.
     * @return the JComponent
     */
    abstract public JComponent createStructureComponent() ;
    
    /**
     * Used to get the JComponent used for the properties pane. Usually a subclass of PropertySheetView.
     * @return the JComponent
     */
    
    public JComponent getPropertiesView(){
        if (propertiesView == null){
            propertiesView = createPropertiesComponent();
            propertiesView.addPropertyChangeListener(new PropertiesDisplayListener());
        }
        return propertiesView;
    }
    
    /**
     * Used to create an instance of the JComponent used for the properties component. Usually a subclass of PropertySheetView.
     * @return JComponent
     */
    abstract public JComponent createPropertiesComponent();
    /**
     * A parent TopComponent can use this method to notify the ComponentPanel and it PanelView children that it was opened
     * and lets them do any needed initialization as a result. Default implementation just delegates to the PanelView.
     */
    public void open(){
        if (contentView!=null)
            ((PanelView)contentView).open();
    }
    /**
     * A parent TopComponent can use this method to notify the ComponentPanel and it PanelView children it is about to close.
     * and lets them determine if they are ready. Default implementation just delegates to the PanelView.
     * @return boolean True if the ComponentPanel is ready to close, false otherwise.
     */
    public boolean canClose(){
        if (contentView!=null)
            return  ((PanelView)contentView).canClose();
        else
            return true;
    }
    
    /**
     * This method supports the PanelFocusCookie. It allows an external source set the focus on a ComponentPanel.
     *  See the JavaDoc for PanelFocusCookie for more information.
     *  Default implementation just delegates to the single PanelView.
     * @param  panelViewNameHint String used as a hint for the appropriate PanelView if there is more than one.
     * @param  panelNameHint String used as a hint for the appropiate panel in the PanelView
     * @param  focusObject Object that can be used to identify the object that should have the focus.
     * @return  true if the ComponentPanel was able to focus on the object.
     */
    public boolean setFocusOn(String panelViewNameHint, String panelNameHint, Object focusObject){
        if (contentView!=null)
            return ((PanelView)contentView).setFocusOn( panelViewNameHint,  panelNameHint,  focusObject);
        else
            return false;
    }
    
    /**
     * returns the HelpCtx for this component.
     * @return the HelpCtx
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ComponentPanel"); // NOI18N
    }
    
    /**
     * returns the preferred size for this component.
     * @return the Dimension
     */
    
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    class NodeSelectedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;
            Node[] selectedNodes = getExplorerManager().getSelectedNodes();
        }
    }
    
    class PropertiesDisplayListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertySheet.PROPERTY_DISPLAY_WRITABLE_ONLY.equals(
            evt.getPropertyName())) {
                //
                //
            }
        }
    }
    static class EmptyInspectorNode extends AbstractNode {
        public EmptyInspectorNode() {
            super(Children.LEAF);
            setIconBase(EMPTY_INSPECTOR_ICON_BASE);
        }
        
        public boolean canRename() {
            return false;
        }
    }
    
    
}
