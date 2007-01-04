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
import java.lang.Runtime;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.filesystems.FileLock;

import org.netbeans.modules.cnd.execution.BinaryExecSupport;


/** Superclass for Elf objects in the Repository.
 *
 */
public class ExeElfObject extends ExeObject {

    /** Serial version number */
    static final long serialVersionUID = 77972208704093735L;

    public ExeElfObject(FileObject pf, ExeLoader loader)
	throws DataObjectExistsException {
	super(pf, loader);
    }
  
    protected void init() {
	CookieSet cookies = getCookieSet();
    
	// Add whatever capabilities you need, e.g.:
	//cookies.add(new BinaryExecSupport(getPrimaryEntry()));
    }
  
    protected Node createNodeDelegate() {
	return new ExeNode(this);
    }


    /**
     *  Renames all entries and changes their files to new ones.
     *  We only override this to prevent you from changing the template
     *  name to something invalid (like an empty name)
     */
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

    protected DataObject handleCopy(DataFolder df) throws IOException {
	// let super do the job and then set the execution flag on the copy
	DataObject dao = super.handleCopy(df);
	setExecutionFlags(dao.getPrimaryFile());
	return dao;
    }

    protected FileObject handleMove(DataFolder df) throws IOException {
	// let super do the job and then set the execution flag on the copy
	FileObject fob = super.handleMove(df);
	setExecutionFlags(fob);
	return fob;
    }

    private void setExecutionFlags(FileObject fob) throws IOException {
	if (fob != null)
	    Runtime.getRuntime().exec("/bin/chmod +x " + // NOI18N
		    FileUtil.toFile(fob).getPath());
    }
}

