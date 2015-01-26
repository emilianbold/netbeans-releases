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
package org.netbeans.modules.cnd.spi.codemodel;

import org.netbeans.modules.cnd.api.codemodel.CMType;
import org.netbeans.modules.cnd.api.codemodel.CMTypeKind;

/**
 * Type information for CMCursors.
 *
 * @author Vladimir Voskresensky
 */
public interface CMTypeImplementation {

    /**
     * Describes the kind of type.
     *
     * @return kind of type
     */
    CMTypeKind getKind();

    /**
     * \brief Pretty-print the underlying type using the rules of the language
     * of the translation unit from which it came.
     *
     * @return pretty-printed spelling, If the type is invalid, an empty string
     * is returned.
     */
    CharSequence getSpelling();

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
    CMTypeImplementation getCanonical();

    /**
     * \brief Determine whether a CMType has the "const" qualifier set, without
     * looking through typedefs that may have added "const" at a different
     * level.
     *
     * @return true for "const" qualified type
     */
    boolean isConstQualified();

    /**
     * \brief Determine whether a CMType has the "volatile" qualifier set,
     * without looking through typedefs that may have added "volatile" at a
     * different level.
     *
     * @return true for "volatile" qualified type
     */
    boolean isVolatileQualified();

    /**
     * \brief Determine whether a CMType has the "restrict" qualifier set,
     * without looking through typedefs that may have added "restrict" at a
     * different level.
     *
     * @return true for "restrict" qualified type
     */
    boolean isRestrictQualified();

    /**
     * \brief For pointer types, returns the type of the pointee.
     *
     * @return pointee type or Invalid
     */
    CMTypeImplementation getPointeeType();

    /**
     * \brief Return the cursor for the declaration of the given type.
     *
     * @return cursor for the declaration of the given type
     */
    CMCursorImplementation getDeclaration();

    /**
     * \brief Retrieve the result type associated with a function type.
     *
     * If a non-function type is passed in, an invalid type is returned.
     *
     * @return return type or invalid
     */
    CMTypeImplementation getFunctionResultType();

    /**
     * \brief Return true if the CMType is a variadic function type, and false
     * otherwise.
     *
     * @return true for variadic functions, false otherwise
     */
    boolean isVariadicFunction();

    /**
     * \brief Retrieve the number of non-variadic arguments associated with a
     * function type.
     *
     * If a non-function type is passed in, -1 is returned.
     *
     * @return number of non-variadic arguments or -1
     */
    int getArgumentsCount();

    /**
     * \brief Retrieve the types of arguments of a function type.
     *
     * If a non-function type is passed in or the function does not have enough
     * parameters, an invalid type is returned.
     *
     * @return types of arguments of a function type
     */
    Iterable<CMTypeImplementation> getFunctionArguments();

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
    boolean isPODType();

    /**
     * \brief Return the element type of an array type.
     *
     * If a non-array type is passed in, an invalid type is returned.
     *
     * @return element type of an array or invalid
     * @see #getElementType
     */
    CMTypeImplementation getArrayElementType();

    /**
     * \brief Return the array size of a constant array.
     *
     * If a non-array type is passed in, -1 is returned.
     *
     * @return size or -1
     * @see #getElementsSize()
     */
    long getArraySize();

    /**
     * \brief Return the element type of an array, complex, or vector type.
     *
     * If a type is passed in that is not an array, complex, or vector type, an
     * invalid type is returned.
     *
     * @return element type of an array, complex, or vector type or invalid
     * @see #getArrayElementType()
     */
    CMTypeImplementation getElementType();

    /**
     * \brief Return the number of elements of an array or vector type.
     *
     * If a type is passed in that is not an array or vector type, -1 is
     * returned.
     *
     * @return size or -1
     * @see #getArraySize()
     */
    long getElementsSize();

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
    CMType.TypeLayout getAlignOf();

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
    CMType.TypeLayout getSizeOf();

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
    CMType.TypeLayout getOffsetOf(CharSequence field);

    /**
     * \brief Retrieve the calling convention associated with a function type.
     *
     * @return calling convention associated with a function type. If a
     * non-function type is passed in, CallingConvention.Invalid is returned.
     */
    CMType.CallingConvention getCallingConvention();

    /**
     * \brief Determine whether two CXTypes represent the same type.
     *
     * @param other type to compare with
     * @return true if the other type represent the same type and false
     * otherwise.
     */
    @Override
    boolean equals(Object/*CMTypeImplementation*/ other);

    @Override
    int hashCode();
}
