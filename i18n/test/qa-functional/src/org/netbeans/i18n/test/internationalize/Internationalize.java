/*
 * Internationalize.java
 *
 * Created on October 29, 2003, 4:06 PM
 */
package org.netbeans.i18n.test.internationalize;

import lib.InternationalizationTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author  Jana Maleckova
 */
public class Internationalize extends InternationalizationTestCase {

    String TEST_PACKAGE = "data";
    String FILE_NAME = "TestFrame";
    String TEST_CLASS = "Internationalize";
    String MENU_PATH = "Tools" + TREE_SEPARATOR + "Internationalization" + TREE_SEPARATOR + "Internationalize...";
    // String testName = suite().getClass().toString();
    /**
     * Constructor - Creates new instance of this class
     */
    public Internationalize(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Internationalize("testInternationalize"));
        return suite;
    }

    /**
     * This method contains body of test
     * @return void
     */
    public void testInternationalize() throws Exception {
        System.out.println();
        System.out.println("===================================================================================");
        System.out.println("=   Test :  Internationalize - complex walkthrough                                =");
        System.out.println("=   See testspec of i18n module:                                                  =");
        System.out.println("=   http://beetle.czech/modules/i18n/                                             =");
        System.out.println("===================================================================================");
        System.out.println("Pracovni adresar: " + getDataDir().toString());
        //System.out.println("Golden files " + getGoldenFile().getAbsolutePath());

        //Open project
        openProject(DEFAULT_PROJECT_NAME);

        //Create new Bundle file
        createNewPropertiesFile(getClassNode(DEFAULT_PROJECT_NAME, ROOT_PACKAGE_NAME + TREE_SEPARATOR + DEFAULT_PROJECT_NAME.toLowerCase()), DEFAUL_BUNDLE_NAME);

        //select testing class with strings
        Node testClass = getClassNode(DEFAULT_PROJECT_NAME, ROOT_PACKAGE_NAME + TREE_SEPARATOR + DEFAULT_PROJECT_NAME.toLowerCase() + TREE_SEPARATOR + TEST_CLASS + ".java");
        testClass.select();

        //Open wizard for simple Internationalize
        testClass.callPopup().pushMenuNoBlock(MENU_PATH, TREE_SEPARATOR);

        /*Process of internationalization, all strings from java files 
        should be internationalized
         */
        NbDialogOperator ndo = new NbDialogOperator(TITLE_INTERNATIONALIZE_DIALOG);
        JButtonOperator jbo = new JButtonOperator(ndo, REPLACE_BUTTON);

        //Check if Replace button is enabled
        if (jbo.isEnabled()) {
            while (jbo.isEnabled()) {
                jbo.push();
            }
        } else {
            throw new Exception("Internationalization dialog doesn't found any string in class");

        }

        //Close dialog for internationalization
        JButtonOperator close = new JButtonOperator(ndo, CLOSE_BUTTON);
        close.push();

        //Close and save recently edited files - Bundle, Internationalize.java
        String[] Files = {DEFAUL_BUNDLE_NAME, TEST_CLASS + ".java"};

        for (int i = 0; i < Files.length; i++) {
            closeFileInEditor(Files[i], true);
        }
        
        /*Test if java class was correctly internationalized
         * and bundle file contains all keys with values
        */
        compareBundle(DEFAUL_BUNDLE_NAME);
        compareJavaFile(TEST_CLASS);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
