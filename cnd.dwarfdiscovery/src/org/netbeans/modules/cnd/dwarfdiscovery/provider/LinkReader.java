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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 *
 * @author Alexander Simon
 */
public class LinkReader {
    private RandomAccessFile reader;
    private String sourcePath;
    private String path;
    private boolean isLSB = true;
    
    /** Creates a new instance of LinkReader */
    public LinkReader(String objFileName) throws FileNotFoundException, WrongFileFormatException {
        reader = new RandomAccessFile(objFileName, "r"); // NOI18N
        path = objFileName;
        readMagic();
    }
    
    public String getSource() {
        return sourcePath;
    }
    
    private void readMagic() throws WrongFileFormatException {
        byte[] bytes = new byte[4];
        try {
            reader.readFully(bytes);
            if (isLinkMagic(bytes)) {
                reader.seek(0x14);
                reader.readFully(bytes);
                readFlags(bytes);
                int position = 0x4C;
                int size;
                reader.seek(position);
                if (isShellItemPresent) {
                    size = (int)readNumber(2);
                    position += size;
                    reader.seek(position);
                }
                // file location always present
                size = (int)readNumber(2);
                position += size + 2;
                reader.seek(position);
                if (isShellItemPresent) {
                    //read location
                }
                if (isDescriptionPresent) {
                    size = (int)readNumber(2);
                    //String description = getString(size);
                    position += size + 2;
                    reader.seek(position);
                }
                if (isRelativePathPresent) {
                    size = (int)readNumber(2);
                    sourcePath = getString(size);
                    if (sourcePath.length()>1 && sourcePath.charAt(1) != ':') {
                        int i = path.lastIndexOf('\\');
                        if (i < 0){
                            i = path.lastIndexOf('/');
                        }
                        if (i > 0){
                            sourcePath = path.substring(0,i+1)+sourcePath;
                        }
                    }
                    return;
                }
            } else {
                throw new WrongFileFormatException(); // NOI18N
            }
        } catch (IOException ex) {
            throw new WrongFileFormatException("Not an link file"); // NOI18N
        } finally {
            dispose();
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
    
    private boolean isLinkMagic(byte[] bytes){
        return bytes[0] == 'L' && bytes[1] == 0 && bytes[2] == 0 && bytes[3] == 0;
    }
    
    private boolean isShellItemPresent;
    private boolean isFileLocationItemPresent;
    private boolean isDescriptionPresent;
    private boolean isRelativePathPresent;
    
    private void readFlags(byte[] bytes){
        int flag = bytes[0];
        if ((flag&1) != 0){
            isShellItemPresent = true;
            //System.out.println("The shell item id list is present.");
        }
        if ((flag&2) != 0){
            isFileLocationItemPresent = true;
            //System.out.println("Points to a file or directory.");
        }
        if ((flag&4) != 0){
            isDescriptionPresent = true;
            //System.out.println("Has a description string.");
        }
        if ((flag&8) != 0){
            isRelativePathPresent = true;
            //System.out.println("Has a relative path string.");
        }
//        if ((flag&16) != 0){
//            System.out.println("Has a working directory.");
//        }
//        if ((flag&32) != 0){
//            System.out.println("Has command line arguments.");
//        }
//        if ((flag&64) != 0){
//            System.out.println("Has a custom icon.");
//        }
    }
    
    private long readNumber(int size) throws IOException {
        byte[] bytes = new byte[size];
        long n = 0;
        reader.readFully(bytes);
        for (int i = 0; i < size; i++) {
            long u = 0;
            if (isLSB) {
                u = (0xff & bytes[i]);
            } else {
                u = (0xff & bytes[size - i - 1]);
            }
            n |= (u << (i * 8));
        }
        return n;
    }
    
    private String getString(int length) throws IOException{
        byte[] bytes = new byte[length];
        reader.readFully(bytes);
        StringBuilder str = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if (bytes[i] == 0) {
                break;
            }
            str.append((char)bytes[i]);
        }
        return str.toString();
    }
}
