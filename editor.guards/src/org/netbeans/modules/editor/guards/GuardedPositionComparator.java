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

package org.netbeans.modules.editor.guards;

import java.util.Comparator;
import org.netbeans.api.editor.guards.GuardedSection;

/** Comparator of the guarded sections. It compares the begin position
* of the sections.
*/
final class GuardedPositionComparator implements Comparator<GuardedSection> {
    /** Compare two objects. Both have to be either SimpleSection
    * either InteriorSection instance.
    */
    public int compare(GuardedSection o1, GuardedSection o2) {
        return getOffset(o1) - getOffset(o2);
    }

    /** Computes the offset of the begin of the section. */
    private int getOffset(GuardedSection o) {
        return o.getStartPosition().getOffset();
    }
}
