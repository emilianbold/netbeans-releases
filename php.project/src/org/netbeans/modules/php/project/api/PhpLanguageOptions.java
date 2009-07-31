/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.api;

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.openide.filesystems.FileObject;

/**
 * Helper class to get PHP language properties like ASP tags supported etc.
 * @author Tomas Mysik
 * @since 2.3
 */
public final class PhpLanguageOptions {
    /**
     * The default value for short tags (&lt?) (<code>{@value #SHORT_TAGS_ENABLED}</code>).
     */
    public static final boolean SHORT_TAGS_ENABLED = true;
    /**
     * The default value for ASP tags (&lt% and %&gt;) (<code>{@value #ASP_TAGS_ENABLED}</code>).
     */
    public static final boolean ASP_TAGS_ENABLED = false;

    private PhpLanguageOptions() {
    }

    /**
     * Get {@link Properties PHP language properties} for the given file (can be <code>null</code>).
     * These properties are project specific. If no project is found for the file, then properties with the default values are returned.
     * @param file a file which could belong to a project (if not or <code>null</code>, properties with the default values are returned).
     * @return {@link Properties properties}.
     * @see #SHORT_TAGS_ENABLED
     * @see #ASP_TAGS_ENABLED
     */
    public static Properties getProperties(FileObject file) {
        boolean shortTagsEnabled = SHORT_TAGS_ENABLED;
        boolean aspTagsEnabled = ASP_TAGS_ENABLED;

        if (file != null) {
            PhpProject phpProject = org.netbeans.modules.php.project.util.PhpProjectUtils.getPhpProject(file);
            if (phpProject != null) {
                shortTagsEnabled = ProjectPropertiesSupport.areShortTagsEnabled(phpProject);
                aspTagsEnabled = ProjectPropertiesSupport.areAspTagsEnabled(phpProject);
            }
        }
        return new Properties(shortTagsEnabled, aspTagsEnabled);
    }

    /**
     * Data object for PHP language properties.
     */
    public static final class Properties {
        private final boolean shortTagsEnabled;
        private final boolean aspTagsEnabled;

        Properties(boolean shorTagsEnabled, boolean aspTagsEnabled) {
            this.shortTagsEnabled = shorTagsEnabled;
            this.aspTagsEnabled = aspTagsEnabled;
        }

        /**
         * Find out whether short tags (&lt;?) are supported or not. This option is project specific.
         * If no project is found for the file, then {@link #SHORT_TAGS_ENABLED the default value} is returned.
         * @return <code>true</code> if short tags are supported, <code>false</code> otherwise.
         * @see #SHORT_TAGS_ENABLED
         */
        public boolean areShortTagsEnabled() {
            return shortTagsEnabled;
        }

        /**
         * Find out whether ASP tags (&lt% and %&gt;) are supported or not. This option is project specific.
         * If no project is found for the file, then {@link #ASP_TAGS_ENABLED the default value} is returned.
         * @return <code>true</code> if ASP tags are supported, <code>false</code> otherwise.
         * @see #ASP_TAGS_ENABLED
         */
        public boolean areAspTagsEnabled() {
            return aspTagsEnabled;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(100);
            sb.append(getClass().getName());
            sb.append(" [shorTagsEnabled: ");
            sb.append(shortTagsEnabled);
            sb.append(", aspTagsEnabled: ");
            sb.append(aspTagsEnabled);
            sb.append("]");
            return sb.toString();
        }
    }
}
