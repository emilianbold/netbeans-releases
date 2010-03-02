/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.Arrays;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

/**
 * A query to find Javadoc root for the given classpath root.
 * @author David Konecny, Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public class JavadocForBinaryQuery {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(JavadocForBinaryQuery.class.getName());
    
    private static final Lookup.Result<? extends JavadocForBinaryQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(JavadocForBinaryQueryImplementation.class);

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
        ClassPathSupport.createResource(binary); // just to check for IAE; XXX might be unnecessary since CP ctor now check this too
        boolean log = ERR.isLoggable(ErrorManager.INFORMATIONAL);
        if (log) ERR.log("JFBQ.findJavadoc: " + binary);
        for  (JavadocForBinaryQueryImplementation impl : implementations.allInstances()) {
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
