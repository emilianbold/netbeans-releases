/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.editor.fortran;

import java.util.HashMap;
import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Fortran token-context defines token-ids and token-categories
* used in Fortran language.
*
*/

public class FTokenContext extends TokenContext {

    // Token category-ids
    public static final int KEYWORDS_ID           = 1;
    public static final int KEYWORD_OPERATORS_ID  = KEYWORDS_ID           + 1;
    public static final int OPERATORS_ID          = KEYWORD_OPERATORS_ID  + 1;
    public static final int NUMERIC_LITERALS_ID   = OPERATORS_ID          + 1;
    public static final int SPECIAL_CHARACTERS_ID = NUMERIC_LITERALS_ID   + 1;
    public static final int ERRORS_ID             = SPECIAL_CHARACTERS_ID + 1;

    // Numeric Literal IDs
    public static final int NUM_LITERAL_INT_ID     = ERRORS_ID               + 1;
    public static final int NUM_LITERAL_REAL_ID    = NUM_LITERAL_INT_ID      + 1;
    public static final int NUM_LITERAL_COMPLEX_ID = NUM_LITERAL_REAL_ID     + 1;
    public static final int NUM_LITERAL_BINARY_ID  = NUM_LITERAL_COMPLEX_ID  + 1;
    public static final int NUM_LITERAL_HEX_ID     = NUM_LITERAL_BINARY_ID   + 1;
    public static final int NUM_LITERAL_OCTAL_ID   = NUM_LITERAL_HEX_ID      + 1;

    //other literal IDS
    public static final int IDENTIFIER_ID     = NUM_LITERAL_OCTAL_ID + 1;
    public static final int WHITESPACE_ID     = IDENTIFIER_ID        + 1;
    public static final int LINE_COMMENT_ID   = WHITESPACE_ID        + 1;
    public static final int STRING_LITERAL_ID = LINE_COMMENT_ID      + 1;

    // Operators IDS

    public static final int OP_POWER_ID  = STRING_LITERAL_ID + 1;
    public static final int OP_MUL_ID    = OP_POWER_ID       + 1;
    public static final int OP_DIV_ID    = OP_MUL_ID         + 1;
    public static final int OP_PLUS_ID   = OP_DIV_ID         + 1;
    public static final int OP_MINUS_ID  = OP_PLUS_ID        + 1;
    public static final int OP_CONCAT_ID = OP_MINUS_ID       + 1;
    public static final int OP_LOG_EQ_ID = OP_CONCAT_ID      + 1;
    public static final int OP_NOT_EQ_ID = OP_LOG_EQ_ID      + 1;
    public static final int OP_LT_ID     = OP_NOT_EQ_ID      + 1;
    public static final int OP_LT_EQ_ID  = OP_LT_ID          + 1;
    public static final int OP_GT_ID     = OP_LT_EQ_ID       + 1;
    public static final int OP_GT_EQ_ID  = OP_GT_ID          + 1;

    // Special Characters IDs
    public static final int EQ_ID              = OP_GT_EQ_ID        + 1;
    public static final int DOT_ID             = EQ_ID              + 1;
    public static final int COMMA_ID           = DOT_ID             + 1;
    public static final int COLON_ID           = COMMA_ID           + 1;
    public static final int LPAREN_ID          = COLON_ID           + 1;
    public static final int RPAREN_ID          = LPAREN_ID          + 1;
    public static final int APOSTROPHE_CHAR_ID = RPAREN_ID          + 1;
    public static final int EXCLAMATION_ID     = APOSTROPHE_CHAR_ID + 1;
    public static final int QUOTATION_ID       = EXCLAMATION_ID     + 1;
    public static final int PERCENT_ID         = QUOTATION_ID       + 1;
    public static final int AMPERSAND_ID       = PERCENT_ID         + 1;
    public static final int SEMICOLON_ID       = AMPERSAND_ID       + 1;
    public static final int QUESTION_MARK_ID   = SEMICOLON_ID       + 1;
    public static final int CURRENCY_ID        = QUESTION_MARK_ID   + 1;

    // Keyword Operator IDs
    public static final int KWOP_EQ_ID    = CURRENCY_ID  + 1;
    public static final int KWOP_NE_ID    = KWOP_EQ_ID   + 1;  
    public static final int KWOP_LT_ID    = KWOP_NE_ID   + 1;  
    public static final int KWOP_LE_ID    = KWOP_LT_ID   + 1;  
    public static final int KWOP_GT_ID    = KWOP_LE_ID   + 1; 
    public static final int KWOP_GE_ID    = KWOP_GT_ID   + 1; 
    public static final int KWOP_AND_ID   = KWOP_GE_ID   + 1;
    public static final int KWOP_OR_ID    = KWOP_AND_ID  + 1;
    public static final int KWOP_NOT_ID   = KWOP_OR_ID   + 1;
    public static final int KWOP_EQV_ID   = KWOP_NOT_ID  + 1;
    public static final int KWOP_NEQV_ID  = KWOP_EQV_ID  + 1;
    public static final int KWOP_TRUE_ID  = KWOP_NEQV_ID + 1;
    public static final int KWOP_FALSE_ID = KWOP_TRUE_ID + 1;

