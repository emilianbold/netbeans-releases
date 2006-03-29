/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
