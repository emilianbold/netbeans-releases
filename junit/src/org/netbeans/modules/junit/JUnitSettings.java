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

import org.openide.*;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileSystem;

import java.io.*;
import java.util.Enumeration;


/** Options for JUnit module, control behavior of test creation and execution.
 *
 * @author  vstejskal
 * @version 1.0
 */
public class JUnitSettings extends SystemOption {

    // static final long serialVersionUID = ...;
    static final long serialVersionUID = 372745543035969452L;

    public static final String PROP_FILE_SYSTEM         = "file_system";
    public static final String PROP_SUITE_TEMPLATE      = "suite_template";
    public static final String PROP_CLASS_TEMPLATE      = "class_template";
    public static final String PROP_MEMBERS_PUBLIC      = "members_public";
    public static final String PROP_MEMBERS_PROTECTED   = "members_protected";
    public static final String PROP_MEMBERS_PACKAGE     = "members_package";
    public static final String PROP_BODY_COMMENTS       = "body_comments";
    public static final String PROP_BODY_CONTENT        = "body_content";
    public static final String PROP_JAVADOC             = "javadoc";
    public static final String PROP_CFGCREATE_ENABLED   = "cfgcreate_enabled";
    public static final String PROP_CFGEXEC_ENABLED     = "cfgexec_enabled";
    public static final String PROP_EXECUTOR_TYPE       = "executor_type";
    public static final String PROP_GENERATE_EXCEPTION_CLASSES = "generate_exceptions";
    public static final String PROP_TEST_RUNNER         = "test_runner";

    public static final int EXECUTOR_EXTERNAL           = 0;
    public static final int EXECUTOR_INTERNAL           = 1;
    public static final int EXECUTOR_DEBUGGER           = 2;
    
    // No constructor please!

