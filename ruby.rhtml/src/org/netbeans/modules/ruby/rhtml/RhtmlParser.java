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
package org.netbeans.modules.ruby.rhtml;

import org.netbeans.api.gsf.OccurrencesFinder;
import org.netbeans.api.gsf.ParserResult.AstTreeNode;
import org.netbeans.api.gsf.SemanticAnalyzer;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.RubyParser;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.parser.RubyParserResult;

import org.netbeans.api.gsf.ParseEvent;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.PositionManager;
import org.netbeans.api.gsf.Severity;
import org.netbeans.api.gsf.SourceFileReader;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.RubyParseResult;
import org.netbeans.modules.ruby.RubyPositionManager;
import org.netbeans.modules.ruby.elements.AstRootElement;
import org.netbeans.modules.ruby.rhtml.editor.completion.RhtmlModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * RhtmlParser: builds up a Ruby
 * 
 * @author Tor Norbye
 */
public class RhtmlParser extends RubyParser {
    private RhtmlModel model;
    
    /**
     * Creates a new instance of RubyParser
     */
    public RhtmlParser() {
    }

    /** Parse the given set of files, and notify the parse listener for each transition
     * (compilation results are attached to the events )
     */
    @Override
    public void parseFiles(List<ParserFile> files, ParseListener listener, SourceFileReader reader) {
        for (ParserFile file : files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            listener.started(beginEvent);
            
            RubyParseResult result = null;

            //CharSequence buffer = reader.read(file);
            FileObject fo = file.getFileObject();
            assert fo != null;

            BaseDocument doc = NbUtilities.getDocument(fo, true);
            if (doc != null) {
                //String source = asString(buffer);
                model = RhtmlModel.get(doc);
                String rubySource = model.getRubyCode();

                int caretOffset = reader.getCaretOffset(file);
                if (caretOffset != -1) {
                    caretOffset = model.sourceToGeneratedPos(caretOffset);
                }
                Context context = new Context(file, listener, rubySource, caretOffset);
                result = parseBuffer(context, Sanitize.NONE);
                model = null;
            } else {
                Logger.getLogger(RhtmlParser.class.getName()).warning("Attempted to parse non-open RHTML file " + FileUtil.getFileDisplayName(fo));
            }

            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            listener.finished(doneEvent);
        }
    }

    @Override
    protected void notifyError(Context context, String key,
        Severity severity, String description, String details, int offset, Sanitize sanitizing) {
        assert model != null;
        if (offset != -1) {
            offset = model.generatedToSourcePos(offset);
        }
        super.notifyError(context, key, severity, description, details, offset, sanitizing);
    }

    @Override
    protected PositionManager createPositionManager() {
        return new RhtmlPositionManager();
    }

    @Override
    protected RubyParseResult createParseResult(ParserFile file, AstRootElement rootElement, AstTreeNode ast, Node root,
        RootNode realRoot, RubyParserResult jrubyResult) {
        return new RhtmlParseResult(file, rootElement, ast, root, realRoot, jrubyResult, model);
    }

    @Override
    public SemanticAnalyzer getSemanticAnalysisTask() {
        // Highlights aren't working in RHTML so don't bother computing them
        return null;
    }

    @Override
    public OccurrencesFinder getMarkOccurrencesTask(int caretPosition) {
        // Highlights aren't working in RHTML so don't bother computing them
        return null;
    }
    
    private class RhtmlPositionManager extends RubyPositionManager {
        @Override
        public int getAstOffset(ParserResult result, int lexicalOffset) {
            RhtmlModel model = ((RhtmlParseResult)result).model;
            return model.sourceToGeneratedPos(lexicalOffset);
        }

        @Override
        public int getLexicalOffset(ParserResult result, int astOffset) {
            RhtmlModel model = ((RhtmlParseResult)result).model;
            return model.generatedToSourcePos(astOffset);
        }

        @Override
        public boolean isTranslatingSource() {
            return true;
        }
    }
    
    private class RhtmlParseResult extends RubyParseResult {
        private RhtmlModel model;

        RhtmlParseResult(ParserFile file, AstRootElement rootElement, AstTreeNode ast, Node root,
            RootNode realRoot, RubyParserResult jrubyResult, RhtmlModel model) {
            super(file, rootElement, ast, root, realRoot, jrubyResult);
            this.model = model;
        }
    }
}
