/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.impl;

import java.awt.Image;
import java.awt.Image;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.netbeans.ide.loaders.DataFolder;
import com.netbeans.ide.loaders.DataFilter;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.actions.SystemAction;

/** Data system encapsulates logical structure of more file systems.
* It also allows filtering of content of DataFolders
* <P>
* The resulting hierarchy is then presented as BeanContext (or DataFolder)
* so it can be browsed by forthcomming Glasgow Explorer.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
final class DataSystem extends AbstractNode {
  /** generated Serialized Version UID */
  static final long serialVersionUID = -7272169513973465669L;

  /** the file system pool to work with */
  private transient FileSystemPool fileSystemPool;

  /** list of folders. Vector of DataFolder.
  * This item is non-transient because DataObjects are in the ObjectStream
  * and they will be not created by calling constructor. So in initialize
  * method will be acquired these instances instead of new ones.
  */
  Vector roots;

  /** array of subnodes */
  private transient Node[] rootArray;

  /** filter for the data system */
  DataFilter filter;

  /** listener for file system pool actions. */
  private transient FileSystemPoolListener fsPoolL;

  /** listeners for changes in hidden state of the file system */
  transient PropertyChangeListener propL;

  /** Constructor.
  * @param fsp file system pool
  * @param filter the filter for filtering files
  */
  private DataSystem(Children ch, FileSystemPool fsp, DataFilter filter) {
    super (ch);
    fileSystemPool = fsp;
    preinitialize (filter);
    initialize();
    setIconBase ("/com.netbeans.developer.impl.resources/repository");
    RequestProcessor.postRequest (new Runnable () {
      public void run () {
        setDisplayName (
          NbBundle.getBundle (this).getString ("dataSystemName")
        );
      }
    });
  }

  /** Constructor. Uses default file system pool.
  * @param filter the filter to use
  */
  private DataSystem(Children ch, DataFilter filter) {
    this (ch, FileSystemPool.getDefault (), filter);
  }


  /** Factory for DataSystem instances */
  public static DataSystem getDataSystem(DataFilter filter) {
    if (filter == null) filter = DataFilter.ALL;
    DSMap my = new DSMap();
    DataSystem ds = new DataSystem(my, filter);
    my.setDS(ds);
    return ds;
  }

  /** Gets a DataSystem */
  public static DataSystem getDataSystem() {
    return getDataSystem(null);
  }

  /** Used in constructor and immediatelly after deserialization.
  * @param filter the filter to use
  */
  void preinitialize (DataFilter filter) {
    this.filter = filter;
    roots = new Vector ();
  }

  /** Initializes object. Called from constructor and read method.
  */
  void initialize () {
    fsPoolL = new FileSystemPoolListener () {
      /** Called when new file system is added to the pool.
      * @param ev event describing the action
      */
      public void fileSystemAdded (FileSystemPoolEvent ev) {
        addFS (ev.getFileSystem (), false);
      }

      /** Called when a file system is deleted from the pool.
      * @param ev event describing the action
      */
      public void fileSystemRemoved (FileSystemPoolEvent ev) {
        removeFS (ev.getFileSystem (), false);
      }
      /** Called when the fsp is reordered */
      public void fileSystemPoolReordered(FileSystemPoolReorderedEvent ev) {
        reorder(ev);
      }
    };
    // PENDING - turn the listener to weak one in JDK 1.2
    //   and add removing of the listener when this object is finalized
    fileSystemPool.addFileSystemPoolListener (fsPoolL);

    propL = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName ().equals ("hidden")) {
          // change in the hidden state of a file system
          FileSystem fs = (FileSystem)ev.getSource ();
          if (fs.isHidden ()) {
            removeFS(fs, true);
          } else {
            addFS (fs, true);
          }
        } else if (ev.getPropertyName().equals("name")) {
          FileSystem fs = (FileSystem)ev.getSource ();
          removeFS(fs, false);
          addFS(fs, false);
        }
      }
    };

    // clear array of nodes
    rootArray = null;
  }

  /** Initializes properties by adding them to the sheet.
  * Called asynchronously from the constructor.
  */
  protected synchronized Sheet createSheet() {
    Sheet s = Sheet.createDefault ();
    Sheet.Set ss = s.get (Sheet.PROPERTIES);
    ss.put (
      new PropertySupport.ReadWrite (
        DataSystem.this.PROP_DISPLAY_NAME,
        String.class,
        NbBundle.getBundle(this).getString("PROP_DS_Name"),
        NbBundle.getBundle(this).getString("HINT_DS_Name")
      ) {

        public Object getValue() {
          return DataSystem.this.getDisplayName();
        }

        public void setValue(Object val) {
          if (! (val instanceof String)) return;
          renameRepository((String) val);
        }
      }
    );
    return s;
  }

  /** renames this node */
  void renameRepository(String name) {
    String old = getDisplayName();
    setDisplayName(name);
    if (old.equals(name)) return;
    firePropertyChange(DataSystem.this.PROP_DISPLAY_NAME, old, name);
  }

  /** writes this node to ObjectOutputStream and its display name
  */
  public Handle getHandle() {
    return new DSHandle(filter);
  }



  /** Creates data folder that will represent the file system.
  * @param fs the file system
  * @return the DataFolder that will represent it
  */
  DataFolder createRoot (FileSystem fs) {
    return DataFolder.findFolder (fs.getRoot());
  }

  /** Getter for set of actions that should be present in the
  * popup menu of this node. This set is used in construction of
  * menu returned from getContextMenu and specially when a menu for
  * more nodes is constructed.
  *
  * @return array of system actions that should be in popup menu
  */
  public com.netbeans.ide.util.actions.SystemAction[] createActions() {
    return new SystemAction[] {
      SystemAction.get (com.netbeans.developer.impl.actions.AddFSAction.class),
      SystemAction.get (com.netbeans.developer.impl.actions.AddJarAction.class),
      null,
      SystemAction.get (com.netbeans.developer.impl.actions.PropertiesAction.class)
    };
  }

  /** Adds a file system as a child.
  * @param fs file system to add
  * @param light only hidden (<CODE>true</CODE>) => do not attach listeners
  */
  void addFS(FileSystem fs, boolean light) {
    if (!light) {
      fs.addPropertyChangeListener (propL);
    }
    if (!fs.isHidden ()) {
      DataFolder df = createRoot(fs);
      roots.addElement (df);
      Node n = new RootFolderNode(df, df.createNodeChildren(filter));
      getDSMap().addFS(df, n);
      rootArray = null;
    }
  }

  /** Removes a file system from list of children.
  * @param fs file system
  * @param light only hidden (<CODE>true</CODE>) => do not detach listeners
  */
  void removeFS(FileSystem fs, boolean light) {
    DataFolder df = createRoot(fs);
    if (!light) {
      fs.removePropertyChangeListener(propL);
    }
    if (roots.removeElement(df)) {
      Node n = getDSMap().removeFS(df);
      rootArray = null;
    }
  }

  /** Reorders nodes.
  * @param ev
  * Acquires from ev.getPermutation() a permutation. However, this one contains
  * filesystems that are hidden. So the method creates new permutation e.g
  * 3,4,1,5 (from original 3,4,1,0,2,5 where 0,2 are hidden). This new permutation
  * is then changed to 1,2,0,3 and sent to ChildrenMap.reorder().
  */
  final void reorder(FileSystemPoolReorderedEvent ev) {
    if (getChildren().getNodesCount() < 2) return; // nothing to do
    final FileSystem[] fss = ev.getFileSystemPool().toArray();
    Heap heap = new Heap();

    int[] perm = ev.getPermutation();
    int[] nperm = new int[getDSMap().size()];  // there is is stored 3,4,1,5
    int npermptr = 0;

    for (int i = 0; i < fss.length; i++) {
      if (! fss[perm[i]].isHidden()) {
        heap.add(perm[i], npermptr);
        nperm[npermptr++] = i;    // store now
      }
    }

    int size = heap.size();

    for (int i = 0; i < size; i++) {
      nperm[heap.getMin()[1]] = i;   // change to 1,2,0,3
    }

    getDSMap().reorder(nperm);
  }

  /** @return a reference to DSMap */
  DSMap getDSMap() {
    return (DSMap) getChildren();
  }

  static class DSMap extends Index.MapChildren {
    DataSystem ref;
    void setDS(DataSystem ref) {
      this.ref = ref;
    }
    void addFS(Object key, Node fsn) {
      super.put(key, fsn);
    }
    Node removeFS(Object key) {
      Node n = (Node) nodes.get(key);
      super.remove(key);
      return n;
    }
    int size() {
      return nodes.size();
    }

    // must not be called from constructor of DataSystem
    protected java.util.Map initMap() {
      if (ref == null) throw new RuntimeException();
      Enumeration en = FileSystemPool.getDefault().getFileSystems();
      java.util.Map map = new java.util.HashMap();
      while (en.hasMoreElements ()) {
        // the root that should represent the file system
        FileSystem fs = (FileSystem)en.nextElement ();
        fs.addPropertyChangeListener(ref.propL);
        if (!fs.isHidden ()) {
          DataFolder df = ref.createRoot(fs);
          ref.roots.addElement(df);
          map.put(df, new RootFolderNode(df, df.createNodeChildren(ref.filter)));
        }
      }
      return map;
    }
  }

  /** Like heapsort heap. */
  static class Heap {
    /** array of integers */
    private int[][] array;
    /** size */
    private int size;

    public Heap() {
      array = new int[10][];
      size = 0;
    }

    /** @return a size of the heap */
    public int size() {
      return size;
    }

    /** Adds an integer to the heap */
    public void add(int key, int val) {
      if (size == array.length) {
        int[][] narray = new int[2 * size][];
        System.arraycopy(array, 0, narray, 0, size);
        array = narray;
      }
      int index = size, prev;
      int[] neu = new int[] {key, val};
      array[size++] = neu;
      while (index > 0) {
        prev = ((index + 1) / 2) - 1;  // prev item
        if (array[index][0] == array[prev][0]) throw new IllegalArgumentException();
        if (array[index][0] < array[prev][0]) {
          int tmp[] = array[index];
          array[index] = array[prev];
          array[prev] = tmp;
        } else {
          break;
        }
        index = prev;
      }
    }

    /** Removes min. */
    public int[] getMin() {
      if (size == 0) throw new java.util.NoSuchElementException();
      int[] ret = array[0];
      array[0] = array[--size];
      int r, l, index = 0;

      do {
        r = 2 * (index + 1);
        l = 2 * (index + 1) - 1;

        if (l >= size) break; // at the bottom
        if (r >= size) {
          int[] tmp = array[l];
          array[l] = array[index];
          array[index] = tmp;
          break;
        }
        int next = array[l][0] < array[r][0] ? l : r;
        if (array[index][0] > array[next][0]) {
          int tmp[] = array[next];
          array[next] = array[index];
          array[index] = tmp;
        }
        index = next;
      } while(true);
      return ret;
    }
  }

  /** Serialization. */
  static class DSHandle implements Handle {
    DataFilter filter;

    public DSHandle(DataFilter f) {
      filter = f;
    }

    public Node getNode() {
      return com.netbeans.ide.TopManager.getDefault().getPlaces().nodes().repository(filter);
    }
  }
}

/*
 * Log
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Fixed outerclass 
 *       specifiers uncompilable under JDK 1.2
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.31        --/--/98 Jaroslav Tulach The DataFolderRoot constructor now takes reference to this
 *  0    Tuborg    0.32        --/--/98 Jaroslav Tulach Serialization
 *  0    Tuborg    0.40        --/--/98 Jaroslav Tulach Nodes
 *  0    Tuborg    0.41        --/--/98 Petr Hamernik   isLeaf added
 *  0    Tuborg    0.43        --/--/98 Jaroslav Tulach changes due to new notification model of fs
 *  0    Tuborg    0.44        --/--/98 Jaroslav Tulach redesign
 *  0    Tuborg    0.45        --/--/98 Jaroslav Tulach now keeps list of all node delegates
 *  0    Tuborg    0.46        --/--/98 Jaroslav Tulach DataFilterCookie
 *  0    Tuborg    0.47        --/--/98 Ales Novak      constructor with parent node added
 *  0    Tuborg    0.48        --/--/98 Jan Formanek    icon modified
 *  0    Tuborg    0.49        --/--/98 Petr Hamernik   add directory action added
 */
