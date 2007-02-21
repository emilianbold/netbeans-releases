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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.autolayout;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import com.nwoods.jgo.JGoLayer;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoPort;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.AbstractCanvasLink;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasFieldNode;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasMethoidNode;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasNodeToTreeLink;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasTreeToNodeLink;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.BasicCanvasTreeToTreeLink;
import org.netbeans.modules.soa.mapper.basicmapper.tree.BasicMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasNodeToNodeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasNodeToTreeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasTreeToNodeLink;

public class TreeLayout {

    static final int YPAD = 20;
    static final int XPAD = 40;
    static final int XOFFSET = 10;

    Map mMap = new HashMap();
    TreeMap mSrcTreeLinks = new TreeMap();
    int mSrcLinks;
    Set mRoots = new HashSet();

    void addLink(int row, Object linkObj) {
        Integer key = new Integer(row);
        List list = (List)mSrcTreeLinks.get(key);
        if (list == null) {
            list = new LinkedList();
            mSrcTreeLinks.put(key, list);
        }
        list.add(linkObj);
        mSrcLinks++;
    }

    public void performLayout(JGoLayer layer) {
        for (JGoListPosition pos = layer.getFirstObjectPos();
             pos != null; pos = layer.getNextObjectPos(pos)) {
             Object obj = layer.getObjectAtPos(pos);
             if (obj instanceof BasicCanvasMethoidNode) {
                 addNode((BasicCanvasMethoidNode) obj);
             } else if (obj instanceof BasicCanvasTreeToTreeLink) {
                 BasicCanvasTreeToTreeLink ttl = 
                     (BasicCanvasTreeToTreeLink)obj;
                 BasicMapperTreeNode tn = 
                     (BasicMapperTreeNode)ttl.getSourceTreeAddress();
                 //tn.expand();
                 addLink(tn.getRow(), ttl);
             }
        }
        Iterator iter = mSrcTreeLinks.values().iterator();
        int i = 1;
        int size = mSrcLinks;
        while (iter.hasNext()) {
            List list = (List)iter.next();
            Iterator ii = list.iterator();
            while (ii.hasNext()) {
                Object jgolink = ii.next();
                // hack
                ((AbstractCanvasLink)jgolink).setPosition(i, size);
                i++;
            }
        }
        List sortedRoots = new ArrayList();
        sortedRoots.addAll(mRoots);
        Collections.sort(sortedRoots,  new Comparator() {
                public int compare(Object l, Object r) {
                    // keep the roots in vertical order
                    DefaultMutableTreeNode ln = (DefaultMutableTreeNode)l;
                    DefaultMutableTreeNode rn = (DefaultMutableTreeNode)r;
                    BasicCanvasMethoidNode n1 = (BasicCanvasMethoidNode)ln.getUserObject();
                    BasicCanvasMethoidNode n2 = (BasicCanvasMethoidNode)rn.getUserObject();
                    return n1.getLocation().y - n2.getLocation().y;
                }
            });
        iter = sortedRoots.iterator();
        int y = 0;
        while (iter.hasNext()) {
            DefaultMutableTreeNode root = 
                (DefaultMutableTreeNode)iter.next();
            int startX = computeWidth(root);
            if (size > 0) {
                //startX -= XPAD; // strip last pad
                startX += (6+(6 * size)); // add space for tree links
            }
            BasicCanvasMethoidNode n = (BasicCanvasMethoidNode)
                root.getUserObject();
            Rectangle rec = n.getBounding();
            y += YPAD;
            y = layout(root, startX, y, rec.width);
        }
        mMap.clear();
        mSrcTreeLinks.clear();
        mSrcLinks = 0;
        mRoots.clear();
    }

