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

package org.netbeans.modules.j2ee.sun.validation;

import org.netbeans.modules.schema2beans.BaseProperty;

/**
 * This Interface defines all the constants used in this framework
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public interface Constants {
    /***/
    int MANDATORY_ELEMENT = BaseProperty.INSTANCE_MANDATORY_ELT;

    int MANDATORY_ARRAY = BaseProperty.INSTANCE_MANDATORY_ARRAY;

    int OPTIONAL_ELEMENT = BaseProperty.INSTANCE_OPTIONAL_ELT;

    int OPTIONAL_ARRAY = BaseProperty.INSTANCE_OPTIONAL_ARRAY;

    String BUNDLE_FILE = 
            "org.netbeans.modules.j2ee.sun.validation.Bundle";          //NOI18N

    String IMPL_FILE = 
            "org.netbeans.modules.j2ee.sun.validation.impl.Impl";       //NOI18N
    
    String XPATH_DELIMITER = "/";                                       //NOI18N

    char XPATH_DELIMITER_CHAR = '/';
}