    // Keyword IDS
    public static final int KW_ACCESS_EQ_ID        = KWOP_FALSE_ID         + 1;
    public static final int KW_ACTION_EQ_ID        = KW_ACCESS_EQ_ID       + 1;
    public static final int KW_ADVANCE_EQ_ID       = KW_ACTION_EQ_ID       + 1;
    public static final int KW_ALLOCATABLE_ID      = KW_ADVANCE_EQ_ID      + 1;
    public static final int KW_ALLOCATE_ID         = KW_ALLOCATABLE_ID     + 1;
    public static final int KW_APOSTROPHE_ID       = KW_ALLOCATE_ID        + 1;
    public static final int KW_ASSIGNMENT_ID       = KW_APOSTROPHE_ID      + 1;
    public static final int KW_BACKSPACE_ID        = KW_ASSIGNMENT_ID      + 1;
    public static final int KW_BLANK_EQ_ID         = KW_BACKSPACE_ID       + 1;
    public static final int KW_BLOCK_ID            = KW_BLANK_EQ_ID        + 1;
    public static final int KW_BLOCKDATA_ID        = KW_BLOCK_ID           + 1;
    public static final int KW_CALL_ID             = KW_BLOCKDATA_ID       + 1;
    public static final int KW_CASE_ID             = KW_CALL_ID            + 1;
    public static final int KW_CHARACTER_ID        = KW_CASE_ID            + 1;
    public static final int KW_CLOSE_ID            = KW_CHARACTER_ID       + 1;
    public static final int KW_COMMON_ID           = KW_CLOSE_ID           + 1;
    public static final int KW_COMPLEX_ID          = KW_COMMON_ID          + 1;
    public static final int KW_CONTAINS_ID         = KW_COMPLEX_ID         + 1;
    public static final int KW_CONTINUE_ID         = KW_CONTAINS_ID        + 1;
    public static final int KW_CYCLE_ID            = KW_CONTINUE_ID        + 1;
    public static final int KW_DATA_ID             = KW_CYCLE_ID           + 1;
    public static final int KW_DEALLOCATE_ID       = KW_DATA_ID            + 1;
    public static final int KW_DEFAULT_ID          = KW_DEALLOCATE_ID      + 1;
    public static final int KW_DELIM_EQ_ID         = KW_DEFAULT_ID         + 1;
    public static final int KW_DIMENSION_ID        = KW_DELIM_EQ_ID        + 1;
    public static final int KW_DIRECT_EQ_ID        = KW_DIMENSION_ID       + 1;
    public static final int KW_DO_ID               = KW_DIRECT_EQ_ID       + 1;
    public static final int KW_DOUBLE_ID           = KW_DO_ID              + 1;
    public static final int KW_DOUBLEPRECISION_ID  = KW_DOUBLE_ID          + 1;
    public static final int KW_ELEMENTAL_ID        = KW_DOUBLEPRECISION_ID + 1;
    public static final int KW_ELSE_ID             = KW_ELEMENTAL_ID      + 1;
    public static final int KW_ELSEIF_ID           = KW_ELSE_ID           + 1;
    public static final int KW_ELSEWHERE_ID        = KW_ELSEIF_ID         + 1;
    public static final int KW_END_ID              = KW_ELSEWHERE_ID      + 1;
    public static final int KW_ENDBLOCK_ID         = KW_END_ID            + 1;
    public static final int KW_ENDBLOCKDATA_ID     = KW_ENDBLOCK_ID       + 1;
    public static final int KW_ENDDO_ID            = KW_ENDBLOCKDATA_ID   + 1;
    public static final int KW_END_EQ_ID           = KW_ENDDO_ID          + 1;
    public static final int KW_ENDFILE_ID          = KW_END_EQ_ID         + 1;
    public static final int KW_ENDFORALL_ID        = KW_ENDFILE_ID        + 1;
    public static final int KW_ENDFUNCTION_ID      = KW_ENDFORALL_ID      + 1;
    public static final int KW_ENDIF_ID            = KW_ENDFUNCTION_ID    + 1;
    public static final int KW_ENDINTERFACE_ID     = KW_ENDIF_ID          + 1;
    public static final int KW_ENDMAP_ID           = KW_ENDINTERFACE_ID   + 1;
    public static final int KW_ENDMODULE_ID        = KW_ENDMAP_ID         + 1;
    public static final int KW_ENDPROGRAM_ID       = KW_ENDMODULE_ID      + 1;
    public static final int KW_ENDSELECT_ID        = KW_ENDPROGRAM_ID     + 1;
    public static final int KW_ENDSTRUCTURE_ID     = KW_ENDSELECT_ID      + 1;
    public static final int KW_ENDSUBROUTINE_ID    = KW_ENDSTRUCTURE_ID   + 1;
    public static final int KW_ENDTYPE_ID          = KW_ENDSUBROUTINE_ID  + 1;
    public static final int KW_ENDUNION_ID         = KW_ENDTYPE_ID        + 1;
    public static final int KW_ENDWHERE_ID         = KW_ENDUNION_ID       + 1;
    public static final int KW_ENTRY_ID            = KW_ENDWHERE_ID       + 1;
    public static final int KW_EOR_EQ_ID           = KW_ENTRY_ID          + 1;
    public static final int KW_EQUIVALENCE_ID      = KW_EOR_EQ_ID         + 1;
    public static final int KW_ERR_EQ_ID           = KW_EQUIVALENCE_ID    + 1;
    public static final int KW_EXIST_EQ_ID         = KW_ERR_EQ_ID         + 1;
    public static final int KW_EXIT_ID             = KW_EXIST_EQ_ID       + 1;
    public static final int KW_EXTERNAL_ID         = KW_EXIT_ID           + 1;
    public static final int KW_FILE_ID             = KW_EXTERNAL_ID       + 1;
    public static final int KW_FILE_EQ_ID          = KW_FILE_ID           + 1;
    public static final int KW_FORALL_ID           = KW_FILE_EQ_ID        + 1;
    public static final int KW_FORM_EQ_ID          = KW_FORALL_ID         + 1;
    public static final int KW_FORMAT_ID           = KW_FORM_EQ_ID        + 1;
    public static final int KW_FORMATTED_ID        = KW_FORMAT_ID         + 1;
    public static final int KW_FUNCTION_ID         = KW_FORMATTED_ID      + 1;
    public static final int KW_GO_ID               = KW_FUNCTION_ID       + 1;
    public static final int KW_GOTO_ID             = KW_GO_ID             + 1;
    public static final int KW_IF_ID               = KW_GOTO_ID           + 1;
    public static final int KW_IMPLICIT_ID         = KW_IF_ID             + 1;
    public static final int KW_IN_ID               = KW_IMPLICIT_ID       + 1;
    public static final int KW_INCLUDE_ID          = KW_IN_ID             + 1;
    public static final int KW_INOUT_ID            = KW_INCLUDE_ID        + 1;
    public static final int KW_INQUIRE_ID          = KW_INOUT_ID          + 1;
    public static final int KW_INTEGER_ID          = KW_INQUIRE_ID        + 1;
    public static final int KW_INTENT_ID           = KW_INTEGER_ID        + 1;
    public static final int KW_INTERFACE_ID        = KW_INTENT_ID         + 1;
    public static final int KW_INTRINSIC_ID        = KW_INTERFACE_ID      + 1;
    public static final int KW_IOSTAT_EQ_ID        = KW_INTRINSIC_ID      + 1;
    public static final int KW_KIND_ID             = KW_IOSTAT_EQ_ID      + 1;
    public static final int KW_LEN_ID              = KW_KIND_ID           + 1;
    public static final int KW_LOGICAL_ID          = KW_LEN_ID            + 1;
    public static final int KW_MAP_ID              = KW_LOGICAL_ID        + 1;
    public static final int KW_MODULE_ID           = KW_MAP_ID            + 1;
    public static final int KW_NAME_EQ_ID          = KW_MODULE_ID         + 1;
    public static final int KW_NAMED_EQ_ID         = KW_NAME_EQ_ID        + 1;
    public static final int KW_NAMELIST_ID         = KW_NAMED_EQ_ID       + 1;
    public static final int KW_NEXTREC_ID          = KW_NAMELIST_ID       + 1;
    public static final int KW_NML_EQ_ID           = KW_NEXTREC_ID        + 1;
    public static final int KW_NONE_ID             = KW_NML_EQ_ID         + 1;
    public static final int KW_NULLIFY_ID          = KW_NONE_ID           + 1;
    public static final int KW_NUMBER_EQ_ID        = KW_NULLIFY_ID        + 1;
    public static final int KW_ONLY_ID             = KW_NUMBER_EQ_ID      + 1;
    public static final int KW_OPEN_ID             = KW_ONLY_ID           + 1;
    public static final int KW_OPENED_EQ_ID        = KW_OPEN_ID           + 1;
    public static final int KW_OPERATOR_ID         = KW_OPENED_EQ_ID      + 1;
    public static final int KW_OPTIONAL_ID         = KW_OPERATOR_ID       + 1;
    public static final int KW_OUT_ID              = KW_OPTIONAL_ID       + 1;
    public static final int KW_PAD_EQ_ID           = KW_OUT_ID            + 1;
    public static final int KW_PARAMETER_ID        = KW_PAD_EQ_ID         + 1;
    public static final int KW_POINTER_ID          = KW_PARAMETER_ID      + 1;
    public static final int KW_POSITION_ID         = KW_POINTER_ID        + 1;
    public static final int KW_PRECISION_ID        = KW_POSITION_ID       + 1;
    public static final int KW_PRINT_ID            = KW_PRECISION_ID      + 1;
    public static final int KW_PRIVATE_ID          = KW_PRINT_ID          + 1;
    public static final int KW_PROCEDURE_ID        = KW_PRIVATE_ID        + 1;
    public static final int KW_PROGRAM_ID          = KW_PROCEDURE_ID      + 1;
    public static final int KW_PUBLIC_ID           = KW_PROGRAM_ID        + 1;
    public static final int KW_PURE_ID             = KW_PUBLIC_ID         + 1;
    public static final int KW_QUOTE_ID            = KW_PURE_ID           + 1;
    public static final int KW_READ_ID             = KW_QUOTE_ID          + 1;
    public static final int KW_READ_EQ_ID          = KW_READ_ID           + 1;
    public static final int KW_READWRITE_EQ_ID     = KW_READ_EQ_ID        + 1;
    public static final int KW_REAL_ID             = KW_READWRITE_EQ_ID   + 1;
    public static final int KW_REC_EQ_ID           = KW_REAL_ID           + 1;
    public static final int KW_RECL_EQ_ID          = KW_REC_EQ_ID         + 1;
    public static final int KW_RECURSIVE_ID        = KW_RECL_EQ_ID        + 1;
    public static final int KW_RESULT_ID           = KW_RECURSIVE_ID      + 1;
    public static final int KW_RETURN_ID           = KW_RESULT_ID         + 1;
    public static final int KW_REWIND_ID           = KW_RETURN_ID         + 1;
    public static final int KW_SAVE_ID             = KW_REWIND_ID         + 1;
    public static final int KW_SELECT_ID           = KW_SAVE_ID           + 1;
    public static final int KW_SELECTCASE_ID       = KW_SELECT_ID         + 1;
    public static final int KW_SEQUENCE_ID         = KW_SELECTCASE_ID     + 1;
    public static final int KW_SEQUENTIAL_EQ_ID    = KW_SEQUENCE_ID       + 1;
    public static final int KW_SIZE_ID             = KW_SEQUENTIAL_EQ_ID  + 1;
    public static final int KW_SIZE_EQ_ID          = KW_SIZE_ID           + 1;
    public static final int KW_STAT_EQ_ID          = KW_SIZE_EQ_ID        + 1;
    public static final int KW_STATUS_EQ_ID        = KW_STAT_EQ_ID        + 1;
    public static final int KW_STOP_ID             = KW_STATUS_EQ_ID      + 1;
    public static final int KW_STRUCTURE_ID        = KW_STOP_ID           + 1;
    public static final int KW_SUBROUTINE_ID       = KW_STRUCTURE_ID      + 1;
    public static final int KW_TARGET_ID           = KW_SUBROUTINE_ID     + 1;
    public static final int KW_THEN_ID             = KW_TARGET_ID         + 1;
    public static final int KW_TO_ID               = KW_THEN_ID           + 1;
    public static final int KW_TYPE_ID             = KW_TO_ID             + 1;
    public static final int KW_UNFORMATTED_EQ_ID   = KW_TYPE_ID           + 1;
    public static final int KW_UNION_ID            = KW_UNFORMATTED_EQ_ID + 1;
    public static final int KW_USE_ID              = KW_UNION_ID          + 1;
    public static final int KW_WHERE_ID            = KW_USE_ID            + 1;
    public static final int KW_WHILE_ID            = KW_WHERE_ID          + 1;
    public static final int KW_WRITE_ID            = KW_WHILE_ID          + 1;
    public static final int KW_WRITE_EQ_ID         = KW_WRITE_ID          + 1;

