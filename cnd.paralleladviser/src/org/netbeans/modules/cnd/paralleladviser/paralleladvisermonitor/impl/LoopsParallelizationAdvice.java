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

import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.*;
import java.net.URL;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.cnd.paralleladviser.utils.ParallelAdviserAdviceUtils;

/**
 * Loop parallelization advice.
 *
 * @author Nick Krasilnikov
 */
public class LoopsParallelizationAdvice implements Advice {

    private final static int REPRESENTATION_TYPE_SEPARATE_TIPS = 0;
    private final static int REPRESENTATION_TYPE_TABLE = 1;
    private final static int representationType = REPRESENTATION_TYPE_TABLE;
    private final List<LoopParallelizationAdvice> tips;

    public LoopsParallelizationAdvice(List<LoopParallelizationAdvice> tips) {
        this.tips = tips;
    }

    public JComponent getComponent() {
        URL iconUrl = LoopsParallelizationAdvice.class.getClassLoader().getResource("org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/ploop.png"); // NOI18N

        String tipsHtml = ""; // NOI18N
        if (representationType == REPRESENTATION_TYPE_SEPARATE_TIPS) {
            for (LoopParallelizationAdvice tip : tips) {
                tipsHtml += ParallelAdviserAdviceUtils.createAdviceHtml(null, "Loop for parallelization has been found", // NOI18N
                        "There is <b><a href=\"loop\">loop</a></b> in function <b><a href=\"function\">" + tip.getFunction().getName() + "</a></b> that could be effectively parallelized.<br>" + // NOI18N
                        "Processor utilization is only <b>" + String.format("%1$.1f", tip.getProcessorUtilization()) + "%</b> now.", 738); // NOI18N
                tipsHtml += "<br>"; // NOI18N
            }
        } else {
            tipsHtml += "    <table cellpadding=\"0\" cellspacing=\"0\" border=\"1\">" + // NOI18N
                    "        <tr bgcolor=\"#c7e3e8\">" + // NOI18N
                    "            <td>Function</td>" + // NOI18N
                    "            <td>Loop</td>" + // NOI18N
                    "            <td>Processor utilization</td>" + // NOI18N
                    "        </tr>"; // NOI18N
            for (LoopParallelizationAdvice tip : tips) {
                tipsHtml += "        <tr>" + // NOI18N
                        "            <td><a href=\"function\">" + tip.getFunction().getName() + "</a></td>" + // NOI18N
                        "            <td><a href=\"loop\">loop</a></td>" + // NOI18N
                        "            <td>" + String.format("%1$.1f", tip.getProcessorUtilization()) + "</td>" + // NOI18N
                        "        </tr>"; // NOI18N
            }
            tipsHtml += "    </table>"; // NOI18N
        }

        return ParallelAdviserAdviceUtils.createAdviceComponent(
                ParallelAdviserAdviceUtils.createAdviceHtml(iconUrl, "Loops for parallelization", // NOI18N
                "There are some loops in program that could be parallelized:" + // NOI18N
                tipsHtml +
                "<br>" + // NOI18N
                "<a href=\"http://en.wikipedia.org/wiki/Parallel_computing\">Parallel computing</a> is a form of computation in which many calculations are carried out simultaneously, " + // NOI18N
                "operating on the principle that large problems can often be divided into smaller ones, " + // NOI18N
                "which are then solved concurrently (\"in parallel\").<br>" + // NOI18N
                "There are several ways to make you program parallel. The easiest one is to use <a href=\"http://en.wikipedia.org/wiki/OpenMP\">OpenMP</a>.", 800), // NOI18N
                new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//                    if (e.getDescription().equals("function")) { // NOI18N
//                        CsmUtilities.openSource(function);
//                    }
//                    if (e.getDescription().equals("loop")) { // NOI18N
//                        CsmUtilities.openSource(loop);
//                    }
                }
            }
        });
    }
}
