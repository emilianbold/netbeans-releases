package org.netbeans.modules.wsdlextensions.ldap.ldif;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class LdifDataNode extends DataNode {

    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/wsdlextensions/ldap/resources/ldiffile16x16.png";

    public LdifDataNode(LdifDataObject obj) {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }
    LdifDataNode(LdifDataObject obj, Lookup lookup) {
        super(obj, Children.LEAF, lookup);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }

//    /** Creates a property sheet. */
//    protected Sheet createSheet() {
//        Sheet s = super.createSheet();
//        Sheet.Set ss = s.get(Sheet.PROPERTIES);
//        if (ss == null) {
//            ss = Sheet.createPropertiesSet();
//            s.put(ss);
//        }
//        // TODO add some relevant properties: ss.put(...)
//        return s;
//    }

}
