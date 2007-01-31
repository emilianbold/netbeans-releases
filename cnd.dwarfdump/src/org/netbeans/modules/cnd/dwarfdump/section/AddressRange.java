/*
 * AddressRange.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import java.io.PrintStream;

/**
 *
 * @author ak119685
 */
public class AddressRange {
    private long address;
    private long length;
    
    public AddressRange(long address, long length) {
        this.address = address;
        this.length = length;
    }
    
    public void dump(PrintStream out) {
        out.printf("\t0x%08x\t%d (0x%x)\n", address, length, length); // NOI18N
    }
}

