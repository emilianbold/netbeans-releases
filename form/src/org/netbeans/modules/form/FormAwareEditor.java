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
