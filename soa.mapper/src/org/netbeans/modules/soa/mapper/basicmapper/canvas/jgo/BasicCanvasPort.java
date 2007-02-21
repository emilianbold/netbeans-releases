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

import com.nwoods.jgo.JGoBrush;
import java.awt.Point;

import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;
import java.awt.Color;
import java.awt.Dimension;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.DrawPort;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;

/**
 * @author radval
 *
 * Port which supports link highlighting.
 */
public class BasicCanvasPort extends JGoPort {
    
    private static Dimension NO_SIZE = new Dimension(0, 0);
    private static final JGoBrush BRUSH =
            JGoBrush.makeStockBrush(new Color(255, 255, 255, 0)); // transparent

    private boolean toggleLinkHighliting = false;
    private DrawPort mDrawPort;
    private JGoPen pen;

    
    public BasicCanvasPort() {
        setBrush(BRUSH);
    }
    
    
    public DrawPort getDrawPort() {
        return mDrawPort;
    }
    
    public void setPortObject(JGoObject obj) {
        super.setPortObject(obj);
        if (obj instanceof DrawPort) {
            mDrawPort = (DrawPort) obj;
        }
    }
    
    public int getLinkPosition() {
        if (mDrawPort != null) {
            if        (mDrawPort.getFieldNode().getFieldNode().isInput()) {
                return JGoObject.LeftCenter;
            } else if (mDrawPort.getFieldNode().getFieldNode().isOutput()) {
                return JGoObject.RightCenter;
            }
        }
        return JGoObject.Center;
    }
    
    public Dimension getDrawSize() {
        if (mDrawPort != null) {
            return mDrawPort.getDrawSize();
        }
        return NO_SIZE;
    }
    
    public boolean doMouseEntered(int modifiers, Point dc, Point vc, JGoView view) {
        
        if (this.getNumLinks() == 0) {
            return false;
        }
        
        JGoListPosition pos = this.getFirstLinkPos();
        while (pos != null) {
            JGoLink link = this.getLinkAtPos(pos);
            if (link instanceof AbstractCanvasLink) {
                AbstractCanvasLink cLink = (AbstractCanvasLink) link;
                IBasicViewManager viewManager = cLink.getMapperCanvas().getParentView().getViewManager();
                //need to check if highlighting is enabled
                if (!viewManager.isHighlightLink() || !viewManager.isToggleHighlighting()) {
                    break;
                }
                
                cLink.startHighlighting();
                
            } else {
                if (pen == null) {
                    pen = link.getPen();
                }
                link.setPen(JGoPen.make(JGoPen.SOLID, 1, ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR));
            }
            pos = this.getNextLinkPos(pos);
        }
        return true;
    }
    
    public boolean doMouseExited(int modifiers, Point dc, Point vc, JGoView view) {
        
        if (this.getNumLinks() == 0) {
            return false;
        }
        
        JGoListPosition pos = this.getFirstLinkPos();
        while (pos != null) {
            JGoLink link = this.getLinkAtPos(pos);
            if (link instanceof AbstractCanvasLink) {
                AbstractCanvasLink cLink = (AbstractCanvasLink) link;
                cLink.stopHighlighting();
            } else {
                link.setPen(pen);
            }
            pos = this.getNextLinkPos(pos);
        }
        
        return true;
    }
}
