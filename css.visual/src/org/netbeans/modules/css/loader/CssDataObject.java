/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CssDataObject.java
 *
 * Created on December 8, 2004, 11:04 PM
 */

package org.netbeans.modules.css.loader;

import org.netbeans.modules.css.editor.CssEditorSupport;
import org.netbeans.modules.css.visual.model.CssMetaModel;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.Node;

/**
 * Data Object that represents one css file.
 * @author Winston Prakash
 * @version 1.0
 */
public class CssDataObject extends MultiDataObject{

    /** Creates a new instance of CssDataObject */
    public CssDataObject(FileObject fileObject, UniFileLoader loader) throws DataObjectExistsException{
        super(fileObject, loader);
        
        CookieSet cookieSet = getCookieSet();
        cookieSet.add(new CssEditorSupport(this));
        // XXX This is an ugly hack for some classes to get the
        // DataObject. Find other neat ways. (I hate this, but time is running out)
        CssMetaModel.setDataObject(this);
    }

    protected Node createNodeDelegate() {
        return new CssDataNode(this);
    }

    /** Add the save cookie to the css data object **/
    public void addSaveCookie(SaveCookie saveCookie){
        if(getCookie(SaveCookie.class) == null) {
            getCookieSet().add(saveCookie);
            setModified(true);
        }
    }

    /** Remove save cookie from the css data object **/
    public void removeSaveCookie(SaveCookie saveCookie){
        Cookie cookie = getCookie(SaveCookie.class);
        if(cookie != null && cookie.equals(saveCookie)) {
            getCookieSet().remove(saveCookie);
            setModified(false);
        }
    }
}
