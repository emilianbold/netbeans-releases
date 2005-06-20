/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * A query to find Javadoc root for the given classpath root.
 * @author David Konecny, Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public class JavadocForBinaryQuery {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(JavadocForBinaryQuery.class.getName());
    
    private static final Lookup.Result/*<JavadocForBinaryQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup (new Lookup.Template (JavadocForBinaryQueryImplementation.class));

    private JavadocForBinaryQuery () {
    }

    /**
     * Find Javadoc information for a classpath root containing Java classes.
     * <p>
     * These methods calls findJavadoc method on the JavadocForBinaryQueryImplementation 
     * instances registered in the lookup until null result is returned for given binaryRoot. The
     * non null result is returned.
     * </p>
     * @param binary URL of a classpath root
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findJavadoc(URL binary) {
        if (FileUtil.isArchiveFile(binary)) {
            throw new IllegalArgumentException("File URL pointing to " + // NOI18N
                "JAR is not valid classpath entry. Use jar: URL. Was: "+binary); // NOI18N
        }
        boolean log = ERR.isLoggable(ErrorManager.INFORMATIONAL);
        if (log) ERR.log("JFBQ.findJavadoc: " + binary);
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            JavadocForBinaryQueryImplementation impl = (JavadocForBinaryQueryImplementation) it.next();
            Result r = impl.findJavadoc(binary);
            if (r != null) {
                if (log) ERR.log("  got result " + Arrays.asList(r.getRoots()) + " from " + impl);
                return r;
            } else {
                if (log) ERR.log("  got no result from " + impl);
            }
        }
        if (log) ERR.log("  got no results from any impl");
        return EMPTY_RESULT;        
    }

    /**
     * Result of finding Javadoc, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public interface Result {
        
        /**
         * Get the Javadoc roots.
         * Each root should contain the main <code>index.html</code>, so that
         * for a class <samp>pkg.Class</samp> the generated documentation would
         * have a path <samp>pkg/Class.html</samp> relative to one of the roots.
         * @return array of roots of Javadoc documentation (may be empty but not null)
         */
        URL[] getRoots();
        
        /**
         * Add a listener to changes in the roots.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Remove a listener to changes in the roots.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
        
    }
    
    private static final Result EMPTY_RESULT = new EmptyResult();
    private static final class EmptyResult implements Result {
        private static final URL[] NO_ROOTS = new URL[0];
        EmptyResult() {}
        public URL[] getRoots() {
            return NO_ROOTS;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }    
    
}
