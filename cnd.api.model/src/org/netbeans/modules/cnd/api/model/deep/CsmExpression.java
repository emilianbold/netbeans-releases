/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.model.deep;

import java.util.List;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.TypeSafeEnum;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * Represents expression
 * @author Vladimir Kvashin
 */
public interface CsmExpression extends CsmOffsetable, CsmObject {

    //TODO: check in accordance to C++ standard
    public class Kind extends TypeSafeEnum {

        private Kind(String id) {
            super(id);
        }
        
        // TODO: fill all constructor parameters
        
        //
        // Primary expressions
        //
        
        // TODO: perhaps it isn't worth making separate kinds for different literals
        public static final Kind INTEGER_LITERAL                = new Kind( "INTEGER_LITERAL" ); // NOI18N
        public static final Kind CHAR_LITERAL                   = new Kind( "CHAR_LITERAL" ); // NOI18N
        public static final Kind FLOAT_LITERAL                  = new Kind( "FLOAT_LITERAL" ); // NOI18N
        public static final Kind STRING_LITERAL                 = new Kind( "STRING_LITERAL" ); // NOI18N
        public static final Kind BOOLEAN_LITERAL                = new Kind( "BOOLEAN_LITERAL" ); // NOI18N
        
        public static final Kind THIS                           = new Kind( "THIS" ); // NOI18N
        public static final Kind PRIMARY_BRACKETED              = new Kind( "PRIMARY_BRACKETED" ); // NOI18N
        
        public static final Kind REFERENCE                      = new Kind( "REFERENCE" ); // AKA id-expression // NOI18N
        
        
        //
        // Postfix expressions
        //
        public static final Kind SUBSCRIPT              = new Kind( "SUBSCRIPT" ); // a[] // NOI18N
        public static final Kind FUNCTIONCALL           = new Kind( "FUNCTIONCALL" ); // NOI18N
        // TODO: what is " postfix-expression ( expression-list ) " ?
        // TODO: add 
        public static final Kind SIMPLETYPE_INT		= new Kind( "" );
        public static final Kind SIMPLETYPE_SHORT	= new Kind( "" );
        public static final Kind SIMPLETYPE_DOUBLE	= new Kind( "" );
        public static final Kind SIMPLETYPE_FLOAT       = new Kind( "" );
        public static final Kind SIMPLETYPE_CHAR        = new Kind( "" );
        public static final Kind SIMPLETYPE_WCHART      = new Kind( "" );
        public static final Kind SIMPLETYPE_SIGNED      = new Kind( "" );
        public static final Kind SIMPLETYPE_UNSIGNED    = new Kind( "" );
        public static final Kind SIMPLETYPE_BOOL        = new Kind( "" );
        public static final Kind SIMPLETYPE_LONG        = new Kind( "" );
        
        public static final Kind TYPENAME_IDENTIFIER    = new Kind( "" );
        public static final Kind TYPENAME_TEMPLATEID    = new Kind( "" );
        
        public static final Kind DOT_IDEXPRESSION       = new Kind( "" );
        public static final Kind ARROW_IDEXPRESSION     = new Kind( "" );
        public static final Kind DOT_TEMPL_IDEXPRESS    = new Kind( "" );
        public static final Kind ARROW_TEMPL_IDEXP      = new Kind( "" );

        public static final Kind DOT_DESTRUCTOR         = new Kind( "" );
        public static final Kind ARROW_DESTRUCTOR       = new Kind( "" );

        public static final Kind POST_INCREMENT         = new Kind( "" );
        public static final Kind POST_DECREMENT         = new Kind( "" );
        
        public static final Kind DYNAMIC_CAST           = new Kind( "" );
        public static final Kind REINTERPRET_CAST       = new Kind( "" );
        public static final Kind STATIC_CAST            = new Kind( "" );
        public static final Kind CONST_CAST             = new Kind( "" );
        public static final Kind TYPEID_EXPRESSION      = new Kind( "" );
        public static final Kind TYPEID_TYPEID          = new Kind( "" );
        
