/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.php.editor.indent.PHPBracketCompleter;
import org.netbeans.modules.php.editor.indent.PHPFormatter;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.DeclarationFinderImpl;
import org.netbeans.modules.php.editor.nav.InstantRenamerImpl;
import org.netbeans.modules.php.editor.nav.OccurrencesFinderImpl;
import org.netbeans.modules.php.editor.nav.PHPTypeSearcher;
import org.netbeans.modules.php.editor.parser.GSFPHPParser;
import org.netbeans.modules.php.editor.parser.PhpStructureScanner;
import org.netbeans.modules.php.editor.parser.SemanticAnalysis;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider;
import org.netbeans.modules.php.project.api.PhpSourcePath;

/**
 *
 * @author Petr Pisl
 */
public class PHPLanguage extends DefaultLanguageConfig {

    @Override
    public String getLineCommentPrefix() {
        return "//";    //NOI18N
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isJavaIdentifierPart(c) || (c == '$') ;
    }

    @Override
    public Language getLexerLanguage() {
        return PHPTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "PHP";
    }

    @Override
    public String getPreferredExtension() {
        return "php"; // NOI18N
    }
    
    // Service Registrations

    @Override
    public Parser getParser() {
        return new GSFPHPParser();
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new PHPCodeCompletion();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new SemanticAnalysis();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new PhpStructureScanner();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new DeclarationFinderImpl();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new OccurrencesFinderImpl();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new PHPFormatter();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new PHPBracketCompleter();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new InstantRenamerImpl();
    }

    @Override
    public boolean hasHintsProvider() {
        return true;
    }

    @Override
    public HintsProvider getHintsProvider() {
        return new PHPHintsProvider();
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return new PHPTypeSearcher();
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new PHPIndexer.Factory();
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return Collections.singleton(PhpSourcePath.BOOT_CP);
    }

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.singleton(PhpSourcePath.SOURCE_CP);
    }

}
