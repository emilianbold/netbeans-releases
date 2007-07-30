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
            String packageName = "";
            if( shortName.length() != classFullQualifiedName.length()) {
                int fqnLength = classFullQualifiedName.length();
                int shortLength = shortName.length();
                packageName = classFullQualifiedName.substring( fqnLength - shortLength - 1 );
            }
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
            
            cd = new ClassData( packageName, shortName, false, false, false, this );
            beanTypes.put( classFullQualifiedName, cd );
            typeCache.put( classFullQualifiedName, cd );
            
            // Get fields
            for( VariableElement e : ElementFilter.fieldsIn( clazz.getEnclosedElements())) {
                // Skip static declarations
                if( e.getModifiers().contains( Modifier.STATIC )) continue;
                // Skip final declarations
                if( e.getModifiers().contains( Modifier.FINAL )) continue;
                
                ClassData fieldClass = traversable.traverseType( e.asType(), typeCache );
                String methodPartName = e.getSimpleName().toString().substring( 0, 1 ).toUpperCase() + 
                        e.getSimpleName().toString().substring( 1 );
                String setterName = "set" + methodPartName;
                String getterName = ( e.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get" ) + methodPartName;
                FieldData field = new FieldData( e.getSimpleName().toString(), fieldClass );
                
                if( e.getModifiers().contains( Modifier.PRIVATE ) || e.getModifiers().contains( Modifier.PROTECTED )) {
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
                    if( hasGetter && hasSetter ) cd.addField( field );
                } else if( e.getModifiers().contains( Modifier.PUBLIC )) {
                    field.setModifier( ClassData.Modifier.PUBLIC );
                    cd.addField( field );
                }
            }
                        
            // Support only java.lang.Object supertypes            
            if( clazz.getSuperclass().getKind() == TypeKind.DECLARED ) {
                TypeElement superclass = (TypeElement)((DeclaredType) clazz.getSuperclass()).asElement();
                if( !"java.lang.Object".equals(superclass.getQualifiedName().toString())) {
                    return null;
                }
//                ClassData superType = traversable.traverseType( superclass.asType(), typeCache );
//                if( superType != null ) {
//                    traversable.registerType( superType, this );
//                    cd.setParent( superType );
//                }
            } 
                        
            return cd;
        }
        
        return null;
    }
        
    public String instanceOf( ClassData type ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            return type.getFullyQualifiedName();
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }
    
    public String toObject( ClassData type, String variable ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            return "(" + type.getFullyQualifiedName() + ")";
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromObject( ClassData type, String object ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            return "(" + type.getFullyQualifiedName() + ")" + object;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( beanTypes.get( type.getFullyQualifiedName()) != null ) {
            String serialization = "";
            String beanInstanceName = "b_" + type.getFullyQualifiedName().replace( ".", "_" );
            serialization += type.getFullyQualifiedName() + " " + beanInstanceName + " = (" + type.getFullyQualifiedName() + ")" + object + ";\n";
            for( FieldData field : type.getFields()) {
                String id = "";
                if( mapping.getProperty( "target" ).equals( "client" )) {
                    id = ", " + mapping.getRegistry().getRegisteredTypeId( field.getType());
                }
                if( mapping.getProperty( "target" ).equals( "client" ) && mapping.getProperty( "create-stubs" ).equals( "true" )) {
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
            for( FieldData field : type.getFields()) {
                if( mapping.getProperty( "target" ).equals( "client" ) && mapping.getProperty( "create-stubs" ).equals( "true" )) {
                    if( field.getType().isPrimitive()) {
                        deserialization += beanInstanceName + " = " + mapping.getRegistry().getTypeSerializer( field.getType()).
                                fromStream( mapping , field.getType(), stream, null ) + "\n";
                    } else {
                        deserialization += beanInstanceName + " = (" + field.getType().getFullyQualifiedName() + ") readObject(" + stream + ");\n";
                    }
                } else {
                    if( field.getType().isPrimitive()) {
                        deserialization += beanInstanceName + "." + getSetter( field ) + "(" + mapping.getRegistry().getTypeSerializer( field.getType()).
                                fromStream( mapping , field.getType(), stream, null ) + ");\n";
                    } else {
                        deserialization += beanInstanceName + "." + getSetter( field ) + "((" + field.getType().getFullyQualifiedName() + ") readObject(" + stream + "));\n";
                    }
                }
            }
            return deserialization;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }
    
    private String getSetter( FieldData field ) {
        return "set" + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 );
    }
    
    private String getGetter( FieldData field ) {
        return "get" + field.getName().substring( 0, 1 ).toUpperCase() + field.getName().substring( 1 );
    }
}
