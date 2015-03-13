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

import java.io.IOException;
import java.util.Collections;
import org.clang.basic.IdentifierInfo;
import org.clang.basic.tok;
import org.clang.lex.Token;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.ClankPreprocessorServices;
import org.clang.tools.services.ClankRunPreprocessorSettings;
import org.clang.tools.services.support.Interrupter;
import org.clang.tools.services.support.TrackIncludeInfoCallback;
import static org.clank.java.std.strcmp;
import org.clank.support.Casts;
import org.clank.support.NativePointer;
import org.llvm.support.llvm;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteConstTextToken;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ClankDriverImpl {

    private static final boolean TRACE = true;

    public static TokenStream getTokenStream(APTFileBuffer buffer,
            PreprocHandler ppHandler,
            final ClankDriver.ClankPreprocessorCallback callback,
            final org.netbeans.modules.cnd.support.Interrupter interrupter) {
        try {
            ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl) ppHandler.getIncludeHandler();
            Token[] tokens = includeHandler.getTokens();
            int nrTokens = includeHandler.getNrTokens();
            if (tokens == null) {
                CharSequence path = buffer.getAbsolutePath();
                byte[] bytes = toBytes(buffer.getCharBuffer());
                // prepare params to run preprocessor
                ClankRunPreprocessorSettings settings = new ClankRunPreprocessorSettings();
                settings.WorkName = path;
                settings.GenerateDiagnostics = true;
                settings.PrettyPrintDiagnostics = true;
                settings.PrintDiagnosticsOS = llvm.errs();
                settings.TraceClankStatistics = false;
                settings.cancelled = new Interrupter() {
                    @Override
                    public boolean isCancelled() {
                        return interrupter.cancelled();
                    }
                };
                FileTokensCallback fileTokensCallback = new FileTokensCallback(path, STOP_AT_FILE_PATH, llvm.errs(), callback);
                settings.IncludeInfoCallbacks = fileTokensCallback;
                ClankCompilationDataBase db = APTToClankCompilationDB.convertPPHandler(ppHandler, path);
                ClankPreprocessorServices.preprocess(Collections.singleton(db), settings);
                tokens = fileTokensCallback.getTokens();
                if (tokens == null) {
                    CndUtils.assertTrueInConsole(false, "no Tokens for " + path);
                    return null;
                }
                nrTokens = fileTokensCallback.getNrTokens();
                includeHandler.setIncludeInfo(fileTokensCallback.getIncludeStackIndex(), tokens, nrTokens);
            }
            if (interrupter.cancelled() || tokens == null) {
                return null;
            }
            return new ClankToAPTTokenStream(tokens, nrTokens);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    private static final int STOP_AT_FILE_PATH = -1;

    private static byte[] toBytes(char[] chars) {
        byte[] asciis = new byte[chars.length];
        for (int i = 0; i < asciis.length; i++) {
            asciis[i] = NativePointer.$(chars[i]);
        }
        return asciis;
    }

    private static class FileTokensCallback extends TrackIncludeInfoCallback {

        private final ClankDriver.ClankPreprocessorCallback delegate;
        private final CharSequence path;
        private final int stopAtIndex;
        private Token[] tokens;
        private int nrTokens;
        private int inclStackIndex;

        public FileTokensCallback(CharSequence path, int stopAtIndex, raw_ostream traceOS, ClankDriver.ClankPreprocessorCallback delegate) {
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
            
            if (stopAtIndex != STOP_AT_FILE_PATH) {
                if (stopAtIndex == fileInfo.getIncludeIndex()) {
                    nrTokens = fileInfo.getTokens().size();
                    tokens = fileInfo.stealTokens();
                    inclStackIndex = fileInfo.getIncludeIndex();
                }
            } else if (fileInfo.isFile() && (strcmp(path, fileInfo.getName()) == 0)) {
                nrTokens = fileInfo.getTokens().size();
                tokens = fileInfo.stealTokens();
                inclStackIndex = fileInfo.getIncludeIndex();
            }
        }

        private Token[] getTokens() {
            return tokens;
        }

        public int getNrTokens() {
            return nrTokens;
        }

        private int getIncludeStackIndex() {
            assert (stopAtIndex == STOP_AT_FILE_PATH) || stopAtIndex == inclStackIndex : "stopAtIndex="+stopAtIndex + " vs. " + inclStackIndex;
            return inclStackIndex;
        }
    }

    private static final class ClankToAPTTokenStream implements APTTokenStream, TokenStream {

        private int index;
        private final int lastIndex;
        private final Token[] tokens;

        public ClankToAPTTokenStream(Token[] tokens, int nrTokens) {
            this.tokens = tokens;
            this.lastIndex = nrTokens - 1;
            this.index = 0;
        }

        @Override
        public APTToken nextToken() {
            if (index < lastIndex) {
                return new ClankToAPTToken(tokens[index++]);
            } else {
                return APTUtils.EOF_TOKEN;
            }
        }
    }
}
