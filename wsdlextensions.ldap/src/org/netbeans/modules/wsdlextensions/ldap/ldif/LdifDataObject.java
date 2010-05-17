package org.netbeans.modules.wsdlextensions.ldap.ldif;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;

public class LdifDataObject extends MultiDataObject 
implements Lookup.Provider {

    public LdifDataObject(FileObject pf, LdifDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }
    
    protected Node createNodeDelegate() {
        return new LdifDataNode(this, getLookup());
    }

    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
