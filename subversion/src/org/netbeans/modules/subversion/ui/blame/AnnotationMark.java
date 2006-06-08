/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.blame;

import java.awt.Color;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Maros Sandor
 */
final class AnnotationMark implements Mark {
    
    private static final Color COLOR = new Color(0x58,0x90,0xBE);
    
    private final int line;
    private final String message;
    
    public AnnotationMark(int line, String message) {
        this.line = line;
        this.message = message;
    }
    
    public String getShortDescription() {
        return message;
    }
    
    public int[] getAssignedLines() {
        return new int[] {line, line};
    }
    
    public Color getEnhancedColor() {
        return COLOR;
    }
    
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    public Status getStatus() {
        return Status.STATUS_OK;
    }
    
    public int getType() {
        return TYPE_ERROR_LIKE;
    }
}
