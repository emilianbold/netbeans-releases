/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/**
 * C++ grammar.
 * @author Fedor Sergeev
 */

parser grammar CXX_grammar;

options {
    backtrack = false;
    language = C;
}

import CXX_tokens;
//import CXX_lexer;


scope QualName {
    int qual;
    int type;
}

scope Declaration {
    declarator_type_t declarator;
    declaration_specifiers_t decl_specifiers;
    int type_specifiers_count;
}

@includes {
    #include "grammar_types.h"
}

@members {

    static int IDT_CLASS_NAME=1;
    static int IDT_ENUM_NAME=2;
    static int IDT_TYPEDEF_NAME=4;
    static int IDT_TEMPLATE_NAME=8;
    static int IDT_OBJECT_NAME = 16;

    void init_declaration(pCXX_grammar ctx, decl_kind kind)
    {
        $Declaration::declarator.init();
        $Declaration::decl_specifiers.init(kind);
        $Declaration::type_specifiers_count = 0;
    }

    boolean type_specifier_already_present(pCXX_grammar ctx)
    {
        boolean result = false;
        if ($Declaration->size($Declaration) > 0) {
            result = $Declaration::type_specifiers_count != 0;
        }
        trace("type_specifier_already_present()=",result);
        return result;
    }
    boolean identifier_is(int x) {
        trace("identifier_is()=",true);
        return true;
    }
    boolean top_level_of_template_arguments() {
        trace("top_level_of_template_arguments()=",true);
        return true;
    }
    boolean operator_is_template() {
        trace("operator_is_template()=",true);
        return true;
    }

    void qual_setup() {  }
    void qual_add_colon2() { }

    void store_type_specifier(type_specifier_t ts, pCXX_grammar ctx) {
        $Declaration::type_specifiers_count++;
        trace("store_type_specifier->", $Declaration::type_specifiers_count);
    }
}

// [gram.basic] 
translation_unit:
        declaration[object_decl]* EOF
    ;

// [gram.stmt]
/*
 * As per 2003 standard:
 * "An expression-statement with a function-style explicit type conversion as its leftmost 
 * subexpression can be indistinguishable from a declaration where the first declarator starts with a '('. 
 * In those cases the statement is a declaration."
 *
 * Resolve declaration vs expression conflict in favor of declaration.
 * (actually declaration synpred is a HUGE hammer, we should try find something else)
 */
statement:
        labeled_statement
    |
        expression_or_declaration_statement
    |
        compound_statement
    |
        selection_statement
    |
        iteration_statement
    |
        jump_statement
    |
        try_block
    ;

labeled_statement:
        IDENT COLON statement
    |
        CASE constant_expression COLON statement
    |
        DEFAULT COLON statement
    ;

expression_statement:
        expression? SEMI
    ;

expression_or_declaration_statement
    :
        (declaration_statement)=> declaration_statement
    |
        expression SEMI
    ;


compound_statement:
        LCURL statement* RCURL
    ;

selection_statement:
        IF LPAR condition RPAR statement ( (ELSE)=> ELSE statement )?
    |
        SWITCH LPAR condition RPAR statement
    ;

/*
 * The same expression-declaration ambiguity as in statement rule.
 */
condition
scope Declaration;
@init { init_declaration(CTX, blockscope_decl); }
    :
        (type_specifier+ declarator EQ)=>
            type_specifier+ declarator EQ assignment_expression
    |
        expression
    ;

iteration_statement:
        WHILE LPAR condition RPAR statement
    |
        DO statement WHILE LPAR expression RPAR SEMI
    |
        FOR LPAR for_init_statement condition? SEMI expression? RPAR statement
    ;
/*
 * The same expression-declaration ambiguity as in statement rule.
 */
for_init_statement:
        (simple_declaration[blockscope_decl])=>
            simple_declaration[blockscope_decl]
    |
        expression_statement

    ;
jump_statement:
        BREAK SEMI
    |
        CONTINUE SEMI
    |
        RETURN expression? SEMI
    |
        GOTO IDENT SEMI
    ;

/*
 * simple_declaration has been split out of block_declaration so to have
 * an easier view of simple_declaration vs function_definition major conflict.
 */
declaration_statement:
        simple_declaration[blockscope_decl]
    |
        block_declaration
    ;

//[gram.dcl] 
/*
 * function_definition merged into one rule with simple_declaration (which in turn was taken out of block_declaration)
 */
declaration [decl_kind kind] :
        block_declaration
    |
        simple_declaration_or_function_definition[kind]
    |
        template_declaration[kind]
    |
        explicit_instantiation[kind]
    |
        explicit_specialization[kind]
    |
        linkage_specification[kind]
    |
        namespace_definition 
    ;
block_declaration:
        asm_definition 
    |
        namespace_alias_definition 
    |
        using_declaration
    |
        using_directive 
    ;

// IDs
id_expression:
        unqualified_id
    |
        qualified_id
    ;

unqualified_id:
        (OPERATOR operator_id)=>
            operator_function_id
    |
        conversion_function_id
    |
        TILDE class_name
    |
        simple_template_id_or_IDENT
    ;

qualified_id:
        nested_name_specifier TEMPLATE? unqualified_id
    |
        COLON2 (
            nested_name_specifier TEMPLATE? unqualified_id
        |
            operator_function_id
        |
            simple_template_id_or_IDENT
        )
    ;

/* original rule:
 *

nested_name_specifier:
        type-name COLON2
    |
        namespace-name COLON2
    |
        nested-name-specifier IDENT COLON2
    |
        nested-name-specifier TEMPLATE? simple-template-id COLON2
    ;

 * left-recursion removed here and TEMPLATE/IDENT ambiguity resolved
 */

nested_name_specifier returns [ name_specifier_t namequal ]
    :
        IDENT COLON2
        (
            (TEMPLATE lookup_simple_template_id_nocheck COLON2 )=> TEMPLATE simple_template_id_nocheck COLON2
        |
            (IDENT COLON2) =>
                IDENT COLON2
        |
            (lookup_simple_template_id COLON2)=>
                simple_template_id COLON2
        )*
    ;

lookup_nested_name_specifier:
        IDENT COLON2
        (
            IDENT COLON2
        |
            TEMPLATE lookup_simple_template_id COLON2
        |
            lookup_simple_template_id COLON2
        )*
    ;

//[gram.dcl]
/*
 * original rule:

simle_declaration
        decl_specifier* (init_declarator (COMMA init_declarator)*)* SEMI
    ;

 * construtor_declarator introduced into init_declarator part to resolve ambiguity
 * between single decl_specifier and the constructor name in a declarator (declarator_id) of constructor.
 *
 */
