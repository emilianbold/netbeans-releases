/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import org.netbeans.modules.editor.errorstripe.spi.Mark;
import org.netbeans.modules.editor.errorstripe.spi.Status;

/**
 *
 * @author Jan Lahoda
 */
public class TestMark implements Mark {
    
    private Status status;
    private String description;
    private Color  color;
    private int[]  lines;
    
    /** Creates a new instance of TestMark */
    public TestMark(Status status, String description, Color color, int[] lines) {
        this.status = status;
        this.description = description;
        this.color = color;
        this.lines = lines;
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
    
}
