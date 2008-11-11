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

package org.netbeans.modules.csl.editor.codetemplates;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.CancellableTask;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.Phase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.napi.gsfret.source.SourceUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.csl.editor.completion.GsfCompletionProvider;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
    
    private GsfCodeTemplateFilter(JTextComponent component, int offset) {
        this.startOffset = offset;
        this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;            
        Source js = Source.create (component.getDocument());
        if (js != null) {
            try {
                if (SourceUtils.isScanInProgress()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GsfCodeTemplateFilter.class, "JCT-scanning-in-progress")); //NOI18N
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    ParserManager.parse (Collections.<Source> singleton (js), this);
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
    
    public void cancel() {
    }

    public synchronized void run (ResultIterator resultIterator) throws IOException, ParseException {
        ParserResult parserResult = (ParserResult) resultIterator.getParserResult (startOffset);
        Snapshot snapshot = parserResult.getSnapshot ();
        parserResult.toPhase(Phase.PARSED);
        
        CodeCompletionHandler completer = GsfCompletionProvider.getCompletable (snapshot.getSource ().getDocument (),  startOffset);
            
        if (completer != null) {
            templates = completer.getApplicableTemplates(parserResult, startOffset, endOffset);
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new GsfCodeTemplateFilter(component, offset);
        }
    }
}
