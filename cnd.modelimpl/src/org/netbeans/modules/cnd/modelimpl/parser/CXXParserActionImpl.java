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

import java.util.Map;
import org.antlr.runtime.Token;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;

/**
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
public class CXXParserActionImpl implements CXXParserActionEx {

    private final CppParserActionImpl orig;

    public CXXParserActionImpl(CsmParserProvider.CsmParserParameters params) {
        orig = new CppParserActionImpl(params);
    }

    @Override
    public void enum_declaration(Token token) {
        orig.enum_declaration(convertToken(token));
    }

    @Override
    public void enum_strongly_typed(Token token) {
        orig.enum_strongly_typed(convertToken(token));
    }

    @Override
    public void enum_name(Token token) {
        orig.enum_name(convertToken(token));
    }

    @Override
    public void enum_body(Token token) {
        orig.enum_body(convertToken(token));
    }

    @Override
    public void enumerator(Token token) {
        orig.enumerator(convertToken(token));
    }

    @Override
    public void end_enum_body(Token token) {
        orig.end_enum_body(convertToken(token));
    }

    @Override
    public void end_enum_declaration(Token token) {
        orig.end_enum_declaration(convertToken(token));
    }

    @Override
    public void class_declaration(Token token) {
        orig.class_declaration(convertToken(token));
    }

    @Override
    public void class_kind(Token token) {
        orig.class_kind(convertToken(token));
    }

    @Override
    public void class_name(Token token) {
        orig.class_name(convertToken(token));
    }

    @Override
    public void class_body(Token token) {
        orig.class_body(convertToken(token));
    }

    @Override
    public void end_class_body(Token token) {
        orig.end_class_body(convertToken(token));
    }

    @Override
    public void end_class_declaration(Token token) {
        orig.end_class_declaration(convertToken(token));
    }

    @Override
    public void namespace_declaration(Token token) {
        orig.namespace_declaration(convertToken(token));
    }

    @Override
    public void namespace_name(Token token) {
        orig.namespace_name(convertToken(token));
    }

    @Override
    public void namespace_body(Token token) {
        orig.namespace_body(convertToken(token));
    }

    @Override
    public void end_namespace_body(Token token) {
        orig.end_namespace_body(convertToken(token));
    }

    @Override
    public void end_namespace_declaration(Token token) {
        orig.end_namespace_declaration(convertToken(token));
    }

    @Override
    public void compound_statement(Token token) {
        orig.compound_statement(convertToken(token));
    }

    @Override
    public void end_compound_statement(Token token) {
        orig.end_compound_statement(convertToken(token));
    }

    @Override
    public void id(Token token) {
        orig.id(convertToken(token));
    }

    @Override
    public void simple_type_id(Token token) {
        orig.simple_type_id(convertToken(token));
    }

    @Override
    public boolean isType(String name) {
        return orig.isType(name);
    }

    @Override
    public void pushFile(CsmFile file) {
        orig.pushFile(file);
    }

    @Override
    public CsmFile popFile() {
        return orig.popFile();
    }

    Map<Integer, CsmObject> getObjectsMap() {
        return orig.getObjectsMap();
    }

    private org.netbeans.modules.cnd.antlr.Token convertToken(Token token) {
        return ParserProviderImpl.convertToken(token);
    }

    @Override
    public void simple_declaration(Token token) {
        orig.simple_declaration(convertToken(token));
    }

    @Override
    public void end_simple_declaration(Token token) {
        orig.end_simple_declaration(convertToken(token));
    }

    @Override
    public void simple_type_specifier(Token token) {
        orig.simple_type_specifier(convertToken(token));
    }

    @Override
    public void nested_name_specifier(Token token) {
        orig.nested_name_specifier(convertToken(token));
    }

    @Override
    public void simple_template_id_nocheck(Token token) {
        orig.simple_template_id_nocheck(convertToken(token));
    }

    @Override
    public void simple_template_id(Token token) {
        orig.simple_template_id_nocheck(convertToken(token));
    }

    @Override
    public void decl_specifier(int kind, Token token) {
        orig.decl_specifier(kind, convertToken(token));
    }
}
