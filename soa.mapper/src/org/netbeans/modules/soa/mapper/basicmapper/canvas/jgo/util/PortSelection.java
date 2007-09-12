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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoHandle;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoView;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasMethoidNode;

/**
 *
 * @author jsandusky
 */
public class PortSelection extends JGoSelection {
    
    private JGoPen myBoundingHandlePen;
    
    
    /** Creates a new instance of PortSelection */
    public PortSelection(JGoView view) {
        super(view);
    }
    
    
    // Override to use a bounding rect of the methoid which
    // does not contain the port rects.
    public JGoHandle createBoundingHandle(JGoObject obj) {
        JGoView view = getView();
        Rectangle rect = new Rectangle(0, 0, 0, 0);
        
        Rectangle b = null;
        if (obj instanceof BasicCanvasMethoidNode) {
            BasicCanvasMethoidNode node = (BasicCanvasMethoidNode) obj;
            b = node.computeBoundingRectWithoutPorts();
        } else {
            b = obj.getBoundingRect();
        }
        
        rect.x = b.x;
        rect.y = b.y;
        rect.width = b.width;
        rect.height = b.height;
        
        // the handle rectangle should just go around the object
        rect.x -= 1;
        rect.y -= 1;
        rect.width += 2;
        rect.height += 2;
        
        JGoHandle handle = new JGoHandle(rect, Cursor.DEFAULT_CURSOR);
        
        handle.setHandleType(JGoHandle.NoHandle);
        handle.setSelectable(false);
        
        Color sel_color;
        if (view != null) {
            if (getPrimarySelection() == obj) {
                sel_color = view.getPrimarySelectionColor();
            } else {
                sel_color = view.getSecondarySelectionColor();
            }
        } else {
            sel_color = JGoBrush.ColorBlack;
        }
        
        if (myBoundingHandlePen == null ||
                !myBoundingHandlePen.getColor().equals(sel_color)) {
            myBoundingHandlePen = JGoPen.make(JGoPen.SOLID, 2, sel_color);
        }
        handle.setPen(myBoundingHandlePen);
        handle.setBrush(null);
        
        addHandle(obj, handle);
        
        return handle;
    }
}