        //
        // Unary expressions
        //
        public static final Kind PRE_INCREMENT                = new Kind( "" );
        public static final Kind PRE_DECREMENT                = new Kind( "" );
        public static final Kind STAR_CASTEXPRESSION      = new Kind( "" );
        public static final Kind AMPSND_CASTEXPRESSION    = new Kind( "" );
        public static final Kind PLUS_CASTEXPRESSION      = new Kind( "" );
        public static final Kind MINUS_CASTEXPRESSION     = new Kind( "" );
        public static final Kind NOT_CASTEXPRESSION       = new Kind( "" );
        public static final Kind TILDE_CASTEXPRESSION     = new Kind( "" );
        public static final Kind SIZEOF_UNARYEXPRESSION   = new Kind( "" );
        public static final Kind SIZEOF_TYPEID            = new Kind( "" );
        
        public static final Kind NEW_NEWTYPEID                  = new Kind( "" );
        public static final Kind NEW_TYPEID                     = new Kind( "" );
        public static final Kind DELETE_CASTEXPRESSION          = new Kind( "" );
        public static final Kind DELETE_VECTORCASTEXPRESSION    = new Kind( "" );
        
        public static final Kind CASTEXPRESSION                 = new Kind( "" );
        public static final Kind PM_DOTSTAR                     = new Kind( "" );
        public static final Kind PM_ARROWSTAR                   = new Kind( "" );
        public static final Kind MULTIPLICATIVE_MULTIPLY        = new Kind( "" );
        public static final Kind MULTIPLICATIVE_DIVIDE          = new Kind( "" );
        public static final Kind MULTIPLICATIVE_MODULUS         = new Kind( "" );
        public static final Kind ADDITIVE_PLUS                  = new Kind( "" );
        public static final Kind ADDITIVE_MINUS                 = new Kind( "" );
        public static final Kind SHIFT_LEFT                     = new Kind( "" );
        public static final Kind SHIFT_RIGHT                    = new Kind( "" );
        
        //
        // Relational
        //
        public static final Kind LESSTHAN            = new Kind( "" );
        public static final Kind GREATERTHAN         = new Kind( "" );
        public static final Kind LESSTHANEQUALTO     = new Kind( "" );
        public static final Kind GREATERTHANEQUALTO  = new Kind( "" );
        
        //
        // Equality
        //
        public static final Kind EQUALS                = new Kind( "" );
        public static final Kind NOTEQUALS             = new Kind( "" );
        
        public static final Kind BITAND                  = new Kind( "" );
        public static final Kind EXCLUSIVEOR          = new Kind( "" );
        public static final Kind INCLUSIVEOR          = new Kind( "" );
        public static final Kind LOGICAL_AND           = new Kind( "" );
        public static final Kind LOGICAL_OR            = new Kind( "" );
        public static final Kind CONDITIONAL          = new Kind( "" );
        public static final Kind THROW= new Kind( "" );
        public static final Kind ASSIGNMENT_NORMAL    = new Kind( "" );
        public static final Kind ASSIGNMENT_PLUS      = new Kind( "" );
        public static final Kind ASSIGNMENT_MINUS     = new Kind( "" );
        public static final Kind ASSIGNMENT_MULT      = new Kind( "" );
        public static final Kind ASSIGNMENT_DIV       = new Kind( "" );
        public static final Kind ASSIGNMENT_MOD       = new Kind( "" );
        public static final Kind ASSIGNMENT_LSHIFT    = new Kind( "" );
        public static final Kind ASSIGNMENT_RSHIFT    = new Kind( "" );
        public static final Kind ASSIGNMENT_AND       = new Kind( "" );
        public static final Kind ASSIGNMENT_OR        = new Kind( "" );
        public static final Kind ASSIGNMENT_XOR       = new Kind( "" );
        public static final Kind LIST                 = new Kind( "List" ); // NOI18N
        
    }
    
    /**
     * Gets this expression kind
     */
    Kind getKind();
    
    
    /**
     * Gets this expression text
     * TODO: delete in the case we add getText to CsmOffsetable
     */
    String getText();
    
    /**
     * Gets parent expression or null if this is no parent expression
     */
    CsmExpression getParent();
    
    
    /**
     * Gets this expression operands
     */
    List/*<CsmExpression>*/ getOperands();
}
