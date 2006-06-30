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

package org.netbeans.modules.ant.debugger;

import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;


/**
 * Debugger Annotation class.
 *
 * @author   Jan Jancura
 */
public class DebuggerAnnotation extends Annotation {

    /** Annotation type constant. */
    public static final String BREAKPOINT_ANNOTATION_TYPE =
        new String ("Breakpoint");
    /** Annotation type constant. */
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE =
        new String ("DisabledBreakpoint");
    /** Annotation type constant. */
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = 
        new String ("CondBreakpoint");
    /** Annotation type constant. */
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = 
        new String ("DisabledCondBreakpoint");
    /** Annotation type constant. */
    public static final String CURRENT_LINE_ANNOTATION_TYPE =
        new String ("CurrentPC");
    /** Annotation type constant. */
    public static final String CURRENT_LINE_ANNOTATION_TYPE2 =
        new String ("CurrentPC2");
    /** Annotation type constant. */
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE =
        new String ("CurrentPCLinePart");
    /** Annotation type constant. */
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 =
        new String ("CurrentPC2LinePart");
    /** Annotation type constant. */
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =
        new String ("CallSite");

    private Annotatable annotatable;
    private String      type;
    
    
    public DebuggerAnnotation (String type, Annotatable annotatable) {
        this.type = type;
        this.annotatable = annotatable;
        attach (annotatable);
    }
    
    public String getAnnotationType () {
        return type;
    }
    
    public String getShortDescription () {
        if (type == BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_BREAKPOINT"); // NOI18N
        else 
        if (type == DISABLED_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_DISABLED_BREAKPOINT"); // NOI18N
        else 
        if (type == CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_CONDITIONAL_BREAKPOINT"); // NOI18N
        else
        if (type == DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT"); // NOI18N
        else
        if (type == CURRENT_LINE_ANNOTATION_TYPE)
            return NbBundle.getMessage 
                (DebuggerAnnotation.class, "TOOLTIP_CURRENT_PC"); // NOI18N
        else
        if (type == CURRENT_LINE_ANNOTATION_TYPE2)
            return NbBundle.getMessage 
                (DebuggerAnnotation.class, "TOOLTIP_CURRENT_PC_2"); // NOI18N
        else
        if (type == CURRENT_LINE_PART_ANNOTATION_TYPE)
            return NbBundle.getMessage
                    (DebuggerAnnotation.class, "TOOLTIP_CURRENT_PC"); // NOI18N
        else
        if (type == CALL_STACK_FRAME_ANNOTATION_TYPE)
            return NbBundle.getBundle (DebuggerAnnotation.class).getString 
                ("TOOLTIP_CALLSITE"); // NOI18N
        return null;
    }
}
