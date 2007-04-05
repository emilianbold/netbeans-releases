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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.screen.display.injector.ScreenInjectorPresenter;
import org.netbeans.modules.vmd.api.io.PopupUtil;
import org.netbeans.modules.vmd.screen.ScreenAccessController;
import org.netbeans.modules.vmd.screen.ScreenViewController;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author David Kaspar
 */
public class TopPanel extends JPanel {

    private static final Color COLOR_SELECTION_FILL = new Color (0x74, 0x8C, 0xC0, 128);
    private static final Color COLOR_SELECTION_DRAW = Color.BLACK;

    private static final Color COLOR_HOVER_FILL = new Color (0xB9, 0xDF, 0xC0, 128);
    private static final Color COLOR_HOVER_DRAW = Color.BLACK;

    private static final Image IMAGE_INJECT = Utilities.loadImage ("org/netbeans/modules/vmd/screen/resources/inject.png"); // NOI18N

    private DevicePanel devicePanel;

    private List<SelectionShape> selectionShapes = Collections.emptyList ();

    private Point lastHoverPoint = null;
    private SelectionShape hoverShape = null;

//    private JComponent editedComponent;
//    private ScreenPropertyEditor editedEditor;

    public TopPanel (DevicePanel devicePanel) {
        this.devicePanel = devicePanel;
        setOpaque (false);
        
        addMouseListener (new MouseListener() {
            public void mouseClicked (MouseEvent e) {
                if (injectorWindow (e, true))
                    return;
                select (e);
                if (e.getButton () == MouseEvent.BUTTON1  &&  e.getClickCount () == 2)
                    editProperty (e);
                if (e.isPopupTrigger ())
                    popupMenu (e);
            }

            public void mousePressed (MouseEvent e) {
                if (injectorWindow (e, false))
                    return;
                select (e);
                if (e.isPopupTrigger ())
                    popupMenu (e);
            }

            public void mouseReleased (MouseEvent e) {
                if (injectorWindow (e, false))
                    return;
                select (e);
                if (e.isPopupTrigger ())
                    popupMenu (e);
            }

            public void mouseEntered (MouseEvent e) {
                hover (e);
            }

            public void mouseExited (MouseEvent e) {
                hover (e);
            }

        });

        addMouseMotionListener (new MouseMotionListener() {
            public void mouseDragged (MouseEvent e) {
                hover (e);
            }

            public void mouseMoved (MouseEvent e) {
                hover (e);
            }
        });

        setDropTarget (new DropTarget (this, new DropTargetListener() {
            public void dragEnter (DropTargetDragEvent dtde) {
                if (isAcceptable (dtde.getLocation (), dtde.getTransferable ()))
                    dtde.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
                else
                    dtde.rejectDrag ();
            }

            public void dragOver (DropTargetDragEvent dtde) {
                if (isAcceptable (dtde.getLocation (), dtde.getTransferable ()))
                    dtde.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
                else
                    dtde.rejectDrag ();
            }

            public void dropActionChanged (DropTargetDragEvent dtde) {
                if (isAcceptable (dtde.getLocation (), dtde.getTransferable ()))
                    dtde.acceptDrag (DnDConstants.ACTION_COPY_OR_MOVE);
                else
                    dtde.rejectDrag ();
            }

            public void dragExit (DropTargetEvent dte) {
            }

            public void drop (DropTargetDropEvent dtde) {
                if (isAcceptable (dtde.getLocation (), dtde.getTransferable ())) {
                    accept (dtde.getLocation (), dtde.getTransferable ());
                    dtde.acceptDrop (DnDConstants.ACTION_COPY_OR_MOVE);
                } else {
                    dtde.rejectDrop ();
                }
            }
        }));
    }

