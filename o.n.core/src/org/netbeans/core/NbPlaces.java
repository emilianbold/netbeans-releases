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
import com.netbeans.ide.util.NotImplementedException;
import com.netbeans.ide.nodes.*;
import com.netbeans.developer.impl.desktop.DesktopPoolContext;

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

  /** Control panel
  */
  public Node controlPanel () {
    return ControlPanelNode.getDefault ();
  }

  /** Node with all desktops */
  public Node desktops () {
    return DesktopPoolContext.getDefault ();
  }

  /** Repository settings */
  public Node repositorySettings () {
    return FSPoolNode.getFSPoolNode ();
  }

  /** Default folder for templates.
  */
  public DataFolder templates () {
    throw new NotImplementedException ();
  }

}

/*
* Log
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
