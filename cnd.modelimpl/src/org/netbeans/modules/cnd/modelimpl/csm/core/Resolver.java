/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.*;
import java.util.*;

/**
 * @author Vladimir Kvasihn
 */
public interface Resolver {

    /**
     * Resolves classifier (class/enum/typedef) or namespace name.
     * Why classifier or namespace? Because in the case org::vk::test
     * you don't know which is class and which is namespace name
     *
     * @param nameTokens tokenized name to resolve
     * (for example, for std::vector it is new String[] { "std", "vector" })
     *
     * @return object of the following class:
     *  CsmClassifier (CsmClass, CsmEnum, CsmTypedef)
     *  CsmNamespace
     */
    public CsmObject resolve(String[] nameTokens);
    
    /**
     * Resolves classifier (class/enum/typedef) or namespace name.
     * Why classifier or namespace? Because in the case org::vk::test
     * you don't know which is class and which is namespace name 
     *
     * @param qualifiedName name to resolve 
     *
     * @return object of the following class:
     *  CsmClassifier (CsmClass, CsmEnum, CsmTypedef)
     *  CsmNamespace
     */
    public CsmObject resolve(String qualifiedName);

}
