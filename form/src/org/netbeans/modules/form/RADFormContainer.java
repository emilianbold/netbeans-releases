/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
