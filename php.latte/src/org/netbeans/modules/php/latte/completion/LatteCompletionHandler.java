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
package org.netbeans.modules.php.latte.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.latte.completion.LatteElement.Parameter;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteCompletionHandler implements CodeCompletionHandler {
    private static final Set<LatteElement> MACROS = new HashSet<>();
    static {
        MACROS.add(LatteElement.Factory.create("link", "link ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("plink", "plink ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("continueIf", "continueIf ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("breakIf", "breakif ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("if", "if ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("else")); //NOI18N
        MACROS.add(LatteElement.Factory.create("elseif", "elseif ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("ifset", "ifset $${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("ifset block", "ifset #${block}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("elseifset", "elseifset $${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("ifCurrent", "ifCurrent ${Presenter}:${action}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("for", "for ${init}; ${cond}; ${exec}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("foreach", "foreach $${array} as $${item}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("while", "while ${true}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("include", "include '${file.latte}'")); //NOI18N
        MACROS.add(LatteElement.Factory.create("include block", "include #{block}'")); //NOI18N
        MACROS.add(LatteElement.Factory.create("extends", "extends '${latte.file}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("layout", "layout '${latte.file}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("control", "control ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("cache","cache $${key}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("snippet", "snippet ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("attr")); //NOI18N
        MACROS.add(LatteElement.Factory.create("block", "block #${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("define", "define #${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("includeblock", "includeblock '${latte.file}'")); //NOI18N
        MACROS.add(LatteElement.Factory.create("contentType", "contentType $${type}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("status", "status $${code}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("capture", "capture $${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("assign")); //NOI18N
        MACROS.add(LatteElement.Factory.create("default", "default $${name} = ${value}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("var", "var $${name} = ${value}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("dump", "dump $${var}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("syntax", "syntax ${mode}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("use", "use ${Class}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("form", "form ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("label", "label ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("input", "input ${name}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("debugbreak", "debugbreak ${cond}")); //NOI18N
        MACROS.add(LatteElement.Factory.create("l")); //NOI18N
        MACROS.add(LatteElement.Factory.create("r")); //NOI18N
        MACROS.add(LatteElement.Factory.create("first")); //NOI18N
        MACROS.add(LatteElement.Factory.create("last")); //NOI18N
        MACROS.add(LatteElement.Factory.create("sep")); //NOI18N
        MACROS.add(LatteElement.Factory.create("_")); //NOI18N
        MACROS.add(LatteElement.Factory.create("!")); //NOI18N
        MACROS.add(LatteElement.Factory.create("!_")); //NOI18N
    }

    private static final Set<LatteElement> HELPERS = new HashSet<>();
    static {
        HELPERS.add(LatteElement.Factory.create("truncate", Arrays.asList(new Parameter[] {new Parameter("length"), new Parameter("append", "'…'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("substr", Arrays.asList(new Parameter[] {new Parameter("offset"), new Parameter("length", "stringLegth")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("trim", Arrays.asList(new Parameter[] {new Parameter("charlist", " \\t\\n\\r\\0\\x0B\\xC2\\xA0")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("striptags")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("strip")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("webalize", Arrays.asList(new Parameter[] {new Parameter("charlist", "NULL"), new Parameter("lower", "true")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("toAscii")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("indent", Arrays.asList(new Parameter[] {new Parameter("level", "1"), new Parameter("char", "'\\t'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("replace", Arrays.asList(new Parameter[] {new Parameter("search"), new Parameter("replace", "''")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("replaceRE", Arrays.asList(new Parameter[] {new Parameter("pattern"), new Parameter("replace", "''")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("padLeft", Arrays.asList(new Parameter[] {new Parameter("length"), new Parameter("pad", "' '")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("padRight", Arrays.asList(new Parameter[] {new Parameter("length"), new Parameter("pad", "' '")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("repeat", Arrays.asList(new Parameter[] {new Parameter("count")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("implode", Arrays.asList(new Parameter[] {new Parameter("glue", "''")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("nl2br")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("lower")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("upper")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("firstLower")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("capitalize")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("date", Arrays.asList(new Parameter[] {new Parameter("'format'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("number", Arrays.asList(new Parameter[] {new Parameter("decimals", "0"), new Parameter("decPoint", "'.'")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("bytes", Arrays.asList(new Parameter[] {new Parameter("precision", "2")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("dataStream", Arrays.asList(new Parameter[] {new Parameter("mimetype", "NULL")}))); //NOI18N
        HELPERS.add(LatteElement.Factory.create("url")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("length")); //NOI18N
        HELPERS.add(LatteElement.Factory.create("null")); //NOI18N
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        return new DefaultCompletionResult(Collections.<CompletionProposal>emptyList(), false);
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        return "";
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return "";
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.ALL_COMPLETION;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

}
