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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.editor.api.parser;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.groovy.editor.api.BracketCompleter;
import org.netbeans.modules.groovy.editor.api.GroovyDeclarationFinder;
import org.netbeans.modules.groovy.editor.api.GroovyIndexer;
import org.netbeans.modules.groovy.editor.api.GroovyInstantRenamer;
import org.netbeans.modules.groovy.editor.api.GroovyTypeSearcher;
import org.netbeans.modules.groovy.editor.api.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.StructureAnalyzer;
import org.netbeans.modules.groovy.editor.api.completion.CompletionHandler;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;

/**
 * Language/lexing configuration for Groovy
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
@LanguageRegistration(mimeType="text/x-groovy")
@PathRecognizerRegistration(mimeTypes="text/x-groovy", sourcePathIds=ClassPath.SOURCE, libraryPathIds={}, binaryLibraryPathIds={}) //NOI18N
public class GroovyLanguage extends DefaultLanguageConfig {
    
    public GroovyLanguage() {
    }

    @Override
    public String getLineCommentPrefix() {
        return GroovyUtils.getLineCommentPrefix();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return GroovyUtils.isIdentifierChar(c);
    }

    @Override
    public Language getLexerLanguage() {
        return GroovyTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "Groovy";
    }

    @Override
    public String getPreferredExtension() {
        return "groovy"; // NOI18N
    }

    // Service Registrations
    
    @Override
    public Parser getParser() {
        return new GroovyParser();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new org.netbeans.modules.groovy.editor.api.Formatter();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new BracketCompleter();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new CompletionHandler();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new GroovySemanticAnalyzer();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new GroovyOccurrencesFinder();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new StructureAnalyzer();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

//    @Override
//    public HintsProvider getHintsProvider() {
//        return new GroovyHintsProvider();
//    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new GroovyDeclarationFinder();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new GroovyInstantRenamer();
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return new GroovyTypeSearcher();
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new GroovyIndexer.Factory();
    }

    @Override
    public Set<String> getSourcePathIds() {
        // We don't have our own source path id, because javascript files can be
        // anywhere in a project. So, for index search we will use all available
        // sourcepath ids.
        // FIXME parsing API
        return Collections.singleton(ClassPath.SOURCE);
    }
}
