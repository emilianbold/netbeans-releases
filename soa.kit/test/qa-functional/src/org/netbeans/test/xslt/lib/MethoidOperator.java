/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.test.xslt.lib;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasFieldNode;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.DrawPort;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;

/**
 *
 * @author ca@netbeans.org
 */

public class MethoidOperator {
    private ICanvasMethoidNode m_canvasMethoidNode;
    
    /** Creates a new instance of MethoidOperator */
    public MethoidOperator(ICanvasMethoidNode canvasMethoidNode) {
        m_canvasMethoidNode = canvasMethoidNode;
    }
    
    public int getPortCount() {
        int counter = 0;
        
        Collection c = m_canvasMethoidNode.getNodes();
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof BasicCanvasFieldNode) {
                counter++;
            }
        }
        
        return counter;
    }
    
    public Point getPortPoint(int portNumber) {
        int counter = 0;
        Point point = null;
        
        Collection c = m_canvasMethoidNode.getNodes();
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof BasicCanvasFieldNode) {
                if (counter++ == portNumber) {
                    BasicCanvasFieldNode fieldNode = (BasicCanvasFieldNode) obj;
                    DrawPort port = fieldNode.getDrawPort();
                    Rectangle rect = port.getBoundingRect();
                    
                    Helpers.writeJemmyLog("Methoid:" + m_canvasMethoidNode.getBounding());
                    return new Point(rect.x + rect.width/2, rect.y + rect.height/2);
                }
            }
        }
        
        return point;
    }
    
    public Rectangle getBoundings() {
        return m_canvasMethoidNode.getBounding();
    }
}
