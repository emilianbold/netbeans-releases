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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.csl.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.api.CancellableTask;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import java.util.EnumMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.ParserResult;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.text.NbDocument;



/**
 * This class is based on JavaHintsFactory in Retouche's org.netbeans.modules.java.hints
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * 
 * @author Jan Lahoda
 * @author leon chiver
 * @author Tor Norbye
 */
public final class GsfHintsProvider implements CancellableTask<CompilationInfo> {
    
    public static ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.gsfret.hints"); // NOI18N
    public static Logger LOG = Logger.getLogger("org.netbeans.modules.gsfret.hints"); // NOI18N
    
    private FileObject file;
    
    /**
     * Creates a new instance of GsfHintsProvider
     */
    GsfHintsProvider(FileObject file) {
        this.file = file;
    }
    
    private static final Map<org.netbeans.modules.csl.api.Severity, Severity> errorKind2Severity;
    
    static {
        errorKind2Severity = new EnumMap<org.netbeans.modules.csl.api.Severity, Severity>(org.netbeans.modules.csl.api.Severity.class);
        errorKind2Severity.put(org.netbeans.modules.csl.api.Severity.ERROR, Severity.ERROR);
        errorKind2Severity.put(org.netbeans.modules.csl.api.Severity.WARNING, Severity.WARNING);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.WARNING, Severity.WARNING);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.NOTE, Severity.WARNING);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.OTHER, Severity.WARNING);
    }
    
    List<ErrorDescription> computeErrors(CompilationInfo info, Document doc, ParserResult result, List<Error> errors, List<ErrorDescription> descs) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "errors = " + errors);
        }
        
        for (Error/*Diagnostic*/ d : errors) {
            if (isCanceled()) {
                return null;
            }
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "d = " + d);

                //Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors();
            }
            
            //Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors();
            
            //List<ErrorRule> rules = code2Rules.get(d.getKey());
            
            //if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                //ERR.log(ErrorManager.INFORMATIONAL, "code= " + d.getKey());
                //ERR.log(ErrorManager.INFORMATIONAL, "rules = " + rules);
            //}
            
            //int position = (int)d.getPosition();
            int astOffset = d.getStartPosition();
            int astEndOffset = d.getEndPosition();
            
            int position, endPosition;
            if (result.getTranslatedSource() != null) {
                position = result.getTranslatedSource().getLexicalOffset(astOffset);
                if (position == -1) {
                    continue;
                }
                endPosition = position+(astEndOffset-astOffset);
            } else {
                position = astOffset;
                endPosition = astEndOffset;
            }
            
            LazyFixList ehm;
            
            //if (rules != null) {
            //    ehm = new CreatorBasedLazyFixList(info.getFileObject(), d.getKey(), (int)getPrefferedPosition(info, d), rules, data);
            //} else {
                ehm = ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList());
            //}
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "ehm=" + ehm);
            }
            
            final String desc = d.getDisplayName();
            final Position[] range = getLine(info, d, doc, position, endPosition);
            
            if (isCanceled()) {
                return null;
            }
            
            if (range[0] == null || range[1] == null) {
                continue;
            }
            
            descs.add(ErrorDescriptionFactory.createErrorDescription(errorKind2Severity.get(d.getSeverity()), desc, ehm, doc, range[0], range[1]));
        }
        
        if (isCanceled()) {
            return null;
        }
        
        return descs;
    }
    
    public Document getDocument() {
        return DataLoadersBridge.getDefault().getDocument(file);
    }
    
    private Position[] getLine(CompilationInfo info, Error d, final Document doc, int startOffset, int endOffset) {
        StyledDocument sdoc = (StyledDocument) doc;
        int lineNumber = NbDocument.findLineNumber(sdoc, startOffset);
        int lineOffset = NbDocument.findLineOffset(sdoc, lineNumber);
        String text = DataLoadersBridge.getDefault().getLine(doc, lineNumber);
        if (text == null) {
            return new Position[2];
        }
        
        boolean rangePrepared = false;
        
        if (!rangePrepared) {
            int column = 0;
            int length = text.length();
            
            while (column < text.length() && Character.isWhitespace(text.charAt(column))) {
                column++;
            }
            
            while (length > 0 && Character.isWhitespace(text.charAt(length - 1))) {
                length--;
            }
            
            startOffset = lineOffset + column;
            endOffset = lineOffset + length;
            if (startOffset > endOffset) {
                // Space only on the line
                startOffset = lineOffset;
            }
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "startOffset = " + startOffset );
            ERR.log(ErrorManager.INFORMATIONAL, "endOffset = " + endOffset );
        }
        
        final int startOffsetFinal = startOffset;
        final int endOffsetFinal = endOffset;
        final Position[] result = new Position[2];
        
        doc.render(new Runnable() {
            public void run() {
                if (isCanceled()) {
                    return;
                }
                
                int len = doc.getLength();
                
                if (startOffsetFinal > len || endOffsetFinal > len) {
                    if (!isCanceled() && ERR.isLoggable(ErrorManager.WARNING)) {
                        ERR.log(ErrorManager.WARNING, "document changed, but not canceled?" );
                        ERR.log(ErrorManager.WARNING, "len = " + len );
                        ERR.log(ErrorManager.WARNING, "startOffset = " + startOffsetFinal );
                        ERR.log(ErrorManager.WARNING, "endOffset = " + endOffsetFinal );
                    }
                    cancel();
                    
                    return;
                }
                
                try {
                    result[0] = NbDocument.createPosition(doc, startOffsetFinal, Bias.Forward);
                    result[1] = NbDocument.createPosition(doc, endOffsetFinal, Bias.Backward);
                } catch (BadLocationException e) {
                    ERR.notify(ErrorManager.ERROR, e);
                }
            }
        });
        
        return result;
    }
    
    private boolean cancel;
    
    synchronized boolean isCanceled() {
        return cancel;
    }
    
    public synchronized void cancel() {
        cancel = true;
    }
    
    synchronized void resume() {
        cancel = false;
    }
    
    public void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();
        
        if (doc == null) {
            Logger.getLogger(GsfHintsProvider.class.getName()).log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return ;
        }
        
        long start = System.currentTimeMillis();
        
        

        Set<String> mimeTypes = info.getEmbeddedMimeTypes();
        LanguageRegistry registry = LanguageRegistry.getInstance();
        List<ErrorDescription> descriptions = new ArrayList<ErrorDescription>();
        
        for (String mimeType : mimeTypes) {
            Language language = registry.getLanguageByMimeType(mimeType);
            HintsProvider provider = language.getHintsProvider();
            GsfHintsManager manager = null;
            RuleContext ruleContext = null;
            if (provider != null) {
                manager = language.getHintsManager();
                if (manager == null) {
                    continue;
                }
                ruleContext = manager.createRuleContext(info, language, -1, -1, -1);
                if (ruleContext == null) {
                    continue;
                }
            }

            for (ParserResult result : info.getEmbeddedResults(mimeType)) {
                assert result != null;
                
                List<Error> errors = result.getDiagnostics();
                List<ErrorDescription> desc = new ArrayList<ErrorDescription>();
                if (provider != null) {
                    assert ruleContext != null;
                    ruleContext.parserResult = result;
                    List<Error> unhandled = new ArrayList<Error>();
                    List<Hint> hints = new ArrayList<Hint>();
                    provider.computeErrors(manager, ruleContext, hints, unhandled);
                    errors = unhandled;
                    boolean allowDisableEmpty = true;
                    for (Hint hint : hints) {
                        ErrorDescription errorDesc = manager.createDescription(hint, ruleContext, allowDisableEmpty);
                        descriptions.add(errorDesc);
                    }
                }
                // Process errors without codes
                desc = computeErrors(info, doc, result, errors, desc);
                if (desc == null) {
                    //meaning: cancelled
                    return;
                }
                
                if (isCanceled()) {
                    return;
                }

                descriptions.addAll(desc);
            }
        }
        HintsController.setErrors(doc, "csl-hints", descriptions);
        
        long end = System.currentTimeMillis();
        
        //TimesCollector.getDefault().reportTime(info.getFileObject(), "com-hints", "Hints", end - start);
    }
}

