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

package org.apache.tools.ant.module.loader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.nodes.AntProjectNode;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;

public class AntProjectDataObject extends MultiDataObject implements PropertyChangeListener {

    public AntProjectDataObject(FileObject pf, AntProjectDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add (new AntProjectDataEditor (this));
        FileObject prim = getPrimaryFile ();
        AntProjectCookie proj = new AntProjectSupport (prim);
        cookies.add (proj);
        if (proj.getFile () != null) {
            MultiDataObject.Entry pe = getPrimaryEntry ();
            cookies.add (new AntActionInstance (proj));
        }
        cookies.add(new CheckXMLSupport(DataObjectAdapters.inputSource(this)));
        addPropertyChangeListener (this);
    }
    
    protected Node createNodeDelegate () {
        return new AntProjectNode (this);
    }

    void addSaveCookie (final SaveCookie save) {
        if (getCookie (SaveCookie.class) == null) {
            getCookieSet ().add (save);
            setModified (true);
        }
    }

    void removeSaveCookie (final SaveCookie save) {
        if (getCookie (SaveCookie.class) == save) {
            getCookieSet ().remove (save);
            setModified (false);
        }
    }

    public void propertyChange (PropertyChangeEvent ev) {
        String prop = ev.getPropertyName ();
        if (prop == null || prop.equals (DataObject.PROP_PRIMARY_FILE)) { // #11979
            // XXX this might be better handled by overriding FileEntry.rename/move:
            ((AntProjectSupport) getCookie (AntProjectSupport.class)).setFileObject (getPrimaryFile ());
        }
    }

}
