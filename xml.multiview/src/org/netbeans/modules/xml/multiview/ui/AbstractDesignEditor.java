/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.beans.*;
import javax.swing.JComponent;
import javax.swing.ActionMap;

import org.openide.util.Lookup;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/**
 * The ComponentPanel three pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane and the nodes properties in the properties pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public abstract class AbstractDesignEditor extends TopComponent implements ExplorerManager.Provider {
    
    
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
    protected PanelView contentView;
    
    private ExplorerManager manager;
    
    /** The icon for ComponentInspector */
    protected static String iconURL = "/org/netbeans/modules/form/resources/inspector.gif"; // NOI18N
    
    protected static final long serialVersionUID =1L;
    
    public AbstractDesignEditor() {
        this.manager = new ExplorerManager();
        
        ActionMap map = this.getActionMap ();
        
        map.put(javax.swing.text.DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(javax.swing.text.DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(javax.swing.text.DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false
        map.put("org-openide-actions-EditAction", org.openide.util.actions.SystemAction.get(org.openide.actions.EditAction.class));
        map.put("org-openide-actions-NewAction", org.openide.util.actions.SystemAction.get(org.openide.actions.NewAction.class));
        // following line tells the top component which lookup should be associated with it
        associateLookup (ExplorerUtils.createLookup (manager, map));
        initComponents();
    }
    
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public AbstractDesignEditor(PanelView contentView){
        this();
        this.contentView = contentView;
        setRootContext(contentView.getRoot());
        //this.contentView.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
    }
    
    public void setContentView(PanelView panelView) {
        contentView = panelView;
        setRootContext(panelView.getRoot());
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
    
    
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     * @param structure The JComponent that will be used in the structure pane. Should follow the
     *			ExplorerManager protocol. Will usually be some subclass of BeanTreeView.
     */
    
    public AbstractDesignEditor(PanelView panel, JComponent structure){
        this(panel);
        structureView = structure;
    }
    
    
    /**
     * Sets the root context for the ExplorerManager
     * @param node The new root context.
     */
    public void setRootContext(Node node) {
        getExplorerManager().setRootContext(node);
    }
    
    protected void initComponents() {
        /*
        ExplorerManager manager = getExplorerManager();
        emptyInspectorNode = new EmptyInspectorNode();
        manager.setRootContext(emptyInspectorNode);
        */
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
    public PanelView getContentView(){
        return contentView;
    }
    
    /**
     * Used to get the JComponent used for the structure pane. Usually a container for the structure component or the structure component itself.
     * @return the JComponent
     */
    public JComponent getStructureView(){
        if (structureView ==null){
            structureView = createStructureComponent();
            structureView.addPropertyChangeListener(new NodeSelectedListener());
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
    
    abstract public ErrorPanel getErrorPanel();
    
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
            if (contentView.isSectionHeaderClicked()) {
                contentView.setSectionHeaderClicked(false);
                return;
            }
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;

            Node[] selectedNodes = getExplorerManager().getSelectedNodes();
            if (selectedNodes!=null && selectedNodes.length>0)
                contentView.showSelection(selectedNodes);
        }
    }
    
    class PropertiesDisplayListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            /*
            if (PropertySheet.PROPERTY_DISPLAY_WRITABLE_ONLY.equals(
            evt.getPropertyName())) {
                //
                //
            }
             */
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
