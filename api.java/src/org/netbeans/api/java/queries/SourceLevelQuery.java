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

import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Returns source level of the given Java source file if it is known.
 * @see org.netbeans.spi.java.queries.SourceLevelQueryImplementation
 * @author David Konecny
 * @since org.netbeans.api.java/1 1.5
 */
public class SourceLevelQuery {
    
    private static final Lookup.Result<? extends SourceLevelQueryImplementation> implementations =
        Lookup.getDefault().lookupResult (SourceLevelQueryImplementation.class);

    private SourceLevelQuery() {
    }

    /**
     * Returns source level of the given Java file, Java package or source folder. For acceptable return values
     * see the documentation of <code>-source</code> command line switch of 
     * <code>javac</code> compiler .
     * @param javaFile Java source file, Java package or source folder in question
     * @return source level of the Java file, e.g. "1.3", "1.4" or "1.5", or null
     *     if it is not known
     */
    public static String getSourceLevel(FileObject javaFile) {
        for  (SourceLevelQueryImplementation sqi : implementations.allInstances()) {
            String s = sqi.getSourceLevel(javaFile);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

}
