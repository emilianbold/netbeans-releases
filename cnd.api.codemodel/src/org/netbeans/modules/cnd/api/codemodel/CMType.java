/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel;

import org.netbeans.modules.cnd.spi.codemodel.CMTypeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;

/**
 * Type information for CMCursors.
 *
 * @author Vladimir Voskresensky
 */
public final class CMType {

    /**
     * Describes the kind of type.
     *
     * @return kind of type
     */
    public CMTypeKind getKind() {
        return impl.getKind();
    }

    /**
     * \brief Pretty-print the underlying type using the rules of the language
     * of the translation unit from which it came.
     *
     * @return pretty-printed spelling, If the type is invalid, an empty string
     * is returned.
     */
    public CharSequence getSpelling() {
        return impl.getSpelling();
    }

    /**
     * \brief Return the canonical type for a CMType.
     *
     * Type system explicitly models typedefs and all the ways a specific type
     * can be represented. The canonical type is the underlying type with all
     * the "sugar" removed. For example, if 'T' is a typedef for 'int', the
     * canonical type for 'T' would be 'int'.
     *
     * @return the canonical type
     */
    public CMType getCanonical() {
        return CMType.fromImpl(impl.getCanonical());
    }

    /**
     * \brief Determine whether a CMType has the "const" qualifier set, without
     * looking through typedefs that may have added "const" at a different
     * level.
     *
     * @return true for "const" qualified type
     */
    public boolean isConstQualified() {
        return impl.isConstQualified();
    }

    /**
     * \brief Determine whether a CMType has the "volatile" qualifier set,
     * without looking through typedefs that may have added "volatile" at a
     * different level.
     *
     * @return true for "volatile" qualified type
     */
    public boolean isVolatileQualified() {
        return impl.isVolatileQualified();
    }

    /**
     * \brief Determine whether a CMType has the "restrict" qualifier set,
     * without looking through typedefs that may have added "restrict" at a
     * different level.
     *
     * @return true for "restrict" qualified type
     */
    public boolean isRestrictQualified() {
        return impl.isRestrictQualified();
    }

    /**
     * \brief For pointer types, returns the type of the pointee.
     *
     * @return pointee type or Invalid
     */
    public CMType getPointeeType() {
        return CMType.fromImpl(impl.getPointeeType());
    }

    /**
     * \brief Return the cursor for the declaration of the given type.
     *
     * @return cursor for the declaration of the given type
     */
    public CMCursor getDeclaration() {
        return CMCursor.fromImpl(impl.getDeclaration());
    }

    /**
     * \brief Retrieve the result type associated with a function type.
     *
     * If a non-function type is passed in, an invalid type is returned.
     *
     * @return return type or invalid
     */
    public CMType getFunctionResultType() {
        return CMType.fromImpl(impl.getFunctionResultType());
    }

    /**
     * \brief Return true if the CMType is a variadic function type, and false
     * otherwise.
     *
     * @return true for variadic functions, false otherwise
     */
    public boolean isVariadicFunction() {
        return impl.isVariadicFunction();
    }

    /**
     * \brief Retrieve the number of non-variadic arguments associated with a
     * function type.
     *
     * If a non-function type is passed in, -1 is returned.
     *
     * @return number of non-variadic arguments or -1
     */
    public int getFunctionArgumentsCount() {
        return impl.getArgumentsCount();
    }

    /**
     * \brief Retrieve the types of arguments of a function type.
     *
     * If a non-function type is passed in or the function does not have enough
     * parameters, an invalid type is returned.
     *
     * @return types of arguments of a function type
     */
    public Iterable<CMType> getFunctionArguments() {
        return CMType.fromImpls(impl.getFunctionArguments());
    }

    /**
     * \brief Return true if the CMType is a POD (plain old data) type, and
     * false otherwise.
     *
     * <code>
     * POD can not have
     * - user-declared constructors
     * - private or protected non-static data members
     * - base classes
     * - virtual functions
     * - non-static data members of non-POD type
     * - user-defined copy assignment operator
     * - user-defined destructor
     *
     * They can have pointers and methods
     * </code>
     *
     * @return true for POD type, false otherwise
     */
    public boolean isPODType() {
        return impl.isPODType();
    }

    /**
     * \brief Return the element type of an array type.
     *
     * If a non-array type is passed in, an invalid type is returned.
     *
     * @return element type of an array or invalid
     * @see #getElementType
     */
    public CMType getArrayElementType() {
        return CMType.fromImpl(impl.getArrayElementType());
    }

    /**
     * \brief Return the array size of a constant array.
     *
     * If a non-array type is passed in, -1 is returned.
     *
     * @return size or -1
     * @see #getElementsSize()
     */
    public long getArraySize() {
        return impl.getArraySize();
    }

    /**
     * \brief Return the element type of an array, complex, or vector type.
     *
     * If a type is passed in that is not an array, complex, or vector type, an
     * invalid type is returned.
     *
     * @return element type of an array, complex, or vector type or invalid
     * @see #getArrayElementType()
     */
    public CMType getElementType() {
        return CMType.fromImpl(impl.getElementType());
    }

    /**
     * \brief Return the number of elements of an array or vector type.
     *
     * If a type is passed in that is not an array or vector type, -1 is
     * returned.
     *
     * @return size or -1
     * @see #getArraySize()
     */
    public long getElementsSize() {
        return impl.getElementsSize();
    }

