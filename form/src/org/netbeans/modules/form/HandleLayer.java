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
import java.util.List;
import javax.swing.border.Border;
import java.text.MessageFormat;

import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.awt.MouseUtils;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.palette.*;

import org.netbeans.modules.form.layoutsupport.*;

/**
 *
 * @author Tran Duc Trung
 */

class HandleLayer extends JPanel
{
    // constants for mode parameter of getMetaComponentAt(Point,int) method
    static final int COMP_DEEPEST = 0; // get the deepest component (at given point)
    static final int COMP_SELECTED = 1; // get the deepest selected component
    static final int COMP_ABOVE_SELECTED = 2; // get the component above the deepest selected component
    static final int COMP_UNDER_SELECTED = 3; // get the component under the deepest selected component
    
    private FormDesigner formDesigner;
    private boolean viewOnly;

    private ComponentDragger componentDragger;
    private Point lastLeftMousePoint;
    private Point prevLeftMousePoint;
    private boolean draggingCanceled = false;
    private int resizeType;
    
    private FormDesigner.Resizer fdResizer;
    private int designerResizeType;
    private boolean wasDragged = false;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormEditor.getFormSettings();


    HandleLayer(FormDesigner fd) {
        formDesigner = fd;
        addMouseListener(new HandleLayerMouseListener());
        addMouseMotionListener(new HandleLayerMouseMotionListener());
//        setNextFocusableComponent(this);
        setLayout(null);

        // on JDK 1.4, we set Ctrl+TAB and Ctrl+Shift+TAB as focus traversal
        // keys - to have TAB and Shift+TAB free for component selection
        java.lang.reflect.Method setFocusTraversalKeysMethod = null;
        try {
            setFocusTraversalKeysMethod =
                getClass().getMethod("setFocusTraversalKeys", // NOI18N
                                     new Class[] { Integer.TYPE, Set.class });
        }
        catch (NoSuchMethodException ex) {} // ignore

        if (setFocusTraversalKeysMethod != null) { // JDK 1.4 or newer
            Set keys = new HashSet();
            try {
                Class awtKeyStrokeClass = Class.forName("java.awt.AWTKeyStroke"); // NOI18N
                java.lang.reflect.Method getAWTKeyStrokeMethod =
                    awtKeyStrokeClass.getMethod("getAWTKeyStroke", // NOI18N
                                                new Class[] { Integer.TYPE,
                                                              Integer.TYPE,
                                                              Boolean.TYPE });
//                keys.add(AWTKeyStroke.getAWTKeyStroke(
//                           9, InputEvent.CTRL_DOWN_MASK, true));
                keys.add(getAWTKeyStrokeMethod.invoke(
                           null,
                           new Object[] { new Integer(9),
                                          new Integer(128),
                                          Boolean.TRUE }));

//                setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
//                                      keys);
                setFocusTraversalKeysMethod.invoke(this,
                                                   new Object[] {
                                                       new Integer(0), keys });

                keys.clear();
//                keys.add(AWTKeyStroke.getAWTKeyStroke(
//                          9,
//                          InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK,
//                          true));
                keys.add(getAWTKeyStrokeMethod.invoke(
                           null,
                           new Object[] { new Integer(9),
                                          new Integer(192),
                                          Boolean.TRUE }));
                
//                setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
//                                      keys);
                setFocusTraversalKeysMethod.invoke(this,
                                                   new Object[] {
                                                       new Integer(1), keys });
            }
            catch (Exception ex) { // should not happen (running on JDK 1.4)
                ex.printStackTrace();
            }
        }
        
        getAccessibleContext().setAccessibleName(FormEditor.getFormBundle().getString("ACSN_HandleLayer"));
        getAccessibleContext().setAccessibleDescription(FormEditor.getFormBundle().getString("ACSD_HandleLayer"));
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

        // JDK 1.3 focus traversal hacks - we don't allow TAB keys to get to
        // the focus manager, we handle it by ourselves - not changing the
        // focus but the component selection
        if (keyCode == KeyEvent.VK_TAB || e.getKeyChar() == '\t') {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if ((e.getModifiers()&InputEvent.CTRL_MASK)==InputEvent.CTRL_MASK) {
                    // Ctrl+TAB
                    if ((e.getModifiers()&InputEvent.SHIFT_MASK)!=InputEvent.SHIFT_MASK)
                        javax.swing.FocusManager.getCurrentManager()
                                                    .focusNextComponent(this);
                    else
                        javax.swing.FocusManager.getCurrentManager()
                                                    .focusPreviousComponent(this);
                }
                else {
                    RADComponent nextComp = formDesigner.getNextVisualComponent(
                        (e.getModifiers()&InputEvent.SHIFT_MASK)!=InputEvent.SHIFT_MASK);

                     if (nextComp != null)
                         formDesigner.setSelectedComponent(nextComp);
                }
            }
            e.consume();
        }
        else if (keyCode == KeyEvent.VK_SPACE) {
            if (!viewOnly && e.getID() == KeyEvent.KEY_RELEASED) {
                Iterator it = formDesigner.getSelectedComponents().iterator();
                if (it.hasNext()) {
                    RADComponent comp = (RADComponent)it.next();
                    if (!it.hasNext()) { // just one component is selected
                        CPManager palette = CPManager.getDefault();
                        if (palette.getMode() == PaletteAction.MODE_SELECTION) {
                            // in selection mode SPACE starts in-place editing
                            formDesigner.startInPlaceEditing(comp);
                        }
                        else if (palette.getMode() == PaletteAction.MODE_ADD) {
                            // in add mode SPACE adds selected item as component
                            PaletteItem item = palette.getSelectedItem();
                            formDesigner.getModel().getComponentCreator()
                                .createComponent(item.getInstanceCookie(),
                                                 comp, null);
                            palette.setMode(PaletteAction.MODE_SELECTION);
                        }
                    }
                }
            }
            e.consume();
        }
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            if (cancelDragging())
                e.consume();
        }
        else if (keyCode == KeyEvent.VK_F10) {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
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
     * returns metacomponent at given position.
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
        Component componentLayer = formDesigner.getComponentLayer();
        point = SwingUtilities.convertPoint(this, point, componentLayer);
        Component comp = SwingUtilities.getDeepestComponentAt(
            componentLayer, point.x, point.y);

        RADComponent topMetaComp = formDesigner.getTopDesignComponent(),
                     firstMetaComp = null,
                     currMetaComp,
                     prevMetaComp = null;

        while (comp != null) {
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
            comp = comp.getParent();
        }
        return firstMetaComp;
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
        boolean ctrl = e.isControlDown() && !e.isAltDown();
        boolean alt = e.isAltDown() && !e.isControlDown();

        int selMode = ctrl ? COMP_UNDER_SELECTED :
                             (alt ? COMP_ABOVE_SELECTED : COMP_DEEPEST);

        RADComponent hitMetaComp = getMetaComponentAt(e.getPoint(), selMode);

        if (e.isShiftDown()) {
            if (formDesigner.isComponentSelected(hitMetaComp))
                formDesigner.removeComponentFromSelection(hitMetaComp);
            else
                formDesigner.addComponentToSelection(hitMetaComp);
        }
        else {
            if (hitMetaComp != null) {
                if (formDesigner.isComponentSelected(hitMetaComp)) {
                    if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                        formDesigner.setSelectedComponent(hitMetaComp);
                    }
                }
                else {
                    formDesigner.setSelectedComponent(hitMetaComp);
                }
            }
            else
                formDesigner.clearSelection();
        }
        repaint();

        return hitMetaComp;
    }
    
    private void selectOtherComponentsNode() {
        FormEditorSupport fes = FormEditorSupport.getSupport(formDesigner.getModel());
        ComponentInspector ci = ComponentInspector.getInstance();
        Node[] selectedNode = new Node[] { ((FormRootNode)fes.getFormRootNode()).getOthersNode() };
        
        try {
            ci.setSelectedNodes(selectedNode, fes);
        }
        catch (java.beans.PropertyVetoException ex) {
            ex.printStackTrace();
        }
        
        formDesigner.setActivatedNodes(selectedNode);
    }

    private void processDoubleClick(MouseEvent e) {
        if (e.isShiftDown() || e.isControlDown())
            return;

        RADComponent metacomp = getMetaComponentAt(e.getPoint(), COMP_SELECTED);
        if (metacomp == null)
            return;

        if (e.isAltDown()) {
            if (metacomp instanceof RADVisualComponent) {
                metacomp = metacomp.getParentComponent();
                if (metacomp == null)
                    return;
            }
        }

        Node node = metacomp.getNodeReference();
        if (node != null) {
            SystemAction action = node.getDefaultAction();
            if (action != null) {// && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(
                        node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            }
        }
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

    private ComponentDragger createComponentDragger(Point hotspot) {
        List selectedComponents = formDesigner.getSelectedComponents();
        if (selectedComponents.size() == 0)
            return null;

        // all selected components must be visible in the designer
        List selComps = new ArrayList(selectedComponents.size());
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
/*                RADVisualComponent metacomp2 = (RADVisualComponent) iter2.next();
                if (metacomp2 != metacomp
                        && metacomp2 instanceof RADVisualContainer) {
                    RADVisualContainer metacont = (RADVisualContainer)
                                                  metacomp.getParentComponent();
                    while (metacont != null) {
                        if (metacont == metacomp2) {
                            children.add(metacomp);
                            break;
                        }
                        metacont = metacont.getParentContainer();
                    }
                } */
            }
        }
        selComps.removeAll(children);
        if (selComps.isEmpty())
            return null;

        draggingCanceled = false;
        
        return new ComponentDragger(
            formDesigner,
            HandleLayer.this,
            (RADVisualComponent[]) selComps.toArray(
                new RADVisualComponent[selComps.size()]),
            hotspot,
            resizeType);
    }

    private boolean cancelDragging() {
        if (componentDragger != null) {
            componentDragger = null;
            repaint();
            draggingCanceled = true;
            return true;
        }
        return false;
    }
    
    private boolean validDesignerResizing(int resizing) {
        return resizing == (LayoutSupportManager.RESIZE_DOWN
                            | LayoutSupportManager.RESIZE_RIGHT)
            || resizing == LayoutSupportManager.RESIZE_DOWN
            || resizing == LayoutSupportManager.RESIZE_RIGHT;
    }
    
    private void checkDesignerResizing(Point p) {
        int resizing = getSelectionResizable(p, formDesigner.getComponentLayer(), 7);
        
        if (validDesignerResizing(resizing)) {
            setResizingCursor(resizing);
        }
        else {
            Cursor cursor = getCursor();
            if (cursor != null && cursor.getType() != Cursor.DEFAULT_CURSOR)
                setCursor(Cursor.getDefaultCursor());
        }
        
        designerResizeType = resizing;
    }

    private void checkResizing(Point p) {
        // check resizing of FormDesigner
        checkDesignerResizing(p);
        
        // check wheteher all selected components are in the same container
        RADComponent parent = null;
        Iterator selected = formDesigner.getSelectedComponents().iterator();
        while (selected.hasNext()) {
            RADComponent comp = (RADComponent) selected.next();
            if (comp instanceof RADVisualComponent) {
                if (parent == null) {
                    parent = comp.getParentComponent();
                    if (parent == null)
                        return; // component without a parent cannot be resized
                }
                else if (comp.getParentComponent() != parent)
                    return; // selected components are not in the same container
            }
        }

        RADComponent compAtPoint = getMetaComponentAt(p, COMP_SELECTED);
        if (!(compAtPoint instanceof RADVisualComponent))
            return;

        RADVisualComponent metacomp = (RADVisualComponent) compAtPoint;
        int resizing = 0;

        if (!formDesigner.isComponentSelected(metacomp)) {
            RADVisualComponent[] otherComps;
            if (metacomp instanceof RADVisualContainer)
                otherComps = ((RADVisualContainer)metacomp).getSubComponents();
            else {
                RADVisualContainer metacont = metacomp.getParentContainer();
                if (metacont != null)
                    otherComps = metacont.getSubComponents();
                else return; // component without a parent
            }

            for (int i=0; i < otherComps.length; i++) {
                metacomp = otherComps[i];
                resizing = getComponentResizable(p, metacomp);
                if (resizing != 0)
                    break;
            }
        }
        else resizing = getComponentResizable(p, metacomp);

        if (resizing != 0) {
            setResizingCursor(resizing);
//            resizedComponent = metacomp;
        }
        else {
            Cursor cursor = getCursor();
            if (cursor != null && cursor.getType() != Cursor.DEFAULT_CURSOR)
                setCursor(Cursor.getDefaultCursor());
//            resizedComponent = null;
        }
        resizeType = resizing;
    }

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

        Component comp = (Component) formDesigner.getComponent(metacomp);

        int resizable = laySup.getResizableDirections(
                                   comp,
                                   metacont.getIndexOf(metacomp));
        if (resizable != 0)
            resizable &= getSelectionResizable(p, comp, 2);

        return resizable;
    }

    private int getSelectionResizable(Point p, Component comp, int borderWidth) {
        if (comp == null) return 0;

        Rectangle bounds = comp.getBounds();
        bounds.x = 0;
        bounds.y = 0;
        bounds = SwingUtilities.convertRectangle(comp, bounds, this);

        Rectangle r1 = new Rectangle(bounds);
        Rectangle r2 = new Rectangle(bounds);

        r1.grow(borderWidth, borderWidth);
        r2.grow(-3, -3);
        if (r2.width < 0) r2.width = 0;
        if (r2.height < 0) r2.height = 0;

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
            || item.getItemClass() == null)
        {
            TopManager.getDefault().setStatusText(""); // NOI18N
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
        TopManager.getDefault().setStatusText(
            MessageFormat.format(
                FormEditor.getFormBundle().getString(formatId),
                args));
    }
    
    private boolean mouseOnComponentLayer(Point p) {
        Rectangle bounds = formDesigner.getComponentLayer().getBounds();
        return bounds.contains(p);
    }

    // ---------

    private class HandleLayerMouseListener implements MouseListener
    {
        public void mouseClicked(MouseEvent e) {
            if (MouseUtils.isRightMouseButton(e) && componentDragger == null) {
                showContextMenu(e.getPoint());
            }
            e.consume();
        }

        public void mouseReleased(MouseEvent e) {
            if (!HandleLayer.this.isVisible())
                return;

            if (MouseUtils.isLeftMouseButton(e)) {
                if (fdResizer != null) {
                    if (wasDragged) {
                        fdResizer.dropDesigner(e.getPoint(), true);
                    }
                    fdResizer.hideCurrentSizeInStatus();
                    fdResizer = null;
                    wasDragged = false;
                }
                
                CPManager palette = CPManager.getDefault();

                if (palette.getMode() == PaletteAction.MODE_SELECTION) {
                    if (componentDragger != null) {
                        componentDragger.dropComponents(e.getPoint());
                        componentDragger = null;
                        repaint();
                    }
                    else if (draggingCanceled) {
                        draggingCanceled = false;
                    }
                    else if (prevLeftMousePoint != null
                             && e.getClickCount() == 1
                             && prevLeftMousePoint.distance(e.getPoint()) <= 2
                             && !e.isShiftDown()
                             && !e.isControlDown()
                             && !e.isAltDown())
                    {
                        formDesigner.startInPlaceEditing(
                            getMetaComponentAt(e.getPoint(), COMP_SELECTED));
                    }
                }

                if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0) {
                    if (palette.getMode() == PaletteAction.MODE_ADD)
                        palette.setMode(PaletteAction.MODE_SELECTION);
                }

                prevLeftMousePoint = lastLeftMousePoint;
                lastLeftMousePoint = null;
            }

            e.consume();
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
            TopManager.getDefault().setStatusText(""); // NOI18N
        }
        
        public void mousePressed(MouseEvent e) {
            formDesigner.componentActivated();
            if (!HandleLayer.this.isVisible())
                return;

            if (MouseUtils.isRightMouseButton(e)) {
                if (componentDragger == null) {
                    if (!mouseOnComponentLayer(e.getPoint())) {
                        selectOtherComponentsNode();
                    }
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
                e.consume();
            }
            else if (MouseUtils.isLeftMouseButton(e)) {
                boolean modifier = e.isControlDown() || e.isAltDown() || e.isShiftDown();
                if (!modifier)
                    lastLeftMousePoint = e.getPoint();

                CPManager palette = CPManager.getDefault();
                int paletteMode = palette.getMode();

                if (paletteMode == PaletteAction.MODE_SELECTION) {
                    if (!modifier)
                        checkResizing(e.getPoint());
                    
                    if (!mouseOnComponentLayer(e.getPoint())) {
                        if (designerResizeType == 0) {
                            selectOtherComponentsNode();
                        }
                    }
                    else if (resizeType == 0) {
                        if (e.getClickCount() == 2)
                            processDoubleClick(e);
                        else {
                            RADComponent hitMetaComp = selectComponent(e);
                            if (hitMetaComp != null && !modifier) // plain single click
                                processMouseClickInLayoutSupport(hitMetaComp, e);
                        }
                    }
                    
                    if (fdResizer == null
                        && lastLeftMousePoint != null
                        && validDesignerResizing(designerResizeType))
                    {
                        fdResizer = new FormDesigner.Resizer(formDesigner,
                                                             designerResizeType);
                        fdResizer.showCurrentSizeInStatus();
                    }
                }
                else if (!viewOnly) {
                    if (palette.getMode() == PaletteAction.MODE_CONNECTION) {
                        selectComponent(e);
                    }
                    else if (paletteMode == PaletteAction.MODE_ADD) {
                        RADComponent hitMetaComp = getMetaComponentAt(
                            e.getPoint(),
                            e.isControlDown() || e.isAltDown() ?
                                COMP_SELECTED : COMP_DEEPEST);

                        PaletteItem item = palette.getSelectedItem();
                        Object constraints;

                        if (!item.isMenu() && item.isVisual()) {
                            constraints = getConstraintsAtPoint(hitMetaComp,
                                                                e.getPoint());
                        }
                        else constraints = null;

                        if (!mouseOnComponentLayer(e.getPoint())) {
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
//                        if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0)
//                            palette.setMode(PaletteAction.MODE_SELECTION);
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
            
            wasDragged = true;
            
            if (fdResizer != null) {
                fdResizer.dropDesigner(e.getPoint(), false);
            }
            else if (componentDragger == null
                && lastLeftMousePoint != null
                && (resizeType != 0 || lastLeftMousePoint.distance(p) > 6)
                && CPManager.getDefault().getMode() == PaletteAction.MODE_SELECTION)
            { // start dragging
                componentDragger = createComponentDragger(lastLeftMousePoint);
//                lastLeftMousePoint = null;
            }

            if (componentDragger != null)
                componentDragger.mouseDragged(p);

            e.consume();
        }

        public void mouseMoved(MouseEvent e) {
            CPManager palette = CPManager.getDefault();
            if (palette.getMode() == PaletteAction.MODE_ADD) {
                RADComponent hitMetaComp = getMetaComponentAt(
                    e.getPoint(),
                    e.isControlDown() || e.isAltDown() ?
                        COMP_SELECTED : COMP_DEEPEST);
                showAddHint(hitMetaComp, e.getPoint(), palette.getSelectedItem());
            }
            else if (palette.getMode() == PaletteAction.MODE_SELECTION) {
                checkResizing(e.getPoint());
            }
        }
    }
}
