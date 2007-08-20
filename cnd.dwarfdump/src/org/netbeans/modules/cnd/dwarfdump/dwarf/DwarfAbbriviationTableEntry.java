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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAttribute;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfAbbriviationTableEntry {
    private long index;
    private long tag;
    private boolean hasChildren;
    private ArrayList<DwarfAttribute> attributes = new ArrayList<DwarfAttribute>();
    
    public DwarfAbbriviationTableEntry(long index, long tag, boolean hasChildren) {
        this.index = index;
        this.tag = tag;
        this.hasChildren = hasChildren;
    }
    
    public void addAttribute(int attrName, int valueForm) {
        if (attrName != 0 && valueForm != 0) {
            attributes.add(new DwarfAttribute(attrName, valueForm));
        }
    }
    
    public long getTableIndex() {
        return index;
    }
    
    public int getAttribute(ATTR attrName) {
        for (int i = 0; i < attributes.size(); i++) {
            DwarfAttribute attr = attributes.get(i);
            if (attr.attrName.equals(attrName)) {
                return i;
            }
        }
        
        return -1;
    }
        
    public DwarfAttribute getAttribute(int idx) {
        return attributes.get(idx);
    }
    
    public int getAttributesCount() {
        return attributes.size();
    }
    
    public boolean hasChildren() {
        return hasChildren;
    }
    
    public void dump() {
        dump(System.out, null);
    }
    
    public void dump(PrintStream out) {
        dump(out, null);
    }

    public void dump(PrintStream out, DwarfEntry dwarfEntry) {
        out.println("Abbrev Number: " + index + " (" + getKind() + ") " + " : " + (hasChildren ? "[has children]" : "[no children]")); // NOI18N
        
        if (dwarfEntry != null) {
            String qname = dwarfEntry.getQualifiedName();
            if (qname != null) {
                out.println("\tQualified Name: " + qname); // NOI18N
            }
        }
        
        dumpAttributes(out, dwarfEntry.getValues());
    }
    
    public TAG getKind() {
        return TAG.get((int)tag);
    }
    
    private void dumpAttributes(PrintStream out, ArrayList<Object> values) {
        for (int i = 0; i < getAttributesCount(); i++) {
            if (values == null) {
                getAttribute(i).dump(out);
            } else {
                getAttribute(i).dump(out, values.get(i));
            }
        }
    }
}
