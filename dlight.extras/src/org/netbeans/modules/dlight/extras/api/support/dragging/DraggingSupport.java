/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.extras.api.support.dragging;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 * @author Alexey Vladykin
 */
public final class DraggingSupport extends MouseAdapter implements MouseMotionListener {

    private final Component component;
    private final List<Draggable> draggables;
    private Draggable currentDraggable;

    public DraggingSupport(Component component, List<Draggable> marks) {
        this.component = component;
        this.draggables = Collections.unmodifiableList(new ArrayList<Draggable>(marks));
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    private Draggable findDraggable(Point p) {
        for (Draggable mark : draggables) {
            if (mark.containsPoint(p)) {
                return mark;
            }
        }
        return null;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Draggable draggableUnderCursor = findDraggable(e.getPoint());
        component.setCursor(draggableUnderCursor == null? Cursor.getDefaultCursor() : draggableUnderCursor.getCursor());
        e.consume();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentDraggable != null) {
            currentDraggable.dragTo(e.getPoint(), true);
            e.consume();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && currentDraggable == null) {
            currentDraggable = findDraggable(e.getPoint());
            if (currentDraggable != null) {
                currentDraggable.startDragging(e.getPoint());
            }
            e.consume();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && currentDraggable != null) {
            currentDraggable.dragTo(e.getPoint(), false);
            currentDraggable.finishDragging();
            currentDraggable = null;
            e.consume();
        }
    }
}
