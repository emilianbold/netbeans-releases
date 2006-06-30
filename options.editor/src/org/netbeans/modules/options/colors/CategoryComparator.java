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

package org.netbeans.modules.options.colors;

import java.util.Comparator;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;


/**
 *
 * @author Jan Jancura
 */
public class CategoryComparator implements Comparator {

    public int compare (Object o1, Object o2) {
        String name_1 = name(o1);
        String name_2 = name(o2);
	if (name_1.startsWith ("default")) // NOI18N
	    return name_2.startsWith ("default") ? 0 : -1; // NOI18N
        if (name_2.startsWith ("default")) // NOI18N
            return 1;
	return name_1.compareTo (name_2);
    }
    
    private static String name (Object o) {
        return (String) ((AttributeSet) o).getAttribute(StyleConstants.NameAttribute);
    }
    
}
