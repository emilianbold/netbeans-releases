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

package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author lahvac
 */
final class AnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final String annotationProcessingEnabledProperty;
    private final String annotationProcessingEnabledInEditorProperty;
    private final String runAllAnnotationProcessorsProperty;
    private final String annotationProcessorsProperty;
    private final Set<String> properties;
    private final String sourceOutputProperty;

    public AnnotationProcessingQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, String annotationProcessingEnabledProperty, String annotationProcessingEnabledInEditorProperty, String runAllAnnotationProcessorsProperty, String annotationProcessorsProperty, String sourceOutputProperty) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.annotationProcessingEnabledProperty = annotationProcessingEnabledProperty;
        this.annotationProcessingEnabledInEditorProperty = annotationProcessingEnabledInEditorProperty;
        this.runAllAnnotationProcessorsProperty = runAllAnnotationProcessorsProperty;
        this.annotationProcessorsProperty = annotationProcessorsProperty;
        this.properties = new HashSet<String>(Arrays.asList(annotationProcessingEnabledProperty, annotationProcessingEnabledInEditorProperty, runAllAnnotationProcessorsProperty, annotationProcessorsProperty, sourceOutputProperty));
        this.sourceOutputProperty = sourceOutputProperty;
    }

    private Reference<Result> cache;

    public synchronized Result getAnnotationProcessingOptions(FileObject file) {
        Result current = cache != null ? cache.get() : null;

        if (current == null) {
            cache = new WeakReference<Result>(current = new ResultImpl());
        }

        return current;
    }

    private static final Set<String> TRUE = new HashSet<String>(Arrays.asList("true", "on", "1"));
    
    private final class ResultImpl implements Result, PropertyChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);

        public ResultImpl() {
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        }

        public boolean annotationProcessingEnabled() {
            return    TRUE.contains(evaluator.getProperty(annotationProcessingEnabledProperty))
                   && TRUE.contains(evaluator.getProperty(annotationProcessingEnabledInEditorProperty));
        }

        public Iterable<? extends String> annotationProcessorsToRun() {
            if (TRUE.contains(evaluator.getProperty(runAllAnnotationProcessorsProperty))) {
                return null;
            }

            String processors = evaluator.getProperty(annotationProcessorsProperty);

            if (processors == null) {
                //TODO: what to do in this case?
                processors = "";
            }

            return Arrays.asList(processors.split(","));
        }

        @Override
        public URL sourceOutputDirectory() {
            //TODO: caching?
            String prop = evaluator.getProperty(sourceOutputProperty);
            if (prop != null) {
                File output = helper.resolveFile(prop);

                try {
                    return output.toURI().toURL();
                } catch (MalformedURLException ex) {
                    Logger.getLogger(AnnotationProcessingQueryImpl.class.getName()).log(Level.FINE, null, ex);
                    return  null;
                }
            }

            return null;
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || properties.contains(evt.getPropertyName())) {
                cs.fireChange();
            }
        }

    }
}
