/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.design;

import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.modules.bpel.design.decoration.DecorationManager;
import org.netbeans.modules.bpel.design.decoration.components.DiagramButton;
import org.netbeans.modules.bpel.design.decoration.providers.CollapseExpandDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.DebuggerDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.LinkToolDecorationProvider;

import org.netbeans.modules.bpel.design.decoration.providers.ToolbarDecorationProvider;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.model.patterns.SequencePattern;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.editors.multiview.DesignerMultiViewElement;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;

import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.ConnectionManager;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.FlowlinkTool;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;

import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.xml.xam.Model;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import org.openide.nodes.Node;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProviderFactory;
import org.netbeans.modules.bpel.design.decoration.components.GlassPane;
import org.netbeans.modules.bpel.design.decoration.providers.SelectionDecorationProvider;
import org.netbeans.modules.bpel.design.decoration.providers.ValidationDecorationProvider;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.design.PartnerlinksView;
import org.netbeans.modules.bpel.design.ProcessView;
import org.netbeans.modules.bpel.design.actions.CancelAction;
import org.netbeans.modules.bpel.design.actions.CollapseCurrentPatternAction;
import org.netbeans.modules.bpel.design.actions.DeleteAction;
import org.netbeans.modules.bpel.design.actions.ExpandAllPatternsAction;
import org.netbeans.modules.bpel.design.actions.ExpandCurrentPatternAction;
import org.netbeans.modules.bpel.design.actions.FindUsagesAction;
import org.netbeans.modules.bpel.design.actions.GoToLoggingAction;
import org.netbeans.modules.bpel.design.actions.GoToMapperAction;
import org.netbeans.modules.bpel.design.actions.GoToSourceAction;
import org.netbeans.modules.bpel.design.actions.RenameAction;
import org.netbeans.modules.bpel.design.actions.ScrollToOperationAction;
import org.netbeans.modules.bpel.design.actions.ShowContextMenuAction;
import org.netbeans.modules.bpel.design.actions.TabToNextComponentAction;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.PartnerRole;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.nodes.actions.GoToAction;
import org.netbeans.modules.bpel.nodes.actions.ShowBpelMapperAction;
import org.netbeans.modules.bpel.properties.NodeUtils;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