    // Error IDS
    public static final int INVALID_HEX_LITERAL_ID       = KW_WRITE_EQ_ID            + 1;
    public static final int INVALID_OCTAL_LITERAL_ID     = INVALID_HEX_LITERAL_ID    + 1;
    public static final int INVALID_BINARY_LITERAL_ID    = INVALID_OCTAL_LITERAL_ID  + 1;
    public static final int INVALID_CHAR_ID              = INVALID_BINARY_LITERAL_ID + 1;
    public static final int INVALID_INTEGER_ID           = INVALID_CHAR_ID           + 1;
    public static final int INCOMPLETE_STRING_LITERAL_ID = INVALID_INTEGER_ID        + 1;

    // Token category

    public static final BaseTokenCategory KEYWORDS =
	new BaseTokenCategory("keywords", KEYWORDS_ID); // NOI18N

    public static final BaseTokenCategory KEYWORD_OPERATORS =
	new BaseTokenCategory("keyword-operators", KEYWORD_OPERATORS_ID); // NOI18N

    public static final BaseTokenCategory OPERATORS =
	new BaseTokenCategory("operators", OPERATORS_ID); // NOI18N

    public static final BaseTokenCategory NUMERIC_LITERALS =
	new BaseTokenCategory("numeric-literals", NUMERIC_LITERALS_ID); // NOI18N

