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

import org.clang.basic.DiagnosticIDs;
import org.clang.basic.DiagnosticOptions;
import org.clang.basic.DiagnosticsEngine;
import org.clang.basic.FileManager;
import org.clang.basic.FileSystemOptions;
import org.clang.basic.LangOptions;
import org.clang.basic.SourceManager;
import org.clang.basic.target.TargetInfo;
import org.clang.basic.target.TargetOptions;
import org.clang.frontend.ClangGlobals;
import org.clang.frontend.FrontendOptions;
import org.clang.lex.HeaderSearch;
import org.clang.lex.HeaderSearchOptions;
import org.clang.lex.ModuleLoader;
import org.clang.lex.Preprocessor;
import org.clang.lex.PreprocessorOptions;
import org.llvm.adt.IntrusiveRefCntPtr;

/**
 *
 * @author Vladimir Voskresensky
 */
class DefaultPreprocessorInitializer implements CsmJClankSerivicesImpl.PreprocessorInitializer {

    protected final FileSystemOptions FileMgrOpts;
    protected final FileManager FileMgr;
    protected final IntrusiveRefCntPtr<DiagnosticIDs> DiagID;
    protected final DiagnosticsEngine Diags;
    protected final SourceManager SourceMgr;
    protected final LangOptions LangOpts;
    protected final IntrusiveRefCntPtr<TargetOptions> TargetOpts;
    protected final IntrusiveRefCntPtr<TargetInfo> Target;

    public DefaultPreprocessorInitializer() {
        this.FileMgrOpts = new FileSystemOptions();
        this.FileMgr = new FileManager(FileMgrOpts);
        this.DiagID = new IntrusiveRefCntPtr<>(new DiagnosticIDs());
        this.Diags = new DiagnosticsEngine(DiagID, new DiagnosticOptions(), new TraceDiagnosticConsumer());
        this.SourceMgr = new SourceManager(Diags, FileMgr);
        this.LangOpts = new LangOptions();
        this.TargetOpts = new IntrusiveRefCntPtr<>(new TargetOptions());
        this.Target = new IntrusiveRefCntPtr<>();
        //END JInit
    }

    @Override
    public Preprocessor createPreprocessor(ModuleLoader ModLoader) {
        PreprocessorOptions PPOpts = createPreprocessorOptions();
        HeaderSearchOptions HSOpts = createHeaderSearchOptions();
        HeaderSearch HS = new HeaderSearch(new IntrusiveRefCntPtr<>(HSOpts), SourceMgr, Diags, LangOpts, Target.getPtr());
//            ClangGlobals.ApplyHeaderSearchOptions(HS, HSOpts, LangOpts, Target.getPtr().getTriple());
        Preprocessor PP = new Preprocessor(new IntrusiveRefCntPtr<>(PPOpts), Diags, LangOpts, Target.getPtr(), SourceMgr, HS, ModLoader, null, false, false);
        ClangGlobals.InitializePreprocessor(PP, PPOpts, HSOpts, new FrontendOptions());
        return PP;
    }

    protected HeaderSearchOptions createHeaderSearchOptions() {
        HeaderSearchOptions HSOpts = new HeaderSearchOptions();
        HSOpts.UseBuiltinIncludes = false;
        HSOpts.UseLibcxx = false;
        HSOpts.UseStandardCXXIncludes = false;
        HSOpts.UseStandardSystemIncludes = false;

        if (CsmJClankSerivicesImpl.TRACE) {
            HSOpts.Verbose = true;
        }
        return HSOpts;
    }

    protected PreprocessorOptions createPreprocessorOptions() {
        PreprocessorOptions out = new PreprocessorOptions();
        out.UsePredefines = false;
        return out;
    }
}
