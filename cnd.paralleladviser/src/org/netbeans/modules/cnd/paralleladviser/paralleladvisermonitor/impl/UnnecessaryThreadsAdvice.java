/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.paralleladviser.paralleladvisermonitor.impl;

import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.*;
import java.net.URL;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.paralleladviser.utils.ParallelAdviserAdviceUtils;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.threadmap.api.ThreadSummaryData;
import org.openide.util.NbBundle;
import org.openide.windows.OutputWriter;

/**
 * Loop parallelization advice.
 *
 * @author Nick Krasilnikov
 */
public class UnnecessaryThreadsAdvice implements Advice {

    private final WeakReference<CsmProject> projectRef;
    private List<ThreadSummaryData> threads;

    public UnnecessaryThreadsAdvice(CsmProject project, List<ThreadSummaryData> threads) {
        projectRef = new WeakReference<CsmProject>(project);
        this.threads = threads;
    }

    public CsmProject getProject() {
        return projectRef.get();
    }

    public JComponent getComponent() {
        return ParallelAdviserAdviceUtils.createAdviceComponent(
                getHtml(),
                null);
    }

    public String getHtml() {
        URL iconUrl = LoopParallelizationAdvice.class.getClassLoader().getResource("org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/toomanythreads.png"); // NOI18N
        if(threads == null || threads.isEmpty()) {
            return ParallelAdviserAdviceUtils.createAdviceHtml(iconUrl,
                    getString("PAT_UnnecessaryThreads_Title"), // NOI18N
                    getString("PAT_UnnecessaryThreads_Body"), // NOI18N
                    800);            
        } else {
            StringBuilder threadsList = new StringBuilder(""); // NOI18N
            for (int i = 0; i < 3 && i < threads.size(); i++) {
                threadsList.append(threads.get(i).getThreadInfo().getThreadName());
                threadsList.append("<br>"); //NOI18N
            }
            return ParallelAdviserAdviceUtils.createAdviceHtml(iconUrl,
                    getString("PAT_UnnecessaryThreads_Title"), // NOI18N
                    getString("PAT_UnnecessaryThreadsWithList_Body", threadsList), // NOI18N
                    800);

        }
    }

    public static double getThreadWaitTime(ThreadSummaryData summaryData) {
        for (ThreadSummaryData.StateDuration stateDuration : summaryData.getThreadSummary()) {
            if (stateDuration.getState() == MSAState.Waiting) {
                return stateDuration.getDuration();
            }
        }
        return 0;
    }

    public void addNotification(OutputWriter writer) {
        if(threads == null || threads.isEmpty()) {
            writer.println(getString("PAT_UnnecessaryThreads_Notification")); // NOI18N
        } else {
            StringBuilder threadsList = new StringBuilder(""); // NOI18N
            for (int i = 0; i < 3 && i < threads.size(); i++) {
                threadsList.append(threads.get(i).getThreadInfo().getThreadName());
                threadsList.append("\n"); //NOI18N
            }
            writer.println(getString("PAT_UnnecessaryThreadsWithList_Notification", threadsList)); // NOI18N
        }
    }

    private static String getString(String name) {
        return NbBundle.getMessage(UnnecessaryThreadsAdvice.class, name);
    }

    private static String getString(String name, Object param1) {
        return NbBundle.getMessage(LoopParallelizationAdvice.class, name, param1);
    }
}
