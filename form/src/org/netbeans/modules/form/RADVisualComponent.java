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

import com.netbeans.developer.modules.loaders.form.layouts.DesignLayout;

import java.awt.Component;
import java.util.HashMap;

/** 
*
* @author Ian Formanek
*/
public class RADVisualComponent extends RADComponent {

  private HashMap constraints = new HashMap (10);

  /** @return The JavaBean visual component represented by this RADVisualComponent */
  public Component getComponent () {
    return (Component)getComponentInstance ();
  }

  /** @return The index of this component within all the subcomponents of its parent */
  public int getComponentIndex () {
    return -1; // [PENDING] ((OrderCookie) Cookies.getInstanceOf (getParentNode ().getCookie (), OrderCookie.class)).getIndexOf (this);
  }
  
  public void setConstraints (Class layoutClass, DesignLayout.ConstraintsDescription constr) {
    constraints.put (layoutClass.getName(), constr);
  }

  public DesignLayout.ConstraintsDescription getConstraints (Class layoutClass) {
    return (DesignLayout.ConstraintsDescription)constraints.get (layoutClass.getName ());
  }


}

/*
 * Log
 *  3    Gandalf   1.2         5/10/99  Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