    public static final BaseTokenCategory SPECIAL_CHARACTERS =
	new BaseTokenCategory("special-characters", SPECIAL_CHARACTERS_ID); // NOI18N

    public static final BaseTokenCategory ERRORS =
	new BaseTokenCategory("errors", ERRORS_ID); // NOI18N

    // Numeric Literals

    public static final BaseTokenID NUM_LITERAL_INT = 
	new BaseTokenID("int-literal", NUM_LITERAL_INT_ID, NUMERIC_LITERALS); // NOI18N
    public static final BaseTokenID NUM_LITERAL_REAL =
	new BaseTokenID("real-literal", NUM_LITERAL_REAL_ID, NUMERIC_LITERALS); // NOI18N
    /**NUM_LITERAL_COMPLEX in here is just a place holder to show that it is a
     * valid numeric literal
     * the sysntax highlighting for complex numbers are handled
     * in integers,reals and paranthesis
     */
    public static final BaseTokenID NUM_LITERAL_COMPLEX =
	new BaseTokenID("complex-literal", NUM_LITERAL_COMPLEX_ID, NUMERIC_LITERALS); // NOI18N
    public static final BaseTokenID NUM_LITERAL_BINARY =
	new BaseTokenID("binary-literal", NUM_LITERAL_BINARY_ID, NUMERIC_LITERALS); // NOI18N
    public static final BaseTokenID NUM_LITERAL_HEX =
	new BaseTokenID("hex-literal", NUM_LITERAL_HEX_ID, NUMERIC_LITERALS); // NOI18N
    public static final BaseTokenID NUM_LITERAL_OCTAL =
	new BaseTokenID("octal-literal", NUM_LITERAL_OCTAL_ID, NUMERIC_LITERALS); // NOI18N

