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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.design.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ShowContextMenuAction extends DesignModeAction {

    private static final long serialVersionUID = 1L;

    public ShowContextMenuAction(DesignView view) {
        super(view);
    }

    public void actionPerformed(ActionEvent e) {
            Pattern p = getDesignView().getSelectionModel().getSelectedPattern();

            if (p == null) {
                return;
            }

            JPopupMenu menu = p.createPopupMenu();

            FBounds r;

            if (p instanceof ProcessPattern) {
                r = ((ProcessPattern) p).getBorder().getBounds();
            } else {
                r = p.getBounds();
            }

            DiagramView dView = p.getView();

            Point topLeft = dView.convertPointToParent(r.getTopLeft());
            Point bottomRight = dView.convertPointToParent(r.getBottomRight());

//            Point topLeft = getDesignView().convertDiagramToScreen(r.getTopLeft());
//            Point bottomRight = getDesignView().convertDiagramToScreen(r.getBottomRight());

            Rectangle vr = getDesignView().getVisibleRect();

            int x1 = Math.max(topLeft.x, vr.x);
            int y1 = Math.max(topLeft.y, vr.y);

            int x2 = Math.min(bottomRight.x, vr.x + vr.width);
            int y2 = Math.min(bottomRight.y, vr.y + vr.height);

            int px = topLeft.x;
            int py = topLeft.y;

            if (x1 <= x2) {
                px = x1;
            } else if (px <= vr.x) {
                px = vr.x;
            } else if (px >= vr.x + vr.width) {
                px = vr.x + vr.width;
            }

            if (y1 <= y2) {
                py = y1;
            } else if (py <= vr.y) {
                py = vr.y;
            } else if (py >= vr.y + vr.height) {
                py = vr.y + vr.height;
            }

            menu.show(getDesignView(), px, py);
    }
}
