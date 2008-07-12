/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.loaders;

import java.util.Comparator;
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

    public int compare(DataObject o1, DataObject o2) {
        return doCompare((Object) o1, (Object) o2);
    }

    /** Comparing method. Can compare two DataObjects
    * or two Nodes (if they have data object cookie) or two FileObjects
    */
    int doCompare(Object obj1, Object obj2) {
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

    static FileObject findFileObject(Object o) {
        if (o instanceof FileObject) {
            return (FileObject) o;
        }
        if (o instanceof DataObject) {
            return ((DataObject) o).getPrimaryFile();
        }
        Node n = (Node) o;
        DataObject obj = (DataObject) n.getCookie(DataObject.class);
        return obj.getPrimaryFile();
    }

    private static DataObject findDataObject(Object o) {
        if (o instanceof DataObject) {
            return (DataObject) o;
        }
        if (o instanceof FileObject) {
            try {
                return DataObject.find((FileObject) o);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
        }
        Node n = (Node) o;
        DataObject obj = (DataObject) n.getCookie(DataObject.class);
        return obj;
    }

    /** for sorting data objects by names */
    private int compareNames(Object o1, Object o2) {     
        return findFileObject(o1).getNameExt().compareTo(findFileObject(o2).getNameExt());
    }

    /** for sorting folders first and then by names */
    private int compareFoldersFirst(Object o1, Object o2) {
        boolean f1 = findFileObject(o1).isFolder();
        boolean f2 = findFileObject(o2).isFolder();

        if (f1 != f2) {
            return f1 ? -1 : 1;
        }

        // otherwise compare by names
        return compareNames(o1, o2);
    }

    /** for sorting data objects by their classes */
    private int compareClass(Object o1, Object o2) {
        DataObject obj1 = findDataObject(o1);
        DataObject obj2 = findDataObject(o2);

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
    private static int compareLastModified(Object o1, Object o2) {
        boolean f1 = findFileObject(o1).isFolder();
        boolean f2 = findFileObject(o2).isFolder();

        if (f1 != f2) {
            return f1 ? -1 : 1;
        }

        FileObject fo1 = findFileObject(o1);
        FileObject fo2 = findFileObject(o2);
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

    /**
     * Sort folders alphabetically first. Then files, biggest to smallest.
     */
    private static int compareSize(Object o1, Object o2) {
        boolean f1 = findFileObject(o1).isFolder();
        boolean f2 = findFileObject(o2).isFolder();

        if (f1 != f2) {
            return f1 ? -1 : 1;
        }

        FileObject fo1 = findFileObject(o1);
        FileObject fo2 = findFileObject(o2);
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
