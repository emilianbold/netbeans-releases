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

package org.netbeans.modules.cnd.makeproject.api.ui.wizard;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.ui.wizards.NewProjectWizardUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Constants that are used by wizards
 */
public final class WizardConstants {

    private WizardConstants() {
    }
    public static final WizardConstant<String> PROPERTY_USER_MAKEFILE_PATH = new WizardConstant<>("makefileName"); // NOI18N
    public static final WizardConstant<String> PROPERTY_GENERATED_MAKEFILE_NAME = new WizardConstant<>("generatedMakefileName"); // NOI18N
    public static final WizardConstant<String> PROPERTY_NAME = new WizardConstant<>("name"); // NOI18N
    public static final WizardConstant<String> MAIN_CLASS = new WizardConstant<>("mainClass"); // NOI18N
    public static final WizardConstant<FSPath> PROPERTY_PROJECT_FOLDER = new WizardConstant<>("projdir"); // NOI18N
    public static final WizardConstant<String> PROPERTY_PROJECT_FOLDER_STRING_VALUE = new WizardConstant<>("projdir.text"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_SIMPLE_MODE = new WizardConstant<>("simpleMode"); // NOI18N
    public static final WizardConstant<String> PROPERTY_HOST_UID = new WizardConstant<>("hostUID"); // NOI18N
    public static final WizardConstant<ExecutionEnvironment> PROPERTY_SOURCE_HOST_ENV = new WizardConstant<>("sourceHostEnv"); // NOI18N
    public static final WizardConstant<CompilerSet> PROPERTY_TOOLCHAIN = new WizardConstant<>("toolchain"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_TOOLCHAIN_DEFAULT = new WizardConstant<>("toolchainDefault"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_READ_ONLY_TOOLCHAIN = new WizardConstant<>("readOnlyToolchain"); // NOI18N
    public static final WizardConstant<Iterator<? extends SourceFolderInfo>> PROPERTY_SOURCE_FOLDERS = new WizardConstant<>("sourceFolders"); // NOI18N
    public static final WizardConstant<List<? extends SourceFolderInfo>> PROPERTY_SOURCE_FOLDERS_LIST = new WizardConstant<>("sourceFoldersList"); // NOI18N
    public static final WizardConstant<String> PROPERTY_SOURCE_FOLDERS_FILTER = new WizardConstant<>("sourceFoldersFilter"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_RESOLVE_SYM_LINKS = new WizardConstant<>("resolveSymLinks"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_USE_BUILD_ANALYZER = new WizardConstant<>("useBuildAnalyzer"); // NOI18N

    public static final WizardConstant<ToolsCacheManager> PROPERTY_TOOLS_CACHE_MANAGER = new WizardConstant<>("ToolsCacheManager"); // NOI18N
    public static final WizardConstant<String> PROPERTY_PREFERED_PROJECT_NAME = new WizardConstant<>("displayName"); // NOI18N
    public static final WizardConstant<String> PROPERTY_NATIVE_PROJ_DIR = new WizardConstant<>("nativeProjDir"); // NOI18N
    public static final WizardConstant<FileObject> PROPERTY_NATIVE_PROJ_FO = new  WizardConstant<>("nativeProjFO"); // NOI18N
    public static final WizardConstant<String> PROPERTY_BUILD_COMMAND = new WizardConstant<>("buildCommandTextField"); // NOI18N
    public static final WizardConstant<String> PROPERTY_CLEAN_COMMAND = new WizardConstant<>("cleanCommandTextField"); // NOI18N
    public static final WizardConstant<String> PROPERTY_BUILD_RESULT = new WizardConstant<>("outputTextField"); // NOI18N
    public static final WizardConstant<IteratorExtension.ProjectKind> PROPERTY_DEPENDENCY_KIND = new WizardConstant<>("dependencyKind"); // NOI18N
    public static final WizardConstant<List<String>> PROPERTY_DEPENDENCIES = new WizardConstant<>("dependencies"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_TRUE_SOURCE_ROOT = new WizardConstant<>("trueSourceRoot"); // NOI18N
    public static final WizardConstant<String> PROPERTY_INCLUDES = new WizardConstant<>("includeTextField"); // NOI18N
    public static final WizardConstant<String> PROPERTY_MACROS = new WizardConstant<>("macroTextField"); // NOI18N
    public static final WizardConstant<String> PROPERTY_CONFIGURE_SCRIPT_PATH = new WizardConstant<>("configureName"); // NOI18N
    public static final WizardConstant<String> PROPERTY_CONFIGURE_SCRIPT_ARGS = new WizardConstant<>("configureArguments"); // NOI18N
    public static final WizardConstant<String> PROPERTY_CONFIGURE_RUN_FOLDER = new WizardConstant<>("configureRunFolder"); // NOI18N
    public static final WizardConstant<String> PROPERTY_CONFIGURE_COMMAND = new WizardConstant<>("configureCommand"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_RUN_CONFIGURE = new WizardConstant<>("runConfigure"); // NOI18N
    public static final WizardConstant<Iterator<? extends SourceFolderInfo>> PROPERTY_TEST_FOLDERS = new WizardConstant<>("testFolders"); // NOI18N
    public static final WizardConstant<List<? extends SourceFolderInfo>> PROPERTY_TEST_FOLDERS_LIST = new WizardConstant<>("testFoldersList"); // NOI18N    
    public static final WizardConstant<Boolean> PROPERTY_RUN_REBUILD = new WizardConstant<>("makeProject"); // NOI18N
    public static final WizardConstant<String> PROPERTY_BUILD_LOG = new WizardConstant<>("buildLog"); // NOI18N
    public static final WizardConstant<Boolean> PROPERTY_MANUAL_CODE_ASSISTANCE = new WizardConstant<>("manualCA"); // NOI18N
    public static final WizardConstant<String> PROPERTY_WORKING_DIR = new WizardConstant<>("buildCommandWorkingDirTextField"); // NOI18N
    public static final WizardConstant<String> PROPERTY_SOURCE_FOLDER_PATH = new WizardConstant<>("sourceFolderPath"); // NOI18N
    public static final WizardConstant<String> PROPERTY_SIMPLE_MODE_FOLDER = new WizardConstant<>("simpleModeFolder"); // NOI18N
    
    public static final WizardConstant<Boolean> PROPERTY_CREATE_MAIN_FILE = new WizardConstant<>("createMainFile"); // NOI18N
    public static final WizardConstant<String> PROPERTY_MAIN_FILE_NAME = new WizardConstant<>("mainFileName"); // NOI18N
    public static final WizardConstant<String> PROPERTY_MAIN_TEMPLATE_NAME = new WizardConstant<>("mainFileTemplate"); // NOI18N
    public static final WizardConstant<String> PROPERTY_LANGUAGE_STANDARD = new WizardConstant<>("languageStandard"); // NOI18N
    public static final WizardConstant<Integer> PROPERTY_ARCHITECURE = new WizardConstant<>("architecture"); // NOI18N

    // the property is not null in case full remote project wizard
    public static final WizardConstant<ExecutionEnvironment> PROPERTY_REMOTE_FILE_SYSTEM_ENV = new WizardConstant<>("REMOTE_FILE_ENV"); //NOI18N
    
    //Interface to dwarf discovery
    //Input properties
    public static final WizardConstant<String> DISCOVERY_BUILD_RESULT = new WizardConstant<>("DW:buildResult"); //NOI18N
    public static final WizardConstant<String> DISCOVERY_LIBRARIES = new WizardConstant<>("DW:libraries"); //NOI18N
    public static final WizardConstant<Boolean> DISCOVERY_RESOLVE_LINKS = new WizardConstant<>("DW:resolveLinks"); //NOI18N
    public static final WizardConstant<FileSystem> DISCOVERY_BINARY_FILESYSTEM = new WizardConstant<>("DW:fileSystem"); //NOI18N
    //Input/Output properties
    public static final WizardConstant<String> DISCOVERY_ROOT_FOLDER = new WizardConstant<>("DW:rootFolder"); //NOI18N
    //Output properties
    public static final WizardConstant<List<String>> DISCOVERY_BINARY_DEPENDENCIES = new WizardConstant<>("DW:dependencies"); //NOI18N
    public static final WizardConstant<List<String>> DISCOVERY_BINARY_SEARCH_PATH = new WizardConstant<>("DW:searchPaths"); //NOI18N
    public static final WizardConstant<String> DISCOVERY_COMPILER = new WizardConstant<>("DW:compiler"); //NOI18N
    public static final WizardConstant<List<String>> DISCOVERY_ERRORS = new WizardConstant<>("DW:errors"); //NOI18N
    
    public static ExecutionEnvironment getSourceExecutionEnvironment(WizardDescriptor wizardDescriptor) {
        ExecutionEnvironment env = WizardConstants.PROPERTY_SOURCE_HOST_ENV.get(wizardDescriptor);
        return (env == null) ? NewProjectWizardUtils.getDefaultSourceEnvironment() : env;
    }

    public static final class WizardConstant<T> {
        private final String key;
        public WizardConstant(String key) {
            this.key = key;
        }
        public String key() {
            return key;
        }
        
        public T get(WizardDescriptor wizard) {
            return (T) wizard.getProperty(key);
        }
        
        public void put(WizardDescriptor wizard, T value){
            wizard.putProperty(key, value);
        }
        
        public T fromMap(Map map) {
            return (T) map.get(key);
        }
        
        public void toMap(Map map, T value){
            map.put(key, value);
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
