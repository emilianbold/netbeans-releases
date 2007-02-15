/*
 * DwarfArangesSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import org.netbeans.modules.cnd.dwarfdump.section.AddressRangeSet;

/**
 *
 * @author ak119685
 */
public class DwarfArangesSection extends ElfSection {
    ArrayList<AddressRangeSet> addressRangeSets = new ArrayList<AddressRangeSet>();
    
    /** Creates a new instance of DwarfArangesSection */
    public DwarfArangesSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    void addAddressRangeSet(AddressRangeSet addressRangeSet) {
        addressRangeSets.add(addressRangeSet);
    }
    
    public ArrayList<AddressRangeSet> getAddressRangeSets() {
        if (addressRangeSets.size() == 0) {
            try {
                read();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return addressRangeSets;
    }
    
    public DwarfArangesSection read() throws IOException {
        long sectionStart = header.getSectionOffset();
        long sectionEnd = header.getSectionSize() + sectionStart;
        
        reader.seek(sectionStart);
        
        while (reader.getFilePointer() != sectionEnd) {
            AddressRangeSet addressRangeSet = new AddressRangeSet();
            addressRangeSet.length = reader.readInt();
            addressRangeSet.version = reader.readShort();
            addressRangeSet.info_offset = reader.readInt();
            addressRangeSet.address_size = (byte)(0xff & reader.readByte());
            addressRangeSet.segment_descriptor_size = (byte)(0xff & reader.readByte());
            
            //  The first tuple following the header in each set begins at an
            // offset that is a multiple of the size of a single tuple
            int multTupleSize = addressRangeSet.address_size * 2;
            int hLength = 12; /* header size */
            
            while (multTupleSize < hLength) {
                multTupleSize <<= 1;
            }
            
            reader.skipBytes(multTupleSize - hLength);
            
            long address, length;

            do {
                address = reader.readNumber(addressRangeSet.address_size);
                length = reader.readNumber(addressRangeSet.address_size);
                addressRangeSet.addRange(address, length);
            } while (address != 0 || length != 0);

            addAddressRangeSet(addressRangeSet);
        }
        
        return this;
    }
    
    public void dump(PrintStream out) {
        super.dump(out);
        
        for (AddressRangeSet addressRangeSet : getAddressRangeSets()) {
            addressRangeSet.dump(out);
        }
        
        out.println();
    }
}
