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
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.datatransfer.NewType;
import org.openide.awt.MouseUtils;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.forminfo.*;
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

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormEditor.getFormSettings();


    HandleLayer(FormDesigner fd) {
        formDesigner = fd;
        addMouseListener(new HandleLayerMouseListener());
        addMouseMotionListener(new HandleLayerMouseMotionListener());
        setNextFocusableComponent(this);
        setLayout(null);
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
        if (comp instanceof Component && ((Component)comp).isShowing()) {
            Component component = (Component) comp;

            Rectangle rect = component.getBounds();
            rect = SwingUtilities.convertRectangle(component.getParent(),
                                                   rect,
                                                   this);

            Rectangle parentRect = new Rectangle(new Point(0,0),
                                                 component.getParent().getSize());
            parentRect = SwingUtilities.convertRectangle(component.getParent(),
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

        // we are interested in TAB key - and we want to know about it
        // before focus manager does - not focus but selection is changed
        if (keyCode == KeyEvent.VK_TAB || e.getKeyChar() == '\t') {
            if (e.getID() == KeyEvent.KEY_PRESSED) {

                RADComponent nextComp = formDesigner.getNextVisualComponent(
                    (e.getModifiers()&InputEvent.SHIFT_MASK)!=InputEvent.SHIFT_MASK);

                 if (nextComp != null)
                     formDesigner.setSelectedComponent(nextComp);
            }
            e.consume();
        }
        else if (keyCode == KeyEvent.VK_SPACE) {
            if (!viewOnly && e.getID() == KeyEvent.KEY_RELEASED) {
                Iterator it = formDesigner.getSelectedComponents().iterator();
                if (it.hasNext()) {
                    RADComponent comp = (RADComponent)it.next();
                    if (!it.hasNext()) // just one component is selected
                        formDesigner.startInPlaceEditing(comp);
                }
            }
            e.consume();
        }
        else if ((keyCode == KeyEvent.VK_ESCAPE)) {
            cancelDragging();
            e.consume();
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

        RADComponent topMetaComp = formDesigner.getTopDesignContainer(),
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
            return ((RADVisualComponent) metacomp).getParentContainer();
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

    private void processDoubleClick(MouseEvent e) {
        if (e.isShiftDown() || e.isControlDown()) return;

        RADComponent metacomp = getMetaComponentAt(e.getPoint(), COMP_SELECTED);

        if (e.isAltDown()) {
            if (metacomp instanceof RADVisualComponent) {
                metacomp = ((RADVisualComponent)metacomp).getParentContainer();
                if (metacomp == null) return;
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
                                                  MouseEvent e) {
        if (!(metacomp instanceof RADVisualComponent)) return;

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
                (RADVisualContainer)metacomp :
                ((RADVisualComponent)metacomp).getParentContainer();

        LayoutSupport laysup = metacont.getLayoutSupport();
        if (laysup instanceof LayoutSupportArranging) {
            Container cont = (Container) formDesigner.getComponent(metacont);
            Point p = SwingUtilities.convertPoint(HandleLayer.this, e.getPoint(), cont);
            ((LayoutSupportArranging)laysup).processMouseClick(p, cont);
        }
    }

    private void setComponentBorder(RADComponent metacomp, PaletteItem item) {
        if (!(metacomp instanceof RADVisualComponent) || !item.isBorder())
            return;

        if (!(JComponent.class.isAssignableFrom(metacomp.getBeanClass()))) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                FormEditor.getFormBundle().getString("MSG_BorderNotApplicable"),
                                                     NotifyDescriptor.INFORMATION_MESSAGE));
            return;
        }

        RADProperty prop = metacomp.getPropertyByName("border");
        if (prop == null) return;

        try {
            Object border = item.createInstance();
            prop.setValue(border);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        formDesigner.setSelectedComponent(metacomp);
    }

    private void setContainerLayout(RADComponent metacomp, PaletteItem item) {
        if (!(metacomp instanceof RADVisualComponent)
                || !item.isLayout())
            return;

        // get container on which the layout will be set
        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            ((RADVisualComponent)metacomp).getParentContainer();

        LayoutSupport layoutSupport = metacont.getLayoutSupport();
        if (layoutSupport != null
                && layoutSupport.getLayoutClass() == null
                && layoutSupport.getClass() != NullLayoutSupport.class)
            return; // layout cannot be changed

        layoutSupport = null;
        try {
            layoutSupport = item.createLayoutSupportInstance();
        }
        catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace();

            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(
                    MessageFormat.format(
                        FormEditor.getFormBundle().getString("FMT_ERR_LayoutInit"),
                        new Object[] { item.getItemClass().getName(),
                                        e.getClass().getName() }),
                    NotifyDescriptor.ERROR_MESSAGE));
            return;
        }

        if (layoutSupport == null) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(
                    MessageFormat.format(
                        FormEditor.getFormBundle().getString("FMT_ERR_LayoutNotFound"),
                        new Object[] { item.getItemClass().getName() }),
                    NotifyDescriptor.ERROR_MESSAGE));
            return;
        }

        metacont.getFormModel().setContainerLayout(metacont, layoutSupport);
    }

    private void addVisualBean(RADComponent metacomp, PaletteItem item,
                               MouseEvent e) {
        if (!(metacomp instanceof RADVisualComponent)
                || !item.isVisual())
            return;

        // get parent container into which new component will be added
        RADVisualContainer parentCont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            ((RADVisualComponent)metacomp).getParentContainer();

        RADVisualComponent newMetacomp = null;
        RADVisualContainer newMetacont = item.isContainer() ?
            new RADVisualContainer() : null;

        while (newMetacomp == null) {
            // initialize meta-component and its bean instance
            newMetacomp = newMetacont == null ?
                new RADVisualComponent() : newMetacont;

            newMetacomp.initialize(formDesigner.getModel());

            try {
                newMetacomp.initInstance(item.getInstanceCookie());
            }
            catch (Throwable th) {
                if (th instanceof ThreadDeath)
                    throw (ThreadDeath)th;
                else {
                    showInstErrorMessage(th);
                    return;
                }
            }

            if (newMetacont != null) { // the new component is a container
                // initialize LayoutSupport
                newMetacont.initLayoutSupport();
                if (newMetacont.getLayoutSupport() == null) {
                    // no LayoutSupport found for the container,
                    // create RADVisualComponent only
                    newMetacont = null;
                    newMetacomp = null;
                }
            }
        }

        Container cont = parentCont.getContainerDelegate(
                                        formDesigner.getComponent(parentCont));
        Point p = SwingUtilities.convertPoint(HandleLayer.this,
                                              e.getPoint(), cont);
        LayoutSupport.ConstraintsDesc constraints =
            parentCont.getLayoutSupport().getNewConstraints(cont, p, null, null);

        formDesigner.getModel().addVisualComponent(newMetacomp, parentCont,
                                                   constraints);

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        FormEditor.defaultComponentInit(newMetacomp);

        formDesigner.setSelectedComponent(newMetacomp);
        //formWindow.validate();
        //fireCodeChange();
    }

    private void addNonVisualBean(PaletteItem item) {
        RADComponent newMetacomp = new RADComponent();
        newMetacomp.initialize(formDesigner.getModel());

        try {
            newMetacomp.initInstance(item.getInstanceCookie());
//                newComp = item.createInstance();
//                newMetacomp.setInstance(newComp);
        }
        catch (Throwable th) {
            if (th instanceof ThreadDeath)
                throw (ThreadDeath)th;
            else {
                showInstErrorMessage(th);
                return;
            }
        }

        formDesigner.getModel().addNonVisualComponent(newMetacomp, null);
        formDesigner.setSelectedComponent(newMetacomp);
        //formWindow.validate();
        //fireCodeChange();
    }

    private void addMenu(PaletteItem item) {
        FormModel formModel = formDesigner.getModel();

        RADMenuComponent newMenuComp = new RADMenuComponent();
        newMenuComp.initialize(formModel);
        newMenuComp.setComponent(item.getItemClass());
        newMenuComp.initSubComponents(new RADComponent[0]);
        formModel.addNonVisualComponent(newMenuComp, null);

        // for some components, we initialize their properties with some
        // non-default values e.g. a label on buttons, checkboxes
        FormEditor.defaultMenuInit(newMenuComp);

        NewType[] newTypes = newMenuComp.getNewTypes();
        if (newTypes.length != 0) {
            try {
                newTypes[0].create();
            } catch (java.io.IOException e) {
            }
        }

        FormInfo formInfo = formModel.getFormInfo();

        if ((formInfo instanceof JMenuBarContainer
             && JMenuBar.class.isAssignableFrom(item.getItemClass()))
            || (formInfo instanceof MenuBarContainer
                && MenuBar.class.isAssignableFrom(item.getItemClass()))) {

            RADVisualFormContainer form = (RADVisualFormContainer)
                                          formModel.getTopRADComponent();
            if (form.getFormMenu() == null) {
                form.setFormMenu(newMenuComp.getName());
                formModel.fireComponentAdded(newMenuComp, null);
            }
        }

        formDesigner.setSelectedComponent(newMenuComp);
    }

    private void showContextMenu(Point popupPos) {
        ComponentInspector ci = ComponentInspector.getInstance();
        if (ci.getFocusedForm() != formDesigner.getModel())
            formDesigner.componentActivated(); // might happen in one case...

        Node[] selectedNodes = ci.getSelectedNodes();
        JPopupMenu popup = NodeOp.findContextMenu(selectedNodes);
        if (popup != null) {
            popup.show(HandleLayer.this, popupPos.x, popupPos.y);
        }
    }

    static private void showInstErrorMessage(Throwable ex) {
//        if (System.getProperty("netbeans.debug.exceptions") != null)
            ex.printStackTrace();

        String message = MessageFormat.format(
            FormEditor.getFormBundle().getString("FMT_ERR_CannotInstantiate"),
            new Object [] { ex.getClass().getName(), ex.getMessage() });
        TopManager.getDefault().notify(new NotifyDescriptor.Message(
            message, NotifyDescriptor.ERROR_MESSAGE));
    }

    // --------

    private ComponentDragger createComponentDragger(Point hotspot) {
        List selectedComponents = formDesigner.getSelectedComponents();
        if (selectedComponents.size() == 0)
            return null;
        
        List selComps = new ArrayList(selectedComponents.size());
        Iterator iter = selectedComponents.iterator();
        while (iter.hasNext()) {
            RADComponent metacomp = (RADComponent) iter.next();
            if (metacomp instanceof RADVisualComponent)
                if (metacomp != formDesigner.getTopDesignContainer())
                    selComps.add(metacomp);
                else return null;
        }

        Set children = new HashSet();
        iter = selComps.iterator();
        while (iter.hasNext()) {
            RADVisualComponent metacomp = (RADVisualComponent) iter.next();
            
            Iterator iter2 = selComps.iterator();
            while (iter2.hasNext()) {
                RADVisualComponent metacomp2 = (RADVisualComponent) iter2.next();
                if (metacomp2 != metacomp
                        && metacomp2 instanceof RADVisualContainer) {
                    RADVisualContainer metacont = metacomp.getParentContainer();
                    while (metacont != null) {
                        if (metacont == metacomp2) {
                            children.add(metacomp);
                            break;
                        }
                        metacont = metacont.getParentContainer();
                    }
                }
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

    private void checkResizing(Point p) {
        // check wheteher all selected components are in the same container
        RADVisualContainer parentCont = null;
        Iterator selected = formDesigner.getSelectedComponents().iterator();
        while (selected.hasNext()) {
            Object comp = selected.next();
            if (comp instanceof RADVisualComponent) {
                if (parentCont == null)
                    parentCont = ((RADVisualComponent)comp).getParentContainer();
                else if (((RADVisualComponent)comp).getParentContainer() != parentCont)
                    return; // selected components are not in the same container
            }
        }

        RADComponent compAtPoint = getMetaComponentAt(p, COMP_SELECTED);
        if (!(compAtPoint instanceof RADVisualComponent))
            return;

        RADVisualComponent metacomp = (RADVisualComponent) compAtPoint;
        int resizing = 0;

        if (!formDesigner.isComponentSelected(metacomp)) {
            RADVisualContainer metacont;
            if (metacomp instanceof RADVisualContainer)
                metacont = (RADVisualContainer) metacomp;
            else
                metacont = metacomp.getParentContainer();

            RADVisualComponent[] metacomps = metacont.getSubComponents();
            for (int i=0; i < metacomps.length; i++) {
                metacomp = metacomps[i];
                resizing = getComponentResizable(p, metacomp);
                if (resizing != 0) break;
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

        RADVisualContainer metacont = metacomp.getParentContainer();
        if (metacont == null || metacomp == formDesigner.getTopDesignContainer())
            return 0;

        LayoutSupport laySup = metacont.getLayoutSupport();
        if (laySup == null)
            return 0;

        Component comp = (Component) formDesigner.getComponent(metacomp);

        int resizable = laySup.getResizableDirections(comp);
        if (resizable != 0)
            resizable &= getSelectionResizable(p, comp);

        return resizable;
    }

    private int getSelectionResizable(Point p, Component comp) {
        if (comp == null) return 0;

        Rectangle bounds = comp.getBounds();
        bounds.x = 0;
        bounds.y = 0;
        bounds = SwingUtilities.convertRectangle(comp, bounds, this);

        Rectangle r1 = new Rectangle(bounds);
        Rectangle r2 = new Rectangle(bounds);

        r1.grow(2, 2);
        r2.grow(-3, -3);
        if (r2.width < 0) r2.width = 0;
        if (r2.height < 0) r2.height = 0;

        int resizable = 0;
        if (r1.contains(p)) {
            if (p.y >= r2.y + r2.height)
                resizable |= LayoutSupport.RESIZE_DOWN;
            else if (p.y < r2.y)
                resizable |= LayoutSupport.RESIZE_UP;
            if (p.x >= r2.x + r2.width)
                resizable |= LayoutSupport.RESIZE_RIGHT;
            else if (p.x < r2.x)
                resizable |= LayoutSupport.RESIZE_LEFT;
        }

        return resizable;
    }

    private void setResizingCursor(int resizeType) {
        Cursor cursor = null;
        if ((resizeType & LayoutSupport.RESIZE_UP) != 0) {
            if ((resizeType & LayoutSupport.RESIZE_LEFT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            else if ((resizeType & LayoutSupport.RESIZE_RIGHT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            else
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        }
        else if ((resizeType & LayoutSupport.RESIZE_DOWN) != 0) {
            if ((resizeType & LayoutSupport.RESIZE_LEFT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            else if ((resizeType & LayoutSupport.RESIZE_RIGHT) != 0)
                cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            else
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
        }
        else if ((resizeType & LayoutSupport.RESIZE_LEFT) != 0)
            cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
        else if ((resizeType & LayoutSupport.RESIZE_RIGHT) != 0)
            cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

        if (cursor == null)
            cursor = Cursor.getDefaultCursor();

        setCursor(cursor);
    }

    private void displayHint(RADComponent metacomp, Point p, PaletteItem item) {
        if (metacomp == null) {
            TopManager.getDefault().setStatusText(""); // NOI18N
            return;
        }

        RADVisualContainer metacont;

        if (metacomp instanceof RADVisualContainer)
            metacont = (RADVisualContainer) metacomp;
        else
            metacont = ((RADVisualComponent)metacomp).getParentContainer();

        if (item.isLayout()) {
            LayoutSupport layoutSupp = metacont.getLayoutSupport();
            if (layoutSupp != null
                && !(layoutSupp instanceof LayoutSupport))
            {
                setStatusText("FMT_MSG_CannotSetLayout",
                              new Object[] { metacont.getName() });
            } else {
                setStatusText("FMT_MSG_SetLayout",
                              new Object[] { metacont.getName() });
            }
        }
        else if (item.isBorder()) {
            if (JComponent.class.isAssignableFrom(metacomp.getBeanClass())) {
                setStatusText("FMT_MSG_SetBorder",
                              new Object[] { metacomp.getName() });
            }
            else {
                setStatusText("FMT_MSG_CannotSetBorder",
                              new Object[] { metacomp.getName() });
            }
        } else if (!item.isVisual() || item.isMenu()) {
            setStatusText("FMT_MSG_AddNonVisualComponent",
                          new Object[] { item.getItemClass().getName() });
        } else {
            LayoutSupport layoutSupp = metacont.getLayoutSupport();
            if (layoutSupp != null) {
                Container cont = metacont.getContainerDelegate(
                        formDesigner.getComponent(metacont));
                Point point = SwingUtilities.convertPoint(
                        HandleLayer.this, p, cont);
                LayoutSupport.ConstraintsDesc cd =
                        layoutSupp.getNewConstraints(cont, point, null, null);
                if (cd != null) {
                    setStatusText("FMT_MSG_AddComponent",
                                  new Object[] {
                                      cd.getJavaInitializationString(),
                                      metacont.getName(),
                                      item.getItemClass().getName()
                                  });
                }
                else {
                    TopManager.getDefault().setStatusText("");
                }
            }
        }
    }

    private static void setStatusText(String formatId, Object[] args) {
        TopManager.getDefault().setStatusText(
            MessageFormat.format(
                FormEditor.getFormBundle().getString(formatId),
                args));
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
                         && !e.isAltDown()) {
                    formDesigner.startInPlaceEditing(
                        getMetaComponentAt(e.getPoint(), COMP_SELECTED));
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
            if (!HandleLayer.this.isVisible())
                return;

            if (MouseUtils.isRightMouseButton(e)) {
                if (componentDragger == null) {
                    RADComponent hitMetaComp =
                        getMetaComponentAt(e.getPoint(), COMP_SELECTED);
                    if (!formDesigner.isComponentSelected(hitMetaComp))
                        formDesigner.setSelectedComponent(hitMetaComp);
                }
                e.consume();
            }
            else if (MouseUtils.isLeftMouseButton(e)) {
                boolean modifier = e.isControlDown() || e.isAltDown() || e.isShiftDown();
                if (!modifier)
                    lastLeftMousePoint = e.getPoint();

                ComponentPalette palette = ComponentPalette.getDefault();

                if (palette.getMode() == PaletteAction.MODE_SELECTION) {
                    if (!modifier)
                        checkResizing(e.getPoint());

                    if (resizeType == 0) {
                        if (e.getClickCount() == 2)
                            processDoubleClick(e);
                        else {
                            RADComponent hitMetaComp = selectComponent(e);
                            if (!modifier) // plain single click
                                processMouseClickInLayoutSupport(hitMetaComp, e);
                        }
                    }
                }
                else if (!viewOnly) {
                    RADComponent hitMetaComp = getMetaComponentAt(e.getPoint(),
                            e.isControlDown() || e.isAltDown() ?
                            COMP_SELECTED : COMP_DEEPEST);

                    if (palette.getMode() == PaletteAction.MODE_CONNECTION) {
                        if (hitMetaComp != null)
                            formDesigner.connectBean(hitMetaComp);
                    }
                    else if (palette.getMode() == PaletteAction.MODE_ADD) {
                        PaletteItem item = palette.getSelectedItem();

                        if (item.isBorder()) {
                            if (hitMetaComp != null)
                                setComponentBorder(hitMetaComp, item);
                        }
                        else if (item.isLayout()) {
                            if (hitMetaComp == null)
                                hitMetaComp = formDesigner.getTopDesignContainer();
                            setContainerLayout(hitMetaComp, item);
                        }
                        else if (item.isMenu()) {
                            addMenu(item);
                        }
                        else if (item.isVisual()) {
                            if (hitMetaComp == null)
                                hitMetaComp = formDesigner.getTopDesignContainer();
                            addVisualBean(hitMetaComp, item, e);
                        }
                        else {
                            addNonVisualBean(item);
                        }

                        if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0)
                            palette.setMode(PaletteAction.MODE_SELECTION);
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

            if (componentDragger == null
                && lastLeftMousePoint != null
                && (resizeType != 0 || lastLeftMousePoint.distance(p) > 6))
            { // start dragging
                componentDragger = createComponentDragger(lastLeftMousePoint);
//                lastLeftMousePoint = null;
            }

            if (componentDragger != null)
                componentDragger.mouseDragged(p);

            e.consume();
        }

        public void mouseMoved(MouseEvent e) {
            ComponentPalette palette = ComponentPalette.getDefault();
            if (palette.getMode() == PaletteAction.MODE_ADD) {
                RADComponent hitMetaComp = getMetaComponentAt(e.getPoint(), COMP_DEEPEST);
                displayHint(hitMetaComp, e.getPoint(), palette.getSelectedItem());
            }
            else if (palette.getMode() == PaletteAction.MODE_SELECTION) {
                checkResizing(e.getPoint());
            }
        }
    }
}
