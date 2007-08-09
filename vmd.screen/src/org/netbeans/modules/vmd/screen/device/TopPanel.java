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
 *
 */

package org.netbeans.modules.vmd.screen.device;

import org.netbeans.modules.vmd.api.io.PopupUtil;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayDataFlavorSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.screen.display.injector.ScreenInjectorPresenter;
import org.netbeans.modules.vmd.screen.MainPanel;
import org.netbeans.modules.vmd.screen.ScreenAccessController;
import org.netbeans.modules.vmd.screen.ScreenViewController;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;


/**
 * @author David Kaspar
 */
public class TopPanel extends JPanel {
    
    //    private static final Color COLOR_SELECTION_FILL = new Color (235, 235, 231, 64);
    private static final Color COLOR_SELECTION_DRAW = MainPanel.SELECT_COLOR;
    
    //    private static final Color COLOR_HOVER_FILL = new Color (0xB9, 0xDF, 0xC0, 128);
    private static final Color COLOR_HOVER_DRAW = MainPanel.HOVER_COLOR;
    private static final Color COLOR_DRAW_DND_LINE = new Color(36, 76, 114);
    //private static final Color COLOR_DRAW_DND_SHAPE = Color.GREEN;
    private static final Stroke STROKE = new BasicStroke(2.0f);
    //private static final Stroke STROKE_DND_SHAPE = new BasicStroke(1.0f);
    private static final Stroke STROKE_DND_LINE = new BasicStroke(3.0f,
            BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER,
            2.0f,
            new float[] {5.0f,10.0f},
            0.0f);
    
    private static final Image IMAGE_INJECT = Utilities.loadImage("org/netbeans/modules/vmd/screen/resources/inject.png"); // NOI18N
    
    private DevicePanel devicePanel;
    private List<SelectionShape> selectionShapes = Collections.emptyList();
    private ScreenDeviceInfo.Edge horizontalPosition;
    private ScreenDeviceInfo.Edge verticalPosition;
    
    private Point lastHoverPoint = null;
    private SelectionShape hoverShape = null;
    private DragSource dragSource;
    private DesignComponent dragedComponent;
    private boolean innerDragingInProgress;
    //private boolean outerGraggingInProgress;
    