    int layout(DefaultMutableTreeNode tn, 
               int startX, int startY,
               int maxColWidth) {
        Object obj = tn.getUserObject();
        int newY = 0;
        int y = startY;
        int yOffset = 0;
        if (obj instanceof BasicCanvasMethoidNode) {
            BasicCanvasMethoidNode n = (BasicCanvasMethoidNode)obj;
            Rectangle rec = n.getBounding();
            int x = startX - rec.width; // right align
            n.setLocation(x, startY);
            startX -= (maxColWidth + XPAD);
            newY = rec.y + rec.height + YPAD;
            Iterator iter = 
                n.getNodes().iterator();
            while (iter.hasNext()) {
                BasicCanvasFieldNode child =
                    (BasicCanvasFieldNode) iter.next();
                JGoPort port = (JGoPort) child.getConnectPointObject();
                JGoListPosition pos = port.getFirstLinkPos();
                if (pos != null) {
                    JGoLink jgolink = port.getLinkAtPos(pos);
                    if (child.getFieldNode().isInput()) {
                        if (jgolink instanceof ICanvasTreeToNodeLink) {
                            Rectangle r = child.getBounding();
                            yOffset = r.y + r.height - rec.y;
                        }
                    }
                }
            }
        }
        y += yOffset;
        Enumeration e = tn.children();
        int colWidth = 0;
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode c = 
                (DefaultMutableTreeNode)e.nextElement();
            BasicCanvasMethoidNode n = 
                (BasicCanvasMethoidNode)c.getUserObject();
            Rectangle r = n.getBounding();
            if (r.width > colWidth) {
                colWidth = r.width;
            }
        }
        e = tn.children();
        int i = tn.getChildCount()-1;
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode c = 
                (DefaultMutableTreeNode)e.nextElement();
            y = layout(c, startX - (i * XOFFSET), y, colWidth);
            if (y > newY) {
                newY = y;
            }
            --i;
        }
        return newY;
    }

    int computeWidth(DefaultMutableTreeNode tn) {
        int colWidth = 0;
        int totalWidth = 0;
        Object obj = tn.getUserObject();
        if (obj instanceof BasicCanvasMethoidNode) {
            BasicCanvasMethoidNode n = (BasicCanvasMethoidNode)obj;
            Rectangle rect = n.getBounding();
            colWidth = rect.width;
        }
        Enumeration e = tn.children();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode)e.nextElement();
            int colTotalWidth = computeWidth(c);
            if (colTotalWidth > totalWidth) {
                totalWidth = colTotalWidth;
            }
            colWidth += XOFFSET;
        }
        colWidth += XPAD;
        return totalWidth + colWidth;
    }

    private DefaultMutableTreeNode addNode(BasicCanvasMethoidNode node) {
        DefaultMutableTreeNode tn = (DefaultMutableTreeNode)mMap.get(node);
        if (tn == null) {
            tn = new DefaultMutableTreeNode(node);
            mMap.put(node, tn);
            Collection list = ((BasicCanvasMethoidNode)node).getNodes();
            int count = list.size();
            Iterator iter = list.iterator();
            boolean noOutput = true;
            boolean linkedToOutputTree = false;
            boolean nullOutput = false;
            int i = 1;
            while (iter.hasNext()) {
                BasicCanvasFieldNode child =
                    (BasicCanvasFieldNode) iter.next();
                JGoPort port = (JGoPort) child.getConnectPointObject();
                JGoListPosition pos = port.getFirstLinkPos();
                if (pos != null) {
                    JGoLink jgolink = port.getLinkAtPos(pos);
                    if (child.getFieldNode().isInput()) {
                        if (jgolink instanceof ICanvasNodeToNodeLink) {
                            ICanvasNodeToNodeLink link =  
                                (ICanvasNodeToNodeLink)jgolink;
                            // hack
                            //((BasicCanvasNodeToNodeLink)link).setPosition(i, count);
                            i++;
                            BasicCanvasFieldNode src = 
                                (BasicCanvasFieldNode)link.getSourceFieldNode();
                            BasicCanvasMethoidNode srcNode = 
                                (BasicCanvasMethoidNode)src.getContainer();
                            tn.add(addNode(srcNode));
                        } else if (jgolink instanceof ICanvasTreeToNodeLink) {
                            BasicCanvasTreeToNodeLink tnl = 
                                (BasicCanvasTreeToNodeLink)jgolink;
                            BasicMapperTreeNode tnode = 
                                (BasicMapperTreeNode)tnl.getSourceTreeAddress();
                            //tnode.expand();
                            addLink(tnode.getRow(), jgolink);
                        }
                    } else {
                        if (jgolink instanceof ICanvasNodeToTreeLink) {
                            BasicCanvasNodeToTreeLink ntl = 
                                (BasicCanvasNodeToTreeLink)jgolink;
                            BasicMapperTreeNode tnode = 
                                (BasicMapperTreeNode)ntl.getDestTreeAddress();
                            //tnode.expand();
                            linkedToOutputTree = true;
                        }
                    }
                } else {
                    // no link
                    if (child.getFieldNode().isOutput()) {
                        nullOutput = true;
                    }
                }
                if (child.getFieldNode().isOutput()) {
                    noOutput = false;
                }
            }
            if (noOutput || linkedToOutputTree || nullOutput) {
                mRoots.add(tn);
            }
        }
        return tn;
    }
}