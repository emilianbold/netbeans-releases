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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.util.Date;
import java.util.Enumeration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 * Compares objects in a folder.
 * @author Jaroslav Tulach, Jesse Glick
 */
class FolderComparator extends DataFolder.SortMode {
    /** modes */
    public static final int NONE = 0;
    public static final int NAMES = 1;
    public static final int CLASS = 2;
    /** first of all DataFolders, then other object, everything sorted
    * by names
    */
    public static final int FOLDER_NAMES = 3;
    /** by folders, then modification time */
    public static final int LAST_MODIFIED = 4;
    /** by folders, then size */
    public static final int SIZE = 5;


    /** mode to use */
    private int mode;

    /** New comparator. Sorts folders first and everything by names.
    */
    public FolderComparator () {
        this (FOLDER_NAMES);
    }

    /** New comparator.
    * @param mode one of sorting type constants
    */
    public FolderComparator (int mode) {
        this.mode = mode;
    }

    /** Comparing method. Can compare two DataObjects
    * or two Nodes (if they have data object cookie)
    */
    public int compare (Object o1, Object o2) {
        DataObject obj1;
        DataObject obj2;

        if (o1 instanceof Node) {
            obj1 = (DataObject)((Node)o1).getCookie (DataObject.class);
            obj2 = (DataObject)((Node)o2).getCookie (DataObject.class);
        } else {
            obj1 = (DataObject)o1;
            obj2 = (DataObject)o2;
        }

        switch (mode) {
        case NONE:
            return 0;
        case NAMES:
            return compareNames (obj1, obj2);
        case CLASS:
            return compareClass (obj1, obj2);
        case FOLDER_NAMES:
            return compareFoldersFirst (obj1, obj2);
        case LAST_MODIFIED:
            return compareLastModified(obj1, obj2);
        case SIZE:
            return compareSize(obj1, obj2);
        default:
            assert false : mode;
            return 0;
        }
    }


    /** for sorting data objects by names */
    private int compareNames (DataObject obj1, DataObject obj2) {
        // #35069 - use extension for sorting if displayname is same.
        //  Otherwise the order of files is random.
        int part = obj1.getName().compareTo(obj2.getName());
        return part != 0 ? part :
            obj1.getPrimaryFile().getExt().compareTo(obj2.getPrimaryFile().getExt());
    }

    /** for sorting folders first and then by names */
    private int compareFoldersFirst (DataObject obj1, DataObject obj2) {
        if (obj1.getClass () != obj2.getClass ()) {
            // if classes are different than the folder goes first
            if (obj1 instanceof DataFolder) {
                return -1;
            }
            if (obj2 instanceof DataFolder) {
                return 1;
            }
        }

        // otherwise compare by names
        return compareNames(obj1, obj2);
    }

    /** for sorting data objects by their classes */
    private int compareClass (DataObject obj1, DataObject obj2) {
        Class<?> c1 = obj1.getClass ();
        Class<?> c2 = obj2.getClass ();

        if (c1 == c2) {
            return compareNames(obj1, obj2);
        }

        // sort by classes
        DataLoaderPool dlp = DataLoaderPool.getDefault();
        final Enumeration loaders = dlp.allLoaders ();

        // PENDING, very very slow
        while (loaders.hasMoreElements ()) {
            Class<? extends DataObject> clazz = ((DataLoader) (loaders.nextElement ())).getRepresentationClass ();

            // Sometimes people give generic DataObject as representation class.
            // It is not always avoidable: see e.g. org.netbeans.core.windows.layers.WSLoader.
            // In this case the overly flexible loader would "poison" sort-by-type, so we
            // make sure to ignore this.
            if (clazz == DataObject.class) continue;
            
            boolean r1 = clazz.isAssignableFrom (c1);
            boolean r2 = clazz.isAssignableFrom (c2);

            if (r1 && r2) return compareNames(obj1, obj2);
            if (r1) return -1;
            if (r2) return 1;
        }
        return compareNames(obj1, obj2);
    }

    /**
     * Sort folders alphabetically first. Then files, newest to oldest.
     */
    private static int compareLastModified(DataObject obj1, DataObject obj2) {
        if (obj1 instanceof DataFolder) {
            if (obj2 instanceof DataFolder) {
                return obj1.getName().compareTo(obj2.getName());
            } else {
                return -1;
            }
        } else {
            if (obj2 instanceof DataFolder) {
                return 1;
            } else {
                FileObject fo1 = obj1.getPrimaryFile();
                FileObject fo2 = obj2.getPrimaryFile();
                Date d1 = fo1.lastModified();
                Date d2 = fo2.lastModified();
                if (d1.after(d2)) {
                    return -1;
                } else if (d2.after(d1)) {
                    return 1;
                } else {
                    return fo1.getNameExt().compareTo(fo2.getNameExt());
                }
            }
        }
    }

    /**
     * Sort folders alphabetically first. Then files, biggest to smallest.
     */
    private static int compareSize(DataObject obj1, DataObject obj2) {
        if (obj1 instanceof DataFolder) {
            if (obj2 instanceof DataFolder) {
                return obj1.getName().compareTo(obj2.getName());
            } else {
                return -1;
            }
        } else {
            if (obj2 instanceof DataFolder) {
                return 1;
            } else {
                FileObject fo1 = obj1.getPrimaryFile();
                FileObject fo2 = obj2.getPrimaryFile();
                long s1 = fo1.getSize();
                long s2 = fo2.getSize();
                if (s1 > s2) {
                    return -1;
                } else if (s2 > s1) {
                    return 1;
                } else {
                    return fo1.getNameExt().compareTo(fo2.getNameExt());
                }
            }
        }
    }

}
