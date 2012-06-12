/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.queries;

import java.io.File;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

/**
 * Support class for creating different types of queries implementations.
 * @author Tomas Mysik
 */
public final class QuerySupport {

    private QuerySupport() {
    }

    /**
     * Create a new query to provide information about where Java sources
     * corresponding to binaries (classfiles) can be found.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining project properties.
     * @param srcRoots a list of source roots.
     * @param testRoots a list of test roots.
     * @return {@link SourceForBinaryQueryImplementation} to provide information about where Java sources can be found.
     * @see SourceForBinaryQueryImplementation
     */
    public static SourceForBinaryQueryImplementation createCompiledSourceForBinaryQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {
        return createCompiledSourceForBinaryQuery(helper,
            evaluator, srcRoots, testRoots, new String[]{"build.classes.dir", "dist.jar"}, new String[]{"build.test.classes.dir"});
    }

    /**
     * Create a new query to provide information about where Java sources
     * corresponding to binaries (classfiles) can be found.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining project properties.
     * @param srcRoots a list of source roots.
     * @param testRoots a list of test roots, may be null if the project does not support tests.
     * @param binaryProperties array of property names of binary artifacts produced by this project, e.g. dist.jar
     * @param testBinaryProperties array of property names of test binary artifacts produced by this project, e.g. build.test.classes.dir
     * If the testRoots parameter is null this parameter has to be null as well.
     * @return {@link SourceForBinaryQueryImplementation} to provide information about where Java sources can be found.
     * @see SourceForBinaryQueryImplementation
     * @since org.netbeans.modules.java.api.common/0 1.3
     */
    public static SourceForBinaryQueryImplementation createCompiledSourceForBinaryQuery(
            @NonNull AntProjectHelper helper,
            @NonNull PropertyEvaluator evaluator,
            @NonNull SourceRoots srcRoots,
            @NullAllowed SourceRoots testRoots,
            @NonNull String[] binaryProperties,
            @NullAllowed String[] testBinaryProperties) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("srcRoots", srcRoots); // NOI18N
        Parameters.notNull("binaryProperties", binaryProperties); // NOI18N
        final boolean validTestParams = testRoots == null ? testBinaryProperties == null : testBinaryProperties != null;
        if (!validTestParams) {
            throw new IllegalArgumentException("Both testRoots and testBinaryProperties have to be null or non null");  //NOI18N
        }
        return new CompiledSourceForBinaryQueryImpl(helper, evaluator, srcRoots, testRoots, binaryProperties, testBinaryProperties);
    }

    /**
     * Create a new query to provide information about encoding of a file. The returned query listens to the changes
     * in particular property values.
     * @param eval {@link PropertyEvaluator} used for obtaining the value of source encoding.
     * @param sourceEncodingPropertyName the source encoding property name.
     * @return a {@link FileEncodingQueryImplementation} to provide information about encoding of a file.
     */
    public static FileEncodingQueryImplementation createFileEncodingQuery(PropertyEvaluator eval,
            String sourceEncodingPropertyName) {
        Parameters.notNull("eval", eval); // NOI18N
        Parameters.notNull("sourceEncodingPropertyName", sourceEncodingPropertyName); // NOI18N

        return new FileEncodingQueryImpl(eval, sourceEncodingPropertyName);
    }

    /**
     * Create a new query to find Javadoc. The returned query listens on changes of the Javadoc directory.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining the Javadoc root.
     * @return a {@link JavadocForBinaryQueryImplementation} to find Javadoc.
     */
    public static JavadocForBinaryQueryImplementation createJavadocForBinaryQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator) {

        return createJavadocForBinaryQuery(helper, evaluator, new String[]{"build.classes.dir", "dist.jar"});
    }

    /**
     * Create a new query to find Javadoc. The returned query listens on changes of the Javadoc directory.
     * @param helper {@link AntProjectHelper} used for resolving files, e.g. output directory.
     * @param evaluator {@link PropertyEvaluator} used for obtaining the Javadoc root.
     * @param binaryProperties array of property names of binary artifacts produced by this project, e.g. dist.jar
     * @return a {@link JavadocForBinaryQueryImplementation} to find Javadoc.
     * @since org.netbeans.modules.java.api.common/0 1.3
     */
    public static JavadocForBinaryQueryImplementation createJavadocForBinaryQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, String[] binaryProperties) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("binaryProperties", binaryProperties); // NOI18N

        return new JavadocForBinaryQueryImpl(helper, evaluator, binaryProperties);
    }

    /**
     * Create a new query to provide information about files sharability. The returned query listens to the changes
     * in particular source roots.
     * @param helper {@link AntProjectHelper} used for creating a query itself.
     * @param evaluator a {@link PropertyEvaluator property evaluator} to interpret paths with.
     * @param srcRoots a list of source roots to treat as sharable.
     * @param testRoots a list of test roots to treat as sharable, may be null if the project does not support tests
     * @param additionalSourceRoots additional paths to treat as sharable (just pure property names, do not
     *          use <i>${</i> and <i>}</i> characters). Can be <code>null</code>.
     * @return a query to provide information about files sharability.
     * @since 1.35
     */
    public static SharabilityQueryImplementation2 createSharabilityQuery2(
            final @NonNull AntProjectHelper helper,
            final @NonNull PropertyEvaluator evaluator,
            final @NonNull SourceRoots srcRoots,
            final @NullAllowed SourceRoots testRoots,
            final @NullAllowed String... additionalSourceRoots) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("srcRoots", srcRoots); // NOI18N

        return new SharabilityQueryImpl(helper, evaluator, srcRoots, testRoots, additionalSourceRoots);
    }
    /**
     * @deprecated since 1.35 use {@link #createSharabilityQuery2} instead
     */
    @Deprecated
    public static SharabilityQueryImplementation createSharabilityQuery(
            final @NonNull AntProjectHelper helper,
            final @NonNull PropertyEvaluator evaluator,
            final @NonNull SourceRoots srcRoots,
            final @NullAllowed SourceRoots testRoots,
            final @NullAllowed String... additionalSourceRoots) {
        final SharabilityQueryImplementation2 sq2 = createSharabilityQuery2(helper, evaluator, srcRoots, testRoots, additionalSourceRoots);
        return new SharabilityQueryImplementation() {
            @Override public int getSharability(File file) {
                return sq2.getSharability(Utilities.toURI(file)).ordinal();
            }
        };
    }
    /**
     * @deprecated since 1.35 use {@link #createSharabilityQuery2} instead
     */
    @Deprecated
    public static SharabilityQueryImplementation createSharabilityQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {

        return createSharabilityQuery(helper, evaluator, srcRoots, testRoots, (String[]) null);
    }

    /**
     * Create a new query to find out specification source level of Java source files.
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties.
     * @return a {@link SourceLevelQueryImplementation} to find out specification source level of Java source files.
     * @deprecated Use {@link QuerySupport#createSourceLevelQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator)}
     */
    @Deprecated
    public static org.netbeans.spi.java.queries.SourceLevelQueryImplementation createSourceLevelQuery(PropertyEvaluator evaluator) {
        Parameters.notNull("evaluator", evaluator); // NOI18N

        return new SourceLevelQueryImpl(evaluator);
    }

    /**
     * Create a new query to find out source level of Java source files (SourceLevelQueryImplementation2).
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties.
     * @return a {@link SourceLevelQueryImplementation2} to find out source level of Java source files.
     * @since 1.22
     */
    public static SourceLevelQueryImplementation2 createSourceLevelQuery2(@NonNull PropertyEvaluator evaluator) {
        Parameters.notNull("evaluator", evaluator); // NOI18N
        return new SourceLevelQueryImpl2(evaluator);
    }

    /**
     * Create a new query to find Java package roots of unit tests for Java package root of sources and vice versa.
     * @param sourceRoots a list of source roots.
     * @param testRoots a list of test roots.
     * @return a {@link MultipleRootsUnitTestForSourceQueryImplementation} to find Java package roots of unit tests
     *         for Java package root of sources and vice versa.
     */
    public static MultipleRootsUnitTestForSourceQueryImplementation createUnitTestForSourceQuery(
            SourceRoots sourceRoots, SourceRoots testRoots) {
        Parameters.notNull("sourceRoots", sourceRoots); // NOI18N
        Parameters.notNull("testRoots", testRoots); // NOI18N

        return new UnitTestForSourceQueryImpl(sourceRoots, testRoots);
    }

    /**
     * Create a new query to test whether a file can be considered to be built (up to date). The returned query
     * listens to the changes in particular source roots.
     * @param helper {@link AntProjectHelper} used for creating a query itself.
     * @param evaluator {@link PropertyEvaluator} used for obtaining needed properties.
     * @param sourceRoots a list of source roots.
     * @param testRoots a list of test roots.
     * @return a {@link FileBuiltQueryImplementation} to test whether a file can be considered to be built (up to date).
     */
    public static FileBuiltQueryImplementation createFileBuiltQuery(AntProjectHelper helper,
            PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("sourceRoots", sourceRoots); // NOI18N
        Parameters.notNull("testRoots", testRoots); // NOI18N

        return new FileBuiltQueryImpl(helper, evaluator, sourceRoots, testRoots);
    }

    /**
     * Creates an implementation of {@link CreateFromTemplateAttributesProvider} providing
     * attributes for the project license and encoding.
     *
     * @param helper {@link AntProjectHelper} used for reading the project properties.
     * @param encodingQuery {@link FileEncodingQueryImplementation} used to obtain an encoding.
     * @return a {@code CreateFromTemplateAttributesProvider}.
     *
     * @since 1.1
     */
    public static CreateFromTemplateAttributesProvider createTemplateAttributesProvider(AntProjectHelper helper, FileEncodingQueryImplementation encodingQuery) {
        Parameters.notNull("helper", helper);
        Parameters.notNull("encodingQuery", encodingQuery);
        return new TemplateAttributesProviderImpl(helper, encodingQuery);
    }
    
    /**
     * Creates an implementation of {@link BinaryForSourceQueryImplementation} 
     * which maps given project source roots and test roots to given folders
     * with built classes and built test classes.
     *
     * @param src project source roots
     * @param test project test roots
     * @param helper AntProjectHelper
     * @param eval PropertyEvaluator
     * @param sourceProp name of property pointing to a folder with built classes
     * @param testProp name of property pointing to a folder with built test classes
     * @return BinaryForSourceQueryImplementation
     * @since org.netbeans.modules.java.api.common/1 1.5
     */
    public static BinaryForSourceQueryImplementation createBinaryForSourceQueryImplementation(
            SourceRoots src, SourceRoots test, AntProjectHelper helper, 
            PropertyEvaluator eval, String sourceProp, String testProp) {
        return new BinaryForSourceQueryImpl(src, test, helper, eval, 
                new String[]{sourceProp}, new String[]{testProp});
    }
    
    /**
     * Shortcut version of {@link #createBinaryForSourceQueryImplementation(org.netbeans.modules.java.api.common.SourceRoots, org.netbeans.modules.java.api.common.SourceRoots, org.netbeans.spi.project.support.ant.AntProjectHelper, org.netbeans.spi.project.support.ant.PropertyEvaluator, java.lang.String, java.lang.String) }
     * which assumes that build classes folder is stored in property <code>build.classes.dir</code> and
     * built test classes folder is stored in property <code>build.test.classes.dir</code>.
     *
     * @param src project source roots
     * @param test project test roots
     * @param helper AntProjectHelper
     * @param eval PropertyEvaluator
     * @param sourceProps array of properties pointing to source folders
     * @param testProps array of properties pointing to test folders
     * @return BinaryForSourceQueryImplementation
     * @since org.netbeans.modules.java.api.common/1 1.5
     */
    public static BinaryForSourceQueryImplementation createBinaryForSourceQueryImplementation(SourceRoots src, SourceRoots test, 
            AntProjectHelper helper, PropertyEvaluator eval) {
        return createBinaryForSourceQueryImplementation(src, test, helper, eval, 
                "build.classes.dir", "build.test.classes.dir"); // NOI18N
    }
    
    /**Create a new query to provide annotation processing configuration data.
     * 
     * @param helper project's AntProjectHelper
     * @param evaluator project's evaluator
     * @param annotationProcessingEnabledProperty property whose value says whether the annotation processing is enabled for the given project at all
     *                                                    (will be returned from {@link Result#annotationProcessingEnabled()})
     * @param annotationProcessingEnabledInEditorProperty property whose value says whether the annotation processing should be enabled
     *                                                    in the editor (will be returned from {@link Result#annotationProcessingEnabled())}
     * @param runAllAnnotationProcessorsProperty when true, {@link Result#annotationProcessorsToRun()} will return null
     * @param annotationProcessorsProperty should contain comma separated list of annotation processors to run (will be returned from  {@link Result#annotationProcessorsToRun()})
     * @param sourceOutputProperty directory to which the annotation processors generate source files (will be returned from  {@link Result#sourceOutputProperty()})
     * @param processorOptionsProperty options passed to the annotation processors (-Akey=value)
     * @return a {@link AnnotationProcessingQueryImplementation} to provide annotation processing configuration data for this project.
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static AnnotationProcessingQueryImplementation createAnnotationProcessingQuery(AntProjectHelper helper, PropertyEvaluator evaluator,
            String annotationProcessingEnabledProperty, String annotationProcessingEnabledInEditorProperty, String runAllAnnotationProcessorsProperty, String annotationProcessorsProperty, String sourceOutputProperty, String processorOptionsProperty) {
        return new AnnotationProcessingQueryImpl(helper, evaluator, annotationProcessingEnabledProperty, annotationProcessingEnabledInEditorProperty, runAllAnnotationProcessorsProperty, annotationProcessorsProperty, sourceOutputProperty, processorOptionsProperty);
    }

    public static ProjectInformation createProjectInformation(AntProjectHelper projectHelper, Project project, Icon icon) {
        return new QuerySupport.AntHelper(projectHelper, project, icon, ProjectInfoImpl.DEFAULT_ELEMENT_NAME);
    }

    public static ProjectInformation createProjectInformation(UpdateHelper updateHelper, Project project, Icon icon) {
        return new QuerySupport.AntUpdateHelper(updateHelper, project, icon, ProjectInfoImpl.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Returns {@link Sources} implementation designed for projects that supports adding
     * or removing of the source roots. The returned instance also implements {@link SourceGroupModifierImplementation}
     * @param project the {@link Project} for which the {@link Sources} should be created
     * @param helper the {@link AntProjectHelper} of the project, used only to resolve files
     * @param evaluator the {@link PropertyEvaluator} to evaluate the properties
     * @param roots the array of {@link Roots} providing the roots of given type
     * @return the {@link Sources} instance implementing also the {@link SourceGroupModifierImplementation} interface
     * @since 1.21
     */
    public static Sources createSources(@NonNull final Project project,
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Roots... roots) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("evaluator", evaluator); //NOI18N
        Parameters.notNull("roots", roots); //NOI18N
        return new SourcesImpl(project, helper, evaluator, roots);
    }
    
    private static class AntHelper extends ProjectInfoImpl {

        private final AntProjectHelper projectHelper;

        @SuppressWarnings("LeakingThisInConstructor")
        public AntHelper(AntProjectHelper projectHelper, Project project, Icon icon, String elementName) {
            super(project, icon, elementName);
            this.projectHelper = projectHelper;

            projectHelper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, projectHelper));
        }


        @Override
        protected Element getPrimaryConfigurationData() {
            return projectHelper.getPrimaryConfigurationData(true);
        }
    }

    private static class AntUpdateHelper extends ProjectInfoImpl {

        private final UpdateHelper updateHelper;

        @SuppressWarnings("LeakingThisInConstructor")
        public AntUpdateHelper(UpdateHelper updateHelper, Project project, Icon icon, String elementName) {
            super(project, icon, elementName);
            this.updateHelper = updateHelper;

            AntProjectHelper projectHelper = updateHelper.getAntProjectHelper();
            projectHelper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, projectHelper));
        }

        @Override
        protected Element getPrimaryConfigurationData() {
            return updateHelper.getPrimaryConfigurationData(true);
        }
    }
}
