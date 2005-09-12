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
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.EditorStyleConstants;


/**
 *
 * @author Administrator
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
