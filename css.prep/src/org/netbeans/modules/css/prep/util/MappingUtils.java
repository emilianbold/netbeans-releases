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
package org.netbeans.modules.css.prep.util;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class MappingUtils {

    private static final String MAPPINGS_DELIMITER = ","; // NOI18N
    private static final String MAPPING_DELIMITER = ":"; // NOI18N


    private MappingUtils() {
    }

    public static String encode(List<String> mappings) {
        return StringUtils.implode(mappings, MAPPINGS_DELIMITER);
    }

    public static List<String> decode(String mappings) {
        return StringUtils.explode(mappings, MAPPINGS_DELIMITER);
    }

    @CheckForNull
    public static File resolveTarget(FileObject webRoot, List<String> mappings, FileObject source) {
        File root = FileUtil.toFile(webRoot);
        File file = FileUtil.toFile(source);
        return resolveTarget(root, mappings, file, source.getName());
    }

    @CheckForNull
    static File resolveTarget(File root, List<String> mappings, File file, String name) {
        for (String mapping : mappings) {
            List<String> exploded = StringUtils.explode(mapping, MAPPING_DELIMITER);
            File from = resolveFile(root, exploded.get(0).trim());
            String relpath = PropertyUtils.relativizeFile(from, file.getParentFile());
            if (relpath != null
                    && !relpath.startsWith("../")) { // NOI18N
                // path match
                File to = resolveFile(root, exploded.get(1).trim());
                to = PropertyUtils.resolveFile(to, relpath);
                return resolveFile(to, makeCssFilename(name));
            }
        }
        // no mapping
        return null;
    }

    private static File resolveFile(File directory, String subpath) {
        if (subpath.startsWith("/")) { // NOI18N
            subpath = subpath.substring(1);
        }
        return PropertyUtils.resolveFile(directory, subpath);
    }

    private static String makeCssFilename(String name) {
        return name + ".css"; // NOI18N
    }

    //~ Inner classes

    public static final class MappingsValidator {

        private static final Pattern MAPPING_PATTERN = Pattern.compile("^[^:]*[^: ][^:]*:[^:]*[^: ][^:]*$"); // NOI18N

        private final ValidationResult result = new ValidationResult();


        public ValidationResult getResult() {
            return result;
        }

        public MappingsValidator validate(List<String> mappings) {
            validateMappings(mappings);
            return this;
        }

        @NbBundle.Messages({
            "MappingsValidator.warning.empty=CSS output folders must be set.",
            "# {0} - mapping",
            "MappingsValidator.warning.format=CSS output folder \"{0}\" is incorrect.",
        })
        private MappingsValidator validateMappings(List<String> mappings) {
            if (mappings.isEmpty()) {
                result.addWarning(new ValidationResult.Message("mappings", Bundle.MappingsValidator_warning_empty())); // NOI18N
            }
            for (String mapping : mappings) {
                if (!MAPPING_PATTERN.matcher(mapping).matches()) {
                    result.addWarning(new ValidationResult.Message("mapping." + mapping, Bundle.MappingsValidator_warning_format(mapping))); // NOI18N
                }
            }
            return this;
        }

    }


}
