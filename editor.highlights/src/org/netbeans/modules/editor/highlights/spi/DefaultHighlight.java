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
package org.netbeans.modules.editor.highlights.spi;

import javax.swing.text.Position;
import org.netbeans.editor.Coloring;

/**
 *
 * @author Jan Lahoda
 */
public final class DefaultHighlight implements Highlight {

    private Coloring coloring;
    private Position start;
    private Position end;

    /** Creates a new instance of DefaultHighlight */
    public DefaultHighlight(Coloring coloring, Position start, Position end) {
        this.coloring = coloring;
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start.getOffset();
    }
    
    public int getEnd() {
        return end.getOffset();
    }
    
    public Coloring getColoring() {
        return coloring;
    }
    
}
