/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.api.common.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.classpath.ProjectClassPathModifierSupport.ClassPathItemProvider;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.util.Parameters;

/**
 * @author Tomas Zezula, Tomas Mysik
 */
public final class ProjectClassPathModifier extends ProjectClassPathModifierImplementation {

    final ClassPathSupport classPathSupport;
    private final ProjectClassPathModifierSupport<Item> classPathModifierSupport;
    private final ClassPathItemProvider<Item> classPathItemProvider;

    // XXX javadoc
    public static ProjectClassPathModifier create(Project project, UpdateHelper helper, PropertyEvaluator eval,
            ReferenceHelper refHelper, ClassPathSupport classPathSupport, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, ProjectClassPathModifierSupport.Properties properties) {
        Parameters.notNull("classPathSupport", classPathSupport);

        return new ProjectClassPathModifier(project, helper, eval, refHelper, classPathSupport, sourceRoots,
                testSourceRoots, properties);
    }

    ProjectClassPathModifier(Project project, UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper,
            ClassPathSupport classPathSupport, SourceRoots sourceRoots, SourceRoots testSourceRoots,
            ProjectClassPathModifierSupport.Properties properties) {
        assert classPathSupport != null;

        this.classPathSupport = classPathSupport;
        classPathModifierSupport = ProjectClassPathModifierSupport.<Item>create(project, helper, eval, refHelper,
                sourceRoots, testSourceRoots, properties);
        classPathItemProvider = new DefaultClassPathItemProvider();
    }

    final class DefaultClassPathItemProvider implements ClassPathItemProvider<Item> {
        public Item createClassPathItem(File file, String property) {
            return ClassPathSupport.Item.create(file, property);
        }

        public Item createClassPathItem(AntArtifact antArtifact, URI antArtifactURI, String property) {
            return ClassPathSupport.Item.create(antArtifact, antArtifactURI, property);
        }

        public Item createClassPathItem(Library library, String property) {
            return ClassPathSupport.Item.create(library, property);
        }

        public List<Item> getClassPathItems(String reference) {
            return classPathSupport.itemsList(reference);
        }

        public String[] encodeToStrings(List<Item> items) {
            return classPathSupport.encodeToStrings(items);
        }

        public String getLibraryReference(Item item) {
            return classPathSupport.getLibraryReference(item);
        }
    }

    @Override
    protected SourceGroup[] getExtensibleSourceGroups() {
        return classPathModifierSupport.getExtensibleSourceGroups();
    }

    @Override
    protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
        return classPathModifierSupport.getExtensibleClassPathTypes(sourceGroup);
    }

    @Override
    protected boolean removeRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleRoots(classPathRoots,
                classPathModifierSupport.getClassPathProperty(sourceGroup, type),
                ProjectClassPathModifierSupport.Operation.REMOVE, classPathItemProvider);
    }

    @Override
    protected boolean addRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleRoots(classPathRoots,
                classPathModifierSupport.getClassPathProperty(sourceGroup, type),
                ProjectClassPathModifierSupport.Operation.ADD, classPathItemProvider);
    }

    @Override
    protected boolean removeAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements,
            final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleAntArtifacts(artifacts, artifactElements,
                classPathModifierSupport.getClassPathProperty(sourceGroup, type),
                ProjectClassPathModifierSupport.Operation.REMOVE, classPathItemProvider);
    }

    @Override
    protected boolean addAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements,
            final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleAntArtifacts(artifacts, artifactElements,
                classPathModifierSupport.getClassPathProperty(sourceGroup, type),
                ProjectClassPathModifierSupport.Operation.ADD, classPathItemProvider);
    }

    @Override
    protected boolean removeLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleLibraries(libraries,
                classPathModifierSupport.getClassPathProperty(sourceGroup, type),
                ProjectClassPathModifierSupport.Operation.REMOVE, classPathItemProvider);
    }

    @Override
    protected boolean addLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleLibraries(libraries,
                classPathModifierSupport.getClassPathProperty(sourceGroup, type),
                ProjectClassPathModifierSupport.Operation.ADD, classPathItemProvider);
    }
}
