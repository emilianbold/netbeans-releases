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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
 * Serializer for primitive types but without float or double
 * 
 * <p>Supported types are:<br>
 * boolean, byte, char, int, short, String
 * 
 * @author Michal Skvor
 */
public class PrimitiveTypeSerializer implements JavonSerializer {
    
    private final ClassData voidClassData        = new ClassData( "", "void", true, false , this);
    private final ClassData booleanClassData     = new ClassData( "", "boolean", true, false, this );
    private final ClassData BooleanClassData     = new ClassData( "java.lang", "Boolean", false, false, this );
    private final ClassData byteClassData        = new ClassData( "", "byte", true, false, this );
    private final ClassData ByteClassData        = new ClassData( "java.lang", "Byte", false, false, this );
    private final ClassData charClassData        = new ClassData( "", "char", true, false, this );
    private final ClassData CharClassData        = new ClassData( "java.lang", "Character", false, false, this );
    private final ClassData intClassData         = new ClassData( "", "int", true, false, this );
    private final ClassData IntClassData         = new ClassData( "java.lang", "Integer", false, false, this );
    private final ClassData longClassData        = new ClassData( "", "long", true, false, this );
    private final ClassData LongClassData        = new ClassData( "java.lang", "Long", false, false, this );
    private final ClassData shortClassData       = new ClassData( "", "short", true, false, this );
    private final ClassData ShortClassData       = new ClassData( "java.lang", "Short", false, false, this );
    private final ClassData stringClassData      = new ClassData( "java.lang", "String", false, false, this );


    /** Creates a new instance of PrimitiveTypeSerializer */
    public PrimitiveTypeSerializer() {

    }
    
    public String getName() {
        return "Primitive type serializer";
    }

