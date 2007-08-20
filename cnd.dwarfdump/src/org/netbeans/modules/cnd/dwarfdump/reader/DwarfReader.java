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

package org.netbeans.modules.cnd.dwarfdump.reader;

import java.io.RandomAccessFile;
import org.netbeans.modules.cnd.dwarfdump.Magic;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATE;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.FORM;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAbbriviationTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfArangesSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfMacroInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.ElfSection;
import org.netbeans.modules.cnd.dwarfdump.section.StringTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAttribute;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfNameLookupTableSection;
import java.io.IOException;

/**
 *
 * @author ak119685
 */
public class DwarfReader extends ElfReader {
    
    public DwarfReader(String fname, RandomAccessFile reader, Magic magic, long shift, long length) throws IOException {
        super(fname, reader, magic, shift, length);
        getSection(SECTIONS.DEBUG_STR); 
    }

    public Object readAttrValue(DwarfAttribute attr) throws IOException {
        Object value = readForm(attr.valueForm);
        
        if (attr.attrName.equals(ATTR.DW_AT_language)) {
            return LANG.get(((Number)value).intValue());
        }
        
        if (attr.attrName.equals(ATTR.DW_AT_encoding)) {
            return ATE.get(((Byte)value).byteValue());
        }
        
        if (attr.attrName.equals(ATTR.DW_AT_decl_line)) {
//            if (attr.valueForm.equals(FORM.DW_FORM_data2)) {
//                byte[] val = (byte[])value;
//                return new Integer((0xFF & val[0]) | ((0xFF & val[1]) << 8));
//            }
            return new Integer(((Number)value).intValue());
        }
        
        return value;
    }
    
    public Object readForm(FORM form) throws IOException {
        if (form.equals(FORM.DW_FORM_addr)) {
            return read(new byte[getAddressSize()]);
        } else if (form.equals(FORM.DW_FORM_block2)) {
            return read(new byte[readShort()]);
        } else if (form.equals(FORM.DW_FORM_block4)) {
            return read(new byte[readInt()]);
        } else if (form.equals(FORM.DW_FORM_data2)) {
            //TODO: check on all architectures!
            //return read(new byte[2]);
            return readShort();
        } else if (form.equals(FORM.DW_FORM_data4)) {
            //TODO: check on all architectures!
            //return read(new byte[4]);
            return readInt();
        } else if (form.equals(FORM.DW_FORM_data8)) {
            //TODO: check on all architectures!
            //return read(new byte[8]);
            return readLong();
        } else if (form.equals(FORM.DW_FORM_string)) {
            return readString();
        } else if (form.equals(FORM.DW_FORM_block)) {
            return read(new byte[readUnsignedLEB128()]);
        } else if (form.equals(FORM.DW_FORM_block1)) {
            return read(new byte[readUnsignedByte()]);
        } else if (form.equals(FORM.DW_FORM_data1)) {
            return readByte();
        } else if (form.equals(FORM.DW_FORM_flag)) {
            return readBoolean();
        } else if (form.equals(FORM.DW_FORM_sdata)) {
            return readSignedLEB128();
        } else if (form.equals(FORM.DW_FORM_strp)) {
            return ((StringTableSection)getSection(SECTIONS.DEBUG_STR)).getString(readInt()); 
        } else if (form.equals(FORM.DW_FORM_udata)) {
            return readUnsignedLEB128();
        } else if (form.equals(FORM.DW_FORM_ref_addr)) {
            return read(new byte[getAddressSize()]);
        } else if (form.equals(FORM.DW_FORM_ref1)) {
            return read(new byte[readUnsignedByte()]);
        } else if (form.equals(FORM.DW_FORM_ref2)) {
            return read(new byte[2]);
        } else if (form.equals(FORM.DW_FORM_ref4)) {
            return readInt();
        } else if (form.equals(FORM.DW_FORM_ref8)) {
            return read(new byte[8]);
        } else if (form.equals(FORM.DW_FORM_ref_udata)) {
            return read(new byte[readUnsignedLEB128()]);
        } else if (form.equals(FORM.DW_FORM_indirect)) {
            return readForm(FORM.get(readUnsignedLEB128()));
        } else {
            throw new IOException("unknown type " + form); // NOI18N
        }
    }
    
    @Override
    ElfSection initSection(Integer sectionIdx, String sectionName) {
        if (sectionName.equals(SECTIONS.DEBUG_STR)) {
            return new StringTableSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_ARANGES)) {
            return new DwarfArangesSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_INFO)) {
            return new DwarfDebugInfoSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_ABBREV)) {
            return new DwarfAbbriviationTableSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_LINE)) {
            return new DwarfLineInfoSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_MACINFO)) {
            return new DwarfMacroInfoSection(this, sectionIdx);
        }
        
        if (sectionName.equals(SECTIONS.DEBUG_PUBNAMES)) {
            return new DwarfNameLookupTableSection(this, sectionIdx);
        }
        
        return null;
    }
}
