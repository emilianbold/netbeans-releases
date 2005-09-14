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
import org.netbeans.api.editor.settings.EditorStyleConstants;


/**
 *
 * @author Jan Jancura
 */
public class CategoryComparator implements Comparator {
    public int compare (Object o1, Object o2) {
	if (name (o1).startsWith ("Default")) 
	    return name (o2).startsWith ("Default") ? 0 : -1;
        if (name (o2).startsWith ("Default"))
            return 1;
	return name (o1).compareTo (name (o2));
    }
    
    private static String name (Object o) {
        return (String) ((AttributeSet) o).getAttribute 
            (EditorStyleConstants.DisplayName);
    }
}
