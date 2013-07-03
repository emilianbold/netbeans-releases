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

package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import javax.tools.Diagnostic;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.jdk.ConvertToDiamondBulkHint;
import org.netbeans.modules.java.hints.jdk.ConvertToLambda;
import org.netbeans.modules.java.hints.legacy.spi.RulesManager;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;



/**
 * @author Jan Lahoda
 * @author leon chiver
 */
public final class ErrorHintsProvider extends JavaParserResultTask {
    
    public static ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.java.hints"); // NOI18N
    public static Logger LOG = Logger.getLogger("org.netbeans.modules.java.hints"); // NOI18N
    
    ErrorHintsProvider() {
        super(Phase.RESOLVED);
    }
    
    private static final Map<Diagnostic.Kind, Severity> errorKind2Severity;
    
    static {
        errorKind2Severity = new EnumMap<Diagnostic.Kind, Severity>(Diagnostic.Kind.class);
        errorKind2Severity.put(Diagnostic.Kind.ERROR, Severity.ERROR);
        errorKind2Severity.put(Diagnostic.Kind.MANDATORY_WARNING, Severity.WARNING);
        errorKind2Severity.put(Diagnostic.Kind.WARNING, Severity.WARNING);
        errorKind2Severity.put(Diagnostic.Kind.NOTE, Severity.WARNING);
        errorKind2Severity.put(Diagnostic.Kind.OTHER, Severity.WARNING);
    }

    /**
     * @return errors for whole file
     */
    List<ErrorDescription> computeErrors(CompilationInfo info, Document doc, String mimeType) throws IOException {
        return computeErrors(info, doc, null, mimeType);
    }
    
    /**
     * @param forPosition position for ehich errors would be computed
     * @return errors for line specified by forPosition
     * @throws IOException
     */
    List<ErrorDescription> computeErrors(CompilationInfo info, Document doc, Integer forPosition, String mimeType) throws IOException {
        if ("text/x-javahints".equals(mimeType)) {
            if (info.getText().startsWith("//no-errors")) return Collections.emptyList();
        }

        List<Diagnostic> errors = info.getDiagnostics();
        List<ErrorDescription> descs = new ArrayList<ErrorDescription>();
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "errors = " + errors );

        boolean isJava = org.netbeans.modules.java.hints.errors.Utilities.JAVA_MIME_TYPE.equals(mimeType);

        Map<Class, Data> data = new HashMap<Class, Data>();

