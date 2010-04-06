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
package org.netbeans.modules.javacard.source;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
/**
 * Hard-coded annotation processor query for javacard.  Provides enough info
 * to JavaCardErrorProcessor for it to detect if it is running in a file owned
 * by a javacard project, and if so, if it should use classic or Java Card
 * 3 limitations on language features.
 *
 * @author Tim Boudreau
 */
public class JavacardAPQI implements AnnotationProcessingQueryImplementation {
    private final JCProject project;
    private static final Logger log = Logger.getLogger(JavacardAPQI.class.getName());
    public JavacardAPQI (JCProject project) {
        log.log(Level.FINER, "Create an AnnotationProcessingQueryImplementation for {0}", project); //NOI18N
        this.project = project;
    }

    @Override
    public Result getAnnotationProcessingOptions(FileObject file) {
        log.log(Level.FINER, "get AP Options for {0} project {1}", new Object[] { file, project}); //NOI18N
        return new Res(project);
    }

    private static final class Res implements Result {
        private final JCProject project;
        public Res (JCProject project) {
            this.project = project;
        }

        @Override
        public boolean annotationProcessingEnabled() {
            return true;
        }

        @Override
        public Iterable<? extends String> annotationProcessorsToRun() {
            return Collections.<String>emptySet();
        }

        @Override
        public URL sourceOutputDirectory() {
            return null;
        }

        @Override
        public Map<? extends String, ? extends String> processorOptions() {
            log.log(Level.FINER, "get AP Options for {0} kind {1}", new Object[] {project, project.kind()}); //NOI18N
            if (project.kind().isClassic()) {
                return Collections.<String,String>singletonMap(JavaCardErrorProcessor.JAVACARD_OPTION, Boolean.TRUE.toString());
            } else {
                return Collections.<String,String>singletonMap(JavaCardErrorProcessor.JAVACARD_OPTION, Boolean.FALSE.toString());
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            //do nothing
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            //do nothing
        }
    }
}
