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

package org.netbeans.modules.cnd.dwarfdump.elf;

import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class SectionHeader {
    public String name;            /* section name */
    public long sh_name = 0;       /* section name */
    public long sh_type = 0;       /* SHT_... */
    public long sh_flags = 0;      /* SHF_... */
    public long sh_addr = 0;       /* x virtual address */
    public long sh_offset = 0;     /* x file offset */
    public long sh_size = 0;       /* x section size */
    public long sh_link = 0;       /* misc info */
    public long sh_info = 0;       /* misc info */
    public long sh_addralign = 0;  /* x memory alignment */
    public long sh_entsize = 0;    /* x entry size if table */

    public long getSectionSize() {
        return sh_size;
    }
    
    public long getSectionOffset() {
        return sh_offset;
    }
    
    public long getSectionEntrySize() {
        return sh_entsize;
    }
    
    public String getSectionName(){
        return name;
    }
    
    public void dump(PrintStream out) {
        out.println("Elf section header:"); // NOI18N
        out.printf("  %-20s %s\n", "Offset:", sh_offset); // NOI18N
        out.printf("  %-20s %s\n", "Length:", sh_size); // NOI18N
        out.printf("  %-20s %s\n", "Memory alignment:", sh_addralign); // NOI18N
        out.println();
    }
}
