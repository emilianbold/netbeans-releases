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

import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;

public class ElfHeader {
    public int elfClass = 0;       /* File class */
    public int elfData = 0;        /* Data encoding */
    public int elfVersion = 0;     /* File version */
    public int elfOs = 0;          /* Operating system/ABI identification */
    public int elfAbi = 0;         /* ABI version */
    
    // Elf Header
    public short e_type = 0;       /* file type */
    public short e_machine = 0;    /* target machine */
    public int   e_version = 0;    /* file version */
    public long  e_entry = 0;      /* start address */
    public long  e_phoff = 0;      /* phdr file offset */
    public long  e_shoff = 0;      /* shdr file offset */
    public int   e_flags = 0;      /* file flags */
    public short e_ehsize = 0;     /* sizeof ehdr */
    public short e_phentsize = 0;  /* sizeof phdr */
    public short e_phnum = 0;      /* number phdrs */
    public short e_shentsize = 0;  /* sizeof shdr */
    public short e_shnum = 0;      /* number shdrs */
    public short e_shstrndx = 0;   /* shdr string index */
      
    public boolean isMSBData() {
        return elfData == ElfConstants.ELFDATA2MSB;
    }

    public boolean isLSBData() {
        return elfData == ElfConstants.ELFDATA2LSB;
    }
    
    public boolean is32Bit() {
        return elfClass == ElfConstants.ELFCLASS32;
    }
    
    public boolean is64Bit() {
        return elfClass == ElfConstants.ELFCLASS64;
    }
    
    public long getSectionHeaderOffset() {
        return e_shoff;
    }
    
    public int getDataEncoding() {
        return elfData;
    }

    public int getFileClass() {
        return elfClass;
    }
    
    public int getNumberOfSectionHeaders() {
        return e_shnum;
    }
    
    public short getELFStringTableSectionIndex() {
        return e_shstrndx;
    }
}
