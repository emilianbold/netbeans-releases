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

package org.netbeans.modules.kenai.ui.treelist;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;

/**
 * UI for tree-like list which forwards mouse events to renderer component under
 * mouse cursor.
 *
 * @author S. Aubrecht
 */
public class TreeListUI extends BasicListUI {

    @Override
    protected MouseInputListener createMouseInputListener() {

        final MouseInputListener orig = super.createMouseInputListener();

        return new MouseInputListener() {

            public void mouseClicked(MouseEvent e) {
                if( !redispatchComponent(e) )
                    orig.mouseClicked(e);
            }

            public void mousePressed(MouseEvent e) {
                if( !redispatchComponent(e) ) {
                    if( showPopup(e) )
                        return;
                    orig.mousePressed(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if( !redispatchComponent(e) ) {
                    if( showPopup(e) )
                        return;
                    orig.mouseReleased(e);
                }
            }

            public void mouseEntered(MouseEvent e) {
                if( !redispatchComponent(e) )
                    orig.mouseEntered(e);
            }

            public void mouseExited(MouseEvent e) {
                if( !redispatchComponent(e) )
                    orig.mouseExited(e);
            }

            public void mouseDragged(MouseEvent e) {
                if( !redispatchComponent(e) )
                    orig.mouseDragged(e);
            }

            public void mouseMoved(MouseEvent e) {
                if( !redispatchComponent(e) ) {
                    list.setCursor(Cursor.getDefaultCursor());
                    orig.mouseMoved(e);
                } else {
                    list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        };
    }

    private boolean redispatchComponent(MouseEvent e) {
        Point p = e.getPoint();
        int index = list.locationToIndex(p);
        if( index < 0 || index >= list.getModel().getSize() )
            return false;

        ListCellRenderer renderer = list.getCellRenderer();
        if( null == renderer )
            return false;
        Component renComponent = renderer.getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false);
        if( null == renComponent )
            return false;
        Rectangle rect = list.getCellBounds(index, index);
        renComponent.setBounds(0,0,rect.width, rect.height);
        renComponent.doLayout();
        Point p3 = rect.getLocation();

        Point p2 = new Point( p.x - p3.x, p.y - p3.y );
        Component dispatchComponent =
                SwingUtilities.getDeepestComponentAt(renComponent,
                        p2.x, p2.y);
        if( dispatchComponent instanceof AbstractButton ) {
            if( !((AbstractButton)dispatchComponent).isEnabled() )
                return false;
            Point p4 = SwingUtilities.convertPoint(renComponent, p2, dispatchComponent);
            MouseEvent newEvent = new MouseEvent(dispatchComponent,
                  e.getID(),
                  e.getWhen(),
                  e.getModifiers(),
                  p4.x,p4.y,
                  e.getClickCount(),
                  e.isPopupTrigger(),
                                  MouseEvent.NOBUTTON );
            dispatchComponent.dispatchEvent(newEvent);
            list.repaint(rect);
            e.consume();
            return true;
        }
        return false;
    }

    private boolean showPopup( MouseEvent e ) {
        if( !e.isPopupTrigger() )
            return false;
        if( !(list instanceof TreeList) )
            return false;

        int index = list.locationToIndex(e.getPoint());
        Rectangle rect = list.getCellBounds(index, index);
        if( !rect.contains(e.getPoint() ) )
            return false;
        ((TreeList)list).showPopupMenuAt( index, e.getPoint() );
        return true;
    }
}
