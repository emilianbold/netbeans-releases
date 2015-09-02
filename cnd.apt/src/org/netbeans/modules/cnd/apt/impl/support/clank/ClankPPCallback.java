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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.clang.basic.FileEntry;
import org.clang.basic.IdentifierInfo;
import org.clang.basic.SourceManager;
import org.clang.basic.SrcMgr;
import org.clang.lex.DefMacroDirective;
import org.clang.lex.MacroDirective;
import org.clang.lex.MacroInfo;
import org.clang.lex.Preprocessor;
import org.clang.lex.Token;
import org.clang.tools.services.support.FileInfo;
import org.clang.tools.services.support.Interrupter;
import org.clang.tools.services.support.FileInfoCallback;
import org.clank.java.std;
import static org.clank.java.std.$second_uint;
import org.clank.support.Casts;
import static org.clank.support.Casts.toJavaString;
import org.clank.support.Native;
import org.clank.support.NativePointer;
import org.clank.support.Unsigned;
import org.clank.support.aliases.char$ptr;
import org.llvm.adt.SmallString;
import org.llvm.adt.StringRef;
import org.llvm.adt.aliases.SmallVector;
import org.llvm.adt.aliases.SmallVectorImplChar;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankDriverImpl.ArrayBasedAPTTokenStream;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankMacroDirective;
import org.netbeans.modules.cnd.apt.support.ClankDriver.FileGuard;
import org.netbeans.modules.cnd.apt.support.ClankDriver.MacroExpansion;
import org.netbeans.modules.cnd.apt.support.ClankDriver.MacroUsage;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ClankPPCallback extends FileInfoCallback {

    public static final class CancellableInterrupter implements Interrupter {

        final org.netbeans.modules.cnd.support.Interrupter outerDelegate;
        private boolean cancelledState = false;

        public CancellableInterrupter(org.netbeans.modules.cnd.support.Interrupter outerDelegate) {
            this.outerDelegate = outerDelegate;
        }

        @Override
        public boolean isCancelled() {
            return cancelledState || outerDelegate.cancelled();
        }

        private void cancel() {
            cancelledState = true;
        }

    }

    private final ClankDriver.ClankPreprocessorCallback delegate;
    private final PreprocHandler ppHandler;
    private final ClankIncludeHandlerImpl includeHandler;
    private final ArrayList<ClankFileInfoWrapper> includeStack = new ArrayList<ClankFileInfoWrapper>(16);
    private final ArrayList<Integer> includeHelperStack = new ArrayList<Integer>(16);
    private final CancellableInterrupter interrupter;
    private final SmallString tokenSpellBuffer = new SmallString(1024);

    public ClankPPCallback(PreprocHandler ppHandler,
            raw_ostream traceOS,
            ClankDriver.ClankPreprocessorCallback delegate,
            ClankPPCallback.CancellableInterrupter interrupter) {
        super(traceOS);
        this.ppHandler = ppHandler;
        this.includeHandler = (ClankIncludeHandlerImpl)ppHandler.getIncludeHandler();
        // reset include stack;
        // will be regenerated from scratch using onEnter/onExit
        this.includeHandler.resetIncludeStack();
        this.delegate = delegate;
        this.interrupter = interrupter;
    }

    @Override
    protected void onUserDiagnosticDirective(FileInfo curStackElement, UserDiagnosticDirectiveInfo directive) {
        if (!directive.isWarning()) {
            PreprocHandler.State stateWhenMetErrorDirective = APTHandlersSupport.createCleanPreprocState(this.ppHandler.getState());
            ClankErrorDirectiveWrapper errorDirectiveWrapper = new ClankErrorDirectiveWrapper(directive, stateWhenMetErrorDirective);
            directive.setAnnotation(errorDirectiveWrapper);
            ClankFileInfoWrapper currentFileWrapper = includeStack.get(includeStack.size() - 1);
            this.delegate.onErrorDirective(currentFileWrapper, errorDirectiveWrapper);
        }
    }

    @Override
    protected void onInclusionDirective(FileInfo curFile, InclusionDirectiveInfo directive) {
        assert includeHelperStack.size() == includeStack.size();
        final int stacksSize = includeStack.size();
        // find ResolvedPath for #include
        ResolvedPath resolvedPath = createResolvedPath(getPreprocessor(), directive);
        StringRef fileNameSpelling = directive.getFileNameSpelling();
        String spelling = Casts.toCharSequence(fileNameSpelling.data(), fileNameSpelling.size()).toString();
        ClankInclusionDirectiveWrapper inclDirectiveWrapper = new ClankInclusionDirectiveWrapper(directive
                                                                                                ,resolvedPath
                                                                                                ,spelling);
        // keep it as annotation 
        directive.setAnnotation(inclDirectiveWrapper);
        ClankFileInfoWrapper currentFileWrapper = includeStack.get(stacksSize - 1);
        includeHelperStack.set(stacksSize - 1, includeHelperStack.get(stacksSize - 1) + 1);
        assert currentFileWrapper.current == curFile || !curFile.isFile();
        if (resolvedPath == null) {
            if (DebugUtils.STANDALONE) {
                if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                    System.err.println("FAILED INCLUDE: from " + CndPathUtilities.getBaseName(currentFileWrapper.getFilePath().toString()) + " for:\n\t" + spelling);// NOI18N
                }
            } else {
                APTUtils.LOG.log(Level.WARNING,
                        "failed resolving path from {0} for {1}", // NOI18N
                        new Object[]{currentFileWrapper.getFilePath(), spelling});
            }
        }
        this.delegate.onInclusionDirective(currentFileWrapper, inclDirectiveWrapper);
    }
    
    @Override
    protected void onDeepInclusion() {
        ClankFileInfoWrapper fileInfo = findRecursiveInclusion(includeStack);
        if (fileInfo != null && fileInfo.getInclusionDirective() != null) {
            CharSequence recursivePath = fileInfo.getFilePath();
            ClankDriver.ClankInclusionDirective recursiveInclusionDirective = fileInfo.getInclusionDirective();
            for (ClankFileInfoWrapper file : includeStack) {
                ClankDriver.ClankInclusionDirective fileInclusionDirective = file.getInclusionDirective();
                if (Objects.equals(file.getFilePath(), recursivePath) 
                    && fileInclusionDirective != null 
                    && Objects.equals(fileInclusionDirective.getResolvedPath().getPath(), recursiveInclusionDirective.getResolvedPath().getPath()) 
                    && fileInclusionDirective.getDirectiveStartOffset() == recursiveInclusionDirective.getDirectiveStartOffset())
                {
                    if (fileInclusionDirective instanceof ClankInclusionDirectiveWrapper) {
                        ClankInclusionDirectiveWrapper mutableDirective = (ClankInclusionDirectiveWrapper) fileInclusionDirective;
                        mutableDirective.setRecursive(true);
                    }
                }
            }
        }
    }

    private ResolvedPath createResolvedPath(Preprocessor PP, InclusionDirectiveInfo directive) {
        FileEntry fileEntry = directive.getFileEntry();
        if (fileEntry == null) {
            // unresolved #include
            return null;
        }
        FileSystem fs;
        String searchPath = Casts.toCharSequence(directive.getSearchPath().data(), directive.getSearchPath().size()).toString();
        if (searchPath.startsWith(ClankFileSystemProviderImpl.RFS_PREFIX)) {            
            FileObject fo = CndFileSystemProvider.urlToFileObject(searchPath); //TODO: optimize!
            searchPath = fo.getPath();
            try {
                fs = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                fs = CndFileSystemProvider.urlToFileSystem(searchPath);
                Exceptions.printStackTrace(ex); // should never be the case
            }
        } else {
            fs = includeHandler.getStartEntry().getFileSystem();
        }
        char$ptr fleEntryName = fileEntry.getName();
        String strFileEntryPath = Casts.toCharSequence(fleEntryName).toString();
        strFileEntryPath = CndFileUtils.normalizeAbsolutePath(fs, strFileEntryPath);
        if (directive.getSearchPath().empty()) {
            // was resolved as absolute path (i.e -include directive)
            CharSequence parent = CndPathUtilities.getDirName(strFileEntryPath);
            parent = ClankFileSystemProviderImpl.getPathFromUrl(parent);
            return new ResolvedPath(fs, FilePathCache.getManager().getString(parent), strFileEntryPath, false, 0);
        } else {
            CharSequence pathCharSeq = ClankFileSystemProviderImpl.getPathFromUrl(strFileEntryPath);
            pathCharSeq = CharSequences.create(pathCharSeq);
            SrcMgr.CharacteristicKind fileDirFlavor = PP.getHeaderSearchInfo().getFileDirFlavor(fileEntry);            
            assert CndPathUtilities.isPathAbsolute(searchPath) : "expected to be abs path [" + searchPath + "]";
            CharSequence folder = CndFileUtils.normalizeAbsolutePath(fs, searchPath);
            folder = ClankFileSystemProviderImpl.getPathFromUrl(folder);
            folder = FilePathCache.getManager().getString(CharSequences.create(folder));
            // FIXME: for now consider user path as isDefaultSearchPath
            boolean isDefaultSearchPath = (fileDirFlavor == SrcMgr.CharacteristicKind.C_User);
            return new ResolvedPath(fs, folder, pathCharSeq, isDefaultSearchPath, 0);
        }
    }

    @Override
    protected void onSkippedInclusionDirective(FileInfo curFile, InclusionDirectiveInfo directive) {

    }

    @Override
    protected boolean onNotFoundInclusionDirective(FileInfo curFile, StringRef FileName, SmallVectorImplChar RecoveryPath) {
        APTFileSearch fileSearch = includeHandler.getFileSearch();
        if (fileSearch != null) {
            char$ptr curFilePath = curFile.getName();
            String headerPath = fileSearch.searchInclude(Native.$toString(FileName.data(), FileName.size()), Native.$toString(curFilePath));
            if (headerPath != null) {
                headerPath = CndPathUtilities.getDirName(headerPath);
                if (headerPath == null) {
                    headerPath = "/"; //NOI18N
                }
                final char$ptr charPtr = NativePointer.create_char$ptr(headerPath);
                RecoveryPath.assign(charPtr, charPtr.$add(headerPath.length()));
                return true;
            }
        }
        return super.onNotFoundInclusionDirective(curFile, FileName, RecoveryPath);
    }

    @Override
    protected void onEnter(FileInfo enteredFrom, FileInfo enteredTo) {
        if (ClankDriverImpl.TRACE) {
            traceOS.$out("Enter: " + enteredTo).$out("\n").flush(); // NOI18N
        }
        // sometimes we enter and leave some built-in buffers
        // usually it is predefines or cmd line options seen by main file
        // we'd prefer to stay in main file context
        if (enteredTo.isFile()) {
            ClankDriver.ClankFileInfo enteredFromWrapper;
            ClankFileInfoWrapper enteredToWrapper = new ClankFileInfoWrapper(enteredTo, ppHandler);
            // main file is not pushed as include, all others are
            if (includeStack.isEmpty()) {
//                assert includeHandler.getStartEntry().getStartFile().toString().contentEquals(Casts.toCharSequence(enteredTo.getName())) :
//                        includeHandler.getStartEntry() + " vs. " + enteredTo; // NOI18N
                if (CndUtils.isDebugMode()) {
                    CharSequence startUrl;
                    if (APTTraceFlags.USE_CLANK) {
                        startUrl = CndFileSystemProvider.toUrl(includeHandler.getStartEntry().getFileSystem(), includeHandler.getStartEntry().getStartFile());
                    } else {
                        startUrl = includeHandler.getStartEntry().getStartFile();
                    }
                    CndUtils.assertPathsEqualInConsole(startUrl, Casts.toCharSequence(enteredTo.getName()), "{0} vs. {1}", //NOI18N
                            includeHandler.getStartEntry(), enteredTo);
                }
                assert includeHandler.getInclStackIndex() == 0 : " expected zero: " + includeHandler.getInclStackIndex();
                assert enteredToWrapper.getFileIndex() == 0 : " expected zero: " + enteredToWrapper.getFileIndex();
                enteredFromWrapper = null;
            } else {
                ResolvedPath resolvedPath = enteredToWrapper.getResolvedPath();
                includeHandler.pushInclude(resolvedPath.getFileSystem(), resolvedPath.getPath(),
                        0/*should not be used by client*/, enteredTo.getIncludeStartOffset(), resolvedPath.getIndex(),
                        includeHelperStack.get(includeHelperStack.size() - 1));
                includeHandler.cacheTokens(enteredToWrapper);
                enteredFromWrapper = includeStack.get(includeStack.size() - 1);
            }
            // keep stack of active files
            includeStack.add(enteredToWrapper);
            includeHelperStack.add(0);

            delegate.onEnter(enteredFromWrapper, enteredToWrapper);
        } else {
            assert includeStack.size() == 1 : "there should be only one main file";
            assert includeStack.get(0).current.isMainFile() : "there should be only main file";
        }
    }

    @Override
    protected void onExit(FileInfo exitedFrom, FileInfo exitedTo) {
        if (ClankDriverImpl.TRACE) {
            traceOS.$out("Exit from "); // NOI18N
            if (exitedFrom.isFile()) {
                traceOS.$out(exitedFrom.getName());
            } else {
                traceOS.$out(exitedFrom.getFileID());
            }
            traceOS.$out(" with #Token: ").$out(exitedFrom.getNrTokens()).$out("\n"); // NOI18N
            int[] offs = exitedFrom.getSkippedRanges();
            if (offs.length > 0) {
                for (int i = 0; i < offs.length; i += 2) {
                    int st = offs[i];
                    int end = offs[i + 1];
                    traceOS.$out("[").$out(st).$out("-").$out(end).$out("] "); // NOI18N
                }
                traceOS.$out("\n"); // NOI18N
            }
            traceOS.flush();
        }
        // sometimes we enter and leave some built-in buffers
        // usually it is predefines or cmd line options seen by main file
        // we'd prefer to stay in main file context
        if (exitedFrom.isFile()) {
            assert includeStack.size() > 0 : "empty include stack?";
            ClankDriver.ClankFileInfo exitedToWrapper;
            ClankFileInfoWrapper exitedFromWrapper = includeStack.remove(includeStack.size() - 1);
            includeHelperStack.remove(includeHelperStack.size() - 1);
            assert exitedFromWrapper.current == exitedFrom;
            // we cache possibly collected tokens in include handler
            // to allow delegate to use them
            exitedFromWrapper.exited();
            includeHandler.cacheTokens(exitedFromWrapper);
            // init where we returned to
            if (includeStack.isEmpty()) {
                exitedToWrapper = null;
            } else {
                exitedToWrapper = includeStack.get(includeStack.size() - 1);
            }

            // ask if delegate wish to continue
            if (!delegate.onExit(exitedFromWrapper, exitedToWrapper)) {
                interrupter.cancel();
            }
            if (exitedToWrapper != null) {
                includeHandler.popInclude();
            }
        } else {
            assert includeStack.size() == 1 : "there should be only one main file";
            assert includeStack.get(0).current.isMainFile() : "there should be only main file";
        }
    }

    @Override
    protected boolean needPPDirectives() {
        return delegate.needPPDirectives();
    }

    @Override
    protected boolean needTokens() {
        return delegate.needTokens();
    }

    @Override
    protected boolean needSkippedRanges() {
        return delegate.needSkippedRanges();
    }

    @Override
    protected boolean needMacroExpansion() {
        return delegate.needMacroExpansion();
    }
    
    private static ClankFileInfoWrapper findRecursiveInclusion(ArrayList<ClankFileInfoWrapper> stack) {
        if (!stack.isEmpty()) {
            ClankFileInfoWrapper best = null;
            int bestFrequency = 0;
            Map<String, Pair<ClankFileInfoWrapper, Integer>> mapping = new HashMap<String, Pair<ClankFileInfoWrapper, Integer>>();
            for (ClankFileInfoWrapper fileInfo : stack) {
                String path = fileInfo.getFilePath().toString();
                Pair<ClankFileInfoWrapper, Integer> pair = mapping.get(path);
                if (pair == null) {
                    pair = Pair.of(fileInfo, 1);
                } else {
                    pair = Pair.of(pair.first(), pair.second() + 1);
                }
                mapping.put(path, pair);
                if (bestFrequency <= pair.second()) {
                    best = pair.first();
                    bestFrequency = pair.second();
                }
            }
            return best;
        }
        return null;
    }

    private static abstract class ClankPreprocessorDirectiveWrapper implements ClankDriver.ClankPreprocessorDirective {

        private Object externalAnnotation;
        private final int startOffset;
        private final int endOffset;

        public ClankPreprocessorDirectiveWrapper(PreprocessorDirectiveInfo delegate) {
            this(delegate.getHashOffset(), delegate.getEodOffset());
        }

        public ClankPreprocessorDirectiveWrapper(int start, int end) {
            this.startOffset = start;
            this.endOffset = end;
        }

        @Override
        public int getDirectiveStartOffset() {
            return this.startOffset;
        }

        @Override
        public int getDirectiveEndOffset() {
            return this.endOffset;
        }

        @Override
        public void setAnnotation(Object annotation) {
            assert externalAnnotation == null : "replacing? " + externalAnnotation;
            this.externalAnnotation = annotation;
        }

        @Override
        public Object getAnnotation() {
            return externalAnnotation;
        }

        @Override
        public String toString() {
            return "[" + startOffset + "-" + endOffset + "] " + "annotation=" + externalAnnotation; // NOI18N
        }
    }

    private static final class ClankMacroDirectiveWrapper extends ClankPreprocessorDirectiveWrapper implements ClankMacroDirective {

        private final List<CharSequence> params;
        private final CharSequence macroName;
        private final boolean isDefined;
        private final /*SourceLocation*/int macroNameTokenSourceLocation;
        private final int macroNameOffset;
        private final CharSequence fileOwnerName; // lazy field based on fileOwner
        private final boolean isBuiltIn;

        static ClankMacroDirectiveWrapper create(DefMacroDirective MD, SourceManager SM) {
            MacroInfo macroInfo = MD.getMacroInfo();
            final int hashLoc = MD.getHashLoc();
            long/*<FileID, uint>*/ decomposedLocBegin = SM.getDecomposedLoc(hashLoc);
            final int eodLoc = MD.getEodLoc();
            long/*<FileID, uint>*/ decomposedLocEnd = SM.getDecomposedLoc(eodLoc);
            long begOffset = $second_uint(decomposedLocBegin);
            long endOffset = $second_uint(decomposedLocEnd);
            List<CharSequence> params = null;
            boolean isVariadic = false;
            if (macroInfo.isFunctionLike()) {
                IdentifierInfo[] args = null;
                args = macroInfo.$ArgumentList();
                if (args != null) { // the following macro will return null arguments: #define XXX() __xxx()
                    isVariadic = macroInfo.isVariadic();
                    params = new ArrayList<CharSequence>(args.length);
                    for (IdentifierInfo arg : args) {
                        CharSequence argName = ClankToAPTUtils.getIdentifierText(arg);
                        params.add(argName);
                    }
                    if (isVariadic) {
                        assert params.size() > 0;
                        params.set(params.size() - 1, APTUtils.VA_ARGS_TOKEN.getTextID());
                    }
                }
            }
            CharSequence strName = MD.getName();
            if (strName == null) {
                strName = "???"; //NOI18N
                CndUtils.assertTrueInConsole(false, "Null macro name " + MD);
            }
            char$ptr bufferName = SM.getBufferName(macroInfo.getDefinitionLoc());
            return new ClankMacroDirectiveWrapper(strName, params, bufferName, begOffset, endOffset);
        }

        public ClankMacroDirectiveWrapper(CharSequence name,
                List<CharSequence> params, char$ptr bufferName, long begOffset, long endOffset) {
            super(Unsigned.long2uint(begOffset), Unsigned.long2uint(endOffset));
            this.params = params;
            this.macroName = name;
            this.isDefined = true;
            this.macroNameTokenSourceLocation = -1;
            macroNameOffset = Unsigned.long2uint(begOffset);
            if (std.strcmp(bufferName, "<built-in>") == 0) { // NOI18N
                // predefined system or user macros
                this.fileOwnerName = null;
                isBuiltIn = true;
            } else if (std.strcmp(bufferName, "<invalid loc>") == 0) { // NOI18N
                // context dependent macros (__FILE__, __LINE__)
                // TODO: separate from other built-in macros
                this.fileOwnerName = null;
                isBuiltIn = true;
            } else if (std.strcmp(bufferName, "Unknown buffer") == 0) { // NOI18N
                assert false : "Unknown location of macro definition "+macroName;
                this.fileOwnerName = null;
                isBuiltIn = true;
            } else {
                this.fileOwnerName = getFileOwnerNameImpl(bufferName);
                if (!Utilities.isWindows() && CharSequences.indexOf(fileOwnerName, "/") < 0) { // NOI18N
                    CndUtils.assertTrueInConsole(false, "Strange fileOwnerName: '" + fileOwnerName + "");
                }
                isBuiltIn = false;
            }
        }

        public ClankMacroDirectiveWrapper(CharSequence macroName,
                List<CharSequence> params, MacroDirectiveInfo clankDelegate, Token macroNameToken) {
            super(clankDelegate);
            this.params = params;
            this.macroName = macroName;
            this.isDefined = clankDelegate.isDefined();
            this.macroNameTokenSourceLocation = macroNameToken.getRawLocation();
            macroNameOffset = clankDelegate.getMacroNameOffset();
            FileInfo fileOwner = clankDelegate.getFileOwner();
            if (fileOwner.isFile()) {
                this.isBuiltIn = false;
                this.fileOwnerName = getFileOwnerNameImpl(fileOwner.getName());
            } else {
                this.isBuiltIn = true;
                this.fileOwnerName = null;
            }
        }

        @Override
        public CharSequence getFile() {
            return isBuiltIn ? BUILD_IN_FILE : fileOwnerName;
        }

        public boolean isBuiltIn() {
            return isBuiltIn;
        }

        @Override
        public boolean isDefined() {
            return this.isDefined;
        }

        @Override
        public CharSequence getMacroName() {
            return this.macroName;
        }

        @Override
        public int getMacroNameOffset() {
            return macroNameOffset;
        }

        @Override
        public List<CharSequence> getParameters() {
            return this.params == null ? null : Collections.unmodifiableList(this.params);
        }
        
        @Override
        public /*SourceLocation*/int getMacroNameLocation() {
            return macroNameTokenSourceLocation;
        }

        @Override
        public String toString() {
            return super.toString() + this.fileOwnerName + (this.isDefined ? " #define " : " #undef ") + this.macroName + // NOI18N
                    (this.params == null ? "" : ("(" + this.params + ")")); // NOI18N
        }

        private static CharSequence getFileOwnerNameImpl(char$ptr fileOwner) {
            CharSequence res = ClankFileSystemProviderImpl.getPathFromUrl(toJavaString(fileOwner));
            if (CharSequenceUtils.startsWith(res, ClankFileSystemProviderImpl.RFS_PREFIX)) {
                Exceptions.printStackTrace(new IllegalArgumentException("File owner name should not contain protocol: " + res)); //NOI18N
               }
            return res;
           }
        }

    private static final class ClankErrorDirectiveWrapper extends ClankPreprocessorDirectiveWrapper implements ClankDriver.ClankErrorDirective {

        private final CharSequence msg;
        private final PreprocHandler.State stateWhenMetErrorDirective;

        public ClankErrorDirectiveWrapper(UserDiagnosticDirectiveInfo clankDelegate,
                PreprocHandler.State stateWhenMetErrorDirective) {
            super(clankDelegate);
            assert stateWhenMetErrorDirective != null;
            this.stateWhenMetErrorDirective = stateWhenMetErrorDirective;
            StringRef message = clankDelegate.getMessage();
            String spelling = Casts.toCharSequence(message.data(), message.size()).toString();
            this.msg = spelling;
        }

        @Override
        public CharSequence getMessage() {
            return this.msg;
        }

        @Override
        public PreprocHandler.State getStateWhenMetErrorDirective() {
            return this.stateWhenMetErrorDirective;
        }

        @Override
        public String toString() {
            return "ClankErrorDirectiveWrapper{" + super.toString() + ",\n" // NOI18N
                    + " msg=" + msg + '}'; // NOI18N
        }
    }

    private static final class ClankInclusionDirectiveWrapper extends ClankPreprocessorDirectiveWrapper implements ClankDriver.ClankInclusionDirective {

        private final ResolvedPath resolvedPath;
        private final String spelling;
        private final boolean isAngled;
        private boolean recursive;

        public ClankInclusionDirectiveWrapper(InclusionDirectiveInfo clankDelegate, ResolvedPath resolvedPath, String spelling) {
            super(clankDelegate);
            this.isAngled = clankDelegate.isAngled();
            this.resolvedPath = resolvedPath;
            this.spelling = spelling;
            this.recursive = false;
        }

        @Override
        public ResolvedPath getResolvedPath() {
            return resolvedPath;
        }

        @Override
        public String getSpellingName() {
            return spelling;
        }

        @Override
        public boolean isAngled() {
            return isAngled;
        }

        @Override
        public boolean isRecursive() {
            return recursive;
        }
        
        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }

        @Override
        public String toString() {
            return "ClankInclusionDirective{\n" + super.toString() + ",\n" // NOI18N
                    + "resolvedPath=" + resolvedPath + ",\n" // NOI18N
                    + "spelling=" + spelling + ",\n" // NOI18N
                    + '}'; // NOI18N
        }

    }

    private static final class ClankFileInfoWrapper implements ClankDriver.ClankFileInfo, ClankDriverImpl.APTTokenStreamCacheImplementation {

        private final boolean needLineColumnsForToken;
        private final FileInfo current;
        private final ClankInclusionDirectiveWrapper includeDirective;
        private final CharSequence filePath;
        private final int includeIndex;
        private APTToken[] convertedTokens;
        private List<ClankDriver.ClankPreprocessorDirective> convertedPPDirectives;
        private List<MacroExpansion> convertedMacroExpansions;
        private List<MacroUsage> convertedMacroUsages;
        private FileGuard convertedGuard;
        private boolean hasTokenStream = false;
        private int[] skippedRanges = null;
        private boolean convertedToAPT = false;

        public ClankFileInfoWrapper(FileInfo current,
                PreprocHandler ppHandler) {
            assert current != null;
            this.needLineColumnsForToken = APTToClankCompilationDB.isFortran(ppHandler);
            this.current = current;
            this.includeIndex = current.getIncludeIndex();
            if (current.getInclusionDirective() == null) {
                assert current.isMainFile() : "forgot to set up include?" + current;
                this.includeDirective = null;
                this.filePath = toJavaString(current.getName());
            } else {
                this.includeDirective = (ClankInclusionDirectiveWrapper)current.getInclusionDirective().getAnnotation();
                assert this.includeDirective != null : "forgot to set up include?" + current;
                assert this.includeDirective.getResolvedPath() != null;
                this.filePath = this.includeDirective.getResolvedPath().getPath();
            }
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public APTToken[] getConvertedTokens() {
            assert convertedToAPT : "was not prepared yet";
            assert (convertedTokens != null);
            return convertedTokens;
        }

        private void prepareConvertedMacroExpansions() {
            Map<MacroDirective, ClankMacroDirective> directives = new HashMap<MacroDirective, ClankMacroDirective>();
            {
                SmallVector<FileInfoCallback.MacroExpansionInfo> macroExpansions = current.getMacroExpansions();
                int size = macroExpansions.size();
                Object[] expansions = macroExpansions.$array();
                convertedMacroExpansions = new ArrayList<MacroExpansion>(size);
                for (int i = 0; i < size; i++) {
                    FileInfoCallback.MacroExpansionInfo e = (FileInfoCallback.MacroExpansionInfo)expansions[i];
                    MacroDirective MD = e.getReferencedMacroDirective();
                    ClankMacroDirective referencedMacro = directives.get(MD);
                    if (referencedMacro == null) {
                        if (MD instanceof DefMacroDirective) {
                            referencedMacro = ClankMacroDirectiveWrapper.create((DefMacroDirective) MD, current.getSourceManager());
                            directives.put(MD, referencedMacro);
                        } else {
                           CndUtils.assertTrueInConsole(false, "Should be DefMacroDirective: " + MD);
                           continue;
                        }
                    }
                    MacroExpansion macroExpansion = new MacroExpansion(e, referencedMacro);
                    convertedMacroExpansions.add(macroExpansion);
                }
            }
            {
                SmallVector<FileInfoCallback.MacroUsageInfo> macroUsages = current.getMacroUsages();
                int size = macroUsages.size();
                Object[] expansions = macroUsages.$array();
                convertedMacroUsages = new ArrayList<MacroUsage>(size);
                for (int i = 0; i < size; i++) {
                    FileInfoCallback.MacroUsageInfo e = (FileInfoCallback.MacroUsageInfo)expansions[i];
                    MacroDirective MD = e.getReferencedMacroDirective();
                    ClankMacroDirective referencedMacro = directives.get(MD);
                    if (referencedMacro == null) {
                        if (MD instanceof DefMacroDirective) {
                            referencedMacro = ClankMacroDirectiveWrapper.create((DefMacroDirective) MD, current.getSourceManager());
                            directives.put(MD, referencedMacro);
                        } else {
                           CndUtils.assertTrueInConsole(false, "Should be DefMacroDirective: " + MD);
                           continue;
                        }
                    }                    
                    MacroUsage macroUsage = new MacroUsage(e, referencedMacro);
                    convertedMacroUsages.add(macroUsage);
                }
            }
        }
        
        private void prepareConvertedGuard() {
            SmallVector<FileGuardInfo> guards = current.getFileGuardsInfo();
            if (guards != null) {
                assert !guards.empty();
                // TODO: use the last for now
                FileGuardInfo fileGuardInfo = guards.$at(guards.size()-1);
                SourceManager srcMgr = current.getSourceManager();
                int start = Unsigned.long2uint($second_uint(srcMgr.getDecomposedLoc(fileGuardInfo.getIfDefMacroLocation())));
                convertedGuard = new FileGuard(start, start+fileGuardInfo.getIfDefMacro().getLength());
            }
        }

        private void prepareConvertedPPDirectives() {
            assert Thread.holdsLock(this);
            SmallVector<PreprocessorDirectiveInfo> ppDirectives = current.getPreprocessorDirectives();
            Object[] directives = ppDirectives.$array();
            int nrDirectives = ppDirectives.size();
            assert this.convertedPPDirectives == null;
            this.convertedPPDirectives = new ArrayList<ClankDriver.ClankPreprocessorDirective>(nrDirectives);
            SmallString spell = new SmallString(1024);
            Preprocessor PP = current.getPreprocessor();
            for (int i = 0; i < nrDirectives; i++) {
                PreprocessorDirectiveInfo curDirective = (PreprocessorDirectiveInfo)directives[i];
                if (curDirective instanceof InclusionDirectiveInfo) {
                    ClankInclusionDirectiveWrapper wrapper = (ClankInclusionDirectiveWrapper)curDirective.getAnnotation();
                    assert wrapper != null;
                    this.convertedPPDirectives.add(wrapper);
                } else if (curDirective instanceof UserDiagnosticDirectiveInfo) {
                    if (!((UserDiagnosticDirectiveInfo)curDirective).isWarning()) {
                        // old model tracked only #error, not #warning
                        ClankErrorDirectiveWrapper wrapper = (ClankErrorDirectiveWrapper)curDirective.getAnnotation();
                        assert wrapper != null;
                        this.convertedPPDirectives.add(wrapper);
                    }
                } else if (curDirective instanceof MacroDirectiveInfo) {
                    MacroDirectiveInfo macroDirective = (MacroDirectiveInfo)curDirective;
                    if (macroDirective.isDefined()) {
                        // old model tracked only #define and not #undef
                        CharSequence macroName = ClankToAPTUtils.getTokenText(macroDirective.getMacroNameToken(), PP, spell);
                        List<CharSequence> params = null;
                        if (macroDirective.isFunctionLike()) {
                            IdentifierInfo[] arguments = macroDirective.getArguments();
                            params = new ArrayList<CharSequence>(arguments.length);
                            for (IdentifierInfo arg : arguments) {
                                CharSequence argName = ClankToAPTUtils.getIdentifierText(arg);
                                params.add(argName);
                            }
                            if (macroDirective.isVariadic()) {
                                // replace the last param name by variadic marker
                                assert params.size() > 0;
                                params.set(params.size() - 1, APTUtils.VA_ARGS_TOKEN.getTextID());
                            }
                        }
                        ClankMacroDirectiveWrapper wrapper = new ClankMacroDirectiveWrapper(macroName, params, macroDirective, macroDirective.getMacroNameToken());
                        this.convertedPPDirectives.add(wrapper);
                    }
                }
            }
        }

        private void prepareConvertedTokensIfAny() {
            assert Thread.holdsLock(this);
            if (current.hasTokens()) {
                convertedTokens = ClankToAPTToken.convertToAPT(current.getPreprocessor(), current.getTokens(), needLineColumnsForToken);
            }
        }

        @Override
        public CharSequence getFilePath() {
            return ClankFileSystemProviderImpl.getPathFromUrl(filePath);
        }

        @Override
        public Collection<ClankDriver.ClankPreprocessorDirective> getPreprocessorDirectives() {
            prepareCachesIfPossible();
            return Collections.unmodifiableList(convertedPPDirectives);
        }

        @Override
        public Collection<MacroExpansion> getMacroExpansions() {
            prepareCachesIfPossible();
            return Collections.unmodifiableList(convertedMacroExpansions);
        }

        @Override
        public Collection<MacroUsage> getMacroUsages() {
            prepareCachesIfPossible();
            return Collections.unmodifiableList(convertedMacroUsages);
        }

        @Override
        public FileGuard getFileGuard() {
            prepareCachesIfPossible();
            return convertedGuard;
        }

        @Override
        public TokenStream getTokenStream() {
            return new ArrayBasedAPTTokenStream(getConvertedTokens());
        }

        @Override
        public int getFileIndex() {
            return includeIndex;
        }

        @Override
        public ClankDriver.ClankInclusionDirective getInclusionDirective() {
            return includeDirective;
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public int[] getSkippedRanges() {
            return skippedRanges;
        }

        @Override
        public boolean hasTokenStream() {
            return hasTokenStream;
        }

        @Override
        public synchronized ClankDriverImpl.APTTokenStreamCacheImplementation prepareCachesIfPossible() {
            prepareCaches();
            return this;
        }

        private void prepareCaches() {
            if (!convertedToAPT) {
                prepareConvertedTokensIfAny();
                prepareConvertedPPDirectives();
                prepareConvertedMacroExpansions();
                prepareConvertedGuard();
                convertedToAPT = true;
            }
        }

        @Override
        public String toString() {
            return "ClankFileInfoImpl{" + "convertedToAPT=" + convertedToAPT + ";"// NOI18N
                    + " hasTokenStream=" + hasTokenStream + ", current=" + current + ",\n"// NOI18N
                    + "currentInclude=" + includeDirective + '}';// NOI18N
        }

        private ResolvedPath getResolvedPath() {
            assert includeDirective != null;
            return includeDirective.getResolvedPath();
        }

        private void exited() {
            hasTokenStream = current.hasTokens();
            skippedRanges = current.getSkippedRanges();
        }
    }
}
