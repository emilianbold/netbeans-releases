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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spellchecker.bindings.ruby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spellchecker.spi.language.TokenList;

/**
 *
 * @author Tor Norbye
 */
public abstract class TokenListTestBase extends NbTestCase {
    private Language language;
    private String mimeType;

    public TokenListTestBase(String testName, Language language, String mimeType) {
        super(testName);
        this.language = language;
        this.mimeType = mimeType;
    }

    protected void tokenListTest(String documentContent, String... golden) throws Exception {
        BaseDocument doc = new BaseDocument(null, false);

        doc.putProperty(Language.class, language);
        doc.putProperty("mimeType", mimeType);

        doc.insertString(0, documentContent, null);

        List<String> words = new ArrayList<String>();

        //TokenList l = new RubyTokenList(doc);
        TokenList l = new RubyTokenListProvider().findTokenList(doc);
        assertNotNull(l);

        l.setStartOffset(0);

        while (l.nextWord()) {
            words.add(l.getCurrentWordText().toString());
        }

        assertEquals(Arrays.asList(golden), words);
    }

    protected void tokenListTestWithWriting(String documentContent, int offset, String text, int startOffset, String... golden) throws Exception {
        BaseDocument doc = new BaseDocument(null, false);

        doc.putProperty(Language.class, language);
        doc.putProperty("mimeType", mimeType);

        doc.insertString(0, documentContent, null);

        List<String> words = new ArrayList<String>();
        TokenList l = new RubyTokenList(doc);

        while (l.nextWord()) {
        }

        doc.insertString(offset, text, null);

        l.setStartOffset(startOffset);

        while (l.nextWord()) {
            words.add(l.getCurrentWordText().toString());
        }

        assertEquals(Arrays.asList(golden), words);
    }
}