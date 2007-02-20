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
package org.netbeans.modules.j2ee.common.method;

import java.util.Collection;

/**
 * Provide a factory for obtaining MethodCustomizer instances
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class MethodCustomizerFactory {

    private MethodCustomizerFactory() {}
    
    public static MethodCustomizer businessMethod(String title, MethodModel method, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                local,
                remote,
                selectLocal,
                selectRemote,
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                true,  // interfaces
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer homeMethod(String title, MethodModel method, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                local,
                remote,
                selectLocal,
                selectRemote,
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                true,  // interfaces
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer createMethod(String title, MethodModel method, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                local,
                remote,
                selectLocal,
                selectRemote,
                false,    // return type
                null,     // EJB QL
                false,    // finder cardinality
                true,     // exceptions
                true,     // interfaces
                "create", // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer finderMethod(String title, MethodModel method, boolean remote, boolean local, boolean selectLocal, boolean selectRemote, String ejbql, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                local,
                remote,
                selectLocal,
                selectRemote,
                false,  // return type
                ejbql,  // EJB QL
                true,   // finder cardinality
                false,  // exceptions
                true,   // interfaces
                "find", // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer operationMethod(String title, MethodModel method, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true, // doesn't matter? interfaces selections is disabled
                true,  // return type
                null,  // EJB QL
                false, // finder cardinality
                true,  // exceptions
                false, // interfaces
                null,  // prefix
                existingMethods
                );
    }
    
    public static MethodCustomizer selectMethod(String title, MethodModel method, String ejbql, Collection<MethodModel> existingMethods) {
        return new MethodCustomizer(
                title,
                method,
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                false,       // doesn't matter? interfaces selections is disabled
                true,        // return type
                ejbql,       // EJB QL
                false,       // finder cardinality
                false,       // exceptions
                false,       // interfaces
                "ejbSelect", // prefix
                existingMethods
                );
    }
    
}
