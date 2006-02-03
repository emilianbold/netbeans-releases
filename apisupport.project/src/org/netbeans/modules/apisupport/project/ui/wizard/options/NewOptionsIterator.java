/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.options;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Wizard for generating OptionsPanel
 *
 * @author Radek Matous
 */
public class NewOptionsIterator extends BasicWizardIterator {
    
    private NewOptionsIterator.DataModel data;
    
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
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        static final int ERR_Blank_DisplayName = 1;
        static final int ERR_Blank_Tooltip = 2;
        static final int ERR_Blank_Title = 4;
        static final int ERR_Blank_CategoryName = 5;
        static final int ERR_Blank_IconPath = 6;
        static final int ERR_Blank_PackageName = 7;
        static final int ERR_Blank_ClassNamePrefix = 8;
        
        static final int WARNING_Incorrect_IconSize = -1;
        
        
        private static String[] TOKENS = new String[] {
            "@@PACKAGE_NAME@@",//NOIN18N
            "@@AdvancedOption_CLASS_NAME@@",//NOIN18N
            "@@OptionsCategory_CLASS_NAME@@",//NOIN18N
            "@@Panel_CLASS_NAME@@",//NOIN18N
            "@@OptionsPanelController_CLASS_NAME@@",//NOIN18N
            "@@ICON_PATH@@"//NOIN18N
        };
        
        private static final String[] CATEGORY_BUNDLE_KEYS = new String[] {
            "OptionsCategory_Title",//NOIN18N
            "OptionsCategory_Name",//NOIN18N
        };
        
        private static final String[] ADVANCED_BUNDLE_KEYS = new String[] {
            "AdvancedOption_DisplayName",//NOIN18N
            "AdvancedOption_Tooltip"//NOIN18N
        };
        
