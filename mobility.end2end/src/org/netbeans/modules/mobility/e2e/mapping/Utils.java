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

package org.netbeans.modules.mobility.e2e.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;

/**
 *
 * @author Michal Skvor
 */
public class Utils {
    
    private ClassDataRegistry registry;
    
    /** Creates a new instance of Utils 
     * @param registry 
     */
    public Utils( ClassDataRegistry registry ) {
        this.registry = registry;
    }
        
    /**
     * Filter out duplicate ClassData instances from Set
     * 
     * @param service 
     * @return filtered instances
     */
    public Set<ClassData> getReturnTypeInstances( JavonMappingImpl.Service service ) {
        return filterInstances( service.getReturnTypes());
    }
    
    /**
     * Filter out duplicate ClassData instances from parameter type Set
     * @param service 
     * @return filtered instances
     */
    public Set<ClassData> getParameterTypeInstances( JavonMappingImpl.Service service ) {
        return filterInstances( service.getParameterTypes());
    }
    
    /**
     * Filter duplicate instance name from the set of classes
     * @param classes 
     * @return 
     */
    public Set<ClassData> filterInstances( Set<ClassData> classes ) {
        Set<String> instanceTypes = new HashSet<String>();
        for( ClassData cd : classes ) {
            instanceTypes.add( registry.getTypeSerializer( cd ).instanceOf( cd ));            
        }
        
        Map<String, ClassData> result = new HashMap<String, ClassData>();
        for( ClassData cd : classes ) {
            if( result.get( registry.getTypeSerializer( cd ).instanceOf( cd )) == null ) {
                result.put( registry.getTypeSerializer( cd ).instanceOf( cd ), cd );
            }
        }
        return Collections.unmodifiableSet( new HashSet( result.values()));
    }
}
