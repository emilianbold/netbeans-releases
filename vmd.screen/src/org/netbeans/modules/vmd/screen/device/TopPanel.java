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
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyEditor;
import org.netbeans.modules.vmd.screen.ScreenViewController;
import org.netbeans.modules.vmd.screen.ScreenAccessController;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.*;
import java.util.List;

/**
 * @author David Kaspar
 */
public class TopPanel extends JPanel {

    private static final Color COLOR_SELECTION_FILL = new Color (0x74, 0x8C, 0xC0, 128);
    private static final Color COLOR_SELECTION_DRAW = Color.BLACK;

    private static final Image IMAGE_INJECT = Utilities.loadImage ("org/netbeans/modules/vmd/screen/resources/inject.png"); // NOI18N

    private DevicePanel devicePanel;
    private List<SelectionShape> selectionShapes = Collections.emptyList ();

    private JComponent editedComponent;
    private ScreenPropertyEditor editedEditor;

    public TopPanel (DevicePanel devicePanel) {
        this.devicePanel = devicePanel;
        setOpaque (false);
        
        addMouseListener (new MouseAdapter() {
            public void mouseClicked (MouseEvent e) {
                select (e);
                if (e.getButton () == MouseEvent.BUTTON1  &&  e.getClickCount () == 2)
                    editProperty (e);
                if (e.isPopupTrigger ())
                    popupMenu (e);
            }

            public void mousePressed (MouseEvent e) {
                select (e);
                if (e.isPopupTrigger ())
                    popupMenu (e);
            }

            public void mouseReleased (MouseEvent e) {
                select (e);
                if (e.isPopupTrigger ())
                    popupMenu (e);
            }
        });

        addMouseMotionListener (new MouseMotionAdapter () {
            public void mouseMoved (MouseEvent e) {
//                hover (e);
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
            Rectangle rectangle = shape.shape.getBounds ();
            gr.drawImage (IMAGE_INJECT, rectangle.x + rectangle.width - 20, rectangle.y - 8, null);
            gr.translate (- shape.x, - shape.y);
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
                newSelectionShapes.add (new SelectionShape (point.x, point.y, shape));
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

        public SelectionShape (int x, int y, Shape shape) {
            this.x = x;
            this.y = y;
            this.shape = shape;
        }

    }

}
