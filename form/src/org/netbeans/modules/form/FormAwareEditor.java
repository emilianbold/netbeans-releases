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

package org.netbeans.modules.form;

/** FormAwareEditor is an interface implemented by property editors
* and Customizers, which want to be aware of extended design-time 
* information about the form - e.g. other components and their properties.
* @author Ian Formanek
*/
public interface FormAwareEditor {

    /** If a property editor or customizer implements the FormAwareEditor
    * interface, this method is called immediately after the PropertyEditor
    * instance is created or the Customizer is obtained from getCustomizer ().
    * @param component The RADComponent representing the JavaBean being edited by this 
    *                  property editor or customizer
    * @param property  The RADProperty being edited by this property editor or null 
    *                  if this interface is implemented by a customizer
    */
    public void setRADComponent (RADComponent component, RADComponent.RADProperty property);
}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/12/99  Ian Formanek    FormAwareEditor.setRADComponent
 *        changes
 *  1    Gandalf   1.0         5/23/99  Ian Formanek    
 * $
 */
