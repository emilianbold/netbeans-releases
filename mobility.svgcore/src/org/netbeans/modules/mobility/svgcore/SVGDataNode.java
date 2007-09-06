/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore;

import java.awt.datatransfer.Transferable;
import java.util.List;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

/**
 *
 * @author Pavel Benes
 */
public class SVGDataNode extends DataNode {
    
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/mobility/svgcore/resources/svg.png"; //NOI18N
    
    public SVGDataNode(SVGDataObject obj) {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }
        
    public boolean canCopy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }
}
