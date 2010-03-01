/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.queries;

import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**Return annotation processing configuration for given Java file, package or source folder.
 *
 * @author Jan Lahoda
 * @since org.netbeans.api.java/1 1.25
 */
public class AnnotationProcessingQuery {

    /**
     * Returns annotation processing configuration for Java file, Java package or source folder.
     * @param file Java source file, Java package or source folder in question
     * @return annotation processing configuration. Never null.
     */
    public static @NonNull Result getAnnotationProcessingOptions(@NonNull FileObject file) {
        Parameters.notNull("file", file);
        
        for (AnnotationProcessingQueryImplementation i : Lookup.getDefault().lookupAll(AnnotationProcessingQueryImplementation.class)) {
            Result r = i.getAnnotationProcessingOptions(file);

            if (r != null) {
                return r;
            }
        }

        return EMPTY;
    }

    /**Annotation processing configuration. The processor path is returned from
     * {@link ClassPath#getClassPath(org.openide.filesystems.FileObject, java.lang.String)}
     * for {@link JavaClassPathConstants#PROCESSOR_PATH}.
     *
     */
    public static interface Result {

        /**Whether the annotation processors should be run inside Java editor.
         *
         * @return true if and only if the annotation processors should be run inside the Java editor
         */
        public boolean annotationProcessingEnabled();

        /**Which annotation processors should be run.
         *
         * @return if null, run all annotation processors found on the {@link JavaClassPathConstants#PROCESSOR_PATH},
         *         otherwise only the selected processors will be run. The values should be binary names of the
         *         annotation processors.
         */
        public @CheckForNull Iterable<? extends String> annotationProcessorsToRun();

        /**Returns directory to which the annotation processing (during build process) generates sources, if any.
         *
         * @return if not-null, a directory to which the annotation processing generates sources.
         */
        public @CheckForNull URL sourceOutputDirectory();

        /**Add a {@link ChangeListener}.
         *
         * @param l the listener
         */
        public void addChangeListener(@NonNull ChangeListener l);

        /**Remove a {@link ChangeListener}.
         *
         * @param l the listener
         */
        public void removeChangeListener(@NonNull ChangeListener l);
    }

    private static final Result EMPTY = new Result() {
        public boolean annotationProcessingEnabled() {
            return false;
        }

        public Iterable<? extends String> annotationProcessorsToRun() {
            return null;
        }

        public URL sourceOutputDirectory() {
            return null;
        }

        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}

    };

    private AnnotationProcessingQuery() {}
}
