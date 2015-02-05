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
package org.netbeans.modules.cnd.model.jclank.bridge.impl;

import java.io.PrintWriter;
import org.netbeans.modules.cnd.model.jclank.bridge.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.clang.basic.ClangGlobals.$out_DiagnosticBuilder_StringRef;
import org.clang.basic.Diagnostic;
import org.clang.basic.DiagnosticIDs;
import org.clang.basic.DiagnosticOptions;
import org.clang.basic.DiagnosticsEngine;
import org.clang.basic.FileEntry;
import org.clang.basic.FileManager;
import org.clang.basic.FileSystemOptions;
import org.clang.basic.LangOptions;
import org.clang.basic.Module;
import org.clang.basic.PresumedLoc;
import org.clang.basic.SourceLocation;
import org.clang.basic.SourceManager;
import org.clang.basic.SrcMgr;
import org.clang.basic.diag;
import org.clang.basic.target.TargetInfo;
import org.clang.basic.target.TargetOptions;
import org.clang.basic.tok;
import org.clang.frontend.ClangGlobals;
import org.clang.frontend.CompilerInvocation;
import org.clang.frontend.FrontendOptions;
import org.clang.frontend.InputKind;
import org.clang.frontend.LangStandard;
import org.clang.frontend.PreprocessorOutputOptions;
import org.clang.lex.HeaderSearch;
import org.clang.lex.HeaderSearchOptions;
import org.clang.lex.ModuleIdPath;
import org.clang.lex.ModuleLoadResult;
import org.clang.lex.ModuleLoader;
import org.clang.lex.Preprocessor;
import org.clang.lex.PreprocessorOptions;
import org.clang.lex.Token;
import org.clang.lex.frontend;
import org.clank.java.std;
import org.clank.support.Converted;
import org.clank.support.Destructors;
import org.clank.support.Native;
import static org.clank.support.NativePointer.*;
import org.clank.support.NativeTrace;
import org.llvm.adt.IntrusiveRefCntPtr;
import org.llvm.adt.StringRef;
import org.llvm.adt.aliases.SmallVectorChar;
import org.llvm.support.MemoryBuffer;
import org.llvm.support.llvm;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class CsmJClankSerivicesImpl {
    public static final boolean TRACE = false;
    
    public static APTTokenStream getAPTTokenStream(NativeFileItem nfi) {
        throw new UnsupportedOperationException();
    }

    public static void dumpTokens(NativeFileItem nfi) {
        Preprocessor /*&*/ PP = getPreprocessor(nfi);
        if (PP != null) {
            // Start preprocessing the specified input file.
            Token Tok/*J*/ = new Token();
            PP.EnterMainSourceFile();
            do {
                PP.Lex(Tok);
                PP.DumpToken(Tok, true);
                llvm.errs().$out($("\n"));
            } while (Tok.isNot(tok.TokenKind.eof));
        }
    }

    public static long dumpPreprocessed(NativeFileItem nfi, PrintWriter printOut) {
        clearStatistics();
        long time = System.currentTimeMillis();
        boolean done = false;
        Preprocessor /*&*/ PP = getPreprocessor(nfi);
        if (PP != null) {
            PreprocessorOutputOptions Opts = createPPOptions(nfi);
            try {
                ClangGlobals.DoPrintPreprocessedInput(PP, llvm.nulls(), Opts);
                time = System.currentTimeMillis() - time;
                done = true;
            } finally {
                llvm.outs().flush();
                llvm.errs().flush();
            }
            PrintStatistics(PP, nfi);
        }
        return done ? time : 0;
    }

    private static void clearStatistics() {
        org.clang.frontendtool.ClangGlobals.clearStatistics();
    }
    
    private static void PrintStatistics(Preprocessor PP, NativeFileItem nfi) {
        if (NativeTrace.STATISTICS) {
          llvm.errs().$out("\nSTATISTICS FOR '").$out(nfi.getAbsolutePath()).$out("':\n");
          PP.PrintStats();
          PP.getIdentifierTable().PrintStats();
          PP.getHeaderSearchInfo().PrintStats();
          PP.getSourceManager().PrintStats();
          llvm.errs().$out("\n");
          org.clang.frontendtool.ClangGlobals.PrintStats(llvm.errs(), System.err);
        }          
    }
    
    private static Preprocessor getPreprocessor(NativeFileItem nfi) {
        PreprocessorInitializer initializer = new AdvancedPreprocessorInitializer(nfi);
        VoidModuleLoader ModLoader/*J*/ = new VoidModuleLoader();
        Preprocessor PP/*J*/ = initializer.createPreprocessor(ModLoader);
        
        StringRef InputFile = new StringRef(nfi.getAbsolutePath());
        FileManager FileMgr = PP.getFileManager();
        FileEntry /*P*/ File = FileMgr.getFile(InputFile, true);
        if (File == null) {
            $out_DiagnosticBuilder_StringRef(PP.getDiagnostics().Report(diag.err_fe_error_reading), InputFile).$destroy();
            return null;
        } else {
            SourceManager SourceMgr = PP.getSourceManager();
            SourceMgr.setMainFileID(
                    SourceMgr.createFileID(
                            File, 
                            new SourceLocation(), 
                            SrcMgr.CharacteristicKind.C_User
                    )
            );
        }   
        
        return PP;
    }

    private static PreprocessorOutputOptions createPPOptions(NativeFileItem nfi) {
        PreprocessorOutputOptions opts = new PreprocessorOutputOptions();
        opts.ShowCPP = true;
        opts.ShowComments = true;
        opts.ShowLineMarkers = true;
        opts.ShowMacros = true;
        opts.ShowMacroComments = true;
        return opts;
    }

    public static interface PreprocessorInitializer {
        Preprocessor createPreprocessor(ModuleLoader modLoader);
    }

    public static void setLangDefaults(LangOptions LangOpts, NativeFileItem startEntry) {
        LangStandard.Kind lang_std = LangStandard.Kind.lang_unspecified;
        InputKind lang = InputKind.IK_None;
        switch (startEntry.getLanguage()) {
            case C:
            case C_HEADER:
                lang = InputKind.IK_C;
                break;
            case CPP:
                lang = InputKind.IK_CXX;
                break;
            case FORTRAN:
            case OTHER:
            default:
                throw new AssertionError(startEntry.getLanguage().name());
        }
        switch (startEntry.getLanguageFlavor()) {
            case DEFAULT:
            case UNKNOWN:
                break;
            case C:
                break;
            case C89:
                lang_std = LangStandard.Kind.lang_gnu89;
                break;
            case C99:
                lang_std = LangStandard.Kind.lang_gnu99;
                break;
            case CPP:
                lang_std = LangStandard.Kind.lang_cxx03;
                break;
            case CPP11:
                lang_std = LangStandard.Kind.lang_gnucxx11;
                break;
            case C11:
                lang_std = LangStandard.Kind.lang_gnu11;
                break;
            case CPP14:
                // FIXME
                lang_std = LangStandard.Kind.lang_gnucxx1y;
                break;
            case F77:
            case F90:
            case F95:
            default:
                throw new AssertionError(startEntry.getLanguageFlavor().name());
        }
        CompilerInvocation.setLangDefaults(LangOpts, lang, lang_std);
    }
    
    public static Level toLoggerLevel(DiagnosticsEngine.Level DiagLevel) {
        switch (DiagLevel) {
            case Ignored:
            case Note:
                return Level.INFO;
            case Warning:
                return Level.WARNING;
            case Error:
            case Fatal:
                return Level.SEVERE;
            default:
                throw new AssertionError(DiagLevel.name());
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="<anonymous namespace>::VoidModuleLoader">
    @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp", line = 31,
            cmd = "jclank.sh ${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp -filter=<anonymous namespace>::VoidModuleLoader")
//</editor-fold>
    public static class VoidModuleLoader extends /*public*/ ModuleLoader implements Destructors.ClassWithDestructor {

        //<editor-fold defaultstate="collapsed" desc="<anonymous namespace>::VoidModuleLoader::loadModule">
        @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp", line = 32,
                cmd = "jclank.sh ${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp -filter=<anonymous namespace>::VoidModuleLoader::loadModule")
        //</editor-fold>
        @Override
        public/*private*/ /*virtual*/ ModuleLoadResult loadModule(SourceLocation ImportLoc, ModuleIdPath Path, Module.NameVisibilityKind Visibility, boolean IsInclusionDirective) {
            return new ModuleLoadResult();
        }

        //<editor-fold defaultstate="collapsed" desc="<anonymous namespace>::VoidModuleLoader::makeModuleVisible">
        @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp", line = 39,
                cmd = "jclank.sh ${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp -filter=<anonymous namespace>::VoidModuleLoader::makeModuleVisible")
        //</editor-fold>
        @Override
        public/*private*/ /*virtual*/ void makeModuleVisible(Module /*P*/ Mod, Module.NameVisibilityKind Visibility, SourceLocation ImportLoc, boolean Complain) {
        }

        //<editor-fold defaultstate="collapsed" desc="<anonymous namespace>::VoidModuleLoader::~VoidModuleLoader">
        @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp", line = 31,
                cmd = "jclank.sh ${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp -filter=<anonymous namespace>::VoidModuleLoader::~VoidModuleLoader")
        //</editor-fold>
        @Override
        public /*inline*/ void $destroy() {
            super.$destroy();
        }

        //<editor-fold defaultstate="collapsed" desc="<anonymous namespace>::VoidModuleLoader::VoidModuleLoader">
        @Converted(kind = Converted.Kind.AUTO, source = "${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp", line = 31,
                cmd = "jclank.sh ${LLVM_SRC}/llvm/tools/clang/unittests/Lex/LexerTest.cpp -filter=<anonymous namespace>::VoidModuleLoader::VoidModuleLoader")
        //</editor-fold>
        public /*inline*/ VoidModuleLoader() {
            /* : ModuleLoader()*/
            //START JInit
            super();
            //END JInit
        }

    }
}
