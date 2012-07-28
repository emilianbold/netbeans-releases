/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.ArrayDeque;
import java.util.Deque;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 * @author nick
 */
public class CppParserEmptyActionImpl implements CppParserActionEx {
    private final Deque<CsmFile> files;

    CppParserEmptyActionImpl(CsmFile file) {
        files = new ArrayDeque<CsmFile>();
        files.push(file);
    }

    @Override
    public boolean type_specifier_already_present(TokenStream input) {        
        return true;
    }
    
    @Override
    public void enum_declaration(Token token) {
    }

    @Override
    public void enum_strongly_typed(Token token) {
    }

    @Override
    public void enum_name(Token token) {
    }

    @Override
    public void enum_body(Token token) {
    }

    @Override
    public void enumerator(Token token) {
    }

    @Override
    public void end_enum_body(Token token) {
    }

    @Override
    public void end_enum_declaration(Token token) {
    }

    @Override
    public void class_name(Token token) {
    }

    @Override
    public void class_body(Token token) {
    }

    @Override
    public void end_class_body(Token token) {
    }

    @Override
    public void namespace_body(Token token) {
    }

    @Override
    public void end_namespace_body(Token token) {
    }

    @Override
    public void compound_statement(Token token) {
    }

    @Override
    public void end_compound_statement(Token token) {
    }

    @Override
    public void id(Token token) {
    }

    @Override
    public boolean isType(String name) {
        return false;
    }

    @Override
    public void namespace_declaration(Token token) {
    }

    @Override
    public void end_namespace_declaration(Token token) {
    }

    @Override
    public void namespace_name(Token token) {
    }

    @Override
    public void class_declaration(Token token) {
    }

    @Override
    public void end_class_declaration(Token token) {
    }

    @Override
    public void class_kind(Token token) {
    }

    @Override
    public void simple_type_id(Token token) {
    }

    @Override
    public void pushFile(CsmFile file) {
        files.push(file);
    }

    @Override
    public CsmFile popFile() {
        CsmFile out = files.peek();
        files.pop();
        return out;
    }

    @Override
    public void simple_type_specifier(Token token) {
    }

    @Override
    public void nested_name_specifier(Token token) {
    }

    @Override
    public void simple_template_id_nocheck(Token token) {
    }

    @Override
    public void simple_template_id_nocheck(int kind, Token token) {
    }
    
    @Override
    public void simple_template_id(Token token) {
    }

    @Override
    public void simple_template_id(int kind, Token token) {
    }
    
    @Override
    public void simple_declaration(Token token) {
    }

    @Override
    public void end_simple_declaration(Token token) {
    }

    @Override
    public void decl_specifier(int kind, Token token) {
    }

    @Override
    public void simple_template_id_or_ident(Token token) {
    }

    @Override
    public void simple_template_id_or_ident(int kind, Token token) {
    }

    @Override
    public void type_parameter(int kind, Token token, Token token2, Token token3) {
    }
    
    @Override
    public void elaborated_type_specifier(Token token) {
    }    
    
    @Override
    public void using_declaration(Token token) {
    }
    
    @Override
    public void parameter_declaration_list() {
    }

    @Override
    public void end_parameter_declaration_list() {
    }

    @Override
    public void decl_specifiers() {
    }

    @Override
    public void end_decl_specifiers() {
    }

    @Override
    public boolean identifier_is(int kind, Token token) {
        return true;
    }

    @Override
    public boolean top_level_of_template_arguments() {
        return true;
    }

    @Override
    public void template_declaration(int kind, Token token) {
    }

    @Override
    public void using_directive(Token usingToken, Token namespaceToken) {
    }

    @Override
    public void using_directive(int kind, Token token) {
    }

    @Override
    public void end_using_directive(Token semicolonToken) {
    }
    
}
