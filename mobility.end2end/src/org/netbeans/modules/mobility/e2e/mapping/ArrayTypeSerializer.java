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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.Traversable;

/**
 *
 * @author Michal Skvor, Jirka Prazak
 */
public class ArrayTypeSerializer implements JavonSerializer {
    
    private Set<ClassData> arrayTypes;
    
    /** Creates a new instance of ArrayTypeSerializer */
    public ArrayTypeSerializer() {
        arrayTypes = new HashSet<ClassData>();
    }
    
    public String getName() {
        return "Array type serializer";
    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.ARRAY != type.getKind()) return false;
        return true;
    }

    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.ARRAY == type.getKind()) {
            ArrayType array = (ArrayType)type;
            TypeMirror componentType = array.getComponentType();
            ClassData cCD = traversable.traverseType( componentType, typeCache );

            if( cCD != null) {
                ClassData cd = new ClassData( cCD.getPackage(), cCD.getClassName(), false, true, this );
                cd.setComponentType( cCD );
                traversable.registerType( cCD );
                //TODO: ??? shouldn't this be cCD instead of cd ???
                arrayTypes.add( cd );
                return cd;
            }
        }

        return null;
    }

    public String instanceOf( JavonMapping mapping, ClassData type  ) {        
        ClassData componentType = type.getComponentType();
        return componentType.getSerializer().instanceOf( mapping, componentType ) + "[]";
    }

    public String toObject( JavonMapping mapping, ClassData type, String variable  ) {
        if( arrayTypes.contains( type )) {
            return variable;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());                
    }

    public String fromObject( JavonMapping mapping, ClassData type, String object  ) {
        if( arrayTypes.contains( type )) {
            return "(" + mapping.getRegistry().getTypeSerializer( type ).instanceOf( mapping, type ) + ")" + object;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( arrayTypes.contains( type )) {
            String serializationCode = "";
            String id = "" + mapping.getRegistry().getRegisteredTypeId( type );
            String instanceOfType = type.getSerializer().instanceOf( mapping, type );
            serializationCode += instanceOfType + " a_result_" + id + " = (" + 
                    instanceOfType + ") o;\n";
            serializationCode += stream + ".writeInt( a_result_" + id + ".length );\n";
            serializationCode += "for( int i  = 0; i < a_result_" + id +".length; i++ ) {\n";
            ClassData componentType = type.getComponentType();
            if( componentType.isPrimitive()) {
                serializationCode += "writeObject( " + stream + ", " + 
                        componentType.getSerializer().toObject( mapping, componentType, "a_result_" + id + "[i]"  ) + " , " +
                        mapping.getRegistry().getRegisteredTypeId( componentType ) + " );\n";
            } else {
                serializationCode += "writeObject( " + stream + ", a_result_" + id + "[i], " + 
                        mapping.getRegistry().getRegisteredTypeId( componentType ) + " );\n";
            }
            serializationCode += "}\n";
            return serializationCode;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( arrayTypes.contains( type )) {
            String deserializationCode = "";
            String id = "" + mapping.getRegistry().getRegisteredTypeId( type );
            ClassData leafComponentType = type;
            int depth = 0;
            while(( leafComponentType = leafComponentType.getComponentType()).isArray()) depth++;
            String appendedBrackets = "";
            for( int i = 0; i < depth; i++ ) appendedBrackets += "[]";
            deserializationCode += "int a_size_" + id + " = " + stream + ".readInt();\n";
            String instanceOfType = type.getSerializer().instanceOf( mapping, type );
            String leafInstanceOfType = leafComponentType.getSerializer().instanceOf( mapping, leafComponentType );
            deserializationCode += instanceOfType + " a_result_" + id + 
                    " = new " + leafInstanceOfType + "[ a_size_" + id + " ]" + appendedBrackets + ";\n";
            String i = "a_i_" + id;
            deserializationCode += "for( int " + i + " = 0; " + i + " < a_size_" + id + "; " + i + "++ ) {\n";
            ClassData componentType = type.getComponentType();
            if( componentType.isPrimitive()) {
                deserializationCode += "a_result_" + id + "[" + i + "] = " + 
                        componentType.getSerializer().fromObject( mapping, componentType, "readObject( " + stream + " )"  ) + ";\n";
            } else {
                deserializationCode += "a_result_" + id + "[" + i + "] = (" + 
                        type.getComponentType().getSerializer().instanceOf( mapping, type.getComponentType()) + ")readObject( " + stream + " );\n";
            }
            deserializationCode += "}\n";
            deserializationCode += ( object == null ? "" :  object + " = a_result_" + id + ";\n" );
            //deserializationCode += "result = (Object)a_result;\n";
            return deserializationCode;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }
    
    public Set<ClassData> getReferencedTypes( ClassData rootClassData, Set<ClassData> usedTypes ) {
        Set<ClassData> result = new HashSet<ClassData>();
        result.add( rootClassData );
        result.addAll( rootClassData.getComponentType().getSerializer().
                getReferencedTypes( rootClassData.getComponentType(), usedTypes ));
        return result;
    }
}
