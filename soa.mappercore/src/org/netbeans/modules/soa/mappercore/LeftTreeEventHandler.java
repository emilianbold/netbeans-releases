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

package org.netbeans.modules.soa.mappercore;

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;

/**
 *
 * @author anjeleevich
 */
public class LeftTreeEventHandler extends AbstractMapperEventHandler {

    private MouseEvent initialEvent = null;
    private TreePath initialPath = null;
    private TreeSourcePin initialSourcePin = null;
    
    
    public LeftTreeEventHandler(LeftTree leftTree) {
        super(leftTree.getMapper(), leftTree);
    }
    
    
    private void reset() {
        initialEvent = null;
        initialPath = null;
        initialSourcePin = null;
    }

    
    public void mousePressed(MouseEvent e) {
        reset();
        
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        
        initialPath = getLeftTree().getPathForLocation(e.getX(), e.getY());
        if (initialPath != null) {
            initialSourcePin = getMapperModel().getTreeSourcePin(initialPath);
            if (initialSourcePin != null) {
                initialEvent = e;
            }
        }
        
        if (initialEvent == null) {
            reset();
        }
    }

    
    public void mouseReleased(MouseEvent e) {
        reset();
    }


    public void mouseDragged(MouseEvent e) {
        if ((initialEvent != null)
                && (initialEvent.getPoint().distance(e.getPoint()) >= 5)) 
        {
            LeftTree leftTree = getLeftTree();
            LinkTool linkTool = getMapper().getLinkTool();
            Transferable transferable = linkTool.activateOutgoing(
                    initialSourcePin, null, null);
            startDrag(initialEvent, transferable, MOVE);
            reset();
        }
        
    }
}
