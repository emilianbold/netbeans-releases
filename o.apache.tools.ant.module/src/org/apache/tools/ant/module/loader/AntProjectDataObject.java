/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick.
 */

package org.apache.tools.ant.module.loader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.cookies.DebuggerCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.nodes.AntProjectNode;
import org.apache.tools.ant.module.xml.AntProjectSupport;

/** Represents a Ant Project object in the Repository.
 */
public class AntProjectDataObject extends MultiDataObject implements PropertyChangeListener {

    private static final long serialVersionUID = -5884496744835713660L;

    public AntProjectDataObject(FileObject pf, AntProjectDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add (new AntProjectDataEditor (this));
        FileObject prim = getPrimaryFile ();
        AntProjectCookie proj = new AntProjectSupport (prim);
        cookies.add (proj);
        if (proj.getFile () != null) {
            MultiDataObject.Entry pe = getPrimaryEntry ();
            cookies.add (new AntCompilerSupport.Compile (pe));
            cookies.add (new AntCompilerSupport.Build (pe));
            cookies.add (new AntCompilerSupport.Clean (pe));
            cookies.add (new AntExecSupport (pe));
            cookies.add (new AntActionInstance (proj));
        }
        addPropertyChangeListener (this);
    }
    
    // #12864: AntExecSupport only incidentally implements DebuggerCookie
    public Node.Cookie getCookie (Class clazz) {
        if (clazz == DebuggerCookie.class) {
            return null;
        }
        return super.getCookie (clazz);
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.identifying-project");
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
        //System.err.println("APDO.propertyChange: " + prop);
        if (prop == null || prop.equals (DataObject.PROP_PRIMARY_FILE)) { // #11979
            // XXX this might be better handled by overriding FileEntry.rename/move:
            ((AntProjectSupport) getCookie (AntProjectSupport.class)).setFileObject (getPrimaryFile ());
        }
    }

}
