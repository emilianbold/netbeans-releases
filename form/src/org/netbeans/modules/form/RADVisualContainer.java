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

import com.netbeans.developerx.loaders.form.formeditor.layouts.DesignLayout;
import com.netbeans.developerx.loaders.form.formeditor.layouts.support.DesignSupportLayout;

import java.awt.Container;

/** 
*
* @author Ian Formanek
*/
public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
  private RADVisualComponent[] subComponents;
  private DesignLayout designLayout;
  private DesignLayout previousDesignLayout;

  /** @return The JavaBean visual container represented by this RADVisualComponent */
  public Container getContainer () {
    return (Container)getComponentInstance ();
  }

// -----------------------------------------------------------------------------
// Layout Manager management

  public DesignLayout getPreviousDesignLayout () {
    return previousDesignLayout;
  }
  
  public DesignLayout getDesignLayout () {
    return designLayout;
  }
  
  public void setDesignLayout (DesignLayout layout) {
    if (designLayout instanceof DesignSupportLayout) {
      throw new InternalError ("Cannot change a design layout on this container");
    }
    if (designLayout != null) {
      if (layout.getClass().equals (designLayout.getClass())) return;
      designLayout.setRADContainer (null);
    }
    if (layout == null) return;

    previousDesignLayout = designLayout;
    designLayout = layout;
    designLayout.setRADContainer (this);

    RADVisualComponent[] children = getSubComponents ();
    for (int i = 0; i < children.length; i++) {
      designLayout.addComponent (children[i]);
    } 
    
    getContainer ().validate();
    getContainer ().repaint();

  }

  public String getContainerGenName () {
    return getName () + ".";
  }

// -----------------------------------------------------------------------------
// Subcomponents management

  public RADComponent[] getSubBeans () {
    return subComponents;
  }
  
  public RADVisualComponent[] getSubComponents () {
    return subComponents;
  }

  public void initSubComponents (RADComponent[] initComponents) {
    subComponents = new RADVisualComponent[initComponents.length];
    System.arraycopy (initComponents, 0, subComponents, 0, initComponents.length);
    for (int i = 0; i < subComponents.length; i++) {
      subComponents[i].initParent (this);
    }
  }

  public void add (RADVisualComponent comp) {
    RADVisualComponent[] newComponents = new RADVisualComponent [subComponents.length + 1];
    System.arraycopy (subComponents, 0, newComponents, 0, subComponents.length);
    newComponents[newComponents.length - 1] = comp;
    comp.initParent (this);
    subComponents = newComponents;
    getNodeReference ().getChildren ().add (new com.netbeans.ide.nodes.Node[] { new RADComponentNode (comp) });
  }

}

/*
 * Log
 *  8    Gandalf   1.7         5/14/99  Ian Formanek    
 *  7    Gandalf   1.6         5/12/99  Ian Formanek    Removed debug print
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/11/99  Ian Formanek    Build 318 version
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
