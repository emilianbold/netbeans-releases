/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.BrokenReferencesSupport;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.ArchiverConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.AssemblerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CodeAssistanceConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder.Kind;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.RequiredProjectsConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;

/**
 * Common subclass to ConfigurationXMLCodec and AuxConfigurationXMLCodec.
 * 
 * Change History:
 * V89 - NB 8.0
 *    support compile command
 * V88 - NB 7.3 (!!!!!!!!!!INVERTED SERIALIZATION!!!!!!!!!!!!)
 *    1) This is the version where serialization of unmanaged projects were inverted
 *    instead of excluded items and personally attributed items we store all 
 *    non-excluded or non-default attributed items. 
 *    2) To support include paths and macro values we keep user specified 
 *       CODE_ASSISTANCE_TRANSIENT_MACROS_ELEMENT and CODE_ASSISTANCE_ENVIRONMENT_ELEMENT
 * V87 - NB 7.3 
 *    roll back default value in BooleanConfiguration for ItemConfiguration (69fa2dbc8b7c)
 * V86 - NB 7.3
 *    roll back changes introduced in V85
 * V85 - NB 7.3
 *    Configurations descriptor is divided on three parts (public, default public and private).
 *    Actual for unmanaged projects.
 *    Project tree and code assistance properties are moved in private area.
 *    Initial project tree and initial code assistance properties (result of project creation) are
 *    duplicated in the default configurations.
 *    Default configurations is read only file that is loaded if private area is absent.
 * V84 - NB 7.2
 *    Support undefined macros
 * V83 - NB 7.2
 *    Code Assistance general properties.
 *    use build analyzer and tools list field introduced in configuration for unmanaged projects.
 * V82 - NB 7.2
 *    Hardcoded extension of dynamic library from project is replaced by macros.
 *    New flavor2 field introduced in item configuration for unmanaged projects.
 * V81 - NB 7.2
 *    Standard selection support for C++ compiler
 *    Standard selection support for C compiler
 * V80 - NB 7.1
 *    Custom configurations
 * V79 - NB 7.0
 *    Configuration type (CONFIGURATION_TYPE_ELEMENT) in project.xml
 * V78 - NB 7.0
 *    storing active configuration index in private/private.xml and no longer in private/configurations.xml
 * V77 (76?) - NB 7.0
 *    Store configuration type in project.xml
 * V76 - NB 7.0
 *    reintroducing No longer generation makefiles for unmanaged projects. Calling projects make directly
 * V75 - NB 7.0
 *    backed out V74
 * V74 - NB 7.0
 *    No longer generation makefiles for unmanaged projects. Calling projects make directly
 * V73 - NB 7.0
 *    add C/C++ flag USE_LINKER_PKG_CONFIG_LIBRARIES
 * Without changing version
 *    added REMOTE_FILESYSTEM_BASE_DIR tag in project.xml
 * V72 - NB 7.0
 *   move platform in private project area
 * V71 - NB 7.0
 *   introduce default tool collection
 * V70 - NB 7.0
 *   move DEVELOPMENT_SERVER_ELEMENT in private area
 * Without changing version yet - NB 7.0
 *   Added remoteSyncFactory
 *   Added language flavor
 * V69 - NB 6.9
 *   Also writing source roots and configurations in project.xml
 * V68 - NB 6.9
 *   Assembler: ASMTOOL_ELEMENT
 * V67 - NB 6.9
 *   REBUILD_PROP_CHANGED
 * V66 - NB 6.9
 *   ranlib tool: RANLIB_TOOL_ELEMENT
 * V65 - NB 6.9
 *   Test folders: TEST_ROOT_LIST_ELEMENT
 * V64 - NB 6.9
 *   Test folders: KIND_ATTR
 * V63 - NB 6.7
 *   REMOVE_INSTRUMENTATION_ELEMENT
 * V62 - NB 6.7
 *   SOURCE_FOLDER_FILTER_ELEMENT
 * V61 - NB 6.7
 *   Store only C/C++ files in disk file list. Other files are dynamically added to view.
 * V60 - NB 6.7
 *   PACK_ADDITIONAL_INFOS_LIST_ELEMENT
 * V59 (?) - NB 6.7
 *   QT_QMAKE_SPEC_ELEMENT
 * V58 - NB 6.7
 *   CCOMPILERTOOL_ELEMENT2
 *   CCCOMPILERTOOL_ELEMENT2
 *   INCLUDE_DIRECTORIES_ELEMENT2
 *   PATH_ELEMENT
 * V57 - NB 6.7
 *   new attributes for ITEM_ELEMENT: <item path="../gcc/zlib/examples/gzlog.h" ex="true" tool="1">
 * V56 - NB 6.7
 *   Don't write ITEM_ELEMENT (item configuration) if default values
 * V55 - NB 6.7
 *   DISK_FOLDER_ELEMENT
 *   ITEM_NAME_ELEMENT
 * V54 - NB 6.7
 *   Qt settings are persisted:
 *   QT_ELEMENT
 *   QT_DESTDIR_ELEMENT
 *   QT_TARGET_ELEMENT
 *   QT_TARGET_VERSION_ELEMENT
 *   QT_BUILD_MODE_ELEMENT
 *   QT_MODULES_ELEMENT
 *   QT_MOC_DIR_ELEMENT
 *   QT_RCC_DIR_ELEMENT
 *   QT_UI_DIR_ELEMENT
 *   QT_DEFS_LIST_ELEMENT
 * V53 - NB 6.7
 *   New configuration types: 4 (QT_APPLICATION), 5 (QT_DYNAMIC_LIBRARY), 6 (QT_STATIC_LIBRARY)
 * V52 - NB 6.7
 *   ASSEMBLER_REQUIRED_ELEMENT
 * V51 - NB 6.5
 *   Now storing package type as name and not as int
 * V50 - 09.10.08 - NB 6.5
 *   Moved source encoding (SOURCE_ENCODING_ELEMENT) to project.xml
 * V49 - 09.02.08 - NB 6.5
 *   RPM package
 * V48 - 08.08.22 - NB 6.5
 *   PACK_TOPDIR_ELEMENT
 * V47 - 08.01.08 - NB 6.5
 *   Packaging persistence:
 *   ADDITIONAL_OPTIONS_ELEMENT
 *   VALUE_ATTR
 *   MANDATORY_ATTR
 *   TO_ATTR
 *   FROM_ATTR
 *   PERM_ATTR
 *   OWNER_ATTR
 *   GROUP_ATTR
 *   VERBOSE_ELEMENT
 *   PACK_ELEMENT
 *   PACK_TYPE_ELEMENT
 *   PACK_FILES_LIST_ELEMENT
 *   PACK_FILE_LIST_ELEMENT
 *   PACK_INFOS_LIST_ELEMENT
 *   PACK_INFO_LIST_ELEMENT
 * V46 - 06.17.08 - NB 6.5
 *   [Remote] Development Server
 * V45 - 01.12-08
 *  Encoding
 * V44 - 3.08.08 - NB 6.1
 *   ???
 * V43 - 3.06.08 - NB 6.1
 *   Storing compiler flavor as name|flavor
 * V42 - 2.26.08 - NB 6.1
 *   Now storing required tools (c, cpp, ...) only if different from default value 
 * V41:
 *   Added SOURCE_ROOT_LIST_ELEMENT
 * V40:
 *   Added PREPROCESSOR_LIST_ELEMENT and LIST_ELEMENT and saving preprocessor symbols as a list
 * V39:
 *   Added Required Projects for unmanaged projects (REQUIRED_PROJECTS_ELEMENT)
 * V38:
 *   Added Mac OS X platform == 4 and changed Generic platform to 5
 * V37:
 *   Moved active configuration (DEFAULT_CONF_ELEMENT) to private
 * V36:
 *   ARCHITECTURE_ELEMENT added.
 * V35:
 *   Gordon added COMPILER set changes?
 * V34:
 *   Added C_REQUIRED_ELEMENT, CPP_REQUIRED_ELEMENT, and FORTRAN_REQUIRED_ELEMENT for build validation
 *   Changed COMPILER_SET_ELEMENT semantics from expecting an integer (0 or 1)
 *   to expecting a string (Sun, GNU, Cygwin, ...)
 * V33:
 *   Added DEPENDENCY_CHECKING (makefile dependency checking)
 * V32:
 *   Added Folder level configurations (FolderXMLCodc)
 * V31:
 *   Now emitting compiler tool info for makefile based projects. This affects 
 *   cCompilerTool, ccCompilerTool, and fortranCompilerTool elements.
 * V30:
 *   added PROJECT_MAKEFILE_ELEMENT (project makefile name)
 * V29:
 *   added FORTRANCOMPILERTOOL_ELEMENT
 */
