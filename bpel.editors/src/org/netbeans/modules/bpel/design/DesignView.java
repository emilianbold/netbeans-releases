/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.design;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.modules.bpel.design.decoration.DecorationManager;
import org.netbeans.modules.bpel.design.decoration.components.DiagramButton;
import org.netbeans.modules.bpel.design.decoration.providers.CollapseExpandDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.DebuggerDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.LinkToolDecorationProvider;

import org.netbeans.modules.bpel.design.decoration.providers.ToolbarDecorationProvider;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.DiagramModelHierarchyIterator;
import org.netbeans.modules.bpel.design.model.DiagramModelIterator;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.model.patterns.SequencePattern;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.utils.Util;
import org.netbeans.modules.bpel.editors.multiview.DesignerMultiViewElement;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;

import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.ConnectionManager;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.FlowlinkTool;
import org.netbeans.modules.bpel.design.selection.GhostSelection;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;

import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.soa.ui.tnv.api.ThumbnailPaintable;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.HelpCtx;

import org.openide.util.Lookup;

import org.openide.nodes.Node;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.bpel.core.validation.BPELValidationController;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProviderFactory;
import org.netbeans.modules.bpel.design.decoration.components.GlassPane;
import org.netbeans.modules.bpel.design.decoration.providers.SelectionDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.ValidationDecorationProvider;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.phmode.PlaceHolderIterator;
import org.netbeans.modules.bpel.design.phmode.PlaceHolderIteratorImpl;
import org.netbeans.modules.bpel.design.phmode.PlaceHolderSelectionModel;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.ExternalBpelEditorTopComponentListener;
import org.netbeans.modules.bpel.editors.multiview.ThumbScrollPane;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

