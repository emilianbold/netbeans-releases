/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.nbparser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.lib.ExtCss3Lexer;
import org.netbeans.modules.css.lib.ExtCss3Parser;
import org.netbeans.modules.css.lib.api.CssParserResult;
import javax.swing.event.ChangeListener;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.css.lib.AbstractParseTreeNode;
import org.netbeans.modules.css.lib.NbParseTreeBuilder;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.CharSequences;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssParser extends Parser {

    private static final CharSequence TEMPLATING_MARK = "@@@"; //NOI18N
    private static final Logger LOG = Logger.getLogger(CssParser.class.getSimpleName());
    
    private boolean cancelled;
    private final String topLevelSnapshotMimetype;

    //cache
    private Snapshot snapshot;
    private AbstractParseTreeNode tree;
    private List<ProblemDescription> problems;

    public CssParser() {
        topLevelSnapshotMimetype = null;
    }
    
    /* test */ public CssParser(String topLevelSnapshotMimetype) {
        this.topLevelSnapshotMimetype = topLevelSnapshotMimetype;
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        if (snapshot == null) {
            return;
        }
        if (cancelled) {
            return ;
        }
        
        this.snapshot = snapshot;
        FileObject fo = snapshot.getSource().getFileObject();
        String fileName = fo == null ? "no file" : fo.getPath(); //NOI18N
        String mimeType = topLevelSnapshotMimetype != null ? topLevelSnapshotMimetype : (fo == null ? null : fo.getMIMEType());
        LOG.log(Level.FINE, "Parsing {0} ", fileName); //NOI18N
        long start = System.currentTimeMillis();
        try {
            CharSequence source = snapshot.getText();
            ExtCss3Lexer lexer = new ExtCss3Lexer(source);
            TokenStream tokenstream = new CommonTokenStream(lexer);
            NbParseTreeBuilder builder = new NbParseTreeBuilder(source);
            ExtCss3Parser parser = new ExtCss3Parser(tokenstream, builder, mimeType);
            
            if(cancelled) {
                return ;
            }
            parser.styleSheet();

            if(cancelled) {
                return ;
            }
            
            AbstractParseTreeNode tree_local = builder.getTree();
            List<ProblemDescription> problems_local = new ArrayList<>();
            //add lexer issues
            problems_local.addAll(lexer.getProblems());
            //add parser issues
            problems_local.addAll(builder.getProblems());

            filterProblemsInVirtualCode(snapshot, problems_local);
            filterTemplatingProblems(snapshot, problems_local);

            if(cancelled) {
                return ;
            }
            
            this.tree = tree_local;
            this.problems = problems_local;
            
        } catch (RecognitionException ex) {
            throw new ParseException(String.format("Error parsing %s snapshot.", snapshot), ex); //NOI18N
        } finally {
            long end = System.currentTimeMillis();
            LOG.log(Level.FINE, "Parsing of {0} took {1} ms.", new Object[]{fileName, (end - start)}); //NOI18N
        }

    }

    @Override
    public CssParserResult getResult(Task task) throws ParseException {
        return cancelled || (tree == null) ? null : new CssParserResult(snapshot, tree, problems);
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        if (CancelReason.SOURCE_MODIFICATION_EVENT == reason) {
            cancelled = true;
            tree = null;
            problems = null;
            snapshot = null;
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        //no-op
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        //no-op
    }
   
    private static void filterProblemsInVirtualCode(Snapshot snapshot, List<ProblemDescription> problems) {
        ListIterator<ProblemDescription> listIterator = problems.listIterator();
        while (listIterator.hasNext()) {
            ProblemDescription p = listIterator.next();
            int from = p.getFrom();
            int to = p.getTo();
            if (snapshot.getOriginalOffset(from) == -1 || snapshot.getOriginalOffset(to) == -1) {
                listIterator.remove();
            }
        }
    }

    //filtering out problems caused by templating languages
    private static void filterTemplatingProblems(Snapshot snapshot, List<ProblemDescription> problems) {
        MimePath mimePath = snapshot.getMimePath();
        CharSequence text = snapshot.getText();
        if (mimePath.size() <= 2 || mimePath.size() == 3 && mimePath.getMimeType(0).equals("text/xhtml")) { //NOI18N
            //text/css
            //or
            //text/html/text/css
            //or
            //hack for the fake text/xhtml language:
            //for .xhtml files the mime is text/xhtml/text/html/text/css
        } else {
            //typically text/php/text/html/text/css
            ListIterator<ProblemDescription> listIterator = problems.listIterator();
            while (listIterator.hasNext()) {
                ProblemDescription p = listIterator.next();
                //XXX Idealy the filtering context should be dependent on the enclosing node
                //sg. like if there's a templating error in an declaration - search the whole
                //declaration for the templating mark. 
                //
                //Using some simplification - line context, though some nodes may span multiple
                //lines and the templating mark may not necessarily be at the line with the error.
                //
                //so find line bounds...

                //the "premature end of file" error has position pointing after the last char (=text.length())!
                if (p.getFrom() == text.length()) {
                    listIterator.remove(); //consider this as hidden error
                    continue;
                }

                int from, to;
                for (from = p.getFrom(); from > 0; from--) {
                    char c = text.charAt(from);
                    if (c == '\n') {
                        break;
                    }
                }
                for (to = p.getTo(); to < text.length(); to++) {
                    char c = text.charAt(to);
                    if (c == '\n') {
                        break;
                    }
                }
                //check if there's the templating mark (@@@) in the context
                CharSequence img = snapshot.getText().subSequence(from, to);
                if (CharSequences.indexOf(img, TEMPLATING_MARK) != -1) {
                    listIterator.remove();
                }
            }
        }
    }
    
}