public abstract class CommonConfigurationXMLCodec
        extends XMLDecoder
        implements XMLEncoder {
    
    public final static int VERSION_WITH_INVERTED_SERIALIZATION = 88;
    public final static int CURRENT_VERSION = 89;
    // Generic
    protected final static String PROJECT_DESCRIPTOR_ELEMENT = "projectDescriptor"; // NOI18N
    protected final static String DEBUGGING_ELEMENT = "justfordebugging"; // NOI18N
    // Old style. FIXUP: should be removed....
    public final static String CONFIGURATION_DESCRIPTOR_ELEMENT = "configurationDescriptor"; // NOI18N
    protected final static String DEFAULT_CONF_ELEMENT = "defaultConf"; // NOI18N
    public final static String CONFS_ELEMENT = "confs"; // NOI18N
    public final static String CONF_ELEMENT = "conf"; // NOI18N
    protected final static String DIRECTORY_PATH_ELEMENT = "directoryPath"; // NOI18N
    protected final static String PATH_ELEMENT = "pElem"; // NOI18N
    protected final static String FOLDER_PATH_ELEMENT = "folderPath"; // Old style. FIXUP : < version 5 // NOI18N
    protected final static String SOURCE_FOLDERS_ELEMENT = "sourceFolders"; // Old style. FIXUP : < version 5 // NOI18N
    protected final static String LOGICAL_FOLDER_ELEMENT = "logicalFolder"; // NOI18N
    protected final static String DISK_FOLDER_ELEMENT = "df"; // NOI18N
    protected final static String ITEM_PATH_ELEMENT = "itemPath"; // NOI18N
    protected final static String ITEM_NAME_ELEMENT = "in"; // NOI18N
    protected final static String PROJECT_MAKEFILE_ELEMENT = "projectmakefile"; // NOI18N
    protected final static String REQUIRED_PROJECTS_ELEMENT = "requiredProjects"; // NOI18N
    protected final static String SOURCE_ROOT_LIST_ELEMENT = "sourceRootList"; // NOI18N
    protected final static String TEST_ROOT_LIST_ELEMENT = "testRootList"; // NOI18N
    protected final static String SOURCE_FOLDERS_FILTER_ELEMENT = "sourceFolderFilter"; // NOI18N
    protected final static String SOURCE_ENCODING_ELEMENT = "sourceEncoding"; // NOI18N
    // Tools Set (Compiler set and platform)
    public final static String TOOLS_SET_ELEMENT = "toolsSet"; // NOI18N
    public final static String DEVELOPMENT_SERVER_ELEMENT = "developmentServer"; // NOI18N
    public final static String FIXED_SYNC_FACTORY_ELEMENT = "remoteSyncFactory"; // NOI18N
    public final static String COMPILER_SET_ELEMENT = "compilerSet"; // NOI18N
    protected final static String C_REQUIRED_ELEMENT = "cRequired"; // NOI18N
    protected final static String CPP_REQUIRED_ELEMENT = "cppRequired"; // NOI18N
    protected final static String FORTRAN_REQUIRED_ELEMENT = "fortranRequired"; // NOI18N
    protected final static String ASSEMBLER_REQUIRED_ELEMENT = "assemblerRequired"; // NOI18N
    public final static String PLATFORM_ELEMENT = "platform"; // NOI18N
    protected final static String DEPENDENCY_CHECKING = "dependencyChecking"; // NOI18N
    protected final static String REBUILD_PROP_CHANGED = "rebuildPropChanged"; // NOI18N
    // Compile Type
    protected final static String NEO_CONF_ELEMENT = "neoConf"; // Old style. FIXUP : should be removed.... // NOI18N
    protected final static String COMPILE_TYPE_ELEMENT = "compileType"; // NOI18N
    // Makefile Type
    protected final static String EXT_CONF_ELEMENT = "extConf"; // Old style. FIXUP : should be removed.... // NOI18N
    protected final static String MAKEFILE_TYPE_ELEMENT = "makefileType"; // NOI18N
    protected final static String MAKETOOL_ELEMENT = "makeTool"; // NOI18N
    protected final static String BUILD_COMMAND_ELEMENT = "buildCommand"; // NOI18N
    protected final static String BUILD_COMMAND_WORKING_DIR_ELEMENT = "buildCommandWorkingDir"; // NOI18N
    protected final static String CLEAN_COMMAND_ELEMENT = "cleanCommand"; // NOI18N
    protected final static String EXECUTABLE_PATH_ELEMENT = "executablePath"; // NOI18N
    // Compile
    protected static final String COMPILE_ID = "compile"; // NOI18N
    protected final static String COMPILE_DIR_ELEMENT = "compiledir"; // NOI18N
    protected final static String COMPILE_DIR_PICKLIST_ELEMENT = "compiledirpicklist"; // NOI18N
    protected final static String COMPILE_DIR_PICKLIST_ITEM_ELEMENT = "compiledirpicklistitem"; // NOI18N
    protected final static String COMPILE_COMMAND_ELEMENT = "compilecommand"; // NOI18N
    protected final static String COMPILE_COMMAND_PICKLIST_ELEMENT = "compilecommandpicklist"; // NOI18N
    protected final static String COMPILE_COMMAND_PICKLIST_ITEM_ELEMENT = "compilecommandpicklistitem"; // NOI18N
    // Common
    protected final static String COMMANDLINE_TOOL_ELEMENT = "commandlineTool"; // NOI18N
    protected final static String ADDITIONAL_DEP_ELEMENT = "additionalDep"; // NOI18N
    protected final static String ADDITIONAL_OPTIONS_ELEMENT = "additionalOptions"; // NOI18N
    public final static String OUTPUT_ELEMENT = "output"; // NOI18N
    protected final static String INHERIT_INC_VALUES_ELEMENT = "inheritIncValues"; // NOI18N
    protected final static String INHERIT_PRE_VALUES_ELEMENT = "inheritPreValues"; // NOI18N
    protected final static String INHERIT_UNDEF_VALUES_ELEMENT = "inheritUndefValues"; // NOI18N
    protected final static String USE_LINKER_PKG_CONFIG_LIBRARIES = "useLinkerLibraries"; // NOI18N
    // Code Assistance
    protected final static String CODE_ASSISTANCE_ELEMENT = "codeAssistance"; // NOI18N
    protected final static String BUILD_ANALAZYER_ELEMENT = "buildAnalyzer"; // NOI18N
    protected final static String BUILD_ANALAZYER_TOOLS_ELEMENT = "buildAnalyzerTools"; // NOI18N
    protected final static String CODE_ASSISTANCE_ENVIRONMENT_ELEMENT = "envVariables"; // NOI18N
    protected final static String CODE_ASSISTANCE_TRANSIENT_MACROS_ELEMENT = "transientMacros"; // NOI18N
    // Compiler (Generic) Tool
    protected final static String INCLUDE_DIRECTORIES_ELEMENT = "includeDirectories"; // NOI18N
    protected final static String INCLUDE_DIRECTORIES_ELEMENT2 = "incDir"; // NOI18N
    protected final static String COMPILERTOOL_ELEMENT = "compilerTool"; // OLD style. FIXUP < version 11 // NOI18N
    protected final static String DEBUGGING_SYMBOLS_ELEMENT = "debuggingSymbols"; // NOI18N
    protected final static String OPTIMIZATION_LEVEL_ELEMENT = "optimizationLevel"; // NOI18N
    protected final static String DEVELOPMENT_MODE_ELEMENT = "developmentMode"; // NOI18N
    protected final static String COMMAND_LINE_ELEMENT = "commandLine"; // NOI18N
    protected final static String STRIP_SYMBOLS_ELEMENT = "stripSymbols"; // NOI18N
    protected final static String SIXTYFOUR_BITS_ELEMENT = "sixtyfourBits"; // NOI18N
    protected final static String ARCHITECTURE_ELEMENT = "architecture"; // NOI18N
    protected final static String STANDARD_ELEMENT = "standard"; // NOI18N
    protected final static String PREPROCESSOR_ELEMENT = "preprocessor"; // NOI18N
    protected final static String PREPROCESSOR_LIST_ELEMENT = "preprocessorList"; // NOI18N
    protected final static String UNDEFS_LIST_ELEMENT = "undefinedList"; // NOI18N
    protected final static String SUPRESS_WARNINGS_ELEMENT = "supressWarnings"; // NOI18N
    protected final static String WARNING_LEVEL_ELEMENT = "warningLevel"; // NOI18N
    protected final static String MT_LEVEL_ELEMENT = "mtLevel"; // NOI18N
    protected final static String STANDARDS_EVOLUTION_ELEMENT = "standardsEvolution"; // NOI18N
    protected final static String LANGUAGE_EXTENSION_ELEMENT = "languageExtension"; // NOI18N
    // C Compiler Tool
    protected final static String SUN_CCOMPILERTOOL_OLD_ELEMENT = "sunCCompilerTool"; // FIXUP <=23 // NOI18N
    protected final static String CCOMPILERTOOL_ELEMENT = "cCompilerTool"; // NOI18N
    protected final static String CCOMPILERTOOL_ELEMENT2 = "cTool"; // NOI18N
    protected final static String CONFORMANCE_LEVEL_ELEMENT = "conformanceLevel"; // FIXUP: <=21 // NOI18N
    protected final static String CPP_STYLE_COMMENTS_ELEMENT = "cppstylecomments"; // FIXUP: <=21 // NOI18N
    // CC Compiler Tool
    protected final static String SUN_CCCOMPILERTOOL_OLD_ELEMENT = "sunCCCompilerTool"; // FIXUP <=23 // NOI18N
    protected final static String CCCOMPILERTOOL_ELEMENT = "ccCompilerTool"; // NOI18N
    protected final static String CCCOMPILERTOOL_ELEMENT2 = "ccTool"; // NOI18N
    protected final static String COMPATIBILITY_MODE_ELEMENT = "compatibilityMode"; // FIXUP: <=21 // NOI18N
    protected final static String LIBRARY_LEVEL_ELEMENT = "libraryLevel"; // NOI18N
    // Fortran Compiler Tool
    protected final static String FORTRANCOMPILERTOOL_ELEMENT = "fortranCompilerTool"; // NOI18N
    // Asm Compiler Tool
    protected final static String ASMTOOL_ELEMENT = "asmTool"; // NOI18N
    // Custom Tool
    protected final static String CUSTOMTOOL_ELEMENT = "customTool"; // NOI18N
    protected final static String CUSTOMTOOL_COMMANDLINE_ELEMENT = "customToolCommandline"; // NOI18N
    protected final static String CUSTOMTOOL_DESCRIPTION_ELEMENT = "customToolDescription"; // NOI18N
    protected final static String CUSTOMTOOL_OUTPUTS_ELEMENT = "customToolOutputs"; // NOI18N
    protected final static String CUSTOMTOOL_ADDITIONAL_DEP_ELEMENT = "customToolAdditionalDep"; // NOI18N
    // Linker Tool
    protected final static String LINKERTOOL_ELEMENT = "linkerTool"; // NOI18N
    protected final static String LINKER_KPIC_ELEMENT = "linkerKpic"; // NOI18N
    protected final static String LINKER_NORUNPATH_ELEMENT = "linkerNorunpath"; // NOI18N
    protected final static String LINKER_ASSIGN_ELEMENT = "linkerAssign"; // NOI18N
    protected final static String LINKER_ADD_LIB_ELEMENT = "linkerAddLib"; // NOI18N
    protected final static String LINKER_DYN_SERCH_ELEMENT = "linkerDynSerch"; // NOI18N
    protected final static String LINKER_LIB_ELEMENT = "linkerLib"; // NOI18N
    protected final static String LINKER_LIB_ITEMS_ELEMENT = "linkerLibItems"; // NOI18N
    protected final static String LINKER_LIB_PROJECT_ITEM_ELEMENT = "linkerLibProjectItem"; // NOI18N
    protected final static String LINKER_LIB_STDLIB_ITEM_ELEMENT = "linkerLibStdlibItem"; // NOI18N
    protected final static String LINKER_LIB_LIB_ITEM_ELEMENT = "linkerLibLibItem"; // NOI18N
    protected final static String LINKER_LIB_FILE_ITEM_ELEMENT = "linkerLibFileItem"; // NOI18N
    protected final static String LINKER_LIB_OPTION_ITEM_ELEMENT = "linkerOptionItem"; // NOI18N
    // Make Artifact
    public final static String MAKE_ARTIFACT_ELEMENT = "makeArtifact"; // NOI18N
    protected final static String MAKE_ARTIFACT_PL_ELEMENT = "PL"; // NOI18N
    protected final static String MAKE_ARTIFACT_CT_ELEMENT = "CT"; // NOI18N
    protected final static String MAKE_ARTIFACT_CN_ELEMENT = "CN"; // NOI18N
    protected final static String MAKE_ARTIFACT_AC_ELEMENT = "AC"; // NOI18N
    protected final static String MAKE_ARTIFACT_BL_ELEMENT = "BL"; // NOI18N
    protected final static String MAKE_ARTIFACT_WD_ELEMENT = "WD"; // NOI18N
    protected final static String MAKE_ARTIFACT_BC_ELEMENT = "BC"; // NOI18N
    protected final static String MAKE_ARTIFACT_CC_ELEMENT = "CC"; // NOI18N
    public final static String MAKE_ARTIFACT_OP_ELEMENT = "OP"; // NOI18N
    // Archiver Tool
    protected final static String ARCHIVERTOOL_ELEMENT = "archiverTool"; // NOI18N
    protected final static String RANLIB_TOOL_ELEMENT = "ranlibTool"; // NOI18N
    protected final static String ARCHIVERTOOL_RUN_RANLIB_ELEMENT = "runRanlib"; // NOI18N
    protected final static String ARCHIVERTOOL_VERBOSE_ELEMENT = "archiverVerbose"; // NOI18N
    protected final static String ARCHIVERTOOL_SUPRESS_ELEMENT = "archiverSupress"; // NOI18N
    public final static String VERSION_ATTR = "version"; // NOI18N
    protected final static String TYPE_ATTR = "type"; // NOI18N
    protected final static String CUSTOMIZERID_ATTR = "customizerid"; // NOI18N
    protected final static String KIND_ATTR = "kind"; // NOI18N
    protected final static String NAME_ATTR = "name"; // NOI18N
    protected final static String ROOT_ATTR = "root"; // NOI18N
    protected final static String SET_ATTR = "set"; // NOI18N
    protected final static String DISPLAY_NAME_ATTR = "displayName"; // NOI18N
    protected final static String PROJECT_FILES_ATTR = "projectFiles"; // NOI18N
    protected final static String VALUE_ATTR = "value"; // NOI18N
    protected final static String MANDATORY_ATTR = "mandatory"; // NOI18N
    protected final static String TO_ATTR = "to"; // NOI18N
    protected final static String FROM_ATTR = "from"; // NOI18N
    protected final static String PERM_ATTR = "perm"; // NOI18N
    protected final static String OWNER_ATTR = "owner"; // NOI18N
    protected final static String GROUP_ATTR = "group"; // NOI18N
    protected final static String TRUE_VALUE = "true"; // NOI18N
    protected final static String FALSE_VALUE = "false"; // NOI18N
    protected final static String LIST_ELEMENT = "Elem"; // NOI18N
    protected final static String VERBOSE_ELEMENT = "verbose"; // NOI18N
    protected final static String PACK_ELEMENT = "packaging"; // NOI18N
    protected final static String PACK_TYPE_ELEMENT = "packType"; // NOI18N
    protected final static String PACK_FILES_LIST_ELEMENT = "packFileList"; // NOI18N
    protected final static String PACK_FILE_LIST_ELEMENT = "packFileListElem"; // NOI18N
    protected final static String PACK_INFOS_LIST_ELEMENT = "packInfoList"; // NOI18NP
    protected final static String PACK_INFO_LIST_ELEMENT = "packInfoListElem"; // NOI18NP
    protected final static String PACK_ADDITIONAL_INFOS_LIST_ELEMENT = "packAddInfosListElem"; // NOI18NP
    protected final static String PACK_TOPDIR_ELEMENT = "packTopDir"; // NOI18N
    // Qt-related
    protected final static String QT_ELEMENT = "qt"; // NOI18N
    protected final static String QT_DESTDIR_ELEMENT = "destdir"; // NOI18N
    protected final static String QT_TARGET_ELEMENT = "target"; // NOI18N
    protected final static String QT_VERSION_ELEMENT = "version"; // NOI18N
    protected final static String QT_BUILD_MODE_ELEMENT = "buildMode"; // NOI18N
    protected final static String QT_MODULES_ELEMENT = "modules"; // NOI18N
    protected final static String QT_MOC_DIR_ELEMENT = "mocDir"; // NOI18N
    protected final static String QT_RCC_DIR_ELEMENT = "rccDir"; // NOI18N
    protected final static String QT_UI_DIR_ELEMENT = "uiDir"; // NOI18N
    protected final static String QT_DEFS_LIST_ELEMENT = "defs"; // NOI18N
    protected final static String QT_QMAKE_SPEC_ELEMENT = "qmakeSpec"; // NOI18N
    private final ConfigurationDescriptor projectDescriptor;
    private final boolean publicLocation;
    
    public static final int PROJECT_LEVEL = 0;
    public static final int FOLDER_LEVEL = 1;
    public static final int ITEM_LEVEL = 2;

    protected CommonConfigurationXMLCodec(ConfigurationDescriptor projectDescriptor, boolean publicLocation) {
        this.projectDescriptor = projectDescriptor;
        this.publicLocation = publicLocation;
    }

    // interface XMLEncoder
    @Override
   public void encode(XMLEncoderStream xes) {
        xes.elementOpen(CONFIGURATION_DESCRIPTOR_ELEMENT, CURRENT_VERSION);
        if (publicLocation) {
            writeLogicalFolders(xes);
            writeSourceRoots(xes);
            //writeSourceEncoding(xes);
        } else {
            writePrivatePhysicalFoldersForUnmanagedProject(xes);
        }
        xes.element(PROJECT_MAKEFILE_ELEMENT, ((MakeConfigurationDescriptor) projectDescriptor).getProjectMakefileName());
//        if (!publicLocation) {
//            xes.element(DEFAULT_CONF_ELEMENT, "" + projectDescriptor.getConfs().getActiveAsIndex()); // NOI18N
//        }
        writeConfsBlock(xes);
        xes.elementClose(CONFIGURATION_DESCRIPTOR_ELEMENT);
    }
    
    private void writeConfsBlock(XMLEncoderStream xes) {
        xes.elementOpen(CONFS_ELEMENT);

        Configurations confs = projectDescriptor.getConfs();
        for (int i = 0; i < confs.size(); i++) {

            MakeConfiguration makeConfiguration =
                    (MakeConfiguration) confs.getConf(i);

            if (makeConfiguration.isCustomConfiguration()) {
                xes.elementOpen(CONF_ELEMENT,
                        new AttrValuePair[]{
                            new AttrValuePair(NAME_ATTR, "" + makeConfiguration.getName()), // NOI18N
                            new AttrValuePair(TYPE_ATTR, "" + makeConfiguration.getConfigurationType().getValue()), // NOI18N
                            new AttrValuePair(CUSTOMIZERID_ATTR, "" + makeConfiguration.getCustomizerId()), // NOI18N
                        });
            } else {
                xes.elementOpen(CONF_ELEMENT,
                        new AttrValuePair[]{
                            new AttrValuePair(NAME_ATTR, "" + makeConfiguration.getName()), // NOI18N
                            new AttrValuePair(TYPE_ATTR, "" + makeConfiguration.getConfigurationType().getValue()), // NOI18N
                        });

            }

            writeToolsSetBlock(xes, makeConfiguration);
            writeCompileConfBlock(xes, makeConfiguration);
            if (publicLocation) {
                if (makeConfiguration.isQmakeConfiguration()) {
                    writeQmakeConfiguration(xes, makeConfiguration.getQmakeConfiguration());
                }
                if (makeConfiguration.isMakefileConfiguration()) {
                    writeCodeAssistanceConfiguration(xes, makeConfiguration.getCodeAssistanceConfiguration());
                    writeMakefileProjectConfBlock(xes, makeConfiguration);
                } else {
                    writeCompiledProjectConfBlock(xes, makeConfiguration);
                }
                writePackaging(xes, makeConfiguration.getPackagingConfiguration());
                ConfigurationAuxObject[] profileAuxObjects = confs.getConf(i).getAuxObjects();
                for (int j = 0; j < profileAuxObjects.length; j++) {
                    ConfigurationAuxObject auxObject = profileAuxObjects[j];
                    if (publicallyVisible(auxObject)) {
                        XMLEncoder encoder = auxObject.getXMLEncoder();
                        encoder.encode(xes);
                    }
                }
            } else {
                ConfigurationAuxObject[] profileAuxObjects = confs.getConf(i).getAuxObjects();
                for (int j = 0; j < profileAuxObjects.length; j++) {
                    ConfigurationAuxObject auxObject = profileAuxObjects[j];
                    if (!auxObject.shared()) {
                        XMLEncoder encoder = auxObject.getXMLEncoder();
                        encoder.encode(xes);
                    }
                }
            }
            xes.elementClose(CONF_ELEMENT);
        }

        xes.elementClose(CONFS_ELEMENT);
    }

    protected abstract void writeToolsSetBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration);

    protected abstract void writeCompileConfBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration);

    private void writeCompiledProjectConfBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration) {
        xes.elementOpen(COMPILE_TYPE_ELEMENT);
            writeCCompilerConfiguration(xes, makeConfiguration.getCCompilerConfiguration(), PROJECT_LEVEL);
            writeCCCompilerConfiguration(xes, makeConfiguration.getCCCompilerConfiguration(), PROJECT_LEVEL);
            writeFortranCompilerConfiguration(xes, makeConfiguration.getFortranCompilerConfiguration());
            writeAsmCompilerConfiguration(xes, makeConfiguration.getAssemblerConfiguration());
        switch (makeConfiguration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_APPLICATION:
            case MakeConfiguration.TYPE_DB_APPLICATION:
            case MakeConfiguration.TYPE_DYNAMIC_LIB:
            case MakeConfiguration.TYPE_QT_APPLICATION:
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
            case MakeConfiguration.TYPE_CUSTOM: // <=== FIXUP
                writeLinkerConfiguration(xes, makeConfiguration.getLinkerConfiguration());
                break;
            case MakeConfiguration.TYPE_STATIC_LIB:
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                writeArchiverConfiguration(xes, makeConfiguration.getArchiverConfiguration());
                break;
        }
        writeRequiredProjects(xes, makeConfiguration.getRequiredProjectsConfiguration());
        xes.elementClose(COMPILE_TYPE_ELEMENT);
    }

    private void writeQmakeConfiguration(XMLEncoderStream xes, QmakeConfiguration qmake) {
        xes.elementOpen(QT_ELEMENT);
        if (qmake.getDestdir().getModified()) {
            xes.element(QT_DESTDIR_ELEMENT, qmake.getDestdirValue());
        }
        if (qmake.getTarget().getModified()) {
            xes.element(QT_TARGET_ELEMENT, qmake.getTargetValue());
        }
        if (qmake.getVersion().getModified()) {
            xes.element(QT_VERSION_ELEMENT, qmake.getVersion().getValue());
        }
        if (qmake.getBuildMode().getModified()) {
            xes.element(QT_BUILD_MODE_ELEMENT, String.valueOf(qmake.getBuildMode().getValue()));
        }
        xes.element(QT_MODULES_ELEMENT, qmake.getEnabledModules());
        if (qmake.getMocDir().getModified()) {
            xes.element(QT_MOC_DIR_ELEMENT, qmake.getMocDir().getValue());
        }
        if (qmake.getRccDir().getModified()) {
            xes.element(QT_RCC_DIR_ELEMENT, qmake.getRccDir().getValue());
        }
        if (qmake.getUiDir().getModified()) {
            xes.element(QT_UI_DIR_ELEMENT, qmake.getUiDir().getValue());
        }
        if (qmake.getQmakeSpec().getModified()) {
            xes.element(QT_QMAKE_SPEC_ELEMENT, qmake.getQmakeSpec().getValue());
        }
        if (qmake.getCustomDefs().getModified()) {
            xes.elementOpen(QT_DEFS_LIST_ELEMENT);
            for (String line : qmake.getCustomDefs().getValue()) {
                xes.element(LIST_ELEMENT, line);
            }
            xes.elementClose(QT_DEFS_LIST_ELEMENT);
        }
        xes.elementClose(QT_ELEMENT);
    }

    private void writeMakefileProjectConfBlock(XMLEncoderStream xes,
            MakeConfiguration makeConfiguration) {
        xes.elementOpen(MAKEFILE_TYPE_ELEMENT);
        xes.elementOpen(MAKETOOL_ELEMENT);
        xes.element(BUILD_COMMAND_WORKING_DIR_ELEMENT, makeConfiguration.getMakefileConfiguration().getBuildCommandWorkingDir().getValue());
        xes.element(BUILD_COMMAND_ELEMENT, makeConfiguration.getMakefileConfiguration().getBuildCommand().getValue());
        xes.element(CLEAN_COMMAND_ELEMENT, makeConfiguration.getMakefileConfiguration().getCleanCommand().getValue());
        xes.element(EXECUTABLE_PATH_ELEMENT, makeConfiguration.getMakefileConfiguration().getOutput().getValue());
        writeCCompilerConfiguration(xes, makeConfiguration.getCCompilerConfiguration(), PROJECT_LEVEL);
        writeCCCompilerConfiguration(xes, makeConfiguration.getCCCompilerConfiguration(), PROJECT_LEVEL);
        writeFortranCompilerConfiguration(xes, makeConfiguration.getFortranCompilerConfiguration());
        writeAsmCompilerConfiguration(xes, makeConfiguration.getAssemblerConfiguration());
        //IZ#110443:Adding "Dependencies" node for makefile projects property is premature
        //if (makeConfiguration.getLinkerConfiguration() != null)
        //    writeLinkerConfiguration(xes, makeConfiguration.getLinkerConfiguration());
        xes.elementClose(MAKETOOL_ELEMENT);
        writeRequiredProjects(xes, makeConfiguration.getRequiredProjectsConfiguration());
        xes.elementClose(MAKEFILE_TYPE_ELEMENT);
    }


    private void writePrivatePhysicalFoldersForUnmanagedProject(XMLEncoderStream xes) {
        Folder root = ((MakeConfigurationDescriptor) projectDescriptor).getLogicalFolders();
        boolean unmanaged = false;
        for (Folder folder : root.getFoldersAsArray()) {
            if (folder.isDiskFolder()) {
                unmanaged = true;
                break;
            }
        }
        if (!unmanaged) {
            return;
        }
        List<AttrValuePair> attrList = new ArrayList<AttrValuePair>();
        attrList.add(new AttrValuePair(NAME_ATTR, "" + root.getName())); // NOI18N
        attrList.add(new AttrValuePair(DISPLAY_NAME_ATTR, "" + root.getDisplayName())); // NOI18N
        attrList.add(new AttrValuePair(PROJECT_FILES_ATTR, "" + root.isProjectFiles())); // NOI18N
        if (root.getKind() == Kind.ROOT) {
            attrList.add(new AttrValuePair(KIND_ATTR, "" + root.getKind())); // NOI18N
        }
        if (root.getRoot() != null) {
            attrList.add(new AttrValuePair(ROOT_ATTR, "" + root.getRoot())); // NOI18N
        }
        xes.elementOpen(LOGICAL_FOLDER_ELEMENT, attrList.toArray(new AttrValuePair[attrList.size()]));
        // write out subfolders
        Folder[] subfolders = root.getFoldersAsArray();
        for (int i = 0; i < subfolders.length; i++) {
            if (subfolders[i].isDiskFolder()) {
                writeDiskFolder(xes, subfolders[i], true);
            }
        }
        // write out items
        // we always write all items for private
        Item[] items = root.getItemsAsArray();
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            xes.element(ITEM_PATH_ELEMENT, item.getPath());
        }
        xes.elementClose(LOGICAL_FOLDER_ELEMENT);
    }
    
    private void writeLogicalFolders(XMLEncoderStream xes) {
        writeLogicalFolder(xes, ((MakeConfigurationDescriptor) projectDescriptor).getLogicalFolders(), 0);
    }
    
    private void writeLogicalFolder(XMLEncoderStream xes, Folder folder, final int level) {
        Kind kind = folder.getKind();
        Kind storedKind = null;
        if (kind != null) {
            switch (kind) {
                case ROOT:
                case TEST:
                case IMPORTANT_FILES_FOLDER:
                case TEST_LOGICAL_FOLDER:
                    storedKind = kind;
            }
        }
        List<AttrValuePair> attrList = new ArrayList<AttrValuePair>();
        attrList.add(new AttrValuePair(NAME_ATTR, "" + folder.getName())); // NOI18N
        attrList.add(new AttrValuePair(DISPLAY_NAME_ATTR, "" + folder.getDisplayName())); // NOI18N
        attrList.add(new AttrValuePair(PROJECT_FILES_ATTR, "" + folder.isProjectFiles())); // NOI18N
        if (storedKind != null) {
            attrList.add(new AttrValuePair(KIND_ATTR, "" + folder.getKind())); // NOI18N
        }
        if (folder.getRoot() != null) {
            attrList.add(new AttrValuePair(ROOT_ATTR, "" + folder.getRoot())); // NOI18N
        }
        xes.elementOpen(LOGICAL_FOLDER_ELEMENT, attrList.toArray(new AttrValuePair[attrList.size()]));
        // write out subfolders
        Folder[] subfolders = folder.getFoldersAsArray();
        for (int i = 0; i < subfolders.length; i++) {
            if (subfolders[i].isDiskFolder()) {
                writeDiskFolder(xes, subfolders[i], false);
            } else {
                writeLogicalFolder(xes, subfolders[i], level + 1);
            }
        }
        // write out items
        // we always write all items of logical folder
        Item[] items = folder.getItemsAsArray();
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            xes.element(ITEM_PATH_ELEMENT, item.getPath());
        }
        xes.elementClose(LOGICAL_FOLDER_ELEMENT);
    }

    private void writeDiskFolder(XMLEncoderStream xes, Folder folder, boolean privateLocation) {
        if (!privateLocation && !folder.hasAttributedItems()) {
            return;
        }
        List<AttrValuePair> attrList = new ArrayList<AttrValuePair>();
        if (folder.getRoot() != null) {
            attrList.add(new AttrValuePair(ROOT_ATTR, "" + folder.getRoot())); // NOI18N
        }
        attrList.add(new AttrValuePair(NAME_ATTR, "" + folder.getName())); // NOI18N
        xes.elementOpen(DISK_FOLDER_ELEMENT, attrList.toArray(new AttrValuePair[attrList.size()]));
        // write out subfolders
        Folder[] subfolders = folder.getFoldersAsArray();
        for (int i = 0; i < subfolders.length; i++) {
            writeDiskFolder(xes, subfolders[i], privateLocation);
        }
        // write out items
        Item[] items = folder.getItemsAsArray();
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            if (privateLocation || item.hasImportantAttributes()) {
                xes.element(ITEM_NAME_ELEMENT, item.getName());
            }
        }
        xes.elementClose(DISK_FOLDER_ELEMENT);
    }

    private void writeSourceRoots(XMLEncoderStream xes) {
        MakeConfigurationDescriptor makeProjectDescriptor = (MakeConfigurationDescriptor) projectDescriptor;
        // Filter
        if (!makeProjectDescriptor.getFolderVisibilityQuery().getRegEx().equals(MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN)) {
            xes.element(SOURCE_FOLDERS_FILTER_ELEMENT, makeProjectDescriptor.getFolderVisibilityQuery().getRegEx());
        }

        List<String> list = makeProjectDescriptor.getSourceRoots();
        if (list.size() > 0) {
            // Source Root
            xes.elementOpen(SOURCE_ROOT_LIST_ELEMENT);
            for (String l : list) {
                xes.element(LIST_ELEMENT, l);
            }
            xes.elementClose(SOURCE_ROOT_LIST_ELEMENT);
        }

        list = makeProjectDescriptor.getTestRoots();
        if (list.size() > 0) {
            // Test Root
            xes.elementOpen(TEST_ROOT_LIST_ELEMENT);
            for (String l : list) {
                xes.element(LIST_ELEMENT, l);
            }
            xes.elementClose(TEST_ROOT_LIST_ELEMENT);
        }
    }

