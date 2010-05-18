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
 */package org.netbeans.modules.mobility.e2e.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
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
public class GenericTypeSerializer implements JavonSerializer {

    public GenericTypeSerializer() {
//        System.err.println("Creating GenericTypeSerializer");
    }

    public String getName() {
        return "Generic type serializer";
    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( type.getKind() == TypeKind.DECLARED ) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            String classFQN = clazz.getQualifiedName().toString();
            if( "java.util.List".equals( classFQN ) || "java.util.Vector".equals( classFQN ) || 
                    "java.util.Stack".equals( classFQN )) 
            {
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();
                if( typeParams.size() == 0 ) return false; // Only parametrized lists are supported
                if( typeParams.size() != 1 ) return false; // Only one parameter is supported
                for( TypeMirror typeParam : typeParams ) {
                    TypeElement paramElement = (TypeElement)((DeclaredType) typeParam).asElement();
                    if( paramElement.getQualifiedName().toString().startsWith( "java.util" )) {
                        return false;
                    }                    
                    if( !traversable.isTypeSupported( typeParam, typeCache )) {
                        return false;
                    }
                }
                return true;
            } else if( "java.util.Hashtable".equals( classFQN )) {
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();
                if( typeParams.size() == 0 ) return false; // Only parametrized lists are supported
                if( typeParams.size() != 2 ) return false; // Only two parameters supported
                for( TypeMirror typeParam : typeParams ) {
                    TypeElement paramElement = (TypeElement)((DeclaredType) typeParam).asElement();
                    if( paramElement.getQualifiedName().toString().startsWith( "java.util" )) {
                        return false;
                    }                    
                    if( !traversable.isTypeSupported( typeParam, typeCache )) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( isTypeSupported( traversable, type, typeCache )) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            String classFQN = clazz.getQualifiedName().toString();
            if( "java.util.List".equals( classFQN )) {
                ClassData cd = new ClassData( "java.util", "List", false, false, this );
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();                
                List<ClassData> typeParameters = new ArrayList<ClassData>();
                for( TypeMirror typeParam : typeParams ) {
                    ClassData cdp = traversable.traverseType( typeParam, typeCache );
                    if( cdp == null ) return null;
                    typeParameters.add( cdp );
                }
                cd.setParameterTypes( typeParameters );                
                for( ClassData ccd : typeParameters ) {
//                    System.err.println(" registering type : " + ccd.getFullyQualifiedName());
                    traversable.registerType( ccd );
                }
                return cd;
            } else if( "java.util.Vector".equals( classFQN )) {
                ClassData cd = new ClassData( "java.util", "Vector", false, false, this );
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();                
                List<ClassData> typeParameters = new ArrayList<ClassData>();
                for( TypeMirror typeParam : typeParams ) {
                    ClassData cdp = traversable.traverseType( typeParam, typeCache );
                    if( cdp == null ) return null;
                    typeParameters.add( cdp );
                }
                cd.setParameterTypes( typeParameters );                
                for( ClassData ccd : typeParameters ) {
//                    System.err.println(" registering type : " + ccd.getFullyQualifiedName());
                    traversable.registerType( ccd );
                }
                return cd;
            } else if( "java.util.Stack".equals( classFQN )) {
                ClassData cd = new ClassData( "java.util", "Stack", false, false, this );
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();                
                List<ClassData> typeParameters = new ArrayList<ClassData>();
                for( TypeMirror typeParam : typeParams ) {
                    ClassData cdp = traversable.traverseType( typeParam, typeCache );
                    if( cdp == null ) return null;
                    typeParameters.add( cdp );
                }
                cd.setParameterTypes( typeParameters );                
                for( ClassData ccd : typeParameters ) {
                    traversable.registerType( ccd );
                }
                return cd;
            } else if( "java.util.Hashtable".equals( classFQN )) {
                ClassData cd = new ClassData( "java.util", "Hashtable", false, false, this );
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();                
                List<ClassData> typeParameters = new ArrayList<ClassData>();
                for( TypeMirror typeParam : typeParams ) {
                    ClassData cdp = traversable.traverseType( typeParam, typeCache );
                    if( cdp == null ) return null;
                    typeParameters.add( cdp );
                }
                cd.setParameterTypes( typeParameters );                
                for( ClassData ccd : typeParameters ) {
//                    System.err.println(" registering type : " + ccd.getFullyQualifiedName());
                    traversable.registerType( ccd );
                }
                return cd;
            }
        }
        return null;
    }

    public String instanceOf( JavonMapping mapping, ClassData type  ) {
        // List has only one parameter
        ClassData parameterType = type.getParameterTypes().get( 0 );
        String typeFQN = type.getFullyQualifiedName();
        if( "java.util.List".equals( typeFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                return parameterType.getSerializer().instanceOf( mapping, parameterType ) + "[]";
            } else {
                return type.getName() + "<" + parameterType.getSerializer().instanceOf( mapping, parameterType ) + ">";
            }
        } else if( "java.util.Vector".equals( typeFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                return type.getName();
            } else {
                return type.getName() + "<" + parameterType.getSerializer().instanceOf( mapping, parameterType ) + ">";
            }
        } else if( "java.util.Stack".equals( typeFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                return type.getName();
            } else {
                return type.getName() + "<" + parameterType.getSerializer().instanceOf( mapping, parameterType ) + ">";
            }
        } else if( "java.util.Hashtable".equals( typeFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                return type.getName();
            } else {
                return type.getName() + "<" + parameterType.getSerializer().instanceOf( mapping, parameterType ) + 
                        "," + type.getParameterTypes().get( 1 ).getSerializer().instanceOf( mapping, type.getParameterTypes().get( 1 )) + ">";
            }
        }
        throw new IllegalArgumentException( "Invalid type: " + typeFQN );
    }

    public String toObject( JavonMapping mapping, ClassData type, String variable  ) {
        return variable;
    }

    public String fromObject( JavonMapping mapping, ClassData type, String object  ) {
        return "(" + type.getSerializer().instanceOf( mapping, type ) + ")" + object;
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        ClassData parameterType = type.getParameterTypes().get( 0 );
        int id = mapping.getRegistry().getRegisteredTypeId( type );
        String genericsType = type.getSerializer().instanceOf( mapping, type );
        String classFQN = type.getFullyQualifiedName();
        if( "java.util.List".equals( classFQN )) {            
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String serializationCode = "";
                serializationCode += genericsType + " g_" + id + " = (" + genericsType + ") o;\n";
                serializationCode += "out.writeInt( g_" + id + ".length );\n";
                serializationCode += "for( int i = 0; i < g_" + id + ".length; i++ ) {\n";
                serializationCode += "writeObject( out, g_" + id + "[i], " + 
                        mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "}";
                return serializationCode;
            } else {
                String serializationCode = "";            
                serializationCode += "@SuppressWarnings( \"unchecked\" )\n";
                serializationCode +=  genericsType + " g_" + id + " = (" + genericsType + ") o;\n";
                serializationCode += "output.writeInt( g_" + id + ".size());\n";
                serializationCode += "for( " + parameterType.getSerializer().instanceOf( mapping, parameterType ) + 
                        " g : g_" + id + " ) {\n";
                serializationCode += "writeObject( output, g, " + mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "}";
                return serializationCode;
            }
        } else if( "java.util.Vector".equals( classFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String serializationCode = "";
                serializationCode += "java.util.Vector g_" + id + " = (java.util.Vector) o;\n";
                serializationCode += "out.writeInt( g_" + id + ".size());\n";
                serializationCode += "for( int i = 0; i < g_" + id + ".size(); i++ ) {\n";
                serializationCode += "writeObject( out, g_" + id + ".elementAt( i ), " + 
                        mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "}";
                return serializationCode;
            } else {                
                String serializationCode = "";
                serializationCode += "@SuppressWarnings( \"unchecked\" )\n";
                serializationCode +=  genericsType + " g_" + id + " = (" + genericsType + ") o;\n";
                serializationCode += "output.writeInt( g_" + id + ".size());\n";
                serializationCode += "for( " + parameterType.getSerializer().instanceOf( mapping, parameterType ) + 
                        " g : g_" + id + " ) {\n";
                serializationCode += "writeObject( output, g, " + mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "}";
                return serializationCode;
            }
        } else if( "java.util.Stack".equals( classFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String serializationCode = "";
                serializationCode += "java.util.Stack g_" + id + " = (java.util.Stack) o;\n";
                serializationCode += "out.writeInt( g_" + id + ".size());\n";
                serializationCode += "for( int i = 0; i < g_" + id + ".size(); i++ ) {\n";
                serializationCode += "writeObject( out, g_" + id + ".elementAt( i ), " + 
                        mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "}";
                return serializationCode;
            } else {                
                String serializationCode = "";
                serializationCode += "@SuppressWarnings( \"unchecked\" )\n";
                serializationCode +=  genericsType + " g_" + id + " = (" + genericsType + ") o;\n";
                serializationCode += "output.writeInt( g_" + id + ".size());\n";
                serializationCode += "for( " + parameterType.getSerializer().instanceOf( mapping, parameterType ) + 
                        " g : g_" + id + " ) {\n";
                serializationCode += "writeObject( output, g, " + mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "}";
                return serializationCode;
            }
        } else if( "java.util.Hashtable".equals( classFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String keyType = parameterType.getSerializer().instanceOf( mapping, parameterType );
                String serializationCode = "";
                serializationCode += "java.util.Hashtable g_" + id + " = (java.util.Hashtable) o;\n";
                serializationCode += "out.writeInt( g_" + id + ".size());\n";
                serializationCode += "for( Enumeration en = g_" + id + ".keys(); en.hasMoreElements(); ) {\n";
                serializationCode += keyType + " key = (" + keyType + ") en.nextElement();\n";
                serializationCode += "writeObject( out, key, " + mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "writeObject( out, g_" + id + ".get( key ), " + 
                        mapping.getRegistry().getRegisteredTypeId( type.getParameterTypes().get( 1 )) + " );\n";                
                serializationCode += "}";
                return serializationCode;
            } else {                
                String serializationCode = "";
                serializationCode += "@SuppressWarnings( \"unchecked\" )\n";
                serializationCode +=  genericsType + " g_" + id + " = (" + genericsType + ") o;\n";
                serializationCode += "output.writeInt( g_" + id + ".size());\n";
                serializationCode += "for( " + parameterType.getSerializer().instanceOf( mapping, parameterType ) + 
                        " key : g_" + id + ".keySet()) {\n";
                serializationCode += "writeObject( output, key, " + mapping.getRegistry().getRegisteredTypeId( parameterType ) + " );\n";
                serializationCode += "writeObject( output, g_" + id + ".get( key ), " + 
                        mapping.getRegistry().getRegisteredTypeId( type.getParameterTypes().get( 1 )) + " );\n";
                serializationCode += "}";
                return serializationCode;
            }
        }
        return "";
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        ClassData parameterType = type.getParameterTypes().get( 0 );
        int id = mapping.getRegistry().getRegisteredTypeId( type );
        String genericsType = type.getSerializer().instanceOf( mapping, type );
        String classFQN = type.getFullyQualifiedName();
        if( "java.util.List".equals( classFQN )) {            
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new " + 
                        parameterType.getSerializer().instanceOf( mapping, parameterType ) + "[g_" + id + "_length];\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result[i] = (" +
                        parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in );\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            } else {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new ArrayList<" + 
                        parameterType.getSerializer().instanceOf( mapping, parameterType ) + ">();\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result.add((" + parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in ));\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            }
        } else if( "java.util.Vector".equals( classFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new " + genericsType + "();\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result.addElement((" +
                        parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in ));\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            } else {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new " + genericsType + "();\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result.addElement((" + parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in ));\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            }
        } else if( "java.util.Stack".equals( classFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new " + genericsType + "();\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result.addElement((" +
                        parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in ));\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            } else {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new " + genericsType + "();\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result.addElement((" + parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in ));\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            }
        } else if( "java.util.Hashtable".equals( classFQN )) {
            if( JavonMapping.CLIENT.equals( mapping.getProperty( JavonMapping.TARGET ))) {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new " + genericsType + "();\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result.put((" +
                        parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in ), (" + 
                        type.getParameterTypes().get( 1 ).getSerializer().instanceOf( mapping, type.getParameterTypes().get( 1 )) + ") readObject( in ));\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            } else {
                String serializationCode = "";
                serializationCode += "int g_" + id + "_length = in.readInt();\n";
                serializationCode += genericsType + " g_" + id + "_result = new " + genericsType + "();\n";
                serializationCode += "for( int i = 0; i < g_" + id + "_length; i++ ) {\n";
                serializationCode += "g_" + id + "_result.put((" + 
                        parameterType.getSerializer().instanceOf( mapping, parameterType ) + ") readObject( in ), (" + 
                        type.getParameterTypes().get( 1 ).getSerializer().instanceOf( mapping, type.getParameterTypes().get( 1 )) + ") readObject( in ));\n";
                serializationCode += "}\n";
                serializationCode += "result = g_" + id + "_result;";
                return serializationCode;
            }
        }
        throw new IllegalArgumentException( "Invalid type: " + genericsType );
    }

    public Set<ClassData> getReferencedTypes( ClassData rootClassData, Set<ClassData> usedTypes ) {
        Set<ClassData> result = new HashSet<ClassData>();
        result.add( rootClassData );
        usedTypes.add( rootClassData );
        for( ClassData cd : rootClassData.getParameterTypes()) {
            result.addAll( cd.getSerializer().getReferencedTypes( cd, usedTypes ));
            usedTypes.add( cd );
        }
        return result;
    }
}
