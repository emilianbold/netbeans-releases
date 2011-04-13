/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.php.smarty.editor;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;

public class TplDataObject extends MultiDataObject implements CookieSet.Factory {

    private transient TplEditorSupport tplEditorSupport;

    public TplDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet set = getCookieSet();
        set.add(TplEditorSupport.class, this);
        set.assign(SaveAsCapable.class, new SaveAsCapable() {
            
            @Override
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                TplEditorSupport es = getCookie( TplEditorSupport.class );
                try {
                    es.saveAs( folder, fileName );
                } catch (UserCancelException e) {
                    //ignore, just not save anything
                }
            }
        });
    }

    @Override
    protected Node createNodeDelegate() {
        DataNode dn = new DataNode(this, Children.LEAF, getLookup());
        dn.setIconBaseWithExtension("org/netbeans/modules/php/smarty/resources/tpl-icon.png"); // NOI18N
        return dn;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

        /** Creates new Cookie */
    public Node.Cookie createCookie(Class klass) {
        if (klass.isAssignableFrom(TplEditorSupport.class)) {
            return getHtmlEditorSupport();
        } else {
            return null;
        }
    }

    private synchronized TplEditorSupport getHtmlEditorSupport() {
        if (tplEditorSupport == null) {
            tplEditorSupport = new TplEditorSupport(this);
        }
        return tplEditorSupport;
    }

    // Package accessibility for TplEditorSupport:
    public CookieSet getCookieSet0() {
        return getCookieSet();
    }
}
