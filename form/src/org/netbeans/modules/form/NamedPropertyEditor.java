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

import java.beans.PropertyEditor;

/** An interface which can be implemented by PropertyEditor to provide a display name 
* to identify the editor. This name will be used for example as the tab name in Form CustomPropertyEditor. 
*
* @author Ian Formanek
*/
public interface NamedPropertyEditor extends PropertyEditor {

  /** @return display name of the property editor */
  public String getDisplayName ();
}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/17/99  Ian Formanek    
 * $
 */
