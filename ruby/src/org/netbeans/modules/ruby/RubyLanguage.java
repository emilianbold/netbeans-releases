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
package org.netbeans.modules.ruby;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.ruby.platform.RubyPlatform;
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
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;

/*
 * Language/lexing configuration for Ruby
 *
 * @author Tor Norbye
 */
/*
 * Language/lexing configuration for Ruby
 *
 * @author Tor Norbye
 */
public class RubyLanguage extends DefaultLanguageConfig {
    
    public RubyLanguage() {
    }

    @Override
    public String getLineCommentPrefix() {
        return RubyUtils.getLineCommentPrefix();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return RubyUtils.isIdentifierChar(c);
    }

    @Override
    public Language getLexerLanguage() {
        return RubyTokenId.language();
    }

    @Override
    public String getDisplayName() {
        return "Ruby";
    }

    @Override
    public String getPreferredExtension() {
        return "rb"; // NOI18N
    }

    @Override
    public Map<String,String> getSourceGroupNames() {
        Map<String,String> sourceGroups = new HashMap<String,String>();
        sourceGroups.put("RubyProject", "ruby"); // NOI18N
        sourceGroups.put("WebProject", "ruby"); // NOI18N
        sourceGroups.put("RailsProject", "ruby"); // NOI18N
        
        return sourceGroups;
    }
    
    
    @Override
    public Collection<FileObject> getCoreLibraries() {
        return Collections.singletonList(RubyPlatform.getRubyStubs());
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new RubyCodeCompleter();
    }

    @Override
    public DeclarationFinder getDeclarationFinder() {
        return new RubyDeclarationFinder();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new RubyFormatter();
    }

    @Override
    public InstantRenamer getInstantRenamer() {
        return new RubyRenameHandler();
    }

    @Override
    public KeystrokeHandler getKeystrokeHandler() {
        return new RubyKeystrokeHandler();
    }

    @Override
    public boolean hasOccurrencesFinder() {
        return true;
    }

    @Override
    public OccurrencesFinder getOccurrencesFinder() {
        return new RubyOccurrencesFinder();
    }

    @Override
    public Parser getParser() {
        return new RubyParser();
    }

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return new RubySemanticAnalyzer();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new RubyStructureAnalyzer();
    }

    @Override
    public IndexSearcher getIndexSearcher() {
        return new RubyTypeSearcher();
    }

    @Override
    public EmbeddingIndexerFactory getIndexerFactory() {
        return new RubyIndexer.Factory();
    }

}
