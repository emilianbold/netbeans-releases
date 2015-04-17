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
import org.clang.basic.SrcMgr;
import org.clang.tools.services.support.Interrupter;
import org.clang.tools.services.support.FileInfoCallback;
import org.clank.support.Casts;
import org.clank.support.Native;
import org.clank.support.NativePointer;
import org.clank.support.aliases.char$ptr;
import org.llvm.adt.StringRef;
import org.llvm.adt.aliases.SmallVectorImplChar;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankDriverImpl.ArrayBasedAPTTokenStream;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

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
    private final ArrayList<ClankFileInfoImpl> includeStack = new ArrayList<ClankFileInfoImpl>(16);
    private final CancellableInterrupter interrupter;

    public ClankPPCallback(PreprocHandler ppHandler, 
            raw_ostream traceOS, 
            ClankDriver.ClankPreprocessorCallback delegate,
            ClankPPCallback.CancellableInterrupter interrupter) {
        super(traceOS);
        this.ppHandler = ppHandler;
        this.includeHandler = (ClankIncludeHandlerImpl) ppHandler.getIncludeHandler();
        // reset include stack;
        // will be regenerated from scratch using onEnter/onExit
        this.includeHandler.resetIncludeStack();
        this.delegate = delegate;
        this.interrupter = interrupter;
    }

    @Override
    protected void onInclusionDirective(FileInfo curFile, InclusionDirectiveInfo directive) {
        
    }

    @Override
    protected void onSkippedInclusionDirective(FileInfo curFile, InclusionDirectiveInfo directive) {
      
    }

    @Override
    protected boolean onNotFoundInclusionDirective(FileInfoCallback.FileInfo curFile, StringRef FileName, SmallVectorImplChar RecoveryPath) {
        APTFileSearch fileSearch = includeHandler.getStartEntry().getFileSearch();
        if (fileSearch != null) {
            String headerPath = fileSearch.searchInclude(
                    Native.$toString(FileName.data(), FileName.size()), Native.$toString(curFile.getName()));
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
    protected void onEnter(FileInfoCallback.FileInfo enteredFrom, FileInfoCallback.FileInfo enteredTo) {
        if (ClankDriverImpl.TRACE) {
            traceOS.$out("Enter: " + enteredTo).$out("\n").flush();
        }
        if (enteredTo.isFile()) {
          ClankDriver.ClankFileInfo enteredFromWrapper;
          ClankFileInfoImpl enteredToWrapper = new ClankFileInfoImpl(enteredTo, ppHandler);
          // main file is not pushed as include, all others are
          if (includeStack.isEmpty()) {
            assert includeHandler.getStartEntry().getStartFile().toString().contentEquals(Casts.toCharSequence(enteredTo.getName())) :
                    includeHandler.getStartEntry() + " vs. " + enteredTo; // NOI18N
            assert includeHandler.getInclStackIndex() == 0 : " expected zero: " + includeHandler.getInclStackIndex();
            assert enteredToWrapper.getFileIndex() == 0 : " expected zero: " + enteredToWrapper.getFileIndex();
            enteredFromWrapper = null;
          } else {
            ResolvedPath resolvedPath = enteredToWrapper.getResolvedPath();
            includeHandler.pushInclude(resolvedPath.getFileSystem(), resolvedPath.getPath(),
                    0/*should not be used by client*/, enteredTo.getIncludeStartOffset(), resolvedPath.getIndex());
            includeHandler.cacheTokens(enteredToWrapper);
            enteredFromWrapper = includeStack.get(includeStack.size() - 1);
          }
          // keep stack of active files
          includeStack.add(enteredToWrapper);
          
          delegate.onEnter(enteredFromWrapper, enteredToWrapper);
        }
    }

    @Override
    protected void onExit(FileInfoCallback.FileInfo exitedFrom, FileInfoCallback.FileInfo exitedTo) {
        if (ClankDriverImpl.TRACE) {
            traceOS.$out("Exit from ");
            if (exitedFrom.isFile()) {
                traceOS.$out(exitedFrom.getName());
            } else {
                traceOS.$out(exitedFrom.getFileID());
            }
            traceOS.$out(" with #Token: ").$out(exitedFrom.getNrTokens()).$out("\n");
            int[] offs = exitedFrom.getSkippedRanges();
            if (offs.length > 0) {
                for (int i = 0; i < offs.length; i += 2) {
                    int st = offs[i];
                    int end = offs[i + 1];
                    traceOS.$out("[").$out(st).$out("-").$out(end).$out("] ");
                }
                traceOS.$out("\n");
            }
            traceOS.flush();
        }
        if (exitedFrom.isFile()) {
          assert includeStack.size() > 0 : "empty include stack?";
          ClankDriver.ClankFileInfo exitedToWrapper;
          ClankFileInfoImpl exitedFromWrapper = includeStack.remove(includeStack.size() - 1);
          assert exitedFromWrapper.current == exitedFrom;
          // we cache possibly collected tokens in include handler
          // to allow delegate to use them
          includeHandler.cacheTokens(exitedFromWrapper.onExit());
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
        }
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

    public static final class ClankFileInfoImpl implements ClankDriver.ClankFileInfo, ClankDriver.APTTokenStreamCache {
      private final FileInfoCallback.FileInfo current;
      private final PreprocHandler ppHandler;
      private final StartEntry startEntry;
      private CharSequence currentPath;
      APTToken[] stolenTokens;
      ResolvedPath resolvedPath;
      
      public ClankFileInfoImpl(FileInfoCallback.FileInfo current,
              PreprocHandler ppHandler) {
        assert current != null;
        this.current = current;
        this.ppHandler = ppHandler;
        this.startEntry = ppHandler.getIncludeHandler().getStartEntry();
      }
      
      public APTToken[] getStolenTokens() {
        assert (stolenTokens != null);
        return stolenTokens;
      }

      private boolean stealTokensIfAny() {
        if (current.hasTokens()) {
          // have to be called before stealing tokens
          stolenTokens = ClankToAPTToken.convertToAPT(current.getPreprocessor(), current.getTokens());
          return true;
        } else {
          return false;
        }
      }

      @Override
      public CharSequence getFilePath() {
        if (currentPath == null) {
          String strPath = Casts.toCharSequence(current.getName()).toString();
          this.currentPath = CharSequences.create(CndFileUtils.normalizeAbsolutePath(startEntry.getFileSystem(), strPath));
        }
        return currentPath;
      }

      @Override
      public TokenStream getTokenStream() {
        return new ArrayBasedAPTTokenStream(getStolenTokens());
      }

      @Override
      public int getFileIndex() {
        return current.getIncludeIndex();
      }

      @Override
      public int getInclusionDirectiveStartOffset() {
        return current.getIncludeStartOffset();
      }

      @Override
      public int getInclusionDirectiveEndOffset() {
        return current.getIncludeEndOffset();
      }

      @Override
      public ResolvedPath getResolvedPath() {
        if (resolvedPath == null) {
          resolvedPath = createResolvedPath(getFilePath());
        }
        return resolvedPath;
      }

      @Override
      public int[] getSkippedRanges() {
        return current.getSkippedRanges();
      }

      private ResolvedPath createResolvedPath(CharSequence pathStr) {
        FileSystem fileSystem = startEntry.getFileSystem();
        CharSequence folder = CndFileUtils.normalizeAbsolutePath(fileSystem, CndPathUtilities.getDirName(pathStr.toString()));
        folder = FilePathCache.getManager().getString(CharSequences.create(folder));
        // FIXME: for now consider user path as isDefaultSearchPath
        boolean isDefaultSearchPath = (current.getFileType() == SrcMgr.CharacteristicKind.C_User);
        return new ResolvedPath(fileSystem, folder, pathStr, isDefaultSearchPath, 0);
      }

      @Override
      public PreprocHandler getHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean hasTokenStream() {
        return hasTokenStream;
      }

      private ClankDriver.APTTokenStreamCache onExit() {
        hasTokenStream = stealTokensIfAny();
        return this;
      }
      
      private boolean hasTokenStream = false;

      @Override
      public String toString() {
        return "ClankFileInfoImpl{" + "hasTokenStream=" + hasTokenStream + ", current=" + current + ",\n"
                + "startEntry=" + startEntry + ",\n"
                + "currentPath=" + currentPath + ",\n"
                + "resolvedPath=" + resolvedPath + '}';
      }
      
      
    }
}

