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
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTError;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler.IncludeState;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.PostIncludeData;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.SimpleOffsetableImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
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
        return TraceFlags.PARSE_HEADERS_WITH_SOURCES;
    }

    public TokenStream getFilteredTokenStream(APTLanguageFilter lang) {
        TokenStream ts = lang.getFilteredStream(getTokenStream());
        // apply preprocessed text indexing
        // disabled for now
//        if (CndTraceFlags.TEXT_INDEX) {
//            ts = APTIndexingSupport.index(getStartProject().getFileSystem(), getFile().getAbsolutePath().toString(), ts);
//        }
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
            APTPreprocHandler preprocHandler = getPreprocHandler();
            FileImpl includedFile = inclFileOwner.prepareIncludedFile(inclFileOwner, inclPath, preprocHandler);
            if (includedFile != null) {
                ProjectBase startProject = getStartProject();
                if (isTokenProducer() && TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                    includeFileWithTokens(startProject, includedFile, preprocHandler, isTriggerParsingActivity());
                } else {
                    FileIncludeInfo inclInfo = includeFileWithoutTokens(inclFileOwner, startProject, includedFile, inclPath, preprocHandler, postIncludeState, mode, isTriggerParsingActivity());
                    if (isTriggerParsingActivity() && inclInfo != null) {
                        inclFileOwner.postIncludeFile(startProject, includedFile, inclPath, inclInfo.ppStatePair, inclInfo.aptCacheEntry);
                    }
                }
            }
            return includedFile;
        } catch (NullPointerException ex) {
            APTUtils.LOG.log(Level.SEVERE, "NPE when processing file " + inclPath, ex);// NOI18N
            DiagnosticExceptoins.register(ex);
        }
        return null;
    }

    @Override
    protected void popInclude(APTInclude aptInclude, ResolvedPath resolvedPath, IncludeState pushState) {
        if (pushState == IncludeState.Success) {
            super.popInclude(aptInclude, resolvedPath, pushState);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    private static final class FileIncludeInfo {
        private final PreprocessorStatePair ppStatePair;
        private final APTFileCacheEntry aptCacheEntry;

        public FileIncludeInfo(PreprocessorStatePair ppStatePair, APTFileCacheEntry aptCacheEntry) {
            this.ppStatePair = ppStatePair;
            this.aptCacheEntry = aptCacheEntry;
        }
    }
    
    private void includeFileWithTokens(ProjectBase startProject, FileImpl includedFile, APTPreprocHandler preprocHandler, boolean triggerParsingActivity) throws IOException {
        APTFile aptFile = CsmCorePackageAccessor.get().getFileAPT(includedFile, true);
        if (aptFile != null) {
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptFile, includedFile, preprocHandler, triggerParsingActivity, null, null);
            FileContent inclFileContent = includedFile.prepareIncludedFileParsingContent();
            walker.setFileContent(inclFileContent);
            includeStream(aptFile, walker);
        } else {
            // in the case file was just removed
            Utils.LOG.log(Level.INFO, "Can not find or build APT for file {0}", includedFile); //NOI18N
        }
    }
    /**
     * called to inform that file was #included from another file with specific
     * preprocHandler
     *
     * @param file included file path
     * @param preprocHandler preprocHandler with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including false if file was
     * included before
     */
    private FileIncludeInfo includeFileWithoutTokens(ProjectBase inclFileOwner, ProjectBase startProject, FileImpl includedFile, CharSequence file, APTPreprocHandler preprocHandler, PostIncludeData postIncludeState, int mode, boolean triggerParsingActivity) throws IOException {
        assert preprocHandler != null : "null preprocHandler for " + file;
        assert includedFile != null : "null FileImpl for " + file;

        if (inclFileOwner.isDisposing() || startProject.isDisposing()) {
            if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                System.err.printf("onFileIncluded: %s file [%s] is interrupted on disposing project\n", file, inclFileOwner.getName());
            }
            return null;
        }
        APTPreprocHandler.State newState = preprocHandler.getState();
        PreprocessorStatePair cachedOut = null;
        APTFileCacheEntry aptCacheEntry = null;
        FilePreprocessorConditionState pcState = null;
        boolean foundInCache = false;
        // check post include cache
        if (postIncludeState != null && postIncludeState.hasDeadBlocks()) {
            assert postIncludeState.hasPostIncludeMacroState() : "how could it be? " + file;
            pcState = CsmCorePackageAccessor.get().createPCState(file, postIncludeState.getDeadBlocks());
            preprocHandler.getMacroMap().setState(postIncludeState.getPostIncludeMacroState());
            foundInCache = true;
        }
        // check visited file cache
        boolean isFileCacheApplicable = (mode == ProjectBase.GATHERING_TOKENS) && (APTHandlersSupport.getIncludeStackDepth(newState) != 0);
        if (!foundInCache && isFileCacheApplicable) {
            
            cachedOut = CsmCorePackageAccessor.get().getCachedVisitedState(includedFile, newState);
            if (cachedOut != null) {
                preprocHandler.getMacroMap().setState(APTHandlersSupport.extractMacroMapState(cachedOut.state));
                pcState = cachedOut.pcState;
                foundInCache = true;
            }
        }
        // if not found in caches => visit include file
        if (!foundInCache) {
            APTFile aptLight = CsmCorePackageAccessor.get().getFileAPT(includedFile, false);
            if (aptLight == null) {
                // in the case file was just removed
                Utils.LOG.log(Level.INFO, "Can not find or build APT for file {0}", file); //NOI18N
                return null;
            }

            // gather macro map from all includes and fill preprocessor conditions state
            FilePreprocessorConditionState.Builder pcBuilder = new FilePreprocessorConditionState.Builder(includedFile.getAbsolutePath());
            // ask for exclusive entry if absent
            aptCacheEntry = includedFile.getAPTCacheEntry(newState, Boolean.TRUE);
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptLight, includedFile, preprocHandler, triggerParsingActivity, pcBuilder, aptCacheEntry);
            walker.visit();
            pcState = pcBuilder.build();
        }
        // updated caches
        // update post include cache
        if (postIncludeState != null && !postIncludeState.hasDeadBlocks()) {
            int[] deadBlocks = CsmCorePackageAccessor.get().getPCStateDeadBlocks(pcState);
            // cache info
            postIncludeState.setDeadBlocks(deadBlocks);
        }
        // updated visited file cache
        if (cachedOut == null && isFileCacheApplicable) {            
            CsmCorePackageAccessor.get().cacheVisitedState(includedFile, newState, preprocHandler, pcState);
        }
        return new FileIncludeInfo(new PreprocessorStatePair(newState, pcState), aptCacheEntry);
    }

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
