/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openfile;

import java.io.File;

import org.openide.filesystems.FileObject;

/**
 * Interface for Open File implementations.
 *
 * @author  Marian Petras
 */
public interface OpenFileImpl {

    /**
     * Opens the specified <code>FileObject</code>.
     *
     * @param  fileObject  file to open
     * @param  line    line number to try to open to (starting at zero),
     *                 or <code>-1</code> to ignore
     * @return true on success, false on failure
     */
    boolean open(FileObject fileObject, int line);

}
