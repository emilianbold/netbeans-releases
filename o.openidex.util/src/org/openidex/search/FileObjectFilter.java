/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import org.openide.filesystems.FileObject;

/**
 * Implementations of this interface define which files and folders should be
 * searched and which should be skipped during search over a directory
 * structure.
 *
 * @since  org.openidex.util/3 3.3
 * @author  Marian Petras
 */
public interface FileObjectFilter {

    /** constant representing answer &quot;do not traverse the folder&quot; */
    public static final int DO_NOT_TRAVERSE = 0;
    /** constant representing answer &quot;traverse the folder&quot; */
    public static final int TRAVERSE = 1;
    /**
     * constant representing answer &quot;traverse the folder and all its direct
     * and indirect children (both files and subfolders)&quot;
     */
    public static final int TRAVERSE_ALL_SUBFOLDERS = 2;

    /**
     * Answers a question whether a given file should be searched.
     * The file must be a plain file (not folder).
     *
     * @return  <code>true</code> if the given file should be searched;
     *          <code>false</code> if not
     * @exception  java.lang.IllegalArgumentException
     *             if the passed <code>FileObject</code> is a folder
     */
    public boolean searchFile(FileObject file)
            throws IllegalArgumentException;

    /**
     * Answers a questions whether a given folder should be traversed
     * (its contents searched).
     * The passed argument must be a folder.
     *
     * @return  one of constants {@link #DO_NOT_TRAVERSE},
     *                           {@link #TRAVERSE},
     *                           {@link #TRAVERSE_ALL_SUBFOLDERS};
     *          if <code>TRAVERSE_ALL_SUBFOLDERS</code> is returned,
     *          this filter will not be applied on the folder's children
     *          (both direct and indirect, both files and folders)
     * @exception  java.lang.IllegalArgumentException
     *             if the passed <code>FileObject</code> is not a folder
     */
    public int traverseFolder(FileObject folder)
            throws IllegalArgumentException;

}
