/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

//import org.netbeans.modules.form.forminfo.FormInfo;

/**
 *
 * @author Ian Formanek
 */
public class RADFormContainer extends RADContainer implements FormContainer {

    /** The form info of form represented by this RADFormContainer */
//    private FormInfo formInfo;

    /** Creates new RADFormContainer for form specified by its FormInfo
     * @param formInfo the info describing the form type
     */
//    public RADFormContainer(FormInfo formInfo) {
//        this.formInfo = formInfo;
//    }

    /** Getter for the Name property of the component - overriden to provide non-null value,
     * as the top-level component does not have a variable
     * @return current value of the Name property
     */
    public String getName() {
        return FormUtils.getBundleString("CTL_FormTopContainerName"); // NOI18N
    }

    /** Setter for the Name property of the component - usually maps to variable declaration for holding the
     * instance of the component
     * @param value new value of the Name property
     */
    public void setName(String value) {
        // noop in forms
    }

    /** Called to create the instance of the bean. Default implementation simply creates instance
     * of the bean's class using the default constructor.  Top-level container(the form object itself) 
     * will redefine this to use FormInfo to create the instance, as e.g. Dialogs cannot be created using 
     * the default constructor 
     * @return the instance of the bean that will be used during design time 
     */
//    protected Object createBeanInstance() {
//        return formInfo.getFormInstance();
//    }

    /** Called to obtain a Java code to be used to generate code to access the container for adding subcomponents.
     * It is expected that the returned code is either ""(in which case the form is the container) or is a name of variable
     * or method call ending with "."(e.g. "container.getContentPane().").
     * @return the prefix code for generating code to add subcomponents to this container
     */
//    public String getContainerGenName() {
//        return formInfo.getContainerGenName();
//    }

    /** @return the form info of form represented by this RADFormContainer */
//    public FormInfo getFormInfo() {
//        return formInfo;
//    }
}
