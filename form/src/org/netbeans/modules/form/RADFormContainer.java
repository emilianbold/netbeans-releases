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

import com.netbeans.developer.modules.loaders.form.forminfo.FormInfo;

/** 
*
* @author Ian Formanek
*/
public class RADFormContainer extends RADContainer implements FormContainer {

  private FormInfo formInfo;
  
  public RADFormContainer (FormInfo formInfo) {
    this.formInfo = formInfo;
  }
  
  public String getContainerGenName () {
    return formInfo.getContainerGenName ();
  }
  
  public FormInfo getFormInfo () {
    return formInfo;
  }
}

/*
 * Log
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
