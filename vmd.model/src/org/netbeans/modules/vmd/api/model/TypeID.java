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
package org.netbeans.modules.vmd.api.model;

/**
 * This immutable class represents a type of objects/components in the model. Each object/component has it unique type id.
 * It is similar to "Class.getName ()" in J2SE.
 * <p>
 * It holds information about kind, string, and dimension. Kind describes whether it is a primitive (any type that is NOT taken
 * as a component in the model), component (any type that IS taken as a component in the model), or an enum.
 * The string is any identification string of the type indepentant on kind and dimension.
 * The dimension is dimension of type similar to arrays in J2SE.
 * <p>
 * This class could be presented as a String too because it has unique 1-1 mapping with it.
 *
 * @author David Kaspar
 */
public final class TypeID {

    private static final char PRIMITIVE_ID = 'P'; // NOI18N
    private static final char ENUM_ID = 'E'; // NOI18N
    private static final char COMPONENT_ID = 'C'; // NOI18N

    /**
     * The type id kind.
     */
    public enum Kind {

        /**
         * Any type that is NOT taken as a component in the model.
         */
        PRIMITIVE,
        /**
         * An enum type.
         */
        ENUM,

        /**
         * Any type that IS taken as a component in the model.
         */
        COMPONENT
    }

    private String encoded;

    private Kind kind;
    private String string;
    private int dimension;

    private TypeID (String string) {
        decode (string);
        this.encoded = string;
    }

    /**
     * Creates a new instance by specifying kind and string. Dimension is 0 = no array.
     * @param kind the kind
     * @param string the string
     */
    public TypeID (Kind kind, String string) {
        this (kind, string, 0);
    }

    /**
     * Creates a new instance by specifying kind, string, dimension.
     * @param kind the kind
     * @param string the string
     * @param dimension the dimension (0 = no array, 1 = one-dimensional array, ...)
     */
    public TypeID (Kind kind, String string, int dimension) {
        this.kind = kind;
        this.string = string;
        this.dimension = dimension;
        encoded = encode ();
    }

    /**
     * Returns a kind.
     * @return the kind
     */
    public Kind getKind () {
        return kind;
    }

    /**
     * Returns an identification string.
     * @return the string
     */
    public String getString () {
        return string;
    }

    /**
     * Return a type dimension.
     * @return the dimension
     */
    public int getDimension () {
        return dimension;
    }

    /**
     * Returns encoded string representation of the type id.
     * @return the encoded string
     */
    public String getEncoded () {
        return encoded;
    }

    private void decode (String string) {
        assert string != null  &&  string.length () >= 1;
        int pos = 0;

        dimension = 0;
        for (;;) {
            assert pos < string.length ();
            char c = string.charAt (pos);
            if (c < '0'  ||  c > '9') // NOI18N
                break;
            dimension = (dimension * 10) + (c - '0'); // NOI18N
            pos ++;
        }

        assert pos < string.length ();
        switch (string.charAt (pos)) {
            case PRIMITIVE_ID:
                kind = Kind.PRIMITIVE;
                break;
            case ENUM_ID:
                kind = Kind.ENUM;
                break;
            case COMPONENT_ID:
                kind = Kind.COMPONENT;
        }
        pos ++;

        this.string = string.substring (pos);
    }

    private String encode () {
        StringBuilder buffer = new StringBuilder ();
        if (dimension > 0)
        buffer.append (dimension);
        switch (kind) {
            case PRIMITIVE:
                buffer.append (PRIMITIVE_ID);
                break;
            case ENUM:
                buffer.append (ENUM_ID);
                break;
            case COMPONENT:
                buffer.append (COMPONENT_ID);
                break;
        }
        return buffer.append (string).toString ();
    }

    /**
     * Returns a component type id of this type id - similar to "Class.getComponentType ()".
     * <p>
     * Note: This type id must be an array.
     * @return the component type id.
     */
    public TypeID getComponentType () {
        assert dimension > 0;
        return new TypeID (kind, string, dimension - 1);
    }

    /**
     * Returns an array type id of this type id - opposite to "TypeID.getComponentType" method.
     * @return the array type id
     */
    public TypeID getArrayType () {
        return new TypeID (kind, string, dimension + 1);
    }

    /**
     * Returns whether the encoded string representation of this type id equals to the one from the specified typeid.
     * @param o the compared type id
     * @return true if equals
     */
    @Override
    public boolean equals (Object o) {
        return o != null  &&  getClass () == o.getClass ()  &&  encoded.equals (((TypeID) o).encoded);
    }

    /**
     * Returns a hash code of the type id.
     * @return the hash code
     */
    @Override
    public int hashCode () {
        return encoded.hashCode ();
    }

    /**
     * Returns encoded string representation of this type id.
     * @return the string
     */
    @Override
    public String toString () {
        return encoded;
    }

    /**
     * Creates a new instance from an encoded string representation of type id.
     * @param string the encoded string
     */
    public static TypeID createFrom (String string) {
        return string != null ? new TypeID (string) : null;
    }

}
