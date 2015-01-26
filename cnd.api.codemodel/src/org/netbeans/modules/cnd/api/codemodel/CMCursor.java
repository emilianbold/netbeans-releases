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

import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;

/**
 * \brief A cursor representing some element in the abstract syntax tree for a
 * translation unit.
 *
 * The cursor abstraction unifies the different kinds of entities in a
 * program--declaration, statements, expressions, references to declarations,
 * etc.--under a single "cursor" abstraction with a common set of operations.
 * Common operation for a cursor include: getting the physical location in a
 * source file where the cursor points, getting the name associated with a
 * cursor, and retrieving cursors for any child nodes of a particular cursor.
 *
 * Cursors can be produced in two specific ways.
 * clang_getTranslationUnitCursor() produces a cursor for a translation unit,
 * from which one can use clang_visitChildren() to explore the rest of the
 * translation unit. clang_getCursor() maps from a physical source location to
 * the entity that resides at that location, allowing one to map from the source
 * code into the AST.
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMCursor {

    /**
     * \brief Returns true if \p cursor is dummy.
     *
     * @return true if \p cursor is valid.
     */
    public boolean isDummy() {
        return impl.isDummy();
    }

    /**
     * \brief Retrieve the kind of the given cursor.
     *
     * @see CMCursorKind
     * @return the kind of the cursor
     */
    public CMCursorKind getKind() {
        return impl.getKind();
    }

    //<editor-fold defaultstate="collapsed" desc="Linkage Kinds">
    /**
     * \brief Describe the linkage of the entity referred to by a cursor.
     */
    public enum Linkage {

        /**
         * \brief This value indicates that no linkage information is available
         * for a provided CMCursor.
         */
        Invalid(0),
        /**
         * \brief This is the linkage for variables, parameters, and so on that
         * have automatic storage. This covers normal (non-extern) local
         * variables.
         */
        NoLinkage(1),
        /**
         * \brief This is the linkage for static variables and static functions.
         */
        Internal(2),
        /**
         * \brief This is the linkage for entities with external linkage that
         * live in C++ anonymous namespaces.
         */
        UniqueExternal(3),
        /**
         * \brief This is the linkage for entities with true, external linkage.
         */
        External(4);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static Linkage valueOf(int val) {
            byte langVal = (byte) val;
            for (Linkage language : Linkage.values()) {
                if (language.value == langVal) {
                    return language;
                }
            }
            assert false : "unsupported kind " + val;
            return Invalid;
        }

        private final byte value;

        private Linkage(int lang) {
            this.value = (byte) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>

    };
    //</editor-fold>

    /**
     * \brief Determine the linkage of the entity referred to by a given cursor.
     *
     * @return linkage kind
     */
    public Linkage getLinkage() {
        return impl.getLinkage();
    }

    //<editor-fold defaultstate="collapsed" desc="Availability Kinds">
    /**
     * \brief Describes the availability of a particular entity, which indicates
     * whether the use of this entity will result in a warning or error due to
     * it being deprecated or unavailable.
     */
    public static enum Availability {

        Invalid(-1),
        /**
         * \brief The entity is available.
         */
        Available(0),
        /**
         * \brief The entity is available, but has been deprecated (and its use
         * is not recommended).
         */
        Deprecated(1),
        /**
         * \brief The entity is not available; any use of it will be an error.
         */
        NotAvailable(2),
        /**
         * \brief The entity is available, but not accessible; any use of it
         * will be an error.
         */
        NotAccessible(3);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static Availability valueOf(int val) {
            byte langVal = (byte) val;
            for (Availability language : Availability.values()) {
                if (language.value == langVal) {
                    return language;
                }
            }
            assert false : "unsupported kind " + val;
            return Invalid;
        }

        private final byte value;

        private Availability(int lang) {
            this.value = (byte) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>
    };
    //</editor-fold>

    /**
     * \brief Determine the availability of the entity that this cursor refers
     * to, taking the current target platform into account.
     *
     * @return The availability kind of the cursor.
     */
    public Availability getAvailability() {
        return impl.getAvailability();
    }

    //<editor-fold defaultstate="collapsed" desc="Language Kinds">
    /**
     * \brief Describe the "language" of the entity referred to by a cursor.
     */
    public enum Language {

        Invalid(0),
        C(1),
        ObjC(2),
        CPlusPlus(3);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static Language valueOf(int val) {
            byte langVal = (byte) val;
            for (Language language : Language.values()) {
                if (language.lang == langVal) {
                    return language;
                }
            }
            assert false : "unsupported language " + val;
            return Invalid;
        }

        private final byte lang;

        private Language(int lang) {
            this.lang = (byte) lang;
        }

        public int getValue() {
            return lang;
        }
        //</editor-fold>

    };
    //</editor-fold>

    /**
     * \brief Determine the "language" of the entity referred to by a given
     * cursor.
     *
     * @return the "language" of the entity referred to by a given cursor.
     */
    public Language getLanguage() {
        return impl.getLanguage();
    }

    /**
     * \brief Returns the translation unit that a cursor originated from.
     *
     * @return translation unit that a cursor originated from
     */
    public CMTranslationUnit getTranslationUnit() {
        CMTranslationUnitImplementation tu = impl.getTranslationUnit();
        return (tu == null) ? null : CMTranslationUnit.fromImpl(tu);
    }

    /**
     * \brief Retrieve the physical location of the source construction
     * referenced by the given cursor.
     *
     * The location of a declaration is typically the location of the name of
     * that declaration, where the name of that declaration would occur if it is
     * unnamed, or some keyword that introduces that particular declaration. The
     * location of a reference is where that reference occurs within the source
     * code.
     *
     * @return physical location
     */
    public CMSourceLocation getLocation() {
        return CMSourceLocation.fromImpl(impl.getLocation());
    }

    /**
     * \brief Retrieve the physical extent of the source construct referenced by
     * the given cursor.
     *
     * The extent of a cursor starts with the file/line/column pointing at the
     * first character within the source construct that the cursor refers to and
     * ends with the last character withinin that source construct. For a
     * declaration, the extent covers the declaration itself. For a reference,
     * the extent covers the location of the reference (e.g., where the
     * referenced entity was actually used).
     *
     * @return physical extent
     */
    public CMSourceRange getExtent() {
        return CMSourceRange.fromImpl(impl.getExtent());
    }

    /**
     * \brief Retrieve a Unified Symbol Resolution (USR) for the entity
     * referenced by the given cursor.
     *
     * A Unified Symbol Resolution (USR) is a string that identifies a
     * particular entity (function, class, variable, etc.) within a program.
     * USRs can be compared across translation units to determine, e.g., when
     * references in one translation refer to an entity defined in another
     * translation unit.
     *
     * @return USR of referenced entity
     */
    public CMUnifiedSymbolResolution getUSR() {
        return impl.getUSR();
    }

    /**
     * \brief Retrieve the canonical cursor corresponding to the given cursor.
     *
     * In the C family of languages, many kinds of entities can be declared
     * several times within a single translation unit. For example, a structure
     * type can be forward-declared (possibly multiple times) and later defined:
     *
     * <code>
     * struct X;
     * struct X;
     * struct X { int member; };
     * </code>
     *
     * The declarations and the definition of \c X are represented by three
     * different cursors, all of which are declarations of the same underlying
     * entity. One of these cursor is considered the "canonical" cursor, which
     * is effectively the representative for the underlying entity. One can
     * determine if two cursors are declarations of the same underlying entity
     * by comparing their canonical cursors.
     *
     * @return The canonical cursor for the entity referred to by the given
     * cursor.
     */
    public CMCursor getCanonical() {
        return CMCursor.fromImpl(impl.getCanonical());
    }

    /**
     * \brief Retrieve a name for the entity referenced by this cursor.
     *
     * @return spelling of entity
     */
    public CharSequence getSpellingName() {
        return impl.getSpellingName();
    }

    /**
     * \brief Retrieve a range for a piece that forms the cursors spelling name.
     *
     * @return spelling name range
     */
    public CMSourceRange getSpellingNameRange() {
        return CMSourceRange.fromImpl(impl.getSpellingNameRange());
    }

    /**
     * \brief Retrieve the display name for the entity referenced by this
     * cursor.
     *
     * The display name contains extra information that helps identify the
     * cursor, such as the parameters of a function or template or the arguments
     * of a class template specialization.
     *
     * @return display name for the entity
     */
    public CharSequence getDisplayName() {
        return impl.getDisplayName();
    }

    /**
     * \brief Determine the semantic parent of the given cursor.
     *
     * The semantic parent of a cursor is the cursor that semantically contains
     * the given \p cursor. For many declarations, the lexical and semantic
     * parents are equivalent (the lexical parent is returned by \c
     * clang_getCursorLexicalParent()). They diverge when declarations or
     * definitions are provided out-of-line. For example:
     *
     * <code>
     * class C {
     *   void f();
     * };
     *
     * void C::f() {
     * }
     * </code>
     *
     * In the out-of-line definition of \c C::f, the semantic parent is the the
     * class \c C, of which this function is a member. The lexical parent is the
     * place where the declaration actually occurs in the source code; in this
     * case, the definition occurs in the translation unit. In general, the
     * lexical parent for a given entity can change without affecting the
     * semantics of the program, and the lexical parent of different
     * declarations of the same entity may be different. Changing the semantic
     * parent of a declaration, on the other hand, can have a major impact on
     * semantics, and redeclarations of a particular entity should all have the
     * same semantic context.
     *
     * In the example above, both declarations of \c C::f have \c C as their
     * semantic context, while the lexical context of the first \c C::f is \c C
     * and the lexical context of the second \c C::f is the translation unit.
     *
     * For global declarations, the semantic parent is the translation unit.
     *
     * @return
     */
    public CMCursor getSemanticParent() {
        return fromImpl(impl.getSemanticParent());
    }

    /**
     * \brief Determine the lexical parent of the given cursor.
     *
     * The lexical parent of a cursor is the cursor in which the given \p cursor
     * was actually written. For many declarations, the lexical and semantic
     * parents are equivalent (the semantic parent is returned by \c
     * clang_getCursorSemanticParent()). They diverge when declarations or
     * definitions are provided out-of-line. For example:
     *
     * <code>
     * class C {
     *   void f();
     * };
     *
     * void C::f() {
     * }
     * </code>
     *
     * In the out-of-line definition of \c C::f, the semantic parent is the the
     * class \c C, of which this function is a member. The lexical parent is the
     * place where the declaration actually occurs in the source code; in this
     * case, the definition occurs in the translation unit. In general, the
     * lexical parent for a given entity can change without affecting the
     * semantics of the program, and the lexical parent of different
     * declarations of the same entity may be different. Changing the semantic
     * parent of a declaration, on the other hand, can have a major impact on
     * semantics, and redeclarations of a particular entity should all have the
     * same semantic context.
     *
     * In the example above, both declarations of \c C::f have \c C as their
     * semantic context, while the lexical context of the first \c C::f is \c C
     * and the lexical context of the second \c C::f is the translation unit.
     *
     * For declarations written in the global scope, the lexical parent is the
     * translation unit.
     *
     * @return
     */
    public CMCursor getLexicalParent() {
        return fromImpl(impl.getLexicalParent());
    }

    /**
     * \brief For a cursor that is a reference, retrieve a cursor representing
     * the entity that it references.
     *
     * Reference cursors refer to other entities in the AST. For example, an
     * Objective-C superclass reference cursor refers to an Objective-C class.
     * This function produces the cursor for the Objective-C class from the
     * cursor for the superclass reference. If the input cursor is a declaration
     * or definition, it returns that declaration or definition unchanged.
     * Otherwise, returns the NULL cursor.
     *
     * @return referenced entity cursor
     */
    public CMCursor getReferencedEntityCursor() {
        return CMCursor.fromImpl(impl.getReferencedEntityCursor());
    }

    /**
     * \brief For a cursor that is either a reference to or a declaration of
     * some entity, retrieve a cursor that describes the definition of that
     * entity.
     *
     * Some entities can be declared multiple times within a translation unit,
     * but only one of those declarations can also be a definition. For example,
     * given:
     *
     * <code>
     * int f(int, int);
     *
     * int g(int x, int y) {
     *   return f(x, y);
     * }
     *
     * int f(int a, int b) {
     *   return a + b;
     * }
     *
     * int f(int, int);
     * </code>
     *
     * there are three declarations of the function "f", but only the second one
     * is a definition. The clang_getCursorDefinition() function will take any
     * cursor pointing to a declaration of "f" (the first or fourth lines of the
     * example) or a cursor referenced that uses "f" (the call to "f' inside
     * "g") and will return a declaration cursor pointing to the definition (the
     * second "f" declaration).
     *
     * If given a cursor for which there is no corresponding definition, e.g.,
     * because there is no definition of that entity within this translation
     * unit, returns a NULL cursor.
     *
     * @return cursor that describes the definition of referenced entity
     */
    public CMCursor getReferencedEntityDefinition() {
        return CMCursor.fromImpl(impl.getReferencedEntityDefinition());
    }

    /**
     * \brief Retrieve the type of a CMCursor (if any).
     *
     * @return the type of a cursor (if any).
     */
    public CMType getType() {
        return CMType.fromImpl(impl.getType());
    }

    /**
     * \brief Retrieve the underlying type of a typedef declaration.
     *
     * @return If the cursor does not reference a typedef declaration, an
     * invalid type is returned.
     */
    public CMType getTypedefUnderlyingType() {
        return CMType.fromImpl(impl.getTypedefUnderlyingType());
    }

    /**
     * \brief Retrieve the integer type of an enum declaration.
     *
     * @return If the cursor does not reference an enum declaration, an invalid
     * type is returned.
     */
    public CMType getEnumIntegerType() {
        return CMType.fromImpl(impl.getEnumIntegerType());
    }

    /**
     * \brief Retrieve the integer value of an enum constant declaration as a
     * signed long long.
     *
     * @return If the cursor does not reference an enum constant declaration,
     * LLONG_MIN is returned. Since this is also potentially a valid constant
     * value, the kind of the cursor must be verified before calling this
     * function.
     */
    public long getEnumeratorValue() {
        return impl.getEnumeratorValue();
    }

    /**
     * \brief For cursors representing an IBOutletCollectionAttr attribute, this
     * function returns the collection element type.
     *
     * @return type for collection element
     */
    public CMType getIBOutletCollectionType() {
        return CMType.fromImpl(impl.getIBOutletCollectionType());
    }

    /**
     * \brief Returns non-zero if the cursor specifies a Record member that is a
     * bitfield.
     *
     * @return true for bit field member
     */
    public boolean isBitField() {
        return impl.isBitField();
    }

    /**
     * \brief Retrieve the bit width of a bit field declaration as an integer.
     *
     * @return If a cursor that is not a bit field declaration, -1 is returned.
     */
    public int getFieldBitWidth() {
        return impl.getFieldBitWidth();
    }

    /**
     * \brief Returns non-zero if the given cursor is a variadic function or
     * method.
     *
     * @return true for variadic functions
     */
    public boolean isVariadic() {
        return impl.isVariadic();
    }

    /**
     * \brief Retrieve the number of non-variadic arguments associated with a
     * given cursor.
     *
     * The number of arguments can be determined for calls as well as for
     * declarations of functions or methods. For other cursors -1 is returned.
     *
     * @return number of arguments or -1
     */
    public int getArgumentsCount() {
        return impl.getArgumentsCount();
    }

    /**
     * \brief Retrieve the arguments cursors of a function or method.
     *
     * The argument cursor can be determined for calls as well as for
     * declarations of functions or methods.
     *
     * @return arguments or null if cursor kind doesn't support arguments
     */
    public Iterable<CMCursor> getArguments() {
        Iterable<CMCursorImplementation> args = impl.getArguments();
        return args == null ? null : CMCursor.fromImpls(args);
    }

    /**
     * \brief Determine whether the declaration pointed to by this cursor is
     * also a definition of that entity.
     *
     * @return true if cursor is definition of entity, false otherwise
     */
    public boolean isDefinition() {
        return impl.isDefinition();
    }

    /**
     * \brief Given a cursor pointing to a C++ method call or an ObjC message,
     * returns non-zero if the method/message is "dynamic", meaning:
     *
     * For a C++ method: the call is virtual. For an ObjC message: the receiver
     * is an object instance, not 'super' or a specific class.
     *
     * If the method/message is "static" or the cursor does not point to a
     * method/message, it will return zero.
     *
     * @return true for virtual call
     */
    public boolean isVirtualCall() {
        return impl.isVirtualCall();
    }

    /**
     * \brief Determine if a C++ member function or member function template is
     * explicitly declared 'virtual' or if it overrides a virtual method from
     * one of the base classes.
     *
     * @return true if method is implicitly or explicitely virtual
     */
    public boolean isVirtualMethod() {
        return impl.isVirtualMethod();
    }

    /**
     * \brief Determine if a C++ member function or member function template is
     * declared 'static'.
     *
     * @return true if member function is static
     */
    public boolean isStaticMethod() {
        return impl.isStaticMethod();
    }

    /**
     * \brief Returns true if the base class specified by the cursor with kind
     * CXXBaseSpecifier is virtual.
     *
     * @return true if marked as virtual
     */
    public boolean isVirtualBase() {
        return impl.isVirtualBase();
    }

    //<editor-fold defaultstate="collapsed" desc="CXXAccessSpecifier kinds">
    /**
     * \brief Represents the C++ access control level to a base class for a
     * cursor with kind CXXBaseSpecifier or class members.
     */
    public enum CXXAccessSpecifier {

        Invalid(0),
        Public(1),
        Protected(2),
        Private(3);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static CXXAccessSpecifier valueOf(int val) {
            byte curVal = (byte) val;
            for (CXXAccessSpecifier language : CXXAccessSpecifier.values()) {
                if (language.val == curVal) {
                    return language;
                }
            }
            assert false : "unsupported value " + val;
            return Invalid;
        }

        private final byte val;

        private CXXAccessSpecifier(int lang) {
            this.val = (byte) lang;
        }

        public int getValue() {
            return val;
        }
        //</editor-fold>
    }
    //</editor-fold>

    /**
     * \brief Returns the access control level for the referenced object.
     *
     * If the cursor refers to a C++ declaration, its access control level
     * within its parent scope is returned. Otherwise, if the cursor refers to a
     * base specifier or access specifier, the specifier itself is returned.
     *
     * @return access control level (if applicable)
     */
    public CXXAccessSpecifier getAccessSpecifier() {
        return impl.getAccessSpecifier();
    }

    /**
     * \brief Given a cursor that represents a template, determine the cursor
     * kind of the specializations would be generated by instantiating the
     * template.
     *
     * This routine can be used to determine what flavor of function template,
     * class template, or class template partial specialization is stored in the
     * cursor. For example, it can describe whether a class template cursor is
     * declared with "struct", "class" or "union".
     *
     * This cursor should represent a template declaration.
     *
     * @return The cursor kind of the specializations that would be generated by
     * instantiating the template \p C. If \p C is not a template, returns \c
     * CMCursorKind.NoDeclFound.
     */
    public CMCursorKind getTemplateKind() {
        return impl.getTemplateKind();
    }

    /**
     * \brief Given a cursor that may represent a specialization or
     * instantiation of a template, retrieve the cursor that represents the
     * template that it specializes or from which it was instantiated.
     *
     * This routine determines the template involved both for explicit
     * specializations of templates and for implicit instantiations of the
     * template, both of which are referred to as "specializations". For a class
     * template specialization (e.g., \c std::vector<bool>), this routine will
     * return either the primary template (\c std::vector) or, if the
     * specialization was instantiated from a class template partial
     * specialization, the class template partial specialization. For a class
     * template partial specialization and a function template specialization
     * (including instantiations), this this routine will return the specialized
     * template.
     *
     * For members of a class template (e.g., member functions, member classes,
     * or static data members), returns the specialized or instantiated member.
     * Although not strictly "templates" in the C++ language, members of class
     * templates have the same notions of specializations and instantiations
     * that templates do, so this routine treats them similarly.
     *
     * This cursor that may be a specialization of a template or a member of a
     * template.
     *
     * @return If the given cursor is a specialization or instantiation of a
     * template or a member thereof, the template or member that it specializes
     * or from which it was instantiated. Otherwise, returns a NULL cursor.
     */
    public CMCursor getSpecializedTemplate() {
        return CMCursor.fromImpl(impl.getSpecializedTemplate());
    }

    /**
     * \brief Determine the set of methods that are overridden by the given
     * method.
     *
     * For cursor representing an Objective-C or C++ method this routine will
     * compute the set of methods that this method overrides. In both
     * Objective-C and C++, a method (aka virtual member function, in C++) can
     * override a virtual method in a base class. For Objective-C, a method is
     * said to override any method in the class's base class, its protocols, or
     * its categories' protocols, that has the same selector and is of the same
     * kind (class or instance). If no such method exists, the search continues
     * to the class's superclass, its protocols, and its categories, and so on.
     * A method from an Objective-C implementation is considered to override the
     * same methods as its corresponding method in the interface.
     *
     * For C++, a virtual member function overrides any virtual member function
     * with the same signature that occurs in its base classes. With multiple
     * inheritance, a virtual member function can override several virtual
     * member functions coming from different base classes.
     *
     * In all cases, this function determines the immediate overridden method,
     * rather than all of the overridden methods. For example, if a method is
     * originally declared in a class A, then overridden in B (which in inherits
     * from A) and also in C (which inherited from B), then the only overridden
     * method returned from this function when invoked on C's method will be B's
     * method. The client may then invoke this function again, given the
     * previously-found overridden methods, to map out the complete
     * method-override set.
     *
     * @return list of directly overridden methods from parent classes.
     */
    public Iterable<CMCursor> getDirectlyOverridden() {
        return fromImpls(impl.getDirectlyOverridden());
    }

    /**
     * \brief Retrieve the overloaded declarations referenced by a \c this
     * OverloadedDeclRef cursor.
     *
     * @return Cursors representing the overloaded declarations referenced by
     * the given \c cursor. If the cursor does not have an associated set of
     * overloaded declarations, returns empty collection;
     */
    public Iterable<CMCursor> getOverloaded() {
        return fromImpls(impl.getOverloaded());
    }

    /**
     * \brief For cursor representing inclusion directive Retrieve the file that
     * is included.
     *
     * @return included file or null for non-include directive
     */
    public CMFile getIncludedFile() {
        CMFileImplementation inc = impl.getIncludedFile();
        return inc == null ? null : CMFile.fromImpl(inc);
    }

    /**
     * \brief Given a cursor that represents a declaration, return the
     * associated comment text, including comment markers.
     *
     * @return comment text associated with declaration
     */
    public CharSequence getRawCommentText() {
        return impl.getRawCommentText();
    }

    /**
     * \brief Given a cursor that represents a documentable entity (e.g.,
     * declaration), return the associated \\brief paragraph; otherwise return
     * the first paragraph.
     *
     * @return brief text
     */
    public CharSequence getBriefCommentText() {
        return impl.getBriefCommentText();
    }

    /**
     * \brief Given a cursor that represents a declaration, return the
     * associated comment's source range. The range may include multiple
     * consecutive comments with whitespace in between.
     *
     * @return comment range
     */
    public CMSourceRange getCommentRange() {
        return CMSourceRange.fromImpl(impl.getCommentRange());
    }

    /**
     * \brief Given a cursor that represents a documentable entity (e.g.,
     * declaration), return the associated parsed comment as a \c
     * CXComment_FullComment AST node.
     *
     * @return comment as AST node
     */
    public CMComment getComment() {
        return new CMComment(impl.getComment());
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMCursorImplementation impl;

    private CMCursor(CMCursorImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    static CMCursor fromImpl(CMCursorImplementation impl) {
        // TODO: share instance for the same impl if needed
        return new CMCursor(impl);
    }

    /*package*/
    static Iterable<CMCursor> fromImpls(Iterable<CMCursorImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    /*package*/
    CMCursorImplementation getImpl() {
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
        if (obj instanceof CMCursor) {
            return this.impl.equals(((CMCursor) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }

    private static final IterableFactory.Converter<CMCursorImplementation, CMCursor> CONV
            = new IterableFactory.Converter<CMCursorImplementation, CMCursor>() {

                @Override
                public CMCursor convert(CMCursorImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
