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

import com.netbeans.ide.nodes.*;
import com.netbeans.developerx.loaders.form.formeditor.layouts.DesignLayout;

import java.awt.Component;
import java.util.HashMap;

/** 
*
* @author Ian Formanek
*/
public class RADVisualComponent extends RADComponent {

  private HashMap constraints = new HashMap (10);
  transient private Node.PropertySet[] visualPropertySet;
  transient private RADVisualContainer parent;

  void initParent (RADVisualContainer parent) {
    this.parent = parent;
  }
  
  /** @return The JavaBean visual component represented by this RADVisualComponent */
  public Component getComponent () {
    return (Component)getComponentInstance ();
  }

  /** @return The index of this component within all the subcomponents of its parent */
  public int getComponentIndex () {
    return -1; // [PENDING] ((OrderCookie) Cookies.getInstanceOf (getParentNode ().getCookie (), OrderCookie.class)).getIndexOf (this);
  }
  
  void initConstraints (HashMap map) {
    for (java.util.Iterator it = map.keySet ().iterator (); it.hasNext ();) {
      String layoutClassName = (String) it.next ();
      constraints.put (layoutClassName, map.get (layoutClassName));
    }
  }
  
  public void setConstraints (Class layoutClass, DesignLayout.ConstraintsDescription constr) {
    constraints.put (layoutClass.getName(), constr);
  }

  public DesignLayout.ConstraintsDescription getConstraints (Class layoutClass) {
    return (DesignLayout.ConstraintsDescription)constraints.get (layoutClass.getName ());
  }

  public Node.PropertySet[] getProperties () {
    if (parent == null) {
      // [PENDING] strange - not initialized yet - it is probably a bad state and this code should be removed
      return super.getProperties ();
    }
    
    if (visualPropertySet == null) {
      Node.PropertySet[] inh = super.getProperties ();
      visualPropertySet = new Node.PropertySet[inh.length+1];
      System.arraycopy (inh, 0, visualPropertySet, 0, inh.length-1);
      visualPropertySet[visualPropertySet.length-2] = 
        new Node.PropertySet ("layout", "Layout", "Layout Properties") {
          public Node.Property[] getProperties () {
            return parent.getDesignLayout ().getComponentProperties (RADVisualComponent.this);
          }
        };
      visualPropertySet[visualPropertySet.length-1] = inh[inh.length-1]; // add events tab to the end
    }
    return visualPropertySet;
  }
  

}

/*
 * Log
 *  5    Gandalf   1.4         5/12/99  Ian Formanek    
 *  4    Gandalf   1.3         5/11/99  Ian Formanek    Build 318 version
 *  3    Gandalf   1.2         5/10/99  Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
