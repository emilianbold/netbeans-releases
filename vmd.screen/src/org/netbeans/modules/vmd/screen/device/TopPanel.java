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
import org.netbeans.modules.vmd.screen.ScreenViewController;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public class TopPanel extends JPanel {

    private DevicePanel devicePanel;

    public TopPanel (DevicePanel devicePanel) {
        this.devicePanel = devicePanel;
        setOpaque (false);
        
        addMouseListener (new MouseAdapter() {
            public void mouseClicked (MouseEvent e) {
                select (e);
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

    public void reload () {
        // TODO
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

}
