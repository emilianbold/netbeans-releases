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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.io.IOException;
import javax.swing.*;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.undo.UndoableEdit;
import org.netbeans.modules.form.actions.DuplicateAction;
import org.netbeans.modules.form.assistant.AssistantModel;
import org.netbeans.spi.palette.PaletteController;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.Utilities;

import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.form.fakepeer.FakePeerSupport;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutdesign.*;
import org.netbeans.modules.form.menu.MenuEditLayer;
import org.openide.util.Lookup;

/**
 * A transparent layer (glass pane) handling user operations in designer (mouse
 * and keyboard events) and painting selection and drag&drop feedback.
 * Technically, this is a layer in FormDesigner, placed over ComponentLayer.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class HandleLayer extends JPanel implements MouseListener, MouseMotionListener
{
    // constants for mode parameter of getMetaComponentAt(Point,int) method
    public static final int COMP_DEEPEST = 0; // get the deepest component (at given position)
    public static final int COMP_SELECTED = 1; // get the deepest selected component
    public static final int COMP_ABOVE_SELECTED = 2; // get the component above the deepest selected component
    public static final int COMP_UNDER_SELECTED = 3; // get the component under the deepest selected component
    
    private static final int DESIGNER_RESIZING = 256; // flag for resizeType
    private static MessageFormat resizingHintFormat;
    private static MessageFormat sizeHintFormat;

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

    private DropTarget dropTarget;
    private NewComponentDropListener dropListener;
    
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
        Set<AWTKeyStroke> keys = new HashSet<AWTKeyStroke>();
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
        
        dropListener = new NewComponentDropListener();
        dropTarget = new DropTarget(this, dropListener);
    }

    void setViewOnly(boolean viewOnly) {
        if(this.viewOnly == viewOnly) {
            return;
        }
        if(viewOnly) {
            dropTarget.removeDropTargetListener(dropListener);            
        } else {
	    try {
		dropTarget.addDropTargetListener(dropListener);                                        
	    } catch (TooManyListenersException ex) {
		ex.printStackTrace();
	    }
        }
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

    @Override
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
            try {
                FormLAF.setUseDesignerDefaults(getFormModel());
                draggedComponent.paintFeedback(g2);
            } finally {
                FormLAF.setUseDesignerDefaults(null);
            }
        }
        else { // just paint the selection of selected components
            g2.setColor(formSettings.getSelectionBorderColor());
            g2.setStroke(getPaintStroke());
            boolean painted = false;
            try {
                boolean inLayout = selectedComponentsInSameVisibleContainer();
                Iterator metacomps = formDesigner.getSelectedComponents().iterator();
                while (metacomps.hasNext()) {
                    RADComponent metacomp = (RADComponent)metacomps.next();
                    RADVisualComponent layoutMetacomp = formDesigner.componentToLayoutComponent(metacomp);
                    if (layoutMetacomp != null)
                        metacomp = layoutMetacomp;
                    paintSelection(g2, metacomp, inLayout);
                }
                painted = true;
            } finally {
                // Make sure that problems in selection painting
                // doesn't cause endless stream of exceptions.
                if (!painted) {
                    formDesigner.clearSelection();
                }
            }

            if (selectionDragger != null)
                selectionDragger.paintDragFeedback(g2);
        }
    }

    /**
     * @param inLayout indicates whether to paint layout related decorations
     *        (layout relations in container and resize handles)
     */
    private void paintSelection(Graphics2D g, RADComponent metacomp, boolean inLayout) {
        if (!(metacomp instanceof RADVisualComponent) && !(metacomp instanceof RADMenuItemComponent))
            return;
        Object comp = formDesigner.getComponent(metacomp);
        if (!(comp instanceof Component))
            return;

        Component component = (Component) comp;
        Component parent = component.getParent();

        if (parent != null && component.isShowing()) {
            Rectangle selRect = component.getBounds();
            RADVisualContainer metacont = metacomp.getParentComponent() instanceof RADVisualContainer
                    ? (RADVisualContainer)metacomp.getParentComponent() : null;
            convertRectangleFromComponent(selRect, parent);
            Rectangle visible = new Rectangle(0, 0, parent.getWidth(), parent.getHeight());
            visible = convertVisibleRectangleFromComponent(visible, parent);

            if (inLayout
                && metacont != null && metacont.getLayoutSupport() == null
                && formDesigner.isInDesigner(metacont))
            {   // component in free design container - layout designer may want to paint something
                Component topComp = formDesigner.getTopDesignComponentView();
                Point convertPoint = convertPointFromComponent(0, 0, topComp);
                g.translate(convertPoint.x, convertPoint.y);
                LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
                Color oldColor = g.getColor();
                g.setColor(formSettings.getGuidingLineColor());
                Shape clip = g.getClip();
                visible.translate(-convertPoint.x, -convertPoint.y);
                Area area = new Area(visible);
                if (clip != null) {
                    area.intersect(new Area(clip));
                }
                g.setClip(area);
                layoutDesigner.paintSelection(g, metacomp.getId());
                g.setClip(clip);
                g.setColor(oldColor);
                visible.translate(convertPoint.x, convertPoint.y);
                g.translate(-convertPoint.x, -convertPoint.y);
            }
            int resizable = 0;
            if (inLayout) {
                resizable = getComponentResizable((RADVisualComponent)metacomp);
            }
            if (resizable == 0) {
                selRect = selRect.intersection(visible);
            }
            int correction = formSettings.getSelectionBorderSize() % 2;
            int x = selRect.x - correction;
            int y = selRect.y - correction;
            int width = selRect.width + correction;
            int height = selRect.height + correction;
            g.drawRect(x, y, width, height);
            if (inLayout) {
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

    void maskDraggingComponents() {
        if (draggedComponent != null) {
            draggedComponent.maskDraggingComponents();
        }
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
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
        } else if ((keyCode == KeyEvent.VK_CONTEXT_MENU)
                || ((keyCode == KeyEvent.VK_F10) && e.isShiftDown())) { // Shift F10 invokes context menu
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
        } else if (e.getID() == KeyEvent.KEY_PRESSED
                && (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP
                    || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT)) {
            // cursor keys
            if (e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
                // duplicating
                DuplicateAction.performAction(formDesigner.getSelectedComponentNodes(), keyCode);
                e.consume();
                return;
            }
        } else if (((keyCode == KeyEvent.VK_D) || (keyCode == KeyEvent.VK_E)) && e.isAltDown() && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            FormModel formModel = formDesigner.getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            if (layoutModel != null) {
                Map<String,String> idToNameMap = new HashMap<String,String>();
                for (RADComponent comp : formModel.getAllComponents()) {
                    if (comp != formModel.getTopRADComponent())
                        idToNameMap.put(comp.getId(), comp.getName());
                }
                System.out.println(layoutModel.dump(idToNameMap));
            }
        } else if (((keyCode == KeyEvent.VK_W)) && e.isAltDown() && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            // generate layout test (one checkpoint)
            if (formDesigner.getLayoutDesigner().logTestCode()) {
                FormModel formModel = formDesigner.getFormModel();
                LayoutModel layoutModel = formModel.getLayoutModel();
                if (layoutModel != null) {
                    Map<String,String> idToNameMap = new HashMap<String,String>();
                    for (RADComponent comp : formModel.getAllComponents()) {
                        idToNameMap.put(comp.getId(), comp.getName());
                    }
                    FormDataObject formDO = formDesigner.getFormEditor().getFormDataObject();
                    LayoutTestUtils.writeTest(formDesigner, formDO, idToNameMap, layoutModel);
                    LayoutDesigner ld = formDesigner.getLayoutDesigner();
                    ld.setModelCounter(ld.getModelCounter() + 1);
                }
            }
        } else if (((keyCode == KeyEvent.VK_S)) && e.isAltDown() && e.isControlDown() && (e.getID() == KeyEvent.KEY_PRESSED)) {
            // start layout test recording
            if (LayoutDesigner.testMode()) {
                FormDataObject formDO = formDesigner.getFormEditor().getFormDataObject();
                FileObject formFile = formDO.getFormFile();
                SaveCookie saveCookie = formDO.getCookie(SaveCookie.class);
                try {
                    if (saveCookie != null)
                        saveCookie.save();
                    FileObject copied = formFile.copy(LayoutTestUtils.getTargetFolder(formFile), 
                                formFile.getName() + "Test-StartingForm", // NOI18N
                                formFile.getExt()); 
                    formDesigner.getLayoutDesigner().setModelCounter(0);
                    formDesigner.resetTopDesignComponent(true);
                    StatusDisplayer.getDefault().setStatusText("The form was successfully copied to: " + copied.getPath()); // NOI18N
                } catch (IOException ioe) {
                    //TODO
                }
            }
        }

        super.processKeyEvent(e);
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    // -------

    /**
     * Returns metacomponent for visual component at given location.
     * @param point - location in component layer's coordinates
     * @param mode - defines what level in the hierarchy to prefer (in order to
     *        distinguish between the leaf components and their parents):
     *   COMP_DEEPEST - get the component which is the deepest in the hierarchy (leaf component)
     *   COMP_SELECTED - get the deepest selected component
     *   COMP_ABOVE_SELECTED - get the component above the deepest selected component
     *   COMP_UNDER_SELECTED - get the component under the deepest selected component
     * @returns the metacomponent at given point
     *   If no component is currently selected then:
     *     for COMP_SELECTED the deepest component is returned
     *     for COMP_ABOVE_SELECTED the deepest component is returned
     *     for COMP_UNDER_SELECTED the top component is returned
     */
    public RADComponent getMetaComponentAt(Point point, int mode) {
        Component[] deepComps = getDeepestComponentsAt(
                                    formDesigner.getComponentLayer(), point);
        if (deepComps == null) {
            return null;
        }

        int dIndex = 0;
        Component comp = deepComps[dIndex];

        // find the component satisfying point and mode
        RADComponent topMetaComp = formDesigner.getTopDesignComponent();
        RADComponent firstMetaComp = null;
        RADComponent currMetaComp;
        RADComponent prevMetaComp = null;

        do {
            currMetaComp = formDesigner.getMetaComponent(comp);
            if (currMetaComp != null && !isDraggedComponent(currMetaComp)) {
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
        Component deepestComp = SwingUtilities.getDeepestComponentAt(parent, point.x, point.y);
        if (deepestComp == null) {
            return null;
        }

        Container deepestParent = deepestComp.getParent();
        Point deepestPosition = SwingUtilities.convertPoint(parent, point, deepestParent);
        java.util.List<Component> compList = null; // in most cases there will be just one component
        for (int i=0, n=deepestParent.getComponentCount(); i < n; i++) {
            Component comp = deepestParent.getComponent(i);
            Point p = comp.getLocation();
            if (comp != deepestComp && comp.isVisible()
                    && comp.contains(deepestPosition.x - p.x, deepestPosition.y - p.y)) {
                if (compList == null) {
                    compList = new ArrayList<Component>(n - i + 1);
                    compList.add(deepestComp);
                }
                compList.add(comp);
            }
        }

        if (compList == null) { // just one component
            return new Component[] { deepestComp };
        } else {
            return compList.toArray(new Component[compList.size()]);
        }
    }

    private RADVisualContainer getMetaContainerAt(Point point, int mode) {
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
    private RADComponent selectComponent(MouseEvent e, boolean mousePressed) {
        RADComponent hitMetaComp;
        if (formDesigner.getSelectedComponents().size() > 1
            && mousePressed
            && !e.isShiftDown() && !e.isControlDown() && !e.isAltDown()) {
            // If multiple components already selected and some of them is on
            // current mouse position, keep this component selected on mouse
            // pressed (i.e. don't try to selected a possible subcomponent).
            // This is to ease dragging of multiple scrollpanes or containers
            // covered entirely by subcomponents.
            // OTOH mouse release should cancel the multiselection - if no
            // dragging happened.
            hitMetaComp = selectedComponentAt(e.getPoint(), 0, false);
            if (hitMetaComp != null) {
                return hitMetaComp;
            }
        }

        int selMode = !e.isAltDown() ? COMP_DEEPEST :
                (!e.isShiftDown() ? COMP_ABOVE_SELECTED : COMP_UNDER_SELECTED);
        hitMetaComp = getMetaComponentAt(e.getPoint(), selMode);

        // Help with selecting a component in scroll pane (e.g. JTable of zero size).
        // Prefer selcting the component rather than the scrollpane if the view port
        // or header is clicked.
        if (hitMetaComp != null && !e.isAltDown()
                && hitMetaComp.getAuxValue("autoScrollPane") != null // NOI18N
                && hitMetaComp instanceof RADVisualContainer) {
            RADVisualComponent[] sub = ((RADVisualContainer)hitMetaComp).getSubComponents();
            Component scroll = (Component) formDesigner.getComponent(hitMetaComp);
            if (sub.length > 0 && scroll instanceof JScrollPane) {
                Point p = e.getPoint();
                convertPointToComponent(p, scroll);
                Component clicked = SwingUtilities.getDeepestComponentAt(scroll, p.x, p.y);
                while (clicked != null && clicked != scroll) {
                    if (clicked instanceof JViewport) {
                        hitMetaComp = sub[0];
                        break;
                    }
                    clicked = clicked.getParent();
                }
            }
        }

        if ((e.isControlDown() || e.isShiftDown()) && !e.isAltDown()) {
            if (hitMetaComp != null) {
                // Shift adds to selection, Ctrl toggles selection,
                // other components selection is not affected
                if (!formDesigner.isComponentSelected(hitMetaComp)) {
                    formDesigner.addComponentToSelection(hitMetaComp);
                } else if (!e.isShiftDown()) {
                    formDesigner.removeComponentFromSelection(hitMetaComp);
                }
            }
        } else if (hitMetaComp != null) {
            formDesigner.setSelectedComponent(hitMetaComp);
        } else {
            formDesigner.clearSelection();
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
        Node[] selectedNode = new Node[] { formEditor.getOthersContainerNode() };
        
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
        if(formDesigner.getMenuEditLayer().isVisible()) {
            if(!formDesigner.getMenuEditLayer().isMenuLayerComponent(metacomp)) {
                formDesigner.getMenuEditLayer().hideMenuLayer();
            }
        }
        if(metacomp != null && metacomp.getBeanClass().getName().equals(javax.swing.JMenu.class.getName())) {
            formDesigner.openMenu(metacomp);
        }
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
        java.util.List<RADComponent> selectedComps = formDesigner.getSelectedComponents();
        java.util.List<RADComponent> workingComps = new ArrayList<RADComponent>(selectedComps.size());
        java.util.List<String> workingIds = null;
        RADVisualContainer parent = null;

 	//outside of a frame, there are no selected components so just return null
 	if(selectedComps.size() == 0) return null;

        for (Iterator it = selectedComps.iterator(); it.hasNext(); ) {
            RADComponent metacomp = (RADComponent) it.next();
            if (!(metacomp instanceof RADVisualComponent)) continue;
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
                    if (metacont == null || !formDesigner.isInDesigner((RADVisualComponent)metacomp)) {
                        return null; // out of visible tree
                    }
                    parent = metacont;
                    if (metacont.getLayoutSupport() == null) { // new layout
                        workingIds = new ArrayList(selectedComps.size());
                    }
                }
                workingComps.add(metacomp);
                if (workingIds != null) {
                    workingIds.add(metacomp.getId());
                }
            }
        }

        if (parent != null && parent.getLayoutSupport() == null) { // new layout may impose more limitation
            workingIds = formDesigner.getLayoutDesigner().getDraggableComponents(workingIds);
            if (workingIds.size() != workingComps.size()) {
                workingComps.clear();
                FormModel formModel = getFormModel();
                for (String compId : workingIds) {
                    workingComps.add(formModel.getMetaComponent(compId));
                }
            }
        }

        return workingComps.isEmpty() ? null :
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
            boolean retVal = true;
            try {
                retVal = draggedComponent.end(e);
            } finally {
                if (retVal) {
                    draggedComponent = null;
                    draggingEnded = true;
                } else {
                    done = false;
                }
            }
        }
        else if (selectionDragger != null) {
            if (e != null)
                selectionDragger.drop(e.getPoint());
            selectionDragger = null;
//                repaint();
        }

        if (done) {
            draggingEnded = true;
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        }

        FormEditor.getAssistantModel(getFormModel()).setContext("select"); // NOI18N
        return done;
    }

    private boolean isDraggedComponent(RADComponent metacomp) {
        if (draggedComponent != null && draggedComponent.movingComponents != null) {
            for (RADComponent c : draggedComponent.movingComponents) {
                if (c == metacomp || c.isParentComponent(metacomp))
                    return true;
            }
        }
        return false;
    }

    // Highlighted panel
    private JPanel darkerPanel = null;
    private static class HighlightBorder extends javax.swing.border.LineBorder {
        HighlightBorder(Color color, int thickness) {
            super(color, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // Hack - don't affect component's content
            return new Insets(0, 0, 0, 0);
        }
    }
    
    // Highlights panel below mouse cursor.
    private void highlightPanel(MouseEvent e, boolean recheck) {
        Component[] comps = getDeepestComponentsAt(formDesigner.getComponentLayer(), e.getPoint());
        if (comps == null) {
            return;
        }
        Component comp = comps[comps.length-1];
        RADComponent radcomp = formDesigner.getMetaComponent(comp);
        if ((radcomp != null) && !(radcomp instanceof RADVisualContainer)) {
            radcomp = radcomp.getParentComponent();
            comp = radcomp != null ? (Component)formDesigner.getComponent(radcomp) : null;
        }
        if ((radcomp == null) || (radcomp == formDesigner.getTopDesignComponent())
            || (!(comp instanceof JPanel))) {
            comp = null;
        }
        JPanel panel = (JPanel)comp;
        if ((darkerPanel != panel) || (recheck && !shouldHighlightPanel(panel, radcomp))) {
            if (darkerPanel != null) {
                // Reset only HighlightBorder border
                if (darkerPanel.getBorder() instanceof HighlightBorder) {
                    darkerPanel.setBorder(null);
                }
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
            if ((formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT)
                && formDesigner.getSelectedLayoutComponents().contains(radPanel)) {
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
                
                MessageFormat mf;
                if(viewOnly) {
                    if (sizeHintFormat == null){                    
                        sizeHintFormat = new MessageFormat(
                            FormUtils.getBundleString("FMT_HINT_DesignerSize")); // NOI18N                                            
                    }                                           
                    mf = sizeHintFormat;                    
                } else {
                    if (resizingHintFormat == null){                    
                        resizingHintFormat = new MessageFormat(
                            FormUtils.getBundleString("FMT_HINT_DesignerResizing")); // NOI18N                                            
                    } 
                    mf = resizingHintFormat;                                        
                }
                   
                String hint = mf.format(
                                new Object[] { new Integer(size.width),
                                               new Integer(size.height) });
                setToolTipText(hint);
                ToolTipManager.sharedInstance().mouseEntered(e);
            }
        }
        else if (getToolTipText() != null)
            setToolTipText(null);

        if (resizing != 0 && !viewOnly)
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
        if (!selectedComponentsInSameVisibleContainer())
            return 0;

        Point p = e.getPoint();
        RADComponent compAtPoint = selectedComponentAt(p, 6, true);

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

    private boolean selectedComponentsInSameVisibleContainer() {
        RADVisualContainer parent = null;
        Iterator selected = formDesigner.getSelectedComponents().iterator();
        while (selected.hasNext()) {
            RADVisualComponent comp = formDesigner.componentToLayoutComponent((RADComponent)selected.next());
            if (comp == null)
                return false; // not visible in designer
            if (parent == null) {
                parent = comp.getParentContainer();
                if (!formDesigner.isInDesigner(parent)) {
                    return false; // not visible in designer
                }
            }
            else if (comp.getParentContainer() != parent) {
                return false; // different parent
            }
        }
        return true;
    }

    // Returns selected component at the given point (even outside the designer area).
    private RADComponent selectedComponentAt(Point p, int borderSize, boolean inLayout) {
        RADComponent compAtPoint = null;
        Iterator selected = (inLayout ? formDesigner.getSelectedLayoutComponents()
                                      : formDesigner.getSelectedComponents())
                .iterator();
        while (selected.hasNext()) {
            RADComponent metacomp = (RADComponent) selected.next();
            if (metacomp instanceof RADVisualComponent && formDesigner.isInDesigner((RADVisualComponent)metacomp)) {
                Component comp = (Component)formDesigner.getComponent(metacomp);
                Rectangle rect = new Rectangle(-borderSize, -borderSize, comp.getWidth()+2*borderSize, comp.getHeight()+2*borderSize);
                convertRectangleFromComponent(rect, comp);
                if (rect.contains(p)) {
                    compAtPoint = metacomp;
                }
            }
        }
        return compAtPoint;
    }

    // Check how possible component resizing (obtained from layout support)
    // matches with mouse position on component selection border. 
    private int getComponentResizable(Point p, RADVisualComponent metacomp) {
//        RADVisualContainer metacont = metacomp.getParentContainer();
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
        int resizable = 0;
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
        }
        else { // old layout support
            Container cont = (Container) formDesigner.getComponent(metacont);
            if (cont != null) { // might be null if component just enclosed in container not yet cloned
                Container contDel = metacont.getContainerDelegate(cont);

                resizable = laySup.getResizableDirections(
                                           cont, contDel,
                                           comp, metacont.getIndexOf(metacomp));
            }
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
                        formDesigner.setDesignerSize(size, null);
                        setToolTipText(null);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
                catch (NumberFormatException ex) {} // silently ignore, do nothing
            }
        }
    }

    private Object getConstraintsAtPoint(RADComponent metacomp, Point point, Point hotSpot) {
        if (!(metacomp instanceof RADVisualComponent))
            return null;

        RADVisualContainer metacont = metacomp instanceof RADVisualContainer ?
            (RADVisualContainer) metacomp :
            (RADVisualContainer) metacomp.getParentComponent();
        LayoutSupportManager laysup = metacont != null ?
                                      metacont.getLayoutSupport() : null;

            Container cont = (Container) formDesigner.getComponent(metacont);
            Container contDel = metacont.getContainerDelegate(cont);
            Point p = convertPointToComponent(point.x, point.y, contDel);
            Object constraints = laysup.getNewConstraints(cont, contDel, null, -1, p, hotSpot);
            if ((constraints == null) && metacomp.getBeanInstance() instanceof Component) {
                int index = laysup.getNewIndex(cont, contDel, (Component)metacomp.getBeanInstance(), -1, p, hotSpot);
                if (index != -1) {
                    constraints = new Integer(index);
                }
            }
            return constraints;
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
    Rectangle convertRectangleToComponent(Rectangle rect,
                                                  Component targetComp)
    {
        Point p = convertPointToComponent(rect.x, rect.y, targetComp);
        rect.x = p.x;
        rect.y = p.y;
        return rect;
    }

    Rectangle convertVisibleRectangleFromComponent(Rectangle rect, Component comp) {
        Component parent;
        while (!formDesigner.isCoordinatesRoot(comp)) {
            parent = comp.getParent();
            Rectangle size = new Rectangle(0, 0, parent.getWidth(), parent.getHeight());
            rect.translate(comp.getX(), comp.getY());
            rect = rect.intersection(size);
            comp = parent;
        }
        comp = this;
        while (!formDesigner.isCoordinatesRoot(comp)) {
            rect.translate(-comp.getX(), -comp.getY());
            comp = comp.getParent();
        }
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
        highlightPanel(e, true);
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
                    && !modifier
                    && !viewOnly)
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
                         && !e.isAltDown()
                         && !e.isControlDown())
                {   // plain click or shift click
                    selectComponent(e, false);
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
            PaletteItem item = PaletteUtils.getSelectedItem();
            if(formDesigner.getMenuEditLayer().isPossibleNewMenuComponent(item)) {
                formDesigner.getMenuEditLayer().startNewMenuComponentPickAndPlop(item,e.getPoint());
                return;
            }
            if( null != item ) {
                StatusDisplayer.getDefault().setStatusText(
                    FormUtils.getFormattedBundleString(
                        "FMT_MSG_AddingComponent", // NOI18N
                        new String[] { item.getNode().getDisplayName() }));
            }
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
                } else {
                    // [we used to only select the component if there was nothing selected
                    //  on current position, but changed to always select - #94543]
                    RADComponent hitMetaComp = selectComponent(e, true);
                    processMouseClickInLayoutSupport(hitMetaComp, e);
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
                        if (!mouseOnVisual(lastLeftMousePoint)) {
                            if ((resizeType == 0) && (selectedComponentAt(lastLeftMousePoint, 0, true) == null))
                                selectOtherComponentsNode();
                        }
                        // Shift+left is reserved for interval or area selection,
                        // applied on mouse release or mouse dragged; ignore it here.
                        else if (resizeType == 0 // no resizing
                                 && (e.getClickCount() != 2 || !processDoubleClick(e)) // no doubleclick
                                 && (!e.isShiftDown() || e.isAltDown())) {
                            RADComponent hitMetaComp = selectComponent(e, true); 
                            if (!modifier) { // plain single click
                                processMouseClickInLayoutSupport(hitMetaComp, e);
                            }
                        }
                    }
                }
//                endDragging(null); // for sure
                draggingEnded = false; // reset flag preventing dragging from start
            }
            else if (!viewOnly) { // form can be modified
                if (formDesigner.getDesignerMode() == FormDesigner.MODE_CONNECT) {
                    selectComponent(e, true);
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
                RADVisualComponent[] draggedComps =
                    (resizeType & DESIGNER_RESIZING) == 0 ? getComponentsToDrag() :
                    new RADVisualComponent[] { formDesigner.getTopDesignComponent() };
                if (draggedComps != null) {
                    if (resizeType == 0) {
                        draggedComponent = new ExistingComponentDrag(
                            draggedComps, lastLeftMousePoint, e.getModifiers());
                    }
                    else  {
                        draggedComponent = new ResizeComponentDrag(
                            draggedComps, lastLeftMousePoint, resizeType&~DESIGNER_RESIZING);
                    }
                }
            }
            if (draggedComponent == null // component dragging has not started
                && lastLeftMousePoint.distance(p) > 4
                && !e.isAltDown() && !e.isControlDown()) {
                // check for possible selection dragging
                RADComponent topComp = formDesigner.getTopDesignComponent();
                RADComponent comp = getMetaComponentAt(lastLeftMousePoint, COMP_DEEPEST);
                if (topComp != null
                    && (e.isShiftDown() || comp == null || comp == topComp || comp.getParentComponent() == null)) {
                    // start selection dragging
                    selectionDragger = new SelectionDragger(lastLeftMousePoint);
                }
            }
        }

        if (draggedComponent != null) {
            draggedComponent.move(e);
            highlightPanel(e, false);
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
            PaletteItem item = PaletteUtils.getSelectedItem();
            if( null == item ) {
                if( null != draggedComponent ) {
                    endDragging( e );
                }
                return;
            }
            if (draggedComponent == null) {
                // first move event, pre-create visual component to be added
                if ((item.getComponentClassName().indexOf('.') == -1) // Issue 79573
                    && (!FormJavaSource.isInDefaultPackage(getFormModel()))) {
                    String message = FormUtils.getBundleString("MSG_DefaultPackageBean"); // NOI18N
                    NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    formDesigner.toggleSelectionMode();
                    return;
                }
                draggedComponent = new NewComponentDrag( item );
            }
            draggedComponent.move(e);
            repaint();
        }
        else if (formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT
                 && !anyDragger())
        {
            checkResizing(e);
        }
        highlightPanel(e, false);
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

    @Override
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
                java.util.List<RADComponent> toSelect = new ArrayList<RADComponent>();
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
                                                  java.util.List<RADComponent> toSelect)
        {
            java.util.List<Component> subContainers = new ArrayList<Component>();

            Component[] comps = cont.getComponents();
            for (int i=0; i < comps.length; i++) {
                Component comp = comps[i];
                Rectangle bounds = convertRectangleFromComponent(
                                       comps[i].getBounds(), cont);
                boolean intersects = selRect.intersects(bounds);

                RADComponent metacomp = formDesigner.getMetaComponent(comp);
                if (metacomp != null && intersects) {
                    toSelect.add(metacomp);
                }

                if (intersects && comp instanceof Container)
                    subContainers.add(comp);
            }

            if (toSelect.size() > 1
                    || (toSelect.size() == 1 && subContainers.size() == 0))
                return true;

            RADComponent theOnlyOne = toSelect.size() == 1 ? toSelect.get(0) : null;

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
        boolean draggableLayoutComponents;
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
            if (formDesigner.getTopDesignComponentView() == null) {
                convertPoint = new Point(0,0);
            } else {
                convertPoint = convertPointFromComponent(0, 0, formDesigner.getTopDesignComponentView());
            }
        }

        // ctor for moving and resizing
        ComponentDrag(RADVisualComponent[] components, Point hotspot) {
            this();
            setMovingComponents(components);

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

        final void setMovingComponents(RADVisualComponent[] components) {
            this.movingComponents = components;
            if (components != null && components.length > 0 && components[0] != null) {
                draggableLayoutComponents = !components[0].isMenuComponent();
            } else {
                draggableLayoutComponents = false;
            }
        }

        final RADVisualContainer getSourceContainer() {
            return movingComponents != null && movingComponents.length > 0
                   && formDesigner.getTopDesignComponent() != movingComponents[0]
                ? movingComponents[0].getParentContainer() : null;
        }

        final boolean isTopComponent() {
            return movingComponents != null && movingComponents.length > 0
                   && formDesigner.getTopDesignComponent() == movingComponents[0];
        }

        final boolean isDraggableLayoutComponent() {
            return draggableLayoutComponents;
        }

        final RADVisualContainer getTargetContainer(Point p, int modifiers) {
            if (fixedTarget != null) {
                return fixedTarget;
            }
            int mode = ((modifiers & InputEvent.ALT_MASK) != 0) ? COMP_SELECTED : COMP_DEEPEST;
            RADVisualContainer metacont = HandleLayer.this.getMetaContainerAt(p, mode);
            if ((metacont != null) && (metacont.getLayoutSupport() == null)) {
                RADVisualContainer dirMetacont = HandleLayer.this.getMetaContainerAt(
                        getMoveDirectionSensitivePoint(p, modifiers), mode);
                if ((dirMetacont != null) && (dirMetacont.getLayoutSupport() == null)) {
                    metacont = dirMetacont;
                }
            }
            if (movingComponents != null) {
                java.util.List comps = Arrays.asList(movingComponents);
                while (comps.contains(metacont)) {
                    metacont = metacont.getParentContainer();
                }
            }
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

            
            // support for highlights in menu containers
            // hack: this only checks the first component.
            if(this.movingComponents != null) {
                RADVisualComponent moveComp = this.movingComponents[0];
                // if  have a menu component over a menu container then do a highlight
                // hack: this only works for new comps, not moving existing comps
                if(newDrag && formDesigner.getMenuEditLayer().canHighlightContainer(targetContainer,moveComp)) {
                    formDesigner.getMenuEditLayer().rolloverContainer(targetContainer);
                } else {
                    formDesigner.getMenuEditLayer().rolloverContainer(null);
                }
            }
            
            if (newDrag && isDraggableLayoutComponent()
                    && targetContainer != null && targetContainer.getLayoutSupport() == null) {
                p.x -= convertPoint.x;
                p.y -= convertPoint.y;
                formDesigner.getLayoutDesigner().move(p,
                                                      targetContainer.getId(),
                                                      ((modifiers & InputEvent.ALT_MASK) == 0),
                                                      ((modifiers & InputEvent.CTRL_MASK) != 0),
                                                      movingBounds);
                String[] position = formDesigner.getLayoutDesigner().positionCode();
                FormEditor.getAssistantModel(getFormModel()).setContext(position[0], position[1]);
            }
            else if (oldDrag && isDraggableLayoutComponent()
                     && targetContainer != null && targetContainer.getLayoutSupport() != null) {
                oldMove(p);
                for (int i=0; i<movingBounds.length; i++) {
                    movingBounds[i].x = p.x - convertPoint.x - hotSpot.x + originalBounds[i].x - convertPoint.x;
                    movingBounds[i].y = p.y - convertPoint.y - hotSpot.y + originalBounds[i].y - convertPoint.y;
                }
            }
            else {
                FormEditor.getAssistantModel(getFormModel()).setContext("generalPosition"); // NOI18N
                for (int i=0; i<movingBounds.length; i++) {
                    movingBounds[i].x = p.x - convertPoint.x - hotSpot.x + originalBounds[i].x - convertPoint.x;
                    movingBounds[i].y = p.y - convertPoint.y - hotSpot.y + originalBounds[i].y - convertPoint.y;
                }
            }
        }

        final void maskDraggingComponents() {
            if (!isTopComponent() && showingComponents != null) {
                for (int i=0; i < showingComponents.length; i++) {
                    Rectangle r = movingBounds[i];
                    showingComponents[i].setBounds(r.x + Short.MIN_VALUE, r.y + Short.MIN_VALUE, r.width, r.height);
                }
            }
        }

        final void paintFeedback(Graphics2D g) {
            if ((movingBounds.length < 1) || (movingBounds[0].x == Integer.MIN_VALUE))
                return;

            for (int i=0; i<showingComponents.length; i++) {
                Graphics gg = g.create(movingBounds[i].x + convertPoint.x,
                                       movingBounds[i].y + convertPoint.y,
                                       movingBounds[i].width + 1,
                                       movingBounds[i].height + 1);

                if (newDrag && isDraggableLayoutComponent()
                    && ((targetContainer != null && targetContainer.getLayoutSupport() == null)
                        || (targetContainer == null && isTopComponent())))
                {   // new layout support
                    // paint the component being moved
                    if (!isTopComponent()) {
                        doLayout(showingComponents[i]);
                        paintDraggedComponent(showingComponents[i], gg);
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
                else if (oldDrag && isDraggableLayoutComponent()
                        && ((targetContainer != null && targetContainer.getLayoutSupport() != null)
                             || (targetContainer == null && isTopComponent()))) {
                    if (!isTopComponent()) {
                        doLayout(showingComponents[i]);
                        oldPaintFeedback(g, gg, i);
                    }
                }
                else { // non-visual area
                    doLayout(showingComponents[i]);
                    paintDraggedComponent(showingComponents[i], gg);
                }
            }
        }

        final boolean end(final MouseEvent e) {
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
                // re-init in next AWT round - to have the designer updated
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        init();
                        move(e);
                    }
                });
            }
            return retVal;
        }

        // methods to extend/override ---

        void init() {
            if (showingComponents != null) {
                // showing components need to be in a container to paint
                // correctly (relates to newly added components);
                // components in old layout need to be hidden
                RADVisualContainer sourceCont = getSourceContainer();
                boolean oldSource = sourceCont != null && sourceCont.getLayoutSupport() != null;
                dragPanel.removeAll();
                for (int i=0; i < showingComponents.length; i++) {
                    Component comp = showingComponents[i];
                    if (comp.getParent() == null) {
                        dragPanel.add(comp);
                    }
                    else if (oldSource) {
                        comp.setVisible(false);
                        // VisualReplicator makes it visible again...
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
            // clear the rollover just in case it was set
            formDesigner.getMenuEditLayer().clearRollover();
            return true;
        }

        void oldMove(Point p) {
        }

        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
        }

        // layout model undo/redo ---

        final void createLayoutUndoableEdit() {
            layoutUndoMark = getLayoutModel().getChangeMark();
            layoutUndoEdit = getLayoutModel().getUndoableEdit();
        }

        final void placeLayoutUndoableEdit(boolean autoUndo) {
            if (!layoutUndoMark.equals(getLayoutModel().getChangeMark())) {
                getFormModel().addUndoableEdit(layoutUndoEdit);
            }
            if (autoUndo) {
                getFormModel().forceUndoOfCompoundEdit();
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

    private static void paintDraggedComponent(Component comp, Graphics g) {
        try {
            if (comp instanceof JComponent)
                comp.paint(g);
            else
                comp.getPeer().paint(g);
        }
        catch (RuntimeException ex) { // inspired by bug #62041 (JProgressBar bug #5035852)
            org.openide.ErrorManager.getDefault().notify(
                org.openide.ErrorManager.INFORMATIONAL, ex);
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

        @Override
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
                    layoutComps, originalBounds, hotSpot, null);                    
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

        @Override
        boolean end(Point p, int modifiers) {    
            // clear the rollover just in case it was set
            formDesigner.getMenuEditLayer().clearRollover();
            
            RADVisualContainer originalCont = getSourceContainer();
            // fail if trying to move a menu component to a non-menu container
            if(MenuEditLayer.containsMenuTypeComponent(movingComponents)) {
                if(!MenuEditLayer.isValidMenuContainer(targetContainer)) {
                    formDesigner.getLayoutDesigner().endMoving(false);
                    formDesigner.updateContainerLayout(originalCont);
                    return true;
                }
            }
            // fail if trying to move a non-menu component into a menu container
            if(!MenuEditLayer.containsMenuTypeComponent(movingComponents)) {
                if(MenuEditLayer.isValidMenuContainer(targetContainer)) {
                    formDesigner.getLayoutDesigner().endMoving(false);
                    formDesigner.updateContainerLayout(originalCont);
                    return true;
                }
            }
            if (p != null) {
                if (targetContainer == null || targetContainer.getLayoutSupport() != null) {
                    // dropped in old layout support, or on non-visual area
                    createLayoutUndoableEdit();
                    boolean autoUndo = true;
                    try {
                        formDesigner.getLayoutDesigner().removeDraggedComponents();
                        oldDragger.dropComponents(p, targetContainer);
                        autoUndo = false;
                    } finally {
                        placeLayoutUndoableEdit(autoUndo);
                    }
                }
                else { // dropped in new layout support
                    if (targetContainer != originalCont) {
                        for (int i=0; i < movingComponents.length; i++) {
                            getFormModel().removeComponent(movingComponents[i], false);
                        }
                        // Issue 69410 (don't mix remove/add chnages)
                        for (int i=0; i < movingComponents.length; i++) {
                            getFormModel().addVisualComponent(movingComponents[i], targetContainer, null, false);
                        }
                    }
                    createLayoutUndoableEdit();
                    boolean autoUndo = true;
                    try {
                        formDesigner.getLayoutDesigner().endMoving(true);
                        autoUndo = false;
                    } finally {
                        getFormModel().fireContainerLayoutChanged(targetContainer, null, null, null);
                        placeLayoutUndoableEdit(autoUndo);
                    }
                }
            }
            else { // canceled
                formDesigner.getLayoutDesigner().endMoving(false);
                formDesigner.updateContainerLayout(originalCont); //, false);
            }

            return true;
        }

        @Override
        void oldMove(Point p) {
            oldDragger.drag(p, targetContainer);
        }

        @Override
        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
            oldDragger.paintDragFeedback(g);

            // don't paint if component dragged from old layout (may have strange size)
            Component comp = showingComponents[index];
            paintDraggedComponent(comp, gg);
        }
    }

    // for resizing existing components
    private class ResizeComponentDrag extends ComponentDrag {
        private int resizeType;
        private Dimension originalSize;

        private ComponentDragger oldDragger; // drags components in the old layout support

        ResizeComponentDrag(RADVisualComponent[] comps,
                            Point hotspot, // in HandleLayer coordinates
                            int resizeType)
        {
            super(comps, hotspot);
            this.resizeType = resizeType;
            init();
        }

        @Override
        void init() {
            RADVisualContainer sourceCont = getSourceContainer();
            if (isTopComponent()) {
                LayoutModel layoutModel = getLayoutModel();
                newDrag = layoutModel != null
                          && layoutModel.getLayoutComponent(movingComponents[0].getId()) != null;
                oldDrag = !newDrag;
                fixedTarget = null;
                originalSize = formDesigner.getComponentLayer().getDesignerSize();
            }
            else if (sourceCont != null) {
                if (sourceCont.getLayoutSupport() == null) {
                    newDrag = true;
                }
                else {
                    oldDrag = true;
                }
                fixedTarget = sourceCont;
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
                    compIds, originalBounds, hotSpot, res, sourceCont != null);

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

        @Override
        boolean end(Point p, int modifiers) {
            if (p != null) {
                if (newDrag) { // new layout support
                    // make sure the visual component has the current size set 
                    // (as still being in its container the layout manager tries to
                    // restore the original size)
                    showingComponents[0].setSize(movingBounds[0].width, movingBounds[0].height);
                    doLayout(showingComponents[0]);

                    createLayoutUndoableEdit();
                    boolean autoUndo = true;
                    try {
                        formDesigner.getLayoutDesigner().endMoving(true);
                        for (int i=0; i < movingComponents.length; i++) {
                            RADVisualComponent metacomp = movingComponents[i];
                            if (metacomp instanceof RADVisualContainer) {
                                RADVisualContainer visCont = (RADVisualContainer) metacomp;
                                if (visCont.getLayoutSupport() == null) {
                                    getFormModel().fireContainerLayoutChanged(
                                        visCont, null, null, null);
                                }
                            }
                        }
                        autoUndo = false;
                    } finally {
                        if (targetContainer != null) {
                            getFormModel().fireContainerLayoutChanged(targetContainer, null, null, null);
                        }
                        placeLayoutUndoableEdit(autoUndo);
                    }
                }
                else { // old layout support
                    if (targetContainer != null) {
                        oldDragger.dropComponents(p, targetContainer);
                    }
                }
                if (isTopComponent()) {
                    formDesigner.setDesignerSize(new Dimension(movingBounds[0].width, movingBounds[0].height),
                                                 originalSize);
                }
            }
            else { // resizing canceled
                formDesigner.getLayoutDesigner().endMoving(false);

                if (isTopComponent()) {
                    // just revert ComponentLayer's designer size (don't need to go through FormDesigner)
                    ComponentLayer compLayer = formDesigner.getComponentLayer();
                    if (!compLayer.getDesignerSize().equals(originalSize)) {
                        compLayer.setDesignerSize(originalSize);
                        compLayer.revalidate();
                    }
                    compLayer.repaint();
                }
                else { // add resized component back
                    formDesigner.updateContainerLayout(getSourceContainer()); //, false);
                }
            }
            return true;
        }

        @Override
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
            } else if (oldDrag && (targetContainer = getTargetContainer(p, modifiers)) != null && targetContainer.getLayoutSupport() != null) {
                oldMove(p);
                for (int i=0; i<movingBounds.length; i++) {
                    int xchange = p.x - convertPoint.x - hotSpot.x;
                    if ((resizeType & LayoutSupportManager.RESIZE_LEFT) != 0) {
                        movingBounds[i].x = originalBounds[i].x - convertPoint.x + xchange;
                        xchange = -xchange;
                    } else {
                        movingBounds[i].x = originalBounds[i].x - convertPoint.x;
                    }
                    if ((resizeType & (LayoutSupportManager.RESIZE_RIGHT | LayoutSupportManager.RESIZE_LEFT)) != 0) {
                        movingBounds[i].width = originalBounds[i].width + xchange;
                    }
                    int ychange = p.y - convertPoint.y - hotSpot.y;
                    if ((resizeType & LayoutSupportManager.RESIZE_UP) != 0) {
                        movingBounds[i].y = originalBounds[i].y - convertPoint.y + ychange;
                        ychange = -ychange;
                    } else {
                        movingBounds[i].y = originalBounds[i].y - convertPoint.y;
                    }
                    if ((resizeType & (LayoutSupportManager.RESIZE_DOWN | LayoutSupportManager.RESIZE_UP)) != 0) {
                        movingBounds[i].height = originalBounds[i].height + ychange;
                    }
                }
            } else {
                super.move(p, modifiers);
            }
        }

        @Override
        void oldMove(Point p) {
            oldDragger.drag(p, targetContainer);
            FormEditor.getAssistantModel(getFormModel()).setContext("generalResizing"); // NOI18N
        }

        @Override
        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
            paintDraggedComponent(showingComponents[index], gg);
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

        @Override
        void init() { // can be re-inited
            RADVisualComponent precreated =
                getComponentCreator().precreateVisualComponent(
                    paletteItem.getComponentClassSource());

            if (precreated != null) {
                if (movingComponents == null) {
                    setMovingComponents(new RADVisualComponent[] { precreated });
                } else { // continuing adding - new instance of the same component
                    movingComponents[0] = precreated;
                }
                 
                LayoutComponent precreatedLC = isDraggableLayoutComponent()
                        ? getComponentCreator().getPrecreatedLayoutComponent() : null;
                // (precreating LayoutComponent also adjusts the initial size of
                // the visual component which is used below)

                showingComponents[0] = (Component) precreated.getBeanInstance();
                // Force creation of peer - AWT components don't have preferred size otherwise
                if (!(showingComponents[0] instanceof JComponent)) {
                    FakePeerSupport.attachFakePeer(showingComponents[0]);
                    if (showingComponents[0] instanceof Container) {
                        FakePeerSupport.attachFakePeerRecursively((Container)showingComponents[0]);
                    }
                }

                Dimension size = showingComponents[0].getPreferredSize();
                if (originalBounds == null) { // new adding
                    hotSpot = new Point();
                    originalBounds = new Rectangle[] { new Rectangle(convertPoint.x, convertPoint.y, size.width, size.height) };
                    movingBounds = new Rectangle[] { new Rectangle(0, 0, size.width, size.height) };
                }
                else { // repeated adding of the same component type, reuse last bounds
                    movingBounds[0].width = size.width;
                    movingBounds[0].height = size.height;
                    originalBounds[0] = movingBounds[0];
                    movingBounds[0] = new Rectangle(movingBounds[0]);
                    originalBounds[0].x += convertPoint.x;
                    originalBounds[0].y += convertPoint.y;
                }
                compoundBounds = originalBounds[0];
                hotSpot.x = movingBounds[0].x + size.width/2 - 4;
                hotSpot.y = movingBounds[0].y + size.height/2;
                if (hotSpot.x < movingBounds[0].x)
                    hotSpot.x = movingBounds[0].x;

                if (precreatedLC != null) {
                    LayoutComponent[] layoutComponents = new LayoutComponent[] { precreatedLC };
                    if (formDesigner.getLayoutDesigner() != null) {
                        formDesigner.getLayoutDesigner().startAdding(
                                layoutComponents, movingBounds, hotSpot,
                                targetContainer != null ? targetContainer.getId() : null);
                    }
                }

                newDrag = oldDrag = true;
            }
            else {
                if (paletteItem.getComponentClass() != null) {
                    // non-visual component - present it as icon
                    Node node = paletteItem.getNode();
                    Image icon;
                    if (node == null) {
                        icon = paletteItem.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
                        if (icon == null) {
                            icon = Utilities.loadImage("org/netbeans/modules/form/resources/form.gif"); // NOI18N
                        }
                    } else {
                        icon = node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
                    }
                    showingComponents[0] = new JLabel(new ImageIcon(icon));
                    Dimension dim = showingComponents[0].getPreferredSize();
                    hotSpot = new Point(dim.width/2, dim.height/2);
                    if (hotSpot.x < 0) {
                        hotSpot.x = 0;
                    }
                    originalBounds = new Rectangle[] { new Rectangle(convertPoint.x, convertPoint.y, dim.width, dim.height) };
                    showingComponents[0].setBounds(originalBounds[0]);
                    movingBounds = new Rectangle[] { showingComponents[0].getBounds() };

                    newDrag = oldDrag = false;
                } else {
                    // The corresponding class cannot be loaded - cancel the drag.
                    showingComponents = null;
                    movingBounds = new Rectangle[0];
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            endDragging(null);
                        }
                    });
                }
            }

            super.init();
        }

        /** Overrides end(Point,int) in ComponentDrag to support adding new components
         */
        @Override
        boolean end(Point p, int modifiers) {
            // clear the rollover just in case it was set
            formDesigner.getMenuEditLayer().clearRollover();
            
            if (p != null) {
                targetContainer = getTargetContainer(p, modifiers);

                if (movingComponents != null) { // there is a precreated visual component
                    boolean newLayout;
                    boolean oldLayout;
                    Object constraints; // for old layout
                    if (targetContainer != null) {
                        newLayout = targetContainer.getLayoutSupport() == null;
                        oldLayout = !newLayout;
                        constraints = oldLayout && isDraggableLayoutComponent()
                            ? getConstraintsAtPoint(targetContainer, p, hotSpot) : null;
                    }
                    else {
                        newLayout = oldLayout = false;
                        constraints = null;
                    }
                    addedComponent = movingComponents[0];
                    LayoutComponent layoutComponent = isDraggableLayoutComponent()
                            ? getComponentCreator().getPrecreatedLayoutComponent() : null;
                    // add the component to FormModel
                    boolean added = getComponentCreator().addPrecreatedComponent(targetContainer, constraints);
                    // add the cmponent to LayoutModel
                    if (layoutComponent != null && getLayoutModel() != null) { // Some beans don't have layout
                        createLayoutUndoableEdit();
                        boolean autoUndo = true;
                        try {
                            formDesigner.getLayoutDesigner().endMoving(added && newLayout);
                            if (added) {
                                if (layoutComponent.isLayoutContainer()) {
                                    if (!newLayout) { // always add layout container to the model 
                                        getLayoutModel().addRootComponent(layoutComponent);
                                    }
                                }
                            } else {
                                repaint();
                            }
                            autoUndo = false;
                        } finally {
                            placeLayoutUndoableEdit(autoUndo);
                        }
                    }
                }
                else { // component not precreated ...
                    RADComponent targetComponent = targetContainer;
                    if (javax.swing.border.Border.class.isAssignableFrom(paletteItem.getComponentClass())) {
                        int mode = ((modifiers & InputEvent.ALT_MASK) != 0) ? COMP_SELECTED : COMP_DEEPEST;
                        targetComponent = HandleLayer.this.getMetaComponentAt(p, mode);
                    }
                    addedComponent = getComponentCreator().createComponent(
                            paletteItem.getComponentClassSource(), targetComponent, null);
                    if (addedComponent == null) {
                        repaint();
                    }
                }

                if (addedComponent != null) {
                    java.beans.BeanDescriptor bDesc = addedComponent.getBeanInfo().getBeanDescriptor();
                    if ((bDesc != null) && (bDesc.getValue("customizeOnCreation") != null)) { // NOI18N
                        modifiers &= ~InputEvent.SHIFT_MASK;
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                RADComponentNode node = addedComponent.getNodeReference();
                                if (node.hasCustomizer()) {
                                    org.openide.nodes.NodeOperation.getDefault().customize(node);
                                }
                            }
                        });
                    }
                }
                if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
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
            formDesigner.toggleSelectionMode();
            return true;
        }

        @Override
        void oldMove(Point p) {
            LayoutSupportManager laysup = targetContainer.getLayoutSupport();
            Container cont = (Container) formDesigner.getComponent(targetContainer);
            Container contDel = targetContainer.getContainerDelegate(cont);
            Point posInCont = convertPointToComponent(p.x, p.y, contDel);
            Point posInComp = hotSpot;
            index = laysup.getNewIndex(cont, contDel,
                                       showingComponents[0], -1,
                                       posInCont, posInComp);
            constraints = laysup.getNewConstraints(cont, contDel,
                                                   showingComponents[0], -1,
                                                   posInCont, posInComp);
        }

        @Override
        void oldPaintFeedback(Graphics2D g, Graphics gg, int index) {
            LayoutSupportManager laysup = targetContainer.getLayoutSupport();
            Container cont = (Container) formDesigner.getComponent(targetContainer);
            Container contDel = targetContainer.getContainerDelegate(cont);
            Point contPos = convertPointFromComponent(0, 0, contDel);
            g.setColor(formSettings.getSelectionBorderColor());
            g.setStroke(ComponentDragger.dashedStroke1);
            g.translate(contPos.x, contPos.y);
            laysup.paintDragFeedback(cont, contDel,
                                     showingComponents[0],
                                     constraints, this.index,
                                     g);
            g.translate(-contPos.x, -contPos.y);
//                    g.setStroke(stroke);
            paintDraggedComponent(showingComponents[0], gg);
        }
    }
    
    private class NewComponentDropListener implements DropTargetListener {
        private NewComponentDrop newComponentDrop;
        private int dropAction;
        /** Assistant context requested by newComponentDrop. */
        private String dropContext;
        /** Additional assistant context requested by newComponentDrop. */
        private String additionalDropContext;
        
        public void dragEnter(DropTargetDragEvent dtde) {
            try {
                dropAction = dtde.getDropAction();
                newComponentDrop = null;
                Transferable transferable = dtde.getTransferable();
                PaletteItem item = null;
                if (dtde.isDataFlavorSupported(PaletteController.ITEM_DATA_FLAVOR)) {
                    Lookup itemLookup = (Lookup)transferable.getTransferData(PaletteController.ITEM_DATA_FLAVOR);
                    item = itemLookup.lookup(PaletteItem.class);
                } else {
                    ClassSource classSource = CopySupport.getCopiedBeanClassSource(transferable);
                    if (classSource != null) {
                        Class componentClass = getComponentCreator().prepareClass(classSource);
                        if (componentClass != null) {
                            item = new PaletteItem(classSource, componentClass);
                        }
                    } else {
                        Lookup.Template<NewComponentDropProvider> template = new Lookup.Template<NewComponentDropProvider>(NewComponentDropProvider.class);
                        Collection<? extends NewComponentDropProvider> providers = Lookup.getDefault().lookup(template).allInstances();
                        for (NewComponentDropProvider provider : providers) {
                            newComponentDrop = provider.processTransferable(getFormModel(), transferable);
                            if (newComponentDrop != null) {
                                dropContext = null;
                                AssistantModel aModel = FormEditor.getAssistantModel(getFormModel());
                                String preContext = aModel.getContext();
                                item = newComponentDrop.getPaletteItem(dtde);
                                String postContext = aModel.getContext();
                                if (!preContext.equals(postContext)) {
                                    dropContext = postContext;
                                    additionalDropContext = aModel.getAdditionalContext();
                                }
                                break;
                            }
                        }
                    }
                }
                //switch to the menu layer if this is a menu component other than JMenuBar
                if(item != null && MenuEditLayer.isMenuRelatedComponentClass(item.getComponentClass()) &&
                        !JMenuBar.class.isAssignableFrom(item.getComponentClass())) {
                    formDesigner.getMenuEditLayer().startNewMenuComponentDragAndDrop(item);
                    return;
                }
                if (item != null) {
                    if ((item.getComponentClassName().indexOf('.') != -1) // Issue 79573
                        || FormJavaSource.isInDefaultPackage(getFormModel())) {
                        draggedComponent = new NewComponentDrag(item);
                        draggedComponent.move(dtde.getLocation(), 0);
                        repaint();
                    } else {
                        dtde.rejectDrag();
                    }
                } else {
                    dtde.rejectDrag();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {
            if (draggedComponent != null) {
                if ((newComponentDrop != null) && (dropAction != dtde.getDropAction())) {
                    dragExit(dtde);
                    dragEnter(dtde);
                    return;
                }
                draggedComponent.move(dtde.getLocation(), 0);
                if (dropContext != null) {
                    FormEditor.getAssistantModel(getFormModel()).setContext(dropContext, additionalDropContext);
                }
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
                NewComponentDrag newComponentDrag = ((NewComponentDrag)draggedComponent);
                try {
                    newComponentDrag.end(dtde.getLocation(), 0);
                } finally {
                    draggedComponent = null;
                    draggingEnded = true;
                }
                if (newComponentDrag.addedComponent != null) {
                    String id = newComponentDrag.addedComponent.getId();
                    if (newComponentDrop != null) {
                        String droppedOverId = null;
                        if (!(newComponentDrag.addedComponent instanceof RADVisualComponent)) {
                            RADComponent comp = getMetaComponentAt(dtde.getLocation(), COMP_DEEPEST);
                            if (comp != null) droppedOverId = comp.getId();
                        }
                        newComponentDrop.componentAdded(id, droppedOverId);
                    }
                }
                formDesigner.toggleSelectionMode();
                formDesigner.requestActive();
            }
        }

    }
    
}
