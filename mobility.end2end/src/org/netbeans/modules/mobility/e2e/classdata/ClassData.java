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

package org.netbeans.modules.mobility.e2e.classdata;

import org.netbeans.modules.mobility.javon.JavonSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;

/**
 *
 * @author Michal Skvor, Jirka Prazak
 */
public class ClassData {
    
    private String packageName;
    private String className;
    private boolean primitive;
    private boolean array;
    private boolean generics;

    private ClassData parent;
    private JavonSerializer supportingSerializer=null;
    
    private List<FieldData> fields = new ArrayList<FieldData>();
    private List<MethodData> methods = new ArrayList<MethodData>();

    private ClassData componentType;
    private List<ClassData> typeParameters = new ArrayList<ClassData>();
    
    private List<MethodData> myInvalidMethods;
    private Map<String,String> myInvalidFields ;
    
    public static final ClassData java_lang_Object = new ClassData( "java.lang", "Object", false, false );
    
    private ClassData( String packageName, String className, boolean primitive, boolean array ) {
        this.packageName = packageName;
        this.className = className;
        this.primitive = primitive;
        this.array = array;

        parent = java_lang_Object;
        myInvalidFields = new HashMap<String, String>();
    }

    public ClassData( ClassData cd) {
        this.packageName=cd.getPackage();
        this.className=cd.getName();
        this.primitive=cd.isPrimitive();
        this.array=cd.isArray();
        this.generics=cd.getParameterTypes().isEmpty() ? true : false;
        this.componentType=cd.getComponentType();
        this.parent=cd.getParent();
        this.supportingSerializer=cd.getSerializer();
        this.fields=cd.getFields();
        this.methods=cd.getMethods();
        this.typeParameters=cd.getParameterTypes();
        myInvalidFields = new HashMap<String, String>();
    }

    public ClassData( String packageName, String className, boolean primitive, boolean array, JavonSerializer serializer ) {
        this( packageName, className, primitive, array);

        this.generics = false;
        this.supportingSerializer=serializer;
    }

    public ClassData( String packageName, String className, boolean array, List<FieldData> fields, List<MethodData> methods, JavonSerializer serializer )
    {
        this( packageName, className, false, array, serializer );
        this.generics = false;
        this.fields = fields;
        this.methods = methods;
    }
    
    public ClassData( String packageName, String className, boolean array, 
            List<FieldData> fields, List<MethodData> methods, 
            JavonSerializer serializer , List<MethodData> invalidMethods )
    {
        this( packageName, className, array, fields, methods , serializer ); 
        myInvalidMethods = invalidMethods;
    }

    public ClassData( String packageName, String className, boolean array, List<ClassData> typeParams, JavonSerializer serializer ) {
        this( packageName, className, false, array, serializer);
        this.generics=true;
        this.typeParameters=typeParams;
    }

    public ClassData( String packageName, String className, boolean array, List<FieldData> fields, List<MethodData> methods, List<ClassData> typeParams,
                      JavonSerializer serializer) {
        this( packageName, className, array, fields, methods, serializer );
        this.generics = true;
        this.typeParameters=typeParams;
    }

    public String getPackage() {
        return packageName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getName() {
        int arrayDepth = 0;
        ClassData t = this;
        while( t.isArray()) {
            t = t.getComponentType();
            arrayDepth++;
        }
        String arrayBrackets = "";
        if( arrayDepth > 0 ) {
            for( int i = 0; i < arrayDepth; i++ ) {
                arrayBrackets += "[]";
            }
        }
        return className + arrayBrackets;
    }
    
    /**
     * Return fully qualified name of the ClassData
     * 
     * @return fully qualified name
     */
    public String getFullyQualifiedName() {
        if( packageName == "" ) {
            return getName();
        }
        return packageName + "." + getName();
    }
    
    public void setParent( ClassData parent ) {
        this.parent = parent;
    }
    
    public ClassData getParent() {
        return parent;
    }

    /**
     * Returns true when the ClassData structure represents
     * primitive type
     *
     * @return true when the ClassData structure represents primitive type
     */
    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isArray() {
        return array;
    }

    public void setComponentType( ClassData type ) {
        this.componentType = type;
    }
    
    public ClassData getComponentType() {
        return componentType;
    }

    public void addField( FieldData field ) {
        fields.add( field );
    }

    public List<FieldData> getFields() {
        return Collections.unmodifiableList( fields );
    }

    public List<FieldData> getAllFields() {
        List<FieldData> f = new ArrayList<FieldData>();
        ClassData p = this;
        while( p != null ) {
            f.addAll( p.getFields());
            p = p.getParent();
        }
        return Collections.unmodifiableList( f );
    }
    
    public void addMethod( MethodData method ) {
        methods.add( method );
    }

    public List<MethodData> getMethods() {
        return Collections.unmodifiableList( methods );
    }
    
    public List<MethodData> getInvalidMethods() {
        if ( myInvalidMethods == null ) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList( myInvalidMethods );
    }
    

    public void addInvalidField( VariableElement e ) {
        String name = e.getSimpleName().toString();
        String type = e.asType().toString();
        myInvalidFields.put( name , type );
    }
    
    public Map<String,String> getInvaidFields(){
        return new HashMap<String, String>( myInvalidFields );
    }

    /**
     * Returns all parameter type specified for this class
     *
     * @return List of all parameter types, EmptyList if no parameter types are specified
     */
    public List<ClassData> getParameterTypes() {
        return Collections.unmodifiableList( typeParameters );
    }
    
    public void setParameterTypes( List<ClassData> parameters ) {
        typeParameters = parameters;
    }

    public String toString() {
        String result = getName();
        if( typeParameters.size() > 0 ) {
            result += "<";
            for( Iterator<ClassData> it = typeParameters.iterator(); it.hasNext();  ) {
                result += it.next().toString();
                if( it.hasNext()) result += ", ";
            }
            result += ">";
        }
        if( fields.size() > 0 ) {
            result += "[";
            for( FieldData field : fields ) {
                result += field.getType().toString() + ", ";
            }
            result += "]";                
        }
        return result;
    }    
    
    public static enum Modifier {
        PUBLIC, PRIVATE
    }

    public JavonSerializer getSerializer() {
        return this.supportingSerializer;
    }

    
    @Override
    public boolean equals( Object o ) {
        if( o instanceof ClassData ) {
            ClassData cd = (ClassData) o;
            if( !getFullyQualifiedName().equals( cd.getFullyQualifiedName())) return false;
            if( primitive != cd.isPrimitive()) return false;
            if( array != cd.isArray()) return false;
            if( typeParameters.size() != cd.getParameterTypes().size()) return false;
            for( int i = 0; i < typeParameters.size(); i++ ) {
                if( !typeParameters.get( i ).equals( cd.getParameterTypes().get( i ))) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return packageName.hashCode() * 37 +
            className.hashCode() * 37 +
            ( primitive ? 7 : 3 ) + 
            ( array ? 7 : 3 );
    }
}
