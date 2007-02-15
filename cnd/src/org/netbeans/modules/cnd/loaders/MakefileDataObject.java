/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;

import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.openide.text.DataEditorSupport;

/**
 *  Represents a Makefile object in the Repository.
 */
public class MakefileDataObject extends CndDataObject {

    /** Serial version number */
    static final long serialVersionUID = -5853234372530618782L;


    /** Constructor for this class */
    public MakefileDataObject(FileObject pf, MultiFileLoader loader)
		throws DataObjectExistsException {
	super(pf, loader);
	try {
	    pf.setAttribute(MakefileDataLoader.PROP_MAKEFILE_TYPE, pf);
	} catch (IOException ex) {		    // ignore this exception
	    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
		ex.printStackTrace();
	    }
	}
    }

    /*
     * Return name with extension so renaming etc works
     * But this breaks creating makefile with names xxx.mk from templates, so
     * check if name starts with "__", then return name without the 'extension'
     * (4899051)
     */
    public String getName() {
	String ename = null;
	ename = super.getName();
	 if (!ename.startsWith("__")) // NOI18N
	    ename = getPrimaryFile().getNameExt();
	return ename;
    }

    protected FileObject handleRename(String name) throws IOException {
        FileLock lock = getPrimaryFile().lock();
        int pos = name.lastIndexOf('.');

        try {
            if (pos <= 0){
                // file without separator
                getPrimaryFile().rename(lock, name, null);
            } else {
		getPrimaryFile().rename(lock, name.substring(0, pos), 
                        name.substring(pos + 1, name.length()));
            }
        } finally {
            lock.releaseLock ();
        }
        return getPrimaryFile ();
    }
  

    /**
     *  The init method is called from CndDataObject's constructor.
     */
    protected void init() {
	CookieSet cookies = getCookieSet();

        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
	cookies.add(new MakeExecSupport(getPrimaryEntry()));
    }


    /** Create the delegate node */
    protected Node createNodeDelegate() {
	return new MakefileDataNode(this);
    }
}
