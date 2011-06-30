/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.gsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.SyntaxAnalyzer;
import org.netbeans.editor.ext.html.parser.api.SyntaxAnalyzerResult;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.spi.UndeclaredContentResolver;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author marek
 */
public class HtmlGSFParser extends Parser {

    private static class AggregatedUndeclaredContentResolver extends UndeclaredContentResolver {

        private Collection<UndeclaredContentResolver> resolvers;
        
        public AggregatedUndeclaredContentResolver(Collection<UndeclaredContentResolver> resolvers) {
            this.resolvers = resolvers;
        }

        @Override
        public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source) {
            Map<String, List<String>> aggregated = new HashMap<String, List<String>>();
            for(UndeclaredContentResolver resolver : resolvers) {
                aggregated.putAll(resolver.getUndeclaredNamespaces(source));
            }
            return aggregated;
        }
    }

    private HtmlParserResult lastResult;

    // ------------------------------------------------------------------------
    // o.n.m.p.spi.Parser implementation
    // ------------------------------------------------------------------------
    public
    @Override
    void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        lastResult = parse(snapshot, event);
    }

    public
    @Override
    Result getResult(Task task) throws ParseException {
        assert lastResult != null : "getResult() called prior parse()"; //NOI18N
        return lastResult;
    }

    public
    @Override
    void cancel() {
        //todo
    }

    public
    @Override
    void addChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    public
    @Override
    void removeChangeListener(ChangeListener changeListener) {
        // no-op, we don't support state changes
    }

    /** logger for timers/counters */
    private static final Logger TIMERS = Logger.getLogger("TIMER.j2ee.parser"); // NOI18N

    private HtmlParserResult parse(Snapshot snapshot, SourceModificationEvent event) {
        HtmlSource source = new HtmlSource(snapshot);
        
        String sourceMimetype = snapshot.getSource().getMimeType();
        Collection<HtmlExtension> exts = HtmlExtension.getRegisteredExtensions(sourceMimetype);
        Collection<UndeclaredContentResolver> resolvers = new ArrayList<UndeclaredContentResolver>();
        for(HtmlExtension ex : exts) {
            UndeclaredContentResolver resolver = ex.getUndeclaredContentResolver();
            if(resolver != null) {
                resolvers.add(resolver);
            }
        }
        
        SyntaxAnalyzerResult spresult = SyntaxAnalyzer.create(source).analyze(new AggregatedUndeclaredContentResolver(resolvers));        
        HtmlParserResult result = HtmlParserResultAccessor.get().createInstance(spresult);

        if (TIMERS.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "HTML parse result"); // NOI18N
            rec.setParameters(new Object[]{result});
            TIMERS.log(rec);
        }

        return result;
    }


    public static ElementHandle resolveHandle(ParserResult info, ElementHandle oldElementHandle) {
        if (oldElementHandle instanceof HtmlElementHandle) {
           HtmlElementHandle element = (HtmlElementHandle)oldElementHandle;
            AstNode oldNode = element.node();

            AstNode oldRoot = AstNodeUtils.getRoot(oldNode);

            HtmlParserResult newResult = (HtmlParserResult)info;

            AstNode newRoot = newResult.root();

            if (newRoot == null) {
                return null;
            }

            // Find newNode
            AstNode newNode = find(oldRoot, oldNode, newRoot);

            if (newNode != null) {
                return new HtmlElementHandle(newNode, info.getSnapshot().getSource().getFileObject());
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
