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
package org.netbeans.modules.cnd.model.jclank.trace;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.netbeans.modules.cnd.model.jclank.bridge.impl.CsmJClankSerivicesImpl;
import static org.netbeans.modules.cnd.model.jclank.trace.Bundle.*;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmJClankTracePreprocessorAction {
    private static long dumpFileTokens(NativeFileItem nfi, PrintWriter printOut) {
        PrintStream origErr = System.err;
        PrintStream origOut = System.out;
        long time = 0;
        try {
            final PrintStream printStreamOut = new PrintStream(new WriterOutputStream(printOut));
//            System.setErr(new PrintStreamDuplex(new WriterOutputStream(printOut), origErr));
//            System.setOut(new PrintStreamDuplex(new WriterOutputStream(printOut), origOut));
            System.setOut(printStreamOut);
            try {
                time = CsmJClankSerivicesImpl.dumpPreprocessed(nfi, printOut);
            } finally {
                printStreamOut.flush();
            }
        } finally {
            System.setErr(origErr);
            System.setOut(origOut);
        }
        return time;
    }

    @ServiceProvider(service = CndDiagnosticProvider.class, position = 102)
    public final static class JClankDumpFileTokens implements CndDiagnosticProvider {

        @NbBundle.Messages({"JClankDumpFileTokens.displayName=Preproces with JClank"})
        @Override
        public String getDisplayName() {
            return JClankDumpFileTokens_displayName();
        }

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            printOut.printf("====Dump File Tokens by JClank\n");// NOI18N 
            Collection<? extends DataObject> allFiles = context.lookupAll(DataObject.class);
            long totalTime = 0;
            int numFiles = 0;
            for (DataObject dob : allFiles) {
                printOut.printf("====Dump Tokens for %s %n", dob);// NOI18N 
                NativeFileItemSet nfs = dob.getLookup().lookup(NativeFileItemSet.class);
                if (nfs == null) {
                    printOut.printf("NO NativeFileItemSet in %s %n", dob);
                    continue;
                }
                if (nfs.isEmpty()) {
                    printOut.printf("EMPTY NativeFileItemSet in %s %n", dob);
                    continue;
                }
                for (NativeFileItem nfi : nfs.getItems()) {
                    try {
                        printOut.printf("dumpFileTokens %s...%n", nfi.getAbsolutePath());
                        long time = CsmJClankTracePreprocessorAction.dumpFileTokens(nfi, printOut);
                        if (time > 0) {
                            numFiles++;
                            totalTime += time;
                        }
                        printOut.printf("dumpFileTokens %s took %,dms %n", nfi.getAbsolutePath(), time);
                    } catch (Throwable e) {
                        new Exception(nfi.getAbsolutePath(), e).printStackTrace(printOut);
                    }
                }
            }
            printOut.printf("====Dump File Tokens by JClank for %d files took %,dms\n", numFiles, totalTime);// NOI18N 
        }
    }   
}
