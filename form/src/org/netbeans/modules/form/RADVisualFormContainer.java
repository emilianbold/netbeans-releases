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

import com.netbeans.developerx.loaders.form.formeditor.layouts.*;
import com.netbeans.developer.modules.loaders.form.forminfo.FormInfo;

import java.awt.Container;

/** RADVisualFormContainer represents the top-level container of the form and the form itself
* during design time.
*
* @author Ian Formanek
*/
public class RADVisualFormContainer extends RADVisualContainer implements FormContainer {
  
  private FormInfo formInfo;
  private Container topContainer;
  
  public RADVisualFormContainer (FormInfo formInfo) {
    super ();
    this.formInfo = formInfo;
    topContainer = formInfo.getTopContainer ();
  }
  
  /** @return The JavaBean visual container represented by this RADVisualComponent */
  public Container getContainer () {
    return topContainer;
  }

  /** Called to create the instance of the bean. Default implementation simply creates instance 
  * of the bean's class using the default constructor.  Top-level container (the form object itself) 
  * will redefine this to use FormInfo to create the instance, as e.g. Dialogs cannot be created using 
  * the default constructor 
  * @return the instance of the bean that will be used during design time 
  */
  protected Object createBeanInstance () {
    return formInfo.getFormInstance ();
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
 *  3    Gandalf   1.2         6/6/99   Ian Formanek    New FormInfo design 
 *       employed to provide correct top-level bean properties
 *  2    Gandalf   1.1         5/11/99  Ian Formanek    Build 318 version
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
