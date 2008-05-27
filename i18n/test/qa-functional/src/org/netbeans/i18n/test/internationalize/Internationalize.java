/*
 * Internationalize.java
 *
 * Created on October 29, 2003, 4:06 PM
 */
package org.netbeans.i18n.test.internationalize;

import lib.InternationalizationTestCase;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author  eh103527
 */
public class Internationalize extends InternationalizationTestCase {

    String TEST_PACKAGE = "data";
    String FILE_NAME = "TestFrame";

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
        System.out.println("Pracovni adresar: " + getDataDir());

//        FileSystem[] fileSystems = Repository.getDefault().toArray();
//        String fileSystemName = null;
//
//        for (int ii = 0; ii < fileSystems.length; ii++) {
//            FileObject file = fileSystems[ii].findResource(TEST_PACKAGE);
//            if (file != null) {
//                System.out.println("> Used Filesystem = " + fileSystems[ii].getDisplayName());
//                fileSystemName = fileSystems[ii].getDisplayName();
//            }
//        }
//
//        if (fileSystemName == null) {
//            throw new Exception("Must be mounted .../" + TEST_PACKAGE + " repository in explorer !");
//        }
//
//        String path = fileSystemName.concat("|" + TEST_PACKAGE + "|" + FILE_NAME);
//        new Action("Tools", "Tools|Internationalization|Internationalize").performPopup(new FilesystemNode(path));
//        new EventTool().waitNoEvent(2500);
//        InternationalizeOperator io = new InternationalizeOperator();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
