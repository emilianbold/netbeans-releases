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

import com.netbeans.ide.nodes.Node;

/** 
*
* @author Ian Formanek
*/
public abstract class CodeGenerator {

  public abstract void initialize (FormManager formManager);
  
  /** Alows the code generator to provide synthetic properties for specified component
  * which are specific to the code generation method.
  * E.g. a JavaCodeGenerator will return variableName property, as it generates
  * global Java variable for every component
  * @param component The RADComponent for which the properties are to be obtained
  */
  public Node.Property[] getSyntheticProperties (RADComponent component) {
    return new Node.Property[0];
  }
}
/*
 * Log
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */

