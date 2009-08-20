/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.annotationsupport;

import java.awt.Color;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author ak119685
 */
public class AnnotationMark implements Mark {
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
        return AnnotationSupport.getInstance().getAnnotationColor();
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
