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
package org.netbeans.modules.editor.highlights;

import java.util.Comparator;
import org.netbeans.modules.editor.highlights.spi.*;

/**
 *
 * @author Jan Lahoda
 */
public final class HighlightComparator implements Comparator/*<Highlight>*/ {

    /** Creates a new instance of HighlightComparator */
    public HighlightComparator() {
    }

    public int compare(Object o1, Object o2) {
        return compare((Highlight) o1, (Highlight) o2);
    }

    public int compare(Highlight o1, Highlight o2) {
        return o1.getEnd() - o2.getEnd();
    }
    
}
