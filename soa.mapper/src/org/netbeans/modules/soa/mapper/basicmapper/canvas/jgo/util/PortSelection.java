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
