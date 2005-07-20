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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Area;
import javax.swing.*;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.undo.UndoableEdit;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.NodeTransfer;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.Utilities;

import org.netbeans.modules.form.palette.CPManager;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutdesign.*;

/**
 * A transparent layer (glass pane) handling user operations in designer (mouse
 * and keyboard events) and painting selection and drag&drop feedback.
 * Technically, this is a layer in FormDesigner, placed over ComponentLayer.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

class HandleLayer extends JPanel implements MouseListener, MouseMotionListener
{
    // constants for mode parameter of getMetaComponentAt(Point,int) method
    static final int COMP_DEEPEST = 0; // get the deepest component (at given point)
    static final int COMP_SELECTED = 1; // get the deepest selected component
    static final int COMP_ABOVE_SELECTED = 2; // get the component above the deepest selected component
    static final int COMP_UNDER_SELECTED = 3; // get the component under the deepest selected component
    
    private static final int DESIGNER_RESIZING = 256; // flag for resizeType
    private static MessageFormat resizingHintFormat;

    private FormDesigner formDesigner;
    private boolean viewOnly;

    private ComponentDrag draggedComponent;
    private JPanel dragPanel;

    private Point lastMousePosition;
    private int lastXPosDiff;
    private int lastYPosDiff;
            
    private Point lastLeftMousePoint;
    private Point prevLeftMousePoint;
    private boolean draggingEnded; // prevents dragging from starting inconveniently
    private int resizeType;

    private SelectionDragger selectionDragger;
    private Image resizeHandle;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormLoaderSettings.getInstance();

    // -------

    HandleLayer(FormDesigner fd) {
        formDesigner = fd;
        addMouseListener(this);
        addMouseMotionListener(this);
        setLayout(null);
        
        // Hack - the panel is used to ensure correct painting of dragged components
        dragPanel = new JPanel();
        dragPanel.setLayout(null);
        dragPanel.setBounds(-1,-1,0,0);
        add(dragPanel);

        // set Ctrl+TAB and Ctrl+Shift+TAB as focus traversal keys - to have
        // TAB and Shift+TAB free for component selection
        Set keys = new HashSet();
        keys.add(AWTKeyStroke.getAWTKeyStroke(9,
                                              InputEvent.CTRL_DOWN_MASK,
                                              true));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                              keys);
        keys.clear();
        keys.add(AWTKeyStroke.getAWTKeyStroke(9,
                                              InputEvent.CTRL_DOWN_MASK
                                                 |InputEvent.SHIFT_DOWN_MASK,
                                              true));
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                              keys);

        getAccessibleContext().setAccessibleName(
            FormUtils.getBundleString("ACSN_HandleLayer")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_HandleLayer")); // NOI18N
        new DropTarget(this, new NewComponentDropListener());
    }

    void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }

    private FormModel getFormModel() {
        return formDesigner.getFormModel();
    }

    private MetaComponentCreator getComponentCreator() {
        return formDesigner.getFormModel().getComponentCreator();
    }

    private LayoutModel getLayoutModel() {
        return formDesigner.getFormModel().getLayoutModel();
    }

    // ---------

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // paint component in the connection mode (if any)
        if (formDesigner.getDesignerMode() == FormDesigner.MODE_CONNECT) {
            RADComponent conSource = formDesigner.getConnectionSource();
            RADComponent conTarget = formDesigner.getConnectionTarget();
            if (conSource != null || conTarget != null) {
                g2.setColor(formSettings.getConnectionBorderColor());
                g2.setStroke(getPaintStroke());
                if (conSource != null)
                    paintSelection(g2, conSource, false);
                if (conTarget != null)
                    paintSelection(g2, conTarget, false);
            }
            return; // that's all in connection mode
        }

        if (draggedComponent != null) {
            draggedComponent.paintFeedback(g2);
        }
        else { // just paint the selection of selected components
            g2.setColor(formSettings.getSelectionBorderColor());
            g2.setStroke(getPaintStroke());
            Iterator metacomps = formDesigner.getSelectedLayoutComponents().iterator();
            boolean first = true;
            while (metacomps.hasNext()) {
                RADComponent metacomp = (RADComponent)metacomps.next();
                paintSelection(g2, metacomp, first || !isInNewLayout(metacomp));
                first = false;
            }

            if (selectionDragger != null)
                selectionDragger.paintDragFeedback(g2);
        }
    }

    private void paintSelection(Graphics2D g, RADComponent metacomp, boolean resizeHandles) {
        Object comp = formDesigner.getComponent(metacomp);
        if (!(comp instanceof Component))
            return;

        Component component = (Component) comp;
        Component parent = component.getParent();

        if (parent != null && component.isShowing()) {
            Rectangle selRect = component.getBounds();
            RADComponent metacont = metacomp.getParentComponent();
            convertRectangleFromComponent(selRect, parent);

            Container topCont = formDesigner.getTopVisualContainer();
            Point convertPoint = convertPointFromComponent(0, 0, topCont);
            g.translate(convertPoint.x, convertPoint.y);
            LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
            Color oldColor = g.getColor();
            g.setColor(formSettings.getGuidingLineColor());
            Shape clip = g.getClip();
            Area area = new Area(new Rectangle(0, 0, topCont.getWidth(), topCont.getHeight()));
            if (clip != null) {
                area.intersect(new Area(clip));
            }
            g.setClip(area);
            layoutDesigner.paintSelection(g, metacomp.getId());
            g.setClip(clip);
            g.setColor(oldColor);
            g.translate(-convertPoint.x, -convertPoint.y);

            int correction = formSettings.getSelectionBorderSize() % 2;
            int x = selRect.x - correction;
            int y = selRect.y - correction;
            int width = selRect.width + correction;
            int height = selRect.height + correction;
            g.drawRect(x, y, width, height);
            if (resizeHandles && (metacomp instanceof RADVisualComponent)) {
                int resizable = getComponentResizable((RADVisualComponent)metacomp);
                Image resizeHandle = resizeHandle();
                int iconHeight = resizeHandle.getHeight(null);
                int iconWidth = resizeHandle.getWidth(null);
                if ((resizable & LayoutSupportManager.RESIZE_LEFT) != 0) {
                    g.drawImage(resizeHandle, x-iconWidth+1, y+(height-iconHeight)/2, null);
                    if ((resizable & LayoutSupportManager.RESIZE_UP) != 0) {
                        g.drawImage(resizeHandle, x-iconWidth+1, y-iconHeight+1, null);
                    }
                    if ((resizable & LayoutSupportManager.RESIZE_DOWN) != 0) {
                        g.drawImage(resizeHandle, x-iconWidth+1, y+height, null);
                    }
                }
                if ((resizable & LayoutSupportManager.RESIZE_RIGHT) != 0) {
                    g.drawImage(resizeHandle, x+width, y+(height-iconHeight)/2, null);
                    if ((resizable & LayoutSupportManager.RESIZE_UP) != 0) {
                        g.drawImage(resizeHandle, x+width, y-iconHeight+1, null);
                    }
                    if ((resizable & LayoutSupportManager.RESIZE_DOWN) != 0) {
                        g.drawImage(resizeHandle, x+width, y+height, null);
                    }
                }
                if ((resizable & LayoutSupportManager.RESIZE_UP) != 0) {
                    g.drawImage(resizeHandle, x+(width-iconWidth)/2, y-iconHeight+1, null);
                }
                if ((resizable & LayoutSupportManager.RESIZE_DOWN) != 0) {
                    g.drawImage(resizeHandle, x+(width-iconWidth)/2, y+height, null);
                }
            }
        }
    }
    
    private boolean isInNewLayout(RADComponent metacomp) {
        RADComponent metacont = metacomp.getParentComponent();
        if (metacont instanceof RADVisualContainer) {
            RADVisualContainer container = (RADVisualContainer)metacont;
            LayoutSupportManager laySup = container.getLayoutSupport();
            if (laySup == null) { // new layout support
                return true;
            }
        }
        return false;
    }
    
    private Image resizeHandle() {
        if (resizeHandle == null) {
            resizeHandle = new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/form/resources/resize_handle.png")).getImage(); // NOI18N
        }
        return resizeHandle;
    }

    // paint stroke cached
    private static int lastPaintWidth = -1;
    private Stroke paintStroke;

    private Stroke getPaintStroke() {
        int width = formSettings.getSelectionBorderSize();
        if (lastPaintWidth != width) {
            paintStroke = null;
        }
        if (paintStroke == null) {
            paintStroke = new BasicStroke(width);
            lastPaintWidth = width;
        }
        return paintStroke;
    }

    public boolean isOpaque() {
        return false;
    }

    protected void processKeyEvent(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_TAB || e.getKeyChar() == '\t') {
            if (!e.isControlDown()) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    RADComponent nextComp = formDesigner.getNextVisualComponent(
                                                              !e.isShiftDown());
                    if (nextComp != null)
                        formDesigner.setSelectedComponent(nextComp);
                }
                e.consume();
                return;
            }
        }
        else if (keyCode == KeyEvent.VK_SPACE) {
            if (!viewOnly && e.getID() == KeyEvent.KEY_RELEASED) {
                java.util.List selected = formDesigner.getSelectedComponents();
                if (selected.size() == 1) { // just one component is selected
                    RADComponent comp = (RADComponent) selected.get(0);
                    if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT) {
                        // in selection mode SPACE starts in-place editing
                        formDesigner.startInPlaceEditing(comp);
                    }
//                    else if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
//                        // in add mode SPACE adds selected item as component
//                        PaletteItem item = CPManager.getDefault().getSelectedItem();
//                        if (item != null) {
//                            formDesigner.getModel().getComponentCreator()
//                                .createComponent(item.getComponentClassSource(),
//                                                 comp,
//                                                 null);
//                            formDesigner.toggleSelectionMode();
//                        }
//                    }
                }
            }
            e.consume();
            return;
        }
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            if (formDesigner.getDesignerMode() != FormDesigner.MODE_SELECT) {
                formDesigner.toggleSelectionMode(); // also calls endDragging(null)
                repaint();
                e.consume();
                return;
            }
            if (endDragging(null)) {
                repaint();
                e.consume();
                return;
            }
        }
        else if (keyCode == KeyEvent.VK_F10) {
            if (e.isShiftDown()) { // Shift F10 invokes context menu
                Point p = null;
                java.util.List selected = formDesigner.getSelectedComponents();
                if (selected.size() > 0) {
                    RADComponent metacomp = (RADComponent) selected.get(0);
                    Object sel = (Component) formDesigner.getComponent(metacomp);
                    if (sel instanceof Component) {
                        Component comp = (Component) sel;
                        p = convertPointFromComponent(comp.getLocation(), comp.getParent());
                    }
                    else p = new Point(6, 6);

                    showContextMenu(p);
                    e.consume();
                    return;
                }
            }
        } else if (keyCode == KeyEvent.VK_D && e.isAltDown() && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            FormModel formModel = formDesigner.getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            if (layoutModel != null) {
                Iterator iter = formModel.getMetaComponents().iterator();
                Map idToNameMap = new HashMap();
                while (iter.hasNext()) {
                    RADComponent comp = (RADComponent)iter.next();
                    org.netbeans.modules.form.codestructure.CodeVariable var =
                        comp.getCodeExpression().getVariable();
                    if (var != null) {
                        idToNameMap.put(comp.getId(), var.getName());
                    }
                }
                System.out.println(layoutModel.dump(idToNameMap));
            }
        } else if (e.isAltDown() && (e.getID() == KeyEvent.KEY_PRESSED)
            && ((keyCode == KeyEvent.VK_LEFT) || (keyCode == KeyEvent.VK_RIGHT)
            || (keyCode == KeyEvent.VK_UP) || (keyCode == KeyEvent.VK_DOWN))) {
            LayoutDesigner designer = formDesigner.getLayoutDesigner();
            int alignment = 0;
            int dimension = 0;
            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                    alignment = LayoutConstants.LEADING;
                    dimension = LayoutConstants.HORIZONTAL; break;
                case KeyEvent.VK_RIGHT:
                    alignment = LayoutConstants.TRAILING;
                    dimension = LayoutConstants.HORIZONTAL; break;
                case KeyEvent.VK_UP:
                    alignment = LayoutConstants.LEADING;
                    dimension = LayoutConstants.VERTICAL; break;
                case KeyEvent.VK_DOWN:
                    alignment = LayoutConstants.TRAILING;
                    dimension = LayoutConstants.VERTICAL; break;
                default: assert false;
            }
            formDesigner.align(e.isControlDown(), dimension, alignment);
        }

        super.processKeyEvent(e);
    }

    public boolean isFocusTraversable() {
        return true;
    }

    // -------

    /**
     * Returns metacomponent at given position.
     * @param point - position on component layer
     * @param mode - what to get:
     *   COMP_DEEPEST - get the deepest component
     *   COMP_SELECTED - get the deepest selected component
     *   COMP_ABOVE_SELECTED - get the component above the deepest selected component
     *   COMP_UNDER_SELECTED - get the component under the deepest selected component
     * @returns the metacomponent at given point
     *   If no component is currently selected then:
     *     for COMP_SELECTED the deepest component is returned
     *     for COMP_ABOVE_SELECTED the deepest component is returned
     *     for COMP_UNDER_SELETCED the top component is returned
     */
    private RADComponent getMetaComponentAt(Point point, int mode) {
        Component[] deepComps = getDeepestComponentsAt(
                                    formDesigner.getComponentLayer(), point);
        if (deepComps.length == 0)
            return null;

        int dIndex = mode == COMP_DEEPEST ? deepComps.length - 1 : 0;
        Component comp = deepComps[dIndex];

        // find the component satisfying point and mode
        RADComponent topMetaComp = formDesigner.getTopDesignComponent();
        RADComponent firstMetaComp = null;
        RADComponent currMetaComp;
        RADComponent prevMetaComp = null;

        do {
            currMetaComp = formDesigner.getMetaComponent(comp);
            if (currMetaComp != null) {
                if (firstMetaComp == null)
                    firstMetaComp = currMetaComp;

                switch (mode) {
                    case COMP_DEEPEST: 
                        return currMetaComp;

                    case COMP_SELECTED:
                        if (formDesigner.isComponentSelected(currMetaComp))
                            return currMetaComp;
                        if (currMetaComp == topMetaComp)
                            return firstMetaComp; // nothing selected - return the deepest
                        break;

                    case COMP_ABOVE_SELECTED:
                        if (prevMetaComp != null 
                                && formDesigner.isComponentSelected(prevMetaComp))
                            return currMetaComp;
                        if (currMetaComp == topMetaComp)
                            return firstMetaComp; // nothing selected - return the deepest
                        break;

                    case COMP_UNDER_SELECTED:
                        if (formDesigner.isComponentSelected(currMetaComp))
                            return prevMetaComp != null ?
                                     prevMetaComp : topMetaComp;
                        if (currMetaComp == topMetaComp)
                            return topMetaComp; // nothing selected - return the top
                        break;
                }

                prevMetaComp = currMetaComp;
            }

            comp = dIndex + 1 < deepComps.length ?
                   deepComps[++dIndex] : comp.getParent();
        }
        while (comp != null);

        return firstMetaComp;
    }

    // [TODO would be nice to rewrite this method not to produce garbage, it is
    // called for each mouse move several times]
    private static Component[] getDeepestComponentsAt(Container parent,
                                                      Point point)
    {
        Component comp = SwingUtilities.getDeepestComponentAt(parent,
                                                              point.x,
                                                              point.y);
        if (comp == null)
            return new Component[0];

        Container deepestParent = comp.getParent();
        Component[] deepestComponents = deepestParent.getComponents();
        Point deepestPosition =
            SwingUtilities.convertPoint(parent, point, deepestParent);

        // in most cases there will be just one component...
        Component[] componentsAtPoint = new Component[1];
        ArrayList compList = null;

        for (int i=0; i < deepestComponents.length; i++) {
            comp = deepestComponents[i];
            Point p = comp.getLocation();
            if (comp.isVisible()
                && comp.contains(deepestPosition.x - p.x,
                                 deepestPosition.y - p.y))
            {
                if (componentsAtPoint[0] == null)
                    componentsAtPoint[0] = comp;
                else {
                    if (compList == null) {
                        compList = new ArrayList();
                        compList.add(componentsAtPoint[0]);
                    }
                    compList.add(comp);
                }
            }
        }

        if (compList == null)
            return componentsAtPoint[0] != null ?
                     componentsAtPoint : new Component[0];

        componentsAtPoint = new Component[compList.size()];
        compList.toArray(componentsAtPoint);
        return componentsAtPoint;
    }

    RADVisualContainer getMetaContainerAt(Point point, int mode) {
        RADComponent metacomp = getMetaComponentAt(point, mode);
        if (metacomp == null)
            return null;
        if (metacomp instanceof RADVisualContainer)
            return (RADVisualContainer) metacomp;
        if (metacomp instanceof RADVisualComponent)
            return (RADVisualContainer) metacomp.getParentComponent();
        return null;
    }

    /** Selects component at the position e.getPoint() on component layer.
     * What component is selected further depends on whether CTRL or ALT
     * keys are hold. */
    private RADComponent selectComponent(MouseEvent e) {
        int selMode = !e.isAltDown() ? COMP_DEEPEST :
                (!e.isShiftDown() ? COMP_ABOVE_SELECTED : COMP_UNDER_SELECTED);

        RADComponent hitMetaComp = getMetaComponentAt(e.getPoint(), selMode);

        if ((e.isControlDown() || e.isShiftDown()) && !e.isAltDown()) {
            // Control is pressed - add component to selection
            if (hitMetaComp != null)
                if (formDesigner.isComponentSelected(hitMetaComp))
                    formDesigner.removeComponentFromSelection(hitMetaComp);
                else
                    formDesigner.addComponentToSelection(hitMetaComp);
        }
        // [interval selection commented out - is confusing if following just
        //  the creation order of components and not the visual appearance]
/*        else if (e.isShiftDown() && !e.isAltDown()) {
            // Shift is pressed - select interval
            if (hitMetaComp != null
                && !formDesigner.isComponentSelected(hitMetaComp))
            {
                if (formDesigner.getSelectedComponents().size() > 0) {
                    RADComponent[] intervalToSelect =
                        getComponentsIntervalToSelect(hitMetaComp, false);
                    if (intervalToSelect == null)
                        intervalToSelect = getComponentsIntervalToSelect(
                                                       hitMetaComp, true);
                    if (intervalToSelect != null)
                        formDesigner.setSelectedComponents(intervalToSelect);
                    else
                        formDesigner.setSelectedComponent(hitMetaComp);
                }
                else
                    formDesigner.setSelectedComponent(hitMetaComp);
            }
        } */
        else { // no reasonable modifier key pressed - select single component
            if (hitMetaComp != null) {
                if (!formDesigner.isComponentSelected(hitMetaComp))
                    formDesigner.setSelectedComponent(hitMetaComp);
            }
            else formDesigner.clearSelection();
        }

        return hitMetaComp;
    }

