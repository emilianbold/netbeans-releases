/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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
package org.netbeans.modules.mobility.end2end.profiles.alljava;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.Traversable;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.FieldData;
import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ArrayType;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bohemius
 */
public class AllJavaSerializer implements JavonSerializer {
    
    public String getName() {
        return "All Java Serializer";//NOI18N
    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        return true;
    }

    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if ( TypeKind.DECLARED == type.getKind() ) {
            return getDeclaredType( traversable, type, typeCache );
        } /*else if ( TypeKind.ARRAY == type.getKind() ) {
            return getArrayType( type, typeCache );
        }*/

        return null;
    }

    private ClassData getDeclaredType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        assert type.getKind()==TypeKind.DECLARED;
        
        TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
        String clsName = clazz.getSimpleName().toString();
        String fqName = clazz.getQualifiedName().toString();
        String pkgName = "";

        if ( clsName.length() != fqName.length())
            pkgName = fqName.substring( 0, fqName.lastIndexOf( '.' ));

        if ( typeCache.containsKey( fqName ) ) {
            return typeCache.get( fqName );
        //add the new class info to the type cache, including its methods and fields
        } else {
            ClassData clsData = new ClassData( pkgName, clsName, false, false, this );
            typeCache.put( fqName, clsData );

            List<ClassData> typeParameters = new ArrayList<ClassData>();
            List<? extends TypeMirror> typeParams = ((DeclaredType) type).getTypeArguments();                
            for( TypeMirror typeParam : typeParams ) {
                ClassData cdp = traversable.traverseType( typeParam, typeCache );
                if( cdp != null ) 
                    typeParameters.add( cdp );
            }
            clsData.setParameterTypes( typeParameters );
            return clsData;
        }
    }

    //TODO this will likely be erased and not used when the ClassDataRegistry is fixed
    private ClassData getArrayType( TypeMirror type, Map<String, ClassData> typeCache ) {
        assert type.getKind()==TypeKind.ARRAY;

        ArrayType array = (ArrayType) type;
        TypeMirror componentType = array.getComponentType();

        if (componentType.getKind()==TypeKind.ARRAY)
            getArrayType( componentType, typeCache);
        else {
            TypeElement componentClazz= (TypeElement) ( ( DeclaredType) type ).asElement();
            String componentClsName=componentClazz.getSimpleName().toString();
            String componentFqName=componentClazz.getQualifiedName().toString();
            String pkgName = "";

            if (componentClsName.length()!= componentFqName.length())
                pkgName=componentFqName.substring( 0, componentFqName.lastIndexOf( '.' ) );

            if (typeCache.containsKey( componentFqName)) {
                ClassData component=typeCache.get( componentFqName);
                if (component.isArray())
                    return component;
            } else {
                ClassData result=new ClassData (pkgName, componentClsName, false, false, this);
                typeCache.put( componentFqName, result);
                return result;
            }
        }
        return null;
    }

    public String instanceOf( JavonMapping mapping,ClassData type  ) {
        return type.getFullyQualifiedName();
    }

    public String toObject( JavonMapping mapping,ClassData type, String variable  ) {
        return null;
    }

    public String fromObject( JavonMapping mapping,ClassData type, String object  ) {
        return null;
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        return null;
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        return null;
    }

    private String displayClassData( ClassData clsData ) {
        StringBuffer result = new StringBuffer( clsData.getFullyQualifiedName() + "\n\n" );

        for ( FieldData fe : clsData.getFields() ) {
            result.append( fe.getModifier() + " " + fe.getType() + " " + fe.getName() + "\n" );
        }

        result.append( "\n" );
        for ( MethodData me : clsData.getMethods() ) {
            result.append( me.getReturnType() + " " + me.getName() + "(" );
            int i = 0;
            for ( MethodParameter mp : me.getParameters() ) {
                result.append( mp.getType().getFullyQualifiedName() + " " + mp.getName() );
                if ( i == me.getParameters().size() - 1 )
                    result.append( "," );
                else
                    result.append( ")\n" );
            }
        }
        return result.toString();
    }

    public Set<ClassData> getReferencedTypes( ClassData rootClassData, Set<ClassData> usedTypes ) {
        return Collections.singleton( rootClassData );
    }
}



