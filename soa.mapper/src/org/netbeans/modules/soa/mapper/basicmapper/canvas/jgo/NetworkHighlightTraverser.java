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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import org.netbeans.modules.soa.mapper.basicmapper.tree.BasicMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;


/**
 * A node/link traverser that will visually highlight or un-highlight
 * a series of connected nodes and links.
 * 
 * @author Josh Sandusky
 */
public class NetworkHighlightTraverser implements NetworkHighlightConstants {
    
    private boolean mIsHighlighting;
    private boolean mIsDetecting;
    private List mVisitedLinks;
    private List mVisitedMethoids;
    
    
    public NetworkHighlightTraverser(boolean isHighlighting) {
        mIsHighlighting = isHighlighting;
        mVisitedLinks = new ArrayList();
        mVisitedMethoids = new ArrayList();
    }
    
    
    public void visit(ICanvasMethoidNode methoidNode) {
        mVisitedMethoids.add(methoidNode);

        // higlight or un-highlight the methoid
        changeMethoidNodeHighlight(methoidNode);
        
        Collection col = methoidNode.getNodes();
        for (Iterator iter=col.iterator(); iter.hasNext();) {
            JGoObject ob = (JGoObject) iter.next();
            if (ob instanceof ICanvasFieldNode) {
                ICanvasFieldNode fieldNode = (ICanvasFieldNode) ob;
                visit(fieldNode);
            }
        }
    }
    
    private void visit(ICanvasFieldNode fieldNode) {
        ICanvasMethoidNode methoidNode = (ICanvasMethoidNode) fieldNode.getContainer();
        if (!isMethoidVisited(methoidNode)) {
            visit(methoidNode);
            return;
        }
        
        // higlight or un-highlight the field
        changeFieldNodeHighlight(fieldNode);
        
        JGoPort port = (JGoPort) fieldNode.getConnectPointObject();
        JGoListPosition firstPosition = port.getFirstLinkPos();
        if (firstPosition != null) {
            JGoLink link = port.getLinkAtPos(firstPosition);
            if (link instanceof ICanvasMapperLink) {
                ICanvasMapperLink canvasLink = (ICanvasMapperLink) link;
                if (!isLinkVisited(canvasLink)) {
                    visit(canvasLink);
                }
            }
        }
    }
    
    public void visit(ICanvasMapperLink link) {
        mVisitedLinks.add(link);
        
        // higlight or un-highlight the link
        changeLinkHighlight(link);
        
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
        if (link.getDestTreeAddress() != null) {
            visit(link.getDestTreeAddress());
        }
    }
    
    private void visit(BasicCanvasTreeToNodeLink link) {
        if (link.getSourceTreeAddress() != null) {
            visit(link.getSourceTreeAddress());
        }
        if (link.getDestFieldNode() != null) {
            visit(link.getDestFieldNode());
        }
    }
    
    private void visit(BasicCanvasTreeToTreeLink link) {
        if (link.getSourceTreeAddress() != null) {
            visit(link.getSourceTreeAddress());
        }
        if (link.getDestTreeAddress() != null) {
            visit(link.getDestTreeAddress());
        }
    }
    
    private void visit(IMapperTreeNode node) {
        changeTreeNodeHighlight(node);
    }
    
    private boolean isLinkVisited(ICanvasMapperLink link) {
        return mVisitedLinks.contains(link);
    }
    
    private boolean isMethoidVisited(ICanvasMethoidNode methoid) {
        return mVisitedMethoids.contains(methoid);
    }
    
    private void changeLinkHighlight(ICanvasMapperLink link) {
        if (mIsHighlighting) {
            // highlight link color
            link.startHighlighting();
        } else {
            // un-highlight, set back to original color
            link.stopHighlighting();
        }
    }
    
    private void changeMethoidNodeHighlight(ICanvasMethoidNode node) {
        if (node instanceof BasicCanvasMethoidNode) {
            BasicCanvasMethoidNode basicMethoidNode = (BasicCanvasMethoidNode) node;
            if (mIsHighlighting) {
                // highlight node title bar background color
                basicMethoidNode.setTitleBarColor(COLOR_NODE_SELECTED);
            } else {
                // un-highlight, set back to original color
                basicMethoidNode.resetTitleBarColor();
            }
        }
    }
    
    private void changeFieldNodeHighlight(ICanvasFieldNode node) {
        if (node instanceof BasicCanvasFieldNode) {
            BasicCanvasFieldNode basicFieldNode = (BasicCanvasFieldNode) node;
            basicFieldNode.setHighlight(mIsHighlighting);
        }
    }
    
    private void changeTreeNodeHighlight(IMapperTreeNode node) {
        if (node instanceof BasicMapperTreeNode) {
            BasicMapperTreeNode basicTreeNode = (BasicMapperTreeNode) node;
            if (mIsHighlighting) {
                basicTreeNode.addToSelection();
            } else {
                // un-highlight, set back to original color
                basicTreeNode.removeFromSelection();
            }
        }
    }
}
