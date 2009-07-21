/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            try {
                String qname = dwarfEntry.getQualifiedName();
                if (qname != null) {
                    out.println("\tQualified Name: " + qname); // NOI18N
                }
            } catch (IOException ex) {
                Logger.getLogger(DwarfAbbriviationTableEntry.class.getName()).log(Level.SEVERE, null, ex);
            }
            dumpAttributes(out, dwarfEntry.getValues());
        }
        
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

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(DwarfEntry dwarfEntry) {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(st);
        dump(out, dwarfEntry);
        return st.toString();
    }
}
