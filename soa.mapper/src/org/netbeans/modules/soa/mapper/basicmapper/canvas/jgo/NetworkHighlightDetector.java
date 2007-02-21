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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;


/**
 * Attempts to determine if a given path has been highlighted.
 *
 * @author Josh Sandusky
 */
public class NetworkHighlightDetector implements NetworkHighlightConstants {

    private boolean mIsHighlightDetected;


    public NetworkHighlightDetector(ICanvasMapperLink link) {
        mIsHighlightDetected = false;
        visit(link);
    }
    
    
    public boolean isHighlightDetected() {
        return mIsHighlightDetected;
    }
    
    private void visit(ICanvasMapperLink link) {
        if (link instanceof BasicCanvasNodeToNodeLink) {
            visit((BasicCanvasNodeToNodeLink) link);
        } else if (link instanceof BasicCanvasNodeToTreeLink) {
            visit((BasicCanvasNodeToTreeLink) link);
        } else if (link instanceof BasicCanvasTreeToNodeLink) {
            visit((BasicCanvasTreeToNodeLink) link);
        } else if (link instanceof BasicCanvasTreeToTreeLink) {
            visit((BasicCanvasTreeToTreeLink) link);
        }
    }
    
    private void visit(BasicCanvasNodeToNodeLink link) {
        if (link.getSourceFieldNode() != null) {
            visit(link.getSourceFieldNode());
        }
        if (link.getDestFieldNode() != null) {
            visit(link.getDestFieldNode());
        }
    }
    
    private void visit(BasicCanvasNodeToTreeLink link) {
        if (link.getSourceFieldNode() != null) {
            visit(link.getSourceFieldNode());
        }
    }
    
    private void visit(BasicCanvasTreeToNodeLink link) {
        if (link.getDestFieldNode() != null) {
            visit(link.getDestFieldNode());
        }
    }
    
    private void visit(ICanvasFieldNode fieldNode) {
        ICanvasMethoidNode methoidNode = (ICanvasMethoidNode) fieldNode.getContainer();
        if (methoidNode instanceof BasicCanvasMethoidNode) {
            BasicCanvasMethoidNode basicMethoidNode = (BasicCanvasMethoidNode) methoidNode;
            if (COLOR_NODE_SELECTED.equals(basicMethoidNode.getTitleBarColor())) {
                mIsHighlightDetected = true;
            }
        }
    }
}
