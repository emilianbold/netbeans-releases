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
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

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
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(SourceForBinaryQuery.class.getName());
    
    private static final Lookup.Result/*<SourceForBinaryQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup (new Lookup.Template (SourceForBinaryQueryImplementation.class));

    private SourceForBinaryQuery () {
    }

    /**
     * Returns the source root for given binary root (for example, src folder for jar file or build folder).
     * @param binaryRoot the ClassPath root of compiled files.
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findSourceRoots (URL binaryRoot) {
        if (FileUtil.isArchiveFile(binaryRoot)) {
            throw new IllegalArgumentException("File URL pointing to " + // NOI18N
                "JAR is not valid classpath entry. Use jar: URL. Was: "+binaryRoot); // NOI18N
        }
        if (!binaryRoot.toExternalForm().endsWith("/")) {
            throw new IllegalArgumentException ("Folder URL must end with '/'. Was: "+binaryRoot);
        }
        boolean log = ERR.isLoggable(ErrorManager.INFORMATIONAL);
        if (log) ERR.log("SFBQ.findSourceRoots: " + binaryRoot);
        for (Iterator it = implementations.allInstances().iterator(); it.hasNext();) {
            SourceForBinaryQueryImplementation impl = (SourceForBinaryQueryImplementation) it.next();
            Result result = impl.findSourceRoots(binaryRoot);
            if (result != null) {
                if (log) ERR.log("  got result " + Arrays.asList(result.getRoots()) + " from " + impl);
                return result;
            }
        }
        return EMPTY_RESULT;
    }
    
    /**
     * Result of finding sources, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public interface Result {
        
        /**
         * Get the source roots.         
         * @return array of roots of sources (may be empty but not null)
         */
        FileObject[] getRoots();
        
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
        private static final FileObject[] NO_ROOTS = new FileObject[0];
        EmptyResult() {}
        public FileObject[] getRoots() {
            return NO_ROOTS;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }    

}
