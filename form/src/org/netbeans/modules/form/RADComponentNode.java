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

import com.netbeans.ide.actions.*;
import com.netbeans.ide.cookies.SaveCookie;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.actions.SystemAction;

import java.awt.Image;
import java.text.MessageFormat;

/** 
*
* @author Ian Formanek
*/
public class RADComponentNode extends AbstractNode implements FormNodeCookie {

  private final static MessageFormat nameFormat = new MessageFormat (NbBundle.getBundle (RADComponentNode.class).getString ("FMT_ComponentName"));
  
  private RADComponent component;
  
  // FINALIZE DEBUG METHOD
  public void finalize () throws Throwable {
    super.finalize ();
    if (System.getProperty ("netbeans.debug.form.finalize") != null) {
      System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this);
    }
  } // FINALIZE DEBUG METHOD
  
  public RADComponentNode (RADComponent component) {
    super ((component instanceof ComponentContainer) ? new RADChildren ((ComponentContainer)component) : Children.LEAF);
    this.component = component;
    component.setNodeReference (this);
    getCookieSet ().add (this);
    updateName ();
  }

  void updateName () {
    String className = component.getComponentClass ().getName ();
    if (component instanceof FormContainer) {
      // [PENDING - handle this better]
      setName (component.getName () + " [form]");
    } else {
      setName (nameFormat.format (new Object[] {component.getName (), className, className.substring (className.lastIndexOf (".") + 1) } ));
    }
  }
  
  public Image getIcon (int iconType) {
    Image ic = BeanSupport.getBeanIcon (component.getComponentClass (), iconType);
    if (ic != null) return ic;
    else return super.getIcon (iconType);
  }
  
  public Image getOpenedIcon (int iconType) {
    return getIcon (iconType);
  }
  
  public Node.PropertySet[] getPropertySets () {
    return component.getProperties ();
  }

  /** Lazily initialize set of node's actions (overridable).
  * The default implementation returns <code>null</code>.
  * <p><em>Warning:</em> do not call {@link #getActions} within this method.
  * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
  * @return array of actions for this node, or <code>null</code> to use the default node actions
  */
  protected SystemAction [] createActions () {
    return new SystemAction [] {
      SystemAction.get(PasteAction.class),
      SystemAction.get(CopyAction.class),
      SystemAction.get(CutAction.class),
      null,
      SystemAction.get(RenameAction.class),
      SystemAction.get(DeleteAction.class),
      null,
      SystemAction.get(PropertiesAction.class),
    };
  }

  /** Can this node be renamed?
  * @return <code>false</code>
  */
  public boolean canRename () {
    return false; // !(component instanceof FormContainer); // [PENDING]
  }

  /** Can this node be destroyed?
  * @return <CODE>false</CODE>
  */
  public boolean canDestroy () {
    return !(component instanceof FormContainer);
  }

  /** Remove the node from its parent and deletes it.
  * The default
  * implementation obtains write access to
  * the {@link Children#MUTEX children's lock}, and removes
  * the node from its parent (if any). Also fires a property change.
  * <P>
  * This may be overridden by subclasses to do any additional
  * cleanup.
  *
  * @exception IOException if something fails
  */
  public void destroy () throws java.io.IOException {
    component.getFormManager ().deleteComponent (component);
    super.destroy ();
  }

  /** Get a cookie from the node.
  * Uses the cookie set as determined by {@link #getCookieSet}.
  *
  * @param type the representation class
  * @return the cookie or <code>null</code>
  */
  public Node.Cookie getCookie (Class type) {
    if (SaveCookie.class.equals (type)) {
      return component.getFormManager ().getFormObject ().getCookie (SaveCookie.class);
    }
    return super.getCookie (type);
  }

// -----------------------------------------------------------------------------
// FormNodeCookie implementation
  
  public RADComponent getRADComponent () {
    return component;
  }

}

/*
 * Log
 *  8    Gandalf   1.7         5/16/99  Ian Formanek    No canRename
 *  7    Gandalf   1.6         5/16/99  Ian Formanek    
 *  6    Gandalf   1.5         5/15/99  Ian Formanek    
 *  5    Gandalf   1.4         5/14/99  Ian Formanek    
 *  4    Gandalf   1.3         5/12/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
