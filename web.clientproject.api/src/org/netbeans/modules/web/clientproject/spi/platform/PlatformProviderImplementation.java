/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.spi.platform;

import java.net.URL;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.BadgeIcon;
import org.netbeans.spi.project.ActionProvider;

/**
 * Interface for platform provider.
 * @since 1.68
 */
public interface PlatformProviderImplementation {

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this provider.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    String getIdentifier();

    /**
     * Returns the display name of this provider. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    String getDisplayName();

    /**
     * Returns badge icon of this provider.
     * @return badge icon of this provider, can be {@code null}
     */
    @CheckForNull
    BadgeIcon getBadgeIcon();

    /**
     * Checks whether this provider is enabled in the given project.
     * @param project project to be checked
     * @return {@code true} if this provider is enabled in the given project, {@code false} otherwise
     */
    boolean isEnabled(@NonNull Project project);

    /**
     * Gets list of source roots.
     * @param project project to be used
     * @return list of source roots, can be empty but never {@code null}
     */
    List<URL> getSourceRoots(@NonNull Project project);

    /**
     * Get action provider of this provider.
     * @param project project to be source of the action
     * @return action provider of this provider, can be {@code null} if not supported
     */
    @CheckForNull
    ActionProvider getActionProvider(@NonNull Project project);

    /**
     * Notifies provider that the given project is being opened.
     * <p>
     * Provider is notified even if it is not {@link #isEnabled(Project) enabled} in the given project.
     * @param project project being opened
     */
    void projectOpened(@NonNull Project project);

    /**
     * Notifies provider that the given project is being closed.
     * <p>
     * Provider is notified even if it is not {@link #isEnabled(Project) enabled} in the given project.
     * @param project project being closed
     */
    void projectClosed(@NonNull Project project);

}
