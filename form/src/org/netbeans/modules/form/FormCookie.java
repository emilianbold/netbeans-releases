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

/** Cookie for Form operations.
*
* @author Ian Formanek
* @version 0.10, May 26, 1998
*/
public interface FormCookie extends Node.Cookie {

  /** Focuses the source editor */
  public void gotoEditor();

  /** Focuses the form */
  public void gotoForm();

}

/*
 * Log
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/11/99  Ian Formanek    Build 318 version
 *  1    Gandalf   1.0         3/17/99  Ian Formanek    
 * $
 */
