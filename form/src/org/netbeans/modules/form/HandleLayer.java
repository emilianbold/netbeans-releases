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
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.text.MessageFormat;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;

import org.netbeans.modules.form.palette.CPManager;
import org.netbeans.modules.form.palette.PaletteItem;

import org.netbeans.modules.form.layoutsupport.*;

/**
 * A transparent layer (glass pane) handling user operations in designer (mouse
 * and keyboard events) and painting selection and drag&drop feedback.
 * Technically, this is a layer in FormDesigner, placed over ComponentLayer.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

class HandleLayer extends JPanel
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

    private ComponentDragger componentDragger;
    private Point lastLeftMousePoint;
    private Point prevLeftMousePoint;
    private boolean draggingCanceled = false;
    private int resizeType;

    private SelectionDragger selectionDragger;

    private DesignerResizer designerResizer;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormEditor.getFormSettings();


    HandleLayer(FormDesigner fd) {
        formDesigner = fd;
        addMouseListener(new HandleLayerMouseListener());
        addMouseMotionListener(new HandleLayerMouseMotionListener());
//        setNextFocusableComponent(this);
        setLayout(null);

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
    }

    void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke(formSettings.getSelectionBorderSize()));

        RADComponent conSource = formDesigner.getConnectionSource();
        RADComponent conTarget = formDesigner.getConnectionTarget();
        if (conSource != null || conTarget != null) {
            // paint connection
            g2.setColor(formSettings.getConnectionBorderColor());
            if (conSource != null)
                paintSelection(g2, conSource);
            if (conTarget != null)
                paintSelection(g2, conTarget);
        }
        else {
            // paint selection
            g2.setColor(formSettings.getSelectionBorderColor());
            Iterator metacomps = formDesigner.getSelectedComponents().iterator();
            while (metacomps.hasNext()) {
                paintSelection(g2, (RADComponent) metacomps.next());
            }
        }

        if (componentDragger != null)
            componentDragger.paintDragFeedback(g2);
        if (selectionDragger != null)
            selectionDragger.paintDragFeedback(g2);
    }

    private void paintSelection(Graphics2D g, RADComponent metacomp) {
        Object comp = formDesigner.getComponent(metacomp);
        if (!(comp instanceof Component))
            return;

        Component component = (Component) comp;
        Component parent = component.getParent();

        if (parent != null && component.isShowing()) {
            Rectangle rect = component.getBounds();
            rect = SwingUtilities.convertRectangle(parent, rect, this);

            Rectangle parentRect = new Rectangle(new Point(0,0),
                                                 parent.getSize());
            parentRect = SwingUtilities.convertRectangle(parent,
                                                         parentRect,
                                                         this);

            Rectangle2D selRect = rect.createIntersection(parentRect);

            int correction = formSettings.getSelectionBorderSize() % 2;
            g.draw(new Rectangle2D.Double(
                selRect.getX() - correction,
                selRect.getY() - correction,
                selRect.getWidth() + correction,
                selRect.getHeight() + correction));
        }
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
            }
        }
        else if (keyCode == KeyEvent.VK_SPACE) {
            if (!viewOnly && e.getID() == KeyEvent.KEY_RELEASED) {
                Iterator it = formDesigner.getSelectedComponents().iterator();
                if (it.hasNext()) {
                    RADComponent comp = (RADComponent)it.next();
                    if (!it.hasNext()) { // just one component is selected
                        if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT) {
                            // in selection mode SPACE starts in-place editing
                            formDesigner.startInPlaceEditing(comp);
                        }
                        else if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
                            // in add mode SPACE adds selected item as component
                            PaletteItem item = CPManager.getDefault().getSelectedItem();
                            if (item != null) {
                                formDesigner.getModel().getComponentCreator()
                                    .createComponent(item.getInstanceCookie(),
                                                     comp, null);
                                formDesigner.toggleSelectionMode();
                            }
                        }
                    }
                }
            }
            e.consume();
        }
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            if (endDragging(null))
                e.consume();
        }
        else if (keyCode == KeyEvent.VK_F10) {
            if (e.isShiftDown()) {
                Point p = null;
                Iterator it = formDesigner.getSelectedComponents().iterator();
                if (it.hasNext()) {
                    RADComponent metacomp = (RADComponent)it.next();
                    Object sel = (Component) formDesigner.getComponent(metacomp);
                    if (sel instanceof Component) {
                        Component comp = (Component) sel;
                        p = SwingUtilities.convertPoint(comp.getParent(),
                                                        comp.getLocation(),
                                                        this);
                    }
                    else p = new Point(0,0);

                    showContextMenu(p);
                    e.consume();
                }
            }
        }
        else {
            super.processKeyEvent(e);
        }
    }

    public boolean isFocusTraversable() {
        return true;
    }

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

    RADVisualContainer getMetaContainerAt(Point point) {
        RADComponent metacomp = getMetaComponentAt(point, COMP_DEEPEST);
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

        if (e.isControlDown()) {
            // Control is pressed - add component to selection
            if (hitMetaComp != null)
                if (formDesigner.isComponentSelected(hitMetaComp))
                    formDesigner.removeComponentFromSelection(hitMetaComp);
                else
                    formDesigner.addComponentToSelection(hitMetaComp);
        }
        else if (e.isShiftDown() && !e.isAltDown()) {
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
        }
        else { // no reasonable modifier key pressed - select single component
            if (hitMetaComp != null) {
                if (!formDesigner.isComponentSelected(hitMetaComp))
                    formDesigner.setSelectedComponent(hitMetaComp);
            }
            else formDesigner.clearSelection();
        }

        repaint();

        return hitMetaComp;
    }

    private RADComponent[] getComponentsIntervalToSelect(
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
    }

    private void selectOtherComponentsNode() {
        FormEditorSupport fes = formDesigner.getFormEditorSupport();
        ComponentInspector ci = ComponentInspector.getInstance();
        Node[] selectedNode = new Node[] {
            ((FormRootNode)fes.getFormRootNode()).getOthersNode() };
        
        try {
            ci.setSelectedNodes(selectedNode, fes);
            formDesigner.clearSelectionImpl();
            repaint();
        }
        catch (java.beans.PropertyVetoException ex) {
            ex.printStackTrace();
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

        Node node = metacomp.getNodeReference();
        if (node != null) {
            Action action = node.getPreferredAction();
            if (action != null) {// && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(
                        node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            }
        }

        return true;
    }

    private void processMouseClickInLayoutSupport(RADComponent metacomp,
                                                  MouseEvent e)
    {
        if (!(metacomp instanceof RADVisualComponent))
            return;

        RADVisualContainer metacont;
        if (metacomp instanceof RADVisualContainer)
            metacont = (RADVisualContainer) metacomp;
        else {
            metacont = (RADVisualContainer) metacomp.getParentComponent();
            if (metacont == null)
                return;
        }

        Container cont = (Container) formDesigner.getComponent(metacont);
        Container contDelegate = metacont.getContainerDelegate(cont);
        Point p = SwingUtilities.convertPoint(HandleLayer.this,
                                              e.getPoint(),
                                              contDelegate);
        metacont.getLayoutSupport().processMouseClick(p, cont, contDelegate);
    }

    private void showContextMenu(Point popupPos) {
        ComponentInspector inspector = ComponentInspector.getInstance();
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated != formDesigner && activated != inspector)
            return;

        formDesigner.componentActivated(); // just for sure...

        Node[] selectedNodes = inspector.getSelectedNodes();
        JPopupMenu popup = NodeOp.findContextMenu(selectedNodes);
        if (popup != null) {
            popup.show(HandleLayer.this, popupPos.x, popupPos.y);
        }
    }

    // --------

    boolean anyDragger() {
        return componentDragger != null
               || selectionDragger != null
               || designerResizer != null;
    }

    private ComponentDragger createComponentDragger(Point hotspot,
                                                    int modifiers)
    {
        java.util.List selectedComponents = formDesigner.getSelectedComponents();
        if (selectedComponents.size() == 0)
            return null;

        // all selected components must be visible in the designer
        java.util.List selComps = new ArrayList(selectedComponents.size());
        Iterator iter = selectedComponents.iterator();
        while (iter.hasNext()) {
            RADComponent metacomp = (RADComponent) iter.next();
            if (metacomp instanceof RADVisualComponent)
                if (metacomp != formDesigner.getTopDesignComponent())
                    selComps.add(metacomp);
                else return null;
        }

        // remove selected components contained in another selected components
        Set children = new HashSet();
        iter = selComps.iterator();
        while (iter.hasNext()) {
            RADComponent metacomp = (RADComponent) iter.next();
            Iterator iter2 = selComps.iterator();
            while (iter2.hasNext()) {
                RADComponent metacomp2 = (RADComponent) iter2.next();
                if (metacomp2 != metacomp
                        && metacomp.isParentComponent(metacomp2))
                    children.add(metacomp2);
            }
        }
        selComps.removeAll(children);
        if (selComps.isEmpty())
            return null;

        RADVisualComponent[] comps = new RADVisualComponent[selComps.size()];
        selComps.toArray(comps);

        if (resizeType == 0) { // dragging
            RADVisualContainer fixedTargetContainer = null;

            if ((modifiers & InputEvent.ALT_MASK) != 0) { // restricted dragging
                RADVisualContainer parent = comps[0].getParentContainer();
                if ((modifiers & InputEvent.SHIFT_MASK) != 0
                    || formDesigner.getTopDesignComponent() == parent)
                {   // restrict dragging only to the parent container
                    // (dragging within the same container)
                    for (int i=1; i < comps.length; i++)
                        if (comps[i].getParentContainer() != parent) {
                            parent = null; // not the same parent
                            break;
                        }
                    fixedTargetContainer = parent;
                }
                else if ((parent = parent.getParentContainer()) != null) {
                    // restrict dragging only to the parent of the parent
                    // container (dragging one level up)
                    for (int i=1; i < comps.length; i++)
                        if (comps[i].getParentContainer().getParentContainer()
                                != parent)
                        {
                            parent = null;
                            break;
                        }
                    fixedTargetContainer = parent;
                }
            }

            return new ComponentDragger(formDesigner, this,
                                        comps, hotspot, fixedTargetContainer);
        }
        else // resizing
            return new ComponentDragger(formDesigner, this,
                                        comps, hotspot, resizeType);
    }

    private boolean endDragging(Point commitPosition) {
        if (anyDragger()) {
            if (resizeType != 0) {
                resizeType = 0;
                Cursor cursor = getCursor();
                if (cursor != null && cursor.getType() != Cursor.DEFAULT_CURSOR)
                    setCursor(Cursor.getDefaultCursor());
                if (getToolTipText() != null)
                    setToolTipText(null);
            }

            if (designerResizer != null) {
                if (commitPosition != null)
                    designerResizer.drop(commitPosition);
                else {
                    Dimension prevSize = formDesigner.getStoredDesignerSize();
                    if (!formDesigner.getComponentLayer().getDesignerSize()
                            .equals(prevSize))
                    {   // restore the previous designer size
                        formDesigner.getComponentLayer()
                                      .updateDesignerSize(prevSize);
                    }
                }
                designerResizer = null;
            }

            if (componentDragger != null) {
                if (commitPosition != null)
                    componentDragger.dropComponents(commitPosition);
                componentDragger = null;
                repaint();
            }

            if (selectionDragger != null) {
                if (commitPosition != null)
                    selectionDragger.drop(commitPosition);
                selectionDragger = null;
                repaint();
            }

            draggingCanceled = commitPosition == null;
            return true;
        }

        return false;
    }

    // Check the mouse cursor if it is at position where a component or the
    // designer can be resized. Change mouse cursor accordingly.
    private void checkResizing(MouseEvent e) {
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
        if (e.isAltDown() || e.isControlDown() || e.isShiftDown()) {
            resizeType = 0;
            return resizeType;
        }

        // check whether all selected components are in the same container
        RADComponent parent = null;
        Iterator selected = formDesigner.getSelectedComponents().iterator();
        while (selected.hasNext()) {
            RADComponent comp = (RADComponent) selected.next();
            if (comp instanceof RADVisualComponent) {
                if (parent == null) {
                    parent = comp.getParentComponent();
                    if (parent == null)
                        return 0; // component without a parent cannot be resized
                }
                else if (comp.getParentComponent() != parent)
                    return 0; // selected components are not in the same container
            }
        }

        Point p = e.getPoint();
        RADComponent compAtPoint = getMetaComponentAt(p, COMP_SELECTED);
        if (compAtPoint instanceof RADVisualComponent) {
            RADVisualComponent metacomp = (RADVisualComponent) compAtPoint;
            if (!formDesigner.isComponentSelected(metacomp)) {
                int resizing = 0;
                RADVisualComponent[] otherComps;
                if (metacomp instanceof RADVisualContainer)
                    otherComps = ((RADVisualContainer)metacomp).getSubComponents();
                else {
                    RADVisualContainer metacont = metacomp.getParentContainer();
                    if (metacont != null)
                        otherComps = metacont.getSubComponents();
                    else return 0; // component without a parent
                }

                for (int i=0; i < otherComps.length; i++) {
                    metacomp = otherComps[i];
                    resizing = getComponentResizable(p, metacomp);
                    if (resizing != 0)
                        break;
                }
                resizeType = resizing;
            }
            else resizeType = getComponentResizable(p, metacomp);
        }
        else resizeType = 0;

        return resizeType;
    }

    // Check how possible component resizing (obtained from layout support)
    // matches with mouse position on component selection border. 
    private int getComponentResizable(Point p, RADVisualComponent metacomp) {
        if (!formDesigner.isComponentSelected(metacomp))
            return 0;

        RADVisualContainer metacont = (RADVisualContainer)
                                      metacomp.getParentComponent();
        if (metacont == null
                || metacomp == formDesigner.getTopDesignComponent())
            return 0;

        LayoutSupportManager laySup = metacont.getLayoutSupport();
        if (laySup == null)
            return 0;

        Container cont = (Container) formDesigner.getComponent(metacont);
        Container contDel = metacont.getContainerDelegate(cont);
        Component comp = (Component) formDesigner.getComponent(metacomp);

        int resizable = laySup.getResizableDirections(
                                   cont, contDel,
                                   comp, metacont.getIndexOf(metacomp));
        if (resizable != 0)
            resizable &= getSelectionResizable(p, comp, 2);

        return resizable;
    }

    // Compute possible resizing directions according to mouse position on
    // component selection border.
    private int getSelectionResizable(Point p, Component comp, int borderWidth) {
        if (comp == null)
            return 0;

        Rectangle bounds = comp.getBounds();
        bounds.x = 0;
        bounds.y = 0;
        bounds = SwingUtilities.convertRectangle(comp, bounds, this);

        Rectangle r1 = new Rectangle(bounds);
        Rectangle r2 = new Rectangle(bounds);

        r1.grow(borderWidth, borderWidth);
        r2.grow(-3, -3);
        if (r2.width < 0)
            r2.width = 0;
        if (r2.height < 0)
            r2.height = 0;

        int resizable = 0;
        if (r1.contains(p)) {
            if (p.y >= r2.y + r2.height)
                resizable |= LayoutSupportManager.RESIZE_DOWN;
            else if (p.y < r2.y)
                resizable |= LayoutSupportManager.RESIZE_UP;
            if (p.x >= r2.x + r2.width)
                resizable |= LayoutSupportManager.RESIZE_RIGHT;
            else if (p.x < r2.x)
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
                        formDesigner.getComponentLayer().updateDesignerSize(size);
                        setToolTipText(null);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
                catch (NumberFormatException ex) {} // silently ignore, do nothing
            }
        }
    }

    private LayoutConstraints getConstraintsAtPoint(RADComponent metacomp,
                                                    Point point)
    {
        if (!(metacomp instanceof RADVisualComponent))
            return null;

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            (RADVisualContainer) metacomp.getParentComponent();
        if (metacont == null)
            return null;

        Container cont = (Container) formDesigner.getComponent(metacont);
        Container contDel = metacont.getContainerDelegate(cont);
        Point p = SwingUtilities.convertPoint(this, point, contDel);
        return metacont.getLayoutSupport().getNewConstraints(cont, contDel,
                                                             null, -1,
                                                             p, null);
    }

    private void showAddHint(RADComponent metacomp, Point p, PaletteItem item) {
        if ((!(metacomp instanceof RADVisualComponent) && metacomp != null)
            || item == null || item.getItemClass() == null)
        {
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
            return;
        }

        if (metacomp == null) {
            setStatusText("FMT_MSG_AddToOthers", // NOI18N
                          new Object[] { item.getDisplayName() });
            return;
        }

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            (RADVisualContainer) metacomp.getParentComponent();

        if (item.isLayout()) {
            if (metacont != null) {
                if (!metacont.getLayoutSupport().isDedicated())
                    setStatusText("FMT_MSG_SetLayout", // NOI18N
                                  new Object[] { item.getDisplayName(),
                                                 metacont.getName() });
                else
                    setStatusText("FMT_MSG_CannotSetLayout", // NOI18N
                                  new Object[] { metacont.getName() });
            }
            else
                setStatusText("FMT_MSG_CannotSetLayout", // NOI18N
                              new Object[] { metacomp.getName() });
        }
        else if (item.isBorder()) {
            if (JComponent.class.isAssignableFrom(metacomp.getBeanClass()))
                setStatusText("FMT_MSG_SetBorder", // NOI18N
                              new Object[] { item.getDisplayName(),
                                             metacomp.getName() });
            else
                setStatusText("FMT_MSG_CannotSetBorder", // NOI18N
                              new Object[] { metacomp.getName() });
        }
        else if (metacont != null
                 && ((item.isMenu()
                        && metacont.getContainerMenu() == null
                        && metacont.canHaveMenu(item.getItemClass()))
                     || (item.isVisual() && !item.isMenu())))
        {
            setStatusText("FMT_MSG_AddComponent", // NOI18N
                          new Object[] { item.getDisplayName(),
                                         metacont.getName() });
        }
        else {
            setStatusText("FMT_MSG_AddToOthers", // NOI18N
                          new Object[] { item.getDisplayName() });
        }
    }

    private static void setStatusText(String formatId, Object[] args) {
        StatusDisplayer.getDefault().setStatusText(
            FormUtils.getFormattedBundleString(formatId, args));
    }
    
    boolean mouseOnVisual(Point p) {
        Rectangle bounds = formDesigner.getComponentLayer().getDesignerBounds();
        return bounds.contains(p);
    }

    // ---------

    private class HandleLayerMouseListener implements MouseListener
    {
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) && !anyDragger())
                if (!draggingCanceled)
                    showContextMenu(e.getPoint());
                else
                    draggingCanceled = false;

            e.consume();
        }

        public void mouseReleased(MouseEvent e) {
            if (!HandleLayer.this.isVisible())
                return;

            if (SwingUtilities.isLeftMouseButton(e)) {
                if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT
                    && !endDragging(e.getPoint()))
                {
                    if (draggingCanceled) {
                        draggingCanceled = false;
                    }
                    else if ((resizeType & DESIGNER_RESIZING) != 0
                             && e.getClickCount() == 2
                             && !e.isShiftDown()
                             && !e.isControlDown()
                             && !e.isAltDown())
                    {   // doubleclick on designer's resizing border
                        setUserDesignerSize();
                    }
                    else if (prevLeftMousePoint != null
                             && e.getClickCount() == 1
                             && prevLeftMousePoint.distance(e.getPoint()) <= 2
                             && !e.isShiftDown()
                             && !e.isControlDown()
                             && !e.isAltDown())
                    {   // second click on the same place in a component
                        formDesigner.startInPlaceEditing(
                            getMetaComponentAt(e.getPoint(), COMP_SELECTED));
                    }
                    else if (e.getClickCount() == 1
                             && !e.isAltDown()
                             && !e.isControlDown()
                             && e.isShiftDown())
                    {   // Shift + mouse release - interval selection
                        selectComponent(e);
                    }
                }

                prevLeftMousePoint = lastLeftMousePoint;
                lastLeftMousePoint = null;
            }

            e.consume();
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        }

        public void mousePressed(MouseEvent e) {
            formDesigner.componentActivated();
            if (!HandleLayer.this.isVisible())
                return;

            if (SwingUtilities.isRightMouseButton(e)) {
                if (!anyDragger()) {
                    if (!mouseOnVisual(e.getPoint()))
                        selectOtherComponentsNode();
                    else {
                        RADComponent hitMetaComp =
                            getMetaComponentAt(e.getPoint(), COMP_SELECTED);
                        if (hitMetaComp != null
                            && !formDesigner.isComponentSelected(hitMetaComp))
                        {
                            formDesigner.setSelectedComponent(hitMetaComp);
                        }
                    }
                }
                else endDragging(null);

                e.consume();
            }
            else if (SwingUtilities.isLeftMouseButton(e)) {
                lastLeftMousePoint = e.getPoint();

                boolean modifier = e.isControlDown() || e.isAltDown() || e.isShiftDown();

                if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT) {
                    checkResizing(e);

                    if (!e.isShiftDown() || e.isAltDown() || e.isControlDown()) {
                        // mouse not pressed with Shift only (reserved for
                        // interval or area selection, applied on mouse release
                        // or mouse dragged)
                        if (designerResizer == null && !modifier
                            && (resizeType & DESIGNER_RESIZING) != 0)
                        {   // start designer resizing
                            if (e.getClickCount() != 2)
                                designerResizer = new DesignerResizer();
                        }
                        else if (!mouseOnVisual(lastLeftMousePoint)) {
                            if (resizeType == 0)
                                selectOtherComponentsNode();
                        }
                        else if (resizeType == 0
                                 && (e.getClickCount() != 2
                                     || !processDoubleClick(e)))
                        {   // no resizing, no doubleclick - select component
                            RADComponent hitMetaComp = selectComponent(e);
                            if (hitMetaComp != null && !modifier) // plain single click
                                processMouseClickInLayoutSupport(hitMetaComp, e);
                        }
                    }
                    draggingCanceled = false;
                }
                else if (!viewOnly) {
                    if (formDesigner.getDesignerMode() == FormDesigner.MODE_CONNECT) {
                        selectComponent(e);
                    }
                    else if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
                        RADComponent hitMetaComp = getMetaComponentAt(
                            lastLeftMousePoint,
                            e.isAltDown() ? COMP_SELECTED : COMP_DEEPEST);

                        PaletteItem item = CPManager.getDefault().getSelectedItem();
                        if (item != null) {
                            Object constraints;

                            if (!item.isMenu() && item.isVisual()) {
                                constraints = getConstraintsAtPoint(
                                                hitMetaComp, lastLeftMousePoint);
                            }
                            else constraints = null;

                            if (!mouseOnVisual(lastLeftMousePoint)) {
                                formDesigner.getModel().getComponentCreator()
                                    .createComponent(item.getInstanceCookie(),
                                                     null, null);
                            }
                            else {
                                formDesigner.getModel().getComponentCreator()
                                    .createComponent(item.getInstanceCookie(),
                                                     hitMetaComp,
                                                     constraints);
                            }
                        }

                        if (!e.isShiftDown()) {
                            formDesigner.toggleSelectionMode();
                            draggingCanceled = true;
                        }
                    }
                }
                e.consume();
            }
        }
    }

    // ---------

    private class HandleLayerMouseMotionListener implements MouseMotionListener
    {
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();

            if (designerResizer != null) {
                designerResizer.drag(e.getPoint());
            }
            else if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT
                     && !anyDragger()
                     && !draggingCanceled)
            {
                if (!viewOnly
                     && !e.isControlDown() && (!e.isShiftDown() || e.isAltDown())
                     && lastLeftMousePoint != null
                     && (resizeType != 0 || lastLeftMousePoint.distance(p) > 8))
                {   // start components dragging
                    componentDragger = createComponentDragger(
                                         lastLeftMousePoint, e.getModifiers());
                }
                else if (formDesigner.getTopDesignComponent()
                                                instanceof RADVisualContainer
                         && !e.isAltDown() && !e.isControlDown()
                         && e.isShiftDown()
                         && lastLeftMousePoint != null
                         && lastLeftMousePoint.distance(p) > 4)
                {   // start selection dragging
                    selectionDragger = new SelectionDragger(lastLeftMousePoint);
                }
            }

            if (componentDragger != null) {
                componentDragger.drag(p);
                repaint();
            }
            if (selectionDragger != null) {
                selectionDragger.drag(p);
                repaint();
            }

            e.consume();
        }

        public void mouseMoved(MouseEvent e) {
            if (formDesigner.getDesignerMode() == FormDesigner.MODE_ADD) {
                RADComponent hitMetaComp =
                    getMetaComponentAt(e.getPoint(),
                                       e.isControlDown() || e.isAltDown() ?
                                           COMP_SELECTED : COMP_DEEPEST);
                showAddHint(hitMetaComp,
                            e.getPoint(),
                            CPManager.getDefault().getSelectedItem());
            }
            else if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT)
                checkResizing(e);
        }
    }

    // ----------

    private class DesignerResizer {
        boolean dragged = false;

        public void drag(Point p) {
            Dimension size = computeDesignerSize(p);
            formDesigner.getComponentLayer().updateDesignerSize(size);
            if (!size.equals(formDesigner.getStoredDesignerSize()))
                dragged = true;
        }

        public void drop(Point p) {
            if (dragged) {
                Dimension size = computeDesignerSize(p);
                formDesigner.setStoredDesignerSize(size);
                // designer size in ComponentLayer will be updated
                // automatically through synthetic property change processing
            }
        }

        private Dimension computeDesignerSize(Point p) {
            Rectangle r = formDesigner.getComponentLayer().getDesignerBounds();
            int w = r.width, h = r.height;

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

            return new Dimension(w, h);
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
                Rectangle bounds = SwingUtilities.convertRectangle(
                                           cont,
                                           comps[i].getBounds(),
                                           HandleLayer.this);
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
}