    protected void paintComponent (Graphics g) {
        super.paintComponent (g);
        Graphics2D gr = (Graphics2D) g;

        for (SelectionShape shape : selectionShapes) {
            gr.translate (shape.x, shape.y);
            gr.setColor (COLOR_SELECTION_FILL);
            gr.fill (shape.shape);
            gr.setColor (COLOR_SELECTION_DRAW);
            gr.draw (shape.shape);
            gr.translate (- shape.x, - shape.y);
        }

        if (hoverShape != null) {
            gr.translate (hoverShape.x, hoverShape.y);
            gr.setColor (COLOR_HOVER_FILL);
            gr.fill (hoverShape.shape);
            gr.setColor (COLOR_HOVER_DRAW);
            gr.draw (hoverShape.shape);
            gr.translate (- hoverShape.x, - hoverShape.y);
        }

        for (SelectionShape shape : selectionShapes) {
            if (shape.enableInjector) {
                gr.translate (shape.x, shape.y);
                Rectangle rectangle = shape.shape.getBounds ();
                gr.drawImage (IMAGE_INJECT, rectangle.x + rectangle.width - 20, rectangle.y - 8, null);
                gr.translate (- shape.x, - shape.y);
            }
        }
    }

    public void reload () {
        ScreenAccessController controller = devicePanel.getController ();
        DesignComponent editedScreen = controller.getEditedScreen ();
        ArrayList<SelectionShape> newSelectionShapes = new ArrayList<SelectionShape> ();
        reloadSelectionShapes (newSelectionShapes, editedScreen);
        selectionShapes = newSelectionShapes;
        repaint ();
    }

    private void reloadSelectionShapes (ArrayList<SelectionShape> newSelectionShapes, DesignComponent component) {
        ScreenDisplayPresenter presenter = component != null ? component.getPresenter (ScreenDisplayPresenter.class) : null;
        if (presenter == null)
            return;
        if (devicePanel.getController ().getDocument ().getSelectedComponents ().contains (component)) {
            Shape shape = presenter.getSelectionShape ();
            if (shape != null) {
                Point point = devicePanel.calculateTranslation (presenter.getView ());
                newSelectionShapes.add (new SelectionShape (point.x, point.y, shape, component.getComponentID (), ! component.getPresenters (ScreenInjectorPresenter.class).isEmpty ()));
            }
        }
        for (DesignComponent child : presenter.getChildren ())
            reloadSelectionShapes (newSelectionShapes, child);
    }

    public void select (final MouseEvent e) {
        final DesignDocument document = devicePanel.getController ().getDocument ();
        if (document == null)
            return;
        document.getTransactionManager ().writeAccess (new Runnable() {
            public void run () {
                DesignComponent component = devicePanel.getDesignComponentAt (e.getPoint ());
                // TODO - invert selection
                document.setSelectedComponents (ScreenViewController.SCREEN_ID, component != null ? Collections.singleton (component) : Collections.<DesignComponent>emptySet ());
            }
        });
    }

