/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
@AntBasedProjectRegistration(
    type=J2MEProject.TYPE,
    iconResource=J2MEProject.ICON,
    sharedNamespace = J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace= J2MEProject.PRIVATE_CONFIGURATION_NAMESPACE
)
public class J2MEProject implements Project {

    public static final String TYPE = "org.netbeans.modules.j2me.project"; //NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2me-embedded-project/1"; //NOI18N
    public static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2me-embedded-project-private/1"; //NOI18N
    static final String ICON = "org/netbeans/modules/j2me/project/ui/resources/j2meProject.gif";    //NOI18N
    private static final String EXTENSION_POINT = "Projects/org-netbeans-modules-j2me-project/Lookup";  //NOI18N

    private final AntProjectHelper helper;
    private final UpdateHelper updateHelper;
    private final AuxiliaryConfiguration auxCfg;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final ClassPathProviderImpl cpProvider;
    private final Lookup lkp;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;

    public J2MEProject(@NonNull final AntProjectHelper helper) {
        Parameters.notNull("helper", helper);   //NOI18N
        this.helper = helper;
        this.updateHelper = new UpdateHelper(new J2MEProjectUpdates(helper), helper);
        this.auxCfg = helper.createAuxiliaryConfiguration();
        this.eval = createPropertyEvaluator();
        this.refHelper = new ReferenceHelper(helper, auxCfg, eval);
        this.sourceRoots = createRoots(false);
        this.testRoots = createRoots(true);
        this.cpProvider = new ClassPathProviderImpl(
                helper,
                eval,
                sourceRoots,
                testRoots);
        this.lkp = createLookup();
    }

    @Override
    @NonNull
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    @NonNull
    public Lookup getLookup() {
        return lkp;
    }

    @NonNull
    public ReferenceHelper getReferenceHelper() {
        return refHelper;
    }

    @NonNull
    public SourceRoots getSourceRoots() {
        return sourceRoots;
    }

    @NonNull
    public PropertyEvaluator evaluator() {
        return eval;
    }

    @NonNull
    ClassPathProviderImpl getClassPathProvider() {
        return cpProvider;
    }

    @NonNull
    private SourceRoots createRoots(final boolean tests) {
        return SourceRoots.create(
            updateHelper,
            eval,
            refHelper,
            PROJECT_CONFIGURATION_NAMESPACE,
            tests ? "test-roots" : "source-roots", //NOI18N
            tests,
            tests ? "test.{0}{1}.dir" : "src.{0}{1}.dir"); //NOI18N
    }

    @NonNull
    private Lookup createLookup() {
        final Lookup base = Lookups.fixed(
                J2MEProject.this,                
                QuerySupport.createProjectInformation(helper, this, ImageUtilities.loadImageIcon(ICON, false)),
                auxCfg,
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                refHelper.createSubprojectProvider(),
                new J2MEActionProvider(
                    this,
                    updateHelper,
                    eval,
                    sourceRoots,
                    testRoots,
                    helper),
                QuerySupport.createFileEncodingQuery(eval, J2MEProjectProperties.SOURCE_ENCODING),
                QuerySupport.createSourceLevelQuery2(eval),
                QuerySupport.createSources(
                    this,
                    helper,
                    eval,
                    sourceRoots,
                    testRoots,
                    Roots.nonSourceRoots(ProjectProperties.BUILD_DIR, J2MEProjectProperties.DIST_DIR))
        );
        return LookupProviderSupport.createCompositeLookup(base, EXTENSION_POINT);
    }

    @NonNull
    private PropertyEvaluator createPropertyEvaluator() {
        final PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG));
        final PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }


    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;

        @SuppressWarnings("LeakingThisInConstructor")
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            } else {
                return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
            }
        }
    }

}
