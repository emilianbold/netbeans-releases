/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.access;

import java.io.InvalidObjectException;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class TruffleEval {
    
    private static final String METHOD_EVALUATE = "evaluate";                   // NOI18N
    private static final String METHOD_EVALUATE_ON_FRAME_SIG =
            "(Lcom/oracle/truffle/api/debug/DebugStackFrame;Ljava/lang/String;)Ljava/lang/Object;"; // NOI18N
    
    private TruffleEval() {}

    @NbBundle.Messages("MSG_NoSuspend=No current suspend location.")
    public static Variable evaluate(JPDADebugger debugger, String expression) throws InvalidExpressionException {
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
        if (currentPCInfo == null) {
            throw new InvalidExpressionException(Bundle.MSG_NoSuspend());
        }
        ObjectVariable stackFrameInstance = currentPCInfo.getSelectedStackFrame().getStackFrameInstance();
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable mirrorExpression = debugger.createMirrorVar(expression);
            Variable valueVar = debugAccessor.invokeMethod(
                    METHOD_EVALUATE,
                    METHOD_EVALUATE_ON_FRAME_SIG,
                    new Variable[] { stackFrameInstance,
                                     mirrorExpression });
            return valueVar;
        } catch (InvalidObjectException | NoSuchMethodException ex) {
            try {
                return debugger.createMirrorVar(ex.getLocalizedMessage());
            } catch (InvalidObjectException iex) {
                Exceptions.printStackTrace(iex);
                return null;
            }
            //return ex.getLocalizedMessage();
        }
    }

}
