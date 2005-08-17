/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.loader;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;


/**
 *
 * @author Andrei Badea
 */
public class SQLDataObject extends MultiDataObject {
    
    public SQLDataObject(FileObject primaryFile, UniFileLoader loader) throws DataObjectExistsException {
        super(primaryFile, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new SQLEditorSupport(this));
    }

    protected Node createNodeDelegate() {
        return new SQLNode(this);
    }
    
    void addSaveCookie(SaveCookie saveCookie) {
        getCookieSet().add(saveCookie);
    }
    
    void removeSaveCookie(SaveCookie saveCookie) {
        getCookieSet().remove(saveCookie);
    }
}
