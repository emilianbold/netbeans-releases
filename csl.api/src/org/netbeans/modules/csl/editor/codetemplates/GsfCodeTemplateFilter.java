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

package org.netbeans.modules.csl.editor.codetemplates;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.csl.editor.completion.GsfCompletionProvider;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;

/**
 * Code template filter for GSF: Delegates to the plugin to determine which
 * templates are applicable. Based on JavaCodeTemplateFilter.
 * 
 * @author Dusan Balek
 * @author Tor Norbye
 */
public class GsfCodeTemplateFilter extends UserTask implements CodeTemplateFilter {
    
    private int startOffset;
    private int endOffset;
    private Set<String> templates;
    private boolean cancelled;
    
    private GsfCodeTemplateFilter(JTextComponent component, int offset) {
        this.startOffset = offset;
        this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;            
        Source js = Source.create (component.getDocument());
        if (js != null) {
            try {
                Future<Void> f = ParserManager.parseWhenScanFinished(Collections.singleton(js), this);
                if (!f.isDone()) {
                    f.cancel(true);
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean accept(CodeTemplate template) {
        // Selection templates are eligible for "Surround With" should be filtered
        // based on whether the surrounding code makes sense (computed by
        // the language plugins)
        if (templates != null && template != null && template.getParametrizedText().indexOf("${selection") != -1) { // NOI18N
            return templates.contains(template.getAbbreviation()) || (template.getParametrizedText().indexOf("allowSurround") != -1); // NOI18N
        }
    
        // Other templates are filtered for code completion listing etc.
        return true;
    }
    
    public synchronized void cancel() {
        cancelled = true;
    }

    private synchronized boolean isCancelled() {
        return cancelled;
    }

    public void run (ResultIterator resultIterator) throws IOException, ParseException {
        if (isCancelled()) {
            return;
        }
        Parser.Result result = resultIterator.getParserResult (startOffset);
        if(!(result instanceof ParserResult)) {
            return ;
        }
        ParserResult parserResult = (ParserResult) result;
        Snapshot snapshot = parserResult.getSnapshot ();
        CodeCompletionHandler completer = GsfCompletionProvider.getCompletable (snapshot.getSource ().getDocument (true),  startOffset);
            
        if (completer != null && !isCancelled()) {
            templates = completer.getApplicableTemplates(parserResult, startOffset, endOffset);
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new GsfCodeTemplateFilter(component, offset);
        }
    }

}
