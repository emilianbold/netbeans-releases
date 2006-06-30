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

package org.netbeans.lib.editor.util.swing;

import javax.swing.text.Element;

/**
 * Various utility methods related to elements.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class ElementUtilities {

    private ElementUtilities() {
        // No instances
    }

    public static void updateOffsetRange(Element[] elements, int[] offsetRange) {
        int elementsLength = elements.length;
        if (elementsLength > 0) {
            offsetRange[0] = Math.min(offsetRange[0], elements[0].getStartOffset());
            offsetRange[1] = Math.max(offsetRange[1], elements[elementsLength - 1].getEndOffset());
        }
    }

}
