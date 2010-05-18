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
package org.netbeans.modules.soa.ui;

import org.netbeans.modules.soa.ui.form.CommonUiBundle;
import org.openide.util.NbBundle;

/**
 * Keeps different reusable constants.
 *
 * @author nk160297
 */
public interface SoaConstants {
    
    String BOUNDED_ATTRIBUTE_NAME = "BOUNDED_ATTRIBUTE_NAME"; // NOI18N
    String BOUNDED_ELEMENT_CLASS = "BOUNDED_ELEMENT_CLASS"; // NOI18N
    String PROPERTY_TYPE_ATTRIBUTE = "PropertyTypeAttribute"; // NOI18N
    String PROPERTY_NODE_ATTRIBUTE = "PropertyNodeAttribute"; // NOI18N
    
    String COLON = ":"; // NOI18N
    String INVALID = NbBundle.getMessage(CommonUiBundle.class, "LBL_Invalid"); // NOI18N
    String MISSING = NbBundle.getMessage(CommonUiBundle.class, "LBL_Missing"); // NOI18N
    String NOT_ASSIGNED = NbBundle.getMessage(CommonUiBundle.class, "LBL_Not_Assigned"); // NOI18N
    
    // The delay between a field input event and the start of fast validation
    int INPUT_VALIDATION_DELAY = 400;
    
}
