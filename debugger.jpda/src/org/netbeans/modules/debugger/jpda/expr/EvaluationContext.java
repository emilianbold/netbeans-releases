/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import java.util.*;

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

    /**
     * Creates a new context in which to evaluate expresions.
     *
     * @param frame the frame in which context evaluation occurrs
     * @param imports list of imports
     * @param staticImports list of static imports
     */
    public EvaluationContext(StackFrame frame, List imports, List staticImports) {
        if (frame == null || imports == null || staticImports == null) throw new IllegalArgumentException("Neither argument may be null");
        this.frame = frame;
        this.sourceImports = imports;
        this.staticImports = staticImports;
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
}

