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
import java.net.InetAddress;
import org.openide.filesystems.FileObject;

/**
 * Interface for Open File implementations.
 *
 * @author  Marian Petras
 */
public interface OpenFileImpl {

    /**
     * Tries to find a <code>FileObject</code> for the specified
     * <code>File</code>. This method may force a user interaction.
     *
     * @param  f  existing file
     * @return  <code>FileObject</code> matching the <code>File</code>;
     *          or <code>null</code> if the matching <code>FileObject</code>
     *          was not determined (possibly due to a user's interaction)
     */
    FileObject findFileObject(File f);
    
    /**
     * Opens the specified <code>FileObject</code>.
     *
     * @param  fileObject  file to open
     * @param  fileName  name of the file (used only in messages)
     * @param  wait    whether to wait until requested to return a status
     * @param  address address to send reply to, valid only if wait set
     * @param  port    port to send reply to, valid only if wait set
     * @param  line    line number to try to open to (starting at zero),
     *                 or <code>-1</code> to ignore
     */
    void open(FileObject fileObject,
              String fileName,
              final boolean wait,
              InetAddress address,
              int port,
              int line);

}
