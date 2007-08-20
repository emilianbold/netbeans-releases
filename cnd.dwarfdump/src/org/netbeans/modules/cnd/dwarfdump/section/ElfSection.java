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
 * ElfSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class ElfSection {
    ElfReader reader;
    SectionHeader header;
    int sectionIdx;
    String sectionName;
    
    public ElfSection(ElfReader reader, int sectionIdx) {
        this.reader = reader;
        this.sectionIdx = sectionIdx;
        this.header = reader.getSectionHeader(sectionIdx);
        this.sectionName = reader.getSectionName(sectionIdx);
    }

    public ElfSection(ElfReader reader, int sectionIdx, SectionHeader header, String sectionName) {
        this.reader = reader;
        this.sectionIdx = sectionIdx;
        this.header = header;
        this.sectionName = sectionName;
    }
    
    public void dump(PrintStream out) {
        out.println("\n** Section " + sectionName); // NOI18N
        header.dump(out);
        out.println("\nContent of the section " + sectionName + "\n"); // NOI18N
    }
    
    public ElfSection read() throws IOException {
        return null;
    }
}