public class DesignView extends JPanel implements
        Autoscroll, HelpCtx.Provider, ThumbScrollPane.Thumbnailable {
    
    public static double CORNER45 = Math.PI/4.0;
    private static final long serialVersionUID = 1;
    private double zoom = 1;
    private Lookup lookup;
    private LayoutManager layoutManager;
    private ConnectionManager connectionManager;
    private DropTarget dTarget;
    private DiagramModel diagramModel;
    private EntitySelectionModel selectionModel;
    
    private MouseHandler mouseHandler;
    private DnDHandler dndHandler;
    private GhostSelection ghost;
    private PlaceHolderManager placeHolderManager;
    private PlaceHolderSelectionModel phSelectionModel;
    private CopyPasteHandler copyPasteHandler;
    private FlowlinkTool flowLinkTool;
    private NameEditor nameEditor;
    private ErrorPanel errorPanel;
    
    private ZoomManager zoomManager;
    private DecorationManager decorationManager;
    
    private SelectionDecorationProvider selectionDecorationProvider;
    private ValidationDecorationProvider validationDecorationProvider;
    private DebuggerDecorationProvider debuggerDecorationProvider;
    
    private ToolbarDecorationProvider toolbarDecorationProvider;
    private LinkToolDecorationProvider linkToolDecorationProvider;
    private CollapseExpandDecorationProvider collapseExpandDectorationProvider;
    
    private SelectionBridge selectionBridge;
    private List mExternalTopComponentListeners = new ArrayList(4);
    private NavigationTools navigationTools;
    private RightStripe rightStripe;
    private DesignViewMode designViewMode = DesignViewMode.DESIGN;
    
    public DesignView(Lookup lookup) {
        super(new DesignViewLayout());
        
        zoomManager = new ZoomManager(this);
        navigationTools = new NavigationTools(this);
        rightStripe = new RightStripe(this);
        
        setBackground(new Color(0xFCFAF5));
        
        this.lookup = lookup;
        
        
        layoutManager = new LayoutManager(this);
        connectionManager = new ConnectionManager(this);
        diagramModel = new DiagramModel(this);
        
        selectionModel = new EntitySelectionModel(diagramModel);
        mouseHandler = new MouseHandler(this);
        dndHandler = new DnDHandler(this);
        
        ghost = new GhostSelection(this);
        flowLinkTool = new FlowlinkTool(this);
        placeHolderManager = new PlaceHolderManager(this);
        copyPasteHandler = new CopyPasteHandler(this);
        phSelectionModel = new PlaceHolderSelectionModel(placeHolderManager);
        
        nameEditor = new NameEditor(this);
        selectionBridge = new SelectionBridge(this);
        setFocusable(true);
        registerActions();
        errorPanel = new ErrorPanel(this);
        decorationManager = new DecorationManager(this);
        loadDecorationProviders();
        
        reloadModel();
        diagramChanged();
        
        initializeExternalListeners();
        notifyExternalLoadersTopComponentCreated();
        
        ToolTipManager.sharedInstance().registerComponent(this);
        setFocusCycleRoot(true);
        setFocusTraversalKeysEnabled(false);

        // vlv: print
        String name = getBPELModel().getProcess().getName() + ""; // NOI18N
        putClientProperty(java.awt.print.Printable.class, name);
    }
    
    public NavigationTools getNavigationTools() {
        return navigationTools;
    }
    
    public RightStripe getRightStripe() {
        return rightStripe;
    }
    
    public void reloadModel() {
        
        if (getBPELModel().getState().equals(BpelModel.State.VALID)){
            BpelEntity selected = selectionModel.getSelected();
            
            Process process = getProcessModel();
            
            try {
                diagramModel.setRootPattern((process != null)
                ? diagramModel.createPattern(process) : null);
            } catch(Exception ex){
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
            
            
            selectionModel.setSelected(selected);
            
        } else {
            diagramModel.setRootPattern(null);
            selectionModel.clear();
        }
    }
    
    public void handleAllPatterns(PatternHandler handler) {
        handlePattern(getRootPattern(), handler);
    }
    
    private void handlePattern(Pattern pattern, PatternHandler handler) {
        if (pattern == null) return;
        
        if (pattern.isSelectable()) {
            handler.handle(pattern);
        }
        
        if (pattern instanceof CompositePattern) {
            for (Pattern child : ((CompositePattern) pattern)
                    .getNestedPatterns()) 
            {
                handlePattern(child, handler);
            }
        }
    }
    
    
    public Point getFocusAreaCenter(Pattern pattern) {
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            
            if (border != null) {
                return convertDiagramToScreen(border.getBounds()
                        .getTopCenter());
            }
        }
        
        return convertDiagramToScreen(pattern.getFirstElement().getBounds()
                .getCenter());
    }
    
    
    public Rectangle getFocusAreaBounds(Pattern pattern) {
        FPoint topLeft = null;
        FPoint bottomRight = null;
        
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            
            if (border != null) {
                FBounds bounds = border.getBounds();
                topLeft = bounds.getTopLeft();
                bottomRight = bounds.getTopRight(); // currect!
            }
        }
        
        if (topLeft == null) {
            FBounds bounds = pattern.getFirstElement().getBounds();
            topLeft = bounds.getTopLeft();
            bottomRight = bounds.getBottomRight();
        }
        
        Point p1 = convertDiagramToScreen(topLeft);
        Point p2 = convertDiagramToScreen(bottomRight);
        
        return new Rectangle(p1.x - 24, p1.y - 24, 
                p2.x - p1.x + 48, p2.y - p1.y + 48);
    }
    
    
    public String getToolTipText(MouseEvent event) {
        Point point = getMousePosition();
        
        if (point == null) {
            point = event.getPoint();
        }
        String result = null;
        VisualElement element = findElement(point);
        Pattern pattern = (element == null) ? null : element.getPattern();
        
        if (pattern != null) {
            if ((pattern instanceof PartnerlinkPattern) 
                    && !(element instanceof BorderElement))
            {
                result = element.getText();
                result = ((result != null) ? result.trim() : "") + " " + NbBundle.getMessage(DesignView.class, "LBL_Operation"); // NOI18N
            } else {
                Node node = getNodeForPattern(pattern);

                if (node != null) {
                    result = node.getDisplayName();
                }

                if (result == null) {
                    result = pattern.getText();
                }
            }
        }
        
        if (result != null && result.trim().length() == 0) {
            result = null;
        }
        if (result == null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ToolTipManager.sharedInstance().setEnabled(false);
                    ToolTipManager.sharedInstance().setEnabled(true);
                }
            });
        }
        return result;
    }
    
    
    public Point getToolTipLocation(MouseEvent event) {
        Point p = getMousePosition();
        
        if (p != null) {
            p.y += 20;
        }
        return p;
    }
    
    public void closeView() {
        if (diagramModel != null){
            diagramModel.release();
            diagramModel = null;
        }
        
        
        getDecorationManager().release();
                
        
       
        
        selectionBridge.release();
        
        notifyExternalLoadersTopComponentClosed();
    }
    
    public BPELValidationController getValidationController() {
        return (BPELValidationController) getLookup()
        .lookup(BPELValidationController.class);
    }
    
    
    
    public EntitySelectionModel getSelectionModel() {
        return selectionModel;
    }
    
    public PlaceHolderSelectionModel getPhSelectionModel() {
        return phSelectionModel;
    }
    
    public GhostSelection getGhost() {
        return ghost;
    }
    
    public LayoutManager getLayoutManager(){
        return layoutManager;
    }
    
    public NameEditor getNameEditor() {
        return nameEditor;
    }
    
    
    public FlowlinkTool getFlowLinkTool() {
        return flowLinkTool;
    }
    
    
    public PlaceHolderManager getPlaceHolderManager() {
        return placeHolderManager;
    }
    
    public CopyPasteHandler getCopyPasteHandler() {
        return copyPasteHandler;
    } 
    
    
    public DecorationManager getDecorationManager() {
        return decorationManager;
    }
    
    
    public ZoomManager getZoomManager() {
        return zoomManager;
    }
    
    
    public BpelModel getBPELModel(){
        return (BpelModel)getLookup().lookup(BpelModel.class);
    }
    /**
     * Helper to access root element of BPEL OM tree
     * NB! Do not cache object returned
     * @return Process element
     **/
    public Process getProcessModel() {
        try {
            BpelModel model = getBPELModel();
            return model.getProcess();
        } catch (Exception ex){
            
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
        return null;
    }
    
    public NodeFactory getNodeFactory(){
        return PropertyNodeFactory.getInstance();
    }
    
    
    public BusinessProcessHelper getProcessHelper() {
        return (BusinessProcessHelper) lookup.lookup(BusinessProcessHelper.class);
    }
    
    
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    
    public Node getNodeForPattern(Pattern pattern){
        if (pattern == null){
            return null;
        }
        
        NodeType nodeType = Util.getBasicNodeType(pattern
                .getOMReference());
        
        NodeFactory factory = getNodeFactory();
        
        if (factory != null){
            return factory.createNode(
                    nodeType,
                    pattern.getOMReference(),
                    DesignView.this.getLookup());
        }
        
        return null;
    }
    
    
    
    
    
    public void performDefaultAction(Pattern pattern) {
        Node node = getNodeForPattern(pattern);
        
        if (node == null){
            return;
        }
        
        Action action = node.getPreferredAction();
        
        if (action != null) {
            action.actionPerformed(new ActionEvent(this, 0, "DBC"));
        }
    }
    
    
    
    public boolean showCustomEditor(Pattern pattern, 
            CustomNodeEditor.EditingMode editingMode) {
        Node node = getNodeForPattern(pattern);
        
        if (node == null){
            return false;
        }
        
        if (getModel().isReadOnly()) {
            return false;
        }
        
        return NodeUtils.showNodeCustomEditor(node, editingMode);
    }
    
    
    public Lookup getLookup() {
        return lookup;
    }
    

    private void registerActions() {
        InputMap im1 = getInputMap(WHEN_FOCUSED);
        InputMap im2 = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "rename-something"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete-something"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-something"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), "gotosource-something"); // NOI18N
//        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.ALT_DOWN_MASK), "findusages-something"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "find_next_mex_peer"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "show_context_menu"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), "show_context_menu"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "go_next_hierarchy_component"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "go_previous_hierarchy_component"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "collapse-current-pattern"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "expand-current-pattern"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), "expand-all-patterns"); // NOI18N

        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy-pattern"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "paste-pattern"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "cut-pattern"); // NOI18N
        
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "rename-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), "gotosource-something"); // NOI18N
//        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.ALT_DOWN_MASK), "findusages-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "find_next_mex_peer"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "show_context_menu"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), "show_context_menu"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_right_component"); // NOI18N
//        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_right_component"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_left_component"); // NOI18N
//        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_left_component"); // NOI18N
        
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_down_component"); // NOI18N
//        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_down_component"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_up_component"); // NOI18N
//        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, KeyEvent.SHIFT_DOWN_MASK), "go_nearest_up_component"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "go_next_hierarchy_component"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "go_previous_hierarchy_component"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "collapse-current-pattern"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "expand-current-pattern"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), "expand-all-patterns"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy-pattern"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "paste-pattern"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "cut-pattern"); // NOI18N

        am.put("rename-something", new RenameAction()); // NOI18N
        am.put("delete-something", new DeleteAction()); // NOI18N
        am.put("cancel-something", new CancelAction()); // NOI18N
        am.put("gotosource-something", new GoToSourceAction()); // NOI18N
