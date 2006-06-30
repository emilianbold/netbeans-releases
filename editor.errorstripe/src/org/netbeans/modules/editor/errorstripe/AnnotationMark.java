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

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationMark implements Mark {

    private AnnotationDesc annotation;

    /** Creates a new instance of AnnotationMark */
    public AnnotationMark(AnnotationDesc annotation) {
        this.annotation = annotation;
    }

    public Status getStatus() {
        AnnotationType type = annotation.getAnnotationTypeInstance();
//        System.err.println("type = " + type );
        AnnotationType.Severity severity = type.getSeverity();
        
//        System.err.println("severity = " + severity );
        
        return AnnotationViewDataImpl.get(severity);
    }

    public Color getEnhancedColor() {
        AnnotationType type = annotation.getAnnotationTypeInstance();
        
        return type.getCustomSidebarColor();
    }

    public int[] getAssignedLines() {
        return new int[] {annotation.getLine(), annotation.getLine()};
    }

    public String getShortDescription() {
        return annotation.getShortDescription();
    }

    public int getType() {
        return TYPE_ERROR_LIKE;
    }
    
    public int getPriority() {
        return annotation.getAnnotationTypeInstance().getPriority();
    }

    public AnnotationDesc getAnnotationDesc() {
        return annotation;
    }
    
}
