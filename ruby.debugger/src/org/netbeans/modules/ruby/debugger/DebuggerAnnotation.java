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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger;

import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.util.NbBundle;

/**
 * Debugger Annotation class.
 *
 * @author Martin Krauskopf
 */
public final class DebuggerAnnotation extends Annotation {
    
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint";
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint";
//    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "CondBreakpoint";
//    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "DisabledCondBreakpoint";
    public static final String CURRENT_LINE_ANNOTATION_TYPE = "CurrentPC";
    public static final String CURRENT_LINE_ANNOTATION_TYPE2 = "CurrentPC2";
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE = "CurrentPCLinePart";
    public static final String CURRENT_LINE_PART_ANNOTATION_TYPE2 = "CurrentPC2LinePart";
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE = "CallSite";
    
    private Annotatable annotatable;
    private String type;
    
    public DebuggerAnnotation(final String type, final Annotatable annotatable) {
        this.type = type;
        this.annotatable = annotatable;
        attach(annotatable);
    }
    
    public String getAnnotationType() {
        return type;
    }
    
    public String getShortDescription() {
        if (type == BREAKPOINT_ANNOTATION_TYPE) {
            return getMessage("TOOLTIP_BREAKPOINT"); // NOI18N
        } else if (type == DISABLED_BREAKPOINT_ANNOTATION_TYPE) {
            return getMessage("TOOLTIP_DISABLED_BREAKPOINT"); // NOI18N
//        } else if (type == CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
//            return getMessage("TOOLTIP_CONDITIONAL_BREAKPOINT"); // NOI18N
//        } else if (type == DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE) {
//            return getMessage("TOOLTIP_DISABLED_CONDITIONAL_BREAKPOINT"); // NOI18N
        } else if (type == CURRENT_LINE_ANNOTATION_TYPE) {
            return getMessage("TOOLTIP_CURRENT_LINE"); // NOI18N
        } else if (type == CURRENT_LINE_ANNOTATION_TYPE2) {
            return getMessage("TOOLTIP_CURRENT_LINE_2"); // NOI18N
        } else if (type == CURRENT_LINE_PART_ANNOTATION_TYPE) {
            return getMessage("TOOLTIP_CURRENT_LINE"); // NOI18N
        } else if (type == CURRENT_LINE_PART_ANNOTATION_TYPE2) {
            return getMessage("TOOLTIP_CURRENT_LINE"); // NOI18N
        } else if (type == CALL_STACK_FRAME_ANNOTATION_TYPE) {
            return getMessage("TOOLTIP_CALL_STACK_FRAME"); // NOI18N
        } else {
            return null;
        }
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getBundle(DebuggerAnnotation.class).getString(key);
    }
    
}