    public boolean isTypeSupported( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.VOID == type.getKind()) {
            return true;
        } else if( TypeKind.BOOLEAN == type.getKind()) {
            return true;
        } else if( TypeKind.BYTE == type.getKind()) {
            return true;
        } else if( TypeKind.CHAR == type.getKind()) {
            return true;
        } else if( TypeKind.INT == type.getKind()) {
            return true;
        } else if( TypeKind.LONG == type.getKind()) {
            return true;
        } else if( TypeKind.SHORT == type.getKind()) {
            return true;
        } else if( TypeKind.DECLARED == type.getKind()) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            String classFullQualifiedName = clazz.getQualifiedName().toString();
            if( "java.lang.String".equals( classFullQualifiedName )) {
                return true;
            } else if( "java.lang.Boolean".equals( classFullQualifiedName )) {
                return true;
            } else if( "java.lang.Byte".equals( classFullQualifiedName )) {
                return true;
            } else if( "java.lang.Character".equals( classFullQualifiedName )) {
                return true;
            } else if( "java.lang.Integer".equals( classFullQualifiedName )) {
                return true;
            } else if( "java.lang.Long".equals( classFullQualifiedName )) {
                return true;
            } else if( "java.lang.Short".equals( classFullQualifiedName )) {
                return true;
            }
        }
        
        return false;
    }
    
    public ClassData getType( Traversable traversable, TypeMirror type, Map<String, ClassData> typeCache ) {
        if( TypeKind.VOID == type.getKind()) {
            return voidClassData;
        } else if( TypeKind.BOOLEAN == type.getKind()) {
            return booleanClassData;
        } else if( TypeKind.BYTE == type.getKind()) {
            return byteClassData;
        } else if( TypeKind.CHAR == type.getKind()) {
            return charClassData;
        } else if( TypeKind.INT == type.getKind()) {
            return intClassData;
        } else if( TypeKind.LONG == type.getKind()) {
            return longClassData;
        } else if( TypeKind.SHORT == type.getKind()) {
            return shortClassData;
        } else if( TypeKind.DECLARED == type.getKind()) {
            TypeElement clazz = (TypeElement)((DeclaredType) type).asElement();
            String classFullQualifiedName = clazz.getQualifiedName().toString();
            if( "java.lang.String".equals( classFullQualifiedName )) {
                return stringClassData;
            } else if( "java.lang.Boolean".equals( classFullQualifiedName )) {
                return BooleanClassData;
            } else if( "java.lang.Byte".equals( classFullQualifiedName )) {
                return ByteClassData;
            } else if( "java.lang.Character".equals( classFullQualifiedName )) {
                return CharClassData;
            } else if( "java.lang.Integer".equals( classFullQualifiedName )) {
                return IntClassData;
            } else if( "java.lang.Long".equals( classFullQualifiedName )) {
                return LongClassData;
            } else if( "java.lang.Short".equals( classFullQualifiedName )) {
                return ShortClassData;
            }
        }

        return null;
    }

    public String instanceOf( JavonMapping mapping, ClassData type  ) {
        if( booleanClassData.equals( type )) { 
            return "boolean";
        } else if( BooleanClassData.equals( type )) {
            return "Boolean";
        } else if( byteClassData.equals( type )) {
            return "byte";
        } else if( ByteClassData.equals( type )) {
            return "Byte";
        } else if( charClassData.equals( type )) { 
            return "char";
        } else if( CharClassData.equals( type )) {
            return "Character";
        } else if( intClassData.equals( type )) {
            return "int";
        } else if( IntClassData.equals( type )) {
            return "Integer";
        } else if( longClassData.equals( type )) { 
            return "long";
        } else if( LongClassData.equals( type )) {
            return "Long";
        } else if( shortClassData.equals( type )) {
            return "short";
        } else if( ShortClassData.equals( type )) {
            return "Short";
        } else if( stringClassData.equals( type )) {
            return "String";
        } else if( voidClassData.equals( type )) {
            return "void";
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());
    }
    
    public String toObject( JavonMapping mapping, ClassData type, String variable  ) {
        if( booleanClassData.equals( type )) {
            return "new Boolean(" + variable + ")";
        } else if( byteClassData.equals( type )) {
            return "new Byte(" + variable + ")";
        } else if( charClassData.equals( type )) {
            return "new Character(" + variable + ")";
        } else if( intClassData.equals( type )) {
            return "new Integer(" + variable + ")";
        } else if( longClassData.equals( type )) {
            return "new Long(" + variable + ")";
        } else if( shortClassData.equals( type )) {
            return "new Short(" + variable + ")";
        } else if( stringClassData.equals( type )) {
            return "(String)" + variable;
        } else if( voidClassData.equals( type )) {
            return "void";
        } else if( BooleanClassData.equals( type )) {
            return "(Boolean)" + variable;
        } else if( ByteClassData.equals( type )) {
            return "(Byte)" + variable;
        } else if( CharClassData.equals( type )) {
            return "(Character)" + variable;
        } else if( IntClassData.equals( type )) {
            return "(Integer)" + variable;
        } else if( LongClassData.equals( type )) {
            return "(Long)" + variable;
        } else if( ShortClassData.equals( type )) {
            return "(Short)" + variable;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromObject( JavonMapping mapping, ClassData type, String object) {
        if( booleanClassData.equals( type )) {
            return "((Boolean)" + object + ").booleanValue()";
        } else if( byteClassData.equals( type )) {
            return "((Byte)" + object + ").byteValue()";
        } else if( charClassData.equals( type )) {
            return "((Character)" + object + ").charValue()";
        } else if( intClassData.equals( type )) {
            return "((Integer)" + object + ").intValue()";
        } else if( longClassData.equals( type )) {
            return "((Long)" + object + ").longValue()";
        } else if( shortClassData.equals( type )) {
            return "((Short)" + object + ").shortValue()";
        } else if( stringClassData.equals( type )) {
            return "(String)" + object;
        } else if( voidClassData.equals( type )) {
            return "void";
        } else if( BooleanClassData.equals( type )) {
            return "(Boolean)" + object;
        } else if( ByteClassData.equals( type )) {
            return "(Byte)" + object;
        } else if( CharClassData.equals( type )) {
            return "(Character)" + object;
        } else if( IntClassData.equals( type )) {
            return "(Integer)" + object;
        } else if( LongClassData.equals( type )) {
            return "(Long)" + object;
        } else if( ShortClassData.equals( type )) {
            return "(Short)" + object;
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String toStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        if( booleanClassData.equals( type )) {
            return stream + ".writeBoolean(" + object + ");";
        } else if( byteClassData.equals( type )) {
            return stream + ".writeByte(" + object + ");";
        } else if( charClassData.equals( type )) {
            return stream + ".writeChar(" + object + ");";
        } else if( intClassData.equals( type )) {
            return stream + ".writeInt(" + object + ");";
        } else if( longClassData.equals( type )) {
            return stream + ".writeLong(" + object + ");";
        } else if( shortClassData.equals( type )) {
            return stream + ".writeShort(" + object + ");";
        } else if( stringClassData.equals( type )) {
            return stream + ".writeUTF(" + fromObject( mapping, type, object  ) + ");";
        } else if( voidClassData.equals( type )) {
            throw new IllegalArgumentException("Void object is not serializable");//return stream + ".writeShort(" + fromObject( type, object ) + ");";
        } else if( BooleanClassData.equals( type )) {
            return stream + ".writeBoolean(" + fromObject( mapping, booleanClassData, object  ) + ");";
        } else if( ByteClassData.equals( type )) {
            return stream + ".writeByte(" + fromObject( mapping, byteClassData, object  ) + ");";
        } else if( CharClassData.equals( type )) {
            return stream + ".writeChar(" + fromObject( mapping, charClassData, object  ) + ");";
        } else if( IntClassData.equals( type )) {
            return stream + ".writeInt(" + fromObject( mapping, intClassData, object  ) + ");";
        } else if( LongClassData.equals( type )) {
            return stream + ".writeLong(" + fromObject( mapping, longClassData, object  ) + ");";
        } else if( ShortClassData.equals( type )) {
            return stream + ".writeShort(" + fromObject( mapping, shortClassData, object  ) + ");";
        }
        throw new IllegalArgumentException( "Invalid type: " + type.getName());        
    }

    public String fromStream( JavonMapping mapping, ClassData type, String stream, String object ) {
        String result = "";
        if( object != null ) result = object + " = ";
        if( voidClassData.equals( type )) {
            return object + " = _;";
            
        } else if(booleanClassData.equals( type )) {
            result += stream + ".readBoolean()";
        } else if( BooleanClassData.equals( type )) {
            result += toObject( mapping, booleanClassData, stream + ".readBoolean()"  );
        } else if( byteClassData.equals( type )) {
            result += stream + ".readByte()";
        } else if( ByteClassData.equals( type )) {
            result += toObject( mapping, byteClassData, stream + ".readByte()"  );
        } else if( charClassData.equals( type )) {
            result += stream + ".readChar()";
        } else if( CharClassData.equals( type )) {
            result += toObject( mapping, charClassData, stream + ".readChar()"  );
        } else if( intClassData.equals( type )) {
            result += stream + ".readInt()";
        } else if( IntClassData.equals( type )) {
            result += toObject( mapping, intClassData, stream + ".readInt()"  );
        } else if( longClassData.equals( type )) {
            result += stream + ".readLong()";
        } else if( LongClassData.equals( type )) {
            result += toObject( mapping, longClassData, stream + ".readLong()"  );
        } else if( shortClassData.equals( type )) {
            result += stream + ".readShort()";
        } else if( ShortClassData.equals( type )) {
            result += toObject( mapping, shortClassData, stream + ".readShort()"  );
        } else if( stringClassData.equals( type )) {
            result += stream + ".readUTF()";
        }
        if( "".equals( result ))
            throw new IllegalArgumentException( "Invalid type: " + type.getName());
        
        if( object != null ) result += ";";
        
        return result;
    }
    
    public Set<ClassData> getReferencedTypes( ClassData rootClassData, Set<ClassData> usedTypes ) {
        return Collections.singleton( rootClassData );
    }    
}
