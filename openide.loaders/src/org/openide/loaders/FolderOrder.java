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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import java.io.IOException;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.loaders.DataFolder.SortMode;


/** A support for keeping order of children for folder list.
 *
 * @author  Jaroslav Tulach
 */
final class FolderOrder extends Object implements Comparator<DataObject> {
    /** Separator of names of two files. The first file should be before
     * the second one in partial ordering
     */
    private static final char SEP = '/';
    
    /** a static map with (FileObject, Reference (Folder))
     */
    private static final WeakHashMap<FileObject, Reference<FolderOrder>> map = 
            new WeakHashMap<FileObject, Reference<FolderOrder>> (101);
    /** A static of known folder orders. Though we hold the
     * FolderOrder with a soft reference which can be collected, even
     * if this happens we would like the new FolderOrder to have any
     * previously determined order attribute. Otherwise under obscure
     * circumstances (#15381) it is possible for the IDE to go into an
     * endless loop recalculating folder orders, since they keep
     * getting collected.
     */
    private static final Map<FileObject, Object> knownOrders = 
            Collections.synchronizedMap(new WeakHashMap<FileObject, Object>(50));
    

    /** map of names of primary files of objects to their index or null */
    private Map<String,Integer> order;
    /** file to store data in */
    private FileObject folder;
    /** if true, partial orderings on disk should be ignored for files in the order */
    private boolean ignorePartials;
    /** a reference to sort mode of this folder order */
    private SortMode sortMode;
    /** previous value of the order */
    private Object previous;

    /** Constructor.
    * @param folder the folder to create order for
    */
    private FolderOrder (FileObject folder) {
        this.folder = folder;
    }
    
    
    /** Changes a sort order for this order
     * @param mode sort mode.
     */
    public void setSortMode (SortMode mode) throws IOException {
        // store the mode to properties
        sortMode = mode;
        mode.write (folder); // writes attribute EA_SORT_MODE -> updates FolderList
        
        // FolderList.changedFolderOrder (folder);
    }
    
    /** Getter for the sort order.
     */
    public SortMode getSortMode () {
        if (sortMode == null) {
            sortMode = SortMode.read (folder);
        }
        return sortMode;
    }
    
    /** Changes the order of data objects.
     */
    public synchronized void setOrder (DataObject[] arr) throws IOException {
        if (arr != null) {
            order = new HashMap<String, Integer> (arr.length * 4 / 3 + 1);

            // each object only once
            Enumeration en = org.openide.util.Enumerations.removeDuplicates (
                org.openide.util.Enumerations.array (arr)
            );

            int i = 0;
            while (en.hasMoreElements ()) {
                DataObject obj = (DataObject)en.nextElement ();
                FileObject fo = obj.getPrimaryFile ();
                if (folder.equals (fo.getParent ())) {
                    // object for my folder
                    order.put (fo.getNameExt (), Integer.valueOf (i++));
                }
            }
            // Explicit order has been set, if written please clear affected
            // order markings.
            ignorePartials = true;
        } else {
            order = null;
        }
        
        write (); // writes attribute EA_ORDER -> updates FolderList
        
        
        // FolderList.changedFolderOrder (folder);
    }

    /**
     * Get ordering constraints for this folder.
     * Returns a map from data objects to lists of data objects they should precede.
     * @param objects a collection of data objects known to be in the folder
     * @return a constraint map, or null if there are no constraints
     */
    public synchronized Map<DataObject, List<DataObject>> getOrderingConstraints(Collection<DataObject> objects) {
        final Set<String> partials = readPartials ();
        if (partials.isEmpty ()) {
            return null;
        } else {
            Map<String, DataObject> objectsByName = new HashMap<String, DataObject>();
            for (DataObject d: objects) {
                objectsByName.put(d.getPrimaryFile().getNameExt(), d);
            }
            Map<DataObject, List<DataObject>> m = new HashMap<DataObject, List<DataObject>>();
            for (String constraint: partials) {
                int idx = constraint.indexOf(SEP);
                String a = constraint.substring(0, idx);
                String b = constraint.substring(idx + 1);
                if (ignorePartials && (order.containsKey(a) || order.containsKey(b))) {
                    continue;
                }
                DataObject ad = objectsByName.get(a);
                if (ad == null) {
                    continue;
                }
                DataObject bd = objectsByName.get(b);
                if (bd == null) {
                    continue;
                }
                List<DataObject> l = m.get(ad);
                if (l == null) {
                    m.put(ad, l = new LinkedList<DataObject>());
                }
                l.add(bd);
            }
            return m;
        }
    }

    /** Read the list of intended partial orders from disk.
     * Each element is a string of the form "a<b" for a, b filenames
     * with extension, where a should come before b.
     */
    private Set<String> readPartials () {
        Enumeration<String> e = folder.getAttributes ();
        Set<String> s = new HashSet<String> ();
        while (e.hasMoreElements ()) {
            String name = e.nextElement ();
            if (name.indexOf (SEP) != -1) {
                Object value = folder.getAttribute (name);
                if ((value instanceof Boolean) && ((Boolean) value).booleanValue ())
                    s.add (name);
            }
        }
        return s;
    }

    /** Compares two data object or two nodes.
    */
    public int compare (DataObject obj1, DataObject obj2) {
        Integer i1 = (order == null) ? null : order.get (obj1.getPrimaryFile ().getNameExt ());
        Integer i2 = (order == null) ? null : order.get (obj2.getPrimaryFile ().getNameExt ());

        if (i1 == null) {
            if (i2 != null) return 1;

            // compare by the provided comparator
            return getSortMode ().compare (obj1, obj2);
        } else {
            if (i2 == null) return -1;
            // compare integers
            if (i1.intValue () == i2.intValue ()) return 0;
            if (i1.intValue () < i2.intValue ()) return -1;
            return 1;
        }
    }

