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

import org.openide.nodes.Node;
import com.netbeans.developerx.loaders.form.formeditor.layouts.DesignLayout;
import com.netbeans.developerx.loaders.form.formeditor.layouts.support.DesignSupportLayout;

import java.awt.Container;
import java.util.ArrayList;

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
  private ArrayList subComponents;
  private DesignLayout designLayout;
  private DesignLayout previousLayout;

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
    return previousLayout;
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

    previousLayout = designLayout;
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
// SubComponents Management

  public RADComponent[] getSubBeans () {
    RADVisualComponent[] components = new RADVisualComponent [subComponents.size ()];
    subComponents.toArray (components);
    return components;
  }
  
  public RADVisualComponent[] getSubComponents () {
    RADVisualComponent[] components = new RADVisualComponent [subComponents.size ()];
    subComponents.toArray (components);
    return components;
  }

  public void initSubComponents (RADComponent[] initComponents) {
    subComponents = new ArrayList (initComponents.length);
    for (int i = 0; i < initComponents.length; i++) {
      subComponents.add (initComponents[i]);
      ((RADVisualComponent)initComponents[i]).initParent (this);
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
    getDesignLayout ().updateLayout ();
    getFormManager ().fireComponentsReordered (this);
   }
  
  public void add (RADVisualComponent comp) {
    subComponents.add (comp);
    comp.initParent (this);
    ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
  }

  public void remove (RADVisualComponent comp) {
    designLayout.removeComponent (comp);
    int index = subComponents.indexOf (comp);
    if (index != -1) {
      subComponents.remove (index);
    }
    ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
  }

  public int getIndexOf (RADVisualComponent comp) {
    return subComponents.indexOf (comp);
  }

// -----------------------------------------------------------------------------
// Debug methods

  public String toString () {
    String ret = super.toString () + ", layout: ---------------\n";
    ret = ret + "current: "+ designLayout +"\n";
    ret = ret + "previous: "+ previousLayout + "\n";
    return ret + "---------------------------";
  }
  
}

/*
 * Log
 *  16   Gandalf   1.15        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  15   Gandalf   1.14        6/3/99   Ian Formanek    Fixed removing 
 *       components
 *  14   Gandalf   1.13        6/2/99   Ian Formanek    ToolsAction, Reorder
 *  13   Gandalf   1.12        5/26/99  Ian Formanek    toString
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
