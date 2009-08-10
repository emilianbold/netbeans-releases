/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author mt154047
 */
class GoToSourceAction extends AbstractAction {

    public GoToSourceAction() {
        super("Test"); // NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }



//    private final FunctionCallNode functionCallNode;
//    private SourceFileInfo sourceInfo;
//    private Future<Boolean> goToSourceTask;
//
//    public GoToSourceAction(FunctionCallNode funcCallNode) {
////        super(NbBundle.getMessage(FunctionsListViewVisualizer.class, "GoToSourceActionName"));//NOI18N
//        this.functionCallNode = funcCallNode;
//        synchronized (sourcePrefetchExecutorLock) {
//            if (sourcePrefetchExecutor == null) {
//                sourcePrefetchExecutor = Executors.newFixedThreadPool(2);
//            }
//        }
//        sourcePrefetchExecutor.submit(new Runnable() {
//
//            public void run() {
//                getSource();
//            }
//        });
//    }
//
//    public synchronized void actionPerformed(ActionEvent e) {
//        if (goToSourceTask == null || goToSourceTask.isDone()) {
//            goToSourceTask = DLightExecutorService.submit(new Callable<Boolean>() {
//
//                public Boolean call() {
//                    return goToSource();
//                }
//            }, "GoToSource from Functions List View"); // NOI18N
//            }
//    }
//
//    private boolean goToSource() {
//        SourceFileInfo source = getSource();
//        if (source != null && source.isSourceKnown()) {
//            sourceSupportProvider.showSource(source);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private SourceFileInfo getSource() {
//        synchronized (this) {
//            if (sourceInfo != null) {
//                return sourceInfo;
//            }
//        }
//        FunctionCallWithMetric functionCall = functionCallNode.getFunctionCall();
//        SourceFileInfo result = dataProvider.getSourceFileInfo(functionCall);
//        if (result != null && result.isSourceKnown()) {
//            synchronized (this) {
//                if (sourceInfo == null) {
//                    sourceInfo = result;
//                }
//            }
//            return sourceInfo;
//        } else {
//            setEnabled(false);
//            functionCallNode.fire();
//            return null;
//        }
//    }
}
