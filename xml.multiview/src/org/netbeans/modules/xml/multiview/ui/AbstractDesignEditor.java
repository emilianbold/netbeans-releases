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
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.openide.util.Lookup;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.actions.SaveAction;

/**
 * The ComponentPanel three pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane and the nodes properties in the properties pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public abstract class AbstractDesignEditor extends TopComponent implements ExplorerManager.Provider {
    
    protected JComponent structureView;
    protected PanelView contentView;
    
    private ExplorerManager manager;
    
    /** The icon for ComponentInspector */
    protected static String iconURL = "/org/netbeans/modules/form/resources/inspector.gif"; // NOI18N
    
    protected static final long serialVersionUID =1L;
    
    public AbstractDesignEditor() {
        this.manager = new ExplorerManager();
        
        ActionMap map = this.getActionMap ();
        
        SaveAction act = (SaveAction)SaveAction.findObject(SaveAction.class);
        KeyStroke stroke = (KeyStroke)act.getValue(Action.ACCELERATOR_KEY);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, "save"); //NOI18N
        map.put("save", act); //NOI18N
        
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
    }
    
    public void setContentView(PanelView panelView) {
        contentView = panelView;
        setRootContext(panelView.getRoot());
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    public void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    
    public void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
    
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
     * returns the HelpCtx for this component.
     * @return the HelpCtx
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ComponentPanel"); // NOI18N
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
    
}
