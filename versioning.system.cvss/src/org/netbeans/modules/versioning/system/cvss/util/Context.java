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

package org.netbeans.modules.versioning.system.cvss.util;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Encapsulates context of an action. There are two ways in which context may be defined:
 * - list of files (f/b.txt, f/c.txt, f/e.txt)
 * - list of roots (top folders) plus list of exclusions (f),(a.txt, d.txt)
 * 
 * @author Maros Sandor
 */
public class Context implements Serializable {

    public static final Context Empty = new Context(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST); 
    
    private static final long serialVersionUID = 1L;
    
    private final List filteredFiles;
    private final List rootFiles;
    private final List exclusions;

    public Context(List filteredFiles, List rootFiles, List exclusions) {
        this.filteredFiles = filteredFiles;
        this.rootFiles = rootFiles;
        this.exclusions = exclusions;
    }

    public List getRoots() {
        return rootFiles;
    }

    public List getExclusions() {
        return exclusions;
    }

    /**
     * Gets exact set of files to operate on, it is effectively defined as (rootFiles - exclusions). This set
     * is NOT suitable for Update command because Update should operate on all rootFiles and just exclude some subfolders.
     * Otherwise update misses new files and folders directly in rootFiles folders. 
     *  
     * @return files to operate on
     */ 
    public File [] getFiles() {
        return (File[]) filteredFiles.toArray(new File[filteredFiles.size()]);
    }
}
