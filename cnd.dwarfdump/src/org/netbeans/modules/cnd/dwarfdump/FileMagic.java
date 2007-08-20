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

package org.netbeans.modules.cnd.dwarfdump;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 *
 * @author Alexander Simon
 */
public class FileMagic {
    private RandomAccessFile reader;
    private Magic magic;
    
    public FileMagic(String objFileName) throws FileNotFoundException, WrongFileFormatException {
        reader = new RandomAccessFile(objFileName, "r"); // NOI18N
        readMagic();
    }

    public RandomAccessFile getReader() {
        return reader;
    }

    public Magic getMagic() {
        return magic;
    }

    private void readMagic() throws WrongFileFormatException {
        byte[] bytes = new byte[8];
        try {
            reader.readFully(bytes);
        } catch (IOException ex) {
            dispose();
            throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
        }
        if (isElfMagic(bytes)) {
            magic = Magic.Elf;
        } else if (isCoffMagic(bytes)) {
            magic = Magic.Coff;
        } else if (isExeMagic(bytes)) {
            magic = Magic.Exe;
        } else if (isPeMagic(bytes)) {
            magic = Magic.Pe;
        } else if (isMachoMagic(bytes)) {
            magic = Magic.Macho;
        } else if (isArchiveMagic(bytes)) {
            magic = Magic.Arch;
        } else {
            dispose();
            throw new WrongFileFormatException("Not an ELF/PE/COFF/MACH-O file"); // NOI18N
        }
    }

    public void dispose(){
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            reader = null;
        }
    }
    
    public static boolean isExeMagic(byte[] bytes){
        return bytes[0] == 'M' && bytes[1] == 'Z';
    }

    public static boolean isPeMagic(byte[] bytes){
        return bytes[0] == 'P' && bytes[1] == 'E' && bytes[2] == 0 && bytes[3] == 0;
    }

    public static boolean isCoffMagic(byte[] bytes){
        return bytes[0] == 0x4c && bytes[1] == 0x01;
    }
    
    public static boolean isElfMagic(byte[] bytes){
        return bytes[0] == 0x7f && bytes[1] == 'E' && bytes[2] == 'L' && bytes[3] == 'F';
    }
    
    public static boolean isMachoMagic(byte[] bytes){
        return (bytes[0] == (byte)0xce || bytes[0] == (byte)0xcf) && bytes[1] == (byte)0xfa && bytes[2] == (byte)0xed && bytes[3] == (byte)0xfe;
    }
    
    public static boolean isArchiveMagic(byte[] bytes){
        return bytes[0] == '!' && bytes[1] == '<' && bytes[2] == 'a' && bytes[3] == 'r' &&
                bytes[4] == 'c' && bytes[5] == 'h' && bytes[6] == '>' && bytes[7] == '\n';
    }

}
