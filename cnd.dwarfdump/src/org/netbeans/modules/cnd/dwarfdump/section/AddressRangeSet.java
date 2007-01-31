/*
 * AddressRangeSet.java
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class AddressRangeSet {
    long length;
    int  version;
    long info_offset;
    byte address_size;
    byte segment_descriptor_size;
    
    private ArrayList<AddressRange> ranges = new ArrayList<AddressRange>();
    
    void addRange(long address, long length) {
        ranges.add(new AddressRange(address, length));
    }
    
    public void dump(PrintStream out) {
        out.println();
        out.println("  Length:\t\t" + length); // NOI18N
        out.println("  Version:\t\t" + version); // NOI18N
        out.println("  Offset info .debug_info: " + info_offset); // NOI18N
        out.println("  Pointer size:\t\t" + address_size); // NOI18N
        out.println("  Segment size:\t\t" + segment_descriptor_size); // NOI18N

        out.println("\n\tAddress\t\tLength"); // NOI18N

        for (AddressRange addressRange : ranges) {
            addressRange.dump(out);
        }
        
        out.println();
    }
    
}
