/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package dwarfvsmodel;

import java.io.*;
import java.util.Collection;

/**
 * Misc. utility finctions
 * @author vk155633
 */
public class DMUtils {
    

    public static PrintStream createStream(File tempDir, String fileName, String ext) throws FileNotFoundException {
	return createStream(tempDir, fileName, ext, false);
    }
    
    public static PrintStream createStream(File tempDir, String fileName, String ext, boolean append) throws FileNotFoundException {
	int pos = fileName.lastIndexOf(File.separatorChar);
	if( pos > 0 ) {
	    fileName = fileName.substring(pos);
	}
	File file = new File(tempDir, fileName + '.' + ext);
	return new PrintStream(new FileOutputStream(file, append));
    }
    
    public static <T> void addAll  (Collection<T> addTo, Iterable<T> addFrom) {
	for( T t : addFrom ) {
	    addTo.add(t);
	}
    }
}