        private static final String FORM_TEMPLATE_SUFFIXES[] = new String[]{"Panel"};//NOIN18N
        private static final String[] JAVA_TEMPLATE_SUFFIXES = new String[] {
            "AdvancedOption",//NOI18N
            "OptionsCategory",//NOI18N
            "Panel",//NOI18N
            "OptionsPanelController"//NOI18N
        };
        private static final String JAVA_TEMPLATE_PREFIX = "template_myplugin";//NOIN18N
        private static final String FORM_TEMPLATE_PREFIX = "template_myplugin_form";//NOIN18N
        
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
        
        
        public String getPackageName() {
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
        
        private Map getTokenMap() {
            Map retval = new HashMap();
            for (int i = 0; i < TOKENS.length; i++) {
                if (isAdvanced() && "@@ICON_PATH@@".equals(TOKENS[i])) {
                    continue;
                }
                retval.put(TOKENS[i], getReplacement(TOKENS[i]));
            }
            return retval;
        }
        
        private String getReplacement(String key) {
            if ("@@PACKAGE_NAME@@".equals(key)) {// NOI18N
                return getPackageName();
            } else if ("@@AdvancedOption_CLASS_NAME@@".equals(key)) {// NOI18N
                return getAdvancedOptionClassName();
            } else if ("@@OptionsCategory_CLASS_NAME@@".equals(key)) {// NOI18N
                return getOptionsCategoryClassName();
            } else if ("@@Panel_CLASS_NAME@@".equals(key)) {// NOI18N
                return getPanelClassName();
            } else if ("@@OptionsPanelController_CLASS_NAME@@".equals(key)) {// NOI18N
                return getOptionsPanelControllerClassName();
            } else if ("@@ICON_PATH@@".equals(key)) {// NOI18N
                return addCreateIconOperation(new CreatedModifiedFiles(getProject()), getIconPath());
            } else {
                throw new AssertionError(key);
            }
        }
        
        
        private String getBundleValue(String key) {
            if ("OptionsCategory_Title".equals(key)) {// NOI18N
                return getTitle();
            } else if ("OptionsCategory_Name".equals(key)) {// NOI18N
                return getCategoryName();
            } else if ("AdvancedOption_DisplayName".equals(key)) {// NOI18N
                return getDisplayName();
            } else if ("AdvancedOption_Tooltip".equals(key)) {// NOI18N
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
                case ERR_Blank_DisplayName:
                    field = "FIELD_DisplayName";//NOI18N
                    break;
                case ERR_Blank_Tooltip:
                    field = "FIELD_Tooltip";//NOI18N
                    break;
                case ERR_Blank_Title:
                    field = "FIELD_Title";//NOI18N
                    break;
                case ERR_Blank_CategoryName:
                    field = "FIELD_CategoryName";//NOI18N
                    break;
                case ERR_Blank_IconPath:
                    field = "FIELD_IconPath";//NOI18N
                    break;
                case ERR_Blank_PackageName:
                    field = "FIELD_PackageName";//NOI18N
                    break;
                case ERR_Blank_ClassNamePrefix:
                    field = "FIELD_ClassNamePrefix";//NOI18N
                    break;
            }
            assert field != null : errCode;
            field = NbBundle.getMessage(NewOptionsIterator.class, field);
            assert field != null : errCode;
            return (errCode > 0) ?
                NbBundle.getMessage(NewOptionsIterator.class, "ERR_FieldInvalid",field) : "";//NOI18N
        }
        
        /**
         * getErrorCode() and getWarningMessage are tigthly coupled. Moreover the
         * order should depend on ordering of textfields in panels.
         */
        String getWarningMessage(int warningCode) {
            assert warningCode < 0;
            String field = null;
            switch(warningCode) {
                case WARNING_Incorrect_IconSize:
                    File icon = new File(getIconPath());
                    assert icon.exists();
                    ImageIcon ic = null;
                    try {
                        ic = new ImageIcon(icon.toURL());
                        assert ic.getIconHeight() != 32;
                        assert ic.getIconWidth() != 32;
                    } catch (MalformedURLException ex) {
                        ErrorManager.getDefault().notify(ex);
                        assert false;
                        return "";//NOI18N
                    }
                    
                    return NbBundle.getMessage(NewOptionsIterator.class, "MSG_IconSize",//NOI18N
                            Integer.toString(ic.getIconWidth()),Integer.toString(ic.getIconHeight()));
            }
            assert false : warningCode;
            return "";//NOI18N
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
                    return ERR_Blank_DisplayName;
                } else if (getTooltip().length() == 0) {
                    return ERR_Blank_Tooltip;
                }
            } else {
                if (getTitle().length() == 0) {
                    return ERR_Blank_Title;
                } else if (getCategoryName().length() == 0) {
                    return ERR_Blank_CategoryName;
                } else if (getIconPath().length() == 0) {
                    return ERR_Blank_IconPath;
                } else if (getTitle().length() == 0) {
                    return ERR_Blank_Title;
                } else {
                    File icon = new File(getIconPath());
                    if (!icon.exists()) {
                        return ERR_Blank_IconPath;
                    }
                }
                //warnings should go at latest
                ImageIcon ic = null;
                File icon = new File(getIconPath());
                assert icon.exists();
                try {
                    ic = new ImageIcon(icon.toURL());
                    if (ic.getIconHeight() != 32 || ic.getIconWidth() != 32) {
                        return WARNING_Incorrect_IconSize;
                    }
                } catch (MalformedURLException ex) {
                    ErrorManager.getDefault().notify(ex);
                }                
            }
            return 0;
        }

        private int checkFinalPanel() {            
            if (getPackageName().length() == 0) {
                return ERR_Blank_PackageName;
            } else if (getClassNamePrefix().length() == 0) {
                return ERR_Blank_ClassNamePrefix;
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
                if (ommit) continue;
                files.add(createJavaFileCopyOperation(JAVA_TEMPLATE_SUFFIXES[i]));
                if (allForms.contains(JAVA_TEMPLATE_SUFFIXES[i])) {
                    files.add(createFormFileCopyOperation(JAVA_TEMPLATE_SUFFIXES[i]));
                }
            }
        }
        
        private void generateBundleKeys() {
            String[] bundleKeys = (isAdvanced()) ? ADVANCED_BUNDLE_KEYS : CATEGORY_BUNDLE_KEYS;
            for (int i = 0; i < bundleKeys.length; i++) {
                files.add(files.bundleKey(getDefaultPackagePath("Bundle.properties"),// NOI18N
                        bundleKeys[i],getBundleValue(bundleKeys[i])));
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
            URL template = NewOptionsIterator.class.getResource(JAVA_TEMPLATE_PREFIX+templateSuffix);
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            return files.createFileWithSubstitutions(getFilePath(templateSuffix), template,getTokenMap());
        }
        
        private String getFilePath(final String templateSuffix) {
            String fileName = getClassNamePrefix()+templateSuffix+ ".java";
            return getDefaultPackagePath(fileName);//NOI18N
        }
        
        private CreatedModifiedFiles.Operation createFormFileCopyOperation(final String templateSuffix) {
            URL template = NewOptionsIterator.class.getResource(FORM_TEMPLATE_PREFIX+templateSuffix);
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            String fileName = getClassNamePrefix()+templateSuffix+ ".form";// NOI18N
            String filePath = getDefaultPackagePath(fileName);
            return files.createFile(filePath, template);
        }
                
        private String getCodeNameBase() {
            if (codeNameBase == null) {
                ManifestManager mm = ManifestManager.getInstance(getProject().getManifest(), false);
                codeNameBase = mm.getCodeNameBase();
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
