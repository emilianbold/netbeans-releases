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

import org.openide.*;
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
            PropertyDescriptor propFileSystem = new PropertyDescriptor ("FileSystem", JUnitSettings.class);
            propFileSystem.setPropertyEditorClass(FileSystemPropEd.class);
            propFileSystem.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_file_system"));
            propFileSystem.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_file_system"));
            
            PropertyDescriptor propSuiteTemplate = new PropertyDescriptor ("SuiteTemplate", JUnitSettings.class);
            propSuiteTemplate.setPropertyEditorClass(TemplatePropEd.class);
            propSuiteTemplate.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_suite_template"));
            propSuiteTemplate.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_suite_template"));

            PropertyDescriptor propClassTemplate = new PropertyDescriptor ("ClassTemplate", JUnitSettings.class);
            propClassTemplate.setPropertyEditorClass(TemplatePropEd.class);
            propClassTemplate.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_class_template"));
            propClassTemplate.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_class_template"));

            PropertyDescriptor propMembersPublic = new PropertyDescriptor ("MembersPublic", JUnitSettings.class);
            propMembersPublic.setPropertyEditorClass(BoolPropEd.class);
            propMembersPublic.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_public"));
            propMembersPublic.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_public"));

            PropertyDescriptor propMembersProtected = new PropertyDescriptor ("MembersProtected", JUnitSettings.class);
            propMembersProtected.setPropertyEditorClass(BoolPropEd.class);
            propMembersProtected.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_protected"));
            propMembersProtected.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_protected"));

            PropertyDescriptor propMembersPackage = new PropertyDescriptor ("MembersPackage", JUnitSettings.class);
            propMembersPackage.setPropertyEditorClass(BoolPropEd.class);
            propMembersPackage.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_members_package"));
            propMembersPackage.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_members_package"));

            PropertyDescriptor propBodyComments = new PropertyDescriptor ("BodyComments", JUnitSettings.class);
            propBodyComments.setPropertyEditorClass(BoolPropEd.class);
            propBodyComments.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_body_comments"));
            propBodyComments.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_body_comments"));

            PropertyDescriptor propBodyContent = new PropertyDescriptor ("BodyContent", JUnitSettings.class);
            propBodyContent.setPropertyEditorClass(BoolPropEd.class);
            propBodyContent.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_body_content"));
            propBodyContent.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_body_content"));

            PropertyDescriptor propJavaDoc = new PropertyDescriptor ("JavaDoc", JUnitSettings.class);
            propJavaDoc.setPropertyEditorClass(BoolPropEd.class);
            propJavaDoc.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_javadoc"));
            propJavaDoc.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_javadoc"));
            
            PropertyDescriptor propCfgConfigEnabled = new PropertyDescriptor ("CfgCreateEnabled", JUnitSettings.class);
            propCfgConfigEnabled.setPropertyEditorClass(BoolPropEd.class);
            propCfgConfigEnabled.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_cfgcreate_enabled"));
            propCfgConfigEnabled.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_cfgcreate_enabled"));

            PropertyDescriptor propCfgExecEnabled = new PropertyDescriptor ("CfgExecEnabled", JUnitSettings.class);
            propCfgExecEnabled.setPropertyEditorClass(BoolPropEd.class);
            propCfgExecEnabled.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_cfgexec_enabled"));
            propCfgExecEnabled.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_cfgexec_enabled"));

            PropertyDescriptor propInternalExecutor = new PropertyDescriptor ("ExecutorType", JUnitSettings.class);
            propInternalExecutor.setPropertyEditorClass(ExecutorPropEd.class);
            propInternalExecutor.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_executor_type"));
            propInternalExecutor.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_executor_type"));
            
            PropertyDescriptor propGenerateExceptionClasses = new PropertyDescriptor ("GenerateExceptionClasses", JUnitSettings.class);
            propGenerateExceptionClasses.setPropertyEditorClass(BoolPropEd.class);
            propGenerateExceptionClasses.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_generate_exception_classes"));
            propGenerateExceptionClasses.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_generate_exception_classes"));

            PropertyDescriptor propTestRunner = new PropertyDescriptor ("TestRunner", JUnitSettings.class);
            propTestRunner.setDisplayName (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "PROP_test_runner"));
            propTestRunner.setShortDescription (NbBundle.getMessage (JUnitSettingsBeanInfo.class, "HINT_test_runner"));
            
            return new PropertyDescriptor[] { propFileSystem, propSuiteTemplate, propClassTemplate,
              propMembersPublic, propMembersProtected, propMembersPackage, propBodyComments, propBodyContent, 
              propJavaDoc, propCfgConfigEnabled, propCfgExecEnabled, propInternalExecutor, 
              propGenerateExceptionClasses, propTestRunner };
        }
        catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon, icon32;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("JUnitSettingsIcon.gif");
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("JUnitSettingsIcon32.gif");
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
        private LinkedList displays = new LinkedList();
        private LinkedList values = new LinkedList();

        public String[] getTags () {
            TreeSet t = new TreeSet(displays);
            return (String []) t.toArray(new String[displays.size() - 1]);
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
                    break;
            }
            return display;
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
                    break;
                }
            }
        }

        protected void put(String display, String value) {
            displays.add(display);
            values.add(value);
        }
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
            put(NbBundle.getMessage(JUnitSettingsBeanInfo.class, "LBL_no_file_system_selected"), "");
            
            Enumeration fss = TopManager.getDefault().getRepository().getFileSystems();
            while (fss.hasMoreElements()) {
                FileSystem fs = (FileSystem) fss.nextElement();
                if (TestUtil.isSupportedFileSystem(fs)) {
                    put(fs.getDisplayName(), fs.getSystemName());
                }
            }
        }
    }

    public static class TemplatePropEd extends SortedListPropEd {
        public TemplatePropEd() {
            FileObject  foJUnitTmpl;
            FileObject  foTemplates[];

            foJUnitTmpl = TopManager.getDefault().getRepository().getDefaultFileSystem().findResource("Templates/JUnit");
            if (null != foJUnitTmpl) {
                foTemplates = foJUnitTmpl.getChildren();
                for(int i = 0; i < foTemplates.length; i++) {
                    if (!foTemplates[i].getExt().equals("java"))
                        continue;
                    put(foTemplates[i].getName(), foTemplates[i].getPackageNameExt('/', '.'));
                }
            }
        }
    }
}