    /**
     * \brief layout value and list the possible error codes for \c getSizeOf,
     * \c getAlignOf and \c getOffsetOf.
     *
     * A value of negative constants for errors can be returned if the target
     * type is not a valid argument to sizeof, alignof or offsetof.
     */
    public static final class TypeLayout {

        /**
         * \brief Type is of kind CXType_Invalid.
         */
        public static final TypeLayout Invalid = new TypeLayout(-1);
        /**
         * \brief The type is an incomplete Type.
         */
        public static final TypeLayout Incomplete = new TypeLayout(-2);
        /**
         * \brief The type is a dependent Type.
         */
        public static final TypeLayout Dependent = new TypeLayout(-3);
        /**
         * \brief The type is not a constant size type.
         */
        public static final TypeLayout NotConstantSize = new TypeLayout(-4);
        /**
         * \brief The Field name is not valid for this record.
         */
        public static final TypeLayout InvalidFieldName = new TypeLayout(-5);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static TypeLayout valueOf(long val) {
            if (val < 0) {
                switch ((int) val) {
                    case -1:
                        return Invalid;
                    case -2:
                        return Incomplete;
                    case -3:
                        return Dependent;
                    case -4:
                        return NotConstantSize;
                    case -5:
                        return InvalidFieldName;
                    default:
                        assert false : "unexpected invalid value " + val;
                        return Invalid;
                }
            } else {
                return new TypeLayout(val);
            }
        }

        private final long value;

        private TypeLayout(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + (int) (this.value ^ (this.value >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeLayout) {
                TypeLayout other = (TypeLayout) obj;
                return value == other.value;
            }
            return false;
        }
        //</editor-fold>
    }

    /**
     * \brief Return the alignment of a type in bytes as per C++[expr.alignof]
     * standard.
     *
     * <code>
     * If the type declaration is invalid, TypeLayout.Invalid is returned.
     * If the type declaration is an incomplete type, TypeLayout.Incomplete
     *   is returned.
     * If the type declaration is a dependent type, TypeLayout.Dependent is
     *   returned.
     * If the type declaration is not a constant size type,
     *   TypeLayout.NotConstantSize is returned.
     * </code>
     *
     * @return alignof value or one of errors
     */
    public TypeLayout getAlignOf() {
        return impl.getAlignOf();
    }

    /**
     * \brief Return the size of a type in bytes as per C++[expr.sizeof]
     * standard.
     *
     * <code>
     * If the type declaration is invalid, TypeLayout.Invalid is returned.
     * If the type declaration is an incomplete type, TypeLayout.Incomplete
     *   is returned.
     * If the type declaration is a dependent type, TypeLayout.Dependent is
     *   returned.
     * </code>
     *
     * @return sizeof value or one of errors
     */
    public TypeLayout getSizeOf() {
        return impl.getSizeOf();
    }

    /**
     * \brief Return the offset of a field named S in a record of type T in bits
     * as it would be returned by __offsetof__ as per C++11[18.2p4]
     *
     * If the cursor is not a record field declaration, TypeLayout.Invalid is
     * returned.
     * <code>
     * If the field's type declaration is an incomplete type,
     *   TypeLayout.Incomplete is returned.
     * If the field's type declaration is a dependent type,
     *   TypeLayout.Dependent is returned.
     * If the field's name S is not found,
     *   TypeLayout.InvalidFieldName is returned.
     * </code>
     *
     * @param field field name in this record type
     * @return offsetof value (in bits) or one of erorrs
     */
    public TypeLayout getOffsetOf(CharSequence field) {
        return impl.getOffsetOf(field);
    }

    //<editor-fold defaultstate="collapsed" desc="CallingConvention Kinds">
    /**
     * \brief Describes the calling convention of a function type
     */
    public enum CallingConvention {

        Default(0),
        C(1),
        X86StdCall(2),
        X86FastCall(3),
        X86ThisCall(4),
        X86Pascal(5),
        AAPCS(6),
        AAPCS_VFP(7),
        PnaclCall(8),
        IntelOclBicc(9),
        Invalid(100),
        Unexposed(200);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static CallingConvention valueOf(int val) {
            short kindVal = (short) val;
            for (CallingConvention kind : CallingConvention.values()) {
                if (kind.value == kindVal) {
                    return kind;
                }
            }
            assert false : "unsupported CallingConvention kind " + val;
            return Invalid;
        }

        private final short value;

        private CallingConvention(int lang) {
            this.value = (short) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>
    }
    //</editor-fold>

    /**
     * \brief Retrieve the calling convention associated with a function type.
     *
     * @return calling convention associated with a function type. If a
     * non-function type is passed in, CallingConvention.Invalid is returned.
     */
    public CallingConvention getCallingConvention() {
        return impl.getCallingConvention();
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMTypeImplementation impl;

    private CMType(CMTypeImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMType fromImpl(CMTypeImplementation impl) {
        // TODO: share instance for the same impl if needed
        return new CMType(impl);
    }

    /*package*/
    static Iterable<CMType> fromImpls(Iterable<CMTypeImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMTypeImplementation getImpl() {
        return impl;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.impl.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof CMType) {
            return this.impl.equals(((CMType) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return "CMType{" + impl + '}'; // NOI18N
    }

    private static final IterableFactory.Converter<CMTypeImplementation, CMType> CONV
            = new IterableFactory.Converter<CMTypeImplementation, CMType>() {

                @Override
                public CMType convert(CMTypeImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
