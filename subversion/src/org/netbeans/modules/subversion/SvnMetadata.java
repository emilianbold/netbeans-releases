/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion;

import java.io.*;
import java.util.*;

/**
 * Encapsulates content of CVS/* files that is used to save to-be-deleted metadata. 
 * 
 * @author Maros Sandor
 */
public class SvnMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Map/*<File, byte[]>*/ entries;

    /**
     * Reads and stores CVS metadata and also marks all files in entries as removed. 
     * 
     * @param file
     * @return
     * @throws java.io.IOException
     */ 
    public static SvnMetadata readAndRemove(File file) throws IOException {
        if (!file.isDirectory()) throw new IllegalArgumentException(file + " is not a directory"); // NOI18N

        SvnMetadata data = new SvnMetadata();
        
        return data;
    }

    /** Saves metadata in the given folder, typically named CVS.
     *
     * @param dir folder to save to
     * @throws java.io.IOException 
     */
    public void save(File dir) throws IOException {
        dir.mkdirs();
    }

    public SvnMetadata() {
    }
}
