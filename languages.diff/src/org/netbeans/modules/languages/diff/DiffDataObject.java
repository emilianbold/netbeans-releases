/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.languages.diff;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;


@MIMEResolver.ExtensionRegistration(
    mimeType="text/x-diff",
    position=180,
    displayName="#DiffResolver",
    extension={ "diff", "rej", "patch" }
)
public class DiffDataObject extends MultiDataObject {

    public DiffDataObject (FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super (pf, loader);
        CookieSet cookies = getCookieSet ();
        cookies.add ((Node.Cookie) DataEditorSupport.create (this, getPrimaryEntry (), cookies));
    }

    @Override
    protected Node createNodeDelegate () {
        DataNode node = new DataNode (this, Children.LEAF, getLookup ());
        return node;
    }

    @Override
    public Lookup getLookup () {
        return getCookieSet ().getLookup ();
    }
}
