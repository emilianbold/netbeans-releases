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
package org.netbeans.api.java.queries;

import java.net.URL;
import org.openide.util.Lookup;
import org.openide.filesystems.*;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;

import java.util.*;


/**
 * The query is used for finding sources for binaries.
 * The examples of usage of this query are:
 * <ul>
 * <li><p>finding source for library</p></li>
 * <li><p>finding src.zip for platform</p></li>
 * <li><p>finding source folder for compiled jar or build folder</p></li>
 * </ul>
 * @see SourceForBinaryQueryImplementation
 * @since org.netbeans.api.java/1 1.4
 */
public class SourceForBinaryQuery {
    
    private static final Lookup.Result/*<SourceForBinaryQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup (new Lookup.Template (SourceForBinaryQueryImplementation.class));

    private SourceForBinaryQuery () {
    }

    /**
     * Returns the source root for given binary root (for example, src folder for jar file or build folder).
     * @param binaryRoot the ClassPath root of compiled files.
     * @return FileObject[] never returns null
     */
    public static FileObject[] findSourceRoot (URL binaryRoot) {
        if (FileUtil.isArchiveFile(binaryRoot)) {
            throw new IllegalArgumentException("File URL pointing to " + // NOI18N
                "JAR is not valid classpath entry. Use jar: URL. Was: "+binaryRoot); // NOI18N
        }
        List result = new ArrayList ();
        for (Iterator it = implementations.allInstances().iterator(); it.hasNext();) {
            result.addAll(Arrays.asList(
                  ((SourceForBinaryQueryImplementation)it.next()).findSourceRoot (binaryRoot)));
        }
        return (FileObject[]) result.toArray (new FileObject[result.size()]);
    }

}
