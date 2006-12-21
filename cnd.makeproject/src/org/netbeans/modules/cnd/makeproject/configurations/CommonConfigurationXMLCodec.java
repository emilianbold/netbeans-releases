/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations;

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

/**
 * Common subclass to ConfigurationXMLCodec and AuxConfigurationXMLCodec
 */

/**
 * History:
 * V31:
 *   Now emitting compiler tool info for makefile based projects. This affects 
 *   cCompilerTool, ccCompilerTool, and fortranCompilerTool elements.
 * V30:
 *   added PROJECT_MAKEFILE_ELEMENT (project makefile name)
 * V29:
 *   added FORTRANCOMPILERTOOL_ELEMENT
 */
abstract class CommonConfigurationXMLCodec
    extends XMLDecoder
    implements XMLEncoder {

    protected final static int CURRENT_VERSION = 31;

    // Generic
    protected final static String PROJECT_DESCRIPTOR_ELEMENT = "projectDescriptor";
    protected final static String DEBUGGING_ELEMENT = "justfordebugging";
 // Old style. FIXUP: should be removed....
    protected final static String CONFIGURATION_DESCRIPTOR_ELEMENT = "configurationDescriptor";
    protected final static String DEFAULT_CONF_ELEMENT = "defaultConf";
    protected final static String CONFS_ELEMENT = "confs";
    protected final static String CONF_ELEMENT = "conf";
    protected final static String DIRECTORY_PATH_ELEMENT = "directoryPath";
    protected final static String FOLDER_PATH_ELEMENT = "folderPath"; // Old style. FIXUP : < version 5
    protected final static String SOURCE_FOLDERS_ELEMENT = "sourceFolders"; // Old style. FIXUP : < version 5
    protected final static String LOGICAL_FOLDER_ELEMENT = "logicalFolder";
    protected final static String ITEM_PATH_ELEMENT = "itemPath";
    protected final static String PROJECT_MAKEFILE_ELEMENT = "projectmakefile";
    // Tools Set (Compiler set and platform)
    protected final static String TOOLS_SET_ELEMENT = "toolsSet";
    protected final static String COMPILER_SET_ELEMENT = "compilerSet";
    protected final static String PLATFORM_ELEMENT = "platform";
    // Compile Type
    protected final static String NEO_CONF_ELEMENT = "neoConf"; // Old style. FIXUP : should be removed....
    protected final static String COMPILE_TYPE_ELEMENT = "compileType";
    // Makefile Type
    protected final static String EXT_CONF_ELEMENT = "extConf"; // Old style. FIXUP : should be removed....
    protected final static String MAKEFILE_TYPE_ELEMENT = "makefileType";
    protected final static String MAKETOOL_ELEMENT = "makeTool";
    protected final static String BUILD_COMMAND_ELEMENT = "buildCommand";
    protected final static String BUILD_COMMAND_WORKING_DIR_ELEMENT = "buildCommandWorkingDir";
    protected final static String CLEAN_COMMAND_ELEMENT = "cleanCommand";
    protected final static String EXECUTABLE_PATH_ELEMENT = "executablePath";
    // Common
    protected final static String COMMANDLINE_TOOL_ELEMENT = "commandlineTool";
    protected final static String ADDITIONAL_DEP_ELEMENT = "additionalDep";
    protected final static String OUTPUT_ELEMENT = "output";
    protected final static String INHERIT_INC_VALUES_ELEMENT = "inheritIncValues";
    protected final static String INHERIT_PRE_VALUES_ELEMENT = "inheritPreValues";
    // Compiler (Generic) Tool
    protected final static String INCLUDE_DIRECTORIES_ELEMENT = "includeDirectories";
    protected final static String COMPILERTOOL_ELEMENT = "compilerTool"; // OLD style. FIXUP < version 11
    protected final static String DEBUGGING_SYMBOLS_ELEMENT = "debuggingSymbols";
    protected final static String OPTIMIZATION_LEVEL_ELEMENT = "optimizationLevel";
    protected final static String DEVELOPMENT_MODE_ELEMENT = "developmentMode";
    protected final static String COMMAND_LINE_ELEMENT = "commandLine";
    protected final static String STRIP_SYMBOLS_ELEMENT = "stripSymbols";
    protected final static String SIXTYFOUR_BITS_ELEMENT = "sixtyfourBits";
    protected final static String PREPROCESSOR_ELEMENT = "preprocessor";
    protected final static String SUPRESS_WARNINGS_ELEMENT = "supressWarnings";
    protected final static String WARNING_LEVEL_ELEMENT = "warningLevel";
    protected final static String MT_LEVEL_ELEMENT = "mtLevel";
    protected final static String STANDARDS_EVOLUTION_ELEMENT = "standardsEvolution";
    protected final static String LANGUAGE_EXTENSION_ELEMENT = "languageExtension";
    // C Compiler Tool
    protected final static String SUN_CCOMPILERTOOL_OLD_ELEMENT = "sunCCompilerTool"; // FIXUP <=23
    protected final static String CCOMPILERTOOL_ELEMENT = "cCompilerTool";
    protected final static String CONFORMANCE_LEVEL_ELEMENT = "conformanceLevel"; // FIXUP: <=21
    protected final static String CPP_STYLE_COMMENTS_ELEMENT = "cppstylecomments"; // FIXUP: <=21
    // CC Compiler Tool
    protected final static String SUN_CCCOMPILERTOOL_OLD_ELEMENT = "sunCCCompilerTool"; // FIXUP <=23
    protected final static String CCCOMPILERTOOL_ELEMENT = "ccCompilerTool";
    protected final static String COMPATIBILITY_MODE_ELEMENT = "compatibilityMode"; // FIXUP: <=21
    protected final static String LIBRARY_LEVEL_ELEMENT = "libraryLevel";
    // Fortran Compiler Tool
    protected final static String FORTRANCOMPILERTOOL_ELEMENT = "fortranCompilerTool";
    // Custom Tool
    protected final static String CUSTOMTOOL_ELEMENT = "customTool";
    protected final static String CUSTOMTOOL_COMMANDLINE_ELEMENT = "customToolCommandline";
    protected final static String CUSTOMTOOL_DESCRIPTION_ELEMENT = "customToolDescription";
    protected final static String CUSTOMTOOL_OUTPUTS_ELEMENT = "customToolOutputs";
    protected final static String CUSTOMTOOL_ADDITIONAL_DEP_ELEMENT = "customToolAdditionalDep";
    // Linker Tool
    protected final static String LINKERTOOL_ELEMENT = "linkerTool";
    protected final static String LINKER_KPIC_ELEMENT = "linkerKpic";
    protected final static String LINKER_NORUNPATH_ELEMENT = "linkerNorunpath";
    protected final static String LINKER_ASSIGN_ELEMENT = "linkerAssign";
    protected final static String LINKER_ADD_LIB_ELEMENT = "linkerAddLib";
    protected final static String LINKER_DYN_SERCH_ELEMENT = "linkerDynSerch";
    protected final static String LINKER_LIB_ELEMENT = "linkerLib";
    protected final static String LINKER_LIB_ITEMS_ELEMENT = "linkerLibItems";
    protected final static String LINKER_LIB_PROJECT_ITEM_ELEMENT = "linkerLibProjectItem";
    protected final static String LINKER_LIB_STDLIB_ITEM_ELEMENT = "linkerLibStdlibItem";
    protected final static String LINKER_LIB_LIB_ITEM_ELEMENT = "linkerLibLibItem";
    protected final static String LINKER_LIB_FILE_ITEM_ELEMENT = "linkerLibFileItem";
    protected final static String LINKER_LIB_OPTION_ITEM_ELEMENT = "linkerOptionItem";
    // Make Artifact
    protected final static String MAKE_ARTIFACT_ELEMENT = "makeArtifact";
    protected final static String MAKE_ARTIFACT_PL_ELEMENT = "PL";
    protected final static String MAKE_ARTIFACT_CT_ELEMENT = "CT";
    protected final static String MAKE_ARTIFACT_CN_ELEMENT = "CN";
    protected final static String MAKE_ARTIFACT_AC_ELEMENT = "AC";
    protected final static String MAKE_ARTIFACT_BL_ELEMENT = "BL";
    protected final static String MAKE_ARTIFACT_WD_ELEMENT = "WD";
    protected final static String MAKE_ARTIFACT_BC_ELEMENT = "BC";
    protected final static String MAKE_ARTIFACT_CC_ELEMENT = "CC";
    protected final static String MAKE_ARTIFACT_OP_ELEMENT = "OP";
    // Archiver Tool
    protected final static String ARCHIVERTOOL_ELEMENT = "archiverTool";
    protected final static String ARCHIVERTOOL_RUN_RANLIB_ELEMENT = "runRanlib";
    protected final static String ARCHIVERTOOL_VERBOSE_ELEMENT = "archiverVerbose";
    protected final static String ARCHIVERTOOL_SUPRESS_ELEMENT = "archiverSupress";

    protected final static String VERSION_ATTR = "version";
    protected final static String TYPE_ATTR = "type";
    protected final static String NAME_ATTR = "name";
    protected final static String SET_ATTR = "set";
    protected final static String DISPLAY_NAME_ATTR = "displayName";
    protected final static String PROJECT_FILES_ATTR = "projectFiles";

    protected final static String TRUE_VALUE = "true";
    protected final static String FALSE_VALUE = "false";


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
	    }
	    xes.element(PROJECT_MAKEFILE_ELEMENT, ((MakeConfigurationDescriptor)projectDescriptor).getProjectMakefileName());
	    xes.element(DEFAULT_CONF_ELEMENT, "" + projectDescriptor.getConfs().getActiveAsIndex());
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
		new AttrValuePair[] {
		    new AttrValuePair(NAME_ATTR, "" + makeConfiguration.getName()),
		    new AttrValuePair(TYPE_ATTR, "" + makeConfiguration.getConfigurationType().getValue()),
		});
	    
	    if (publicLocation) {
                writeToolsSetBlock(xes, makeConfiguration);
		if (makeConfiguration.isCompileConfiguration())
		    writeCompiledProjectConfBlock(xes, makeConfiguration);
		if (makeConfiguration.isMakefileConfiguration())
		    writeMakefileProjectConfBlock(xes, makeConfiguration);
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
	xes.element(COMPILER_SET_ELEMENT, "" + makeConfiguration.getCompilerSet().getValue());
	xes.element(PLATFORM_ELEMENT, "" + makeConfiguration.getPlatform().getValue());
	xes.elementClose(TOOLS_SET_ELEMENT);
    }

    private void writeCompiledProjectConfBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration) {
	xes.elementOpen(COMPILE_TYPE_ELEMENT);
	writeCCompilerConfiguration(xes, makeConfiguration.getCCompilerConfiguration());
	writeCCCompilerConfiguration(xes, makeConfiguration.getCCCompilerConfiguration());
	writeFortranCompilerConfiguration(xes, makeConfiguration.getFortranCompilerConfiguration());
	if (makeConfiguration.isLinkerConfiguration())
	    writeLinkerConfiguration(xes, makeConfiguration.getLinkerConfiguration());
	if (makeConfiguration.isArchiverConfiguration())
	    writeArchiverConfiguration(xes, makeConfiguration.getArchiverConfiguration());
	xes.elementClose(COMPILE_TYPE_ELEMENT);
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
	xes.elementClose(MAKETOOL_ELEMENT);
	xes.elementClose(MAKEFILE_TYPE_ELEMENT);
    }

    private void writeLogicalFolders(XMLEncoderStream xes) {
	writeLogicalFolder(xes, ((MakeConfigurationDescriptor)projectDescriptor).getLogicalFolders());
    }
    
    private void writeLogicalFolder(XMLEncoderStream xes, Folder folder) {
	xes.elementOpen(LOGICAL_FOLDER_ELEMENT,
		new AttrValuePair[] {
		    new AttrValuePair(NAME_ATTR, "" + folder.getName()),
		    new AttrValuePair(DISPLAY_NAME_ATTR, "" + folder.getDisplayName()),
		    new AttrValuePair(PROJECT_FILES_ATTR, "" + folder.isProjectFiles()),
	});
	// write out subfolders
	Folder[] subfolders = folder.getFoldersAsArray();
	for (int i = 0; i < subfolders.length; i++) {
	    writeLogicalFolder(xes, subfolders[i]);
	}
	// write out items
	Item[] items = folder.getItemsAsArray();
	for (int i = 0; i < items.length; i++) {
	    xes.element(ITEM_PATH_ELEMENT, items[i].getPath());
	}
	xes.elementClose(LOGICAL_FOLDER_ELEMENT);
    }

    public static void writeCCompilerConfiguration(XMLEncoderStream xes, CCompilerConfiguration cCompilerConfiguration) {
	xes.elementOpen(CCOMPILERTOOL_ELEMENT);
	if (cCompilerConfiguration.getDevelopmentMode().getModified())
	    xes.element(DEVELOPMENT_MODE_ELEMENT, "" + cCompilerConfiguration.getDevelopmentMode().getValue());
	if (cCompilerConfiguration.getStrip().getModified())
	    xes.element(STRIP_SYMBOLS_ELEMENT, "" + cCompilerConfiguration.getStrip().getValue());
	if (cCompilerConfiguration.getSixtyfourBits().getModified())
	    xes.element(SIXTYFOUR_BITS_ELEMENT, "" + cCompilerConfiguration.getSixtyfourBits().getValue());
	if (cCompilerConfiguration.getTool().getModified())
	    xes.element(COMMANDLINE_TOOL_ELEMENT, "" + cCompilerConfiguration.getTool().getValue());
	if (cCompilerConfiguration.getIncludeDirectories().getModified())
	    writeDirectories(xes, INCLUDE_DIRECTORIES_ELEMENT, cCompilerConfiguration.getIncludeDirectories().getValueAsArray());
	if (cCompilerConfiguration.getStandardsEvolution().getModified())
	    xes.element(STANDARDS_EVOLUTION_ELEMENT, "" + cCompilerConfiguration.getStandardsEvolution().getValue());
	if (cCompilerConfiguration.getLanguageExt().getModified())
	    xes.element(LANGUAGE_EXTENSION_ELEMENT, "" + cCompilerConfiguration.getLanguageExt().getValue());
	if (cCompilerConfiguration.getInheritIncludes().getModified())
	    xes.element(INHERIT_INC_VALUES_ELEMENT, "" + cCompilerConfiguration.getInheritIncludes().getValue());
	if (cCompilerConfiguration.getCommandLineConfiguration().getModified())
	    xes.element(COMMAND_LINE_ELEMENT, "" + cCompilerConfiguration.getCommandLineConfiguration().getValue());
	if (cCompilerConfiguration.getPreprocessorConfiguration().getModified())
	    xes.element(PREPROCESSOR_ELEMENT, "" + cCompilerConfiguration.getPreprocessorConfiguration().getValue());
	if (cCompilerConfiguration.getInheritPreprocessor().getModified())
	    xes.element(INHERIT_PRE_VALUES_ELEMENT, "" + cCompilerConfiguration.getInheritPreprocessor().getValue());
	if (cCompilerConfiguration.getWarningLevel().getModified())
	    xes.element(WARNING_LEVEL_ELEMENT, "" + cCompilerConfiguration.getWarningLevel().getValue());
	if (cCompilerConfiguration.getMTLevel().getModified())
	    xes.element(MT_LEVEL_ELEMENT, "" + cCompilerConfiguration.getMTLevel().getValue());
	if (cCompilerConfiguration.getAdditionalDependencies().getModified())
	    xes.element(ADDITIONAL_DEP_ELEMENT, "" + cCompilerConfiguration.getAdditionalDependencies().getValue());
	xes.elementClose(CCOMPILERTOOL_ELEMENT);
    }

    public static void writeCCCompilerConfiguration(XMLEncoderStream xes, CCCompilerConfiguration ccCompilerConfiguration) {
	xes.elementOpen(CCCOMPILERTOOL_ELEMENT);
	if (ccCompilerConfiguration.getDevelopmentMode().getModified())
	    xes.element(DEVELOPMENT_MODE_ELEMENT, "" + ccCompilerConfiguration.getDevelopmentMode().getValue());
	if (ccCompilerConfiguration.getStrip().getModified())
	    xes.element(STRIP_SYMBOLS_ELEMENT, "" + ccCompilerConfiguration.getStrip().getValue());
	if (ccCompilerConfiguration.getSixtyfourBits().getModified())
	    xes.element(SIXTYFOUR_BITS_ELEMENT, "" + ccCompilerConfiguration.getSixtyfourBits().getValue());
	if (ccCompilerConfiguration.getTool().getModified())
	    xes.element(COMMANDLINE_TOOL_ELEMENT, "" + ccCompilerConfiguration.getTool().getValue());
	if (ccCompilerConfiguration.getIncludeDirectories().getModified())
	    writeDirectories(xes, INCLUDE_DIRECTORIES_ELEMENT, ccCompilerConfiguration.getIncludeDirectories().getValueAsArray());
	if (ccCompilerConfiguration.getStandardsEvolution().getModified())
	    xes.element(STANDARDS_EVOLUTION_ELEMENT, "" + ccCompilerConfiguration.getStandardsEvolution().getValue());
	if (ccCompilerConfiguration.getLanguageExt().getModified())
	    xes.element(LANGUAGE_EXTENSION_ELEMENT, "" + ccCompilerConfiguration.getLanguageExt().getValue());
	if (ccCompilerConfiguration.getInheritIncludes().getModified())
	    xes.element(INHERIT_INC_VALUES_ELEMENT, "" + ccCompilerConfiguration.getInheritIncludes().getValue());
	if (ccCompilerConfiguration.getCommandLineConfiguration().getModified())
	    xes.element(COMMAND_LINE_ELEMENT, "" + ccCompilerConfiguration.getCommandLineConfiguration().getValue());
	if (ccCompilerConfiguration.getPreprocessorConfiguration().getModified())
	    xes.element(PREPROCESSOR_ELEMENT, "" + ccCompilerConfiguration.getPreprocessorConfiguration().getValue());
	if (ccCompilerConfiguration.getInheritPreprocessor().getModified())
	    xes.element(INHERIT_PRE_VALUES_ELEMENT, "" + ccCompilerConfiguration.getInheritPreprocessor().getValue());
	if (ccCompilerConfiguration.getWarningLevel().getModified())
	    xes.element(WARNING_LEVEL_ELEMENT, "" + ccCompilerConfiguration.getWarningLevel().getValue());
	if (ccCompilerConfiguration.getMTLevel().getModified())
	    xes.element(MT_LEVEL_ELEMENT, "" + ccCompilerConfiguration.getMTLevel().getValue());
	if (ccCompilerConfiguration.getLibraryLevel().getModified())
	    xes.element(LIBRARY_LEVEL_ELEMENT, "" + ccCompilerConfiguration.getLibraryLevel().getValue());
	if (ccCompilerConfiguration.getAdditionalDependencies().getModified())
	    xes.element(ADDITIONAL_DEP_ELEMENT, "" + ccCompilerConfiguration.getAdditionalDependencies().getValue());
	xes.elementClose(CCCOMPILERTOOL_ELEMENT);
    }
    
    public static void writeFortranCompilerConfiguration(XMLEncoderStream xes, FortranCompilerConfiguration fortranCompilerConfiguration) {
	xes.elementOpen(FORTRANCOMPILERTOOL_ELEMENT);
	if (fortranCompilerConfiguration.getDevelopmentMode().getModified())
	    xes.element(DEVELOPMENT_MODE_ELEMENT, "" + fortranCompilerConfiguration.getDevelopmentMode().getValue());
	if (fortranCompilerConfiguration.getStrip().getModified())
	    xes.element(STRIP_SYMBOLS_ELEMENT, "" + fortranCompilerConfiguration.getStrip().getValue());
	if (fortranCompilerConfiguration.getSixtyfourBits().getModified())
	    xes.element(SIXTYFOUR_BITS_ELEMENT, "" + fortranCompilerConfiguration.getSixtyfourBits().getValue());
	if (fortranCompilerConfiguration.getTool().getModified())
	    xes.element(COMMANDLINE_TOOL_ELEMENT, "" + fortranCompilerConfiguration.getTool().getValue());
	if (fortranCompilerConfiguration.getCommandLineConfiguration().getModified())
	    xes.element(COMMAND_LINE_ELEMENT, "" + fortranCompilerConfiguration.getCommandLineConfiguration().getValue());
	if (fortranCompilerConfiguration.getWarningLevel().getModified())
	    xes.element(WARNING_LEVEL_ELEMENT, "" + fortranCompilerConfiguration.getWarningLevel().getValue());
	xes.elementClose(FORTRANCOMPILERTOOL_ELEMENT);
    }

    public static void writeCustomToolConfiguration(XMLEncoderStream xes, CustomToolConfiguration customToolConfiguration) {
	if (!customToolConfiguration.getModified())
	    return;
	xes.elementOpen(CUSTOMTOOL_ELEMENT);
	if (customToolConfiguration.getCommandLine().getModified())
	    xes.element(CUSTOMTOOL_COMMANDLINE_ELEMENT, "" + customToolConfiguration.getCommandLine().getValue());
	if (customToolConfiguration.getDescription().getModified())
	    xes.element(CUSTOMTOOL_DESCRIPTION_ELEMENT, "" + customToolConfiguration.getDescription().getValue());
	if (customToolConfiguration.getOutputs().getModified())
	    xes.element(CUSTOMTOOL_OUTPUTS_ELEMENT, "" + customToolConfiguration.getOutputs().getValue());
	if (customToolConfiguration.getAdditionalDependencies().getModified())
	    xes.element(CUSTOMTOOL_ADDITIONAL_DEP_ELEMENT, "" + customToolConfiguration.getAdditionalDependencies().getValue());
	xes.elementClose(CUSTOMTOOL_ELEMENT);
    }

    public static void writeLinkerConfiguration(XMLEncoderStream xes, LinkerConfiguration linkerConfiguration) {
	xes.elementOpen(LINKERTOOL_ELEMENT);
	if (linkerConfiguration.getOutput().getModified())
	    xes.element(OUTPUT_ELEMENT, linkerConfiguration.getOutput().getValue());
	if (linkerConfiguration.getAdditionalLibs().getModified())
	    writeDirectories(xes, LINKER_ADD_LIB_ELEMENT, linkerConfiguration.getAdditionalLibs().getValueAsArray());
	if (linkerConfiguration.getDynamicSearch().getModified())
	    writeDirectories(xes, LINKER_DYN_SERCH_ELEMENT, linkerConfiguration.getDynamicSearch().getValueAsArray());
	if (linkerConfiguration.getStripOption().getModified())
	    xes.element(STRIP_SYMBOLS_ELEMENT, "" + linkerConfiguration.getStripOption().getValue());
	if (linkerConfiguration.getKpicOption().getModified())
	    xes.element(LINKER_KPIC_ELEMENT, "" + linkerConfiguration.getKpicOption().getValue());
	if (linkerConfiguration.getNorunpathOption().getModified())
	    xes.element(LINKER_NORUNPATH_ELEMENT, "" + linkerConfiguration.getNorunpathOption().getValue());
	if (linkerConfiguration.getNameassignOption().getModified())
	    xes.element(LINKER_ASSIGN_ELEMENT, "" + linkerConfiguration.getNameassignOption().getValue());
	if (linkerConfiguration.getAdditionalDependencies().getModified())
	    xes.element(ADDITIONAL_DEP_ELEMENT, "" + linkerConfiguration.getAdditionalDependencies().getValue());
	if (linkerConfiguration.getTool().getModified())
	    xes.element(COMMANDLINE_TOOL_ELEMENT, linkerConfiguration.getTool().getValue());
	writeLibrariesConfiguration(xes, linkerConfiguration.getLibrariesConfiguration());
	if (linkerConfiguration.getCommandLineConfiguration().getModified())
	    xes.element(COMMAND_LINE_ELEMENT, "" + linkerConfiguration.getCommandLineConfiguration().getValue());
	//xes.element(DEBUGGING_ELEMENT, "" + linkerConfiguration.getTool().getValue() + " " + linkerConfiguration.getOptions());
	xes.elementClose(LINKERTOOL_ELEMENT);
    }

    public static void writeLibrariesConfiguration(XMLEncoderStream xes, LibrariesConfiguration librariesConfiguration) {
	xes.elementOpen(LINKER_LIB_ITEMS_ELEMENT);
	LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
	for (int i = 0; i < libraryItems.length; i++) {
	    if (libraryItems[i] instanceof LibraryItem.ProjectItem) {
		xes.elementOpen(LINKER_LIB_PROJECT_ITEM_ELEMENT);
		writeMakeArtifact(xes, ((LibraryItem.ProjectItem)libraryItems[i]).getMakeArtifact());
		xes.elementClose(LINKER_LIB_PROJECT_ITEM_ELEMENT);
	    }
	    else if (libraryItems[i] instanceof LibraryItem.StdLibItem) {
		xes.element(LINKER_LIB_STDLIB_ITEM_ELEMENT, ((LibraryItem.StdLibItem)libraryItems[i]).getName());
	    }
	    else if (libraryItems[i] instanceof LibraryItem.LibItem) {
		xes.element(LINKER_LIB_LIB_ITEM_ELEMENT, ((LibraryItem.LibItem)libraryItems[i]).getLibName());
	    }
	    else if (libraryItems[i] instanceof LibraryItem.LibFileItem) {
		xes.element(LINKER_LIB_FILE_ITEM_ELEMENT, ((LibraryItem.LibFileItem)libraryItems[i]).getPath());
	    }
	    else if (libraryItems[i] instanceof LibraryItem.OptionItem) {
		xes.element(LINKER_LIB_OPTION_ITEM_ELEMENT, ((LibraryItem.OptionItem)libraryItems[i]).getLibraryOption());
	    }
	}
	xes.elementClose(LINKER_LIB_ITEMS_ELEMENT);
    }

    public static void writeMakeArtifact(XMLEncoderStream xes, MakeArtifact makeArtifact) {
	xes.elementOpen(MAKE_ARTIFACT_ELEMENT,
		new AttrValuePair[] {
		    new AttrValuePair(MAKE_ARTIFACT_PL_ELEMENT, makeArtifact.getProjectLocation()),
		    new AttrValuePair(MAKE_ARTIFACT_CT_ELEMENT, "" + makeArtifact.getConfigurationType()),
		    new AttrValuePair(MAKE_ARTIFACT_CN_ELEMENT, makeArtifact.getConfigurationName()),
		    new AttrValuePair(MAKE_ARTIFACT_AC_ELEMENT, "" + makeArtifact.getActive()),
		    new AttrValuePair(MAKE_ARTIFACT_BL_ELEMENT, "" + makeArtifact.getBuild()),
		    new AttrValuePair(MAKE_ARTIFACT_WD_ELEMENT, makeArtifact.getWorkingDirectory()),
		    new AttrValuePair(MAKE_ARTIFACT_BC_ELEMENT, makeArtifact.getBuildCommand()),
		    new AttrValuePair(MAKE_ARTIFACT_CC_ELEMENT, makeArtifact.getCleanCommand()),
		    new AttrValuePair(MAKE_ARTIFACT_OP_ELEMENT, makeArtifact.getOutput()),
	});
	/*
	xes.elementOpen(MAKE_ARTIFACT_ELEMENT);
	xes.element(MAKE_ARTIFACT_PL_ELEMENT, makeArtifact.getProjectLocation());
	xes.element(MAKE_ARTIFACT_CT_ELEMENT, "" + makeArtifact.getConfigurationType());
	xes.element(MAKE_ARTIFACT_CN_ELEMENT, makeArtifact.getConfigurationName());
	xes.element(MAKE_ARTIFACT_AC_ELEMENT, "" + makeArtifact.getActive());
	xes.element(MAKE_ARTIFACT_WD_ELEMENT, makeArtifact.getWorkingDirectory());
	xes.element(MAKE_ARTIFACT_BC_ELEMENT, makeArtifact.getBuildCommand());
	xes.element(MAKE_ARTIFACT_CC_ELEMENT, makeArtifact.getCleanCommand());
	xes.element(MAKE_ARTIFACT_CC_ELEMENT, makeArtifact.getOutput());
	*/
	xes.elementClose(MAKE_ARTIFACT_ELEMENT);
    }

    public static void writeArchiverConfiguration(XMLEncoderStream xes, ArchiverConfiguration archiverConfiguration) {
	xes.elementOpen(ARCHIVERTOOL_ELEMENT);
	if (archiverConfiguration.getOutput().getModified())
	    xes.element(OUTPUT_ELEMENT, archiverConfiguration.getOutput().getValue());
	if (archiverConfiguration.getRunRanlib().getModified())
	    xes.element(ARCHIVERTOOL_RUN_RANLIB_ELEMENT, "" + archiverConfiguration.getRunRanlib().getValue());
	if (archiverConfiguration.getVerboseOption().getModified())
	    xes.element(ARCHIVERTOOL_VERBOSE_ELEMENT, "" + archiverConfiguration.getVerboseOption().getValue());
	if (archiverConfiguration.getSupressOption().getModified())
	    xes.element(ARCHIVERTOOL_SUPRESS_ELEMENT, "" + archiverConfiguration.getSupressOption().getValue());
	if (archiverConfiguration.getAdditionalDependencies().getModified())
	    xes.element(ADDITIONAL_DEP_ELEMENT, "" + archiverConfiguration.getAdditionalDependencies().getValue());
	if (archiverConfiguration.getTool().getModified())
	    xes.element(COMMANDLINE_TOOL_ELEMENT, "" + archiverConfiguration.getTool().getValue());
	if (archiverConfiguration.getCommandLineConfiguration().getModified())
	    xes.element(COMMAND_LINE_ELEMENT, "" + archiverConfiguration.getCommandLineConfiguration().getValue());
	//xes.element(DEBUGGING_ELEMENT, "" + archiverConfiguration.getTool().getValue() + " " + archiverConfiguration.getOptions());
	xes.elementClose(ARCHIVERTOOL_ELEMENT);
    }

    public static void writeDirectories(XMLEncoderStream xes, String tag, String[] directories) {
	if (directories.length == 0)
	    return;
	xes.elementOpen(tag);
	for (int i = 0; i < directories.length; i++)
	    xes.element(DIRECTORY_PATH_ELEMENT, directories[i]);
	xes.elementClose(tag);
    }
}
