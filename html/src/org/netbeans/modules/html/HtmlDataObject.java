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
    
    // public static final String PROP_FOR_EDIT = "forEdit";   // NOI18N
    
    // boolean forEdit = false;
    
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
        DataNode n = new HtmlDataNode (this, Children.LEAF);
        n.setIconBase (HTML_ICON_BASE);
        return n;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HtmlLoader.class.getName () + ".Obj"); // NOI18N
    }

    /*
    public boolean isForEdit () {
        Object o = getPrimaryFile ().getAttribute (PROP_FOR_EDIT);
        boolean ret = false;
        if (o instanceof Boolean)
            ret = ((Boolean) o).booleanValue ();
        return ret;
    }
    
    public void setForEdit (boolean isForEdit) throws IOException {
        FileObject fo = getPrimaryFile ();

        boolean oldVal = false;
        Object o = fo.getAttribute (HtmlDataObject.PROP_FOR_EDIT);
        if ((o instanceof Boolean) && ((Boolean)o).booleanValue ())
            oldVal = true;
        if (oldVal == isForEdit)
            return;

        fo.setAttribute(HtmlDataObject.PROP_FOR_EDIT, (isForEdit ? new Boolean (true) : null));
        // firePropertyChange(DataObject.PROP_TEMPLATE, new Boolean(!newTempl), new Boolean(newTempl));
    }
     */
}

