/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.apigen.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.apigen.commands.ApiGenScript;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * ApiGen preferences for {@link PhpModule}.
 */
public final class ApiGenPreferences {

    public static final Property<String> TITLE = new Property<String>("title") { // NOI18N
        @Override
        public String getDefaultValue(PhpModule phpModule) {
            return phpModule.getDisplayName();
        }
    };
    public static final Property<String> CONFIG = new Property<String>("config") { // NOI18N
        @Override
        public String getDefaultValue(PhpModule phpModule) {
            List<FileObject> dirs = Arrays.asList(
                    phpModule.getProjectDirectory(),
                    phpModule.getSourceDirectory());
            for (FileObject dir : dirs) {
                FileObject config = dir.getFileObject(ApiGenScript.DEFAULT_CONFIG_NAME);
                if (config != null && config.isData()) {
                    return FileUtil.toFile(config).getAbsolutePath();
                }
            }
            return null;
        }
    };
    public static final Property<String> CHARSETS = new Property<String>("charsets") { // NOI18N
        @Override
        public String getDefaultValue(PhpModule phpModule) {
            return phpModule.getProperties().getEncoding();
        }
    };
    public static final Property<String> EXCLUDES = new Property<String>("excludes"); // NOI18N
    public static final Property<String> ACCESS_LEVELS = new Property<String>("accessLevels") { // NOI18N
        @Override
        public String getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_ACCESS_LEVELS;
        }
    };
    public static final Property<Boolean> INTERNAL = new Property<Boolean>("internal") { // NOI18N
        @Override
        public Boolean getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_INTERNAL;
        }
    };
    public static final Property<Boolean> PHP = new Property<Boolean>("php") { // NOI18N
        @Override
        public Boolean getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_PHP;
        }
    };
    public static final Property<Boolean> TREE = new Property<Boolean>("tree") { // NOI18N
        @Override
        public Boolean getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_TREE;
        }
    };
    public static final Property<Boolean> DEPRECATED = new Property<Boolean>("deprecated") { // NOI18N
        @Override
        public Boolean getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_DEPRECATED;
        }
    };
    public static final Property<Boolean> TODO = new Property<Boolean>("todo") { // NOI18N
        @Override
        public Boolean getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_TODO;
        }
    };
    public static final Property<Boolean> DOWNLOAD = new Property<Boolean>("download") { // NOI18N
        @Override
        public Boolean getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_DOWNLOAD;
        }
    };
    public static final Property<Boolean> SOURCE_CODE = new Property<Boolean>("sourceCode") { // NOI18N
        @Override
        public Boolean getDefaultValue(PhpModule phpModule) {
            return ApiGenScript.DEFAULT_SOURCE_CODE;
        }
    };
    public static final Property<Boolean> HAS_CONFIG = new Property<Boolean>("hasConfig"); // NOI18N

    // package private
    static final Property<Object> TARGET = new Property<Object>("target"); // NOI18N

    private static final String DEFAULT_VALUE = ""; // NOI18N
    private static final String SEPARATOR = ","; // NOI18N


    private ApiGenPreferences() {
    }

    public static String getTarget(PhpModule phpModule, boolean showPanel) {
        String target = get(phpModule, TARGET);
        if (StringUtils.isEmpty(target) && showPanel) {
            target = BrowseFolderPanel.open(phpModule);
            if (target == null) {
                // cancelled
                return null;
            }
            put(phpModule, TARGET, target);
        }
        return target;
    }

    public static void putTarget(PhpModule phpModule, String target) {
        put(phpModule, TARGET, target);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    public static String get(PhpModule phpModule, Property<? extends Object> property) {
        // get default value lazyly since it can do anything...
        String value = getPreferences(phpModule).get(property.getKey(), DEFAULT_VALUE);
        if (value == DEFAULT_VALUE) {
            Object defaultValue = property.getDefaultValue(phpModule);
            if (defaultValue == null) {
                return null;
            }
            return defaultValue.toString();
        }
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value;
    }

    public static List<String> getMore(PhpModule phpModule, Property<? extends Object> property) {
        return StringUtils.explode(get(phpModule, property), SEPARATOR);
    }

    public static boolean getBoolean(PhpModule phpModule, Property<Boolean> property) {
        return Boolean.parseBoolean(get(phpModule, property));
    }

    public static void put(PhpModule phpModule, Property<? extends Object> property, String value) {
        if (StringUtils.hasText(value) && !value.equals(property.getDefaultValue(phpModule))) {
            getPreferences(phpModule).put(property.getKey(), value);
        } else {
            getPreferences(phpModule).remove(property.getKey());
        }
    }

    public static void putMore(PhpModule phpModule, Property<? extends Object> property, List<String> values) {
        if (values.isEmpty()
                || Collections.singletonList(property.getDefaultValue(phpModule)).equals(values)) {
            put(phpModule, property, null);
        } else {
            put(phpModule, property, StringUtils.implode(values, SEPARATOR));
        }
    }

    public static void putBoolean(PhpModule phpModule, Property<Boolean> property, boolean value) {
        put(phpModule, property, Boolean.toString(value));
    }

    private static Preferences getPreferences(PhpModule phpModule) {
        return phpModule.getPreferences(ApiGenPreferences.class, false);
    }

    //~ Inner classes

    public static class Property<T> {

        private final String key;

        private Property(String key) {
            assert key != null;
            this.key = key;
        }

        String getKey() {
            return key;
        }

        public T getDefaultValue(PhpModule phpModule) {
            return null;
        }

    }

}
