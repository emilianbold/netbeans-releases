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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;

/**
 *
 */
public final class TruffleStackFrame {

    private static final Logger LOG = Logger.getLogger(TruffleStackFrame.class.getName());
    
    private final JPDADebugger debugger;
    private final int depth;
    private final ObjectVariable frameInstance;
    private final String methodName;
    private final String sourceLocation;
    
    private final int    sourceId;
    private final String sourceName;
    private final String sourcePath;
    private final URI    sourceURI;
    private final int    sourceLine;
    private final StringReference codeRef;
    private TruffleVariable[] vars;
    private final ObjectVariable thisObject;
    private final boolean isInternal;
    
    public TruffleStackFrame(JPDADebugger debugger, int depth,
                             ObjectVariable frameInstance,
                             String frameDefinition, StringReference codeRef,
                             TruffleVariable[] vars, ObjectVariable thisObject,
                             boolean includeInternal) {
        if (LOG.isLoggable(Level.FINE)) {
            try {
                LOG.fine("new TruffleStackFrame("+depth+", "+
                         frameInstance.getToStringValue()+" of type "+frameInstance.getClassType().getName()+
                         ", "+frameDefinition+", vars = "+Arrays.toString(vars)+
                         ", "+thisObject+")");
            } catch (InvalidExpressionException iex) {
                LOG.log(Level.FINE, iex.getMessage(), iex);
            }
        }
        this.debugger = debugger;
        this.depth = depth;
        this.frameInstance = frameInstance;
        boolean internalFrame = includeInternal;
        try {
            int i1 = 0;
            int i2 = frameDefinition.indexOf('\n');
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
            i2 = frameDefinition.indexOf('\n', i1);
            try {
                sourceURI = new URI(frameDefinition.substring(i1, i2));
            } catch (URISyntaxException usex) {
                throw new IllegalStateException("Bad URI: "+frameDefinition.substring(i1, i2), usex);
            }
            i1 = i2 + 1;
            if (includeInternal) {
                i2 = frameDefinition.indexOf('\n', i1);
                sourceLine = Integer.parseInt(frameDefinition.substring(i1, i2));
                i1 = i2 + 1;
                internalFrame = Boolean.valueOf(frameDefinition.substring(i1));
            } else {
                sourceLine = Integer.parseInt(frameDefinition.substring(i1));
            }
        } catch (IndexOutOfBoundsException ioob) {
            throw new IllegalStateException("frameDefinition='"+frameDefinition+"'", ioob);
        }
        this.codeRef = codeRef;
        this.vars = vars;
        this.thisObject = thisObject;
        this.isInternal = internalFrame;
    }
    
    public final JPDADebugger getDebugger() {
        return debugger;
    }
    
    public final int getDepth() {
        return depth;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getDisplayName() {
        if (!methodName.isEmpty()) {
            return methodName + " ("+sourceLocation+")";
        } else {
            return sourceLocation;
        }
    }
    
    public SourcePosition getSourcePosition() {
        Source src = Source.getExistingSource(debugger, sourceId);
        if (src == null) {
            src = Source.getSource(debugger, sourceId, sourceName, sourcePath, sourceURI, codeRef);
        }
        SourcePosition sp = new SourcePosition(debugger, sourceId, src, sourceLine);
        return sp;
    }
    
    public ObjectVariable getStackFrameInstance() {
        return frameInstance;// also is: (ObjectVariable) stackTrace.getFields(0, Integer.MAX_VALUE)[depth - 1];
    }
    
    public TruffleVariable[] getVars() {
        if (vars == null) {
            vars = TruffleAccess.createFrameVars(debugger, /*suspendedInfo,*/ getStackFrameInstance());
        }
        return vars;
    }
    
    public ObjectVariable getThis() {
        return thisObject;
    }
    
    public boolean isInternal() {
        return isInternal;
    }
    
}
