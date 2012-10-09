/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.nashorn.ir.FunctionNode;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public class JsParser extends SanitizingParser {

    public JsParser() {
        super(JsTokenId.javascriptLanguage());
    }

    @Override
    public String getDefaultScriptName() {
        return "javascript.js"; // NOI18N
    }

    @Override
    protected FunctionNode parseSource(Snapshot snapshot, String name, String text, JsErrorManager errorManager) throws Exception {
        String parsableText = text;
        // handle shebang
        if (parsableText.startsWith("#!")) { // NOI18N
            StringBuilder sb = new StringBuilder(parsableText);
            int index = parsableText.indexOf("\n"); // NOI18N
            if (index < 0) {
                index = parsableText.length();
            }

            sb.delete(0, index);
            for (int i = 0; i < index; i++) {
                sb.insert(i, ' ');
            }

            parsableText = sb.toString();
        }

        com.oracle.nashorn.runtime.Source source = new com.oracle.nashorn.runtime.Source(name, parsableText);
        com.oracle.nashorn.runtime.options.Options options = new com.oracle.nashorn.runtime.options.Options("nashorn");
        options.process(new String[] {
            "--parse-only=true", // NOI18N
            "--empty-statements=true", // NOI18N
            "--debug-lines=false"}); // NOI18N

        errorManager.setLimit(0);
        com.oracle.nashorn.runtime.Context contextN = new com.oracle.nashorn.runtime.Context(options, errorManager);
        com.oracle.nashorn.runtime.Context.setContext(contextN);
        com.oracle.nashorn.codegen.Compiler compiler = new com.oracle.nashorn.codegen.Compiler(source, contextN);
        com.oracle.nashorn.parser.Parser parser = new com.oracle.nashorn.parser.Parser(compiler);
        com.oracle.nashorn.ir.FunctionNode node = parser.parse(com.oracle.nashorn.codegen.CompilerConstants.runScriptName);
        return node;
    }
}
