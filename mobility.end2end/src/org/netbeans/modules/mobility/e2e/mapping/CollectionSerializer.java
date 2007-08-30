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
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.Traversable;

/**
 *
 * @author Michal Skvor
 */
public class CollectionSerializer implements JavonSerializer {
    
    private final ClassData vectorClassData = new ClassData( "java.util", "Vector", false, false, this );
    
    /** Creates a new instance of CollectionSerializer */
    public CollectionSerializer() {
    }
    
    public String getName() {
        return "Collection serializer";
    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.DECLARED == type.getKind()) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            String fqn = clazz.getQualifiedName().toString();
            if( "java.util.Vector".equals( fqn )) {
                return true;
            }
        }
        return false;
    }

    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.DECLARED == type.getKind()) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            String fqn = clazz.getQualifiedName().toString();
            if( "java.util.Vector".equals( fqn )) {
                return vectorClassData;
            }
        }
        return null;
    }

    public String instanceOf( ClassData type ) {
        if( vectorClassData.equals( type )) {
            return "Vector";
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String toObject( ClassData type, String object ) {
        if( vectorClassData.equals( type )) {
            return "(java.util.Vector)" + object;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromObject( ClassData type, String object ) {
        if( vectorClassData.equals( type )) {
            return "(java.util.Vector)" + object;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( vectorClassData.equals( type )) {
            String serialization = "";
            serialization += "java.util.Vector v = (java.util.Vector) " + object + ";\n";
            serialization += stream + ".writeInt(v.size());\n";
            serialization += "for( int i = 0; i < v.size(); i++ ) {\n";
            serialization += "writeObject(" + stream + ", v.elementAt( i ));\n";
            serialization += "}";
            return serialization;
        } 
        throw new IllegalArgumentException( "Invalid type: " + type.getName());                
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( vectorClassData.equals( type )) {
            String deserialization = "";
            deserialization += "int size = " + stream + ".readInt();\n";
            deserialization += "java.util.Vector v = new java.util.Vector(size);\n";
            deserialization += "for( int i = 0; i < size; i++ ) {\n";
            deserialization += "v.addElement(readObject(" + stream + "));\n";
            deserialization += "}\n";
            deserialization += "result = v;";
            return deserialization;
        } 
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public Set<ClassData> getReferencesTypes( ClassData rootClassData, Set<ClassData> usedTypes ) {
        return Collections.singleton( rootClassData );
    }
}
