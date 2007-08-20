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

    public StringTableSection(ElfReader reader, byte[] stringtable) {
        super(null, 0, null, null);
        this.stringtable = stringtable;
    }
    
    @Override
    public StringTableSection read() {
        try {
            long filePos = reader.getFilePointer();
            reader.seek(header.getSectionOffset());
            stringtable = new byte[(int)header.getSectionSize()];
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
    
    public String getString(long offset) {
        StringBuilder str = new StringBuilder();
        
        for (int i = (int)offset; i < stringtable.length; i++) {
            if (stringtable[i] == 0) {
                break;
            }
            str.append((char)stringtable[i]);
        }
        
        return str.toString();
    }
    
    @Override
    public void dump(PrintStream out) {
        super.dump(out);
        
        if (stringtable == null) {
            out.println("<Empty table>"); // NOI18N
            return;
        }
        
        int offset = 1;
        int idx = 0;

        out.printf("No.\tOffset\tString\n"); // NOI18N
        
        while (offset < stringtable.length) {
            String string = getString(offset);
            out.printf("%d.\t%d\t%s\n", ++idx, offset, string); // NOI18N
            offset += string.length() + 1;
        }
    }
}