    public TopPanel(final DevicePanel devicePanel) {
        this.devicePanel = devicePanel;
        setOpaque(false);
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,new DragGestureListener() {
            public void dragGestureRecognized(final DragGestureEvent dgEvent) {
                devicePanel.getController().getDocument().getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        dragedComponent = devicePanel.getDesignComponentAt(dgEvent.getDragOrigin());
                        if (dragedComponent == null)
                            return;
                        ScreenDisplayPresenter presenter = dragedComponent.getPresenter(ScreenDisplayPresenter.class);
                        if (presenter == null || presenter.isDraggable() == false) {
                            innerDragingInProgress = false;
                            return;
                        }
                        dragSource.startDrag(dgEvent, null,new ScreenDisplaylTransferable(dragedComponent), null);
                    }
                });
            }
        });
        
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (injectorWindow(e, true))
                    return;
                select(e);
                if (e.getButton() == MouseEvent.BUTTON1  &&  e.getClickCount() == 2)
                    editProperty(e);
                if (e.isPopupTrigger())
                    popupMenu(e);
            }
            
            public void mousePressed(MouseEvent e) {
                if (injectorWindow(e, false))
                    return;
                select(e);
                if (e.isPopupTrigger())
                    popupMenu(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                if (injectorWindow(e, false))
                    return;
                select(e);
                if (e.isPopupTrigger())
                    popupMenu(e);
            }
            
            public void mouseEntered(MouseEvent e) {
                hover(e.getPoint());
            }
            
            public void mouseExited(MouseEvent e) {
                hover(e.getPoint());
            }
            
        });
        
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                hover(e.getPoint());
            }
            
            public void mouseMoved(MouseEvent e) {
                hover(e.getPoint());
            }
        });
        
        setDropTarget(new DropTarget(this,new DropTargetListener() {
            
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.getTransferable().isDataFlavorSupported(ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR))
                    innerDragingInProgress = true;
                updatePosition(dtde.getLocation());
                AcceptSuggestion suggestion = getSugestion(dtde.getTransferable());
                if (isAcceptable(dtde.getLocation(), dtde.getTransferable(), suggestion))
                    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                else
                    dtde.rejectDrag();
            }
            
            public void dragOver(final DropTargetDragEvent dtde) {
                updatePosition(dtde.getLocation());
                AcceptSuggestion suggestion = getSugestion(dtde.getTransferable());
                if (isAcceptable(dtde.getLocation(), dtde.getTransferable(),  suggestion)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                    hoverDnD(dtde.getLocation());
                } else {
                    hover(dtde.getLocation());
                    dtde.rejectDrag();
                }
            }
            
            public void dropActionChanged(DropTargetDragEvent dtde) {
                updatePosition(dtde.getLocation());
                AcceptSuggestion suggestion = getSugestion(dtde.getTransferable());
                if (isAcceptable(dtde.getLocation(), dtde.getTransferable(), suggestion)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                } else
                    dtde.rejectDrag();
            }
            
            public void dragExit(DropTargetEvent dte) {
                innerDragingInProgress = false;
                hover(lastHoverPoint);
            }
            
            public void drop(DropTargetDropEvent dtde) {
                updatePosition(dtde.getLocation());
                AcceptSuggestion suggestion = getSugestion(dtde.getTransferable());
                if (isAcceptable(dtde.getLocation(), dtde.getTransferable(), suggestion)) {
                    accept(dtde.getLocation(), dtde.getTransferable(), suggestion);
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                } else {
                    dtde.rejectDrop();
                }
                innerDragingInProgress = false;
                hover(dtde.getLocation());
            }
        }));
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr = (Graphics2D) g;
        
        Stroke previousStroke = gr.getStroke();
        gr.setStroke(STROKE);
        
        for (SelectionShape shape : selectionShapes) {
            gr.translate(shape.x, shape.y);
            //            gr.setColor (COLOR_SELECTION_FILL);
            //            gr.fill (shape.shape);
            gr.setColor(COLOR_SELECTION_DRAW);
            gr.draw(shape.shape);
            gr.translate(- shape.x, - shape.y);
        }
        
        if (hoverShape != null) {
            gr.translate(hoverShape.x, hoverShape.y);
            //            gr.setColor (COLOR_HOVER_FILL);
            //            gr.fill (hoverShape.shape);
            
            
            if (innerDragingInProgress) {
                gr.translate(- hoverShape.x, - hoverShape.y);
                gr.setColor(COLOR_DRAW_DND_LINE);
                gr.setStroke(STROKE_DND_LINE);
                gr.setColor(COLOR_DRAW_DND_LINE);
                int space = 3;
                if (verticalPosition == ScreenDeviceInfo.Edge.BOTTOM) {
                    int x1 = (int) hoverShape.x + space;
                    int y1 = hoverShape.y + (int) hoverShape.shape.getBounds().getHeight();
                    int x2 = (int) hoverShape.x + (int) hoverShape.shape.getBounds().getWidth() - space;
                    int y2 = (int) hoverShape.y + (int) hoverShape.shape.getBounds().getHeight();
                    gr.drawLine(x1, y1 , x2, y2);
                } else if (verticalPosition == ScreenDeviceInfo.Edge.TOP) {
                    int x1 = (int) hoverShape.x + space;
                    int y1 = hoverShape.y;
                    int x2 = (int) hoverShape.x + (int) hoverShape.shape.getBounds().getWidth() - space;
                    int y2 = (int) hoverShape.y;
                    gr.drawLine(x1, y1 , x2, y2);
                }
            } else {
                gr.setColor(COLOR_HOVER_DRAW);
                gr.draw(hoverShape.shape);
                gr.translate(- hoverShape.x, - hoverShape.y);
            }
        }
        
        gr.setStroke(previousStroke);
        
        for (SelectionShape shape : selectionShapes) {
            if (shape.enableInjector) {
                gr.translate(shape.x, shape.y);
                Rectangle rectangle = shape.shape.getBounds();
                gr.drawImage(IMAGE_INJECT, rectangle.x + rectangle.width - 20, rectangle.y - 8, null);
                gr.translate(- shape.x, - shape.y);
            }
        }
    }
    
    public void reload() {
        ScreenAccessController controller = devicePanel.getController();
        DesignComponent editedScreen = controller.getEditedScreen();
        ArrayList<SelectionShape> newSelectionShapes = new ArrayList<SelectionShape> ();
        reloadSelectionShapes(newSelectionShapes, editedScreen);
        selectionShapes = newSelectionShapes;
        repaint();
    }
    
    private void reloadSelectionShapes(ArrayList<SelectionShape> newSelectionShapes, DesignComponent component) {
        ScreenDisplayPresenter presenter = component != null ? component.getPresenter(ScreenDisplayPresenter.class) : null;
        if (presenter == null)
            return;
        if (devicePanel.getController().getDocument().getSelectedComponents().contains(component)) {
            Shape shape = presenter.getSelectionShape();
            if (shape != null) {
                Point point = devicePanel.calculateTranslation(presenter.getView());
                boolean containsInjector = false;
                for (ScreenInjectorPresenter injector : component.getPresenters(ScreenInjectorPresenter.class)) {
                    if (injector.isEnabled()) {
                        containsInjector = true;
                        break;
                    }
                }
                newSelectionShapes.add(new SelectionShape(point.x, point.y, shape, component.getComponentID(), containsInjector));
            }
        }
        for (DesignComponent child : presenter.getChildren())
            reloadSelectionShapes(newSelectionShapes, child);
    }
    
    public void select(final MouseEvent e) {
        final DesignDocument document = devicePanel.getController().getDocument();
        if (document == null)
            return;
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent component = devicePanel.getDesignComponentAt(e.getPoint());
                if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
                    if (component != null) {
                        ArrayList<DesignComponent> list = new ArrayList<DesignComponent> (document.getSelectedComponents());
                        if (! list.remove(component))
                            list.add(component);
                        document.setSelectedComponents(ScreenViewController.SCREEN_ID, list);
                    }
                } else {
                    if (component == null)
                        document.setSelectedComponents(ScreenViewController.SCREEN_ID, Collections.<DesignComponent>emptySet());
                    else if (! document.getSelectedComponents().contains(component))
                        document.setSelectedComponents(ScreenViewController.SCREEN_ID, Collections.singleton(component));
                }
            }
        });
    }
    
    private void hover(final Point point) {
        lastHoverPoint = point != null ? point : null;
        final DesignDocument document = devicePanel.getController().getDocument();
        if (lastHoverPoint != null  &&  document != null)
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    DesignComponent component = devicePanel.getDesignComponentAt(lastHoverPoint);
                    ScreenDisplayPresenter presenter = component != null ? component.getPresenter(ScreenDisplayPresenter.class) : null;
                    Collection<ScreenPropertyDescriptor> properties = presenter != null ? presenter.getPropertyDescriptors() : Collections.<ScreenPropertyDescriptor>emptySet();
                    if (properties == null)
                        return;
                    for (ScreenPropertyDescriptor property : properties) {
                        Point editorOrigin = devicePanel.calculateTranslation(property.getRelatedView());
                        Shape shape = property.getSelectionShape();
                        if (shape.contains(new Point(point.x - editorOrigin.x, point.y - editorOrigin.y))) {
                            hoverShape = new SelectionShape(editorOrigin.x, editorOrigin.y, shape, Long.MIN_VALUE, false);
                            return;
                        }
                    }
                    hoverShape = null;
                }
            });
            repaint();
    }
    
    private void hoverDnD(final Point point) {
        lastHoverPoint = point != null ? point : null;
        final DesignDocument document = devicePanel.getController().getDocument();
        if (lastHoverPoint != null  &&  document != null)
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    DesignComponent component = devicePanel.getDesignComponentAt(lastHoverPoint);
                    ScreenDisplayPresenter presenter = component != null ? component.getPresenter(ScreenDisplayPresenter.class) : null;
                    Point editorOrigin = devicePanel.calculateTranslation(presenter.getView());
                    Shape shape = presenter.getSelectionShape();
                    if (shape.contains(new Point(point.x - editorOrigin.x, point.y - editorOrigin.y))) {
                        hoverShape = new SelectionShape(editorOrigin.x, editorOrigin.y, shape, Long.MIN_VALUE, false);
                        return;
                    }
                    hoverShape = null;
                }
            });
            repaint();
    }
    
    private void updatePosition(final Point point) {
        final DesignDocument document = devicePanel.getController().getDocument();
        if (document != null)
            document.getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    if (point ==  null)
                        return;
                    DesignComponent component = devicePanel.getDesignComponentAt(point);
                    ScreenDisplayPresenter presenter = component != null ? component.getPresenter(ScreenDisplayPresenter.class) : null;
                    if (presenter == null)
                        return;
                    Point editorOrigin = devicePanel.calculateTranslation(presenter.getView());
                    double halfVertical = presenter.getView().getHeight() / 2;
                    double halfHorizontal = presenter.getView().getWidth() / 2;
                    if ((editorOrigin.getY() + halfVertical) > point.getY())
                        verticalPosition = ScreenDeviceInfo.Edge.TOP;
                    else if ((editorOrigin.getY() + halfVertical) < point.getY())
                        verticalPosition = ScreenDeviceInfo.Edge.BOTTOM;
                    if ((editorOrigin.getX() + halfHorizontal) > point.getX())
                        horizontalPosition = ScreenDeviceInfo.Edge.LEFT;
                    else if ((editorOrigin.getX() + halfHorizontal) < point.getX())
                        horizontalPosition = ScreenDeviceInfo.Edge.RIGHT;
                }
            });
    }
    
    public boolean isAcceptable(final Point point, final Transferable transferable, final AcceptSuggestion suggestion) {
        final DesignDocument document = devicePanel.getController().getDocument();
        if (document == null)
            return false;
        final boolean[] ret = new boolean[1];
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent component = devicePanel.getDesignComponentAt(point);
                ret[0] = AcceptSupport.isAcceptable(component, transferable, suggestion);
            }
        });
        return ret[0];
    }
    
    public void accept(final Point point, final Transferable transferable, final AcceptSuggestion suggestion) {
        final DesignDocument document = devicePanel.getController().getDocument();
        if (document == null)
            return;
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent component = devicePanel.getDesignComponentAt(point);
                ComponentProducer.Result result = AcceptSupport.accept(component, transferable, suggestion);
                AcceptSupport.selectComponentProducerResult(result);
            }
        });
    }
    
    public void popupMenu(final MouseEvent e) {
        final DesignDocument document = devicePanel.getController().getDocument();
        if (document == null)
            return;
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent component = devicePanel.getDesignComponentAt(e.getPoint());
                if (component == null)
                    return;
                JPopupMenu menu = Utilities.actionsToPopup(ActionsSupport.createActionsArray(component), TopPanel.this);
                menu.show(TopPanel.this, e.getX(), e.getY());
            }
        });
    }
    
    private boolean injectorWindow(MouseEvent e, boolean invoke) {
        for (SelectionShape shape : selectionShapes) {
            if (! shape.enableInjector)
                continue;
            Rectangle bounds = shape.shape.getBounds();
            if (new Rectangle(bounds.x + bounds.width - 20, bounds.y - 8, 16, 16).contains(e.getX() - shape.x, e.getY() - shape.y)) {
                if (invoke)
                    invokeInjectorWindow(shape.componentID, shape.x + bounds.x + bounds.width - 20, shape.y + bounds.y + 8);
                return true;
            }
        }
        return false;
    }
    
    private void invokeInjectorWindow(final long componentID, final int x, final int y) {
        final DesignDocument document = devicePanel.getController().getDocument();
        if (document == null)
            return;
        final ArrayList<JComponent> views = new ArrayList<JComponent> ();
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent component = document.getComponentByUID(componentID);
                ArrayList<ScreenInjectorPresenter> list = new ArrayList<ScreenInjectorPresenter> (component.getPresenters(ScreenInjectorPresenter.class));
                DocumentSupport.sortPresentersByOrder(list);
                for (ScreenInjectorPresenter presenter : list) {
                    if (! presenter.isEnabled())
                        continue;
                    JComponent view = presenter.getViewComponent();
                    if (view == null)
                        continue;
                    views.add(view);
                }
            }
        });
        if (views.isEmpty())
            return;
        
        JPanel pane = new JPanel();
        pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        pane.setLayout(new GridBagLayout());
        for (JComponent view : views)
            pane.add(view, new GridBagConstraints(GridBagConstraints.REMAINDER, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 6));
        
        Point screen = getLocationOnScreen();
        PopupUtil.showPopup(pane, NbBundle.getMessage (TopPanel.class, "TITLE_ActionsMenu"), screen.x + x, screen.y + y, true); // NOI18N
    }
    
    private void editProperty(final MouseEvent e) {
        final DesignDocument document = devicePanel.getController().getDocument();
        if (document == null)
            return;
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent component = devicePanel.getDesignComponentAt(e.getPoint());
                ScreenDisplayPresenter presenter = component != null ? component.getPresenter(ScreenDisplayPresenter.class) : null;
                if (presenter == null)
                    return;
                Collection<ScreenPropertyDescriptor> properties = presenter.getPropertyDescriptors();
                if (properties == null)
                    return;
                for (ScreenPropertyDescriptor property : properties) {
                    JComponent relatedView = property.getRelatedView();
                    Shape shape = property.getSelectionShape();
                    Point editorOrigin = devicePanel.calculateTranslation(relatedView);
                    if (shape.contains(new Point(e.getX() - editorOrigin.x, e.getY() - editorOrigin.y))) {
                        Rectangle bounds = shape.getBounds();
                        JComponent editorView = property.getEditor().createEditorComponent(property);
                        if (editorView == null)
                            return;
                        Insets insets = property.getEditor().getEditorComponentInsets(editorView);
                        bounds.x -= insets.left;
                        bounds.width += insets.left + insets.right;
                        bounds.y -= insets.top;
                        bounds.height += insets.top + insets.bottom;
                        editorView.setPreferredSize(bounds.getSize());
                        Point relatedViewLocationOnScreen = relatedView.getLocationOnScreen();
                        bounds.translate(relatedViewLocationOnScreen.x, relatedViewLocationOnScreen.y);
                        PopupUtil.showPopup(editorView, NbBundle.getMessage (TopPanel.class, "TITLE_EditorMenu"), bounds.x, bounds.y, true); // NOI18N
                    }
                }
            }
        });
    }
    
    private static class SelectionShape {
        
        private int x, y;
        private Shape shape;
        private long componentID;
        private boolean enableInjector;
        
        public SelectionShape(int x, int y, Shape shape, long componentID, boolean enableInjector) {
            this.x = x;
            this.y = y;
            this.shape = shape;
            this.componentID = componentID;
            this.enableInjector = enableInjector;
        }
    }
    
    private AcceptSuggestion getSugestion(final Transferable transferable) {
        final ScreenDisplayPresenter[] displayPresenterWrapper = new ScreenDisplayPresenter[1];
        if (!(transferable.isDataFlavorSupported(DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR)))
            return null;
        devicePanel.getController().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                try {
                    DesignComponent component = (DesignComponent) transferable.getTransferData(DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR);
                    displayPresenterWrapper[0] = component.getPresenter(ScreenDisplayPresenter.class);
                } catch (UnsupportedFlavorException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        if (displayPresenterWrapper[0] == null)
            return null;
        return displayPresenterWrapper[0].createSuggestion(transferable);
    }
    private class ScreenDisplaylTransferable implements Transferable {
        private List DATA_FLAVORS = Arrays.asList(new DataFlavor[]{
            DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR,
            ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR,
            ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR
                    
        });
        
        private WeakReference<DesignComponent> component;
        
        public ScreenDisplaylTransferable(DesignComponent component) {
            this.component = new WeakReference<DesignComponent>(component);
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) DATA_FLAVORS.toArray();
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (DATA_FLAVORS.contains(flavor))
                return true;
            return false;
        }
        
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor == DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR)
                return component.get();
            if (flavor == ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR)
                return horizontalPosition;
            if (flavor == ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR)
                return verticalPosition;
            return null;
        }
        
    }
    
}