//        am.put("findusages-something", new FindUsagesAction()); // NOI18N
        am.put("find_next_mex_peer", new CycleMexAction()); // NOI18N
        am.put("show_context_menu", new ShowContextMenu()); // NOI18N
        am.put("go_next_hierarchy_component", new GoNextHieComponentAction()); // NOI18N
        am.put("go_previous_hierarchy_component", new GoPrevHieComponentAction()); // NOI18N
        
        am.put("go_nearest_right_component", new GoRightNearestComponentAction()); // NOI18N
        am.put("go_nearest_left_component", new GoLeftNearestComponentAction()); // NOI18N
        am.put("go_nearest_up_component", new GoUpNearestComponentAction()); // NOI18N
        am.put("go_nearest_down_component", new GoDownNearestComponentAction()); // NOI18N

        am.put("expand-current-pattern", new ExpandCurrentPatternAction()); // NOI18N
        am.put("collapse-current-pattern", new CollapseCurrentPatternAction()); // NOI18N
        am.put("expand-all-patterns", new ExpandAllPatternsAction());
        
        am.put("copy-pattern", new CopyAction()); // NOI18N
        am.put("cut-pattern", new CutAction()); // NOI18N
        am.put("paste-pattern", new PasteAction()); // NOI18N

/**
         im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy-pattern"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "paste-pattern"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "cut-pattern"); // NOI18N
*/        
        
    }
    
    
    public CollapseExpandDecorationProvider getCollapseExpandDecorationProvider() {
        return collapseExpandDectorationProvider;
    }
    
    
    public DiagramModel getModel() {
        return diagramModel;
    }
    
    
    public FDimension getDiagramSize() {
        FDimension dim =  getModel().getRootPattern().getBounds().getSize();
        return dim;
//        new FDimension(dim.width + LayoutManager.HSPACING * 2,
//                dim.height + LayoutManager.VSPACING * 2);
    }
    
    
    public void updateAccordingToViewFiltersStatus() {
        if (getRootPattern() != null) {
            updateAccordingToViewFiltersStatus(getRootPattern());
            diagramChanged();
        }
    }

    private void updateAccordingToViewFiltersStatus(Pattern pattern) {
        if (pattern instanceof SequencePattern) {
            ((SequencePattern) pattern).updateAccordingToViewFiltersStatus();
        } else if (pattern instanceof ProcessPattern) {
            ((ProcessPattern) pattern).updateAccordingToViewFiltersStatus();
        }
        
        if (pattern instanceof CompositePattern) {
            for (Pattern p : ((CompositePattern) pattern).getNestedPatterns()) {
                updateAccordingToViewFiltersStatus(p);
            }
        }
    }
    
    
    public void diagramChanged() {
        if (getProcessModel() != null
                && getBPELModel().getState() == Model.State.VALID
                && getModel().getRootPattern() != null) {
            
            
            double zoom = getCorrectedZoom();
            setToolBarEnabled(true);
            
            layoutManager.layout();
            
            connectionManager.reconnectAll();
            
            connectionManager.layoutConnections();
            
            FDimension dim = getModel().getRootPattern().getBounds().getSize();
            
            int printWidth = (int) Math.round(dim.width
                    + 2 * LayoutManager.HMARGIN);
            
            int printHeight = (int) Math.round(dim.height
                    + 2 * LayoutManager.VMARGIN);
            
            putClientProperty(Dimension.class, new Dimension(printWidth, printHeight));
            
            getDecorationManager().repositionComponentsRecursive();
            
            revalidate();
            repaint();
            errorPanel.uninstall();
            rightStripe.repaint();
        } else {
            setToolBarEnabled(false);
            errorPanel.install();
        }
    }
    
    private void setToolBarEnabled(final boolean enabled) {
        zoomManager.setEnabled(enabled);
        navigationTools.setEnabled(enabled);

        JComponent toolBar = findToolBar();
        
        if (toolBar != null) {
            setToolBarEnabled(toolBar, enabled);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setToolBarEnabled(findToolBar(), enabled);
                }
            });
        }
    }
    
    private void setToolBarEnabled(JComponent toolBar, boolean enabled) {
        if (toolBar == null) {
            return;
        }

        for (Component component : toolBar.getComponents()) {
            component.setEnabled(enabled);
        }
    }
    
    private JComponent findToolBar() {
        for (Component c = getView().getParent(); c != null; 
                c = c.getParent()) 
        {
            if (c instanceof DesignerMultiViewElement) {
                return ((DesignerMultiViewElement) c)
                        .getToolbarRepresentation();
            }
        }
        return null;
    }
    
    
    public JComponent getView() {
        return (errorPanel.isInstalled()) ? errorPanel : this;
    }
    
    
    public TopComponent getTopComponent() {
        return (TopComponent) SwingUtilities
                .getAncestorOfClass(TopComponent.class, this);
    }
    
    
    public Font getZoomedDiagramFont() {
        Font font = DiagramFontUtil.getFont();
        return font.deriveFont(font.getSize2D()
        * (float) getCorrectedZoom());
    }
    
    
    public double getZoom() {
        return zoomManager.getScale();
    }
    
    
    
    
    public double getCorrectedZoom() {
        return zoomManager.getScale() * DiagramFontUtil.getZoomCorrection();
    }
    
    
    public Pattern getRootPattern() {
        return diagramModel.getRootPattern();
    }
    
    
    public FPoint convertScreenToDiagram(Point point) {
        return convertScreenToDiagram(point, getCorrectedZoom());
    }
    
    
    public Point convertDiagramToScreen(FPoint point) {
        return convertDiagramToScreen(point, getCorrectedZoom());
    }
    
    
    public Pattern findPattern(Point point) {
        FPoint fPoint = convertScreenToDiagram(point);
        return findPattern(fPoint.x, fPoint.y);
    }
    
    
    public Pattern findPattern(double x, double y) {
        VisualElement element = findElement(x, y);
        return (element != null) ? element.getPattern() : null;
    }
    
    
    public VisualElement findElement(Point point) {
        FPoint fPoint = convertScreenToDiagram(point);
        return findElement(fPoint.x, fPoint.y);
    }
    
    
    public VisualElement findElement(double x, double y) {
        Pattern root = diagramModel.getRootPattern();
        if (root != null) {
            VisualElement result = findElementInPattern(root, x, y);
//            if (result == null) {
//                return ((CompositePattern) root).getBorder();
//            }
            return result;
        }
        return null;
    }
    
    
    public VisualElement findElementInPattern(Pattern pattern,
            double x, double y) {
        boolean isComposite = pattern instanceof CompositePattern;
        
        if (isComposite) {
            CompositePattern composite = (CompositePattern) pattern;
            
            for (Pattern p : composite.getNestedPatterns()) {
                VisualElement res = findElementInPattern(p, x, y);
                if (res != null) {
                    return res;
                }
            }
        }
        
        for (VisualElement e : pattern.getElements()) {
            if (e.contains(x, y)) return e;
        }
        
        if (isComposite) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            if (border != null && border.getShape().contains(x, y)) {
                return border;
            }
        }
