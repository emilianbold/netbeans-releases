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

/**
 * \brief Describes the kind of entity that a cursor refers to.
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public enum CMCursorKind {
  /* Declarations */
  /**
   * \brief A declaration whose specific kind is not exposed via this
   * interface.
   *
   * Unexposed declarations have the same operations as any other kind
   * of declaration; one can extract their location information,
   * spelling, find their definitions, etc. However, the specific kind
   * of the declaration is not reported.
   */
  UnexposedDecl                 (1),
  /** \brief A C or C++ struct. */
  StructDecl                    (2),
  /** \brief A C or C++ union. */
  UnionDecl                     (3),
  /** \brief A C++ class. */
  ClassDecl                     (4),
  /** \brief An enumeration. */
  EnumDecl                      (5),
  /**
   * \brief A field (in C) or non-static data member (in C++) in a
   * struct, union, or C++ class.
   */
  FieldDecl                     (6),
  /** \brief An enumerator constant. */
  EnumConstantDecl              (7),
  /** \brief A function. */
  FunctionDecl                  (8),
  /** \brief A variable. */
  VarDecl                       (9),
  /** \brief A function or method parameter. */
  ParmDecl                      (10),
  /** \brief An Objective-C \@interface. */
  ObjCInterfaceDecl             (11),
  /** \brief An Objective-C \@interface for a category. */
  ObjCCategoryDecl              (12),
  /** \brief An Objective-C \@protocol declaration. */
  ObjCProtocolDecl              (13),
  /** \brief An Objective-C \@property declaration. */
  ObjCPropertyDecl              (14),
  /** \brief An Objective-C instance variable. */
  ObjCIvarDecl                  (15),
  /** \brief An Objective-C instance method. */
  ObjCInstanceMethodDecl        (16),
  /** \brief An Objective-C class method. */
  ObjCClassMethodDecl           (17),
  /** \brief An Objective-C \@implementation. */
  ObjCImplementationDecl        (18),
  /** \brief An Objective-C \@implementation for a category. */
  ObjCCategoryImplDecl          (19),
  /** \brief A typedef */
  TypedefDecl                   (20),
  /** \brief A C++ class method. */
  CXXMethod                     (21),
  /** \brief A C++ namespace. */
  Namespace                     (22),
  /** \brief A linkage specification, e.g. 'extern "C"'. */
  LinkageSpec                   (23),
  /** \brief A C++ constructor. */
  Constructor                   (24),
  /** \brief A C++ destructor. */
  Destructor                    (25),
  /** \brief A C++ conversion function. */
  ConversionFunction            (26),
  /** \brief A C++ template type parameter. */
  TemplateTypeParameter         (27),
  /** \brief A C++ non-type template parameter. */
  NonTypeTemplateParameter      (28),
  /** \brief A C++ template template parameter. */
  TemplateTemplateParameter     (29),
  /** \brief A C++ function template. */
  FunctionTemplate              (30),
  /** \brief A C++ class template. */
  ClassTemplate                 (31),
  /** \brief A C++ class template partial specialization. */
  ClassTemplatePartialSpecialization (32),
  /** \brief A C++ namespace alias declaration. */
  NamespaceAlias                (33),
  /** \brief A C++ using directive. */
  UsingDirective                (34),
  /** \brief A C++ using declaration. */
  UsingDeclaration              (35),
  /** \brief A C++ alias declaration */
  TypeAliasDecl                 (36),
  /** \brief An Objective-C \@synthesize definition. */
  ObjCSynthesizeDecl            (37),
  /** \brief An Objective-C \@dynamic definition. */
  ObjCDynamicDecl               (38),
  /** \brief An access specifier. */
  CXXAccessSpecifier            (39),

  FirstDecl                     (UnexposedDecl.value),
  LastDecl                      (CXXAccessSpecifier.value), 

  /* References */
  FirstRef                      (40), /* Decl references */
  ObjCSuperClassRef             (40),
  ObjCProtocolRef               (41),
  ObjCClassRef                  (42),
  /**
   * \brief A reference to a type declaration.
   *
   * A type reference occurs anywhere where a type is named but not
   * declared. For example, given:
   *
   * \code
   * typedef unsigned size_type;
   * size_type size;
   * \endcode
   *
   * The typedef is a declaration of size_type (TypedefDecl)),
   * while the type of the variable "size" is referenced. The cursor
   * referenced by the type of size is the typedef for size_type.
   */
  TypeRef                       (43),
  /**
   * base class specifier, i.e. class A : public Base {};
   */
  CXXBaseSpecifier              (44),
  /** 
   * \brief A reference to a class template, function template, template
   * template parameter, or class template partial specialization.
   */
  TemplateRef                   (45),
  /**
   * \brief A reference to a namespace or namespace alias.
   */
  NamespaceRef                  (46),
  /**
   * \brief A reference to a member of a struct, union, or class that occurs in 
   * some non-expression context, e.g., a designated initializer.
   */
  MemberRef                     (47),
  /**
   * \brief A reference to a labeled statement.
   *
   * This cursor kind is used to describe the jump to "start_over" in the 
   * goto statement in the following example:
   *
   * \code
   *   start_over:
   *     ++counter;
   *
   *     goto start_over;
   * \endcode
   *
   * A label reference cursor refers to a label statement.
   */
  LabelRef                      (48),
  
  /**
   * \brief A reference to a set of overloaded functions or function templates
   * that has not yet been resolved to a specific function or function template.
   *
   * An overloaded declaration reference cursor occurs in C++ templates where
   * a dependent name refers to a function. For example:
   *
   * \code
   * template<typename T> void swap(T&, T&);
   *
   * struct X { ... };
   * void swap(X&, X&);
   *
   * template<typename T>
   * void reverse(T* first, T* last) {
   *   while (first < last - 1) {
   *     swap(*first, *--last);
   *     ++first;
   *   }
   * }
   *
   * struct Y { };
   * void swap(Y&, Y&);
   * \endcode
   *
   * Here, the identifier "swap" is associated with an overloaded declaration
   * reference. In the template definition, "swap" refers to either of the two
   * "swap" functions declared above, so both results will be available. At
   * instantiation time, "swap" may also refer to other functions found via
   * argument-dependent lookup (e.g., the "swap" function at the end of the
   * example).
   *
   * The functions \c clang_getNumOverloadedDecls() and 
   * \c clang_getOverloadedDecl() can be used to retrieve the definitions
   * referenced by this cursor.
   */
  OverloadedDeclRef             (49),
  
  /**
   * \brief A reference to a variable that occurs in some non-expression 
   * context, e.g., a C++ lambda capture list.
   */
  VariableRef                   (50),
  
  LastRef                       (VariableRef.value),

  /* Error conditions */
  FirstInvalid                  (70),
  InvalidFile                   (70),
  NoDeclFound                   (71),
  NotImplemented                (72),
  InvalidCode                   (73),
  LastInvalid                   (InvalidCode.value),

  /* Expressions */
  FirstExpr                     (100),

  /**
   * \brief An expression whose specific kind is not exposed via this
   * interface.
   *
   * Unexposed expressions have the same operations as any other kind
   * of expression; one can extract their location information),
   * spelling, children, etc. However, the specific kind of the
   * expression is not reported.
   */
  UnexposedExpr                 (100),

  /**
   * \brief An expression that refers to some value declaration, such
   * as a function, varible, or enumerator.
   */
  DeclRefExpr                   (101),

  /**
   * \brief An expression that refers to a member of a struct, union),
   * class, Objective-C class, etc.
   */
  MemberRefExpr                 (102),

  /** \brief An expression that calls a function. */
  CallExpr                      (103),

  /** \brief An expression that sends a message to an Objective-C
   object or class. */
  ObjCMessageExpr               (104),

  /** \brief An expression that represents a block literal. */
  BlockExpr                     (105),

  /** \brief An integer literal.
   */
  IntegerLiteral                (106),

  /** \brief A floating point number literal.
   */
  FloatingLiteral               (107),

  /** \brief An imaginary number literal.
   */
  ImaginaryLiteral              (108),

  /** \brief A string literal.
   */
  StringLiteral                 (109),

  /** \brief A character literal.
   */
  CharacterLiteral              (110),

  /** \brief A parenthesized expression, e.g. "(1)".
   *
   * This AST node is only formed if full location information is requested.
   */
  ParenExpr                     (111),

  /** \brief This represents the unary-expression's (except sizeof and
   * alignof).
   */
  UnaryOperator                 (112),

  /** \brief [C99 6.5.2.1] Array Subscripting.
   */
  ArraySubscriptExpr            (113),

  /** \brief A builtin binary operation expression such as "x + y" or
   * "x <(y".
   */
  BinaryOperator                (114),

  /** \brief Compound assignment such as "+(.
   */
  CompoundAssignOperator        (115),

  /** \brief The ?: ternary operator.
   */
  ConditionalOperator           (116),

  /** \brief An explicit cast in C (C99 6.5.4) or a C-style cast in C++
   * (C++ [expr.cast]), which uses the syntax (Type)expr.
   *
   * For example: (int)f.
   */
  CStyleCastExpr                (117),

  /** \brief [C99 6.5.2.5]
   */
  CompoundLiteralExpr           (118),

  /** \brief Describes an C or C++ initializer list.
   */
  InitListExpr                  (119),

  /** \brief The GNU address of label extension, representing &&label.
   */
  AddrLabelExpr                 (120),

  /** \brief This is the GNU Statement Expression extension: ({int X=4; X;})
   */
  StmtExpr                      (121),

  /** \brief Represents a C11 generic selection.
   */
  GenericSelectionExpr          (122),

  /** \brief Implements the GNU __null extension, which is a name for a null
   * pointer constant that has integral type (e.g., int or long) and is the same
   * size and alignment as a pointer.
   *
   * The __null extension is typically only used by system headers, which define
   * NULL as __null in C++ rather than using 0 (which is an integer that may not
   * match the size of a pointer).
   */
  GNUNullExpr                   (123),

  /** \brief C++'s static_cast<> expression.
   */
  CXXStaticCastExpr             (124),

  /** \brief C++'s dynamic_cast<> expression.
   */
  CXXDynamicCastExpr            (125),

  /** \brief C++'s reinterpret_cast<> expression.
   */
  CXXReinterpretCastExpr        (126),

  /** \brief C++'s const_cast<> expression.
   */
  CXXConstCastExpr              (127),

  /** \brief Represents an explicit C++ type conversion that uses "functional"
   * notion (C++ [expr.type.conv]).
   *
   * Example:
   * \code
   *   x (int(0.5);
   * \endcode
   */
  CXXFunctionalCastExpr         (128),

  /** \brief A C++ typeid expression (C++ [expr.typeid]).
   */
  CXXTypeidExpr                 (129),

  /** \brief [C++ 2.13.5] C++ Boolean Literal.
   */
  CXXBoolLiteralExpr            (130),

  /** \brief [C++0x 2.14.7] C++ Pointer Literal.
   */
  CXXNullPtrLiteralExpr         (131),

  /** \brief Represents the "this" expression in C++
   */
  CXXThisExpr                   (132),

  /** \brief [C++ 15] C++ Throw Expression.
   *
   * This handles 'throw' and 'throw' assignment-expression. When
   * assignment-expression isn't present, Op will be null.
   */
  CXXThrowExpr                  (133),

  /** \brief A new expression for memory allocation and constructor calls, e.g:
   * "new CXXNewExpr(foo)".
   */
  CXXNewExpr                    (134),

  /** \brief A delete expression for memory deallocation and destructor calls),
   * e.g. "delete[] pArray".
   */
  CXXDeleteExpr                 (135),

  /** \brief A unary expression.
   */
  UnaryExpr                     (136),

  /** \brief An Objective-C string literal i.e. @"foo".
   */
  ObjCStringLiteral             (137),

  /** \brief An Objective-C \@encode expression.
   */
  ObjCEncodeExpr                (138),

  /** \brief An Objective-C \@selector expression.
   */
  ObjCSelectorExpr              (139),

  /** \brief An Objective-C \@protocol expression.
   */
  ObjCProtocolExpr              (140),

  /** \brief An Objective-C "bridged" cast expression, which casts between
   * Objective-C pointers and C pointers, transferring ownership in the process.
   *
   * \code
   *   NSString *str ((__bridge_transfer NSString *)CFCreateString();
   * \endcode
   */
  ObjCBridgedCastExpr           (141),

  /** \brief Represents a C++0x pack expansion that produces a sequence of
   * expressions.
   *
   * A pack expansion expression contains a pattern (which itself is an
   * expression) followed by an ellipsis. For example:
   *
   * \code
   * template<typename F, typename ...Types>
   * void forward(F f, Types &&...args) {
   *  f(static_cast<Types&&>(args)...);
   * }
   * \endcode
   */
  PackExpansionExpr             (142),

  /** \brief Represents an expression that computes the length of a parameter
   * pack.
   *
   * \code
   * template<typename ...Types>
   * struct count {
   *   static const unsigned value (sizeof...(Types);
   * };
   * \endcode
   */
  SizeOfPackExpr                (143),

  /* \brief Represents a C++ lambda expression that produces a local function
   * object.
   *
   * \code
   * void abssort(float *x, unsigned N) {
   *   std::sort(x, x + N),
   *             [](float a, float b) {
   *               return std::abs(a) < std::abs(b);
   *             });
   * }
   * \endcode
   */
  LambdaExpr                    (144),
  
  /** \brief Objective-c Boolean Literal.
   */
  ObjCBoolLiteralExpr           (145),

  /** \brief Represents the "self" expression in a ObjC method.
   */
  ObjCSelfExpr                  (146),

  LastExpr                      (ObjCSelfExpr.value),

  /* Statements */
  FirstStmt                     (200),
  /**
   * \brief A statement whose specific kind is not exposed via this
   * interface.
   *
   * Unexposed statements have the same operations as any other kind of
   * statement; one can extract their location information, spelling),
   * children, etc. However, the specific kind of the statement is not
   * reported.
   */
  UnexposedStmt                 (200),
  
  /** \brief A labelled statement in a function. 
   *
   * This cursor kind is used to describe the "start_over:" label statement in 
   * the following example:
   *
   * \code
   *   start_over:
   *     ++counter;
   * \endcode
   *
   */
  LabelStmt                     (201),

  /** \brief A group of statements like { stmt stmt }.
   *
   * This cursor kind is used to describe compound statements, e.g. function
   * bodies.
   */
  CompoundStmt                  (202),

  /** \brief A case statment.
   */
  CaseStmt                      (203),

  /** \brief A default statement.
   */
  DefaultStmt                   (204),

  /** \brief An if statement
   */
  IfStmt                        (205),

  /** \brief A switch statement.
   */
  SwitchStmt                    (206),

  /** \brief A while statement.
   */
  WhileStmt                     (207),

  /** \brief A do statement.
   */
  DoStmt                        (208),

  /** \brief A for statement.
   */
  ForStmt                       (209),

  /** \brief A goto statement.
   */
  GotoStmt                      (210),

  /** \brief An indirect goto statement.
   */
  IndirectGotoStmt              (211),

  /** \brief A continue statement.
   */
  ContinueStmt                  (212),

  /** \brief A break statement.
   */
  BreakStmt                     (213),

  /** \brief A return statement.
   */
  ReturnStmt                    (214),

  /** \brief A GCC inline assembly statement extension.
   */
  GCCAsmStmt                    (215),
  AsmStmt                       (GCCAsmStmt.value), 

  /** \brief Objective-C's overall \@try-\@catch-\@finally statement.
   */
  ObjCAtTryStmt                 (216),

  /** \brief Objective-C's \@catch statement.
   */
  ObjCAtCatchStmt               (217),

  /** \brief Objective-C's \@finally statement.
   */
  ObjCAtFinallyStmt             (218),

  /** \brief Objective-C's \@throw statement.
   */
  ObjCAtThrowStmt               (219),

  /** \brief Objective-C's \@synchronized statement.
   */
  ObjCAtSynchronizedStmt        (220),

  /** \brief Objective-C's autorelease pool statement.
   */
  ObjCAutoreleasePoolStmt       (221),

  /** \brief Objective-C's collection statement.
   */
  ObjCForCollectionStmt         (222),

  /** \brief C++'s catch statement.
   */
  CXXCatchStmt                  (223),

  /** \brief C++'s try statement.
   */
  CXXTryStmt                    (224),

  /** \brief C++'s for (* : *) statement.
   */
  CXXForRangeStmt               (225),

  /** \brief Windows Structured Exception Handling's try statement.
   */
  SEHTryStmt                    (226),

  /** \brief Windows Structured Exception Handling's except statement.
   */
  SEHExceptStmt                 (227),

  /** \brief Windows Structured Exception Handling's finally statement.
   */
  SEHFinallyStmt                (228),

  /** \brief A MS inline assembly statement extension.
   */
  MSAsmStmt                     (229),

  /** \brief The null satement ";": C99 6.8.3p3.
   *
   * This cursor kind is used to describe the null statement.
   */
  NullStmt                      (230),

  /** \brief Adaptor class for mixing declarations with statements and
   * expressions.
   */
  DeclStmt                      (231),

  LastStmt                      (DeclStmt.value),

  /**
   * \brief Cursor that represents the translation unit itself.
   *
   * The translation unit cursor exists primarily to act as the root
   * cursor for traversing the contents of a translation unit.
   */
  TranslationUnit               (300),

  /* Attributes */
  FirstAttr                     (400),
  /**
   * \brief An attribute whose specific kind is not exposed via this
   * interface.
   */
  UnexposedAttr                 (400),

  IBActionAttr                  (401),
  IBOutletAttr                  (402),
  IBOutletCollectionAttr        (403),
  CXXFinalAttr                  (404),
  CXXOverrideAttr               (405),
  AnnotateAttr                  (406),
  AsmLabelAttr                  (407),
  LastAttr                      (AsmLabelAttr.value),
     
  /* Preprocessing */
  PreprocessingDirective        (500),
  MacroDefinition               (501),
  MacroExpansion                (502),
  MacroInstantiation            (MacroExpansion.value),
  InclusionDirective            (503),
  FirstPreprocessing            (PreprocessingDirective.value),
  LastPreprocessing             (InclusionDirective.value),

  /* Extra Declarations */
  /**
   * \brief A module import declaration.
   */
  ModuleImportDecl              (600),
  FirstExtraDecl                (ModuleImportDecl.value),
  LastExtraDecl                 (ModuleImportDecl.value);
  
    /**
     * \brief Determine whether the given cursor kind represents a declaration.
     * @return true for declaration kind
     */
    public boolean isDeclaration() {
        return (value >= FirstDecl.value && value <= LastDecl.value)
                || (value >= FirstExtraDecl.value && value <= LastExtraDecl.value);
    }

    /**
     * \brief Determine whether the given cursor kind represents an invalid
     * cursor.
     * @return 
     */
    public boolean isInvalid() {
        return value >= FirstInvalid.value && value <= LastInvalid.value;
    }

    /**
     * \brief Determine whether the given cursor kind represents a simple
     * reference.
     *
     * Note that other kinds of cursors (such as expressions) can also refer to
     * other cursors. Use CMCursor.getCursorReferenced() to determine whether a
     * particular cursor refers to another entity.
     * @return 
     */
    public boolean isReference() {
        return value >= FirstRef.value && value <= LastRef.value;
    }

    /**
     * \brief Determine whether the given cursor kind represents an expression.
     * @return 
     */
    public boolean isExpression() {
        return value >= FirstExpr.value && value <= LastExpr.value;
    }

    /**
     * \brief Determine whether the given cursor kind represents a statement.
     * @return 
     */
    public boolean isStatement() {
        return value >= FirstStmt.value && value <= LastStmt.value;
    }

    /**
     * \brief Determine whether the given cursor kind represents an attribute.
     * @return 
     */
    public boolean isAttribute() {
        return value >= FirstAttr.value && value <= LastAttr.value;
    }

    /**
     * \brief Determine whether the given cursor kind represents a translation
     * unit.
     * @return 
     */
    public boolean isTranslationUnit() {
        return this == TranslationUnit;
    }

    /**
     * *
     * \brief Determine whether the given cursor represents a preprocessing
     * element, such as a preprocessor directive or macro instantiation.
     * @return 
     */
    public boolean isPreprocessing() {
        return value >= FirstPreprocessing.value && value <= LastPreprocessing.value;
    }

    /**
     * *
     * \brief Determine whether the given cursor represents a currently
     * unexposed piece of the AST (e.g., UnexposedStmt).
     * @return 
     */
    public boolean isUnexposed() {
        switch (this) {
            case UnexposedDecl:
            case UnexposedExpr:
            case UnexposedStmt:
            case UnexposedAttr:
                return true;
            default:
                return false;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    public static CMCursorKind valueOf(int val) {
        // FIXME: make constant
        short kindVal = (short) val;
        for (CMCursorKind kind : CMCursorKind.values()) {
            if (kind.value == kindVal) {
                return kind;
            }
        }
        assert false : "no kind for " + val;
        return NotImplemented;
    }
    
    private final short value;
    
    private CMCursorKind(int val) {
        this.value = (short) val;
    }
    
    public int getValue() {
        return value;
    }
    //</editor-fold>
}

