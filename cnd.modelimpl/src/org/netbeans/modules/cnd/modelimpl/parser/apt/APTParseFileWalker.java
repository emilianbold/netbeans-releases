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
package org.netbeans.modules.cnd.modelimpl.parser.apt;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTError;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.APTIndexingSupport;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.SimpleOffsetableImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * implementation of walker used when parse files/collect macromap
 * @author Vladimir Voskresensky
 */
public class APTParseFileWalker extends APTProjectFileBasedWalker {

    /** 
     * A callback that should be invoked
     * when each conditional is evaluated
     */
    public interface EvalCallback {

        void onEval(APT apt, boolean result);

        void onStoppedDirective(APT apt);
    }
    private FileContent fileContent;
    private final boolean triggerParsingActivity;
    private final EvalCallback evalCallback;
    private static final EvalCallback EMPTY_EVAL_CALLBACK = new EvalCallback() {
        @Override
        public void onEval(APT apt, boolean result) { }
        @Override
        public void onStoppedDirective(APT apt) { }
    };

    public APTParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler, boolean triggerParsingActivity, EvalCallback evalCallback, APTFileCacheEntry cacheEntry) {
        super(base, apt, file, preprocHandler, cacheEntry);
        this.evalCallback = evalCallback != null ? evalCallback : EMPTY_EVAL_CALLBACK;
        this.triggerParsingActivity = triggerParsingActivity;
    }

    public void setFileContent(FileContent content) {
        this.fileContent = content;
    }

    protected boolean needMacroAndIncludes() {
        return this.fileContent != null;
    }

    public final boolean isTriggerParsingActivity() {
        return triggerParsingActivity;
    }

    @Override
    protected boolean needPPTokens() {
        return APTTraceFlags.INCLUDE_TOKENS_IN_TOKEN_STREAM;
    }

    public TokenStream getFilteredTokenStream(APTLanguageFilter lang) {
        TokenStream ts = lang.getFilteredStream(getTokenStream());
        // apply preprocessed text indexing
        if (CndTraceFlags.TEXT_INDEX) {
            ts = APTIndexingSupport.index(getStartProject().getFileSystem(), getFile().getAbsolutePath().toString(), ts);
        }
        return ts;
    }

    @Override
    public TokenStream getTokenStream() {
        return getTokenStream(true);
    }

    public TokenStream getTokenStream(boolean filtered) {
        setMode(ProjectBase.GATHERING_TOKENS);
        // get original
        TokenStream ts = super.getTokenStream();
        // expand macros
        ts = new APTMacroExpandedStream(ts, getMacroMap(), !filtered);
        if (filtered) {
            // remove comments
            ts = new APTCommentsFilter(ts);
        }
        return ts;
    }

    @Override
    protected void onDefine(APT apt) {
        super.onDefine(apt);
        if (needMacroAndIncludes()) {
            MacroImpl macro = createMacro((APTDefine) apt);
            if (macro != null) {
                this.fileContent.addMacro(macro);
            }
        }
    }

    @Override
    protected void onErrorNode(APT apt) {
        super.onErrorNode(apt);
        if (needMacroAndIncludes()) {
            this.fileContent.addError(createError((APTError)apt));
            evalCallback.onStoppedDirective(apt);
        }
    }

    @Override
    protected void onPragmaNode(APT apt) {
        super.onPragmaNode(apt);
        if (isStopped()) {
            evalCallback.onStoppedDirective(apt);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of abstract methods
    @Override
    protected void postInclude(APTInclude apt, FileImpl included, IncludeState pushIncludeState) {
        if (needMacroAndIncludes()) {
            this.fileContent.addInclude(createInclude(apt, included, pushIncludeState == IncludeState.Recursive), pushIncludeState != IncludeState.Success);
        }
    }

    @Override
    protected boolean hasIncludeActionSideEffects() {
        return needMacroAndIncludes();
    }

    @Override
    protected FileImpl includeAction(ProjectBase inclFileOwner, CharSequence inclPath, int mode, APTInclude apt, PostIncludeData postIncludeState) throws IOException {
        try {
            return inclFileOwner.onFileIncluded(getStartProject(), inclPath, getPreprocHandler(), postIncludeState, mode, isTriggerParsingActivity());
        } catch (NullPointerException ex) {
            APTUtils.LOG.log(Level.SEVERE, "NPE when processing file " + inclPath, ex);// NOI18N
            DiagnosticExceptoins.register(ex);
        } finally {
            getIncludeHandler().popInclude();
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    private ErrorDirectiveImpl createError(APTError error) {
        APTToken token = error.getToken();
        SimpleOffsetableImpl pos = getOffsetable(token);
        setEndPosition(pos, token);
        return ErrorDirectiveImpl.create(this.getFile(), token.getTextID(), pos);
    }

    private MacroImpl createMacro(APTDefine define) {
        // create even for invalid macro (to have possibility of showing error HL)
        List<CharSequence> params = null;
        Collection<APTToken> paramTokens = define.getParams();
        if (paramTokens != null) {
            params = new ArrayList<CharSequence>(paramTokens.size());
            for (APTToken elem : paramTokens) {
                if (APTUtils.isID(elem)) {
                    params.add(NameCache.getManager().getString(elem.getTextID()));
                }
            }
            if (params.isEmpty()) {
                params = Collections.<CharSequence>emptyList();
            }
        }

        int startOffset = define.getToken().getOffset();
        List<APTToken> bodyTokens = define.getBody();
        APTToken last;
        String body = "";
        if (bodyTokens.isEmpty()) {
            last = define.getName();
        } else {
            last = bodyTokens.get(bodyTokens.size() - 1);
            //APTToken start = (APTToken) bodyTokens.get(0);
            // FIXUP (performance/memory). For now:
            // 1) nobody uses macros.getText
            // 2) its realization is ineffective
            // so we temporarily switch this off
            body = ""; //file.getText( start.getOffset(), last.getEndOffset());
        }
        int endOffset = (last != null && !APTUtils.isEOF(last) && last.getEndOffset() > 0) ? last.getEndOffset() : startOffset;       
        CsmMacro.Kind kind = define.isValid() ? CsmMacro.Kind.DEFINED : CsmMacro.Kind.INVALID;
        return MacroImpl.create(define.getName().getTextID(), params, body/*sb.toString()*/, getFile(), startOffset, endOffset, kind);
    }

    private IncludeImpl createInclude(final APTInclude apt, final FileImpl included, boolean recursive) {
        int startOffset = apt.getToken().getOffset();
        APTToken lastToken = getLastToken(apt.getInclude());
        if(lastToken == null || APTUtils.isEOF(lastToken)) {
            lastToken = apt.getToken();
        }
        int endOffset = (lastToken != null && !APTUtils.isEOF(lastToken)) ? lastToken.getEndOffset() : startOffset;
        IncludeImpl incImpl = IncludeImpl.create(apt.getFileName(getMacroMap()), apt.isSystem(getMacroMap()), recursive, included, getFile(), startOffset, endOffset);
        return incImpl;
    }

    private SimpleOffsetableImpl getOffsetable(APTToken token) {
        return new SimpleOffsetableImpl(token.getLine(), token.getColumn(), token.getOffset());
    }

    private void setEndPosition(SimpleOffsetableImpl offsetable, APTToken token) {
        if (token != null && !APTUtils.isEOF(token)) {
            offsetable.setEndPosition(token.getEndLine(), token.getEndColumn(), token.getEndOffset());
        } else {
            assert offsetable.getStartPosition() != null;
            offsetable.setEndPosition(offsetable.getStartPosition());
        }
    }

    private APTToken getLastToken(TokenStream ts) {
        try {
            Token last = ts.nextToken();
            for (Token curr = null; !APTUtils.isEOF(curr = ts.nextToken());) {
                last = curr;
            }
            return (APTToken) last;
        } catch (TokenStreamException e) {
            DiagnosticExceptoins.register(e);
            return null;
        }
    }

    @Override
    protected void onEval(APT apt, boolean result) {
        evalCallback.onEval(apt, result);
    }
}