    //other literals  [no category]

    public static final BaseTokenID IDENTIFIER =
	new BaseTokenID("identifier", IDENTIFIER_ID ); // NOI18N
    public static final BaseTokenID WHITESPACE =
	new BaseTokenID("whitespace", WHITESPACE_ID); // NOI18N
    public static final BaseTokenID LINE_COMMENT =
	new BaseTokenID("line-comment", LINE_COMMENT_ID); // NOI18N
    public static final BaseTokenID STRING_LITERAL =
	new BaseTokenID("string-literal", STRING_LITERAL_ID); // NOI18N

    // Operators

    public static final BaseImageTokenID OP_POWER =
	new BaseImageTokenID("power", OP_POWER_ID, OPERATORS, "**"); // NOI18N
    public static final BaseImageTokenID OP_MUL =
	new BaseImageTokenID("mul", OP_MUL_ID, OPERATORS, "*"); // NOI18N
    public static final BaseImageTokenID OP_DIV =
	new BaseImageTokenID("div", OP_DIV_ID, OPERATORS, "/"); // NOI18N
    public static final BaseImageTokenID OP_PLUS =
	new BaseImageTokenID("plus", OP_PLUS_ID, OPERATORS, "+"); // NOI18N
    public static final BaseImageTokenID OP_MINUS =
	new BaseImageTokenID("minus", OP_MINUS_ID, OPERATORS, "-"); // NOI18N
    public static final BaseImageTokenID OP_CONCAT =
	new BaseImageTokenID("concat", OP_CONCAT_ID, OPERATORS, "//"); // NOI18N
    public static final BaseImageTokenID OP_LOG_EQ =
	new BaseImageTokenID("logical-equals", OP_LOG_EQ_ID, OPERATORS, "=="); // NOI18N
    public static final BaseImageTokenID OP_NOT_EQ =
	new BaseImageTokenID("not-equals", OP_NOT_EQ_ID, OPERATORS, "/="); // NOI18N
    public static final BaseImageTokenID OP_LT =
	new BaseImageTokenID("less-than", OP_LT_ID, OPERATORS, "<"); // NOI18N
    public static final BaseImageTokenID OP_LT_EQ =
	new BaseImageTokenID("less-than-equals", OP_LT_EQ_ID, OPERATORS, "<="); // NOI18N
    public static final BaseImageTokenID OP_GT =
	new BaseImageTokenID("greater-than", OP_GT_ID, OPERATORS, ">"); // NOI18N
    public static final BaseImageTokenID OP_GT_EQ =
	new BaseImageTokenID("greater-than-equals", OP_GT_EQ_ID, OPERATORS, ">="); // NOI18N

    // Special Characters
    public static final BaseImageTokenID EQ =
	new BaseImageTokenID("equals", EQ_ID, SPECIAL_CHARACTERS, "="); // NOI18N
    public static final BaseImageTokenID DOT =
	new BaseImageTokenID("dot", DOT_ID, SPECIAL_CHARACTERS, "."); // NOI18N
    public static final BaseImageTokenID COMMA =
	new BaseImageTokenID("comma", COMMA_ID, SPECIAL_CHARACTERS, ","); // NOI18N
    public static final BaseImageTokenID COLON =
	new BaseImageTokenID("colon", COLON_ID, SPECIAL_CHARACTERS, ":"); // NOI18N
    public static final BaseImageTokenID LPAREN =
        new BaseImageTokenID("left-paran", LPAREN_ID, SPECIAL_CHARACTERS, "("); // NOI18N
    public static final BaseImageTokenID RPAREN =
        new BaseImageTokenID("right-paran", RPAREN_ID, SPECIAL_CHARACTERS, ")"); // NOI18N
    public static final BaseImageTokenID APOSTROPHE_CHAR =
        new BaseImageTokenID("apostrophe-char", APOSTROPHE_CHAR_ID, SPECIAL_CHARACTERS, "'"); // NOI18N
    /**SC_EXCLAMATION in here is just a place holder to show that it is 
     * a special character the sysntax highlighting for exclamations are
     * handled in LINE_COMMENT
     */
    public static final BaseImageTokenID EXCLAMATION =
	new BaseImageTokenID("exclamation", EXCLAMATION_ID, SPECIAL_CHARACTERS, "!"); // NOI18N
    /**SC_QUOTATION in here is just a place holder to show that it is a 
     * special character the sysntax highlighting for quotes are handled
     * in STRING_LITERAL
     */
    public static final BaseImageTokenID QUOTATION =
	new BaseImageTokenID("quotation", QUOTATION_ID, SPECIAL_CHARACTERS, "\""); // NOI18N
    public static final BaseImageTokenID PERCENT =
	new BaseImageTokenID("percent", PERCENT_ID, SPECIAL_CHARACTERS, "%"); // NOI18N
    public static final BaseImageTokenID AMPERSAND =
	new BaseImageTokenID("ampersand", AMPERSAND_ID, SPECIAL_CHARACTERS, "&"); // NOI18N
    public static final BaseImageTokenID SEMICOLON =
	new BaseImageTokenID("semicolon", SEMICOLON_ID, SPECIAL_CHARACTERS, ";"); // NOI18N
    public static final BaseImageTokenID QUESTION_MARK =
	new BaseImageTokenID("question-mark", QUESTION_MARK_ID, SPECIAL_CHARACTERS, "?"); // NOI18N
    public static final BaseImageTokenID CURRENCY =
	new BaseImageTokenID("currency", CURRENCY_ID, SPECIAL_CHARACTERS, "$"); // NOI18N


