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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mobility.e2e.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        System.err.println("Creating GenericTypeSerializer");
    }

    public String getName() {
        return "Generic type serializer";
    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        System.err.println("~" + type.toString());
        if( type.getKind() == TypeKind.DECLARED ) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            System.err.println(" - declared type: " + clazz.getQualifiedName().toString());
            if( "java.util.List".equals( clazz.getQualifiedName().toString())) {
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();
                boolean areParametersSupported = true;
                for( TypeMirror typeParam : typeParams ) {
                    if( !traversable.isTypeSupported( typeParam, typeCache )) {
                        areParametersSupported = false;
                        break;
                    }
                }
                if( areParametersSupported ) return true;
            }
        }
        return false;
    }

    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( isTypeSupported( traversable, type, typeCache )) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            if( "java.util.List".equals( clazz.getQualifiedName().toString())) {
                ClassData cd = new ClassData( "java.util", "List", false, false, this );
                List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();
                List<ClassData> typeParameters = new ArrayList<ClassData>();
                for( TypeMirror typeParam : typeParams ) {
                    ClassData cdp = traversable.traverseType( typeParam, typeCache );
                    typeParameters.add( cdp );
                }
                cd.setParameterTypes( typeParameters );
                
                return cd;
            }
        }
        return null;
    }

    public String instanceOf( ClassData type ) {        
        return type.getName() + "<" + type.getParameterTypes().get( 0 ).getName() + ">";
    }

    public String toObject( ClassData type, String variable ) {
        return "";
    }

    public String fromObject( ClassData type, String object ) {
        return "";
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( "client".equals( mapping.getProperty( "target" ))) {
            return type.getParameterTypes().get( 0 ).getName() + "[]";
        } else {
            return type.getName() + "<" + type.getParameterTypes().get( 0 ).getName() + ">";
        }
//        return "";
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( "client".equals( mapping.getProperty( "target" ))) {
            return type.getParameterTypes().get( 0 ).getName() + "[]";
        } else {
            return type.getName() + "<" + type.getParameterTypes().get( 0 ).getName() + ">";
        }
    }

}
