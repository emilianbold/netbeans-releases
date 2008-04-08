/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.FieldData;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.Traversable;

/**
 *
 * @author Michal Skvor
 */
public class BeanTypeSerializer implements JavonSerializer {
    
    /** Map of all types */
    private Map<String, ClassData> beanTypes;
    
    private static final Set<String> UNSUPPORTED_PACKAGES = 
            new HashSet<String>( Arrays.asList( new String[] {
        "javax", "java"
    }));
    
    /** Creates a new instance of BeanTypeSerializer */
    public BeanTypeSerializer() {
        beanTypes = new HashMap<String, ClassData>();
    }
    
    public String getName() {
        return "Bean type serializer";
    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.ARRAY == type.getKind()) return false;
        if( TypeKind.DECLARED == type.getKind()) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            String shortName = clazz.getSimpleName().toString();
            String classFullQualifiedName = clazz.getQualifiedName().toString();
            if( "java.lang.Object".equals( classFullQualifiedName )) return false;
            if( classFullQualifiedName.startsWith( "java.util" )) return false;
            String packageName = "";
            if( shortName.length() != classFullQualifiedName.length()) {
                int fqnLength = classFullQualifiedName.length();
                int shortLength = shortName.length();
                packageName = classFullQualifiedName.substring( fqnLength - shortLength - 1 );
            }
            
            // Check for parent
            if( clazz.getSuperclass().getKind() != TypeKind.DECLARED ) return false;
            TypeElement superClass = (TypeElement)((DeclaredType) clazz.getSuperclass()).asElement();
            if( !traversable.isTypeSupported( clazz.getSuperclass(), typeCache ) &&
                    !"java.lang.Object".equals( superClass.getQualifiedName().toString())) return false;
            