    // Keyword Operator

    public static final BaseImageTokenID KWOP_EQ =
	new BaseImageTokenID(".eq.", KWOP_EQ_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_NE =
	new BaseImageTokenID(".ne.", KWOP_NE_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_LT =
	new BaseImageTokenID(".lt.", KWOP_LT_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_LE =
	new BaseImageTokenID(".le.", KWOP_LE_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_GT =
	new BaseImageTokenID(".gt.", KWOP_GT_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_GE =
	new BaseImageTokenID(".ge.", KWOP_GE_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_AND =
	new BaseImageTokenID(".and.", KWOP_AND_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_OR =
	new BaseImageTokenID(".or.", KWOP_OR_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_NOT =
	new BaseImageTokenID(".not.", KWOP_NOT_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_EQV =
	new BaseImageTokenID(".eqv.", KWOP_EQV_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_NEQV =
	new BaseImageTokenID(".neqv.", KWOP_NEQV_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_TRUE =
	new BaseImageTokenID(".true.", KWOP_TRUE_ID, KEYWORD_OPERATORS); //NOI18N
    public static final BaseImageTokenID KWOP_FALSE =
	new BaseImageTokenID(".false.", KWOP_FALSE_ID, KEYWORD_OPERATORS); //NOI18N

    // keywords
    /** some keywords specified here are only keywords
        when they are succeeded or preceeded by another keyword.
        For the sake of simplicity, we assume all of these words
        are keywords. The table below lists these keywords:

        assumed keyword    complete keyword
        block              block data
        double             double precision
        file               end file
        go                 go to
        precision          double precision
        procedure          module procedure
        select             select case, end select
        to                 go to
    */

    public static final BaseImageTokenID KW_ACCESS_EQ =
	new BaseImageTokenID("access=", KW_ACCESS_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ACTION_EQ =
	new BaseImageTokenID("action=", KW_ACTION_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ADVANCE_EQ =
	new BaseImageTokenID("advance=", KW_ADVANCE_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ALLOCATABLE =
	new BaseImageTokenID("allocatable", KW_ALLOCATABLE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ALLOCATE =
	new BaseImageTokenID("allocate", KW_ALLOCATE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_APOSTROPHE =
	new BaseImageTokenID("apostrophe", KW_APOSTROPHE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ASSIGNMENT =
	new BaseImageTokenID("assignment", KW_ASSIGNMENT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_BACKSPACE =
	new BaseImageTokenID("backspace", KW_BACKSPACE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_BLANK_EQ =
	new BaseImageTokenID("blank=", KW_BLANK_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_BLOCK =
	new BaseImageTokenID("block", KW_BLOCK_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_BLOCKDATA =
	new BaseImageTokenID("blockdata", KW_BLOCKDATA_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_CALL =
	new BaseImageTokenID("call", KW_CALL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_CASE =
	new BaseImageTokenID("case", KW_CASE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_CHARACTER =
	new BaseImageTokenID("character", KW_CHARACTER_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_CLOSE =
	new BaseImageTokenID("close", KW_CLOSE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_COMMON =
	new BaseImageTokenID("common", KW_COMMON_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_COMPLEX =
	new BaseImageTokenID("complex", KW_COMPLEX_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_CONTAINS =
	new BaseImageTokenID("contains", KW_CONTAINS_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_CONTINUE =
	new BaseImageTokenID("continue", KW_CONTINUE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_CYCLE =
	new BaseImageTokenID("cycle", KW_CYCLE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DATA =
	new BaseImageTokenID("data", KW_DATA_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_DEALLOCATE =
	new BaseImageTokenID("deallocate", KW_DEALLOCATE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DEFAULT =
	new BaseImageTokenID("default", KW_DEFAULT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DELIM_EQ =
	new BaseImageTokenID("delim=", KW_DELIM_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DIMENSION =
	new BaseImageTokenID("dimension", KW_DIMENSION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DIRECT_EQ =
	new BaseImageTokenID("direct=", KW_DIRECT_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DO =
	new BaseImageTokenID("do", KW_DO_ID , KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DOUBLE =
	new BaseImageTokenID("double", KW_DOUBLE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_DOUBLEPRECISION =
	new BaseImageTokenID("doubleprecision", KW_DOUBLEPRECISION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ELEMENTAL =
	new BaseImageTokenID("elemental", KW_ELEMENTAL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ELSE =
	new BaseImageTokenID("else", KW_ELSE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ELSEIF =
	new BaseImageTokenID("elseif", KW_ELSEIF_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_ELSEWHERE =
	new BaseImageTokenID("elsewhere", KW_ELSEWHERE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_END =
	new BaseImageTokenID("end", KW_END_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDBLOCK =
	new BaseImageTokenID("endblock", KW_ENDBLOCK_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDBLOCKDATA =
	new BaseImageTokenID("endblockdata", KW_ENDBLOCKDATA_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDDO =
	new BaseImageTokenID("enddo", KW_ENDDO_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_END_EQ =
	new BaseImageTokenID("end=", KW_END_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDFILE =
	new BaseImageTokenID("endfile", KW_ENDFILE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDFORALL =
	new BaseImageTokenID("endforall", KW_ENDFORALL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDFUNCTION =
	new BaseImageTokenID("endfunction", KW_ENDFUNCTION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDIF =
	new BaseImageTokenID("endif", KW_ENDIF_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_ENDINTERFACE =
	new BaseImageTokenID("endinterface", KW_ENDINTERFACE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDMAP =
	new BaseImageTokenID("endmap", KW_ENDMAP_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDMODULE =
	new BaseImageTokenID("endmodule", KW_ENDMODULE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDPROGRAM =
	new BaseImageTokenID("endprogram", KW_ENDPROGRAM_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDSELECT =
	new BaseImageTokenID("endselect", KW_ENDSELECT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDSTRUCTURE =
	new BaseImageTokenID("endstructure", KW_ENDSTRUCTURE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDSUBROUTINE =
	new BaseImageTokenID("endsubroutine", KW_ENDSUBROUTINE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDTYPE =
	new BaseImageTokenID("endtype", KW_ENDTYPE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDUNION =
	new BaseImageTokenID("endunion", KW_ENDUNION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENDWHERE =
	new BaseImageTokenID("endwhere", KW_ENDWHERE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ENTRY =
	new BaseImageTokenID("entry", KW_ENTRY_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_EOR_EQ =
	new BaseImageTokenID("eor=", KW_EOR_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_EQUIVALENCE =
	new BaseImageTokenID("equivalance", KW_EQUIVALENCE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_ERR_EQ =
	new BaseImageTokenID("err=", KW_ERR_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_EXIST_EQ =
	new BaseImageTokenID("exist=", KW_EXIST_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_EXIT =
	new BaseImageTokenID("exit", KW_EXIT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_EXTERNAL =
	new BaseImageTokenID("external", KW_EXTERNAL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_FILE =
	new BaseImageTokenID("file", KW_FILE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_FILE_EQ =
	new BaseImageTokenID("file=", KW_FILE_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_FORALL =
	new BaseImageTokenID("forall", KW_FORALL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_FORM_EQ =
	new BaseImageTokenID("form=", KW_FORM_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_FORMAT =
	new BaseImageTokenID("format", KW_FORMAT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_FORMATTED =
	new BaseImageTokenID("formatted", KW_FORMATTED_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_FUNCTION =
	new BaseImageTokenID("function", KW_FUNCTION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_GO =
	new BaseImageTokenID("go", KW_GO_ID , KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_GOTO =
	new BaseImageTokenID("goto", KW_GOTO_ID , KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_IF =
	new BaseImageTokenID("if", KW_IF_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_IMPLICIT =
	new BaseImageTokenID("implicit", KW_IMPLICIT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_IN =
	new BaseImageTokenID("in", KW_IN_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_INCLUDE =
	new BaseImageTokenID("include", KW_INCLUDE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_INOUT =
	new BaseImageTokenID("inout", KW_INOUT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_INQUIRE =
	new BaseImageTokenID("inquire", KW_INQUIRE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_INTEGER =
	new BaseImageTokenID("integer", KW_INTEGER_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_INTENT =
	new BaseImageTokenID("intent", KW_INTENT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_INTERFACE =
	new BaseImageTokenID("interface", KW_INTERFACE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_INTRINSIC =
	new BaseImageTokenID("intrinsic", KW_INTRINSIC_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_IOSTAT_EQ =
	new BaseImageTokenID("iostat=", KW_IOSTAT_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_KIND =
	new BaseImageTokenID("kind", KW_KIND_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_LEN =
	new BaseImageTokenID("len", KW_LEN_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_LOGICAL =
	new BaseImageTokenID("logical", KW_LOGICAL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_MAP =
	new BaseImageTokenID("map", KW_MAP_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_MODULE =
	new BaseImageTokenID("module", KW_MODULE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_NAME_EQ =
	new BaseImageTokenID("name=", KW_NAME_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_NAMED_EQ =
	new BaseImageTokenID("named=", KW_NAMED_EQ_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_NAMELIST =
	new BaseImageTokenID("namelist", KW_NAMELIST_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_NEXTREC =
	new BaseImageTokenID("nextrec", KW_NEXTREC_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_NML_EQ =
	new BaseImageTokenID("nml=", KW_NML_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_NONE =
	new BaseImageTokenID("none", KW_NONE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_NULLIFY =
	new BaseImageTokenID("nullify", KW_NULLIFY_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_NUMBER_EQ =
	new BaseImageTokenID("number=", KW_NUMBER_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_ONLY =
	new BaseImageTokenID("only", KW_ONLY_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_OPEN =
	new BaseImageTokenID("open", KW_OPEN_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_OPENED_EQ =
	new BaseImageTokenID("opened=", KW_OPENED_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_OPERATOR =
	new BaseImageTokenID("operator", KW_OPERATOR_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_OPTIONAL =
	new BaseImageTokenID("optional", KW_OPTIONAL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_OUT =
	new BaseImageTokenID("out", KW_OUT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PAD_EQ =
	new BaseImageTokenID("pad=", KW_PAD_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PARAMETER =
	new BaseImageTokenID("parameter", KW_PARAMETER_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_POINTER =
	new BaseImageTokenID("pointer", KW_POINTER_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_POSITION_EQ =
	new BaseImageTokenID("position=", KW_POSITION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PRECISION =
	new BaseImageTokenID("precision", KW_PRECISION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PRINT =
	new BaseImageTokenID("print", KW_PRINT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PRIVATE =
	new BaseImageTokenID("private", KW_PRIVATE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PROCEDURE =
	new BaseImageTokenID("procedure", KW_PROCEDURE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_PROGRAM =
	new BaseImageTokenID("program", KW_PROGRAM_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PUBLIC =
	new BaseImageTokenID("public", KW_PUBLIC_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_PURE =
	new BaseImageTokenID("pure", KW_PURE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_QUOTE =
	new BaseImageTokenID("quote", KW_QUOTE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_READ =
	new BaseImageTokenID("read", KW_READ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_READ_EQ =
	new BaseImageTokenID("read=", KW_READ_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_READWRITE_EQ =
	new BaseImageTokenID("readwrite=", KW_READWRITE_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_REAL =
	new BaseImageTokenID("real", KW_REAL_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_REC_EQ =
	new BaseImageTokenID("rec=", KW_REC_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_RECL_EQ =
	new BaseImageTokenID("recl=", KW_RECL_EQ_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_RECURSIVE =
	new BaseImageTokenID("recursive", KW_RECURSIVE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_RESULT =
	new BaseImageTokenID("result", KW_RESULT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_RETURN =
	new BaseImageTokenID("return", KW_RETURN_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_REWIND =
	new BaseImageTokenID("rewind", KW_REWIND_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SAVE =
	new BaseImageTokenID("save", KW_SAVE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SELECT =
	new BaseImageTokenID("select", KW_SELECT_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SELECTCASE =
	new BaseImageTokenID("selectcase", KW_SELECTCASE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SEQUENCE =
	new BaseImageTokenID("sequence", KW_SEQUENCE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SEQUENTIAL_EQ =
	new BaseImageTokenID("sequential=", KW_SEQUENTIAL_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SIZE =
	new BaseImageTokenID("size", KW_SIZE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SIZE_EQ =
	new BaseImageTokenID("size=", KW_SIZE_EQ_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_STAT_EQ =
	new BaseImageTokenID("stat=", KW_STAT_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_STATUS_EQ =
	new BaseImageTokenID("status=", KW_STATUS_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_STOP =
	new BaseImageTokenID("stop", KW_STOP_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_STRUCTURE =
	new BaseImageTokenID("structure", KW_STRUCTURE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_SUBROUTINE =
	new BaseImageTokenID("subroutine", KW_SUBROUTINE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_TARGET =
	new BaseImageTokenID("target", KW_TARGET_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_THEN =
	new BaseImageTokenID("then", KW_THEN_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_TO =
	new BaseImageTokenID("to", KW_TO_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_TYPE =
	new BaseImageTokenID("type", KW_TYPE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_UNFORMATTED_EQ =
	new BaseImageTokenID("unformatted=", KW_UNFORMATTED_EQ_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_UNION =
	new BaseImageTokenID("union", KW_UNION_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_USE =
	new BaseImageTokenID("use", KW_USE_ID, KEYWORDS); // NOI18N

    public static final BaseImageTokenID KW_WHERE =
	new BaseImageTokenID("where", KW_WHERE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_WHILE =
	new BaseImageTokenID("while", KW_WHILE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_WRITE =
	new BaseImageTokenID("write", KW_WRITE_ID, KEYWORDS); // NOI18N
    public static final BaseImageTokenID KW_WRITE_EQ =
	new BaseImageTokenID("write=", KW_WRITE_EQ_ID, KEYWORDS); // NOI18N

    // Errors

    public static final BaseTokenID ERR_INVALID_HEX_LITERAL =
	new BaseTokenID("invalid-hex-literal", INVALID_HEX_LITERAL_ID, ERRORS); // NOI18N
    public static final BaseTokenID ERR_INVALID_OCTAL_LITERAL=
	new BaseTokenID("invalid-octal-literal", INVALID_OCTAL_LITERAL_ID, ERRORS); // NOI18N
    public static final BaseTokenID ERR_INVALID_BINARY_LITERAL=
	new BaseTokenID("invalid-binary-literal", INVALID_BINARY_LITERAL_ID, ERRORS); // NOI18N
    public static final BaseTokenID ERR_INVALID_CHAR =
	new BaseTokenID("invalid-char", INVALID_CHAR_ID, ERRORS); // NOI18N
    public static final BaseTokenID ERR_INVALID_INTEGER =
	new BaseTokenID("invalid-integer", INVALID_INTEGER_ID, ERRORS); // NOI18N
    public static final BaseTokenID ERR_INCOMPLETE_STRING_LITERAL =
	new BaseTokenID("incomplete-string-literal", INCOMPLETE_STRING_LITERAL_ID, ERRORS); // NOI18N

    // Context instance declaration
    public static final FTokenContext context = new FTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private FTokenContext() {
	// the argument below is prepended to settings names
        super("fortran-"); //NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { //NOI18N
                e.printStackTrace();
            }
        }

    }

}