/*    private RADComponent[] getComponentsIntervalToSelect(
                             RADComponent clickedComp,
                             boolean forward)
    {
        if (!(clickedComp instanceof RADVisualComponent))
            return null;

        java.util.List toSelect = new LinkedList();
        RADVisualComponent comp = (RADVisualComponent) clickedComp;
        boolean selected = false;

        do  // starting with clickedComp,
        {   // go forward/backward in components until a selected one is reached
            if (forward)
                toSelect.add(comp);
            else
                toSelect.add(0, comp);

            comp = formDesigner.getNextVisualComponent(comp, forward);
        }
        while (comp != null && comp != clickedComp
               && !(selected = formDesigner.isComponentSelected(comp))
               && comp != formDesigner.getTopDesignComponent());

        if (selected) { // selected component found - we can make the interval
            if (comp == formDesigner.getTopDesignComponent()) {
                if (!forward) // top component is fine when going backward
                    toSelect.add(0, comp);
            }
            else { // add also already selected components in the direction
                selected = false;
                do {
                    if (forward)
                        toSelect.add(comp);
                    else
                        toSelect.add(0, comp);

                    comp = formDesigner.getNextVisualComponent(comp, forward);
                }
                while (comp != null
                       && (selected = formDesigner.isComponentSelected(comp))
                       && comp != formDesigner.getTopDesignComponent());

                if (selected && !forward)
                    toSelect.add(0, comp); // top comp is fine when going backward
            }            

            RADComponent[] compArray = new RADComponent[toSelect.size()];
            toSelect.toArray(compArray);
            return compArray;
        }

        return null;
    } */

    private void selectOtherComponentsNode() {
        FormEditor formEditor = formDesigner.getFormEditor();
        ComponentInspector ci = ComponentInspector.getInstance();
        Node[] selectedNode = new Node[] {
            ((FormRootNode)formEditor.getFormRootNode()).getOthersNode() };
        
        try {
            ci.setSelectedNodes(selectedNode, formEditor);
            formDesigner.clearSelectionImpl();
            formDesigner.repaintSelection();
        }
        catch (java.beans.PropertyVetoException ex) {
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
        }

        formDesigner.setActivatedNodes(selectedNode);
    }

    private boolean processDoubleClick(MouseEvent e) {
        if (e.isShiftDown() || e.isControlDown())
            return false;

        RADComponent metacomp = getMetaComponentAt(e.getPoint(), COMP_SELECTED);
        if (metacomp == null)
            return true;

        if (e.isAltDown()) {
             if (metacomp == formDesigner.getTopDesignComponent()) {
                metacomp = metacomp.getParentComponent();
                if (metacomp == null)
                    return true;
            }
             else return false;
        }

        formDesigner.startInPlaceEditing(metacomp);

        // [perhaps we should remove the default action from node completely]
//        Node node = metacomp.getNodeReference();
//        if (node != null) {
//            Action action = node.getPreferredAction();
//            if (action != null) {// && action.isEnabled()) {
//                action.actionPerformed(new ActionEvent(
//                        node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
//            }
//        }

        return true;
    }

    private void processMouseClickInLayoutSupport(RADComponent metacomp,
                                                  MouseEvent e)
    {
        if (!(metacomp instanceof RADVisualComponent))
            return;

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            (RADVisualContainer) metacomp.getParentComponent();
        LayoutSupportManager laysup = metacont != null ?
                                      metacont.getLayoutSupport() : null;
        if (laysup == null)
            return;

        Container cont = (Container) formDesigner.getComponent(metacont);
        Container contDelegate = metacont.getContainerDelegate(cont);
        Point p = convertPointToComponent(e.getPoint(), contDelegate);
        laysup.processMouseClick(p, cont, contDelegate);
    }

    private void showContextMenu(Point popupPos) {
        ComponentInspector inspector = ComponentInspector.getInstance();
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated != formDesigner.multiViewObserver.getTopComponent()
                && activated != inspector)
            return;

        formDesigner.componentActivated(); // just for sure...

        Node[] selectedNodes = inspector.getSelectedNodes();
        JPopupMenu popup = NodeOp.findContextMenu(selectedNodes);
        if (popup != null) {
            popup.show(HandleLayer.this, popupPos.x, popupPos.y);
        }
    }

    // --------

    private boolean anyDragger() {
        return draggedComponent != null || selectionDragger != null;
    }

    private RADVisualComponent[] getComponentsToDrag() {
        // all selected components must be visible in the designer and have the
        // same parent; redundant sub-contained components must be filtered out
        java.util.List selectedComps = formDesigner.getSelectedComponents();
        java.util.List workingComps = new ArrayList(selectedComps.size());
        RADVisualContainer parent = null;

        for (Iterator it = selectedComps.iterator(); it.hasNext(); ) {
            RADComponent metacomp = (RADComponent) it.next();
            boolean subcontained = false;
            for (Iterator it2 = selectedComps.iterator(); it2.hasNext(); ) {
                RADComponent metacomp2 = (RADComponent) it2.next();
                if (metacomp2 != metacomp && metacomp2.isParentComponent(metacomp)) {
                    subcontained = true;
                    break;
                }
            }
            if (!subcontained) {
                RADVisualContainer metacont =
                    (RADVisualContainer) metacomp.getParentComponent();
                
                if (substituteForContainer(metacont)) {
                    // hack: if trying to drag something in scrollpane,
                    // drag the whole scrollpane instead
                    metacomp = metacont;
                    metacont = (RADVisualContainer) metacomp.getParentComponent();
                }

                if (parent != null) {
                    if (parent != metacont)
                        return null; // components in different containers
                }
                else {
                    if (metacont == null
                        || !formDesigner.getTopDesignComponent().isParentComponent(metacomp))
                    {   // out of visible tree
                        return null;
                    }
                    parent = metacont;
                }
                workingComps.add(metacomp);
            }
        }

        return workingComps.isEmpty() ? null : (RADVisualComponent[])
            workingComps.toArray(new RADVisualComponent[workingComps.size()]);
    }

    boolean endDragging(MouseEvent e) {
        if (!anyDragger())
            return false;

        if (resizeType != 0) {
            resizeType = 0;
            Cursor cursor = getCursor();
            if (cursor != null && cursor.getType() != Cursor.DEFAULT_CURSOR)
                setCursor(Cursor.getDefaultCursor());
            if (getToolTipText() != null)
                setToolTipText(null);
        }

        boolean done = true;

        if (draggedComponent != null) {
            if (draggedComponent.end(e))
                draggedComponent = null;
            else
                done = false;
        }
        else if (selectionDragger != null) {
            if (e != null)
                selectionDragger.drop(e.getPoint());
            selectionDragger = null;
//                repaint();
        }

        if (done)
            draggingEnded = true;

        return done;
    }

    // Highlighted panel
    private JPanel darkerPanel = null;
    private static class HighlightBorder extends javax.swing.border.LineBorder {
        HighlightBorder(Color color, int thickness) {
            super(color, thickness);
        }

        public Insets getBorderInsets(Component c) {
            // Hack - don't affect component's content
            return new Insets(0, 0, 0, 0);
        }
    }
    
    // Highlights panel below mouse cursor.
    private void highlightPanel(MouseEvent e) {
        Component[] comps = getDeepestComponentsAt(formDesigner.getComponentLayer(), e.getPoint());
        if (comps.length == 0)
            return;
        Component comp = comps[comps.length-1];
        RADComponent radcomp = formDesigner.getMetaComponent(comp);
        if ((radcomp != null) && !(radcomp instanceof RADVisualContainer)) {
            radcomp = radcomp.getParentComponent();
            comp = comp.getParent();
        }
        if ((radcomp == null) || (radcomp == formDesigner.getTopDesignComponent())
            || (!(comp instanceof JPanel))) {
            comp = null;
        }
        JPanel panel = (JPanel)comp;
        if (darkerPanel != panel) {
            if (darkerPanel != null) {
                darkerPanel.setBorder(null);
                darkerPanel = null;
            }
            if (shouldHighlightPanel(panel, radcomp)) {
                panel.setBorder(new HighlightBorder(darkerPanelColor(panel.getBackground()), 1));
                darkerPanel = panel;
            }
        }
    }
    
    private boolean shouldHighlightPanel(JPanel panel, RADComponent radPanel) {
        if (panel != null) {
            if (panel.getBorder() != null) { // Maybe we should highlight also panels with EmptyBorder
                return false;
            }
            if (!(panel.getBackground() instanceof javax.swing.plaf.UIResource)) {
                return false;
            }
            if (radPanel == formDesigner.getTopDesignComponent()) {
                return false;
            }
            if (radPanel instanceof RADVisualContainer) {
                RADVisualContainer metacont = (RADVisualContainer)radPanel;
                RADVisualContainer parent = metacont.getParentContainer();
                if (parent != null) {
                    LayoutSupportManager manager = parent.getLayoutSupport();
                    if ((manager != null) && manager.isDedicated()) {
                        return false;
                    }
                    JPanel realPanel = (JPanel)formDesigner.getComponent(radPanel);
                    Component parentBean = (Component)parent.getBeanInstance();
                    Component realParent = (Component)formDesigner.getComponent(parent);
                    if (realParent.getSize().equals(realPanel.getSize()) && realPanel.getLocation().equals(new Point(0,0))) {
                        if (parentBean instanceof JPanel) {
                            return shouldHighlightPanel((JPanel)parentBean, parent);
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return (panel != null);
    }
    
    private static Color darkerPanelColor(Color color) {
        double factor = 0.9;
	return new Color((int)(color.getRed()*factor), 
			 (int)(color.getGreen()*factor),
			 (int)(color.getBlue()*factor));
    }

    // Check the mouse cursor if it is at position where a component or the
    // designer can be resized. Change mouse cursor accordingly.
    private void checkResizing(MouseEvent e) {
        if (formDesigner.getTopDesignComponent() == null) return; // bean forms
        int resizing = checkComponentsResizing(e);
        if (resizing == 0) {
            resizing = checkDesignerResizing(e);
            if (resizing == 0) {
                if (getToolTipText() != null)
                    setToolTipText(null);
            }
            else if (getToolTipText() == null) {
                Dimension size = formDesigner.getComponentLayer()
                                               .getDesignerSize();
                if (resizingHintFormat == null)
                    resizingHintFormat = new MessageFormat(
                        FormUtils.getBundleString("FMT_HINT_DesignerResizing")); // NOI18N
                String hint = resizingHintFormat.format(
                                new Object[] { new Integer(size.width),
                                               new Integer(size.height) });
                setToolTipText(hint);
                ToolTipManager.sharedInstance().mouseEntered(e);
            }
        }
        else if (getToolTipText() != null)
            setToolTipText(null);

        if (resizing != 0)
            setResizingCursor(resizing);
        else {
            Cursor cursor = getCursor();
            if (cursor != null && cursor.getType() != Cursor.DEFAULT_CURSOR)
                setCursor(Cursor.getDefaultCursor());
        }
    }

    // Check the mouse cursor if it is at position where designer can be
    // resized.
    private int checkDesignerResizing(MouseEvent e) {
        if (!e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
            ComponentLayer compLayer = formDesigner.getComponentLayer();
            int resizing = getSelectionResizable(
                             e.getPoint(),
                             compLayer.getComponentContainer(),
                             compLayer.getDesignerOutsets().right + 2);

            resizeType = validDesignerResizing(resizing) ?
                         resizing | DESIGNER_RESIZING : 0;
        }
        else resizeType = 0;

        return resizeType;
    }

    // Check whether given resize type is valid for designer.
    private boolean validDesignerResizing(int resizing) {
        return resizing == (LayoutSupportManager.RESIZE_DOWN
                            | LayoutSupportManager.RESIZE_RIGHT)
            || resizing == LayoutSupportManager.RESIZE_DOWN
            || resizing == LayoutSupportManager.RESIZE_RIGHT;
    }

    // Check the mouse cursor if it is at position where a component (or more
    // components) can be resized.
    private int checkComponentsResizing(MouseEvent e) {
        resizeType = 0;
        if (e.isAltDown() || e.isControlDown() || e.isShiftDown()) {
            return 0;
        }

        // check selected components whether they are in the same container
        RADComponent parent = null;
        Iterator selected = formDesigner.getSelectedComponents().iterator();
        while (selected.hasNext()) {
            RADComponent comp = (RADComponent) selected.next();
            if (parent == null) {
                if (!formDesigner.getTopDesignComponent().isParentComponent(comp)) {
                    return 0; // selected component without parent in visible tree
                }
                parent = comp.getParentComponent();
            }
            else if (comp.getParentComponent() != parent) {
                return 0; // different parent
            }
        }

        Point p = e.getPoint();
        RADComponent compAtPoint = selectedComponentAt(p, 6);

        if (!(compAtPoint instanceof RADVisualComponent))
            return 0;

/*        if (!formDesigner.isComponentSelected(compAtPoint)) {
            // not on a selected component, but some might be near
            RADVisualComponent[] otherComps;
            if (compAtPoint instanceof RADVisualContainer) {
                otherComps = ((RADVisualContainer)compAtPoint).getSubComponents();
            }
            else {
                RADVisualContainer metacont = (RADVisualContainer)
                                              compAtPoint.getParentComponent();
                if (metacont != null)
                    otherComps = metacont.getSubComponents();
                else
                    return 0; // component without a parent
            }

            for (int i=0; i < otherComps.length; i++) {
                if (formDesigner.isComponentSelected(otherComps[i])) {
                    resizeType = getComponentResizable(p, otherComps[i]);
                    if (resizeType != 0)
                        break;
                }
            }
        }
        else { // mouse on selected component */
            resizeType = getComponentResizable(p, (RADVisualComponent)compAtPoint);
//        }

        return resizeType;
    }
    
    // Returns selected component at the given point (even outside the designer area).
    private RADComponent selectedComponentAt(Point p, int borderSize) {
        RADComponent compAtPoint = null;
        Iterator selected = formDesigner.getSelectedLayoutComponents().iterator();
        while (selected.hasNext()) {
            RADComponent metacomp = (RADComponent) selected.next();
            Component comp = (Component)formDesigner.getComponent(metacomp);
            if (comp == null || comp.getParent() == null)
                continue;  // might be not added yet after move operation
            Rectangle rect = new Rectangle(-borderSize, -borderSize, comp.getWidth()+2*borderSize, comp.getHeight()+2*borderSize);
            convertRectangleFromComponent(rect, comp);
            if (rect.contains(p)) {
                compAtPoint = metacomp;
            }
        }
        return compAtPoint;
    }

    // Check how possible component resizing (obtained from layout support)
    // matches with mouse position on component selection border. 
    private int getComponentResizable(Point p, RADVisualComponent metacomp) {
        RADVisualContainer metacont = metacomp.getParentContainer();
//        if (substituteForContainer(metacont)) {
//            metacomp = metacont;
//            metacont = metacomp.getParentContainer();
//        }

        int resizable = getComponentResizable(metacomp);
        if (resizable != 0) {
            Component comp = (Component) formDesigner.getComponent(metacomp);
            resizable &= getSelectionResizable(p, comp, 6);
        }

        return resizable;
    }
    
    private int getComponentResizable(RADVisualComponent metacomp) {
        RADVisualContainer metacont = metacomp.getParentContainer();
        if (metacont == null || metacomp == formDesigner.getTopDesignComponent()) {
            return 0;
        }
        Component comp = (Component) formDesigner.getComponent(metacomp);
        int resizable;
        LayoutSupportManager laySup = metacont.getLayoutSupport();
        if (laySup == null) { // new layout support
            java.util.List selectedComps = formDesigner.getSelectedComponents();
            if (selectedComps.size() == 1) {
                // [real resizability spec TBD]
                resizable = LayoutSupportManager.RESIZE_LEFT
                            | LayoutSupportManager.RESIZE_RIGHT
                            | LayoutSupportManager.RESIZE_UP
                            | LayoutSupportManager.RESIZE_DOWN;
            }
            else resizable = 0;
        }
        else { // old layout support
            Container cont = (Container) formDesigner.getComponent(metacont);
            Container contDel = metacont.getContainerDelegate(cont);

            resizable = laySup.getResizableDirections(
                                       cont, contDel,
                                       comp, metacont.getIndexOf(metacomp));
        }
        return resizable;
    }

    // Compute possible resizing directions according to mouse position on
    // component selection border.
    private int getSelectionResizable(Point p, Component comp, int borderWidth) {
        if (comp == null)
            return 0;

        int resizable = 0;

        Rectangle r = new Rectangle(0, 0, comp.getWidth(), comp.getHeight());
        convertRectangleFromComponent(r, comp);
        r.grow(borderWidth, borderWidth);
        if (r.contains(p)) {
            r.grow(-borderWidth, -borderWidth);
            r.grow(-3, -3);
            if (r.width < 0)
                r.width = 0;
            if (r.height < 0)
                r.height = 0;

            if (p.y >= r.y + r.height)
                resizable |= LayoutSupportManager.RESIZE_DOWN;
            else if (p.y < r.y)
                resizable |= LayoutSupportManager.RESIZE_UP;
            if (p.x >= r.x + r.width)
                resizable |= LayoutSupportManager.RESIZE_RIGHT;
            else if (p.x < r.x)
                resizable |= LayoutSupportManager.RESIZE_LEFT;
        }

        return resizable;
    }

    private void setResizingCursor(int resizeType) {
        Cursor cursor = null;
        if ((resizeType & LayoutSupportManager.RESIZE_UP) != 0) {
            if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            else if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            else
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        }
        else if ((resizeType & LayoutSupportManager.RESIZE_DOWN) != 0) {
            if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            else if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            else
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
        }
        else if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0)
            cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
        else if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0)
            cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

        if (cursor == null)
            cursor = Cursor.getDefaultCursor();

        setCursor(cursor);
    }

    private void setUserDesignerSize() {
        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
            FormUtils.getBundleString("CTL_SetDesignerSize_Label"), // NOI18N
            FormUtils.getBundleString("CTL_SetDesignerSize_Title")); // NOI18N
        Dimension size = formDesigner.getComponentLayer().getDesignerSize();
        input.setInputText(Integer.toString(size.width) + ", " // NOI18N
                           + Integer.toString(size.height));

        if (DialogDisplayer.getDefault().notify(input) == NotifyDescriptor.OK_OPTION) {
            String txt = input.getInputText();
            int i = txt.indexOf(',');
            if (i > 0) {
                int n = txt.length();
                try {
                    int w = Integer.parseInt(txt.substring(0, i));
                    while (++i < n && txt.charAt(i) == ' ');
                    int h = Integer.parseInt(txt.substring(i, n));
                    if (w >= 0 && h >= 0) {
                        size = new Dimension(w ,h);
                        formDesigner.setStoredDesignerSize(size);
                        // update must be done immediately because of a weird
                        // mouse move event occurring after closing the input
                        // dialog but before updating the designer through
                        // synthetic property change processing
                        formDesigner.getComponentLayer().setDesignerSize(size);
//                        formDesigner.updateComponentLayer();
                        
                        setToolTipText(null);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
                catch (NumberFormatException ex) {} // silently ignore, do nothing
            }
        }
    }

    private Object getConstraintsAtPoint(RADComponent metacomp, Point point) {
        if (!(metacomp instanceof RADVisualComponent))
            return null;

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            (RADVisualContainer) metacomp.getParentComponent();
        LayoutSupportManager laysup = metacont != null ?
                                      metacont.getLayoutSupport() : null;

//        if (laysup != null) { // old layout support
            Container cont = (Container) formDesigner.getComponent(metacont);
            Container contDel = metacont.getContainerDelegate(cont);
            Point p = convertPointToComponent(point.x, point.y, contDel);
            return laysup.getNewConstraints(cont, contDel, null, -1, p, null);
//        }
//        else { // new layout support
//            return formDesigner.getLayoutDesigner().getSuggestedGuidingLines();
//        }
    }

    private static boolean substituteForContainer(RADVisualContainer metacont) {
        return metacont != null
               && metacont.getBeanClass().isAssignableFrom(JScrollPane.class)
               && metacont.getSubComponents().length > 0;
    }

    // ------

    boolean mouseOnVisual(Point p) {
        Rectangle r = formDesigner.getComponentLayer().getDesignerOuterBounds();
        return r.contains(p);
    }
    
    /**
     * Determines whether the passed point is above the non-visual tray.
     *
     * @return <code>true</code> if the point is above the non-visual tray,
     * returns <code>false</code> otherwise.
     */
    boolean mouseOnNonVisualTray(Point p) {
        Component tray = formDesigner.getNonVisualTray();
        return tray != null ? tray.getBounds().contains(p) : false;
    }

    // NOTE: does not create a new Point instance
    private Point convertPointFromComponent(Point p, Component sourceComp) {
        return formDesigner.pointFromComponentToHandleLayer(p, sourceComp);
    }

    private Point convertPointFromComponent(int x, int y, Component sourceComp) {
        return formDesigner.pointFromComponentToHandleLayer(new Point(x, y), sourceComp);
    }

    // NOTE: does not create a new Point instance
    private Point convertPointToComponent(Point p, Component targetComp) {
        return formDesigner.pointFromHandleToComponentLayer(p, targetComp);
    }

    private Point convertPointToComponent(int x, int y, Component targetComp) {
        return formDesigner.pointFromHandleToComponentLayer(new Point(x, y), targetComp);
    }

    // NOTE: does not create a new Rectangle instance
    private Rectangle convertRectangleFromComponent(Rectangle rect,
                                                    Component sourceComp)
    {
        Point p = convertPointFromComponent(rect.x, rect.y, sourceComp);
        rect.x = p.x;
        rect.y = p.y;
        return rect;
    }

    // NOTE: does not create a new Rectangle instance
    private Rectangle convertRectangleToComponent(Rectangle rect,
                                                  Component targetComp)
    {
        Point p = convertPointToComponent(rect.x, rect.y, targetComp);
        rect.x = p.x;
        rect.y = p.y;
        return rect;
    }

    // ---------
    // MouseListener implementation

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)
            && !draggingEnded && !endDragging(null))
        {
            if (mouseOnNonVisualTray(e.getPoint())) {
                dispatchToNonVisualTray(e);
            } else {
                showContextMenu(e.getPoint());
            }
        }
        e.consume();
    }

    public void mouseReleased(MouseEvent e) {
        if (!HandleLayer.this.isVisible())
            return;

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT
                && !draggingEnded && !endDragging(e))
            {   // there was no dragging, so mouse release may have other meaning
                boolean modifier = e.isControlDown() || e.isAltDown() || e.isShiftDown();
                if ((resizeType & DESIGNER_RESIZING) != 0
                    && e.getClickCount() == 2
                    && !modifier)
                {   // doubleclick on designer's resizing border
                    setUserDesignerSize();
                } else if (mouseOnNonVisualTray(e.getPoint())) {
                    dispatchToNonVisualTray(e);
                }
                else if (prevLeftMousePoint != null
                         && e.getClickCount() == 1
                         && prevLeftMousePoint.distance(e.getPoint()) <= 2
                         && !modifier)
                {   // second click on the same place in a component
                    RADComponent metacomp = getMetaComponentAt(e.getPoint(), COMP_SELECTED);
                    if (metacomp != null) {
                        formDesigner.startInPlaceEditing(metacomp);
                    }
                }
                else if (e.getClickCount() == 1
                         && e.isShiftDown()
                         && !e.isAltDown()
                         && !e.isControlDown())
                {   // Shift + mouse release - interval selection
                    selectComponent(e);
                }
            }

            prevLeftMousePoint = lastLeftMousePoint;
            lastLeftMousePoint = null;
        } else if (mouseOnNonVisualTray(e.getPoint())) {
            dispatchToNonVisualTray(e);
        }

        e.consume();
    }

    public void mouseEntered(MouseEvent e) {
        if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
            formDesigner.requestActive();
            StatusDisplayer.getDefault().setStatusText(
                FormUtils.getFormattedBundleString(
                    "FMT_MSG_AddingComponent", // NOI18N
                    new String[] { CPManager.getDefault().getSelectedItem()
                                               .getNode().getDisplayName() }));
        }
    }

    public void mouseExited(MouseEvent e) {
        if (draggedComponent != null && formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
            draggedComponent.move(null);
            repaint();
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        }
    }

    public void mousePressed(MouseEvent e) {
        formDesigner.componentActivated();
        if (!HandleLayer.this.isVisible())
            return;

        if (SwingUtilities.isRightMouseButton(e)) {
            if (formDesigner.getDesignerMode() != FormDesigner.MODE_SELECT) {
                formDesigner.toggleSelectionMode(); // calls endDragging(null)
                repaint();
            }
            else if (endDragging(null)) { // there was dragging, now canceled
                repaint();
            }
            else if (!SwingUtilities.isLeftMouseButton(e)) {
                // no dragging, ensure a component is selected for conext menu
                if (mouseOnNonVisualTray(e.getPoint())) {
                    dispatchToNonVisualTray(e);
                } else if (!mouseOnVisual(e.getPoint())) {
                    selectOtherComponentsNode();
                }
                else { // select component only if there is nothing selected on current position
                    RADComponent hitMetaComp =
                        getMetaComponentAt(e.getPoint(), COMP_SELECTED);
                    if (!formDesigner.isComponentSelected(hitMetaComp)) {   
                        formDesigner.setSelectedComponent(hitMetaComp);
                    }
                }
                draggingEnded = false; // reset flag preventing dragging from start
            }
            e.consume();
        }
        else if (SwingUtilities.isLeftMouseButton(e)) {
            lastLeftMousePoint = e.getPoint();

            boolean modifier = e.isControlDown() || e.isAltDown() || e.isShiftDown();

            if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT) {
                if (mouseOnNonVisualTray(e.getPoint())) {
                    dispatchToNonVisualTray(e);
                } else {
                checkResizing(e);
                if (!(e.isShiftDown() && e.isAltDown() && e.isControlDown())) {
                    // mouse not pressed with Shift only (which is reserved for
                    // interval or area selection - applied on mouse release or
                    // mouse dragged)
                    if (draggedComponent == null && !modifier
                        && (resizeType & DESIGNER_RESIZING) != 0)
                    {   // start designer resizing
                        if (e.getClickCount() != 2) {
                            draggedComponent = new ResizeComponentDrag(
                                new RADVisualComponent[] { formDesigner.getTopDesignComponent() },
                                lastLeftMousePoint,
                                resizeType&~DESIGNER_RESIZING);
                        }
                    }
                    else if (!mouseOnVisual(lastLeftMousePoint)) {
                        if ((resizeType == 0) && (selectedComponentAt(lastLeftMousePoint, 0) == null))
                            selectOtherComponentsNode();
                    }
                    else if (resizeType == 0
                             && (e.getClickCount() != 2
                                 || !processDoubleClick(e))
                             && !e.isShiftDown()) // selection with shift only on mouse release
                    {   // no resizing, no doubleclick - select component
                        RADComponent hitMetaComp = selectComponent(e); 
                        if (hitMetaComp != null && !modifier) // plain single click
                            processMouseClickInLayoutSupport(hitMetaComp, e);
                    }
                }
                }
//                endDragging(null); // for sure
                draggingEnded = false; // reset flag preventing dragging from start
            }
            else if (!viewOnly) { // form can be modified
                if (formDesigner.getDesignerMode() == FormDesigner.MODE_CONNECT) {
                    selectComponent(e);
                }
                else if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
                    endDragging(e);
                    if (!e.isShiftDown()) {
                        formDesigner.toggleSelectionMode();
                    }
                    // otherwise stay in adding mode
                }
            }
            e.consume();
        }
    }

    // ---------
    // MouseMotionListener implementation

    public void mouseDragged(MouseEvent e) {
        if (formDesigner.getDesignerMode() != FormDesigner.MODE_SELECT)
            return; // dragging makes sense only selection mode

        Point p = e.getPoint();
        if (lastMousePosition != null) {
            lastXPosDiff = p.x - lastMousePosition.x;
            lastYPosDiff = p.y - lastMousePosition.y;
        }

        if (!draggingEnded && !anyDragger() && lastLeftMousePoint != null) { // no dragging yet
            if (!viewOnly
                 && !e.isControlDown() && (!e.isShiftDown() || e.isAltDown())
                 && (resizeType != 0 || lastLeftMousePoint.distance(p) > 6))
            {   // start component dragging
                RADVisualComponent[] draggedComps = getComponentsToDrag();
                if (draggedComps != null) {
                    if (resizeType == 0)
                        draggedComponent = new ExistingComponentDrag(
                            draggedComps, lastLeftMousePoint, e.getModifiers());
                    else 
                        draggedComponent = new ResizeComponentDrag(
                            draggedComps, lastLeftMousePoint, resizeType);
                }
            }
            if (draggedComponent == null // component dragging has not started
                && formDesigner.getTopDesignComponent() instanceof RADVisualContainer
                && lastLeftMousePoint.distance(p) > 4
                && !e.isAltDown() && !e.isControlDown()
                && (e.isShiftDown() || getMetaComponentAt(lastLeftMousePoint, COMP_DEEPEST)
                                       == formDesigner.getTopDesignComponent()))
            {   // start selection dragging
                selectionDragger = new SelectionDragger(lastLeftMousePoint);
            }
        }

        if (draggedComponent != null) {
            draggedComponent.move(e);
            highlightPanel(e);
            repaint();
        }
        else if (selectionDragger != null) {
            selectionDragger.drag(p);
            repaint();
        }

        lastMousePosition = p;
        e.consume();
    }

    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        if (lastMousePosition != null) {
            lastXPosDiff = p.x - lastMousePosition.x;
            lastYPosDiff = p.y - lastMousePosition.y;
        }
        if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
            if (draggedComponent == null) {
                // first move event, pre-create visual component to be added
                draggedComponent = new NewComponentDrag(
                      CPManager.getDefault().getSelectedItem());
            }
            draggedComponent.move(e);
            repaint();
        }
        else if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT
                 && !anyDragger())
        {
            checkResizing(e);
        }
        highlightPanel(e);
        lastMousePosition = p;
    }

    /**
     * Dispatches the mouse event to the non-visual tray.
     *
     * @param e the event to dispatch.
     */
    private void dispatchToNonVisualTray(final MouseEvent e) {
        NonVisualTray tray = formDesigner.getNonVisualTray();
        if (tray == null) {
            return;
        }
        Point point = SwingUtilities.convertPoint(this, e.getPoint(), tray);
        Component component = SwingUtilities.getDeepestComponentAt(tray, point.x, point.y);
        point = SwingUtilities.convertPoint(tray, point, component);
        component.dispatchEvent(new MouseEvent(
            component,
            e.getID(),
            e.getWhen(),
            e.getModifiers(),
            point.x,
            point.y,
            e.getClickCount(),
            e.isPopupTrigger()));
    }

    public String getToolTipText(MouseEvent e) {
        if (mouseOnNonVisualTray(e.getPoint())) {
            NonVisualTray tray = formDesigner.getNonVisualTray();
            Point point = SwingUtilities.convertPoint(this, e.getPoint(), tray);
            JComponent component = (JComponent)SwingUtilities.getDeepestComponentAt(tray, point.x, point.y);
            point = SwingUtilities.convertPoint(tray, point, component);
            return component.getToolTipText(new MouseEvent(
                tray,
                e.getID(),
                e.getWhen(),
                e.getModifiers(),
                point.x,
                point.y,
                e.getClickCount(),
                e.isPopupTrigger()));
        } else {
            return super.getToolTipText(e);
        }
    }

    // ----------

    private class SelectionDragger {
        private Point startPoint;
        private Point lastPoint;

        public SelectionDragger(Point startPoint) {
            this.startPoint = startPoint;
        }

        public void paintDragFeedback(Graphics g) {
            if (startPoint != null && lastPoint != null) {
                Rectangle r = getRectangle();
                g.drawRect(r.x, r.y, r.width, r.height);
            }
        }

        public void drag(Point p) {
            lastPoint = p;
        }

        public void drop(Point endPoint) {
            if (startPoint != null && endPoint != null) {
                lastPoint = endPoint;
                ArrayList toSelect = new ArrayList();
                collectSelectedComponents(
                    getRectangle(),
                    formDesigner.getComponentLayer().getComponentContainer(),
                    toSelect);

                RADComponent[] selected = new RADComponent[toSelect.size()];
                toSelect.toArray(selected);
                formDesigner.setSelectedComponents(selected);
            }
        }

        private Rectangle getRectangle() {
            int x = startPoint.x <= lastPoint.x ? startPoint.x : lastPoint.x;
            int y = startPoint.y <= lastPoint.y ? startPoint.y : lastPoint.y;
            int w = lastPoint.x - startPoint.x;
            if (w < 0)
                w = -w;
            int h = lastPoint.y - startPoint.y;
            if (h < 0)
                h = -h;

            return new Rectangle(x, y, w, h);
        }

        private boolean collectSelectedComponents(Rectangle selRect,
                                                  Container cont,
                                                  java.util.List toSelect)
        {
            ArrayList subContainers = new ArrayList();

            Component[] comps = cont.getComponents();
            for (int i=0; i < comps.length; i++) {
                Component comp = comps[i];
                Rectangle bounds = convertRectangleFromComponent(
                                       comps[i].getBounds(), cont);
                boolean intersects = selRect.intersects(bounds);

                RADComponent metacomp = formDesigner.getMetaComponent(comp);
                if (metacomp instanceof RADComponent) {
                    if (intersects)
                        toSelect.add(metacomp);
                    if (!(metacomp instanceof ComponentContainer))
                        continue;
                }

                if (intersects && comp instanceof Container)
                    subContainers.add(comp);
            }

            if (toSelect.size() > 1
                    || (toSelect.size() == 1 && subContainers.size() == 0))
                return true;

            Object theOnlyOne = toSelect.size() == 1 ? toSelect.get(0) : null;

            for (int i=0; i < subContainers.size(); i++) {
                toSelect.clear();
                if (collectSelectedComponents(selRect,
                                              (Container)subContainers.get(i),
                                              toSelect))
                    return true;
            }

            if (theOnlyOne != null) {
                toSelect.add(theOnlyOne);
                return true;
            }
            
            return false;
        }
    }

    // -------

    private abstract class ComponentDrag {
        RADVisualComponent[] movingComponents;
        RADVisualContainer targetContainer;
        RADVisualContainer fixedTarget;
        Component[] showingComponents;
        Rectangle[] originalBounds; // in coordinates of HandleLayer
        Rectangle compoundBounds; // compound from original bounds
        Rectangle[] movingBounds; // in coordinates of ComponentLayer
        Point hotSpot; // in coordinates of ComponentLayer
        Point convertPoint; // from HandleLayer to ComponentLayer (top visual component)
        boolean newDrag;
        boolean oldDrag;
        Object layoutUndoMark;
        UndoableEdit layoutUndoEdit;

        // ctor for adding new
        ComponentDrag() {
            if (formDesigner.getTopVisualContainer() == null) {
                convertPoint = new Point(0,0);
            } else {
                convertPoint = convertPointFromComponent(0, 0, formDesigner.getTopVisualContainer());
            }
        }

        // ctor for moving and resizing
        ComponentDrag(RADVisualComponent[] components, Point hotspot) {
            this();
            this.movingComponents = components;

            int count = components.length;
            showingComponents = new Component[count]; // [provisional - just one component can be moved]
            originalBounds = new Rectangle[count];
            movingBounds = new Rectangle[count];
            for (int i=0; i < count; i++) {
                showingComponents[i] = (Component) formDesigner.getComponent(movingComponents[i]);
                originalBounds[i] = showingComponents[i].getBounds();
                convertRectangleFromComponent(originalBounds[i], showingComponents[i].getParent());
                compoundBounds = compoundBounds != null ?
                                 compoundBounds.union(originalBounds[i]) : originalBounds[i];
                movingBounds[i] = new Rectangle();
                movingBounds[i].width = originalBounds[i].width;
                movingBounds[i].height = originalBounds[i].height;
            }

            this.hotSpot = hotspot == null ?
                new Point(4, 4) :
                new Point(hotspot.x - convertPoint.x, hotspot.y - convertPoint.y);
        }

        final RADVisualContainer getSourceContainer() {
            return (movingComponents == null) ? null : movingComponents[0].getParentContainer();
        }

        final boolean isOldLayoutSource() {
            RADVisualContainer metacont = getSourceContainer();
            return metacont != null && metacont.getLayoutSupport() != null;
        }

        final boolean isTopComponent() {
            return movingComponents != null && formDesigner.getTopDesignComponent() == movingComponents[0];
        }
                
        final RADVisualContainer getTargetContainer(Point p, int modifiers) {
            if (fixedTarget != null) {
                return fixedTarget;
            }
            RADVisualContainer metacont = HandleLayer.this.getMetaContainerAt(
                    getMoveDirectionSensitivePoint(p, modifiers),
                    ((modifiers & InputEvent.ALT_MASK) != 0) ? COMP_SELECTED : COMP_DEEPEST);
            if (substituteForContainer(metacont)) {
                metacont = metacont.getParentContainer();
            }
            return metacont;
        }

        private Point getMoveDirectionSensitivePoint(Point p, int modifiers) {
            if (lastMousePosition != null
                && compoundBounds != null
                && (modifiers & (InputEvent.ALT_MASK|InputEvent.CTRL_MASK|InputEvent.SHIFT_MASK)) == 0)
            {
                if (compoundBounds.width <= 0 || compoundBounds.height <= 0) {
                    return p;
                }
                int x;
                int y;
                if (lastXPosDiff != 0 && lastYPosDiff != 0) {
                    double dx = lastXPosDiff;
                    double dy = lastYPosDiff;
                    double d = Math.abs(dy/dx);
                    double r = compoundBounds.getHeight() / compoundBounds.getWidth();
                    if (d > r) {
                        x = p.x + (int)Math.round(compoundBounds.getHeight() / d / 2.0) * (lastXPosDiff > 0 ? 1 : -1);
                        y = p.y - convertPoint.y - hotSpot.y + compoundBounds.y + (lastYPosDiff > 0 ? compoundBounds.height : 0);
                    }
                    else {
                        x = p.x - convertPoint.x - hotSpot.x + compoundBounds.x + (lastXPosDiff > 0 ? compoundBounds.width : 0);
                        y = p.y + (int)Math.round(compoundBounds.getWidth() * d / 2.0) * (lastYPosDiff > 0 ? 1 : -1);
                    }
                }
                else {
                    x = lastXPosDiff == 0 ? p.x :
                        p.x - convertPoint.x - hotSpot.x + compoundBounds.x + (lastXPosDiff > 0 ? compoundBounds.width : 0);
                    y = lastYPosDiff == 0 ? p.y :
                        p.y - convertPoint.y - hotSpot.y + compoundBounds.y + (lastYPosDiff > 0 ? compoundBounds.height : 0);
                }
                Rectangle boundaries = formDesigner.getComponentLayer().getDesignerInnerBounds();
                // don't let the component component fall into non-visual area easily
                if (x < boundaries.x && x + 8 >= boundaries.x) {
                    x = boundaries.x;
                }
                else if (x > boundaries.x + boundaries.width && x - 8 < boundaries.x + boundaries.width) {
                    x = boundaries.x + boundaries.width - 1;
                }
                if (y < boundaries.y && y + 8 >= boundaries.y) {
                    y = boundaries.y;
                }
                else if (y > boundaries.y + boundaries.height && y - 8 < boundaries.y + boundaries.height) {
                    y = boundaries.y + boundaries.height - 1;
                }
                return new Point(x, y);
            }
            else return p;
        }

        final void move(MouseEvent e) {
            if (e == null) {
                move(null, 0);
            } else {
                move(e.getPoint(), e.getModifiers());
            }
        }
        
        void move(Point p, int modifiers) {
            if (p == null) {
                for (int i=0; i<movingBounds.length; i++) {
                    movingBounds[i].x = Integer.MIN_VALUE;
                }
                return;
            }

            targetContainer = getTargetContainer(p, modifiers);

            if (newDrag && targetContainer != null && targetContainer.getLayoutSupport() == null) {
                p.x -= convertPoint.x;
                p.y -= convertPoint.y;
                formDesigner.getLayoutDesigner().move(p,
                                                      targetContainer.getId(),
                                                      ((modifiers & InputEvent.ALT_MASK) == 0),
                                                      ((modifiers & InputEvent.CTRL_MASK) != 0),
                                                      movingBounds);
            }
            else if (oldDrag && targetContainer != null && targetContainer.getLayoutSupport() != null) {
                oldMove(p);

                    for (int i=0; i<movingBounds.length; i++) {
                        movingBounds[i].x = p.x - convertPoint.x - hotSpot.x + originalBounds[i].x - convertPoint.x;
                        movingBounds[i].y = p.y - convertPoint.y - hotSpot.y + originalBounds[i].y - convertPoint.y;
                    }
            }
            else {
                for (int i=0; i<movingBounds.length; i++) {
                    movingBounds[i].x = p.x - convertPoint.x - hotSpot.x + originalBounds[i].x - convertPoint.x;
                    movingBounds[i].y = p.y - convertPoint.y - hotSpot.y + originalBounds[i].y - convertPoint.y;
                }
            }
        }

        final void paintFeedback(Graphics2D g) {
            if (movingBounds[0].x == Integer.MIN_VALUE)
                return;

            for (int i=0; i<showingComponents.length; i++) {
                Graphics gg = g.create(movingBounds[i].x + convertPoint.x,
                                       movingBounds[i].y + convertPoint.y,
                                       movingBounds[i].width + 1,
                                       movingBounds[i].height + 1);

                if (newDrag
                    && ((targetContainer != null && targetContainer.getLayoutSupport() == null)
                        || (targetContainer == null && isTopComponent())))
                {   // new layout support
                    // paint the component being moved
                    if (!isTopComponent()) {
                        showingComponents[i].setBounds(movingBounds[i]);
                        doLayout(showingComponents[i]);
                        if (showingComponents[i] instanceof JComponent) {
                            showingComponents[i].paint(gg);
                        } else {
                            showingComponents[i].getPeer().paint(gg);
                        }
                    } // resized top design component is painted automatically

                    // paint the selection rectangle
                    gg.setColor(formSettings.getSelectionBorderColor());
                    gg.drawRect(0, 0, movingBounds[i].width, movingBounds[i].height);

                    // paint the layout designer feedback
                    g.translate(convertPoint.x, convertPoint.y);
                    g.setColor(formSettings.getGuidingLineColor());
                    formDesigner.getLayoutDesigner().paintMoveFeedback(g);
                    g.translate(-convertPoint.x, -convertPoint.y);
                }
                else if (oldDrag && targetContainer != null && targetContainer.getLayoutSupport() != null) {
                    oldPaintFeedback(g, gg);
                }
                else if (showingComponents != null) { // non-visual area
                    Component comp = showingComponents[i];
                    if (!isOldLayoutSource()) { // don't paint if component dragged from old layout
                        comp.setBounds(movingBounds[i]);
                        doLayout(comp);
                        if (comp instanceof JComponent)
                            comp.paint(gg);
                        else
                            comp.getPeer().paint(gg);
                    }
                }
            }
        }

        final boolean end(MouseEvent e) {
            dragPanel.removeAll();

            boolean retVal;
            if (e == null) {
                retVal = end(null, 0);
            }
            else {
                retVal = end(e.getPoint(), e.getModifiers());
            }
            if (retVal) {
                movingComponents = null;
                targetContainer = null;
                fixedTarget = null;
                showingComponents = null;
            }
            else {
                init();
            }
            return retVal;
        }

        // methods to extend/override ---

        void init() {
            if (showingComponents != null) {
                // showing components need to be in a container to paint correctly,
                // components in new layout need to be hidden on their original location
                dragPanel.removeAll();
                boolean needPainting = !isTopComponent()
                        && (getSourceContainer() == null // being newly added
                            || getSourceContainer().getLayoutSupport() == null); // or in new layout
                for (int i=0; i < showingComponents.length; i++) {
                    Component comp = showingComponents[i];
                    if (needPainting) {
                        if (comp.getParent() == null) {
                            dragPanel.add(comp);
                        }
                        else {
                            comp.setVisible(false);
                        }
                    }
                    avoidDoubleBuffering(comp);
                }
            }
        }

        private void avoidDoubleBuffering(Component comp) {
            if (comp instanceof JComponent) {
                ((JComponent)comp).setDoubleBuffered(false);
            }
            if (comp instanceof Container) {
                Container cont = (Container)comp;
                for (int i=0; i<cont.getComponentCount(); i++) {
                    avoidDoubleBuffering(cont.getComponent(i));
                }
            }
        }

        boolean end(Point p, int modifiers) {
            return true;
        }

        void oldMove(Point p) {
        }

        void oldPaintFeedback(Graphics2D g, Graphics gg) {
        }

        // layout model undo/redo ---

        final void createLayoutUndoableEdit() {
            layoutUndoMark = getLayoutModel().getChangeMark();
            layoutUndoEdit = getLayoutModel().getUndoableEdit();
        }

        final void placeLayoutUndoableEdit() {
            if (!layoutUndoMark.equals(getLayoutModel().getChangeMark())) {
                getFormModel().addUndoableEdit(layoutUndoEdit);
            }
            layoutUndoMark = null;
            layoutUndoEdit = null;
        }
    }

    private static void doLayout(Component component) {
        if (component instanceof Container) {
            Container cont = (Container) component;
            cont.doLayout();
            for (int i=0, n=cont.getComponentCount(); i < n; i++) {
                doLayout(cont.getComponent(i));
            }
        }
    }

    // for moving existing components
    private class ExistingComponentDrag extends ComponentDrag {
        private int modifiers; // for the old layout support
        private ComponentDragger oldDragger; // drags components in the old layout support

        ExistingComponentDrag(RADVisualComponent[] comps,
                              Point hotspot, // in HandleLayer coordinates
                              int modifiers)
        {
            super(comps, hotspot);
            this.modifiers = modifiers;
            init();
        }

        void init() {
            RADVisualContainer metacont = getSourceContainer();
            String[] compIds = new String[showingComponents.length];
            for (int i=0; i < showingComponents.length; i++) {
                compIds[i] = movingComponents[i].getId();
                originalBounds[i].x -= convertPoint.x;
                originalBounds[i].y -= convertPoint.y;
            }

            if (metacont.getLayoutSupport() == null) { // new layout support
                formDesigner.getLayoutDesigner().startMoving(
                    compIds, originalBounds, hotSpot);
            }
            else { // dragging started in the old layout support
                LayoutComponent[] layoutComps = new LayoutComponent[compIds.length];
                for (int i=0; i < compIds.length; i++) {
                    layoutComps[i] = getLayoutModel().getLayoutComponent(compIds[i]);
                    if (layoutComps[i] == null) {
                        layoutComps[i] = new LayoutComponent(compIds[i], false);
                    }
                }
                formDesigner.getLayoutDesigner().startAdding(
                    layoutComps, originalBounds, hotSpot);                    
            }

            if ((modifiers & InputEvent.ALT_MASK) != 0) {
                // restricted dragging - within the same container, or one level up
                fixedTarget = (modifiers & InputEvent.SHIFT_MASK) != 0 
                           || formDesigner.getTopDesignComponent() == metacont ?
                    metacont : metacont.getParentContainer();
            }

            // old layout component dragger requires coordinates related to HandleLayer
            for (int i=0; i < originalBounds.length; i++) {
                originalBounds[i].x += convertPoint.x;
                originalBounds[i].y += convertPoint.y;
            }
            oldDragger = new ComponentDragger(
                formDesigner,
                HandleLayer.this,
                movingComponents,
                originalBounds,
                new Point(hotSpot.x + convertPoint.x, hotSpot.y + convertPoint.y),
                fixedTarget);

            newDrag = oldDrag = true;

            super.init();
        }

        boolean end(Point p, int modifiers) {    
            RADVisualContainer originalCont = getSourceContainer();
            if (p != null) {
                if (targetContainer == null || targetContainer.getLayoutSupport() != null) {
                    // dropped in old layout support, or on non-visual area
                    createLayoutUndoableEdit();
                    formDesigner.getLayoutDesigner().removeDraggedComponents();
                    oldDragger.dropComponents(p);
                    placeLayoutUndoableEdit();
                }
                else { // dropped in new layout support
                    if (targetContainer != originalCont) {
                        for (int i=0; i < movingComponents.length; i++) {
                            getFormModel().removeComponent(movingComponents[i], false);
                            getFormModel().addComponent(movingComponents[i], targetContainer);
                        }
                    }
                    createLayoutUndoableEdit();
                    formDesigner.getLayoutDesigner().endMoving(true);
                    getFormModel().fireContainerLayoutChanged(targetContainer, null, null, null);
                    placeLayoutUndoableEdit();
                }
            }
            else { // canceled
                formDesigner.getLayoutDesigner().endMoving(false);
                formDesigner.updateContainerLayout(originalCont, false);
            }

            return true;
        }

        void oldMove(Point p) {
            oldDragger.drag(p);
        }

        void oldPaintFeedback(Graphics2D g, Graphics gg) {
            oldDragger.paintDragFeedback(g);

            // don't paint if component dragged from old layout (may have strange size)
            Component comp = showingComponents[0];
            if (!isOldLayoutSource()) {
                if (comp instanceof JComponent)
                    comp.paint(gg);
                else
                    comp.getPeer().paint(gg);
            }
        }
    }

    // for resizing existing components
    private class ResizeComponentDrag extends ComponentDrag {
        private int resizeType;

        private ComponentDragger oldDragger; // drags components in the old layout support

        ResizeComponentDrag(RADVisualComponent[] comps,
                            Point hotspot, // in HandleLayer coordinates
                            int resizeType)
        {
            super(comps, hotspot);
            this.resizeType = resizeType;
            init();
        }

        void init() {
            RADVisualContainer metacont = getSourceContainer();
            if (isTopComponent()) {
                newDrag = getLayoutModel().getLayoutComponent(movingComponents[0].getId()) != null;
                fixedTarget = null;
            }
            else if (metacont != null) {
                if (metacont.getLayoutSupport() == null) {
                    newDrag = true;
                }
                else {
                    oldDrag = true;
                }
                fixedTarget = metacont;
            }

            if (newDrag) { // new layout support
                String[] compIds = new String[showingComponents.length];
                for (int i=0; i < showingComponents.length; i++) {
                    compIds[i] = movingComponents[i].getId();
                    originalBounds[i].x -= convertPoint.x;
                    originalBounds[i].y -= convertPoint.y;
                }

                int[] res = new int[2];
                int horiz = resizeType & (LayoutSupportManager.RESIZE_LEFT
                                          | LayoutSupportManager.RESIZE_RIGHT);
                if (horiz == LayoutSupportManager.RESIZE_LEFT) {
                    res[LayoutConstants.HORIZONTAL] = LayoutConstants.LEADING;
                }
                else if (horiz == LayoutSupportManager.RESIZE_RIGHT) {
                    res[LayoutConstants.HORIZONTAL] = LayoutConstants.TRAILING;
                }
                else {
                    res[LayoutConstants.HORIZONTAL] = LayoutConstants.DEFAULT;
                }
                int vert = resizeType & (LayoutSupportManager.RESIZE_UP
                                          | LayoutSupportManager.RESIZE_DOWN);
                if (vert == LayoutSupportManager.RESIZE_UP) {
                    res[LayoutConstants.VERTICAL] = LayoutConstants.LEADING;
                }
                else if (vert == LayoutSupportManager.RESIZE_DOWN) {
                    res[LayoutConstants.VERTICAL] = LayoutConstants.TRAILING;
                }
                else {
                    res[LayoutConstants.VERTICAL] = LayoutConstants.DEFAULT;
                }

                formDesigner.getLayoutDesigner().startResizing(
                    compIds, originalBounds, hotSpot, res);

                // convert back to HandleLayer
                for (int i=0; i < originalBounds.length; i++) {
                    originalBounds[i].x += convertPoint.x;
                    originalBounds[i].y += convertPoint.y;
                }
            }
            else if (oldDrag) { // old layout support
                oldDragger = new ComponentDragger(
                    formDesigner,
                    HandleLayer.this,
                    movingComponents,
                    originalBounds,
                    new Point(hotSpot.x + convertPoint.x, hotSpot.y + convertPoint.y),
                    resizeType);
            }

            super.init();
        }

        boolean end(Point p, int modifiers) {
            if (p != null) {
                if (newDrag) { // new layout support
                    // make sure the visual component has the current size set 
                    // (as still being in its container the layout manager tries to
                    // restore the original size)
                    showingComponents[0].setSize(movingBounds[0].width, movingBounds[0].height);
                    doLayout(showingComponents[0]);

                    createLayoutUndoableEdit();
                    formDesigner.getLayoutDesigner().endMoving(true);
                    for (int i=0; i < movingComponents.length; i++) {
                        RADVisualComponent metacomp = movingComponents[i];
                        if (metacomp instanceof RADVisualContainer) {
                            LayoutComponent layoutComp =
                                    getLayoutModel().getLayoutComponent(metacomp.getId());
                            if (layoutComp.isLayoutContainer()) {
                                getFormModel().fireContainerLayoutChanged(
                                    (RADVisualContainer)metacomp, null, null, null);
                            }
                        }
                    }
                    if (targetContainer != null) {
                        getFormModel().fireContainerLayoutChanged(targetContainer, null, null, null);
                    }
                    placeLayoutUndoableEdit();
                }
                else { // old layout support
                    if (targetContainer != null) {
                        oldDragger.dropComponents(p);
                    }
                }
                if (isTopComponent()) {
                    Dimension size = new Dimension(movingBounds[0].width, movingBounds[0].height);
                    formDesigner.setStoredDesignerSize(size);
                }
            }
            else { // resizing canceled
                formDesigner.getLayoutDesigner().endMoving(false);
                if (isTopComponent()) {
                    Dimension prevSize = formDesigner.getStoredDesignerSize();
                    if (!formDesigner.getComponentLayer().getDesignerSize().equals(prevSize)) {
                        // restore the previous designer size 	 
                        formDesigner.getComponentLayer().setDesignerSize(prevSize);
                    }
                    formDesigner.updateContainerLayout((RADVisualContainer)formDesigner.getTopDesignComponent(), false);
                }
                else {
                    formDesigner.updateContainerLayout(getSourceContainer(), false);
                }
            }
            return true;
        }

        void move(Point p, int modifiers) {
            if (isTopComponent()) {
                if (newDrag) {
                    p.x -= convertPoint.x;
                    p.y -= convertPoint.y;
                    formDesigner.getLayoutDesigner().move(p,
                                                          null,
                                                          ((modifiers & InputEvent.ALT_MASK) == 0),
                                                          ((modifiers & InputEvent.CTRL_MASK) != 0),
                                                          movingBounds);
                    showingComponents[0].setSize(movingBounds[0].width, movingBounds[0].height);
                }
                else {
                    Rectangle r = formDesigner.getComponentLayer().getDesignerInnerBounds();
                    int w = r.width;
                    int h = r.height;
                    if ((resizeType & LayoutSupportManager.RESIZE_DOWN) != 0) {
                        h = p.y - r.y;
                        if (h < 0)
                            h = 0;
                    }
                    if ((resizeType & LayoutSupportManager.RESIZE_RIGHT) != 0) {
                        w = p.x - r.x;
                        if (w < 0)
                            w = 0;
                    }
                    movingBounds[0].width = w;
                    movingBounds[0].height = h;
                }
                Dimension size = new Dimension(movingBounds[0].width, movingBounds[0].height);
                formDesigner.getComponentLayer().setDesignerSize(size);
                doLayout(formDesigner.getComponentLayer());
            }
            else {
                super.move(p, modifiers);
            }
        }

        void oldMove(Point p) {
            oldDragger.drag(p);
        }

        void oldPaintFeedback(Graphics2D g, Graphics gg) {
            oldDragger.paintDragFeedback(g);
        }
    }

    // for moving a component being newly added
    private class NewComponentDrag extends ComponentDrag {
        private PaletteItem paletteItem;
        RADComponent addedComponent;

        private int index = - 1; // for the old layout support
        private LayoutConstraints constraints; // for the old layout support

        NewComponentDrag(PaletteItem paletteItem) {
            super();
            this.paletteItem = paletteItem;
            showingComponents = new Component[1];
            init();
        }

        void init() { // can be re-inited
            RADVisualComponent precreated =
                getComponentCreator().precreateVisualComponent(
                    paletteItem.getComponentClassSource());

            if (precreated != null) {
                if (movingComponents == null) {
                    movingComponents = new RADVisualComponent[1];
                }
                movingComponents[0] = precreated;
                LayoutComponent[] layoutComponents = new LayoutComponent[] {
                        getComponentCreator().getPrecreatedLayoutComponent() };

                showingComponents[0] = (Component) precreated.getBeanInstance();
                // Force creation of peer - AWT components don't have preferred size otherwise
                if (!(showingComponents[0] instanceof JComponent)) {
                    FakePeerSupport.attachFakePeer(showingComponents[0]);
                    if (showingComponents[0] instanceof Container) {
                        FakePeerSupport.attachFakePeerRecursively((Container)showingComponents[0]);
                    }
                }
                Dimension size = showingComponents[0].getPreferredSize();
                hotSpot = new Point(size.width/2 - 4, size.height/2);
                if (hotSpot.x < 0) {
                    hotSpot.x = 0;
                }
                if (originalBounds == null) {
                    originalBounds = new Rectangle[] { new Rectangle(convertPoint.x, convertPoint.y, size.width, size.height) };
                    showingComponents[0].setBounds(originalBounds[0]);
                    compoundBounds = originalBounds[0];
                    movingBounds = new Rectangle[] { new Rectangle(0, 0, size.width, size.height) };
                }

                if (formDesigner.getLayoutDesigner() != null) {
                    formDesigner.getLayoutDesigner().startAdding(
                            layoutComponents,
                            new Rectangle[] { new Rectangle(0, 0, size.width, size.height) },
                            hotSpot);
                }

                newDrag = oldDrag = true;
            }
            else { // non-visual component - present it as icon
                showingComponents[0] = new JLabel(
                    new ImageIcon(paletteItem.getNode().getIcon(
                        java.beans.BeanInfo.ICON_COLOR_16x16)));
                Dimension dim = showingComponents[0].getPreferredSize();
                hotSpot = new Point(dim.width/2, dim.height/2);
                if (hotSpot.x < 0) {
                    hotSpot.x = 0;
                }
                originalBounds = new Rectangle[] { new Rectangle(convertPoint.x, convertPoint.y, dim.width, dim.height) };
                showingComponents[0].setBounds(originalBounds[0]);
                movingBounds = new Rectangle[] { showingComponents[0].getBounds() };

                newDrag = oldDrag = false;
            }

            super.init();
        }

        boolean end(Point p, int modifiers) {
            if (p != null) {
                targetContainer = getTargetContainer(p, modifiers);

                boolean newLayout;
                boolean oldLayout;
                if (targetContainer != null && movingComponents != null) {
                    newLayout = targetContainer.getLayoutSupport() == null;
                    oldLayout = !newLayout;
                }
                else newLayout = oldLayout = false;

                Object constraints;
                if (oldLayout) {
                    Point point = new Point(p.x - hotSpot.x, p.y - hotSpot.y);
                    constraints = !paletteItem.isMenu() && paletteItem.isVisual() ?
                        getConstraintsAtPoint(targetContainer, point) : null;
                }
                else constraints = null;

                if (movingComponents != null) { // there is a precreated visual component
                    addedComponent = movingComponents[0];
                    if (getLayoutModel() != null) { // Some beans don't have layout
                        createLayoutUndoableEdit();
                        formDesigner.getLayoutDesigner().endMoving(newLayout);
                        LayoutComponent layoutComponent =
                                getComponentCreator().getPrecreatedLayoutComponent();
                        if (layoutComponent.isLayoutContainer()) {
                            if (!newLayout) { // always add layout container to the model 
                                getLayoutModel().addRootComponent(layoutComponent);
                            }
                        }
                        placeLayoutUndoableEdit();
                    }
                    getComponentCreator().addPrecreatedComponent(
                                            targetContainer, constraints);
                }
                else { // component not precreated ...
                    addedComponent = getComponentCreator().createComponent(
                            paletteItem.getComponentClassSource(),
                            targetContainer,
                            constraints);
                    // ... but it still can be a visual container - e.g. JFrame
                    if (addedComponent instanceof RADVisualContainer
                        && ((RADVisualContainer)addedComponent).getLayoutSupport() == null)
                    {
                        createLayoutUndoableEdit();
                        getLayoutModel().addRootComponent(new LayoutComponent(addedComponent.getId(), true));
                        placeLayoutUndoableEdit();
                    }
                }

                if (addedComponent.getBeanInfo().getBeanDescriptor().getValue("customizeOnCreation") != null) { // NOI18N
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            RADComponentNode node = addedComponent.getNodeReference();
                            if (node.hasCustomizer()) {
                                org.openide.nodes.NodeOperation.getDefault().customize(node);
                            }
                        }
                    });
                }
                else if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
