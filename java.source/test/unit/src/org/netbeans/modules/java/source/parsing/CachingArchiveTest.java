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
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Lahoda
 */
public class CachingArchiveTest extends NbTestCase {
    
    public CachingArchiveTest(String testName) {
        super(testName);
    }

    public void testPutName() throws Exception {
        File archive = null;
        
        String cp = System.getProperty("sun.boot.class.path");
        String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));
        
        for (String path : paths) {
            File f = new File(path);
            
            if (!f.canRead())
                continue;
            
            if (f.getName().endsWith("jar") || f.getName().endsWith("zip")) {
                archive = f;
                break;
            }
        }
        
        assertNotNull(archive);
        
        CachingArchive a = new CachingArchive(archive, false);
        
        a.initialize();
        
        a.putName(new byte[65536]);
        
        a.clear();
        a.initialize();
        
        a.putName(new byte[1]);
    }

    public void testJoin() throws Exception {
        long smallLong = ((long) Integer.MAX_VALUE) + 1;
        
        for (long mtime : Arrays.asList(3003611096031047874L, new Long(Integer.MAX_VALUE), smallLong, new Long(Integer.MIN_VALUE))) {
            int  higher = (int)(mtime >> 32);
            int  lower = (int)(mtime & 0xFFFFFFFF);
            
            assertEquals(mtime, CachingArchive.join(higher, lower));
        }
    }
}
