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

package com.netbeans.developer.modules.loaders.form.forminfo;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;

/** 
*
* @author Ian Formanek
*/
public interface FormInfo {

  public PropertyDescriptor[] getFormProperties ();
  
  public EventSetDescriptor[] getFormEvents ();

  public String getContainerGenName ();
  
  public Container getTopContainer ();

  public Component getTestComponent ();
  
  public Container getTestTopContainer (Component comp);

}

/*
 * Log
 *  3    Gandalf   1.2         5/10/99  Ian Formanek    
 *  2    Gandalf   1.1         5/10/99  Ian Formanek    
 *  1    Gandalf   1.0         5/3/99   Ian Formanek    
 * $
 */
