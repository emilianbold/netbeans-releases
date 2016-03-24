/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Tomas Zezula
 */
public final class TestProject implements Project {

    public static final String PROJECT_CONFIGURATION_NAMESPACE = "urn:test";

    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    private final ReferenceHelper refHelper;
    private final SourceRoots src;
    private final SourceRoots test;
    private Lookup lookup;

    private TestProject(AntProjectHelper helper) {
        this.helper = new UpdateHelper(new UpdateImplementation() {
                @Override public boolean isCurrent() {
                    return true;
                }
                @Override public boolean canUpdate() {
                    throw new AssertionError();
                }
                @Override public void saveUpdate(EditableProperties props) throws IOException {
                    throw new AssertionError();
                }
                @Override public Element getUpdatedSharedConfigurationData() {
                    throw new AssertionError();
                }
                @Override public EditableProperties getUpdatedProjectProperties() {
                    throw new AssertionError();
                }
            }, helper);
        this.evaluator = helper.getStandardPropertyEvaluator();
        this.aux = helper.createAuxiliaryConfiguration();
        this.refHelper = new ReferenceHelper(helper, aux, evaluator);
        this.src = SourceRoots.create(this.helper, evaluator, refHelper, PROJECT_CONFIGURATION_NAMESPACE,
                    "source-roots", false, "src.{0}{1}.dir");
        this.test = SourceRoots.create(this.helper, evaluator, refHelper, PROJECT_CONFIGURATION_NAMESPACE,
                    "test-roots", false, "test.{0}{1}.dir");
    }

    @Override
    public FileObject getProjectDirectory() {
        return helper.getAntProjectHelper().getProjectDirectory();
    }

    @NonNull
    public UpdateHelper getUpdateHelper() {
        return helper;
    }

    @NonNull
    public SourceRoots getSourceRoots() {
        return src;
    }

