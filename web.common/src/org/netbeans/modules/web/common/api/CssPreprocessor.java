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
package org.netbeans.modules.web.common.api;

import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.cssprep.CssPreprocessorAccessor;
import org.netbeans.modules.web.common.spi.CssPreprocessorImplementation;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Parameters;

/**
 * The API representation of a single CSS preprocessor.
 * @since 1.40
 */
public final class CssPreprocessor {

    private final CssPreprocessorImplementation delegate;


    static {
        CssPreprocessorAccessor.setDefault(new CssPreprocessorAccessor() {
            @Override
            public CssPreprocessor create(CssPreprocessorImplementation cssPreprocessorImplementation) {
                return new CssPreprocessor(cssPreprocessorImplementation);
            }
        });
    }

    private CssPreprocessor(CssPreprocessorImplementation delegate) {
        this.delegate = delegate;
    }

    /**
     * Return the <b>non-localized (usually english)</b> identifier of this CSS preprocessor.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}
     */
    @NonNull
    public String getIdentifier() {
        String identifier = delegate.getIdentifier();
        Parameters.notNull("identifier", identifier); // NOI18N
        return identifier;
    }

    /**
     * Return the display name of this CSS preprocessor. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    public String getDisplayName() {
        String displayName = delegate.getDisplayName();
        Parameters.notNull("displayName", displayName); // NOI18N
        return displayName;
    }

    /**
     * Process given file (can be a folder as well).
     * <p>
     * For folder, it usually means that all children should be processed.
     * <p>
     * <b>Warning:</b> The given file can be {@link FileObject#isValid() invalid}, it means deleted.
     * <p>
     * It usually means that if the given file can be processed by this CSS preprocessor, some action is done
     * (usually compiling).
     * @param project project where the file belongs, can be {@code null} for file without a project
     * @param fileObject valid or even invalid file (or folder) to be processed
     */
    public void process(@NullAllowed Project project, @NonNull FileObject fileObject) {
        delegate.process(project, fileObject);
    }

    /**
     * Create a {@link Customizer customizer} for this CSS preprocessor
     * and the given project.
     * @param project the project that is to be customized
     * @return a new CSS preprocessor customizer; can be {@code null} if the CSS preprocessor doesn't need
     *         to store/read any project specific properties (or does not need to be added/removed to given project)
     * @see org.netbeans.modules.web.common.api.CssPreprocessorsCustomizer
     */
    @CheckForNull
    public Customizer createCustomizer(@NonNull Project project) {
        CssPreprocessorImplementation.Customizer customizer = delegate.createCustomizer(project);
        if (customizer == null) {
            return null;
        }
        return new Customizer(customizer);
    }

    /**
     * Create a {@link ProjectProblemsProvider} for this CSS preprocessor.
     * @param support support needed for proper provider creation and resolving
     * @return {@link ProjectProblemsProvider} for this CSS preprocessor or {@code null} if not supported
     * @since 1.41
     */
    @CheckForNull
    public ProjectProblemsProvider createProjectProblemsProvider(@NonNull CssPreprocessor.ProjectProblemsProviderSupport support) {
        Parameters.notNull("support", support); // NOI18N
        return delegate.createProjectProblemsProvider(support);
    }

    //~ Inner classes

    /**
     * Provide support for customizing a project (via Project Properties dialog).
     * For reading and storing properties, {@link org.netbeans.api.project.ProjectUtils#getPreferences(Project, Class, boolean)} can be used.
     * <p>
     * Implementations <b>must be thread safe</b> since {@link #save() save} method is called in a background thread.
     */
    public static final class Customizer {

        private final CssPreprocessorImplementation.Customizer delegate;


        private Customizer(CssPreprocessorImplementation.Customizer delegate) {
            this.delegate = delegate;
        }

        /**
         * Return the display name of this customizer.
         * @return display name used in customizer, cannot be empty
         */
        @NonNull
        public String getDisplayName() {
            String displayName = delegate.getDisplayName();
            Parameters.notNull("displayName", displayName); // NOI18N
            return displayName;
        }

        /**
         * Attach a change listener that is to be notified of changes
         * in the customizer (e.g., the result of the {@link #isValid} method
         * has changed.
         * @param listener a listener, can be {@code null}
         */
        public void addChangeListener(@NonNull ChangeListener listener) {
            delegate.addChangeListener(listener);
        }

        /**
         * Removes a change listener.
         * @param listener a listener, can be {@code null}
         */
        public void removeChangeListener(@NonNull ChangeListener listener) {
            delegate.removeChangeListener(listener);
        }

        /**
         * Return a UI component used to allow the user to customize the given project.
         * <p>
         * This method might be called more than once and it is expected to always return the same instance.
         * @return a component that provides configuration UI
         */
        @NonNull
        public JComponent getComponent() {
            JComponent component = delegate.getComponent();
            Parameters.notNull("component", component); // NOI18N
            return component;
        }

        /**
         * Return a help context for {@link #getComponent}.
         * @return a help context; can be {@code null}
         */
        @CheckForNull
        public HelpCtx getHelp() {
            return delegate.getHelp();
        }

        /**
         * Checks if this customizer is valid (e.g., if the configuration set
         * using the UI component returned by {@link #getComponent} is valid).
         * <p>
         * If it returns {@code false}, check {@link #getErrorMessage() error message}, it
         * should not be {@code null}.
         * @return {@code true} if the configuration is valid, {@code false} otherwise
         * @see #getErrorMessage()
         * @see #getWarningMessage()
         */
        public boolean isValid() {
            return delegate.isValid();
        }

        /**
         * Get error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}.
         * @return error message or {@code null} if the {@link #getComponent component} is {@link #isValid() valid}
         * @see #isValid()
         * @see #getWarningMessage()
         */
        @CheckForNull
        public String getErrorMessage() {
            return delegate.getErrorMessage();
        }

        /**
         * Get warning message that can be not {@code null} even for {@link #isValid() valid} extender.
         * In other words, it is safe to customize the given project even if this method returns a message.
         * @return warning message or {@code null}
         * @see #isValid()
         * @see #getErrorMessage()
         */
        @CheckForNull
        public String getWarningMessage() {
            return delegate.getWarningMessage();
        }

        /**
         * Called to update properties of the given project. This method
         * is called in a background thread and only if user clicks the OK button;
         * also, it cannot be called if {@link #isValid()} is {@code false}.
         * <p>
         * <b>Please notice that this method is called under project write lock
         * so it should finish as fast as possible.</b> But it is possible, if it is a long-running task
         * (e.g. sending e-mail, connecting to a remote server), to create {@link org.openide.util.RequestProcessor} and run the code in it.
         * @see #isValid()
         * @see org.netbeans.api.project.ProjectUtils#getPreferences(Project, Class, boolean)
         */
        public void save() throws IOException {
            delegate.save();
        }

    }

    /**
     * Support class for creating and solving {@link CssPreprocessors#createProjectProblemsProvider(ProjectProblemsProviderSupport) project problems resolver}.
     * @since 1.41
     */
    public interface ProjectProblemsProviderSupport {

        /**
         * Get actual project for checking problems.
         * @return actual project, never {@code null}
         */
        Project getProject();

        /**
         * Open project customizer with CSS preprocessors.
         * @see CssPreprocessors#CUSTOMIZER_IDENT
         */
        void openCustomizer();

    }

}
