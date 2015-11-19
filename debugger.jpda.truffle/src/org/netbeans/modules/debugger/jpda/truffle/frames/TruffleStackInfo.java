/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.frames;

import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
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
public class TruffleStackInfo {
    
    private static final String METHOD_GET_FRAMES_INFO = "getFramesInfo";       // NOI18N
    //private static final String METHOD_GET_FRAMES_INFO_SIG = "([Lcom/oracle/truffle/api/frame/FrameInstance;)[Lorg/netbeans/modules/debugger/jpda/backend/truffle/TruffleFrame;";   // NOI18N
    private static final String METHOD_GET_FRAMES_INFO_SIG = "([Lcom/oracle/truffle/api/frame/FrameInstance;)[Ljava/lang/Object;";   // NOI18N
    
    private final JPDADebugger debugger;
    private final ObjectVariable stackTrace;
    private TruffleStackFrame[] stackFrames;

    public TruffleStackInfo(JPDADebugger debugger, Variable[] frameSlots, ObjectVariable stackTrace) {
        this.debugger = debugger;
        this.stackTrace = stackTrace;
        /*
        int n = stackTrace.length;
        this.stackFrames = new TruffleStackFrame[n];
        for (int i = 0; i < n; i++) {
            this.stackFrames[i] = new TruffleStackFrame(this, (ObjectVariable) stackTrace[i]);
        }*/
    }

    public TruffleStackFrame[] getStackFrames() {
        if (stackFrames == null) {
            stackFrames = loadStackFrames();
        }
        return stackFrames;
    }
        
    private TruffleStackFrame[] loadStackFrames() {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable framesVar = debugAccessor.invokeMethod(METHOD_GET_FRAMES_INFO,
                                                            METHOD_GET_FRAMES_INFO_SIG,
                                                            new Variable[] { stackTrace });
            /*
            Field[] frames = ((ObjectVariable) framesVar).getFields(0, Integer.MAX_VALUE);
            int n = frames.length;
            TruffleStackFrame[] truffleFrames = new TruffleStackFrame[n];
            for (int i = 0; i < n; i++) {
            String callTargetName = ((ObjectVariable) frames[i]).getField("callTargetName").getValue();
            String methodName = ((ObjectVariable) frames[i]).getField("methodName").getValue();
            String sourceLocation = ((ObjectVariable) frames[i]).getField("sourceLocation").getValue();
            truffleFrames[i] = new TruffleStackFrame(callTargetName, methodName, sourceLocation);
            }
             */
            Field[] framesInfos = ((ObjectVariable) framesVar).getFields(0, Integer.MAX_VALUE);
            String framesDesc = (String) framesInfos[0].createMirrorObject();
            Field[] codes = ((ObjectVariable) framesInfos[1]).getFields(0, Integer.MAX_VALUE);
            Field[] thiss = ((ObjectVariable) framesInfos[2]).getFields(0, Integer.MAX_VALUE);
            int i1 = 0;
            int i2;
            int depth = 1;
            List<TruffleStackFrame> truffleFrames = new ArrayList<>();
            while ((i2 = framesDesc.indexOf("\n\n", i1)) > 0) {
                StringReference codeRef = (StringReference) ((JDIVariable) codes[depth-1]).getJDIValue();
                ObjectVariable frameInstance = (ObjectVariable) stackTrace.getFields(0, Integer.MAX_VALUE)[depth - 1];
                TruffleStackFrame tsf = new TruffleStackFrame(debugger, depth, frameInstance, stackTrace, framesDesc.substring(i1, i2), codeRef, null, (ObjectVariable) thiss[depth-1]);
                truffleFrames.add(tsf);
                i1 = i2 + 2;
                depth++;
            }
            return truffleFrames.toArray(new TruffleStackFrame[truffleFrames.size()]);
        } catch (InvalidExpressionException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return new TruffleStackFrame[] {};
        }
    }
    
}