    public SourceRoots getTestRoots() {
        return test;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookups.fixed(
                    this,
                    aux,
                    new TestSources(this, helper.getAntProjectHelper(), evaluator, src, test),
                    new ClassPathProviderImpl(helper.getAntProjectHelper(), evaluator, src, test),
                    QuerySupport.createCompiledSourceForBinaryQuery(helper.getAntProjectHelper(), evaluator, src, test),
                    QuerySupport.createBinaryForSourceQueryImplementation(src, test, helper.getAntProjectHelper(), evaluator),
                    QuerySupport.createUnitTestForSourceQuery(src, test),
                    QuerySupport.createSourceLevelQuery(evaluator),
                    QuerySupport.createFileBuiltQuery(helper.getAntProjectHelper(), evaluator, src, test),
                    QuerySupport.createFileEncodingQuery(evaluator, "encoding")
            );
        }
        return lookup;
    }

    @NonNull
    public static AntBasedProjectType createProjectType() {
        return new TestAntBasedProjectType();
    }

    @NonNull
    public static Project createProject(
            @NonNull final FileObject projectFolder,
            @NullAllowed final FileObject srcRoot,
            @NullAllowed final FileObject testRoot) {
        return ProjectManager.mutex().writeAccess(() -> {
            try {
                AntProjectHelper h = ProjectGenerator.createProject(projectFolder, "test");
                EditableProperties pp = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (srcRoot != null) {
                    pp.setProperty("src.dir", PropertyUtils.relativizeFile(
                            FileUtil.toFile(projectFolder),
                            FileUtil.toFile(srcRoot)));
                }
                if (testRoot != null) {
                    pp.setProperty("test.src.dir", PropertyUtils.relativizeFile(
                            FileUtil.toFile(projectFolder),
                            FileUtil.toFile(testRoot)));
                }
                pp.setProperty("build.dir", "build");
                pp.setProperty("build.classes.dir", "${build.dir}/classes");
                pp.setProperty("build.test.classes.dir", "${build.dir}/test/classes");
                pp.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources");
                pp.setProperty("javac.classpath", "lib.jar");
                pp.setProperty("javac.test.classpath", "${javac.classpath}:junit.jar");
                pp.setProperty("run.classpath", "${javac.classpath}:${build.classes.dir}:runlib.jar");
                pp.setProperty("run.test.classpath", "${javac.test.classpath}:${build.test.classes.dir}:runlib.jar");
                pp.setProperty("dist.dir", "dist");
                pp.setProperty("dist.jar", "${dist.dir}/x.jar");
                pp.setProperty("javac.source", "1.6");
                pp.setProperty("encoding", "UTF-8");
                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pp);
                Element data = h.getPrimaryConfigurationData(true);
                Document doc = data.getOwnerDocument();
                ((Element) data.appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots")).
                        appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "root"))).
                        setAttribute("id", "src.dir");
                ((Element) data.appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "test-roots")).
                        appendChild(doc.createElementNS(TestProject.PROJECT_CONFIGURATION_NAMESPACE, "root"))).
                        setAttribute("id", "test.src.dir");
                h.putPrimaryConfigurationData(data, true);
                Project p = ProjectManager.getDefault().findProject(projectFolder);
                if (p == null) {
                    throw new IllegalStateException("No project");  //NOI18N
                }
                if (p.getClass() != TestProject.class) {
                    throw new IllegalStateException("Wrong project type");  //NOI18N
                }
                ProjectManager.getDefault().saveProject(p);
                return p;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
    }

    private static class TestAntBasedProjectType implements AntBasedProjectType {
        public String getType() {
            return "test";
        }
        public Project createProject(AntProjectHelper helper) throws IOException {
            return new TestProject(helper);
        }
        public String getPrimaryConfigurationDataElementName(boolean shared) {
            return "data";
        }
        public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
            return PROJECT_CONFIGURATION_NAMESPACE;
        }
    }

    /**
     * Simplified copy of J2SESources.
     */
    private static class TestSources implements Sources, PropertyChangeListener, ChangeListener {

        private final Project project;
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final SourceRoots sourceRoots;
        private final SourceRoots testRoots;
        private SourcesHelper sourcesHelper;
        private Sources delegate;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        TestSources(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots) {
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.evaluator.addPropertyChangeListener(this);
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
            this.testRoots = testRoots;
            this.testRoots.addPropertyChangeListener(this);
            initSources();
        }

        public SourceGroup[] getSourceGroups(final String type) {
            return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
                public SourceGroup[] run() {
                    Sources _delegate;
                    synchronized (TestSources.this) {
                        if (delegate == null) {
                            delegate = initSources();
                            delegate.addChangeListener(TestSources.this);
                        }
                        _delegate = delegate;
                    }
                    return _delegate.getSourceGroups(type);
                }
            });
        }

        private Sources initSources() {
            sourcesHelper = new SourcesHelper(project, helper, evaluator);
            register(sourceRoots);
            register(testRoots);
            sourcesHelper.addNonSourceRoot("${build.dir}");
            return sourcesHelper.createSources();
        }

        private void register(SourceRoots roots) {
            String[] propNames = roots.getRootProperties();
            String[] rootNames = roots.getRootNames();
            for (int i = 0; i < propNames.length; i++) {
                String prop = propNames[i];
                String displayName = roots.getRootDisplayName(rootNames[i], prop);
                String loc = "${" + prop + "}";
                sourcesHelper.sourceRoot(loc).displayName(displayName).add();
                sourcesHelper.sourceRoot(loc).type(JavaProjectConstants.SOURCES_TYPE_JAVA).displayName(displayName).add();
            }
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
            changeSupport.addChangeListener(changeListener);
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
            changeSupport.removeChangeListener(changeListener);
        }

        private void fireChange() {
            synchronized (this) {
                if (delegate != null) {
                    delegate.removeChangeListener(this);
                    delegate = null;
                }
            }
            changeSupport.fireChange();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) || "build.dir".equals(propName)) {
                this.fireChange();
            }
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            this.fireChange();
        }
    }

}
