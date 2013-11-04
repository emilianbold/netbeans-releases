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

package org.netbeans.modules.web.clientproject.api.jstesting;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.jstesting.JsTestingProviderAccessor;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * The API representation of a single provider for JavaScript (unit) testing.
 * @since 1.49
 */
public final class JsTestingProvider {

    private final JsTestingProviderImplementation delegate;

    static {
        JsTestingProviderAccessor.setDefault(new JsTestingProviderAccessor() {

            @Override
            public JsTestingProvider create(JsTestingProviderImplementation jsTestingProviderImplementation) {
                return new JsTestingProvider(jsTestingProviderImplementation);
            }

            @Override
            public String getIdentifier(JsTestingProvider jsTestingProvider) {
                return jsTestingProvider.getIdentifier();
            }

            @Override
            public String getDisplayName(JsTestingProvider jsTestingProvider) {
                return jsTestingProvider.getDisplayName();
            }

            @Override
            public void runTests(JsTestingProvider jsTestingProvider, Project project, TestRunInfo runInfo) {
                jsTestingProvider.runTests(project, runInfo);
            }

            @Override
            public FileObject fromServer(JsTestingProvider jsTestingProvider, Project project, URL serverUrl) {
                return jsTestingProvider.fromServer(project, serverUrl);
            }

            @Override
            public URL toServer(JsTestingProvider jsTestingProvider, Project project, FileObject projectFile) {
                return jsTestingProvider.toServer(project, projectFile);
            }

            @Override
            public ProjectCustomizer.CompositeCategoryProvider createCustomizer(JsTestingProvider jsTestingProvider, Project project) {
                return jsTestingProvider.createCustomizer(project);
            }

            @Override
            public void notifyEnabled(JsTestingProvider jsTestingProvider, Project project, boolean enabled) {
                jsTestingProvider.notifyEnabled(project, enabled);
            }

            @Override
            public void projectOpened(JsTestingProvider jsTestingProvider, Project project) {
                jsTestingProvider.projectOpened(project);
            }

            @Override
            public void projectClosed(JsTestingProvider jsTestingProvider, Project project) {
                jsTestingProvider.projectClosed(project);
            }

            @Override
            public NodeFactory createNodeFactory(JsTestingProvider jsTestingProvider) {
                return jsTestingProvider.createNodeFactory();
            }

        });
    }


    private JsTestingProvider(JsTestingProviderImplementation delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this JS testing provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    public String getIdentifier() {
        return delegate.getIdentifier();
    }

    /**
     * Returns the display name of this JS testing provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Run tests for the given {@link TestRunInfo info}.
     * @param project the project to run tests for; never {@code null}
     * @param runInfo info about the test run; never {@code null}
     * @see org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
     */
    public void runTests(@NonNull Project project, @NonNull TestRunInfo runInfo) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("runInfo", runInfo); // NOI18N
        delegate.runTests(project, runInfo);
    }

    /**
     * Map server URL to project file.
     * @param project target project
     * @param serverUrl URL to be mapped
     * @return project file or {@code null} if it cannot be mapped
     */
    @CheckForNull
    public FileObject fromServer(@NonNull Project project, @NonNull URL serverUrl) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("serverUrl", serverUrl); // NOI18N
        return delegate.fromServer(project, serverUrl);
    }

    /**
     * Map project file to server URL.
     * @param project source project
     * @param projectFile file to be mapped
     * @return server URL or {@code null} if it cannot be mapped
     */
    @CheckForNull
    public URL toServer(@NonNull Project project, @NonNull FileObject projectFile) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("projectFile", projectFile); // NOI18N
        return delegate.toServer(project, projectFile);
    }

    /**
     * Create project customizer for the given project.
     * @param project  the project; never {@code null}
     * @return project customizer, can be {@code null} if not supported
     */
    @CheckForNull
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer(@NonNull Project project) {
        return delegate.createCustomizer(project);
    }

    /**
     * Notify provider that it has been enabled/disabled in the given project (so
     * the provider can, if necessary, adjust UI etc.).
     * @param project the project, never {@code null}
     * @param enabled {@code true} if enabled, {@code false} otherwise
     */
    public void notifyEnabled(@NonNull Project project, boolean enabled) {
        delegate.notifyEnabled(project, enabled);
    }

    /**
     * Notify JS testing provider that the given project is being opened.
     * @param project project being opened
     */
    public void projectOpened(@NonNull Project project) {
        delegate.projectOpened(project);
    }

    /**
     * Notify JS testing provider that the given project is being closed.
     * @param project project being closed
     */
    public void projectClosed(@NonNull Project project) {
        delegate.projectClosed(project);
    }

    /**
     * Create JS testing provider node. This node can be visible/hidden based
     * on e.g. {@link #notifyEnabled(Project, boolean)}.
     * @return JS testing provider node, can be {@code null} if not supported
     * @see NodeFactory.Registration
     */
    @CheckForNull
    public NodeFactory createNodeFactory() {
        return delegate.createNodeFactory();
    }

}
