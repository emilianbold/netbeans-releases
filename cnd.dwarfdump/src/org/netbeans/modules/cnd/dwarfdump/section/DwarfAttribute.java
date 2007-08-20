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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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
        out.print("\t" + attrName + " [" + valueForm + "]"); // NOI18N
        
        if (value != null) {
            if (valueForm.equals(FORM.DW_FORM_ref4)) {
                out.printf(" <%x>", value); // NOI18N
            } else if (valueForm.equals(FORM.DW_FORM_block1)) {
                byte[] data = (byte[])value;
                out.printf(" %d bytes: ", data.length); // NOI18N
                for (int i = 0; i < data.length; i++) {
                    out.printf(" 0x%x", data[i]); // NOI18N
                }
            } else {
                out.printf(" %s", value.toString()); // NOI18N
            }
            
            out.printf("\n"); // NOI18N
        } else {
            out.println(""); // NOI18N
        }
    }
    
    public void dump(PrintStream out) {
        dump(out, null);
    }
    
    public void dump() {
        dump(System.out, null);
    }
}

