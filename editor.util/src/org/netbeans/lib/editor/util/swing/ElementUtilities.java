/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
