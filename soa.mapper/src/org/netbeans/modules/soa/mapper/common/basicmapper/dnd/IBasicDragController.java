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

package org.netbeans.modules.soa.mapper.common.basicmapper.dnd;

import java.awt.Point;
import java.awt.dnd.DragSourceContext;

import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IBasicTreeViewSelectionPathController;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;

public interface IBasicDragController {
    public void setTransferObject(Object ob);

    public Object getTransferObject();

    public void setOriginatingDragNode(IMapperNode node);

    public IMapperNode getOriginatingDragNode();
    
    public void setDragLinkEndLocation(Point location);
    
    public void resetDragLinkEndLocation();
    
    public void setDragLink(Point location, IBasicTreeViewSelectionPathController controller);

    public void setDragLink(ICanvasLink link);
    
    public void clearDragLink();
    
    public void setLinkDragSourceContext(DragSourceContext dsc);
    
    public DragSourceContext getLinkDragSourceContext();
}
