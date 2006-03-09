package org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
 
public class WSWebBndDataNode extends DataNode {
    
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/j2ee/websphere6/dd/resources/ws5.gif";
    
    public WSWebBndDataNode(WSWebBndDataObject obj) {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }
}
