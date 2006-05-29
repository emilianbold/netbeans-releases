/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

public class WSEjbExtDataNode extends DataNode {
    
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/j2ee/websphere6/dd/resources/ws4.gif";
    
    public WSEjbExtDataNode(WSEjbExtDataObject obj) {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }
    
}
