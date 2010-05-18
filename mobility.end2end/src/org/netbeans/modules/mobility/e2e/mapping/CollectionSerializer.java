/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    public String instanceOf( JavonMapping mapping,ClassData type  ) {
        if( vectorClassData.equals( type )) {
            return type.getFullyQualifiedName();
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String toObject( JavonMapping mapping, ClassData type, String object  ) {
        if( vectorClassData.equals( type )) {
            return "(java.util.Vector)" + object;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromObject( JavonMapping mapping, ClassData type, String object  ) {
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

    public Set<ClassData> getReferencedTypes( ClassData rootClassData, Set<ClassData> usedTypes ) {
        return Collections.singleton( rootClassData );
    }
}
