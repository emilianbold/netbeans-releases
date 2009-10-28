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
import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.*;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.paralleladviser.utils.ParallelAdviserAdviceUtils;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Loop parallelization advice.
 *
 * @author Nick Krasilnikov
 */
public class LoopParallelizationAdvice implements Advice {

    private final WeakReference<CsmFunction> functionRef;
    private final WeakReference<CsmLoopStatement> loopRef;
    private final double processorUtilization;
    private final String functionName;
    private final String fileName;
    private final int lineInFile;
    private final PositionBounds functionPosition;
    private final PositionBounds loopPosition;


    public LoopParallelizationAdvice(CsmFunction function, CsmLoopStatement loop, double processorUtilization) {
        this.functionRef = new WeakReference<CsmFunction>(function);
        this.loopRef = new WeakReference<CsmLoopStatement>(loop);
        this.processorUtilization = processorUtilization;
        this.functionName = function.getName().toString();
        this.fileName = loop.getContainingFile().getName().toString();
        this.lineInFile = loop.getStartPosition().getLine();
        this.loopPosition = CsmUtilities.createPositionBounds(loop);
        this.functionPosition = CsmUtilities.createPositionBounds(function);
    }

    public CsmProject getProject() {
        CsmFunction function = functionRef.get();
        if (function != null) {
            CsmFile file = function.getContainingFile();
            if (file != null) {
                return file.getProject();
            }
        }
        return null;
    }

    public CsmFunction getFunction() {
        return functionRef.get();
    }

    public CsmLoopStatement getLoop() {
        return loopRef.get();
    }

    public PositionBounds getLoopPosition() {
        return loopPosition;
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
                                CsmUtilities.openSource(functionPosition);
                            }
                            if (e.getDescription().equals("loop")) { // NOI18N
                                CsmUtilities.openSource(loopPosition);
                            }
                        }
                    }
                });
    }

    public String getHtml() {
        URL iconUrl = LoopParallelizationAdvice.class.getClassLoader().getResource("org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/loopforparallelization.png"); // NOI18N

        return ParallelAdviserAdviceUtils.createAdviceHtml(iconUrl,
                getString("PAT_LoopParallelization_Title"), // NOI18N
                getString("PAT_LoopParallelization_Body", functionName, String.format("%1$.1f", processorUtilization)), // NOI18N
                800);
    }

    public void addNotification(OutputWriter writer) {
        try {
            writer.println(getString("PAT_LoopParallelization_Notification", fileName, lineInFile, functionName), // NOI18N
                    new OutputListener() {

                public void outputLineSelected(OutputEvent ev) {
                }

                public void outputLineAction(OutputEvent ev) {
                    CsmUtilities.openSource(loopPosition);
                }

                public void outputLineCleared(OutputEvent ev) {
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static String getString(String name) {
        return NbBundle.getMessage(LoopParallelizationAdvice.class, name);
    }

    private static String getString(String name, Object param1, Object param2) {
        return NbBundle.getMessage(LoopParallelizationAdvice.class, name, param1, param2);
    }

    private static String getString(String name, Object param1, Object param2, Object param3) {
        return NbBundle.getMessage(LoopParallelizationAdvice.class, name, param1, param2, param3);
    }
}
