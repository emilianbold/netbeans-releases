/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.versioning.diff;

import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.netbeans.api.diff.Difference;

import java.awt.*;

/**
 * Error stripe mark for differences.
 *
 * @author Maros Sandor
 */
final class DiffMark implements Mark {

    private final int[] span;
    private final Color color;
    private final String desc;

    public DiffMark(Difference difference, Color color) {
        if (difference.getType() == Difference.DELETE) {
            int start = difference.getSecondStart() - 1;
            if (start < 0) start = 0;
            span = new int[] { start, start };
        } else {
            span = new int[] { difference.getSecondStart() - 1, difference.getSecondEnd() - 1 };
        }
        this.color = color;
        desc = DiffSidebar.getShortDescription(difference);
    }

    public int getType() {
        return TYPE_ERROR_LIKE;
    }

    public Status getStatus() {
        return Status.STATUS_OK;
    }

    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    public Color getEnhancedColor() {
        return color;
    }

    public int[] getAssignedLines() {
        return span;
    }

    public String getShortDescription() {
        return desc;
    }
}