public class DesignView extends JPanel implements
        HelpCtx.Provider/*, ThumbScrollPane.Thumbnailable */{

    public static double CORNER45 = Math.PI/4.0;
    private static final long serialVersionUID = 1;

    private double zoom = 1;
    private Lookup lookup;

    private DiagramModel diagramModel;
    private EntitySelectionModel selectionModel;
    private LayoutManager layoutManager;
    private ConnectionManager connectionManager;

    private DnDHandler dndHandler;
    private CopyPasteHandler copyPasteHandler;
    private FlowlinkTool flowLinkTool;

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

    private NavigationTools navigationTools;
    private RightStripe rightStripe;


    private PartnerlinksView consumersView;
    private PartnerlinksView providersView;
    private ProcessView processView;
    private OverlayPanel overlayView;

    private TriScrollPane scrollPane;
    private boolean printMode = false;
    
    // Memory leak probing
    private static final Logger TIMERS = Logger.getLogger("TIMER.bpel"); // NOI18N


    public DesignView(Lookup lookup) {
        super();
        
        if (TIMERS.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "BPEL DesignView"); // NOI18N
            rec.setParameters(new Object[] {this});
            TIMERS.log(rec);
        }


        zoomManager = new ZoomManager(this);
        rightStripe = new RightStripe(this);

        setBackground(new Color(0xFCFAF5));

        this.lookup = lookup;
        diagramModel = new DiagramModel(this);
        selectionModel = new EntitySelectionModel(diagramModel);


        overlayView = new OverlayPanel(this);

        consumersView = new PartnerlinksView(this, PartnerRole.CONSUMER);
        consumersView.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(DesignView.class, "ACSN_ConsumersPLPanel"));
        consumersView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(DesignView.class, "ACSD_ConsumersPLPanel"));
        
        providersView = new PartnerlinksView(this, PartnerRole.PROVIDER);
        providersView.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(DesignView.class, "ACSN_ProvidersPLPanel"));
        providersView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(DesignView.class, "ACSD_ProvidersPLPanel"));

        processView = new ProcessView(this);
        processView.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(DesignView.class, "ACSN_ProcessPanel"));
        processView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(DesignView.class, "ACSD_ProcessPanel"));
 
        navigationTools = new NavigationTools(this);
 
        scrollPane = new TriScrollPane(processView, consumersView, 
                providersView, navigationTools, overlayView);
        this.add(scrollPane, 0);

        dndHandler = new DnDHandler(this);

        flowLinkTool = new FlowlinkTool(this);
        copyPasteHandler = new CopyPasteHandler(this);


        layoutManager = new LayoutManager();
        connectionManager = new ConnectionManager();
        selectionBridge = new SelectionBridge(this);
        setFocusable(true);

        // register before to get esc action first (117432)
        ToolTipManager.sharedInstance().registerComponent(this);
        registerActions();
        errorPanel = new ErrorPanel(this);
        decorationManager = new DecorationManager(this);
        loadDecorationProviders();

        setFocusCycleRoot(true);
        setFocusTraversalKeysEnabled(false);

        scrollPane.addScrollListener(new TriScrollPane.ScrollListener() {

            public void viewScrolled(JComponent view) {
                overlayView.revalidate();
                overlayView.repaint();
            }
        });
        
        reloadModel();
        diagramChanged();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        EntitySelectionModel selModel = getSelectionModel();
        Pattern selPattern = null;
        if (selModel != null) {
            selPattern = selModel.getSelectedPattern();
        }
        
        DiagramView dView = null;
        if (selPattern != null) {
            dView = selPattern.getView();
        }
        return dView != null ? dView.getAccessibleContext() : super.getAccessibleContext();
    }

    public DiagramView getConsumersView() {
        return consumersView;
    }

    public Object getMouseHandler() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public DiagramView getProcessView() {
        return processView;
    }

    public DiagramView getProvidersView() {
        return providersView;
    }

    public OverlayPanel getOverlayView(){
        return this.overlayView;
    }

    public NavigationTools getNavigationTools() {
        return navigationTools;
    }
    
    public CopyPasteHandler getCopyPasteHandler(){
        return this.copyPasteHandler;
    }
    
    public RightStripe getRightStripe() {
        return rightStripe;
    }
    
    public boolean getPrintMode() {
        return printMode;
    }
    
    public void setPrintMode(boolean printMode) {
        this.printMode = printMode;
    }
    
    public TriScrollPane getScrollPane() {
        return scrollPane;
    }
    
    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        for(Component c: getComponents()){
            c.setBounds(0,0,w,h);

        }
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
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
        getDecorationManager().release();
        selectionBridge.release();
    }

    public Controller getValidationController() {
        return getLookup().lookup(Controller.class);
    }

    public EntitySelectionModel getSelectionModel() {
        return selectionModel;
    }

    public FlowlinkTool getFlowLinkTool() {
        return flowLinkTool;
    }

    public DecorationManager getDecorationManager() {
        return decorationManager;
    }

    public ZoomManager getZoomManager() {
        return zoomManager;
    }

    public BpelModel getBPELModel(){
        return getLookup().lookup(BpelModel.class);
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
        return lookup.lookup(BusinessProcessHelper.class);
    }

    public Node getNodeForPattern(Pattern pattern){
        if (pattern == null){
            return null;
        }

        NodeType nodeType = EditorUtil.getBasicNodeType(pattern.getOMReference());

        NodeFactory factory = getNodeFactory();

        if (factory != null){
            return factory.createNode(
                    nodeType,
                    pattern.getOMReference(),
                    DesignView.this.getLookup());
        }
        return null;
    }

    public DiagramView getView(Point pt){
        return (DiagramView) scrollPane.getComponent(pt);
    }

    public Pattern findPattern(Point pt){
        DiagramView view = getView(pt);
        if(view != null){
            FPoint fp = view.convertPointFromParent(pt);
            return view.findPattern(fp.x, fp.y);
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
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete-something"); // NOI18N
        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK 
                + KeyEvent.SHIFT_DOWN_MASK), "scroll-to-operation"); // NOI18N

        KeyStroke gotoSourceKey = GoToAction.getKeyStroke(org.netbeans.modules.bpel.nodes.actions.GoToSourceAction.class);
        KeyStroke gotoMapperKey = GoToAction.getKeyStroke(ShowBpelMapperAction.class);
        KeyStroke gotoLoggingKey = GoToAction.getKeyStroke(org.netbeans.modules.bpel.nodes.actions.GoToLoggingAction.class);

        if (gotoSourceKey != null) {
//            im1.put(org.netbeans.modules.bpel.nodes.actions.GoToSourceAction.GOTOSOURCE_KEYSTROKE, "gotosource-something"); // NOI18N
            im1.put(gotoSourceKey, "gotosource-something"); // NOI18N
            im2.put(gotoSourceKey, "gotosource-something"); // NOI18N
        }
        if (gotoMapperKey != null) {
//            im1.put(ShowBpelMapperAction.GOTOMAPPER_KEYSTROKE, "gotomapper-something"); // NOI18N
            im1.put(gotoMapperKey, "gotomapper-something"); // NOI18N
            im2.put(gotoMapperKey, "gotomapper-something"); // NOI18N
        }
        if (gotoLoggingKey != null) {
//            im1.put(org.netbeans.modules.bpel.nodes.actions.GoToLoggingAction.GOTOLOGGING_KEYSTROKE, "gotologging-something"); // NOI18N
            im1.put(gotoLoggingKey, "gotologging-something"); // NOI18N
            im2.put(gotoLoggingKey, "gotologging-something"); // NOI18N
        }
