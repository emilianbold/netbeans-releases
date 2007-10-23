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

package org.netbeans.modules.apisupport.project.ui.wizard.options;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Wizard for generating OptionsPanel
 *
 * @author Radek Matous
 */
final class NewOptionsIterator extends BasicWizardIterator {
    
    private NewOptionsIterator.DataModel data;
    
    private NewOptionsIterator() {  /* Use factory method. */ }
    
    public static NewOptionsIterator createIterator() {
        return new NewOptionsIterator();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewOptionsIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new OptionsPanel0(wiz, data),
            new OptionsPanel(wiz, data)
        };
    }
    
    public @Override void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private static final int ERR_BLANK_DISPLAYNAME = 1;
        private static final int ERR_BLANK_TOOLTIP = 2;
        private static final int ERR_BLANK_TITLE = 4;
        private static final int ERR_BLANK_CATEGORY_NAME = 5;
        private static final int ERR_BLANK_ICONPATH = 6;
        private static final int ERR_BLANK_PACKAGE_NAME = 7;
        private static final int ERR_BLANK_CLASSNAME_PREFIX = 8;
        private static final int ERR_INVALID_CLASSNAME_PREFIX = 9;
        
        
        private static final int WARNING_INCORRECT_ICON_SIZE = -1;
        
        private static final String[] CATEGORY_BUNDLE_KEYS = {
            "OptionsCategory_Title", // NOI18N
            "OptionsCategory_Name", // NOI18N
        };
        
        private static final String[] ADVANCED_BUNDLE_KEYS = {
            "AdvancedOption_DisplayName", // NOI18N
            "AdvancedOption_Tooltip" // NOI18N
        };
        
        private static final String[] TOKENS = {
            "PACKAGE_NAME", // NOI18N
            "AdvancedOption_CLASS_NAME", // NOI18N
            "OptionsCategory_CLASS_NAME", // NOI18N
            "Panel_CLASS_NAME", // NOI18N
            "OptionsPanelController_CLASS_NAME", // NOI18N
            "ICON_PATH", // NOI18N
            ADVANCED_BUNDLE_KEYS[0],
            ADVANCED_BUNDLE_KEYS[1],
            CATEGORY_BUNDLE_KEYS[0],
            CATEGORY_BUNDLE_KEYS[1]
        };
                
        private static final String FORM_TEMPLATE_SUFFIXES[] = {"Panel"}; // NOI18N
        private static final String[] JAVA_TEMPLATE_SUFFIXES = {
            "AdvancedOption",//NOI18N
            "OptionsCategory",//NOI18N
            "Panel",//NOI18N
            "OptionsPanelController"//NOI18N
        };
        private static final String JAVA_TEMPLATE_PREFIX = "template_myplugin"; // NOI18N
        private static final String FORM_TEMPLATE_PREFIX = "template_myplugin_form"; // NOI18N
        
        private CreatedModifiedFiles files;
        private String codeNameBase;
        private boolean advanced;
        
        //Advanced panel
        private String displayName;
        private String tooltip;
        
        //OptionsCategory
        private String title;
        private String categoryName;
        private String iconPath;
        
        private String classNamePrefix;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        int setDataForAdvanced(final String displayName, final String tooltip) {
            this.advanced = true;
            this.displayName = displayName;
            this.tooltip = tooltip;
            return checkFirstPanel();
        }
        
        int setDataForOptionCategory(final String title,
                final String categoryName, final String iconPath) {
            this.advanced = false;
            this.title = title;
            this.categoryName = categoryName;
            this.iconPath = iconPath;
            return checkFirstPanel();
        }
        
        public @Override String getPackageName() {
            String retValue;
            retValue = super.getPackageName();
            if (retValue == null) {
                retValue = getCodeNameBase();
                super.setPackageName(retValue);
            }
            return retValue;
        }
        
        public int setPackageAndPrefix(String packageName, String classNamePrefix) {
            setPackageName(packageName);
            this.classNamePrefix = classNamePrefix;
            int errCode = checkFinalPanel();
            if (isSuccessCode(errCode)) {
                generateCreatedModifiedFiles();
            }
            return errCode;
        }
        
        private Map<String, String> getTokenMap() {
            Map<String, String> retval = new HashMap<String, String>();
            for (int i = 0; i < TOKENS.length; i++) {
                if (isAdvanced() && "ICON_PATH".equals(TOKENS[i])) { // NOI18N
                    continue;
                }
                retval.put(TOKENS[i], getReplacement(TOKENS[i]));
            }
            return retval;
        }
        
        private String getReplacement(String key) {
            if ("PACKAGE_NAME".equals(key)) {// NOI18N
                return getPackageName();
            } else if ("AdvancedOption_CLASS_NAME".equals(key)) {// NOI18N
                return getAdvancedOptionClassName();
            } else if ("OptionsCategory_CLASS_NAME".equals(key)) {// NOI18N
                return getOptionsCategoryClassName();
            } else if ("Panel_CLASS_NAME".equals(key)) {// NOI18N
                return getPanelClassName();
            } else if ("OptionsPanelController_CLASS_NAME".equals(key)) {// NOI18N
                return getOptionsPanelControllerClassName();
            } else if ("ICON_PATH".equals(key)) {// NOI18N
                return addCreateIconOperation(new CreatedModifiedFiles(getProject()), getIconPath());
            } else {
                return key + "_" + getClassNamePrefix();
            }
            
        }
        
        
        private String getBundleValue(String key) {
            if (key.startsWith("OptionsCategory_Title")) {// NOI18N
                return getTitle();
            } else if (key.startsWith("OptionsCategory_Name")) {// NOI18N
                return getCategoryName();
            } else if (key.startsWith("AdvancedOption_DisplayName")) {// NOI18N
                return getDisplayName();
            } else if (key.startsWith("AdvancedOption_Tooltip")) {// NOI18N
                return getTooltip();
            } else {
                throw new AssertionError(key);
            }
        }
        
        /**
         * getErrorCode() and getErrorMessage are tigthly coupled. Moreover the
         * order should depend on ordering of textfields in panels.
         */
        String getErrorMessage(int errCode) {
            assert errCode > 0;
            String field = null;
            switch(errCode) {
                case ERR_BLANK_DISPLAYNAME:
                    field = "FIELD_DisplayName";//NOI18N
                    break;
                case ERR_BLANK_TOOLTIP:
                    field = "FIELD_Tooltip";//NOI18N
                    break;
                case ERR_BLANK_TITLE:
                    field = "FIELD_Title";//NOI18N
                    break;
                case ERR_BLANK_CATEGORY_NAME:
                    field = "FIELD_CategoryName";//NOI18N
                    break;
                case ERR_BLANK_ICONPATH:
                    field = "FIELD_IconPath";//NOI18N
                    break;
                case ERR_BLANK_PACKAGE_NAME:
                    field = "FIELD_PackageName";//NOI18N
                    break;
                case ERR_BLANK_CLASSNAME_PREFIX:
                    field = "FIELD_ClassNamePrefix";//NOI18N
                    break;
                case ERR_INVALID_CLASSNAME_PREFIX:
                    return NbBundle.getMessage(NewOptionsIterator.class, "ERR_Name_Prefix_Invalid");//NOI18N
                default:
                    assert false : "Unknown errCode: " + errCode;
            }
            field = NbBundle.getMessage(NewOptionsIterator.class, field);
            return (errCode > 0) ?
                NbBundle.getMessage(NewOptionsIterator.class, "ERR_FieldInvalid",field) : "";//NOI18N
        }
        
        /**
         * getErrorCode() and getWarningMessage are tigthly coupled. Moreover the
         * order should depend on ordering of textfields in panels.
         */
        String getWarningMessage(int warningCode) {
            assert warningCode < 0;
            String result;
            switch(warningCode) {
                case WARNING_INCORRECT_ICON_SIZE:
                    File icon = new File(getIconPath());
                    assert icon.exists();
                    result = UIUtil.getIconDimensionWarning(icon, 32, 32);
                    break;
                default:
                    assert false : "Unknown warningCode: " + warningCode;
                    result = "";
            }
            return result;
        }
        
        static boolean isSuccessCode(int code) {
            return code == 0;
        }
        
        static boolean isErrorCode(int code) {
            return code > 0;
        }
        
        static boolean isWarningCode(int code) {
            return code < 0;
        }
        
        private int checkFirstPanel() {
            if (advanced) {
                if (getDisplayName().length() == 0) {
                    return ERR_BLANK_DISPLAYNAME;
                } else if (getTooltip().length() == 0) {
                    return ERR_BLANK_TOOLTIP;
                }
            } else {
                if (getTitle().length() == 0) {
                    return ERR_BLANK_TITLE;
                } else if (getCategoryName().length() == 0) {
                    return ERR_BLANK_CATEGORY_NAME;
                } else if (getIconPath().length() == 0) {
                    return ERR_BLANK_ICONPATH;
                } else if (getTitle().length() == 0) {
                    return ERR_BLANK_TITLE;
                } else {
                    File icon = new File(getIconPath());
                    if (!icon.exists()) {
                        return ERR_BLANK_ICONPATH;
                    }
                }
                //warnings should go at latest
                File icon = new File(getIconPath());
                assert icon.exists();
                if (!UIUtil.isValidIcon(icon, 32, 32)) {
                    return WARNING_INCORRECT_ICON_SIZE;
                }
            }
            return 0;
        }
        
        private int checkFinalPanel() {
            if (getPackageName().length() == 0) {
                return ERR_BLANK_PACKAGE_NAME;
            } else if (getClassNamePrefix().length() == 0) {
                return ERR_BLANK_CLASSNAME_PREFIX;
            } else if (!Utilities.isJavaIdentifier(getClassNamePrefix())) {
                return ERR_INVALID_CLASSNAME_PREFIX;
        }
            
            return 0;
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            if (files == null) {
                files = generateCreatedModifiedFiles();
            }
            return files;
        }
        
        private CreatedModifiedFiles generateCreatedModifiedFiles() {
            assert isSuccessCode(checkFirstPanel()) || isWarningCode(checkFirstPanel());
            assert isSuccessCode(checkFinalPanel());
            files = new CreatedModifiedFiles(getProject());
            generateFiles();
            generateBundleKeys();
            generateDependencies();
            generateLayerEntry();
            if (!isAdvanced()) {
                addCreateIconOperation(files, getIconPath());
            }
            return files;
        }
        
        private void generateFiles() {
            List allForms = Arrays.asList(FORM_TEMPLATE_SUFFIXES);
            for (int i = 0; i < JAVA_TEMPLATE_SUFFIXES.length; i++) {
                boolean ommit = (isAdvanced()) ? "OptionsCategory".equals(JAVA_TEMPLATE_SUFFIXES[i]) : // NOI18N
                    "AdvancedOption".equals(JAVA_TEMPLATE_SUFFIXES[i]);// NOI18N
                if (ommit) {
                    continue;
                }
                files.add(createJavaFileCopyOperation(JAVA_TEMPLATE_SUFFIXES[i]));
                if (allForms.contains(JAVA_TEMPLATE_SUFFIXES[i])) {
                    files.add(createFormFileCopyOperation(JAVA_TEMPLATE_SUFFIXES[i]));
                }
            }
        }
        
        private void generateBundleKeys() {
            String[] bundleKeys = (isAdvanced()) ? ADVANCED_BUNDLE_KEYS : CATEGORY_BUNDLE_KEYS;
            for (int i = 0; i < bundleKeys.length; i++) {
                String key = getReplacement(bundleKeys[i]);
                String value = getBundleValue(key);
                files.add(files.bundleKey(getDefaultPackagePath("Bundle.properties", true),key,value));// NOI18N                        
            }
        }
        
        private void generateDependencies() {
            files.add(files.addModuleDependency("org.openide.util")); // NOI18N
            files.add(files.addModuleDependency("org.netbeans.modules.options.api","0-1",null,true));// NOI18N
            files.add(files.addModuleDependency("org.openide.awt")); // NOI18N
        }
        
        private void generateLayerEntry() {
            String resourcePathPrefix = (isAdvanced()) ? "OptionsDialog/Advanced/" : "OptionsDialog/";// NOI18N
            String instanceName = isAdvanced() ? getAdvancedOptionClassName() : getOptionsCategoryClassName();
            String instanceFullPath = resourcePathPrefix + getPackageName().replace('.','-') + "-" + instanceName + ".instance";//NOI18N
            files.add(files.createLayerEntry(instanceFullPath, null, null, null, null));
        }
        
        private CreatedModifiedFiles.Operation createJavaFileCopyOperation(final String templateSuffix) {
            FileObject template = CreatedModifiedFiles.getTemplate(JAVA_TEMPLATE_PREFIX + templateSuffix + ".java");
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            return files.createFileWithSubstitutions(getFilePath(templateSuffix), template, getTokenMap());
        }
        
        private String getFilePath(final String templateSuffix) {
            String fileName = getClassNamePrefix()+templateSuffix+ ".java"; // NOI18N
            return getDefaultPackagePath(fileName, false);//NOI18N
        }
        
        private CreatedModifiedFiles.Operation createFormFileCopyOperation(final String templateSuffix) {
            FileObject template = CreatedModifiedFiles.getTemplate(FORM_TEMPLATE_PREFIX + templateSuffix + ".form");
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            String fileName = getClassNamePrefix()+templateSuffix+ ".form";// NOI18N
            String filePath = getDefaultPackagePath(fileName, false);
            return files.createFile(filePath, template);
        }
        
        private String getCodeNameBase() {
            if (codeNameBase == null) {
                NbModuleProvider mod = getProject().getLookup().lookup(NbModuleProvider.class);
                codeNameBase = mod.getCodeNameBase();
            }
            return codeNameBase;
        }
        
        private String getDisplayName() {
            assert !isAdvanced() || displayName != null;
            return displayName;
        }
        
        private String getTooltip() {
            assert !isAdvanced() || tooltip != null;
            return tooltip;
        }
        
        private String getTitle() {
            assert isAdvanced() || title != null;
            return title;
        }
        
        
        private String getCategoryName() {
            assert isAdvanced() || categoryName != null;
            return categoryName;
        }
        
        private String getIconPath() {
            assert isAdvanced() || iconPath != null;
            return iconPath;
        }
        
        String getClassNamePrefix() {
            if (classNamePrefix == null) {
                classNamePrefix = getCodeNameBase();
                classNamePrefix = classNamePrefix.substring(classNamePrefix.lastIndexOf(".")+1);// NOI18N
                classNamePrefix = classNamePrefix.substring(0,1).toUpperCase(Locale.ENGLISH) + classNamePrefix.substring(1); // NOI18N
            }
            return classNamePrefix;
        }
        
        private boolean isAdvanced() {
            return advanced;
        }
        
        private String getAdvancedOptionClassName() {
            return getClassName(JAVA_TEMPLATE_SUFFIXES[0]);
        }
        
        private String getOptionsCategoryClassName() {
            return getClassName(JAVA_TEMPLATE_SUFFIXES[1]);
        }
        
        private String getPanelClassName() {
            return getClassName(JAVA_TEMPLATE_SUFFIXES[2]);
        }
        
        private String getOptionsPanelControllerClassName() {
            return getClassName(JAVA_TEMPLATE_SUFFIXES[3]);
        }
        
        private String getClassName(String suffix) {
            return getClassNamePrefix() + suffix;
        }
        
    }
}
