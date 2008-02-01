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

package org.netbeans.modules.cnd.modelimpl.trace;

/**
 * pre-integration tests for parser
 * @author Vladimir Voskresensky
 */
public class FileModelTest extends TraceModelTestBase {

    public FileModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
	System.setProperty("parser.report.errors", "true");
        super.setUp();
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }
    
    public void testIncludeMacroExpansion() throws Exception {
        // IZ#124635
        performTest("include_macro_expanding.cc");
    }
    
    public void testParserRecover() throws Exception {
        performTest("parser_recover.cc");
    }
    
    public void testBitFields() throws Exception {
        performTest("bitFields.cc");
    }
    
    public void testFunWithoutRetTypeInClassBody() throws Exception {
        performTest("constructors_and_fun_no_ret_types.cc");
    }
    
    public void testStackOverflowOnCastExpression() throws Exception {
        // IZ#115549 StackOverflowError on parsing long expressions
        performTest("stackoverflow.cc");        
    }
    
    public void testIncompleteString() throws Exception {
        performTest("incomplete_string.cc");        
    }
    
    public void testPreProcDefinedKeyword() throws Exception {
        performTest("preproc_defined_keyword.cc");        
    }
    
    public void testFriendsDeclaration() throws Exception {
        performTest("friend.cc"); // NOI18N
    }
    
    public void testCNavigation() throws Exception {
        performTest("cnav.c"); // NOI18N
    }
    
    public void testDummy() throws Exception {
        performTest("dummy.cc"); // NOI18N
    }
    
    public void testDefineMacro() throws Exception {
        performTest("define_macro.cc"); // NOI18N
    }
    
    public void testIncludeCorrectness() throws Exception {
        performTest("test_include_correcteness.cc"); // NOI18N
    }   
    
    public void testTemplateExplicitInstantiation() throws Exception {
        performTest("template_explicit_instantiation.cc"); // NOI18N
    }
    
    public void testIntStaticField() throws Exception {
	performTest("int_static_field.cc"); // NOI18N
    }

    public void testResolverInfiniteLoop1() throws Exception {
	performTest("infinite1.cc"); // NOI18N
    }

    public void testResolverInfiniteLoop2() throws Exception {
	performTest("infinite2.cc"); // NOI18N
    }
    
    public void testResolverInfiniteLoop3() throws Exception {
	performTest("infinite3.cc"); // NOI18N
    }

    public void testCdeclAndPointerReturnType() throws Exception {
	performTest("cdecl_and_poniter_return_type.cc"); // NOI18N
    }
    
    public void testNestedClassesAndEnums_1() throws Exception {
	performTest("nested_classes_and_enums_1.cc"); // NOI18N
    }
    
    public void testFunctionPointerAsParameterType () throws Exception {
	performTest("function_pointer_as_param_type.cc"); // NOI18N
    }

    public void testFunctionPointerAsVariableType () throws Exception {
	performTest("function_pointer_as_var_type.cc"); // NOI18N
    }

    public void testFunctionPointerMisc() throws Exception {
	performTest("function_pointer_misc.cc"); // NOI18N
    }
	
    public void testUsingExtern() throws Exception {
        performTest("using_extern.h");
    }     

    public void testPartialSpeciazationsAndOperatorLess() throws Exception {
        performTest("partial_specializations.cc");
    }     
    
    public void testFuncDeclPrefixAttributes() throws Exception {
        performTest("func_decl_prefix_attributes.cc");
    }
    
    public void testVariableDefinition() throws Exception {
        performTest("variable_definition.cc"); // NOI18N
    }

    public void testFunctionPointerAsReturnType () throws Exception {
        performTest("function_pointer_as_return_type.cc"); // NOI18N
    }  
    
    public void testFunctionPointerAsTypeCast() throws Exception {
        performTest("function_pointer_as_type_cast.cc"); // NOI18N
    }
    
    public void testFunExpandedUnnamedParams() throws Exception {
        performTest("function_expanded_unnamed_params.cc"); // NOI18N
    }
    
    public void testErrorDirective() throws Exception {
	performTest("error_directive.cc"); // NOI18N
    }
    
    public void testEmptyLongHex() throws Exception {
	performTest("empty_long_hex.c"); // NOI18N
    }
    
    public void testUnresolvedPersistence() throws Exception {
        performTest("unresolved_persistence.cc"); // NOI18N
    }

    public void test0x01() throws Exception {
        performTest("0x01.c"); // NOI18N
    }
    
//  disable this test because it's OS-locale dependent and can fail where
//  russian is not installed    
//    public void test0x16() throws Exception {
//        performTest("0x16.cc"); // NOI18N
//    }
    
    public void testPreProcExpressionAndEmptyBodyMacro() throws Exception {
        performTest("ppExpressionAndEmptyBodyMacro.cc"); //NOI18N
    }
    
    public void testExprAfterIf() throws Exception {
        performTest("lparenAfterPPKwds.cc"); // NOI18N
    }

    public void testNamespaceAttribute() throws Exception {
        performTest("namespace_attrib.cc"); // NOI18N
    }
    
    public void testTypedefEnumInClassScope() throws Exception {
        performTest("typedef_enum_in_class_scope.cc"); // NOI18N
    }
    
    public void testNamedMemberEnumTypedef() throws Exception {
        performTest("named_member_enum_typedef.cc"); // NOI18N
    }
    
    public void testTypedefInsideFunc() throws Exception {
        performTest("typedef_inside_func.cc"); // NOI18N
    }
    
    public void testAttributesAlignedClass() throws Exception {
        performTest("attributes_aligned_class.cc"); // NOI18N
    }
    
    public void testStaticStruct() throws Exception {
        performTest("static_struct.cc"); // NOI18N
    }

    public void testInlineDtorDefinitionName() throws Exception {
        performTest("inline_dtor_definition_name.cc"); // NOI18N
    }
        
    public void testThrowConst() throws Exception {
        performTest("throw_const.cc"); // NOI18N
    }
    
    public void testTemplateDtorDefinition() throws Exception {
        performTest("template_dtor_definition.cc"); // NOI18N
    }
    
    public void testKAndRParams() throws Exception {
        performTest("k_and_r_params.c"); // NOI18N
    }
    
    public void testFunctionsAndVariables() throws Exception {
        performTest("functions_and_variables.cc"); // NOI18N
    }

    public void testStaticFunction() throws Exception {
        performTest("static_function.cc"); // NOI18N
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends TraceModelTestBase {
	
        public Failed(String testName) {
            super(testName);
        }

	@Override
	protected void setUp() throws Exception {
	    System.setProperty("parser.report.errors", "true");
	    super.setUp();
	}
	
        @Override
	protected Class getTestCaseDataClass() {
	    return FileModelTest.class;
	}
	
	public void testTemplateInnerClassDtorDefinition() throws Exception {
	    performTest("template_inner_class_dtor_definition.cc"); // NOI18N
	}
        
        @Override
	protected void postSetUp() {
	    // init flags needed for file model tests
	    getTraceModel().setDumpModel(true);
	    getTraceModel().setDumpPPState(true);
	}
   }
    
}
