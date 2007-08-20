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
