/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.ArchiverConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.RequiredProjectsConfiguration;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;

/**
 * Common subclass to ConfigurationXMLCodec and AuxConfigurationXMLCodec
 */
/**
 * Change History:
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
 *   new attributs for ITEM_ELEMENT: <item path="../gcc/zlib/examples/gzlog.h" ex="true" tool="1">
 * V56 - NB 6.7
 *   Dont write ITEM_ELEMENT (item configuration) if default values
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
 *   Packaging persistance:
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
 *   Now storing required tools (c, cpp, ...) only if different from default vaue 
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

    public final static int CURRENT_VERSION = 62;

    // Generic
    protected final static String PROJECT_DESCRIPTOR_ELEMENT = "projectDescriptor"; // NOI18N
    protected final static String DEBUGGING_ELEMENT = "justfordebugging"; // NOI18N
    // Old style. FIXUP: should be removed....
    protected final static String CONFIGURATION_DESCRIPTOR_ELEMENT = "configurationDescriptor"; // NOI18N
    protected final static String DEFAULT_CONF_ELEMENT = "defaultConf"; // NOI18N
    protected final static String CONFS_ELEMENT = "confs"; // NOI18N
    protected final static String CONF_ELEMENT = "conf"; // NOI18N
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
    protected final static String SOURCE_FOLDERS_FILTER_ELEMENT = "sourceFolderFilter"; // NOI18N
    protected final static String SOURCE_ENCODING_ELEMENT = "sourceEncoding"; // NOI18N
    // Tools Set (Compiler set and platform)
    protected final static String TOOLS_SET_ELEMENT = "toolsSet"; // NOI18N
    protected final static String DEVELOPMENT_SERVER_ELEMENT = "developmentServer"; // NOI18N
    protected final static String COMPILER_SET_ELEMENT = "compilerSet"; // NOI18N
    protected final static String C_REQUIRED_ELEMENT = "cRequired"; // NOI18N
    protected final static String CPP_REQUIRED_ELEMENT = "cppRequired"; // NOI18N
    protected final static String FORTRAN_REQUIRED_ELEMENT = "fortranRequired"; // NOI18N
    protected final static String ASSEMBLER_REQUIRED_ELEMENT = "assemblerRequired"; // NOI18N
    protected final static String PLATFORM_ELEMENT = "platform"; // NOI18N
    protected final static String DEPENDENCY_CHECKING = "dependencyChecking"; // NOI18N
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
    // Common
    protected final static String COMMANDLINE_TOOL_ELEMENT = "commandlineTool"; // NOI18N
    protected final static String ADDITIONAL_DEP_ELEMENT = "additionalDep"; // NOI18N
    protected final static String ADDITIONAL_OPTIONS_ELEMENT = "additionalOptions"; // NOI18N
    protected final static String OUTPUT_ELEMENT = "output"; // NOI18N
    protected final static String INHERIT_INC_VALUES_ELEMENT = "inheritIncValues"; // NOI18N
    protected final static String INHERIT_PRE_VALUES_ELEMENT = "inheritPreValues"; // NOI18N
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
    protected final static String PREPROCESSOR_ELEMENT = "preprocessor"; // NOI18N
    protected final static String PREPROCESSOR_LIST_ELEMENT = "preprocessorList"; // NOI18N
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
    protected final static String MAKE_ARTIFACT_ELEMENT = "makeArtifact"; // NOI18N
    protected final static String MAKE_ARTIFACT_PL_ELEMENT = "PL"; // NOI18N
    protected final static String MAKE_ARTIFACT_CT_ELEMENT = "CT"; // NOI18N
    protected final static String MAKE_ARTIFACT_CN_ELEMENT = "CN"; // NOI18N
    protected final static String MAKE_ARTIFACT_AC_ELEMENT = "AC"; // NOI18N
    protected final static String MAKE_ARTIFACT_BL_ELEMENT = "BL"; // NOI18N
    protected final static String MAKE_ARTIFACT_WD_ELEMENT = "WD"; // NOI18N
    protected final static String MAKE_ARTIFACT_BC_ELEMENT = "BC"; // NOI18N
    protected final static String MAKE_ARTIFACT_CC_ELEMENT = "CC"; // NOI18N
    protected final static String MAKE_ARTIFACT_OP_ELEMENT = "OP"; // NOI18N
    // Archiver Tool
    protected final static String ARCHIVERTOOL_ELEMENT = "archiverTool"; // NOI18N
    protected final static String ARCHIVERTOOL_RUN_RANLIB_ELEMENT = "runRanlib"; // NOI18N
    protected final static String ARCHIVERTOOL_VERBOSE_ELEMENT = "archiverVerbose"; // NOI18N
    protected final static String ARCHIVERTOOL_SUPRESS_ELEMENT = "archiverSupress"; // NOI18N
    protected final static String VERSION_ATTR = "version"; // NOI18N
    protected final static String TYPE_ATTR = "type"; // NOI18N
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
    private ConfigurationDescriptor projectDescriptor;
    private boolean publicLocation;

    protected CommonConfigurationXMLCodec(ConfigurationDescriptor projectDescriptor,
            boolean publicLocation) {
        this.projectDescriptor = projectDescriptor;
        this.publicLocation = publicLocation;
    }

    // interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
        xes.elementOpen(CONFIGURATION_DESCRIPTOR_ELEMENT, CURRENT_VERSION);
        if (publicLocation) {
            writeLogicalFolders(xes);
            writeSourceRoots(xes);
        //writeSourceEncoding(xes);
        }
        xes.element(PROJECT_MAKEFILE_ELEMENT, ((MakeConfigurationDescriptor) projectDescriptor).getProjectMakefileName());
        if (!publicLocation) {
            xes.element(DEFAULT_CONF_ELEMENT, "" + projectDescriptor.getConfs().getActiveAsIndex()); // NOI18N
        }
        writeConfsBlock(xes);
        xes.elementClose(CONFIGURATION_DESCRIPTOR_ELEMENT);
    }

    private void writeConfsBlock(XMLEncoderStream xes) {
        xes.elementOpen(CONFS_ELEMENT);

        Configurations confs = projectDescriptor.getConfs();
        for (int i = 0; i < confs.size(); i++) {

            MakeConfiguration makeConfiguration =
                    (MakeConfiguration) confs.getConf(i);

            xes.elementOpen(CONF_ELEMENT,
                    new AttrValuePair[]{
                        new AttrValuePair(NAME_ATTR, "" + makeConfiguration.getName()), // NOI18N
                        new AttrValuePair(TYPE_ATTR, "" + makeConfiguration.getConfigurationType().getValue()), // NOI18N
                    });

            if (publicLocation) {
                writeToolsSetBlock(xes, makeConfiguration);
                if (makeConfiguration.isQmakeConfiguration()) {
                    writeQmakeConfiguration(xes, makeConfiguration.getQmakeConfiguration());
                }
                if (makeConfiguration.isMakefileConfiguration()) {
                    writeMakefileProjectConfBlock(xes, makeConfiguration);
                } else {
                    writeCompiledProjectConfBlock(xes, makeConfiguration);
                }
                writePackaging(xes, makeConfiguration.getPackagingConfiguration());
                ConfigurationAuxObject[] profileAuxObjects = confs.getConf(i).getAuxObjects();
                for (int j = 0; j < profileAuxObjects.length; j++) {
                    ConfigurationAuxObject auxObject = profileAuxObjects[j];
                    if (auxObject.shared()) {
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

    private void writeToolsSetBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration) {
        xes.elementOpen(TOOLS_SET_ELEMENT);
        xes.element(DEVELOPMENT_SERVER_ELEMENT, makeConfiguration.getDevelopmentHost().getHostKey());
        xes.element(COMPILER_SET_ELEMENT, "" + makeConfiguration.getCompilerSet().getNameAndFlavor());
        if (makeConfiguration.getCRequired().getValue() != makeConfiguration.getCRequired().getDefault()) {
            xes.element(C_REQUIRED_ELEMENT, "" + makeConfiguration.getCRequired().getValue());
        }
        if (makeConfiguration.getCppRequired().getValue() != makeConfiguration.getCppRequired().getDefault()) {
            xes.element(CPP_REQUIRED_ELEMENT, "" + makeConfiguration.getCppRequired().getValue());
        }
        if (makeConfiguration.getFortranRequired().getValue() != makeConfiguration.getFortranRequired().getDefault()) {
            xes.element(FORTRAN_REQUIRED_ELEMENT, "" + makeConfiguration.getFortranRequired().getValue());
        }
        if (makeConfiguration.getAssemblerRequired().getValue() != makeConfiguration.getAssemblerRequired().getDefault()) {
            xes.element(ASSEMBLER_REQUIRED_ELEMENT, "" + makeConfiguration.getAssemblerRequired().getValue());
        }
        xes.element(PLATFORM_ELEMENT, "" + makeConfiguration.getDevelopmentHost().getBuildPlatform()); // NOI18N
        if (makeConfiguration.getDependencyChecking().getModified()) {
            xes.element(DEPENDENCY_CHECKING, "" + makeConfiguration.getDependencyChecking().getValue()); // NOI18N
        }
        xes.elementClose(TOOLS_SET_ELEMENT);
    }

    private void writeCompiledProjectConfBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration) {
        xes.elementOpen(COMPILE_TYPE_ELEMENT);
        writeCCompilerConfiguration(xes, makeConfiguration.getCCompilerConfiguration());
        writeCCCompilerConfiguration(xes, makeConfiguration.getCCCompilerConfiguration());
        writeFortranCompilerConfiguration(xes, makeConfiguration.getFortranCompilerConfiguration());
        switch (makeConfiguration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_APPLICATION:
            case MakeConfiguration.TYPE_DYNAMIC_LIB:
            case MakeConfiguration.TYPE_QT_APPLICATION:
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
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
        writeCCompilerConfiguration(xes, makeConfiguration.getCCompilerConfiguration());
        writeCCCompilerConfiguration(xes, makeConfiguration.getCCCompilerConfiguration());
        writeFortranCompilerConfiguration(xes, makeConfiguration.getFortranCompilerConfiguration());
        //IZ#110443:Adding "Dependencies" node for makefile projects property is premature
        //if (makeConfiguration.getLinkerConfiguration() != null)
        //    writeLinkerConfiguration(xes, makeConfiguration.getLinkerConfiguration());
        xes.elementClose(MAKETOOL_ELEMENT);
        writeRequiredProjects(xes, makeConfiguration.getRequiredProjectsConfiguration());
        xes.elementClose(MAKEFILE_TYPE_ELEMENT);
    }

    private void writeLogicalFolders(XMLEncoderStream xes) {
        writeLogicalFolder(xes, ((MakeConfigurationDescriptor) projectDescriptor).getLogicalFolders());
    }

    private void writeLogicalFolder(XMLEncoderStream xes, Folder folder) {
        xes.elementOpen(LOGICAL_FOLDER_ELEMENT,
                new AttrValuePair[]{
                    new AttrValuePair(NAME_ATTR, "" + folder.getName()), // NOI18N
                    new AttrValuePair(DISPLAY_NAME_ATTR, "" + folder.getDisplayName()), // NOI18N
                    new AttrValuePair(PROJECT_FILES_ATTR, "" + folder.isProjectFiles()), // NOI18N
                });
        // write out subfolders
        Folder[] subfolders = folder.getFoldersAsArray();
        for (int i = 0; i < subfolders.length; i++) {
            if (subfolders[i].isDiskFolder()) {
                writeDiskFolder(xes, subfolders[i]);
            } else {
                writeLogicalFolder(xes, subfolders[i]);
            }
        }
        // write out items
        Item[] items = folder.getItemsAsArray();
        for (int i = 0; i < items.length; i++) {
            xes.element(ITEM_PATH_ELEMENT, items[i].getPath());
        }
        xes.elementClose(LOGICAL_FOLDER_ELEMENT);
    }

    private void writeDiskFolder(XMLEncoderStream xes, Folder folder) {
        if (folder.getRoot() != null) {
            xes.elementOpen(DISK_FOLDER_ELEMENT,
                    new AttrValuePair[]{
                        new AttrValuePair(NAME_ATTR, "" + folder.getName()), // NOI18N
                        new AttrValuePair(ROOT_ATTR, "" + folder.getRoot()), // NOI18N
                    });
        } else {
            xes.elementOpen(DISK_FOLDER_ELEMENT,
                    new AttrValuePair[]{
                        new AttrValuePair(NAME_ATTR, "" + folder.getName()), // NOI18N
                    });
        }
        // write out subfolders
        Folder[] subfolders = folder.getFoldersAsArray();
        for (int i = 0; i < subfolders.length; i++) {
            writeDiskFolder(xes, subfolders[i]);
        }
        // write out items
        Item[] items = folder.getItemsAsArray();
        for (int i = 0; i < items.length; i++) {
            xes.element(ITEM_NAME_ELEMENT, items[i].getName());
        }
        xes.elementClose(DISK_FOLDER_ELEMENT);
    }

    private void writeSourceRoots(XMLEncoderStream xes) {
        MakeConfigurationDescriptor makeProjectDescriptor = (MakeConfigurationDescriptor) projectDescriptor;
        List<String> list = makeProjectDescriptor.getSourceRoots();
        if (list.size() > 0) {
            // Filter
            if (!makeProjectDescriptor.getFolderVisibilityQuery().getRegEx().equals(MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN)) {
                xes.element(SOURCE_FOLDERS_FILTER_ELEMENT, makeProjectDescriptor.getFolderVisibilityQuery().getRegEx());
            }

            // Source Root
            xes.elementOpen(SOURCE_ROOT_LIST_ELEMENT);
            for (String l : list) {
                xes.element(LIST_ELEMENT, l);
            }
            xes.elementClose(SOURCE_ROOT_LIST_ELEMENT);
        }
    }

//    private void writeSourceEncoding(XMLEncoderStream xes) {
//        xes.element(SOURCE_ENCODING_ELEMENT, ((MakeConfigurationDescriptor)projectDescriptor).getSourceEncoding());
//    }
    public static void writeCCompilerConfiguration(XMLEncoderStream xes, CCompilerConfiguration cCompilerConfiguration) {
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
        if (cCompilerConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, "" + cCompilerConfiguration.getTool().getValue()); // NOI18N
        }
        if (cCompilerConfiguration.getIncludeDirectories().getModified()) {
            writeDirectories(xes, INCLUDE_DIRECTORIES_ELEMENT2, cCompilerConfiguration.getIncludeDirectories().getValue());
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
            writeList(xes, PREPROCESSOR_LIST_ELEMENT, cCompilerConfiguration.getPreprocessorConfiguration().getValue());
        }
        if (cCompilerConfiguration.getInheritPreprocessor().getModified()) {
            xes.element(INHERIT_PRE_VALUES_ELEMENT, "" + cCompilerConfiguration.getInheritPreprocessor().getValue()); // NOI18N
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

    public static void writeCCCompilerConfiguration(XMLEncoderStream xes, CCCompilerConfiguration ccCompilerConfiguration) {
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
        if (ccCompilerConfiguration.getTool().getModified()) {
            xes.element(COMMANDLINE_TOOL_ELEMENT, "" + ccCompilerConfiguration.getTool().getValue()); // NOI18N
        }
        if (ccCompilerConfiguration.getIncludeDirectories().getModified()) {
            writeDirectories(xes, INCLUDE_DIRECTORIES_ELEMENT2, ccCompilerConfiguration.getIncludeDirectories().getValue()); // NOI18N
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
            writeList(xes, PREPROCESSOR_LIST_ELEMENT, ccCompilerConfiguration.getPreprocessorConfiguration().getValue());
        }
        if (ccCompilerConfiguration.getInheritPreprocessor().getModified()) {
            xes.element(INHERIT_PRE_VALUES_ELEMENT, "" + ccCompilerConfiguration.getInheritPreprocessor().getValue()); // NOI18N
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
        writeLibrariesConfiguration(xes, linkerConfiguration.getLibrariesConfiguration());
        if (linkerConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + linkerConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
        }	//xes.element(DEBUGGING_ELEMENT, "" + linkerConfiguration.getTool().getValue() + " " + linkerConfiguration.getOptions()); // NOI18N
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
                    new AttrValuePair(MAKE_ARTIFACT_OP_ELEMENT, makeArtifact.getOutput()),});
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
        if (archiverConfiguration.getCommandLineConfiguration().getModified()) {
            xes.element(COMMAND_LINE_ELEMENT, "" + archiverConfiguration.getCommandLineConfiguration().getValue()); // NOI18N
        }	//xes.element(DEBUGGING_ELEMENT, "" + archiverConfiguration.getTool().getValue() + " " + archiverConfiguration.getOptions()); // NOI18N
        xes.elementClose(ARCHIVERTOOL_ELEMENT);
    }

    public static void writeList(XMLEncoderStream xes, String tag, List<String> list) {
        writeList(xes, tag, LIST_ELEMENT, list);
    }

    public static void writeDirectories(XMLEncoderStream xes, String tag, List<String> directories) {
        writeList(xes, tag, PATH_ELEMENT, directories);
    }

    public static void writeList(XMLEncoderStream xes, String tag, String listTag, List<String> directories) {
        if (directories.size() == 0) {
            return;
        }
        xes.elementOpen(tag);
        for (String dir : directories) {
            xes.element(listTag, dir);
        }
        xes.elementClose(tag);
    }
}
