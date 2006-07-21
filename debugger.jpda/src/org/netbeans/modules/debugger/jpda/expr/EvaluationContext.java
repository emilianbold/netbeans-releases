/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.StackFrame;

import java.util.*;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * Defines the exection context in which to evaluate a given expression. The context consists of:
 * the current stack frame and the source file in which the expression would exist. The source file
 * is needed for the import facility to work.
 *
 * @author Maros Sandor
 */
public class EvaluationContext {

    /**
     * The runtime context of a JVM is represented by a stack frame.
     */
    private StackFrame frame;
    private List       sourceImports;
    private List       staticImports;
    private boolean canInvokeMethods;
    private Runnable methodInvokePreproc;
    private JPDADebuggerImpl debugger;

    /**
     * Creates a new context in which to evaluate expresions.
     *
     * @param frame the frame in which context evaluation occurrs
     * @param imports list of imports
     * @param staticImports list of static imports
     */
    public EvaluationContext(StackFrame frame, List imports, List staticImports,
                             boolean canInvokeMethods, Runnable methodInvokePreproc,
                             JPDADebuggerImpl debugger) {
        if (frame == null) throw new IllegalArgumentException("Frame argument must not be null");
        if (imports == null) throw new IllegalArgumentException("Imports argument must not be null");
        if (staticImports == null) throw new IllegalArgumentException("Static imports argument must not be null");
        this.frame = frame;
        this.sourceImports = imports;
        this.staticImports = staticImports;
        this.canInvokeMethods = canInvokeMethods;
        this.methodInvokePreproc = methodInvokePreproc;
        this.debugger = debugger;
    }

    public List getStaticImports() {
        return staticImports;
    }

    public List getImports() {
        return sourceImports;
    }

    public StackFrame getFrame() {
        return frame;
    }
    
    public boolean canInvokeMethods() {
        return canInvokeMethods;
    }
    
    void setCanInvokeMethods(boolean canInvokeMethods) {
        this.canInvokeMethods = canInvokeMethods;
    }
    
    void methodToBeInvoked() {
        if (methodInvokePreproc != null) {
            methodInvokePreproc.run();
        }
    }
    
    JPDADebuggerImpl getDebugger() {
        return debugger;
    }
    
}

