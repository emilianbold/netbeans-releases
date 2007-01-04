/*
 * StringTableSection.java
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class StringTableSection extends ElfSection {
    byte[] stringtable = null;
    
    public StringTableSection(ElfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
        read();
    }
    
    public StringTableSection read() {
        try {
            long filePos = reader.getFilePointer();
            reader.seek(header.getSectionOffset());
            stringtable = new byte[header.getSectionSize()];
            reader.read(stringtable);
            reader.seek(filePos);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return this;
    }
    
    public byte[] getStringTable() {
        return stringtable;
    }
    
    public String getString(int offset) {
        StringBuffer str = new StringBuffer();
        
        for (int i = offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }
        
        return str.toString();
    }
    
    public void dump(PrintStream out) {
        super.dump(out);
        
        if (stringtable == null) {
            out.println("<Empty table>");
            return;
        }
        
        int offset = 1;
        int idx = 0;

        out.printf("No.\tOffset\tString\n");
        
        while (offset < stringtable.length) {
            String string = getString(offset);
            out.printf("%d.\t%d\t%s\n", ++idx, offset, string);
            offset += string.length() + 1;
        }
    }
}