    protected void initialize () {
        // If you have more complex default values which might require
        // other parts of the module to already be installed, do not
        // put them here; e.g. make the getter return them as a
        // default if getProperty returns null. (The class might be
        // initialized partway through module installation.)
        
        super.initialize();
        
        putProperty(PROP_FILE_SYSTEM, "", true);
        putProperty(PROP_SUITE_TEMPLATE, "Templates/JUnit/SimpleTest.java", true);
        putProperty(PROP_CLASS_TEMPLATE, "Templates/JUnit/SimpleTest.java", true);
        putProperty(PROP_MEMBERS_PUBLIC, new Boolean(true), true);
        putProperty(PROP_MEMBERS_PROTECTED, new Boolean(true), true);
        putProperty(PROP_MEMBERS_PACKAGE, new Boolean(true), true);
        putProperty(PROP_BODY_COMMENTS, new Boolean(true), true);
        putProperty(PROP_BODY_CONTENT, new Boolean(true), true);
        putProperty(PROP_JAVADOC, new Boolean(true), true);
        putProperty(PROP_CFGCREATE_ENABLED, new Boolean(true), true);
        putProperty(PROP_CFGEXEC_ENABLED, new Boolean(true), true);
        putProperty(PROP_EXECUTOR_TYPE, new Integer(EXECUTOR_EXTERNAL), true);
        putProperty(PROP_GENERATE_EXCEPTION_CLASSES, new Boolean(true), true);
        putProperty(PROP_TEST_RUNNER, "org.netbeans.modules.junit.TestRunner", true);
    }

    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeObject(getProperty(PROP_FILE_SYSTEM));
        out.writeObject(getProperty(PROP_SUITE_TEMPLATE));
        out.writeObject(getProperty(PROP_CLASS_TEMPLATE));
        out.writeObject(getProperty(PROP_MEMBERS_PUBLIC));
        out.writeObject(getProperty(PROP_MEMBERS_PROTECTED));
        out.writeObject(getProperty(PROP_MEMBERS_PACKAGE));
        out.writeObject(getProperty(PROP_BODY_COMMENTS));
        out.writeObject(getProperty(PROP_BODY_CONTENT));
        out.writeObject(getProperty(PROP_JAVADOC));
        out.writeObject(getProperty(PROP_CFGCREATE_ENABLED));
        out.writeObject(getProperty(PROP_CFGEXEC_ENABLED));
        out.writeObject(getProperty(PROP_EXECUTOR_TYPE));
        out.writeObject(getProperty(PROP_GENERATE_EXCEPTION_CLASSES));
        out.writeObject(getProperty(PROP_TEST_RUNNER));
    }
    
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        putProperty(PROP_FILE_SYSTEM, in.readObject(), true);
        putProperty(PROP_SUITE_TEMPLATE, in.readObject(), true);
        putProperty(PROP_CLASS_TEMPLATE, in.readObject(), true);
        putProperty(PROP_MEMBERS_PUBLIC, in.readObject(), true);
        putProperty(PROP_MEMBERS_PROTECTED, in.readObject(), true);
        putProperty(PROP_MEMBERS_PACKAGE, in.readObject(), true);
        putProperty(PROP_BODY_COMMENTS, in.readObject(), true);
        putProperty(PROP_BODY_CONTENT, in.readObject(), true);
        putProperty(PROP_JAVADOC, in.readObject(), true);
        putProperty(PROP_CFGCREATE_ENABLED, in.readObject(), true);
        putProperty(PROP_CFGEXEC_ENABLED, in.readObject(), true);
        putProperty(PROP_EXECUTOR_TYPE, in.readObject(), true);
        putProperty(PROP_GENERATE_EXCEPTION_CLASSES, in.readObject(), true);
        putProperty(PROP_TEST_RUNNER, in.readObject(), true);
    }

    public String displayName () {
        return NbBundle.getMessage (JUnitSettings.class, "LBL_junit_settings");
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
        // If you provide context help then use:
        // return new HelpCtx (JUnitSettings.class);
    }

    /** Default instance of this system option, for the convenience of associated classes. */
    public static JUnitSettings getDefault () {
        return (JUnitSettings) findObject (JUnitSettings.class, true);
    }

    public String getFileSystem() {
        return (String) getProperty(PROP_FILE_SYSTEM);
    }
    
    public void setFileSystem(String newVal) {
        putProperty(PROP_FILE_SYSTEM, newVal, true);
    }

    public String getSuiteTemplate() {
        return (String) getProperty(PROP_SUITE_TEMPLATE);
    }

    public void setSuiteTemplate(String newVal) {
        putProperty(PROP_SUITE_TEMPLATE, newVal, true);
    }

    public String getClassTemplate() {
        return (String) getProperty(PROP_CLASS_TEMPLATE);
    }

    public void setClassTemplate(String newVal) {
        putProperty(PROP_CLASS_TEMPLATE, newVal, true);
    }

    public boolean isMembersPublic() {
        return ((Boolean) getProperty(PROP_MEMBERS_PUBLIC)).booleanValue();
    }

    public void setMembersPublic(boolean newVal) {
        putProperty(PROP_MEMBERS_PUBLIC, new Boolean(newVal), true);
    }

    public boolean isMembersProtected() {
        return ((Boolean) getProperty(PROP_MEMBERS_PROTECTED)).booleanValue();
    }

    public void setMembersProtected(boolean newVal) {
        putProperty(PROP_MEMBERS_PROTECTED, new Boolean(newVal), true);
    }

    public boolean isMembersPackage() {
        return ((Boolean) getProperty(PROP_MEMBERS_PACKAGE)).booleanValue();
    }

    public void setMembersPackage(boolean newVal) {
        putProperty(PROP_MEMBERS_PACKAGE, new Boolean(newVal), true);
    }

    public boolean isBodyComments() {
        return ((Boolean) getProperty(PROP_BODY_COMMENTS)).booleanValue();
    }

    public void setBodyComments(boolean newVal) {
        putProperty(PROP_BODY_COMMENTS, new Boolean(newVal), true);
    }

    public boolean isBodyContent() {
        return ((Boolean) getProperty(PROP_BODY_CONTENT)).booleanValue();
    }

    public void setBodyContent(boolean newVal) {
        putProperty(PROP_BODY_CONTENT, new Boolean(newVal), true);
    }

    public boolean isJavaDoc() {
        return ((Boolean) getProperty(PROP_JAVADOC)).booleanValue();
    }

    public void setJavaDoc(boolean newVal) {
        putProperty(PROP_JAVADOC, new Boolean(newVal), true);
    }
   
    public boolean isCfgCreateEnabled() {
        return ((Boolean) getProperty(PROP_CFGCREATE_ENABLED)).booleanValue();
    }
    
    public void setCfgCreateEnabled(boolean newVal) {
        putProperty(PROP_CFGCREATE_ENABLED, new Boolean(newVal), true);
    }

    public boolean isCfgExecEnabled() {
        return ((Boolean) getProperty(PROP_CFGEXEC_ENABLED)).booleanValue();
    }
    
    public void setCfgExecEnabled(boolean newVal) {
        putProperty(PROP_CFGEXEC_ENABLED, new Boolean(newVal), true);
    }

    public int getExecutorType() {
        return ((Integer) getProperty(PROP_EXECUTOR_TYPE)).intValue();
    }
    
    public void setExecutorType(int newVal) {
        putProperty(PROP_EXECUTOR_TYPE, new Integer(newVal), true);
    }

    public boolean isGenerateExceptionClasses() {
        return ((Boolean) getProperty(PROP_GENERATE_EXCEPTION_CLASSES)).booleanValue();
    }

    public void setGenerateExceptionClasses(boolean newVal) {
        putProperty(PROP_GENERATE_EXCEPTION_CLASSES, new Boolean(newVal), true);
    }

    public String getTestRunner() {
        return (String) getProperty(PROP_TEST_RUNNER);
    }

    public void setTestRunner(String newVal) {
        putProperty(PROP_TEST_RUNNER, newVal, true);
    }

    public boolean isGlobal() {
        return false;
    }
}
