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

  /** The form info of form represented by this RADFormContainer */
  private FormInfo formInfo;
  
  /** Creates new RADFormContainer for form specified by its FormInfo
  * @param formInfo the info describing the form type
  */
  public RADFormContainer (FormInfo formInfo) {
    this.formInfo = formInfo;
  }
  
  /** Setter for the Name property of the component - usually maps to variable declaration for holding the 
  * instance of the component
  * @param value new value of the Name property
  */
  public void setName (String value) {
    // noop in forms
  }

  /** Called to create the instance of the bean. Default implementation simply creates instance 
  * of the bean's class using the default constructor.  Top-level container (the form object itself) 
  * will redefine this to use FormInfo to create the instance, as e.g. Dialogs cannot be created using 
  * the default constructor 
  * @return the instance of the bean that will be used during design time 
  */
  protected Object createBeanInstance () {
    return formInfo.getContainerGenName ();
  }

  public String getContainerGenName () {
    return formInfo.getContainerGenName ();
  }
  
  /** @return the form info of form represented by this RADFormContainer */
  public FormInfo getFormInfo () {
    return formInfo;
  }
}

/*
 * Log
 *  3    Gandalf   1.2         7/25/99  Ian Formanek    Variables management 
 *       moved to RADComponent
 *  2    Gandalf   1.1         6/6/99   Ian Formanek    New FormInfo design 
 *       employed to provide correct top-level bean properties
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
