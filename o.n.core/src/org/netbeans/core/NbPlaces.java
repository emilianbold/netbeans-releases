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

import java.util.ArrayList;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.NotImplementedException;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import com.netbeans.developer.impl.workspace.WorkspacePoolContext;
import com.netbeans.developer.impl.execution.ExecutionEngine;

/** Important places in the system.
*
* @author Jaroslav Tulach
*/
final class NbPlaces extends Object implements Places, Places.Nodes, Places.Folders {
  /** session settings icon base */
  private static final String SESSION_SETTINGS_ICON_BASE="/com/netbeans/developer/impl/resources/sessionSettings";
  
  /** default */
  private static NbPlaces places;
  /** set of roots */
  private static ArrayList roots = new ArrayList ();
  /** session node */
  private static AbstractNode session;

  static {
    session = new AbstractNode (new Children.Array ());
    session.setName (NbBundle.getBundle (NbPlaces.class).getString ("CTL_Session_Settings"));
    session.setIconBase (SESSION_SETTINGS_ICON_BASE);
  }
  
  /** No instance outside this class.
  */
  private NbPlaces() {
  }

  /** @return the default implementation of places */
  public static NbPlaces getDefault () {
    if (places == null) {
      places = new NbPlaces ();
    }
    return places;
  }

  /** Adds new root node.
  */
  public static void addRoot (Node n) {
    roots.add (n);
    NbTopManager.change.firePropertyChange (NbTopManager.PROP_PLACES, null, null);
  }
  
  /** Removes new root node.
  */
  public static void removeRoot (Node n) {
    if (roots.remove (n)) {
      NbTopManager.change.firePropertyChange (NbTopManager.PROP_PLACES, null, null);
    }
  }
  
  /** Adds new session node.
  */
  public static void addSession (Node n) {
    session.getChildren ().add (new Node[] { n });
  }
  
  /** Removes new session node.
  */
  public static void removeSession (Node n) {
    session.getChildren ().remove (new Node[] { n });
  }

  /** Interesting places for nodes.
  * @return object that holds "node places"
  */
  public Places.Nodes nodes () {
    return this;
  }

  /** Interesting places for data objects.
  * @return interface that provides access to data objects' places
  */
  public Places.Folders folders () {
    return this;
  }

  /** Repository node.
  */
  public Node repository () {
    return DataSystem.getDataSystem ();
  }

  /** Repository node with given DataFilter. */
  public Node repository(DataFilter f) {
    return DataSystem.getDataSystem (f);
  }

  /** Get a root of packages with a given data filter. 
  * @param f the requested filter
  * @return the node
  */ 
  public Node packages (DataFilter f) {
    return new PackageNode (repository (), f);
  }

  /** Node with all installed loaders.
  */
  public Node loaderPool () {
    return LoaderPoolNode.getLoaderPoolNode ();
  }

  /** Environment node. Place for all transient information about
  * the IDE.
  */
  public Node environment () {
    return EnvironmentNode.getDefault ();
  }

  /** Get the Executors node.
  * @return the node
  */
  public Node executors () {
    return ExecutionEngine.getNode ();
  }

  /** Session node */
  public Node session () {
    return NbPlaces.session;
  }

  /** Control panel
  */
  public Node controlPanel () {
    return ControlPanelNode.getDefault ();
  }

  /** Node with all workspaces */
  public Node workspaces () {
    return WorkspacePoolContext.getDefault ();
  }

  /** Repository settings */
  public Node repositorySettings () {
    return FSPoolNode.getFSPoolNode ();
  }

  /** Workspace node for current project. This node can change when project changes.
  */
  public Node projectDesktop () {
    return NbProjectOperation.getProject ().projectDesktop ();
  }

  /** Root nodes.
  */
  public Node[] roots () {
    return (Node[])roots.toArray (new Node[0]);
  }

  /** Default folder for templates.
  */
  public DataFolder templates () {
    return findSessionFolder ("Templates");
  }

  /** Default folder for toolbars.
  */
  public DataFolder toolbars () {
    return findSessionFolder ("Toolbars");
  }

  /** Default folder for menus.
  */
  public DataFolder menus () {
    return findSessionFolder ("Menu");
  }

  /** Default folder for actions pool.
  */
  public DataFolder actions () {
    return findSessionFolder ("Actions");
  }

  /** Default folder for bookmarks.
  */
  public DataFolder bookmarks () {
    return findSessionFolder ("Bookmarks");
  }

  /** Default folder for projects.
  */
  public DataFolder projects () {
    return findSessionFolder ("Projects");
  }

  /** Startup folder.
  */
  public DataFolder startup () {
    return findSessionFolder ("Startup");
  }

  /**
   * Returns a DataFolder subfolder of the session folder.  In the DataFolder
   * folders go first (sorted by name) followed by the rest of objects sorted
   * by name.
   */
  private DataFolder findSessionFolder (String name) {
    try {
      FileObject fo = NbTopManager.getDefaultRepository().findResource(name);

      if (fo == null) {
        // resource not found, try to create new folder
        fo = NbTopManager.getDefaultRepository ().getDefaultFileSystem ().getRoot ().createFolder (name);
      }

      DataFolder df = DataFolder.findFolder(fo);
      return df;
    } catch (java.io.IOException ex) {
      throw new InternalError ("Folder not found and cannot be created: " + name);
    }
  }

}

/*
* Log
*  21   Gandalf   1.20        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  20   Gandalf   1.19        6/8/99   Ian Formanek    Added method actions() to
*       obtain folder for ActionsPool
*  19   Gandalf   1.18        5/27/99  Jaroslav Tulach Executors rearanged.
*  18   Gandalf   1.17        5/7/99   Jan Jancura     Places.Nodes.packages () 
*       method added
*  17   Gandalf   1.16        3/29/99  Jaroslav Tulach places ().nodes 
*       ().session ()
*  16   Gandalf   1.15        3/25/99  Jaroslav Tulach Faster startup.
*  15   Gandalf   1.14        3/19/99  Jaroslav Tulach TopManager.getDefault 
*       ().getRegistry ()
*  14   Gandalf   1.13        3/13/99  Jaroslav Tulach Places.roots ()
*  13   Gandalf   1.12        3/11/99  Ian Formanek    
*  12   Gandalf   1.11        3/11/99  Ian Formanek    Bookmarks & Startup added
*       to Session Settings
*  11   Gandalf   1.10        2/26/99  David Simonek   
*  10   Gandalf   1.9         2/19/99  Jaroslav Tulach added startup directory
*  9    Gandalf   1.8         2/12/99  Ian Formanek    Reflected renaming 
*       Desktop -> Workspace
*  8    Gandalf   1.7         2/11/99  Ian Formanek    Renamed FileSystemPool ->
*       Repository
*  7    Gandalf   1.6         2/2/99   Jaroslav Tulach Tries to create non 
*       existing folders
*  6    Gandalf   1.5         1/25/99  Jaroslav Tulach Added default project, 
*       its desktop and changed default explorer in Main.
*  5    Gandalf   1.4         1/25/99  David Peroutka  support for menus and 
*       toolbars
*  4    Gandalf   1.3         1/20/99  Jaroslav Tulach 
*  3    Gandalf   1.2         1/20/99  David Peroutka  
*  2    Gandalf   1.1         1/6/99   Jan Jancura     
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
