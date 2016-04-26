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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.json.api.JsonOptionsQuery;
import org.netbeans.modules.javascript2.json.parser.JsonLexer;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class JsonParser extends SanitizingParser<JsonParserResult> {

    public JsonParser() {
        super(JsTokenId.jsonLanguage());
    }

    @Override
    protected String getDefaultScriptName() {
        return "json.json"; // NOI18N
    }

    @Override
    protected JsonParserResult parseSource(SanitizingParser.Context ctx, JsErrorManager errorManager) throws Exception {
        final Snapshot snapshot = ctx.getSnapshot();
        final String text = ctx.getSource();
        final FileObject fo = snapshot.getSource().getFileObject();
        final boolean allowComments = fo != null && JsonOptionsQuery.getOptions(fo).isCommentSupported();
        final JsonLexer lex = new JsonLexer(new ANTLRInputStream(text), allowComments);
        lex.removeErrorListeners(); //Remove default console log listener
//        lex.addErrorListener(errorManager);
        final CommonTokenStream tokens = new CommonTokenStream(lex);
        org.netbeans.modules.javascript2.json.parser.JsonParser parser =
                new org.netbeans.modules.javascript2.json.parser.JsonParser(tokens);
        parser.removeErrorListeners();  //Remove default console log listener
        parser.addErrorListener(errorManager);
        return new JsonParserResult(
                snapshot,
                parser.json());
    }

    @NonNull
    @Override
    protected JsonParserResult createErrorResult(Snapshot snapshot) {
        return new JsonParserResult(snapshot, null);
    }

    @Override
    protected String getMimeType() {
        return JsTokenId.JSON_MIME_TYPE;
    }

    @Override
    protected Sanitize getSanitizeStrategy() {
        return Sanitize.NEVER;
    }

}
