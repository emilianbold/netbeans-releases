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
package org.netbeans.modules.cnd.apt.support;

import java.util.Collection;
import java.util.List;
import org.clang.tools.services.support.FileInfoCallback;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.clank.ClankDriverImpl;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.util.CharSequences;

/**
 *
 */
public final class ClankDriver {

    private ClankDriver() {
    }

    public interface ClankPreprocessorOutput {

      int getFileIndex();

      int[] getSkippedRanges();

      TokenStream getTokenStream();

      boolean hasTokenStream();

      Collection<ClankPreprocessorDirective> getPreprocessorDirectives();

      Collection<MacroExpansion> getMacroExpansions();

      Collection<MacroUsage> getMacroUsages();

      FileGuard getFileGuard();
    }

    public static int extractFileIndex(PreprocHandler ppHandler) {
      return ClankDriverImpl.extractFileIndex(ppHandler);
    }

    public static ClankPreprocessorOutput extractPreprocessorOutput(PreprocHandler ppHandler) {
      return ClankDriverImpl.extractPreprocessorOutputImplementation(ppHandler);
    }

    public static TokenStream extractPreparedTokenStream(ClankDriver.ClankFileInfo file) {
        ClankPreprocessorOutput ppOutput = ClankDriver.extractPreparedPreprocessorOutput(file);
        if (ppOutput != null) {
            return ppOutput.getTokenStream();
        }
        return null;
    }

    public static ClankPreprocessorOutput extractPreparedPreprocessorOutput(ClankDriver.ClankFileInfo file) {
      ClankDriverImpl.ClankPreprocessorOutputImplementation ppOutputImpl = ClankDriverImpl.extractPreprocessorOutputImplementation(file);
      if (ppOutputImpl != null) {
          return ppOutputImpl.prepareCachesIfPossible();
      }
      return null;
    }

    public static ClankPreprocessorOutput extractPreparedPreprocessorOutput(PreprocHandler ppHandler) {
      ClankDriverImpl.ClankPreprocessorOutputImplementation cache = ClankDriverImpl.extractPreprocessorOutputImplementation(ppHandler);
      return cache.prepareCachesIfPossible();
    }

    public static void preparePreprocessorOutputIfPossible(ClankDriver.ClankFileInfo fileInfo) {
        if (fileInfo instanceof ClankDriverImpl.ClankPreprocessorOutputImplementation) {
            ((ClankDriverImpl.ClankPreprocessorOutputImplementation) fileInfo).prepareCachesIfPossible();
        }
    }

    public static boolean preprocess(APTFileBuffer buffer,
            PreprocHandler ppHandler,
            ClankPreprocessorCallback callback, Interrupter interrupter) {
        return ClankDriverImpl.preprocessImpl(buffer, ppHandler, callback, interrupter);
    }

    public static final class MacroExpansion {
        private final int startOfset;
        private final int endOfset;
        private final int macroNameLength;
        private final ClankMacroDirective referencedMacro;
        
        public MacroExpansion(FileInfoCallback.MacroExpansionInfo expansion, ClankMacroDirective referencedDirective) {
            startOfset = expansion.getStartOffset();
            endOfset = expansion.getEndOffset();
            macroNameLength = expansion.getMacroNameLength();
            referencedMacro = referencedDirective;
        }

        public int getStartOfset() {
            return startOfset;
        }

        public int getEndOfset() {
            return endOfset;
        }

        public int getMacroNameLength() {
            return macroNameLength;
        }

        public ClankMacroDirective getReferencedMacro() {
            return referencedMacro;
        }
    }

    public static final class MacroUsage {
        private final int startOfset;
        private final int endOfset;
        private final ClankMacroDirective referencedMacro;

        public MacroUsage(int startOfset, int endOfset, ClankMacroDirective referencedDirective) {
            this.startOfset = startOfset;
            this.endOfset = endOfset;
            referencedMacro = referencedDirective;
        }

        public int getStartOfset() {
            return startOfset;
        }

        public int getEndOfset() {
            return endOfset;
        }

        public ClankMacroDirective getReferencedMacro() {
            return referencedMacro;
        }
    }

    public static final class FileGuard {
        private final int startOfset;
        private final int endOfset;

        public FileGuard(int start, int end) {
            startOfset = start;
            endOfset = end;
        }

        public int getStartOfset() {
            return startOfset;
        }

        public int getEndOfset() {
            return endOfset;
        }
    }

    public interface ClankPreprocessorDirective {
      void setAnnotation(Object attr);
      Object getAnnotation();
      int getDirectiveStartOffset();
      int getDirectiveEndOffset();
    }

    // // #define or #undef directive
    public interface ClankMacroDirective extends ClankPreprocessorDirective {
        public static final CharSequence BUILD_IN_FILE = CharSequences.create("BUILD_IN_FILE"); //NOI18N
        
      // #define or #undef
      boolean isDefined();

      /*SourceLocation*/int getMacroNameLocation();

      /**
       *
       * @return name of file where directive was #defined/#undefed
       */
      CharSequence getFile();

      /**
       * @return name of #define'd or #undef'ed macro
       */
      CharSequence getMacroName();

      int getMacroNameOffset();

      /**
       * @return null for object-like macros, collection of parameter names for
       *        function-like macros
       */
      public List<CharSequence> getParameters();
    }

    public interface ClankErrorDirective extends ClankPreprocessorDirective {
      CharSequence getMessage();
      PreprocHandler.State getStateWhenMetErrorDirective();
    }
    
    public interface ClankInclusionDirective extends ClankPreprocessorDirective {
      ResolvedPath getResolvedPath();
      CharSequence getSpellingName();
      boolean isAngled();
      boolean isRecursive();
      int getIncludeDirectiveIndex();
    }

    public interface ClankPreprocessorCallback {
      /**
       *
       * @param directiveOwner
       * @param directive
       */
      void onInclusionDirective(ClankFileInfo directiveOwner, ClankInclusionDirective directive);

      /**
       * 
       * @param enteredFrom
       * @param enteredTo
       * @return true to continue, false to cancel
       */
      boolean onEnter(ClankFileInfo enteredFrom, ClankFileInfo enteredTo);
      
      /**
       * return true to continue or false to stop preprocessing and exit
       * @param exitedFrom
       * @param exitedTo
       * @return true to continue, false to cancel
       */
      boolean onExit(ClankFileInfo exitedFrom, ClankFileInfo exitedTo);

      boolean needPPDirectives();
      boolean needTokens();
      boolean needSkippedRanges();
      boolean needMacroExpansion();
      boolean needComments();
    }

    public interface ClankFileInfo {
      CharSequence getFilePath();
      int getFileIndex();
      ClankInclusionDirective getInclusionDirective();
      int[] getSkippedRanges();
    }

    ////////////////////////////////////////////////////////////////////////////
    // state/cache related methods
    public static void invalidate(APTFileBuffer buffer) {
        if (APTTraceFlags.USE_CLANK) {
            ClankDriverImpl.invalidateImpl(buffer);
        }
    }

    public static void invalidateAll() {
        if (APTTraceFlags.USE_CLANK) {
            ClankDriverImpl.invalidateAllImpl();
        }
    }

    public static void close() {
        if (APTTraceFlags.USE_CLANK) {
            invalidateAll();
        }
    }
}
