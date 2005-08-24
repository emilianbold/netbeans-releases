/*
 * CategoryComparator.java
 *
 * Created on 22. èervenec 2005, 10:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.options.colors;

import java.util.Comparator;
import org.netbeans.modules.options.colors.ColorModel.Category;


/**
 *
 * @author Administrator
 */
public class CategoryComparator implements Comparator {
    public int compare (Object o1, Object o2) {
	if (((Category) o1).getDisplayName ().startsWith ("Default")) 
	    return ((Category) o2).getDisplayName ().startsWith ("Default") ? 0 : -1;
        if (((Category) o2).getDisplayName ().startsWith ("Default"))
            return 1;
	return ((Category) o1).getDisplayName ().compareTo (
	    ((Category) o2).getDisplayName ()
	);
    }
}
