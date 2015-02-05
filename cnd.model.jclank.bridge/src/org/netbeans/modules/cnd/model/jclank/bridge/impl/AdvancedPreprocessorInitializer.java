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

import org.clang.basic.target.TargetInfo;
import org.clang.lex.HeaderSearchOptions;
import org.clang.lex.PreprocessorOptions;
import org.clang.lex.frontend;
import org.clank.support.NativePointer;
import org.llvm.adt.IntrusiveRefCntPtr;
import org.llvm.adt.StringRef;
import org.llvm.support.MemoryBuffer;
import org.llvm.support.llvm;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Voskresensky
 */
class AdvancedPreprocessorInitializer extends DefaultPreprocessorInitializer {
    private final NativeFileItem startEntry;

    public AdvancedPreprocessorInitializer(NativeFileItem startEntry, raw_ostream llvm_err) {
        super(llvm_err);
        this.startEntry = startEntry;
        CsmJClankSerivicesImpl.setLangDefaults(this.LangOpts, startEntry);
        this.TargetOpts.$arrow().Triple.$assign("x86_64");
        this.Target.$assign(new IntrusiveRefCntPtr<>(TargetInfo.CreateTargetInfo(Diags, /*AddrOf*/ TargetOpts.$star())));
    } /*AddrOf*/

    @Override
    protected HeaderSearchOptions createHeaderSearchOptions() {
        HeaderSearchOptions options = super.createHeaderSearchOptions();
        // -I
        for (FSPath fSPath : startEntry.getUserIncludePaths()) {
            FileObject fileObject = fSPath.getFileObject();
            if (fileObject != null && fileObject.isFolder()) {
                if (CsmJClankSerivicesImpl.TRACE) {
                    llvm.errs().$out("user include ").$out(fSPath.getPath()).$out(NativePointer.$("\n"));
                }
                options.AddPath(new StringRef(fSPath.getPath()), frontend.IncludeDirGroup.Angled, false, true);
            }
        }
        // -isystem
        for (FSPath fSPath : startEntry.getSystemIncludePaths()) {
            FileObject fileObject = fSPath.getFileObject();
            if (fileObject != null && fileObject.isFolder()) {
                if (CsmJClankSerivicesImpl.TRACE) {
                    llvm.errs().$out("sys include ").$out(fSPath.getPath()).$out(NativePointer.$("\n"));
                }
                // add search path
                options.AddPath(new StringRef(fSPath.getPath()), frontend.IncludeDirGroup.System, false, true);
                // and register as system header prefix
                options.AddSystemHeaderPrefix(new StringRef(fSPath.getPath()), true);
            }
        }
        return options;
    }

    @Override
    protected PreprocessorOptions createPreprocessorOptions() {
        PreprocessorOptions out = super.createPreprocessorOptions();
        // handle -include
        for (String path : startEntry.getIncludeFiles()) {
            if (CsmJClankSerivicesImpl.TRACE) {
                llvm.errs().$out("file include ").$out(path).$out(NativePointer.$("\n"));
            }
            out.Includes.push_back(path);
        }
        for (String macro : startEntry.getSystemMacroDefinitions()) {
            if (CsmJClankSerivicesImpl.TRACE) {
                llvm.errs().$out("sys macro ").$out(macro).$out(NativePointer.$("\n"));
            }
            out.addMacroDef(new StringRef(macro));
        }
        for (String macro : startEntry.getUserMacroDefinitions()) {
            if (CsmJClankSerivicesImpl.TRACE) {
                llvm.errs().$out("user macro ").$out(macro).$out(NativePointer.$("\n"));
            }
            out.addMacroDef(new StringRef(macro));
        }
        if (false) {
            // remap unsaved files as memory buffers
            out.addRemappedFile(null, (MemoryBuffer) null);
        }
        return out;
    }
    
}
