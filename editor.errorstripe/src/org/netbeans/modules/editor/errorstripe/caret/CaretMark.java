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

package org.netbeans.modules.editor.errorstripe.caret;
import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.errorstripe.AnnotationView;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public class CaretMark implements Mark {
    
    private static final Color COLOR = new Color(0xC0B883);
    
    private int line;
    
    public CaretMark(int line) {
        this.line = line;
    }
    
    public String getShortDescription() {
        return NbBundle.getMessage(AnnotationView.class, "TP_Current_Line");
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
        return TYPE_CARET;
    }
    
}
