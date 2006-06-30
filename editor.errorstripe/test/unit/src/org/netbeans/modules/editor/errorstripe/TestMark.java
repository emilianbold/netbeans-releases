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
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class TestMark implements Mark {

    private Status status;
    private String description;
    private Color  color;
    private int[]  lines;
    private int    priority;

    public TestMark(Status status, String description, Color color, int[] lines) {
        this(status, description, color, lines, PRIORITY_DEFAULT);
    }
    
    public TestMark(Status status, String description, Color color, int[] lines, int priority) {
        this.status = status;
        this.description = description;
        this.color = color;
        this.lines = lines;
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public String getShortDescription() {
        return description;
    }

    public Color getEnhancedColor() {
        return color;
    }

    public int[] getAssignedLines() {
        return lines;
    }

    public int getType() {
        return TYPE_ERROR_LIKE;
    }
    
    public int getPriority() {
        return priority;
    }
    
}
