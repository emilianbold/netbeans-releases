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

package com.netbeans.developer.modules.loaders.form.formeditor;

import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.NbBundle;

import java.awt.Image;
import java.text.MessageFormat;

/** 
*
* @author Ian Formanek
*/
public class RADComponentNode extends AbstractNode {

  private final static MessageFormat nameFormat = new MessageFormat (NbBundle.getBundle (RADComponentNode.class).getString ("FMT_ComponentName"));
  
  private RADComponent component;
  
  public RADComponentNode (RADComponent component) {
    super ((component instanceof ComponentContainer) ? new RADChildren ((ComponentContainer)component) : Children.LEAF);
    this.component = component;
    String className = component.getComponentClass ().getName ();
    setName (nameFormat.format (new Object[] {component.getName (), className, className.substring (className.lastIndexOf (".") + 1) } ));
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
}

/*
 * Log
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
