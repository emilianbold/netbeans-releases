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

package org.netbeans.modules.mobility.javon;

import java.util.Map;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;

/**
 *
 * @author Michal Skvor
 */
public interface Traversable {
    
    /**
     * Traverse given type and return the substructure
     * 
     * @param type 
     * @return 
     */
    public ClassData traverseType( TypeMirror type, Map<String, ClassData> typeCache );
    
    /**
     * Checks whether the type is supported by the Traversable
     * 
     * @param type to check
     * @return true if type is supported
     */
    public boolean isTypeSupported( TypeMirror type, Map<String, ClassData> typeCache );

     /**
     *  Register type with its serializer
     *
     *  @param type to be registered
     *  @return serializer that registered it or null type has no serializer associated with it
     */

    public JavonSerializer registerType( ClassData type );
}
