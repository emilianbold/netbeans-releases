/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html;
import java.io.IOException;

import org.openide.*;
import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.filesystems.*;
import org.openide.text.DataEditorSupport;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.cookies.ViewCookie;

/** Object that represents one html file.
*
* @author Ian Formanek
*/
public class HtmlDataObject extends MultiDataObject implements CookieSet.Factory {

    private static final String HTML_ICON_BASE =
        "/org/netbeans/modules/html/htmlObject"; // NOI18N

    static final long serialVersionUID =8354927561693097159L;
    
    /** New instance.
    * @param pf primary file object for this data object
    * @param loader the data loader creating it
    * @exception DataObjectExistsException if there was already a data object for it 
    */
    public HtmlDataObject(FileObject pf, UniFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet set = getCookieSet();
        set.add(HtmlEditorSupport.class, this);
        set.add(ViewSupport.class, this);
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        DataNode n = new HtmlDataNode (this, Children.LEAF);
        n.setIconBase (HTML_ICON_BASE);
        return n;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HtmlLoader.class.getName () + ".Obj"); // NOI18N
    }
    
    /** return a cookie for given Class */
    public Node.Cookie getCookie(Class klass) {
        if (org.openide.cookies.CompilerCookie.class.isAssignableFrom(klass)) {
            return null;
        }
        
        return super.getCookie(klass);
    }

    /** Creates new Cookie */
    public Node.Cookie createCookie(Class klass) {
        if (klass == HtmlEditorSupport.class) {
            HtmlEditorSupport es = new HtmlEditorSupport(this);
            return es;
        } else if (klass == ViewSupport.class) {
            return new ViewSupport(getPrimaryEntry());
        } else {
            return null;
        }
    }
    
    static final class ViewSupport implements ViewCookie {
        /** entry */
        private MultiDataObject.Entry primary;
        
        /** Constructs new ViewSupport */
        public ViewSupport(MultiDataObject.Entry primary) {
            this.primary = primary;
        }
        
         public void view () {
             try {
                 TopManager.getDefault ().showUrl (primary.getFile ().getURL ());
             } catch (FileStateInvalidException e) {
             }
         }
    }
    
}
