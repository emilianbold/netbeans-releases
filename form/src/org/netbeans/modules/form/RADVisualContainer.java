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
  private RADLayoutNode layoutNode;

  transient private Container containerDelegate;
  
  public void setComponent (Class beanClass) {
    super.setComponent (beanClass);
    Object value = getBeanInfo ().getBeanDescriptor ().getValue ("containerDelegate"); // NOI18N
    if ((value != null) && (value instanceof String) && ((String)value).equals ("getContentPane")) { // NOI18N
      try {
        java.lang.reflect.Method m = beanClass.getMethod ("getContentPane", new Class [0]); // NOI18N
        containerDelegate = (Container) m.invoke (getBeanInstance (), new Object [0]);
      } catch (Exception e) { // effectively ignored - simply no containerDelegate
      }
    }
  }
    
  /** @return The JavaBean visual container represented by this RADVisualComponent */
  public Container getContainer () {
    if (containerDelegate != null) {
      return containerDelegate;
    }
    return (Container)getBeanInstance ();
  }

  public void setLayoutNodeReference (RADLayoutNode node) {
    this.layoutNode = node;
  }

  public RADLayoutNode getLayoutNodeReference () {
    return layoutNode;
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
      throw new InternalError ("Cannot change a design layout on this container"); // NOI18N
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

  /** Called to obtain a Java code to be used to generate code to access the container for adding subcomponents.
  * It is expected that the returned code is either "" (in which case the form is the container) or is a name of variable
  * or method call ending with "." (e.g. "container.getContentPane ().").
  * @return the prefix code for generating code to add subcomponents to this container
  */
  public String getContainerGenName () {
    if (containerDelegate != null) {
      return getName () + ".getContentPane ()."; // NOI18N
    }
    return getName () + "."; // NOI18N
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
  
  public void add (RADComponent comp) {
    if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException ();
    subComponents.add (comp);
    ((RADVisualComponent)comp).initParent (this);
    if (getNodeReference () != null) { // it can be null in the case when copying containers with components
      ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
    }
  }

  public void remove (RADComponent comp) {
    if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException ();
    designLayout.removeComponent (((RADVisualComponent)comp));
    int index = subComponents.indexOf (comp);
    if (index != -1) {
      subComponents.remove (index);
    }
    ((RADChildren)getNodeReference ().getChildren ()).updateKeys ();
  }

  public int getIndexOf (RADComponent comp) {
    if (!(comp instanceof RADVisualComponent)) throw new IllegalArgumentException ();
    return subComponents.indexOf (comp);
  }

// -----------------------------------------------------------------------------
// Debug methods

  public String toString () {
    String ret = super.toString () + ", layout: ---------------\n"; // NOI18N
    ret = ret + "current: "+ designLayout +"\n"; // NOI18N
    ret = ret + "previous: "+ previousLayout + "\n"; // NOI18N
    return ret + "---------------------------"; // NOI18N
  }
  
}

/*
 * Log
 *  25   Gandalf   1.24        1/5/00   Ian Formanek    NOI18N
 *  24   Gandalf   1.23        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  23   Gandalf   1.22        10/6/99  Ian Formanek    Better handling of 
 *       special state during copy/paste of components
 *  22   Gandalf   1.21        8/15/99  Ian Formanek    Fixed bug 3289 - Wrong 
 *       Code Generation in JInternalFrame
 *  21   Gandalf   1.20        8/6/99   Ian Formanek    setComponent is public
 *  20   Gandalf   1.19        7/5/99   Ian Formanek    implemented additions to
 *       ComponentsContainer
 *  19   Gandalf   1.18        7/5/99   Ian Formanek    Fixed last change
 *  18   Gandalf   1.17        7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  17   Gandalf   1.16        7/5/99   Ian Formanek    Added access to layout 
 *       node
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
