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
    
    @Override
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
    
    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        
        for (AddressRangeSet addressRangeSet : getAddressRangeSets()) {
            addressRangeSet.dump(out);
        }
        
        out.println();
    }
}
