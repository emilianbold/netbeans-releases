/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

