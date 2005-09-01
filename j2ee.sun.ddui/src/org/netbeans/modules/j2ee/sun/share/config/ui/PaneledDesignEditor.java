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

public class PaneledDesignEditor extends AbstractDesignEditor {
    
    public static final int CONTENT_RIGHT = 0;
    public static final int CONTENT_LEFT = 1;
    
    /** The default width of the ComponentInspector */
    public static final int DEFAULT_STRUCTURE_WIDTH = 170;
    /** The default height of the ComponentInspector */
    public static final int DEFAULT_STRUCTURE_HEIGHT = 300;
    /** The default percents of the splitting of the ComponentInspector */
    public static final int DEFAULT_STRUCTURE_SPLIT = 500;
    public static final int DEFAULT_CONTENT_SPLIT = 150;
    
    protected static EmptyInspectorNode emptyInspectorNode;
    
    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
    "/org/netbeans/modules/form/resources/emptyInspector"; // NOI18N
    
    
    
    protected JSplitPane split1;
    protected JSplitPane split2;
    protected int panelOrientation;
    
    /** The icon for ComponentInspector */
    protected static String iconURL = "/org/netbeans/modules/form/resources/inspector.gif"; // NOI18N
    
    protected static final long serialVersionUID =1L;
    
    
    protected PaneledDesignEditor(){
    }
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public PaneledDesignEditor(PanelView panel){
        contentView = panel;
        contentView.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
        initComponents();
        setRootContext(panel.getRoot());
    }
    
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     * @param orientation Determines if the content pane is on the left or the right.
     */
    public PaneledDesignEditor(PanelView panel, int orientation){
        contentView = panel;
        contentView.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
        panelOrientation = orientation;
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
    
    public PaneledDesignEditor(PanelView panel, JComponent structure){
        contentView = panel;
        contentView.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
        structureView = structure;
        initComponents();
        setRootContext(panel.getRoot());
    }
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     * @param structure The JComponent that will be used in the structure pane. Should follow the
     *                  ExplorerManager protocol. Will usually be some subclass of BeanTreeView.
     * @param orientation Determines if the content pane is on the left or the right.
     */
    
    public PaneledDesignEditor(PanelView panel, JComponent structure, int orientation){
        contentView = panel;
        contentView.setPreferredSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
        panelOrientation = orientation;
        structureView = structure;
        initComponents();
        setRootContext(panel.getRoot());
    }
    
    
    protected void initComponents() {
        super.initComponents();
        createVerticalSplit();
        createHorizontalSplit();
        add(BorderLayout.CENTER, split1);
        

    }
    
    protected void createHorizontalSplit() {
        if (panelOrientation == 1)
            split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,getContentView(), split2);
        else
            split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,split2, getContentView());
        //split1.setDividerLocation(DEFAULT_CONTENT_SPLIT);
        split1.setOneTouchExpandable(true);
        
    }
    
    protected void createVerticalSplit() {
        split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,getStructureView(),getPropertiesView());
        split2.setPreferredSize(new Dimension(200,400));
        split2.setMinimumSize(new Dimension(100,100));
        split2.setOneTouchExpandable(true);
        split2.setDividerLocation(DEFAULT_STRUCTURE_SPLIT);
    }
    

    
    /**
     * Used to get the JComponent used for the structure pane. Usually a container for the structure component or the structure component itself.
     * @return the JComponent
     */
    public JComponent getStructureView(){
        if (structureView ==null){
            structureView = createStructureComponent();
            structureView.getAccessibleContext().setAccessibleName("ACS_StructureView");
            structureView.getAccessibleContext().setAccessibleDescription("ACSD_StructureView");
            structureView.setMinimumSize(new Dimension(100,100));
            structureView.setPreferredSize(new Dimension(DEFAULT_STRUCTURE_WIDTH ,DEFAULT_STRUCTURE_HEIGHT ));
        }
        return structureView;
    }
    /**
     * Used to create an instance of the JComponent used for the structure component. Usually a subclass of BeanTreeView.
     * @return the JComponent
     */
    public JComponent createStructureComponent() {
        return new BeanTreeView();
    }
    
    /**
     * Used to get the JComponent used for the properties pane. Usually a subclass of PropertySheetView.
     * @return the JComponent
     */
    
    public JComponent getPropertiesView(){
        if (propertiesView == null){
            propertiesView = createPropertiesComponent();
            propertiesView.addPropertyChangeListener(new PropertiesDisplayListener());
            propertiesView.setMinimumSize(new Dimension(100,100));
            propertiesView.setPreferredSize(new Dimension(100,100));
        }
        return propertiesView;
    }
    
    /**
     * Used to create an instance of the JComponent used for the properties component. Usually a subclass of PropertySheetView.
     * @return JComponent
     */
    public JComponent createPropertiesComponent() {
        return new PropertySheetView();
    }
}
