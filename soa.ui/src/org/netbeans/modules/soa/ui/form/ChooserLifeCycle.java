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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.soa.ui.form;

/**
 * Describes important stages of the life cycle of a Chooser dialog.
 *
 * The Generics paramether T represents the type of value which is implied to be chosen.
 *
 * @author nk160297
 */
public interface ChooserLifeCycle<T> extends FormLifeCycle {

    /**
     * Sets the value which has to be shown in the chooser as a selection.
     * <p>
     * Optionally, the value can be passed to a chooser in a constructor.
     * In other case, the selection of current value isn't necessary at all. 
     * So the call of this method isn't obligatory. 
     */
    void setSelectedValue(T newValue);
    
    /**
     * Returns the value which is the current selection in the chooser.
     * This method is intended to be used after the chooser is closed. 
     * It also can be used to do validation for enabling/disabling of the Ok button. 
     */
    T getSelectedValue();
}
