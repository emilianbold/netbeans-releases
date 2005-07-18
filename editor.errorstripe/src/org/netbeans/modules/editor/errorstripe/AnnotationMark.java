/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
        return PRIORITY_DEFAULT;
    }

}