//
//
//        TextElement textElement = pattern.getTextElement();
//        if (textElement != null && !textElement.isEmptyText()
//        && textElement.contains(x, y)) {
//            return textElement;
//        }
        
        return null;
    }
    
    
    public void scrollToFocusVisible(Pattern pattern) {
        scrollRectToVisible(getFocusAreaBounds(pattern));
    }
    
    
    public void paintThumbnail(Graphics g) {
        Pattern rootPattern = getRootPattern();
        
        if (rootPattern == null) return;
        
        Graphics2D g2 = GUtils.createGraphics(g);
        
        double zoom = getCorrectedZoom();
        
        Point p = convertDiagramToScreen(new FPoint(0, 0));
        g2.translate(p.x, p.y);
        g2.scale(zoom, zoom);
        
        Graphics2D g2bw = new BWGraphics2D(g2);
        
        Rectangle clipBounds = g2.getClipBounds();
        
        double exWidth = layoutManager.HSPACING * zoom;
        double exHeight = layoutManager.VSPACING * zoom;

        FBounds exClipBounds = new FBounds(
                clipBounds.x - exWidth,
                clipBounds.y - exHeight,
                clipBounds.width + 2 * exWidth,
                clipBounds.height + 2 * exHeight);
        
        paintPatternThumbnail(g2, g2bw, exClipBounds, rootPattern);
        paintPatternThumbnailConnections(g2, g2bw, rootPattern);
        g2.dispose();
        
        Graphics componentGraphics = g.create();
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            Component c = getComponent(i);
            if (c instanceof GlassPane) {
                int tx = c.getX();
                int ty = c.getY();
                componentGraphics.translate(tx, ty);
                ((GlassPane) c).paintThumbnail(componentGraphics);
                componentGraphics.translate(-tx, -ty);
            }
        }
        componentGraphics.dispose();
    }
    
    
    private void paintPatternThumbnail(Graphics2D g2, Graphics2D g2bw, 
            FBounds clipBounds, Pattern pattern) {
        if (!pattern.getBounds().isIntersects(clipBounds)) {
            return;
        }
        
        Decoration decoration = getDecoration(pattern);
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            
            BorderElement border = composite.getBorder();
            
            Graphics2D g = (decoration.hasDimmed()) ? g2bw : g2;
            
            if (border != null) {
                border.paintThumbnail(g);
            }
            
            for (VisualElement e : composite.getElements()) {
                e.paintThumbnail(g);
            }
            
            for (Pattern p : composite.getNestedPatterns()) {
                paintPatternThumbnail(g2, g2bw, clipBounds, p);
            }
            
            if (decoration.hasStroke()) {
                decoration.getStroke().paint(g2, composite.createSelection());
            }
        } else {
            Graphics2D g = (decoration.hasDimmed()) ? g2bw : g2;
            
            for (VisualElement e : pattern.getElements()) {
                e.paintThumbnail(g);
            }
            
            if (decoration.hasStroke()) {
                decoration.getStroke().paint(g2, pattern.createSelection());
            }
        }
    }
    

    private void paintPatternThumbnailConnections(Graphics2D g2, 
            Graphics2D g2bw, Pattern pattern) 
    {
        if (pattern == null) return;
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            for (Pattern p : composite.getNestedPatterns()) {
                paintPatternThumbnailConnections(g2, g2bw, p);
            }
        }
        
        Graphics2D g = (getDecoration(pattern).hasDimmed()) ? g2bw : g2;
        
        for (Connection c : pattern.getConnections()) {
            c.paintThumbnail(g);
        }
    }
    
    
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintContent(g, getCorrectedZoom(), false);
    }
    
    
    private void paintContent(Graphics g, double zoom, boolean printMode) {
        if (getModel() == null) {
            return;
        }
        
        Pattern root = getModel().getRootPattern();
        
        Graphics2D g2 = GUtils.createGraphics(g);
        Graphics2D g2bw = new BWGraphics2D(g2);
        
        if (!printMode) {
            Point p = convertDiagramToScreen(new FPoint(0, 0));
            g2.translate(p.x, p.y);
        }
        
        g2.scale(zoom, zoom);
        
        if (root != null) {
            Rectangle clipBounds = g2.getClipBounds();
            
            double exWidth = layoutManager.HSPACING * zoom;
            double exHeight = layoutManager.VSPACING * zoom;
            
            FBounds exClipBounds = new FBounds(
                    clipBounds.x - exWidth,
                    clipBounds.y - exHeight,
                    clipBounds.width + 2 * exWidth,
                    clipBounds.height + 2 * exHeight);
            
            g2.setFont(DiagramFontUtil.getFont());
            
            paintPattern(g2, g2bw, root, exClipBounds, printMode);
            paintPatternConnections(g2, g2bw, root, printMode);
            
            if (!printMode) {
                placeHolderManager.paint(g2);
                flowLinkTool.paint(g2);
                ghost.paint(g2);
            }
        }
    }
    
    protected void paintChildren(Graphics g) {
        if (!ghost.isEmpty()) return;
        super.paintChildren(g);
    }
    
    protected void printComponent(Graphics g) {
        Dimension s = (Dimension) getClientProperty(Dimension.class);
        if (s == null) return;
        
        Color oldColor = g.getColor();
        g.setColor(getBackground());
        Rectangle clip = g.getClipBounds();
        if (clip != null) {
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
        } else {
            g.fillRect(0, 0, s.width, s.height);
        }
        
        g.setColor(oldColor);
        paintContent(g, 1, true);
    }
    
    protected void printChildren(Graphics g) {}
    
    private void paintPattern(Graphics2D g2, Graphics2D g2bw, Pattern pattern,
            FBounds clipBounds, boolean printMode) {
        
        if (!pattern.getBounds().isIntersects(clipBounds)) {
            return;
        }
        
        Decoration decoration = getDecoration(pattern);
        
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            
            BorderElement border = composite.getBorder();
            
            Graphics2D g = (decoration.hasDimmed() && !printMode) ? g2bw : g2;
            
            
            if (decoration.hasGlow() && !printMode) {
                decoration.getGlow().paint(g2, composite.createOutline());
            }
            
            if (border != null) {
                border.paint(g);
            }
            
            for (VisualElement e : composite.getElements()) {
                e.paint(g);
            }
            
            for (Pattern p : composite.getNestedPatterns()) {
                paintPattern(g2, g2bw, p, clipBounds, printMode);
            }
            
            if (decoration.hasStroke() && !printMode) {
                decoration.getStroke().paint(g2, composite.createSelection());
            }
        } else {
            Graphics2D g = (decoration.hasDimmed() && !printMode) ? g2bw : g2;
            
            if (decoration.hasGlow() && !printMode) {
                decoration.getGlow().paint(g2, pattern.createOutline());
            }
            
            for (VisualElement e : pattern.getElements()) {
                e.paint(g);
            }
            
            if (decoration.hasStroke() && !printMode) {
                decoration.getStroke().paint(g2, pattern.createSelection());
            }
        }
    }
    
    
    private void paintPatternConnections(Graphics2D g2, Graphics2D g2bw,
            Pattern pattern, boolean printMode) {
        if (pattern == null) return;
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            for (Pattern p : composite.getNestedPatterns()) {
                paintPatternConnections(g2, g2bw, p, printMode);
            }
        }
        
        Graphics2D g = (getDecoration(pattern).hasDimmed() && !printMode) ? g2bw : g2;
        
        for (Connection c : pattern.getConnections()) {
            c.paint(g);
        }
    }
    
    
    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new Insets(inner.y - outer.y + AUTOSCROLL_INSETS,
                inner.x - outer.x + AUTOSCROLL_INSETS,
                outer.height - inner.height - inner.y + outer.y + AUTOSCROLL_INSETS,
                outer.width - inner.width - inner.x + outer.x + AUTOSCROLL_INSETS);
    }
    
    
    public void autoscroll(Point location) {
        JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(
                JScrollPane.class, this);
        
        if (scroller != null) {
            repaint();
            
            final int SPEED_FACTOR = 5;
            
            Insets scrollInsets = new Insets(12,12,12,12);
            Rectangle r = getVisibleRect();
            
            int h_distance = 0;
            if(location.x <= r.x + AUTOSCROLL_INSETS) {
                h_distance = location.x - (r.x + AUTOSCROLL_INSETS);
            } else if(location.x >= r.x + r.width - AUTOSCROLL_INSETS){
                h_distance = location.x - (r.x + r.width - AUTOSCROLL_INSETS);

            }
            if(h_distance != 0){
                JScrollBar bar = scroller.getHorizontalScrollBar();
                bar.setValue(bar.getValue() + h_distance * SPEED_FACTOR);
            }
            
            
            int v_dist = 0;
            if(location.y <= r.y + AUTOSCROLL_INSETS) {
                v_dist = location.y - (r.y + AUTOSCROLL_INSETS);
            } else if (location.y >= r.y + r.height - scrollInsets.bottom) {
                v_dist = location.y - (r.y + r.height - scrollInsets.bottom);
            }
            if(v_dist != 0){
                JScrollBar bar = scroller.getVerticalScrollBar();
                bar.setValue(bar.getValue() + v_dist * SPEED_FACTOR);
            }
        }
    }
    
    
    
    
    
    /**
     * ensures that components are added to container in correct order.
     * GlassPanes should go before Buttons
     **/
    protected void addImpl(Component comp, Object constraints, int index) {
        
        int count = getComponentCount();
        
        if (comp instanceof GlassPane){
            index = 0;
            for (int i = 0; i < count; i++) {
                Component c = getComponent(i);
                if ((c instanceof GlassPane) || (c instanceof DiagramButton)) {
                    index = i;
                    break;
                    
                }
            }
            
        } else if (comp instanceof DiagramButton){
            index = -1;
            for (int i = 0; i < count; i++) {
                Component c = getComponent(i);
                if (c instanceof DiagramButton) {
                    index = i;
                    break;
                    
                }
            }
        }
        super.addImpl(comp, constraints, index);
    }
    
    
    abstract class DesignModeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Defines an <code>Action</code> object with a default
         * description string and default icon.
         */
        public DesignModeAction() {
            super();
        }

        /**
         * Defines an <code>Action</code> object with the specified
         * description string and a default icon.
         */
        public DesignModeAction(String name) {
            super(name);
        }

        /**
         * Defines an <code>Action</code> object with the specified
         * description string and a the specified icon.
         */
        public DesignModeAction(String name, Icon icon) {
            super(name, icon);
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && !getModel().isReadOnly() && isDesignMode();
        }
    }
    
    abstract class PhModeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && !getModel().isReadOnly() && !isDesignMode();
        }
    }

    class DeleteAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) return;
            
            Pattern selected = getSelectionModel().getSelectedPattern();
            
            if (selected == null){
                return;
            }
            
            
            Node node = getNodeForPattern(selected);
            
            if (node == null){
                return;
            }
            
            Action[] actions = node.getActions(true);
            if (actions == null){
                return;
            }
            
            for (int i = actions.length - 1; i >= 0; i--) {
                Action action = actions[i];
                if (action instanceof BpelNodeAction){
                    if (((BpelNodeAction) action).getType() == ActionType.REMOVE) {
                        action.actionPerformed(e);
                        return;
                    }
                }
            }
        }
    }
    
    
    class CancelAction extends AbstractAction {
        
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            mouseHandler.cancel();
            exitPlaceHolderMode();
        }
    }
    
    class CopyAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