            // Check for default non static constructor
            List<ExecutableElement> constructors = ElementFilter.constructorsIn( clazz.getEnclosedElements());
            boolean validConstructor = false;
            for( ExecutableElement ee : constructors ) {
                if( ee.getParameters().size() == 0 ) {
                    if( ee.getModifiers().contains( Modifier.PUBLIC ) && 
                            !ee.getModifiers().contains( Modifier.STATIC )) {
                        validConstructor = true;
                        break;
                    }
                }
            }            
            if( !validConstructor ) return false;                        
            return true;
        }
        return false;
    }

    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.ARRAY == type.getKind()) return null;
        if( TypeKind.DECLARED == type.getKind()) {
            // Get class
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            
            String shortName = clazz.getSimpleName().toString();
            String classFullQualifiedName = clazz.getQualifiedName().toString();
            String packageName = "";
            if( shortName.length() != classFullQualifiedName.length()) {
                int fqnLength = classFullQualifiedName.length();
                int shortLength = shortName.length();
                packageName = classFullQualifiedName.substring( 0, fqnLength - shortLength - 1 );
            }
            
            // Check for supported package
            for( String unsupportedPackage : UNSUPPORTED_PACKAGES ) {
                if( packageName.startsWith( unsupportedPackage )) return null;
            }
            
            // Check whether the type is in cache
            if( typeCache.get( classFullQualifiedName ) != null ) {
                return typeCache.get( classFullQualifiedName );
            }
            // Chech whether the type is in registered types
            ClassData cd = beanTypes.get( classFullQualifiedName );
            if( cd != null ) {
                return cd; 
            }
            
            cd = new ClassData( packageName, shortName, false, false, this );
            beanTypes.put( classFullQualifiedName, cd );
            typeCache.put( classFullQualifiedName, cd );
            
            // Get fields
            for( VariableElement e : ElementFilter.fieldsIn( clazz.getEnclosedElements())) {
                // Skip static declarations
                if( e.getModifiers().contains( Modifier.STATIC )) continue;
                // Skip final declarations
                if( e.getModifiers().contains( Modifier.FINAL )) continue;
                
                ClassData fieldClass = traversable.traverseType( e.asType(), typeCache );
                if( fieldClass == null ) continue;
                String methodPartName = e.getSimpleName().toString().substring( 0, 1 ).toUpperCase() + 
                        e.getSimpleName().toString().substring( 1 );
                String setterName = "set" + methodPartName;
                String getterName = ( e.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get" ) + methodPartName;
                FieldData field = new FieldData( e.getSimpleName().toString(), fieldClass );
                
                if( e.getModifiers().contains( Modifier.PUBLIC )) { 
                    field.setModifier( ClassData.Modifier.PUBLIC );
                    cd.addField( field );
                    traversable.registerType( fieldClass );
                } else {
                    boolean hasSetter = false, hasGetter = false;
                    for( ExecutableElement ee : ElementFilter.methodsIn( clazz.getEnclosedElements())) {
                        String eeName = ee.getSimpleName().toString();
                        if( setterName.equals( eeName )) {
//                            System.err.println(" - setter : " + setterName );
                            field.setSetter( setterName );
                            hasSetter = true;
                        }
                        if( getterName.equals( eeName )) {
//                            System.err.println(" - getter : " + getterName );
                            field.setGetter( getterName );
                            hasGetter = true;
                        }
                    }
                    field.setModifier( ClassData.Modifier.PRIVATE );
                    if( hasGetter && hasSetter ) {
                        cd.addField( field );
                        traversable.registerType( fieldClass );
                    }
                }
            }
                        
            // Support only bean supertypes            
            if( clazz.getSuperclass().getKind() == TypeKind.DECLARED ) {
                TypeElement superclass = (TypeElement)((DeclaredType) clazz.getSuperclass()).asElement();
                ClassData superType = traversable.traverseType( superclass.asType(), typeCache );
                if( superType != null ) {
                    traversable.registerType( superType );
                    cd.setParent( superType );
                } else {
                    if( !"java.lang.Object".equals(superclass.getQualifiedName().toString())) {
                        return null;
                    }
                }
            } 
                        
            return cd;
        }
        
        return null;
    }
        
    public String instanceOf( JavonMapping mapping, ClassData type  ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            return type.getFullyQualifiedName();
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }
    
    public String toObject( JavonMapping mapping,ClassData type, String variable  ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            return "(" + type.getFullyQualifiedName() + ")" + ( variable == null ? "" : variable );
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromObject( JavonMapping mapping,ClassData type, String object  ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            return "(" + type.getFullyQualifiedName() + ")" + object;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            String serialization = "";
            String beanInstanceName = "b_" + type.getFullyQualifiedName().replace( ".", "_" );
//            if( mapping.getProperty( "target" ).equals( "client" )) {
                serialization += type.getFullyQualifiedName() + " " + beanInstanceName + " = (" + type.getFullyQualifiedName() + ")" + object + ";\n";
//            }
//            for( FieldData field : type.getFields()) {
            for( FieldData field : type.getAllFields()) {
                String id = "";
                id = ", " + mapping.getRegistry().getRegisteredTypeId( field.getType());
                if(( mapping.getProperty( "target" ).equals( "client" ) && mapping.getProperty( "create-stubs" ).equals( "true" )) 
                        || field.getModifier() == ClassData.Modifier.PUBLIC ) {
                    if( field.getType().isPrimitive()) {
                        serialization += mapping.getRegistry().getTypeSerializer( field.getType()).
                                toStream( mapping , field.getType(), stream, beanInstanceName + "." + field.getName()) + "\n";
                    } else {
                        serialization += "writeObject(" + stream + ", " + beanInstanceName + "." + field.getName() + id + ");\n";
                    }
                } else {
                    if( field.getType().isPrimitive()) {
                        serialization += mapping.getRegistry().getTypeSerializer( field.getType()).
                                toStream( mapping , field.getType(), stream, beanInstanceName + "." + getGetter( field ) + "()" ) + "\n";
                    } else {                        
                        serialization += "writeObject(" + stream + ", " + beanInstanceName + "." + getGetter( field ) + "()" + id + ");\n";
                    }
                }
            }
            return serialization;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            String beanInstanceName = "b_" + type.getFullyQualifiedName().replace( ".", "_" );
            String deserialization = type.getFullyQualifiedName() + " " + beanInstanceName + 
                    " = new " + type.getFullyQualifiedName() + "();\n";
//            for( FieldData field : type.getFields()) {
            for( FieldData field : type.getAllFields()) {
                if(( mapping.getProperty( "target" ).equals( "client" ) && mapping.getProperty( "create-stubs" ).equals( "true" )) 
                        || field.getModifier() == ClassData.Modifier.PUBLIC ) {
                    if( field.getType().isPrimitive() && !field.getType().isArray()) {
                        deserialization += beanInstanceName + "." + field.getName() + " = " + 
                                field.getType().getSerializer().fromStream( mapping , field.getType(), stream, null ) + ";\n";
                    } else {
                        deserialization += beanInstanceName + "." + field.getName() + " = (" + 
                                field.getType().getSerializer().instanceOf( mapping, field.getType()) + ") readObject(" + stream + ");\n";
                    }
                } else {
                    if( field.getType().isPrimitive() && !field.getType().isArray()) {
                        deserialization += beanInstanceName + "." + getSetter( field ) + "(" + mapping.getRegistry().getTypeSerializer( field.getType()).
                                fromStream( mapping , field.getType(), stream, null ) + ");\n";
                    } else {
                        deserialization += beanInstanceName + "." + getSetter( field ) + 
                                "((" + field.getType().getSerializer().instanceOf( mapping, field.getType()) + ") readObject(" + stream + "));\n";
                    }
                }
            }
            deserialization += ( object == null ? "" :  object + " = " + beanInstanceName + ";\n" );
            return deserialization;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }
    
    private String getSetter( FieldData field ) {
        return "set" + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 );
    }
    
    private String getGetter( FieldData field ) {        
        return (field.getType().getClassName().toLowerCase().equals("boolean") ? "get" : "is") + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 ); //NOI18N
    }
    
    public Set<ClassData> getReferencedTypes( ClassData rootClassData, Set<ClassData> usedTypes ) {
        Set<ClassData> result = new HashSet<ClassData>();
        result.add( rootClassData );
        usedTypes.add( rootClassData );
        ClassData parent = rootClassData.getParent();
        if( parent != null && parent.getSerializer() != null && !usedTypes.contains( parent )) {
            usedTypes.addAll( parent.getSerializer().getReferencedTypes( parent, usedTypes ));
        }
        for( FieldData fieldCD : rootClassData.getFields()) {
            ClassData cd = fieldCD.getType();
            if( !usedTypes.contains( cd )) {
                result.addAll( cd.getSerializer().getReferencedTypes( cd, usedTypes ));
                usedTypes.add( cd );
            }
        }
        return result;
    }    
}
