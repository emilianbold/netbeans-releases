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
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleSlotVariable;

/**
 *
 * @author Martin
 */
public class TruffleStackFrame {

    private final JPDADebugger debugger;
    private final int depth;
    private final ObjectVariable frameInstance;
    private final ObjectVariable stackTrace;
    private final String callTargetName;
    private final String methodName;
    private final String sourceLocation;
    
    private final int    sourceId;
    private final String sourceName;
    private final String sourcePath;
    private final int    sourceLine;
    private final StringReference codeRef;
    private TruffleSlotVariable[] vars;
    private final ObjectVariable thisObject;
    
    /*
    TruffleStackFrame(int depth, String callTargetName, String methodName, String sourceLocation) {
        this.depth = depth;
        this.callTargetName = callTargetName;
        this.methodName = methodName;
        this.sourceLocation = sourceLocation;
    }
    */

    public TruffleStackFrame(JPDADebugger debugger, int depth,
                             ObjectVariable frameInstance, ObjectVariable stackTrace,
                             String frameDefinition, StringReference codeRef,
                             TruffleSlotVariable[] vars, ObjectVariable thisObject) {
        /*
        try {
            System.err.println("new TruffleStackFrame("+depth+", "+frameInstance.getToStringValue()+" of type "+frameInstance.getClassType().getName());
        } catch (InvalidExpressionException iex) {
            iex.printStackTrace();
        }*/
        this.debugger = debugger;
        this.depth = depth;
        this.frameInstance = frameInstance;
        this.stackTrace = stackTrace;
        int i1 = 0;
        int i2 = frameDefinition.indexOf('\n');
        callTargetName = frameDefinition.substring(i1, i2);
        i1 = i2 + 1;
        i2 = frameDefinition.indexOf('\n', i1);
        methodName = frameDefinition.substring(i1, i2);
        i1 = i2 + 1;
        i2 = frameDefinition.indexOf('\n', i1);
        sourceLocation = frameDefinition.substring(i1, i2);
        i1 = i2 + 1;
        i2 = frameDefinition.indexOf('\n', i1);
        sourceId = Integer.parseInt(frameDefinition.substring(i1, i2));
        i1 = i2 + 1;
        i2 = frameDefinition.indexOf('\n', i1);
        sourceName = frameDefinition.substring(i1, i2);
        i1 = i2 + 1;
        i2 = frameDefinition.indexOf('\n', i1);
        sourcePath = frameDefinition.substring(i1, i2);
        i1 = i2 + 1;
        sourceLine = Integer.parseInt(frameDefinition.substring(i1));
        this.codeRef = codeRef;
        this.vars = vars;
        this.thisObject = thisObject;
    }
    
    public final JPDADebugger getDebugger() {
        return debugger;
    }
    
    public final int getDepth() {
        return depth;
    }
    
    public String getCallTargetName() {
        return callTargetName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getDisplayName() {
        return methodName + " ("+sourceLocation+")";
    }
    
    public SourcePosition getSourcePosition() {
        Source src = Source.getExistingSource(debugger, sourceId);
        if (src == null) {
            src = Source.getSource(debugger, sourceId, sourceName, sourcePath, codeRef);
        }
        SourcePosition sp = new SourcePosition(debugger, sourceId, src, sourceLine);
        return sp;
    }
    
    public ObjectVariable getStackFrameInstance() {
        return frameInstance;// also is: (ObjectVariable) stackTrace.getFields(0, Integer.MAX_VALUE)[depth - 1];
    }
    
    public TruffleSlotVariable[] getVars() {
        if (vars == null) {
            vars = TruffleAccess.createVars(debugger, getStackFrameInstance());
        }
        return vars;
    }
    
    public ObjectVariable getThis() {
        return thisObject;
    }
    
}
