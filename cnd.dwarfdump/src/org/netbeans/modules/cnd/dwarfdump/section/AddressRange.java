/*
 * AddressRange.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

/**
 *
 * @author ak119685
 */
public class AddressRange {
    long length;
    int  version;
    long info_offset;
    byte address_size;
    byte segment_descriptor_size;
    
    /** Creates a new instance of AddressRange */
    public AddressRange() {
    }
    
}
