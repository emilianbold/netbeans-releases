/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.InputStream;
import java.io.IOException;

/**
 * Makes a package-private method accessible to the Ant task.
 * @author Jesse Glick
 */
public class CRC32Calculator {
    
    private CRC32Calculator() {}
    
    public static String computeCrc32(InputStream is) throws IOException {
        return GeneratedFilesHelper.computeCrc32(is);
    }
    
}
