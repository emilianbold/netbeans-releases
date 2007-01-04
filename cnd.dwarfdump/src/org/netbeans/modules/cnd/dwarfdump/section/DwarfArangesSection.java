/*
 * DwarfArangesSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.elf.SectionHeader;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class DwarfArangesSection extends ElfSection {
    ArrayList<AddressRange> ranges = new ArrayList<AddressRange>();
    
    /** Creates a new instance of DwarfArangesSection */
    public DwarfArangesSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    void addAddressRange(AddressRange arange) {
        ranges.add(arange);
    }
    
    public ArrayList<AddressRange> getRanges() {
        return ranges;
    }
    
    public DwarfArangesSection read() throws IOException {
        reader.seek(header.getSectionOffset());
        
        while (header.getSectionSize() != reader.getFilePointer() - header.getSectionOffset()) {
            AddressRange arange = new AddressRange();
            arange.length = reader.readInt();
            arange.version = reader.readShort();
            arange.info_offset = reader.readInt();
            arange.address_size = (byte)(0xff & reader.readByte());
            arange.segment_descriptor_size = (byte)(0xff & reader.readByte());
            
            reader.skipBytes(arange.address_size);
            
            long address = -1, length = -1;
            
            while (address !=0 || length !=0) {
                address = reader.readNumber(arange.address_size);
                length = reader.readNumber(arange.address_size);
            }
            
            addAddressRange(arange);
        }
        
        return this;
    }
    
    public void dump(PrintStream out) {
        for (AddressRange range : ranges) {
            out.println("arange: " + Long.toHexString(range.info_offset));
        }
    }
}
