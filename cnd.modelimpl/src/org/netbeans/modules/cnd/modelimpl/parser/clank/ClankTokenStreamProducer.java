/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankPreprocessorCallback;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankMacroDirective;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.Line2Offset;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ClankTokenStreamProducer extends TokenStreamProducer {

    private int[] skipped;

    private ClankTokenStreamProducer(FileImpl file, FileContent newFileContent, boolean fromEnsureParsed) {
        super(file, newFileContent, fromEnsureParsed);
    }
    
    public static TokenStreamProducer createImpl(FileImpl file, FileContent newFileContent, boolean fromEnsureParsed) {
        return new ClankTokenStreamProducer(file, newFileContent, fromEnsureParsed);
    }

    public static List<CsmReference> getMacroUsages(FileImpl file, PreprocHandler handler, Interrupter interrupter) {
        FileContent newFileContent = FileContent.getHardReferenceBasedCopy(file.getCurrentFileContent(), true);
        // TODO: we do NOT need file content at all here
        ClankTokenStreamProducer tsp = new ClankTokenStreamProducer(file, newFileContent, false);
        PreprocHandler.State ppState = handler.getState();
        String contextLanguage = file.getContextLanguage(ppState);
        String contextLanguageFlavor = file.getContextLanguageFlavor(ppState);
        tsp.prepare(handler, contextLanguage, contextLanguageFlavor, false);
        TokenStreamProducer.Parameters params = TokenStreamProducer.Parameters.createForMacroUsages();
        List<CsmReference> res = tsp.getMacroUsages(params, interrupter);
        tsp.release();
        return res;
    }

    @Override
    public TokenStream getTokenStreamOfIncludedFile(PreprocHandler.State includeOwnerState, CsmInclude include, Interrupter interrupter) {
        FileImpl fileImpl = getMainFile();
        ProjectBase projectImpl = fileImpl.getProjectImpl(true);
        if (projectImpl == null) {
          return null;
        }
        PreprocHandler ppHandler = projectImpl.createPreprocHandlerFromState(fileImpl.getAbsolutePath(), includeOwnerState);
        int fileOwnerIndex = ClankDriver.extractTokenStream(ppHandler).getFileIndex();
        // do preprocessing
        IncludeDirectiveTokensStreamCallback callback =
                new IncludeDirectiveTokensStreamCallback(ppHandler, fileOwnerIndex, include.getStartOffset());
        boolean tsFromClank = ClankDriver.preprocess(fileImpl.getBuffer(), ppHandler, callback, interrupter);
        if (!tsFromClank) {
            return null;
        }
        ClankDriver.APTTokenStreamCache tokStreamCache = callback.getPPOut();
        if (tokStreamCache == null) {
          return null;
        }
        skipped = new int[0];
        TokenStream tokenStream = tokStreamCache.getTokenStream();
        if (tokenStream == null) {
          return null;
        }
        return tokenStream;
    }

    @Override
    public TokenStream getTokenStream(Parameters parameters, Interrupter interrupter) {
        PreprocHandler ppHandler = getCurrentPreprocHandler();
        ClankDriver.APTTokenStreamCache tokStreamCache = ClankDriver.extractTokenStream(ppHandler);
        assert tokStreamCache != null;
        FileImpl fileImpl = getMainFile();
        if (!tokStreamCache.hasTokenStream()) {
          // do preprocessing
          FileTokenStreamCallback callback = new FileTokenStreamCallback(
                  ppHandler,
                  parameters,
                  fileImpl,
                  tokStreamCache.getFileIndex());
          FileBuffer buffer = fileImpl.getBuffer();
          if (getCodePatch() != null) {
              buffer = new PatchedFileBuffer(buffer, getCodePatch());
          }
          boolean tsFromClank = ClankDriver.preprocess(buffer, ppHandler, callback, interrupter);
          if (!tsFromClank) {
              return null;
          }
          tokStreamCache = callback.getPPOut();
          if (tokStreamCache == null) {
            return null;
          }
          cacheMacroUsagesInFileIfNeed(parameters, callback.getFoundFileInfo());
        }
        TokenStream tokenStream = tokStreamCache.getTokenStream();
        if (tokenStream == null) {
          return null;
        }
        if (super.isFromEnsureParsed()) {
          addPreprocessorDirectives(fileImpl, getFileContent(), tokStreamCache);
          addMacroExpansions(fileImpl, getFileContent(), getStartFile(), tokStreamCache);
          setFileGuard(fileImpl, getFileContent(), tokStreamCache);
        }
        skipped = tokStreamCache.getSkippedRanges();
        if (parameters.applyLanguageFilter) {
          APTLanguageFilter languageFilter = fileImpl.getLanguageFilter(ppHandler.getState());
          tokenStream = languageFilter.getFilteredStream(new APTCommentsFilter(tokenStream));
        }
        return tokenStream;
    }

    private List<CsmReference> getMacroUsages(TokenStreamProducer.Parameters parameters, Interrupter interrupter) {
        PreprocHandler ppHandler = getCurrentPreprocHandler();
        int fileIndex = ClankDriver.extractTokenStream(ppHandler).getFileIndex();
        FileImpl fileImpl = getMainFile();
        FileTokenStreamCallback callback = new FileTokenStreamCallback(
                ppHandler,
                parameters,
                fileImpl,
                fileIndex);
        if (ClankDriver.preprocess(fileImpl.getBuffer(), ppHandler, callback, interrupter)) {
            ClankDriver.ClankFileInfo foundFileInfo = callback.getFoundFileInfo();
            return ClankMacroUsagesSupport.getMacroUsages(fileImpl, getStartFile(), foundFileInfo);
        } else {
            return Collections.emptyList();
        }
    }

    private void cacheMacroUsagesInFileIfNeed(Parameters parameters, ClankDriver.ClankFileInfo foundFileInfo) {
        if (foundFileInfo == null) {
            return; // can this happen? should we assert? (softly!)
        }
        // TODO: shouldn't we introduce a special flag for this?
        if (parameters.needMacroExpansion == YesNoInterested.INTERESTED && parameters.needPPDirectives == YesNoInterested.INTERESTED) {
            FileImpl fileImpl = getMainFile();
            List<CsmReference> macroUsages = ClankMacroUsagesSupport.getMacroUsages(fileImpl, getStartFile(), foundFileInfo);
            fileImpl.setLastMacroUsages(macroUsages);
        }
    }

    @Override
    public FilePreprocessorConditionState release() {
        return FilePreprocessorConditionState.build(getMainFile().getAbsolutePath(), skipped);
    }

    private static final class FileTokenStreamCallback implements ClankPreprocessorCallback {
        private final ProjectBase startProject;
        private final PreprocHandler ppHandler;

        private final FileImpl stopFileImpl;
        private final int stopAtIndex;
        private ClankDriver.APTTokenStreamCache foundTokens;
        private ClankDriver.ClankFileInfo foundFileInfo;

        private enum State {
          INITIAL,
          SEEN,
          EXITED
        }
        private State alreadySeenInterestedFileEnter = State.INITIAL;
        private boolean insideInterestedFile = false;
        private boolean skipCurrentFileContentOptimization = false;
        private final Parameters parameters;

        private final List<FileImpl> curFiles = new ArrayList<>();
        private final List<Boolean>  skipCurFileContentOptimizations = new ArrayList<>();

        private FileTokenStreamCallback(
                PreprocHandler ppHandler,
                Parameters parameters,
                FileImpl stopFileImpl, 
                int stopAtIndex) {
            this.ppHandler = ppHandler;
            StartEntry startEntry = ppHandler.getIncludeHandler().getStartEntry();
            this.startProject = Utils.getStartProject(startEntry);
            this.parameters = parameters;
            this.stopFileImpl = stopFileImpl;
            this.stopAtIndex = stopAtIndex;
        }

        boolean isTrace() {
          if (false && stopFileImpl.getName().toString().endsWith(".h")) {// NOI18N
            return true;
          }
          return false;
        }

        @Override
        public void onErrorDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankErrorDirective directive) {
        }

        
        private boolean valueOf(/*YesNoInterested*/int param) {
            switch (param) {
                case YesNoInterested.ALWAYS:
                    return true;
                case YesNoInterested.NEVER:
                    return false;
                case YesNoInterested.INTERESTED:
                    return insideInterestedFile;
                default:
                    throw new AssertionError("unknown" + param);

            }
        }
        
        @Override
        public boolean needPPDirectives() {
            return !skipCurrentFileContentOptimization && valueOf(parameters.needPPDirectives);
        }

        @Override
        public boolean needTokens() {
            return !skipCurrentFileContentOptimization && valueOf(parameters.needTokens);
        }

        @Override
        public boolean needSkippedRanges() {
          return valueOf(parameters.needSkippedRanges);
        }

        @Override
        public boolean needMacroExpansion() {
            return valueOf(parameters.needMacroExpansion);
        }

        @Override
        public boolean needComments() {
            return !skipCurrentFileContentOptimization && valueOf(parameters.needComments);
        }

        /**
         * in the stack on tracked files return top one and pop if needed.
         * @param pop true to pop, false to peek only
         * @return non null top file
         */
        private FileImpl getCurFile(boolean pop) {
          assert curFiles.size() > 0;
          FileImpl curFile;
          if (pop) {
            curFile = curFiles.remove(curFiles.size() - 1);
          } else {
            curFile = curFiles.get(curFiles.size() - 1);
          }
          assert curFile != null;
          return curFile;
        }
        
        private boolean getSkipCurrentFileContentOptimization(boolean pop) {
          assert skipCurFileContentOptimizations.size() > 0;
          Boolean curFileSkipOptimization;
          if (pop) {
            curFileSkipOptimization = skipCurFileContentOptimizations.remove(skipCurFileContentOptimizations.size() - 1);
          } else {
            curFileSkipOptimization = skipCurFileContentOptimizations.get(skipCurFileContentOptimizations.size() - 1);
          }
          assert curFileSkipOptimization != null;
          return curFileSkipOptimization;
        }

        private ProjectBase getStartProject() {
          return startProject;
        }
        
        @Override
        public void onInclusionDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankInclusionDirective directive) {
            // 1. proceed only when onEnter already have met interested file
            // 2. if 1 is satisfied, then the next condition is to handle include only 
            //    in parsing full TU mode or when onEnter has pushed interested file on top of stack
            if ((alreadySeenInterestedFileEnter == State.SEEN) && (parameters.triggerParsingActivity || insideInterestedFile)) {
              // let's resolve include as FileImpl
              ResolvedPath resolvedPath = directive.getResolvedPath();
              if (resolvedPath == null) {
                // broken #include path
                return;
              }
              // peek file from onEnter
              FileImpl curFile = getCurFile(false);
              FileImpl includedFile = null;
              CharSequence path = resolvedPath.getPath();
              ProjectBase aStartProject = startProject;
              if (aStartProject != null) {
                  // resolve if not interrupted
                  if (aStartProject.isValid() && curFile.isValid()) {
                      ProjectBase inclFileOwner = aStartProject.getLibraryManager().resolveFileProjectOnInclude(aStartProject, curFile, resolvedPath);
                      if (inclFileOwner == null) {
                          // resolveFileProjectOnInclude() javadoc reads: "Can return NULL !"; and it asserts itself
                          if (aStartProject.getFileSystem() == resolvedPath.getFileSystem()) {
                              // if file systems do match, then use start project as fallback
                              inclFileOwner = aStartProject;
                          }
                      }
                      if (inclFileOwner == null) {
                          new IllegalStateException("Can not resolve #include file owner for " + resolvedPath).printStackTrace(); //NOI18N
                          return;
                      }
                      if (CndUtils.isDebugMode()) {
                          CndUtils.assertTrue(inclFileOwner.getFileSystem() == resolvedPath.getFileSystem(), "Different FS for " + path + ": " + inclFileOwner.getFileSystem() + " vs " + resolvedPath.getFileSystem()); // NOI18N
                      }
                      // when owner of included file is detected we can ask it for FileImpl instance
                      includedFile = inclFileOwner.prepareIncludedFile(aStartProject, path, ppHandler);
                      if (includedFile == null) {
                        if (CsmModelAccessor.isModelAlive() && inclFileOwner.isValid()) {
                            if (aStartProject.isValid()) {
                                assert false : "something wrong when including " + path + " from " + curFile;
                            } else {
                                APTUtils.LOG.log(Level.INFO, "invalid start project {0} when including {1} from {2}", new Object[] {aStartProject, path, curFile});
                            }
                        }
                      }
                  } else {
                    APTUtils.LOG.log(Level.INFO, "invalid start project {0} or file when including {1} from {2}", new Object[] {aStartProject, path, curFile});
                    // assert false : "invalid start project when including " + path + " from " + curFile;
                  }
              } else {
                  APTUtils.LOG.log(Level.SEVERE, "FileTokenStreamCallback: file {0} without project!!!", new Object[]{path});// NOI18N
              }
              // annotated include directive to have access to FileImpl from onEnter which follows all resolved #includes 
              directive.setAnnotation(includedFile);
            }            
        }

        private static final boolean ALLOW_TO_SKIP_TOKENS_BETWEEN_DIRECTIVES = Boolean.valueOf(System.getProperty("clank.callback.allow.skip.token", "false")); //NativeTrace.DEBUG;
        @Override
        public void onEnter(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {
            assert enteredTo != null;
            boolean canSkipFileContent = false;
            boolean onEnterIntoInterestedFile = (enteredTo.getFileIndex() == stopAtIndex);
            // when parse TU stopAtIndex is 0, so the branch below is met first and switch mode to SEEN
            if (onEnterIntoInterestedFile) {
                // entered into interested file, it can be only once
                // because even without guards other enter int header has different unique file-index
                assert alreadySeenInterestedFileEnter == State.INITIAL;
                alreadySeenInterestedFileEnter = State.SEEN;
                CndUtils.assertPathsEqualInConsole(enteredTo.getFilePath(), stopFileImpl.getAbsolutePath(),
                        "{0}\n vs. \n{1}", enteredTo, stopFileImpl);// NOI18N
                // we entered target file and after that we can
                // handle inclusive #includes
                // remember entered file in stack to be paired in onExit
                curFiles.add(stopFileImpl);
                skipCurFileContentOptimizations.add(Boolean.FALSE);
            } else {
              // first must be switched to SEEN state in the branch above;
              // we can skip till interested file, i.e. when restore TS for some file in deep inclusion stack;
              if ((alreadySeenInterestedFileEnter == State.SEEN) && parameters.triggerParsingActivity) {
                // let's keep stack of inner includes prepared in onInclusionDirective
                // then during onExit post process headers wthich should be parsed
                FileImpl includedFile = (FileImpl) enteredTo.getInclusionDirective().getAnnotation();
                FileImpl pushFile;
                if (includedFile != null) {
                  // remember entered file in stack to to have pair in onExit
                  pushFile = includedFile;
                  // do the best to reduce work in parsing phase to be done on include of resolved file
                  if (includedFile.checkIfFileWasIncludedBeforeWithBetterOrEqualContent(ppHandler)) {
                    // i.e. no need to keep tokens, because postInclude would discard included file
                    CndUtils.assertTrueInConsole(!onEnterIntoInterestedFile, "how can we skip interested file?", includedFile);
                    canSkipFileContent = ALLOW_TO_SKIP_TOKENS_BETWEEN_DIRECTIVES;
                  }                  
                } else {
                  // it is suspicious to see unresolved include followed by onEnter hook
                  // it might be in case of cancelled/interrupted query
                  FileImpl curFile = getCurFile(false);
                  canSkipFileContent = false;
                  if (CsmModelAccessor.isModelAlive() && curFile.isValid()) {
                    assert false : "something wrong when including " + enteredTo.getFilePath() + " from " + curFile;
                  }
                  // for error recovery add at least current file to have pair in onExit
                  pushFile = curFile;
                }
                curFiles.add(pushFile);
                skipCurFileContentOptimizations.add(canSkipFileContent ? Boolean.TRUE : Boolean.FALSE);
              }
            }
            insideInterestedFile = onEnterIntoInterestedFile;
            skipCurrentFileContentOptimization = canSkipFileContent;
        }

        @Override
        public boolean onExit(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo) {
            assert exitedFrom != null;
            // fast paths
            if (alreadySeenInterestedFileEnter == State.EXITED) {
              // stop all activity recursively 
              return false;
            } else if (alreadySeenInterestedFileEnter == State.INITIAL) {
              // continue till onEnter meets interested file and switch to SEEN
              return true;
            }
            insideInterestedFile = (exitedTo != null) && (exitedTo.getFileIndex() == stopAtIndex);
            skipCurrentFileContentOptimization = false;
            if (stopAtIndex == exitedFrom.getFileIndex()) {
              CndUtils.assertPathsEqualInConsole(exitedFrom.getFilePath(), stopFileImpl.getAbsolutePath(),
                      "{0} expected {1}", stopFileImpl.getAbsolutePath(), exitedFrom);// NOI18N
              foundTokens = ClankDriver.extractPreparedCachedTokenStream(ppHandler);
              foundFileInfo = exitedFrom;
              if (foundFileInfo != null && parameters.needTokens == YesNoInterested.NEVER) {
                  // most probably someone was interested in macro expansion, not tokens
                  // prepare caches while PP is in valid state
                  ClankDriver.prepareCachesIfPossible(foundFileInfo);
              }
              assert parameters.needTokens == YesNoInterested.NEVER || foundTokens.hasTokenStream();
              // stop all activity
              alreadySeenInterestedFileEnter = State.EXITED;
              return false;
            } else if (parameters.triggerParsingActivity) {
              assert alreadySeenInterestedFileEnter == State.SEEN;
              // check if onEnter we decided to skip this file content
              FileImpl currentInclusion = getCurFile(true);
              skipCurrentFileContentOptimization = getSkipCurrentFileContentOptimization(true);
              if (!skipCurrentFileContentOptimization) {
                try {
                  assert ClankDriver.extractTokenStream(ppHandler).hasTokenStream();
                  PreprocHandler.State inclState = ppHandler.getState();
                  assert !inclState.isCleaned();
                  if (currentInclusion != null) {
                    CharSequence inclPath = currentInclusion.getAbsolutePath();
                    ProjectBase inclFileOwner = currentInclusion.getProjectImpl(true);
                    ProjectBase aStartProject = getStartProject();
                    if (inclFileOwner.isDisposing() || aStartProject.isDisposing()) {
                      if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                        System.err.printf("onFileIncluded: %s file [%s] is interrupted on disposing project%n", inclPath, inclFileOwner.getName());
                      }
                    } else {
                      FilePreprocessorConditionState pcState = FilePreprocessorConditionState.build(inclPath, exitedFrom.getSkippedRanges());
                      PreprocessorStatePair ppStatePair = new PreprocessorStatePair(inclState, pcState);
                      inclFileOwner.postIncludeFile(aStartProject, currentInclusion, inclPath, ppStatePair, null);
                    }
                  }
                } catch (Exception ex) {
                  APTUtils.LOG.log(Level.SEVERE, "MyClankPreprocessorCallback: error on including {0}:%n{1}", new Object[]{exitedFrom.getFilePath(), ex});
                  DiagnosticExceptoins.register(ex);
                }
              }
            }
            return true;
        }

        private ClankDriver.ClankFileInfo getFoundFileInfo() {
            return foundFileInfo;
        }

        private ClankDriver.APTTokenStreamCache getPPOut() {
            return foundTokens;
        }
    }
    
    private static final class IncludeDirectiveTokensStreamCallback implements ClankPreprocessorCallback {
        private final PreprocHandler ppHandler;

        private final int includeFileOnwerIndex;
        private final int interestedClankIncludeDirectiveOffset;
        private ClankDriver.APTTokenStreamCache foundTokens = null;
        private boolean insideInterestedFile = false;

        private ClankDriver.ClankFileInfo includedFileInfo = null;

        private IncludeDirectiveTokensStreamCallback(PreprocHandler ppHandler,
                int includeFileOnwerIndex, int interestedIncludeDirectiveOffset) {
            this.ppHandler = ppHandler;
            this.includeFileOnwerIndex = includeFileOnwerIndex;
            // adjust to Clank offset
            this.interestedClankIncludeDirectiveOffset = interestedIncludeDirectiveOffset;
        }

        @Override
        public void onErrorDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankErrorDirective directive) {
        }

        @Override
        public boolean needPPDirectives() {
            return false;
        }

        @Override
        public boolean needTokens() {
          return this.insideInterestedFile;
        }

        @Override
        public boolean needMacroExpansion() {
          return false;
        }

        @Override
        public boolean needComments() {
          return false;
        }

        @Override
        public boolean needSkippedRanges() {
          return false;
        }

        @Override
        public void onInclusionDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankInclusionDirective directive) {

        }

        @Override
        public void onEnter(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {
            assert enteredTo != null;
            insideInterestedFile = false;
            // TODO: we can collect all included recursively, but not now
            if (enteredFrom != null && enteredFrom.getFileIndex() == includeFileOnwerIndex) {
              // entering from file owner into include directive
              if (enteredTo.getInclusionDirective().getDirectiveStartOffset() == this.interestedClankIncludeDirectiveOffset) {
                assert includedFileInfo == null : "seen twice? " + includedFileInfo;
                includedFileInfo = enteredTo;
                insideInterestedFile = true;
              }
            }
        }

        @Override
        public boolean onExit(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo) {
            if (foundTokens != null) {
              return false;
            }
            assert exitedFrom != null;
            if (exitedFrom == includedFileInfo) {
              // stop all activity on exit from interested include directive
              foundTokens = ClankDriver.extractPreparedCachedTokenStream(ppHandler);
              assert foundTokens.hasTokenStream();
              return false;
            }
            // gather when come back to interested file
            insideInterestedFile = (exitedTo == includedFileInfo);
            return true;
        }

        private ClankDriver.APTTokenStreamCache getPPOut() {
            return foundTokens;
        }
    }

    private static void addPreprocessorDirectives(FileImpl curFile, FileContent parsingFileContent, ClankDriver.APTTokenStreamCache cache) {
        assert parsingFileContent != null;
        assert curFile != null;
        assert cache != null;
        for (ClankDriver.ClankPreprocessorDirective cur : cache.getPreprocessorDirectives()) {
            if (cur instanceof ClankDriver.ClankInclusionDirective) {
                addInclude(curFile, parsingFileContent, (ClankDriver.ClankInclusionDirective)cur);
            } else if (cur instanceof ClankDriver.ClankErrorDirective) {
                addError(curFile, parsingFileContent, (ClankDriver.ClankErrorDirective)cur);
            } else if (cur instanceof ClankMacroDirective) {
                addMacro(curFile, parsingFileContent, (ClankMacroDirective)cur);
            } else {
              CndUtils.assertTrueInConsole(false, "unknown directive " + cur.getClass().getSimpleName() + " " + cur);
            }
        }
    }
    
    private static void setFileGuard(FileImpl curFile, FileContent parsingFileContent, ClankDriver.APTTokenStreamCache cache) {
        ClankDriver.FileGuard fileGuard = cache.getFileGuard();
        if (fileGuard != null) {
            curFile.setFileGuard(fileGuard.getStartOfset(), fileGuard.getEndOfset());
        } else {
            curFile.setFileGuard(-1, -1);
        }
    }

    private static void addMacroExpansions(FileImpl curFile, FileContent parsingFileContent, FileImpl startFile, ClankDriver.APTTokenStreamCache cache) {
        for (ClankDriver.MacroExpansion cur : cache.getMacroExpansions()) {
            ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                addMacroUsage(curFile, parsingFileContent, MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getStartOfset()+cur.getMacroNameLength(), startFile, directive));
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective "+cur;
            }
        }
        for(ClankDriver.MacroUsage cur : cache.getMacroUsages()) {
            ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                addMacroUsage(curFile, parsingFileContent, MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getEndOfset(), startFile, directive));
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective "+cur;
            }
        }
    }

    private static void addMacroUsage(FileImpl curFile, FileContent parsingFileContent, MacroReference macroReference) {
        parsingFileContent.addReference(macroReference, macroReference.getReferencedObject());
    }

    private static void addMacro(FileImpl curFile, FileContent parsingFileContent, ClankMacroDirective ppDirective) {
        if (!ppDirective.isDefined()) {
            // only #define are handled by old model, not #undef
            return;
        }
        CsmMacro.Kind kind = CsmMacro.Kind.DEFINED;
        List<CharSequence> params = ppDirective.getParameters();
        CharSequence name = ppDirective.getMacroName();
        String body = "";
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        int macroNameOffset = ppDirective.getMacroNameOffset();
        CsmMacro impl = MacroImpl.create(name, params, body/*sb.toString()*/, curFile, startOffset, endOffset, kind);
        parsingFileContent.addMacro(impl);
        parsingFileContent.addReference(new MacroDeclarationReference(curFile, impl, macroNameOffset), impl);
    }

    private static void addError(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankErrorDirective ppDirective) {
        CharSequence msg = ppDirective.getMessage();
        PreprocHandler.State state = ppDirective.getStateWhenMetErrorDirective();
        int start = ppDirective.getDirectiveStartOffset();
        int end = ppDirective.getDirectiveEndOffset();
        ErrorDirectiveImpl impl = ErrorDirectiveImpl.create(curFile, msg, new CsmOffsetableImpl(curFile, start, end), state);
        parsingFileContent.addError(impl);
    }

    private static void addInclude(FileImpl curFile, FileContent parsingFileContent, ClankDriver.ClankInclusionDirective ppDirective) {
        ResolvedPath resolvedPath = ppDirective.getResolvedPath();
        CharSequence fileName = ppDirective.getSpellingName();
        boolean system = ppDirective.isAngled();
        boolean broken = (resolvedPath == null);
        FileImpl includedFile = (FileImpl) ppDirective.getAnnotation();
        if ((includedFile == null) != broken) {
            if (CsmModelAccessor.isModelAlive()) {
                assert false : "broken " + broken + " vs. " + includedFile;
            }
        }
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        //boolean hasRecursiveInclude = curFile.equals(includedFile);
        IncludeImpl incl = IncludeImpl.create(fileName.toString(), system, ppDirective.isRecursive(), includedFile, curFile, startOffset, endOffset);
        parsingFileContent.addInclude(incl, broken || ppDirective.isRecursive());
    }

    private static final class CsmOffsetableImpl implements CsmOffsetable {

        private final CsmFile file;
        private final int selectionStart;
        private final int selectionEnd;

        public CsmOffsetableImpl(CsmFile file, int selectionStart, int selectionEnd) {
            this.file = file;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        @Override
        public CsmFile getContainingFile() {
            return file;
        }

        @Override
        public int getStartOffset() {
            return selectionStart;
        }

        @Override
        public int getEndOffset() {
            return selectionEnd;
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class PatchedFileBuffer implements FileBuffer {
        private final FileBuffer delegate;
        private final CodePatch codePatch;
        private char[] res;
        private Line2Offset lines;

        private PatchedFileBuffer(FileBuffer delegate, CodePatch patchCode) {
            this.delegate = delegate;
            this.codePatch = patchCode;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isFileBased() {
            return delegate.isFileBased();
        }

        @Override
        public FileObject getFileObject() {
            return delegate.getFileObject();
        }

        @Override
        public CharSequence getUrl() {
            return delegate.getUrl();
        }

        @Override
        public String getText(int start, int end) throws IOException {
            return new String(getCharBuffer(), start, end - start);
        }

        @Override
        public CharSequence getText() throws IOException {
            return new FileBufferFile.MyCharSequence(getCharBuffer());
        }

        @Override
        public long lastModified() {
            return delegate.lastModified()+1;
        }

        @Override
        public long getCRC() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int[] getLineColumnByOffset(int offset) throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }

            return lines.getLineColumnByOffset(offset);
        }

        @Override
        public int getLineCount() throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }
            return lines.getLineCount();
        }

        @Override
        public int getOffsetByLineColumn(int line, int column) throws IOException {
            if (lines == null) {
                lines = new Line2Offset(getCharBuffer());
            }
            return lines.getOffsetByLineColumn(line, column);
        }

        @Override
        public CharSequence getAbsolutePath() {
            return delegate.getAbsolutePath();
        }

        @Override
        public FileSystem getFileSystem() {
            return delegate.getFileSystem();
        }

        @Override
        public char[] getCharBuffer() throws IOException {
            if (res == null) {
                char[] charBuffer = delegate.getCharBuffer();
                char[] patch = codePatch.getPatch().toCharArray();
                res = new char[charBuffer.length-(codePatch.getEndOffset()-codePatch.getStartOffset())+patch.length];
                System.arraycopy(charBuffer, 0, res, 0, codePatch.getStartOffset());
                System.arraycopy(patch, 0, res, codePatch.getStartOffset(), patch.length);
                System.arraycopy(charBuffer, codePatch.getEndOffset(), res, codePatch.getStartOffset()+patch.length, charBuffer.length - codePatch.getEndOffset());
            }
            return res;
        }

        @Override
        public BufferType getType() {
            return delegate.getType();
        }

    }

            }
