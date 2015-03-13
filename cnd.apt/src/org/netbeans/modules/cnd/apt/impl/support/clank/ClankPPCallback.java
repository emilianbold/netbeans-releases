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

import org.clang.lex.Token;
import org.clang.tools.services.support.TrackIncludeInfoCallback;
import static org.clank.java.std.strcmp;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ClankPPCallback extends TrackIncludeInfoCallback {

    private final ClankDriver.ClankPreprocessorCallback delegate;
    private final CharSequence path;
    private final int stopAtIndex;
    private Token[] tokens;
    private int nrTokens;

    public ClankPPCallback(CharSequence path, int stopAtIndex, raw_ostream traceOS, ClankDriver.ClankPreprocessorCallback delegate) {
        super(traceOS);
        this.path = path;
        this.stopAtIndex = stopAtIndex;
        this.delegate = delegate;
    }

    @Override
    public void onExit(TrackIncludeInfoCallback.IncludeFileInfo fileInfo) {
        if (ClankDriverImpl.TRACE) {
            traceOS.$out("Exit from ");
            if (fileInfo.isFile()) {
                traceOS.$out(fileInfo.getName());
            } else {
                traceOS.$out(fileInfo.getFileID());
            }
            traceOS.$out(" with #Token: ").$out(fileInfo.getTokens().size()).$out("\n");
            int[] offs = fileInfo.getSkippedRanges();
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

        if (stopAtIndex == fileInfo.getIncludeIndex()) {
            CndUtils.assertTrueInConsole((fileInfo.isFile() && (strcmp(path, fileInfo.getName()) == 0)), "expected " + path, fileInfo.getName());
            nrTokens = fileInfo.getTokens().size();
            tokens = fileInfo.stealTokens();
        }
    }

    public Token[] getTokens() {
        return tokens;
    }

    public int getNrTokens() {
        return nrTokens;
    }
}

