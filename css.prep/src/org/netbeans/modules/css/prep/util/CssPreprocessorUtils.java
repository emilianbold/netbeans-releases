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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.preferences.CssPreprocessorPreferences;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class CssPreprocessorUtils {

    private static final Logger LOGGER = Logger.getLogger(CssPreprocessorUtils.class.getName());

    static final String MAPPINGS_DELIMITER = ","; // NOI18N
    static final String MAPPING_DELIMITER = ":"; // NOI18N


    private CssPreprocessorUtils() {
    }

    @NbBundle.Messages({
        "# {0} - preprocessor name",
        "CssPreprocessorUtils.fileSaved.title=Configure {0}",
        "# {0} - preprocessor name",
        "CssPreprocessorUtils.fileSaved.question=<html>Do you want to configure automatic {0} compilation on save?<br>"
            + "Note that you can always turn this on (or off) later in Project Properties.",
    })
    public static void processSavedFile(Project project, CssPreprocessorType type) {
        assert project != null;
        assert type != null;
        // if not configured, ask user; if YES, prefill preferences and open customizer
        CssPreprocessorPreferences projectPreferences = type.getPreferences();
        assert projectPreferences != null;
        if (projectPreferences.isConfigured(project)) {
            return;
        }
        // we are now configured, in any case
        projectPreferences.setConfigured(project, true);
        String displayName = type.getDisplayName();
        if (!askUser(Bundle.CssPreprocessorUtils_fileSaved_title(displayName), Bundle.CssPreprocessorUtils_fileSaved_question(displayName))) {
            return;
        }
        projectPreferences.setEnabled(project, true);
        projectPreferences.setMappings(project, getDefaultMappings(type));
        CustomizerProvider2 customizerProvider = project.getLookup().lookup(CustomizerProvider2.class);
        assert customizerProvider != null : "CustomizerProvider2 not found in lookup of project " + project.getClass().getName();
        customizerProvider.showCustomizer(CssPreprocessors.CUSTOMIZER_IDENT, null);
    }

    public static List<Pair<String, String>> getDefaultMappings(CssPreprocessorType type) {
        return Collections.singletonList(Pair.of("/" + type.getDefaultDirectoryName(), "/css")); // NOI18N
    }

    private static boolean askUser(String title, String question) {
        Object result = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(question, title, NotifyDescriptor.YES_NO_OPTION));
        return result == NotifyDescriptor.YES_OPTION;
    }

    public static String encodeMappings(List<Pair<String, String>> mappings) {
        StringBuilder buffer = new StringBuilder(200);
        for (Pair<String, String> mapping : mappings) {
            if (buffer.length() > 0) {
                buffer.append(MAPPINGS_DELIMITER);
            }
            buffer.append(mapping.first());
            buffer.append(MAPPING_DELIMITER);
            buffer.append(mapping.second());
        }
        return buffer.toString();
    }

    public static List<Pair<String, String>> decodeMappings(String mappings) {
        List<String> pairs = StringUtils.explode(mappings, MAPPINGS_DELIMITER);
        List<Pair<String, String>> result = new ArrayList<>(pairs.size());
        for (String pair : pairs) {
            List<String> paths = StringUtils.explode(pair, MAPPING_DELIMITER);
            result.add(Pair.of(paths.get(0), paths.get(1)));
        }
        return result;
    }

    @CheckForNull
    public static File resolveTarget(FileObject webRoot, List<Pair<String, String>> mappings, FileObject source) {
        File root = FileUtil.toFile(webRoot);
        File file = FileUtil.toFile(source);
        return resolveTarget(root, mappings, file, source.getName());
    }

    @CheckForNull
    public static File resolveTarget(FileObject webRoot, List<Pair<String, String>> mappings, File source) {
        File root = FileUtil.toFile(webRoot);
        String name = source.getName();
        String extension = FileUtil.getExtension(name);
        if (!extension.isEmpty()) {
            name = name.substring(0, name.length() - (extension.length() + 1));
        }
        return resolveTarget(root, mappings, source, name);
    }

    public static File resolveInput(FileObject webRoot, Pair<String, String> mapping) {
        return resolveFile(FileUtil.toFile(webRoot), mapping.first().trim());
    }

    @CheckForNull
    static File resolveTarget(File root, List<Pair<String, String>> mappings, File file, String name) {
        for (Pair<String, String> mapping : mappings) {
            File from = resolveFile(root, mapping.first().trim());
            String relpath = PropertyUtils.relativizeFile(from, file.getParentFile());
            if (relpath != null
                    && !relpath.startsWith("..")) { // NOI18N
                // path match
                File to = resolveFile(root, mapping.second().trim());
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

        private static final Pattern MAPPING_PATTERN = Pattern.compile("[^" + MAPPING_DELIMITER + "]+"); // NOI18N

        private final ValidationResult result = new ValidationResult();


        public ValidationResult getResult() {
            return result;
        }

        public MappingsValidator validate(List<Pair<String, String>> mappings) {
            validateMappings(mappings);
            return this;
        }

        @NbBundle.Messages({
            "MappingsValidator.warning.none=At least one input and output path must be set.",
            "MappingsValidator.warning.input.empty=Input path cannot be empty.",
            "MappingsValidator.warning.output.empty=Output path cannot be empty.",
            "# {0} - mapping",
            "MappingsValidator.warning.input.format=Input path \"{0}\" is incorrect.",
            "# {0} - mapping",
            "MappingsValidator.warning.output.format=Output path \"{0}\" is incorrect.",
        })
        private MappingsValidator validateMappings(List<Pair<String, String>> mappings) {
            if (mappings.isEmpty()) {
                result.addError(new ValidationResult.Message("mappings", Bundle.MappingsValidator_warning_none())); // NOI18N
            }
            for (Pair<String, String> mapping : mappings) {
                // input
                String input = mapping.first();
                if (!StringUtils.hasText(input)) {
                    result.addError(new ValidationResult.Message("mapping." + input, Bundle.MappingsValidator_warning_input_empty())); // NOI18N
                } else if (!MAPPING_PATTERN.matcher(input).matches()) {
                    result.addError(new ValidationResult.Message("mapping." + input, Bundle.MappingsValidator_warning_input_format(input))); // NOI18N
                }
                // output
                String output = mapping.second();
                if (!StringUtils.hasText(output)) {
                    result.addError(new ValidationResult.Message("mapping." + output, Bundle.MappingsValidator_warning_output_empty())); // NOI18N
                } else if (!MAPPING_PATTERN.matcher(output).matches()) {
                    result.addError(new ValidationResult.Message("mapping." + output, Bundle.MappingsValidator_warning_output_format(output))); // NOI18N
                }
            }
            return this;
        }

    }

}
