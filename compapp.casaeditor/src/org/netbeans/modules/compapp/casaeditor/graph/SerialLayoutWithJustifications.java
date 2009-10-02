/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
* SerialLayoutWithJustifications.java
*
* Created on November 29, 2006, 11:47 AM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * This is a SerialLayout which modified "justify" for right alignment of children.
 * 
 * The regular layout can't RIGHT align child to that of parent right bound, because parent bounds are not yet known.
 * Once all the layout's sizes determined, then "justify" will be called and this justify right align the children.
 * 
 * 
 * @author rdara
 */
public final class SerialLayoutWithJustifications implements Layout {

    private boolean verticalOrientation;
    private LayoutFactory.SerialAlignment alignment;
    private int gap;

    public SerialLayoutWithJustifications (boolean verticalOrientation, LayoutFactory.SerialAlignment alignment, int gap) {
        this.verticalOrientation = verticalOrientation;
        this.alignment = alignment;
        this.gap = gap;
    }

    public void layout (Widget widget) {
        int max = 0;
        Collection<Widget> children = widget.getChildren ();
        if (verticalOrientation) {
            for (Widget child : children) {
                Rectangle preferredBounds = child.getPreferredBounds ();
                int i = preferredBounds.width;
                if (i > max)
                    max = i;
            }
            int pos = 0;
            for (Widget child : children) {
                Rectangle preferredBounds = child.getPreferredBounds ();
                int x = preferredBounds.x;
                int y = preferredBounds.y;
                int width = preferredBounds.width;
                int height = preferredBounds.height;
                int lx = - x;
                int ly = pos - y;
                switch (alignment) {
                    case CENTER:
                        lx += (max - width) / 2;
                        break;
                    case JUSTIFY:
                        width = max;
                        break;
                    case LEFT_TOP:
                        break;
                    case RIGHT_BOTTOM:
                        lx += max - width;
                        break;
                }
                child.resolveBounds (new Point (lx, ly), new Rectangle (x, y, width, height));
                pos += height + gap;
            }
        } else {
            for (Widget child : children) {
                Rectangle preferredBounds = child.getPreferredBounds ();
                int i = preferredBounds.height;
                if (i > max)
                    max = i;
            }
            int pos = 0;
            for (Widget child : children) {
                Rectangle preferredBounds = child.getPreferredBounds ();
                int x = preferredBounds.x;
                int y = preferredBounds.y;
                int width = preferredBounds.width;
                int height = preferredBounds.height;
                int lx = pos - x;
                int ly = - y;
                
                switch (alignment) {
                    case CENTER:
                        ly += (max - height) / 2;
                        break;
                    case JUSTIFY:
                        height = max;
                        break;
                    case LEFT_TOP:
                        break;
                    case RIGHT_BOTTOM:
                        ly += max - height;
                        break;
                }
                child.resolveBounds (new Point (lx, ly), new Rectangle (x, y, width, height));
                pos += width + gap;
            }
        }
    }

    public boolean requiresJustification (Widget widget) {
       return true;
    }
    
    public void justify (Widget widget) {
        Rectangle bounds = widget.getParentWidget().getClientArea ();
        
        if (
                alignment == LayoutFactory.SerialAlignment.JUSTIFY || 
                alignment == LayoutFactory.SerialAlignment.RIGHT_BOTTOM) {
            justifyChildren(widget, bounds);
        }
        
        // Ensure the widget stretches to the width of the parent.
        Rectangle widgetBounds = widget.getPreferredBounds();
        widgetBounds.width = bounds.width;
        widget.resolveBounds(widget.getLocation(), widgetBounds);
    }

    // right-align the children
    private void justifyChildren(Widget widget, Rectangle bounds) {
        int childrenWidth = 0 - gap;
        for (Widget child : widget.getChildren ()) {
            childrenWidth += child.getPreferredBounds().width;
            childrenWidth += gap;
        }
        int childX =  bounds.x + bounds.width - childrenWidth;
        for (Widget child : widget.getChildren ()) {
            Point location = child.getLocation ();
            Rectangle childBounds = child.getBounds ();

            if (verticalOrientation) {
                int parentX1 = bounds.x;
                int parentX2 = parentX1 + bounds.width;
                int childX1 = location.x + childBounds.x;
                int childX2 = childX1 + childBounds.width;

                childBounds.x = Math.min (parentX1, childX1);
                childBounds.width = Math.max (parentX2, childX2) - childBounds.x;
                childBounds.x -= location.x;
                child.resolveBounds (location, childBounds);
            } else {
                int parentY1 = bounds.y;
                int parentY2 = parentY1 + bounds.height;
                int childY1 = location.y + childBounds.y;
                int childY2 = childY1 + childBounds.height;
                
                childBounds.y = Math.min (parentY1, childY1);
                childBounds.height = Math.max (parentY2, childY2) - childBounds.y;
                childBounds.y -= location.y;
                
                switch (alignment) {
                    case RIGHT_BOTTOM:
                        location.x = childX;
                        childX += (childBounds.width + gap);
                        child.resolveBounds (location, null);
                        break;
                    case CENTER:
                    case JUSTIFY:
                    case LEFT_TOP:
                        child.resolveBounds (location, childBounds);
                        break;
                }
            }
        }
    }
}
