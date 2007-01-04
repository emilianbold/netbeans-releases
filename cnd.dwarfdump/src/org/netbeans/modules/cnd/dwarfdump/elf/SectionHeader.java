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

package org.netbeans.modules.cnd.dwarfdump.elf;

/**
 *
 * @author ak119685
 */
public class SectionHeader {
    public int sh_name = 0;        /* section name */
    public int sh_type = 0;        /* SHT_... */
    public int sh_flags = 0;       /* SHF_... */
    public long sh_addr = 0;       /* x virtual address */
    public long sh_offset = 0;     /* x file offset */
    public long sh_size = 0;       /* x section size */
    public int sh_link = 0;        /* misc info */
    public int sh_info = 0;        /* misc info */
    public long sh_addralign = 0;  /* x memory alignment */
    public long sh_entsize = 0;    /* x entry size if table */

    public int getSectionSize() {
        return (int)sh_size;
    }
    
    public long getSectionOffset() {
        return sh_offset;
    }
    
    public int getSectionEntrySize() {
        return (int)sh_entsize;
    }
}
