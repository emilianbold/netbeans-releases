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

import java.util.List;

/**
 * Customizer of set of properties of components.
 *
 * @author Jan Stola
 */
public interface PropertyModifier {

    /**
     * Customizes the set of properties of given component.
     *
     * @param metacomp component whose properties should be customized.
     * @param prefProps preferred properties.
     * @param normalProps normal properties.
     * @param expertProps expert properties.
     * @return <code>true</code> if some properties were removed/added,
     * returns <code>false</code> otherwise.
     */
    boolean modifyProperties(RADComponent metacomp, List<FormProperty> prefProps, List<FormProperty> normalProps, List<FormProperty> expertProps);
    
}
