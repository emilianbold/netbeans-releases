/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

/** FormAwareEditor is an interface implemented by property editors
 * and Customizers, which want to be aware of extended design-time 
 * information about the form - e.g. other components.
 * @author Ian Formanek
 */
public interface FormAwareEditor {

    /** If a property editor or customizer implements the FormAwareEditor
     * interface, this method is called immediately after the PropertyEditor
     * instance is created or the Customizer is obtained from getCustomizer().
     * @model The FormModel representing meta-data of current form
     */
    public void setFormModel(FormModel model);
}
