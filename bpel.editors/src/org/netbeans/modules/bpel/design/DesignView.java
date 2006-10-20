/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.netbeans.modules.bpel.design.decoration.DecorationManager;
import org.netbeans.modules.bpel.design.decoration.components.DiagramButton;
import org.netbeans.modules.bpel.design.decoration.providers.DebuggerDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.LinkToolDecorationProvider;

import org.netbeans.modules.bpel.design.decoration.providers.ToolbarDecorationProvider;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.model.patterns.SequencePattern;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;

import org.netbeans.modules.bpel.design.geom.FDimension;
import org.netbeans.modules.bpel.design.geom.FPoint;
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
//import org.netbeans.modules.soa.orch.design.selection.SelectionHandler;

import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.HelpCtx;

import org.openide.util.Lookup;

import org.openide.nodes.Node;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.bpel.core.validation.BPELValidationController;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.components.GlassPane;
import org.netbeans.modules.bpel.design.decoration.providers.SelectionDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.ValidationDecorationProvider;
import org.netbeans.modules.bpel.design.nodes.NodeFactory;
import org.netbeans.modules.bpel.nodes.ExternalBpelEditorTopComponentListener;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;

public class DesignView extends JPanel implements
        Autoscroll,
        HelpCtx.Provider {
    
    ////////////////////////////////////////////////////////////////////////////
    ///////////////////////// Private Constants ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    
    private static final long serialVersionUID = 1;
    

    

    
    private float zoom = 1;
    
    ////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// instance  //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
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
    private FlowlinkTool flowLinkTool;
    private NameEditor nameEditor;
    private ErrorPanel errorPanel;
    private ZoomPanel zoomPanel;
    
    private DecorationManager decorationManager;
    
    private SelectionDecorationProvider     selectionDecorationProvider;
    private ValidationDecorationProvider    validationDecorationProvider;
    private DebuggerDecorationProvider      debuggerDecorationProvider;

    private ToolbarDecorationProvider       toolbarDecorationProvider;
    private LinkToolDecorationProvider    linkToolDecorationProvider;
    
    private SelectionBridge selectionBridge;
    private List mExternalTopComponentListeners = new ArrayList(4);
    
    
    public DesignView(Lookup lookup) {
        super(new DesignViewLayout());
        
                  
       
        
      
        
        
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
        
        
        nameEditor = new NameEditor(this);
        
        
        
        selectionBridge = new SelectionBridge(this);
        
        setFocusable(true);
        
        registerActions();
        
        errorPanel = new ErrorPanel(this);
        zoomPanel = new ZoomPanel(this);
        
        
        
        
        
        decorationManager = new DecorationManager(this);
        
        selectionDecorationProvider = new SelectionDecorationProvider(this);
        validationDecorationProvider = new ValidationDecorationProvider(this);
        debuggerDecorationProvider = new DebuggerDecorationProvider(this);
        toolbarDecorationProvider = new ToolbarDecorationProvider(this);

        linkToolDecorationProvider = new LinkToolDecorationProvider(this);
        
        reloadModel();
        diagramChanged();
        
        initializeExternalListeners();
        notifyExternalLoadersTopComponentCreated();
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
    
    
    public void closeView() {
        if (diagramModel != null){
            diagramModel.release();
            diagramModel = null;
        }
        
        
        // Removed validation listener.
        getValidationController().removeValidationListener(validationDecorationProvider);
        
        if (debuggerDecorationProvider != null){
            debuggerDecorationProvider.unsubscribe();
        }
        
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
    
    
    public SelectionDecorationProvider getButtonsManager() {
        return selectionDecorationProvider;
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
    
    
    public DecorationManager getDecorationManager() {
        return decorationManager;
    }
    
    
    public ZoomPanel getZoomPanel() {
        return zoomPanel;
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
        NodeFactory factory = getNodeFactory();
        
        if (factory != null){
            return factory.createNode(
                    pattern.getNodeType(),
                    pattern.getOMReference(),
                    pattern.getExtInfo(),
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
    
    
    
    public boolean showCustomEditor(Pattern pattern) {
        Node node = getNodeForPattern(pattern);
        
        if (node == null){
            return false;
        }

        if (getModel().isReadOnly()) {
            return false;
        }
        
        return NodeUtils.showNodeCustomEditor(node);
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
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "find_next_mex_peer"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "show_context_menu"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), "show_context_menu"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "rename-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), "gotosource-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK), "find_next_mex_peer"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "show_context_menu"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK), "show_context_menu"); // NOI18N
        
        am.put("rename-something", new RenameAction()); // NOI18N
        am.put("delete-something", new DeleteAction()); // NOI18N
        am.put("cancel-something", new CancelAction()); // NOI18N
        am.put("gotosource-something", new GoToSourceAction()); // NOI18N
        am.put("find_next_mex_peer", new CycleMexAction()); // NOI18N
        am.put("show_context_menu", new ShowContextMenu()); // NOI18N
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
            
            
            float zoom = getCorrectedZoom();
            
            zoomPanel.setEnabled(true);
            
            layoutManager.layout();
            
            connectionManager.reconnectAll();
            
            connectionManager.layoutConnections();
            
            FDimension dim = getModel().getRootPattern().getBounds().getSize();
            
            int printWidth = (int) Math.round(dim.width
                    + 2 * LayoutManager.HMARGIN);
            
            int printHeight = (int) Math.round(dim.height
                    + 2 * LayoutManager.VMARGIN);
            
            putClientProperty(Dimension.class.getName(),
                    new Dimension(printWidth, printHeight));
            
            getDecorationManager().repositionComponentsRecursive();
            
            revalidate();
            repaint();
            errorPanel.uninstall();
        } else {
            
            zoomPanel.setEnabled(false);
            errorPanel.install();
            
        }
    }
    
    
    public JComponent getView() {
        return (errorPanel.isInstalled()) ? errorPanel : this;
    }
    
    /*
    public void addGlassPane(GlassPane glassPane) {
        int count = getComponentCount();
     
        for (int i = 0; i < count; i++) {
            java.awt.Component c = getComponent(i);
            if ((c instanceof GlassPane) || (c instanceof DiagramButton)) {
                add(glassPane, i);
                return;
            }
        }
     
        add(glassPane, 0);
    }
     
     
    public void addButton(DiagramButton button) {
        int count = getComponentCount();
     
        for (int i = 0; i < count; i++) {
            java.awt.Component c = getComponent(i);
     
            if (c instanceof DiagramButton) {
                add(button, i);
                return;
            }
        }
     
        add(button);
    }*/
    
    
    public void setZoom(float z) {
        this.zoom = z;
        
        getDecorationManager().repositionComponentsRecursive();
        nameEditor.updateBounds();
        
        revalidate();
        repaint();
    }
    
    

    public TopComponent getTopComponent() {
        return (TopComponent) SwingUtilities
                .getAncestorOfClass(TopComponent.class, this);
    }
    
    
    public Font getZoomedDiagramFont() {
        Font font = DiagramFontUtil.getFont();
        return font.deriveFont(font.getSize2D()
        * getCorrectedZoom());
    }
    
    
    public float getZoom() {
        return zoom;
    }
    
    
    
    
    public float getCorrectedZoom() {
        return zoom * DiagramFontUtil.getZoomCorrection();
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
    
    
    public Pattern findPattern(float x, float y) {
        VisualElement element = findElement(x, y);
        return (element != null) ? element.getPattern() : null;
    }
    
    
    public VisualElement findElement(float x, float y) {
        Pattern root = diagramModel.getRootPattern();
        if (root != null) {
            VisualElement result = findElementInPattern(root, x, y);
            if (result == null) {
                return ((CompositePattern) root).getBorder();
            }
            return result;
        }
        return null;
    }
    
    
    public VisualElement findElementInPattern(Pattern pattern, float x, float y) {
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
            if (border != null && border.getShape().position(x, y) >= 0) {
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
    
    
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintContent(g, getCorrectedZoom(), false);
    }
    
    
//    protected void paintChildren(Graphics g) {
//        Rectangle clip = g.getClipBounds();
//        if (clip == null) {
//            clip = new Rectangle(0, 0, getWidth(), getHeight());
//        }
//
//        synchronized(getTreeLock()) {
//            int childrenCount = getComponentCount();
//            for (int i = 0; i < childrenCount; i++) {
//                Component child = getComponent(i);
//
//                if (child.isVisible() == false) {
//                    continue;
//                }
//
//                Rectangle bounds = child.getBounds();
//
//                int cx = bounds.x;
//                int cy = bounds.y;
//                int cw = bounds.width;
//                int ch = bounds.height;
//
//                if (!g.hitClip(cx, cy, cw, ch)) {
//                    continue;
//                }
//
//                Graphics cg = g.create(cx, cy, cw, ch);
//                cg.setFont(child.getFont());
//                cg.setColor(child.getForeground());
//
//                try {
//                    child.paint(cg);
//                } finally {
//                    cg.dispose();
//                }
//            }
//        }
//    }
    
    
    private void paintContent(Graphics g, float zoom, boolean printMode) {
        if (getModel() == null) {
          return;
        }
        Pattern root = getModel().getRootPattern();
        
        Graphics2D g2 = GUtils.createGraphics(g);
        Graphics2D g2bw = new BWGraphics2D(g2);
        
        if (!printMode) {
            Point p = convertDiagramToScreen(new FPoint(0, 0));
            g2.translate(p.x, p.y);
//
//            g2.translate(DesignViewLayout.MARGIN_LEFT,
//                    DesignViewLayout.MARGIN_TOP);
        }
        
        g2.scale(zoom, zoom);
//
//        if (!printMode) {
//            g2.translate(-LayoutManager.HMARGIN, -LayoutManager.VMARGIN);
//        }
        
        if (root != null) {
            Rectangle clipBounds = g2.getClipBounds();
            
            float exWidth = layoutManager.HSPACING * zoom;
            float exHeight = layoutManager.VSPACING * zoom;
            
            FRectangle exClipBounds = new FRectangle(
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
        Dimension s = (Dimension) getClientProperty(Dimension.class.getName());
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
            FRectangle clipBounds, boolean printMode) {
        
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
    
    
    
    
    class DeleteAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            if (getModel().isReadOnly()) return;
            
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
        }
    }
    
    
    class RenameAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            if (getModel().isReadOnly()) return;
            getNameEditor().startEdit(getSelectionModel().getSelectedPattern());
        }
    }
    
    
    public Decoration getDecoration(Pattern p) {
        return decorationManager.getDecoration(p);
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
    
    class CycleMexAction extends AbstractAction {
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
    
    
    class GoToSourceAction extends AbstractAction {
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

    
    class ShowContextMenu extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformed(ActionEvent e) {
            Pattern p = getSelectionModel().getSelectedPattern();
            
            if (p == null);
            
            JPopupMenu menu = p.createPopupMenu();
            
            FRectangle r;
                    
            if (p instanceof ProcessPattern) {
                r = ((ProcessPattern) p).getBorder().getBounds();
            } else {
                r = p.getBounds();
            }
            
            Point topLeft = convertDiagramToScreen(new FPoint(r.x, r.y));
            Point bottomRight = convertDiagramToScreen(new FPoint(r.x + r.width, 
                    r.y + r.height));
            
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
    
    
    
    public static FPoint convertScreenToDiagram(Point point, float zoom) {
        float x = ((point.x - DesignViewLayout.MARGIN_LEFT) / zoom)
        + LayoutManager.HMARGIN;
        
        float y = ((point.y - DesignViewLayout.MARGIN_TOP) / zoom)
        + LayoutManager.VMARGIN;
        
        return new FPoint(x, y);
    }
    
    
    public static Point convertDiagramToScreen(FPoint point, float zoom) {
        float x = (point.x - LayoutManager.HMARGIN) * zoom
                + DesignViewLayout.MARGIN_LEFT;
        float y = (point.y - LayoutManager.VMARGIN) * zoom
                + DesignViewLayout.MARGIN_TOP;
        
        return new Point(Math.round(x), Math.round(y));
    }
    
    
    public  void scrollSelectedToView(){
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                
                Pattern pattern = getSelectionModel().getSelectedPattern();
                
                if (pattern == null){
                    return;
                }
                /**
                 * Get the position of selected node and scroll view to make
                 * the corresponding pattern visible
                 **/ 
                
                FRectangle bounds = pattern.getBounds();
                
                
                
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
        });
    }
    
    
    private static int AUTOSCROLL_INSETS = 20;
    
    public ValidationDecorationProvider getValidationDecorationProvider() {
        return validationDecorationProvider;
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
}
