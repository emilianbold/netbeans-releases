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

import java.io.IOException;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.*;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.paralleladviser.utils.ParallelAdviserAdviceUtils;
import org.openide.util.Exceptions;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Loop parallelization advice.
 *
 * @author Nick Krasilnikov
 */
public class LoopParallelizationAdvice implements Advice {

    private final static int REPRESENTATION_TYPE_FULL = 0;
    private final static int REPRESENTATION_TYPE_REDUCED = 1;
    private final static int REPRESENTATION_TYPE_FOLDING = 2;
    private final static int representationType = REPRESENTATION_TYPE_REDUCED;
    private final CsmFunction function;
    private final CsmLoopStatement loop;
    private final double processorUtilization;
    private final static boolean MORE = true;
    private final static boolean LESS = false;
    private boolean moreOrLess = MORE;

    public LoopParallelizationAdvice(CsmFunction function, CsmLoopStatement loop, double processorUtilization) {
        this.function = function;
        this.loop = loop;
        this.processorUtilization = processorUtilization;
    }

    public CsmFunction getFunction() {
        return function;
    }

    public CsmLoopStatement getLoop() {
        return loop;
    }

    public double getProcessorUtilization() {
        return processorUtilization;
    }

    public JComponent getComponent() {
        return ParallelAdviserAdviceUtils.createAdviceComponent(
                getHtml(),
                new HyperlinkListener() {

                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            if (e.getDescription().equals("function")) { // NOI18N
                                CsmUtilities.openSource(function);
                            }
                            if (e.getDescription().equals("loop")) { // NOI18N
                                CsmUtilities.openSource(loop);
                            }
                            if (e.getDescription().equals("more")) { // NOI18N
                                moreOrLess = MORE;
                                ParallelAdviserTopComponent.findInstance().updateTips();
                            }
                            if (e.getDescription().equals("less")) { // NOI18N
                                moreOrLess = LESS;
                                ParallelAdviserTopComponent.findInstance().updateTips();
                            }
                        }
                    }
                });
    }

    public String getHtml() {
        URL iconUrl = LoopParallelizationAdvice.class.getClassLoader().getResource("org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/ploop.png"); // NOI18N

        String html = "There is <b><a href=\"loop\">loop</a></b> in function <b><a href=\"function\">" + function.getName() + "</a></b> that could be effectively parallelized.<br>" + // NOI18N
                "Processor utilization is only <b>" + String.format("%1$.1f", processorUtilization) + "%</b> now."; // NOI18N
        if (representationType == REPRESENTATION_TYPE_FULL) {
            html += "<br><br>" + // NOI18N
                    "<a href=\"http://en.wikipedia.org/wiki/Parallel_computing\">Parallel computing</a> is a form of computation in which many calculations are carried out simultaneously, " + // NOI18N
                    "operating on the principle that large problems can often be divided into smaller ones, " + // NOI18N
                    "which are then solved concurrently (\"in parallel\").<br>" + // NOI18N
                    "There are several ways to make you program parallel. The easiest one is to use <a href=\"http://en.wikipedia.org/wiki/OpenMP\">OpenMP</a>."; // NOI18N
        }
        if (representationType == REPRESENTATION_TYPE_FOLDING) {
            if (moreOrLess == MORE) {
                html += "<br><br>" + // NOI18N
                        "<a href=\"http://en.wikipedia.org/wiki/Parallel_computing\">Parallel computing</a> is a form of computation in which many calculations are carried out simultaneously, " + // NOI18N
                        "operating on the principle that large problems can often be divided into smaller ones, " + // NOI18N
                        "which are then solved concurrently (\"in parallel\").<br>" + // NOI18N
                        "There are several ways to make you program parallel. The easiest one is to use <a href=\"http://en.wikipedia.org/wiki/OpenMP\">OpenMP</a>."; // NOI18N
                html += "<br><a href=\"less\">Less...</a>"; // NOI18N
            } else {
                html += "<br><a href=\"more\">More...</a>"; // NOI18N
            }
        }
        return ParallelAdviserAdviceUtils.createAdviceHtml(iconUrl, "Loop for parallelization has been found", // NOI18N
                html, 800); // NOI18N
    }

    public void addNotification(OutputWriter writer) {
        try {
            writer.println("\"" + loop.getContainingFile().getName() + "\", line " + loop.getStartPosition().getLine() + ": There is loop in function " + function.getName() + " that could be effectively parallelized.", // NOI18N
                    new OutputListener() {

                        public void outputLineSelected(OutputEvent ev) {
                        }

                        public void outputLineAction(OutputEvent ev) {
                            CsmUtilities.openSource(loop);
                        }

                        public void outputLineCleared(OutputEvent ev) {
                        }
                    });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
