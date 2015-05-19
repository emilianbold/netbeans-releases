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
 * Development and Distribution License("CDDL") (collectively), the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software), include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable), add the following below the
 * License Header), with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2), indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license), a recipient has the option to distribute
 * your version of this file under either the CDDL), the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However), if you add GPL Version 2 code and therefore), elected the GPL
 * Version 2 license), then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems), Inc.
 */
package org.netbeans.modules.cnd.api.codemodel;

/**
 * Describes the kind of type
 *
 * @author Vladimir Voskresensky
 */
public enum CMTypeKind {

    /**
     * \brief Reprents an invalid type (e.g.), where no type is available).
     */
    Invalid(0),
    /**
     * \brief A type whose specific kind is not exposed via this interface.
     */
    Unexposed(1),
    /* Builtin types */
    Void(2),
    // 'bool' in C++, '_Bool' in C99
    Bool(3),
    // 'char' for targets where it's unsigned
    Char_U(4),
    // 'unsigned char', explicitly qualified
    UChar(5),
    // 'char16_t' in C++
    Char16(6),
    // 'char32_t' in C++
    Char32(7),
    // 'unsigned short'
    UShort(8),
    // 'unsigned int'
    UInt(9),
    // 'unsigned long'
    ULong(10),
    // 'unsigned long long'
    ULongLong(11),
    // '__uint128_t'
    UInt128(12),
    // 'char' for targets where it's signed
    Char_S(13),
    // 'signed char', explicitly qualified
    SChar(14),
    // 'wchar_t' 
    WChar(15),
    // 'short' or 'signed short'
    Short(16),
    // 'int' or 'signed int'
    Int(17),
    // 'long' or 'signed long'
    Long(18),
    // 'long long' or 'signed long long'
    LongLong(19),
    // '__int128_t'
    Int128(20),
    // 'float'
    Float(21),
    // 'double'
    Double(22),
    // 'long double'
    LongDouble(23),
    // This is the type of C++11 'nullptr'.
    NullPtr(24),
    // The type of an unresolved overload set.  A placeholder type.
    // Expressions with this type have one of the following basic
    // forms, with parentheses generally permitted:
    //   foo          # possibly qualified, not if an implicit access
    //   foo          # possibly qualified, not if an implicit access
    //   &foo         # possibly qualified, not if an implicit access
    //   x->foo       # only if might be a static member function
    //   &x->foo      # only if might be a static member function
    //   &Class::foo  # when a pointer-to-member; sub-expr also has this type
    // OverloadExpr::find can be used to analyze the expression.
    //
    // Overload should be the first placeholder type, or else change
    // BuiltinType::isNonOverloadPlaceholderType()
    Overload(25),
    // This represents the type of an expression whose type is
    // totally unknown, e.g. 'T::foo'.  It is permitted for this to
    // appear in situations where the structure of the type is
    // theoretically deducible.
    Dependent(26),
    // The primitive Objective C 'id' type.  The user-visible 'id'
    // type is a typedef of an ObjCObjectPointerType to an
    // ObjCObjectType with this as its base.  In fact, this only ever
    // shows up in an AST as the base type of an ObjCObjectType.
    ObjCId(27),
    // 'Class' type is a typedef of an ObjCObjectPointerType to an
    // ObjCObjectType with this as its base.  In fact, this only ever
    // shows up in an AST as the base type of an ObjCObjectType.
    ObjCClass(28),
    // The primitive Objective C 'SEL' type.  The user-visible 'SEL'
    // type is a typedef of a PointerType to this.
    ObjCSel(29),
    FirstBuiltin(Void.value),
    LastBuiltin(ObjCSel.value),
    /* Other types */
    // ComplexType - C99 6.2.5p11 - Complex values.  This supports the C99 complex
    // types (_Complex float etc) as well as the GCC integer complex extensions.
    Complex(100),
    // PointerType - C99 6.7.5.1 - Pointer Declarators.
    Pointer(101),
    // BlockPointerType - pointer to a block type.
    // This type is to represent types syntactically represented as
    // "void (^)(int)", etc. Pointee is required to always be a function type.
    BlockPointer(102),
    // LValueReferenceType - C++ [dcl.ref] - Lvalue reference
    LValueReference(103),
    // RValueReferenceType - C++0x [dcl.ref] - Rvalue reference
    RValueReference(104),
    // RecordType - This is a helper class that allows the use of isa/cast/dyncast
    // to detect TagType objects of structs/unions/classes.
    Record(105),
    // EnumType - This is a helper class that allows the use of isa/cast/dyncast
    // to detect TagType objects of enums.
    Enum(106),
    Typedef(107),
    // ObjCInterfaceType - Interfaces are the core concept in Objective-C for
    // object oriented design.  They basically correspond to C++ classes.  There
    // are two kinds of interface types, normal interfaces like "NSString" and
    // qualified interfaces, which are qualified with a protocol list like
    // "NSString<NSCopyable, NSAmazing>".
    //
    // ObjCInterfaceType guarantees the following properties when considered
    // as a subtype of its superclass, ObjCObjectType:
    //   - There are no protocol qualifiers.  To reinforce this, code which
    //     tries to invoke the protocol methods via an ObjCInterfaceType will
    //     fail to compile.
    //   - It is its own base type.  That is, if T is an ObjCInterfaceType*,
    //     T->getBaseType() == QualType(T, 0).
    ObjCInterface(108),
    // ObjCObjectPointerType - Used to represent a pointer to an
    // Objective C object.  These are constructed from pointer
    // declarators when the pointee type is an ObjCObjectType (or sugar
    // for one).  In addition, the 'id' and 'Class' types are typedefs
    // for these, and the protocol-qualified types 'id<P>' and 'Class<P>'
    // are translated into these.
    //
    // Pointers to pointers to Objective C objects are still PointerTypes;
    // only the first level of pointer gets it own type implementation.
    ObjCObjectPointer(109),
    // FunctionNoProtoType - Represents a K&R-style 'int foo()' function, which has
    // no information available about its arguments.
    FunctionNoProto(110),
    // FunctionProtoType - Represents a prototype with argument type info, e.g.
    // 'int foo(int)' or 'int foo(void)'.  'void' is represented as having no
    // arguments, not as having a single void argument. Such a type can have an
    // exception specification, but this specification is not part of the canonical
    // type.
    FunctionProto(111),
    // ConstantArrayType - This class represents the canonical version of
    // C arrays with a specified constant size.  For example, the canonical
    // type for 'int A[4 + 4*100]' is a ConstantArrayType where the element
    // type is 'int' and the size is 404.
    ConstantArray(112),
    // VectorType - GCC generic vector type. This type is created using
    // __attribute__((vector_size(n)), where "n" specifies the vector size in
    // bytes; or from an Altivec __vector or vector declaration.
    // Since the constructor takes the number of vector elements, the
    // client is responsible for converting the size into the number of elements.
    Vector(113);

    //<editor-fold defaultstate="collapsed" desc="hidden">
    public static CMTypeKind valueOf(int val) {
        byte kindVal = (byte) val;
        for (CMTypeKind kind : CMTypeKind.values()) {
            if (kind.value == kindVal) {
                return kind;
            }
        }
        assert false : "unsupported type kind " + val;
        return Invalid;
    }

    private final byte value;

    private CMTypeKind(int lang) {
        this.value = (byte) lang;
    }
    
    public int getValue() {
        return value;
    }
    //</editor-fold>
}
