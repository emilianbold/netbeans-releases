/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Sheryl Su
 */
public class PanAction extends WidgetAction.LockedAdapter {

    private Scene scene;
    private JScrollPane scrollPane;
    private Point lastLocation;
    public Cursor MOUSE_PRESSED;
    private Cursor MOUSE_RELEASED;
    
    
    private void initCursor()
    {
         MOUSE_PRESSED = Utilities.createCustomCursor(scene.getView(), 
                ImageUtilities.icon2Image(ImageUtil.instance().getIcon("pan-closed-hand.gif")),
                "PanClosedHand");
        MOUSE_RELEASED = Utilities.createCustomCursor(scene.getView(), 
                ImageUtilities.icon2Image(ImageUtil.instance().getIcon("pan-open-hand.gif")),
                "PanOpenedHand");
    }
    
    protected boolean isLocked () {
        return scrollPane != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        scene = widget.getScene ();
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1) {   
            scrollPane = findScrollPane (scene.getView ());
            if (scrollPane != null) {
                if (MOUSE_PRESSED == null)
                    initCursor();
                scene.getView ().setCursor(MOUSE_PRESSED);
                lastLocation = scene.convertSceneToView (widget.convertLocalToScene (event.getPoint ()));
                SwingUtilities.convertPointToScreen (lastLocation, scene.getView ());
                return State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    private JScrollPane findScrollPane (JComponent component) {
        for (;;) {
            if (component == null)
                return null;
            if (component instanceof JScrollPane)
                return ((JScrollPane) component);
            Container parent = component.getParent ();
            if (! (parent instanceof JComponent))
                return null;
            component = (JComponent) parent;
        }
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (MOUSE_RELEASED == null)
            initCursor();

        boolean state = pan (widget, event.getPoint ());
        if (state)
            scrollPane = null;
        scene.getView ().setCursor(MOUSE_RELEASED);
        return state ? State.createLocked (widget, this) : State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return pan (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean pan (Widget widget, Point newLocation) {
        if (scrollPane == null  ||  scene != widget.getScene ())
            return false;
        newLocation = scene.convertSceneToView (widget.convertLocalToScene (newLocation));
        SwingUtilities.convertPointToScreen (newLocation, scene.getView ());
        JComponent view = scene.getView ();
        view.setCursor(MOUSE_PRESSED);
        Rectangle rectangle = view.getVisibleRect ();
        rectangle.x += lastLocation.x - newLocation.x;
        rectangle.y += lastLocation.y - newLocation.y;
        view.scrollRectToVisible (rectangle);
        lastLocation = newLocation;
        return true;
    }

}
