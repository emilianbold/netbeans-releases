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
package org.netbeans.modules.html.editor.gsf;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.spi.DefaultError;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
public class HtmlGSFParser implements Parser, PositionManager {

    private static final Logger LOGGER = Logger.getLogger(HtmlGSFParser.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public void parseFiles(final Job job) {
        for (final ParserFile file : job.files) {
            try {
                ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
                job.listener.started(beginEvent);

                HtmlParserResult result = null;

                CharSequence buffer = job.reader.read(file);
                int caretOffset = job.reader.getCaretOffset(file);

                SyntaxParser parser = SyntaxParser.create(buffer);
                List<SyntaxElement> elements = parser.parseImmutableSource();

                if (LOG) {
                    for (SyntaxElement element : elements) {
                        LOGGER.log(Level.FINE, element.toString());
                    }
                }

                result = new HtmlParserResult(this, file, elements);

                //highlight unpaired tags
                AstNode root = result.root();
                AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                    public void visit(AstNode node) {
                        if (node.type() == AstNode.NodeType.UNMATCHED_TAG) {
                            Error error =
                                    new DefaultError("unmatched_tag", NbBundle.getMessage(this.getClass(), "MSG_Unmatched_Tag"), null, file.getFileObject(),
                                    node.startOffset(), node.endOffset(), Severity.WARNING); //NOI18N
                            job.listener.error(error);
                        
                        }
                    }
                });
                
                
                
                // co jsme udelal ve vlaku:
                // 1. oprava html navigatoru - ted uz se po modifikacich nerefreshuje
                // 2. implementace jednoducheho error checkingu na unmatched tags
                
                //zadat Tomasovi bugy na parse tree - napr. 4 urovne zahloubeni nebo 
                //unmatched trags fakt moc nefungujou :-(
                
                
                

                ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
                job.listener.finished(doneEvent);
            } catch (IOException ex) {
                job.listener.exception(ex);
                Exceptions.printStackTrace(ex);
            }

        }
    }

    public PositionManager getPositionManager() {
        return this;
    }

    public OffsetRange getOffsetRange(CompilationInfo info, ElementHandle object) {
        if (object instanceof HtmlElementHandle) {
            ParserResult presult = info.getEmbeddedResults(HTMLKit.HTML_MIME_TYPE).iterator().next();
            final TranslatedSource source = presult.getTranslatedSource();
            AstNode node = ((HtmlElementHandle) object).node();
            return new OffsetRange(AstUtils.documentPosition(node.startOffset(), source), AstUtils.documentPosition(node.endOffset(), source));

        } else {
            throw new IllegalArgumentException((("Foreign element: " + object + " of type " +
                    object) != null) ? object.getClass().getName() : "null"); //NOI18N
        }
    }
}
