/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.netbeans.modules.cnd.antlr.TokenBuffer;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.FortranParser;
import org.openide.util.Exceptions;

/**
 *
 * @author nk220367
 */
public class FortranParserEx extends FortranParser {
    public List<Object> parsedObjects = new ArrayList<Object>();

    public class ProgramData {
        public String name;
        public int startOffset;
        public int endOffset;
    }

    public class SubroutineData {
        public String name;
        public int startOffset;
        public int endOffset;

        public List<String> args = null;
    }

    public class ModuleData {
        public String name;
        public int startOffset;
        public int endOffset;

        public List<Object> members = null;
    }

    public static class MyTokenSource implements TokenSource {

        TokenStream ts;

        public MyTokenSource(TokenStream ts) {
            this.ts = ts;
        }

        public Token nextToken() {
            org.netbeans.modules.cnd.antlr.Token nextToken = null;
            try {
                nextToken = ts.nextToken();
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }
            return (nextToken != null) ? new MyToken(nextToken) : null;
        }

        public String getSourceName() {
            return "my token source"; // NOI18N
        }

    }

    public FortranParserEx(TokenStream ts) {

        super(new FortranTokenStream(new MyTokenSource(ts)));
        //super(new MyTokenStream(new TokenBuffer(ts)));

//        FortranTokenStream tokens = (FortranTokenStream) getTokenStream();
//        FortranLexicalPrepass prepass = new FortranLexicalPrepass(tokens);
//        prepass.performPrepass();
//        tokens.finalizeTokenStream();



        inputStreams = new Stack<String>();

        action = new IFortranParserAction() {

            // Proogram

            ProgramData programData = null;

            public void program_stmt(Token label, Token programKeyword, Token id, Token eos) {
                if (id != null && programKeyword instanceof MyToken && ((MyToken) programKeyword).t instanceof APTToken) {
                    programData = new ProgramData();
                    programData.name = id.getText();
                    programData.startOffset = ((APTToken) ((MyToken) programKeyword).t).getOffset();
                    // System.out.println("program " + id);
                }
            }

            public void end_program_stmt(Token label, Token endKeyword, Token programKeyword, Token id, Token eos) {

                if(programData != null) {
                    if(endKeyword instanceof APTToken) {
                        programData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        programData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    parsedObjects.add(programData);
                }
                programData = null;

                if(subroutineData != null && subroutineData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(subroutineData);
                    } else {
                        parsedObjects.add(subroutineData);
                    }
                }
                subroutineData = null;

                if(functionData != null && functionData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        functionData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(functionData);
                    } else {
                        parsedObjects.add(functionData);
                    }
                }
                functionData = null;

                if(moduleData != null && moduleData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    parsedObjects.add(moduleData);
                }
                moduleData = null;
            }

            // Subroutine

            SubroutineData subroutineData = null;

            public void subroutine_stmt__begin() {
                subroutineData = new SubroutineData();
            }

            public void subroutine_stmt(Token label, Token keyword, Token name, Token eos, boolean hasPrefix, boolean hasDummyArgList, boolean hasBindingSpec, boolean hasArgSpecifier) {
                if (subroutineData != null && name != null && name.getText() != null && keyword instanceof MyToken && ((MyToken) keyword).t instanceof APTToken) {
                    subroutineData.name = name.getText();
                    subroutineData.startOffset = ((APTToken) ((MyToken) keyword).t).getOffset();
                }
            }

            public void end_subroutine_stmt(Token label, Token keyword1, Token keyword2, Token name, Token eos) {
                if(subroutineData != null && subroutineData.name != null) {
                    if (keyword2 instanceof MyToken && ((MyToken) keyword2).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken) ((MyToken) keyword2).t).getEndOffset();
                    } else if(keyword1 instanceof MyToken && ((MyToken) keyword1).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)((MyToken)keyword1).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(subroutineData);
                    } else {
                        parsedObjects.add(subroutineData);
                    }
                }
                subroutineData = null;
            }

            // Function

            SubroutineData functionData = null;

            public void function_stmt__begin() {
                functionData = new SubroutineData();
            }

            public void function_stmt(Token label, Token keyword, Token name, Token eos, boolean hasGenericNameList, boolean hasSuffix) {
                if (functionData != null && name != null && name.getText() != null && keyword instanceof MyToken && ((MyToken) keyword).t instanceof APTToken) {
                    functionData.name = name.getText();
                    functionData.startOffset = ((APTToken) ((MyToken) keyword).t).getOffset();
                }
            }

            public void end_function_stmt(Token label, Token keyword1, Token keyword2, Token name, Token eos) {
                if(functionData != null && functionData.name != null) {
                    if (keyword2 instanceof MyToken && ((MyToken) keyword2).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken) ((MyToken) keyword2).t).getEndOffset();
                    } else if(keyword1 instanceof MyToken && ((MyToken) keyword1).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken)((MyToken)keyword1).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(functionData);
                    } else {
                        parsedObjects.add(functionData);
                    }
                }
                functionData = null;
            }

            // Subroutine arguments

            public void dummy_arg(Token dummy) {
                if(dummy!= null && subroutineData != null && subroutineData.args != null) {
                    subroutineData.args.add(dummy.getText());
                }
            }

            public void dummy_arg_list__begin() {
                if(subroutineData != null) {
                    subroutineData.args = new ArrayList<String>();
                }
            }

            public void dummy_arg_list(int count) {
            }

            // Function arguments

            public void generic_name_list_part(Token ident) {
                if(ident!= null && subroutineData != null && subroutineData.args != null) {
                    subroutineData.args.add(ident.getText());
                }
            }

            public void generic_name_list__begin() {
                if(subroutineData != null) {
                    subroutineData.args = new ArrayList<String>();
                }
            }

            public void generic_name_list(int count) {
            }

            // Module

            ModuleData moduleData = null;

            public void module() {

            }

            public void module_stmt__begin() {
                moduleData = new ModuleData();
            }

            public void module_stmt(Token label, Token moduleKeyword, Token id, Token eos) {
                if (moduleData != null && id != null && moduleKeyword instanceof MyToken && ((MyToken) moduleKeyword).t instanceof APTToken) {
                    moduleData.name = id.getText();
                    moduleData.startOffset = ((APTToken) ((MyToken) moduleKeyword).t).getOffset();
                    moduleData.members = new ArrayList<Object>();
                }
            }

            public void end_module_stmt(Token label, Token endKeyword, Token moduleKeyword, Token id, Token eos) {

                if(subroutineData != null && subroutineData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        subroutineData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(subroutineData);
                    } else {
                        parsedObjects.add(subroutineData);
                    }
                }
                subroutineData = null;

                if(functionData != null && functionData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        functionData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        functionData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    if(moduleData != null && moduleData.members != null) {
                        moduleData.members.add(functionData);
                    } else {
                        parsedObjects.add(functionData);
                    }
                }
                functionData = null;

                if(moduleData != null && moduleData.name != null) {
                    if(endKeyword instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)endKeyword).getEndOffset();
                    } else if(endKeyword instanceof MyToken && ((MyToken) endKeyword).t instanceof APTToken) {
                        moduleData.endOffset = ((APTToken)((MyToken)endKeyword).t).getEndOffset();
                    }
                    parsedObjects.add(moduleData);
                }
                moduleData = null;
            }

            public void module_subprogram_part() {

            }

            public void module_subprogram(boolean hasPrefix) {

            }




            public void specification_part(int numUseStmts, int numImportStmts, int numDeclConstructs) {

            }

            public void declaration_construct() {

            }

            public void execution_part() {

            }

            public void execution_part_construct() {

            }

            public void internal_subprogram_part(int count) {

            }

            public void internal_subprogram() {

            }

            public void specification_stmt() {

            }

            public void executable_construct() {

            }

            public void action_stmt() {

            }

            public void keyword() {

            }

            public void name(Token id) {

            }

            public void constant(Token id) {

            }

            public void scalar_constant() {

            }

            public void literal_constant() {

            }

            public void int_constant(Token id) {

            }

            public void char_constant(Token id) {

            }

            public void intrinsic_operator() {

            }

            public void defined_operator(Token definedOp, boolean isExtended) {

            }

            public void extended_intrinsic_op() {

            }

            public void label(Token lbl) {

            }

            public void label_list__begin() {

            }

            public void label_list(int count) {

            }

            public void type_spec() {

            }

            public void type_param_value(boolean hasExpr, boolean hasAsterisk, boolean hasColon) {

            }

            public void intrinsic_type_spec(Token keyword1, Token keyword2, int type, boolean hasKindSelector) {

            }

            public void kind_selector(Token token1, Token token2, boolean hasExpression) {

            }

            public void signed_int_literal_constant(Token sign) {

            }

            public void int_literal_constant(Token digitString, Token kindParam) {

            }

            public void kind_param(Token kind) {

            }

            public void boz_literal_constant(Token constant) {

            }

            public void signed_real_literal_constant(Token sign) {

            }

            public void real_literal_constant(Token realConstant, Token kindParam) {

            }

            public void complex_literal_constant() {

            }

            public void real_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {

            }

            public void imag_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {

            }

            public void char_selector(Token tk1, Token tk2, int kindOrLen1, int kindOrLen2, boolean hasAsterisk) {

            }

            public void length_selector(Token len, int kindOrLen, boolean hasAsterisk) {

            }

            public void char_length(boolean hasTypeParamValue) {

            }

            public void scalar_int_literal_constant() {

            }

            public void char_literal_constant(Token digitString, Token id, Token str) {

            }

            public void logical_literal_constant(Token logicalValue, boolean isTrue, Token kindParam) {

            }

            public void derived_type_def() {

            }

            public void type_param_or_comp_def_stmt(Token eos, int type) {

            }

            public void type_param_or_comp_def_stmt_list() {

            }

            public void derived_type_stmt(Token label, Token keyword, Token id, Token eos, boolean hasTypeAttrSpecList, boolean hasGenericNameList) {

            }

            public void type_attr_spec(Token keyword, Token id, int specType) {

            }

            public void type_attr_spec_list__begin() {

            }

            public void type_attr_spec_list(int count) {

            }

            public void private_or_sequence() {

            }

            public void end_type_stmt(Token label, Token endKeyword, Token typeKeyword, Token id, Token eos) {

            }

            public void sequence_stmt(Token label, Token sequenceKeyword, Token eos) {

            }

            public void type_param_decl(Token id, boolean hasInit) {

            }

            public void type_param_decl_list__begin() {

            }

            public void type_param_decl_list(int count) {

            }

            public void type_param_attr_spec(Token kindOrLen) {

            }

            public void component_def_stmt(int type) {

            }

            public void data_component_def_stmt(Token label, Token eos, boolean hasSpec) {

            }

            public void component_attr_spec(Token attrKeyword, int specType) {

            }

            public void component_attr_spec_list__begin() {

            }

            public void component_attr_spec_list(int count) {

            }

            public void component_decl(Token id, boolean hasComponentArraySpec, boolean hasCoArraySpec, boolean hasCharLength, boolean hasComponentInitialization) {

            }

            public void component_decl_list__begin() {

            }

            public void component_decl_list(int count) {

            }

            public void component_array_spec(boolean isExplicit) {

            }

            public void deferred_shape_spec_list__begin() {

            }

            public void deferred_shape_spec_list(int count) {

            }

            public void component_initialization() {

            }

            public void proc_component_def_stmt(Token label, Token procedureKeyword, Token eos, boolean hasInterface) {

            }

            public void proc_component_attr_spec(Token attrSpecKeyword, Token id, int specType) {

            }

            public void proc_component_attr_spec_list__begin() {

            }

            public void proc_component_attr_spec_list(int count) {

            }

            public void private_components_stmt(Token label, Token privateKeyword, Token eos) {

            }

            public void type_bound_procedure_part(int count, boolean hasBindingPrivateStmt) {

            }

            public void binding_private_stmt(Token label, Token privateKeyword, Token eos) {

            }

            public void proc_binding_stmt(Token label, int type, Token eos) {

            }

            public void specific_binding(Token procedureKeyword, Token interfaceName, Token bindingName, Token procedureName, boolean hasBindingAttrList) {

            }

            public void generic_binding(Token genericKeyword, boolean hasAccessSpec) {

            }

            public void binding_attr(Token bindingAttr, int attr, Token id) {

            }

            public void binding_attr_list__begin() {

            }

            public void binding_attr_list(int count) {

            }

            public void final_binding(Token finalKeyword) {

            }

            public void derived_type_spec(Token typeName, boolean hasTypeParamSpecList) {

            }

            public void type_param_spec(Token keyword) {

            }

            public void type_param_spec_list__begin() {

            }

            public void type_param_spec_list(int count) {

            }

            public void structure_constructor(Token id) {

            }

            public void component_spec(Token id) {

            }

            public void component_spec_list__begin() {

            }

            public void component_spec_list(int count) {

            }

            public void component_data_source() {

            }

            public void enum_def(int numEls) {

            }

            public void enum_def_stmt(Token label, Token enumKeyword, Token bindKeyword, Token id, Token eos) {

            }

            public void enumerator_def_stmt(Token label, Token enumeratorKeyword, Token eos) {

            }

            public void enumerator(Token id, boolean hasExpr) {

            }

            public void enumerator_list__begin() {

            }

            public void enumerator_list(int count) {

            }

            public void end_enum_stmt(Token label, Token endKeyword, Token enumKeyword, Token eos) {

            }

            public void array_constructor() {

            }

            public void ac_spec() {

            }

            public void ac_value() {

            }

            public void ac_value_list__begin() {

            }

            public void ac_value_list(int count) {

            }

            public void ac_implied_do() {

            }

            public void ac_implied_do_control(boolean hasStride) {

            }

            public void scalar_int_variable() {

            }

            public void type_declaration_stmt(Token label, int numAttributes, Token eos) {

            }

            public void declaration_type_spec(Token udtKeyword, int type) {

            }

            public void attr_spec(Token attrKeyword, int attr) {

            }

            public void entity_decl(Token id) {

            }

            public void entity_decl_list__begin() {

            }

            public void entity_decl_list(int count) {

            }

            public void initialization(boolean hasExpr, boolean hasNullInit) {

            }

            public void null_init(Token id) {

            }

            public void access_spec(Token keyword, int type) {

            }

            public void language_binding_spec(Token keyword, Token id, boolean hasName) {

            }

            public void array_spec(int count) {

            }

            public void array_spec_element(int type) {

            }

            public void explicit_shape_spec(boolean hasUpperBound) {

            }

            public void explicit_shape_spec_list__begin() {

            }

            public void explicit_shape_spec_list(int count) {

            }

            public void co_array_spec() {

            }

            public void intent_spec(Token intentKeyword1, Token intentKeyword2, int intent) {

            }

            public void access_stmt(Token label, Token eos, boolean hasList) {

            }

            public void deferred_co_shape_spec() {

            }

            public void deferred_co_shape_spec_list__begin() {

            }

            public void deferred_co_shape_spec_list(int count) {

            }

            public void explicit_co_shape_spec() {

            }

            public void explicit_co_shape_spec_suffix() {

            }

            public void access_id() {

            }

            public void access_id_list__begin() {

            }

            public void access_id_list(int count) {

            }

            public void allocatable_stmt(Token label, Token keyword, Token eos, int count) {

            }

            public void allocatable_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {

            }

            public void asynchronous_stmt(Token label, Token keyword, Token eos) {

            }

            public void bind_stmt(Token label, Token eos) {

            }

            public void bind_entity(Token entity, boolean isCommonBlockName) {

            }

            public void bind_entity_list__begin() {

            }

            public void bind_entity_list(int count) {

            }

            public void data_stmt(Token label, Token keyword, Token eos, int count) {

            }

            public void data_stmt_set() {

            }

            public void data_stmt_object() {

            }

            public void data_stmt_object_list__begin() {

            }

            public void data_stmt_object_list(int count) {

            }

            public void data_implied_do(Token id, boolean hasThirdExpr) {

            }

            public void data_i_do_object() {

            }

            public void data_i_do_object_list__begin() {

            }

            public void data_i_do_object_list(int count) {

            }

            public void data_stmt_value(Token asterisk) {

            }

            public void data_stmt_value_list__begin() {

            }

            public void data_stmt_value_list(int count) {

            }

            public void scalar_int_constant() {

            }

            public void hollerith_constant(Token hollerithConstant) {

            }

            public void data_stmt_constant() {

            }

            public void dimension_stmt(Token label, Token keyword, Token eos, int count) {

            }

            public void dimension_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {

            }

            public void dimension_spec(Token dimensionKeyword) {

            }

            public void intent_stmt(Token label, Token keyword, Token eos) {

            }

            public void optional_stmt(Token label, Token keyword, Token eos) {

            }

            public void parameter_stmt(Token label, Token keyword, Token eos) {

            }

            public void named_constant_def_list__begin() {

            }

            public void named_constant_def_list(int count) {

            }

            public void named_constant_def(Token id) {

            }

            public void pointer_stmt(Token label, Token keyword, Token eos) {

            }

            public void pointer_decl_list__begin() {

            }

            public void pointer_decl_list(int count) {

            }

            public void pointer_decl(Token id, boolean hasSpecList) {

            }

            public void protected_stmt(Token label, Token keyword, Token eos) {

            }

            public void save_stmt(Token label, Token keyword, Token eos, boolean hasSavedEntityList) {

            }

            public void saved_entity_list__begin() {

            }

            public void saved_entity_list(int count) {

            }

            public void saved_entity(Token id, boolean isCommonBlockName) {

            }

            public void target_stmt(Token label, Token keyword, Token eos, int count) {

            }

            public void target_decl(Token id, boolean hasArraySpec, boolean hasCoArraySpec) {

            }

            public void value_stmt(Token label, Token keyword, Token eos) {

            }

            public void volatile_stmt(Token label, Token keyword, Token eos) {

            }

            public void implicit_stmt(Token label, Token implicitKeyword, Token noneKeyword, Token eos, boolean hasImplicitSpecList) {

            }

            public void implicit_spec() {

            }

            public void implicit_spec_list__begin() {

            }

            public void implicit_spec_list(int count) {

            }

            public void letter_spec(Token id1, Token id2) {

            }

            public void letter_spec_list__begin() {

            }

            public void letter_spec_list(int count) {

            }

            public void namelist_stmt(Token label, Token keyword, Token eos, int count) {

            }

            public void namelist_group_name(Token id) {

            }

            public void namelist_group_object(Token id) {

            }

            public void namelist_group_object_list__begin() {

            }

            public void namelist_group_object_list(int count) {

            }

            public void equivalence_stmt(Token label, Token equivalenceKeyword, Token eos) {

            }

            public void equivalence_set() {

            }

            public void equivalence_set_list__begin() {

            }

            public void equivalence_set_list(int count) {

            }

            public void equivalence_object() {

            }

            public void equivalence_object_list__begin() {

            }

            public void equivalence_object_list(int count) {

            }

            public void common_stmt(Token label, Token commonKeyword, Token eos, int numBlocks) {

            }

            public void common_block_name(Token id) {

            }

            public void common_block_object_list__begin() {

            }

            public void common_block_object_list(int count) {

            }

            public void common_block_object(Token id, boolean hasShapeSpecList) {

            }

            public void variable() {

            }

            public void designator(boolean hasSubstringRange) {

            }

            public void designator_or_func_ref() {

            }

            public void substring_range_or_arg_list() {

            }

            public void substr_range_or_arg_list_suffix() {

            }

            public void logical_variable() {

            }

            public void default_logical_variable() {

            }

            public void scalar_default_logical_variable() {

            }

            public void char_variable() {

            }

            public void default_char_variable() {

            }

            public void scalar_default_char_variable() {

            }

            public void int_variable() {

            }

            public void substring(boolean hasSubstringRange) {

            }

            public void substring_range(boolean hasLowerBound, boolean hasUpperBound) {

            }

            public void data_ref(int numPartRef) {

            }

            public void part_ref(Token id, boolean hasSelectionSubscriptList, boolean hasImageSelector) {

            }

            public void section_subscript(boolean hasLowerBound, boolean hasUpperBound, boolean hasStride, boolean isAmbiguous) {

            }

            public void section_subscript_list__begin() {

            }

            public void section_subscript_list(int count) {

            }

            public void vector_subscript() {

            }

            public void allocate_stmt(Token label, Token allocateKeyword, Token eos, boolean hasTypeSpec, boolean hasAllocOptList) {

            }

            public void image_selector(int exprCount) {

            }

            public void alloc_opt(Token allocOpt) {

            }

            public void alloc_opt_list__begin() {

            }

            public void alloc_opt_list(int count) {

            }

            public void allocation(boolean hasAllocateShapeSpecList, boolean hasAllocateCoArraySpec) {

            }

            public void allocation_list__begin() {

            }

            public void allocation_list(int count) {

            }

            public void allocate_object() {

            }

            public void allocate_object_list__begin() {

            }

            public void allocate_object_list(int count) {

            }

            public void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound) {

            }

            public void allocate_shape_spec_list__begin() {

            }

            public void allocate_shape_spec_list(int count) {

            }

            public void nullify_stmt(Token label, Token nullifyKeyword, Token eos) {

            }

            public void pointer_object() {

            }

            public void pointer_object_list__begin() {

            }

            public void pointer_object_list(int count) {

            }

            public void deallocate_stmt(Token label, Token deallocateKeyword, Token eos, boolean hasDeallocOptList) {

            }

            public void dealloc_opt(Token id) {

            }

            public void dealloc_opt_list__begin() {

            }

            public void dealloc_opt_list(int count) {

            }

            public void allocate_co_array_spec() {

            }

            public void allocate_co_shape_spec(boolean hasExpr) {

            }

            public void allocate_co_shape_spec_list__begin() {

            }

            public void allocate_co_shape_spec_list(int count) {

            }

            public void primary() {

            }

            public void level_1_expr(Token definedUnaryOp) {

            }

            public void defined_unary_op(Token definedOp) {

            }

            public void power_operand(boolean hasPowerOperand) {

            }

            public void power_operand__power_op(Token powerOp) {

            }

            public void mult_operand(int numMultOps) {

            }

            public void mult_operand__mult_op(Token multOp) {

            }

            public void add_operand(Token addOp, int numAddOps) {

            }

            public void add_operand__add_op(Token addOp) {

            }

            public void level_2_expr(int numConcatOps) {

            }

            public void power_op(Token powerKeyword) {

            }

            public void mult_op(Token multKeyword) {

            }

            public void add_op(Token addKeyword) {

            }

            public void level_3_expr(Token relOp) {

            }

            public void concat_op(Token concatKeyword) {

            }

            public void rel_op(Token relOp) {

            }

            public void and_operand(boolean hasNotOp, int numAndOps) {

            }

            public void and_operand__not_op(boolean hasNotOp) {

            }

            public void or_operand(int numOrOps) {

            }

            public void equiv_operand(int numEquivOps) {

            }

            public void equiv_operand__equiv_op(Token equivOp) {

            }

            public void level_5_expr(int numDefinedBinaryOps) {

            }

            public void level_5_expr__defined_binary_op(Token definedBinaryOp) {

            }

            public void not_op(Token notOp) {

            }

            public void and_op(Token andOp) {

            }

            public void or_op(Token orOp) {

            }

            public void equiv_op(Token equivOp) {

            }

            public void expr() {

            }

            public void defined_binary_op(Token binaryOp) {

            }

            public void assignment_stmt(Token label, Token eos) {

            }

            public void pointer_assignment_stmt(Token label, Token eos, boolean hasBoundsSpecList, boolean hasBRList) {

            }

            public void data_pointer_object() {

            }

            public void bounds_spec() {

            }

            public void bounds_spec_list__begin() {

            }

            public void bounds_spec_list(int count) {

            }

            public void bounds_remapping() {

            }

            public void bounds_remapping_list__begin() {

            }

            public void bounds_remapping_list(int count) {

            }

            public void proc_pointer_object() {

            }

            public void where_stmt__begin() {

            }

            public void where_stmt(Token label, Token whereKeyword) {

            }

            public void where_construct(int numConstructs, boolean hasMaskedElsewhere, boolean hasElsewhere) {

            }

            public void where_construct_stmt(Token id, Token whereKeyword, Token eos) {

            }

            public void where_body_construct() {

            }

            public void masked_elsewhere_stmt(Token label, Token elseKeyword, Token whereKeyword, Token id, Token eos) {

            }

            public void masked_elsewhere_stmt__end(int numBodyConstructs) {

            }

            public void elsewhere_stmt(Token label, Token elseKeyword, Token whereKeyword, Token id, Token eos) {

            }

            public void elsewhere_stmt__end(int numBodyConstructs) {

            }

            public void end_where_stmt(Token label, Token endKeyword, Token whereKeyword, Token id, Token eos) {

            }

            public void forall_construct() {

            }

            public void forall_construct_stmt(Token label, Token id, Token forallKeyword, Token eos) {

            }

            public void forall_header() {

            }

            public void forall_triplet_spec(Token id, boolean hasStride) {

            }

            public void forall_triplet_spec_list__begin() {

            }

            public void forall_triplet_spec_list(int count) {

            }

            public void forall_body_construct() {

            }

            public void forall_assignment_stmt(boolean isPointerAssignment) {

            }

            public void end_forall_stmt(Token label, Token endKeyword, Token forallKeyword, Token id, Token eos) {

            }

            public void forall_stmt__begin() {

            }

            public void forall_stmt(Token label, Token forallKeyword) {

            }

            public void block() {

            }

            public void if_construct() {

            }

            public void if_then_stmt(Token label, Token id, Token ifKeyword, Token thenKeyword, Token eos) {

            }

            public void else_if_stmt(Token label, Token elseKeyword, Token ifKeyword, Token thenKeyword, Token id, Token eos) {

            }

            public void else_stmt(Token label, Token elseKeyword, Token id, Token eos) {

            }

            public void end_if_stmt(Token label, Token endKeyword, Token ifKeyword, Token id, Token eos) {

            }

            public void if_stmt__begin() {

            }

            public void if_stmt(Token label, Token ifKeyword) {

            }

            public void case_construct() {

            }

            public void select_case_stmt(Token label, Token id, Token selectKeyword, Token caseKeyword, Token eos) {

            }

            public void case_stmt(Token label, Token caseKeyword, Token id, Token eos) {

            }

            public void end_select_stmt(Token label, Token endKeyword, Token selectKeyword, Token id, Token eos) {

            }

            public void case_selector(Token defaultToken) {

            }

            public void case_value_range() {

            }

            public void case_value_range_list__begin() {

            }

            public void case_value_range_list(int count) {

            }

            public void case_value_range_suffix() {

            }

            public void case_value() {

            }

            public void associate_construct() {

            }

            public void associate_stmt(Token label, Token id, Token associateKeyword, Token eos) {

            }

            public void association_list__begin() {

            }

            public void association_list(int count) {

            }

            public void association(Token id) {

            }

            public void selector() {

            }

            public void end_associate_stmt(Token label, Token endKeyword, Token associateKeyword, Token id, Token eos) {

            }

            public void select_type_construct() {

            }

            public void select_type_stmt(Token label, Token selectConstructName, Token associateName, Token eos) {

            }

            public void select_type(Token selectKeyword, Token typeKeyword) {

            }

            public void type_guard_stmt(Token label, Token typeKeyword, Token isOrDefaultKeyword, Token selectConstructName, Token eos) {

            }

            public void end_select_type_stmt(Token label, Token endKeyword, Token selectKeyword, Token id, Token eos) {

            }

            public void do_construct() {

            }

            public void block_do_construct() {

            }

            public void do_stmt(Token label, Token id, Token doKeyword, Token digitString, Token eos, boolean hasLoopControl) {

            }

            public void label_do_stmt(Token label, Token id, Token doKeyword, Token digitString, Token eos, boolean hasLoopControl) {

            }

            public void loop_control(Token whileKeyword, boolean hasOptExpr) {

            }

            public void do_variable() {

            }

            public void end_do() {

            }

            public void end_do_stmt(Token label, Token endKeyword, Token doKeyword, Token id, Token eos) {

            }

            public void do_term_action_stmt(Token label, Token endKeyword, Token doKeyword, Token id, Token eos) {

            }

            public void cycle_stmt(Token label, Token cycleKeyword, Token id, Token eos) {

            }

            public void exit_stmt(Token label, Token exitKeyword, Token id, Token eos) {

            }

            public void goto_stmt(Token goKeyword, Token toKeyword, Token label, Token eos) {

            }

            public void computed_goto_stmt(Token label, Token goKeyword, Token toKeyword, Token eos) {

            }

            public void assign_stmt(Token label1, Token assignKeyword, Token label2, Token toKeyword, Token name, Token eos) {

            }

            public void assigned_goto_stmt(Token label, Token goKeyword, Token toKeyword, Token name, Token eos) {

            }

            public void stmt_label_list() {

            }

            public void pause_stmt(Token label, Token pauseKeyword, Token constant, Token eos) {

            }

            public void arithmetic_if_stmt(Token label, Token ifKeyword, Token label1, Token label2, Token label3, Token eos) {

            }

            public void continue_stmt(Token label, Token continueKeyword, Token eos) {

            }

            public void stop_stmt(Token label, Token stopKeyword, Token eos, boolean hasStopCode) {

            }

            public void stop_code(Token digitString) {

            }

            public void scalar_char_constant() {

            }

            public void io_unit() {

            }

            public void file_unit_number() {

            }

            public void open_stmt(Token label, Token openKeyword, Token eos) {

            }

            public void connect_spec(Token id) {

            }

            public void connect_spec_list__begin() {

            }

            public void connect_spec_list(int count) {

            }

            public void close_stmt(Token label, Token closeKeyword, Token eos) {

            }

            public void close_spec(Token closeSpec) {

            }

            public void close_spec_list__begin() {

            }

            public void close_spec_list(int count) {

            }

            public void read_stmt(Token label, Token readKeyword, Token eos, boolean hasInputItemList) {

            }

            public void write_stmt(Token label, Token writeKeyword, Token eos, boolean hasOutputItemList) {

            }

            public void print_stmt(Token label, Token printKeyword, Token eos, boolean hasOutputItemList) {

            }

            public void io_control_spec(boolean hasExpression, Token keyword, boolean hasAsterisk) {

            }

            public void io_control_spec_list__begin() {

            }

            public void io_control_spec_list(int count) {

            }

            public void format() {

            }

            public void input_item() {

            }

            public void input_item_list__begin() {

            }

            public void input_item_list(int count) {

            }

            public void output_item() {

            }

            public void output_item_list__begin() {

            }

            public void output_item_list(int count) {

            }

            public void io_implied_do() {

            }

            public void io_implied_do_object() {

            }

            public void io_implied_do_control() {

            }

            public void dtv_type_spec(Token typeKeyword) {

            }

            public void wait_stmt(Token label, Token waitKeyword, Token eos) {

            }

            public void wait_spec(Token id) {

            }

            public void wait_spec_list__begin() {

            }

            public void wait_spec_list(int count) {

            }

            public void backspace_stmt(Token label, Token backspaceKeyword, Token eos, boolean hasPositionSpecList) {

            }

            public void endfile_stmt(Token label, Token endKeyword, Token fileKeyword, Token eos, boolean hasPositionSpecList) {

            }

            public void rewind_stmt(Token label, Token rewindKeyword, Token eos, boolean hasPositionSpecList) {

            }

            public void position_spec(Token id) {

            }

            public void position_spec_list__begin() {

            }

            public void position_spec_list(int count) {

            }

            public void flush_stmt(Token label, Token flushKeyword, Token eos, boolean hasFlushSpecList) {

            }

            public void flush_spec(Token id) {

            }

            public void flush_spec_list__begin() {

            }

            public void flush_spec_list(int count) {

            }

            public void inquire_stmt(Token label, Token inquireKeyword, Token id, Token eos, boolean isType2) {

            }

            public void inquire_spec(Token id) {

            }

            public void inquire_spec_list__begin() {

            }

            public void inquire_spec_list(int count) {

            }

            public void format_stmt(Token label, Token formatKeyword, Token eos) {

            }

            public void format_specification(boolean hasFormatItemList) {

            }

            public void format_item(Token descOrDigit, boolean hasFormatItemList) {

            }

            public void format_item_list__begin() {

            }

            public void format_item_list(int count) {

            }

            public void v_list_part(Token plus_minus, Token digitString) {

            }

            public void v_list__begin() {

            }

            public void v_list(int count) {

            }

            public void main_program__begin() {

            }

            public void main_program(boolean hasProgramStmt, boolean hasExecutionPart, boolean hasInternalSubprogramPart) {

            }

            public void ext_function_subprogram(boolean hasPrefix) {

            }

            public void use_stmt(Token label, Token useKeyword, Token id, Token onlyKeyword, Token eos, boolean hasModuleNature, boolean hasRenameList, boolean hasOnly) {

            }

            public void module_nature(Token nature) {

            }

            public void rename(Token id1, Token id2, Token op1, Token defOp1, Token op2, Token defOp2) {

            }

            public void rename_list__begin() {

            }

            public void rename_list(int count) {

            }

            public void only() {

            }

            public void only_list__begin() {

            }

            public void only_list(int count) {

            }

            public void block_data() {

            }

            public void block_data_stmt__begin() {

            }

            public void block_data_stmt(Token label, Token blockKeyword, Token dataKeyword, Token id, Token eos) {

            }

            public void end_block_data_stmt(Token label, Token endKeyword, Token blockKeyword, Token dataKeyword, Token id, Token eos) {

            }

            public void interface_block() {

            }

            public void interface_specification() {

            }

            public void interface_stmt__begin() {

            }

            public void interface_stmt(Token label, Token abstractToken, Token keyword, Token eos, boolean hasGenericSpec) {

            }

            public void end_interface_stmt(Token label, Token kw1, Token kw2, Token eos, boolean hasGenericSpec) {

            }

            public void interface_body(boolean hasPrefix) {

            }

            public void procedure_stmt(Token label, Token module, Token procedureKeyword, Token eos) {

            }

            public void generic_spec(Token keyword, Token name, int type) {

            }

            public void dtio_generic_spec(Token rw, Token format, int type) {

            }

            public void import_stmt(Token label, Token importKeyword, Token eos, boolean hasGenericNameList) {

            }

            public void external_stmt(Token label, Token externalKeyword, Token eos) {

            }

            public void procedure_declaration_stmt(Token label, Token procedureKeyword, Token eos, boolean hasProcInterface, int count) {

            }

            public void proc_interface(Token id) {

            }

            public void proc_attr_spec(Token attrKeyword, Token id, int spec) {

            }

            public void proc_decl(Token id, boolean hasNullInit) {

            }

            public void proc_decl_list__begin() {

            }

            public void proc_decl_list(int count) {

            }

            public void intrinsic_stmt(Token label, Token intrinsicToken, Token eos) {

            }

            public void function_reference(boolean hasActualArgSpecList) {

            }

            public void call_stmt(Token label, Token callKeyword, Token eos, boolean hasActualArgSpecList) {

            }

            public void procedure_designator() {

            }

            public void actual_arg_spec(Token keyword) {

            }

            public void actual_arg_spec_list__begin() {

            }

            public void actual_arg_spec_list(int count) {

            }

            public void actual_arg(boolean hasExpr, Token label) {

            }

            public void function_subprogram(boolean hasExePart, boolean hasIntSubProg) {

            }

            public void proc_language_binding_spec() {

            }

            public void prefix(int specCount) {

            }

            public void t_prefix(int specCount) {

            }

            public void prefix_spec(boolean isDecTypeSpec) {

            }

            public void t_prefix_spec(Token spec) {

            }

            public void suffix(Token resultKeyword, boolean hasProcLangBindSpec) {

            }

            public void result_name() {

            }

            public void entry_stmt(Token label, Token keyword, Token id, Token eos, boolean hasDummyArgList, boolean hasSuffix) {

            }

            public void return_stmt(Token label, Token keyword, Token eos, boolean hasScalarIntExpr) {

            }

            public void contains_stmt(Token label, Token keyword, Token eos) {

            }

            public void stmt_function_stmt(Token label, Token functionName, Token eos, boolean hasGenericNameList) {

            }

            public void end_of_stmt(Token eos) {

            }

            public void start_of_file(String fileName) {

            }

            public void end_of_file() {

            }

            public void cleanUp() {

            }
        };
    }

    static public class MyToken implements Token {

        org.netbeans.modules.cnd.antlr.Token t;

        public MyToken(org.netbeans.modules.cnd.antlr.Token t) {
            if(t.getType() == APTTokenTypes.EOF) {
                t = APTUtils.EOF_TOKEN2;
            }
            this.t = t;
        }

        public String getText() {
            return t.getText();
        }

        public void setText(String arg0) {
            t.setText(arg0);
        }

        public int getType() {
            return t.getType();
        }

        public void setType(int arg0) {
            t.setType(arg0);
        }

        public int getLine() {
            return t.getLine();
        }

        public void setLine(int arg0) {
            t.setLine(arg0);
        }

        public int getCharPositionInLine() {
            return t.getColumn();
        }

        public void setCharPositionInLine(int arg0) {
            t.setColumn(arg0);
        }

        public int getChannel() {
            //
            return 0;
        }

        public void setChannel(int arg0) {
            //
        }

        public int getTokenIndex() {
            //
            return 0;
        }

        public void setTokenIndex(int arg0) {
            //
        }

        public CharStream getInputStream() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setInputStream(CharStream arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public String toString() {
            return t.toString();
        }

    }


    static public class MyTokenStream implements org.antlr.runtime.TokenStream {
        TokenBuffer tb;

        public MyTokenStream(TokenBuffer tb) {
            this.tb = tb;
        }

        public Token LT(int arg0) {

            if (arg0 < 0) {
                arg0++; // e.g., translate LA(-1) to use offset i=0; then data[p+0-1]
                if ((tb.index() + arg0 - 1) < 0) {
                    return new MyToken(tb.LT(APTTokenTypes.EOF));
                }
            }

            return new MyToken(tb.LT(arg0));
        }

        public void consume() {
            tb.consume();
        }

        public int LA(int arg0) {
            int la = tb.LA(arg0);
            return (la != 1)?la:-1;
        }

        public int mark() {
            return tb.mark();
        }

        public int index() {
            return tb.index();
        }

        public void rewind(int arg0) {
            tb.rewind(arg0);
        }

        public void rewind() {
            tb.rewind(0);
        }

        public void seek(int arg0) {
            tb.seek(arg0);
        }

        public Token get(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public TokenSource getTokenSource() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String toString(int arg0, int arg1) {
            return tb.toString();
        }

        public String toString(Token arg0, Token arg1) {
            return tb.toString();
        }

        public void release(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int size() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getSourceName() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }


}
