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

import com.netbeans.developerx.loaders.form.formeditor.layouts.*;

import java.awt.Container;

/** 
*
* @author Ian Formanek
*/
public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
  private RADVisualComponent[] subComponents;
  private DesignLayout designLayout;

  /** @return The JavaBean visual container represented by this RADVisualComponent */
  public Container getContainer () {
    return (Container)getComponentInstance ();
  }

  public RADComponent[] getSubBeans () {
    return subComponents;
  }
  
  public RADVisualComponent[] getSubComponents () {
    return subComponents;
  }

  public void initSubComponents (RADComponent[] initComponents) {
    subComponents = new RADVisualComponent[initComponents.length];
    System.arraycopy (initComponents, 0, subComponents, 0, initComponents.length);
  }

  public DesignLayout getDesignLayout () {
    return designLayout;
  }
  
  public void setDesignLayout (DesignLayout layout) {
    Thread.dumpStack();
    if (designLayout != null) {
      designLayout.setRADContainer (null);
    }
    designLayout = layout;
    designLayout.setRADContainer (this);
  }

  public String getContainerGenName () {
    return getName () + ".";
  }
}

/*
 * Log
 *  5    Gandalf   1.4         5/11/99  Ian Formanek    Build 318 version
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
