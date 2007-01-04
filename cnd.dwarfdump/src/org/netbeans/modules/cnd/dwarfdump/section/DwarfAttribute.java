/*
 * DwardAttribute.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.FORM;
import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class DwarfAttribute {
    public ATTR attrName;
    public FORM valueForm;

    public DwarfAttribute(int nameOrdinal, int formOrdinal) {
        this.attrName = ATTR.get(nameOrdinal);
        this.valueForm = FORM.get(formOrdinal);
    }
    
    public void dump(PrintStream out, Object value) {
        out.print("\t" + attrName + " [" + valueForm + "]");
        
        if (value != null) {
            String val;
            
            if (valueForm.equals(FORM.DW_FORM_ref4)) {
                val = "<" + Integer.toHexString(((Integer)value).intValue()) + ">";
            } else {
                val = value.toString();
            }
            
            out.println("  " +  val);
        } else {
            out.println("");
        }
    }
    
    public void dump(PrintStream out) {
        dump(out, null);
    }
    
    public void dump() {
        dump(System.out, null);
    }
}

