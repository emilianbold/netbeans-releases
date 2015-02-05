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
import java.util.LinkedHashSet;
import java.util.Set;
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

    public static abstract class JClankAbstractDiagnosticProvider implements CndDiagnosticProvider {

        @Override
        public void dumpInfo(Lookup context, PrintWriter printOut) {
            Collection<? extends DataObject> allFiles = context.lookupAll(DataObject.class);
            Set<NativeFileItem> nfis = new LinkedHashSet<>();
            for (DataObject dob : allFiles) {
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
                    nfis.add(nfi);
                }
            }
            dumpNativeFileItems(nfis, printOut);
        }

        protected abstract void dumpNativeFileItems(Set<NativeFileItem> nfis, PrintWriter printOut);
    }   
    
    @ServiceProvider(service = CndDiagnosticProvider.class, position = 102)
    public static final class JClankDumpFileTokens extends JClankAbstractDiagnosticProvider {
        private final boolean printTokens;
        private final boolean printStatistics;

        public JClankDumpFileTokens() {
            this.printTokens = true;
            this.printStatistics = true;
        }
        
        @NbBundle.Messages({"JClankDumpFileTokens.displayName=Preproces with JClank"})
        @Override
        public String getDisplayName() {
            return JClankDumpFileTokens_displayName();
        }

        @Override
        protected void dumpNativeFileItems(Set<NativeFileItem> nfis, PrintWriter printOut) {
            printOut.printf("====Dump File Tokens by JClank\n");// NOI18N 
            long totalTime = 0;
            int numFiles = 0;
            for (NativeFileItem nfi : nfis) {
                printOut.printf("====Dump Tokens for %s %n", nfi);// NOI18N 
                try {
                    printOut.printf("dumpFileTokens %s...%n", nfi.getAbsolutePath());
                    long time = CsmJClankSerivicesImpl.dumpPreprocessed(nfi, printOut, printTokens, printStatistics);
                    if (time > 0) {
                        numFiles++;
                        totalTime += time;
                    }
                    printOut.printf("dumpFileTokens %s took %,dms %n", nfi.getAbsolutePath(), time);
                } catch (Throwable e) {
                    new Exception(nfi.getAbsolutePath(), e).printStackTrace(printOut);
                }
            }
            printOut.printf("====Dump File Tokens by JClank for %d files took %,dms\n", numFiles, totalTime);// NOI18N 
        }
    }   
}