//                    init();
                    return false;
                }
            }
            else {
                if (formDesigner.getLayoutDesigner() != null) {
                    formDesigner.getLayoutDesigner().endMoving(false);
                }
                getComponentCreator().releasePrecreatedComponent();
            }
            return true;
        }

        void oldMove(Point p) {
            LayoutSupportManager laysup = targetContainer.getLayoutSupport();
            Container cont = (Container) formDesigner.getComponent(targetContainer);
            Container contDel = targetContainer.getContainerDelegate(cont);
            Point posInCont = convertPointToComponent(p.x, p.y, cont);
            Point posInComp = hotSpot;
            index = laysup.getNewIndex(cont, contDel,
                                       showingComponents[0], -1,
                                       posInCont, posInComp);
            constraints = laysup.getNewConstraints(cont, contDel,
                                                   showingComponents[0], -1,
                                                   posInCont, posInComp);
        }

        void oldPaintFeedback(Graphics2D g, Graphics gg) {
            LayoutSupportManager laysup = targetContainer.getLayoutSupport();
            Container cont = (Container) formDesigner.getComponent(targetContainer);
            Container contDel = targetContainer.getContainerDelegate(cont);
            Point contPos = convertPointFromComponent(0, 0, contDel);
            g.setColor(formSettings.getSelectionBorderColor());
            g.setStroke(ComponentDragger.dashedStroke1);
            g.translate(contPos.x, contPos.y);
            laysup.paintDragFeedback(cont, contDel,
                                     showingComponents[0],
                                     constraints, index,
                                     g);
            g.translate(-contPos.x, -contPos.y);
//                    g.setStroke(stroke);
            if (showingComponents[0] instanceof JComponent)
                showingComponents[0].paint(gg);
            else
                showingComponents[0].getPeer().paint(gg);
        }
    }
    
    private class NewComponentDropListener implements DropTargetListener {
        
        public void dragEnter(DropTargetDragEvent dtde) {
            try {
                DropTargetContext context = dtde.getDropTargetContext();
                // getTransferable() method on DropTargetDragEvent should be used
                // when it is possible to drop support of JDK 1.4
                java.lang.reflect.Method method = context.getClass().getDeclaredMethod("getTransferable", new Class[0]); // NOI18N
                method.setAccessible(true);
                Transferable transferable = (Transferable)method.invoke(context, new Object[0]);
                Node node = NodeTransfer.node(transferable, NodeTransfer.DND_COPY);
                if (node == null) return;
                NewComponentDrop newComponentDrop = (NewComponentDrop)node.getCookie(NewComponentDrop.class);
                if (newComponentDrop != null) {
                    PaletteItem item = newComponentDrop.getPaletteItem();
                    if (item != null) {
                        draggedComponent = new NewComponentDrag(item);
                        draggedComponent.move(dtde.getLocation(), 0);
                        repaint();                    
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {
            if (draggedComponent != null) {
                draggedComponent.move(dtde.getLocation(), 0);
                repaint();
            }
        }
        
        public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dtde) {
        }
        
        public void dragExit(java.awt.dnd.DropTargetEvent dte) {
            if (draggedComponent != null) {
                endDragging(null);
                repaint();
            }
        }
        
        public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
            if (draggedComponent != null) {
                Node node = NodeTransfer.node(dtde.getTransferable(), NodeTransfer.DND_COPY);
                NewComponentDrag newComponentDrag = ((NewComponentDrag)draggedComponent);
                newComponentDrag.end(dtde.getLocation(), 0);
                String id = newComponentDrag.addedComponent.getId();
                draggedComponent = null;
                draggingEnded = true;
                NewComponentDrop newComponentDrop = (NewComponentDrop)node.getCookie(NewComponentDrop.class);
                newComponentDrop.componentAdded(getFormModel(), id);
            }
        }

    }
    
}
