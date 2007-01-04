/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfNameLookupTable {
    public int unit_length;
    public int version;
    public int debug_info_offset;
    public int debug_info_length;

    public ArrayList<DwarfNameLookupEntry> entries = new ArrayList<DwarfNameLookupEntry>();
    
    public void dump(PrintStream out) {
        out.println("\nPublic names:\n");
        out.printf("  %-40s %s\n", "Length:", unit_length);
        out.printf("  %-40s %s\n", "Version:", version);
        out.printf("  %-40s %s\n", "Offset into .debug_info section:", debug_info_offset);
        out.printf("  %-40s %s\n", "Size of area in .debug_info section:", debug_info_length);
        out.println("\n    Offset      Name");
    
        for (DwarfNameLookupEntry entry : entries) {
            entry.dump(out);
        }
    }
    
}
