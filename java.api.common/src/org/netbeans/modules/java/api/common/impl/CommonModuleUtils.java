/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.impl;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 * @since 1.91
 */
public final class CommonModuleUtils {
    public static final SpecificationVersion JDK9 = new SpecificationVersion("9");  //NOI18N
    private static final String ARG_ADDMODS = "--add-modules";       //NOI18N
    private static final String ARG_PATCH_MOD = "--patch-module";   //NOI18N
    private static final String ARG_XMODULE = "-Xmodule";      //NOI18N
    private static final Pattern MATCHER_XMODULE =
            Pattern.compile(String.format("%s:(\\S+)", ARG_XMODULE));  //NOI18N
    private static final Pattern MATCHER_PATCH =
            Pattern.compile("(.+)=(.+)");  //NOI18N

    private CommonModuleUtils() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }


    @NonNull
    public static Set<String> getAddModules(@NonNull final CompilerOptionsQuery.Result res) {
        Parameters.notNull("res", res); //NOI18N
        final Set<String> mods = new HashSet<>();
        boolean addmod = false;
        for (String arg : res.getArguments()) {
            if (addmod) {
                //<module>(,<module>)*
                mods.addAll(Arrays.stream(arg.split(","))   //NOI18N
                        .map((m) -> m.trim())
                        .collect(Collectors.toList()));
            }
            addmod = ARG_ADDMODS.equals(arg);
        }
        return mods;
    }

    @CheckForNull
    public static String getXModule(@NonNull final CompilerOptionsQuery.Result res) {
        Parameters.notNull("res", res); //NOI18N
        String module = null;
        for (String arg : res.getArguments()) {
            final Matcher m = MATCHER_XMODULE.matcher(arg);
            if (m.matches()) {
                module = m.group(1);
                break;
            }
        }
        return module;
    }

    @NonNull
    public static Map<String,List<URL>> getPatches(@NonNull final CompilerOptionsQuery.Result res) {
        Parameters.notNull("res", res); //NOI18N
        final Map<String,List<URL>> patches = new HashMap<>();
        boolean patch = false;
        for (String arg : res.getArguments()) {
            if (patch) {
                //<module>=<file>(:<file>)*
                final Matcher m = MATCHER_PATCH.matcher(arg);
                if (m.matches() && m.groupCount() == 2) {
                    final String module = m.group(1);
                    final String path = m.group(2);
                    if (!module.isEmpty() && !path.isEmpty()) {
                        patches.putIfAbsent(
                                module,
                                Arrays.stream(PropertyUtils.tokenizePath(path))
                                        .map((p) -> FileUtil.normalizeFile(new File(p)))
                                        .map(FileUtil::urlForArchiveOrDir)
                                        .collect(Collectors.toList()));
                    }
                }
            }
            patch = ARG_PATCH_MOD.equals(arg);
        }
        return patches;
    }
}
