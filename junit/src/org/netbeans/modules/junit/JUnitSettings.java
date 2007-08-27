/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.util.prefs.Preferences;
import org.openide.nodes.BeanNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Options for JUnit module, control behavior of test creation and execution.
 *
 * @author  vstejskal
 * @author  Marian Petras
 */
public class JUnitSettings {
    private static final JUnitSettings INSTANCE = new JUnitSettings();
    
    /** prefix for names of generated test classes */
    static final String TEST_CLASSNAME_PREFIX = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_test_classname_prefix");                //NOI18N
    /** suffix for names of generated test classes */
    static final String TEST_CLASSNAME_SUFFIX = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_test_classname_suffix");                //NOI18N
    /** prefix for names of generated test suites */
    static final String SUITE_CLASSNAME_PREFIX = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_suite_classname_prefix");               //NOI18N
    /** suffix for names of generated test suites */
    static final String SUITE_CLASSNAME_SUFFIX = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_suite_classname_suffix");               //NOI18N
    /** should it be possible to create tests for tests? */
    static final boolean GENERATE_TESTS_FROM_TEST_CLASSES = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_generate_tests_from_test_classes").equals("true");    //NOI18N
    /** generate test initializer method by default? */
    static final boolean DEFAULT_GENERATE_SETUP = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_generate_setUp_default").equals("true");              //NOI18N
    /** generate test finalizer method by default? */
    static final boolean DEFAULT_GENERATE_TEARDOWN = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_generate_tearDown_default").equals("true");           //NOI18N
    /** generate test class initializer method by default? */
    static final boolean DEFAULT_GENERATE_CLASS_SETUP = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_generate_class_setUp_default").equals("true");        //NOI18N
    /** generate test class finalizer method by default? */
    static final boolean DEFAULT_GENERATE_CLASS_TEARDOWN = NbBundle.getMessage(
            JUnitSettings.class,
            "PROP_generate_class_tearDown_default").equals("true");     //NOI18N
    /** */
    static final String JUNIT3_GENERATOR = JUnitVersion.JUNIT3.name().toLowerCase();
    /** */
    static final String JUNIT4_GENERATOR = JUnitVersion.JUNIT4.name().toLowerCase();
    /** */
    static final String JUNIT_GENERATOR_ASK_USER = "ask";               //NOI18N
    /** */
    static final String DEFAULT_GENERATOR = JUNIT_GENERATOR_ASK_USER;

    // XXX this property has to go too - will not work any longer, need some src -> test query
    private static final String PROP_FILE_SYSTEM         = "fileSystem";
    public static final String PROP_MEMBERS_PUBLIC      = "membersPublic";
    public static final String PROP_MEMBERS_PROTECTED   = "membersProtected";
    public static final String PROP_MEMBERS_PACKAGE     = "membersPackage";
    public static final String PROP_BODY_COMMENTS       = "bodyComments";
    public static final String PROP_BODY_CONTENT        = "bodyContent";
    public static final String PROP_JAVADOC             = "javaDoc";
    public static final String PROP_GENERATE_EXCEPTION_CLASSES = "generateExceptionClasses";
    public static final String PROP_GENERATE_ABSTRACT_IMPL = "generateAbstractImpl";
    public static final String PROP_GENERATE_SUITE_CLASSES   = "generateSuiteClasses";
    
    public static final String PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES = "includePackagePrivateClasses";
    public static final String PROP_GENERATE_MAIN_METHOD = "generateMainMethod";
    public static final String PROP_GENERATE_MAIN_METHOD_BODY = "generateMainMethodBody";
    public static final String PROP_GENERATE_SETUP      = "generateSetUp";
    public static final String PROP_GENERATE_TEARDOWN   = "generateTearDown";
    public static final String PROP_GENERATE_CLASS_SETUP      = "generateClassSetUp";
    public static final String PROP_GENERATE_CLASS_TEARDOWN   = "generateClassTearDown";
    public static final String PROP_GENERATOR = "generator";
    public static final String PROP_ROOT_SUITE_CLASSNAME = "rootSuiteClassName";                

    public static final String PROP_RESULTS_SPLITPANE_DIVIDER = "resultsSplitDivider";
    
