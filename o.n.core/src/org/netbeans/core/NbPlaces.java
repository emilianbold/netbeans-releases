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

import com.netbeans.ide.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.util.NotImplementedException;
import com.netbeans.ide.nodes.*;
import com.netbeans.developer.impl.workspace.WorkspacePoolContext;

/** Important places in the system.
*
* @author Jaroslav Tulach
*/
final class NbPlaces extends Object implements Places, Places.Nodes, Places.Folders {
  /** default */
  private static NbPlaces places;

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

  /** Default folder for bookmarks.
  */
  public DataFolder bookmarks () {
    return findSessionFolder ("Bookmarks");
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
      FileObject fo = Repository.getDefault().findResource(name);

      if (fo == null) {
        // resource not found, try to create new folder
        fo = Repository.getDefault ().getDefaultFileSystem ().getRoot ().createFolder (name);
      }

      DataFolder df = DataFolder.findFolder(fo);
      df.setSortMode(DataFolder.SortMode.FOLDER_NAMES);
      return df;
    } catch (java.io.IOException ex) {
      throw new InternalError ("Folder not found and cannot be created: " + name);
    }
  }

}

/*
* Log
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
