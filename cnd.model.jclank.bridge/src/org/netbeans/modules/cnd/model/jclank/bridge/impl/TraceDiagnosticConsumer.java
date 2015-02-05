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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.clang.basic.Diagnostic;
import org.clang.basic.DiagnosticOptions;
import org.clang.basic.DiagnosticsEngine;
import org.clang.basic.LangOptions;
import org.clang.basic.PresumedLoc;
import org.clang.basic.spi.PreprocessorImplementation;
import org.clang.frontend.TextDiagnosticPrinter;
import org.clank.java.std;
import org.clank.support.Native;
import org.llvm.adt.aliases.SmallVectorChar;
import org.llvm.support.raw_ostream;

/**
 *
 * @author Vladimir Voskresensky
 */
public class TraceDiagnosticConsumer extends /*public*/ TextDiagnosticPrinter {
    private boolean insideSourceFile = false;
    private final raw_ostream out;
    
    public TraceDiagnosticConsumer(raw_ostream /*&*/ os, DiagnosticOptions /*P*/ diags) {
        super(os, diags, false);
        assert os != null;
        this.out = os;
    }

    @Override
    public /*virtual*/ void BeginSourceFile(/*const*//*const*/LangOptions /*&*/ LangOpts, /*const*/PreprocessorImplementation /*P*/ PP) {
        super.BeginSourceFile(LangOpts, PP); 
        insideSourceFile = true;
    }
    
    @Override
    public void EndSourceFile() {
        insideSourceFile = false;
        super.EndSourceFile(); 
    }
    
    @Override
    public void HandleDiagnostic(DiagnosticsEngine.Level DiagLevel, Diagnostic Info) {
        if (insideSourceFile) {
            super.HandleDiagnostic(DiagLevel, Info);
            return;
        }
        SmallVectorChar out = new SmallVectorChar(1024);
        Info.FormatDiagnostic(out);
        String Loc = "";
        if (Info.hasSourceManager()) {
            PresumedLoc presumedLoc = Info.getSourceManager().getPresumedLoc(Info.getLocation());
            if (presumedLoc.isValid()) {
                Loc = "In file " + Native.$toString(presumedLoc.getFilename()) + " line " + presumedLoc.getLine() + ", col " + presumedLoc.getColumn() + "\n";
            }
        }
        Level level = CsmJClankSerivicesImpl.toLoggerLevel(DiagLevel);
        String msg = new std.string(out.data()).toJavaString();
        if (DiagLevel.getValue() >= DiagnosticsEngine.Level.Error.getValue()) {
            msg = msg;
        }
        Logger.getLogger(TraceDiagnosticConsumer.class.getName()).log(level, "{2}{0}:{1}\n", new Object[]{DiagLevel, msg, Loc});
    }
    
}
