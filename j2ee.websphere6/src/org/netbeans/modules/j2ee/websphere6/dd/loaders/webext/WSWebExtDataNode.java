package org.netbeans.modules.j2ee.websphere6.dd.loaders.webext;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

public class WSWebExtDataNode extends DataNode {
    
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/j2ee/websphere6/dd/resources/ws6.gif";
    
    public WSWebExtDataNode(WSWebExtDataObject obj) {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }
    
}
