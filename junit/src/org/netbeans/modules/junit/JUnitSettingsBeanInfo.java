/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CreateTestAction.java
 *
 * Created on January 29, 2001, 7:08 PM
 */

package org.netbeans.modules.junit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Enumeration;
import java.awt.Image;
import java.beans.*;

import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** Description of {@link JUnitSettings}.
 *
 * @author vstejskal
 */
public class JUnitSettingsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor propFileSystem = new PropertyDescriptor (JUnitSettings.PROP_FILE_SYSTEM, JUnitSettings.class);
            propFileSystem.setPropertyEditorClass(FileSystemPropEd.class);
            propFileSystem.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_file_system"));
            propFileSystem.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_file_system"));
            
            PropertyDescriptor propSuiteTemplate = new PropertyDescriptor (JUnitSettings.PROP_SUITE_TEMPLATE, JUnitSettings.class);
            propSuiteTemplate.setPropertyEditorClass(TemplatePropEd.class);
            propSuiteTemplate.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_suite_template"));
            propSuiteTemplate.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_suite_template"));

            PropertyDescriptor propClassTemplate = new PropertyDescriptor (JUnitSettings.PROP_CLASS_TEMPLATE, JUnitSettings.class);
            propClassTemplate.setPropertyEditorClass(TemplatePropEd.class);
            propClassTemplate.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_class_template"));
            propClassTemplate.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_class_template"));

            PropertyDescriptor propMembersPublic = new PropertyDescriptor (JUnitSettings.PROP_MEMBERS_PUBLIC, JUnitSettings.class);
            propMembersPublic.setPropertyEditorClass(BoolPropEd.class);
            propMembersPublic.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_public"));
            propMembersPublic.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_public"));

            PropertyDescriptor propMembersProtected = new PropertyDescriptor (JUnitSettings.PROP_MEMBERS_PROTECTED, JUnitSettings.class);
            propMembersProtected.setPropertyEditorClass(BoolPropEd.class);
            propMembersProtected.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_protected"));
            propMembersProtected.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_protected"));

            PropertyDescriptor propMembersPackage = new PropertyDescriptor (JUnitSettings.PROP_MEMBERS_PACKAGE, JUnitSettings.class);
            propMembersPackage.setPropertyEditorClass(BoolPropEd.class);
            propMembersPackage.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_package"));
            propMembersPackage.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_package"));

            PropertyDescriptor propBodyComments = new PropertyDescriptor (JUnitSettings.PROP_BODY_COMMENTS, JUnitSettings.class);
            propBodyComments.setPropertyEditorClass(BoolPropEd.class);
            propBodyComments.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_body_comments"));
            propBodyComments.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_body_comments"));

            PropertyDescriptor propBodyContent = new PropertyDescriptor (JUnitSettings.PROP_BODY_CONTENT, JUnitSettings.class);
            propBodyContent.setPropertyEditorClass(BoolPropEd.class);
            propBodyContent.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_body_content"));
            propBodyContent.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_body_content"));

            PropertyDescriptor propJavaDoc = new PropertyDescriptor (JUnitSettings.PROP_JAVADOC, JUnitSettings.class);
            propJavaDoc.setPropertyEditorClass(BoolPropEd.class);
            propJavaDoc.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_javadoc"));
            propJavaDoc.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_javadoc"));
            
            PropertyDescriptor propCfgConfigEnabled = new PropertyDescriptor (JUnitSettings.PROP_CFGCREATE_ENABLED, JUnitSettings.class);
            propCfgConfigEnabled.setPropertyEditorClass(BoolPropEd.class);
            propCfgConfigEnabled.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_cfgcreate_enabled"));
            propCfgConfigEnabled.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_cfgcreate_enabled"));            
            
            
            PropertyDescriptor propGenerateExceptionClasses = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_EXCEPTION_CLASSES, JUnitSettings.class);
            propGenerateExceptionClasses.setPropertyEditorClass(BoolPropEd.class);
            propGenerateExceptionClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_exception_classes"));
            propGenerateExceptionClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_exception_classes"));
            
            PropertyDescriptor propGenerateAbstractImpl = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_ABSTRACT_IMPL, JUnitSettings.class);
            propGenerateAbstractImpl.setPropertyEditorClass(BoolPropEd.class);
            propGenerateAbstractImpl.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_abstract_impl"));
            propGenerateAbstractImpl.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_abstract_impl"));

            
            PropertyDescriptor propGenerateSuiteClasses = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_SUITE_CLASSES, JUnitSettings.class);
            propGenerateSuiteClasses.setPropertyEditorClass(BoolPropEd.class);
            propGenerateSuiteClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_suite_classes"));
            propGenerateSuiteClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_suite_classes"));
            
            PropertyDescriptor propIncludePackagePrivateClasses = new PropertyDescriptor (JUnitSettings.PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES, JUnitSettings.class);
            propIncludePackagePrivateClasses.setPropertyEditorClass(BoolPropEd.class);
            propIncludePackagePrivateClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_include_package_private_classes"));
            propIncludePackagePrivateClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_include_package_private_classes"));
            

            
            // expert properties
            PropertyDescriptor propTestRunner = new PropertyDescriptor (JUnitSettings.PROP_TEST_RUNNER, JUnitSettings.class);
            propTestRunner.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_test_runner"));
            propTestRunner.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_test_runner"));
            propTestRunner.setExpert(true);
            
            PropertyDescriptor propProperties = new PropertyDescriptor (JUnitSettings.PROP_PROPERTIES, JUnitSettings.class);
            propProperties.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_properties"));
            propProperties.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_properties"));
            propProperties.setExpert(true);
            
            PropertyDescriptor propInternalExecutor = new PropertyDescriptor (JUnitSettings.PROP_EXECUTOR_TYPE, JUnitSettings.class);
            propInternalExecutor.setPropertyEditorClass(ExecutorPropEd.class);
            propInternalExecutor.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_executor_type"));
            propInternalExecutor.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_executor_type"));            
            propInternalExecutor.setExpert(true);           
            
            PropertyDescriptor propGenerateTestsFromTestClasses = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_TESTS_FROM_TEST_CLASSES, JUnitSettings.class);
            propGenerateTestsFromTestClasses.setPropertyEditorClass(BoolPropEd.class);
            propGenerateTestsFromTestClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_test_from_test_classes"));
            propGenerateTestsFromTestClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_test_from_test_classes"));
            propGenerateTestsFromTestClasses.setExpert(true);
            
            PropertyDescriptor propGenerateMainMethod = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_MAIN_METHOD, JUnitSettings.class);
            propGenerateMainMethod.setPropertyEditorClass(BoolPropEd.class);
            propGenerateMainMethod.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_main_method"));
            propGenerateMainMethod.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_main_method"));
            propGenerateMainMethod.setExpert(true);
            
            PropertyDescriptor propGenerateMainMethodBody = new PropertyDescriptor (JUnitSettings.PROP_GENERATE_MAIN_METHOD_BODY, JUnitSettings.class);
            propGenerateMainMethodBody.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_main_method_body"));
            propGenerateMainMethodBody.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_main_method_body"));            
            propGenerateMainMethodBody.setExpert(true);

            PropertyDescriptor propTestClassNamePrefix = new PropertyDescriptor (JUnitSettings.PROP_TEST_CLASSNAME_PREFIX, JUnitSettings.class);
            propTestClassNamePrefix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_test_classname_prefix"));
            propTestClassNamePrefix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_test_classname_prefix"));
            propTestClassNamePrefix.setExpert(true);

            PropertyDescriptor propTestClassNameSuffix = new PropertyDescriptor (JUnitSettings.PROP_TEST_CLASSNAME_SUFFIX, JUnitSettings.class);
            propTestClassNameSuffix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_test_classname_suffix"));
            propTestClassNameSuffix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_test_classname_suffix"));            
            propTestClassNameSuffix.setExpert(true);
            
            PropertyDescriptor propSuiteClassNamePrefix = new PropertyDescriptor (JUnitSettings.PROP_SUITE_CLASSNAME_PREFIX, JUnitSettings.class);
            propSuiteClassNamePrefix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_suite_classname_prefix"));
            propSuiteClassNamePrefix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_suite_classname_prefix"));
            propSuiteClassNamePrefix.setExpert(true);

            PropertyDescriptor propSuiteClassNameSuffix = new PropertyDescriptor (JUnitSettings.PROP_SUITE_CLASSNAME_SUFFIX, JUnitSettings.class);
            propSuiteClassNameSuffix.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_suite_classname_suffix"));
            propSuiteClassNameSuffix.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_suite_classname_suffix"));            
            propSuiteClassNameSuffix.setExpert(true);
            
            PropertyDescriptor propRootSuiteClassName = new PropertyDescriptor (JUnitSettings.PROP_ROOT_SUITE_CLASSNAME, JUnitSettings.class);
            propRootSuiteClassName.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_root_suite_classname"));
            propRootSuiteClassName.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_root_suite_classname"));            
            propRootSuiteClassName.setExpert(true);
                        

            return new PropertyDescriptor[] { propFileSystem, propSuiteTemplate, propClassTemplate,
              propMembersPublic, propMembersProtected, propMembersPackage, propBodyComments, propBodyContent, 
              propJavaDoc, propCfgConfigEnabled, propInternalExecutor, 
              propGenerateExceptionClasses, propGenerateAbstractImpl, propIncludePackagePrivateClasses, 
              propGenerateSuiteClasses, propTestRunner, propProperties,  propGenerateTestsFromTestClasses,
              propGenerateMainMethod, propGenerateMainMethodBody, propTestClassNamePrefix, propTestClassNameSuffix,
              propSuiteClassNamePrefix, propSuiteClassNameSuffix, propRootSuiteClassName 
            };
        }
        catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("/org/netbeans/modules/junit/resources/JUnitSettingsIcon.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/junit/resources/JUnitSettingsIcon32.gif");
            return icon32;
        }
    }

    public static class BoolPropEd extends PropertyEditorSupport {
        private static final String[] tags = {
            NbBundle.getMessage (JUnitSettingsBeanInfo.class, "LBL_true"),
            NbBundle.getMessage (JUnitSettingsBeanInfo.class, "LBL_false")
	};

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            return tags[((Boolean) getValue ()).booleanValue () ? 0 : 1];
        }

        public void setAsText (String text) throws IllegalArgumentException {
            if (tags[0].equals (text))
                setValue (Boolean.TRUE);
            else if (tags[1].equals (text))
                setValue (Boolean.FALSE);
            else
                throw new IllegalArgumentException ();
        }
    }
    
    public static class SortedListPropEd extends PropertyEditorSupport {
        private LinkedList  displays = new LinkedList();
        private LinkedList  values = new LinkedList();
        private String      defaultDisplay = NbBundle.getMessage(JUnitSettingsBeanInfo.class, "LBL_value_not_found");
        private String      defaultValue = "";

        public String[] getTags () {
            TreeSet t = new TreeSet(displays);
            if (displays.size() > 0) {
                return (String []) t.toArray(new String[displays.size() - 1]);
            } else {
                return new String[0];
            }
        }

        public String getAsText () {
            String      value = null;
            String      display = null;
            Iterator    iD = displays.iterator();
            Iterator    iV = values.iterator();
            while (iV.hasNext()) {
                value = (String) iV.next();
                display = (String) iD.next();
                if (value.equals(getValue()))
                    return display;
            }
            return defaultDisplay;
        }
        
        public void setAsText (String text) throws IllegalArgumentException {
            String      value = null;
            String      display = null;
            Iterator    iD = displays.iterator();
            Iterator    iV = values.iterator();
            while (iD.hasNext()) {
                value = (String) iV.next();
                display = (String) iD.next();
                if (display.equals(text)) {
                    setValue(value);
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }

        protected void put(String display, String value, int type) {
            if (SHOW_IN_LIST == (type & SHOW_IN_LIST)) {
                displays.add(display);
                values.add(value);
            }
            if (IS_DEFAULT == (type & IS_DEFAULT)) {
                defaultDisplay = display;
                defaultValue = value;
            }
        }
        
        protected static int SHOW_IN_LIST   = 1;
        protected static int IS_DEFAULT     = 2;
    }

    public static class ExecutorPropEd extends PropertyEditorSupport {
        private static final String[] tags = {
            NbBundle.getMessage (JUnitSettingsBeanInfo.class, "LBL_executor_external"),
            NbBundle.getMessage (JUnitSettingsBeanInfo.class, "LBL_executor_internal"),
            NbBundle.getMessage (JUnitSettingsBeanInfo.class, "LBL_executor_debugger")
	};

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            return tags[((Integer)getValue()).intValue()];
       }

        public void setAsText (String text) throws IllegalArgumentException {
            for(int i = 0; i < tags.length; i++) {
                if (tags[i].equals(text)) {
                    setValue(new Integer(i));
                    return;
               }
            }
            throw new IllegalArgumentException ();
        }
    }

    public static class FileSystemPropEd extends SortedListPropEd {
        public FileSystemPropEd() {
            // default value, when no file system is selected
            // 
            
            int fsCounter = 0;            
            Enumeration fss = Repository.getDefault().getFileSystems();            
            while (fss.hasMoreElements()) {                
                FileSystem fs = (FileSystem) fss.nextElement();                
                if (TestUtil.isSupportedFileSystem(fs)) {
                    fsCounter++;
                    int propertyState;
                    if (fsCounter == 1) {
                        propertyState =  SHOW_IN_LIST | IS_DEFAULT;
                    } else {
                        propertyState = SHOW_IN_LIST;
                    }
                    put(fs.getDisplayName(), fs.getSystemName(), propertyState);
                }
            }
            
            if (fsCounter == 0) {
                put(NbBundle.getMessage(JUnitSettingsBeanInfo.class, "LBL_no_file_system_selected"), "", SHOW_IN_LIST | IS_DEFAULT);
            }
            
            
        }
    }

    public static class TemplatePropEd extends SortedListPropEd {
        public TemplatePropEd() {
            FileObject  foJUnitTmpl;
            FileObject  foTemplates[];

            foJUnitTmpl = Repository.getDefault().getDefaultFileSystem().findResource("Templates/JUnit");
            if (null != foJUnitTmpl) {
                foTemplates = foJUnitTmpl.getChildren();
                for(int i = 0; i < foTemplates.length; i++) {
                    if (!foTemplates[i].getExt().equals("java"))
                        continue;
                    put(foTemplates[i].getName(), foTemplates[i].getPackageNameExt('/', '.'), SHOW_IN_LIST);
                }
            }
        }
    }
}
