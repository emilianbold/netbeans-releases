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

import java.awt.*;
import javax.swing.*;
import org.openide.explorer.view.*;
import org.openide.explorer.*;
import org.openide.util.Lookup;
import javax.swing.text.DefaultEditorKit;

import org.openide.util.actions.SystemAction;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.netbeans.modules.xml.multiview.Error;

/**
 * The ComponentPanel three pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane and the nodes properties in the properties pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public class ToolBarDesignEditor extends AbstractDesignEditor implements org.openide.cookies.EditCookie {
    
    protected JComponent designPanel;
    protected JPanel actionPanel;
    private ErrorPanel errorPanel;

    /**
     * Creates a new instance of ToolBarDesignEditor
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public ToolBarDesignEditor(){
        super();
        createDesignPanel();
        add(java.awt.BorderLayout.CENTER,designPanel);
        add(java.awt.BorderLayout.SOUTH,createErrorPanel());
    }
    
    /**
     * Creates a new instance of ToolBarDesignEditor
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public ToolBarDesignEditor(PanelView panel){
        super(panel);
        createDesignPanel();
        designPanel.add(panel,BorderLayout.CENTER);
        add(java.awt.BorderLayout.CENTER,designPanel);
        add(java.awt.BorderLayout.SOUTH,createErrorPanel());
        panel.attachErrorPanel(errorPanel);
    }
    
    public JComponent createDesignPanel(){
        designPanel = new JPanel(new BorderLayout());
        actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        designPanel.add(actionPanel, BorderLayout.SOUTH);
        return designPanel;
    }
    
    public void setContentView(PanelView panel) {
        //createDesignPanel();
        if (getContentView()!=null) {
            designPanel.remove(getContentView());
        }
        designPanel.add(panel,BorderLayout.CENTER);
        panel.attachErrorPanel(errorPanel);
        super.setContentView(panel);
    }
    
    public ErrorPanel getErrorPanel() {
        return errorPanel;
    }
    
    public Error getError() {
        return(errorPanel==null?null:errorPanel.getError());
    }
    
    private ErrorPanel createErrorPanel() {
        errorPanel = new ErrorPanel();
        return errorPanel;
    }
    
    public void componentActivated() {
        super.componentActivated();
    }

    public void componentClosed() {
        super.componentClosed();
    }
    public void componentOpened() {
        super.componentOpened();
    }
    public void componentShowing() {
        super.componentShowing();
    }
    
    public JPanel getActionPanel() {
        return actionPanel;
    }

    /**
     * Used to create an instance of the JComponent used for the structure component. Usually a subclass of BeanTreeView.
     * @return the JComponent
     */
    
    public JComponent createStructureComponent() {
        JToolBar toolbar = new ToolBarView(getExplorerManager(),getContentView().getRoot());
        
        EditAction edit = (EditAction)SystemAction.get(EditAction.class);
        ((JComponent)edit.getToolbarPresenter()).setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
        toolbar.add(edit.getToolbarPresenter());

        DeleteAction del = (DeleteAction)SystemAction.get(DeleteAction.class);
        ((JComponent)del.getToolbarPresenter()).setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
        toolbar.add(del.getToolbarPresenter());

        NewAction n= (NewAction)SystemAction.get(NewAction.class);
        ((JComponent)n.getToolbarPresenter()).setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
        toolbar.add(n.getToolbarPresenter());
        
        return toolbar;
    }
    
    public void edit() {
        
    }

    
    /**
     * Used to create an instance of the JComponent used for the properties component. Usually a subclass of PropertySheetView.
     * @return JComponent
     */
    public JComponent createPropertiesComponent(){
        return null;
    }
 
    private static class ToolBarView extends JToolBar implements ExplorerManager.Provider, Lookup.Provider, org.openide.cookies.EditCookie {
        private ExplorerManager manager;
        private Lookup lookup;
        ToolBarView(final ExplorerManager manager, org.openide.nodes.Node root) {
            
            this.manager=manager;
            // same as before...

            ActionMap map = getActionMap();
            // ...and initialization of lookup variable
            lookup = ExplorerUtils.createLookup (manager, map);

            ChoiceView cView = new ChoiceView();
            ((NodeListModel)(cView.getModel())).setNode(root);
            //cView.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));

            cView.setPreferredSize(new java.awt.Dimension(150,19));
            ((NodeListModel)(cView.getModel())).setDepth(3);
            add(cView);
            add(new javax.swing.JSeparator());
            
        }
        // ...method as before and getLookup
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        // ...methods as before, but replace componentActivated and
        // componentDeactivated with e.g.:
        
        public void addNotify() {
            //System.out.println("addNotify()");
            super.addNotify();
            ExplorerUtils.activateActions(manager, true);
        }
        public void removeNotify() {
            //System.out.println("removeNotify()");
            ExplorerUtils.activateActions(manager, false);
            //super.removeNotify();
        }
        
        public void edit() {
            System.out.println("edit");
        }
    }
    
    public interface EditSourceCookie extends Node.Cookie {
        public org.openide.filesystems.FileObject getSource();
        public void open();
    }
    
    
}