        for (Diagnostic d : errors) {
            if (ConvertToDiamondBulkHint.CODES.contains(d.getCode())) {
                if (isJava) continue; //handled separatelly in the hint
                if (!ConvertToDiamondBulkHint.isHintEnabled()) continue; //disabled
            }
            
            if (ConvertToLambda.CODES.contains(d.getCode())) {
                continue;
            }
            
            if (isCanceled())
                return null;

            if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
                ERR.log(ErrorManager.INFORMATIONAL, "d = " + d );
            
            Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors(mimeType);
            
            List<ErrorRule> rules = code2Rules.get(d.getCode());
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "code= " + d.getCode());
                ERR.log(ErrorManager.INFORMATIONAL, "rules = " + rules);
            }
            
            LazyFixList ehm;

            if (rules != null) {
                int pos = (int)getPrefferedPosition(info, d);
                
                ehm = new CreatorBasedLazyFixList(info.getFileObject(), d.getCode(), pos, rules, data);
            } else {
                ehm = ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList());
            }
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
                ERR.log(ErrorManager.INFORMATIONAL, "ehm=" + ehm);
            
            final String desc = d.getMessage(null);
            final Position[] range = getLine(info, d, doc, (int)d.getStartPosition(), (int)d.getEndPosition());

            if (isCanceled())
                return null;
            
            if (range[0] == null || range[1] == null)
                continue;

            if (forPosition != null) {
                try {
                    int posRowStart = org.netbeans.editor.Utilities.getRowStart((NbEditorDocument) doc, forPosition);
                    int errRowStart = org.netbeans.editor.Utilities.getRowStart((NbEditorDocument) doc, range[0].getOffset());
                    if (posRowStart != errRowStart) {
                        continue;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            descs.add(ErrorDescriptionFactory.createErrorDescription(errorKind2Severity.get(d.getKind()), desc, ehm, doc, range[0], range[1]));
        }
        
        if (isCanceled())
            return null;

        Set<Severity> disabled = org.netbeans.modules.java.hints.spiimpl.Utilities.disableErrors(info.getFileObject());
        List<ErrorDescription> result = new ArrayList<ErrorDescription>(descs.size());

        for (ErrorDescription ed : descs) {
            if (!disabled.contains(ed.getSeverity())) {
                result.add(ed);
            }
        }

        if (isJava) {
            LazyHintComputationFactory.getAndClearToCompute(info.getFileObject());
        } else {
            for (ErrorDescription d : result) {
                d.getFixes().getFixes();
            }
        }
        
        return result;
    }
    
    public static Token findUnresolvedElementToken(CompilationInfo info, int offset) throws IOException {
        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
        
        if (ts == null) {
            return null;
        }
        
        ts.move(offset);
        if (ts.moveNext()) {
            Token t = ts.token();

            if (t.id() == JavaTokenId.DOT) {
                ts.moveNext();
                t = ts.token();
            } else {
                if (t.id() == JavaTokenId.LT) {
                    ts.moveNext();
                    t = ts.token();
                } else {
                    if (t.id() == JavaTokenId.NEW || t.id() == JavaTokenId.WHITESPACE) {
                        t = skipWhitespaces(ts);

                        if (t == null) return null;
                    } else if (t.id() == JavaTokenId.IMPORT) {
                        t = skipWhitespaces(ts);

                        if (t == null) return null;
                    }
                }
            }

            while (t.id() == JavaTokenId.WHITESPACE) {
                ts.moveNext();
                t = ts.token();
            }
            
            if (t.id() == JavaTokenId.IDENTIFIER) {
                return ts.offsetToken();
            }
        }
        return null;
    }

    private static Token skipWhitespaces(TokenSequence<JavaTokenId> ts) {
        boolean cont = ts.moveNext();

        while (cont && ts.token().id() == JavaTokenId.WHITESPACE) {
            cont = ts.moveNext();
        }

        if (!cont) {
            return null;
        }

        return ts.token();
    }
    
    private static int[] findUnresolvedElementSpan(CompilationInfo info, int offset) throws IOException {
        Token t = findUnresolvedElementToken(info, offset);
        
        if (t != null) {
            return new int[] {
                t.offset(null),
                t.offset(null) + t.length()
            };
        }
        
        return null;
    }
    
    public static TreePath findUnresolvedElement(CompilationInfo info, int offset) throws IOException {
        int[] span = findUnresolvedElementSpan(info, offset);
        
        if (span != null) {
            return info.getTreeUtilities().pathFor(span[0] + 1);
        } else {
            return null;
        }
    }
    
    private static final Set<String> INVALID_METHOD_INVOCATION = new HashSet<String>(Arrays.asList(
        "compiler.err.prob.found.req",
        "compiler.err.cant.apply.symbol",
        "compiler.err.cant.apply.symbol.1",
//        "compiler.err.cant.resolve.location",
        "compiler.err.cant.resolve.location.args"
    ));
    
    private static final Set<String> CANNOT_RESOLVE = new HashSet<String>(Arrays.asList(
            "compiler.err.cant.resolve",
            "compiler.err.cant.resolve.location",
            "compiler.err.cant.resolve.location.args",
            "compiler.err.doesnt.exist",
            "compiler.err.type.error"
    ));
    
    private static final Set<String> UNDERLINE_IDENTIFIER = new HashSet<String>(Arrays.asList(
            "compiler.err.local.var.accessed.from.icls.needs.final",
            "compiler.err.var.might.not.have.been.initialized",
            "compiler.err.report.access",
            "compiler.err.does.not.override.abstract",
            "compiler.err.abstract.cant.be.instantiated",
            "compiler.warn.missing.SVUID",
            "compiler.warn.has.been.deprecated",
            "compiler.warn.raw.class.use",
            "compiler.err.class.public.should.be.in.file"
    ));
    
    private static final Set<String> USE_PROVIDED_SPAN = new HashSet<String>(Arrays.asList(
            "compiler.err.method.does.not.override.superclass",
            "compiler.err.illegal.unicode.esc"
    ));

    private static final Set<JavaTokenId> WHITESPACE = EnumSet.of(JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.WHITESPACE);
    
    private int[] handlePossibleMethodInvocation(CompilationInfo info, Diagnostic d, final Document doc, int startOffset, int endOffset) throws IOException {
        int pos = (int) getPrefferedPosition(info, d);
        TreePath tp = info.getTreeUtilities().pathFor(pos + 1);
        
        if (tp != null && tp.getParentPath() != null && tp.getParentPath().getLeaf() != null && (tp.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION || tp.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS)) {
            int[] index = new int[1];
            
            tp = tp.getParentPath();
            
            if (!Utilities.fuzzyResolveMethodInvocation(info, tp, new ArrayList<TypeMirror>(), index).isEmpty()) {
                Tree a;
                
                if (tp.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
                    MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
                    
                    a = mit.getArguments().get(index[0]);
                } else {
                    NewClassTree mit = (NewClassTree) tp.getLeaf();
                    
                    a = mit.getArguments().get(index[0]);
                }

                int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), a);
                int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), a);
            
                return new int[] {start, end};
            }
        }
        
        return null;
    }
    
    private Position[] getLine(CompilationInfo info, Diagnostic d, final Document doc, int startOffset, int endOffset) throws IOException {
        StyledDocument sdoc = (StyledDocument) doc;
        DataObject dObj = (DataObject)doc.getProperty(doc.StreamDescriptionProperty );
        if (dObj == null)
            return new Position[] {null, null};
        LineCookie lc = dObj.getCookie(LineCookie.class);
        int originalStartOffset = info.getSnapshot().getOriginalOffset(startOffset);
        int lineNumber = NbDocument.findLineNumber(sdoc, originalStartOffset);
        int lineOffset = NbDocument.findLineOffset(sdoc, lineNumber);
        Line line = lc.getLineSet().getCurrent(lineNumber);
        
        boolean rangePrepared = false;

        if (INVALID_METHOD_INVOCATION.contains(d.getCode())) {
            int[] span = translatePositions(info, handlePossibleMethodInvocation(info, d, doc, startOffset, endOffset));
            
            if (span != null) {
                startOffset = span[0];
                endOffset = span[1];
                rangePrepared = true;
            }
        }
        
        if (CANNOT_RESOLVE.contains(d.getCode()) && !rangePrepared) {
            int[] span = translatePositions(info, findUnresolvedElementSpan(info, (int) getPrefferedPosition(info, d)));
            
            if (span != null) {
                startOffset = span[0];
                endOffset   = span[1];
                rangePrepared = true;
            }
        }
        
        if (UNDERLINE_IDENTIFIER.contains(d.getCode())) {
            int offset = (int) getPrefferedPosition(info, d);
            TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            
            int diff = ts.move(offset);
            
            if (ts.moveNext() && diff >= 0 && diff < ts.token().length()) {
                Token<JavaTokenId> t = ts.token();
                
                if (t.id() == JavaTokenId.DOT) {
                    while (ts.moveNext() && WHITESPACE.contains(ts.token().id()))
                        ;
                    t = ts.token();
                }
                
                if (t.id() == JavaTokenId.NEW) {
                    while (ts.moveNext() && WHITESPACE.contains(ts.token().id()))
                        ;
                    t = ts.token();
                }
                
                if (t.id() == JavaTokenId.CLASS) {
                    while (ts.moveNext() && WHITESPACE.contains(ts.token().id()))
                        ;
                    t = ts.token();
                }

                if (t.id() == JavaTokenId.IDENTIFIER) {
                    int[] span = translatePositions(info, new int[] {ts.offset(), ts.offset() + t.length()});
                    
                    if (span != null) {
                        startOffset = span[0];
                        endOffset   = span[1];
                        rangePrepared = true;
                    }
                }
            }
        }

        String text = null;

        if (!rangePrepared) {
            text =line.getText();

            if (text == null) {
                //#116560, (according to the javadoc, means the document is closed):
                cancel();
                return null;
            }
        }

        if (!rangePrepared && d.getCode().endsWith("proc.messager")) {
            int originalEndOffset = info.getSnapshot().getOriginalOffset(endOffset);

            if (originalEndOffset <= lineOffset + text.length() && originalStartOffset != (-1) && originalEndOffset != (-1)) {
                startOffset = originalStartOffset;
                endOffset = originalEndOffset;
                rangePrepared = true;
            }
        }
        
        if (!rangePrepared && USE_PROVIDED_SPAN.contains(d.getCode())) {
            startOffset = originalStartOffset;
            endOffset = info.getSnapshot().getOriginalOffset(endOffset);
            rangePrepared = true;
        }
        
        if (!rangePrepared || endOffset < startOffset) {
            int column = 0;
            int length = text.length();

            while (column < text.length() && Character.isWhitespace(text.charAt(column)))
                column++;

            while (length > 0 && Character.isWhitespace(text.charAt(length - 1)))
                length--;

            if(length == 0) //whitespace only
                startOffset = lineOffset;
            else
                startOffset = lineOffset + column;

            endOffset = lineOffset + length;
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
                if (isCanceled())
                    return;
                
                int len = doc.getLength();
                
                if (startOffsetFinal >= len || endOffsetFinal > len) {
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
    
    @Override
    public void run(Result result, SchedulerEvent event) {
        resume();

        CompilationInfo info = CompilationInfo.get(result);

        if (info == null) {
            return ;
        }

        Document doc = result.getSnapshot().getSource().getDocument(false);
        
        if (doc == null) {
            Logger.getLogger(ErrorHintsProvider.class.getName()).log(Level.FINE, "SemanticHighlighter: Cannot get document!");
            return ;
        }

        long version = DocumentUtilities.getDocumentVersion(doc);
        String mimeType = result.getSnapshot().getSource().getMimeType();
        
        long start = System.currentTimeMillis();

        try {
            List<ErrorDescription> errors = computeErrors(info, doc, mimeType);

            if (errors == null) //meaning: cancelled
                return ;

            HintsController.setErrors(doc, ErrorHintsProvider.class.getName(), errors);

            ErrorPositionRefresherHelper.setVersion(doc, errors);
            
            long end = System.currentTimeMillis();

            Logger.getLogger("TIMER").log(Level.FINE, "Java Hints",
                    new Object[]{info.getFileObject(), end - start});
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    private int[] translatePositions(CompilationInfo info, int[] span) {
        if (span == null || span[0] == (-1) || span[1] == (-1))
            return null;
        
        int start = info.getSnapshot().getOriginalOffset(span[0]);
        int end   = info.getSnapshot().getOriginalOffset(span[1]);
        
        if (start == (-1) || end == (-1) || end < start)
            return null;
        
        return new int[] {start, end};
    }
    
    private long getPrefferedPosition(CompilationInfo info, Diagnostic d) throws IOException {
        if ("compiler.err.doesnt.exist".equals(d.getCode())) {
            return d.getStartPosition();
        }
        if ("compiler.err.cant.resolve.location".equals(d.getCode()) || "compiler.err.cant.resolve.location.args".equals(d.getCode())) {
            int[] span = findUnresolvedElementSpan(info, (int) d.getPosition());
            
            if (span != null) {
                return span[0];
            } else {
                return d.getPosition();
            }
        }
        if ("compiler.err.not.stmt".equals(d.getCode())) {
            //check for "Collections.":
            TreePath path = findUnresolvedElement(info, (int) d.getStartPosition() - 1);
            Element el = path != null ? info.getTrees().getElement(path) : null;
            
            if (el == null || el.asType().getKind() == TypeKind.ERROR) {
                return d.getStartPosition() - 1;
            }
            
            if (el.asType().getKind() == TypeKind.PACKAGE) {
                //check if the package does actually exist:
                String s = ((PackageElement) el).getQualifiedName().toString();
                if (info.getElements().getPackageElement(s) == null) {
                    //it does not:
                    return d.getStartPosition() - 1;
                }
            }
            
            return d.getStartPosition();
        }
        
        return d.getPosition();
    }

}

