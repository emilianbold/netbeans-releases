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
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Wizard for generating OptionsPanel
 *
 * @author Radek Matous
 */
public class NewOptionsIterator extends BasicWizardIterator {
    
    private static final long serialVersionUID = 1L;
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
        private static String[] TOKENS = new String[] {
            "@@PACKAGE_NAME@@",//NOIN18N
            "@@AdvancedOption_CLASS_NAME@@",//NOIN18N
            "@@OptionsCategory_CLASS_NAME@@",//NOIN18N
            "@@Panel_CLASS_NAME@@",//NOIN18N
            "@@OptionsPanelController_CLASS_NAME@@",//NOIN18N
            "@@MODULE_CNB@@",//NOIN18N
            "@@ICON_PATH@@"//NOIN18N
        };
        
        private static final String[] CATEGORY_BUNDLE_KEYS = new String[] {
            "OptionsCategory_Description",//NOIN18N
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
        private String description;
        private String categoryName;
        private String iconPath;
                
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        int setDataForAdvanced(final String displayName, final String tooltip) {
            this.advanced = true;
            this.displayName = displayName;
            this.tooltip = tooltip;
            return getErrorCode();
        }
        
        int setDataForOptionCategory(final String title, final String description,
                final String categoryName, final String iconPath) {
            this.advanced = false;
            this.title = title;
            this.description = description;
            this.categoryName = categoryName;
            this.iconPath = iconPath;
            return getErrorCode();
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
        
        public int setPackage(String packageName) {
            setPackageName(packageName);
            int errCode = getErrorCode();
            if (errCode == 0) {
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
            } else if ("@@MODULE_CNB@@".equals(key)) {// NOI18N
                return getCodeNameBase();
            } else if ("@@ICON_PATH@@".equals(key)) {// NOI18N
                return addCreateIconOperation(new CreatedModifiedFiles(getProject()), getIconPath());
            }
            assert false;
            throw new IllegalArgumentException(key);
        }
        
        
        private String getBundleValue(String key) {
            if ("OptionsCategory_Description".equals(key)) {// NOI18N
                return getDescription();
            } else if ("OptionsCategory_Title".equals(key)) {// NOI18N
                return getTitle();
            } else if ("OptionsCategory_Name".equals(key)) {// NOI18N
                return getCategoryName();
            } else if ("AdvancedOption_DisplayName".equals(key)) {// NOI18N
                return getDisplayName();
            } else if ("AdvancedOption_Tooltip".equals(key)) {// NOI18N
                return getTooltip();
            }
            
            assert false;
            throw new IllegalArgumentException(key);
        }
        
        
        private String getDefaultPackagePath() {
            return getDefaultPackagePath("");
        }
        
        /**
         * getErrorCode() and getErrorMessage are tigthly coupled. Moreover the
         * order should depend on ordering of textfields in panels.
         */
        static String getErrorMessage(int errCode) {
            String field = null;
            switch(errCode) {
                case 1:
                    field = "FIELD_DisplayName";//NOI18N
                    break;
                case 2:
                    field = "FIELD_Tooltip";//NOI18N
                    break;
                case 4:
                    field = "FIELD_Description";//NOI18N
                    break;
                case 5:
                    field = "FIELD_Title";//NOI18N
                    break;                    
                case 6:
                    field = "FIELD_CategoryName";//NOI18N
                    break;
                case 7:
                    field = "FIELD_IconPath";//NOI18N
                    break;
                case 11:
                    field = "FIELD_PackageName";//NOI18N
                    break;                                        
            }
            assert field != null : errCode;
            field = NbBundle.getMessage(NewOptionsIterator.class, field);
            assert field != null : errCode;            
            return NbBundle.getMessage(NewOptionsIterator.class, "ERR_FieldInvalid",field);//NOI18N
        }
        
        
        private int getErrorCode() {
            if (advanced) {
                int emptyCode = checkIfEmpty(new String[] {getDisplayName(), getTooltip(), getAdvancedOptionClassName()});
                if (emptyCode != 0) {
                    return emptyCode;
                }
            } else {
                int emptyCode = checkIfEmpty(new String[] {getDescription(), getTitle(), getCategoryName(), getIconPath(), getOptionsCategoryClassName()});
                if (emptyCode != 0) {
                    return 3+emptyCode;
                } else {
                    File icon = new File(getIconPath());
                    if (!icon.exists()) {
                        return 7;
                    }
                }
            }
            
            int emptyCode = checkIfEmpty(new String[] {getFileNamePrefix(),getCodeNameBase(), getPackageName(), getPanelClassName(),getOptionsPanelControllerClassName()});
            if (emptyCode != 0) {
                return 8+emptyCode;
            }
            return 0;
        }
        
        private int checkIfEmpty(final String[] checked) {
            for (int i = 0; i < checked.length; i++) {
                assert (checked[i] != null) : i;
                if (checked[i].length() == 0) {
                    return i+1;
                }
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
            assert getErrorCode() == 0;
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
                files.add(files.bundleKey(getDefaultPackagePath()+"/Bundle.properties",// NOI18N
                        bundleKeys[i],getBundleValue(bundleKeys[i])));
            }
        }
        
        private void generateDependencies() {
            files.add(files.addModuleDependency("org.openide.util")); // NOI18N
            files.add(files.addModuleDependency("org.netbeans.modules.options.api"));// NOI18N
        }
        
        private void generateLayerEntry() {
            String resourcePathPrefix = (isAdvanced()) ? "OptionsDialog/Advanced/" : "OptionsDialog/";// NOI18N
            String instanceName = (isAdvanced()) ? getAdvancedOptionClassName() : getOptionsCategoryClassName();
            String instanceFullPath = resourcePathPrefix + getPackageName().replace('.','/')+"/"+instanceName+".instance";//NOI18N
            files.add(files.createLayerEntry(instanceFullPath, null, null, null, null));
            String suffix = (isAdvanced()) ? "@@AdvancedOption_CLASS_NAME@@" : "@@OptionsCategory_CLASS_NAME@@"; //NOI18N
            files.add(files.createLayerAttribute(instanceFullPath, "instanceClass", getPackageName() + "."+ instanceName)); // NOI18N
        }
        
        private CreatedModifiedFiles.Operation createJavaFileCopyOperation(final String templateSuffix) {
            URL template = NewOptionsIterator.class.getResource(JAVA_TEMPLATE_PREFIX+templateSuffix);
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            return files.createFileWithSubstitutions(getFilePath(templateSuffix), template,getTokenMap());
        }
        
        private String getFilePath(final String templateSuffix) {
            String fileName = getFileNamePrefix()+templateSuffix+ ".java";
            return getDefaultPackagePath(fileName);//NOI18N
        }
        
        private CreatedModifiedFiles.Operation createFormFileCopyOperation(final String templateSuffix) {
            URL template = NewOptionsIterator.class.getResource(FORM_TEMPLATE_PREFIX+templateSuffix);
            assert template != null : JAVA_TEMPLATE_PREFIX+templateSuffix;
            String fileName = getFileNamePrefix()+templateSuffix+ ".form";// NOI18N
            String filePath = getDefaultPackagePath(fileName);
            return files.createFile(filePath, template);
        }
        
        private String getFileNamePrefix() {
            String replacement;
            String codeNameBase = getCodeNameBase();
            replacement = codeNameBase.substring(codeNameBase.lastIndexOf(".")+1);// NOI18N
            return replacement.substring(0,1).toUpperCase()+replacement.substring(1);// NOI18N
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
        
        
        private boolean isAdvanced() {
            return advanced;
        }
        
        private String getDescription() {
            assert isAdvanced() || description != null;
            return description;
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
            return getFileNamePrefix() + suffix;
        }
    }
}