//        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), "gotosource-something"); // NOI18N
//        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), "gotomapper-something"); // NOI18N
//        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), "gotologging-something"); // NOI18N
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
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete-something"); // NOI18N
        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-something"); // NOI18N
//        im2.put(org.netbeans.modules.bpel.nodes.actions.GoToSourceAction.GOTOSOURCE_KEYSTROKE, "gotosource-something"); // NOI18N
//        im2.put(ShowBpelMapperAction.GOTOMAPPER_KEYSTROKE, "gotomapper-something"); // NOI18N
//        im2.put(org.netbeans.modules.bpel.nodes.actions.GoToLoggingAction.GOTOLOGGING_KEYSTROKE, "gotologging-something"); // NOI18N
//        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK), "gotosource-something"); // NOI18N
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

        am.put("rename-something", new RenameAction(this)); // NOI18N
        am.put("delete-something", new DeleteAction(this)); // NOI18N
        am.put("cancel-something", new CancelAction(this)); // NOI18N
        am.put("gotosource-something", new GoToSourceAction(this)); // NOI18N
        am.put("gotomapper-something", new GoToMapperAction(this)); // NOI18N
        am.put("gotologging-something", new GoToLoggingAction(this)); // NOI18N
        am.put("findusages-something", new FindUsagesAction(this)); // NOI18N
//        am.put("find_next_mex_peer", new CycleMexAction()); // NOI18N
        am.put("show_context_menu", new ShowContextMenuAction(this)); // NOI18N
        am.put("go_next_hierarchy_component", new TabToNextComponentAction(this, true)); // NOI18N
        am.put("go_previous_hierarchy_component", new TabToNextComponentAction(this, false)); // NOI18N
        am.put("scroll-to-operation", new ScrollToOperationAction(this)); // NOI18N
//
//        am.put("go_nearest_right_component", new GoRightNearestComponentAction()); // NOI18N
//        am.put("go_nearest_left_component", new GoLeftNearestComponentAction()); // NOI18N
//        am.put("go_nearest_up_component", new GoUpNearestComponentAction()); // NOI18N
//        am.put("go_nearest_down_component", new GoDownNearestComponentAction()); // NOI18N
//
        am.put("expand-current-pattern", new ExpandCurrentPatternAction(this)); // NOI18N
        am.put("collapse-current-pattern", new CollapseCurrentPatternAction(this)); // NOI18N
        am.put("expand-all-patterns", new ExpandAllPatternsAction(this));