    /** Stores the order to files.
    */
    public void write () throws IOException {
        // Let it throw the IOException:
        //if (folder.getFileSystem ().isReadOnly ()) return; // cannot write to read-only FS
        if (order == null) {
            // if we should clear the order
            folder.setAttribute (DataFolder.EA_ORDER, null);
        } else {
            // Stores list of file names separated by /
            java.util.Iterator<Map.Entry<String, Integer>> it = order.entrySet ().iterator ();
            String[] filenames = new String[order.size ()];
            while (it.hasNext ()) {
                Map.Entry<String, Integer> en = it.next ();
                String fo = en.getKey ();
                int indx = en.getValue ().intValue ();
                filenames[indx] = fo;
            }
            StringBuffer buf = new StringBuffer (255);
            for (int i = 0; i < filenames.length; i++) {
                if (i > 0) {
                    buf.append ('/');
                }
                buf.append (filenames[i]);
            }

            // Read *before* setting EA_ORDER, since org.netbeans.modules.apisupport.project.layers.WritableXMLFileSystem
            // will kill off the partials when it gets that:
            Set<String> p = ignorePartials ? readPartials() : null;
            
            folder.setAttribute (DataFolder.EA_ORDER, buf.toString ());

            if (ignorePartials) {
                // Reverse any existing partial orders among files explicitly
                // mentioned in the order.
                if (! p.isEmpty ()) {
                    Set<String> f = new HashSet<String> ();
                    for (String fo: order.keySet()) {
                        f.add (fo);
                    }
                    for (String s: p) {
                        int idx = s.indexOf (SEP);
                        if (f.contains (s.substring (0, idx)) &&
                            f.contains (s.substring (idx + 1))) {
                            folder.setAttribute (s, null);
                        }
                    }
                }
                // Need not do this again for this order:
                ignorePartials = false;
            }
        }
    }
    
    /** Reads the order from disk.
     */
    private void read () {
        Object o = folder.getAttribute (DataFolder.EA_ORDER);
        
        if ((previous == null && o == null) ||
            (previous != null && previous.equals (o))) {
            // no change in order
            return;
        }
        
        if ((o instanceof Object[]) && (previous instanceof Object[])) {
            if (compare((Object[]) o, (Object[]) previous)) {
                return;
            }
        }
        
        doRead (o);
        
        previous = o;
        if (previous != null) {
            knownOrders.put(folder, previous);
        }
        
        FolderList.changedFolderOrder (folder);
    }

    /** Compares two arrays */
    private static boolean compare(Object[] a, Object[] b) {
        if (a == b) {
            return true;
        }
        
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            if (a[i] != b[i]) {
                if (a[i] == null) {
                    return false;
                }
                
                if (a[i].equals(b[i])) {
                    continue;
                }
                
                if ((a[i] instanceof Object[]) && (b[i] instanceof Object[])) {
                    if (compare((Object[]) a[i], (Object[]) b[i])) {
                        continue;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        
        Object[] arr = (a.length > b.length) ? a : b;
        if (checkNonNull(arr, len)) {
            return false;
        }
        
        return true;
    }
    
    private static boolean checkNonNull(Object[] a, int from) {
        for (int i = from; i < a.length; i++) {
            if (a[i] != null) {
                return true;
            }
        }
        
        return false;
    }
    
    /** Reads the values from the object o
     * @param o value of attribute EA_ORDER
     */
    private void doRead (Object o) {
        if (o == null) {
            order = null;
            return;
        } else if (o instanceof String[][]) {
            // Compatibility:
            String[][] namesExts = (String[][]) o;

            if (namesExts.length != 2) {
                order = null;
                return;
            }
            String[] names = namesExts[0];
            String[] exts = namesExts[1];

            if (names == null || exts == null || names.length != exts.length) {
                // empty order
                order = null;
                return;
            }


            Map<String, Integer> set = new HashMap<String, Integer> (names.length);

            for (int i = 0; i < names.length; i++) {
                set.put (names[i], Integer.valueOf (i));
            }
            order = set;
            return;
            
        } else if (o instanceof String) {
            // Current format:
            String sepnames = (String) o;
            Map<String, Integer> set = new HashMap<String, Integer> ();
            StringTokenizer tok = new StringTokenizer (sepnames, "/"); // NOI18N
            int i = 0;
            while (tok.hasMoreTokens ()) {
                String file = tok.nextToken ();
                set.put (file, Integer.valueOf (i));
                i++;
            }
            
            order = set;
            return;
        } else {
            // Unknown format:
            order = null;
            return;
        }
    }
    

    /** Creates order for given folder object.
    * @param f the folder
    * @return the order
    */
    public static FolderOrder findFor (FileObject folder) {
        FolderOrder order = null;
        synchronized (map) {
            Reference<FolderOrder> ref = map.get (folder);
            order = ref == null ? null : ref.get ();
            if (order == null) {
                order = new FolderOrder (folder);
                order.previous = knownOrders.get(folder);
                order.doRead(order.previous);
                
                map.put (folder, new SoftReference<FolderOrder> (order));
            }
        }
        // always reread the order from disk, so it is uptodate
        synchronized (order) {
            order.read ();
            return order;            
        }        
    }
        
     
    
}
