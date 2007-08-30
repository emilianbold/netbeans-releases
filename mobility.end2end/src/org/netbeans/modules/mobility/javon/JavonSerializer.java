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
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;

/**
 *
 * @author Michal Skvor
 */
public interface JavonSerializer {
    
    /**
     * Return name of the JavonSerializer
     * 
     * @return name of the serializer
     */
    public String getName();
    
    /**
     * Return true when the given type is supported
     * 
     * @param traversable 
     * @param type 
     * @return true when the type is supported
     */
    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache );
    
    /**
     * 
     * @param traversable 
     * @param type 
     * @return 
     */
    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache );
    
    /**
     * Return fully qualified or short name of the given type. The short name
     * could be used for java.lang package when no import is necesary
     * 
     * @param type
     * @return fully qualified name or short name of type
     */
    public String instanceOf( ClassData type );
    
    /**
     * 
     * @param type 
     * @param variable 
     * @return 
     */
    public String toObject( ClassData type, String variable );
    
    /**
     * 
     * @param type 
     * @param object 
     * @return 
     */
    public String fromObject( ClassData type, String object );
    
    /**
     * 
     * @param type 
     * @param stream 
     * @param object 
     * @return 
     */
    public String toStream( JavonMapping mapping, ClassData type, String stream, String object );
    
    /**
     * Create serialization block for given type
     * 
     * <p>Sample implementation:
     * <code>
     * return object + " = new Integer(" + stream + ".readInt());";
     * </code>
     * 
     * @param type 
     * @param stream 
     * @param object 
     * @return 
     */
    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object );
    
    /**
     * Return Set of all types on which is the root ClassData object 
     * depending
     * 
     * @param rootClassData root class data
     * @return Set<ClassData> of all referenced types
     */
    public Set<ClassData> getReferencesTypes( ClassData rootClassData, Set<ClassData> usedTypes );
    
    
}
