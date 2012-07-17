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
package org.netbeans.modules.cnd.modelimpl.parser;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;

/**
 *
 * @author nick
 */
public interface CXXParserAction extends CsmParserProvider.CsmParseCallback {
    
    public static int DECL_SPECIFIER__STORAGE_CLASS_SPECIFIER = 0;
    public static int DECL_SPECIFIER__FUNCTION_SPECIFIER = 1;
    public static int DECL_SPECIFIER__LITERAL_FRIEND = 2;
    public static int DECL_SPECIFIER__LITERAL_TYPEDEF = 3;
    public static int DECL_SPECIFIER__TYPE_SPECIFIER = 4;
    public static int DECL_SPECIFIER__LITERAL_CONSTEXPR = 5;
    
    public static int SIMPLE_TEMPLATE_ID__TEMPLATE_ARGUMENT_LIST = 0;
    public static int SIMPLE_TEMPLATE_ID__END_TEMPLATE_ARGUMENT_LIST = 1;
    
    public static int SIMPLE_TEMPLATE_ID_OR_IDENT__TEMPLATE_ARGUMENT_LIST = 0;
    public static int SIMPLE_TEMPLATE_ID_OR_IDENT__END_TEMPLATE_ARGUMENT_LIST = 1;

    public static int SIMPLE_TEMPLATE_ID_NOCHECK__TEMPLATE_ARGUMENT_LIST = 0;
    public static int SIMPLE_TEMPLATE_ID_NOCHECK__END_TEMPLATE_ARGUMENT_LIST = 1;
                      
    public static int TEMPLATE_DECLARATION__TEMPLATE_ARGUMENT_LIST = 0;
    public static int TEMPLATE_DECLARATION__END_TEMPLATE_ARGUMENT_LIST = 1;
    
    public static int TYPE_PARAMETER__CLASS = 0;
    public static int TYPE_PARAMETER__CLASS_ASSIGNEQUAL = 1;
    public static int TYPE_PARAMETER__TYPENAME = 2;
    public static int TYPE_PARAMETER__TYPENAME_ASSIGNEQUAL = 3;
    
    boolean type_specifier_already_present(TokenStream input);

    boolean identifier_is(int kind, Token token);
    
    boolean top_level_of_template_arguments();
    
    void enum_declaration(Token token);
    void enum_strongly_typed(Token token);
    void enum_name(Token token);
    void enum_body(Token token);
    void enumerator(Token token);
    void end_enum_body(Token token);
    void end_enum_declaration(Token token);

    void class_declaration(Token token);
    void class_kind(Token token);
    void class_name(Token token);
    void class_body(Token token);
    void end_class_body(Token token);
    void end_class_declaration(Token token);
    
    void namespace_declaration(Token token);
    void namespace_name(Token token);
    void namespace_body(Token token);    
    void end_namespace_body(Token token);
    void end_namespace_declaration(Token token);

    void compound_statement(Token token);
    void end_compound_statement(Token token);
    
    void simple_declaration(Token token);
    void end_simple_declaration(Token token);
    
    void decl_specifier(int kind, Token token);
    
    void simple_type_specifier(Token token);
    void nested_name_specifier(Token token);
    
    void id(Token token);
    
    void simple_type_id(Token token);
    
    boolean isType(String name);
        
    void simple_template_id(Token token);
    void simple_template_id(int kind, Token token);
    
    void simple_template_id_or_ident(Token token);
    void simple_template_id_or_ident(int kind, Token token);

    void simple_template_id_nocheck(Token token);
    void simple_template_id_nocheck(int kind, Token token);
    
    void template_declaration(int kind, Token token);
    
    void type_parameter(int kind, Token token, Token token2, Token token3);

    void elaborated_type_specifier(Token token);
    
    void using_declaration(Token token);
    
    void parameter_declaration_list();
    void end_parameter_declaration_list();

    void decl_specifiers();
    void end_decl_specifiers();
    
}