simple_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier*
        (
            SEMI
        |
            (
                (constructor_declarator)=> constructor_declarator
            |
                init_declarator
            )
            // this is a continuation of init_declarator_list after constructor_declarator/init_declarator
            (COMMA init_declarator)* SEMI
        )
    ;

/*
 * This is the above simple_declaration rule merged together with function definition
 * The idea is to avoid doing any lookaheads unless absolutely necessary (constructor declarator).
 * It requires certain duplication as common constructs in each branch of choice are a bit different
 * (see different init_declarator_list continuation sequences).
 */
simple_declaration_or_function_definition [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier*
        (
            SEMI
        |
            (constructor_declarator)=>
                constructor_declarator
                (
                    // this is a continuation of init_declarator_list after constructor_declarator
                    ( COMMA init_declarator )* SEMI
                |
                    function_definition_after_declarator
                )
        |
            // greedy_declarator starts init_declarator
            greedy_declarator
            (
                { $greedy_declarator.type.is_function() }?
                    function_definition_after_declarator
            |
                // this is a continuation of init_declarator_list after greedy_declarator
                initializer? ( COMMA init_declarator )* SEMI
            )
        )
    ;

decl_specifier
    :
        storage_class_specifier
        { $Declaration::decl_specifiers.apply_specifier($decl_specifier.start, CTX); }
    |
        function_specifier 
        { $Declaration::decl_specifiers.apply_specifier($decl_specifier.start, CTX); }
    |
        FRIEND
        { $Declaration::decl_specifiers.apply_specifier($FRIEND, CTX); }
    |
        TYPEDEF
        { $Declaration::decl_specifiers.apply_specifier($TYPEDEF, CTX); }
    |
        type_specifier
        { $Declaration::decl_specifiers.add_type($type_specifier.ts, CTX); }
    ;

storage_class_specifier:
        AUTO 
    |
        REGISTER 
    |
        STATIC 
    |
        EXTERN 
    |
        MUTABLE 
    |
        THREAD
    ;
function_specifier:
        INLINE 
    |
        VIRTUAL 
    |
        EXPLICIT 
    ;

/*
 * original rule

type_specifier:
        simple_type_specifier 
    |
        class_specifier 
    |
        enum_specifier 
    |
        elaborated_type_specifier
    |
        cv_qualifier 
    ;

 * Ambiguity in CLASS because of class_specifier vs elaborated_type_specifier conflict
 * Ambiguity in ENUM because of enum_specifier vs elaborated_type_specifier conflict
 *
 * Note, that (CLASS COLON2) sequence is not valid for class_specifier
 * Similarly (ENUM COLON2) sequence is not valid for enum_specifier
 */

type_specifier returns [type_specifier_t ts]
    :
        // CLASS COLON2 does not cover all the elaborated_type_specifier cases even with CLASS
        (CLASS COLON2)=>
            elaborated_type_specifier
    |
        // thus we need to make serious lookahead here to catch LCURL
        (class_head LCURL)=>
            class_specifier
    |
        // enum_specifier start sequence is simple
        (ENUM IDENT? LCURL)=>
            enum_specifier
    |
        simple_type_specifier
        { store_type_specifier($simple_type_specifier.ts_val, CTX); }
    |
        // CLASS COLON2 above does not cover all the elaborated_type_specifier cases
        elaborated_type_specifier
    |
        cv_qualifier
    ;

simple_type_specifier returns [type_specifier_t ts_val]
scope QualName;
@init { qual_setup(); }
    :
        CHAR
    |
        WCHAR_T
    |
        BOOL
    |
        SHORT
    |
        INT
    |
        LONG
    |
        SIGNED
    |
        UNSIGNED
    |
        FLOAT
    |
        DOUBLE
    |
        VOID
    |
        /*
         * "at most one type-specifier is allowed in the complete decl-specifier-seq of a declaration..."
         * In particular (qualified)type_name is allowed only once.
         */
        { !type_specifier_already_present(CTX) }? =>
            (COLON2 {{ qual_add_colon2(); }} )?
            /* note that original rule does not allow empty nested_name_specifier for the TEMPLATE alternative */
            (
                (lookup_nested_name_specifier)=>
                    nested_name_specifier (IDENT | TEMPLATE simple_template_id)
            |
                IDENT
            )
    ;

lookup_type_name:
        IDENT { identifier_is(IDT_CLASS_NAME|IDT_ENUM_NAME|IDT_TYPEDEF_NAME) }?
    ;

/*
 * original rule:
 *
elaborated_type_specifier:
        class_key COLON2? nested_name_specifier? IDENT 
    |
        class_key COLON2? nested_name_specifier? TEMPLATE? simple_template_id 
    |
        ENUM COLON2? nested_name_specifier? IDENT 
    |
        TYPENAME COLON2? nested_name_specifier IDENT 
    |
        TYPENAME COLON2? nested_name_specifier TEMPLATE? simple_template_id 
    ;
* Ambiguity introduced by IDENT COLON2 IDENT sequence in a context of
* elaborated_type_specifier going right before declarators in simple declaration.
* Resolved by factoring out nested_name_specifier construct in 'class' situation.
* Resolved by specifically predicating IDENT COLON2 in 'enum' situation.
*/

elaborated_type_specifier:
        class_key COLON2? (
            (IDENT COLON2) =>
                nested_name_specifier (simple_template_id_or_IDENT | TEMPLATE simple_template_id_nocheck)
        |
             (simple_template_id_or_IDENT | TEMPLATE simple_template_id_nocheck)
        )
    |
        ENUM COLON2? (
            (IDENT COLON2)=>
                nested_name_specifier IDENT
        |
            nested_name_specifier IDENT
        |
            (IDENT)=>
                IDENT
        )
    |
        typename_specifier
    ;

// In C++0x this is factored out already
typename_specifier:
        TYPENAME COLON2? nested_name_specifier ( simple_template_id_or_IDENT  | TEMPLATE simple_template_id_nocheck )
    ;

/*
 * original rule (not needed now):
enum_name:
        IDENT
    ;
 *
 */
enum_specifier:
        ENUM IDENT? LCURL enumerator_list? RCURL
    ;
enumerator_list:
        enumerator_definition (COMMA enumerator_definition)* 
    ;
enumerator_definition:
        enumerator 
    |
        enumerator ASG constant_expression 
    ;

enumerator:
        IDENT 
    ;

/*
 * original rules (not needed now):

namespace_name:
        original_namespace_name 
    |
        namespace_alias 
    ;

original_namespace_name:
        IDENT
    ;
 *
 */

/*
 * original rules:

namespace_definition:
        named_namespace_definition
    |
        unnamed_namespace_definition
    ;

named_namespace_definition:
        original_namespace_definition
    |
        extension_namespace_definition
    ;
original_namespace_definition:
        NAMESPACE IDENT LCURL namespace_body RCURL
    ;
extension_namespace_definition:
        NAMESPACE original_namespace_name LCURL namespace_body RCURL
    ;

unnamed_namespace_definition:
        NAMESPACE LCURL namespace_body RCURL
    ;

 * This is all unnecessarily complicated. We can easily handle it by one single rule:
 */
namespace_definition:
        NAMESPACE IDENT? LCURL namespace_body RCURL
    ;

namespace_body:
        declaration[object_decl] *
    ;

namespace_alias:
        IDENT
    ;

namespace_alias_definition:
        NAMESPACE IDENT ASG qualified_namespace_specifier SEMI
    ;

qualified_namespace_specifier:
        COLON2? nested_name_specifier? IDENT
    ;

/*
 * original rule:

using-declaration:
        USING TYPENAME? COLON2? nested_name_specifier unqualified_id SEMI
     |
        USING COLON2 unqualified_id SEMI
     ;

 * Ambiguity in COLON2 between two alternatives resolved by collapsing them into one.
 * Note that new rule allows USING unqualified_id w/o COLON2, not allowed before.
 * It should be ruled out after the parsing.
 */
using_declaration
     : USING TYPENAME? COLON2? nested_name_specifier? unqualified_id SEMI
    ;

using_directive:
        USING NAMESPACE COLON2? nested_name_specifier? IDENT SEMI
    ;


asm_definition:
        ASM LPAR STRINGCONST RPAR SEMI
    ;

linkage_specification [decl_kind kind]:
        EXTERN STRINGCONST LCURL declaration[kind] * RCURL
    |
        EXTERN STRINGCONST declaration[kind]
    ;

init_declarator_list:
        init_declarator (COMMA init_declarator)*
    ;

/*
 * As per 2003 standard:
 * Ambiguity happens "between a function declaration with a redundant set of parentheses
 * around a parameter name and an object declaration with a function-style cast as the initializer."
 *
 * Thus declarator (which can end in parameters_and_qualifiers) conflicts with "()"-initializer.
 * "the resolution is to consider any construct that could possibly be a declaration a declaration".
 * Thus we take parameters_and_qualifiers as far as possible.
 *
 */
init_declarator:
        greedy_declarator initializer?
    ;

/*
 * original rule (naming as per C++0X)
declarator:
    ptr_declarator
    ;
ptr_declarator:
        noptr_declarator
    |
        ptr_operator ptr_declarator
    ;
noptr_declarator:
        declarator_id
    |
        noptr_declarator parameters-and-qualifiers
    |
        noptr_declarator LBRACK constant_expression? RBRACK
    |
        LPAR ptr_declarator RPAR
    ;
 * Ambiguity on nested_name qualifier is caused by ptr_operator vs declarator_id (of direct declarator).
 * It qualifies either STAR (for ptr_operator) or type_name (for declarator_id).
 * Resolve by syntactically probing ptr_operator first.
 */



declarator returns [declarator_type_t type]
    :
        noptr_declarator 
            {{ type = $noptr_declarator.type; }}
    |
        (ptr_operator)=>
            ptr_operator nested=declarator
                {{ type = $nested.type;
                   type.apply_ptr($ptr_operator.type);
                }}
    ;

// is quite unpretty because of left recursion removed here
noptr_declarator returns [declarator_type_t type]
    :
        (
            declarator_id
                {{ type = $declarator_id.type; }}
        |
            LPAR declarator RPAR
                {{ type = $declarator.type; }}
        ) // continued
        (
            parameters_and_qualifiers
                {{ type.apply_parameters($parameters_and_qualifiers.pq); }}
         |
             LBRACK constant_expression? RBRACK
                {{ type.apply_array($constant_expression.expr); }}
        )*
    ;

/*
 *   This rule was crafted in order to resolve ambiguity between decl_specifier (type_specifier)
 * and constructor declaration (which has declarator_id == class name).
 * For that we create a special "constructor-declarator", which is a function declarator *BUT* without a
 * leading class name.
 */
function_declarator returns [declarator_type_t type]
    :
        (constructor_declarator)=>
            constructor_declarator {{ type = $constructor_declarator.type; }}
    |
        declarator {{ type = $declarator.type; }}
    ;

constructor_declarator returns [declarator_type_t type]
    :
        parameters_and_qualifiers
            {{ type.set_constructor($parameters_and_qualifiers.pq); }}
    ;

/*

abstract_declarator:
        ptr_abstract_declarator
    ;
ptr_abstract_declarator:
        noptr_abstract_declarator
    |
        ptr_operator ptr_abstract_declarator?
    ;

noptr_abstract_declarator:
        noptr_abstract_declarator? parameters_and_qualifiers
    |
        noptr_abstract_declarator? LBRACK constant_expression RBRACK
    |
        ( ptr_abstract_declarator )
    ;
*/

abstract_declarator returns [declarator_type_t type]
    :
        noptr_abstract_declarator {{ type = $noptr_abstract_declarator.type; }}
    |
        ptr_operator decl=abstract_declarator?
            {{ type = $decl.type;
               type.apply_ptr($ptr_operator.type);
            }}
    ;

noptr_abstract_declarator returns [declarator_type_t type]
    :
        ( parameters_and_qualifiers | LBRACK constant_expression? RBRACK )+
    |
        (LPAR abstract_declarator RPAR)=>
            LPAR abstract_declarator RPAR ( parameters_and_qualifiers | LBRACK constant_expression? RBRACK )*
    ;

universal_declarator returns [declarator_type_t type]
options { backtrack = true; }
    :
        declarator { type = $declarator.type; }
    |
        abstract_declarator { type = $abstract_declarator.type; }
    ;

greedy_declarator returns [declarator_type_t type]
    :
        greedy_nonptr_declarator {{ type = $greedy_nonptr_declarator.type; }}
    |
        (ptr_operator)=>
            ptr_operator decl=greedy_declarator
            {{ type = $decl.type;
               type.apply_ptr($ptr_operator.type);
            }}
    ;

/*
 * This is to resolve ambiguity between declarator and subsequent (expression) initializer in init_declarator.
 * Eat as much parameter sets as possible.
 */
greedy_nonptr_declarator returns [declarator_type_t type]
    :
        (
            declarator_id
                {{ type = $declarator_id.type; }}
        |
            LPAR greedy_declarator RPAR
                {{ type = $greedy_declarator.type; }}
        ) // continued
        (
            (parameters_and_qualifiers)=>
                parameters_and_qualifiers
                {{ type.apply_parameters($parameters_and_qualifiers.pq); }}
        |
            LBRACK constant_expression? RBRACK
                {{ type.apply_array($constant_expression.expr); }}
        )*
    ;

ptr_operator returns [ declarator_type_t type ]
    :
        STAR cv_qualifier*
            {{ type.set_ptr(NULL, $cv_qualifier.qual); }}
    |
        AMPERSAND 
            {{ type.set_ref(); }}
    |
        COLON2? nested_name_specifier STAR cv_qualifier*
            {{ type.set_ptr(& $nested_name_specifier.namequal, $cv_qualifier.qual); }}
    ;

cv_qualifier returns [ qualifier_t qual ]:
        CONST {{ qual = CONST; }}
    |
        VOLATILE {{ qual = VOLATILE; }}
    ;

/*
 * original rule:

    |
        COLON2? nested_name_specifier? type_name 

 * This alternative deleted, as it actually is contained in id_expression
 */

declarator_id returns [ declarator_type_t type ] :
        id_expression {{ type.set_ident(); }}
    ;

/*
 * from 8.2 Ambiguity resolution:
 * "any construct that could possibly be a type-id in its syntactic context
 * shall be considered a type-id"
 */
type_id:
        type_specifier+ abstract_declarator?
    ;

parameters_and_qualifiers returns [ parameters_and_qualifiers_t pq ]
    :

        LPAR parameter_declaration_clause RPAR cv_qualifier* exception_specification?
    ;

parameter_declaration_clause
scope Declaration; /* need it zero'ed to handle hoisted type_specifier predicate */
@init { init_declaration(CTX, parm_decl); }
    :
        DOT3?
    |
        parameter_declaration_list (COMMA? DOT3)?
    ;

parameter_declaration_list:
        parameter_declaration[parm_decl] (COMMA parameter_declaration[parm_decl])*
    ;
parameter_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier+ universal_declarator? (ASG assignment_expression)?
    ;

/*
 * original rule:

function_definition:
        decl_specifier* declarator ctor_initializer? function_body 
    |
        decl_specifier* declarator function_try_block
    ;

 * Factoring out a sequence that follows declarator, as it helps disambiguating in context when
 * function_definition conflicts because of decl_specifier
 */
function_definition_after_declarator:
        ctor_initializer? function_body
    |
        function_try_block
    ;

/*
 * We have a baaad conflict caused by declaration w/o decl_specifier,
 * that is w/o return type specification.
 *
 * In old K&R C times this was an "implicit int" declaration.
 * Currently we allow only constructors/destructors to have no return type
 * (and surely it does not mean "implicit int").
 *
 * However constructor's name conflicts with type_specifier of an ordinary declaration.
 *
 * This conflict rises for any function declaration
 */

function_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier* function_declarator
    ;

function_definition [decl_kind kind]:
        function_declaration[kind] function_definition_after_declarator
    ;

function_body:
        compound_statement 
    ;

initializer:
        ASG initializer_clause 
    |
        LPAR expression_list RPAR 
    ;
initializer_clause:
        assignment_expression 
    |
        LCURL initializer_list COMMA? RCURL
    |
        LCURL RCURL
    ;
initializer_list:
        initializer_clause (COMMA initializer_clause )*
    ;

//[gram.class] 
class_name:
        simple_template_id_or_IDENT 
    ;

class_specifier:
        class_head LCURL member_specification? RCURL
    ;

/*
 * Original rule:

class_head:
        class_key IDENT? base_clause? 
    |
        class_key nested_name_specifier IDENT base_clause? 
    |
        class_key nested_name_specifier? simple_template_id base_clause? 
    ;

*  Ambiguity due to nested_name_specifier usage
*/
optionally_qualified_name
    :
        nested_name_specifier? simple_template_id_or_IDENT
    ;

class_head:
        class_key optionally_qualified_name? base_clause?
    ;

class_key:
        CLASS 
    |
        STRUCT
    |
        UNION 
    ;
member_specification :
        member_declaration[field_decl] member_specification?
    |
        access_specifier COLON member_specification?
    ;


/*
 * original rule (part that was rewritten)

 member_declaration:
        decl_specifier* member_declarator_list? SEMI
    |
        function_definition SEMI?
    |
        COLON2? nested_name_specifier TEMPLATE? unqualified_id SEMI
    |

member_declarator:
        declarator constant_initializer?
    |
        IDENT? COLON constant_expression
    ;

 *
 * (optional SEMI? deleted after function_defition, as the first alternative takes care of it already)
 * Conflict on decl_specifier between first alternative and second one (function_definition) resolved
 * by factorizing on common parts of the first member_declarator (decl_specifier* declarator).
 * It was pretty involved, and besides member_declaration also affecting 3 other rules.
 *
 * Another conflict is between first set of alternatives and access declaration.
 * Access declaration is being subsumed by member declaration with absent decl_specifier.
 * There needs to be a special semantic check for "access declaration" when handling results of member declaration.
 */
member_declaration [decl_kind kind]
scope Declaration;
@init { init_declaration(CTX, kind); }
    :
        decl_specifier*
        (
            (IDENT? COLON)=>
                member_bitfield_declarator ( COMMA member_declarator )* SEMI
        |
            (constructor_declarator)=>
                constructor_declarator
                (
                    // this was member_declarator_list
                    ( COMMA member_declarator )* SEMI
                |
                    function_definition_after_declarator
                )
        |
            declarator
            (
                { $declarator.type.is_function() }?
                    function_definition_after_declarator
            |
                // this was member_declarator_list
                constant_initializer? ( COMMA member_declarator )* SEMI
            )
        |
            SEMI
        )
    |
        /* this is likely to be covered by decl_specifier/declarator part of member_declarator
            COLON2? nested_name_specifier TEMPLATE? unqualified_id SEMI
    |
        */

        using_declaration
    |
        template_declaration[kind]
    ;

member_bitfield_declarator:
        IDENT? COLON constant_expression
    ;

member_declarator:
        declarator constant_initializer?
    |
        member_bitfield_declarator
    ;

/*
 * original rule:

member_declarator_list:
        member_declarator ( COMMA member_declarator )*
    ;

 *
 * No longer needed as this list was inserted into member_declaration rule in order to
 * factorize first member_declaration entry.
 */

// = 0 (not used, as it conflicts with constant_initializer
pure_specifier:
        ASG literal
    ;

constant_initializer:
        ASG constant_expression 
    ;

// [gram.class.derived] 
base_clause:
        COLON base_specifier_list 
    ;
base_specifier_list:
        base_specifier ( COMMA base_specifier )*
    ;
base_specifier:
        COLON2? nested_name_specifier? class_name 
    |
        VIRTUAL access_specifier? COLON2? nested_name_specifier? class_name 
    |
        access_specifier VIRTUAL? COLON2? nested_name_specifier? class_name 
    ;
access_specifier:
        PRIVATE
    |
        PROTECTED
    |
        PUBLIC
    ;

// [gram.special] 
conversion_function_id:
        OPERATOR conversion_type_id 
    ;
/*
 * original rule:

conversion_type_id:
        type_specifier+ conversion_declarator?
    ;
conversion_declarator:
        ptr_operator+
    ;

 * As per 2003 standard:
 * "The conversion-type-id in a conversion-function-id is the longest possible sequence
 *  of conversion-declarators... This prevents ambiguities between the declarator operator *
 *  and its expression counterparts."
 *
 * Resolve by folding and adding a synpred.
 */
conversion_type_id:
        type_specifier+
        ((ptr_operator)=> ptr_operator)*
    ;

ctor_initializer:
        COLON mem_initializer_list
    ;

mem_initializer_list:
        mem_initializer ( COMMA mem_initializer )*
    ;

mem_initializer:
        mem_initializer_id LPAR expression_list? RPAR 
    ;

/*
 * original rule:
mem_initializer_id:
        COLON2? nested_name_specifier? class_name 
    |
        IDENT 
    ;
 * Ambiguity resolved by removing special class_name case
 */
mem_initializer_id:
        COLON2? nested_name_specifier? IDENT
    ;

// [gram.over] 
operator_function_id:
        OPERATOR operator_id ( { operator_is_template() }?=> LSS template_argument_list? GTR)?
    ;
/*
 * Ambiguity between operator new/delete and operator new/delete[] resolved towards the latter.
 */
operator_id returns [int id]:
        (NEW LBRACK RBRACK)=>
            NEW LBRACK RBRACK |
        (TOK_DELETE LBRACK RBRACK)=>
            TOK_DELETE LBRACK RBRACK |
        NEW | TOK_DELETE |
        PLUS | MINUS | STAR | DIV | MOD | XOR | AMPERSAND | BITOR | TILDE |
        NOT | ASG | LSS | GTR | PLUS_ASG | MINUS_ASG | MUL_ASG | DIV_ASG | MOD_ASG |
        XOR_ASG | AND_ASG | OR_ASG | LSHIFT | RSHIFT | RSHIFT_ASG | LSHIFT_ASG | EQ | NEQ |
        LEQ | GEQ | AND | OR | PLUSPLUS | MINUSMINUS | COMMA | ARROWSTAR | ARROW | 
        LPAR RPAR | LBRACK RBRACK
    ;

// [gram.temp] 
template_declaration [decl_kind kind]:
        EXPORT? TEMPLATE LSS template_parameter_list GTR declaration[kind]
    ;

template_parameter_list:
        template_parameter ( COMMA template_parameter )*
    ;

/*
 * Ambiguity resolution for CLASS {IDENT,GTR,COMMA,ASG} conflict between type_parameter
 * and type_specifier, which starts parameter_declaration.
 * To resolve this ambiguity just make an additional type_parameter syntactically predicated
 * with this fixed lookahead.
 *
 * Note that COMMA comes from template_parameter_list rule and GTR comes even further from 
 * template_declaration rule
*/
template_parameter:
    (CLASS ( IDENT | GTR | COMMA | ASG ) )=>
        type_parameter
    |
        // this should map the rest of type_parameter that starts differently from above
        type_parameter
    |
        parameter_declaration[tparm_decl]
    ;
type_parameter:
        CLASS IDENT? 
    |
        CLASS IDENT? ASG type_id 
    |
        TYPENAME IDENT? 
    |
        TYPENAME IDENT? ASG type_id 
    |
        TEMPLATE LSS template_parameter_list GTR CLASS IDENT? (ASG id_expression)?
    ;

simple_template_id
    :
        IDENT LSS { (identifier_is(IDT_TEMPLATE_NAME)) }?
            template_argument_list? GTR
    ;
lookup_simple_template_id
    :
        IDENT LSS { (identifier_is(IDT_TEMPLATE_NAME)) }?
            look_after_tmpl_args
    ;

simple_template_id_nocheck
    :
        IDENT LSS template_argument_list? GTR
    ;
lookup_simple_template_id_nocheck
    :
        IDENT LSS look_after_tmpl_args
    ;

simple_template_id_or_IDENT
    :
        IDENT
        ( (LSS { (identifier_is(IDT_TEMPLATE_NAME)) }?) =>
            LSS template_argument_list? GTR
        )?
    ;

lookup_simple_template_id_or_IDENT
    :
        IDENT
        ( { (identifier_is(IDT_TEMPLATE_NAME)) }?=>
            LSS look_after_tmpl_args
        )?
    ;

/*
 * original rule:
template_name:
        IDENT
    ;
 * not needed
 */

template_argument_list:
        template_argument ( COMMA template_argument )*
    ;
template_argument:
        // id_exression is included into assignment_expression, thus we need to explicitly rule it up
        (id_expression)=> id_expression
    |
        (type_id)=> type_id
    |
        assignment_expression
    ;

explicit_instantiation [decl_kind kind]:
        TEMPLATE declaration[kind]
    ;
explicit_specialization [decl_kind kind]:
        TEMPLATE LSS GTR declaration[kind]
    ;
// [gram.except] 
try_block:
        TRY compound_statement handler+
    ;
function_try_block:
        TRY ctor_initializer? function_body handler+
    ;

handler:
        CATCH LPAR exception_declaration RPAR compound_statement 
    ;

/*
 * original rule:
exception_declaration:
        type_specifier+ declarator
    |
        type_specifier+ abstract_declarator?
    |
        DOT3
    ;

 * Ambiguity in declarator vs abstract_declarator resolved by moving it into universal_declarator
 */
exception_declaration
scope Declaration;
@init { init_declaration(CTX, blockscope_decl); }
    :
        type_specifier+ universal_declarator?
    |
        DOT3
    ;
throw_expression:
        THROW assignment_expression? 
    ;
exception_specification:
        THROW LPAR type_id_list? RPAR 
    ;
type_id_list:
        type_id ( COMMA type_id )*
    ;

// EXPRESSIONS
// [gram.expr]
primary_expression:
        literal
    |
        THIS
    |
        LPAR expression RPAR 
    |
        id_expression 
    ;

/*
 * original rule:
postfix_expression:
        primary_expression
    |
        postfix_expression LBRACK expression RBRACK
    |
        postfix_expression LPAR expression_list? RPAR
    |
        simple_type_specifier LPAR expression_list? RPAR
    |
        TYPENAME COLON2? nested_name_specifier IDENT LPAR expression_list? RPAR
    |
        TYPENAME COLON2? nested_name_specifier TEMPLATE? template_id LPAR expression_list? RPAR
    |
        postfix_expression DOT TEMPLATE? id_expression
    |
        postfix_expression ARROW TEMPLATE? id_expression
    |
        postfix_expression DOT pseudo_destructor_name
    |
        postfix_expression ARROW pseudo_destructor_name
    |
        postfix_expression PLUSPLUS
    |
        postfix_expression MINUSMINUS
    |
        dynamic_cast LSS type_id GTR LPAR expression RPAR
    |
        static_cast LSS type_id GTR LPAR expression RPAR
    |
        reinterpret_cast LSS type_id GTR LPAR expression RPAR
    |
        const_cast LSS type_id GTR LPAR expression RPAR
    |
        typeid LPAR expression RPAR
    |
        typeid LPAR type_id RPAR
    ;
/*
 * Left recursion removed by moving non-recursive into basic_postfix_expression and applying "recursive"
 * parts by a loop on top of it.
 *
 * "pseudo-destructor-name" thing is heavily conflicting with id_expression,
 * so it does not make any sense to introduce. This means that id_expression should
 * allow everything pseudo-destructor-name allows, and then be semantically checked later.
 */
postfix_expression:
        basic_postfix_expression
        (
            LBRACK expression RBRACK
        |
            LPAR expression_list? RPAR
        |
            DOT
            (
                TEMPLATE? id_expression
//            |
//                pseudo_destructor_name
            )
        |
            ARROW
            (
                TEMPLATE? id_expression
//            |
//                pseudo_destructor_name
            )
        |
            PLUSPLUS
        |
            MINUSMINUS
        )*
    ;

basic_postfix_expression:
        primary_expression
    |
        simple_type_specifier LPAR expression_list? RPAR
    |
        TYPENAME COLON2? nested_name_specifier (
            IDENT LPAR expression_list? RPAR
        |
            TEMPLATE? simple_template_id LPAR expression_list? RPAR
        )
    |
        DYNAMIC_CAST LSS type_id GTR LPAR expression RPAR
    |
        STATIC_CAST LSS type_id GTR LPAR expression RPAR
    |
        REINTERPRET_CAST LSS type_id GTR LPAR expression RPAR
    |
        CONST_CAST LSS type_id GTR LPAR expression RPAR
    |
        // AMB
        // expression and type_id conflict in "simple_type_specifier"
        // rule up type_id, as it should be easier to check
        TYPEID LPAR ( (type_id)=> type_id |  expression ) RPAR
    ;

expression_list:
        assignment_expression ( COMMA assignment_expression )*
    ;
/*
 * original rule:
pseudo_destructor_name:
        COLON2? nested_name_specifier? type_name COLON2 TILDE type_name
    |
        COLON2? nested_name_specifier TEMPLATE simple_template_id COLON2 TILDE type_name
    |
        COLON2? nested_name_specifier? TILDE type_name
    ;

 * A healthy dose of left-factoring solves the issue.
 *
 * This rule is not used anymore

pseudo_destructor_name:
        COLON2?
        (
            nested_name_specifier? TEMPLATE? IDENT COLON2 TILDE IDENT
        |
            nested_name_specifier TEMPLATE simple_template_id COLON2 TILDE IDENT
        )
    ;
 *
 */

/*
 * ambiguity between postfix_expression and new/delete_expression caused by presence of
 * id_expression in a former alternative and is problematic to resolve.
 * For now just synpred on new/delete. Reconsider if it appears to be costly.
 *
 * As per 2003 standard:
 * "There is an ambiguity in the unary-expression ~X(), where X is a class-name.
 * The ambiguity is resolved in favor of treating ~ as a unary complement rather than
 * treating ~X as referring to a destructor."
 */
unary_expression:
       (TILDE cast_expression)=>
             TILDE cast_expression
    |
        (new_expression)=>
            new_expression
    |
        (delete_expression)=>
            delete_expression
    |
        postfix_expression
    |
        PLUSPLUS cast_expression
    |
        MINUSMINUS cast_expression
    |
        unary_operator_but_not_TILDE cast_expression
    |
        SIZEOF (
            unary_expression
        |
            (LPAR type_id RPAR)=>
                LPAR type_id RPAR
        )
    ;

unary_operator:
        unary_operator_but_not_TILDE | TILDE
    ;
unary_operator_but_not_TILDE:
        STAR | AMPERSAND | PLUS | MINUS | NOT
    ;

/*
 * original rule:

new_expression:
        COLON2? NEW new_placement? new_type_id new_initializer? 
    |
        COLON2? NEW new_placement? LPAR type_id RPAR new_initializer? 
    ;

 *
 * Complication appears due to the optional new_placement and (type_id).
 * Unhealthy dose of left-factoring solves this issue.
 */
new_expression:
        COLON2? NEW
        (
            new_placement ( new_type_id | LPAR type_id RPAR )
        |
            (LPAR type_id RPAR)=>
                LPAR type_id RPAR
        |
            new_type_id
        ) new_initializer?
    ;

new_placement:
        LPAR expression_list RPAR 
    ;

/*
 * As per 2003 standard:
 * "The new-type-id in a new-expression is the longest possible sequence of new-declarators"
 *
 * As all the ambiguities in new_type_id seem to come from new_declarator's ptr_operator
 * force it by synpreds.
 *
 * Is this resolution correct??
 *  new (int(*p)) int; // new-placement expression
 */
new_type_id:
        type_specifier+
        ((ptr_operator)=>
            new_declarator)?
    ;

new_declarator:
        (ptr_operator)=>
            ptr_operator new_declarator
    |
        direct_new_declarator
    ;

direct_new_declarator:
        LBRACK expression RBRACK ( LBRACK constant_expression RBRACK )*
    ;

new_initializer:
        LPAR expression_list? RPAR
    ;
delete_expression:
        COLON2? TOK_DELETE cast_expression
    |
        COLON2? TOK_DELETE LBRACK RBRACK cast_expression
    ;

cast_expression :
        (LPAR type_id RPAR)=>
            LPAR type_id RPAR cast_expression
    |
        unary_expression
    ;

pm_expression :
        cast_expression ( DOTSTAR cast_expression | ARROWSTAR cast_expression ) *
    ;

multiplicative_expression:
        pm_expression
        (
            STAR pm_expression
        |
            DIV pm_expression
        |
            MOD pm_expression
        )*
    ;

additive_expression:
        multiplicative_expression ( PLUS multiplicative_expression | MINUS multiplicative_expression )*
    ;

shift_expression:
        additive_expression ( LSHIFT additive_expression | RSHIFT additive_expression )*
    ;

/*
 * GTR ambiguity (GTR in relational expression vs GTR closing template arguments list) is one of
 * C++ dumbest ambiguities. Resolve it by tracking whether expression is a top-level expression (e.g. not
 * parenthesized) and parsed in a context of template argument - then do not accept is as a continuation of
 * relational expression.
 */
relational_expression:
        shift_expression
        ( 
            { !top_level_of_template_arguments() }?=>
            GTR shift_expression
          |
            LSS shift_expression
          |
            LEQ shift_expression
          |
            GEQ shift_expression
        )*
    ;
equality_expression:
        relational_expression ( EQ relational_expression | NEQ relational_expression)*
    ;
and_expression:
        equality_expression ( AMPERSAND equality_expression )*
    ;
exclusive_or_expression:
        and_expression ( XOR and_expression )*
    ;
inclusive_or_expression:
        exclusive_or_expression ( BITOR exclusive_or_expression )*
    ;
logical_and_expression:
        inclusive_or_expression ( AND inclusive_or_expression )*
    ;
logical_or_expression:
        logical_and_expression ( OR logical_and_expression )*
    ;
conditional_expression:
        logical_or_expression (QUESTION expression COLON assignment_expression)?
    |
        QUESTION expression COLON assignment_expression
    ;
/*
 * These are the example of "precedence climbing" implementation
 *

binary_operator returns [ int prec]:
        PLUS| MINUS |
        STAR | DIV | MOD | XOR | AMPERSAND | BITOR |
        NOT | LSS | GTR |
        LSHIFT | RSHIFT |
        EQ | NEQ | LEQ | GEQ | AND | OR
    ;

fast_expression:
        climbing_expression[0]
    ;

climbing_expression [int prio]:
        primary_climbing
        ((binary_operator { $binary_operator.prec >= prio }? )=>
         binary_operator  climbing_expression[$binary_operator.prec+1])?
    ;

primary_climbing:
        unary_operator climbing_expression[$unary_operator.prec]
    ;
*/

/*
 * original rule:

assignment_expression:
        conditional_expression 
    |
        logical_or_expression assignment_operator assignment_expression 
    |

 * Ambiguity on logical_or_expression in assignment vs conditional_expression.
 * Resolved by unpretty rule-splitting and left-factoring.
 */
assignment_expression:
        // this is taken from conditional_expression
        QUESTION expression COLON assignment_expression
    |
        logical_or_expression (
            // this is taken from conditional_expression
            (QUESTION expression COLON assignment_expression)?
        |
            assignment_operator assignment_expression
        )
    |
        throw_expression
    ;

assignment_operator:
        ASG | MUL_ASG | DIV_ASG | MOD_ASG | PLUS_ASG | MINUS_ASG | RSHIFT_ASG | LSHIFT_ASG |
        AND_ASG | XOR_ASG | OR_ASG
    ;

expression:
        assignment_expression ( COMMA assignment_expression )*
    ;

constant_expression returns [ expression_t expr ]
    :
        conditional_expression
    ;

// [gram.lex]

literal:
    INTCONST|REALCONST|CHARCONST|STRINGCONST
    ;

// lookahead stuff
// token list arg_syms from parseutil.cc, to implement look_after_tmpl_args

// $<Look ahead

lookahead_tokenset_arg_syms
    :
        IDENT|INTCONST|REALCONST|CHARCONST|STRINGCONST|
        PLUS|MINUS|STAR|AMPERSAND|SIZEOF|TILDE|
        NOT|PLUSPLUS|MINUSMINUS|OPERATOR|NEW|TOK_DELETE|
        THIS|
        VOID|CHAR|SHORT|LONG|FLOAT|DOUBLE|SIGNED|UNSIGNED|INT|
        DIV|LSHIFT|RSHIFT|BITOR|AND|OR|XOR|
        EQ|LEQ|GEQ|NEQ|
        ASG|AND_ASG|DIV_ASG|LSHIFT_ASG|RSHIFT_ASG|MINUS_ASG|PLUS_ASG|
        MOD_ASG|MUL_ASG|OR_ASG|XOR_ASG|DOT|MOD|
        ARROW|QUESTION|COLON|COLON2|DOTSTAR|ARROWSTAR|COMMA|DOT3|
        TYPEDEF|EXTERN|STATIC|AUTO|REGISTER|THREAD|
        CONST|VOLATILE|STRUCT|UNION|CLASS|ENUM|TYPENAME|
        OFFSETOF|ALIGNOF|THROW|WCHAR_T|TYPEID|
        CONST_CAST|STATIC_CAST|DYNAMIC_CAST|REINTERPRET_CAST|
        BOOL|TRUE|FALSE|
        LD_GLOBAL|LD_SYMBOLIC|LD_HIDDEN|DECLSPEC|
        ATTRIBUTE|TYPEOF|
        IS_ENUM|IS_UNION|IS_CLASS|IS_POD|IS_ABSTRACT|HAS_VIRT_DESTR|IS_EMPTY|IS_BASEOF|IS_POLYMORPH
    ;

look_after_tmpl_args
scope {
    int level;
}
@init{ 
    $look_after_tmpl_args::level = 0;
    int npar = 0;
    int nbrac = 0;
}
    :
        (
            // this gets us out if GTR is met when level == 0
            (GTR {
                    ($look_after_tmpl_args::level > 0)
                  }? )=>
            GTR
                {{ if (npar == 0 && nbrac == 0) {
                            $look_after_tmpl_args::level--;
                            println("level-- (", $look_after_tmpl_args::level);
                        }
                }}
        |
            LSS {{ if (npar == 0 && nbrac == 0) {
                            $look_after_tmpl_args::level++;
                            println("level++ (", $look_after_tmpl_args::level);
                    }
                }}
        |
            LPAR {{ npar++; }}
        |
            RPAR {{ if (npar > 0) npar--; }}
        |
            LBRACK {{ nbrac++; }}
        |
            RBRACK {{ if (nbrac > 0) nbrac--; }}
        |
            lookahead_tokenset_arg_syms
        )* GTR
    ;

skip_balanced_Curl
            :
            LCURL
            (options {greedy=false;}:
                skip_balanced_Curl | .
            )*
            RCURL
        ;

// $>
// ==============
//

// TEMPLATE: 'template';
// COLON: ':'; COLON2: '::';

// DOT: '.'; DOT3: '...';
// MINUS: '-'; PLUS: '+'; MINUSMINUS: '--'; PLUSPLUS: '++'; PLUS_ASG: '+='; MINUS_ASG: '-=';

// ARROW: '->'; 
// STAR: '*'; MUL_ASG: '*='; DOTSTAR: '.*'; ARROWSTAR: '->*';
// DIV: '/'; DIV_ASG: '/=';
// MOD: '%'; MOD_ASG: '%=';
// NOT: '!';
// ASG: '=';
// EQ: '=='; LEQ: '<='; GEQ: '>='; NEQ: '!=';
// AMPERSAND: '&'; AND: '&&'; AND_ASG: '&=';
// BITOR: '|'; OR: '||'; OR_ASG: '|=';
// XOR_ASG: '^='; XOR: '^'; 
// LSHIFT: '<<'; RSHIFT: '>>'; LSHIFT_ASG: '<<='; RSHIFT_ASG: '>>=';
// THIS: 'this';
// TYPENAME: 'typename';
// TYPEID: 'typeid';
// LPAR: '('; RPAR: ')';
// LBRACK: '['; RBRACK: ']';
// LCURL: '{'; RCURL: '}';
// LSS: '<'; GTR: '>';

// CHAR: 'char';
// WCHAR_T: 'wchar_t';
// BOOL: 'bool'; 
// TRUE: 'true';
// FALSE: 'false';
// SHORT: 'short';
// INT: 'int';
// LONG: 'long';
// SIGNED: 'signed';
// UNSIGNED: 'unsigned';
// FLOAT: 'float';
// DOUBLE: 'double';
// VOID: 'void';

// ENUM: 'enum'; CLASS: 'class'; STRUCT: 'struct'; UNION: 'union';

// DYNAMIC_CAST: 'dynamic_cast';
// STATIC_CAST: 'static_cast';
// REINTERPRET_CAST: 'reinterpret_cast';
// CONST_CAST: 'const_cast';

// COMMA: ',';
// TILDE: '~';

// NEW: 'new'; TOK_DELETE: 'delete';
// NAMESPACE: 'namespace'; USING: 'using';

// OPERATOR: 'operator';

// FRIEND: 'friend';
// TYPEDEF: 'typedef';
// AUTO: 'auto';
// REGISTER: 'register';
// STATIC: 'static';
// EXTERN: 'extern';
// MUTABLE: 'mutable';
// INLINE: 'inline';
// VIRTUAL: 'virtual';
// EXPLICIT: 'explicit';
// EXPORT: 'export';
// PRIVATE: 'private';
// PROTECTED: 'protected';
// PUBLIC: 'public';
// SEMI: ';';

// TRY: 'try'; CATCH: 'catch'; THROW: 'throw';

// CONST: 'const'; VOLATILE: 'volatile';
// ASM: 'asm';
// BREAK: 'break'; CONTINUE: 'continue'; RETURN: 'return';

// GOTO: 'goto';
// FOR: 'for'; WHILE: 'while'; DO: 'do';
// IF: 'if'; ELSE: 'else';
// SWITCH: 'switch'; CASE: 'case'; DEFAULT: 'default';

// QUESTION: '?'; SIZEOF: 'sizeof';
// OFFSETOF: '__offsetof';
// THREAD: '__thread';

// LD_GLOBAL: '__global';
// LD_SYMBOLIC: '__symbolic';
// LD_HIDDEN: '__hidden';

// DECLSPEC: '__declspec';
// ATTRIBUTE: '__attribute__';
// TYPEOF: '__typeof__';
// ALIGNOF: '__alignof';

// PP_PRAGMA: '_Pragma';

// HAS_TRIVIAL_DESTR: '__oracle_has_trivial_destructor';
// HAS_VIRTUAL_DESTR: '__oracle_has_virtual_destructor';
// IS_ENUM: '__oracle_is_enum';
// IS_UNION: '__oracle_is_union';
// IS_CLASS: '__oracle_is_class';
// IS_POD: '__oracle_is_pod';
// IS_ABSTRACT: '__oracle_is_abstract';
// IS_EMPTY: '__oracle_is_empty';
// IS_POLYMORPH: '__oracle_is_polymorphic';
// IS_BASEOF: '__oracle_is_base_of';

// CHARACTER_LITERAL
//     :   '\'' ( EscapeSequence | ~('\''|'\\') ) '\''
//     ;

// STRING
//     :  '"' STRING_GUTS '"'
//     ;

// fragment
// STRING_GUTS :	( EscapeSequence | ~('\\'|'"') )* ;

// fragment
// HEX_LITERAL : '0' ('x'|'X') HexDigit+ IntegerTypeSuffix? ;

// fragment
// DECIMAL_LITERAL : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix? ;

// fragment
// OCTAL_LITERAL : '0' ('0'..'7')+ IntegerTypeSuffix? ;

// fragment
// HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

// fragment
// IntegerTypeSuffix
//     :	('l'|'L')
//     |	('u'|'U')  ('l'|'L')?
//     ;

// fragment
// Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

// fragment
// FloatTypeSuffix : ('f'|'F'|'d'|'D') ;

// fragment
// EscapeSequence
//     :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
//     |   OctalEscape
//     ;

// fragment
// OctalEscape
//     :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
//     |   '\\' ('0'..'7') ('0'..'7')
//     |   '\\' ('0'..'7')
//     ;

// fragment
// UnicodeEscape
//     :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
//     ;

// WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
//     ;

// COMMENT
//     :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
//     ;

// LINE_COMMENT
//     : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
//     ;

// LITERAL:
//         HEX_LITERAL
//     |
//         DECIMAL_LITERAL
//     |
//         OCTAL_LITERAL
//     ;

// IDENT
//     :	LETTER (LETTER|'0'..'9')*
//     ;
	
// fragment
// LETTER
//     :	'$'
//     |	'A'..'Z'
//     |	'a'..'z'
//     |	'_'
//     ;
