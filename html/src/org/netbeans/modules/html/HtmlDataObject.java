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

import org.openide.*;
import org.openide.actions.ViewAction;
import org.openide.filesystems.*;
import org.openide.text.EditorSupport;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

/** Object that represents one html file.
*
* @author Ian Formanek
*/
public class HtmlDataObject extends MultiDataObject {

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
                // use editor support
        EditorSupport es = new EditorSupport(getPrimaryEntry());
        es.setMIMEType ("text/html"); // NOI18N
        getCookieSet().add(es);
	
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        DataNode n = new DataNode (this, Children.LEAF);
        n.setIconBase (HTML_ICON_BASE);
        n.setDefaultAction (SystemAction.get (ViewAction.class));
        return n;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HtmlLoader.class.getName () + ".Obj"); // NOI18N
    }

}

/*
 * Log
 *  4    Gandalf   1.3         1/13/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         8/9/99   Ian Formanek    
 * $
 */
