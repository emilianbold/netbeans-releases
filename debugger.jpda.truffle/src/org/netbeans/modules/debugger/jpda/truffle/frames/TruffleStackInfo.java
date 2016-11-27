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

package org.netbeans.modules.debugger.jpda.truffle.frames;

import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public final class TruffleStackInfo {
    
    private static final String METHOD_GET_FRAMES_INFO = "getFramesInfo";       // NOI18N
    private static final String METHOD_GET_FRAMES_INFO_SIG = "([Lcom/oracle/truffle/api/debug/DebugStackFrame;Z)[Ljava/lang/Object;";   // NOI18N
    
    private final JPDADebugger debugger;
    private final ObjectVariable stackTrace;
    private TruffleStackFrame[] stackFrames;
    private boolean includedInternalFrames;
    private boolean areInternalFrames;

    public TruffleStackInfo(JPDADebugger debugger, ObjectVariable stackTrace) {
        this.debugger = debugger;
        this.stackTrace = stackTrace;
    }

    public TruffleStackFrame[] getStackFrames(boolean includeInternal) {
        if (stackFrames == null || includedInternalFrames != includeInternal) {
            stackFrames = loadStackFrames(includeInternal);
            this.includedInternalFrames = includeInternal;
        }
        return stackFrames;
    }
    
    public boolean hasInternalFrames() {
        return areInternalFrames;
    }
        
    private TruffleStackFrame[] loadStackFrames(boolean includeInternal) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable internalVar = debugger.createMirrorVar(includeInternal, true);
            Variable framesVar = debugAccessor.invokeMethod(METHOD_GET_FRAMES_INFO,
                                                            METHOD_GET_FRAMES_INFO_SIG,
                                                            new Variable[] { stackTrace,
                                                                             internalVar });
            Field[] framesInfos = ((ObjectVariable) framesVar).getFields(0, Integer.MAX_VALUE);
            String framesDesc = (String) framesInfos[0].createMirrorObject();
            Field[] codes = ((ObjectVariable) framesInfos[1]).getFields(0, Integer.MAX_VALUE);
            Field[] thiss = ((ObjectVariable) framesInfos[2]).getFields(0, Integer.MAX_VALUE);
            areInternalFrames = false;
            if (!includeInternal) {
                areInternalFrames = (Boolean) framesInfos[3].createMirrorObject();
            }
            int i1 = 0;
            int i2;
            int depth = 1;
            List<TruffleStackFrame> truffleFrames = new ArrayList<>();
            while ((i2 = framesDesc.indexOf("\n\n", i1)) > 0) {
                StringReference codeRef = (StringReference) ((JDIVariable) codes[depth-1]).getJDIValue();
                ObjectVariable frameInstance = (ObjectVariable) stackTrace.getFields(0, Integer.MAX_VALUE)[depth - 1];
                TruffleStackFrame tsf = new TruffleStackFrame(
                        debugger, depth, frameInstance, framesDesc.substring(i1, i2),
                        codeRef, null, (ObjectVariable) thiss[depth-1], includeInternal);
                truffleFrames.add(tsf);
                if (includeInternal && tsf.isInternal()) {
                    areInternalFrames = true;
                }
                i1 = i2 + 2;
                depth++;
            }
            return truffleFrames.toArray(new TruffleStackFrame[truffleFrames.size()]);
        } catch (InvalidExpressionException | NoSuchMethodException | InvalidObjectException ex) {
            Exceptions.printStackTrace(ex);
            return new TruffleStackFrame[] {};
        }
    }
    
}
