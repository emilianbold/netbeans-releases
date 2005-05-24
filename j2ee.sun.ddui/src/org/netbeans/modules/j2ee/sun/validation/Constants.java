/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