//    private void writeSourceEncoding(XMLEncoderStream xes) {
//        xes.element(SOURCE_ENCODING_ELEMENT, ((MakeConfigurationDescriptor)projectDescriptor).getSourceEncoding());
//    }
    public static void writeCCompilerConfiguration(XMLEncoderStream xes, CCompilerConfiguration cCompilerConfiguration, int kind) {
        if (!cCompilerConfiguration.getModified()) {
            return;
        }
        xes.elementOpen(CCOMPILERTOOL_ELEMENT2);
        if (cCompilerConfiguration.getDevelopmentMode().getModified()) {
            xes.element(DEVELOPMENT_MODE_ELEMENT, "" + cCompilerConfiguration.getDevelopmentMode().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getStrip().getModified()) {
            xes.element(STRIP_SYMBOLS_ELEMENT, "" + cCompilerConfiguration.getStrip().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getSixtyfourBits().getModified()) {
            xes.element(ARCHITECTURE_ELEMENT, "" + cCompilerConfiguration.getSixtyfourBits().getValue()); // NOI18N
        }
        if (kind != ITEM_LEVEL) {
            if (cCompilerConfiguration.getCStandard().getModified()) {
                xes.element(STANDARD_ELEMENT, "" + cCompilerConfiguration.getCStandardExternal()); // NOI18N
            }
        }
        if (cCompilerConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, "" + cCompilerConfiguration.getTool().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getIncludeDirectories().getModified()) {
            writeDirectoriesWithConversion(xes, INCLUDE_DIRECTORIES_ELEMENT2, cCompilerConfiguration.getIncludeDirectories().getValue(), getIncludeConverter(cCompilerConfiguration.getOwner()));
        }
        if (cCompilerConfiguration.getStandardsEvolution().getModified()) {
            xes.element(STANDARDS_EVOLUTION_ELEMENT, "" + cCompilerConfiguration.getStandardsEvolution().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getLanguageExt().getModified()) {
            xes.element(LANGUAGE_EXTENSION_ELEMENT, "" + cCompilerConfiguration.getLanguageExt().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getInheritIncludes().getModified()) {
            xes.element(INHERIT_INC_VALUES_ELEMENT, "" + cCompilerConfiguration.getInheritIncludes().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + cCompilerConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getPreprocessorConfiguration().getModified()) {
            List<String> sortedList = new ArrayList<String>(cCompilerConfiguration.getPreprocessorConfiguration().getValue());
            writeSortedListWithConversion(xes, PREPROCESSOR_LIST_ELEMENT, sortedList, getMacroConverter(cCompilerConfiguration.getOwner()));
        }
        if (cCompilerConfiguration.getInheritPreprocessor().getModified()) {
            xes.element(INHERIT_PRE_VALUES_ELEMENT, "" + cCompilerConfiguration.getInheritPreprocessor().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getUndefinedPreprocessorConfiguration().getModified()) {
            List<String> sortedList = new ArrayList<String>(cCompilerConfiguration.getUndefinedPreprocessorConfiguration().getValue());
            Collections.sort(sortedList);
            writeList(xes, UNDEFS_LIST_ELEMENT, sortedList);
        }
        if (cCompilerConfiguration.getInheritUndefinedPreprocessor().getModified()) {
            xes.element(INHERIT_UNDEF_VALUES_ELEMENT, "" + cCompilerConfiguration.getInheritUndefinedPreprocessor().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getUseLinkerLibraries().getModified()) {
            xes.element(USE_LINKER_PKG_CONFIG_LIBRARIES, "" + cCompilerConfiguration.getUseLinkerLibraries().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getWarningLevel().getModified()) {
            xes.element(WARNING_LEVEL_ELEMENT, "" + cCompilerConfiguration.getWarningLevel().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getMTLevel().getModified()) {
            xes.element(MT_LEVEL_ELEMENT, "" + cCompilerConfiguration.getMTLevel().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getAdditionalDependencies().getModified()) {
            xes.element(ADDITIONAL_DEP_ELEMENT, "" + cCompilerConfiguration.getAdditionalDependencies().getValue()); // NOI18N
        }
        xes.elementClose(CCOMPILERTOOL_ELEMENT2);
    }

    public static void writeCCCompilerConfiguration(XMLEncoderStream xes, CCCompilerConfiguration ccCompilerConfiguration, int kind) {
        if (!ccCompilerConfiguration.getModified()) {
            return;
        }
        xes.elementOpen(CCCOMPILERTOOL_ELEMENT2);
        if (ccCompilerConfiguration.getDevelopmentMode().getModified()) {
            xes.element(DEVELOPMENT_MODE_ELEMENT, "" + ccCompilerConfiguration.getDevelopmentMode().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getStrip().getModified()) {
            xes.element(STRIP_SYMBOLS_ELEMENT, "" + ccCompilerConfiguration.getStrip().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getSixtyfourBits().getModified()) {
            xes.element(ARCHITECTURE_ELEMENT, "" + ccCompilerConfiguration.getSixtyfourBits().getValue()); // NOI18N
        }
        if (kind != ITEM_LEVEL) {
            if (ccCompilerConfiguration.getCppStandard().getModified()) {
                xes.element(STANDARD_ELEMENT, "" + ccCompilerConfiguration.getCppStandardExternal()); // NOI18N
            }
        }
        if (ccCompilerConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, "" + ccCompilerConfiguration.getTool().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getIncludeDirectories().getModified()) {
            writeDirectoriesWithConversion(xes, INCLUDE_DIRECTORIES_ELEMENT2, ccCompilerConfiguration.getIncludeDirectories().getValue(), getIncludeConverter(ccCompilerConfiguration.getOwner())); // NOI18N
        }
        if (ccCompilerConfiguration.getStandardsEvolution().getModified()) {
            xes.element(STANDARDS_EVOLUTION_ELEMENT, "" + ccCompilerConfiguration.getStandardsEvolution().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getLanguageExt().getModified()) {
            xes.element(LANGUAGE_EXTENSION_ELEMENT, "" + ccCompilerConfiguration.getLanguageExt().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getInheritIncludes().getModified()) {
            xes.element(INHERIT_INC_VALUES_ELEMENT, "" + ccCompilerConfiguration.getInheritIncludes().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + ccCompilerConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getPreprocessorConfiguration().getModified()) {
            List<String> sortedList = new ArrayList<String>(ccCompilerConfiguration.getPreprocessorConfiguration().getValue());
            writeSortedListWithConversion(xes, PREPROCESSOR_LIST_ELEMENT, sortedList, getMacroConverter(ccCompilerConfiguration.getOwner()));
        }
        if (ccCompilerConfiguration.getInheritPreprocessor().getModified()) {
            xes.element(INHERIT_PRE_VALUES_ELEMENT, "" + ccCompilerConfiguration.getInheritPreprocessor().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getUndefinedPreprocessorConfiguration().getModified()) {
            List<String> sortedList = new ArrayList<String>(ccCompilerConfiguration.getUndefinedPreprocessorConfiguration().getValue());
            Collections.sort(sortedList);
            writeList(xes, UNDEFS_LIST_ELEMENT, sortedList);
        }
        if (ccCompilerConfiguration.getInheritUndefinedPreprocessor().getModified()) {
            xes.element(INHERIT_UNDEF_VALUES_ELEMENT, "" + ccCompilerConfiguration.getInheritUndefinedPreprocessor().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getUseLinkerLibraries().getModified()) {
            xes.element(USE_LINKER_PKG_CONFIG_LIBRARIES, "" + ccCompilerConfiguration.getUseLinkerLibraries().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getWarningLevel().getModified()) {
            xes.element(WARNING_LEVEL_ELEMENT, "" + ccCompilerConfiguration.getWarningLevel().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getMTLevel().getModified()) {
            xes.element(MT_LEVEL_ELEMENT, "" + ccCompilerConfiguration.getMTLevel().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getLibraryLevel().getModified()) {
            xes.element(LIBRARY_LEVEL_ELEMENT, "" + ccCompilerConfiguration.getLibraryLevel().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getAdditionalDependencies().getModified()) {
            xes.element(ADDITIONAL_DEP_ELEMENT, "" + ccCompilerConfiguration.getAdditionalDependencies().getValue()); // NOI18N
        }
        xes.elementClose(CCCOMPILERTOOL_ELEMENT2);
    }

    public static void writeFortranCompilerConfiguration(XMLEncoderStream xes, FortranCompilerConfiguration fortranCompilerConfiguration) {
        if (!fortranCompilerConfiguration.getModified()) {
            return;
        }
        xes.elementOpen(FORTRANCOMPILERTOOL_ELEMENT);
        if (fortranCompilerConfiguration.getDevelopmentMode().getModified()) {
            xes.element(DEVELOPMENT_MODE_ELEMENT, "" + fortranCompilerConfiguration.getDevelopmentMode().getValue()); // NOI18N
        }
        if (fortranCompilerConfiguration.getStrip().getModified()) {
            xes.element(STRIP_SYMBOLS_ELEMENT, "" + fortranCompilerConfiguration.getStrip().getValue()); // NOI18N
        }
        if (fortranCompilerConfiguration.getSixtyfourBits().getModified()) {
            xes.element(ARCHITECTURE_ELEMENT, "" + fortranCompilerConfiguration.getSixtyfourBits().getValue()); // NOI18N
        }
        if (fortranCompilerConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, "" + fortranCompilerConfiguration.getTool().getValue()); // NOI18N
        }
        if (fortranCompilerConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + fortranCompilerConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
        }
        if (fortranCompilerConfiguration.getWarningLevel().getModified()) {
            xes.element(WARNING_LEVEL_ELEMENT, "" + fortranCompilerConfiguration.getWarningLevel().getValue()); // NOI18N
        }
        if (fortranCompilerConfiguration.getAdditionalDependencies().getModified()) {
            xes.element(ADDITIONAL_DEP_ELEMENT, "" + fortranCompilerConfiguration.getAdditionalDependencies().getValue()); // NOI18N
        }
        xes.elementClose(FORTRANCOMPILERTOOL_ELEMENT);
    }

    public static void writeAsmCompilerConfiguration(XMLEncoderStream xes, AssemblerConfiguration asmCompilerConfiguration) {
        if (!asmCompilerConfiguration.getModified()) {
            return;
        }
        xes.elementOpen(ASMTOOL_ELEMENT);
        if (asmCompilerConfiguration.getDevelopmentMode().getModified()) {
            xes.element(DEVELOPMENT_MODE_ELEMENT, "" + asmCompilerConfiguration.getDevelopmentMode().getValue()); // NOI18N
        }
        if (asmCompilerConfiguration.getSixtyfourBits().getModified()) {
            xes.element(ARCHITECTURE_ELEMENT, "" + asmCompilerConfiguration.getSixtyfourBits().getValue()); // NOI18N
        }
        if (asmCompilerConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, "" + asmCompilerConfiguration.getTool().getValue()); // NOI18N
        }
        if (asmCompilerConfiguration.getWarningLevel().getModified()) {
            xes.element(WARNING_LEVEL_ELEMENT, "" + asmCompilerConfiguration.getWarningLevel().getValue()); // NOI18N
        }
        if (asmCompilerConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + asmCompilerConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
        }
        xes.elementClose(ASMTOOL_ELEMENT);
    }

    public static void writeCustomToolConfiguration(XMLEncoderStream xes, CustomToolConfiguration customToolConfiguration) {
        if (!customToolConfiguration.getModified()) {
            return;
        }
        xes.elementOpen(CUSTOMTOOL_ELEMENT);
        if (customToolConfiguration.getCommandLine().getModified()) {
            xes.element(CUSTOMTOOL_COMMANDLINE_ELEMENT, "" + customToolConfiguration.getCommandLine().getValue()); // NOI18N
        }
        if (customToolConfiguration.getDescription().getModified()) {
            xes.element(CUSTOMTOOL_DESCRIPTION_ELEMENT, "" + customToolConfiguration.getDescription().getValue()); // NOI18N
        }
        if (customToolConfiguration.getOutputs().getModified()) {
            xes.element(CUSTOMTOOL_OUTPUTS_ELEMENT, "" + customToolConfiguration.getOutputs().getValue()); // NOI18N
        }
        if (customToolConfiguration.getAdditionalDependencies().getModified()) {
            xes.element(CUSTOMTOOL_ADDITIONAL_DEP_ELEMENT, "" + customToolConfiguration.getAdditionalDependencies().getValue()); // NOI18N
        }
        xes.elementClose(CUSTOMTOOL_ELEMENT);
    }

    public static void writeLinkerConfiguration(XMLEncoderStream xes, LinkerConfiguration linkerConfiguration) {
        if (!linkerConfiguration.getModified()) {
            return;
        }
        xes.elementOpen(LINKERTOOL_ELEMENT);
        if (linkerConfiguration.getOutput().getModified()) {
            xes.element(OUTPUT_ELEMENT, linkerConfiguration.getOutput().getValue());
        }
        if (linkerConfiguration.getAdditionalLibs().getModified()) {
            writeDirectories(xes, LINKER_ADD_LIB_ELEMENT, linkerConfiguration.getAdditionalLibs().getValue());
        }
        if (linkerConfiguration.getDynamicSearch().getModified()) {
            writeDirectories(xes, LINKER_DYN_SERCH_ELEMENT, linkerConfiguration.getDynamicSearch().getValue());
        }
        if (linkerConfiguration.getStripOption().getModified()) {
            xes.element(STRIP_SYMBOLS_ELEMENT, "" + linkerConfiguration.getStripOption().getValue()); // NOI18N
        }
        if (linkerConfiguration.getPICOption().getModified()) {
            xes.element(LINKER_KPIC_ELEMENT, "" + linkerConfiguration.getPICOption().getValue()); // NOI18N
        }
        if (linkerConfiguration.getNorunpathOption().getModified()) {
            xes.element(LINKER_NORUNPATH_ELEMENT, "" + linkerConfiguration.getNorunpathOption().getValue()); // NOI18N
        }
        if (linkerConfiguration.getNameassignOption().getModified()) {
            xes.element(LINKER_ASSIGN_ELEMENT, "" + linkerConfiguration.getNameassignOption().getValue()); // NOI18N
        }
        if (linkerConfiguration.getAdditionalDependencies().getModified()) {
            xes.element(ADDITIONAL_DEP_ELEMENT, "" + linkerConfiguration.getAdditionalDependencies().getValue()); // NOI18N
        }
        if (linkerConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, linkerConfiguration.getTool().getValue());
        }
        if (linkerConfiguration.getLibrariesConfiguration().getModified()) {
            writeLibrariesConfiguration(xes, linkerConfiguration.getLibrariesConfiguration());
        }
        if (linkerConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + linkerConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
            //xes.element(DEBUGGING_ELEMENT, "" + linkerConfiguration.getTool().getValue() + " " + linkerConfiguration.getOptions()); // NOI18N
        }
        xes.elementClose(LINKERTOOL_ELEMENT);
    }

    public static void writeLibrariesConfiguration(XMLEncoderStream xes, LibrariesConfiguration librariesConfiguration) {
        xes.elementOpen(LINKER_LIB_ITEMS_ELEMENT);
        List<LibraryItem> libraryItems = librariesConfiguration.getValue();
        for (LibraryItem item : libraryItems) {
            if (item instanceof LibraryItem.ProjectItem) {
                xes.elementOpen(LINKER_LIB_PROJECT_ITEM_ELEMENT);
                writeMakeArtifact(xes, ((LibraryItem.ProjectItem) item).getMakeArtifact());
                xes.elementClose(LINKER_LIB_PROJECT_ITEM_ELEMENT);
            } else if (item instanceof LibraryItem.StdLibItem) {
                xes.element(LINKER_LIB_STDLIB_ITEM_ELEMENT, ((LibraryItem.StdLibItem) item).getName());
            } else if (item instanceof LibraryItem.LibItem) {
                xes.element(LINKER_LIB_LIB_ITEM_ELEMENT, ((LibraryItem.LibItem) item).getLibName());
            } else if (item instanceof LibraryItem.LibFileItem) {
                xes.element(LINKER_LIB_FILE_ITEM_ELEMENT, ((LibraryItem.LibFileItem) item).getPath());
            } else if (item instanceof LibraryItem.OptionItem) {
                xes.element(LINKER_LIB_OPTION_ITEM_ELEMENT, ((LibraryItem.OptionItem) item).getLibraryOption());
            }
        }
        xes.elementClose(LINKER_LIB_ITEMS_ELEMENT);
    }

    public static void writeRequiredProjects(XMLEncoderStream xes, RequiredProjectsConfiguration requiredProjectsConfiguration) {
        List<LibraryItem.ProjectItem> projectItems = requiredProjectsConfiguration.getValue();
        if (!projectItems.isEmpty()) {
            xes.elementOpen(REQUIRED_PROJECTS_ELEMENT);
            for (LibraryItem.ProjectItem item : projectItems) {
                writeMakeArtifact(xes, item.getMakeArtifact());
            }
            xes.elementClose(REQUIRED_PROJECTS_ELEMENT);
        }
    }

    private static void writePackaging(XMLEncoderStream xes, PackagingConfiguration packagingConfiguration) {
        if (!packagingConfiguration.isModified()) {
            return;
        }
        xes.elementOpen(PACK_ELEMENT);
        xes.element(PACK_TYPE_ELEMENT, "" + packagingConfiguration.getName()); // NOI18N
        if (packagingConfiguration.getVerbose().getModified()) {
            xes.element(VERBOSE_ELEMENT, "" + packagingConfiguration.getVerbose().getValue()); // NOI18N
        }
        if (packagingConfiguration.getOutput().getModified()) {
            xes.element(OUTPUT_ELEMENT, packagingConfiguration.getOutput().getValue());
        }
        if (packagingConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, packagingConfiguration.getTool().getValue());
        }
        if (packagingConfiguration.getOptions().getModified()) {
            xes.element(ADDITIONAL_OPTIONS_ELEMENT, packagingConfiguration.getOptions().getValue());
        }
        if (packagingConfiguration.getTopDir().getModified()) {
            xes.element(PACK_TOPDIR_ELEMENT, packagingConfiguration.getTopDir().getValue());
        }
        xes.elementOpen(PACK_FILES_LIST_ELEMENT);
        List<PackagerFileElement> filesList = packagingConfiguration.getFiles().getValue();
        for (PackagerFileElement elem : filesList) {
            xes.element(PACK_FILE_LIST_ELEMENT,
                    new AttrValuePair[]{
                        new AttrValuePair(TYPE_ATTR, "" + elem.getType().toString()), // NOI18N
                        new AttrValuePair(TO_ATTR, "" + elem.getTo()), // NOI18N
                        new AttrValuePair(FROM_ATTR, "" + elem.getFrom()), // NOI18N
                        new AttrValuePair(PERM_ATTR, "" + elem.getPermission()), // NOI18N
                        new AttrValuePair(OWNER_ATTR, "" + elem.getOwner()), // NOI18N
                        new AttrValuePair(GROUP_ATTR, "" + elem.getGroup()), // NOI18N
                    });
        }
        xes.elementClose(PACK_FILES_LIST_ELEMENT);
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(packagingConfiguration.getType().getValue());
        if (packager.hasInfoList()) {
            xes.elementOpen(PACK_INFOS_LIST_ELEMENT);
            List<PackagerInfoElement> infoList = packagingConfiguration.getHeaderSubList(packagingConfiguration.getType().getValue());
            for (PackagerInfoElement elem : infoList) {
                xes.element(PACK_INFO_LIST_ELEMENT,
                        new AttrValuePair[]{
                            new AttrValuePair(NAME_ATTR, "" + elem.getName()), // NOI18N
                            new AttrValuePair(VALUE_ATTR, "" + elem.getValue()), // NOI18N
                            new AttrValuePair(MANDATORY_ATTR, "" + elem.isMandatory()), // NOI18N
                        });
            }
            xes.elementClose(PACK_INFOS_LIST_ELEMENT);
            if (!packagingConfiguration.getAdditionalInfo().getValue().isEmpty()) {
                writeList(xes, PACK_ADDITIONAL_INFOS_LIST_ELEMENT, packagingConfiguration.getAdditionalInfo().getValue());
            }
        }
        xes.elementClose(PACK_ELEMENT);
    }

    public static void writeMakeArtifact(XMLEncoderStream xes, MakeArtifact makeArtifact) {
        xes.elementOpen(MAKE_ARTIFACT_ELEMENT,
                new AttrValuePair[]{
                    new AttrValuePair(MAKE_ARTIFACT_PL_ELEMENT, makeArtifact.getProjectLocation()),
                    new AttrValuePair(MAKE_ARTIFACT_CT_ELEMENT, "" + makeArtifact.getConfigurationType()), // NOI18N
                    new AttrValuePair(MAKE_ARTIFACT_CN_ELEMENT, makeArtifact.getConfigurationName()),
                    new AttrValuePair(MAKE_ARTIFACT_AC_ELEMENT, "" + makeArtifact.getActive()), // NOI18N
                    new AttrValuePair(MAKE_ARTIFACT_BL_ELEMENT, "" + makeArtifact.getBuild()), // NOI18N
                    new AttrValuePair(MAKE_ARTIFACT_WD_ELEMENT, makeArtifact.getWorkingDirectory()),
                    new AttrValuePair(MAKE_ARTIFACT_BC_ELEMENT, makeArtifact.getBuildCommand()),
                    new AttrValuePair(MAKE_ARTIFACT_CC_ELEMENT, makeArtifact.getCleanCommand()),
                    new AttrValuePair(MAKE_ARTIFACT_OP_ELEMENT, makeArtifact.getStoredOutput()),});
        /*
        xes.elementOpen(MAKE_ARTIFACT_ELEMENT);
        xes.element(MAKE_ARTIFACT_PL_ELEMENT, makeArtifact.getProjectLocation());
        xes.element(MAKE_ARTIFACT_CT_ELEMENT, "" + makeArtifact.getConfigurationType()); // NOI18N
        xes.element(MAKE_ARTIFACT_CN_ELEMENT, makeArtifact.getConfigurationName());
        xes.element(MAKE_ARTIFACT_AC_ELEMENT, "" + makeArtifact.getActive()); // NOI18N
        xes.element(MAKE_ARTIFACT_WD_ELEMENT, makeArtifact.getWorkingDirectory());
        xes.element(MAKE_ARTIFACT_BC_ELEMENT, makeArtifact.getBuildCommand());
        xes.element(MAKE_ARTIFACT_CC_ELEMENT, makeArtifact.getCleanCommand());
        xes.element(MAKE_ARTIFACT_CC_ELEMENT, makeArtifact.getOutput());
         */
        xes.elementClose(MAKE_ARTIFACT_ELEMENT);
    }

    public static void writeArchiverConfiguration(XMLEncoderStream xes, ArchiverConfiguration archiverConfiguration) {
        xes.elementOpen(ARCHIVERTOOL_ELEMENT);
        if (archiverConfiguration.getOutput().getModified()) {
            xes.element(OUTPUT_ELEMENT, archiverConfiguration.getOutput().getValue());
        }
        if (archiverConfiguration.getRunRanlib().getModified()) {
            xes.element(ARCHIVERTOOL_RUN_RANLIB_ELEMENT, "" + archiverConfiguration.getRunRanlib().getValue()); // NOI18N
        }
        if (archiverConfiguration.getVerboseOption().getModified()) {
            xes.element(ARCHIVERTOOL_VERBOSE_ELEMENT, "" + archiverConfiguration.getVerboseOption().getValue()); // NOI18N
        }
        if (archiverConfiguration.getSupressOption().getModified()) {
            xes.element(ARCHIVERTOOL_SUPRESS_ELEMENT, "" + archiverConfiguration.getSupressOption().getValue()); // NOI18N
        }
        if (archiverConfiguration.getAdditionalDependencies().getModified()) {
            xes.element(ADDITIONAL_DEP_ELEMENT, "" + archiverConfiguration.getAdditionalDependencies().getValue()); // NOI18N
        }
        if (archiverConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, "" + archiverConfiguration.getTool().getValue()); // NOI18N
        }
        if (archiverConfiguration.getRanlibTool().getModified()) {
            xes.element(RANLIB_TOOL_ELEMENT, "" + archiverConfiguration.getRanlibTool().getValue()); // NOI18N
        }
        if (archiverConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + archiverConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
        }	//xes.element(DEBUGGING_ELEMENT, "" + archiverConfiguration.getTool().getValue() + " " + archiverConfiguration.getOptions()); // NOI18N
        xes.elementClose(ARCHIVERTOOL_ELEMENT);
    }

    private void writeCodeAssistanceConfiguration(XMLEncoderStream xes, CodeAssistanceConfiguration codeAssistanceConfiguration) {
        xes.elementOpen(CODE_ASSISTANCE_ELEMENT);
        if (codeAssistanceConfiguration.getBuildAnalyzer().getModified()) {
            xes.element(BUILD_ANALAZYER_ELEMENT, "" + codeAssistanceConfiguration.getBuildAnalyzer().getValue()); // NOI18N
        }
        if (codeAssistanceConfiguration.getTools().getModified()) {
            xes.element(BUILD_ANALAZYER_TOOLS_ELEMENT, "" + codeAssistanceConfiguration.getTools().getValue()); // NOI18N
        }
        // evn variables and transient macros
        if (codeAssistanceConfiguration.getEnvironmentVariables().getModified()) {
            List<String> sortedList = new ArrayList<String>(codeAssistanceConfiguration.getEnvironmentVariables().getValue());
            Collections.sort(sortedList);
            writeList(xes, CODE_ASSISTANCE_ENVIRONMENT_ELEMENT, sortedList);
        }
        if (codeAssistanceConfiguration.getTransientMacros().getModified()) {
            List<String> sortedList = new ArrayList<String>(codeAssistanceConfiguration.getTransientMacros().getValue());
            Collections.sort(sortedList);
            writeList(xes, CODE_ASSISTANCE_TRANSIENT_MACROS_ELEMENT, sortedList);
        }
        xes.elementClose(CODE_ASSISTANCE_ELEMENT);
    }
    
    private static void writeSortedListWithConversion(XMLEncoderStream xes, String tag, List<String> list, StringConverter conv) {
        Collections.sort(list);
        writeList(xes, tag, LIST_ELEMENT, list, conv);
    }

    private static void writeDirectoriesWithConversion(XMLEncoderStream xes, String tag, List<String> directories, StringConverter conv) {
        writeList(xes, tag, PATH_ELEMENT, directories, conv);
    }

    private static void writeList(XMLEncoderStream xes, String tag, List<String> list) {
        writeList(xes, tag, LIST_ELEMENT, list, EMPTY_CONVERTER);
    }

    private static void writeDirectories(XMLEncoderStream xes, String tag, List<String> directories) {
        writeList(xes, tag, PATH_ELEMENT, directories, EMPTY_CONVERTER);
    }

    private static void writeList(XMLEncoderStream xes, String tag, String listTag, List<String> list, StringConverter conv) {
        if (list.isEmpty()) {
            return;
        }
        xes.elementOpen(tag);
        for (String entry : list) {
            xes.element(listTag, conv.convert(entry));
        }
        xes.elementClose(tag);
    }

    private boolean publicallyVisible(ConfigurationAuxObject auxObject) {
        if (auxObject instanceof org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration) {
            return ((org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration)auxObject).isVCSVisible();
        } else if (auxObject instanceof org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration) {
            return ((org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration)auxObject).isVCSVisible();
        }
        return auxObject.shared();
    }
    
    private static StringConverter getMacroConverter(MakeConfiguration conf) {
        if (conf != null) {
            final CodeAssistanceConfiguration caConf = conf.getCodeAssistanceConfiguration();
            if (caConf != null && caConf.getTransientMacros().getModified()) {
                return new MacroConverterImpl(caConf);
            }
        }
        return EMPTY_CONVERTER;
    }
    
    private static StringConverter getIncludeConverter(final MakeConfiguration conf) {
        if (conf != null) {
            final CodeAssistanceConfiguration caConf = conf.getCodeAssistanceConfiguration();
            if (caConf != null && caConf.getEnvironmentVariables().getModified()) {
                return new IncludeConverterImpl(conf, caConf);
            }
        }
        return EMPTY_CONVERTER;
    }
    
    private interface StringConverter {
        /**
         * convert original string if needed
         * @param orig string to convert
         * @param shared public or private serialization
         * @return converted string
         */
        public String convert(String orig);
    }
    
    private static final StringConverter EMPTY_CONVERTER = new StringConverter() {

        @Override
        public String convert(String orig) {
            return orig;
        }
    };    

    private static final class IncludeConverterImpl implements StringConverter {

        private final Map<String, String> replacements = new HashMap<String, String>();

        public IncludeConverterImpl(MakeConfiguration conf, CodeAssistanceConfiguration caConf) {
            Map<String, String> environment = Collections.emptyMap();
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(conf.getFileSystemHost());
                environment = hostInfo.getEnvironment();
                Map<String, String> temporaryEnv = BrokenReferencesSupport.getTemporaryEnv(conf.getDevelopmentHost().getExecutionEnvironment());
                if (temporaryEnv != null) {
                    Map<String, String> res = new HashMap<String, String>(temporaryEnv);
                    res.putAll(environment);
                    environment = res;
                }
            } catch (    IOException | ConnectionManager.CancellationException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (!environment.isEmpty()) {
                for (String envVariableName : caConf.getEnvironmentVariables().getValue()) {
                    String toReplace = environment.get(envVariableName);
                    // for now we support abs paths replacements
                    if (toReplace != null && !toReplace.isEmpty() && 
                        CndPathUtilities.isPathAbsolute(toReplace)) { 
                        replacements.put(toReplace, "${" + envVariableName + "}"); // NOI18N
                        toReplace = toReplace.replace('\\', '/');
                        replacements.put(toReplace, "${" + envVariableName + "}"); // NOI18N
                    } else {
                        System.err.println("env Variable " + envVariableName + " with unexpected value: " + toReplace);
                    }
                }
            }
        }
        
        @Override
        public String convert(String orig) {
            String out = orig;
            int len = -1;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String key = entry.getKey();
                if (orig.startsWith(key)) {
                    if (len < key.length()) {
                        len = key.length();
                        out = entry.getValue() + orig.substring(len);
                    }
                }
            }
            return out;
        }
    }

    private static final class MacroConverterImpl implements StringConverter {

        private final Map<String, String> replacements = new HashMap<String, String>();

        public MacroConverterImpl(CodeAssistanceConfiguration caConf) {
            for (String macroWithDefaultValue : caConf.getTransientMacros().getValue()) {
                int idx = macroWithDefaultValue.indexOf('=');
                if (idx < 0) {
                    System.err.println("macro without default value " + macroWithDefaultValue);
                } else {
                    String prefix = macroWithDefaultValue.substring(0, idx);
                    replacements.put(prefix, macroWithDefaultValue);
                }
            }
        }

        @Override
        public String convert(String orig) {
            String out = orig;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                if (orig.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return out;
        }
    }
}