//            if (getModel().isReadOnly()) {
//                return;
//            }
            
            Pattern copiedPattern = getPatternCopy(getSelectionModel().getSelectedPattern());
            goPlaceHolderMode(copiedPattern, true);
            repaint();
        }
        
        private Pattern getPatternCopy(Pattern pattern) {
            if (pattern == null) {
                return null;
            }
            Pattern copiedPattern = null;
            BpelEntity entity = pattern.getOMReference();
            if (entity == null) {
                return null;
            }

            copiedPattern = diagramModel.createPattern(
                    entity.copy(new HashMap<UniqueId, UniqueId>()));

            return copiedPattern;
        }
        
    }

    class CutAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
//            if (getModel().isReadOnly() || !isDesignMode()) {
//                return;
//            }
            Pattern cuttedPattern = getPatternCut(getSelectionModel().getSelectedPattern());
    
            goPlaceHolderMode(cuttedPattern, false);
            repaint();
        }

        private Pattern getPatternCut(Pattern pattern) {
            if (pattern == null) {
                return null;
            }
            Pattern cuttedPattern = null;
            BpelEntity entity = pattern.getOMReference();
            if (entity == null) {
                return null;
            }

            cuttedPattern = diagramModel.createPattern(entity.cut());

            return cuttedPattern;
        }
    }

    class PasteAction extends PhModeAction {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            if (isDesignMode() || getModel().isReadOnly()) {
                return;
            }
            PlaceHolder ph = getPhSelectionModel().getSelectedPlaceHolder();
            if (ph != null) {
                ph.drop();
            }
            exitPlaceHolderMode();
            repaint();
        }
    }
    
    private void goPlaceHolderMode(Pattern bufferedPattern, boolean isCopyAction) {
        if (bufferedPattern == null) {
            return;
        }
        CopyPasteHandler cpHandler = getCopyPasteHandler();
        cpHandler.initPlaceHolderMode(bufferedPattern);
        setDesignViewMode(isCopyAction 
                ? DesignViewMode.COPY_PLACE_HOLDER 
                : DesignViewMode.CUT_PLACE_HOLDER);
    }
    
    private void exitPlaceHolderMode() {
//        Pattern selectedPattern = getSelectionModel().getSelectedPattern();
//        if (selectedPattern == null) {
//            return;
//        }
        CopyPasteHandler cpHandler = getCopyPasteHandler();
        cpHandler.exitPlaceHolderMode();
        setDesignViewMode(DesignViewMode.DESIGN);
    }

    class RenameAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            if (getModel().isReadOnly()) return;
            getNameEditor().startEdit(getSelectionModel().getSelectedPattern());
        }
    }
    
    
    public Decoration getDecoration(Pattern p) {
        return decorationManager.getDecoration(p);
    }
    
    
    public JButton createExpandAllPatternsToolBarButton() {
        JButton button = new JButton(new ExpandAllPatternsAction());
        button.setText(null);
        button.setFocusable(false);
        return button;
    }
    
    
    public HelpCtx getHelpCtx() {
        HelpCtx helpCtx = new HelpCtx(DesignView.class);
        Pattern selected = getSelectionModel().getSelectedPattern();
        if (selected == null){
            return helpCtx;
        }
        
        Node node = getNodeForPattern(selected);
        if (node == null){
            return helpCtx;
        }
        
        HelpCtx nodeHelpCtx = node.getHelpCtx();
        
        return nodeHelpCtx == null ? helpCtx : nodeHelpCtx;
    }
    
    class CycleMexAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            Pattern selected = getSelectionModel().getSelectedPattern();
            
            if (selected == null){
                return;
            }
            
            
            Node node = getNodeForPattern(selected);
            
            if (node == null){
                return;
            }
            
            Action[] actions = node.getActions(true);
            if (actions == null){
                return;
            }
            
            for (int i = actions.length - 1; i >= 0; i--) {
                Action action = actions[i];
                if (action instanceof BpelNodeAction &&
                        ((BpelNodeAction) action).getType() == ActionType.CYCLE_MEX) {
                    ((BpelNodeAction) action).performAction(new Node[] {node} );
                    return;
                }
            }
        }
    }
    
    
    class GoToSourceAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            Pattern selected = getSelectionModel().getSelectedPattern();
            
            if (selected == null){
                return;
            }
            
            
            Node node = getNodeForPattern(selected);
            
            if (node == null){
                return;
            }
            
            Action[] actions = node.getActions(true);
            if (actions == null){
                return;
            }
            
            for (int i = actions.length - 1; i >= 0; i--) {
                Action action = actions[i];
                if (action instanceof org.netbeans.modules.bpel.nodes.actions.GoToSourceAction){
                    ((org.netbeans.modules.bpel.nodes.actions.GoToSourceAction)action).performAction(new Node[] {node} );
                    return;
                }
            }
        }
    }
    
    class FindUsagesAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            Pattern selected = getSelectionModel().getSelectedPattern();
            
            if (selected == null){
                return;
            }
            
            
            Node node = getNodeForPattern(selected);
            
            if (node == null){
                return;
            }
            
            Action[] actions = node.getActions(true);
            if (actions == null){
                return;
            }
            
            for (int i = actions.length - 1; i >= 0; i--) {
                Action action = actions[i];
                if (action instanceof org.netbeans.modules.bpel.nodes.actions.FindUsagesAction){
                    ((org.netbeans.modules.bpel.nodes.actions.FindUsagesAction)action).performAction(new Node[] {node} );
                    return;
                }
            }
        }
    }
    
    abstract class GoHieComponentAction extends AbstractGoThroughDesignAction {
        protected DiagramModelIterator getDiagramModelIterator() {
            return new DiagramModelHierarchyIterator(getModel(), getSelectionModel());
        }
        
        protected PlaceHolderIterator getPlaceHolderIterator() {
            return new PlaceHolderIteratorImpl(getPlaceHolderManager(), getPhSelectionModel());
        }
    }
    
    
    class ExpandCurrentPatternAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent event) {
//            System.out.println("ExpandCurrentPatternAction");
            Pattern pattern = getSelectionModel().getSelectedPattern();
            if (pattern == null) return;
            if (!(pattern instanceof CollapsedPattern)) return;

            getCollapseExpandDecorationProvider()
                    .createCollapseExpandAction(pattern)
                    .actionPerformed(event);
       }
    }

    
    class CollapseCurrentPatternAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent event) {
//            System.out.println("CollapseCurrentPatternAction");
            Pattern pattern = getSelectionModel().getSelectedPattern();
            if (pattern == null) return;
            if (pattern instanceof CollapsedPattern) return;

            Action action = getCollapseExpandDecorationProvider()
                    .createCollapseExpandAction(pattern);
            
            if (action != null) {
                action.actionPerformed(event);
            }
        }
    }

    
    class ExpandAllPatternsAction extends DesignModeAction {
        private static final long serialVersionUID = 1L;
        
        
        public ExpandAllPatternsAction() {
            super(NbBundle.getMessage(DesignView.class, "LBL_ExpandAll"), 
                    new ImageIcon(DesignView.class
                    .getResource("resources/expand_all.png")));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(DesignView.class, 
                    "LBL_ExpandAll_Description"));
        }
        
        
        public void actionPerformed(ActionEvent event) {
            getModel().expandAll();
        }
    }

    abstract class GoNearestComponentAction extends AbstractAction implements PatternHandler {
        private static final long serialVersionUID = 1L;
        private Pattern nearestPattern;
        private Pattern selPattern;
        private double bestDistance;

        public void actionPerformed(ActionEvent e) {
            nearestPattern = null;
//            System.out.println("try to perform go nearest "+getClass()+" action ....");
            EntitySelectionModel selModel = getSelectionModel();
            selPattern = selModel != null ? selModel.getSelectedPattern() :null;
            handleAllPatterns(this);
            
            bestDistance = Double.POSITIVE_INFINITY;
            
            if (nearestPattern != null) {
//                System.out.println("founded nearest Pattern : "+nearestPattern);
                
                getSelectionModel().setSelectedPattern(nearestPattern);
                scrollToFocusVisible(nearestPattern);
            }
        }

        public void handle(Pattern pattern) {
            assert pattern != null;
            
            Point ps = getFocusAreaCenter(selPattern);
            Point pc = getFocusAreaCenter(pattern);
            
            if (!isCorrectPattern(ps, pc)) {
                return;
            }
            
            double dist = ps.distance(pc);
            
            if (dist < bestDistance) {
                bestDistance = dist;
                nearestPattern = pattern;
            }
        }
        
//        protected double getDistance(Pattern mainPattern, Pattern pattern2) {
//            double distance = -1;
//            Point mainP = getFocusAreaCenter(mainPattern);
//            Point p2 = getFocusAreaCenter(pattern2);
//            if (mainP != null 
//                    && p2 != null 
//                    && isCorrectPattern(mainP, p2)) 
//            {
//                distance = mainP.distance(p2);
//            }
//            
//            return distance;
//        }
        
        protected abstract boolean isCorrectPattern(Point mainPoint, Point nextPoint);
    }
    
    class GoRightNearestComponentAction extends GoNearestComponentAction {
        protected boolean isCorrectPattern(Point mainPoint, Point nextPoint) {
            assert mainPoint != null;
            
//            double dy = nextPoint.y - mainPoint.y;
//            double dx = nextPoint.x - mainPoint.x;
//            
//            return Math.abs(Math.toDegrees(Math.atan2(dy, dx))) <= 45;
            boolean isCorrect = nextPoint != null;
            if (isCorrect) {
                isCorrect = mainPoint.x < nextPoint.x;
            }
            
////            if (isCorrect) {
////                double dy = mainPoint.y - nextPoint.y;
////                double dx = mainPoint.x - nextPoint.x;
////                isCorrect = Math.atan2(Math.abs(dy),dx) <= CORNER45;
////            }
            
            return isCorrect;
        }
    }
    
    class GoLeftNearestComponentAction extends GoNearestComponentAction {
        protected boolean isCorrectPattern(Point mainPoint, Point nextPoint) {
            assert mainPoint != null;
            boolean isCorrect = nextPoint != null;
            if (isCorrect) {
                isCorrect = mainPoint.x > nextPoint.x;
            }
            
////            if (isCorrect) {
////                double dy = mainPoint.y - nextPoint.y;
////                double dx = mainPoint.x - nextPoint.x;
////                isCorrect = Math.atan2(Math.abs(dy),dx) <= CORNER45;
////            }
            
            return isCorrect;
        }
    }

    class GoDownNearestComponentAction extends GoNearestComponentAction {
        protected boolean isCorrectPattern(Point mainPoint, Point nextPoint) {
            assert mainPoint != null;
            boolean isCorrect = nextPoint != null;
            if (isCorrect) {
                isCorrect = mainPoint.y < nextPoint.y;
            }
            
////            if (isCorrect) {
////                double dx = nextPoint.y - mainPoint.y ;
////                double dy = mainPoint.x - nextPoint.x;
////                isCorrect = Math.atan2(Math.abs(dy),dx) <= CORNER45;
////            }
////
            return isCorrect;
        }
    }

    class GoUpNearestComponentAction extends GoNearestComponentAction {
        protected boolean isCorrectPattern(Point mainPoint, Point nextPoint) {
            assert mainPoint != null;
            boolean isCorrect = nextPoint != null;
            if (isCorrect) {
                isCorrect = mainPoint.y > nextPoint.y;
            }
            
////            if (isCorrect) {
////                double dx = mainPoint.y - nextPoint.y ;
////                double dy = mainPoint.x - nextPoint.x;
////                isCorrect = Math.atan2(Math.abs(dy),dx) <= CORNER45;
////            }
////
            return isCorrect;
        }
    }

    class GoNextHieComponentAction extends GoHieComponentAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            if (isDesignMode()) {
                Pattern nextPattern = getDiagramModelIterator().next();
                if (nextPattern != null) {
                    getSelectionModel().setSelectedPattern(nextPattern);
                    scrollSelectedToView();
                }
            } else {
                PlaceHolder nextPh = getPlaceHolderIterator().next();
                if (nextPh != null) {
                    getPhSelectionModel().setSelectedPlaceHolder(nextPh);
                    scrollPlaceHolderToView(nextPh);
                    repaint();
                }
            }
        }
        
    }
    
    class GoPrevHieComponentAction extends GoHieComponentAction {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            if (isDesignMode()) {
                Pattern prevPattern = getDiagramModelIterator().previous();
                if (prevPattern != null) {
                    getSelectionModel().setSelectedPattern(prevPattern);
                    scrollSelectedToView();
                }
            } else {
                PlaceHolder prevPh = getPlaceHolderIterator().previous();
                if (prevPh != null) {
                    getPhSelectionModel().setSelectedPlaceHolder(prevPh);
                    scrollPlaceHolderToView(prevPh);
                    repaint();
                }
            }
        }
    }

    class ShowContextMenu extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            Pattern p = getSelectionModel().getSelectedPattern();
            
            if (p == null) {
                return;
            }
            
            JPopupMenu menu = p.createPopupMenu();
            
            FBounds r;
            
            if (p instanceof ProcessPattern) {
                r = ((ProcessPattern) p).getBorder().getBounds();
            } else {
                r = p.getBounds();
            }
            
            Point topLeft = convertDiagramToScreen(r.getTopLeft());
            Point bottomRight = convertDiagramToScreen(r.getBottomRight());
            
            Rectangle vr = getVisibleRect();
            
            int x1 = Math.max(topLeft.x, vr.x);
            int y1 = Math.max(topLeft.y, vr.y);
            
            int x2 = Math.min(bottomRight.x, vr.x + vr.width);
            int y2 = Math.min(bottomRight.y, vr.y + vr.height);
            
            int px = topLeft.x;
            int py = topLeft.y;
            
            if (x1 <= x2) {
                px = x1;
            } else if (px <= vr.x) {
                px = vr.x;
            } else if (px >= vr.x + vr.width) {
                px = vr.x + vr.width;
            }
            
            if (y1 <= y2) {
                py = y1;
            } else if (py <= vr.y) {
                py = vr.y;
            } else if (py >= vr.y + vr.height) {
                py = vr.y + vr.height;
            }
            
            menu.show(DesignView.this, px, py);
        }
    }

    
    
    
    public static FPoint convertScreenToDiagram(Point point, double zoom) {
        double x = ((point.x - DesignViewLayout.MARGIN_LEFT) / zoom)
        + LayoutManager.HMARGIN;
        
        double y = ((point.y - DesignViewLayout.MARGIN_TOP) / zoom)
        + LayoutManager.VMARGIN;
        
        return new FPoint(x, y);
    }
    
    
    public static Point convertDiagramToScreen(FPoint point, double zoom) {
        double x = (point.x - LayoutManager.HMARGIN) * zoom
                + DesignViewLayout.MARGIN_LEFT;
        double y = (point.y - LayoutManager.VMARGIN) * zoom
                + DesignViewLayout.MARGIN_TOP;
        
        return new Point((int) Math.round(x), (int) Math.round(y));
    }
    
    
    public void scrollPatternToView(Pattern pattern) {
        if (pattern == null){
            return;
        }
        /**
         * Get the position of selected node and scroll view to make
         * the corresponding pattern visible
         **/

        FBounds bounds = pattern.getBounds();

        Point screenTL = convertDiagramToScreen(bounds.getTopLeft());
        Point screenBR = convertDiagramToScreen(bounds.getBottomRight());

        int x1 = Math.max(0, screenTL.x - 8);
        int y1 = Math.max(0, screenTL.y - 32);

        int x2 = Math.min(getWidth(), screenBR.x + 8);
        int y2 = Math.min(getHeight(), screenBR.y + 8);

        int w = Math.max(1, x2 - x1);
        int h = Math.max(1, y2 - y1);

        scrollRectToVisible(new Rectangle(x1, y1, w, h));
    }
    
    public void scrollPlaceHolderToView(PlaceHolder ph) {
        if (ph == null){
            return;
        }
        /**
         * Get the position of selected node and scroll view to make
         * the corresponding pattern visible
         **/

        FShape shape = ph.getShape();

        Point screenTL = convertDiagramToScreen(shape.getTopLeft());
        Point screenBR = convertDiagramToScreen(shape.getBottomRight());

        int x1 = Math.max(0, screenTL.x - 8);
        int y1 = Math.max(0, screenTL.y - 32);

        int x2 = Math.min(getWidth(), screenBR.x + 8);
        int y2 = Math.min(getHeight(), screenBR.y + 8);

        int w = Math.max(1, x2 - x1);
        int h = Math.max(1, y2 - y1);

        scrollRectToVisible(new Rectangle(x1, y1, w, h));
    }
    
    public void scrollSelectedToView(){
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                scrollPatternToView(getSelectionModel()
                        .getSelectedPattern());
            }
        });
    }
    
    public void scrollSelectedPlaceHolderToView(){
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                getPhSelectionModel().getSelectedPlaceHolder();
                scrollPatternToView(getSelectionModel()
                        .getSelectedPattern());
            }
        });
    }
    
    private static int AUTOSCROLL_INSETS = 20;
    
    public ValidationDecorationProvider getValidationDecorationProvider() {
        return validationDecorationProvider;
    }
    
    
    private void loadDecorationProviders(){
        selectionDecorationProvider = new SelectionDecorationProvider(this);
        validationDecorationProvider = new ValidationDecorationProvider(this);
        
        debuggerDecorationProvider = new DebuggerDecorationProvider(this);
        
        toolbarDecorationProvider = new ToolbarDecorationProvider(this);
        linkToolDecorationProvider = new LinkToolDecorationProvider(this);
        collapseExpandDectorationProvider
                = new CollapseExpandDecorationProvider(this);
        
        Lookup.Result result =
                Lookup.getDefault().lookup(new Lookup.Template(DecorationProviderFactory.class));
        
        for (Object inst: result.allInstances()){
            if (inst instanceof DecorationProviderFactory){
                ((DecorationProviderFactory) inst).createInstance(this);
            };
            
            if (inst instanceof DebuggerDecorationProvider){
                debuggerDecorationProvider =
                        (DebuggerDecorationProvider) inst;
            }
            
        }
    }
    private void initializeExternalListeners() {
        Lookup.Result lookupResult =
                Lookup.getDefault().lookup(new Lookup.Template(ExternalBpelEditorTopComponentListener.class));
        if (lookupResult != null) {
            Collection instances = lookupResult.allInstances();
            for (Iterator iter=instances.iterator(); iter.hasNext();) {
                ExternalBpelEditorTopComponentListener listener =
                        (ExternalBpelEditorTopComponentListener) iter.next();
                mExternalTopComponentListeners.add(new WeakReference(listener));
            }
        }
    }
    
    private void notifyExternalLoadersTopComponentCreated() {
        for (Iterator iter=mExternalTopComponentListeners.iterator(); iter.hasNext();) {
            WeakReference ref = (WeakReference) iter.next();
            ExternalBpelEditorTopComponentListener listener =
                    (ExternalBpelEditorTopComponentListener) ref.get();
            if (listener != null) {
                listener.created();
            }
        }
    }
    
    private void notifyExternalLoadersTopComponentClosed() {
        for (Iterator iter=mExternalTopComponentListeners.iterator(); iter.hasNext();) {
            WeakReference ref = (WeakReference) iter.next();
            ExternalBpelEditorTopComponentListener listener =
                    (ExternalBpelEditorTopComponentListener) ref.get();
            if (listener != null) {
                listener.closed();
            }
        }
    }
    
    public DnDHandler getDndHandler() {
        return dndHandler;
    }

    private DesignViewMode getDesignViewMode() {
        return designViewMode;
    }
    
    private void setDesignViewMode(DesignViewMode mode) {
        if (mode == null) {
            return;
        }
        designViewMode = mode;
    }

    public boolean isDesignMode() {
        return DesignViewMode.DESIGN.equals(designViewMode);
    }
            
    private enum DesignViewMode {
        CUT_PLACE_HOLDER,
        COPY_PLACE_HOLDER,
        DESIGN;
        
    }
}
