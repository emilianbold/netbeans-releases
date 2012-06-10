/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.api.wizards;

import org.netbeans.modules.cnd.makeproject.ui.wizards.NewProjectWizardUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;

/**
 * Constants that are used by wizards
 * @author Vladimir Kvashin
 */
public class WizardConstants {

    private WizardConstants() {
    }

    public static final String PROPERTY_USER_MAKEFILE_PATH = "makefileName"; // String // NOI18N
    public static final String PROPERTY_GENERATED_MAKEFILE_NAME = "generatedMakefileName"; // String // NOI18N
    public static final String PROPERTY_NAME = "name"; // String // NOI18N
    public static final String MAIN_CLASS = "mainClass"; // String // NOI18N
    public static final String PROPERTY_PROJECT_FOLDER = "projdir"; // File // NOI18N
    public static final String PROPERTY_SIMPLE_MODE = "simpleMode"; // Boolean // NOI18N
    public static final String PROPERTY_HOST_UID = "hostUID"; // String // NOI18N
    public static final String PROPERTY_SOURCE_HOST_ENV = "sourceHostEnv"; // ExecutionEnvironment // NOI18N
    public static final String PROPERTY_TOOLCHAIN = "toolchain"; // CompilerSet // NOI18N
    public static final String PROPERTY_TOOLCHAIN_DEFAULT = "toolchainDefault"; // Boolean // NOI18N
    public static final String PROPERTY_READ_ONLY_TOOLCHAIN = "readOnlyToolchain"; // Boolean // NOI18N
    public static final String PROPERTY_SOURCE_FOLDERS = "sourceFolders"; // Iterator<FolderEntry> // NOI18N
    public static final String PROPERTY_SOURCE_FOLDERS_LIST = "sourceFoldersList"; // ArrayList<FolderEntry> // NOI18N
    public static final String PROPERTY_SOURCE_FOLDERS_FILTER = "sourceFoldersFilter"; // String // NOI18N

    public static final String PROPERTY_TOOLS_CACHE_MANAGER = "ToolsCacheManager"; // String // NOI18N
    public static final String PROPERTY_PREFERED_PROJECT_NAME = "displayName"; // String // NOI18N
    public static final String PROPERTY_NATIVE_PROJ_DIR = "nativeProjDir"; // String // NOI18N
    public static final String PROPERTY_NATIVE_PROJ_FO = "nativeProjFO"; // String // NOI18N
    public static final String PROPERTY_BUILD_COMMAND = "buildCommandTextField"; // String // NOI18N
    public static final String PROPERTY_CLEAN_COMMAND = "cleanCommandTextField"; // String // NOI18N
    public static final String PROPERTY_BUILD_RESULT = "outputTextField"; // String // NOI18N
    public static final String PROPERTY_DEPENDENCY_KIND = "dependencyKind"; // IteratorExtension.ProjectKind // NOI18N
    public static final String PROPERTY_DEPENDENCIES = "dependencies"; // ArrayList<String> // NOI18N
    public static final String PROPERTY_TRUE_SOURCE_ROOT = "trueSourceRoot"; // Boolean // NOI18N
    public static final String PROPERTY_INCLUDES = "includeTextField"; // String // NOI18N
    public static final String PROPERTY_MACROS = "macroTextField"; // String // NOI18N
    public static final String PROPERTY_CONFIGURE_SCRIPT_PATH = "configureName"; // String // NOI18N
    public static final String PROPERTY_CONFIGURE_SCRIPT_ARGS = "configureArguments"; // String // NOI18N
    public static final String PROPERTY_CONFIGURE_RUN_FOLDER = "configureRunFolder"; // String // NOI18N
    public static final String PROPERTY_RUN_CONFIGURE = "runConfigure"; // String // NOI18N
    public static final String PROPERTY_CONSOLIDATION_LEVEL = "consolidationLevel"; // String // NOI18N
    public static final String PROPERTY_TEST_FOLDERS = "testFolders"; // String // NOI18N
    public static final String PROPERTY_RUN_REBUILD = "makeProject"; // String // NOI18N
    public static final String PROPERTY_MANUAL_CODE_ASSISTANCE = "manualCA"; // String // NOI18N
    public static final String PROPERTY_WORKING_DIR = "buildCommandWorkingDirTextField"; // String // NOI18N
    public static final String PROPERTY_SOURCE_FOLDER_PATH = "sourceFolderPath"; // String // NOI18N
    public static final String PROPERTY_SIMPLE_MODE_FOLDER = "simpleModeFolder"; // String // NOI18N

    public static final String PROPERTY_REMOTE_FILE_SYSTEM_ENV = "REMOTE_FILE_ENV"; //NOI18N
    
    public static ExecutionEnvironment getSourceExecutionEnvironment(WizardDescriptor wizardDescriptor) {
        ExecutionEnvironment env = (ExecutionEnvironment) wizardDescriptor.getProperty(WizardConstants.PROPERTY_SOURCE_HOST_ENV);
        return (env == null) ? NewProjectWizardUtils.getDefaultSourceEnvironment() : env;
    }    
}
