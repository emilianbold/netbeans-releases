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

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataObject;
import org.openide.filesystems.*;
import org.openide.util.datatransfer.*;
import org.openide.util.*;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;

/** Data system encapsulates logical structure of more file systems.
* It also allows filtering of content of DataFolders
*
* @author Jaroslav Tulach, Petr Hamernik
*/
final class DataSystem extends AbstractNode implements RepositoryListener {
  /** default instance */
  private static DataSystem def;

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
    setName (NbBundle.getBundle (DataSystem.class).getString ("dataSystemName"));
    setShortDescription (NbBundle.getBundle (DataSystem.class).getString ("CTL_Repository_Hint"));
  }

  /** Constructor. Uses default file system pool.
  * @param filter the filter to use
  */
  private DataSystem(Children ch, DataFilter filter) {
    this (ch, NbTopManager.getDefaultRepository (), filter);
  }

  public HelpCtx getHelpCtx () {
    return new HelpCtx (DataSystem.class);
  }

  /** Factory for DataSystem instances */
  public static DataSystem getDataSystem(DataFilter filter) {
    if (filter == null) {
      if (def != null) {
        return def;
      }
      return def = new DataSystem(new DSMap (), DataFilter.ALL);
    } else {
      DataSystem ds = new DataSystem(new DSMap (), filter);
      return ds;
    }
  }

  /** Gets a DataSystem */
  public static DataSystem getDataSystem() {
    return getDataSystem(null);
  }

  void initialize () {
    fileSystemPool.addRepositoryListener (WeakListener.repository (this, fileSystemPool));
    Enumeration en = fileSystemPool.getFileSystems ();
    while (en.hasMoreElements ()) {
      FileSystem fs = (FileSystem)en.nextElement ();
      fs.addPropertyChangeListener (WeakListener.propertyChange ((DSMap)getChildren (), fs));
    }
    refresh ();
  }

  /** writes this node to ObjectOutputStream and its display name
  */
  public Handle getHandle() {
    return filter == DataFilter.ALL ? new DSHandle (null) : new DSHandle(filter);
  }


  /** Creates data folder that will represent the file system.
  * @param fs the file system
  * @return the DataFolder that will represent it
  */
  static DataFolder createRoot (FileSystem fs) {
    return DataFolder.findFolder (fs.getRoot());
  }

  /** @return available new types */
  public NewType[] getNewTypes () {
    return ModuleFSSection.listOfNewTypes(false);
  }

  /** Getter for set of actions that should be present in the
  * popup menu of this node. This set is used in construction of
  * menu returned from getContextMenu and specially when a menu for
  * more nodes is constructed.
  *
  * @return array of system actions that should be in popup menu
  */
  public org.openide.util.actions.SystemAction[] createActions() {
    return new SystemAction[] {
      SystemAction.get (com.netbeans.developer.impl.actions.AddFSAction.class),
      SystemAction.get (com.netbeans.developer.impl.actions.AddJarAction.class),
      null,
      SystemAction.get (org.openide.actions.NewAction.class),
      null,
      SystemAction.get (org.openide.actions.PropertiesAction.class),
      SystemAction.get (org.openide.actions.ToolsAction.class),
    };
  }

  /** Called when new file system is added to the pool.
  * @param ev event describing the action
  */
  public void fileSystemAdded (RepositoryEvent ev) {
    ev.getFileSystem ().addPropertyChangeListener (
      WeakListener.propertyChange ((DSMap)getChildren (), ev.getFileSystem ())
    );
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
      //System.out.println ("Property change");
      DataSystem ds = getDS ();
      if (ds == null) return;
      
      if (ev.getPropertyName ().equals ("hidden")) {
        // change in the hidden state of a file system
        ds.refresh ();
      } else if (ev.getPropertyName().equals("root")) {
        FileSystem fs = (FileSystem)ev.getSource ();
        ds.refresh (fs);
        ds.refresh ();
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

static final long serialVersionUID =-2266375092419944364L;
    public DSHandle(DataFilter f) {
      filter = f;
    }

    public Node getNode() {
      return getDataSystem (filter);
    }
  }
}

/*
 * Log
 *  23   Gandalf   1.22        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  22   Gandalf   1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   Gandalf   1.20        9/6/99   Jaroslav Tulach #3576
 *  20   Gandalf   1.19        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  19   Gandalf   1.18        8/3/99   Jaroslav Tulach Serialization of 
 *       NbMainExplorer improved again.
 *  18   Gandalf   1.17        7/8/99   Jesse Glick     Context help.
 *  17   Gandalf   1.16        6/9/99   Ian Formanek    ToolsAction
 *  16   Gandalf   1.15        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  15   Gandalf   1.14        5/9/99   Ian Formanek    Fixed bug 1655 - 
 *       Renaming of top level nodes is not persistent (removed the possibility 
 *       to rename).
 *  14   Gandalf   1.13        5/4/99   Jaroslav Tulach No new directory & jar 
 *       in Repository node.
 *  13   Gandalf   1.12        4/9/99   Ian Formanek    Removed debug printlns
 *  12   Gandalf   1.11        3/26/99  Jaroslav Tulach 
 *  11   Gandalf   1.10        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  10   Gandalf   1.9         3/22/99  Jaroslav Tulach Added new section.
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