    public String displayName () {
        return NbBundle.getMessage (JUnitSettings.class, "LBL_junit_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(JUnitSettings.class); 
    }

    private  static Preferences getPreferences() {
        return NbPreferences.forModule(JUnitSettings.class);
    }

    /** Default instance of this system option, for the convenience of associated classes. */
    public static JUnitSettings getDefault () {
        return INSTANCE;
    }

    public boolean isMembersPublic() {
        return getPreferences().getBoolean(PROP_MEMBERS_PUBLIC,true);
    }

    public void setMembersPublic(boolean newVal) {
        getPreferences().putBoolean(PROP_MEMBERS_PUBLIC,newVal);
    }

    public boolean isMembersProtected() {
        return getPreferences().getBoolean(PROP_MEMBERS_PROTECTED,true);
    }

    public void setMembersProtected(boolean newVal) {
        getPreferences().putBoolean(PROP_MEMBERS_PROTECTED,newVal);
    }

    public boolean isMembersPackage() {
        return getPreferences().getBoolean(PROP_MEMBERS_PACKAGE,true);
    }

    public void setMembersPackage(boolean newVal) {
        getPreferences().putBoolean(PROP_MEMBERS_PACKAGE,newVal);
    }

    public boolean isBodyComments() {
        return getPreferences().getBoolean(PROP_BODY_COMMENTS,true);
        
    }

    public void setBodyComments(boolean newVal) {
        getPreferences().putBoolean(PROP_BODY_COMMENTS,newVal);
    }

    public boolean isBodyContent() {
        return getPreferences().getBoolean(PROP_BODY_CONTENT,true);
    }

    public void setBodyContent(boolean newVal) {
        getPreferences().putBoolean(PROP_BODY_CONTENT,newVal);
    }

    public boolean isJavaDoc() {
        return getPreferences().getBoolean(PROP_JAVADOC,true);
    }

    public void setJavaDoc(boolean newVal) {
        getPreferences().putBoolean(PROP_JAVADOC,newVal);
    }
   
    public boolean isGenerateExceptionClasses() {
        return getPreferences().getBoolean(PROP_GENERATE_EXCEPTION_CLASSES,true);
    }

    public void setGenerateExceptionClasses(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_EXCEPTION_CLASSES,newVal);
    }
    
   
    public boolean isGenerateAbstractImpl() {
     return getPreferences().getBoolean(PROP_GENERATE_ABSTRACT_IMPL,true);
    }

    public void setGenerateAbstractImpl(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_ABSTRACT_IMPL,newVal);
    }

    public boolean isGenerateSuiteClasses() {
        return getPreferences().getBoolean(PROP_GENERATE_SUITE_CLASSES,true);
    }

    public void setGenerateSuiteClasses(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_SUITE_CLASSES,newVal);
    }

    
    public boolean isIncludePackagePrivateClasses() {
        return getPreferences().getBoolean(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES,true);
    }

    public void setIncludePackagePrivateClasses(boolean newVal) {
        getPreferences().putBoolean(PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES,newVal);
    }    
    
    public boolean isGenerateMainMethod() {
        return getPreferences().getBoolean(PROP_GENERATE_MAIN_METHOD,true);
    }

    public void setGenerateMainMethod(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_MAIN_METHOD,newVal);
    }
    
    public boolean isGenerateSetUp() {
        return getPreferences().getBoolean(PROP_GENERATE_SETUP,
                                           DEFAULT_GENERATE_SETUP);
    }

    public void setGenerateSetUp(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_SETUP,newVal);
    }
    
    public boolean isGenerateTearDown() {
        return getPreferences().getBoolean(PROP_GENERATE_TEARDOWN,
                                           DEFAULT_GENERATE_TEARDOWN);
    }

    public void setGenerateTearDown(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_TEARDOWN,newVal);
    }
    
    public boolean isGenerateClassSetUp() {
        return getPreferences().getBoolean(PROP_GENERATE_CLASS_SETUP,
                                           DEFAULT_GENERATE_CLASS_SETUP);
    }

    public void setGenerateClassSetUp(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_CLASS_SETUP, newVal);
    }
    
    public boolean isGenerateClassTearDown() {
        return getPreferences().getBoolean(PROP_GENERATE_CLASS_TEARDOWN,
                                           DEFAULT_GENERATE_CLASS_TEARDOWN);
    }

    public void setGenerateClassTearDown(boolean newVal) {
        getPreferences().putBoolean(PROP_GENERATE_CLASS_TEARDOWN, newVal);
    }
    
    public String getGenerator() {
        return getPreferences().get(PROP_GENERATOR, DEFAULT_GENERATOR);
    }
    
    public void setGenerator(String generator) {
        getPreferences().put(PROP_GENERATOR, generator);
    }
    
    public String getGenerateMainMethodBody() {
        return getPreferences().get(PROP_GENERATE_MAIN_METHOD_BODY,
                NbBundle.getMessage(JUnitSettings.class, "PROP_generate_main_method_body_default_value"));
    }

    public void setGenerateMainMethodBody(String newVal) {
        getPreferences().put(PROP_GENERATE_MAIN_METHOD_BODY,newVal);
    }
    
    public String getRootSuiteClassName() {        
        return getPreferences().get(PROP_ROOT_SUITE_CLASSNAME,
                NbBundle.getMessage(JUnitSettings.class, "PROP_root_suite_classname_default_value"));
    }

    public void setRootSuiteClassName(String newVal) {
        getPreferences().put(PROP_ROOT_SUITE_CLASSNAME,newVal);
    }    

    public double getResultsSplitPaneDivider() {        
        return getPreferences().getDouble(PROP_RESULTS_SPLITPANE_DIVIDER, 0.5);
    }

    public void setResultsSplitPaneDivider(double newVal) {
        getPreferences().putDouble(PROP_RESULTS_SPLITPANE_DIVIDER, newVal);
    }    
    
    private static BeanNode createViewNode() throws java.beans.IntrospectionException {
        return new BeanNode<JUnitSettings>(JUnitSettings.getDefault());
    }         
}
