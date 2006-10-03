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

package org.netbeans.api.java.queries;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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

    private static final Logger LOGGER = Logger.getLogger(SourceLevelQuery.class.getName());

    private static final Pattern SOURCE_LEVEL = Pattern.compile("\\d+\\.\\d+");

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
                if (!SOURCE_LEVEL.matcher(s).matches()) {
                    LOGGER.log(Level.WARNING, "#83994: Ignoring bogus source level {0} for {1} from {2}", new Object[] {s, javaFile, sqi});
                    continue;
                }
                LOGGER.log(Level.FINE, "Found source level {0} for {1} from {2}", new Object[] {s, javaFile, sqi});
                return s;
            }
        }
        LOGGER.log(Level.FINE, "No source level found for {0}", javaFile);
        return null;
    }

}
