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
  
  /** Getter for the Name property of the component - overriden to provide non-null value, 
  * as the top-level component does not have a variable
  * @return current value of the Name property
  */
  public String getName () {
    return FormEditor.getFormBundle ().getString ("CTL_FormTopContainerName");
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
    return formInfo.getFormInstance ();
  }

  /** Called to obtain a Java code to be used to generate code to access the container for adding subcomponents.
  * It is expected that the returned code is either "" (in which case the form is the container) or is a name of variable
  * or method call ending with "." (e.g. "container.getContentPane ().").
  * @return the prefix code for generating code to add subcomponents to this container
  */
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
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/15/99  Ian Formanek    getContainerGenName 
 *       usage clarified
 *  4    Gandalf   1.3         7/30/99  Ian Formanek    Fixed bugs 2915 - 
 *       Changing "viewport" property of the JScrollPane does not work - 
 *       "Property" and 2916 - Changing "viewport" property of the JScrollPane 
 *       does not work - "Method Call"
 *  3    Gandalf   1.2         7/25/99  Ian Formanek    Variables management 
 *       moved to RADComponent
 *  2    Gandalf   1.1         6/6/99   Ian Formanek    New FormInfo design 
 *       employed to provide correct top-level bean properties
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
