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