    public void hover (final MouseEvent e) {
        lastHoverPoint = e != null ? e.getPoint () : null;
        final DesignDocument document = devicePanel.getController ().getDocument ();
        if (lastHoverPoint != null  &&  document != null)
            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    DesignComponent component = devicePanel.getDesignComponentAt (lastHoverPoint);
                    ScreenDisplayPresenter presenter = component != null ? component.getPresenter (ScreenDisplayPresenter.class) : null;
                    Shape shape = presenter != null ? presenter.getSelectionShape () : null;
                    if (shape != null) {
                        Point point = devicePanel.calculateTranslation (presenter.getView ());
                        hoverShape = new SelectionShape (point.x, point.y, shape, component.getComponentID (), false);
                    } else
                        hoverShape = null;
                }
            });
        repaint ();
    }

    public boolean isAcceptable (final Point point, final Transferable transferable) {
        final DesignDocument document = devicePanel.getController ().getDocument ();
        if (document == null)
            return false;
        final boolean[] ret = new boolean[1];
        document.getTransactionManager ().readAccess (new Runnable() {
            public void run () {
                DesignComponent component = devicePanel.getDesignComponentAt (point);
                ret[0] = AcceptSupport.isAcceptable (component, transferable);
            }
        });
        return ret[0];
    }

    public void accept (final Point point, final Transferable transferable) {
        final DesignDocument document = devicePanel.getController ().getDocument ();
        if (document == null)
            return;
        document.getTransactionManager ().writeAccess (new Runnable() {
            public void run () {
                DesignComponent component = devicePanel.getDesignComponentAt (point);
                AcceptSupport.accept (component, transferable);
            }
        });
    }

    public void popupMenu (final MouseEvent e) {
        final DesignDocument document = devicePanel.getController ().getDocument ();
        if (document == null)
            return;
        document.getTransactionManager ().writeAccess (new Runnable() {
            public void run () {
                DesignComponent component = devicePanel.getDesignComponentAt (e.getPoint ());
                if (component == null)
                    return;
                JPopupMenu menu = Utilities.actionsToPopup (ActionsSupport.createActionsArray (component), TopPanel.this);
                menu.show (TopPanel.this, e.getX (), e.getY ());
            }
        });
    }

    private boolean injectorWindow (MouseEvent e, boolean invoke) {
        for (SelectionShape shape : selectionShapes) {
            if (! shape.enableInjector)
                continue;
            Rectangle bounds = shape.shape.getBounds ();
            if (new Rectangle (bounds.x + bounds.width - 20, bounds.y - 8, 16, 26).contains (e.getX () - shape.x, e.getY () - shape.y)) {
                if (invoke)
                    invokeInjectorWindow (shape.componentID, shape.x + bounds.x + bounds.width - 20, shape.y + bounds.y + 8);
                return true;
            }
        }
        return false;
    }

    private void invokeInjectorWindow (final long componentID, final int x, final int y) {
        final DesignDocument document = devicePanel.getController ().getDocument ();
        if (document == null)
            return;
        final ArrayList<JComponent> views = new ArrayList<JComponent> ();
        document.getTransactionManager ().readAccess (new Runnable() {
            public void run () {
                DesignComponent component = document.getComponentByUID (componentID);
                ArrayList<ScreenInjectorPresenter> list = new ArrayList<ScreenInjectorPresenter> (component.getPresenters (ScreenInjectorPresenter.class));
                DocumentSupport.sortPresentersByOrder (list);
                for (ScreenInjectorPresenter presenter : list) {
                    JComponent view = presenter.getViewComponent ();
                    if (view == null)
                        continue;
                    views.add (view);
                }
            }
        });
        if (views.isEmpty ())
            return;

        JPanel pane = new JPanel ();
        pane.setBorder (BorderFactory.createBevelBorder (BevelBorder.RAISED));
        pane.setLayout (new GridBagLayout ());
        for (JComponent view : views)
            pane.add (view, new GridBagConstraints (GridBagConstraints.REMAINDER, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets (0, 0, 0, 0), 0, 6));

        Point screen = getLocationOnScreen ();
        PopupUtil.showPopup (pane, "Actions", screen.x + x, screen.y + y, true);
    }

    private void editProperty (final MouseEvent e) {
        final DesignDocument document = devicePanel.getController ().getDocument ();
        if (document == null)
            return;
        document.getTransactionManager ().readAccess (new Runnable() {
            public void run () {
                DesignComponent component = devicePanel.getDesignComponentAt (e.getPoint ());
                ScreenDisplayPresenter presenter = component != null ? component.getPresenter (ScreenDisplayPresenter.class) : null;
                if (presenter == null)
                    return;
                Collection<ScreenPropertyDescriptor> properties = presenter.getPropertyDescriptors ();
                for (ScreenPropertyDescriptor property : properties) {
                    Point editorOrigin = devicePanel.calculateTranslation (property.getRelatedView ());
                    if (property.getSelectionShape ().contains (new Point (e.getX () - editorOrigin.x, e.getY () - editorOrigin.y))) {
        //                property.getEditor ().createEditorComponent ();
                        // TODO - edit
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

        public SelectionShape (int x, int y, Shape shape, long componentID, boolean enableInjector) {
            this.x = x;
            this.y = y;
            this.shape = shape;
            this.componentID = componentID;
            this.enableInjector = enableInjector;
        }

    }

}
