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
import java.util.*;

import com.netbeans.ide.loaders.DataFolder;
import com.netbeans.ide.loaders.DataFilter;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.actions.SystemAction;

/** Data system encapsulates logical structure of more file systems.
* It also allows filtering of content of DataFolders
*
* @author Jaroslav Tulach, Petr Hamernik
*/
final class DataSystem extends AbstractNode implements RepositoryListener {
  /** generated Serialized Version UID */
  static final long serialVersionUID = -7272169513973465669L;

  /** the file system pool to work with */
  private transient Repository fileSystemPool;

  /** filter for the data system */
  DataFilter filter;

  /** Constructor.
  * @param fsp file system pool
  * @param filter the filter for filtering files
  */
  private DataSystem(Children ch, Repository fsp, DataFilter filter) {
    super (ch);
    fileSystemPool = fsp;
    this.filter = filter;
    initialize();
    setIconBase ("/com/netbeans/developer/impl/resources/repository");
    setName (NbBundle.getBundle (this).getString ("dataSystemName"));
    setShortDescription (NbBundle.getBundle (DataSystem.this).getString ("CTL_Repository_Hint"));
  }

  /** Constructor. Uses default file system pool.
  * @param filter the filter to use
  */
  private DataSystem(Children ch, DataFilter filter) {
    this (ch, NbTopManager.getDefaultRepository (), filter);
  }


  /** Factory for DataSystem instances */
  public static DataSystem getDataSystem(DataFilter filter) {
    if (filter == null) filter = DataFilter.ALL;
    DataSystem ds = new DataSystem(new DSMap (), filter);
    return ds;
  }

  /** Gets a DataSystem */
  public static DataSystem getDataSystem() {
    return getDataSystem(null);
  }

  void initialize () {
    fileSystemPool.addRepositoryListener (new WeakListener.Repository (this));
    Enumeration en = fileSystemPool.getFileSystems ();
    while (en.hasMoreElements ()) {
      FileSystem fs = (FileSystem)en.nextElement ();
      fs.addPropertyChangeListener (new WeakListener.PropertyChange ((DSMap)getChildren ()));
    }
    refresh ();
  }

  /** Initializes properties by adding them to the sheet.
  * Called asynchronously from the constructor.
  */
  protected synchronized Sheet createSheet() {
    Sheet s = Sheet.createDefault ();
    Sheet.Set ss = s.get (Sheet.PROPERTIES);
    ss.put (
      new PropertySupport.ReadWrite (
        DataSystem.this.PROP_NAME,
        String.class,
        NbBundle.getBundle(this).getString("PROP_DS_Name"),
        NbBundle.getBundle(this).getString("HINT_DS_Name")
      ) {

        public Object getValue() {
          return DataSystem.this.getName();
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
    String old = getName();
    setName(name);
    if (old.equals(name)) return;
    firePropertyChange(DataSystem.this.PROP_NAME, old, name);
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
  static DataFolder createRoot (FileSystem fs) {
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
      SystemAction.get (com.netbeans.ide.actions.PropertiesAction.class)
    };
  }

  /** Called when new file system is added to the pool.
  * @param ev event describing the action
  */
  public void fileSystemAdded (RepositoryEvent ev) {
    ev.getFileSystem ().addPropertyChangeListener (new WeakListener.PropertyChange ((DSMap)getChildren ()));
    refresh ();
  }

  /** Called when a file system is deleted from the pool.
  * @param ev event describing the action
  */
  public void fileSystemRemoved (RepositoryEvent ev) {
    refresh ();
  }
  /** Called when the fsp is reordered */
  public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
    refresh ();
  }
  
  /** Refreshes the pool.
  */
  void refresh () {
    refresh (null);
  }
  
  /** Refreshes the pool.
  * @param fs file system to remove
  */
  void refresh (FileSystem fs) {
    ((DSMap)getChildren ()).refresh (fileSystemPool, fs);
  }

  /** Children that listens to changes in filesystem pool.
  */
  static class DSMap extends Children.Keys implements PropertyChangeListener {

    public void propertyChange (PropertyChangeEvent ev) {
      System.out.println ("Property change");
      if (ev.getPropertyName ().equals ("hidden")) {
        // change in the hidden state of a file system
        getDS ().refresh ();
      } else if (ev.getPropertyName().equals("root")) {
        FileSystem fs = (FileSystem)ev.getSource ();
        getDS ().refresh (fs);
        getDS ().refresh ();
      }
    }
    
    /** The node */
    private DataSystem getDS() {
      return (DataSystem)getNode ();
    }

    protected Node[] createNodes (Object key) {
      DataFolder df = createRoot ((FileSystem)key);
      return new Node[] { new RootFolderNode (df, df.createNodeChildren (getDS ().filter)) };
    }

    /** Refreshes the pool.
    * @param fileSystemPool the pool
    * @param fs file system to remove
    */
    public void refresh (Repository fileSystemPool, FileSystem fs) {
      Enumeration en = fileSystemPool.getFileSystems ();
      ArrayList list = new ArrayList ();
      while (en.hasMoreElements ()) {
        Object o = en.nextElement ();
        if (fs != o && !((FileSystem)o).isHidden ()) {
          list.add (o);
        }
      }
      setKeys (list);
    }
    
  }

  /** Serialization. */
  private static class DSHandle implements Handle {
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
 *  9    Gandalf   1.8         3/21/99  Jaroslav Tulach Repository displayed ok.
 *  8    Gandalf   1.7         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  7    Gandalf   1.6         3/17/99  Ian Formanek    Short Description, 
 *       cleaned up displayName -> Name
 *  6    Gandalf   1.5         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  5    Gandalf   1.4         1/7/99   Jan Jancura     
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
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
