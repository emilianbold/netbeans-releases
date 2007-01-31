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
    
    public void dump(PrintStream out) {
        out.println("\n** Section " + sectionName); // NOI18N
        header.dump(out);
        out.println("\nContent of the section " + sectionName + "\n"); // NOI18N
    }
    
    public ElfSection read() throws IOException {
        return null;
    }
}
