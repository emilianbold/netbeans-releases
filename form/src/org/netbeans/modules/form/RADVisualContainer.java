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
* Initialization order: <UL>
* <LI> Constructor: new RADVisualContainer ();
* <LI> FormManager2 init: initialize (FormManager2)
* <LI> Bean init: setComponent (Class)
* <LI> SubComponents init: initSubComponents (RADComponent[])
* <LI> DesignLayout init: setDesignLayout (DesignLayout) </UL>
 
* @author Ian Formanek
*/
public class RADVisualContainer extends RADVisualComponent implements ComponentContainer {
  private RADVisualComponent[] subComponents;
  private DesignLayout designLayout;
  private DesignLayout previousDesignLayout;

  transient private Container containerDelegate;
  
  void setComponent (Class beanClass) {
    super.setComponent (beanClass);
    Object value = getBeanInfo ().getBeanDescriptor ().getValue ("containerDelegate");
    if ((value != null) && (value instanceof String) && ((String)value).equals ("getContentPane")) {
      try {
        java.lang.reflect.Method m = beanClass.getMethod ("getContentPane", new Class [0]);
        containerDelegate = (Container) m.invoke (getComponentInstance (), new Object [0]);
      } catch (Exception e) { // effectively ignored - simply no containerDelegate
      }
    }
  }
    
  /** @return The JavaBean visual container represented by this RADVisualComponent */
  public Container getContainer () {
    if (containerDelegate != null) {
      return containerDelegate;
    }
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
  
  /** Must be called after initSubComponents!!! */
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
    if (containerDelegate != null) {
      return getName () + ".getContentPane ()";
    }
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

  public void remove (RADVisualComponent comp) {
    designLayout.removeComponent (comp);
    int index = -1;
    for (int i = 0; i < subComponents.length; i++) {
      if (subComponents[i] == comp) {
        index = i;
        break;
      }
    }
    if (index != -1) {
      RADVisualComponent[] newComponents = new RADVisualComponent[subComponents.length-1];
      System.arraycopy (subComponents, 0, newComponents, 0, index);
      if (index != subComponents.length - 1) {
        System.arraycopy (subComponents, index+1, newComponents, index, subComponents.length-index-1);
      }
      subComponents = newComponents;
    }
  }

  public int getIndexOf (RADVisualComponent comp) {
    for (int i = 0; i < subComponents.length; i++) {
      if (subComponents[i] == comp)
        return i;
    }
    return -1;
  }
}

/*
 * Log
 *  12   Gandalf   1.11        5/17/99  Ian Formanek    Fixed bug 1850 - An 
 *       exception is thrown when opening form, which contains JInternalFrame 
 *       component. 
 *  11   Gandalf   1.10        5/16/99  Ian Formanek    
 *  10   Gandalf   1.9         5/15/99  Ian Formanek    
 *  9    Gandalf   1.8         5/15/99  Ian Formanek    
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
