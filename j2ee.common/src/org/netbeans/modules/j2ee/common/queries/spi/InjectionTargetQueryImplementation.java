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

package org.netbeans.modules.j2ee.common.queries.spi;

import org.netbeans.jmi.javamodel.JavaClass;

/**
 * Knowledge of ability to use resouce injection in Java class and the way the injection is generated
 * @see org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery
 * @author Martin Adamek
 */
public interface InjectionTargetQueryImplementation {
    
    /**
     * Decide if dependency injection can be used in given class.<br>
     * @param jc class where annotated field or method should be inserted
     * @return true if any container or environment is able to inject resources in given class, false otherwise
     */
    boolean isInjectionTarget(JavaClass jc);
    
    /**
     * Decide if injected reference must be static in given class. 
     * For example, in application client injection can be used only in class with main method and all
     * injected fields must be static<br>
     * Implementation 
     * @param jc class where annotated field or method should be inserted
     * @return true if static reference is required in given class, false otherwise
     */
    boolean isStaticReferenceRequired(JavaClass jc);
    
}