//
        am.put("copy-pattern", getCopyPasteHandler().getCopyAction()); // NOI18N
        am.put("cut-pattern", getCopyPasteHandler().getCutAction()); // NOI18N
        am.put("paste-pattern", getCopyPasteHandler().getPasteAction()); // NOI18N

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

            layoutManager.layout(getModel().getRootPattern());

            connectionManager.reconnectAll(getModel().getRootPattern());

            connectionManager.layoutConnections(getModel().getRootPattern());

            FDimension dim = getModel().getRootPattern().getBounds().getSize();

            int printWidth = (int) Math.round(dim.width
                    + 2 * LayoutManager.HMARGIN);

            int printHeight = (int) Math.round(dim.height
                    + 2 * LayoutManager.VMARGIN);

            processView.revalidate();
            consumersView.revalidate();
            providersView.revalidate();

            repaint();

            installErrorPanel(false);

            rightStripe.repaint();
        } else {
            setToolBarEnabled(false);
            installErrorPanel(true);
        }
    }

    private void installErrorPanel(boolean install){
        if (install){
            errorPanel.updateErrorMessage();
            this.remove(scrollPane);
            this.add(errorPanel);
            
        } else {
            this.remove(errorPanel);
            this.add(scrollPane);
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
        for (Component c = getParent(); c != null;
                c = c.getParent())
        {
            if (c instanceof DesignerMultiViewElement) {
                return ((DesignerMultiViewElement) c)
                        .getToolbarRepresentation();
            }
        }
        return null;
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

    /*
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
    */

//    FIXME
//    protected void paintChildren(Graphics g) {
//        if (!ghost.isEmpty()) return;
//        super.paintChildren(g);
//    }
//
//    protected void printComponent(Graphics g) {
//        Dimension s = (Dimension) getClientProperty(Dimension.class);
//        if (s == null) return;
//
//        Color oldColor = g.getColor();
//        g.setColor(getBackground());
//        Rectangle clip = g.getClipBounds();
//        if (clip != null) {
//            g.fillRect(clip.x, clip.y, clip.width, clip.height);
//        } else {
//            g.fillRect(0, 0, s.width, s.height);
//        }
//
//        g.setColor(oldColor);
//        paintContent(g, 1, true);
//    }

    protected void printChildren(Graphics g) {}





//FIXME
    public FPoint convertScreenToDiagram(Point point) {
        return new FPoint(0, 0);
        //return convertScreenToDiagram(point, designView.getCorrectedZoom());
    }
//FIXME
    public Point convertDiagramToScreen(FPoint point) {
        return new Point(0, 0);
        //return convertDiagramToScreen(point, designView.getCorrectedZoom());
    }
   //FIXME
    public static Point convertDiagramToScreen(FPoint topRight, double zoom) {
        return new Point(0, 0);
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

    public void scrollPatternToView(Pattern pattern) {
        consumersView.scrollPatternToView(pattern);
        processView.scrollPatternToView(pattern);
        providersView.scrollPatternToView(pattern);
    }
    
    
    public void scrollToOperation(MessageConnection messageConnection) {
        VisualElement sourceElement = messageConnection.getSource();
        VisualElement targetElement = messageConnection.getTarget();

        Pattern sourcePattern = sourceElement.getPattern();

        DiagramView centerView = getProcessView();

        VisualElement centerElement;
        VisualElement sideElement;

        if (sourcePattern.getView() == centerView) {
            centerElement = sourceElement;
            sideElement = targetElement;
        } else {
            centerElement = targetElement;
            sideElement = sourceElement;
        }

        DiagramView sideView = sideElement.getPattern().getView();

        FBounds centerBounds = centerElement.getBounds();
        Point centerPoint1 = centerView.convertDiagramToScreen(
                centerBounds.getTopLeft());
        Point centerPoint2 = centerView.convertDiagramToScreen(
                centerBounds.getBottomRight());

        FBounds sideBounds = sideElement.getBounds();
        Point sidePoint1 = centerView.convertDiagramToScreen(
                sideBounds.getTopLeft());
        Point sidePoint2 = centerView.convertDiagramToScreen(
                sideBounds.getBottomRight());

        Rectangle centerVisibleRect = centerView.getVisibleRect();
        Rectangle sideVisibleRect = sideView.getVisibleRect();

        int centerOffset = (centerPoint2.y + centerPoint1.y) / 2 
                - centerVisibleRect.y;

        int sideOffset = (sidePoint1.y + sidePoint2.y) / 2 
                - sideVisibleRect.y;

        sideVisibleRect.y += sideOffset - centerOffset;

        sideView.scrollRectToVisible(sideVisibleRect);
    }

    /**
     * ensures that components are added to container in correct order.
     * GlassPanes should go before Buttons
     **/
    protected void addImpl(Component comp, Object constraints, int index) {

        int count = getComponentCount();

        if (comp instanceof GlassPane) {
            index = 0;
            for (int i = 0; i < count; i++) {
                Component c = getComponent(i);
                if ((c instanceof GlassPane) || (c instanceof DiagramButton)) {
                    index = i;
                    break;

                }
            }

        } else if (comp instanceof DiagramButton) {
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
//    public void scrollPlaceHolderToView(PlaceHolder ph) {
//        if (ph == null){
//            return;
//        }
//        /**
//         * Get the position of selected node and scroll view to make
//         * the corresponding pattern visible
//         **/
//
//        FShape shape = ph.getShape();
//
//        Point screenTL = convertDiagramToScreen(shape.getTopLeft());
//        Point screenBR = convertDiagramToScreen(shape.getBottomRight());
//
//        int x1 = Math.max(0, screenTL.x - 8);
//        int y1 = Math.max(0, screenTL.y - 32);
//
//        int x2 = Math.min(getWidth(), screenBR.x + 8);
//        int y2 = Math.min(getHeight(), screenBR.y + 8);
//
//        int w = Math.max(1, x2 - x1);
//        int h = Math.max(1, y2 - y1);
//
//        scrollRectToVisible(new Rectangle(x1, y1, w, h));
//    }

    public void scrollSelectedToView(){
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                scrollPatternToView(getSelectionModel()
                        .getSelectedPattern());
            }
        });
    }

//    public void scrollSelectedPlaceHolderToView(){
//        SwingUtilities.invokeLater( new Runnable() {
//            public void run() {
//                getPhSelectionModel().getSelectedPlaceHolder();
//                scrollPatternToView(getSelectionModel()
//                        .getSelectedPattern());
//            }
//        });
//    }



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

   
    public DnDHandler getDndHandler() {
        return dndHandler;
    }
}
