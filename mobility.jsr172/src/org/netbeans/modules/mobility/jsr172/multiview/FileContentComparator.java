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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.jsr172.multiview;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;



public class FileContentComparator{
    
    private FileContentComparator() {
        //To avoid instantiation
    }
    
    public static boolean equalFiles(final InputStream is1, final InputStream is2)throws IOException{
        return getCheckSum(is1) == getCheckSum(is2);
    }
    
    private static long getCheckSum( final InputStream is ) throws IOException {
        final byte b[] = new byte[1024] ;
        boolean done = false ;
        long fileCRC = 0 ;
        final CRC32 crc32 = new CRC32() ;
        
        while ( ! done ) {
            int bytesRead = 0 ;
            bytesRead = is.read( b , 0 , 1024) ;
            if ( bytesRead != -1 ) {
                crc32.reset();
                crc32.update(b) ;
                final long lineCRC = crc32.getValue();
                fileCRC = fileCRC + lineCRC ;
            } else {
                done = true ;
            }
        }
        return fileCRC ;
    }
}

