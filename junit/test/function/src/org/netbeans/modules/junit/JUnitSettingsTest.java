
package org.netbeans.modules.junit;

import org.openide.*;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.io.*;

import junit.framework.*;
import org.openide.*;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileSystem;
import java.io.*;
import java.util.Enumeration;
import junit.framework.*;

public class JUnitSettingsTest extends TestCase {

    public JUnitSettingsTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(JUnitSettingsTest.class);
    }
    
    /** Test of readExternal method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testReadExternal() {
        System.out.println("testReadExternal");
        
        // this test is done togeter with test of writeExternal method
    }
    
    /** Test of writeExternal method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testWriteExternal() throws Exception {
        System.out.println("testWriteExternal");
        JUnitSettings def1 = JUnitSettings.getDefault();
        
        File fTmp = new File(System.getProperty("xdata") + "/JUnitSettings/ser1.test");
        File fSer = new File(System.getProperty("xdata") + "/JUnitSettings/ser1.pass");
        FileInputStream fis;
        ObjectInputStream ois;
        FileOutputStream fos;
        ObjectOutputStream oos;

        // write settings
        fos = new FileOutputStream(fTmp);
        oos = new ObjectOutputStream(fos);
        
        def1.setFileSystem(TST_FILE_SYSTEM_1);
        def1.setSuiteTemplate(TST_SUITE_TEMPLATE_1);
        def1.setClassTemplate(TST_CLASS_TEMPLATE_1);
        def1.setMembersPublic(false);
        def1.setMembersProtected(true);
        def1.setMembersPackage(false);
        def1.setBodyComments(true);
        def1.setBodyContent(false);
        def1.setJavaDoc(true);
        def1.setCfgCreateEnabled(false);
        def1.setCfgExecEnabled(true);
        def1.setExecutorType(JUnitSettings.EXECUTOR_DEBUGGER);
        def1.setGenerateExceptionClasses(false);
        def1.setTestRunner(TST_TEST_RUNNER_1);
        def1.setProperties(TST_PROPERTIES_1);
        
        JUnitSettings.getDefault().writeExternal(oos);
        oos.close();
        assertFile(fTmp, fSer, new File(System.getProperty("xresults")));
        
        // read settings
        fis = new FileInputStream(fSer);
        ois = new ObjectInputStream(fis);
        
        JUnitSettings.getDefault().readExternal(ois);
        assert(def1.getFileSystem().equals(TST_FILE_SYSTEM_1));
        assert(def1.getSuiteTemplate().equals(TST_SUITE_TEMPLATE_1));
        assert(def1.getClassTemplate().equals(TST_CLASS_TEMPLATE_1));
        assert(false == def1.isMembersPublic());
        assert(def1.isMembersProtected());
        assert(false == def1.isMembersPackage());
        assert(def1.isBodyComments());
        assert(false == def1.isBodyContent());
        assert(def1.isJavaDoc());
        assert(false == def1.isCfgCreateEnabled());
        assert(def1.isCfgExecEnabled());
        assert(JUnitSettings.EXECUTOR_DEBUGGER == def1.getExecutorType());
        assert(false == def1.isGenerateExceptionClasses());
        assertEquals(def1.getTestRunner(), TST_TEST_RUNNER_1);
        assertEquals(def1.getProperties(), TST_PROPERTIES_1);

        fTmp = new File(System.getProperty("xdata") + "/JUnitSettings/ser2.test");
        fSer = new File(System.getProperty("xdata") + "/JUnitSettings/ser2.pass");
        
        // write settings
        fos = new FileOutputStream(fTmp);
        oos = new ObjectOutputStream(fos);

        def1.setFileSystem(TST_FILE_SYSTEM_2);
        def1.setSuiteTemplate(TST_SUITE_TEMPLATE_2);
        def1.setClassTemplate(TST_CLASS_TEMPLATE_2);
        def1.setMembersPublic(true);
        def1.setMembersProtected(false);
        def1.setMembersPackage(true);
        def1.setBodyComments(false);
        def1.setBodyContent(true);
        def1.setJavaDoc(false);
        def1.setCfgCreateEnabled(true);
        def1.setCfgExecEnabled(false);
        def1.setExecutorType(JUnitSettings.EXECUTOR_INTERNAL);
        def1.setGenerateExceptionClasses(true);
        def1.setTestRunner(TST_TEST_RUNNER_2);
        def1.setProperties(TST_PROPERTIES_2);
        
        JUnitSettings.getDefault().writeExternal(oos);
        
        // read settings
        fis = new FileInputStream(fSer);
        ois = new ObjectInputStream(fis);

        JUnitSettings.getDefault().readExternal(ois);
        assert(def1.getFileSystem().equals(TST_FILE_SYSTEM_2));
        assert(def1.getSuiteTemplate().equals(TST_SUITE_TEMPLATE_2));
        assert(def1.getClassTemplate().equals(TST_CLASS_TEMPLATE_2));
        assert(def1.isMembersPublic());
        assert(false == def1.isMembersProtected());
        assert(def1.isMembersPackage());
        assert(false == def1.isBodyComments());
        assert(def1.isBodyContent());
        assert(false == def1.isJavaDoc());
        assert(def1.isCfgCreateEnabled());
        assert(false == def1.isCfgExecEnabled());
        assert(JUnitSettings.EXECUTOR_INTERNAL == def1.getExecutorType());
        assert(def1.isGenerateExceptionClasses());
        assertEquals(def1.getTestRunner(), TST_TEST_RUNNER_2);
        assertEquals(def1.getProperties(), TST_PROPERTIES_2);
        
        oos.close();
        assertFile(fTmp, fSer, new File(System.getProperty("xresults")));
    }
    
    /** Test of displayName method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testDisplayName() {
        System.out.println("testDisplayName");
        assert(null != JUnitSettings.getDefault().displayName());
    }
    
    /** Test of getHelpCtx method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetHelpCtx() {
        System.out.println("testGetHelpCtx");
        // Add your test code here.
    }
    
    /** Test of getDefault method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetDefault() {
        System.out.println("testGetDefault");
        JUnitSettings def1 = JUnitSettings.getDefault();
        JUnitSettings def2 = JUnitSettings.getDefault();
        
        assert(null != def1);   // is it creatable
        assert(def1 == def2);   // as a singelton class
        
        def1.initialize();  // force default values to be set
        
        // test default values
        assert(def1.getFileSystem().equals(""));
        assert(def1.getSuiteTemplate().equals("Templates/JUnit/SimpleTest.java"));
        assert(def1.getClassTemplate().equals("Templates/JUnit/SimpleTest.java"));
        assert(def1.isMembersPublic());
        assert(def1.isMembersProtected());
        assert(def1.isMembersPackage());
        assert(def1.isBodyComments());
        assert(def1.isBodyContent());
        assert(def1.isJavaDoc());
        assert(def1.isCfgCreateEnabled());
        assert(def1.isCfgExecEnabled());
        assert(JUnitSettings.EXECUTOR_EXTERNAL == def1.getExecutorType());
    }
    
    /** Test of getFileSystem method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetFileSystem() {
        System.out.println("testGetFileSystem");
        JUnitSettings.getDefault().setFileSystem(TST_FILE_SYSTEM_1);
        assert(JUnitSettings.getDefault().getFileSystem().equals(TST_FILE_SYSTEM_1));
        JUnitSettings.getDefault().setFileSystem(TST_FILE_SYSTEM_2);
        assert(JUnitSettings.getDefault().getFileSystem().equals(TST_FILE_SYSTEM_2));
    }

    /** Test of setFileSystem method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetFileSystem() {
        System.out.println("testSetFileSystem");
        // this test is done in test of getFileSystem
    }
    
    /** Test of getSuiteTemplate method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetSuiteTemplate() {
        System.out.println("testGetSuiteTemplate");
        JUnitSettings.getDefault().setSuiteTemplate(TST_SUITE_TEMPLATE_1);
        assert(JUnitSettings.getDefault().getSuiteTemplate().equals(TST_SUITE_TEMPLATE_1));
        JUnitSettings.getDefault().setSuiteTemplate(TST_SUITE_TEMPLATE_2);
        assert(JUnitSettings.getDefault().getSuiteTemplate().equals(TST_SUITE_TEMPLATE_2));
    }

    /** Test of setSuiteTemplate method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetSuiteTemplate() {
        System.out.println("testSetSuiteTemplate");
        // this test is done in test of getSuiteTemplate
    }
    
    /** Test of getClassTemplate method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetClassTemplate() {
        System.out.println("testGetClassTemplate");
        JUnitSettings.getDefault().setClassTemplate(TST_CLASS_TEMPLATE_1);
        assert(JUnitSettings.getDefault().getClassTemplate().equals(TST_CLASS_TEMPLATE_1));
        JUnitSettings.getDefault().setClassTemplate(TST_CLASS_TEMPLATE_2);
        assert(JUnitSettings.getDefault().getClassTemplate().equals(TST_CLASS_TEMPLATE_2));
    }

    /** Test of setClassTemplate method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetClassTemplate() {
        System.out.println("testSetClassTemplate");
        // this test is done in test of getCLassTemplate
    }
    
    /** Test of isMembersPublic method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsMembersPublic() {
        System.out.println("testIsMembersPublic");
        JUnitSettings.getDefault().setMembersPublic(true);
        assert(JUnitSettings.getDefault().isMembersPublic());
        JUnitSettings.getDefault().setMembersPublic(false);
        assert(false == JUnitSettings.getDefault().isMembersPublic());
    }

    /** Test of setMembersPublic method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetMembersPublic() {
        System.out.println("testSetMembersPublic");
        // this test is done in test of isMembersPublic
    }
    
    /** Test of isMembersProtected method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsMembersProtected() {
        System.out.println("testIsMembersProtected");
        JUnitSettings.getDefault().setMembersProtected(true);
        assert(JUnitSettings.getDefault().isMembersProtected());
        JUnitSettings.getDefault().setMembersProtected(false);
        assert(false == JUnitSettings.getDefault().isMembersProtected());
    }

    /** Test of setMembersProtected method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetMembersProtected() {
        System.out.println("testSetMembersProtected");
        // this test is done in test of isMembersProtected
    }
    
    /** Test of isMembersPackage method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsMembersPackage() {
        System.out.println("testIsMembersPackage");
        JUnitSettings.getDefault().setMembersPackage(true);
        assert(JUnitSettings.getDefault().isMembersPackage());
        JUnitSettings.getDefault().setMembersPackage(false);
        assert(false == JUnitSettings.getDefault().isMembersPackage());
    }

    /** Test of setMembersPackage method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetMembersPackage() {
        System.out.println("testSetMembersPackage");
        // this test is done in test of isMembersPackage
    }
    
    /** Test of isBodyComments method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsBodyComments() {
        System.out.println("testIsBodyComments");
        JUnitSettings.getDefault().setBodyComments(true);
        assert(JUnitSettings.getDefault().isBodyComments());
        JUnitSettings.getDefault().setBodyComments(false);
        assert(false == JUnitSettings.getDefault().isBodyComments());
    }

    /** Test of setBodyComments method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetBodyComments() {
        System.out.println("testSetBodyComments");
        // this test is done in test of isBodyComments
    }
    
    /** Test of isBodyContent method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsBodyContent() {
        System.out.println("testIsBodyContent");
        JUnitSettings.getDefault().setBodyContent(true);
        assert(JUnitSettings.getDefault().isBodyContent());
        JUnitSettings.getDefault().setBodyContent(false);
        assert(false == JUnitSettings.getDefault().isBodyContent());
    }
    
    /** Test of setBodyContent method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetBodyContent() {
        System.out.println("testSetBodyContent");
        // this test is done in test of isBodyContent
    }
    
    /** Test of isJavaDoc method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsJavaDoc() {
        System.out.println("testIsJavaDoc");
        JUnitSettings.getDefault().setJavaDoc(true);
        assert(JUnitSettings.getDefault().isJavaDoc());
        JUnitSettings.getDefault().setJavaDoc(false);
        assert(false == JUnitSettings.getDefault().isJavaDoc());
    }
    
    /** Test of setJavaDoc method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetJavaDoc() {
        System.out.println("testSetJavaDoc");
        // this test is done in test of isJavaDoc
    }

    /** Test of isCfgCreateEnabled method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsCfgCreateEnabled() {
        System.out.println("testIsCfgCreateEnabled");
        JUnitSettings.getDefault().setCfgCreateEnabled(true);
        assert(JUnitSettings.getDefault().isCfgCreateEnabled());
        JUnitSettings.getDefault().setCfgCreateEnabled(false);
        assert(false == JUnitSettings.getDefault().isCfgCreateEnabled());
    }
    
    /** Test of setCfgCreateEnabled method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetCfgCreateEnabled() {
        System.out.println("testSetCfgCreateEnabled");
        // this test is done in test of isCfgCreateEnabled
    }

    /** Test of isCfgExecEnabled method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsCfgExecEnabled() {
        System.out.println("testIsCfgExecEnabled");
        JUnitSettings.getDefault().setCfgExecEnabled(true);
        assert(JUnitSettings.getDefault().isCfgExecEnabled());
        JUnitSettings.getDefault().setCfgExecEnabled(false);
        assert(false == JUnitSettings.getDefault().isCfgExecEnabled());
    }
    
    /** Test of setCfgExecEnabled method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetCfgExecEnabled() {
        System.out.println("testSetCfgExecEnabled");
        // this test is done in test of isCfgExecEnabled
    }
    
    /** Test of getExecutorType method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetExecutorType() {
        System.out.println("testGetExecutorType");
        JUnitSettings.getDefault().setExecutorType(JUnitSettings.EXECUTOR_INTERNAL);
        assert(JUnitSettings.EXECUTOR_INTERNAL == JUnitSettings.getDefault().getExecutorType());
        JUnitSettings.getDefault().setExecutorType(JUnitSettings.EXECUTOR_DEBUGGER);
        assert(JUnitSettings.EXECUTOR_DEBUGGER == JUnitSettings.getDefault().getExecutorType());
    }
    
    /** Test of setExecutorType method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetExecutorType() {
        System.out.println("testSetExecutorType");
        // this test is done in test of getExecutorType
    }
    
    /** Test of isGenerateExceptionClasses method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsGenerateExceptionClasses() {
        System.out.println("testIsGenerateExceptionClasses");
        JUnitSettings.getDefault().setGenerateExceptionClasses(true);
        assert(JUnitSettings.getDefault().isGenerateExceptionClasses());
        JUnitSettings.getDefault().setGenerateExceptionClasses(false);
        assert(false == JUnitSettings.getDefault().isGenerateExceptionClasses());
    }
    
    /** Test of setGenerateExceptionClasses method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetGenerateExceptionClasses() {
        System.out.println("testSetGenerateExceptionClasses");
        // this test is done in test of isGenerateExceptionClasses
    }
    
    /** Test of getTestRunner method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetTestRunner() {
        System.out.println("testGetTestRunner");
        JUnitSettings.getDefault().setTestRunner(TST_TEST_RUNNER_1);
        assertEquals(JUnitSettings.getDefault().getTestRunner(), TST_TEST_RUNNER_1);
        JUnitSettings.getDefault().setTestRunner(TST_TEST_RUNNER_2);
        assertEquals(JUnitSettings.getDefault().getTestRunner(), TST_TEST_RUNNER_2);
    }
    
    /** Test of setTestRunner method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetTestRunner() {
        System.out.println("testSetTestRunner");
        // this test is done in test of getTestRunner
    }

    /** Test of getProperties method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testGetProperties() {
        System.out.println("testGetProperties");
        JUnitSettings.getDefault().setProperties(TST_PROPERTIES_1);
        assertEquals(JUnitSettings.getDefault().getProperties(), TST_PROPERTIES_1);
        JUnitSettings.getDefault().setProperties(TST_PROPERTIES_2);
        assertEquals(JUnitSettings.getDefault().getProperties(), TST_PROPERTIES_2);
    }
    
    /** Test of setProperties method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testSetProperties() {
        System.out.println("testSetProperties");
        // this test is done in test of getProperties
    }

    /** Test of isGlobal method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testIsGlobal() {
        System.out.println("testIsGlobal");
        assert(false == JUnitSettings.getDefault().isGlobal());
    }
    
    /** Test of initialize method, of class org.netbeans.modules.junit.JUnitSettings. */
    public void testInitialize() {
        System.out.println("testInitialize");
        // this test is done in test of testGetDefault
    }

    // protected members
    protected File dump = new File(System.getProperty("xdata") + "/JUnitSettings/settings.dump");
    
    protected void setUp() {
        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            // write settings
            fos = new FileOutputStream(dump);
            oos = new ObjectOutputStream(fos);

            JUnitSettings.getDefault().writeExternal(oos);
            oos.close();
        }
        catch (IOException e) {
            System.out.println("JUnitSettingsTest.setUp has failed " + e.getMessage());
        }
    }
    
    protected void tearDown() {
        FileInputStream fis;
        ObjectInputStream ois;

        try {
            // read settings
            fis = new FileInputStream(dump);
            ois = new ObjectInputStream(fis);

            JUnitSettings.getDefault().readExternal(ois);
            ois.close();
        }
        catch (Exception e) {
            System.out.println("JUnitSettingsTest.tearDown has failed " + e.getMessage());
        }
    }
    
    // private members
    private static final String TST_FILE_SYSTEM_1 = "MyTestFileSystem";
    private static final String TST_SUITE_TEMPLATE_1 = "My/Test.Suite\\Template 1";
    private static final String TST_CLASS_TEMPLATE_1 = "My/Test Class\\Template.1";
    private static final String TST_TEST_RUNNER_1 = "test.of.test.runner.property";
    private static final String TST_PROPERTIES_1 = "prop1=val1\nprop2=val2\n";

    private static final String TST_FILE_SYSTEM_2 = "MyTestFileSystem";
    private static final String TST_SUITE_TEMPLATE_2 = "My/Test.Suite\\Template 1";
    private static final String TST_CLASS_TEMPLATE_2 = "My/Test Class\\Template.1";
    private static final String TST_TEST_RUNNER_2 = "test.of.test.runner.property";
    private static final String TST_PROPERTIES_2 = "# comment\n\n";
}
