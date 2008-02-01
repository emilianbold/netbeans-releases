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

package org.netbeans.modules.bpel.debugger.api;

import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Yaroslavskiy
 */
public final class AnnotationType {

    public static final AnnotationType CURRENT_POSITION = new AnnotationType(
            "CurrentPC", "LBL_Current_Position"); // NOI18N
    
    public static final AnnotationType CURRENTLY_EXECUTING = new AnnotationType(
            "CurrentlyExecuting", "LBL_Currently_Executing", true, false); // NOI18N

    public static final AnnotationType ENABLED_BREAKPOINT = new AnnotationType(
            "BpelBreakpoint_normal", "LBL_Enabled_Breakpoint"); // NOI18N

    public static final AnnotationType DISABLED_BREAKPOINT = new AnnotationType(
            "BpelBreakpoint_disabled", "LBL_Disabled_Breakpoint"); // NOI18N
    
    public static final AnnotationType BROKEN_BREAKPOINT = new AnnotationType(
            "BpelBreakpoint_broken", "LBL_Broken_Breakpoint", false, true);
    
    public static final AnnotationType NEVER_EXECUTED_ELEMENT = new AnnotationType(
            "NeverExecutedElement", "LBL_Never_Executed_Element", true, false); // NOI18N
    
    public static final AnnotationType STARTED_ELEMENT = new AnnotationType(
            "StartedElement", "LBL_Started_Element", true, false); //NOI18N
    
    public static final AnnotationType COMPLETED_ELEMENT = new AnnotationType(
            "CompletedElement", "LBL_Completed_Element", true, false); //NOI18N
    
    public static final AnnotationType FAULTED_ELEMENT = new AnnotationType(
            "FaultedElement", "LBL_Faulted_Element", true, false); //NOI18N
    
    private String myType;
    private String myDescription;
    private boolean myIsForDiagram;
    private boolean myIsForSourceEditor;
    
    private AnnotationType(
            final String type, 
            final String nlsDescKey) {
        
        this(type, nlsDescKey, true, true);
    }
    
    private AnnotationType(
            final String type,
            final String nlsDescKey,
            final boolean isForDiagram,
            final boolean isForSourceEditor) {
        
        myType = type;
        myDescription = NbBundle.getMessage(AnnotationType.class, nlsDescKey);
        myIsForDiagram = isForDiagram;
        myIsForSourceEditor = isForSourceEditor;
    }
    
    /**
     * Returns user friendly description for this annotation type.
     * @return user friendly description for this annotation type
     */
    public String getDescription() {
        return myDescription;
    }
    
    /**
     * Returns key <code>String</code> for this annotation type. 
     * @return key <code>String</code> for this annotation type
     */
    public String getType() {
        return myType;
    }
    
    public boolean isForDiagram() {
        return myIsForDiagram;
    }
    
    public boolean isForSourceEditor() {
        return myIsForSourceEditor;
    }
}
