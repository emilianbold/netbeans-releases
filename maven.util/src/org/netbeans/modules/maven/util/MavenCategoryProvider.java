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
package org.netbeans.modules.maven.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 * Allows to register project customizer panels for Maven projects that have
 * specific Maven plugins in their POM.
 * 
 * @author S. Aubrecht
 */
public abstract class MavenCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private MavenPluginParameters pluginParameters;

    final void setPluginParameters( MavenPluginParameters params ) {
        this.pluginParameters = params;
    }

    /**
     * Access utility methods to get/set Maven plugin parameters.
     * @return Plugin parameters or null when not registered properly (without annotations).
     */
    public final MavenPluginParameters getPluginParameters(){
        return pluginParameters;
    }
    
    /**
     * Used to register customizer panels for Maven projects that have specific
     * plugin in their POM.
     * There are three ways this annotation can be used:
     * <ol>
     * <li>Register a "leaf" panel with no children.
     *     {@link #category} can be omitted for a top-level panel;
     *     if specified, the panel is placed in the named subcategory.
     *     {@link #categoryLabel} should not be specified.
     *     The annotation must be placed on a class or factory method implementing {@link CompositeCategoryProvider}.
     * <li>Register a category folder with no panel.
     *     {@link #category} must be specified; the last path component is the
     *     folder being defined, and any previous components are parent folders.
     *     {@link #categoryLabel} must be specified.
     *     The annotation must be placed on some package declaration (in {@code package-info.java}).
     * <li>Register a category folder also with its own panel (i.e. {@code Self}).
     *     {@link #category} and {@link #categoryLabel} must be specified as for #2,
     *     but the annotation must be on a provider implementation as for #1.
     * </ol>
     * To represent hierarchies of panels, the {@link #category} of a #1 can
     * match the {@link #category} of a #2 or #3, and the {@link #category} of a #2 or #3
     * preceding the last {@code /} can match the {@link #category} of another #2 or #3.
     * <p>Multiple registrations may be made in one place using {@link Registrations}.
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {

        /**
         * Maven plugin group id that the project customizer applies to.
         */
        String groupId();

        /**
         * Maven artifact id that the project customizer applies to.
         */
        String artifactId();

        /**
         * Category folder (perhaps multiple components separated by {@code /})
         * in which to place this panel or which is the name of this panel folder.
         */
        String category() default "";
        /**
         * Display name when defining a category folder.
         * Can use {@code pkg.of.Bundle#key_name} syntax.
         */
        String categoryLabel() default "";
        /**
         * Position of this panel or subfolder within its folder.
         */
        int position() default Integer.MAX_VALUE;
    }
}
