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

package org.netbeans.modules.editor.errorstripe.caret;

import java.awt.Color;
import org.netbeans.modules.editor.errorstripe.AnnotationView;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public class CaretMark implements Mark {

    private static final Color COLOR = new Color( 110, 110, 110 ); //new Color(0xC0B883);

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
    
    public static Color getCaretMarkColor() {
        return COLOR;
    }
    
}
