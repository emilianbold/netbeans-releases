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
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.modules.html.editor.HTMLKit;
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

    /** logger for timers/counters */
    private static final Logger TIMERS = Logger.getLogger("TIMER.j2ee.parser"); // NOI18N
    private static final Logger LOGGER = Logger.getLogger(HtmlGSFParser.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public void parseFiles(final Job job) {
        for (final ParserFile file : job.files) {
            try {
                ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
                job.listener.started(beginEvent);

                HtmlParserResult result = null;

                CharSequence buffer = job.reader.read(file);
                if(buffer == null) {
                    //likely invalid state, the source shouldn't be null I guess
                    LOGGER.info("Job.reader.read(file) returned null for file " + file.getFile().getAbsolutePath());
                    buffer = ""; //recover
                }
                List<SyntaxElement> elements = SyntaxParser.parseImmutableSource(buffer);

                if (LOG) {
                    for (SyntaxElement element : elements) {
                        LOGGER.log(Level.FINE, element.toString());
                    }
                }

                result = new HtmlParserResult(this, file, elements);

                if (TIMERS.isLoggable(Level.FINE)) {
                    LogRecord rec = new LogRecord(Level.FINE, "HTML parse result"); // NOI18N
                    rec.setParameters(new Object[] { result });
                    TIMERS.log(rec);
                }

                //highlight unpaired tags
                final DTD dtd = result.dtd();
                AstNodeUtils.visitChildren(result.root(),
                        new AstNodeVisitor() {

                            public void visit(AstNode node) {
                                if (node.type() == AstNode.NodeType.UNMATCHED_TAG) {
                                    AstNode unmatched = node.children().get(0);
                                    if (dtd != null) {
                                        //check the unmatched tag according to the DTD
                                        Element element = dtd.getElement(node.name().toUpperCase());
                                        if (element != null) {
                                            if (unmatched.type() == AstNode.NodeType.OPEN_TAG && element.hasOptionalEnd() || unmatched.type() == AstNode.NodeType.ENDTAG && element.hasOptionalStart()) {
                                                return;
                                            }
                                        }
                                    }

                                    Error error =
                                            new DefaultError("unmatched_tag", NbBundle.getMessage(this.getClass(), "MSG_Unmatched_Tag"), null, file.getFileObject(),
                                            node.startOffset(), node.endOffset(), Severity.WARNING); //NOI18N
                                    job.listener.error(error);

                                }
                            }
                        });

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

    public OffsetRange getOffsetRange(CompilationInfo info, ElementHandle handle) {
      ElementHandle object = resolveHandle(info, handle);
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
    
    public static ElementHandle resolveHandle(CompilationInfo info, ElementHandle oldElementHandle) {
        if (oldElementHandle instanceof HtmlElementHandle) {
           HtmlElementHandle element = (HtmlElementHandle)oldElementHandle;
            AstNode oldNode = element.node(); 

            AstNode oldRoot = AstNodeUtils.getRoot(oldNode);
            
            HtmlParserResult newResult = (HtmlParserResult)info.getEmbeddedResult(HTMLKit.HTML_MIME_TYPE, 0);
            
            AstNode newRoot = newResult.root();
            
            if (newRoot == null) {
                return null;
            }

            // Find newNode
            AstNode newNode = find(oldRoot, oldNode, newRoot);

            if (newNode != null) {
                return new HtmlElementHandle(newNode, info.getFileObject());
            }
        }
        
        return null;
    }

    private static AstNode find(AstNode oldRoot, AstNode oldObject, AstNode newRoot) {
        // Walk down the tree to locate oldObject, and in the process, pick the same child for newRoot
        if (oldRoot == oldObject) {
            // Found it!
            return newRoot;
        }

        List<AstNode> oChildren = oldRoot.children();
        List<AstNode> nChildren = newRoot.children();
        
        for(int i = 0; i < oChildren.size(); i++) {
            
            AstNode oCh = oChildren.get(i);
            
            if(i == nChildren.size()) {
                //no more new children
                return null;
            }
            AstNode nCh = nChildren.get(i);

            if (oCh == oldObject) {
                // Found it!
                return nCh;
            }

            // Recurse
            AstNode match = find(oCh, oldObject, nCh);

            if (match != null) {
                return match;
            }
            
        }

        return null;
    }
    
    
    
    
    
    
    
    
    
    
    
}
