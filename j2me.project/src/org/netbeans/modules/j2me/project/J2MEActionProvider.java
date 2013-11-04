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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.BaseActionProvider;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COMPILE_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COPY;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DELETE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_MOVE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RENAME;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
class J2MEActionProvider extends BaseActionProvider {

    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_PROFILE,
        JavaProjectConstants.COMMAND_JAVADOC,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };

    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_PROFILE,
        JavaProjectConstants.COMMAND_JAVADOC,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };

    private static final Map<String,String[]> commands;
    static {
        Map<String,String[]> tmp = new HashMap<>();
        tmp.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        tmp.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        tmp.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        tmp.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        tmp.put(COMMAND_PROFILE, new String[] {"profile"}); // NOI18N
        tmp.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        tmp.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands = Collections.unmodifiableMap(tmp);
    }

    private static final Set<String> bkgScanSensitiveActions = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE
    )));

    private static final Set<String>  needJavaModelActions = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
        JavaProjectConstants.COMMAND_DEBUG_FIX
    )));

    J2MEActionProvider(
        @NonNull final J2MEProject project,
        @NonNull final UpdateHelper updateHelper,
        @NonNull final SourceRoots src,
        @NonNull final SourceRoots test) {
        super(
            project,
            updateHelper,
            project.evaluator(),
            src,
            test,
            updateHelper.getAntProjectHelper(),
            new Provider(project));
    }

    @Override
    protected String[] getPlatformSensitiveActions() {
        return platformSensitiveActions;
    }

    @Override
    protected String[] getActionsDisabledForQuickRun() {
        return supportedActions;
    }

    @Override
    public Map<String, String[]> getCommands() {
        return commands;
    }

    @Override
    protected Set<String> getScanSensitiveActions() {
        return bkgScanSensitiveActions;
    }

    @Override
    protected Set<String> getJavaModelActions() {
        return needJavaModelActions;
    }

    @Override
    protected boolean isCompileOnSaveEnabled() {
        return false;
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    private static final class Provider implements CustomPlatformCallback {

        private final Callback delegate;
        private final PropertyEvaluator eval;


        Provider(@NonNull final J2MEProject prj) {
            delegate = new BaseActionProvider.CallbackImpl(prj.getClassPathProvider());
            eval = prj.evaluator();
        }

        @Override
        public JavaPlatform getActivePlatform() {
            return CommonProjectUtils.getActivePlatform(
                    eval.getProperty(ProjectProperties.PLATFORM_ACTIVE),
                    J2MEProjectProperties.PLATFORM_TYPE_J2ME);
        }

        @Override
        public ClassPath getProjectSourcesClassPath(String type) {
            return delegate.getProjectSourcesClassPath(type);
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            return delegate.findClassPath(file, type);
        }

    }

}
