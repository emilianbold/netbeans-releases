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

import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;


/**
 * Indicates whether a Java package should be considered publicly accessible.
 * <div class="nonnormative">
 * <p>Suggested uses:</p>
 * <ol>
 * <li>Visually marking public and private packages as such.</li>
 * <li>Editor code completion could refuse to include private packages from
 * other compilation units.</li>
 * <li>Javadoc editing tools (the suggestions provider and/or AutoComment) could
 * treat missing or incomplete Javadoc in private packages as a minor error, or
 * not an error.</li>
 * </ol>
 * <p>If the Java Project module is enabled, you may register an implementation
 * to the lookup for a project rather than the default lookup.</p>
 * </div>
 * @see AccessibilityQueryImplementation
 * @author Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public class AccessibilityQuery {
    
    private static final Lookup.Result<? extends AccessibilityQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(AccessibilityQueryImplementation.class);

    private AccessibilityQuery() {}

    /**
     * Check whether a given Java source package should be considered publicly
     * accessible for use by other compilation units.
     * If not, then even public classes in the package should be treated as
     * effectively private by the IDE (though the Java compiler will not forbid
     * you to access them).
     * @param pkg a Java source package (must have a corresponding
     *        {@link org.netbeans.api.java.classpath.ClassPath#SOURCE} root)
     * @return true if the package is definitely intended for public access from
     *         other compilation units, false if it is definitely not, or null if
     *         this information is not known
     */
    public static Boolean isPubliclyAccessible(FileObject pkg) {
        if (!pkg.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + pkg); // NOI18N
        }
        for ( AccessibilityQueryImplementation aqi : implementations.allInstances()) {
            Boolean b = aqi.isPubliclyAccessible(pkg);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

}
