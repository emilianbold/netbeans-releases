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

package com.netbeans.developer.modules.loaders.form;

import java.util.ArrayList;

/** 
*
* @author Ian Formanek
*/
public class RADContainer extends RADComponent implements ComponentContainer {
  private ArrayList subComponents;

  public RADComponent[] getSubBeans () {
    RADComponent[] components = new RADComponent [subComponents.size ()];
    subComponents.toArray (components);
    return components;
  }

  public void initSubComponents (RADComponent[] initComponents) {
    subComponents = new ArrayList (initComponents.length);
    for (int i = 0; i < initComponents.length; i++) {
      subComponents.add (initComponents[i]);
    }
  }

  public void reorderSubComponents (int[] perm) {
    for (int i = 0; i < perm.length; i++) {
      int from = i;
      int to = perm[i];
      if (from == to) continue;
      Object value = subComponents.remove (from);
      if (from < to) {
        subComponents.add (to - 1, value);
      } else {
        subComponents.add (to, value);
      }
    }
    getFormManager ().fireComponentsReordered (this);
  }

  public void add (RADComponent comp) {
    subComponents.add (comp);
    ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
  }

  public void remove (RADComponent comp) {
    int index = subComponents.indexOf (comp);
    if (index != -1) {
      subComponents.remove (index);
    }
    ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
  }

  public int getIndexOf (RADComponent comp) {
    return subComponents.indexOf (comp);
  }


  public String getContainerGenName () {
    return "";
  }
}

/*
 * Log
 *  8    Gandalf   1.7         7/5/99   Ian Formanek    implemented additions to
 *       ComponentsContainer
 *  7    Gandalf   1.6         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/11/99  Ian Formanek    Build 318 version
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
